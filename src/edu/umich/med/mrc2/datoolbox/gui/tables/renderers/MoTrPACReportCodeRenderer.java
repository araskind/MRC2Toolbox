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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MoTrPACReportCodeRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5557806455244344088L;

	private static final Icon checkedIcon = GuiUtils.getIcon("level", 16);
	private static final Icon uncheckedIcon = GuiUtils.getIcon("levelInactive", 16);
	
	public MoTrPACReportCodeRenderer() {
		super();
		setBorder(new EmptyBorder(1, 5, 1, 5));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    	Component rendererComponent = table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
    	setForeground(rendererComponent.getForeground());
    	setBackground(rendererComponent.getBackground());    	
		if(value == null) {
			setText("");
			setIcon(null);
			return this;
		}
		if (value instanceof MoTrPACReportCode) {
			
			MoTrPACReportCode code = (MoTrPACReportCode)value;
			setText(" " + code.toString());
			if (code.getOptionCode().equals("0")) 				
				setIcon(uncheckedIcon);			
			else 
				setIcon(checkedIcon);			
		}
		return this;
	}
}
