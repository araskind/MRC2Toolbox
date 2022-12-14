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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.SiriusMsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerSiriusMsClusterExportTask extends IDTrackerSiriusMsExportTask {

	private MSMSClusterDataSet msmsClusterDataSet;

	public IDTrackerSiriusMsClusterExportTask(
			MSMSClusterDataSet msmsClusterDataSet, File outputFile2) {
		super(outputFile2);
		this.msmsClusterDataSet = msmsClusterDataSet;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);	
		try {
			createSiriusClusters();
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
		try {
			writeMsFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
	}
	
	private void createSiriusClusters() {
		
		msmsclusters = new ArrayList<SiriusMsMsCluster>();
		taskDescription = "Converting clusters for SIRIUS export";
		Set<MsFeatureInfoBundleCluster> msmsClusters = msmsClusterDataSet.getClusters();
		total = msmsClusters.size();
		processed = 1;
				
		for(MsFeatureInfoBundleCluster cluster : msmsClusters) {
			
			msmsclusters.add(new SiriusMsMsCluster(cluster)) ;
			processed++;
		}
	}
	
	@Override
	public Task cloneTask() {

		return new IDTrackerSiriusMsClusterExportTask(
				 msmsClusterDataSet,
				 outputFile);
	}
}
