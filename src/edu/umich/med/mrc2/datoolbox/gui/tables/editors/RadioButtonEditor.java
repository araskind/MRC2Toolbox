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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;

public class RadioButtonEditor extends DefaultCellEditor implements ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -5639973332738850529L;
	
	   private JRadioButton button;
	   
	   public RadioButtonEditor(JCheckBox checkBox) {
	      super(checkBox);
	      button = new JRadioButton();	      
	   }
	   
	   @Override
	   public Component getTableCellEditorComponent(
			   JTable table, Object value, boolean isSelected, int row, int column) {
		   
		 button.addItemListener(this);
         boolean selected = false;
         if (value instanceof Boolean) {
             selected = ((Boolean)value).booleanValue();
         }
         else if (value instanceof String) {
             selected = value.equals("true");
         }
         button.setSelected(selected);
         return button;
	   }
	   
	   @Override
	   public Object getCellEditorValue() {	
		  button.removeItemListener(this);
	      return button.isSelected();
	   }
	   
	   public void itemStateChanged(ItemEvent e) {
	      super.fireEditingStopped();
	   }
}
