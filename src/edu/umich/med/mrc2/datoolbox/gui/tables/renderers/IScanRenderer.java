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

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import umich.ms.datatypes.scan.IScan;

public class IScanRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3031237037375192757L;
	private SortProperty field;

	public IScanRenderer(SortProperty field) {
		super();
		this.field = field;
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if(value == null) {
			setText("");
			return this;
		}
		if (value instanceof IScan) {

			IScan scan = (IScan) value;
			if(field.equals(SortProperty.RT))
				setText(MRC2ToolBoxConfiguration.getRtFormat().format(scan.getRt()));
			else if(field.equals(SortProperty.scanNumber))
				setText(Integer.toString(scan.getNum()));
			else
				setText("");
			
			if(scan.getMsLevel() == 1)
				setForeground(Color.BLUE);
			else
				setForeground(Color.RED);
		}
		return this;
	}
}