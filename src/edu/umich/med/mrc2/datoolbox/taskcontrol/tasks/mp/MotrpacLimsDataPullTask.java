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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp;

import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCash;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MotrpacLimsDataPullTask extends AbstractTask {

	public MotrpacLimsDataPullTask() {
		super();
		}

	@Override
	public Task cloneTask() {
		return new MotrpacLimsDataPullTask();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 1;

		try {
			try {
				taskDescription = "Getting user data ...";
				LIMSDataCash.refreshUserList();;
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Getting organization data ...";
				LIMSDataCash.refreshOrganizationList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Getting project data ...";
				LIMSDataCash.refreshProjectList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Getting experiment data ...";
				LIMSDataCash.refreshExperimentList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC subject type list";
				MoTrPACDatabaseCash.refreshMotrpacSubjectTypes();
				processed = processed + 1;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC study list";
				MoTrPACDatabaseCash.refreshMotrpacStudyList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC assay list";
				MoTrPACDatabaseCash.refreshMotrpacAssayList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC sample type list";
				MoTrPACDatabaseCash.refreshMotrpacSampleTypeList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC tissue code list";
				MoTrPACDatabaseCash.refreshMotrpacTissueCodeList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC report list";
				MoTrPACDatabaseCash.refreshReportList();
				processed = 100;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
}











