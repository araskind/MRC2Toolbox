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

import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
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
				LIMSDataCache.refreshUserList();;
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Getting organization data ...";
				LIMSDataCache.refreshOrganizationList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Getting project data ...";
				LIMSDataCache.refreshProjectList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Getting experiment data ...";
				LIMSDataCache.refreshExperimentList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC subject type list";
				MoTrPACDatabaseCache.refreshMotrpacSubjectTypes();
				processed = processed + 1;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC study list";
				MoTrPACDatabaseCache.refreshMotrpacStudyList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC assay list";
				MoTrPACDatabaseCache.refreshMotrpacAssayList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC sample type list";
				MoTrPACDatabaseCache.refreshMotrpacSampleTypeList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC tissue code list";
				MoTrPACDatabaseCache.refreshMotrpacTissueCodeList();
				processed = processed + 10;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			try {
				taskDescription = "Refreshing MoTrPAC report list";
				MoTrPACDatabaseCache.refreshReportList();
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











