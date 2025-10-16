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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class RelativeIntensityRenderer extends DefaultTableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 5890896192437376644L;
	private final NumberFormat defaultFormat = MRC2ToolBoxConfiguration.getIntensityFormat();
	private double maxValue;

	public RelativeIntensityRenderer() {
		super();
		setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(CENTER);
        maxValue = 0.0d;
	}

	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());
		
		if(value == null || Double.isNaN((double)value) || (double) value == 0.0d) {
			setText("\t");
			setIcon(null);
			return this;
		}
		if (value instanceof Double) {

			int width = table.getColumnModel().getColumn(column).getPreferredWidth();
			int height = table.getRowHeight(row);
			double relativeValue = 1.0d;
			if(maxValue > 0)
				relativeValue = (double)value/maxValue;

			//	Color backgroundColor = (row % 2 == 0 ? BasicTable.ALTERNATE_ROW_COLOR : BasicTable.WHITE_COLOR);
			//	Icon colorBar = new ColorBar(width, height, relativeValue, backgroundColor);
			setIcon(new ColorBar(width, height, relativeValue, getBackground()));
			setText(defaultFormat.format(value));
		}
		return this;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
}
