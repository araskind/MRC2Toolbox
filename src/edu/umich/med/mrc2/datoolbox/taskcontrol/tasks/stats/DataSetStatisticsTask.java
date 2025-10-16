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

import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class DataSetStatisticsTask extends AbstractTask {

	private DataPipeline dataPipeline;
	private DataAnalysisProject currentExperiment;
	private Collection<DataFileStatisticalSummary> statsList;

	public DataSetStatisticsTask(
			DataAnalysisProject experiment, DataPipeline dataPipeline) {
		
		this.currentExperiment = experiment;
		this.dataPipeline = dataPipeline;
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Calculating statistics for all samples in " + 
				dataPipeline.getName();
		total = currentExperiment.getDataFilesForAcquisitionMethod(
				dataPipeline.getAcquisitionMethod()).size();
		processed = 0;
		statsList = new ArrayList<DataFileStatisticalSummary>();
		try {
			calculateStatistics();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}

	private void calculateStatistics() {

		for (DataFile file : currentExperiment.getDataFilesForAcquisitionMethod(
				dataPipeline.getAcquisitionMethod())) {

			DataFileStatisticalSummary fileSummary = 
					new DataFileStatisticalSummary(file);
			fileSummary.calculateFileStat();
			statsList.add(fileSummary);
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		return new DataSetStatisticsTask(currentExperiment, dataPipeline);
	}

	public Collection<DataFileStatisticalSummary> getStatsList() {
		return statsList;
	}
}
