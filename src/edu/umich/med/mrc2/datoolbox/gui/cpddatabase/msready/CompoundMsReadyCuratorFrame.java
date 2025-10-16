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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import ambit2.base.data.Property;
import ambit2.tautomers.TautomerConst.GAT;
import ambit2.tautomers.TautomerManager;
import ambit2.tautomers.processor.StructureStandardizer;
import ambit2.tautomers.zwitterion.ZwitterionManager;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.StandardizedStructure;
import edu.umich.med.mrc2.datoolbox.dbparse.StructureStandardizationUtils;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.cpd.CompoundCurationPopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.cpd.DockableCompoundCurationListingTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std.CompoundStandardizerSettingsDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std.TautomerGeneratorSettingsDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std.ZwitterIonSettingsDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.utils.MSReadyUtils;
import io.github.dan2097.jnainchi.InchiStatus;

public class CompoundMsReadyCuratorFrame extends JFrame
		implements ListSelectionListener, PersistentLayout, BackedByPreferences, ActionListener, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8823134394231969905L;

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.CompoundMsReadyCuratorFrame";
		
	private static final Icon curateCompoundIcon = GuiUtils.getIcon("curateMsReadyCompound", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "CompoundMsReadyCuratorFrame.layout");
	
	private CompoundMsReadyCuratorToolbar toolbar;
	private CControl control;
	private CGrid grid;
	private DockableMolStructurePanel originalMolStructurePanel;
	private DockableMolStructurePanel msReadyMolStructurePanel;
	private DockableCompoundCurationListingTable compoundCurationListingTable;
	
	private DockableCompoundStructuralDescriptorsPanel originalStructuralDescriptorsPanel;
	private DockableCompoundStructuralDescriptorsPanel msReadyStructuralDescriptorsPanel;
	
	private CompoundStandardizerSettingsDialog compoundStandardizerSettingsDialog;
	private TautomerGeneratorSettingsDialog tautomerGeneratorSettingsDialog;	
	private ZwitterIonSettingsDialog zwitterIonSettingsDialog;
		
	private static final IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
	private static final CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(bldr);
	private static final CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(bldr);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	private static final SmilesParser smilesParser = new SmilesParser(bldr);
	
	private InChIGeneratorFactory igfactory;
	private InChIGenerator inChIGenerator;
	private Aromaticity aromaticity;
	private StructureStandardizer structureStandardizer;
	private ZwitterionManager zwitterionManager;	
	private TautomerManager tautomerManager;
	private GAT tautomerGeneratorAlgorithm;
	
	private CompoundIdentity selectedIdentity;
	private Map<CompoundIdentity,CompoundIdentity>curatedCompounds;
	
	public CompoundMsReadyCuratorFrame() throws HeadlessException {

		super("Compound MS-ready curator");
		setIconImage(((ImageIcon) curateCompoundIcon).getImage());
		setSize(new Dimension(1200, 860));
		setPreferredSize(new Dimension(1200, 860));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new CompoundMsReadyCuratorToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		compoundCurationListingTable = 
				new DockableCompoundCurationListingTable();
		compoundCurationListingTable.getTable().
			getSelectionModel().addListSelectionListener(this);
		compoundCurationListingTable.getTable().
			addTablePopupMenu(new CompoundCurationPopupMenu(this));
		
		originalMolStructurePanel = new DockableMolStructurePanel(
				"CompoundMsReadyCuratorPrimaryMolStructurePanel");
		originalMolStructurePanel.setTitleText("Original compound structure");
		msReadyMolStructurePanel = new DockableMolStructurePanel(
				"CompoundMsReadyCuratorMSReadyMolStructurePanel");
		msReadyMolStructurePanel.setTitleText("MSReady compound structure");
		
		originalStructuralDescriptorsPanel = 
				new DockableCompoundStructuralDescriptorsPanel(
						"OriginalStructuralDescriptorsPanel", "Original compound data", false);
		originalStructuralDescriptorsPanel.lockEditing(true);
		msReadyStructuralDescriptorsPanel = 
				new DockableCompoundStructuralDescriptorsPanel(
						"MSReadyStructuralDescriptorsPanel", "MS-ready compound data", true);
		msReadyStructuralDescriptorsPanel.lockEditing(false);
		
		grid.add(0, 0, 1, 1,
				compoundCurationListingTable, 
				originalStructuralDescriptorsPanel,
				msReadyStructuralDescriptorsPanel,
				originalMolStructurePanel,
				msReadyMolStructurePanel);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		loadPreferences();
	
		aromaticity = new Aromaticity(
				ElectronDonation.cdk(),Cycles.cdkAromaticSet());		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		curatedCompounds = new HashMap<CompoundIdentity,CompoundIdentity>();
		compoundStandardizerSettingsDialog = new CompoundStandardizerSettingsDialog(this);
		structureStandardizer = 
				compoundStandardizerSettingsDialog.getConfiguredStructureStandardizer();
		compoundStandardizerSettingsDialog.dispose();
		
		zwitterIonSettingsDialog = new ZwitterIonSettingsDialog(this);
		zwitterionManager =  zwitterIonSettingsDialog.getConfiguredZwitterionManager();
		zwitterIonSettingsDialog.dispose();
		
		tautomerGeneratorSettingsDialog = new TautomerGeneratorSettingsDialog(this);
		tautomerManager = tautomerGeneratorSettingsDialog.getConfiguredTautomerManager();
		tautomerGeneratorAlgorithm = tautomerGeneratorSettingsDialog.getAlgorithm();
		tautomerGeneratorSettingsDialog.dispose();
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton standardizeButton = new JButton(MainActionCommands.STANDARDIZE_STRUCTURE.getName());
		standardizeButton.setActionCommand(MainActionCommands.STANDARDIZE_STRUCTURE.getName());
		standardizeButton.addActionListener(this);
		panel.add(standardizeButton);
		
		JButton tautomersButton = new JButton(MainActionCommands.GENERATE_TAUTOMERS.getName());
		tautomersButton.setActionCommand(MainActionCommands.GENERATE_TAUTOMERS.getName());
		tautomersButton.addActionListener(this);
		panel.add(tautomersButton);

		JButton validateButton = new JButton(MainActionCommands.GENERATE_ZWITTER_IONSS.getName());
		validateButton.setActionCommand(MainActionCommands.GENERATE_ZWITTER_IONSS.getName());
		validateButton.addActionListener(this);
		panel.add(validateButton);
		
//		JButton btnCancel = new JButton("Cancel");
//		panel.add(btnCancel);
//		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
//		ActionListener al = new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				dispose();
//			}
//		};
//		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
//		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
			
		if(command.equals(MainActionCommands.FETCH_COMPOUND_DATA_FOR_CURATION.getName()))
			fetchCompoundDataForCuration();
		
		if(command.equals(MainActionCommands.VALIDATE_MS_READY_STRUCTURE.getName()))
			validateMsReadyStructureAndShowErrors();
		
		if(command.equals(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName()))
			saveMsReadyStructure();
		
		if(command.equals(MainActionCommands.EDIT_COMPOUND_STANDARDIZER_SETTINGS_COMMAND.getName()))
			editCompoundStandardizerSettings();
		
		if(command.equals(MainActionCommands.SAVE_COMPOUND_STANDARDIZER_SETTINGS_COMMAND.getName()))
			saveCompoundStandardizerSettings();
		
		if(command.equals(MainActionCommands.EDIT_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName()))
			editTautomerGeneratorSettings();
		
		if(command.equals(MainActionCommands.SAVE_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName()))
			saveTautomerGeneratorSettings();
		
		if(command.equals(MainActionCommands.EDIT_ZWITTER_ION_GENERATOR_SETTINGS_COMMAND.getName()))
			editZwitterIonGeneratorSettings();
		
		if(command.equals(MainActionCommands.SAVE_ZWITTER_ION_GENERATOR_SETTINGS_COMMAND.getName()))
			saveZwitterIonGeneratorSettings();
			
		if(command.equals(MainActionCommands.STANDARDIZE_STRUCTURE.getName()))
			standardizeStructure();
		
		if(command.equals(MainActionCommands.GENERATE_TAUTOMERS.getName()))
			generateTautomers();
		
		if(command.equals(MainActionCommands.GENERATE_ZWITTER_IONSS.getName()))
			generateZwitterIons();
		
		if(command.equals(MainActionCommands.BATCH_GENERATE_TAUTOMERS.getName()))
			batchGenerateTautomers();
	}

	private void batchGenerateTautomers() {
		
		try {
			//	CompoundDatabaseScripts.fetchPubChemData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		String logDirPath = "E:\\DataAnalysis\\Databases\\_LATEST";
//		Map<String,String>dbMap = new LinkedHashMap<String,String>();
////		dbMap.put("DRUGBANK_COMPOUND_DATA", "DRUGBANK");
////		dbMap.put("LIPIDMAPS_COMPOUND_DATA", "LIPIDMAPS");
////		dbMap.put("T3DB_COMPOUND_DATA", "T3DB");	
//		dbMap.put("FOODB_COMPOUND_DATA", "FOODB");	
//		dbMap.put("HMDB_COMPOUND_DATA", "HMDB");
//		
//		for(Entry<String, String> dbe : dbMap.entrySet()) {
//			System.out.println("Started processing " + dbe.getValue() + " data");
//			try {				
//				CompoundDatabaseScripts.generateTautomersForCompoundDatabaseUsingIndigo(
//						dbe.getKey(), dbe.getValue(), true, logDirPath);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
	}

	private Collection<String> generateZwitterIons() {

		Collection<String>errors = new ArrayList<String>();
		String smiles = msReadyStructuralDescriptorsPanel.getSmiles();
		if(smiles.isEmpty()) {
			errors.add("Please enter SMILES string for MS-ready form");
			return errors;
		}
		IAtomContainer mol = msReadyMolStructurePanel.showStructure(smiles);	
		if(mol == null) {
			errors.add("SMILES string not valid.");
			return errors;
		}
		List<IAtomContainer> res = new ArrayList<IAtomContainer>();
		try {
			zwitterionManager.setStructure(mol);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			res = zwitterionManager.generateZwitterions();		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(res.isEmpty()) {
			MessageDialog.showWarningMsg("No zwitter ions detected", this.getContentPane());
			return errors;
		}
		if(res.size() > 0) {
			
			int count = 1;
			for(IAtomContainer taut : res) {
				
				String tautSmiles = "";
				try {
					tautSmiles = smilesGenerator.create(taut);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("#" + count + ": " + tautSmiles);
				count++;
			}
		}		
		return errors;	
	}

	private Collection<String> generateTautomers() {

		Collection<String>errors = new ArrayList<String>();
		String smiles = msReadyStructuralDescriptorsPanel.getSmiles();
		if(smiles.isEmpty()) {
			errors.add("Please enter SMILES string for MS-ready form");
			return errors;
		}
		IAtomContainer mol = msReadyMolStructurePanel.showStructure(smiles);	
		if(mol == null) {
			errors.add("SMILES string not valid.");
			return errors;
		}
		List<IAtomContainer> res = new ArrayList<IAtomContainer>();
		try {
			tautomerManager.setStructure(mol);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			switch (tautomerGeneratorAlgorithm) {
				case Comb_Pure:
					res = tautomerManager.generateTautomers();
					break;

				case Comb_Improved:
					res = tautomerManager.generateTautomers_ImprovedCombApproach();
					break;

				case Incremental:
					res = tautomerManager.generateTautomersIncrementaly();
					break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(res.isEmpty() || res.size() == 1) {
			MessageDialog.showWarningMsg("No tautomers detected", this.getContentPane());
			return errors;
		}
		if(res.size() > 1) {
			
			int count = 1;
			for(IAtomContainer taut : res) {
				
				String tautSmiles = "";
				try {
					tautSmiles = smilesGenerator.create(taut);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("#" + count + ": " + tautSmiles);
				count++;
			}
		}		
		return errors;
	}

	private Collection<String> standardizeStructure() {
		
		Collection<String>errors = new ArrayList<String>();
		String smiles = msReadyStructuralDescriptorsPanel.getSmiles();
		if(smiles.isEmpty()) {
			errors.add("Please enter SMILES string for MS-ready form");
			return errors;
		}
		IAtomContainer mol = msReadyMolStructurePanel.showStructure(smiles);	
		if(mol == null) {
			errors.add("SMILES string not valid.");
			return errors;
		}
		IAtomContainer stdMol = null;
		try {
			stdMol = structureStandardizer.process(mol);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.showErrorMsg(e.getMessage(), this.getContentPane());
			errors.add(e.getMessage());
			return errors;
		}
		if(stdMol != null) {
	
			smiles = (String)stdMol.getProperty(Property.getSMILESInstance());
			if(structureStandardizer.isGenerateInChI()) {
				msReadyStructuralDescriptorsPanel.setInchiKey(
						(String)stdMol.getProperty(Property.opentox_InChIKey));
			}
			else {
				try {
					inChIGenerator = igfactory.getInChIGenerator(stdMol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println("InChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println("InChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					msReadyStructuralDescriptorsPanel.setInchiKey(inChIGenerator.getInchiKey());
				} 
				catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			IMolecularFormula molFormula = 
					MolecularFormulaManipulator.getMolecularFormula(stdMol);			
			String mfFromFStringFromSmiles = 
					MolecularFormulaManipulator.getString(molFormula);	
			double smilesMass = MolecularFormulaManipulator.getMass(
							molFormula, MolecularFormulaManipulator.MonoIsotopic);	
			
			msReadyStructuralDescriptorsPanel.setSmiles(smiles);
			msReadyStructuralDescriptorsPanel.setFormula(mfFromFStringFromSmiles);
			msReadyStructuralDescriptorsPanel.setCharge(molFormula.getCharge());
			msReadyStructuralDescriptorsPanel.setMass(smilesMass);			
			msReadyMolStructurePanel.showStructure(stdMol);
		}
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\\n"), this.getContentPane());
		}
		return errors;
	}

	private void editCompoundStandardizerSettings() {

		compoundStandardizerSettingsDialog = new CompoundStandardizerSettingsDialog(this);
		compoundStandardizerSettingsDialog.setLocationRelativeTo(this.getContentPane());
		compoundStandardizerSettingsDialog.setVisible(true);
	}

	private void saveCompoundStandardizerSettings() {
		
		structureStandardizer = 
				compoundStandardizerSettingsDialog.getConfiguredStructureStandardizer();
		compoundStandardizerSettingsDialog.savePreferences();
		compoundStandardizerSettingsDialog.dispose();
	}

	private void editTautomerGeneratorSettings() {

		tautomerGeneratorSettingsDialog = new TautomerGeneratorSettingsDialog(this);
		tautomerGeneratorSettingsDialog.setLocationRelativeTo(this.getContentPane());
		tautomerGeneratorSettingsDialog.setVisible(true);
	}

	private void saveTautomerGeneratorSettings() {
		// TODO Auto-generated method stub
		tautomerManager = tautomerGeneratorSettingsDialog.getConfiguredTautomerManager();
		tautomerGeneratorAlgorithm = tautomerGeneratorSettingsDialog.getAlgorithm();
		tautomerGeneratorSettingsDialog.savePreferences();
		tautomerGeneratorSettingsDialog.dispose();
	}
		
	private void editZwitterIonGeneratorSettings() {
		// TODO Auto-generated method stub
		
		zwitterIonSettingsDialog = new ZwitterIonSettingsDialog(this);
		zwitterIonSettingsDialog.setLocationRelativeTo(this.getContentPane());
		zwitterIonSettingsDialog.setVisible(true);
	}

	private void saveZwitterIonGeneratorSettings() {
		// TODO Auto-generated method stub
		
		zwitterionManager =  zwitterIonSettingsDialog.getConfiguredZwitterionManager();
		zwitterIonSettingsDialog.savePreferences();
		zwitterIonSettingsDialog.dispose();
	}

	private void clearPanel() {
		
		compoundCurationListingTable.getTable().getSelectionModel().removeListSelectionListener(this);
		compoundCurationListingTable.clearTable();
		originalStructuralDescriptorsPanel.clearPanel();
		msReadyStructuralDescriptorsPanel.clearPanel();
		originalMolStructurePanel.clearPanel();
		msReadyMolStructurePanel.clearPanel();
		compoundCurationListingTable.getTable().getSelectionModel().addListSelectionListener(this);
		selectedIdentity = null;
		curatedCompounds.clear();
	}
	
	private void fetchCompoundDataForCuration() {
		
		CompoundDatabaseEnum db = toolbar.getSelectedDatabase();
		if(db == null)
			return;
		
		clearPanel();
		CompoundDataRetrievalTask task = new CompoundDataRetrievalTask(db);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Fetching compound data for curation from " + 
		db.getName(), this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class CompoundDataRetrievalTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private CompoundDatabaseEnum db;
		private Collection<CompoundIdentity>compoundCollection;

		public CompoundDataRetrievalTask(CompoundDatabaseEnum db) {
			this.db = db;
//			cpdIds = null;
		}

		@Override
		public Void doInBackground() {

			if(db.equals(CompoundDatabaseEnum.HMDB)) {
				
				try {					
					compoundCollection = fetchHMDBDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.DRUGBANK)) {
				
				try {					
					compoundCollection = fetchDrugBankDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.FOODB)) {
				
				try {					
					compoundCollection = fetchFooDbDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.T3DB)) {
				
				try {					
					compoundCollection = fetchT3DbDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.LIPIDMAPS)) {
				
				try {					
					compoundCollection = fetchLipidMapsDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.COCONUT)) {
				
				try {					
					compoundCollection = fetchCoconutDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			if(db.equals(CompoundDatabaseEnum.NIST_MS)) {
				
				try {					
					compoundCollection = fetchNISTMSMStDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.MSDIAL_METABOLITES)) {
				
				try {					
					compoundCollection = fetchMSDIALMetabolitesDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(db.equals(CompoundDatabaseEnum.MSDIAL_LIPIDS)) {
				
				try {					
					compoundCollection = fetchMSDIALLipidBlastDataForCuration();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			if(compoundCollection != null && !compoundCollection.isEmpty()) {
				
				curatedCompounds.clear();
				Collection<CompoundIdentity>cleanedCompounds = new ArrayList<CompoundIdentity>();
				for(CompoundIdentity cid : compoundCollection) {
							
					fixBadSmiles(cid);
					curatedCompounds.put(cid, null);
					CompoundIdentity pl = MSReadyUtils.neutralizePhosphoCholine(cid);
					if(pl != null) {
						
						if(db.equals(CompoundDatabaseEnum.COCONUT) && pl.getCharge() == 0) {
							try {
								updateCoconutCompoundData(pl);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							cleanedCompounds.add(cid);
						}
						else {
							curatedCompounds.put(cid, pl);
						}
					}
					CompoundIdentity pyr = MSReadyUtils.neutralizeSmiles(cid);
					if(pyr != null) {
						
						if(db.equals(CompoundDatabaseEnum.COCONUT) && pyr.getCharge() == 0) {
							try {
								updateCoconutCompoundData(pyr);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							cleanedCompounds.add(cid);
						}
						else {
							curatedCompounds.put(cid, pyr);
						}
					}
				}
				if(!cleanedCompounds.isEmpty())
					compoundCollection.removeAll(cleanedCompounds);
				
				compoundCurationListingTable.setTableModelFromCompoundCollection(compoundCollection);
			}			
			return null;
		}
	}
	
	private void fixBadSmiles(CompoundIdentity cid) {
		
		IAtomContainer atomContainer = null;
		try {
			atomContainer = smilesParser.parseSmiles(cid.getSmiles());
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(atomContainer == null) {	//	Fix invalid SMILES
			StandardizedStructure stdMol = 
					StructureStandardizationUtils.standardizeStructure(cid.getSmiles(), cid.getInChi());
			if(stdMol != null) 
				cid.setSmiles(stdMol.getStdSmiles());						
		}
	}
	
	private Collection<CompoundIdentity> fetchHMDBDataForCuration() throws Exception{
		
		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, NAME, FORMULA_FROM_SMILES, EXACT_MASS, SMILES, INCHI, INCHI_KEY, CHARGE " +
			"FROM COMPOUNDDB.HMDB_COMPOUND_DATA D WHERE MS_READY_INCHI_KEY IS NULL";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.HMDB, 
					rs.getString("ACCESSION"),
					rs.getString("NAME"), 					
					rs.getString("NAME"), 
					rs.getString("FORMULA_FROM_SMILES"), 
					rs.getDouble("EXACT_MASS"), 
					rs.getString("SMILES"));
			identity.setInChiKey(rs.getString("INCHI_KEY"));
			identity.setInChi(rs.getString("INCHI"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	private Collection<CompoundIdentity> fetchDrugBankDataForCuration() throws Exception{
		
		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, COMMON_NAME, FORMULA_FROM_SMILES, EXACT_MASS, SMILES, INCHI, INCHI_KEY, CHARGE " +
			"FROM COMPOUNDDB.DRUGBANK_COMPOUND_DATA D WHERE MS_READY_INCHI_KEY IS NULL AND SMILES IS NOT NULL";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.DRUGBANK, 
					rs.getString("ACCESSION"),
					rs.getString("COMMON_NAME"), 					
					rs.getString("COMMON_NAME"), 
					rs.getString("FORMULA_FROM_SMILES"), 
					rs.getDouble("EXACT_MASS"), 
					rs.getString("SMILES"));
			identity.setInChiKey(rs.getString("INCHI_KEY"));
			identity.setInChi(rs.getString("INCHI"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	private Collection<CompoundIdentity> fetchFooDbDataForCuration() throws Exception{
		
		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT PUBLIC_ID, NAME, FORMULA_FROM_SMILES, MOLDB_MONO_MASS, MOLDB_SMILES, MOLDB_INCHI, MOLDB_INCHIKEY, CHARGE " +
			"FROM COMPOUNDDB.FOODB_COMPOUND_DATA D WHERE MS_READY_INCHI_KEY IS NULL AND MOLDB_SMILES IS NOT NULL";
	
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.FOODB, 
					rs.getString("PUBLIC_ID"),
					rs.getString("NAME"), 					
					rs.getString("NAME"), 
					rs.getString("FORMULA_FROM_SMILES"), 
					rs.getDouble("MOLDB_MONO_MASS"), 
					rs.getString("MOLDB_SMILES"));
			identity.setInChiKey(rs.getString("MOLDB_INCHIKEY"));
			identity.setInChi(rs.getString("MOLDB_INCHI"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	private Collection<CompoundIdentity> fetchMSDIALLipidBlastDataForCuration() throws Exception{
		
		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
//		String query =
//			"SELECT PUBLIC_ID, NAME, FORMULA_FROM_SMILES, "
//			+ "MOLDB_MONO_MASS, MOLDB_SMILES, MOLDB_INCHI, "
//			+ "MOLDB_INCHIKEY, CHARGE " +
//			"FROM COMPOUNDDB.FOODB_COMPOUND_DATA D "
//			+ "WHERE MS_READY_INCHI_KEY IS NULL "
//			+ "AND MOLDB_SMILES IS NOT NULL";
//	
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()){
//
//			CompoundIdentity identity = new CompoundIdentity(
//					CompoundDatabaseEnum.FOODB, 
//					rs.getString("PUBLIC_ID"),
//					rs.getString("NAME"), 					
//					rs.getString("NAME"), 
//					rs.getString("FORMULA_FROM_SMILES"), 
//					rs.getDouble("MOLDB_MONO_MASS"), 
//					rs.getString("MOLDB_SMILES"));
//			identity.setInChiKey(rs.getString("MOLDB_INCHIKEY"));
//			identity.setInChi(rs.getString("MOLDB_INCHI"));
//			identity.setCharge(rs.getInt("CHARGE"));
//			idList.add(identity);
//		}
//		rs.close();
//		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}

	private Collection<CompoundIdentity> fetchMSDIALMetabolitesDataForCuration() throws Exception{

		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT MSDM_CPD_ID, NAME, FORMULA_FROM_SMILES, "
			+ "EXACT_MASS, SMILES, INCHI_KEY, CHARGE "
			+ "FROM COMPOUNDDB.MSDIAL_METAB_UNIQUE_COMPOUNDS D "
			+ "WHERE MS_READY_INCHI_KEY IS NULL "
			+ "AND SMILES IS NOT NULL";
	
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.MSDIAL_METABOLITES, 
					rs.getString("MSDM_CPD_ID"),
					rs.getString("NAME"), 					
					rs.getString("NAME"), 
					rs.getString("FORMULA_FROM_SMILES"), 
					rs.getDouble("EXACT_MASS"), 
					rs.getString("SMILES"));
			identity.setInChiKey(rs.getString("INCHI_KEY"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	private Collection<CompoundIdentity> fetchT3DbDataForCuration() throws Exception{

		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, COMMON_NAME, FORMULA_FROM_SMILES, MASS_FROM_SMILES, SMILES, INCHI, INCHIKEY, CHARGE " +
			"FROM COMPOUNDDB.T3DB_COMPOUND_DATA D WHERE MS_READY_INCHI_KEY IS NULL AND SMILES IS NOT NULL";
	
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.T3DB, 
					rs.getString("ACCESSION"),
					rs.getString("COMMON_NAME"), 					
					rs.getString("COMMON_NAME"), 
					rs.getString("FORMULA_FROM_SMILES"), 
					rs.getDouble("MASS_FROM_SMILES"), 
					rs.getString("SMILES"));
			identity.setInChiKey(rs.getString("INCHIKEY"));
			identity.setInChi(rs.getString("INCHI"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}

	private Collection<CompoundIdentity> fetchLipidMapsDataForCuration() throws Exception{

		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT LMID, COMMON_NAME, SYSTEMATIC_NAME, MOLECULAR_FORMULA, EXACT_MASS, SMILES, INCHI, INCHI_KEY, CHARGE " +
			"FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D WHERE MS_READY_INCHI_KEY IS NULL AND SMILES IS NOT NULL";
	
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.LIPIDMAPS, 
					rs.getString("LMID"),
					rs.getString("COMMON_NAME"), 					
					rs.getString("SYSTEMATIC_NAME"), 
					rs.getString("MOLECULAR_FORMULA"), 
					rs.getDouble("EXACT_MASS"), 
					rs.getString("SMILES"));
			identity.setInChiKey(rs.getString("INCHI_KEY"));
			identity.setInChi(rs.getString("INCHI"));
			identity.setCharge(rs.getInt("CHARGE"));
			if(rs.getString("COMMON_NAME") == null)
				identity.setCommonName(rs.getString("SYSTEMATIC_NAME"));
			
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}

	private Collection<CompoundIdentity> fetchCoconutDataForCuration() throws Exception{

		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, NAME, CDK_FORMULA, CDK_MASS, "
			+ "CDK_SMILES, CDK_INCHI_KEY, INCHI, CHARGE " 
			+ "FROM COMPOUNDDB.COCONUT_COMPOUND_DATA D "
			+ "WHERE MS_READY_INCHI_KEY IS NULL "
			+ "AND CDK_SMILES IS NOT NULL "
			+ "AND ANNOT_LEVEL > 2";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.COCONUT, 
					rs.getString("ACCESSION"),
					rs.getString("NAME"), 					
					rs.getString("NAME"), 
					rs.getString("CDK_FORMULA"), 
					rs.getDouble("CDK_MASS"), 
					rs.getString("CDK_SMILES"));
			identity.setInChiKey(rs.getString("CDK_INCHI_KEY"));
			identity.setInChi(rs.getString("INCHI"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;		
	}
	
	private Collection<CompoundIdentity> fetchNISTMSMStDataForCuration() throws Exception{

		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT NAME, FORMULA_FROM_SMILES, MASS_FROM_SMILES, "
			+ "CANONICAL_SMILES, INCHI_KEY, CHARGE " +
			"FROM COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA D "
			+ "WHERE MS_READY_INCHI_KEY IS NULL "
			+ "AND CANONICAL_SMILES IS NOT NULL";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.NIST_MS, 
					rs.getString("NAME"),
					rs.getString("NAME"), 					
					rs.getString("NAME"), 
					rs.getString("FORMULA_FROM_SMILES"), 
					rs.getDouble("MASS_FROM_SMILES"), 
					rs.getString("CANONICAL_SMILES"));
			identity.setInChiKey(rs.getString("INCHI_KEY"));
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	private void validateMsReadyStructureAndShowErrors() {
		
		Collection<String>errors = validateMsReadyStructure();
		if(!errors.isEmpty()) {
			
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
		}
	}

	private Collection<String> validateMsReadyStructure() {
		
		Collection<String>errors = new ArrayList<String>();
		String smiles = msReadyStructuralDescriptorsPanel.getSmiles();
		if(smiles.isEmpty()) {
			errors.add("Please enter SMILES string for MS-ready form");
			return errors;
		}
		IAtomContainer mol = msReadyMolStructurePanel.showStructure(smiles);	
		if(mol == null) {
			errors.add("SMILES string not valid.");
			return errors;
		}
		else {
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
			} catch (CDKException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				CDKHydrogenAdder.getInstance(mol.getBuilder()).addImplicitHydrogens(mol);
			} catch (CDKException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				smiles = smilesGenerator.create(mol);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						   
			IMolecularFormula molFormula = 
					MolecularFormulaManipulator.getMolecularFormula(mol);			
			String mfFromFStringFromSmiles = 
					MolecularFormulaManipulator.getString(molFormula);	
			double smilesMass = MolecularFormulaManipulator.getMass(
							molFormula, MolecularFormulaManipulator.MonoIsotopic);	
			
			msReadyStructuralDescriptorsPanel.setSmiles(smiles);
			msReadyStructuralDescriptorsPanel.setFormula(mfFromFStringFromSmiles);
			msReadyStructuralDescriptorsPanel.setCharge(molFormula.getCharge());
			msReadyStructuralDescriptorsPanel.setMass(smilesMass);			
			try {
				inChIGenerator = igfactory.getInChIGenerator(mol);
				InchiStatus inchiStatus = inChIGenerator.getStatus();
				if (inchiStatus.equals(InchiStatus.WARNING)) {
					System.out.println("InChI warning: " + inChIGenerator.getMessage());
				} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
					System.out.println("InChI failed: [" + inChIGenerator.getMessage() + "]");
				}
				String inchiKey = inChIGenerator.getInchiKey();
				msReadyStructuralDescriptorsPanel.setInchiKey(inchiKey);
			} 
			catch (CDKException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}		
		return errors;
	}

	@Override
	public void dispose() {
		
		savePreferences();
		saveLayout(layoutConfigFile);
		super.dispose();
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(!e.getValueIsAdjusting()) {
			
			MsFeatureIdentity mfid = 
					compoundCurationListingTable.getSelectedIdentity();
			if(mfid == null)
				return;
			
			selectedIdentity = mfid.getCompoundIdentity();			
			originalMolStructurePanel.showStructure(selectedIdentity.getSmiles());
			originalStructuralDescriptorsPanel.loadCompoundIdentity(selectedIdentity);
			
			msReadyMolStructurePanel.clearPanel();
			msReadyStructuralDescriptorsPanel.clearPanel();	
			
			CompoundIdentity curatedId = curatedCompounds.get(selectedIdentity);
			if(curatedId != null) {
				msReadyStructuralDescriptorsPanel.loadCompoundIdentity(curatedId);
				msReadyMolStructurePanel.showStructure(curatedId.getSmiles());
			}
			else
				msReadyStructuralDescriptorsPanel.setSmiles(selectedIdentity.getSmiles());
		}
	}
	
	private void saveMsReadyStructure() {

		Collection<String> errors = validateMsReadyStructure();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), this.getContentPane());
			return;
		}
		if(msReadyStructuralDescriptorsPanel.getCharge() != 0) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"MS-ready form still contains charge, save anyway?", this.getContentPane());
			if(res != JOptionPane.YES_OPTION)
				return;
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.HMDB)) {
			try {
				updateHMDBCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.DRUGBANK)) {
			try {
				updateDrugBankCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.FOODB)) {
			try {
				updateFooDbCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.T3DB)) {
			try {
				updateT3DbCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.LIPIDMAPS)) {
			try {
				updateLipidMapsCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.COCONUT)) {
			try {
				updateCoconutCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.NIST_MS)) {
			try {
				updateNISTMSMSCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.MSDIAL_METABOLITES)) {
			try {
				updateMSDIALMetabolitesCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(toolbar.getSelectedDatabase().equals(CompoundDatabaseEnum.MSDIAL_LIPIDS)) {
			try {
				updateMSDIALLipidBlastCompoundData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		CompoundIdentity curatedId =  new CompoundIdentity(
				selectedIdentity.getPrimaryDatabase(), 
				selectedIdentity.getPrimaryDatabaseId(),
				selectedIdentity.getName(), 					
				selectedIdentity.getSysName(), 
				msReadyStructuralDescriptorsPanel.getFormula(), 
				msReadyStructuralDescriptorsPanel.getMass(), 
				msReadyStructuralDescriptorsPanel.getSmiles());
		curatedId.setInChiKey(msReadyStructuralDescriptorsPanel.getInchiKey());
		curatedId.setCharge(msReadyStructuralDescriptorsPanel.getCharge());
		curatedCompounds.put(selectedIdentity, curatedId);
	}

	private void updateHMDBCompoundData() throws Exception{
		
		String query = 
				"UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE ACCESSION = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
		
	private void updateNISTMSMSCompoundData() throws Exception{
		
		String query = 
				"UPDATE COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE NAME = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getName());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	private void updateMSDIALLipidBlastCompoundData() throws Exception{
		// TODO Auto-generated method stub

	}

	private void updateMSDIALMetabolitesCompoundData() throws Exception{
		
		String query = 
				"UPDATE COMPOUNDDB.MSDIAL_METAB_UNIQUE_COMPOUNDS " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE MSDM_CPD_ID = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getDbId(CompoundDatabaseEnum.MSDIAL_METABOLITES));			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	private void updateDrugBankCompoundData() throws Exception{
		
		String query = 
				"UPDATE COMPOUNDDB.DRUGBANK_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE ACCESSION = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	private void updateFooDbCompoundData() throws Exception{
		
		String query = 
				"UPDATE COMPOUNDDB.FOODB_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE PUBLIC_ID = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	private void updateT3DbCompoundData() throws Exception{

		String query = 
				"UPDATE COMPOUNDDB.T3DB_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE ACCESSION = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}

	private void updateLipidMapsCompoundData() throws Exception{

		String query = 
				"UPDATE COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE LMID = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}

	private void updateCoconutCompoundData() throws Exception{

		String query = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE ACCESSION = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msReadyStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, msReadyStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, msReadyStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, msReadyStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, msReadyStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	private void updateCoconutCompoundData(CompoundIdentity cid) throws Exception{

		String query = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE ACCESSION = ? ";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, cid.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, msReadyStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, cid.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, cid.getInChiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, cid.getInChiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, cid.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, cid.getPrimaryDatabaseId());			//	ACCESSION
		
		ps.executeUpdate();
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
	
	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		// TODO
	}

	@Override
	public void loadPreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		//	TODO
	}

//	@Override
//	public void toFront() {
//	  super.setVisible(true);
//	  super.toFront();
//	  super.requestFocus();
//	}
}
