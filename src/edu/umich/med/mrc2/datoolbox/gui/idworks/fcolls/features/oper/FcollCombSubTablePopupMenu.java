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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.oper;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FcollCombSubTablePopupMenu extends BasicTablePopupMenu {

	private static final long serialVersionUID = 8828829260409589130L;
	
	protected static final Icon combineIcon = GuiUtils.getIcon("addCollection", 24);
	protected static final Icon subtractIcon = GuiUtils.getIcon("removeFromCollection", 24);
	
	protected JMenuItem
		addCollectionToCombineListMenuItem,
		addCollectionToSubtractListMenuItem;

	public FcollCombSubTablePopupMenu(
			ActionListener listener,
			BasicTable copyListener) {

		super(listener, copyListener);
		
		addCollectionToCombineListMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_FEATURE_COLLECTION_TO_COMBINE_LIST_COMMAND.getName(), listener,
				MainActionCommands.ADD_FEATURE_COLLECTION_TO_COMBINE_LIST_COMMAND.getName());
		addCollectionToCombineListMenuItem.setIcon(combineIcon);
		
		addCollectionToSubtractListMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_FEATURE_COLLECTION_TO_SUBTRACT_LIST_COMMAND.getName(), listener,
				MainActionCommands.ADD_FEATURE_COLLECTION_TO_SUBTRACT_LIST_COMMAND.getName());
		addCollectionToSubtractListMenuItem.setIcon(subtractIcon);
	}
}



