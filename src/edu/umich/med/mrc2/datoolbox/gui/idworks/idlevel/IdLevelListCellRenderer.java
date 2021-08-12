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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ColorBar;

public class IdLevelListCellRenderer extends JLabel implements ListCellRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = -24302043000923092L;

	public IdLevelListCellRenderer() {
		super();
		setFont(new Font("Tahoma", Font.BOLD, 12));
		setOpaque(true);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		
		if(value == null) {
			setText("");
			setIcon(null);
			return this;
		}
		if(value instanceof MSFeatureIdentificationLevel) {
			MSFeatureIdentificationLevel level = (MSFeatureIdentificationLevel)value;
			setFont(new Font("Tahoma", Font.BOLD, 12));
			setText(level.getName());	
			if(level.getColorCode() != null) {
				Icon colorBar = new ColorBar(40, 30, level.getColorCode(), Color.WHITE);
				setIcon(colorBar);
			}
			else {
				Icon colorBar = new ColorBar(40, 30, Color.WHITE, Color.WHITE);
				setIcon(colorBar);
			}
		}
		return this;
	}
}
