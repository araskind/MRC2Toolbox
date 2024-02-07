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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msms;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSFeaturePlotPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1420991571317499448L;
	private static final Icon filterSelectedFeaturesIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 24);

	private JMenuItem filterSelectedFeaturesMenuItem;
	private JMenuItem createNewCollectionMenuItem;
	private JMenuItem addToExistingCollectionMenuItem;	

	public MSMSFeaturePlotPopupMenu(ActionListener listener) {

		super();
		
		filterSelectedFeaturesMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName(), listener,
				MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName());
		filterSelectedFeaturesMenuItem.setIcon(filterSelectedFeaturesIcon);
		
		this.addSeparator();

		createNewCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(), listener,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName());
		createNewCollectionMenuItem.setIcon(createNewCollectionIcon);
//		createNewCollectionMenuItem.setEnabled(false);

		addToExistingCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(), listener,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName());
		addToExistingCollectionMenuItem.setIcon(addToExistingCollectionIcon);
//		addToExistingCollectionMenuItem.setEnabled(false);
	}
}

