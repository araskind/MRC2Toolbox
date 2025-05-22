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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.Collection;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.BinnerAdductRenderer;

public class SimpleBinnerAnnotationsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;

	private boolean showTier;
	
	public SimpleBinnerAnnotationsTable(boolean showTier) {

		super();
		this.showTier = showTier;
		model = new SimpleBinnerAnnotationsTableModel(showTier);
		setModel(model);
		
		rowSorter = new TableRowSorter<SimpleBinnerAnnotationsTableModel>(
				(SimpleBinnerAnnotationsTableModel)model);
		setRowSorter(rowSorter);
		getSelectionModel().setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		setDefaultRenderer(BinnerAdduct.class, new BinnerAdductRenderer(SortProperty.Name));

		columnModel.getColumnById(
				SimpleBinnerAnnotationsTableModel.MASS_CORRECTION_COLUMN)
				.setCellRenderer(mzRenderer);


		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromBinnerAdductCollection(Collection<BinnerAdduct> collection) {
		thf.setTable(null);
		((SimpleBinnerAnnotationsTableModel)model).setTableModelFromBinnerAdductList(collection);
		thf.setTable(this);
		adjustColumns();
	}
	
	public BinnerAdduct getSelectedBinnerAdduct() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (BinnerAdduct)getValueAt(
				row, getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN));		
	}
	
	public void removeBinnerAdduct(BinnerAdduct adduct) {
		
		int column = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			if(adduct.equals(model.getValueAt(i, column))) {
				model.removeRow(i);
				return;
			}
		}		
	}

	public void selectBinnerAdduct(BinnerAdduct adduct) {
		
		int column = getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if(adduct.equals(getValueAt(i, column))) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}		
	}

	public void setTableModelFromBinnerAdductTierMap(Map<BinnerAdduct, Integer> tierMap) {
		
		thf.setTable(null);
		((SimpleBinnerAnnotationsTableModel)model).setTableModelFromBinnerAdductTierMap(tierMap);
		thf.setTable(this);
		adjustColumns();
	}
}
