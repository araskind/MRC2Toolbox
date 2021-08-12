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
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class FormattedDecimalRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1716045593159599309L;
	protected NumberFormat doubleFormat;
	protected boolean hideZeros;
	protected boolean switchToScientificNotation;
	
	private static final DecimalFormat sciFormatter = new DecimalFormat("0.###E0");
	
	public FormattedDecimalRenderer(NumberFormat doubleFormat) {
		this(doubleFormat, false, false);
	}

	public FormattedDecimalRenderer(NumberFormat doubleFormat, boolean hideZeros) {		
		this(doubleFormat, hideZeros, false);
	}		

	public FormattedDecimalRenderer(NumberFormat doubleFormat, boolean hideZeros, boolean switchToScientificNotation) {
		super();
		this.doubleFormat = doubleFormat;
		this.hideZeros = hideZeros;
		this.switchToScientificNotation = switchToScientificNotation;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
		if(value == null || (hideZeros && (double) value == 0.0d)) {
			this.setText("");
			return this;
		}
		double numValue = (double) value;
		try {
			if(switchToScientificNotation && (numValue < 0.001d || numValue > 1000.d))
				this.setText(sciFormatter.format(value));
			else
				this.setText(doubleFormat.format(value));
		} catch (Exception e) {
			//	
		}
		return this;
	}
}
