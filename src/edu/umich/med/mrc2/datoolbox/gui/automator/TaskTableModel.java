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

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.QualAutomation.QualAutomationDataProcessingTask;

public class TaskTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -2772014067760250399L;

	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String METHOD_COLUMN = "Method";

	public TaskTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DATA_FILE_COLUMN, "Raw data file", String.class, false),
			new ColumnContext(METHOD_COLUMN, "Data processing method", String.class, false)
		};
	}

	public void addTask(Task taskToAdd) {

		if (taskToAdd.getClass().equals(QualAutomationDataProcessingTask.class)) {

			QualAutomationDataProcessingTask dp = (QualAutomationDataProcessingTask) taskToAdd;

			Object[] obj = {

					FilenameUtils.getName(dp.getDataFile().getAbsolutePath()),
					FilenameUtils.getName(dp.getMethodFile().getAbsolutePath()) };
			super.addRow(obj);
		}
	}
}
