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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.prop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataFieldCategory;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundCollectionComponentPropertiesTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2715013996794159556L;

	public static final String CATEGORY_COLUMN = "Category";
	public static final String PROPERTY_COLUMN = "Property";
	public static final String VALUE_COLUMN = "Value";
	
	public CompoundCollectionComponentPropertiesTableModel() {
		super();
		columnArray = new ColumnContext[] {

				new ColumnContext(CATEGORY_COLUMN, "Metadata category", CpdMetadataFieldCategory.class, false),
				new ColumnContext(PROPERTY_COLUMN, PROPERTY_COLUMN, CpdMetadataField.class, false), 
				new ColumnContext(VALUE_COLUMN, VALUE_COLUMN, String.class, false),			
		};
	}

	public void setTableModelFromCompoundCollectionComponent(
			CompoundCollectionComponent component) {
		
		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();		

		for(Entry<CpdMetadataField, String> prop : component.getMetadata().entrySet()){

			Object[] obj = {
					prop.getKey().getCategory(),
					prop.getKey(),
					prop.getValue(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

}
