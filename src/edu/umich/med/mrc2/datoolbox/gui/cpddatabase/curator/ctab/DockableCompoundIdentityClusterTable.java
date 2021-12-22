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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.ctab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCompoundIdentityClusterTable 
	extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("clusterFeatureTable", 16);	
	private static final Icon multipleIdsIcon = GuiUtils.getIcon("multipleIds", 16);
	private static final Icon uniqueIdsIcon = GuiUtils.getIcon("checkboxFull", 16);

	private CompoundIdentityClusterTable idTable;
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.DockableCompoundIdentityClusterTable";
//	public static final String SHOW_UNIQUE_IDENTITY_LIBRARY_HITS = "SHOW_UNIQUE_IDENTITY_LIBRARY_HITS";
	private boolean showUniqueIdsOnly;	
	private SimpleButtonAction toggelUniqueIdsButton;

	public DockableCompoundIdentityClusterTable() {

		super("DockableCompoundIdentityClusterTable", componentIcon, 
					"Equivalent compound list", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		idTable = new CompoundIdentityClusterTable();
		idTable.setDefaultIdTableModelListener(
				new CompoundIdentityClusterTableModelListener(idTable));
		add(new JScrollPane(idTable));
		
		loadPreferences();
		initButtons();
	}
	
	private void initButtons() {

//		DefaultDockActionSource actions = new DefaultDockActionSource(
//				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));
//
//		if (showUniqueIdsOnly) {
//			toggelUniqueIdsButton = GuiUtils.setupButtonAction(
//					MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName(), 
//					MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName(), 
//					uniqueIdsIcon, this);
//		}
//		else {
//			toggelUniqueIdsButton = GuiUtils.setupButtonAction(
//					MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName(), 
//					MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName(), 
//					multipleIdsIcon, this);
//		}
//		actions.add(toggelUniqueIdsButton);
//		actions.addSeparator();
//		intern().setActionOffers(actions);
	}

	public CompoundIdentityClusterTable getTable() {
		return idTable;
	}
	public CompoundIdentity getCompoundIdentityAtPopup() {
		return idTable.getCompoundIdentityAtPopup();
	}

	public synchronized void clearTable() {
		idTable.clearTable();
	}

	public void setModelFromCompoundIdentityCluster(CompoundIdentityCluster cluster) {
		idTable.setModelFromCompoundIdentityCluster(cluster);
	}
	
	public CompoundIdentity getSelectedIdentity() {
		return idTable.getSelectedCompoundId();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command  == null)
			return;
		
	}	

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;

	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
	}
}













