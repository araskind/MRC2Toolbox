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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.cpd;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class CompoundCurationPopupMenu  extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -4582990028092698728L;

	private static final Icon validateIcon = GuiUtils.getIcon("checkboxFull", 24);
	private static final Icon saveMsReadyIcon = GuiUtils.getIcon("save", 24);

	private JMenuItem
		validateMenuItem,
		saveMsReadyMenuItem;

	public CompoundCurationPopupMenu(ActionListener listener) {

		super();

		validateMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.VALIDATE_MS_READY_STRUCTURE.getName(), listener,
				MainActionCommands.VALIDATE_MS_READY_STRUCTURE.getName());
		validateMenuItem.setIcon(validateIcon);

		saveMsReadyMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName(), listener,
				MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
		saveMsReadyMenuItem.setIcon(saveMsReadyIcon);
	}

}






