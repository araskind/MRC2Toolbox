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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.gui.io.excel.ReportDataType;

public class ReportDataTypeRenderer extends DefaultTableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -6292735841674485351L;
	private JComboBox<ReportDataType> comboBox = new JComboBox<ReportDataType>();


	/**
	 * @param comboBox
	 */
	public ReportDataTypeRenderer() {

		super();
		comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(ReportDataType.values()));
		comboBox.setToolTipText("Click to select value");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null){
			
			if(value == null)
				comboBox.setSelectedIndex(-1);
			
			return this;
		}
		if (value instanceof ReportDataType)
			comboBox.setSelectedItem((ReportDataType) value);
		
		return comboBox;
	}
}
