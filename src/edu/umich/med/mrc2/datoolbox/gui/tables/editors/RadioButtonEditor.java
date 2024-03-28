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

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

public class RadioButtonEditor extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = -5639973332738850529L;
	public JRadioButton btn;

	@SuppressWarnings("serial")
	public RadioButtonEditor(JCheckBox checkBox) {

		super(checkBox);

		btn = new JRadioButton();
		editorComponent = btn;
        delegate = new EditorDelegate() {

            public void setValue(Object value) {

                boolean selected = false;
                if (value instanceof Boolean) {
                    selected = ((Boolean)value).booleanValue();
                }
                else if (value instanceof String) {
                    selected = value.equals("true");
                }
                btn.setSelected(selected);
            }

            public Object getCellEditorValue() {
                return Boolean.valueOf(btn.isSelected());
            }
        };
        btn.addActionListener(delegate);
        btn.setRequestFocusEnabled(false);
	}
/*
	public Object getCellEditorValue() {

		return btn.isSelected();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value == null)
			return null;

		btn.addActionListener(this);

		if (((Boolean) value).booleanValue())
			btn.setSelected(true);
		else
			btn.setSelected(false);

		return btn;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		stopCellEditing();
	}*/
}
