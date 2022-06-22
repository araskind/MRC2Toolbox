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

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;

public class CompoundLibraryRenderer extends DefaultTableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -2256032699275318337L;

	private SortProperty field;

	public CompoundLibraryRenderer(SortProperty field) {
		super();
		this.field = field;
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
			return this;
		}
		if (value instanceof CompoundLibrary) {

			if(field.equals(SortProperty.ID))
				setText(((CompoundLibrary) value).getLibraryId());

			if(field.equals(SortProperty.Name))
				setText(((CompoundLibrary) value).getLibraryName());
		}
		return this;
	}
}


