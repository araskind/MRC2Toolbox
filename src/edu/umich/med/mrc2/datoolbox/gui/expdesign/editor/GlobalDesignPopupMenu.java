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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class GlobalDesignPopupMenu extends BasicTablePopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = 702774401229793555L;

	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 24);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 24);
	private static final Icon invertEnabledIcon = GuiUtils.getIcon("invertSelection", 24);
	private static final Icon assignLevelIcon = GuiUtils.getIcon("dropdown", 24);

	private JMenuItem enableSelectedMenuItem;
	private JMenuItem disableSelectedMenuItem;
	private JMenuItem invertSelectedMenuItem;
	private JMenuItem assignLevelsMenuItem;

	public GlobalDesignPopupMenu(ActionListener listener, BasicTable copyListener) {

		super(listener, copyListener);

		enableSelectedMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(), listener,
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName());
		enableSelectedMenuItem.setIcon(enableAllIcon);

		disableSelectedMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(), listener,
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName());
		disableSelectedMenuItem.setIcon(disableAllIcon);


		invertSelectedMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(), listener,
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName());
		invertSelectedMenuItem.setIcon(invertEnabledIcon);

		this.addSeparator();

		assignLevelsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName(), listener,
				MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName());
		assignLevelsMenuItem.setIcon(assignLevelIcon);
		
		addCopyBlock();
	}
}
