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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PropertiesTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5122553444280341859L;
	public static final String PROPERTY_COLUMN = "Property";
	public static final String VALUE_COLUMN = "Value";

	public PropertiesTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PROPERTY_COLUMN, Object.class, false),
			new ColumnContext(VALUE_COLUMN, Integer.class, false)
		};
	}

	public void setTableModelFromPropertyMap(
			Map<? extends Object,? extends Object>properties) {

		setRowCount(0);
		if(properties == null || properties.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<? extends Object, ? extends Object> entry : properties.entrySet()) {

			Object[] obj = { 
					entry.getKey(), 
					entry.getValue(), 
				};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}
