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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.projinfo;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.TristateCheckboxRenderer;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataPipelinesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3232039944841480790L;
	private DataPipelinesTableModel model;

	public DataPipelinesTable() {

		super();
		model = new DataPipelinesTableModel();		
		setModel(model);
		rowSorter = new TableRowSorter<DataPipelinesTableModel>(model);
		setRowSorter(rowSorter);
		
		columnModel.getColumnById(DataPipelinesTableModel.ACTIVE_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(DataPipelinesTableModel.WORKLIST_COLUMN)
			.setCellRenderer(new TristateCheckboxRenderer());
		
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		finalizeLayout();
	}

	public void setTableModelFromProject(DataAnalysisProject currentProject) {
		model.setTableModelFromProject(currentProject);
		tca.adjustColumns();
	}
	
	public DataPipeline getSelectedDataPipeline() {
	
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (DataPipeline)model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(DataPipelinesTableModel.DATA_PIPELINE_COLUMN));
	}
	
	public void selectPipeline(DataPipeline toSelect) {
		
		int col = model.getColumnIndex(DataPipelinesTableModel.DATA_PIPELINE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			if(model.getValueAt(convertRowIndexToModel(i), col).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				return;
			}
		}
	}
}
