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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims;

import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LimsDataPullTask extends AbstractTask {

	public LimsDataPullTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return new LimsDataPullTask();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 10;

		try {
			try {
				taskDescription = "Getting user data ...";
				LIMSDataCache.refreshUserList();;
				processed = 25;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
			try {
				taskDescription = "Getting organization data ...";
				LIMSDataCache.refreshOrganizationList();
				processed = 50;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
			try {
				taskDescription = "Getting project data ...";
				LIMSDataCache.refreshProjectList();
				processed = 75;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
			try {
				taskDescription = "Getting experiment data ...";
				LIMSDataCache.refreshExperimentList();
				processed = 100;
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}
}











