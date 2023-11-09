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

package edu.umich.med.mrc2.datoolbox.gui.assay;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AssayMethodRenderer;

public class AssayTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5022129546919771647L;

	public AssayTable() {
		super();
		model = new AssayTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<AssayTableModel>((AssayTableModel)model);
		setRowSorter(rowSorter);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(AssayTableModel.ASSAY_COLUMN).setCellRenderer(new AssayMethodRenderer());
		setExactColumnWidth(AssayTableModel.ASSAY_ID_COLUMN, 80);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromAssayCollection(Collection<Assay> assayCollection) {

		thf.setTable(null);
		((AssayTableModel)model).setTableModelFromAssayCollection(assayCollection);
		thf.setTable(this);
		adjustColumns();
	}
		
	public Assay getSelectedAssay() {

		int row = getSelectedRow();
		if (row == -1)
			return null;

		Assay method = (Assay) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(AssayTableModel.ASSAY_COLUMN));

		return method;
	}
}
