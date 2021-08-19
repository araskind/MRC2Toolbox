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
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CompoundDatabasePanelToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2581549897040815879L;

	private static final Icon dbSearchIcon = GuiUtils.getIcon("dbLookup", 32);
	private static final Icon clearSearchIcon = GuiUtils.getIcon("clearSearch", 32);
	private static final Icon importCompoundsIcon = GuiUtils.getIcon("importLibraryToDb", 32);
	private static final Icon exportCompoundsIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 32);
	private static final Icon addToLibraryIcon = GuiUtils.getIcon("databaseToLibrary", 32);
	private static final Icon newLibraryIcon = GuiUtils.getIcon("newLibrary", 32);
	private static final Icon editFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 32);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 32);
	private static final Icon pubChemImportIcon = GuiUtils.getIcon("pubChemDownload", 32);
	private static final Icon addCompoundIcon = GuiUtils.getIcon("addCompound", 32);
	private static final Icon findCompoundListIcon = GuiUtils.getIcon("findList", 32);
	private static final Icon curateCompoundIcon = GuiUtils.getIcon("curateCompound", 32);

	@SuppressWarnings("unused")
	private JButton
		dbSearchButton,
		clearSearchButton,
		importCompoundsButton,
		exportCompoundsButton,
		addToLibraryButton,
		newLibraryButton,
		editEntryButton,
		deleteEntryButton,
		pubChemImportButton,
		addCompoundButton,
		findCompoundListButton,
		curateCompoundButton;

	public CompoundDatabasePanelToolbar(ActionListener commandListener) {

		super(commandListener);
/*
		dbSearchButton = GuiUtils.addButton(this, null, dbSearchIcon, commandListener,
				MainActionCommands.SHOW_DATABASE_SEARCH_COMMAND.getName(),
				MainActionCommands.SHOW_DATABASE_SEARCH_COMMAND.getName(), buttonDimension);*/

		findCompoundListButton = GuiUtils.addButton(this, null, findCompoundListIcon, commandListener,
				MainActionCommands.SHOW_DATABASE_BATCH_SEARCH_COMMAND.getName(),
				MainActionCommands.SHOW_DATABASE_BATCH_SEARCH_COMMAND.getName(), buttonDimension);

		clearSearchButton = GuiUtils.addButton(this, null, clearSearchIcon, commandListener,
				MainActionCommands.CLEAR_DATABASE_SEARCH_COMMAND.getName(),
				MainActionCommands.CLEAR_DATABASE_SEARCH_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		importCompoundsButton = GuiUtils.addButton(this, null, importCompoundsIcon, commandListener,
				MainActionCommands.IMPORT_COMPOUNDS_TO_DATABASE_COMMAND.getName(),
				MainActionCommands.IMPORT_COMPOUNDS_TO_DATABASE_COMMAND.getName(), buttonDimension);

//		exportCompoundsButton = GuiUtils.addButton(this, null, exportCompoundsIcon, commandListener,
//				MainActionCommands.EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND.getName(),
//				MainActionCommands.EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		editEntryButton = GuiUtils.addButton(this, null, editFeatureIcon, commandListener,
				MainActionCommands.EDIT_DATABASE_ENTRY_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_DATABASE_ENTRY_DIALOG_COMMAND.getName(), buttonDimension);

		deleteEntryButton = GuiUtils.addButton(this, null, deleteFeatureIcon, commandListener,
				MainActionCommands.DELETE_DATABASE_ENTRY_COMMAND.getName(),
				MainActionCommands.DELETE_DATABASE_ENTRY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

//		newLibraryButton = GuiUtils.addButton(this, null, newLibraryIcon, commandListener,
//				MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName(),
//				MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName(), buttonDimension);

		addToLibraryButton = GuiUtils.addButton(this, null, addToLibraryIcon, commandListener,
				MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		pubChemImportButton = GuiUtils.addButton(this, null, pubChemImportIcon, commandListener,
				MainActionCommands.SHOW_PUBCHEM_DATA_LOADER.getName(),
				MainActionCommands.SHOW_PUBCHEM_DATA_LOADER.getName(),
				buttonDimension);

		addCompoundButton = GuiUtils.addButton(this, null, addCompoundIcon, commandListener,
				MainActionCommands.SHOW_CUSTOM_COMPOUND_LOADER.getName(),
				MainActionCommands.SHOW_CUSTOM_COMPOUND_LOADER.getName(),
				buttonDimension);

		importCompoundsButton.setEnabled(false);
		editEntryButton.setEnabled(false);
		deleteEntryButton.setEnabled(false);
		
		addSeparator(buttonDimension);
		
		curateCompoundButton = GuiUtils.addButton(this, null, curateCompoundIcon, commandListener,
				MainActionCommands.SHOW_COMPOUND_DATABASE_CURATOR.getName(),
				MainActionCommands.SHOW_COMPOUND_DATABASE_CURATOR.getName(),
				buttonDimension);
		curateCompoundButton.setEnabled(false);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
