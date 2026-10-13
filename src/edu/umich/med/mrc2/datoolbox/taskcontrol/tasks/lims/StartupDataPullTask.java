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

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerMetadataPullTask;

public class StartupDataPullTask extends AbstractTask implements TaskListener {
	
	private LimsDataPullTask limsDataPullTask;
	private IDTrackerMetadataPullTask idTrackerMetadataPullTask;


	public StartupDataPullTask() {
		super();
	}

	@Override
	public void run() {

		limsDataPullTask = new LimsDataPullTask();
		limsDataPullTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(limsDataPullTask);
		idTrackerMetadataPullTask = new IDTrackerMetadataPullTask();
		idTrackerMetadataPullTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(idTrackerMetadataPullTask);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if(e.getStatus().equals(TaskStatus.FINISHED)) {
			
			if(limsDataPullTask != null && limsDataPullTask.getStatus().equals(TaskStatus.FINISHED)
					&& idTrackerMetadataPullTask != null && idTrackerMetadataPullTask.getStatus().equals(TaskStatus.FINISHED))
				setStatus(TaskStatus.FINISHED);
		}
	}

	@Override
	public Task cloneTask() {
		return null;
	}
}
