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

package edu.umich.med.mrc2.datoolbox.gui.fdata.noid;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MissingIdTablePopupMenu extends JPopupMenu{

	/**
	 *
	 */
	private static final long serialVersionUID = 6660765995893665375L;

	private static final Icon lookupAdductMassesIcon = GuiUtils.getIcon("searchMsMs", 24);

	private JMenuItem lookupAdductMassesMenuItem;

	public MissingIdTablePopupMenu(ActionListener listener) {

		super();

		lookupAdductMassesMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.FIND_REATURES_BY_ADDUCT_MASS.getName(), listener,
				MainActionCommands.FIND_REATURES_BY_ADDUCT_MASS.getName());
		lookupAdductMassesMenuItem.setIcon(lookupAdductMassesIcon);
	}
}
