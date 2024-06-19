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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class CCComponentMetadataFieldSelectionTable extends BasicTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4327961356138766378L;
	
	public CCComponentMetadataFieldSelectionTable() {
		super();
		model = new CCComponentMetadataFieldSelectionTableModel();
		setModel(model);	
		WordWrapCellRenderer wwr = new WordWrapCellRenderer();
		columnModel.getColumnById(
				CCComponentMetadataFieldSelectionTableModel.PROPERTY_COLUMN).
				setCellRenderer(wwr);
		columnModel.getColumnById(
				CCComponentMetadataFieldSelectionTableModel.SELECTED_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(
				CCComponentMetadataFieldSelectionTableModel.CATEGORY_COLUMN).setMaxWidth(150);
		
		fixedWidthColumns.add(model.getColumnIndex(
				CCComponentMetadataFieldSelectionTableModel.SELECTED_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(
				CCComponentMetadataFieldSelectionTableModel.CATEGORY_COLUMN));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromPropertyMap(
			Map<CpdMetadataField,Boolean>selectedPropertiesMap) {
		((CCComponentMetadataFieldSelectionTableModel)model).
				setTableModelFromPropertyMap(selectedPropertiesMap);
		adjustColumns();
	}
	
	public Collection<CpdMetadataField> getSelectedProperties() {

		Collection<CpdMetadataField>propertyMap = 
				new ArrayList<CpdMetadataField>();

		int propertyCol = model.getColumnIndex(
				CCComponentMetadataFieldSelectionTableModel.PROPERTY_COLUMN);
		int selectedCol = model.getColumnIndex(
				CCComponentMetadataFieldSelectionTableModel.SELECTED_COLUMN);
		
		for(int i=0; i<model.getRowCount(); i++) {
			
			if((Boolean)model.getValueAt(i, selectedCol))
			propertyMap.add((CpdMetadataField)model.getValueAt(i, propertyCol));
		}
		return propertyMap;
	}
}
