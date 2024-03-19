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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class BasicTablePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	protected static final long serialVersionUID = -6064748415801630180L;
	
	protected static final Icon copyAllIcon = GuiUtils.getIcon("copyWorklistToClipboard", 24);
	protected static final Icon copySelectedIcon = GuiUtils.getIcon("copySelectedToClipboard", 24);
	protected static final Icon copyValueIcon = GuiUtils.getIcon("copy", 24);

	protected ActionListener mainActionListener;
	protected BasicTable copyListener;
	
	protected JMenuItem 
		copySelectedValueMenuItem,
		copySelectedWithHeaderMenuItem,
		copyAllWithHeaderMenuItem;
	
	public BasicTablePopupMenu(
			ActionListener mainActionListener,
			BasicTable copyListener) {

		super();
		this.mainActionListener = mainActionListener;
		this.copyListener = copyListener;
	}
	
	public void addCopyBlock() {
		
		if(copyListener == null)
			return;
		
		addSeparator();
		
		copySelectedValueMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_SELECTED_VALUE_COMMAND.getName(), copyListener,
				MainActionCommands.COPY_SELECTED_VALUE_COMMAND.getName());
		copySelectedValueMenuItem.setIcon(copyValueIcon);
		
		copySelectedWithHeaderMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_SELECTED_TABLE_ROWS_COMMAND.getName(), copyListener,
				MainActionCommands.COPY_SELECTED_TABLE_ROWS_COMMAND.getName());
		copySelectedWithHeaderMenuItem.setIcon(copySelectedIcon);
		
		copyAllWithHeaderMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_VISIBLE_TABLE_ROWS_COMMAND.getName(), copyListener,
				MainActionCommands.COPY_VISIBLE_TABLE_ROWS_COMMAND.getName());
		copyAllWithHeaderMenuItem.setIcon(copyAllIcon);		
	}
	
	
	protected JMenuItem addItem(JMenu menu, String title, String command, Icon defaultIcon) {

		JMenuItem item = new JMenuItem(title);
		item.setActionCommand(command);
		item.addActionListener(mainActionListener);
		item.setIcon(defaultIcon);
		menu.add(item);
		return item;
	}
}



