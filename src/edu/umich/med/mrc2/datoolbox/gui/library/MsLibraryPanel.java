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
import edu.umich.med.mrc2.datoolbox.database.idt.RemoteMsLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.CompoundDatabasePanel;
import edu.umich.med.mrc2.datoolbox.gui.io.msms.DecoyMSMSLibraryImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.msms.ReferenceMSMSLibraryExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableLibraryFeatureEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.manager.LibraryManager;
import edu.umich.med.mrc2.datoolbox.gui.library.upload.LibraryRtImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ReferenceMSMSLibraryExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibEditorImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LoadDatabaseLibraryTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms.DecoyLibraryGenerationTask;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MsLibraryPanel extends DockableMRC2ToolboxPanel implements ItemListener {

	private DockableLibraryFeatureTable libraryFeatureTable;
	private DockableMolStructurePanel molStructurePanel;
	private LibraryManager libraryManager;
	private LibraryExportDialog libraryExportDialog;
	private ConvertLibraryForRecursionDialog convertLibraryForRecursionDialog;
	private DockableLibraryFeatureEditorPanel libraryFeatureEditorPanel;
	private CompoundLibrary currentLibrary;
	private File baseDirectory;
	private boolean showFeaturePending = false;
	private String pendingFeatureId = null;
	private LibraryRtImportDialog libraryRtImportDialog;

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

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MsLibraryPanel.layout");

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

		if (command.equals(MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND.getName())) {
			
			if(libraryManager == null)
				libraryManager = new LibraryManager(this);
			
			libraryManager.setLocationRelativeTo(this.getContentPane());
			libraryManager.setVisible(true);
		}

		if (command.equals(MainActionCommands.DELETE_LIBRARY_COMMAND.getName()))
			deleteSelectedLibrary();

		if (command.equals(MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND.getName()))
			importLibrary();

		if (command.equals(MainActionCommands.EDIT_MS_LIBRARY_INFO_COMMAND.getName()))
			editLibraryInformation();

		if (command.equals(MainActionCommands.CREATE_NEW_LIBRARY_COMMAND.getName())) {
			try {
				createNewLibrary();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (command.equals(MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName())
				|| command.equals(MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName()))
			exportLibrary(command);

		if (command.equals(MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND.getName()))
			convertLibraryForRecursion();

		if (command.equals(MainActionCommands.CLOSE_LIBRARY_COMMAND.getName()))
			closeActiveLibrary();

		if (command.equals(MainActionCommands.OPEN_LIBRARY_COMMAND.getName()))
			openLibrary();

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

		if (command.equals(MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND.getName())) {

			if(currentLibrary == null)
				return;

			libraryRtImportDialog = new LibraryRtImportDialog(this);
			libraryRtImportDialog.setLocationRelativeTo(this.getContentPane());
			libraryRtImportDialog.setVisible(true);
		}
		if (command.equals(MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_COMMAND.getName()))
			importLibraryFeatureRtFromFile();

		if (command.equals(MainActionCommands.COPY_COMPOUND_FORMULA_COMMAND.getName()))
			copyCompoundFormula();

		if (command.equals(MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName()))
			copyCompoundAccession();

		if (command.equals(MainActionCommands.COPY_COMPOUND_NAME_COMMAND.getName()))
			copyCompoundName();
		
		if (command.equals(MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))	
			showRefMSMSLibraryExportDialog();
		
		if (command.equals(MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))	
			showDecoyMSMSLibraryImportDialog();
		
		if (command.equals(MainActionCommands.LOAD_SELECTED_LIBRARY_COMMAND.getName())) {
			//	event.getSource()
			
			if (event.getSource() instanceof JCheckBoxMenuItem) {
				
				Object libObject = 
						((JCheckBoxMenuItem)event.getSource()).getClientProperty(MsLibraryPanelMenuBar.LIBRARY_OBJECT);

				if(libObject != null && libObject instanceof CompoundLibrary) {
					
					currentLibrary = (CompoundLibrary) libObject;
					libraryFeatureEditorPanel.clearPanel();
					molStructurePanel.clearPanel();
					loadLibrary(currentLibrary);
				}
			}
		}
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

	private void copyCompoundFormula() {

		if (libraryFeatureTable.getTable().getSelectedFeature() == null)
			return;

		CompoundIdentity featureId = libraryFeatureTable.getTable().getSelectedFeature().
				getPrimaryIdentity().getCompoundIdentity();

		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(featureId.getFormula());
		clpbrd.setContents(stringSelection, null);
	}

	private void copyCompoundAccession() {

		if (libraryFeatureTable.getTable().getSelectedFeature() == null)
			return;

		CompoundIdentity featureId = libraryFeatureTable.getTable().getSelectedFeature().
				getPrimaryIdentity().getCompoundIdentity();

		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(featureId.getPrimaryDatabaseId());
		clpbrd.setContents(stringSelection, null);
	}

	private void copyCompoundName() {

		if (libraryFeatureTable.getTable().getSelectedFeature() == null)
			return;

		CompoundIdentity featureId = libraryFeatureTable.getTable().getSelectedFeature().
				getPrimaryIdentity().getCompoundIdentity();

		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(featureId.getName());
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
					RemoteMsLibraryUtils.loadLibraryFeature(lf, currentLibrary.getLibraryId());
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
			RemoteMsLibraryUtils.updateLibraryEntry(edited);
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
					RemoteMsLibraryUtils.deleteLibraryFeature(selected);
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

	private void createNewLibrary() throws Exception {

		String libraryName = libraryManager.getLibraryInfoDialog().getLibraryName();
		String libraryDescription = libraryManager.getLibraryInfoDialog().getLibraryDescription();
		String libId = RemoteMsLibraryUtils.createNewLibrary(libraryName, libraryDescription);
		libraryManager.refreshLibraryListing();
		libraryManager.hideLibInfoDialog();

		for (CompoundLibrary l : RemoteMsLibraryUtils.getAllLibraries()) {

			if (l.getLibraryId().equals(libId)) {

				currentLibrary = l;
				MRC2ToolBoxCore.getActiveMsLibraries().add(currentLibrary);
				break;
			}
		}
	}

	private void deleteSelectedLibrary() {

		CompoundLibrary selected = libraryManager.getSelectedLibrary();

		int approve = MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete the library \"" + selected.getLibraryName() + "\"?", libraryManager);

		if (approve == JOptionPane.YES_OPTION) {

			MRC2ToolBoxCore.getActiveMsLibraries().remove(selected);
			try {
				RemoteMsLibraryUtils.deleteLibrary(selected);
			} catch (Exception e) {
				e.printStackTrace();
			}
			libraryManager.refreshLibraryListing();
		}
		if (currentLibrary != null) {

			if (currentLibrary.equals(selected))
				clearPanel();
		} else
			clearPanel();

		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(currentLibrary, MRC2ToolBoxCore.getActiveMsLibraries());
	}

	private void editLibraryInformation() {

		CompoundLibrary selected = libraryManager.getLibraryInfoDialog().getEditedLibrary();
		String libraryName = libraryManager.getLibraryInfoDialog().getLibraryName();
		String libraryDescription = libraryManager.getLibraryInfoDialog().getLibraryDescription();
		selected.setLibraryName(libraryName);
		selected.setLibraryDescription(libraryDescription);

		try {
			RemoteMsLibraryUtils.updateLibraryInfo(selected);
		} catch (Exception e) {
			e.printStackTrace();
		}
		libraryManager.refreshLibraryListing();
		libraryManager.hideLibInfoDialog();

		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(currentLibrary, MRC2ToolBoxCore.getActiveMsLibraries());
	}

	private void importLibrary() {

		if (currentLibrary == null) {
			MessageDialog.showErrorMsg(
					"Create new library or open existing one first\n" 
					+ "in order to import data from file!");
			return;
		}
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.addFilter("Library Editor files", "xml");		
		fc.setTitle("Select library file to import");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputFile = fc.getSelectedFile();
			if (inputFile.exists()) {

				baseDirectory = inputFile.getParentFile();

				// Read lib editor file
				if (inputFile.getName().endsWith("mslibrary.xml")) {

					LibEditorImportTask lit = new LibEditorImportTask(inputFile, currentLibrary);
					lit.addTaskListener(this);
					MRC2ToolBoxCore.getTaskController().addTask(lit);
				}
				//	TODO read CEF file
			}
		}
	}

	public void loadLibrary(CompoundLibrary selectedLibrary) {

		libraryFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		clearPanel();
		currentLibrary = selectedLibrary;
		((MsLibraryPanelMenuBar)menuBar).updateLibraryList(currentLibrary, MRC2ToolBoxCore.getActiveMsLibraries());
		libraryFeatureTable.getTable().setTableModelFromCompoundLibrary(currentLibrary);
		libraryFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

	public void openLibrary() {

		if (libraryManager.getSelectedLibrary() != null) {

			for (CompoundLibrary lib : MRC2ToolBoxCore.getActiveMsLibraries()) {

				if (lib.getLibraryId().equals(libraryManager.getSelectedLibrary().getLibraryId())) {

					loadLibrary(lib);
					libraryManager.setVisible(false);
					return;
				}
			}
			LoadDatabaseLibraryTask ldbltask = new LoadDatabaseLibraryTask(
					libraryManager.getSelectedLibrary().getLibraryId());
			ldbltask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ldbltask);
			libraryManager.setVisible(false);
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			// Import library
			if (e.getSource().getClass().equals(LibEditorImportTask.class)) {
				LibEditorImportTask task = (LibEditorImportTask) e.getSource();
				loadLibrary(task.getLibrary());
			}
			if (e.getSource().getClass().equals(LoadDatabaseLibraryTask.class)) 
				finalizeLoadDatabaseLibraryTask( (LoadDatabaseLibraryTask) e.getSource());
		
			if (e.getSource().getClass().equals(DecoyLibraryGenerationTask.class))
				finalizeDecoyLibraryGenerationTask((DecoyLibraryGenerationTask)e.getSource());
			
			if (e.getSource().getClass().equals(ReferenceMSMSLibraryExportTask.class))
				finalizeReferenceMSMSLibraryExportTask((ReferenceMSMSLibraryExportTask)e.getSource());
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
		loadLibrary(task.getLibrary());

		if (showFeaturePending && pendingFeatureId != null) {

			LibraryMsFeature featureToShow = (LibraryMsFeature) currentLibrary.getFeatureById(pendingFeatureId);

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
				loadLibrary(currentLibrary);
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

				loadLibrary(library);
				selectFeature(featureToShow);
				return;
			}
		}
		// Check other libraries in the database
		CompoundLibrary dbLibrary = null;
		try {
			dbLibrary = RemoteMsLibraryUtils.getLibraryForTarget(targetId);
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
}
