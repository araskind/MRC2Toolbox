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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms1;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEventListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ReferenceMsOneFeaturePopupMenu extends BasicTablePopupMenu implements IdentificationLevelEventListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;

	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 24);
	private static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 24);
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

	private JMenu idLevelMenu;
	private JMenuItem matchFeatureToLibraryMenuItem;
	private JMenuItem searchFeatureAgainstDatabaseMenuItem;
	private JMenuItem addManualIdentificationMenuItem;
	private JMenuItem disablePrimaryIdMenuItem;
	private JMenuItem clearFeatureIdentificationsMenuItem;
	private JMenuItem copySelectedMenuItem;
	private JMenuItem copySelectedWithHeaderMenuItem;
	private JMenuItem editFollowupStepsMenuItem;
	private JMenuItem editIdStandardFeatureAnnotationMenuItem;
	private JMenuItem goToCompoundDatabaseMenuItem;
	private JMenuItem xicMenuItem;

	public ReferenceMsOneFeaturePopupMenu(
			ActionListener listener,
			BasicTable copyListener) {

		super(listener, copyListener);

		matchFeatureToLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName(), listener,
				MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName());
		matchFeatureToLibraryMenuItem.setIcon(searchLibraryIcon);

//		searchFeatureAgainstDatabaseMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName(), listener,
//				MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName());
//		searchFeatureAgainstDatabaseMenuItem.setIcon(searchDatabaseIcon);

		addManualIdentificationMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.ADD_MANUAL_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.ADD_MANUAL_IDENTIFICATION_COMMAND.getName());
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

		addSeparator();		

		addCopyBlock();
		
//		copySelectedMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.COPY_SELECTED_MS1_ROWS_COMMAND.getName(), listener,
//				MainActionCommands.COPY_SELECTED_MS1_ROWS_COMMAND.getName());
//		copySelectedMenuItem.setIcon(copySelectedIcon);
//
//		copySelectedWithHeaderMenuItem = GuiUtils.addMenuItem(this,
//				MainActionCommands.COPY_SELECTED_MS1_ROWS_WITH_HEADER_COMMAND.getName(), listener,
//				MainActionCommands.COPY_SELECTED_MS1_ROWS_WITH_HEADER_COMMAND.getName());
//		copySelectedWithHeaderMenuItem.setIcon(copySelectedWithHeaderIcon);
		
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
					idLevelMenu, level.getName(), mainActionListener, 
					MSFeatureIdentificationLevel.SET_PRIMARY + level.getName(), levelIcon);
			levelItem.putClientProperty(
					MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
		}
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
