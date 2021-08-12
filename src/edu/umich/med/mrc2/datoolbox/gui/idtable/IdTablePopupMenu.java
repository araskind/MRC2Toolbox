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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IdTablePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;

	private static final Icon deleteIdIcon = GuiUtils.getIcon("delete", 24);
	private static final Icon deleteAllIdsIcon = GuiUtils.getIcon("deleteCollection", 24);
	private static final Icon goToLibraryIcon = GuiUtils.getIcon("goToLibrary", 24);

	private JMenuItem
		deleteIdMenuItem,
		deleteAllIdsMenuItem,
		goToLibraryMenuItem;

	public IdTablePopupMenu(ActionListener listener) {

		super();

		deleteIdMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.DELETE_IDENTIFICATION_COMMAND.getName());
		deleteIdMenuItem.setIcon(deleteIdIcon);

		deleteAllIdsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName(), listener,
				MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName());
		deleteAllIdsMenuItem.setIcon(deleteAllIdsIcon);

		addSeparator();

		goToLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.GO_TO_LIBRARY_FEATURE_COMMAND.getName(), listener,
				MainActionCommands.GO_TO_LIBRARY_FEATURE_COMMAND.getName());
		goToLibraryMenuItem.setIcon(goToLibraryIcon);
	}
}
