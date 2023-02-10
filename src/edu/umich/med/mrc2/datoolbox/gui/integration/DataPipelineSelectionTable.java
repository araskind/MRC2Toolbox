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

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataPipelineSelectionTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4918759221443655308L;
	private DataPipelineSelectionTableModel model;

	public DataPipelineSelectionTable() {

		super();
		model = new DataPipelineSelectionTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DataPipelineSelectionTableModel>(model);
		setRowSorter(rowSorter);
		
		getTableHeader().setReorderingAllowed(false);
		finalizeLayout();
	}

	public void setTableModelFromExperiment(DataAnalysisProject currentProject) {
		model.setTableModelFromExperiment(currentProject);
		tca.adjustColumns();
	}
	
	public Collection<DataPipeline>getSelectedDataPipelines(){
		
		Collection<DataPipeline>selectedPipelines = new ArrayList<DataPipeline>();
		int dpCol = model.getColumnIndex(DataPipelineSelectionTableModel.DATA_PIPELINE_COLUMN);
		for(int i : getSelectedRows())			
			selectedPipelines.add((DataPipeline)model.getValueAt(convertRowIndexToModel(i), dpCol));
		
		return selectedPipelines;
	}
}
