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

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;

public class AdductTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	private AdductTableModel model;

	public AdductTable() {

		super();
		model = new AdductTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<AdductTableModel>(model);
		setRowSorter(rowSorter);

		chmodRenderer = new AdductRenderer();
		setDefaultRenderer(Adduct.class, chmodRenderer);

		columnModel.getColumnById(AdductTableModel.MASS_CORRECTION_COLUMN)
				.setCellRenderer(mzRenderer);
		columnModel.getColumnById(AdductTableModel.MASS_CORRECTION_ABS_COLUMN)
				.setCellRenderer(mzRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromAdductList(Collection<Adduct> collection) {
		thf.setTable(null);
		model.setTableModelFromAdductList(collection);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public Adduct getSelectedModification() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (Adduct)getValueAt(row, getColumnIndex(AdductTableModel.CHEM_MOD_COLUMN));		
	}
	
	public void removeAdduct(Adduct adduct) {
		
		int column = model.getColumnIndex(AdductTableModel.CHEM_MOD_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			if(adduct.equals(model.getValueAt(i, column))) {
				model.removeRow(i);
				return;
			}
		}		
	}

	public void selectAdduct(Adduct adduct) {
		
		int column = getColumnIndex(AdductTableModel.CHEM_MOD_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if(adduct.equals(getValueAt(i, column))) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}		
	}
}
