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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

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
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class UniversalIdentificationResultsTablePopupMenu 
		extends BasicTablePopupMenu implements IdentificationLevelEventListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -6064748415801630180L;
	
	private static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();	

	private static final Icon deleteIdIcon = GuiUtils.getIcon("deleteFeature", 24);
	private static final Icon deleteAllIdsIcon = GuiUtils.getIcon("clearIdentifications", 24);
	private static final Icon disablePrimaryIdIcon = GuiUtils.getIcon("multipleIdsDisabled", 24);
	private static final Icon goToLibraryIcon = GuiUtils.getIcon("goToLibrary", 24);
	private static final Icon setIdLevelIcon = GuiUtils.getIcon("editIdStatus", 24);
	private static final Icon goToDatabaseIcon = GuiUtils.getIcon("goToDatabase", 24);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon copyAsArrayIcon = GuiUtils.getIcon("copyAsArray", 24);
	private static final Icon setPrimaryIdForCluster = GuiUtils.getIcon("setPrimaryIDForCluster", 24);
	private static final Icon clearIcon = GuiUtils.getIcon("clear", 24);
	
	private JMenuItem
		setPrimaryIdForClusterMenuItem,
		deleteIdMenuItem,
		deleteAllIdsMenuItem,
		disablePrimaryIdMenuItem,
		goToCompoundDatabaseMenuItem,
		goToLibraryMenuItem,
		copySpectrumAsMSPMenuItem,
		copyAsArrayMenuItem;
	
	private JMenu idLevelMenu;

	public UniversalIdentificationResultsTablePopupMenu(
			ActionListener listener,
			BasicTable copyListener) {

		super(listener, copyListener);
		
		idLevelMenu = new JMenu("Set ID confidence level");
		idLevelMenu.setIcon(setIdLevelIcon);
		populateIdLevelMenu();
		add(idLevelMenu);
		
		addSeparator();
		
		setPrimaryIdForClusterMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.SET_AS_PRIMARY_ID_FOR_CLUSTER.getName(), listener,
				MainActionCommands.SET_AS_PRIMARY_ID_FOR_CLUSTER.getName());
		setPrimaryIdForClusterMenuItem.setIcon(setPrimaryIdForCluster);
		
		addSeparator();
		
	
		disablePrimaryIdMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DISABLE_PRIMARY_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.DISABLE_PRIMARY_IDENTIFICATION_COMMAND.getName());
		disablePrimaryIdMenuItem.setIcon(disablePrimaryIdIcon);

		deleteIdMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_IDENTIFICATION_COMMAND.getName(), listener,
				MainActionCommands.DELETE_IDENTIFICATION_COMMAND.getName());
		deleteIdMenuItem.setIcon(deleteIdIcon);

		deleteAllIdsMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName(), listener,
				MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName());
		deleteAllIdsMenuItem.setIcon(deleteAllIdsIcon);

		addSeparator();
		
		goToCompoundDatabaseMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.GO_TO_COMPOUND_IN_DATABASE_COMMAND.getName(), listener,
				MainActionCommands.GO_TO_COMPOUND_IN_DATABASE_COMMAND.getName());
		goToCompoundDatabaseMenuItem.setIcon(goToDatabaseIcon);

		goToLibraryMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.GO_TO_LIBRARY_FEATURE_COMMAND.getName(), listener,
				MainActionCommands.GO_TO_LIBRARY_FEATURE_COMMAND.getName());
		goToLibraryMenuItem.setIcon(goToLibraryIcon);
		
		addSeparator();
		
		addCopyBlock();
		
		addSeparator();
		
		copySpectrumAsMSPMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_LIBRARY_SPECTRUM_AS_MSP_COMMAND.getName(), listener,
				MainActionCommands.COPY_LIBRARY_SPECTRUM_AS_MSP_COMMAND.getName());
		copySpectrumAsMSPMenuItem.setIcon(exportMSPIcon);
		
		copyAsArrayMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.COPY_LIBRARY_SPECTRUM_AS_ARRAY_COMMAND.getName(), listener,
				MainActionCommands.COPY_LIBRARY_SPECTRUM_AS_ARRAY_COMMAND.getName());
		copyAsArrayMenuItem.setIcon(copyAsArrayIcon);
	}

	private void populateIdLevelMenu() {
		
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			Icon levelIcon = new IdLevelIcon(24, level.getColorCode());
			JMenuItem levelItem = 
					GuiUtils.addMenuItem(idLevelMenu, level.getName(), mainActionListener, level.getName(), levelIcon);
			levelItem.putClientProperty(
					MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
			if(level.getShorcut() != null)
				levelItem.setAccelerator(KeyStroke.getKeyStroke(level.getShorcut().charAt(0), 
						MASK | InputEvent.SHIFT_DOWN_MASK));
		}
		JMenuItem clearIdLevelMenuItem = GuiUtils.addMenuItem(idLevelMenu,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName(), mainActionListener,
				MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName());
		clearIdLevelMenuItem.putClientProperty(
				MRC2ToolBoxCore.COMPONENT_IDENTIFIER, copyListener.getClass().getSimpleName());
		clearIdLevelMenuItem.setIcon(clearIcon);
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

	public JMenu getIdLevelMenu() {
		return idLevelMenu;
	}
}




