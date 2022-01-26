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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.featurelist;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.LibraryExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class FeatureSubsetPanel extends DockableMRC2ToolboxPanel {

	private FeatureSubsetToolbar toolbar;
	private JPanel wrapper;
	private JScrollPane scrollPane;
	private FeatureSubsetTable featureSetTable;
	private FeatureSubsetDialog featureSubsetDialog;

	private MsFeatureSet activeSet;
	private static final Icon componentIcon = 
			GuiUtils.getIcon("editCollection", 16);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "FeatureSubsetPanel.layout");

	public FeatureSubsetPanel() {

		super("FeatureSubsetPanel", "Feature subsets", componentIcon);
		setLayout(new BorderLayout(0, 0));

		toolbar = new FeatureSubsetToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		wrapper = new JPanel();
		wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(wrapper, BorderLayout.CENTER);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		featureSetTable = new FeatureSubsetTable();
		featureSetTable.setBorder(new EmptyBorder(10, 0, 20, 0));
		scrollPane = new JScrollPane(featureSetTable);
		scrollPane.setBorder(new EmptyBorder(0, 0, 20, 0));
		wrapper.add(scrollPane);
		
		//	TODO switch to "DISPOSE_ON_CLOSE"
		featureSubsetDialog = new FeatureSubsetDialog(this);
		initActions();
		loadLayout(layoutConfigFile);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getActionCommand().equals(MainActionCommands.NEW_FEATURE_SUBSET_COMMAND.getName()))
			showNewFeatureSetDialog();

		if (event.getActionCommand().equals(MainActionCommands.EDIT_FEATURE_SUBSET_COMMAND.getName()))
			showFeatureSetEditor();

		if (event.getActionCommand().equals(MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName()))
			exportSelectedFeatureSet();

		if (event.getActionCommand().equals(MainActionCommands.DELETE_FEATURE_SUBSET_COMMAND.getName()))
			deleteSelectedFeatureSet();

		if (event.getActionCommand().equals(MainActionCommands.FINISH_FEATURE_SUBSET_EDIT_COMMAND.getName()))
			finishSubsetEdit();

		if (event.getActionCommand().equals(MainActionCommands.SHOW_FEATURE_EXPORT_DIALOG_COMMAND.getName()))
			showExportDialog();

		if (event.getActionCommand().equals(MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName()))
			removeSelectedFeatures();

		if (event.getActionCommand().equals(MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName())
				|| event.getActionCommand()
						.equals(MainActionCommands.ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND.getName())) {

			addFeaturesFromStatsTableToActiveSet(event.getActionCommand());
		}
		if (event.getActionCommand().equals(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName()))
			linkToFeatureDataPanel();

		if (event.getActionCommand().equals(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName()))
			unlinkFromFeatureDataPanel();
	}

	private void activateDefaultSet() {
		
		MsFeatureSet allfeatureSet = currentProject.getAllFeaturesSetFordataPipeline(activeDataPipeline);
		currentProject.setActiveFeatureSetForDataPipeline(allfeatureSet, activeDataPipeline);


//		int column = featureSetTable.getColumnIndex(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN);
//
//		for (int i = 0; i < featureSetTable.getRowCount(); i++) {
//
//			MsFeatureSet set = (MsFeatureSet) featureSetTable.getValueAt(i, column);
//
//			if (set.getName().equals(GlobalDefaults.ALL_FEATURES.getName()))
//				set.setActive(true);
//			else
//				set.setActive(false);
//		}
	}

	private void addFeaturesFromStatsTableToActiveSet(String actionCommand) {

		if (activeSet.isLocked()) {

			MessageDialog.showWarningMsg(
					"Active set is locked and can't be edited!\n" + 
					"Please select or create another feature set.");
			return;
		}
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
		MsFeature[] features = new MsFeature[0];

		if (actionCommand.equals(MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName()))
			features = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
					.getSelectedFeaturesArray();

		if (actionCommand.equals(MainActionCommands.ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND.getName()))
			features = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
					.getFilteredFeatures();

		if (activeSet != null && features.length > 0)
			activeSet.addFeatures(Arrays.asList(features));
	}

	private void addSelectedFeatures(MsFeatureSet editedSet) {

		if (editedSet.isLocked()) {

			MessageDialog.showWarningMsg(
					"Active set is locked and can't be edited!\n" + 
					"Please select or create another feature set.");
			return;
		}

		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
		MsFeature[] selected = new MsFeature[0];

		if (featureSubsetDialog.getFeaturesSelectionToAdd().equals(TableRowSubset.SELECTED))
			selected = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
					.getSelectedFeaturesArray();

		if (featureSubsetDialog.getFeaturesSelectionToAdd().equals(TableRowSubset.FILTERED))
			selected = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
					.getFilteredFeatures();

		if (selected.length > 0)
			editedSet.addFeatures(Arrays.asList(selected));
	}

	private void addSetListeners(MsFeatureSet newSet) {

		newSet.addListener(this);
		newSet.addListener((FeatureSetListener) 
				MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA));

		// TODO link other panels if necessary
	}

	private void createNewFeatureSet(MsFeatureSet newSet) {

		String newName = featureSubsetDialog.getSubsetName();
		MsFeatureSet existing  = currentProject.getMsFeatureSetsForDataPipeline(activeDataPipeline).stream().
			filter(s -> s.getName().equalsIgnoreCase(newName)).findFirst().orElse(null);
		if (existing != null) {
			MessageDialog.showWarningMsg("Feature set with the name '" + newSet.getName()
					+ "' already exists.\nPlease specify a different name.");
			return;
		}
		newSet.setName(newName);
		newSet.addFeatures(featureSubsetDialog.getSelectedSourceSubset().getFeatures());

		if (featureSubsetDialog.getFeaturesSelectionToAdd() != null)
			addSelectedFeatures(newSet);

		addSetListeners(newSet);
		activeSet = newSet;
		newSet.fireFeatureSetEvent(ParameterSetStatus.CREATED);
	}

	private void deleteSelectedFeatureSet() {
		
		if(featureSetTable.getSelectedRow() == -1)
			return;

		MsFeatureSet setToDelete = 
				(MsFeatureSet) featureSetTable.getValueAt(featureSetTable.getSelectedRow(), 1);

		if (setToDelete.isLocked()) {
			MessageDialog
					.showErrorMsg("Feature subset '" + setToDelete.getName() + 
							"' is locked and can not be deleted!");
			return;
		} 
		int delete = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete feature subset \"" + setToDelete.getName() + "\"?");

		if (delete == JOptionPane.YES_OPTION){
			
			currentProject.getMsFeatureSetsForDataPipeline(activeDataPipeline).remove(setToDelete);
			if(setToDelete.isActive())
				activateDefaultSet();			
		}
	}

	private void editSelectedFeatureSet(MsFeatureSet editedSet) {

		editedSet.setSuppressEvents(true);
		editedSet.setName(featureSubsetDialog.getSubsetName());
		if (featureSubsetDialog.getFeaturesSelectionToAdd() != null)
			addSelectedFeatures(editedSet);
		
		editedSet.setSuppressEvents(false);
		editedSet.fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	private void exportSelectedFeatureSet() {

		if (featureSetTable.getSelectedRow() == -1)
			return;

		MsFeatureSet setToExport =
				(MsFeatureSet)featureSetTable.getValueAt(featureSetTable.getSelectedRow(), 1);
		if(setToExport.getFeatures().isEmpty()) {
			MessageDialog.showErrorMsg("Selected collection is empty!", this.getContentPane());
			return;
		}
		DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
		String libraryName =
				currentProject.getName() + "_" +
				currentProject.getActiveDataPipeline().getCode() + "_" +
				setToExport.getName();

		CompoundLibrary currentLibrary = new CompoundLibrary(libraryName);
		LibraryExportDialog libraryExportDialog = 
				new LibraryExportDialog(MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName());
		libraryExportDialog.setCurrentLibrary(currentLibrary);
		libraryExportDialog.setFeatureSubset(setToExport.getFeatures());
		libraryExportDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		libraryExportDialog.setVisible(true);
	}

	private void finishSubsetEdit() {

		MsFeatureSet setToEdit = featureSubsetDialog.getActiveSet();
		if (currentProject.getMsFeatureSetsForDataPipeline(activeDataPipeline).contains(setToEdit))
			editSelectedFeatureSet(setToEdit);
		else
			createNewFeatureSet(setToEdit);			

		featureSubsetDialog.dispose();
	}

	private void linkToFeatureDataPanel() {

		int column = featureSetTable.getColumnIndex(
				FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN);

		for (int i = 0; i < featureSetTable.getRowCount(); i++) {

			MsFeatureSet set = (MsFeatureSet) featureSetTable.getValueAt(i, column);
			addSetListeners(set);
		}
		activeSet.fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	private void removeSelectedFeatures() {

		MsFeature[] selected = featureSubsetDialog.getSelectedFeatures();
		if (selected.length > 0) {

			int approve = MessageDialog.showChoiceMsg(
					"Remove selected features from the subset?",
					this.getContentPane());

			if (approve == JOptionPane.YES_OPTION) {

				MsFeatureSet setToEdit = featureSubsetDialog.getActiveSet();
				setToEdit.removeFeatures(selected);
			}
		}
	}

	private void removeSetListeners(MsFeatureSet newSet) {
		newSet.removeAllListeners();
	}

	private void showExportDialog() {
		// TODO Auto-generated method stub

	}

	private void showFeatureSetEditor() {
		if (featureSetTable.getSelectedRow() == -1) 
			return;

		MsFeatureSet setToEdit = 
				(MsFeatureSet) featureSetTable.getValueAt(featureSetTable.getSelectedRow(), 1);

		if (setToEdit.isLocked()) {
			MessageDialog
					.showErrorMsg("Feature subset '" + setToEdit.getName() + 
							"' is locked and can not be edited!", this.getContentPane());
		} else {
			featureSubsetDialog = new FeatureSubsetDialog(this);
			featureSubsetDialog.loadFeatureSubset(setToEdit);
			featureSubsetDialog.disableSetSelector();
			featureSubsetDialog.setLocationRelativeTo(this.getContentPane());
			featureSubsetDialog.setVisible(true);
		}	
	}

	private void showNewFeatureSetDialog() {

		MsFeatureSet newSet = new MsFeatureSet("New feature set");
		featureSubsetDialog = new FeatureSubsetDialog(this);
		featureSubsetDialog.loadFeatureSubset(newSet);
		featureSubsetDialog.populateSetSelector();
		featureSubsetDialog.enableSetSelector();
		featureSubsetDialog.setLocationRelativeTo(this.getContentPane());
		featureSubsetDialog.setVisible(true);
	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {

		MsFeatureSet featureSet = (MsFeatureSet) e.getSource();
		ParameterSetStatus status = e.getStatus();

		if (status.equals(ParameterSetStatus.CHANGED)) {

			if (featureSubsetDialog.isVisible())
				featureSubsetDialog.loadFeatureSubset(featureSet);
		}
		if (status.equals(ParameterSetStatus.CREATED)) {

			if(!activeSet.equals(featureSet)) {
				activeSet = featureSet;
				currentProject.setActiveFeatureSetForDataPipeline(activeSet, activeDataPipeline);
			}
		}
		if (status.equals(ParameterSetStatus.DELETED)) {

			if (activeSet.equals(featureSet))
				activateDefaultSet();
		}
		if (status.equals(ParameterSetStatus.ENABLED)) {

			if(!activeSet.equals(featureSet)) {
				
				activeSet = featureSet;
				currentProject.setActiveFeatureSetForDataPipeline(activeSet, activeDataPipeline);
			}
		}
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	private void unlinkFromFeatureDataPanel() {

		int column = featureSetTable.getColumnIndex(
				FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN);

		for (int i = 0; i < featureSetTable.getRowCount(); i++) {

			MsFeatureSet set = (MsFeatureSet) featureSetTable.getValueAt(i, column);
			removeSetListeners(set);
		}
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newDataPipeline);
		if(project == null || newDataPipeline == null)
			return;
		
		featureSetTable.setModelFromProject(currentProject, newDataPipeline);
		scrollPane.setPreferredSize(featureSetTable.getPreferredScrollableViewportSize());
		activeSet = null;
		if (newDataPipeline != null) {

			for (MsFeatureSet set : currentProject.getMsFeatureSetsForDataPipeline(newDataPipeline))
				addSetListeners(set);

			activeSet = currentProject.getActiveFeatureSetForDataPipeline(newDataPipeline);
		}
		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, newDataPipeline);
	}
	
	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void closeProject() {

		super.closeProject();
		clearPanel();
		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public synchronized void clearPanel() {
		featureSetTable.clearTable();
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

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
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(!e.getValueIsAdjusting()) {

		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		
	}
}
