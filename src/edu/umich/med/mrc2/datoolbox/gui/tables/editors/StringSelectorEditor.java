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

package edu.umich.med.mrc2.datoolbox.gui.tables.editors;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class StringSelectorEditor extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = 5754878052726312789L;

	/**
	 * @param comboBox
	 */
	public StringSelectorEditor(Collection<String>items, JTable table) {

		super(new JComboBox<String>());
		JComboBox<String> cBox = ((JComboBox<String>)editorComponent);
		cBox.setModel(new SortedComboBoxModel(items));
		cBox.setToolTipText("Click to select value");
		cBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent arg0) {

				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					stopCellEditing();
					((DefaultTableModel) table.getModel()).fireTableDataChanged();
				}
			}
		});
		cBox.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				cBox.showPopup();
			}
		});
	}

	@Override
	public Object getCellEditorValue() {
		return ((JComboBox)editorComponent).getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof String || value == null) {

			JComboBox cBox = ((JComboBox)editorComponent);
			cBox.setSelectedIndex(-1);

			String selectedItem = (String) value;
			if (selectedItem != null)
				cBox.setSelectedItem(selectedItem);
		}
		return editorComponent;
	}
}
