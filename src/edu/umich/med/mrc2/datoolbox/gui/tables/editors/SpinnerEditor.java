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

import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class SpinnerEditor extends DefaultCellEditor {
	
	private static final long serialVersionUID = 1L;
	private JSpinner spinner;

	public SpinnerEditor(int min, int max) {
		
		super(new JTextField());
		spinner = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof Integer)
			spinner.setValue(value);
		
		return spinner;
	}
	
	@Override
	public Object getCellEditorValue() {
		return spinner.getValue();
	}
}
