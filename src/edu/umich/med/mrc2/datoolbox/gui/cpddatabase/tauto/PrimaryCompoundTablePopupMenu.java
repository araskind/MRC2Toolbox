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

public class PrimaryCompoundTablePopupMenu  extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -4582990028092698728L;

	private static final Icon markIcon = GuiUtils.getIcon("checkboxFull", 24);
	private static final Icon unmarkIcon = GuiUtils.getIcon("checkboxEmpty", 24);
	private static final Icon copyAccessionIcon = GuiUtils.getIcon("copy", 24);

	private JMenuItem
		markAsCuratedMenuItem,
		markNotCuratedMenuItem,
		copyAccessionMenuItem;

	public PrimaryCompoundTablePopupMenu(ActionListener listener) {

		super();
		
		markAsCuratedMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.MARK_COMPOUND_GROUP_CURATED_COMMAND.getName(), listener,
				MainActionCommands.MARK_COMPOUND_GROUP_CURATED_COMMAND.getName());
		markAsCuratedMenuItem.setIcon(markIcon);

		markNotCuratedMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.MARK_COMPOUND_GROUP_NOT_CURATED_COMMAND.getName(), listener,
				MainActionCommands.MARK_COMPOUND_GROUP_NOT_CURATED_COMMAND.getName());
		markNotCuratedMenuItem.setIcon(unmarkIcon);
		
		addSeparator();

		copyAccessionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName(), listener,
				MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName());
		copyAccessionMenuItem.setIcon(copyAccessionIcon);
	}

}






