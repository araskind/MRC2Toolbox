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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.commons.text.WordUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;

public class MzFrequencyTablePopupMenu extends BasicTablePopupMenu {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8828829260409589130L;

	private static final Icon editIcon = GuiUtils.getIcon("editLibraryFeature", 24);
	private static final Icon setIdLevelIcon = GuiUtils.getIcon("editIdStatus", 24);
	private static final Icon clearIcon = GuiUtils.getIcon("clear", 24);
	private static final Icon addToRecentCollectionIcon = GuiUtils.getIcon("addToRecentCollection", 24);
	private static final Icon fcIcon = GuiUtils.getIcon("enableAll", 24);
	private static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 24);
	
	private static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
	
	private JMenu idLevelMenu;
	private JMenu recentFeatureCollectionsMenu;
	private boolean enableTrackerCommands;
	
	private JMenuItem 
		createNewCollectionMenuItem,
		addToExistingCollectionMenuItem;
		
	public MzFrequencyTablePopupMenu(
			ActionListener listener, 
			BasicTable copyListener, 
			boolean enableTrackerCommands) {
		super(listener, copyListener);
		this.enableTrackerCommands = enableTrackerCommands;
				
		if(enableTrackerCommands) {
			
			idLevelMenu = new JMenu("Set ID confidence level for selected");
			idLevelMenu.setIcon(setIdLevelIcon);
			populateIdLevelMenu();
			add(idLevelMenu);
			
			addSeparator();
			
			createNewCollectionMenuItem = GuiUtils.addMenuItem(this,
					MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(), listener,
					MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName());
			createNewCollectionMenuItem.setIcon(createNewCollectionIcon);
					
			recentFeatureCollectionsMenu = new JMenu("Add Add selected feature(s) to recent collection");
			recentFeatureCollectionsMenu.setIcon(addToRecentCollectionIcon);
			updateRecentFeatureCollectionList();
			add(recentFeatureCollectionsMenu);
			
			addToExistingCollectionMenuItem = GuiUtils.addMenuItem(this,
					MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(), listener,
					MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName());
			addToExistingCollectionMenuItem.setIcon(addToExistingCollectionIcon);
		}
		addCopyBlock();
	}
	
	private void populateIdLevelMenu() {
		
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			Icon levelIcon = new IdLevelIcon(24, level.getColorCode());
			JMenuItem levelItem = 
					GuiUtils.addMenuItem(idLevelMenu, 
							level.getName(), mainActionListener, level.getName(), levelIcon);
			levelItem.putClientProperty(
					MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
			
			if(level.getShorcut() != null)
				levelItem.setAccelerator(KeyStroke.getKeyStroke(
						level.getShorcut().charAt(0), MASK | InputEvent.SHIFT_DOWN_MASK));
		}
		JMenuItem clearIdLevelMenuItem = GuiUtils.addMenuItem(idLevelMenu,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName(), mainActionListener,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName());
		clearIdLevelMenuItem.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
		clearIdLevelMenuItem.setIcon(clearIcon);
	}
	
	public void updateRecentFeatureCollectionList() {
		
		if(!enableTrackerCommands)
			return;
		
		recentFeatureCollectionsMenu.removeAll();
		Set<MsFeatureInfoBundleCollection> fcList = 
				RecentDataManager.getRecentFeatureCollections();
		for(MsFeatureInfoBundleCollection fc : fcList) {
			
			String title = "<html>" + WordUtils.wrap(fc.getName(), 50, "<br />", true);
			String command = MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name() + "|" + fc.getId();
			JMenuItem fcItem = addItem(recentFeatureCollectionsMenu, title, command, fcIcon);		
			fcItem.setToolTipText(fc.getFormattedMetadata());
		}
	}
}





