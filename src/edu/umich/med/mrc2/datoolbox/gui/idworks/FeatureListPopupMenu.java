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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.commons.text.WordUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEventListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;

public class FeatureListPopupMenu extends BasicTablePopupMenu implements IdentificationLevelEventListener {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;
	
	protected static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();	
	
	protected static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	protected static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 24);
	protected static final Icon addToRecentCollectionIcon = GuiUtils.getIcon("addToRecentCollection", 24);
	protected static final Icon removeFromActiveCollectionIcon = GuiUtils.getIcon("deleteCollection", 24);
	protected static final Icon fcIcon = GuiUtils.getIcon("enableAll", 24);
	protected static final Icon setIdLevelIcon = GuiUtils.getIcon("editIdStatus", 24);
	protected static final Icon clearIcon = GuiUtils.getIcon("clear", 24);
	
	protected boolean enableTrackerCommands;
	
	protected JMenu 
			idLevelMenu,
			recentFeatureCollectionsMenu;
	protected JMenuItem 
			createNewCollectionMenuItem,
			addToExistingCollectionMenuItem,
			removeFromActiveCollectionMenuItem;

	public FeatureListPopupMenu(
			ActionListener mainActionListener, 
			BasicTable copyListener,
			boolean enableTrackerCommands) {
		super(mainActionListener, copyListener);
		this.enableTrackerCommands = enableTrackerCommands;
	}
	
	protected void addFeatureCollectionMenu(boolean includeRemoveCollectionCommand) {
		
		createNewCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(), mainActionListener,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName());
		createNewCollectionMenuItem.setIcon(createNewCollectionIcon);
				
		recentFeatureCollectionsMenu = new JMenu("Add selected feature(s) to recent collection");
		recentFeatureCollectionsMenu.setIcon(addToRecentCollectionIcon);
		updateRecentFeatureCollectionList();
		add(recentFeatureCollectionsMenu);
		
		addToExistingCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(), mainActionListener,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName());
		addToExistingCollectionMenuItem.setIcon(addToExistingCollectionIcon);
		
		if(includeRemoveCollectionCommand) {
			
			removeFromActiveCollectionMenuItem = GuiUtils.addMenuItem(this,
					MainActionCommands.REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION.getName(), mainActionListener,
					MainActionCommands.REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION.getName());
			removeFromActiveCollectionMenuItem.setIcon(removeFromActiveCollectionIcon);
		}
	}
	
	protected void addIdLevelMenu() {
		
		idLevelMenu = new JMenu("Set ID confidence level");
		idLevelMenu.setIcon(setIdLevelIcon);
		populateIdLevelMenu();
		add(idLevelMenu);
	}

	public void updateRecentFeatureCollectionList() {
		
		if(!enableTrackerCommands)
			return;
		
		if(recentFeatureCollectionsMenu == null)
			return;

		recentFeatureCollectionsMenu.removeAll();
		Set<MsFeatureInfoBundleCollection>fcSet = 
				RecentDataManager.getRecentFeatureCollections().
					stream().collect(Collectors.toCollection(() -> 
					new TreeSet<>(new MsFeatureInfoBundleCollectionComparator(SortProperty.Name))));
		
		for(MsFeatureInfoBundleCollection fc : fcSet) {
			
			if(fc.equals(FeatureCollectionManager.msmsSearchResults))
				continue;
			
			String title = "<html>" + WordUtils.wrap(fc.getName(), 50, "<br />", true);
			String command = MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name() + "|" + fc.getId();
			JMenuItem fcItem = addItem(recentFeatureCollectionsMenu, title, command, fcIcon);		
			fcItem.setToolTipText(fc.getFormattedMetadata());
		}
	}
	
	protected void populateIdLevelMenu() {
		
		if(idLevelMenu == null)
			return;
		
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			Icon levelIcon = new IdLevelIcon(24, level.getColorCode());
			JMenuItem levelItem = GuiUtils.addMenuItem(
					idLevelMenu, level.getName(), mainActionListener, 
					MSFeatureIdentificationLevel.SET_PRIMARY + level.getName(), levelIcon);
			levelItem.putClientProperty(
					MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
			if(level.getShorcut() != null)
				levelItem.setAccelerator(
						KeyStroke.getKeyStroke(level.getShorcut().charAt(0), 
								MASK | InputEvent.SHIFT_DOWN_MASK));
		}
		JMenuItem clearIdLevelMenuItem = GuiUtils.addMenuItem(idLevelMenu,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName(), mainActionListener,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName());
		clearIdLevelMenuItem.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, this.getClass().getSimpleName());
		clearIdLevelMenuItem.setIcon(clearIcon);
		clearIdLevelMenuItem.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
	}
	
	public void refreshIdLevelMenu() {
		
		if(idLevelMenu == null)
			return;
				
		idLevelMenu.removeAll();
		IDTDataCache.refreshMsFeatureIdentificationLevelList();
		populateIdLevelMenu();
	}

	@Override
	public void identificationLevelDefinitionChanged(IdentificationLevelEvent e) {
		refreshIdLevelMenu();
	}
}
