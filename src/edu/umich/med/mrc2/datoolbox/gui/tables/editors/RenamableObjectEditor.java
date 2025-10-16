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

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import edu.umich.med.mrc2.datoolbox.data.Renamable;

public class RenamableObjectEditor extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = 8672443776041731993L;
	private Renamable rnObject;

	public RenamableObjectEditor(JTextField textField) {
		super(textField);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof Renamable) {

			rnObject = (Renamable) value;
			((JTextField)editorComponent).setText(rnObject.getName());
		}
		else {
			rnObject = null;
			((JTextField)editorComponent).setText(value.toString());
		}
		return editorComponent;
	}

	@Override
    public Object getCellEditorValue() {
        return rnObject;
    }

	@Override
    public boolean stopCellEditing() {

		if(rnObject != null)
			rnObject.setName(getNewName());

        return delegate.stopCellEditing();
    }

	public Renamable getRnObject() {
		return rnObject;
	}

	public String getNewName() {
		return ((JTextField)editorComponent).getText().trim();
	}
}
