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

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class MSFeatureIdentificationLevelColorRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8798194001665071029L;
	

	public MSFeatureIdentificationLevelColorRenderer() {
		super();
		setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		
		if(value == null) {
			setToolTipText(null);
			setIcon(null);
			return this;
		}
		if (value instanceof MSFeatureIdentificationLevel) {
			MSFeatureIdentificationLevel level = (MSFeatureIdentificationLevel)value;
			if(level.getColorCode() == null) {
				setIcon(null);
				return this;
			}
			else {
				int width = table.getColumnModel().getColumn(column).getPreferredWidth();
				int height = table.getRowHeight(row);
				Color backgroundColor = (row % 2 == 0 ? BasicTable.ALTERNATE_ROW_COLOR : BasicTable.WHITE_COLOR);
				Icon colorBar = new ColorBar(width, height, level.getColorCode(), backgroundColor);
				setIcon(colorBar);
				setToolTipText(level.getName());
			}
		}			
		return this;
	}
}
