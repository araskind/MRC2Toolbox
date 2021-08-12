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
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleComboboxRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class ExperimentalSampleSelectorEditor extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = 5754878052726312789L;

	/**
	 * @param comboBox
	 */
	public ExperimentalSampleSelectorEditor(Collection<ExperimentalSample>samples, JTable table) {

		super(new JComboBox<>());
		JComboBox cBox = ((JComboBox)editorComponent);
		cBox.setRenderer(new ExperimentalSampleComboboxRenderer());
		cBox.setModel(new SortedComboBoxModel(samples));
		cBox.setToolTipText("Click to select value");
		cBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent arg0) {

				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					stopCellEditing();
					((DefaultTableModel) table.getModel()).fireTableDataChanged();
				}
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

		if (value instanceof ExperimentalSample || value == null) {

			JComboBox cBox = ((JComboBox)editorComponent);
			cBox.setSelectedIndex(-1);

			ExperimentalSample selectedSample = (ExperimentalSample) value;
			if (selectedSample != null)
				cBox.setSelectedItem(selectedSample);
		}
		return editorComponent;
	}
}
