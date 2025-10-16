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

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.renderer.LookupPaintScale;

import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;

public class McMillanDeltaPercentColorRenderer extends FormattedDecimalRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6782307937197891620L;

	private LookupPaintScale paintScale;
	
	public McMillanDeltaPercentColorRenderer(NumberFormat doubleFormat) {
		
		super(doubleFormat, true);
		setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(CENTER);
		paintScale = ColorUtils.createColorLookupScale(
				0.0d, 
				50.0d, 
				50,
				ColorGradient.REDS,
				ColorScale.LINEAR);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null){
			setText("");
			setIcon(null);
			return this;
		}
		Double mcm = (Double)value;
		try {
			if (mcm <= 0.0d) 
				setIcon(null);
			else {
				int width = table.getColumnModel().
						getColumn(table.convertColumnIndexToModel(column)).getWidth();
				int height = table.getRowHeight(row);
				setIcon(new ColorBar(width, height, 0.0d, (Color)paintScale.getPaint(mcm)));
			}
			this.setText(doubleFormat.format(mcm));
		} 
		catch (Exception e) {

		}
		return this;
	}
}
