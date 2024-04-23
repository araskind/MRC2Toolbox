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

package edu.umich.med.mrc2.datoolbox.taskcontrol;

public interface Task extends Runnable {

	public void addTaskListener(TaskListener t);
	public void removeTaskListener(TaskListener t);
	
	/**
	 * Cancel a running task by user request.
	 */
	public void cancel();

	public Task cloneTask();

	/**
	 * After the task is finished, this method returns an array of all objects
	 * newly created by this task (peak lists, raw data files). This is used for
	 * batch processing. Tasks which are never used in batch steps can return
	 * null.
	 */
	public Object[] getCreatedObjects();

	public String getErrorMessage();

	public double getFinishedPercentage();

	public TaskStatus getStatus();

	public String getTaskDescription();

	public TaskListener[] getTaskListeners();

	public void setStatus(TaskStatus newStatus);
}
