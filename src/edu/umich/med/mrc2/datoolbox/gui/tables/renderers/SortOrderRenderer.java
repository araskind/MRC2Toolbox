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

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SortOrderRenderer  extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6064383951058604434L;
	private static final Icon ascIcon = GuiUtils.getIcon("sortAsc", 16);
	private static final Icon descIcon = GuiUtils.getIcon("sortDesc", 16);

	public SortOrderRenderer() {
		super();
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
		if (value instanceof SortOrder) {
			
			SortOrder order = (SortOrder) value;
			setText(order.name());
			if(order.equals(SortOrder.ASCENDING))
				setIcon(ascIcon);
			else if(order.equals(SortOrder.DESCENDING))
				setIcon(descIcon);
			else {
				setText("");
				setIcon(null);
			}
		}
		return this;
	}
}
