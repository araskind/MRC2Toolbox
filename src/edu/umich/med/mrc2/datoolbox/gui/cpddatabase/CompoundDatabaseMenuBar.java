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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CompoundDatabaseMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon clearSearchIcon = GuiUtils.getIcon("clearSearch", 24);
	private static final Icon importCompoundsIcon = GuiUtils.getIcon("importLibraryToDb", 24);
	private static final Icon exportCompoundsIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 24);
	private static final Icon exportCompoundsIconSmall = GuiUtils.getIcon("exportFilteredLibraryToFile", 16);
	private static final Icon addToLibraryIcon = GuiUtils.getIcon("databaseToLibrary", 24);
	private static final Icon editFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 24);
	private static final Icon editFeatureIconSmall = GuiUtils.getIcon("editLibraryFeature", 16);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 24);
	private static final Icon pubChemImportIcon = GuiUtils.getIcon("pubChemDownload", 24);
	private static final Icon addCompoundIcon = GuiUtils.getIcon("addCompound", 24);
	private static final Icon addCompoundIconSmall = GuiUtils.getIcon("addCompound", 16);
	private static final Icon findCompoundListIcon = GuiUtils.getIcon("findList", 24);
	private static final Icon findCompoundListIconSmall = GuiUtils.getIcon("findList", 16);

	private static final Icon curateCompoundIcon = GuiUtils.getIcon("curateCompound", 24);

	// Menus
	private JMenu
		searchMenu,
		editMenu,
		importMenu,
		exportMenu;

	// Search items
	private JMenuItem
		batchSearchMenuItem,
		clearSearchResultsMenuItem;

	// Edit items
	private JMenuItem
		curateDbMenuItem,
		editEntryMenuItem,
		deleteEntryMenuItem;

	// Import data
	private JMenuItem
		addFromPubChemMenuItem,
		addManualMenuItem,
		batchAddMenuItem;

	// Export data
	private JMenuItem
		sendToLibraryMenuItem,
		exportCompoundsMenuItem;
	
	public CompoundDatabaseMenuBar(ActionListener listener) {

		super(listener);

		// Search
		searchMenu = new JMenu("Search");
		searchMenu.setIcon(findCompoundListIconSmall);
		
		batchSearchMenuItem = addItem(searchMenu,
				MainActionCommands.SHOW_DATABASE_BATCH_SEARCH_COMMAND, 
				findCompoundListIcon);
		clearSearchResultsMenuItem = addItem(searchMenu, 
				MainActionCommands.CLEAR_DATABASE_SEARCH_COMMAND, 
				clearSearchIcon);

		add(searchMenu);

		//	Edit
		editMenu = new JMenu("Edit");
		editMenu.setIcon(editFeatureIconSmall);
		
		editEntryMenuItem = addItem(editMenu, 
				MainActionCommands.EDIT_DATABASE_ENTRY_DIALOG_COMMAND, 
				editFeatureIcon);
		editEntryMenuItem.setEnabled(false);
		
		deleteEntryMenuItem = addItem(editMenu, 
				MainActionCommands.DELETE_DATABASE_ENTRY_COMMAND, 
				deleteFeatureIcon);
		deleteEntryMenuItem.setEnabled(false);
		
		editMenu.addSeparator();
		
		curateDbMenuItem = addItem(editMenu, 
				MainActionCommands.SHOW_COMPOUND_DATABASE_CURATOR, 
				curateCompoundIcon);
		
		add(editMenu);
		
		//	Import
		importMenu = new JMenu("Import");
		importMenu.setIcon(addCompoundIconSmall);
		
		addFromPubChemMenuItem = addItem(importMenu, 
				MainActionCommands.SHOW_PUBCHEM_DATA_LOADER, 
				pubChemImportIcon);
		addManualMenuItem = addItem(importMenu, 
				MainActionCommands.SHOW_CUSTOM_COMPOUND_LOADER, 
				addCompoundIcon);
		batchAddMenuItem = addItem(importMenu, 
				MainActionCommands.SETUP_BATCH_COMPOUND_IMPORT_TO_DATABASE_COMMAND, 
				importCompoundsIcon);
		batchAddMenuItem.setEnabled(false);
		
		add(importMenu);
		
		//	Export
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(exportCompoundsIconSmall);
		
		sendToLibraryMenuItem = addItem(exportMenu, 
				MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND, 
				addToLibraryIcon);
		
		exportMenu.addSeparator();
		
		exportCompoundsMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND, 
				exportCompoundsIcon);
		exportCompoundsMenuItem.setEnabled(false);
		
		add(exportMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
