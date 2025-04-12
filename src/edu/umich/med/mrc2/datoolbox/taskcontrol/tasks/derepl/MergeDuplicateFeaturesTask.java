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

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
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

	
	public void removeDuplicateFeatures() {
		
		if (mergeOption.equals(DuplicatesCleanupOptions.USE_HIGHEST_AREA))
			removeDuplicateFeaturesUsingHighestAreaFeature();
		
		if (mergeOption.equals(DuplicatesCleanupOptions.USE_PRIMARY_AND_FILL_MISSING)) 
			removeDuplicateFeaturesFillingMissingValues();
	}	
	
	private void removeDuplicateFeaturesFillingMissingValues() {

		taskDescription = "Merging duplicate features filling missing values in primary";
		total = duplicateList.size();
		processed = 0;
		Matrix dataMatrix = 
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
		ArrayList<MsFeature> featuresToRemove = new ArrayList<MsFeature>();
		ArrayList<MsFeature> featuresToMerge = new ArrayList<MsFeature>();
		TreeSet<Long>secondaryFeatureCoorinates = new TreeSet<Long>();	
		Set<DataFile> dataFiles = 
				currentExperiment.getDataFilesForAcquisitionMethod(
						activeDataPipeline.getAcquisitionMethod());
				
		long[] primaryFeatureCoordinates = new long[2];
		long[] replacementFeatureCoordinates = new long[2];
		long replacementFeaturePosition = -1;
		double median, delta, diff, replacementValue;
				
		for (MsFeatureCluster fclust : duplicateList) {
			
			featuresToMerge.clear();
			secondaryFeatureCoorinates.clear();
			MsFeature primFeature = fclust.getPrimaryFeature();
			primaryFeatureCoordinates[1] = dataMatrix.getColumnForLabel(primFeature);
			fclust.getFeatureMap().get(activeDataPipeline)
				.stream().filter(f -> !f.equals(primFeature))
				.forEach(f -> {
					featuresToRemove.add(f);
					featuresToMerge.add(f);
					secondaryFeatureCoorinates.add(dataMatrix.getColumnForLabel(f));
				});
			
			for(DataFile df : dataFiles) {
				
				primaryFeatureCoordinates[0] = dataMatrix.getRowForLabel(df);
				double primaryValue = dataMatrix.getAsDouble(primaryFeatureCoordinates);
				if(primaryValue > 0.0d)
					continue;
				
				replacementFeatureCoordinates[0] = primaryFeatureCoordinates[0];				
				double newValue = 0.0d;
				if(secondaryFeatureCoorinates.size() == 1) {
					
					replacementFeatureCoordinates[1] = secondaryFeatureCoorinates.iterator().next();
					newValue = dataMatrix.getAsDouble(replacementFeatureCoordinates);
				}
				if(secondaryFeatureCoorinates.size() > 1) {
					
					median = primFeature.getStatsSummary().getSampleMedian();
					delta = 1.5E20;
					replacementValue = 0.0d;
					replacementFeaturePosition = -1;

					for(long sc : secondaryFeatureCoorinates) {
						
						replacementFeatureCoordinates[1] = sc;
						replacementValue = dataMatrix.getAsDouble(replacementFeatureCoordinates);
						if(replacementValue > 0.0d) {
							
							diff = Math.abs(median - replacementValue);
							if(diff < delta) {
								delta = diff;
								newValue = replacementValue;
								replacementFeaturePosition = sc;
							}
						}
					}
				}	
				if(newValue > 0 && replacementFeaturePosition >=0) {
					
					dataMatrix.setAsDouble(newValue, primaryFeatureCoordinates);
					replacementFeatureCoordinates[1] = replacementFeaturePosition;
					SimpleMsFeature replacementSmf = 
							(SimpleMsFeature)msFeatureMatrix.getAsObject(replacementFeatureCoordinates);
					msFeatureMatrix.setAsObject(replacementSmf, primaryFeatureCoordinates);
				}
			}
			processed++;
		}
		removeRedundantFeatures(featuresToRemove);
	}

	public void removeDuplicateFeaturesUsingHighestAreaFeature() {
		
		taskDescription = "Merging duplicate features using largest peak area";
		total = duplicateList.size();
		processed = 0;
		Matrix dataMatrix = 
				currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
		ArrayList<MsFeature> featuresToRemove = new ArrayList<MsFeature>();
		ArrayList<MsFeature> featuresToMerge = new ArrayList<MsFeature>();
		DataAcquisitionMethod acquisitionMethod = activeDataPipeline.getAcquisitionMethod();
		MsFeature dataFeature = null;
		long[] coordinates = new long[2];
		long[] replacementFeatureCoordinates = new long[2];
		double area, topArea;

		for (MsFeatureCluster fclust : duplicateList) {

			coordinates[0] = 0;
			featuresToMerge.clear();
			for (MsFeature cf : fclust.getFeatures()) {

				if (cf.equals(fclust.getPrimaryFeature())) {
					dataFeature = cf;
				}
				else {
					featuresToRemove.add(cf);
					featuresToMerge.add(cf);
				}
			}
			for (DataFile df : currentExperiment.getDataFilesForAcquisitionMethod(acquisitionMethod)) {

				long dfIndex = dataMatrix.getRowForLabel(df);
				if (dfIndex < 0)
					continue;
				
				coordinates[0] = dfIndex;
				replacementFeatureCoordinates[0] = dfIndex;
				boolean replace = false;					
				coordinates[1] = dataMatrix.getColumnForLabel(dataFeature);
				topArea = dataMatrix.getAsDouble(coordinates);

				for (MsFeature msf : featuresToMerge) {

					coordinates[1] = dataMatrix.getColumnForLabel(msf);
					area = dataMatrix.getAsDouble(coordinates);
					if (area > topArea) {
						topArea = area;
						replacementFeatureCoordinates[1] = coordinates[1];
						replace = true;
					}
				}
				if(replace) {
					
					coordinates[1] = dataMatrix.getColumnForLabel(dataFeature);
					dataMatrix.setAsDouble(topArea, coordinates);
					
					SimpleMsFeature replacementSmf = 
							(SimpleMsFeature)msFeatureMatrix.getAsObject(replacementFeatureCoordinates);
					msFeatureMatrix.setAsObject(replacementSmf, coordinates);
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
		
		processed = 40;
		
		if(msFeatureMatrix != null) {
			
			Matrix newMsFeatureLabelMatrix = 
					msFeatureMatrix.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, rem);			
			Matrix newMsFeatureMatrix = msFeatureMatrix.deleteColumns(Ret.NEW, rem);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(0, newMsFeatureLabelMatrix);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(1, msFeatureMatrix.getMetaDataDimensionMatrix(1));
			processed = 70;
			saveMsFeatureMatrix(newMsFeatureMatrix);
		}
		currentExperiment.setDuplicateClustersForDataPipeline(
				activeDataPipeline, new HashSet<MsFeatureCluster>());
		processed = 100;
	}
		
	private void readFeatureMatrix() {

		taskDescription = "Reading MS feature matrix data";
		total = 100;
		processed = 30;
		msFeatureMatrix = 
				ExperimentUtils.readFeatureMatrix(currentExperiment, activeDataPipeline, true);
		if(msFeatureMatrix == null)
			msFeatureMatrix = 
				ExperimentUtils.readFeatureMatrix(currentExperiment, activeDataPipeline, false);
		processed = 100;
	}
	
	private void saveMsFeatureMatrix(Matrix newMsFeatureMatrix) {
		
		if(newMsFeatureMatrix == null)
			return;
		
		taskDescription = 
				"Saving feature matrix for  " + currentExperiment.getName() +
				"(" + currentExperiment.getName() + ")";
		processed = 70;
		ExperimentUtils.saveFeatureMatrixToFile(
				newMsFeatureMatrix,
				currentExperiment, 
				activeDataPipeline,
				false);	//	TODO handle undo for duplicates removal
		currentExperiment.setFeatureMatrixForDataPipeline(activeDataPipeline, null);
		msFeatureMatrix = null;
		System.gc();
		processed = 100;				
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
