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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.prop;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataFieldComparator;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class CompoundCollectionComponentPropertiesTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6761893383490446061L;
	
	private CompoundCollectionComponentPropertiesTableModel model;

	public CompoundCollectionComponentPropertiesTable() {
		super();
		model = new CompoundCollectionComponentPropertiesTableModel();
		setModel(model);	
		WordWrapCellRenderer wwr = new WordWrapCellRenderer();
		columnModel.getColumnById(
				CompoundCollectionComponentPropertiesTableModel.PROPERTY_COLUMN).
				setCellRenderer(wwr);
		columnModel.getColumnById(
				CompoundCollectionComponentPropertiesTableModel.VALUE_COLUMN).
				setCellRenderer(wwr);
		
		addTablePopupMenu(new PropertiesTablePopupMenu(this));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromCompoundCollectionComponent(
			CompoundCollectionComponent component) {
		thf.setTable(null);
		model.setTableModelFromCompoundCollectionComponent(component);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public Map<CpdMetadataField, String> getSelectedProperties() {

		Map<CpdMetadataField, String>propertyMap = 
				new TreeMap<CpdMetadataField,String>(new CpdMetadataFieldComparator());
		if(getSelectedRowCount() == 0)
			return propertyMap;

		int propertyCol = model.getColumnIndex(CompoundCollectionComponentPropertiesTableModel.PROPERTY_COLUMN);
		int valueCol = model.getColumnIndex(CompoundCollectionComponentPropertiesTableModel.VALUE_COLUMN);
		for(int i : getSelectedRows()) {
			int modelRow = convertRowIndexToModel(i);
			propertyMap.put(
					(CpdMetadataField)model.getValueAt(modelRow, propertyCol), 
					(String)model.getValueAt(modelRow, valueCol));
		}
		return propertyMap;
	}

}
