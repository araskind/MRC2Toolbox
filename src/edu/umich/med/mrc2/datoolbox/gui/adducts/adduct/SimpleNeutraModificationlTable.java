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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;

public class SimpleNeutraModificationlTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	private SimpleNeutralModificationTableModel model;

	public SimpleNeutraModificationlTable() {

		super();
		model = new SimpleNeutralModificationTableModel();
		setModel(model);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		rowSorter = new TableRowSorter<SimpleNeutralModificationTableModel>(model);
		setRowSorter(rowSorter);
		setDefaultRenderer(Adduct.class, new AdductRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public SimpleAdduct getSelectedModification() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (SimpleAdduct)getValueAt(row, getColumnIndex(SimpleNeutralModificationTableModel.CHEM_MOD_COLUMN));		
	}

	public void setTableModelFromAdductList(Collection<Adduct> adducts) {
		thf.setTable(null);
		model.setTableModelFromAdductList(adducts);
		thf.setTable(this);
		tca.adjustColumns();
	}
}
