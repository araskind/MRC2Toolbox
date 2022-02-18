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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

public class MergeDuplicateFeaturesTask extends AbstractTask {

	private DataPipeline activeDataPipeline;
	private DataAnalysisProject currentProject;
	private Set<MsFeatureCluster> duplicateList;
	private DuplicatesCleanupOptions mergeOption;

	public MergeDuplicateFeaturesTask(
			DataAnalysisProject currentProject,
			DataPipeline activeDataPipeline,
			DuplicatesCleanupOptions duplicatesCleanupOptions) {
		
		this.activeDataPipeline = activeDataPipeline;
		this.currentProject = currentProject;
		this.mergeOption = duplicatesCleanupOptions;

		duplicateList = 
				currentProject.getDuplicateClustersForDataPipeline(activeDataPipeline);
		total = duplicateList.size();
		processed = 20;
	}

	public MergeDuplicateFeaturesTask(
			Collection<MsFeature>featuresToMerge,
			DataAnalysisProject currentProject,
			DataPipeline activeDataPipeline,
			DuplicatesCleanupOptions duplicatesCleanupOptions) {

		this.activeDataPipeline = activeDataPipeline;
		this.currentProject = currentProject;
		this.mergeOption = duplicatesCleanupOptions;

		MsFeatureCluster mergeCluster = new MsFeatureCluster();
		featuresToMerge.stream().
			forEach(f -> mergeCluster.addFeature(f, activeDataPipeline));

		duplicateList = new HashSet<MsFeatureCluster>();
		duplicateList.add(mergeCluster);
		total = duplicateList.size();
		processed = 20;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			removeDuplicateFeatures();
			setStatus(TaskStatus.FINISHED);
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private Matrix readFeatureMatrix() {

		taskDescription = "Reading MS feature matrix data";
		processed = 30;
		
		File featureMatrixFile = Paths.get(currentProject.getProjectDirectory().getAbsolutePath(), 
				currentProject.getFeatureMatrixFileNameForDataPipeline(activeDataPipeline)).toFile();
		if (!featureMatrixFile.exists())
			return null;
		
		Matrix featureMatrix = null;
		if (featureMatrixFile.exists()) {
			try {
				featureMatrix = Matrix.Factory.load(featureMatrixFile);
			} catch (ClassNotFoundException | IOException e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
			if (featureMatrix != null) {

				featureMatrix.setMetaDataDimensionMatrix(0, 
						currentProject.getMetaDataMatrixForDataPipeline(activeDataPipeline, 0));
				featureMatrix.setMetaDataDimensionMatrix(1, 
						currentProject.getMetaDataMatrixForDataPipeline(activeDataPipeline, 1));
			}
		}		
		return featureMatrix;
	}

	public void removeDuplicateFeatures() {

		if (duplicateList.isEmpty())
			return;
				
		Matrix msFeatureMatrix = readFeatureMatrix();		
		
		taskDescription = "Removing duplicate features for active data pipeline";
		processed = 50;
		Matrix dataMatrix = 
				currentProject.getDataMatrixForDataPipeline(activeDataPipeline);
		Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);
		ArrayList<MsFeature> featuresToRemove = new ArrayList<MsFeature>();
		ArrayList<MsFeature> featuresToMerge = new ArrayList<MsFeature>();
		DataAcquisitionMethod acquisitionMethod = activeDataPipeline.getAcquisitionMethod();

		// Swap ID and data source feature where necessary to get better quality
		// data
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
				for (ExperimentalSample sample : currentProject.getExperimentDesign().getSamples()) {

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
		}
		ArrayList<Long> rem = new ArrayList<Long>();
		for (MsFeature cf : featuresToRemove)
			rem.add(dataMatrix.getColumnForLabel(cf));

		currentProject.deleteFeatures(featuresToRemove, activeDataPipeline);
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
			
			//	TODO - there is no undo for it here, if the project is not saved
			//	Same for areas matrix?? they need backup for this case !!!
			//	Maybe for now force save the project and warn that there is no UNDO
			saveMsFeatureMatrix(newMsFeatureMatrix);
			
			System.err.println("Data: " + newDataMatrix.getRowCount() + 
					" by " + newDataMatrix.getColumnCount());
			System.err.println("Features: " + newMsFeatureMatrix.getRowCount() + 
					" by " + newMsFeatureMatrix.getColumnCount());
		}
		currentProject.setDataMatrixForDataPipeline(
				activeDataPipeline, newDataMatrix);
		duplicateList = new HashSet<MsFeatureCluster>();
		currentProject.setDuplicateClustersForDataPipeline(
				activeDataPipeline, duplicateList);
	}
	
	private void saveMsFeatureMatrix(Matrix msFeatureMatrix) {
		
		String featureMatrixFileName = currentProject.getFeatureMatrixFileNameForDataPipeline(activeDataPipeline);
		if(featureMatrixFileName != null) {
			
			taskDescription = "Saving feature matrix for  " + currentProject.getName() +
					"(" + currentProject.getName() + ")";
			processed = 90;
			File featureMatrixFile = 
					Paths.get(currentProject.getProjectDirectory().getAbsolutePath(), 
					featureMatrixFileName).toFile();
			try {
				Matrix featureMatrix = 
						Matrix.Factory.linkToArray(msFeatureMatrix.toObjectArray());
				featureMatrix.save(featureMatrixFile);
				processed = 100;
			} catch (IOException e) {
				e.printStackTrace();
//					setStatus(TaskStatus.ERROR);
			}
			msFeatureMatrix = null;
			System.gc();
		}				
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
				currentProject, activeDataPipeline, mergeOption);
	}

	public DataPipeline getDataPipeline() {
		return activeDataPipeline;
	}
}
