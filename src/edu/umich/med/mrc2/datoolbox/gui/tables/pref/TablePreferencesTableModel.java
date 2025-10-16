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

package edu.umich.med.mrc2.datoolbox.gui.tables.pref;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SortOrder;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class TablePreferencesTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6668820289960924964L;
	
	public static final String VISIBLE_COLUMN = "Visible";
	public static final String COLUMN_NAME_COLUMN = "Column name";	
	public static final String SORTING_ORDER_COLUMN = "Sort order";

	public TablePreferencesTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(VISIBLE_COLUMN, "Select visible columns", Boolean.class, true),
			new ColumnContext(COLUMN_NAME_COLUMN, COLUMN_NAME_COLUMN, String.class, false),			
			new ColumnContext(SORTING_ORDER_COLUMN, 
					"Data sorting order for this column (if any)", SortOrder.class, false),
		};
	}

	public void setTableModelFromColumns(TableColumnState[] columns) {

		setRowCount(0);
		if(columns == null || columns.length == 0)
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (TableColumnState column : columns) {

			Object[] obj = {
				column.isVisible(),
				column.getColumnName(),				
				column.getSortOrder(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
