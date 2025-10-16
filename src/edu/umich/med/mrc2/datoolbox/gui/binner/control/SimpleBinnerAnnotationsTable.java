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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.SpinnerEditor;
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
		setDefaultEditor(Integer.class, new SpinnerEditor(1, 2));		
		columnModel.getColumnById(
				SimpleBinnerAnnotationsTableModel.MASS_CORRECTION_COLUMN)
				.setCellRenderer(mzRenderer);
		
		setExactColumnWidth(SimpleBinnerAnnotationsTableModel.ORDER_COLUMN, 40);
		fixedWidthColumns.add(model.getColumnIndex(
				SimpleBinnerAnnotationsTableModel.ORDER_COLUMN));
		
		addTablePopupMenu(new BasicTablePopupMenu(null, this, true));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		
		if(convertColumnIndexToModel(column) == 
				model.getColumnIndex(SimpleBinnerAnnotationsTableModel.ORDER_COLUMN))
			return row+1;
		else
			return super.getValueAt(row, column);
	}

	public void setTableModelFromBinnerAdductTierMap(Map<BinnerAdduct, Integer> tierMap) {
		
		thf.setTable(null);
		((SimpleBinnerAnnotationsTableModel)model).setTableModelFromBinnerAdductTierMap(tierMap);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void setTableModelFromBinnerAdductCollection(Collection<BinnerAdduct> collection) {
		thf.setTable(null);
		((SimpleBinnerAnnotationsTableModel)model).setTableModelFromBinnerAdductList(collection);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void removeBinnerAdduct(BinnerAdduct adduct) {
		
		int column = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			if(adduct.equals(model.getValueAt(i, column))) {
				model.removeRow(i);
				adjustColumns();
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
	
	public Collection<BinnerAdduct> getSelectedBinnerAdducts() {
		
		Set<BinnerAdduct> selected = new TreeSet<BinnerAdduct>();
		int[]selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return selected;
		
		int adductColIndex = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int row : selectedRows) {

			BinnerAdduct adduct = 
					(BinnerAdduct)model.getValueAt(convertRowIndexToModel(row), adductColIndex);
			selected.add(adduct);
		}
		return selected;		
	}
	
	public Collection<BinnerAdduct> getAllBinnerAdducts(){
		
		Set<BinnerAdduct> all = new TreeSet<BinnerAdduct>();
		int adductColIndex = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		for(int row=0; row<model.getRowCount(); row++)
			all.add((BinnerAdduct)model.getValueAt(row, adductColIndex));
		
		return all;
	}
	
	public Map<BinnerAdduct, Integer> getSelectedBinnerAdductTierMap(){
		
		Map<BinnerAdduct, Integer> tierMap = new TreeMap<BinnerAdduct, Integer>();
		int[]selectedRows = getSelectedRows();
		if(!showTier || selectedRows.length == 0)
			return tierMap;
		
		int adductColIndex = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		int tierColIndex = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.TIER_COLUMN);
		for(int row : selectedRows) {
			
			int modelRow = convertRowIndexToModel(row);
			BinnerAdduct adduct = (BinnerAdduct)model.getValueAt(modelRow, adductColIndex);
			Integer tier = (Integer)model.getValueAt(modelRow, tierColIndex);
			if(adduct != null)
				tierMap.put(adduct, tier);
		}
		return tierMap;
	}
	
	public Map<BinnerAdduct, Integer> getCompleteBinnerAdductTierMap(){
		
		Map<BinnerAdduct, Integer> tierMap = new TreeMap<BinnerAdduct, Integer>();
		if(!showTier)
			return tierMap;
		
		int adductColIndex = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.BINNER_ADDUCT_COLUMN);
		int tierColIndex = model.getColumnIndex(SimpleBinnerAnnotationsTableModel.TIER_COLUMN);
		for(int row=0; row<model.getRowCount(); row++) {
			
			BinnerAdduct adduct = (BinnerAdduct)model.getValueAt(row, adductColIndex);
			Integer tier = (Integer)model.getValueAt(row, tierColIndex);
			if(adduct != null)
				tierMap.put(adduct, tier);
		}		
		return tierMap;
	}
}
