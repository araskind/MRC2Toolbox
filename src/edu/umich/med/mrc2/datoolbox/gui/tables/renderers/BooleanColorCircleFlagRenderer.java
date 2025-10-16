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

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;

public class BooleanColorCircleFlagRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6288418450082966551L;
	
	private Icon trueIcon;
	private Icon falseIcon;
	
	public BooleanColorCircleFlagRenderer(int size) {
		super();
		trueIcon = new IdLevelIcon(size, Color.GREEN);
		falseIcon = new IdLevelIcon(size, Color.RED);
		setOpaque(true);
	}
	
	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		if(value == null) {
			setIcon(null);
			return this;
		}
		if(value instanceof Boolean) {			
			boolean flag = (Boolean)value;
			if(flag)
				setIcon(trueIcon);
			else
				setIcon(falseIcon);
		}	
		return this;
	}
}
