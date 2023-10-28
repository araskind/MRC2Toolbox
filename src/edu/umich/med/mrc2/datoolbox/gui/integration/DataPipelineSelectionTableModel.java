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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataPipelineSelectionTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8757581409205015995L;

	public static final String SELECTED_COLUMN = "Selected";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";

	public DataPipelineSelectionTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SELECTED_COLUMN, SELECTED_COLUMN, Boolean.class, true),
			new ColumnContext(DATA_PIPELINE_COLUMN, DATA_PIPELINE_COLUMN, DataPipeline.class, false),
		};
	}

	public void setTableModelFromExperiment(DataAnalysisProject currentProject) {

		setRowCount(0);
		if(currentProject == null 
				|| currentProject.getDataPipelines().isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (DataPipeline dp : currentProject.getDataPipelines()) {

			Object[] obj = {
					true,
					dp
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
