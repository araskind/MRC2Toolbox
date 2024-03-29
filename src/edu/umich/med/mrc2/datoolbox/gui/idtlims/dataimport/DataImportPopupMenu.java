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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dataimport;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DataImportPopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = 6109578300557179546L;
	private static final Icon assignLevelIcon = GuiUtils.getIcon("dropdown", 24);
	private JMenuItem assignLevelsMenuItem;

	public DataImportPopupMenu(ActionListener listener) {

		super();

		assignLevelsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName(), listener,
				MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName());
		assignLevelsMenuItem.setIcon(assignLevelIcon);
	}
}
