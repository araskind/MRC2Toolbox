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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DoubleValueTableModel extends BasicTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String VALUE_COLUMN = "Value";

	public DoubleValueTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(VALUE_COLUMN, VALUE_COLUMN, Double.class, true),
		};
	}
	
	public void addNewRow() {		
		addRow(new Object[] {null});
	}

	public void setTableModelFromValues(Collection<Double> values) {
		
		setRowCount(0);
		if(values == null || values.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(Double d : values)
			rowData.add(new Object[] {d});
		
		if(!rowData.isEmpty())
			addRows(rowData);		
	}
}
