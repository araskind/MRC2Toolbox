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

package edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FormulaTablePopupMenu extends BasicTablePopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = 1429925123500070269L;
	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 24);
	private static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 24);
//	private static final Icon copyFormulaIcon = GuiUtils.getIcon("clipBoard", 24);
//	private static final Icon copyLineIcon = GuiUtils.getIcon("clipBoard", 24);

	private JMenuItem databaseLookupMenuItem;
	private JMenuItem libraryLookupMenuItem;
//	private JMenuItem copyFormulaMenuItem;
//	private JMenuItem copyLineMenuItem;

	public FormulaTablePopupMenu(ActionListener listener, BasicTable copyListener) {

		super(listener, copyListener);

		databaseLookupMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FORMULA_AGAINST_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FORMULA_AGAINST_DATABASE_COMMAND.getName());
		databaseLookupMenuItem.setIcon(searchDatabaseIcon);

		libraryLookupMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FORMULA_AGAINST_LIBRARY_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FORMULA_AGAINST_LIBRARY_COMMAND.getName());
		libraryLookupMenuItem.setIcon(searchLibraryIcon);

//		this.addSeparator();
//
//		copyFormulaMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.COPY_FORMULA_COMMAND.getName(), listener,
//				MainActionCommands.COPY_FORMULA_COMMAND.getName());
//		copyFormulaMenuItem.setIcon(copyFormulaIcon);
//
//		copyLineMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.COPY_LINE_COMMAND.getName(), listener,
//				MainActionCommands.COPY_LINE_COMMAND.getName());
//		copyLineMenuItem.setIcon(copyLineIcon);
		
		addCopyBlock();
	}
}
