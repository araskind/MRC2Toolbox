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

import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ClusterCorrelationMatrixTask extends AbstractTask {
	
	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;
	
	public ClusterCorrelationMatrixTask() {
		
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		activeDataPipeline = currentExperiment.getActiveDataPipeline();
	}

	@Override
	public Task cloneTask() {

		return new ClusterCorrelationMatrixTask();
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		try {	
			taskDescription = "Recalculating cluster correlation matrixes...";
			Set<MsFeatureCluster> clusters = 
					currentExperiment.getMsFeatureClustersForDataPipeline(activeDataPipeline);
			total = clusters.size();
			processed = 0;			
			for(MsFeatureCluster cluster : clusters) {			
				cluster.setClusterCorrMatrix(cluster.createClusterCorrelationMatrix(false));
				processed++;
			}
			setStatus(TaskStatus.FINISHED);	

		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}			
	}
}




