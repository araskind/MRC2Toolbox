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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class LibraryManagerPopupMenu  extends BasicTablePopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4169550796571758831L;
	private static final Icon openLibraryIcon = GuiUtils.getIcon("open", 24);
	private static final Icon newLibraryIcon = GuiUtils.getIcon("newLibrary", 24);
	private static final Icon editLibInfoIcon = GuiUtils.getIcon("editLibrary", 24);	
	private static final Icon duplicateLibraryIcon = GuiUtils.getIcon("duplicateLibrary", 24);
	private static final Icon deleteLibraryIcon = GuiUtils.getIcon("deleteLibrary", 24);
		
	private JMenuItem
		openLibraryMenuItem,
		newLibraryMenuItem,
		editLibraryInfoMenuItem,		
		duplicateLibraryMenuItem,
		deleteLibraryMenuItem;

	public LibraryManagerPopupMenu(
			ActionListener listener,
			BasicTable copyListener) {

		super(listener, copyListener);
		
		openLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.OPEN_LIBRARY_COMMAND.getName(), listener,
				MainActionCommands.OPEN_LIBRARY_COMMAND.getName());
		openLibraryMenuItem.setIcon(openLibraryIcon);
		
		addSeparator();

		newLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.NEW_LIBRARY_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.NEW_LIBRARY_DIALOG_COMMAND.getName());
		newLibraryMenuItem.setIcon(newLibraryIcon);

		editLibraryInfoMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_LIBRARY_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.EDIT_LIBRARY_DIALOG_COMMAND.getName());
		editLibraryInfoMenuItem.setIcon(editLibInfoIcon);
		
		duplicateLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DUPLICATE_LIBRARY_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.DUPLICATE_LIBRARY_DIALOG_COMMAND.getName());
		duplicateLibraryMenuItem.setIcon(duplicateLibraryIcon);
		
		addSeparator();
		
		deleteLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_LIBRARY_COMMAND.getName(), listener,
				MainActionCommands.DELETE_LIBRARY_COMMAND.getName());
		deleteLibraryMenuItem.setIcon(deleteLibraryIcon);	
		
		addCopyBlock();
	}
}






