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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundPropertiesTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2715013996794159556L;

	public static final String PROPERTY_COLUMN = "Property";
	public static final String VALUE_COLUMN = "Value";
	public static final String VALUE_TYPE_COLUMN = "Value type";

	private static final ColumnContext[] columnArray = new ColumnContext[] {

			new ColumnContext(PROPERTY_COLUMN, PROPERTY_COLUMN, String.class, false), 
			new ColumnContext(VALUE_COLUMN, VALUE_COLUMN, String.class, false),
			new ColumnContext(VALUE_TYPE_COLUMN, VALUE_TYPE_COLUMN, String.class, false)
	};
}
