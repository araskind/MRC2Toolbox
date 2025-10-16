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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.InChiKeyPortion;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.ctab.DockableCompoundIdentityClusterTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.search.SimpleRedundantCompoundDataPullDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.struct.DockableCompoundStructureTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class CompoundDatabaseCuratorFrame extends JFrame 
		implements ListSelectionListener, PersistentLayout, BackedByPreferences, ActionListener  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4781460173985140279L;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.CompoundDatabaseCuratorFrame";
	
	public static final String MW_START = "MW_START";
	public static final String MW_END = "MW_END";
	public static final String INCHI_KEY_PORTION_FOR_MATCHING = "INCHI_KEY_PORTION_FOR_MATCHING";
	public static final String MAX_RECORDS = "MAX_RECORDS";
	
	private static final Icon curateCompoundIcon = GuiUtils.getIcon("curateCompound", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "CompoundDatabaseCuratorFrame.layout");
	
	private CompoundDatabaseCuratorToolbar toolbar;
	private CControl control;
	private CGrid grid;
	private IndeterminateProgressDialog idp;
	private DockableCompoundIdentityClusterTable compoundIdentityClusterTable;
	private DockableCompoundStructureTable compoundStructureTable;
	private SimpleRedundantCompoundDataPullDialog simpleRedundantCompoundDataPullDialog;
	
	private Range massRange;
	private InChiKeyPortion inChiKeyPortionForMatching;
	private int maxRecords;
	
	public CompoundDatabaseCuratorFrame() {

		super("Compound database curator");
		setIconImage(((ImageIcon) curateCompoundIcon).getImage());
		setSize(new Dimension(1200, 860));
		setPreferredSize(new Dimension(1200, 860));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new CompoundDatabaseCuratorToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		compoundIdentityClusterTable = new DockableCompoundIdentityClusterTable();
		compoundIdentityClusterTable.getTable().getSelectionModel().addListSelectionListener(this);
		
		compoundStructureTable = new DockableCompoundStructureTable();

		grid.add(0, 0, 1, 1,
				compoundIdentityClusterTable, compoundStructureTable);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		loadPreferences();
		
		testGui();
	}
	
	//	Debug only
	private void testGui() {
		String[] ids = new String[] {
				"CHEBI:100013",
				"CHEBI:100019",
				"CHEBI:100057",
				"CHEBI:100061" };
		CompoundIdentityCluster cluster = new CompoundIdentityCluster();
		for(int i=0; i<ids.length; i++) {
			
			CompoundIdentity id = null;
			try {
				id = CompoundDatabaseUtils.getCompoundById(ids[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(id != null)
				cluster.addIdentity(new CompoundIdentityInfoBundle(id));
		}
		loadCompoundIdentityCluster(cluster);		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command  == null)
			return;
		
		if(command.equals(MainActionCommands.SHOW_SIMPLE_REDUNDANT_COMPOUND_DATA_PULL_DIALOG.getName()))
			showSimpleRedundantCompoundDataPullDialog();
		
		if(command.equals(MainActionCommands.PULL_REDUNDANT_COMPOUNDS_FROM_DATABASE.getName()))
			pullredundantCompoundsFromDatabase();
		
	}
	
	private void pullredundantCompoundsFromDatabase() {
		
		Collection<String>errors = simpleRedundantCompoundDataPullDialog.validateInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), simpleRedundantCompoundDataPullDialog);
			return;
		}
		massRange = simpleRedundantCompoundDataPullDialog.getMassRange();
		inChiKeyPortionForMatching = simpleRedundantCompoundDataPullDialog.getInChiKeyPortion();
		maxRecords = simpleRedundantCompoundDataPullDialog.getMaxRecords();		
		savePreferences();
		simpleRedundantCompoundDataPullDialog.dispose();
		
		//	TODO
	}
	
	private void showSimpleRedundantCompoundDataPullDialog() {
		
		simpleRedundantCompoundDataPullDialog = new SimpleRedundantCompoundDataPullDialog(this);
		simpleRedundantCompoundDataPullDialog.setMassRange(massRange);
		simpleRedundantCompoundDataPullDialog.setInChiKeyPortion(inChiKeyPortionForMatching);
		simpleRedundantCompoundDataPullDialog.setMaxRecords(maxRecords);
		simpleRedundantCompoundDataPullDialog.setLocationRelativeTo(this);
		simpleRedundantCompoundDataPullDialog.setVisible(true);
	}
	
	@Override
	public void setVisible(boolean b) {
		
		if(!b) {
			saveLayout(layoutConfigFile);
			savePreferences();
		}
		super.setVisible(b);
		if(b)
			toFront();
	}
		
	@Override
	public void dispose() {
		
		saveLayout(layoutConfigFile);
		savePreferences();
		super.dispose();
	}
	
	public void clearPanels() {
		
		compoundIdentityClusterTable.clearTable();;		
		compoundStructureTable.clearTable();
	}
	
	public void loadCompoundIdentityCluster(CompoundIdentityCluster cluster) {
		clearPanels();
		if(cluster  != null) {
			compoundIdentityClusterTable.setModelFromCompoundIdentityCluster(cluster);
			compoundStructureTable.setModelFromCompoundIdentityCluster(cluster);
		}
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
		double minMass = preferences.getDouble(MW_START, 0.0d);
		double maxMass = preferences.getDouble(MW_END, 0.0d);
		massRange = new Range(minMass, maxMass);
		maxRecords = preferences.getInt(MAX_RECORDS, 50);
		inChiKeyPortionForMatching = InChiKeyPortion.getOptionByName(
				preferences.get(INCHI_KEY_PORTION_FOR_MATCHING, InChiKeyPortion.COMPLETE.name()));
	}

	@Override
	public void loadPreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);

		preferences.putDouble(MW_START, massRange.getMin());
		preferences.putDouble(MW_END, massRange.getMax());
		preferences.putInt(MAX_RECORDS, maxRecords);
		if(inChiKeyPortionForMatching != null)
			preferences.put(INCHI_KEY_PORTION_FOR_MATCHING, inChiKeyPortionForMatching.name());
	}
	
	public void setParentPanel(DockableMRC2ToolboxPanel parentPanel) {

	}
	
	class CreateMzRtDataSetTask extends LongUpdateTask {
		
		public CreateMzRtDataSetTask() {

		}

		@Override
		public Void doInBackground() {

			try {
				
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if(e.getValueIsAdjusting() || e.getSource() == null)
			return;
		
		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(compoundIdentityClusterTable.getTable())) {
				
				CompoundIdentity id = compoundIdentityClusterTable.getSelectedIdentity();
				if(id != null)
					compoundStructureTable.selectStructureForIdentity(id);
				
				return;
			}
		}
	}
}







