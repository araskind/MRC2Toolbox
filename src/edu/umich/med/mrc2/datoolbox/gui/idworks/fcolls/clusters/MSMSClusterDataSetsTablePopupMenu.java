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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSClusterDataSetsTablePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;

	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon deleteFeatureCollectionIcon = GuiUtils.getIcon("deleteCollection", 24);
	private static final Icon loadFeatureCollectionIcon = GuiUtils.getIcon("openCollection", 24);
	private static final Icon showLookupFeatureListIcon = GuiUtils.getIcon("searchLibrary", 24);
	private static final Icon showBinnerAnnotationsIcon = GuiUtils.getIcon("bin", 24);

	private JMenuItem addFeatureCollectionMenuItem;
	private JMenuItem editFeatureCollectionMenuItem;
	private JMenuItem deleteFeatureCollectionMenuItem;
	private JMenuItem loadFeatureCollectionMenuItem;
	private JMenuItem showLookupFeaturesCollectionMenuItem;
	private JMenuItem showBinnerAnnotationsMenuItem;

	public MSMSClusterDataSetsTablePopupMenu(ActionListener listener) {

		super();
		addFeatureCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.ADD_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName());
		addFeatureCollectionMenuItem.setIcon(addFeatureCollectionIcon);
		
		editFeatureCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName());
		editFeatureCollectionMenuItem.setIcon(editFeatureCollectionIcon);
		
		addSeparator();
		
		deleteFeatureCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_MSMS_CLUSTER_DATASET_COMMAND.getName(), listener,
				MainActionCommands.DELETE_MSMS_CLUSTER_DATASET_COMMAND.getName());
		deleteFeatureCollectionMenuItem.setIcon(deleteFeatureCollectionIcon);
		
		addSeparator();
		
		loadFeatureCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.LOAD_MSMS_CLUSTER_DATASET_COMMAND.getName(), listener,
				MainActionCommands.LOAD_MSMS_CLUSTER_DATASET_COMMAND.getName());
		loadFeatureCollectionMenuItem.setIcon(loadFeatureCollectionIcon);
		
		showLookupFeaturesCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SHOW_LOOKUP_FEATURE_LIST_FOR_CLUSTER_DATA_SET_COMMAND.getName(), listener,
				MainActionCommands.SHOW_LOOKUP_FEATURE_LIST_FOR_CLUSTER_DATA_SET_COMMAND.getName());
		showLookupFeaturesCollectionMenuItem.setIcon(showLookupFeatureListIcon);
		
		showBinnerAnnotationsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SHOW_BINNER_ANNOTATION_SET_FOR_CLUSTER_DATA_SET_COMMAND.getName(), listener,
				MainActionCommands.SHOW_BINNER_ANNOTATION_SET_FOR_CLUSTER_DATA_SET_COMMAND.getName());
		showBinnerAnnotationsMenuItem.setIcon(showBinnerAnnotationsIcon);
		

	}
}
