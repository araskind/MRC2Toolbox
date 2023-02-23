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

package edu.umich.med.mrc2.datoolbox.gui.adducts.bindif;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;

public class BinnerNeutralMassDifferenceTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5791881460238004998L;
	private BinnerNeutralMassDifferenceTableModel model;
	
	public BinnerNeutralMassDifferenceTable() {

		super();
		model = new BinnerNeutralMassDifferenceTableModel();
		setModel(model);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		rowSorter = new TableRowSorter<BinnerNeutralMassDifferenceTableModel>(model);
		setRowSorter(rowSorter);
		setDefaultRenderer(Adduct.class, new AdductRenderer());
	
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void selectMassDifference(BinnerNeutralMassDifference toSelect) {

		int column = model.getColumnIndex(BinnerNeutralMassDifferenceTableModel.NAME_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(((BinnerNeutralMassDifference)model.getValueAt(convertRowIndexToModel(i), column)).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}

	public BinnerNeutralMassDifference getSelectedMassDifference() {

		if (getSelectedRow() == -1)
			return null;

		return (BinnerNeutralMassDifference) model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(BinnerNeutralMassDifferenceTableModel.NAME_COLUMN));
	}

	public void setTableModelFromBinnerNeutralMassDifferenceList(Collection<BinnerNeutralMassDifference> list) {

		model.setTableModelFromBinnerNeutralMassDifferenceList(list);
		tca.adjustColumns();
	}
}


