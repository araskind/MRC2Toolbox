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

package edu.umich.med.mrc2.datoolbox.taskcontrol;

import java.util.LinkedList;

public abstract class AbstractTask implements Task {

	private TaskStatus status = TaskStatus.WAITING;
	private LinkedList<TaskListener> taskListeners = new LinkedList<>();
	protected String errorMessage = null;

	protected int total;
	protected int processed;
	protected String taskDescription;

	/**
	 * Adds a TaskListener to this Task
	 *
	 * @param t
	 *            The TaskListener to add
	 */
	public void addTaskListener(TaskListener t) {
		taskListeners.add(t);
	}

	public void removeTaskListener(TaskListener t) {
		taskListeners.remove(t);
	}

	/**
	 * @see edu.umich.med.mrc2.cefanalyzer.taskcontrol.mzmine.taskcontrol.Task#cancel()
	 */
	public void cancel() {
		setStatus(TaskStatus.CANCELED);
	}

	/**
	 * Triggers a TaskEvent and notifies the listeners
	 */
	protected void fireTaskEvent() {
		TaskEvent event = new TaskEvent(this);
		for (TaskListener t : this.taskListeners) {
			t.statusChanged(event);
		}
	}

	public Object[] getCreatedObjects() {
		return null;
	}

	/**
	 * @see edu.umich.med.mrc2.cefanalyzer.taskcontrol.mzmine.taskcontrol.Task#getErrorMessage()
	 */
	public final String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public double getFinishedPercentage() {

		if(total == 0)
			return 0.0d;
		else
			return (double) processed / total;
	}

	/**
	 * Returns the TaskStatus of this Task
	 *
	 * @return The current status of this task
	 */
	public final TaskStatus getStatus() {
		return this.status;
	}

	@Override
	public String getTaskDescription() {

		return taskDescription;
	}

	/**
	 * Returns all of the TaskListeners which are listening to this task.
	 *
	 * @return An array containing the TaskListeners
	 */
	public TaskListener[] getTaskListeners() {
		return taskListeners.toArray(new TaskListener[0]);
	}

	/**
	 * Convenience method for determining if this task has been canceled. Also
	 * returns true if the task encountered an error.
	 *
	 * @return true if this task has been canceled or stopped due to an error
	 */
	public final boolean isCanceled() {
		return (status == TaskStatus.CANCELED) || (status == TaskStatus.ERROR);
	}

	/**
	 * Convenience method for determining if this task has been completed
	 *
	 * @return true if this task is finished
	 */
	public final boolean isFinished() {
		return status == TaskStatus.FINISHED;
	}

	/**
	 * @see edu.umich.med.mrc2.cefanalyzer.taskcontrol.mzmine.taskcontrol.Task#setStatus()
	 */
	public final void setStatus(TaskStatus newStatus) {
		this.status = newStatus;
		this.fireTaskEvent();
	}
	
	public final void reportErrorAndExit(Exception e) {
		
		e.printStackTrace();
		errorMessage = e.getMessage();
		if(e.getCause() != null && e.getCause().getMessage() != null)
			errorMessage += "\n" + e.getCause().getMessage();
		
		setStatus(TaskStatus.ERROR);		
	}
}
