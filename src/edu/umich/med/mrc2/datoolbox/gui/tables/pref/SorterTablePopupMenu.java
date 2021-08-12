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

package edu.umich.med.mrc2.datoolbox.gui.tables.pref;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SorterTablePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;

	private static final Icon moveSorterUpIcon = GuiUtils.getIcon("goUp", 24);
	private static final Icon moveSorterDownIcon = GuiUtils.getIcon("goDown", 24);
	private static final Icon deleteSorterIcon = GuiUtils.getIcon("delete", 24);

	private JMenuItem moveSorterUpMenuItem;
	private JMenuItem moveSorterDownMenuItem;
	private JMenuItem deleteSorterMenuItem;

	public SorterTablePopupMenu(ActionListener listener) {

		super();

		moveSorterUpMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.INCREASE_SORTER_PRIORITY_COMMAND.getName(), listener,
				MainActionCommands.INCREASE_SORTER_PRIORITY_COMMAND.getName());
		moveSorterUpMenuItem.setIcon(moveSorterUpIcon);

		moveSorterDownMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DECREASE_SORTER_PRIORITY_COMMAND.getName(), listener,
				MainActionCommands.DECREASE_SORTER_PRIORITY_COMMAND.getName());
		moveSorterDownMenuItem.setIcon(moveSorterDownIcon);
		
		addSeparator();
		
		deleteSorterMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.REMOVE_COLUMN_FROM_SORTER_COMMAND.getName(), listener,
				MainActionCommands.REMOVE_COLUMN_FROM_SORTER_COMMAND.getName());
		deleteSorterMenuItem.setIcon(deleteSorterIcon);
	}
}


