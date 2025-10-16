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

package edu.umich.med.mrc2.datoolbox.gui.integration.dpalign;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AlignedDataSetTable extends BasicTable {

	private static final long serialVersionUID = -4918759221443655308L;

	public AlignedDataSetTable() {

		super();
		model = new AlignedDataSetTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<AlignedDataSetTableModel>(
				(AlignedDataSetTableModel)model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		finalizeLayout();
	}

	public void setTableModelFromExperiment(DataAnalysisProject currentProject) {
		((AlignedDataSetTableModel)model).setTableModelFromExperiment(currentProject);
		adjustColumns();
	}

	public DataPipelineAlignmentResults getSelectedDataPipelineAlignmentResults() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		else {
			return (DataPipelineAlignmentResults)model.getValueAt(convertRowIndexToModel(row), 
					model.getColumnIndex(AlignedDataSetTableModel.DATA_SET_ALIGNMENT_COLUMN));
		}
	}
}
