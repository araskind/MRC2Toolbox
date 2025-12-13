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

import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.FeatureSetAlteringTask;

public class RemoveEmptyFeaturesTask extends FeatureSetAlteringTask {

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

		List<MsFeature> featuresToRemove = 
			currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).stream().
			filter(f -> Double.isNaN(f.getStatsSummary().getTotalMedian())).
			collect(Collectors.toList());
		
		if(featuresToRemove.isEmpty()) {
			processed = 100;
			return;
		}
		List<Long> featureIndices = featuresToRemove.stream().
				mapToLong(cf -> currentExperiment.getDataMatrixForDataPipeline(dataPipeline).getColumnForLabel(cf)).
				boxed().collect(Collectors.toList());
		
		cleanAndSaveDataMatrix(currentExperiment, dataPipeline, featureIndices);
		cleanAndSaveFeatureMatrix(currentExperiment, dataPipeline, featureIndices);

		taskDescription = "Removing features with no data for active data pipeline";
		total = 100;
		processed = 20;
			
		currentExperiment.deleteFeatures(featuresToRemove, dataPipeline);		
		processed = 100;
		
		saveFeaturesForPipeline(currentExperiment, dataPipeline);
	}
}









