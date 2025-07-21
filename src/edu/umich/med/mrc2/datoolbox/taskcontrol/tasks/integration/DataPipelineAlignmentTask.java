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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class DataPipelineAlignmentTask extends AbstractTask {

	private DataAnalysisProject currentExperiment;
	private Collection<DataPipeline>selectedPipelines;
	private double massWindow;
	private MassErrorType massErrorType;
	private double retentionWindow;
	private Set<MsFeatureCluster> clusterList;
		
	public DataPipelineAlignmentTask(
			DataAnalysisProject currentExperiment,
			Collection<DataPipeline> selectedPipelines, 
			double massWindow,
			MassErrorType massErrorType, 
			double retentionWindow) {
		super();
		this.currentExperiment = currentExperiment;
		this.selectedPipelines = selectedPipelines;
		this.massWindow = massWindow;
		this.massErrorType = massErrorType;
		this.retentionWindow = retentionWindow;
		clusterList = new HashSet<MsFeatureCluster>();
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {

		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {

		return new DataPipelineAlignmentTask(
				currentExperiment, 
				selectedPipelines, 
				massWindow, 
				massErrorType, 
				retentionWindow);
	}

	public Set<MsFeatureCluster> getClusterList() {
		return clusterList;
	}
}


















