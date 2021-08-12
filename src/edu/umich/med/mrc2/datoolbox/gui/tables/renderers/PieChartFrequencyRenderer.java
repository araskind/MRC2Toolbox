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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.gui.plot.PieChart;

public class PieChartFrequencyRenderer extends DefaultTableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -6424288724361691172L;
	private PieChart pieChart;
	private final NumberFormat defaultFormat = NumberFormat.getPercentInstance();	
	
	public PieChartFrequencyRenderer() {

		super();
		defaultFormat.setMinimumFractionDigits(2);
		defaultFormat.setMaximumFractionDigits(2);
		setBorder(new EmptyBorder(2,2,2,2));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		if (value instanceof Double) {

			if (value.equals(Double.NaN)) {
				setText("\t");
				setIcon(null);
			}
			else {
				setText("\t" + defaultFormat.format(value));
				setIcon(new SmallPie(table.getRowHeight()-4, (double) value));
			}
		}
		return this;
	}
}
