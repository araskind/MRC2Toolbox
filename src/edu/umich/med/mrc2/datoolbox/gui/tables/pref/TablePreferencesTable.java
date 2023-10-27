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
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SortOrderRenderer;

public class TablePreferencesTable extends BasicTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1119544379104686403L;

	public TablePreferencesTable() {
		super();
		//  Do not allow table column reordering
		getTableHeader().setReorderingAllowed(false);		 
		//  Disable row sorting 
		setRowSorter(null);
		getTableHeader().setEnabled(false);
		
		model = new TablePreferencesTableModel();
		setModel(model);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(SortOrder.class, new SortOrderRenderer());
	}

	public void setTableModelFromColumns(TableColumnState[] columns) {
		((TablePreferencesTableModel)model).setTableModelFromColumns(columns);
	}
	
	public TableColumnState getSelectedColumnSettings() {
		
		int selectedRow = getSelectedRow();
		if(selectedRow == -1)
			return null;
		
		int row = convertRowIndexToModel(selectedRow);		
		String name = (String)model.getValueAt(row, 
				model.getColumnIndex(TablePreferencesTableModel.COLUMN_NAME_COLUMN));
		Boolean isVisible = (Boolean)model.getValueAt(row,  
				model.getColumnIndex(TablePreferencesTableModel.VISIBLE_COLUMN));
		SortOrder order = (SortOrder)model.getValueAt(row,  
				model.getColumnIndex(TablePreferencesTableModel.SORTING_ORDER_COLUMN));
		return new TableColumnState(name, isVisible, order);	
	}

	public TableColumnState[] getColumnSettings() {
		
		int nameColumn = model.getColumnIndex(TablePreferencesTableModel.COLUMN_NAME_COLUMN);
		int visibleColumn = model.getColumnIndex(TablePreferencesTableModel.VISIBLE_COLUMN);
		int orderColumn = model.getColumnIndex(TablePreferencesTableModel.SORTING_ORDER_COLUMN);
		
		TableColumnState[] settings = new TableColumnState[model.getRowCount()];
		for(int i=0; i<model.getRowCount(); i++) {
			
			String name = (String)model.getValueAt(i, nameColumn);
			Boolean isVisible = (Boolean)model.getValueAt(i, visibleColumn);
			SortOrder order = (SortOrder)model.getValueAt(i, orderColumn);
			settings[i] = new TableColumnState(name, isVisible, order);		
		}		
		return settings;		
	}
}

