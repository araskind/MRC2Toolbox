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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class CompoundPopupMenu  extends BasicTablePopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -4582990028092698728L;
	private static final Icon addToLibraryIcon = GuiUtils.getIcon("databaseToLibrary", 24);
	private static final Icon copyAccessionIcon = GuiUtils.getIcon("copy", 24);
	private static final Icon copyIdentityIcon = GuiUtils.getIcon("copyWithHeader", 24);
	
	private JMenu copyPropertiesMenu;
	
	private JMenuItem
		addSelectedToLibraryMenuItem,
		copyIdentityMenuItem;

	public CompoundPopupMenu(			
			ActionListener mainActionListener,
			BasicTable copyListener) {

		super(mainActionListener, copyListener);

		addSelectedToLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName(), mainActionListener,
				MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName());
		addSelectedToLibraryMenuItem.setIcon(addToLibraryIcon);

		addSeparator();
		
		copyPropertiesMenu = new JMenu("Copy compound property");
		copyPropertiesMenu.setIcon(copyAccessionIcon);
		GuiUtils.addMenuItem(copyPropertiesMenu,
				MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName(), mainActionListener,
				MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName());
		GuiUtils.addMenuItem(copyPropertiesMenu,
				MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName(), mainActionListener,
				MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName());
		GuiUtils.addMenuItem(copyPropertiesMenu,
				MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName(), mainActionListener,
				MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName());
		GuiUtils.addMenuItem(copyPropertiesMenu,
				MainActionCommands.COPY_COMPOUND_INCHI_KEY_COMMAND.getName(), mainActionListener,
				MainActionCommands.COPY_COMPOUND_INCHI_KEY_COMMAND.getName());
		GuiUtils.addMenuItem(copyPropertiesMenu,
				MainActionCommands.COPY_COMPOUND_SMILES_COMMAND.getName(), mainActionListener,
				MainActionCommands.COPY_COMPOUND_SMILES_COMMAND.getName());
		
		add(copyPropertiesMenu);

		copyIdentityMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_COMPOUND_IDENTITY_COMMAND.getName(), mainActionListener,
				MainActionCommands.COPY_COMPOUND_IDENTITY_COMMAND.getName());
		copyIdentityMenuItem.setIcon(copyIdentityIcon);
		
		addCopyBlock();
	}
}






