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

package edu.umich.med.mrc2.datoolbox.gui.tables.pref;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class TablePreferencesPopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;

	private static final Icon addToSorterIcon = GuiUtils.getIcon("addWorklist", 24);
	private JMenuItem addToSorterMenuItem;

	public TablePreferencesPopupMenu(ActionListener listener) {

		super();
		addToSorterMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_COLUMN_TO_SORTER_COMMAND.getName(), listener,
				MainActionCommands.ADD_COLUMN_TO_SORTER_COMMAND.getName());
		addToSorterMenuItem.setIcon(addToSorterIcon);
	}
}


