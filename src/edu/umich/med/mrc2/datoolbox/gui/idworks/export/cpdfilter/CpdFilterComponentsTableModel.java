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

package edu.umich.med.mrc2.datoolbox.gui.idworks.export.cpdfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CpdFilterComponentsTableModel extends BasicTableModel {

	private static final long serialVersionUID = 1L;
	public static final String INPUT_VALUE_COLUMN = "Input value";
	public static final String FILTER_COMPONENT_COLUMN = "Filter component";

	public CpdFilterComponentsTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(INPUT_VALUE_COLUMN, INPUT_VALUE_COLUMN, String.class, false),	
			new ColumnContext(FILTER_COMPONENT_COLUMN, FILTER_COMPONENT_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromValuePairs(Map<String,String> valuePairs) {

		setRowCount(0);

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<String, String> p : valuePairs.entrySet()) {

			Object[] obj = {
				p.getKey(),
				p.getValue(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}















