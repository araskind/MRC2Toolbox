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
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.text.WordUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;

public class MSMSFeaturePlotPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1420991571317499448L;
	private static final Icon filterSelectedFeaturesIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 24);
	private static final Icon addToRecentCollectionIcon = GuiUtils.getIcon("addToRecentCollection", 24);
	private static final Icon fcIcon = GuiUtils.getIcon("enableAll", 24);

	private JMenu recentFeatureCollectionsMenu;
	private JMenuItem filterSelectedFeaturesMenuItem;
	private JMenuItem createNewCollectionMenuItem;
	private JMenuItem addToExistingCollectionMenuItem;	
	private ActionListener listener;

	public MSMSFeaturePlotPopupMenu(ActionListener listener) {

		super();
		this.listener = listener;
		
		filterSelectedFeaturesMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName(), listener,
				MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName());
		filterSelectedFeaturesMenuItem.setIcon(filterSelectedFeaturesIcon);
		
		this.addSeparator();

		addSeparator();
		
		createNewCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(), listener,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName());
		createNewCollectionMenuItem.setIcon(createNewCollectionIcon);
				
		recentFeatureCollectionsMenu = new JMenu("Add selected feature(s) to recent collection");
		recentFeatureCollectionsMenu.setIcon(addToRecentCollectionIcon);
		updateRecentFeatureCollectionList();
		add(recentFeatureCollectionsMenu);
		
		addToExistingCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(), listener,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName());
		addToExistingCollectionMenuItem.setIcon(addToExistingCollectionIcon);
	}
	
	public void updateRecentFeatureCollectionList() {

		recentFeatureCollectionsMenu.removeAll();
		List<MsFeatureInfoBundleCollection> fcList = 
				RecentDataManager.getRecentFeatureCollections().
				stream().sorted(new MsFeatureInfoBundleCollectionComparator(SortProperty.Name)).
				collect(Collectors.toList());
		for(MsFeatureInfoBundleCollection fc : fcList) {
			
			String title = "<html>" + WordUtils.wrap(fc.getName(), 50, "<br />", true);
			String command = MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name() + "|" + fc.getId();
			JMenuItem fcItem = addItem(recentFeatureCollectionsMenu, title, command, fcIcon);		
			fcItem.setToolTipText(fc.getFormattedMetadata());
		}
	}
	
	protected JMenuItem addItem(JMenu menu, String title, String command, Icon defaultIcon) {

		JMenuItem item = new JMenuItem(title);
		item.setActionCommand(command);
		item.addActionListener(listener);
		item.setIcon(defaultIcon);
		menu.add(item);
		return item;
	}
}

