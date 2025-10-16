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

import java.util.EventObject;

public class TaskEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7275201406287435966L;
	private TaskStatus status;

	/**
	 * Creates a new TaskEvent
	 * 
	 * @param source
	 *            The Task which caused this event.
	 */
	public TaskEvent(Task source) {
		super(source);
		this.status = source.getStatus();
	}

	/**
	 * Creates a new TaskEvent
	 * 
	 * @param source
	 *            The Task which caused this event.
	 * @param status
	 *            The new TaskStatus of the Task.
	 */
	public TaskEvent(Task source, TaskStatus status) {
		super(source);
		this.status = status;
	}

	/**
	 * Get the source of this TaskEvent
	 * 
	 * @return The Task which caused this event
	 */
	public Task getSource() {
		return (Task) this.source;
	}

	/**
	 * Get the new TaskStatus of the source Task
	 * 
	 * @return The new TaskStatus
	 */
	public TaskStatus getStatus() {
		return status;
	}

}
