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

package edu.umich.med.mrc2.datoolbox.gui.tables.pref;

import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.SortOrderEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SortOrderRenderer;

public class SorterTable extends BasicTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1947571928720135555L;
	private SorterTableModel model;
	
	public SorterTable() {
		super();
		//  Do not allow table column reordering
		getTableHeader().setReorderingAllowed(false);		 
		//  Disable row sorting 
		setRowSorter(null);
		getTableHeader().setEnabled(false);
		
		model = new SorterTableModel();
		setModel(model);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		setDefaultEditor(SortOrder.class, new SortOrderEditor());
		setDefaultRenderer(SortOrder.class, new SortOrderRenderer());
	}

	public void setTableModelFromColumns(TableColumnState[] columns) {
		model.setTableModelFromColumns(columns);
	}
	
	public void addColumnToSorter(TableColumnState columnState) {
		model.addColumnToSorter(columnState);
	}
	
	public boolean containsSorter(TableColumnState newSorter) {
		
		int nameColumn = model.getColumnIndex(SorterTableModel.COLUMN_NAME_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			String name = (String)model.getValueAt(i, nameColumn);
			if(newSorter.getColumnName().equals(name))
				return true;		
		}
		return false;
	}
	
	public void removeSelectedSorter() {
		
		int selectedRow = getSelectedRow();
		if(selectedRow == -1)
			return;
		
		model.removeRow(convertRowIndexToModel(selectedRow));
	}
	
	public void moveSelectedSorterUp() {
		
		int selectedRow = getSelectedRow();
		int rowCount = getRowCount();
		if(selectedRow == -1 || selectedRow == 0 || rowCount < 2)
			return;
		
		TableColumnState[] sorters = getColumnSettings();
		TableColumnState[] newSorters = new TableColumnState[rowCount];
		for(int i=0; i<rowCount; i++) {
			
			if(i == selectedRow)
				newSorters[i-1] = sorters[i];
			else if(i == selectedRow - 1)
				newSorters[i+1] = sorters[i];
			else
				newSorters[i] = sorters[i];	
		}	
		model.setTableModelFromColumns(newSorters);
	}
	
	public void moveSelectedSorterDown() {
		
		int selectedRow = getSelectedRow();
		int rowCount = getRowCount();
		if(selectedRow == -1  || selectedRow == getRowCount()-1 || rowCount < 2)
			return;
		
		TableColumnState[] sorters = getColumnSettings();
		TableColumnState[] newSorters = new TableColumnState[rowCount];
		for(int i=0; i<rowCount; i++) {
			
			if(i == selectedRow)
				newSorters[i+1] = sorters[i];
			else if(i == selectedRow + 1)
				newSorters[i-1] = sorters[i];
			else
				newSorters[i] = sorters[i];	
		}	
		model.setTableModelFromColumns(newSorters);
	}

	public TableColumnState[] getColumnSettings() {
		
		int nameColumn = model.getColumnIndex(SorterTableModel.COLUMN_NAME_COLUMN);
		int orderColumn = model.getColumnIndex(SorterTableModel.SORTING_ORDER_COLUMN);
		
		TableColumnState[] settings = new TableColumnState[model.getRowCount()];
		for(int i=0; i<model.getRowCount(); i++) {
			
			String name = (String)model.getValueAt(i, nameColumn);
			SortOrder order = (SortOrder)model.getValueAt(i, orderColumn);
			settings[i] = new TableColumnState(name, true, order, i);		
		}		
		return settings;		
	}
}

