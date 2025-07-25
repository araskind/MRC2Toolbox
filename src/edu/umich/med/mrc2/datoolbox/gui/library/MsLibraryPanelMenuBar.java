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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MsLibraryPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	public static final String LIBRARY_OBJECT = "LIBRARY_OBJECT";
	
	// Icons	
	private static final Icon libraryManagerIcon = GuiUtils.getIcon("libraryManager", 24);
	private static final Icon libraryManagerIconSmall = GuiUtils.getIcon("libraryManager", 16);
	private static final Icon closeLibraryIcon = GuiUtils.getIcon("close", 24);
	private static final Icon openLibraryIcon = GuiUtils.getIcon("open", 24);
	private static final Icon importLibraryIcon = GuiUtils.getIcon("importFromXML", 24);
	private static final Icon importLibraryIconSmall = GuiUtils.getIcon("importLibraryToDb", 16);
	private static final Icon importPCDLLibraryIcon = GuiUtils.getIcon("importFromPCDL", 24);
	private static final Icon newPCDLfromBaseIcon = GuiUtils.getIcon("newPCDLfromBase", 24);
	private static final Icon prescanLibraryIcon = GuiUtils.getIcon("searchCompounds", 24);
	private static final Icon exportLibraryIcon = GuiUtils.getIcon("exportLibrary", 24);
	private static final Icon exportLibraryIconSmall = GuiUtils.getIcon("exportLibrary", 16);
	private static final Icon exportFilteredLibraryIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 24);
	private static final Icon mergeLibrariesIcon = GuiUtils.getIcon("mergeLibraries", 24);
	private static final Icon newFeatureIcon = GuiUtils.getIcon("newLibraryFeature", 24);
	private static final Icon editFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 24);
	private static final Icon editFeatureIconSmall = GuiUtils.getIcon("editLibraryFeature", 16);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 24);
	private static final Icon importRtIcon = GuiUtils.getIcon("importLibraryRtValues", 24);
	private static final Icon libraryExportIcon = GuiUtils.getIcon("exportLibrary", 24);
	private static final Icon libraryImportIcon = GuiUtils.getIcon("importLibraryToDb", 24);
	private static final Icon utilitiesIconSmall = GuiUtils.getIcon("preferences", 16);
	private static final Icon activeLibraryIcon = GuiUtils.getIcon("duplicateLibrary", 24);
	private static final Icon activeLibrariesIconSmall = GuiUtils.getIcon("duplicateLibrary", 24);
	
	private JLabel activeLibraryLabel;
	
	// Menus
	private JMenu
		manageMenu,
		editFeaturesMenu,
		importMenu,
		exportMenu,
		utilitiesMenu,
		activeLibrariesMenu;

	// Manage
	private JMenuItem
		libraryManagerMenuItem,
		openLibraryMenuItem,
		closeLibraryMenuItem;
	
	// Edit
	private JMenuItem
		newFeatureMenuItem,
		editFeatureMenuItem,
		deleteFeatureMenuItem;
	
	//	Import
	private JMenuItem
		prescanLibraryMenuItem,
		importLibraryMenuItem,
		importPCDLLibraryMenuItem,
		newPCDLfromBaseMenuItem,
		importRtDataMenuItem,
		importDecoyMSMSLibraryMenuItem,
		importIDtrackerLibraryMenuItem;
	
	//	Export
	private JMenuItem
		exportMzRtLibraryMenuItem,
		exportFilteredMzRtLibraryMenuItem,
		exportReferenceMSMSLibraryMenuItem;
	
	//	Utilities
	private JMenuItem
		convertCefForRecursionMenuItem;

	public MsLibraryPanelMenuBar(ActionListener listener) {

		super(listener);
		
		// Manage
		manageMenu = new JMenu("Manage");
		manageMenu.setIcon(libraryManagerIconSmall);
		
		libraryManagerMenuItem = addItem(manageMenu, 
				MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND, 
				libraryManagerIcon);
		
		manageMenu.addSeparator();
		
		closeLibraryMenuItem = addItem(manageMenu, 
				MainActionCommands.CLOSE_LIBRARY_COMMAND, 
				closeLibraryIcon);
		
		add(manageMenu);
		
		// Edit
		editFeaturesMenu = new JMenu("Edit");
		editFeaturesMenu.setIcon(editFeatureIconSmall);
		
		newFeatureMenuItem = addItem(editFeaturesMenu, 
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND, 
				newFeatureIcon);
		newFeatureMenuItem.setEnabled(false);
		
//		editFeatureMenuItem = addItem(editFeaturesMenu, 
//				MainActionCommands.EDIT_LIBRARY_FEATURE_DIALOG_COMMAND, 
//				editFeatureIcon);
//		editFeatureMenuItem.setEnabled(false);
		
		editFeaturesMenu.addSeparator();
		
		deleteFeatureMenuItem = addItem(editFeaturesMenu, 
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND, 
				deleteFeatureIcon);
		
		add(editFeaturesMenu);
		
		//	Import
		importMenu = new JMenu("Import");
		importMenu.setIcon(importLibraryIconSmall);
		
		//
		prescanLibraryMenuItem = addItem(importMenu, 
				MainActionCommands.PRESCAN_LIBRARY_COMPOUNDS_COMMAND, 
				prescanLibraryIcon);

		importMenu.addSeparator();
		
		importPCDLLibraryMenuItem = addItem(importMenu, 
				MainActionCommands.SET_UP_PCDL_DATA_IMPORT_COMMAND, 
				importPCDLLibraryIcon);	
		newPCDLfromBaseMenuItem = addItem(importMenu, 
				MainActionCommands.NEW_PCDL_LIBRARY_FROM_PCDL_TEXT_FILE_SETUP_COMMAND, 
				newPCDLfromBaseIcon);			
		importLibraryMenuItem = addItem(importMenu, 
				MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND, 
				importLibraryIcon);
		importRtDataMenuItem = addItem(importMenu, 
				MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND, 
				importRtIcon);
		
//		importMenu.addSeparator();
//		
//		importIDtrackerLibraryMenuItem = addItem(importMenu, 
//				MainActionCommands.IMPORT_IDTRACKER_LIBRARY_COMMAND, 
//				importLibraryIcon);
		
		importMenu.addSeparator();
		
		importDecoyMSMSLibraryMenuItem = addItem(importMenu, 
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND, 
				libraryImportIcon);	//	TODO create different icon
		importDecoyMSMSLibraryMenuItem.setEnabled(false);
		
		add(importMenu);
		
		//	Export
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(exportLibraryIconSmall);
		
		exportMzRtLibraryMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND, 
				exportLibraryIcon);
		exportFilteredMzRtLibraryMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND, 
				exportFilteredLibraryIcon);
		
		exportMenu.addSeparator();
		
		exportReferenceMSMSLibraryMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND, 
				libraryExportIcon);	//	TODO create different icon
		
		add(exportMenu);
		
		//	Utilities
		utilitiesMenu = new JMenu("Utilities");
		utilitiesMenu.setIcon(utilitiesIconSmall);
		
		convertCefForRecursionMenuItem = addItem(utilitiesMenu, 
				MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND, 
				mergeLibrariesIcon);
		
		add(utilitiesMenu);
		
		//	Active libraries
		activeLibrariesMenu = new JMenu("Active libraries");
		activeLibrariesMenu.setIcon(activeLibrariesIconSmall);
		
		add(activeLibrariesMenu);
	}
	
	public void createActiveLibraryLabelBlock() {
		
		super.add(Box.createHorizontalGlue());
		super.add(new JLabel("Active library: "));
		activeLibraryLabel = new JLabel("");
		activeLibraryLabel.setFont(new Font("Arial", Font.BOLD, 14));
		super.add(activeLibraryLabel);
	}
	
	public void updateLibraryList(
			CompoundLibrary selectedLibrary, Collection<CompoundLibrary>activeLibraries) {
		
		activeLibrariesMenu.removeAll();
		for (CompoundLibrary library : activeLibraries) {

			boolean isSelected = false;
			if(selectedLibrary != null)
				isSelected = selectedLibrary.equals(library);
			
			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(library.getLibraryName(), isSelected);
			cbMenuItem.putClientProperty(LIBRARY_OBJECT, library);
			cbMenuItem.addActionListener(alistener);
			cbMenuItem.setActionCommand(MainActionCommands.LOAD_SELECTED_LIBRARY_COMMAND.getName());		
			//	cbMenuItem.setIcon(activeLibraryIcon);
			activeLibrariesMenu.add(cbMenuItem);
		}
		if(selectedLibrary != null)
			activeLibraryLabel.setText(selectedLibrary.getLibraryName());
		else
			activeLibraryLabel.setText("");			
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
