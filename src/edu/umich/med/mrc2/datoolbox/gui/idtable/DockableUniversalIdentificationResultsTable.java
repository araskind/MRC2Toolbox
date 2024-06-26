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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.multi.MultipleSpectraDisplayDialog;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.tables.XTableColumnModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableUniversalIdentificationResultsTable 
	extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("text", 16);	
	private static final Icon multipleIdsIcon = GuiUtils.getIcon("multipleIds", 16);
	private static final Icon uniqueIdsIcon = GuiUtils.getIcon("checkboxFull", 16);
	
	private static final Icon msrtIcon = GuiUtils.getIcon("feature", 16);
	private static final Icon msmsIcon = GuiUtils.getIcon("msTwo", 16);
	private static final Icon msmsMultiIcon = GuiUtils.getIcon("msmsMulti", 16);

	private UniversalIdentificationResultsTable idTable;
	
	private Preferences preferences;
	public static final String PREFS_NODE = 
			"edu.umich.med.mrc2.DockableUniversalIdentificationResultsTable";
	public static final String SHOW_UNIQUE_IDENTITY_LIBRARY_HITS = 
			"SHOW_UNIQUE_IDENTITY_LIBRARY_HITS";
	private boolean showUniqueIdsOnly;	
	private SimpleButtonAction toggelUniqueIdsButton;
	private SimpleButtonAction toggelColumnSubsetButton;
	private SimpleButtonAction showMultiSpectraPlotButton;
	private Collection<CompoundIdSource>sorcesToExclude;
	private MultipleSpectraDisplayDialog multipleSpectraDisplayDialog;

	public DockableUniversalIdentificationResultsTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		idTable = new UniversalIdentificationResultsTable();
		add(new JScrollPane(idTable));
		
		loadPreferences();
		initButtons();
	}
	
	private void initButtons() {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));

		if (showUniqueIdsOnly) {
			toggelUniqueIdsButton = GuiUtils.setupButtonAction(
					MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName(), 
					MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName(), 
					uniqueIdsIcon, this);
		}
		else {
			toggelUniqueIdsButton = GuiUtils.setupButtonAction(
					MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName(), 
					MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName(), 
					multipleIdsIcon, this);
		}
		actions.add(toggelUniqueIdsButton);
		
		toggelColumnSubsetButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MS_RT_COLUMN_SUBSET.getName(), 
				MainActionCommands.SHOW_MS_RT_COLUMN_SUBSET.getName(), 
				msmsIcon, this);
		actions.add(toggelColumnSubsetButton);
		
		actions.addSeparator();
		
		showMultiSpectraPlotButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MULTISPECTRA_DISPLAY_COMMAND.getName(), 
				MainActionCommands.SHOW_MULTISPECTRA_DISPLAY_COMMAND.getName(), 
				msmsMultiIcon, this);
		actions.add(showMultiSpectraPlotButton);
		
		intern().setActionOffers(actions);
	}

	public UniversalIdentificationResultsTable getTable() {
		return idTable;
	}
	
	public MsFeatureIdentity getFeatureIdAtPopup() {
		return idTable.getFeatureIdAtPopup();
	}

	public void setValueAt(Object value, int row, int col) {
		idTable.setValueAt(value, row, col);
	}

	public synchronized void clearTable() {
		idTable.clearTable();
	}
	
	public void refreshTable() {
		
		MsFeature feature = idTable.getParentFeature();
		if(feature == null)
			return;
		
		MsFeatureIdentity currentId = getSelectedIdentity();
		idTable.setModelFromMsFeature(
				feature, sorcesToExclude, showUniqueIdsOnly);
		if(currentId != null)
			idTable.selectIdentity(currentId);
	}

	public void setModelFromMsFeature(MsFeature feature) {
		this.sorcesToExclude = null;
		idTable.setModelFromMsFeature(feature, showUniqueIdsOnly);
		
		if(multipleSpectraDisplayDialog != null 				
				&& multipleSpectraDisplayDialog.isVisible()) {
			multipleSpectraDisplayDialog.showMSMSLibraryMatches(
					feature, idTable.getAllIdentities());
		}
	}
	
	private void setModelFromMsFeature(
			MsFeature feature, Collection<CompoundIdSource>sorcesToExclude) {
		
		this.sorcesToExclude = sorcesToExclude;
		idTable.setModelFromMsFeature(
				feature, sorcesToExclude, showUniqueIdsOnly);
		if(multipleSpectraDisplayDialog != null 				
				&& multipleSpectraDisplayDialog.isVisible()) {
			multipleSpectraDisplayDialog.showMSMSLibraryMatches(
					feature, idTable.getAllIdentities());
		}
	}

	public MsFeatureIdentity getSelectedIdentity() {
		return idTable.getSelectedIdentity();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command  == null)
			return;
		
		if(command.equals(MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName()))
			switchUniqueIdPrferences(false);
	
		if(command.equals(MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName()))
			switchUniqueIdPrferences(true);
		
		if(command.equals(MainActionCommands.SHOW_MS_RT_COLUMN_SUBSET.getName()))
			showMSRTColumnSubset();
		
		if(command.equals(MainActionCommands.SHOW_MSMS_COLUMN_SUBSET.getName()))
			showMSMSColumnSubset();
		
		if (command.equals(MainActionCommands.SHOW_MULTISPECTRA_DISPLAY_COMMAND.getName()))
			showMultipleSpectraDisplayDialog();
	}
	
	private void showMultipleSpectraDisplayDialog() {
		
		if(multipleSpectraDisplayDialog != null 
				&& multipleSpectraDisplayDialog.isVisible()) {
			multipleSpectraDisplayDialog.toFront();
			return;
		}
		else {
			multipleSpectraDisplayDialog = new MultipleSpectraDisplayDialog();
			multipleSpectraDisplayDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			multipleSpectraDisplayDialog.setVisible(true);
			multipleSpectraDisplayDialog.toFront();
		}
	}
	
	public void showMSRTColumnSubset() {
		
		showColumnList(UniversalIdentificationResultsTableModel.msRtColumnList);
		
		toggelColumnSubsetButton.setText(
				MainActionCommands.SHOW_MS_RT_COLUMN_SUBSET.getName());
		toggelColumnSubsetButton.setCommand(
				MainActionCommands.SHOW_MSMS_COLUMN_SUBSET.getName());
		toggelColumnSubsetButton.setIcon(msrtIcon);
	}
	
	public void showMSMSColumnSubset() {
		
		showColumnList(UniversalIdentificationResultsTableModel.msmsColumnList);
		
		toggelColumnSubsetButton.setText(
				MainActionCommands.SHOW_MSMS_COLUMN_SUBSET.getName());
		toggelColumnSubsetButton.setCommand(
				MainActionCommands.SHOW_MS_RT_COLUMN_SUBSET.getName());
		toggelColumnSubsetButton.setIcon(msmsIcon);
	}
	
	private void showColumnList(Collection<String> msmscolumnlist) {

		XTableColumnModel colModel = 
				(XTableColumnModel)idTable.getColumnModel();
		TableModel model = idTable.getModel();
		for(int i=0; i<model.getColumnCount(); i++) {
			
			TableColumn column = colModel.getColumnByModelIndex(i);
			colModel.setColumnVisible(
					column, msmscolumnlist.contains(column.getIdentifier()));
		}
	}

	private void switchUniqueIdPrferences(boolean uniqueOnly) {
		
		showUniqueIdsOnly = uniqueOnly;		
		if (showUniqueIdsOnly) {
			toggelUniqueIdsButton.setText(
					MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName());
			toggelUniqueIdsButton.setCommand(
					MainActionCommands.SHOW_ALL_LIB_HITS_FOR_COMPOUND.getName());
			toggelUniqueIdsButton.setIcon(uniqueIdsIcon);
		}
		else {
			toggelUniqueIdsButton.setText(
					MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName());
			toggelUniqueIdsButton.setCommand(
					MainActionCommands.SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND.getName());
			toggelUniqueIdsButton.setIcon(multipleIdsIcon);
		}
		savePreferences();
		if(idTable.getParentFeature() == null)
			return;
		
		if(sorcesToExclude == null)
			setModelFromMsFeature(idTable.getParentFeature());
		else
			setModelFromMsFeature(idTable.getParentFeature(), sorcesToExclude);
	}
	
	
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		showUniqueIdsOnly = preferences.getBoolean(SHOW_UNIQUE_IDENTITY_LIBRARY_HITS, false);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.putBoolean(SHOW_UNIQUE_IDENTITY_LIBRARY_HITS, showUniqueIdsOnly);  
	}
	
	public void selectIdentity(MsFeatureIdentity id) {
		idTable.selectIdentity(id);
	}
}













