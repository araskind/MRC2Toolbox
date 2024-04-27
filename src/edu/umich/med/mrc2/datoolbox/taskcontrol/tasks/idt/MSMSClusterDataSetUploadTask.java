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

import java.sql.Connection;

import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MSMSClusterDataSetUploadTask extends MSMSClusterTask {

	private IMSMSClusterDataSet dataSet;
	
	public MSMSClusterDataSetUploadTask(IMSMSClusterDataSet dataSet) {
		super();
		this.dataSet = dataSet;
	}

	@Override
	public void run() {

		taskDescription = "Uploading data for MSMS cluster data set " + dataSet.getName();
		setStatus(TaskStatus.PROCESSING);
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			insertNewMSMSClusterDataSet(dataSet, conn);
		} catch (Exception e1) {
			errorMessage = e1.getMessage();
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}	
		if(conn != null) {
			try {
				ConnectionManager.releaseConnection(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		taskDescription = "Refreshing data set list ... ";
		total = 100;
		processed = 80;
		MSMSClusterDataSetManager.refreshMSMSClusterDataSetList();
		MSMSClusterDataSetManager.refreshMsmsClusteringParameters();
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new MSMSClusterDataSetUploadTask(dataSet);
	}

	public IMSMSClusterDataSet getDataSet() {
		return dataSet;
	}


}
