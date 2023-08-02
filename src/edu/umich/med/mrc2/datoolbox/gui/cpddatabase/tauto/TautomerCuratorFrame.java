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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.tauto;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundCurationUtils;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.DockableCuratedDatabaseCompoundTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.DockableCompoundStructuralDescriptorsPanel;
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

public class TautomerCuratorFrame extends JFrame
		implements ListSelectionListener, PersistentLayout, BackedByPreferences, ActionListener, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8823134394231969905L;

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.TautomerCuratorFrame";
		
	private static final Icon dialogIcon = GuiUtils.getIcon("tautomerSettings", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "TautomerCuratorFrame.layout");
	
	private TautomerCuratorToolbar toolbar;
	private CControl control;
	private CGrid grid;
	
	private DockableCuratedDatabaseCompoundTable primaryCompoundListingTable;
	private DockableCompoundListingTable tautomerListingTable;
	
	private DockableCompoundStructuralDescriptorsPanel primaryStructuralDescriptorsPanel;
	private DockableCompoundStructuralDescriptorsPanel tautomerStructuralDescriptorsPanel;
	private DockableMolStructurePanel primaryMolStructurePanel;
	private DockableMolStructurePanel tautomerMolStructurePanel;
		
	private static final IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
	private static final CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(bldr);
	private static final CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(bldr);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	
	private InChIGeneratorFactory igfactory;
	private InChIGenerator inChIGenerator;
	
	private CompoundIdentity selectedIdentity;
	private Map<CompoundIdentity,Collection<CompoundIdentity>>tautomerMap;
	private Map<CompoundIdentity,Boolean>curationMap;
	
	public TautomerCuratorFrame() throws HeadlessException {

		super("Tautomers curator");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setSize(new Dimension(1400, 1000));
		setPreferredSize(new Dimension(1400, 100));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new TautomerCuratorToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		primaryCompoundListingTable = 
				new DockableCuratedDatabaseCompoundTable(
						"PrimaryCompoundsListingTable", "Primary compounds");
		primaryCompoundListingTable.getTable().
			getSelectionModel().addListSelectionListener(this);		
		primaryCompoundListingTable.getTable().addTablePopupMenu(
				new PrimaryCompoundTablePopupMenu(this));

		tautomerListingTable = 
				new DockableCompoundListingTable("TautomersListingTable", "Tautomers");
		tautomerListingTable.getTable().
			getSelectionModel().addListSelectionListener(this);
		tautomerListingTable.getTable().addTablePopupMenu(
				new TautomerTablePopupMenu(this));
				
		primaryStructuralDescriptorsPanel = 
				new DockableCompoundStructuralDescriptorsPanel(
						"OriginalStructuralDescriptorsPanel", "Primary compound data", false);
		primaryStructuralDescriptorsPanel.lockEditing(true);
		tautomerStructuralDescriptorsPanel = 
				new DockableCompoundStructuralDescriptorsPanel(
						"MSReadyStructuralDescriptorsPanel", "Tautomer data", true);
		tautomerStructuralDescriptorsPanel.lockEditing(true);
		
		primaryMolStructurePanel = new DockableMolStructurePanel(
				"TautomerCuratorPrimaryMolStructurePanel");
		primaryMolStructurePanel.setTitleText("Primary compound structure");
		tautomerMolStructurePanel = new DockableMolStructurePanel(
				"TautomerCuratorTautomerMolStructurePanel");
		tautomerMolStructurePanel.setTitleText("Tautomer structure");
		
		grid.add(0, 0, 1, 1,
				primaryCompoundListingTable, 
				tautomerListingTable,
				primaryMolStructurePanel,
				tautomerMolStructurePanel,
				primaryStructuralDescriptorsPanel,
				tautomerStructuralDescriptorsPanel);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		loadPreferences();
		
		tautomerMap = new TreeMap<CompoundIdentity,Collection<CompoundIdentity>>();
						
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
//		JButton standardizeButton = new JButton(MainActionCommands.STANDARDIZE_STRUCTURE.getName());
//		standardizeButton.setActionCommand(MainActionCommands.STANDARDIZE_STRUCTURE.getName());
//		standardizeButton.addActionListener(this);
//		panel.add(standardizeButton);
//		
//		JButton tautomersButton = new JButton(MainActionCommands.GENERATE_TAUTOMERS.getName());
//		tautomersButton.setActionCommand(MainActionCommands.GENERATE_TAUTOMERS.getName());
//		tautomersButton.addActionListener(this);
//		panel.add(tautomersButton);
//
//		JButton validateButton = new JButton(MainActionCommands.GENERATE_ZWITTER_IONSS.getName());
//		validateButton.setActionCommand(MainActionCommands.GENERATE_ZWITTER_IONSS.getName());
//		validateButton.addActionListener(this);
//		panel.add(validateButton);
//
//		JButton btnSave = new JButton(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
//		btnSave.setActionCommand(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
//		btnSave.addActionListener(this);
//		panel.add(btnSave);
//		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
//		rootPane.setDefaultButton(btnSave);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
			
		if(command.equals(MainActionCommands.FETCH_COMPOUND_DATA_FOR_CURATION.getName()))
			fetchCompoundDataForCuration();
		
		if(command.equals(MainActionCommands.MARK_COMPOUND_GROUP_CURATED_COMMAND.getName()))
			markCompoundGroupCurated(true);
		
		if(command.equals(MainActionCommands.MARK_COMPOUND_GROUP_NOT_CURATED_COMMAND.getName()))
			markCompoundGroupCurated(false);
		
		if(command.equals(MainActionCommands.COPY_COMPOUND_ACCESSION_COMMAND.getName()))
			copyPrimaryCompoundAccession();
		
		if(command.equals(MainActionCommands.ADD_TAUTOMER_AS_NEW_COMPOUND_COMMAND.getName()))
			addSelectedTautomerAsNewCompound();
		
		if(command.equals(MainActionCommands.REPLACE_PRIMARY_COMPOUND_WITH_TAUTOMER_COMMAND.getName()))
			replacePrimaryCompoundWithSelectedTautomer();
		
		if(command.equals(MainActionCommands.COPY_TAUTOMER_ACCESSION_COMMAND.getName()))
			copyTautomerAccession();
	}
	
	private void markCompoundGroupCurated(boolean b) {
		
		CompoundIdentity cid = primaryCompoundListingTable.getSelectedCompound();
		if(cid == null)
			return;
		
		try {
			CompoundCurationUtils.setCompoundTautomerGroupCuratedFlag(cid.getPrimaryDatabaseId(), b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		primaryCompoundListingTable.updateCidData(cid, b);
		curationMap.put(cid, b);
	}

	private void copyPrimaryCompoundAccession() {

		CompoundIdentity cid = primaryCompoundListingTable.getSelectedCompound();
		if(cid == null)
			return;
		
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(cid.getPrimaryDatabaseId());
		clpbrd.setContents(stringSelection, null);		
	}

	private void addSelectedTautomerAsNewCompound() {

		CompoundIdentity primaryCid = 
				primaryCompoundListingTable.getSelectedCompound();
		CompoundIdentity tautomer = 
				tautomerListingTable.getSelectedCompound();
		
		if(primaryCid == null || tautomer == null)
			return;
		
		CompoundCurationUtils.addSelectedTautomerAsNewCompound(primaryCid, tautomer);		
		tautomerMap.get(primaryCid).remove(tautomer);
		tautomerMap.put(tautomer, new TreeSet<CompoundIdentity>());
		curationMap.put(tautomer, true);
		primaryCompoundListingTable.setTableModelFromCompoundCollection(curationMap);
		primaryCompoundListingTable.selectCompoundIdentity(tautomer);
	}

	private void replacePrimaryCompoundWithSelectedTautomer() {

		CompoundIdentity primaryCid = 
				primaryCompoundListingTable.getSelectedCompound();
		CompoundIdentity tautomer = 
				tautomerListingTable.getSelectedCompound();
		
		if(primaryCid == null || tautomer == null)
			return;
		
		String message = 
				"Do you want to set " + tautomer.getName() + " (" + tautomer.getPrimaryDatabaseId() + 
				") \nas a primary compound in the tautomer group instead of\n"
				+ primaryCid.getName() + " (" + primaryCid.getPrimaryDatabaseId() + ")?";
		int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		CompoundCurationUtils.replacePrimaryCompoundWithSelectedTautomer(primaryCid, tautomer);
		
		Collection<CompoundIdentity> tautList = tautomerMap.remove(primaryCid);
		tautList.remove(tautomer);
		tautList.add(primaryCid);
		tautomerMap.put(tautomer, tautList);
		boolean curated = curationMap.remove(primaryCid);
		curationMap.put(tautomer, curated);
		primaryCompoundListingTable.setTableModelFromCompoundCollection(curationMap);
		primaryCompoundListingTable.selectCompoundIdentity(tautomer);
	}

	private void copyTautomerAccession() {

		CompoundIdentity cid = tautomerListingTable.getSelectedCompound();
		if(cid == null)
			return;
		
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(cid.getPrimaryDatabaseId());
		clpbrd.setContents(stringSelection, null);	
	}

	private void clearPanel() {
		
		primaryCompoundListingTable.getTable().getSelectionModel().removeListSelectionListener(this);
		primaryCompoundListingTable.clearTable();
		tautomerListingTable.getTable().getSelectionModel().removeListSelectionListener(this);
		tautomerListingTable.clearTable();
		primaryStructuralDescriptorsPanel.clearPanel();
		tautomerStructuralDescriptorsPanel.clearPanel();
		primaryCompoundListingTable.getTable().getSelectionModel().addListSelectionListener(this);
		tautomerListingTable.getTable().getSelectionModel().addListSelectionListener(this);		
		primaryMolStructurePanel.clearPanel();
		tautomerMolStructurePanel.clearPanel();		
		selectedIdentity = null;
		tautomerMap.clear();
	}
	
	private void fetchCompoundDataForCuration() {
		
		clearPanel();
		TautomerDataRetrievalTask task = new TautomerDataRetrievalTask();
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Fetching tautomer data for curation", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class TautomerDataRetrievalTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
	
		private Collection<CompoundIdentity>compoundCollection;

		public TautomerDataRetrievalTask() {
			
		}

		@Override
		public Void doInBackground() {
			
			try {
				getTautomerMap();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!tautomerMap.isEmpty()) 				
				primaryCompoundListingTable.setTableModelFromCompoundCollection(curationMap) ;
						
			return null;			
		}
	}	
	
	private void getTautomerMap() throws Exception{
		
		curationMap = new TreeMap<CompoundIdentity,Boolean>();
		Map<String,Boolean>curatedIdMap = new TreeMap<String,Boolean>();
		Map<String,Collection<String>>cpdIdMap = new TreeMap<String,Collection<String>>();
		Map<String,Collection<String>>dbIdMap = new TreeMap<String,Collection<String>>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT PRIMARY_ACCESSION, PRIMARY_SOURCE_DB,  " +
				"SECONDARY_ACCESSION, SECONDARY_SOURCE_DB, CURATED  " +
				"FROM COMPOUNDDB.COMPOUND_GROUP";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String pa = rs.getString("PRIMARY_ACCESSION");
			String sa = rs.getString("SECONDARY_ACCESSION");
			String pdb = rs.getString("PRIMARY_SOURCE_DB");
			String sdb = rs.getString("SECONDARY_SOURCE_DB");
			boolean isCurated = (rs.getString("CURATED") != null);
			
			if(!cpdIdMap.containsKey(pa))
				cpdIdMap.put(pa, new TreeSet<String>());
			
			cpdIdMap.get(pa).add(sa);
			
			if(!dbIdMap.containsKey(pdb))
				dbIdMap.put(pdb, new TreeSet<String>());
			
			dbIdMap.get(pdb).add(pa);

			if(!dbIdMap.containsKey(sdb))
				dbIdMap.put(sdb, new TreeSet<String>());
			
			dbIdMap.get(sdb).add(sa);
			
			curatedIdMap.put(pa, isCurated);
		}
		rs.close();
		ps.close();
		
		Map<String,CompoundIdentity>compoundIdMap = new TreeMap<String,CompoundIdentity>();
		for(Entry<String, Collection<String>> dbEntry : dbIdMap.entrySet()) {
			
			CompoundDatabaseEnum cpdDb = CompoundDatabaseEnum.getCompoundDatabaseByName(dbEntry.getKey());
			Collection<CompoundIdentity>cpdList = fetchCompoundIds(cpdDb, dbEntry.getValue());
			for(CompoundIdentity cid : cpdList)
				compoundIdMap.put(cid.getPrimaryDatabaseId(), cid);
		}
		for(Entry<String, Collection<String>> cpdEntry : cpdIdMap.entrySet()) {
			
			Set<CompoundIdentity>secCids = new TreeSet<CompoundIdentity>();
			for(String sid : cpdEntry.getValue())
				secCids.add(compoundIdMap.get(sid));
			
			tautomerMap.put(compoundIdMap.get(cpdEntry.getKey()), secCids);
		}
		for(CompoundIdentity cid : tautomerMap.keySet()) 
			curationMap.put(cid, curatedIdMap.get(cid.getPrimaryDatabaseId()));
		
		ConnectionManager.releaseConnection(conn);
	}
	
	private Collection<CompoundIdentity> fetchCompoundIds(
			CompoundDatabaseEnum db, Collection<String> idList) {

		Collection<CompoundIdentity>compoundCollection = new TreeSet<CompoundIdentity>();
		if(db.equals(CompoundDatabaseEnum.HMDB)) {
			
			try {					
				compoundCollection = fetchHMDBDataForCuration(idList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(db.equals(CompoundDatabaseEnum.DRUGBANK)) {
			
			try {					
				compoundCollection = fetchHMDBDataForCuration(idList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(db.equals(CompoundDatabaseEnum.FOODB)) {
			
			try {					
				compoundCollection = fetchHMDBDataForCuration(idList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(db.equals(CompoundDatabaseEnum.T3DB)) {
			
			try {					
				compoundCollection = fetchHMDBDataForCuration(idList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(db.equals(CompoundDatabaseEnum.LIPIDMAPS)) {
			
			try {					
				compoundCollection = fetchHMDBDataForCuration(idList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return compoundCollection;
	}

	private Collection<CompoundIdentity> fetchHMDBDataForCuration(Collection<String> idList2) throws Exception{
		
		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, NAME, FORMULA_FROM_SMILES, "
			+ "EXACT_MASS, SMILES, INCHI_KEY_FROM_SMILES, CHARGE " +
			"FROM COMPOUNDDB.HMDB_COMPOUND_DATA D WHERE ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(String cid : idList2) {
			
			ps.setString(1, cid);
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
				identity.setInChiKey(rs.getString("INCHI_KEY_FROM_SMILES"));
				identity.setCharge(rs.getInt("CHARGE"));
				idList.add(identity);
			}
			rs.close();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	private Collection<CompoundIdentity> fetchDrugBankDataForCuration() throws Exception{
		
		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, COMMON_NAME, FORMULA_FROM_SMILES, EXACT_MASS, SMILES, INCHI_KEY, CHARGE " +
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
			"SELECT PUBLIC_ID, NAME, FORMULA_FROM_SMILES, MOLDB_MONO_MASS, MOLDB_SMILES, MOLDB_INCHIKEY, CHARGE " +
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
			"SELECT ACCESSION, COMMON_NAME, FORMULA_FROM_SMILES, MASS_FROM_SMILES, SMILES, INCHIKEY, CHARGE " +
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
			"SELECT LMID, COMMON_NAME, SYSTEMATIC_NAME, MOLECULAR_FORMULA, EXACT_MASS, SMILES, INCHI_KEY, CHARGE " +
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
			+ "CDK_SMILES, CDK_INCHI_KEY, CHARGE " 
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
			identity.setCharge(rs.getInt("CHARGE"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;		
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
			
		if(e.getValueIsAdjusting() || e.getSource() == null)
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			//	If primary compound changed
			if(listener.equals(primaryCompoundListingTable.getTable())) {
				
				CompoundIdentity mfid = 
						primaryCompoundListingTable.getSelectedCompound();
				if(mfid == null)
					return;
				
				primaryStructuralDescriptorsPanel.loadCompoundIdentity(mfid);
				primaryMolStructurePanel.showStructure(mfid.getSmiles());
				Collection<CompoundIdentity> tautomers = tautomerMap.get(mfid);
				tautomerListingTable.setTableModelFromCompoundCollection(tautomers);
				if(tautomers.size() == 1) {
					
					CompoundIdentity tautomer = tautomers.iterator().next();
					tautomerStructuralDescriptorsPanel.loadCompoundIdentity(tautomer);	
					tautomerMolStructurePanel.showStructure(tautomer.getSmiles());
				}
			}
			//	If tautomer changed
			if(listener.equals(tautomerListingTable.getTable())) {
				
				CompoundIdentity mfid = 
						tautomerListingTable.getSelectedCompound();
				if(mfid == null)
					return;
				
				tautomerStructuralDescriptorsPanel.clearPanel();
				tautomerStructuralDescriptorsPanel.loadCompoundIdentity(mfid);
				tautomerMolStructurePanel.showStructure(mfid.getSmiles());
			}
		}	
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
		ps.setString(1, tautomerStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, tautomerStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, tautomerStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, tautomerStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, tautomerStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
		ps.setString(7, selectedIdentity.getPrimaryDatabaseId());			//	ACCESSION
		
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
		ps.setString(1, tautomerStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, tautomerStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, tautomerStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, tautomerStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, tautomerStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
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
		ps.setString(1, tautomerStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, tautomerStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, tautomerStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, tautomerStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, tautomerStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
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
		ps.setString(1, tautomerStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, tautomerStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, tautomerStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, tautomerStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, tautomerStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
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
		ps.setString(1, tautomerStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, tautomerStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, tautomerStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, tautomerStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, tautomerStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
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
		ps.setString(1, tautomerStructuralDescriptorsPanel.getFormula());	//	MS_READY_MOL_FORMULA
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
		ps.setString(3, tautomerStructuralDescriptorsPanel.getSmiles());		//	MS_READY_SMILES
		ps.setString(4, tautomerStructuralDescriptorsPanel.getInchiKey());	//	MS_READY_INCHI_KEY
		ps.setString(5, tautomerStructuralDescriptorsPanel.getInchiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
		ps.setInt(6, tautomerStructuralDescriptorsPanel.getCharge());		//	MS_READY_CHARGE
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
		ps.setDouble(2, tautomerStructuralDescriptorsPanel.getMass());		//	MS_READY_EXACT_MASS
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
