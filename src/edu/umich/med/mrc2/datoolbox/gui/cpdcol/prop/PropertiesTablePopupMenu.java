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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.prop;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class PropertiesTablePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;
	private static final Icon copyAllIcon = GuiUtils.getIcon("copyWorklistToClipboard", 24);
	private static final Icon copySelectedIcon = GuiUtils.getIcon("copySelectedToClipboard", 24);
	private static final Icon editIcon = GuiUtils.getIcon("edit", 24);

	private JMenuItem 
		copySelectedWithHeaderMenuItem,
		copyAllWithHeaderMenuItem,
		editSelectedPropertyMenuItem;
	
	public PropertiesTablePopupMenu(ActionListener listener) {

		super();
		copySelectedWithHeaderMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_SELECTED_TABLE_ROWS_COMMAND.getName(), listener,
				MainActionCommands.COPY_SELECTED_TABLE_ROWS_COMMAND.getName());
		copySelectedWithHeaderMenuItem.setIcon(copySelectedIcon);
		
		copyAllWithHeaderMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_VISIBLE_TABLE_ROWS_COMMAND.getName(), listener,
				MainActionCommands.COPY_VISIBLE_TABLE_ROWS_COMMAND.getName());
		copyAllWithHeaderMenuItem.setIcon(copyAllIcon);
		
		addSeparator();
		
		editSelectedPropertyMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_SELECTED_FIELD_COMMAND.getName(), listener,
				MainActionCommands.EDIT_SELECTED_FIELD_COMMAND.getName());
		editSelectedPropertyMenuItem.setIcon(editIcon);
	}
}



