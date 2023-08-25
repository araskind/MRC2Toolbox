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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class ComboBoxRendererWithIcons extends BasicComboBoxRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7212012880658882182L;
	private Map<Object,Icon> imageMap;
	
	public ComboBoxRendererWithIcons(Map<Object, Icon> imageMap) {
		super();
		this.imageMap = imageMap;
	}

	@Override
	public Component getListCellRendererComponent(
			JList list, 
			Object value, 
			int index, 
			boolean isSelected,
			boolean cellHasFocus) {

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setFont(list.getFont());
		setText(value.toString());
		Icon icon = null;
		if(imageMap != null)
			icon = imageMap.get(value);

		setIcon(icon);
		return this;
	}
}
