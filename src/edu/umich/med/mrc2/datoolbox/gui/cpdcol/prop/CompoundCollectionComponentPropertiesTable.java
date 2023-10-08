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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataFieldComparator;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class CompoundCollectionComponentPropertiesTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6761893383490446061L;
	
	private CompoundCollectionComponentPropertiesTableModel model;
	private CompoundCollectionComponent compoundCollectionComponent;
	private PropertyEditorDialog propertyEditorDialog;

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
		
		addTablePopupMenu(new PropertiesTablePopupMenu(this, this));
		addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							editSelectedField();
						}
					}
				});
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromCompoundCollectionComponent(
			CompoundCollectionComponent component) {
//		thf.setTable(null);
		this.compoundCollectionComponent = component;
		model.setTableModelFromCompoundCollectionComponent(component);
//		thf.setTable(this);
		tca.adjustColumns();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		super.actionPerformed(event);
		String command = event.getActionCommand();
		
		if(command.equals(MainActionCommands.EDIT_SELECTED_FIELD_COMMAND.getName()))
			editSelectedField();
		
		if(command.equals(MainActionCommands.SAVE_CHANGES_COMMAND.getName()))
			saveChangesToSelectedField();		
	}
	
	private void editSelectedField() {
		
		Entry<CpdMetadataField, String>propEntry =  getSelectedProperty();
		if(propEntry == null)
			return;
		
		propertyEditorDialog = new PropertyEditorDialog(
				propEntry.getKey(), 
				propEntry.getValue(), 
				this);
		
		propertyEditorDialog.setLocationRelativeTo(null);
		propertyEditorDialog.setVisible(true);
	}

	private void saveChangesToSelectedField() {
		// TODO Auto-generated method stub
		
		String newValue = propertyEditorDialog.getPropertyValue();
		if(newValue == null || newValue.isEmpty()) {
			MessageDialog.showErrorMsg("Value can not be empty.", propertyEditorDialog);
			return;
		}		
		try {
			CompoundMultiplexUtils.updateMetadataFieldForComponent(
					compoundCollectionComponent, 
					propertyEditorDialog.getField(), 
					newValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setTableModelFromCompoundCollectionComponent(compoundCollectionComponent);
		propertyEditorDialog.dispose();
	}

	public void clearTable() {
		
		super.clearTable();
		compoundCollectionComponent = null;
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
	
	public Entry<CpdMetadataField, String> getSelectedProperty() {

		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		int modelRow = convertRowIndexToModel(row);
		CpdMetadataField field = (CpdMetadataField)model.getValueAt(modelRow, 
				model.getColumnIndex(CompoundCollectionComponentPropertiesTableModel.PROPERTY_COLUMN));
		String value = (String)model.getValueAt(modelRow, 
				model.getColumnIndex(CompoundCollectionComponentPropertiesTableModel.VALUE_COLUMN));

		Entry<CpdMetadataField, String> tuple = 
				new AbstractMap.SimpleEntry<CpdMetadataField, String>(field, value);

		return tuple;
	}

}
