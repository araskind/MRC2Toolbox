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

package edu.umich.med.mrc2.datoolbox.taskcontrol.impl;

import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskPriority;

public class WrappedTask {

	private Task task;
	private TaskPriority priority;
	private WorkerThread assignedTo;

	WrappedTask(Task task, TaskPriority priority) {
		this.task = task;
		this.priority = priority;
	}

	void assignTo(WorkerThread thread) {
		assignedTo = thread;
	}

	/**
	 * @return Returns the task.
	 */
	public synchronized Task getActualTask() {
		return task;
	}

	/**
	 * @return Returns the priority.
	 */
	TaskPriority getPriority() {
		return priority;
	}

	/**
	 * @return Returns the assigned.
	 */
	boolean isAssigned() {
		return assignedTo != null;
	}

	synchronized void removeTaskReference() {
		task = new FinishedTask(task);
	}

	/**
	 * @param priority
	 *            The priority to set.
	 */
	void setPriority(TaskPriority priority) {
		this.priority = priority;
		if (assignedTo != null) {
			switch (priority) {
			case HIGH:
				assignedTo.setPriority(Thread.MAX_PRIORITY);
				break;
			case NORMAL:
				assignedTo.setPriority(Thread.NORM_PRIORITY);
				break;
			}
		}
	}

	public synchronized String toString() {
		return task.getTaskDescription();
	}
}
