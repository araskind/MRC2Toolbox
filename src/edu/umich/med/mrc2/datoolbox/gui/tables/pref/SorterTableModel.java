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

import javax.swing.SortOrder;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SorterTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2997991798374188031L;
	public static final String COLUMN_NAME_COLUMN = "Column name";
	public static final String SORTING_ORDER_COLUMN = "Sort order";

	public SorterTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(COLUMN_NAME_COLUMN, String.class, false),			
			new ColumnContext(SORTING_ORDER_COLUMN, SortOrder.class, true),
		};
	}

	public void setTableModelFromColumns(TableColumnState[] columns) {

		setRowCount(0);

		for (TableColumnState column : columns) {
			
			if(column.getSortOrder().equals(SortOrder.UNSORTED))
				column.setSortOrder(SortOrder.ASCENDING);

			Object[] obj = {
				column.getColumnName(),				
				column.getSortOrder(),
			};
			super.addRow(obj);
		}
	}

	public void addColumnToSorter(TableColumnState columnState) {

		if(columnState.getSortOrder().equals(SortOrder.UNSORTED))
			columnState.setSortOrder(SortOrder.ASCENDING);
		
		Object[] obj = {
				columnState.getColumnName(),				
				columnState.getSortOrder(),
		};
		super.addRow(obj);
	}
}
