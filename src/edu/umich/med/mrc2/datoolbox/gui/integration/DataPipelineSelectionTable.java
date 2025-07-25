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
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataPipelineSelectionTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4918759221443655308L;

	public DataPipelineSelectionTable() {

		super();
		model = new DataPipelineSelectionTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DataPipelineSelectionTableModel>(
				(DataPipelineSelectionTableModel)model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(DataPipelineSelectionTableModel.SELECTED_COLUMN).setMaxWidth(80);
		
		getTableHeader().setReorderingAllowed(false);
		finalizeLayout();
	}

	public void setTableModelFromExperiment(DataAnalysisProject currentProject) {
		((DataPipelineSelectionTableModel)model).setTableModelFromExperiment(currentProject);
		adjustColumns();
	}
	
	public void setTableModelFromDataPipelineCollection(Collection<DataPipeline>pipelines) {
		((DataPipelineSelectionTableModel)model).setTableModelFromDataPipelineCollection(pipelines);
		adjustColumns();
	}
	
	public Collection<DataPipeline>getCheckedDataPipelines(){
		
		Collection<DataPipeline>selectedPipelines = new ArrayList<DataPipeline>();
		int dpCol = model.getColumnIndex(DataPipelineSelectionTableModel.DATA_PIPELINE_COLUMN);
		int checkedCol = model.getColumnIndex(DataPipelineSelectionTableModel.SELECTED_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {	
			if((boolean)model.getValueAt(i, checkedCol))
				selectedPipelines.add((DataPipeline)model.getValueAt(i, dpCol));
		}		
		return selectedPipelines;
	}

	public DataPipeline getSelectedDataPipeline() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		else {
			return (DataPipeline)model.getValueAt(convertRowIndexToModel(row), 
					model.getColumnIndex(DataPipelineSelectionTableModel.DATA_PIPELINE_COLUMN));
		}
	}
}
