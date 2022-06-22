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
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PercentValueRenderer extends DecimalAlignRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -397304660915803312L;
	private final NumberFormat defaultFormat = NumberFormat.getPercentInstance();

	public PercentValueRenderer() {

		super();
		defaultFormat.setMinimumFractionDigits(2);
		defaultFormat.setMaximumFractionDigits(2);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null || value.equals(Double.NaN)){
			setText("\t");
			return this;
		}
		if (value instanceof Double) 
			setText("\t" + defaultFormat.format(value));
		
		return this;
	}
}
