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

package edu.umich.med.mrc2.datoolbox.gui.tables.editors;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.umich.med.mrc2.datoolbox.gui.io.excel.ReportDataType;

public class ReportDataTypeEditor  extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = 2021614643128963942L;
	private JComboBox<ReportDataType> comboBox = new JComboBox<ReportDataType>();


	/**
	 * @param comboBox
	 */
	public ReportDataTypeEditor() {

		super(new JComboBox<>());
		comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(ReportDataType.values()));
		comboBox.setToolTipText("Click to select value");
	}

	@Override
	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if(value == null)
			comboBox.setSelectedIndex(-1);

		if (value instanceof ReportDataType) {

			ReportDataType selectedFile = (ReportDataType) value;
			comboBox.setSelectedItem(selectedFile);
			comboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent arg0) {

					if (arg0.getStateChange() == ItemEvent.SELECTED) {
						stopCellEditing();
						((DefaultTableModel) table.getModel()).fireTableDataChanged();
					}
				}
			});
		}
		return comboBox;
	}
}
