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

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameCategory;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MoleculeProperties;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseCache;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableCompoundClasyFireViewer;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableCompoundPropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableConcentrationsTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableDatabaseLinksTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableNarrativeDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableSynonymsTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.EditSynonymDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.SynonymTableModelListener;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.CompoundDatabaseCuratorFrame;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport.AddCustomCompoundDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport.AddPubchemCompoundsDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport.DatabaseCompoundImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.CompoundMsReadyCuratorFrame;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.spectrum.DockableSpectraTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.tauto.TautomerCuratorFrame;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockableMSMSLibraryEntryPropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.plot.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb.PubChemDataFetchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb.SearchCompoundDatabaseTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class CompoundDatabasePanel extends DockableMRC2ToolboxPanel implements ListSelectionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -1518116406412542826L;

	private DockableDatabaseCompoundTable compoundTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockableNarrativeDataPanel narrativeDataPanel;
	private DockableDatabaseLinksTable dbLinksTable;
	private DockableSynonymsTable synonymsTable;
	private DockableCompoundPropertiesTable propertiesTable;
	private DockableSpectraTable spectraTable;
	private DockableConcentrationsTable concentrationsTable;
	private DatabaseSearchDialog databaseSearchDialog;
	private DatabaseEntryEditDialog databaseEntryEditDialog;
	private DatabaseToLibraryDialog dbLibDialog;
	private DatabaseCompoundImportDialog databaseCompoundImportDialog;
	private DockableCompoundDatabaseSearchPanel compoundDatabaseSearchPanel;
	private AddPubchemCompoundsDialog addPubchemCompoundsDialog;
	private AddCustomCompoundDialog addCustomCompoundDialog;
	private EditSynonymDialog editSynonymDialog;
	private DockableSpectumPlot msTwoPlot;
	private DockableMsMsTable msTwoTable;
	private DockableMSMSLibraryEntryPropertiesTable msmsLibraryEntryPropertiesTable;
	private IndeterminateProgressDialog idp;
	private DockableCompoundClasyFireViewer clasyFireViewer;
	private SynonymTableModelListener synMoldelListener;
	private BatchCompoundDatabaseSearchDialog batchCompoundDatabaseSearchDialog;
	private CompoundDatabaseCuratorFrame compoundDatabaseCuratorFrame;
	private static CompoundMsReadyCuratorFrame compoundMsReadyCuratorFrame;	
	private static TautomerCuratorFrame tautomerCuratorFrame;

	private static final Icon componentIcon = GuiUtils.getIcon("pubChem", 16);
	private static final Icon clearSearchIcon = GuiUtils.getIcon("clearSearch", 24);
	private static final Icon importCompoundsIcon = GuiUtils.getIcon("importLibraryToDb", 24);
	private static final Icon exportCompoundsIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 24);
	private static final Icon addToLibraryIcon = GuiUtils.getIcon("databaseToLibrary", 24);
	private static final Icon editFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 24);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 24);
	private static final Icon pubChemImportIcon = GuiUtils.getIcon("pubChemDownload", 24);
	private static final Icon addCompoundIcon = GuiUtils.getIcon("addCompound", 24);
	private static final Icon findCompoundListIcon = GuiUtils.getIcon("findList", 24);
	private static final Icon curateCompoundIcon = GuiUtils.getIcon("curateCompound", 24);

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "CompoundDatabasePanel.layout");

	public CompoundDatabasePanel() {

		super("CompoundDatabasePanel", PanelList.DATABASE.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new CompoundDatabaseMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		compoundTable = new DockableDatabaseCompoundTable();
		compoundTable.getTable().addCompoundPopupListener(this);
		compoundTable.getTable().getSelectionModel().addListSelectionListener(this);

		molStructurePanel = new DockableMolStructurePanel(
				"CompoundDatabasePanelDockableMolStructurePanel");
		narrativeDataPanel = new DockableNarrativeDataPanel();
		dbLinksTable = new DockableDatabaseLinksTable();
		synonymsTable = new DockableSynonymsTable(this);

		synMoldelListener =
				new SynonymTableModelListener(synonymsTable.getTable(),
						compoundTable.getTable());
		synonymsTable.getTable().setSynonymsModelListener(synMoldelListener);

		propertiesTable = 
				new DockableCompoundPropertiesTable("DatabaseCompoundPropertiesTable");
		concentrationsTable = new DockableConcentrationsTable();
		spectraTable = new DockableSpectraTable();
		spectraTable.getTable().getSelectionModel().addListSelectionListener(this);
		compoundDatabaseSearchPanel = new DockableCompoundDatabaseSearchPanel(this);
		
		msTwoPlot = new DockableSpectumPlot(
				"CompoundDatabasePanelDockableSpectumPlot", "Library MSMS plot");
		msTwoTable = new DockableMsMsTable(
				"CompoundDatabasePanelDockableMsMsTable", "Library MSMS table");
		msmsLibraryEntryPropertiesTable = new DockableMSMSLibraryEntryPropertiesTable();
		
		clasyFireViewer = new DockableCompoundClasyFireViewer();

		grid.add(0, 0, 75, 40, compoundTable);
		grid.add(75, 0, 25, 40, molStructurePanel, compoundDatabaseSearchPanel);
		grid.add(0, 50, 100, 60, narrativeDataPanel, dbLinksTable, synonymsTable, 				
				propertiesTable, concentrationsTable, spectraTable, 
				msTwoPlot, msTwoTable, msmsLibraryEntryPropertiesTable, clasyFireViewer);
		grid.select(0, 50, 100, 60, narrativeDataPanel);
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		
		databaseSearchDialog = new DatabaseSearchDialog(this);
		dbLibDialog = new DatabaseToLibraryDialog();		
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_DATABASE_BATCH_SEARCH_COMMAND.getName(),
				MainActionCommands.SHOW_DATABASE_BATCH_SEARCH_COMMAND.getName(), 
				findCompoundListIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAR_DATABASE_SEARCH_COMMAND.getName(),
				MainActionCommands.CLEAR_DATABASE_SEARCH_COMMAND.getName(), 
				clearSearchIcon, this));
		
		menuActions.addSeparator();
		
		SimpleButtonAction editAction = GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_DATABASE_ENTRY_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_DATABASE_ENTRY_DIALOG_COMMAND.getName(), 
				editFeatureIcon, this);
		editAction.setEnabled(false);
		menuActions.add(editAction);
		
		SimpleButtonAction deleteAction = GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_DATABASE_ENTRY_COMMAND.getName(),
				MainActionCommands.DELETE_DATABASE_ENTRY_COMMAND.getName(), 
				deleteFeatureIcon, this);
		deleteAction.setEnabled(false);
		menuActions.add(deleteAction);
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND.getName(), 
				addToLibraryIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_PUBCHEM_DATA_LOADER.getName(),
				MainActionCommands.SHOW_PUBCHEM_DATA_LOADER.getName(), 
				pubChemImportIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_CUSTOM_COMPOUND_LOADER.getName(),
				MainActionCommands.SHOW_CUSTOM_COMPOUND_LOADER.getName(), 
				addCompoundIcon, this));
		
		SimpleButtonAction importAction = GuiUtils.setupButtonAction(
				MainActionCommands.SETUP_BATCH_COMPOUND_IMPORT_TO_DATABASE_COMMAND.getName(),
				MainActionCommands.SETUP_BATCH_COMPOUND_IMPORT_TO_DATABASE_COMMAND.getName(), 
				importCompoundsIcon, this);
		importAction.setEnabled(false);
		menuActions.add(importAction);
		
		SimpleButtonAction exportAction = GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND.getName(),
				MainActionCommands.EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND.getName(), 
				exportCompoundsIcon, this);
		exportAction.setEnabled(false);
		menuActions.add(exportAction);
		
		menuActions.addSeparator();
		
		SimpleButtonAction curateAction = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_COMPOUND_DATABASE_CURATOR.getName(),
				MainActionCommands.SHOW_COMPOUND_DATABASE_CURATOR.getName(), 
				curateCompoundIcon, this);
		curateAction.setEnabled(false);
		menuActions.add(curateAction);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(event);
		
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SHOW_DATABASE_SEARCH_COMMAND.getName())) {

			databaseSearchDialog.setLocationRelativeTo(this.getContentPane());
			databaseSearchDialog.setVisible(true);
		}

		if (command.equals(MainActionCommands.SEARCH_DATABASE_COMMAND.getName()))
			searchDatabase();

		if (command.equals(MainActionCommands.CLEAR_DATABASE_SEARCH_COMMAND.getName()))
			clearPanel();

		if (command.equals(MainActionCommands.SETUP_BATCH_COMPOUND_IMPORT_TO_DATABASE_COMMAND.getName()))
			setupBatchCompoundImport();
				
		if (command.equals(MainActionCommands.IMPORT_COMPOUNDS_TO_DATABASE_COMMAND.getName())) 
			batchImportCompounds();

		if (command.equals(MainActionCommands.EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND.getName())) {

		}

		if (command.equals(MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName())) {

		}

		if (command.equals(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName()))
			showAddSelectedDatabaseEntryToActiveLibraryDialog();

		if (command.equals(MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND.getName()))
			showAddCompoundListToActiveLibraryDialog();

		if (command.equals(MainActionCommands.EDIT_DATABASE_ENTRY_DIALOG_COMMAND.getName()))
			editCompoundDatabaseEntry();

		if (command.equals(MainActionCommands.EDIT_DATABASE_ENTRY_COMMAND.getName()))
			editSelectedDatabaseEntry();

		if (command.equals(MainActionCommands.DELETE_DATABASE_ENTRY_COMMAND.getName()))
			deleteSelectedDatabaseEntry();

		if (command.equals(MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName()))
			copyCompoundAccession();

		if (command.equals(MainActionCommands.COPY_COMPOUND_IDENTITY_COMMAND.getName()))
			copyCompoundIdentity();

		if(command.equals(MainActionCommands.SHOW_PUBCHEM_DATA_LOADER.getName()))
			openPubChemLoader();

		if(command.equals(MainActionCommands.FETCH_PUBCHEM_DATA.getName()))
			fetchPubChemData();

		if(command.equals(MainActionCommands.SHOW_CUSTOM_COMPOUND_LOADER.getName()))
			openCustomCompoundLoader();

		if(command.equals(MainActionCommands.SAVE_CUSTOM_COMPOUND_DATA.getName()))
			saveCustomCompoundData();

		//	Synonyms editor
		if(command.equals(MainActionCommands.ADD_SYNONYM_DIALOG_COMMAND.getName()))
			showSynonymEditor(null);

		if(command.equals(MainActionCommands.EDIT_SYNONYM_DIALOG_COMMAND.getName()))
			showSynonymEditor(synonymsTable.getSelectedNames());

		if(command.equals(MainActionCommands.ADD_SYNONYM_COMMAND.getName()))
			updateSynonyms();
		
		if(command.equals(MainActionCommands.EDIT_SYNONYM_COMMAND.getName()))
			updateSynonyms();

		if(command.equals(MainActionCommands.DELETE_SYNONYM_COMMAND.getName()))
			deleteSynonyms();

		if(command.equals(MainActionCommands.SHOW_DATABASE_BATCH_SEARCH_COMMAND.getName()))
			showDatabaseBatchSearchDialog();

		if(command.equals(MainActionCommands.BATCH_SEARCH_COMPOUND_DATABASE_COMMAND.getName()))
			batchSearchCompoundDatabase();
		
		if(command.equals(MainActionCommands.SHOW_COMPOUND_DATABASE_CURATOR.getName()))
			showCompoundDatabaseCurator();	
		
		if(command.equals(MainActionCommands.SHOW_MS_READY_COMPOUND_CURATOR.getName()))
			showMsReadyCompoundCurator();		
		
		if(command.equals(MainActionCommands.SHOW_TAUTOMER_CURATOR.getName()))
			showTautomerCurator();	
	}
	
	private void showTautomerCurator() {
		
		if(tautomerCuratorFrame != null) {
			
			if(!tautomerCuratorFrame.isVisible())
				tautomerCuratorFrame.setVisible(true);
			
			tautomerCuratorFrame.toFront();
			return;
		}		
		tautomerCuratorFrame = new TautomerCuratorFrame();
		tautomerCuratorFrame.setLocationRelativeTo(this.getContentPane());
		tautomerCuratorFrame.setVisible(true);
		tautomerCuratorFrame.toFront();
	}
	
	private void showMsReadyCompoundCurator() {
		
		if(compoundMsReadyCuratorFrame != null) {
			
			if(!compoundMsReadyCuratorFrame.isVisible())
				compoundMsReadyCuratorFrame.setVisible(true);
			
			compoundMsReadyCuratorFrame.toFront();
			return;
		}		
		compoundMsReadyCuratorFrame = new CompoundMsReadyCuratorFrame();
		compoundMsReadyCuratorFrame.setLocationRelativeTo(this.getContentPane());
		compoundMsReadyCuratorFrame.setVisible(true);
		compoundMsReadyCuratorFrame.toFront();
	}	
	
	private void editCompoundDatabaseEntry() {
		// TODO Auto-generated method stub
		CompoundIdentity id = null;
		databaseEntryEditDialog = new DatabaseEntryEditDialog(this);
		databaseEntryEditDialog.loadData(id);
		databaseEntryEditDialog.setLocationRelativeTo(this.getContentPane());
		databaseEntryEditDialog.setVisible(true);
	}

	private void setupBatchCompoundImport() {
		
		databaseCompoundImportDialog = new DatabaseCompoundImportDialog(this);
		databaseCompoundImportDialog.setLocationRelativeTo(this.getContentPane());
		databaseCompoundImportDialog.setVisible(true);
	}

	private void batchImportCompounds() {
		// TODO Auto-generated method stub
		
		
		databaseCompoundImportDialog.dispose();
	}

	private void showCompoundDatabaseCurator() {
		
		if(compoundDatabaseCuratorFrame == null) {
			compoundDatabaseCuratorFrame = new CompoundDatabaseCuratorFrame();
			compoundDatabaseCuratorFrame.setLocationRelativeTo(this.getContentPane());
		}
		compoundDatabaseCuratorFrame.setVisible(true);
	}

	private void batchSearchCompoundDatabase() {
		// TODO Auto-generated method stub



		batchCompoundDatabaseSearchDialog.dispose();
	}

	private void showDatabaseBatchSearchDialog() {

		batchCompoundDatabaseSearchDialog = new BatchCompoundDatabaseSearchDialog(this);
		batchCompoundDatabaseSearchDialog.setLocationRelativeTo(this.getContentPane());
		batchCompoundDatabaseSearchDialog.setVisible(true);
	}

	private void saveCustomCompoundData() {

		Collection<String>errors = addCustomCompoundDialog.validateInputData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), addCustomCompoundDialog);
			return;
		}
		CompoundDatabaseEnum db = addCustomCompoundDialog.getIdSourceDatabase();
		String accession = addCustomCompoundDialog.getAccession();
		if(db.equals(CompoundDatabaseEnum.MRC2_MSMS)) {
			
			try {
				accession = SQLUtils.getNextIdFromSequence(
						"MRC2_COMPOUND_SEQ",
						DataPrefix.MRC2_COMPOUND,
						"0",
						4);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(accession == null) {
				System.out.println("Could not generate compound ID!");
				return;
			}
		}
		Collection<String>synonyms = addCustomCompoundDialog.getSynonymList();
		String description = addCustomCompoundDialog.getDescription();
		IAtomContainer molecule = null;
		try {
			molecule = addCustomCompoundDialog.generateMolData();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(molecule != null) {

			CompoundIdentity newCompound = new CompoundIdentity(db, accession);
			IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(molecule);
			newCompound.setCommonName(addCustomCompoundDialog.getName());
			newCompound.setExactMass(MolecularFormulaManipulator.getTotalExactMass(molFormula));
			newCompound.setFormula(MolecularFormulaManipulator.getString(molFormula));
			newCompound.setSmiles(addCustomCompoundDialog.getSmiles());
			newCompound.setInChi(molecule.getProperty(MoleculeProperties.INCHI.name()));
			newCompound.setInChiKey(molecule.getProperty(MoleculeProperties.INCHIKEY.name()));
			CompoundNameSet nameSet = new CompoundNameSet(accession);
			for(String synonym : synonyms)
				nameSet.addName(synonym, CompoundNameCategory.SYN.name());

			nameSet.setPrimaryName(newCompound.getCommonName());
			CompoundIdentity inserted = null;
			try {
				inserted = CompoundDatabaseUtils.insertNewCompound(newCompound, nameSet, description);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(inserted != null) {

				compoundTable.setTableModelFromCompoundCollection(Collections.singleton(inserted));
				compoundTable.getTable().setRowSelectionInterval(0, 0);
			}
		}
		addCustomCompoundDialog.dispose();
	}

	private void openCustomCompoundLoader() {

		addCustomCompoundDialog = new AddCustomCompoundDialog(this);
		addCustomCompoundDialog.setLocationRelativeTo(this.getContentPane());
		addCustomCompoundDialog.setVisible(true);
	}

	private void showSynonymEditor(Map<String, Boolean> selectedNames) {

		if (compoundTable.getSelectedCompound() == null)
			return;

		if(selectedNames == null) {
			editSynonymDialog = new EditSynonymDialog(null, false, this);
			editSynonymDialog.setLocationRelativeTo(this.getContentPane());
			editSynonymDialog.setVisible(true);
		}
		else {
			if(selectedNames.isEmpty())
				return;

			Entry<String, Boolean> entry = selectedNames.entrySet().stream().findFirst().get();
			editSynonymDialog = new EditSynonymDialog(entry.getKey(), entry.getValue(), this);
			editSynonymDialog.setLocationRelativeTo(this.getContentPane());
			editSynonymDialog.setVisible(true);
		}
	}

	private void updateSynonyms() {
						
		CompoundNameSet nameSet = synonymsTable.getNameSet();
		if(nameSet == null)
			return;

		String original = editSynonymDialog.getOriginalName();
		String newName = editSynonymDialog.getEditedName();
		if(newName.isEmpty()) {
			MessageDialog.showErrorMsg("Name can not be empty!", editSynonymDialog);
			return;
		}
		synonymsTable.getTable().getModel().removeTableModelListener(synMoldelListener);
		String nameType = CompoundNameCategory.SYN.name();
		if(editSynonymDialog.isNamePrimary())
			nameType = CompoundNameCategory.PRI.name();

		if(original != null)
			nameSet.removeNames(Collections.singleton(original));

		nameSet.addName(newName, nameType);
		try {
			CompoundDatabaseUtils.updateSynonyms(nameSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		compoundTable.getSelectedIdentity().getCompoundIdentity().setCommonName(nameSet.getPrimaryName());
		compoundTable.updateCidData(compoundTable.getSelectedIdentity());
		editSynonymDialog.dispose();		
		compoundTable.getSelectedIdentity().getCompoundIdentity().setCommonName(nameSet.getPrimaryName());
		compoundTable.updateCidData(compoundTable.getSelectedIdentity());		
		synonymsTable.getTable().getModel().addTableModelListener(synMoldelListener);
		synonymsTable.loadNameSet(nameSet);
	}

	private void deleteSynonyms() {

		if (compoundTable.getSelectedIdentity() == null ||
				synonymsTable.getSelectedNames().isEmpty())
			return;

		CompoundNameSet nameSet = synonymsTable.getCurrentNameSet();
		if(synonymsTable.getSelectedNames().size() == nameSet.getSynonyms().size()) {
			MessageDialog.showErrorMsg("You can not deleta all compound sysnonyms!", this.getContentPane());
			return;
		}
		String yesNoQuestion = "Do you want to delete selected synonyms?";
		if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion,
				this.getContentPane()) == JOptionPane.NO_OPTION)
			return;

		nameSet.removeNames(synonymsTable.getSelectedNames().keySet());
		try {
			CompoundDatabaseUtils.updateSynonyms(nameSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synonymsTable.loadNameSet(nameSet);
		compoundTable.updateCidData(compoundTable.getSelectedIdentity());
	}

	private void copyCompoundAccession() {

		if (compoundTable.getSelectedCompound() == null)
			return;

		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(
				compoundTable.getSelectedCompound().getPrimaryDatabaseId());
		clpbrd.setContents(stringSelection, null);
	}

	private void copyCompoundIdentity() {
		// TODO Auto-generated method stub
		if (compoundTable.getSelectedCompound() == null)
			return;

	}

	private void importCompoundsFromFile(File selectedFile) {
		// TODO Auto-generated method stub

		databaseCompoundImportDialog.setVisible(false);
	}

	private void showAddCompoundListToActiveLibraryDialog() {

		Collection<CompoundIdentity> compoundList = compoundTable.getListedCompounds();
		if(compoundList.isEmpty())
			return;

		if (MRC2ToolBoxCore.getActiveMsLibraries().isEmpty()) {

			MessageDialog.showErrorMsg(
					"No active libraries available.\n"
							+ "Please use library manager to load one or more libraries from the database first.",
					this.getContentPane());
			return;
		}


	}

	private void showAddSelectedDatabaseEntryToActiveLibraryDialog() {

		if (compoundTable.getSelectedCompound() == null)
			return;

		if (MRC2ToolBoxCore.getActiveMsLibraries().isEmpty()) {

			MessageDialog.showErrorMsg(
					"No active libraries available.\n"
							+ "Please use library manager to load one or more libraries from the database first.",
					this.getContentPane());
			return;
		}
		CompoundIdentity id = compoundTable.getSelectedCompound();
		dbLibDialog.updateDialog(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName(), id);
		dbLibDialog.setLocationRelativeTo(this.getContentPane());
		dbLibDialog.setVisible(true);
	}

	private void editSelectedDatabaseEntry() {
		// TODO Auto-generated method stub

	}

	private void deleteSelectedDatabaseEntry() {
		// TODO Auto-generated method stub

	}

	private void searchDatabaseOld() {

		String cpdName = compoundDatabaseSearchPanel.getCompoundName();
		String molFormula = compoundDatabaseSearchPanel.getFormula();
		String cpdId = compoundDatabaseSearchPanel.getId();
		String inchi = compoundDatabaseSearchPanel.getInChi();
		Range massRange = compoundDatabaseSearchPanel.getMassRange();

		// Check if any search parameters provided
		if (cpdName.isEmpty() && molFormula.isEmpty() && cpdId.isEmpty() && inchi.isEmpty() && massRange == null) {
			MessageDialog.showErrorMsg("No search parameters specified!", this.getContentPane());
			return;
		}
		if (!molFormula.isEmpty()) {

			try {
				IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molFormula,
						DefaultChemObjectBuilder.getInstance());
			} catch (Exception e) {

				MessageDialog.showErrorMsg("Formula not valid!", this.getContentPane());
				return;
			}
		}
		clearPanel();
		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				cpdName,
				molFormula,
				cpdId,
				inchi,
				massRange,
				compoundDatabaseSearchPanel.getExactMatch(),
				compoundDatabaseSearchPanel.searchSynonyms(),
				compoundDatabaseSearchPanel.allowSpellingErrors());
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}

	private void searchDatabase() {

		String cpdName = compoundDatabaseSearchPanel.getCompoundName();
		String molFormula = compoundDatabaseSearchPanel.getFormula();
		String cpdId = compoundDatabaseSearchPanel.getId();
		String inchi = compoundDatabaseSearchPanel.getInChi();
		Range massRange = compoundDatabaseSearchPanel.getMassRange();

		// Check if any search parameters provided
		if (cpdName.isEmpty() && molFormula.isEmpty() && cpdId.isEmpty() && inchi.isEmpty() && massRange == null) {
			MessageDialog.showErrorMsg("No search parameters specified!", this.getContentPane());
			return;
		}
		if (!molFormula.isEmpty()) {
			try {
				IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molFormula,
						DefaultChemObjectBuilder.getInstance());
			} catch (Exception e) {

				MessageDialog.showErrorMsg("Formula not valid!", this.getContentPane());
				return;
			}
		}
		clearPanel();
		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				cpdName,
				molFormula,
				cpdId,
				inchi,
				massRange,
				compoundDatabaseSearchPanel.getNameScope(),
				compoundDatabaseSearchPanel.getNameMatchFidelity());
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}

	public void searchDatabaseById(String accession) {

		if (accession == null)
			return;

		if (accession.isEmpty())
			return;

		String empty = "";
		databaseSearchDialog.clearForm();
		databaseSearchDialog.setId(accession);
		clearPanel();
		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				empty,
				empty,
				accession,
				empty,
				null,
				true,
				false,
				false);
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}

	public void searchDatabaseByFormula(String molFormula) {

		if (molFormula == null)
			return;

		if (molFormula.isEmpty())
			return;

		try {
			IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molFormula,
					DefaultChemObjectBuilder.getInstance());
		} catch (Exception e) {
			MessageDialog.showErrorMsg("Formula not valid!", this.getContentPane());
			return;
		}
		String empty = "";
		databaseSearchDialog.clearForm();
		databaseSearchDialog.setFormula(molFormula);
		clearPanel();
		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				empty,
				molFormula,
				empty,
				empty,
				null,
				true,
				false,
				false);
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}

	public void searchDatabaseByMass(Double mass) {

		if (mass == 0.0d)
			return;

		clearPanel();

		String empty = "";
		databaseSearchDialog.clearForm();
		databaseSearchDialog.setLookupMass(mass);
		Range massRange = databaseSearchDialog.getMassRange();

		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				empty,
				empty,
				empty,
				empty,
				massRange,
				true,
				false,
				false);
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}

	@Override
	public synchronized void clearPanel() {

		compoundTable.clearTable();
		clearDataPanels();
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(SearchCompoundDatabaseTask.class))
				showSearchResults((SearchCompoundDatabaseTask) e.getSource());

			if (e.getSource().getClass().equals(PubChemDataFetchTask.class))
				showImportedCompounds((PubChemDataFetchTask)e.getSource());
		}
	}

	private void showImportedCompounds(PubChemDataFetchTask importTask) {

		if(!importTask.getFetchLog().isEmpty()) {
			InformationDialog id = new InformationDialog(
					"PubChem import log",
					"Errors and notes for compound data import from PubChem:",
					StringUtils.join(importTask.getFetchLog(), "\n"),
					addPubchemCompoundsDialog);
		}
		loadCompoundDataByReference(importTask.getImportedIds());
//		if(!importTask.getImportedIds().isEmpty()) {
//			compoundTable.setTableModelFromCompoundCollection(importTask.getImportedIds());
//			compoundTable.getTable().setRowSelectionInterval(0, 0);
//		}
		addPubchemCompoundsDialog.dispose();
	}

	private void showSearchResults(SearchCompoundDatabaseTask searchTask) {

		loadCompoundDataByReference(searchTask.getCompoundList());		
//		if (searchTask.getCompoundList().isEmpty())
//			return;
//
//		compoundTable.setTableModelFromCompoundCollection(searchTask.getCompoundList());
//		if(compoundTable.getTable().getRowCount() > 0)
//			compoundTable.getTable().setRowSelectionInterval(0, 0);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(e.getValueIsAdjusting() || e.getSource() == null) 
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(compoundTable.getTable())) {
				
				clearDataPanels();
				if (compoundTable.getSelectedCompound() != null)
					loadCompoundData(compoundTable.getSelectedCompound());
				
				return;
			}
			if(listener.equals(spectraTable.getTable())) {
				
				clearSpectrumDataPanels();
				MsMsLibraryFeature feature = spectraTable.getSelectedFeature();
				if(feature != null) {
					msTwoPlot.showLibraryTandemMs(feature);
					msTwoTable.setTableModelFromDataPoints(feature.getSpectrum(), feature.getParent());
					msmsLibraryEntryPropertiesTable.showMsMsLibraryFeatureProperties(feature);
				}
				return;
			}
		}	
	}
	
	private void clearSpectrumDataPanels() {
		
		msTwoPlot.removeAllDataSets();
		msTwoTable.clearTable();
		msmsLibraryEntryPropertiesTable.clearTable();
	}

	private void clearDataPanels() {

		narrativeDataPanel.clearPanel();
		synonymsTable.clearTable();
		dbLinksTable.clearTable();
		propertiesTable.clearTable();
		concentrationsTable.clearTable();
		spectraTable.clearTable();
		molStructurePanel.clearPanel();
		clearSpectrumDataPanels();
		clasyFireViewer.clearPanel();
	}
	
	public void loadCompoundDataByReference(Collection<CompoundIdentity> cpdIds) {
		
		clearPanel();
		if (cpdIds== null || cpdIds.isEmpty())
			return;

		compoundTable.setTableModelFromCompoundCollection(cpdIds);
		if(compoundTable.getTable().getRowCount() > 0)
			compoundTable.getTable().setRowSelectionInterval(0, 0);
	}

	public void loadCompoundData(CompoundIdentity cpd) {

		CompoundDataRetrievalTask task = new CompoundDataRetrievalTask(cpd);
		idp = new IndeterminateProgressDialog("Fetching compound data ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	private void fetchPubChemData() {

		Collection<String> idList = addPubchemCompoundsDialog.getIdList();
		if(idList.isEmpty()) {
			MessageDialog.showErrorMsg("No valid IDs specified!", addPubchemCompoundsDialog);
			return;
		}
		PubChemDataFetchTask task = new PubChemDataFetchTask(idList);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void openPubChemLoader() {

		addPubchemCompoundsDialog = new AddPubchemCompoundsDialog(this);
		addPubchemCompoundsDialog.setLocationRelativeTo(this.getContentPane());
		addPubchemCompoundsDialog.setVisible(true);
	}

	@Override
	public void reloadDesign() {
		super.switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void closeExperiment() {
		super.closeExperiment();
		menuBar.updateMenuFromExperiment(null, null);
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
	
	class CompoundDataRetrievalTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private CompoundIdentity cpd;		

		public CompoundDataRetrievalTask(CompoundIdentity cpd) {
			this.cpd = cpd;
//			cpdIds = null;
		}
		
//		private Collection<CompoundIdentity> cpdIds;
//		public CompoundDataRetrievalTask(Collection<CompoundIdentity> cpdIds) {
//			this.cid = null;
//			this.cpdIds = cpdIds;
//		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {
				if(cpd != null) {
					
					molStructurePanel.showStructure(cpd.getSmiles());
					narrativeDataPanel.loadCompoundData(cpd);
					synonymsTable.loadCompoundData(cpd);
					dbLinksTable.loadCompoundData(cpd);

					//	TODO properties
					concentrationsTable.setModelFromConcentrations(
							CompoundDatabaseCache.getConcentrationsForCompound(cpd));
					
					spectraTable.setTableModelFromLibraryFeatureCollection(
							CompoundDatabaseCache.getMSMSLibraryEntriesForCompound(cpd));
					
					clasyFireViewer.showCompoundData(cpd.getPrimaryDatabaseId());
				}
//				if(cpdIds != null)
//					loadCompoundDataByReference(cpdIds);
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}
