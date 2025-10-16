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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.TableColumnAdjuster;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;

public class TaskTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6038476299497788543L;
	
	public TaskTable() {

		model = new TaskTableModel();
		setModel(model);
		addColumnSelectorPopup();
		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
	}

	public void addTask(Task taskToAdd) {

		((TaskTableModel)model).addTask(taskToAdd);
		tca.adjustColumns();
	}
}
