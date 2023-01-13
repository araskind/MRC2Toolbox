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

import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.BinnerAdductRenderer;

public class BinnerAnnotationsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	private BinnerAnnotationsTableModel model;

	public BinnerAnnotationsTable() {

		super();
		model = new BinnerAnnotationsTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<BinnerAnnotationsTableModel>(model);
		setRowSorter(rowSorter);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setDefaultRenderer(BinnerAdduct.class, new BinnerAdductRenderer(SortProperty.Name));

		columnModel.getColumnById(BinnerAnnotationsTableModel.MASS_CORRECTION_COLUMN)
				.setCellRenderer(mzRenderer);


		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromBinnerAdducttList(Collection<BinnerAdduct> collection) {
		model.setTableModelFromBinnerAdductList(collection);
		tca.adjustColumns();
	}
	
	public BinnerAdduct getSelectedBinnerAdduct() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (BinnerAdduct)getValueAt(row, getColumnIndex(BinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN));		
	}
	
	public void removeBinnerAdduct(BinnerAdduct adduct) {
		
		int column = model.getColumnIndex(BinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			if(adduct.equals(model.getValueAt(i, column))) {
				model.removeRow(i);
				return;
			}
		}		
	}

	public void selectBinnerAdduct(BinnerAdduct adduct) {
		
		int column = getColumnIndex(BinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if(adduct.equals(getValueAt(i, column))) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}		
	}
}
