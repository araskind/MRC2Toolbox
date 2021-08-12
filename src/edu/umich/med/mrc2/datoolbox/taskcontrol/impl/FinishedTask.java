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

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;

public class FinishedTask extends AbstractTask {

	private String description;
	private double finishedPercentage;
	private Task ftask;

	public FinishedTask(Task task) {

		ftask = task;
		setStatus(task.getStatus());
		description = task.getTaskDescription();
		errorMessage = task.getErrorMessage();
		finishedPercentage = task.getFinishedPercentage();
	}

	public void cancel() {
		// ignore any attempt to cancel this task, because it is finished
	}

	@Override
	public Task cloneTask() {

		return ftask.cloneTask();
	}

	public double getFinishedPercentage() {
		return finishedPercentage;
	}

	public String getTaskDescription() {
		return description;
	}

	public void run() {
		// ignore any attempt to run this task, because it is finished
	}

}
