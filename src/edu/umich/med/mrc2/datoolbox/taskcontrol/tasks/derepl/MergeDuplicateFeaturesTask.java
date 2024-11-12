/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates.DuplicatesCleanupOptions;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class MergeDuplicateFeaturesTask extends AbstractTask {

	private DataPipeline activeDataPipeline;
	private DataAnalysisProject currentExperiment;
	private Set<MsFeatureCluster> duplicateList;
	private DuplicatesCleanupOptions mergeOption;
	private Matrix msFeatureMatrix;

	public MergeDuplicateFeaturesTask(
			DataAnalysisProject experiment,
			DataPipeline activeDataPipeline,
			DuplicatesCleanupOptions duplicatesCleanupOptions) {
		
		this.activeDataPipeline = activeDataPipeline;
		this.currentExperiment = experiment;
		this.mergeOption = duplicatesCleanupOptions;
		duplicateList = 
				experiment.getDuplicateClustersForDataPipeline(activeDataPipeline);
	}

	public MergeDuplicateFeaturesTask(
			Collection<MsFeature>featuresToMerge,
			DataAnalysisProject experiment,
			DataPipeline activeDataPipeline,
			DuplicatesCleanupOptions duplicatesCleanupOptions) {

		this.activeDataPipeline = activeDataPipeline;
		this.currentExperiment = experiment;
		this.mergeOption = duplicatesCleanupOptions;

		MsFeatureCluster mergeCluster = new MsFeatureCluster();
		featuresToMerge.stream().
			forEach(f -> mergeCluster.addFeature(f, activeDataPipeline));

		duplicateList = new HashSet<MsFeatureCluster>();
		duplicateList.add(mergeCluster);
	}

	@Override
	public void run() {
		
		if (duplicateList.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		setStatus(TaskStatus.PROCESSING);
		try {
			readFeatureMatrix();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			removeDuplicateFeatures();
			setStatus(TaskStatus.FINISHED);
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void readFeatureMatrix() {

		taskDescription = "Reading MS feature matrix data";
		processed = 30;
		
		msFeatureMatrix = 
				ExperimentUtils.readFeatureMatrix(currentExperiment, activeDataPipeline);
		
//		File featureMatrixFile = 
//				Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(), 
//				currentExperiment.getFeatureMatrixFileNameForDataPipeline(activeDataPipeline)).toFile();
//		if (!featureMatrixFile.exists())
//			return;
//		
//		msFeatureMatrix = null;
//		if (featureMatrixFile.exists()) {
//			try {
//				msFeatureMatrix = Matrix.Factory.load(featureMatrixFile);
//			} catch (ClassNotFoundException | IOException e) {
//				e.printStackTrace();
//				setStatus(TaskStatus.ERROR);
//				return;				
//			}
//			if (msFeatureMatrix != null) {
//
//				msFeatureMatrix.setMetaDataDimensionMatrix(0, 
//						currentExperiment.getMetaDataMatrixForDataPipeline(activeDataPipeline, 0));
//				msFeatureMatrix.setMetaDataDimensionMatrix(1, 
//						currentExperiment.getMetaDataMatrixForDataPipeline(activeDataPipeline, 1));
//			}
//		}		
	}
	
	public void removeDuplicateFeatures() {
		
		taskDescription = "Merging duplicate feature data for active data pipeline";
		total = duplicateList.size();
		processed = 0;
		Matrix dataMatrix = 
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
		Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);
		ArrayList<MsFeature> featuresToRemove = new ArrayList<MsFeature>();
		ArrayList<MsFeature> featuresToMerge = new ArrayList<MsFeature>();
		TreeSet<Long>secondaryFeatureCoorinates = new TreeSet<Long>();
		DataAcquisitionMethod acquisitionMethod = activeDataPipeline.getAcquisitionMethod();		
		TreeSet<DataFile> dataFiles = currentExperiment.getExperimentDesign().getSamples()
			.stream().flatMap(s -> s.getDataFilesForMethod(acquisitionMethod).stream())
			.collect(Collectors.toCollection(TreeSet::new));
				
		long[] coordinates = new long[2];
		long[] coordinatesForReplacement = new long[2];
		long primaryFeatureCoordinate = -1;
		long bestFit = -1;
		double area, topArea, median, delta, diff, replacementValue;
		
		for (MsFeatureCluster fclust : duplicateList) {
			
			featuresToMerge.clear();
			secondaryFeatureCoorinates.clear();
			MsFeature primFeature = fclust.getPrimaryFeature();
			primaryFeatureCoordinate = dataMatrix.getColumnForLabel(primFeature);
			fclust.getFeatureMap().get(activeDataPipeline)
				.stream().filter(f -> !f.equals(primFeature))
				.forEach(f -> {
					featuresToRemove.add(f);
					featuresToMerge.add(f);
					secondaryFeatureCoorinates.add(dataMatrix.getColumnForLabel(f));
				});
			
			for(DataFile df : dataFiles) {
				
				coordinates[0] = dataMatrix.getRowForLabel(df);
				coordinatesForReplacement[0] = dataMatrix.getRowForLabel(df);				
				coordinates[1] = primaryFeatureCoordinate;				
				if (mergeOption.equals(DuplicatesCleanupOptions.USE_HIGHEST_AREA)) {
					
				}
				if (mergeOption.equals(DuplicatesCleanupOptions.USE_PRIMARY_AND_FILL_MISSING)) {
					
					double primaryValue = dataMatrix.getAsDouble(coordinates);
					double newValue = 0.0d;
					if(primaryValue <= 0.0d) {
						
						if(secondaryFeatureCoorinates.size() == 1) {
							
							coordinatesForReplacement[1] = secondaryFeatureCoorinates.iterator().next();
							newValue = dataMatrix.getAsDouble(coordinatesForReplacement);
						}
						if(secondaryFeatureCoorinates.size() > 1) {
							
							median = primFeature.getStatsSummary().getSampleMedian();
							delta = 1.5E20;
							bestFit = -1;
							replacementValue = 0.0d;

							for(long sc : secondaryFeatureCoorinates) {
								
								coordinatesForReplacement[1] = sc;
								replacementValue = dataMatrix.getAsDouble(coordinatesForReplacement);
								diff = Math.abs(median - replacementValue);
								if(diff < delta) {
									delta = diff;
									newValue = replacementValue;
								}
							}
						}						
						dataMatrix.setAsDouble(newValue, coordinates);						
					}
				}
			}
			processed++;
		}
		removeRedundantFeatures(featuresToRemove);
	}
	
	private void removeRedundantFeatures(Collection<MsFeature>featuresToRemove) {
		
		taskDescription = "Removing duplicate features from data matrices";
		total = 100;
		processed = 20;
		
		Matrix dataMatrix = 
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
		
		ArrayList<Long> rem = new ArrayList<Long>();
		for (MsFeature cf : featuresToRemove)
			rem.add(dataMatrix.getColumnForLabel(cf));

		currentExperiment.deleteFeatures(featuresToRemove, activeDataPipeline);
		Matrix newFeatureMatrix = 
				dataMatrix.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, rem);
		Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
		newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMatrix);
		newDataMatrix.setMetaDataDimensionMatrix(1, dataMatrix.getMetaDataDimensionMatrix(1));
		currentExperiment.setDataMatrixForDataPipeline(activeDataPipeline, newDataMatrix);
		
		if(msFeatureMatrix != null) {
			
			Matrix newMsFeatureLabelMatrix = 
					msFeatureMatrix.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, rem);			
			Matrix newMsFeatureMatrix = msFeatureMatrix.deleteColumns(Ret.NEW, rem);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(0, newMsFeatureLabelMatrix);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(1, msFeatureMatrix.getMetaDataDimensionMatrix(1));
			
			//	TODO - there is no undo for it here, if the experiment is not saved
			//	Same for areas matrix?? they need backup for this case !!!
			//	Maybe for now force save the experiment and warn that there is no UNDO
			
			saveMsFeatureMatrix(newMsFeatureMatrix);
			
			//			System.err.println("Data: " + newDataMatrix.getRowCount() + 
			//					" by " + newDataMatrix.getColumnCount());
			//			System.err.println("Features: " + newMsFeatureMatrix.getRowCount() + 
			//					" by " + newMsFeatureMatrix.getColumnCount());
		}
		currentExperiment.setDuplicateClustersForDataPipeline(
				activeDataPipeline, new HashSet<MsFeatureCluster>());
	}

	public void removeDuplicateFeaturesUsingHighestAreaFeature() {

		
		total = duplicateList.size();
		processed = 0;
		Matrix dataMatrix = 
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
		Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);
		ArrayList<MsFeature> featuresToRemove = new ArrayList<MsFeature>();
		ArrayList<MsFeature> featuresToMerge = new ArrayList<MsFeature>();
		DataAcquisitionMethod acquisitionMethod = activeDataPipeline.getAcquisitionMethod();

		// Swap ID and data source feature where necessary to get better quality data
		MsFeature idFeature = null;
		MsFeature dataFeature = null;
		MsFeature labelFeature = null;

		int idfId = 0;
		int dfId = 0;
		long[] coordinates = new long[2];
		double area, topArea;

		for (MsFeatureCluster fclust : duplicateList) {

			coordinates[0] = 0;
			idFeature = null;
			featuresToMerge.clear();

			for (MsFeature cf : fclust.getFeatures()) {

				if (cf.equals(fclust.getPrimaryFeature())) {
					idFeature = cf;
				}
				else {
					featuresToRemove.add(cf);
					featuresToMerge.add(cf);
				}
				if (cf.isActive())
					dataFeature = cf;
			}
			// Swap feature coordinates
			if (!idFeature.equals(dataFeature)) {

				for (int i = 0; i < featureMatrix.getColumnCount(); i++) {

					coordinates[1] = i;
					labelFeature = (MsFeature) featureMatrix.getAsObject(coordinates);

					if (labelFeature.equals(idFeature))
						idfId = i;

					if (labelFeature.equals(dataFeature))
						dfId = i;
				}
				swapFeatureStats(idFeature, dataFeature);

				coordinates[1] = dfId;
				featureMatrix.setAsObject(idFeature, coordinates);

				coordinates[1] = idfId;
				featureMatrix.setAsObject(dataFeature, coordinates);
			}
			// Set sample values to top area among all features if the option is
			// selected
			if (mergeOption.equals(DuplicatesCleanupOptions.USE_HIGHEST_AREA)) {

				long idColumn = dataMatrix.getColumnForLabel(idFeature);
				coordinates[1] = idColumn;
				for (ExperimentalSample sample : currentExperiment.getExperimentDesign().getSamples()) {

					if (sample.getDataFilesForMethod(acquisitionMethod) != null) {

						for (DataFile df : sample.getDataFilesForMethod(acquisitionMethod)) {

							coordinates[0] = dataMatrix.getRowForLabel(df);

							if (coordinates[0] > -1) {
								topArea = dataMatrix.getAsDouble(coordinates);

								for (MsFeature msf : featuresToMerge) {

									coordinates[1] = dataMatrix.getColumnForLabel(msf);
									area = dataMatrix.getAsDouble(coordinates);
									if (area > topArea)
										topArea = area;
								}
								coordinates[1] = idColumn;
								dataMatrix.setAsDouble(topArea, coordinates);
							}
						}
					}
				}
			}
			processed++;
		}
		taskDescription = "Removing duplicate features from data matrices";
		total = 100;
		processed = 20;
		
		ArrayList<Long> rem = new ArrayList<Long>();
		for (MsFeature cf : featuresToRemove)
			rem.add(dataMatrix.getColumnForLabel(cf));

		currentExperiment.deleteFeatures(featuresToRemove, activeDataPipeline);
		Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
		Matrix newFeatureMatrix = featureMatrix.deleteColumns(Ret.NEW, rem);

		newDataMatrix.setMetaDataDimensionMatrix(
				0, newFeatureMatrix);
		newDataMatrix.setMetaDataDimensionMatrix(
				1, dataMatrix.getMetaDataDimensionMatrix(1));
		
		if(msFeatureMatrix != null) {
			
			Matrix newMsFeatureMatrix = msFeatureMatrix.deleteColumns(Ret.NEW, rem);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(
					0, newFeatureMatrix);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(
					1, dataMatrix.getMetaDataDimensionMatrix(1));
			
			//	TODO - there is no undo for it here, if the experiment is not saved
			//	Same for areas matrix?? they need backup for this case !!!
			//	Maybe for now force save the experiment and warn that there is no UNDO
			saveMsFeatureMatrix(newMsFeatureMatrix);
			
			System.err.println("Data: " + newDataMatrix.getRowCount() + 
					" by " + newDataMatrix.getColumnCount());
			System.err.println("Features: " + newMsFeatureMatrix.getRowCount() + 
					" by " + newMsFeatureMatrix.getColumnCount());
		}
		currentExperiment.setDataMatrixForDataPipeline(
				activeDataPipeline, newDataMatrix);
		duplicateList = new HashSet<MsFeatureCluster>();
		currentExperiment.setDuplicateClustersForDataPipeline(
				activeDataPipeline, duplicateList);
	}
	
	private void saveMsFeatureMatrix(Matrix msFeatureMatrix) {
		
		taskDescription = 
				"Saving feature matrix for  " + currentExperiment.getName() +
				"(" + currentExperiment.getName() + ")";
		processed = 70;
		ExperimentUtils.saveFeatureMatrixToFile(
				msFeatureMatrix,
				currentExperiment, 
				activeDataPipeline);
		currentExperiment.setFeatureMatrixForDataPipeline(activeDataPipeline, null);
		msFeatureMatrix = null;
		System.gc();
		processed = 100;
		
//		if(featureMatrixFileName != null) {
//			
//			taskDescription = "Saving feature matrix for  " + currentExperiment.getName() +
//					"(" + currentExperiment.getName() + ")";
//			processed = 90;
//			File featureMatrixFile = 
//					Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(), 
//					featureMatrixFileName).toFile();
//			try {
//				Matrix featureMatrix = 
//						Matrix.Factory.linkToArray(msFeatureMatrix.toObjectArray());
//				featureMatrix.save(featureMatrixFile);
//				processed = 100;
//			} catch (IOException e) {
//				e.printStackTrace();
//				// setStatus(TaskStatus.ERROR);
//				return;
//			}
//			msFeatureMatrix = null;
//			System.gc();
//		}				
	}

	private void swapFeatureStats(MsFeature featureOne, MsFeature featureTwo) {

		MsFeatureStatisticalSummary sumOne = 
				new MsFeatureStatisticalSummary(featureOne.getStatsSummary());
		MsFeatureStatisticalSummary sumTwo = 
				new MsFeatureStatisticalSummary(featureTwo.getStatsSummary());

		featureOne.setStatsSummary(sumTwo);
		featureTwo.setStatsSummary(sumOne);
	}

	@Override
	public Task cloneTask() {
		return new MergeDuplicateFeaturesTask(
				currentExperiment, activeDataPipeline, mergeOption);
	}

	public DataPipeline getDataPipeline() {
		return activeDataPipeline;
	}
}
