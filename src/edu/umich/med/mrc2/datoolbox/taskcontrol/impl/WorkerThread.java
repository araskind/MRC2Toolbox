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

package edu.umich.med.mrc2.datoolbox.taskcontrol.impl;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class WorkerThread extends Thread {

	private WrappedTask wrappedTask;
	private boolean finished = false;

	WorkerThread(WrappedTask wrappedTask) {
		super("Thread executing task " + wrappedTask);
		this.wrappedTask = wrappedTask;
		wrappedTask.assignTo(this);
	}

	boolean isFinished() {
		return finished;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		Task actualTask = wrappedTask.getActualTask();

		try {

			// Process the actual task
			actualTask.run();

			// Check if task finished with an error
			if (actualTask.getStatus() == TaskStatus.ERROR) {

				String errorMsg = actualTask.getErrorMessage();

				if (errorMsg == null)
					errorMsg = "Unspecified error";

				MRC2ToolBoxCore.getMainWindow().displayErrorMessage("Error of task " + actualTask.getTaskDescription(),
						errorMsg);
			}

			/*
			 * This is important to allow the garbage collector to remove the
			 * task, while keeping the task description in the
			 * "Tasks in progress" window
			 */
			// if (actualTask.getStatus() != TaskStatus.ERROR &&
			// actualTask.getStatus() != TaskStatus.EXTERNAL_ERROR)
			wrappedTask.removeTaskReference();
		} catch (Throwable e) {

			/*
			 * This should never happen, it means the task did not handle its
			 * exception properly, or there was some severe error, like
			 * OutOfMemoryError
			 */

			e.printStackTrace();

			MRC2ToolBoxCore.getMainWindow().displayErrorMessage(
					"Unhandled exception in task " + actualTask.getTaskDescription(), e.toString());
		}

		/*
		 * Mark this thread as finished
		 */
		finished = true;

	}

}
