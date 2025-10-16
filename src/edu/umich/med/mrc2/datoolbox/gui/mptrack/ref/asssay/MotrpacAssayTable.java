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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.asssay;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MotrpacAssayRenderer;

public class MotrpacAssayTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -381502820936496986L;

	public MotrpacAssayTable() {
		super();
		model = new MotrpacAssayTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MotrpacAssayTableModel>(
				(MotrpacAssayTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		
		columnModel.getColumnById(MotrpacAssayTableModel.ASSAY_COLUMN)
			.setCellRenderer(new MotrpacAssayRenderer(SortProperty.ID));

		finalizeLayout();
	}

	public void setTableModelFromAssays(Collection<MoTrPACAssay> assays)  {

		((MotrpacAssayTableModel)model).setTableModelFromAssays(assays);
		adjustColumns();
	}

	public MoTrPACAssay getSelectedAssay(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MoTrPACAssay) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MotrpacAssayTableModel.ASSAY_COLUMN));
	}

	public void selectAssay(MoTrPACAssay assay) {

		int colIdx = model.getColumnIndex(MotrpacAssayTableModel.ASSAY_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), colIdx).equals(assay)){
				setRowSelectionInterval(i, i);
				this.scrollToSelected();
				return;
			}
		}
	}
}
