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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.database.idt.BasePCDLutils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.CompoundDatabasePanel;
import edu.umich.med.mrc2.datoolbox.gui.io.msms.DecoyMSMSLibraryImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.msms.ReferenceMSMSLibraryExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableLibraryFeatureEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.manager.LibraryInfoDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.manager.LibraryManager;
import edu.umich.med.mrc2.datoolbox.gui.library.manager.NewPCLDLfromBaseDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.upload.LibraryRtImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.InfoDialogType;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ReferenceMSMSLibraryExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.IDTraclerLibraryImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibEditorImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LoadDatabaseLibraryTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.PCDLTextLibraryImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.PCDLfromBaseLibraryTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms.DecoyLibraryGenerationTask;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MsLibraryPanel extends DockableMRC2ToolboxPanel implements ItemListener {

	private DockableLibraryFeatureTable libraryFeatureTable;
	private DockableMolStructurePanel molStructurePanel;
	private LibraryManager libraryManager;
	private LibraryExportDialog libraryExportDialog;
	private LibraryInfoDialog libraryInfoDialog;
	private ConvertLibraryForRecursionDialog convertLibraryForRecursionDialog;
	private DockableLibraryFeatureEditorPanel libraryFeatureEditorPanel;
	private CompoundLibrary currentLibrary;
	private File baseDirectory;
	private boolean showFeaturePending = false;
	private String pendingFeatureId = null;
	private LibraryRtImportDialog libraryRtImportDialog;
	private NewPCLDLfromBaseDialog newPCLDLfromBaseDialog;

	private static final Icon componentIcon = GuiUtils.getIcon("editLibrary", 16);
	private static final Icon libraryManagerIcon = GuiUtils.getIcon("libraryManager", 24);
	private static final Icon closeLibraryIcon = GuiUtils.getIcon("close", 24);
	private static final Icon importLibraryIcon = GuiUtils.getIcon("importLibraryToDb", 24);
	private static final Icon exportLibraryIcon = GuiUtils.getIcon("exportLibrary", 24);
	private static final Icon exportFilteredLibraryIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 24);
	private static final Icon mergeLibrariesIcon = GuiUtils.getIcon("mergeLibraries", 24);
	private static final Icon newFeatureIcon = GuiUtils.getIcon("newLibraryFeature", 24);
	private static final Icon editFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 24);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 24);
	private static final Icon importRtIcon = GuiUtils.getIcon("importLibraryRtValues", 24);
	private static final Icon libraryExportIcon = GuiUtils.getIcon("exportLibrary", 24);
	private static final Icon libraryImportIcon = GuiUtils.getIcon("importLibraryToDb", 24);
	
	

	private static final File layoutConfigFile = new File(
			MRC2ToolBoxCore.configDir + "MsLibraryPanel.layout");

	public MsLibraryPanel() {

		super("MsLibraryPanel", PanelList.MS_LIBRARY.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new MsLibraryPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		libraryFeatureTable = new DockableLibraryFeatureTable(
				"MsLibraryPanelDockableLibraryFeatureTable", "Library feature listing", this);
		molStructurePanel = new DockableMolStructurePanel(
				"MsLibraryPanelDockableMolStructurePanel");
		libraryFeatureEditorPanel = new DockableLibraryFeatureEditorPanel(this);

		grid.add(0, 0, 75, 40, libraryFeatureTable);
		grid.add(75, 0, 25, 40, molStructurePanel);
		grid.add(0, 50, 100, 60, libraryFeatureEditorPanel);
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		((MsLibraryPanelMenuBar)menuBar).createActiveLibraryLabelBlock();
		baseDirectory = new File(MRC2ToolBoxCore.dataDir);
		currentLibrary = null;
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND.getName(), 
				libraryManagerIcon, this));
		
		menuActions.addSeparator();	
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLOSE_LIBRARY_COMMAND.getName(),
				MainActionCommands.CLOSE_LIBRARY_COMMAND.getName(), 
				closeLibraryIcon, this));
		
		menuActions.addSeparator();	
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND.getName(),
				MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND.getName(), 
				importLibraryIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND.getName(),
				MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND.getName(), 
				importRtIcon, this));
		
		menuActions.addSeparator();	
				
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName(), 
				exportLibraryIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName(), 
				exportFilteredLibraryIcon, this));
		
		menuActions.addSeparator();	
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND.getName(),
				MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND.getName(), 
				mergeLibrariesIcon, this));
		
		menuActions.addSeparator();	
		
		SimpleButtonAction newFeatureAction = GuiUtils.setupButtonAction(
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND.getName(),
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND.getName(), 
				newFeatureIcon, this);
		newFeatureAction.setEnabled(false);
		menuActions.add(newFeatureAction);
		
		SimpleButtonAction editFeatureAction = GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_LIBRARY_FEATURE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_LIBRARY_FEATURE_DIALOG_COMMAND.getName(), 
				editFeatureIcon, this);
		editFeatureAction.setEnabled(false);
		menuActions.add(editFeatureAction);
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName(),
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName(), 
				deleteFeatureIcon, this));
		
		menuActions.addSeparator();	
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName(), 
				libraryExportIcon, this));
				
		SimpleButtonAction importDecoyAction = GuiUtils.setupButtonAction(
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName(),
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName(), 
				libraryImportIcon, this);
		importDecoyAction.setEnabled(false);
		menuActions.add(importDecoyAction);
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

		if (command.equals(MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND.getName()))
			showLibraryManager();
		
		if (command.equals(MainActionCommands.PRESCAN_LIBRARY_COMPOUNDS_COMMAND.getName()))
			prescanLibraryForImport(); 
				
		if (command.equals(MainActionCommands.SET_UP_PCDL_DATA_IMPORT_COMMAND.getName()))
			setupPCDLdataImport();

		if (command.equals(MainActionCommands.IMPORT_PCDL_COMPOUND_LIBRARY_COMMAND.getName()))
			importPCDLLibrary();
		
		if (command.equals(MainActionCommands.NEW_PCDL_LIBRARY_FROM_PCDL_TEXT_FILE_SETUP_COMMAND.getName()))
			setupNewPCDLfromBase();

		if (command.equals(MainActionCommands.NEW_PCDL_LIBRARY_FROM_PCDL_TEXT_FILE_COMMAND.getName()))
			createNewPCDLfromBase();

		if (command.equals(MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND.getName()))
			importLibrary();
		
		if (command.equals(MainActionCommands.IMPORT_IDTRACKER_LIBRARY_COMMAND.getName()))
			importIDTrackerLibrary();
		
		if (command.equals(MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName())
				|| command.equals(MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName()))
			exportLibrary(command);

		if (command.equals(MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND.getName()))
			convertLibraryForRecursion();

		if (command.equals(MainActionCommands.CLOSE_LIBRARY_COMMAND.getName()))
			closeActiveLibrary();

		if (command.equals(MainActionCommands.UNDO_LIBRARY_FEATURE_EDIT_COMMAND.getName()))
			undoLibraryFeatureEdit();

		if (command.equals(MainActionCommands.EDIT_LIBRARY_FEATURE_COMMAND.getName()))
			editSelectedLibraryFeature();

		if (command.equals(MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName()))
			deleteSelectedFeature();

		if (command.equals(MainActionCommands.DUPLICATE_LIBRARY_FEATURE_COMMAND.getName()))
			duplicateSelectedFeature();

		if (command.equals(MainActionCommands.SEARCH_FEATURE_FORMULA_IN_DATABASE_COMMAND.getName()))
			lookupFeatureByFormulaInDatabase();

		if (command.equals(MainActionCommands.SEARCH_FEATURE_MASS_IN_DATABASE_COMMAND.getName()))
			lookupFeatureByMassInDatabase();

		if (command.equals(MainActionCommands.SEARCH_FEATURE_ACCESSION_IN_DATABASE_COMMAND.getName()))
			lookupFeatureByAccessionDatabase();

		if (command.equals(MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND.getName()))
			showLibraryRtImportDialog();

		if (command.equals(MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_COMMAND.getName()))
			importLibraryFeatureRtFromFile();

		if (command.equals(MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_COMPOUND_INCHI_KEY_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_COMPOUND_SMILES_COMMAND.getName())) {
					
			copyCompoundProperty(command);
		}
		
		if (command.equals(MainActionCommands.COPY_MSRT_FEATURE_ID_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_MSRT_FEATURE_NAME_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_MSRT_FEATURE_RT_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_MSRT_FEATURE_AS_SIRIUS_MS_COMMAND.getName())) {			
			copyFeatureProperty(command);
		}
				
		if (command.equals(MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))	
			showRefMSMSLibraryExportDialog();
		
		if (command.equals(MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))	
			showDecoyMSMSLibraryImportDialog();
		
		if (command.equals(MainActionCommands.LOAD_SELECTED_LIBRARY_COMMAND.getName()))
			loadLibrarySelectedFromMenu(event.getSource());
	}

	private void prescanLibraryForImport() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.addFilter("Library Editor files", "xml", "XML");	
		fc.addFilter("TAB-separated text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select library file to scan");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {		

			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			
			//	TODO Agilent Library Editor files
			if (inputFile.getName().toLowerCase().endsWith("mslibrary.xml")) {

				MessageDialog.showWarningMsg(
						"Library Editor files validation under development.", this.getContentPane());
			}
			//	TODO Agilent CEF files
			if (inputFile.getName().toLowerCase().endsWith(".cef")) {
				MessageDialog.showWarningMsg(
						"CEF library validation under development.", this.getContentPane());
			}
			if (inputFile.getName().toLowerCase().endsWith(".txt")
					|| inputFile.getName().toLowerCase().endsWith(".tsv")) {
				PCDLTextLibraryImportTask task = 
						new PCDLTextLibraryImportTask(inputFile, null, null, true);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
	}
	
	private void setupPCDLdataImport() {
		
		if (currentLibrary == null) {
			MessageDialog.showErrorMsg(
					"Create new library or open existing one first\n" 
					+ "in order to import data from file!");
			return;
		}		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("TAB-separated text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select library file to import");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {		

			libraryInfoDialog = new LibraryInfoDialog(this);
			libraryInfoDialog.loadLibraryInfoAndDataForImport(
					currentLibrary, fc.getSelectedFile());
			baseDirectory = fc.getSelectedFile().getParentFile();
			libraryInfoDialog.setLocationRelativeTo(this.getContentPane());
			libraryInfoDialog.setVisible(true);
		}		
	}
	
	private void importPCDLLibrary() {
		
		Collection<String>errors = libraryInfoDialog.validateLibraryData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), libraryInfoDialog);
			return;
		}	
		CompoundLibrary selected = libraryInfoDialog.getLibrary();
		String libraryName = libraryInfoDialog.getLibraryName();
		String libraryDescription = libraryInfoDialog.getLibraryDescription();
		selected.setLibraryName(libraryName);
		selected.setLibraryDescription(libraryDescription);
		try {
			MSRTLibraryUtils.updateLibraryInfo(selected);
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateLibraryMenuAndLabel();
		File inputFile = libraryInfoDialog.getInputLibraryFile();
		Collection<Adduct> adductList = libraryInfoDialog.getSelectedAdducts();
		
		if(inputFile != null && inputFile.exists())
			importLibraryFromFile(inputFile, adductList);
			
		libraryInfoDialog.dispose();
	}
	
	private void setupNewPCDLfromBase() {
	
		GetBasePCDLTask task = new GetBasePCDLTask();
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Loading basePCDL library ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	private void createNewPCDLfromBase() {
		
		Collection<String>errors = newPCLDLfromBaseDialog.validateLibraryData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), newPCLDLfromBaseDialog);
		    return;
		}	
		CompoundLibrary basePCDLlibrary = newPCLDLfromBaseDialog.getBasePCDLlibrary();
		CompoundLibrary newLlibrary = new CompoundLibrary(
				newPCLDLfromBaseDialog.getLibraryName(),
				newPCLDLfromBaseDialog.getLibraryDescription(),
				newPCLDLfromBaseDialog.getPolarity());
		File inputLibraryFile = newPCLDLfromBaseDialog.getInputLibraryFile();
		Collection<Adduct> selectedAdducts = newPCLDLfromBaseDialog.getSelectedAdducts();	
		PCDLfromBaseLibraryTask task = new PCDLfromBaseLibraryTask(
				basePCDLlibrary, 
				newLlibrary, 
				inputLibraryFile,
				selectedAdducts);
		
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		newPCLDLfromBaseDialog.dispose();
	}
	
	class GetBasePCDLTask extends LongUpdateTask {

		private CompoundLibrary basePCDLlibrary;
			
		public GetBasePCDLTask() {
			super();
		}

		@Override
		public Void doInBackground() {
			
			basePCDLlibrary = BasePCDLutils.getPCDLbaseLibrary();			
			return null;
		}
		
	    @Override
	    public void done() {
	    	
	    	super.done();
			newPCLDLfromBaseDialog = new NewPCLDLfromBaseDialog(MsLibraryPanel.this);
			newPCLDLfromBaseDialog.setBasePCDLlibrary(basePCDLlibrary);
			newPCLDLfromBaseDialog.setLocationRelativeTo(MsLibraryPanel.this.getContentPane());
			newPCLDLfromBaseDialog.setVisible(true);
	    }
	}

	private void loadLibrarySelectedFromMenu(Object selectionEventSource) {
		
		if (selectionEventSource instanceof JCheckBoxMenuItem) {
			
			Object libObject = 
					((JCheckBoxMenuItem)selectionEventSource).
						getClientProperty(MsLibraryPanelMenuBar.LIBRARY_OBJECT);

			if(libObject != null && libObject instanceof CompoundLibrary) {
				
				currentLibrary = (CompoundLibrary) libObject;
				libraryFeatureEditorPanel.clearPanel();
				molStructurePanel.clearPanel();
				reloadLibraryData(currentLibrary);
			}
		}
	}	
	
	private void showLibraryRtImportDialog() {

		if(currentLibrary == null)
			return;

		libraryRtImportDialog = new LibraryRtImportDialog(this);
		libraryRtImportDialog.setLocationRelativeTo(this.getContentPane());
		libraryRtImportDialog.setVisible(true);
	}

	private void showLibraryManager() {

		libraryManager = new LibraryManager(this, currentLibrary);		
		libraryManager.setLocationRelativeTo(this.getContentPane());
		libraryManager.setVisible(true);
	}

	private void showRefMSMSLibraryExportDialog() {
		
		ReferenceMSMSLibraryExportDialog dialog = 
				new ReferenceMSMSLibraryExportDialog(this);
		dialog.setLocationRelativeTo(this.getContentPane());
		dialog.setVisible(true);
	}
	
	private void showDecoyMSMSLibraryImportDialog() {
		
		DecoyMSMSLibraryImportDialog dialog = 
				new DecoyMSMSLibraryImportDialog(this);
		dialog.setLocationRelativeTo(this.getContentPane());
		dialog.setVisible(true);
	}
	
	private void copyCompoundProperty(String copyCommand) {
		
		LibraryMsFeature feature = 
				libraryFeatureTable.getTable().getSelectedFeature();
		
		if (feature == null
				|| feature.getPrimaryIdentity() == null
				|| feature.getPrimaryIdentity().getCompoundIdentity() == null)
			return;
		
		CompoundIdentity cpd = libraryFeatureTable.getTable().getSelectedFeature().
				getPrimaryIdentity().getCompoundIdentity();
		
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();		
		StringSelection stringSelection = new StringSelection("");
		
		if(copyCommand.equals(MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName())
				&& cpd.getPrimaryDatabaseId() != null)
			stringSelection = new StringSelection(cpd.getPrimaryDatabaseId());
			
		if(copyCommand.equals(MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName()))
			stringSelection = new StringSelection(cpd.getName());
			
		if(copyCommand.equals(MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName())
				&& cpd.getFormula() != null)
			stringSelection = new StringSelection(cpd.getFormula());
			
		if(copyCommand.equals(MainActionCommands.COPY_COMPOUND_INCHI_KEY_COMMAND.getName())
				&& cpd.getInChiKey() != null)
			stringSelection = new StringSelection(cpd.getInChiKey());
			
		if(copyCommand.equals(MainActionCommands.COPY_COMPOUND_SMILES_COMMAND.getName())
				&& cpd.getSmiles() != null)
			stringSelection = new StringSelection(cpd.getSmiles());
		
		clpbrd.setContents(stringSelection, null);
	}
	
	private void copyFeatureProperty(String copyCommand) {
		
		LibraryMsFeature feature = 
				libraryFeatureTable.getTable().getSelectedFeature();
		
		if (feature == null)
			return;
		
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();		
		StringSelection stringSelection = new StringSelection("");
		
		if(copyCommand.equals(MainActionCommands.COPY_MSRT_FEATURE_ID_COMMAND.getName()))
			stringSelection = new StringSelection(feature.getId());
		
		if(copyCommand.equals(MainActionCommands.COPY_MSRT_FEATURE_NAME_COMMAND.getName())
				&& feature.getName() != null)
			stringSelection = new StringSelection(feature.getName());		
		
		if(copyCommand.equals(MainActionCommands.COPY_MSRT_FEATURE_RT_COMMAND.getName())
				&& feature.getRetentionTime() > 0)
			stringSelection = new StringSelection(
					MRC2ToolBoxConfiguration.getRtFormat().format(feature.getRetentionTime()));
		
		if(copyCommand.equals(MainActionCommands.COPY_MSRT_FEATURE_AS_SIRIUS_MS_COMMAND.getName())
				&& feature.getSpectrum() != null
				&& feature.getSpectrum().getCompletePattern().length > 0) {
			//	TODO
		}		
		clpbrd.setContents(stringSelection, null);
	}

	private void importLibraryFeatureRtFromFile() {

		Collection<String>errors = new ArrayList<String>();
		String[][] inputData = libraryRtImportDialog.getInputData();
		if(inputData == null)
			errors.add("Input data file missing or can not be processed.");

		int rtColumn = libraryRtImportDialog.getRtColumnIndex();
		if(rtColumn == -1)
			errors.add("RT column not specified.");

		int compoundIdColumn = libraryRtImportDialog.getCompoundIdColumnIndex();
		int formulaColumn = libraryRtImportDialog.getFormulaColumnIndex();
		int nameColumn = libraryRtImportDialog.getNameColumnIndex();
		int inchiColumn = libraryRtImportDialog.getinChIKeyColumnIndex();

		if(compoundIdColumn == -1 && nameColumn == -1 && inchiColumn == -1)
			errors.add("Compound name and/or compound identifier and/or InChI key\n" +
				"have to be specified to match RT data to library features.\n" +
				"Molecular formula is optional and will be used only to veryfy the match based on name");

		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), libraryRtImportDialog);
			return;
		}
		List<MsFeature> withRt = currentLibrary.getFeatures().stream().
				filter(f -> f.getRetentionTime() > 0.0d).collect(Collectors.toList());

		int result = JOptionPane.YES_OPTION;
		boolean replaceExistingRts = true;
		if(!withRt.isEmpty()) {
			String yesNoQuestion =
				"Some of the library entries already have assigned retention time.\n" +
				"Select \"Yes\" to replace any assigned retention times with new ones,\n" +
				"\"No\" - to assign new retention times only where it is missing," +
				"\"Cancel\" - to exit RT import process.";

			result = MessageDialog.showChooseOrCancelMsg(yesNoQuestion, libraryRtImportDialog);
		}
		if(result == JOptionPane.CANCEL_OPTION) {
			libraryRtImportDialog.dispose();
			return;
		}
		if(result == JOptionPane.NO_OPTION)
			replaceExistingRts = false;

		// Dry run to find unmatched compounds
		Map<LibraryMsFeature,Double>rtMap = new HashMap<LibraryMsFeature,Double>();
		ArrayList<Integer>unmatched = new ArrayList<Integer>();
		Map<Integer,Collection<? extends MsFeature>>ambiguous = new TreeMap<Integer,Collection<? extends MsFeature>>();
		Collection<LibraryMsFeature> libFeatures = currentLibrary.getFeatures();
		List<MsFeature> matches;

		for(int j=1; j<inputData.length; j++) {

			boolean matched = false;
			matches = new ArrayList<MsFeature>();
			double rt = Double.parseDouble(inputData[j][rtColumn]);

			//	Match on feature name
			if(nameColumn > -1) {

				String compoundName = inputData[j][nameColumn].trim();
				matches = libFeatures.stream().
					filter(f -> f.getName().equalsIgnoreCase(compoundName)).collect(Collectors.toList());

				if(!matches.isEmpty()) {

					for(MsFeature f : matches)
						rtMap.put((LibraryMsFeature) f, rt);

					if(matches.size() > 1)
						ambiguous.put(j, matches);

					matched = true;
				}
				//	TODO check with formula
			}
			//	Match on compound ID - TODO use not just primary ID?
			if(!matched && compoundIdColumn > -1) {

				String compoundId = inputData[j][compoundIdColumn];
				matches = libFeatures.stream().
					filter(f -> Objects.nonNull(f.getPrimaryIdentity().
							getCompoundIdentity().getPrimaryDatabaseId())).
					filter(f -> f.getPrimaryIdentity().getCompoundIdentity().
							getPrimaryDatabaseId().equals(compoundId)).
					collect(Collectors.toList());

				if(!matches.isEmpty()) {

					for(MsFeature f : matches)
						rtMap.put((LibraryMsFeature) f, rt);

					if(matches.size() > 1)
						ambiguous.put(j, matches);

					matched = true;
				}
			}
			//	Match on InChI key
			if(!matched && inchiColumn > -1) {

				String inchiKey = inputData[j][inchiColumn];
				matches = libFeatures.stream().
					filter(f -> Objects.nonNull(f.getPrimaryIdentity().
							getCompoundIdentity().getInChiKey())).
					filter(f -> f.getPrimaryIdentity().getCompoundIdentity().
							getInChiKey().equals(inchiKey)).
					collect(Collectors.toList());

				if(!matches.isEmpty()) {

					for(MsFeature f : matches)
						rtMap.put((LibraryMsFeature) f, rt);

					if(matches.size() > 1)
						ambiguous.put(j, matches);

					matched = true;
				}
			}
			//	TODO match synonyms?

			if(!matched)
				unmatched.add(j);
		}
		//	Show import summary
		System.out.println("********************************\nMatched unique\n********************************");
		rtMap.forEach((k,v) -> System.out.println(k.getName() + " ~ " + MRC2ToolBoxConfiguration.getRtFormat().format(v)));

		System.out.println("********************************\nMatched ambiguous\n********************************");
		for (Entry<Integer, Collection<? extends MsFeature>> entry : ambiguous.entrySet()) {

			System.out.println(StringUtils.join(inputData[entry.getKey()], MRC2ToolBoxConfiguration.getTabDelimiter()));
			for(MsFeature f : entry.getValue())
				System.out.println(f.getName());
		}

		System.out.println("********************************\nUnmatched\n********************************");
		for(int i : unmatched)
			System.out.println(StringUtils.join(inputData[i], MRC2ToolBoxConfiguration.getTabDelimiter()));

		libraryRtImportDialog.savePreferences();
		libraryRtImportDialog.dispose();
	}

	private void lookupFeatureByAccessionDatabase() {
		// TODO Auto-generated method stub
		LibraryMsFeature selected = libraryFeatureTable.getTable().getSelectedFeature();
		if (selected == null)
			selected = libraryFeatureTable.getTable().getFeatureAtPopup();

		if (selected == null)
			return;

		if (selected.getPrimaryIdentity() == null)
			return;

		String accession = selected.getPrimaryIdentity().getCompoundIdentity().getPrimaryDatabaseId();

		if (accession == null)
			return;

		if (accession.isEmpty())
			return;

		CompoundDatabasePanel databasePanel = (CompoundDatabasePanel) MRC2ToolBoxCore.getMainWindow()
				.getPanel(PanelList.DATABASE);

		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.DATABASE);
		databasePanel.searchDatabaseById(accession);
	}

	private void lookupFeatureByMassInDatabase() {

		LibraryMsFeature selected = libraryFeatureTable.getTable().getSelectedFeature();
		if (selected == null)
			selected = libraryFeatureTable.getTable().getFeatureAtPopup();

		if (selected == null)
			return;

		if (selected.getPrimaryIdentity() == null)
			return;

		String molFormula = selected.getPrimaryIdentity().getCompoundIdentity().getFormula();

		if (molFormula == null)
			return;

		if (molFormula.isEmpty())
			return;

		IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molFormula,
				DefaultChemObjectBuilder.getInstance());
		double exactMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);

		CompoundDatabasePanel databasePanel = (CompoundDatabasePanel) MRC2ToolBoxCore.getMainWindow()
				.getPanel(PanelList.DATABASE);

		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.DATABASE);
		databasePanel.searchDatabaseByMass(exactMass);
	}

	private void lookupFeatureByFormulaInDatabase() {

		LibraryMsFeature selected = libraryFeatureTable.getTable().getSelectedFeature();
		if (selected == null)
			selected = libraryFeatureTable.getTable().getFeatureAtPopup();

		if (selected == null)
			return;

		if (selected.getPrimaryIdentity() == null)
			return;

		String molFormula = selected.getPrimaryIdentity().getCompoundIdentity().getFormula();

		if (molFormula == null)
			return;

		if (molFormula.isEmpty())
			return;

		CompoundDatabasePanel databasePanel = (CompoundDatabasePanel) MRC2ToolBoxCore.getMainWindow()
				.getPanel(PanelList.DATABASE);

		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.DATABASE);
		databasePanel.searchDatabaseByFormula(molFormula);
	}

	private void duplicateSelectedFeature() {

		LibraryMsFeature selected = libraryFeatureTable.getTable().getSelectedFeature();
		if (selected == null)
			selected = libraryFeatureTable.getTable().getFeatureAtPopup();

		if (selected != null && currentLibrary != null) {

			if (MessageDialog.showChoiceMsg("Duplicate " + selected.getName() + "?",
					this.getContentPane()) == JOptionPane.YES_OPTION) {

				CompoundIdentity id = selected.getPrimaryIdentity().getCompoundIdentity();

				long existingEntries = currentLibrary.getFeatures().stream()
						.filter(f -> f.getPrimaryIdentity().getCompoundIdentity().equals(id)).count();

				String newName = id.getName() + " #" + Integer.toString((int) (existingEntries + 1));
				LibraryMsFeature lf = new LibraryMsFeature(selected);
				lf.setName(newName);
				lf.setLibraryId(currentLibrary.getLibraryId());

				boolean added = false;
				try {
					MSRTLibraryUtils.loadLibraryFeature(lf, currentLibrary.getLibraryId());
					added = true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (added) {
					currentLibrary.addFeature(lf);
					libraryFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
					libraryFeatureTable.getTable().setTableModelFromCompoundLibrary(currentLibrary);
					libraryFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
					selectFeature(lf);
				} else {
					MessageDialog.showErrorMsg("Inserting new library entry failed!", this.getContentPane());
				}
			}
		}
	}

	private void undoLibraryFeatureEdit() {

		LibraryMsFeature feature = libraryFeatureEditorPanel.getActiveFeature();

		if (feature != null)
			libraryFeatureEditorPanel.loadFeature(feature, feature.getPolarity());
		else
			libraryFeatureEditorPanel.clearPanel();
	}

	public void editSelectedLibraryFeature() {

		LibraryMsFeature edited = libraryFeatureEditorPanel.getActiveFeature();
		String newName = libraryFeatureEditorPanel.getFeatureName();
		if (newName.isEmpty()) {
			MessageDialog.showErrorMsg("Name can not be empty!", this.getContentPane());
			return;
		}
		edited.setName(newName);
		edited.setLastModified(new Date());
		edited.setRetentionTime(libraryFeatureEditorPanel.getRetentionTime());
		edited.setRtRange(libraryFeatureEditorPanel.getRtRange());

		//	Re-create spectrum
		MassSpectrum newSpectrum = new MassSpectrum();
		Map<Adduct, Collection<MsPoint>> adductMap =
				MsUtils.createIsotopicPatternCollection(
						edited.getPrimaryIdentity().getCompoundIdentity(),
						libraryFeatureEditorPanel.getAdducts());

		adductMap.entrySet().stream().
			forEach(e -> newSpectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

		// Transfer MSMS data
		if (edited.getSpectrum() != null)
			newSpectrum.getTandemSpectra().addAll(edited.getSpectrum().getTandemSpectra());

		edited.setSpectrum(newSpectrum);

		// Update ID confidence level
		if (edited.getPrimaryIdentity() != null)
			edited.getPrimaryIdentity().setConfidenceLevel(libraryFeatureEditorPanel.getIdConfidence());

		//	Update data in database
		try {
			MSRTLibraryUtils.updateLibraryEntry(edited);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//	Update display
		int row = libraryFeatureTable.getTable().getFeatureRow(edited);
		if (row > -1) {
			((LibraryFeatureTableModel) libraryFeatureTable.getTable().getModel()).updateFeatureData(edited);
			if (row != libraryFeatureTable.getTable().getSelectedRow())
				libraryFeatureTable.getTable().setRowSelectionInterval(row, row);
			else {
				libraryFeatureEditorPanel.loadFeature(edited, edited.getPolarity());
				try {
					molStructurePanel.showStructure(edited.getPrimaryIdentity().getCompoundIdentity().getSmiles());
				} catch (Exception e1) {
					molStructurePanel.clearPanel();
				}
			}
		}
	}

	private void deleteSelectedFeature() {

		LibraryMsFeature selected = libraryFeatureTable.getTable().getSelectedFeature();
		if (selected == null)
			selected = libraryFeatureTable.getTable().getFeatureAtPopup();

		if (selected != null) {

			String yesNoQuestion = "Do you really want to delete " + selected.getName() + " from the library?";
			if (MessageDialog.showChoiceWithWarningMsg(yesNoQuestion,
					this.getContentPane()) == JOptionPane.YES_OPTION) {

				// Remove from opened library
				currentLibrary.removeFeature(selected);

				// Remove from database
				try {
					MSRTLibraryUtils.deleteLibraryFeature(selected);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Update table
				int tableRow = libraryFeatureTable.getTable().getFeatureRow(selected);
				if (tableRow > -1) {

					int delRow = libraryFeatureTable.getTable().convertRowIndexToModel(tableRow);
					((LibraryFeatureTableModel) libraryFeatureTable.getTable().getModel()).removeRow(delRow);
				}
				// Clear feature editor panel
				libraryFeatureEditorPanel.clearPanel();
				molStructurePanel.clearPanel();
			}
		}
	}

	private void exportLibrary(String exportCommand) {

		if (currentLibrary != null) {

			libraryExportDialog = 
					new LibraryExportDialog(exportCommand, currentLibrary);

			if (exportCommand.equals(MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName()))
				libraryExportDialog.setTargetSubset(null);

			if (exportCommand.equals(MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName()))
				libraryExportDialog.setTargetSubset(libraryFeatureTable.getFilteredTargets());

			libraryExportDialog.setLocationRelativeTo(this.getContentPane());
			libraryExportDialog.setVisible(true);
		}
	}

	private void convertLibraryForRecursion() {
		
		convertLibraryForRecursionDialog = new ConvertLibraryForRecursionDialog();
		convertLibraryForRecursionDialog.setLocationRelativeTo(this.getContentPane());
		convertLibraryForRecursionDialog.setVisible(true);		
	}

	@Override
	public synchronized void clearPanel() {

		((LibraryFeatureTableModel) libraryFeatureTable.getTable().getModel()).setRowCount(0);
		libraryFeatureEditorPanel.clearPanel();
		molStructurePanel.clearPanel();
		currentLibrary = null;
		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(currentLibrary, MRC2ToolBoxCore.getActiveMsLibraries());
	}

	private void closeActiveLibrary() {

		MRC2ToolBoxCore.getActiveMsLibraries().remove(currentLibrary);
		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(null, MRC2ToolBoxCore.getActiveMsLibraries());
		clearPanel();
		currentLibrary = null;
	}
	
	private void importIDTrackerLibrary() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(MsLibraryFormat.IDTRACKER.getName(), MsLibraryFormat.IDTRACKER.getFileExtension());
		fc.setTitle("Select library file to import");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane())))			
			importIdTrackerLibraryFromFile(fc.getSelectedFile());	
	}

	private void importIdTrackerLibraryFromFile(File selectedFile) {

		IDTraclerLibraryImportTask task = new IDTraclerLibraryImportTask(selectedFile);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	}

	private void importLibrary() {

		if (currentLibrary == null) {
			MessageDialog.showErrorMsg(
					"Create new library or open existing one first\n" 
					+ "in order to import data from file!");
			return;
		}
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(MsLibraryFormat.CEF.getName(), MsLibraryFormat.CEF.getFileExtension());
		fc.addFilter("Library Editor files", "xml", "XML");	
		//	fc.addFilter("TAB-separated text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select library file to import");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane())))			
			importLibraryFromFile(fc.getSelectedFile(), null);		
	}
	
	public void importLibraryFromFile(File inputFile, Collection<Adduct> adductList) {
		
		if(inputFile == null || !inputFile.exists())
			return;
		
		baseDirectory = inputFile.getParentFile();

		// Read lib editor file
		if (inputFile.getName().toLowerCase().endsWith("mslibrary.xml")) {

			LibEditorImportTask lit = 
					new LibEditorImportTask(inputFile, currentLibrary);
			lit.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(lit);
		}
		//	TODO read CEF file
		if (inputFile.getName().toLowerCase().endsWith(".cef")) {
			MessageDialog.showWarningMsg(
					"CEF library import under development.", this.getContentPane());
		}
		if (inputFile.getName().toLowerCase().endsWith(".txt")
				|| inputFile.getName().toLowerCase().endsWith(".tsv")) {
			PCDLTextLibraryImportTask task = 
					new PCDLTextLibraryImportTask(inputFile, currentLibrary, adductList, false);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}

	public void reloadLibraryData(CompoundLibrary selectedLibrary) {
		
		if(!MRC2ToolBoxCore.getActiveMsLibraries().contains(selectedLibrary)) {
			
			LoadDatabaseLibraryTask ldbltask = 
					new LoadDatabaseLibraryTask(selectedLibrary.getLibraryId());
			ldbltask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ldbltask);	
			return;
		}
		libraryFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		clearPanel();
		currentLibrary = selectedLibrary;
		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(
				currentLibrary, MRC2ToolBoxCore.getActiveMsLibraries());
		libraryFeatureTable.getTable().setTableModelFromCompoundLibrary(currentLibrary);
		libraryFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
		libraryFeatureEditorPanel.setAndLockFeaturePolarity(currentLibrary.getPolarity());
	}

	public void openLibraryFromDatabase(CompoundLibrary selectedLibrary) {
		
		if(currentLibrary != null && currentLibrary.equals(selectedLibrary))
			return;
		
		if(MRC2ToolBoxCore.getActiveMsLibraries().contains(selectedLibrary)) {
			reloadLibraryData(selectedLibrary);
			return;
		}
		LoadDatabaseLibraryTask ldbltask = 
				new LoadDatabaseLibraryTask(selectedLibrary.getLibraryId());
		ldbltask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(ldbltask);		
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			// Import library
			if (e.getSource().getClass().equals(LibEditorImportTask.class))
				reloadLibraryData(((LibEditorImportTask) e.getSource()).getLibrary());
			
			if (e.getSource().getClass().equals(LoadDatabaseLibraryTask.class)) 
				finalizeLoadDatabaseLibraryTask( (LoadDatabaseLibraryTask) e.getSource());
		
			if (e.getSource().getClass().equals(DecoyLibraryGenerationTask.class))
				finalizeDecoyLibraryGenerationTask((DecoyLibraryGenerationTask)e.getSource());
			
			if (e.getSource().getClass().equals(ReferenceMSMSLibraryExportTask.class))
				finalizeReferenceMSMSLibraryExportTask((ReferenceMSMSLibraryExportTask)e.getSource());
			
			if (e.getSource().getClass().equals(PCDLTextLibraryImportTask.class)) 
				finalizePCDLTextLibraryImportTask((PCDLTextLibraryImportTask)e.getSource());	
			
			if (e.getSource().getClass().equals(PCDLfromBaseLibraryTask.class)) 
				finalizePCDLfromBaseLibraryTask((PCDLfromBaseLibraryTask)e.getSource());
			
			if (e.getSource().getClass().equals(IDTraclerLibraryImportTask.class)) 
				finalizeIDTraclerLibraryImportTask((IDTraclerLibraryImportTask)e.getSource());
		}
	}

	private void finalizeIDTraclerLibraryImportTask(IDTraclerLibraryImportTask task) {
		// TODO Auto-generated method stub
		//	MRC2ToolBoxCore.getActiveMsLibraries().add(task.getLibrary());
		if(task.getLibrary() == null) {
			MessageDialog.showErrorMsg(
					"Failed to load IDTracker library export", 
					this.getContentPane());
			return;
		}
		reloadLibraryData(task.getLibrary());
		showPendingFeature();
	}

	private void finalizePCDLfromBaseLibraryTask(PCDLfromBaseLibraryTask task) {
		
		if(!task.getUnmatchedFeatures().isEmpty()) {
			
			String details = "The following entries were not found in PCDL base library:\n";
			List<String>missingEntries = task.getUnmatchedFeatures().stream().
					map(f -> f.getName() + "\t" + f.getFormula()).sorted().
					collect(Collectors.toList());
			details += StringUtils.join(missingEntries, "\n");
			InformationDialog errorDialog = new InformationDialog(
					"Error creating new PCDL library from base", 
					"Failed to create the new PCDL library from base PCDL list", 
					details, 
					InfoDialogType.ERROR);
			errorDialog.setLocationRelativeTo(this.getContentPane());
			errorDialog.setVisible(true);
		}
		else {
			LoadDatabaseLibraryTask loadLibTask = 
					new LoadDatabaseLibraryTask(task.getNewLlibrary().getLibraryId());
			loadLibTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(loadLibTask);
		}
	}

	private void finalizePCDLTextLibraryImportTask(PCDLTextLibraryImportTask task) {
		
		String libName = task.getInputLibraryFile().getName();
		if(task.getLibrary() != null)
			libName = task.getLibrary().getLibraryName();
			
		if(!task.getUnmatchedIdList().isEmpty()) {
			
			ArrayList<String>errors = new ArrayList<String>();
			if(task.getLibrary() != null) {
				errors.add("Library \"" + libName + "\" was created,");
				errors.add("but library data import failed.");
			}
			errors.add("The following compounds could not be matched to the database:");
			for(CompoundIdentity id : task.getUnmatchedIdList()) 	{			
				
				String cleanName = 
						id.getName().toUpperCase().replace("[ISTD]", "").replaceAll("_.+_.+$", "").trim();
				errors.add(id.getName() + "\t" + id.getFormula() + "\t" + cleanName);
			}
			errors.add("Please edit the import file or add the missing compounds to the database");
			errors.add("Then use \"Import PCDL library data from text file\" "
					+ "command to import the data into created library");
			InformationDialog id = new InformationDialog(
					"Unmatched compounds", 
					"Library data import failed", 
					StringUtils.join(errors, "\n"),
					InfoDialogType.ERROR);
			id.setLocationRelativeTo(this.getContentPane());
			id.setVisible(true);
		} else {
			if(task.getLibrary() != null) {
				
				MRC2ToolBoxCore.getActiveMsLibraries().add(task.getLibrary());
				reloadLibraryData(task.getLibrary());
			}
			MessageDialog.showInfoMsg(
					"All entries in the library \"" + libName 
					+ "\" were processed successfully.", this.getContentPane());
		}
	}

	private void finalizeDecoyLibraryGenerationTask(DecoyLibraryGenerationTask task) {
		
		File results = task.getOutputFile();
		if(results != null && results.exists()) {

			if(MessageDialog.showChoiceMsg(
					"Decoy MSP file created, do you want to open containing folder?",
				this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(results.getParentFile());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void finalizeLoadDatabaseLibraryTask(LoadDatabaseLibraryTask task) {
		
		MRC2ToolBoxCore.getActiveMsLibraries().add(task.getLibrary());
		reloadLibraryData(task.getLibrary());
		showPendingFeature();
	}
	
	private void showPendingFeature() {
		
		if (showFeaturePending && pendingFeatureId != null) {

			LibraryMsFeature featureToShow = 
					(LibraryMsFeature) currentLibrary.getFeatureById(pendingFeatureId);

			if (featureToShow != null) {
				SwingUtilities.invokeLater(() -> {
					this.selectFeature(featureToShow);

					if (libraryFeatureEditorPanel.getActiveFeature() == null) {
						loadFeatureData(featureToShow);
					} else {
						if (!libraryFeatureEditorPanel.getActiveFeature().equals(featureToShow))
							loadFeatureData(featureToShow);
					}
				});
			}
			showFeaturePending = false;
			pendingFeatureId = null;
		}
	}

	private void finalizeReferenceMSMSLibraryExportTask(ReferenceMSMSLibraryExportTask task) {

		File results = task.getOutputFile();
		if(results.exists()) {

			if(MessageDialog.showChoiceMsg(
					"Export file created, do you want to open containing folder?",
				this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(results.getParentFile());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void loadFeatureData(LibraryMsFeature featureToShow) {

		if(featureToShow.getPolarity() == null)
			featureToShow.setPolarity(currentLibrary.getPolarity());			
		
		libraryFeatureEditorPanel.loadFeature(featureToShow, featureToShow.getPolarity());

		if (featureToShow.getPrimaryIdentity() == null)
			return;
		
		try {
			molStructurePanel.showStructure(featureToShow.getPrimaryIdentity().getCompoundIdentity().getSmiles());
		} catch (Exception e1) {
			molStructurePanel.clearPanel();
		}

		// if(featureToShow.getPrimaryIdentity().getCompoundIdentity().getPrimaryDatabase().equals(CompoundDatabase.LIPIDMAPS_BULK))
		// libraryFeatureEditorPanel.setDuplicateButtonStatus(true);
		// else
		// libraryFeatureEditorPanel.setDuplicateButtonStatus(false);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			LibraryMsFeature selected = libraryFeatureTable.getTable().getSelectedFeature();
			if (selected != null)
				loadFeatureData(selected);
			else {
				libraryFeatureEditorPanel.clearPanel();
				molStructurePanel.clearPanel();
			}
		}
	}

	public boolean selectFeature(LibraryMsFeature feature) {

		int row = libraryFeatureTable.getTable().getFeatureRow(feature);

		if (row > -1) {
			libraryFeatureTable.getTable().setRowSelectionInterval(row, row);
			libraryFeatureTable.getTable().scrollToSelected();
			return true;
		} else
			return false;
	}

	@Override
	public void reloadDesign() {
		super.switchDataPipeline(currentExperiment, activeDataPipeline);
		menuBar.updateMenuFromExperiment(currentExperiment, activeDataPipeline);
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
	public void itemStateChanged(ItemEvent event) {

		if (event.getStateChange() == ItemEvent.SELECTED) {

			if (event.getItem() instanceof CompoundLibrary) {

				currentLibrary = (CompoundLibrary) event.getItem();
				libraryFeatureEditorPanel.clearPanel();
				molStructurePanel.clearPanel();
				reloadLibraryData(currentLibrary);
			}
		}
	}

	public void showLibraryFeature(String targetId) {

		LibraryMsFeature featureToShow = null;

		// Check if in current library
		if (currentLibrary != null) {

			featureToShow = (LibraryMsFeature) currentLibrary.getFeatureById(targetId);

			if (featureToShow != null) {
				selectFeature(featureToShow);
				return;
			}
		}
		// Check other loaded libraries
		for (CompoundLibrary library : MRC2ToolBoxCore.getActiveMsLibraries()) {

			featureToShow = (LibraryMsFeature) library.getFeatureById(targetId);

			if (featureToShow != null) {

				reloadLibraryData(library);
				selectFeature(featureToShow);
				return;
			}
		}
		// Check other libraries in the database
		CompoundLibrary dbLibrary = null;
		try {
			dbLibrary = MSRTLibraryUtils.getLibraryForTarget(targetId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dbLibrary != null) {

			showFeaturePending = true;
			pendingFeatureId = targetId;
			LoadDatabaseLibraryTask ldbltask = new LoadDatabaseLibraryTask(dbLibrary.getLibraryId());
			ldbltask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ldbltask);
		} else
			MessageDialog.showErrorMsg("Selected target not in database!");
	}
	
	public void updateLibraryMenuAndLabel() {
		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(
				currentLibrary, MRC2ToolBoxCore.getActiveMsLibraries());
	}
	
	public void finalizeLibraryDeletion(CompoundLibrary deleted) {
		
		if (currentLibrary != null) {

			if (currentLibrary.equals(deleted))
				clearPanel();
		} else
			clearPanel();

		updateLibraryMenuAndLabel() ;
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
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

	@Override
	public void updateGuiWithRecentData() {
		// TODO Auto-generated method stub
		
	}
}
