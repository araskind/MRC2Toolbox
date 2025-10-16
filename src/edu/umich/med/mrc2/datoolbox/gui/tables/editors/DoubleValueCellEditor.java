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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.math.NumberUtils;


public class DoubleValueCellEditor extends DefaultCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JFormattedTextField textField;
	
	public DoubleValueCellEditor(NumberFormat format) {
		
		super(new JFormattedTextField(format));
        textField = (JFormattedTextField) getComponent();
        textField.setBorder(new LineBorder(Color.RED));

        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "validate");
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "validate");
        textField.getActionMap().put("validate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isValidValue(textField.getText())) {
                    JOptionPane.showMessageDialog(textField, "Invalid input!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    textField.selectAll(); // Keep focus on the invalid input
                } else {
                    stopCellEditing(); // Commit the valid value
                }
            }
        });
	}
	
    @Override
    public Object getCellEditorValue() {
        return NumberUtils.createDouble(textField.getText());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText(value != null ? value.toString() : "");
        return textField;
    }

    @Override
    public boolean stopCellEditing() {
        if (!isValidValue(textField.getText())) {
            JOptionPane.showMessageDialog(textField, "Invalid input!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false; // Prevent stopping if invalid
        }
        return super.stopCellEditing();
    }

    private boolean isValidValue(String text) {
    	return NumberUtils.isCreatable(text);
    }
}
