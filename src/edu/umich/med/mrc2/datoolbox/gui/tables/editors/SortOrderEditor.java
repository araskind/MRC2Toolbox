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
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SortOrderEditor  extends DefaultCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3078189404930737221L;
	private static final Icon ascIcon = GuiUtils.getIcon("sortAsc", 16);
	private static final Icon descIcon = GuiUtils.getIcon("sortDesc", 16);

	/**
	 * @param comboBox
	 */
	public SortOrderEditor() {

		super(new JComboBox<>());
		@SuppressWarnings("unchecked")
		JComboBox<SortOrder> comboBox = ((JComboBox<SortOrder>)editorComponent);
		comboBox.setModel(new DefaultComboBoxModel<SortOrder>(new SortOrder[] {SortOrder.ASCENDING, SortOrder.DESCENDING}));
		comboBox.setToolTipText("Click to select value");
		comboBox.setRenderer(new ItemRenderer());
	}

	@Override
	public Object getCellEditorValue() {
		return ((JComboBox)editorComponent).getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		JComboBox<SortOrder> comboBox = ((JComboBox<SortOrder>)editorComponent);
//		if(value == null)
//			comboBox.setSelectedItem(SortOrder.UNSORTED);

		if (value instanceof SortOrder)
			comboBox.setSelectedItem((SortOrder) value);
		
		return comboBox;
	}
	
	@SuppressWarnings("serial")
	private class ItemRenderer extends BasicComboBoxRenderer {
		  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			  
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof SortOrder) {

					SortOrder order = (SortOrder) value;
					setText(order.name());
					if (order.equals(SortOrder.ASCENDING))
						setIcon(ascIcon);
					else if (order.equals(SortOrder.DESCENDING))
						setIcon(descIcon);
					else
						setIcon(null);
				}
				return this;
			}
		}
}
