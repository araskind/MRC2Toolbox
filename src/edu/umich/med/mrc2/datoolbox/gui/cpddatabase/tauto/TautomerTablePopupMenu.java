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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.tauto;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class TautomerTablePopupMenu  extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -4582990028092698728L;

	private static final Icon addIcon = GuiUtils.getIcon("addCompound", 24);
	private static final Icon replaceIcon = GuiUtils.getIcon("replaceCompound", 24);
	private static final Icon copyAccessionIcon = GuiUtils.getIcon("copyId", 24);
	private static final Icon copyTwoAccessionsIcon = GuiUtils.getIcon("copyWorklistToClipboard", 24);
	
	private JMenuItem
		addTautomerAsNewCompoundMenuItem,
		replacePrimaryWithTautomerMenuItem,
		copyAccessionMenuItem,
		copyTwoAccessionsMenuItem;

	public TautomerTablePopupMenu(ActionListener listener) {

		super();

		addTautomerAsNewCompoundMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_TAUTOMER_AS_NEW_COMPOUND_COMMAND.getName(), listener,
				MainActionCommands.ADD_TAUTOMER_AS_NEW_COMPOUND_COMMAND.getName());
		addTautomerAsNewCompoundMenuItem.setIcon(addIcon);

		replacePrimaryWithTautomerMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.REPLACE_PRIMARY_COMPOUND_WITH_TAUTOMER_COMMAND.getName(), listener,
				MainActionCommands.REPLACE_PRIMARY_COMPOUND_WITH_TAUTOMER_COMMAND.getName());
		replacePrimaryWithTautomerMenuItem.setIcon(replaceIcon);
		
		addSeparator();

		copyAccessionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_TAUTOMER_ACCESSION_COMMAND.getName(), listener,
				MainActionCommands.COPY_TAUTOMER_ACCESSION_COMMAND.getName());
		copyAccessionMenuItem.setIcon(copyAccessionIcon);
		
		copyTwoAccessionsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_PRIMARY_AND_TAUTOMER_ACCESSION_COMMAND.getName(), listener,
				MainActionCommands.COPY_PRIMARY_AND_TAUTOMER_ACCESSION_COMMAND.getName());
		copyTwoAccessionsMenuItem.setIcon(copyTwoAccessionsIcon);
	}
}






