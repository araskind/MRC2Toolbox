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

package edu.umich.med.mrc2.datoolbox.gui.adducts.exchange;

import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductExchangeDeltaMassRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;

public class AdductExchangeTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5791881460238004998L;
	private AdductExchangeTableModel model;
	
	public AdductExchangeTable() {

		super();
		model = new AdductExchangeTableModel();
		setModel(model);

		rowSorter = new TableRowSorter<AdductExchangeTableModel>(model);
		setRowSorter(rowSorter);
		setDefaultRenderer(Adduct.class, new AdductRenderer());
		setDefaultRenderer(AdductExchange.class, new AdductExchangeDeltaMassRenderer());
	
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void selectExchange(AdductExchange toSelect) {

		int column = model.getColumnIndex(AdductExchangeTableModel.MASS_DIFFERENCE_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(((AdductExchange)model.getValueAt(convertRowIndexToModel(i), column)).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}

	public AdductExchange getSelectedExchange() {

		if (getSelectedRow() == -1)
			return null;

		return (AdductExchange) model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(AdductExchangeTableModel.MASS_DIFFERENCE_COLUMN));
	}

	public void setTableModelFromAdductExchangeList(Collection<AdductExchange> list) {
		thf.setTable(null);
		model.setTableModelFromAdductExchangeList(list);
		thf.setTable(this);
		tca.adjustColumns();
	}
}


