/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class RemoveEmptyFeaturesTask extends AbstractTask {

	private DataPipeline dataPipeline;
	private DataAnalysisProject currentExperiment;
	
	public RemoveEmptyFeaturesTask(
			DataAnalysisProject experiment, DataPipeline dataPipeline) {
		
		this.currentExperiment = experiment;
		this.dataPipeline = dataPipeline;
		processed = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			removeEmptyFeatures();
		}
		catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {
		return new RemoveEmptyFeaturesTask(
				currentExperiment, dataPipeline);
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	private void removeEmptyFeatures() {

		taskDescription = "Removing features with no data for active data pipeline";
		total = 100;
		processed = 20;

		List<MsFeature> featuresToRemove = 
			currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).stream().
			filter(f -> (f.getStatsSummary().getTotalMedian() == 0.0d)).
			collect(Collectors.toList());

		if(!featuresToRemove.isEmpty()) {

			Matrix dataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);
			Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);

			List<Long> rem = featuresToRemove.stream().
					mapToLong(cf -> dataMatrix.getColumnForLabel(cf)).
					boxed().collect(Collectors.toList());

			processed = 50;

			currentExperiment.deleteFeatures(featuresToRemove, dataPipeline);
			Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
			Matrix newFeatureMatrix = featureMatrix.deleteColumns(Ret.NEW, rem);
			newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMatrix);
			newDataMatrix.setMetaDataDimensionMatrix(1, dataMatrix.getMetaDataDimensionMatrix(1));
			currentExperiment.setDataMatrixForDataPipeline(dataPipeline, newDataMatrix);
			saveMsFeatureMatrix(newFeatureMatrix);
			
			taskDescription = "Removing features from active data pipeline libary";
			processed = 90;
			
			Collection<LibraryMsFeature>libFeatures = 
					featuresToRemove.stream().filter(LibraryMsFeature.class::isInstance).
					map(LibraryMsFeature.class::cast).collect(Collectors.toList());
			
			currentExperiment.getCompoundLibraryForDataPipeline(dataPipeline).removeFeatures(libFeatures);
		}		
		processed = 100;
	}
	
	private void saveMsFeatureMatrix(Matrix msFeatureMatrix) {
		
		taskDescription = "Saving feature matrix for  " + currentExperiment.getName() +
				"(" + currentExperiment.getName() + ")";
		total = 100;
		processed = 50;
		
		ProjectUtils.saveFeatureMatrixToFile(
				msFeatureMatrix,
				currentExperiment, 
				dataPipeline,
				true);
		msFeatureMatrix = null;
		currentExperiment.setFeatureMatrixForDataPipeline(dataPipeline, null);
		System.gc();
		
//		String featureMatrixFileName = currentExperiment.getFeatureMatrixFileNameForDataPipeline(dataPipeline);
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
//				//					setStatus(TaskStatus.ERROR);
//				return;
//
//			}
//			msFeatureMatrix = null;
//			System.gc();
//		}				
	}
}









