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

public class SimpleAdductTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	private SimpleAdductTableModel model;

	public SimpleAdductTable() {

		super();
		model = new SimpleAdductTableModel();
		setModel(model);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<SimpleAdductTableModel>(model);
		setRowSorter(rowSorter);
		setDefaultRenderer(Adduct.class, new AdductRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public SimpleAdduct getSelectedSimpleAdduct() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (SimpleAdduct) model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(SimpleAdductTableModel.CHEM_MOD_COLUMN));
	}

	public Adduct getSelectedAdduct() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (Adduct) model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(SimpleAdductTableModel.CHEM_MOD_COLUMN));	
	}
	
	public void setTableModelFromAdductList(Collection<Adduct> adducts) {
		model.setTableModelFromAdductList(adducts);
		tca.adjustColumns();
	}
	
	public void selectAdduct(Adduct toSelect) {
		
		int col = model.getColumnIndex(SimpleAdductTableModel.CHEM_MOD_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(model.getValueAt(convertRowIndexToModel(i), col).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}
}
