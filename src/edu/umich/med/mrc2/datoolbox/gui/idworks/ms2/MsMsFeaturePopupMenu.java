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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEventListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MsMsFeaturePopupMenu extends BasicTablePopupMenu implements IdentificationLevelEventListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;
	private static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();	
	
	private static final Icon nistPepMsIcon = GuiUtils.getIcon("NISTMS-pep", 24);
	private static final Icon manualIdentificationIcon = GuiUtils.getIcon("manualIdentification", 24);
//	private static final Icon copySelectedIcon = GuiUtils.getIcon("copy", 24);
//	private static final Icon copySelectedWithHeaderIcon = GuiUtils.getIcon("copyWithHeader", 24);
	private static final Icon disablePrimaryIdIcon = GuiUtils.getIcon("multipleIdsDisabled", 24);
	private static final Icon clearIdentificationsIcon = GuiUtils.getIcon("clearIdentifications", 24);
	private static final Icon fusEditIcon = GuiUtils.getIcon("editIdFollowupStep", 24);
	private static final Icon editStandardFeatureAnnotationIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon setIdLevelIcon = GuiUtils.getIcon("editIdStatus", 24);
	private static final Icon goToDatabaseIcon = GuiUtils.getIcon("goToDatabase", 24);
	private static final Icon xicIcon = GuiUtils.getIcon("xic", 24);
	private static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 24);
	private static final Icon removeFromActiveCollectionIcon = GuiUtils.getIcon("deleteCollection", 24);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon copyAsArrayIcon = GuiUtils.getIcon("copyAsArray", 24);
	private static final Icon clearIcon = GuiUtils.getIcon("clear", 24);
	
	private JMenuItem 
			setupNISTPepSearchMenuItem,
			addManualIdentificationMenuItem,
			disablePrimaryIdMenuItem,
			clearFeatureIdentificationsMenuItem,
			copySelectedMenuItem,
			copySelectedWithHeaderMenuItem,
			editFollowupStepsMenuItem,
			editIdStandardFeatureAnnotationMenuItem,
			goToCompoundDatabaseMenuItem,
			xicMenuItem,
			createNewCollectionMenuItem,
			addToExistingCollectionMenuItem,
			removeFromActiveCollectionMenuItem,
			copySpectrumAsMSPMenuItem, 
			copyAsArrayMenuItem;
	
	private JMenu idLevelMenu;
	private ActionListener alistener;

	public MsMsFeaturePopupMenu(
			ActionListener listener,
			ActionListener copyListener) {

		super(copyListener);
		this.alistener = listener;

		setupNISTPepSearchMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName(), listener,
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName());
		setupNISTPepSearchMenuItem.setIcon(nistPepMsIcon);

		addManualIdentificationMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_MANUAL_MSMS_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.ADD_MANUAL_MSMS_IDENTIFICATION_COMMAND.getName());
		addManualIdentificationMenuItem.setIcon(manualIdentificationIcon);
		
		disablePrimaryIdMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DISABLE_PRIMARY_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.DISABLE_PRIMARY_IDENTIFICATION_COMMAND.getName());
		disablePrimaryIdMenuItem.setIcon(disablePrimaryIdIcon);

		clearFeatureIdentificationsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName(), listener,
				MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName());
		clearFeatureIdentificationsMenuItem.setIcon(clearIdentificationsIcon);
		
		this.addSeparator();
		
		editFollowupStepsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ASSIGN_ID_FOLLOWUP_STEPS_TO_FEATURE_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.ASSIGN_ID_FOLLOWUP_STEPS_TO_FEATURE_DIALOG_COMMAND.getName());
		editFollowupStepsMenuItem.setIcon(fusEditIcon);
		
		editIdStandardFeatureAnnotationMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ASSIGN_STANDARD_FEATURE_ANNOTATIONS_TO_FEATURE_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.ASSIGN_STANDARD_FEATURE_ANNOTATIONS_TO_FEATURE_DIALOG_COMMAND.getName());
		editIdStandardFeatureAnnotationMenuItem.setIcon(editStandardFeatureAnnotationIcon);
		
		idLevelMenu = new JMenu("Set ID confidence level");
		idLevelMenu.setIcon(setIdLevelIcon);
		populateIdLevelMenu();
		add(idLevelMenu);
		
		this.addSeparator();

		createNewCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(), listener,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName());
		createNewCollectionMenuItem.setIcon(createNewCollectionIcon);

		addToExistingCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(), listener,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName());
		addToExistingCollectionMenuItem.setIcon(addToExistingCollectionIcon);
		
		removeFromActiveCollectionMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION.getName(), listener,
				MainActionCommands.REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION.getName());
		removeFromActiveCollectionMenuItem.setIcon(removeFromActiveCollectionIcon);
		
		addSeparator();

		addCopyBlock();
		
//		copySelectedMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.COPY_SELECTED_MS2_ROWS_COMMAND.getName(), listener,
//				MainActionCommands.COPY_SELECTED_MS2_ROWS_COMMAND.getName());
//		copySelectedMenuItem.setIcon(copySelectedIcon);
//		copySelectedMenuItem.setEnabled(false);
//
//		copySelectedWithHeaderMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.COPY_SELECTED_MS2_ROWS_WITH_HEADER_COMMAND.getName(), listener,
//				MainActionCommands.COPY_SELECTED_MS2_ROWS_WITH_HEADER_COMMAND.getName());
//		copySelectedWithHeaderMenuItem.setIcon(copySelectedWithHeaderIcon);
//		copySelectedWithHeaderMenuItem.setEnabled(false);
		
		addSeparator();
		
		copySpectrumAsMSPMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_FEATURE_SPECTRUM_AS_MSP_COMMAND.getName(), listener,
				MainActionCommands.COPY_FEATURE_SPECTRUM_AS_MSP_COMMAND.getName());
		copySpectrumAsMSPMenuItem.setIcon(exportMSPIcon);
		
		copyAsArrayMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_FEATURE_SPECTRUM_AS_ARRAY_COMMAND.getName(), listener,
				MainActionCommands.COPY_FEATURE_SPECTRUM_AS_ARRAY_COMMAND.getName());
		copyAsArrayMenuItem.setIcon(copyAsArrayIcon);
		
		addSeparator();
		
		xicMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.XIC_FOR_FEATURE_DIALOG_COMMAND.getName(), listener,
				MainActionCommands.XIC_FOR_FEATURE_DIALOG_COMMAND.getName());
		xicMenuItem.setIcon(xicIcon);
		
		addSeparator();
		
		goToCompoundDatabaseMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.GO_TO_PRIMARY_COMPOUND_IN_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.GO_TO_PRIMARY_COMPOUND_IN_DATABASE_COMMAND.getName());
		goToCompoundDatabaseMenuItem.setIcon(goToDatabaseIcon);
	}
	
	private void populateIdLevelMenu() {
		
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			Icon levelIcon = new IdLevelIcon(24, level.getColorCode());
			JMenuItem levelItem = GuiUtils.addMenuItem(
					idLevelMenu, level.getName(), alistener, 
					MSFeatureIdentificationLevel.SET_PRIMARY + level.getName(), levelIcon);
			levelItem.putClientProperty(
					MRC2ToolBoxCore.COMPONENT_IDENTIFIER, listener.getClass().getSimpleName());
			if(level.getShorcut() != null)
				levelItem.setAccelerator(
						KeyStroke.getKeyStroke(level.getShorcut().charAt(0), 
								MASK | InputEvent.SHIFT_DOWN_MASK));
		}
		JMenuItem clearIdLevelMenuItem = GuiUtils.addMenuItem(idLevelMenu,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName(), alistener,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName());
		clearIdLevelMenuItem.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, this.getClass().getSimpleName());
		clearIdLevelMenuItem.setIcon(clearIcon);
		clearIdLevelMenuItem.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, listener.getClass().getSimpleName());
	}
	
	public void refreshIdLevelMenu() {
				
		idLevelMenu.removeAll();
		IDTDataCache.refreshMsFeatureIdentificationLevelList();
		populateIdLevelMenu();
	}

	@Override
	public void identificationLevelDefinitionChanged(IdentificationLevelEvent e) {
		refreshIdLevelMenu();
	}
}


