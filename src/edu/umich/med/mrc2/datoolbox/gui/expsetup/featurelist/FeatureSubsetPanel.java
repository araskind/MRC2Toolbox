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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
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
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class FeatureSubsetPanel extends DockableMRC2ToolboxPanel {

	private FeatureSubsetToolbar toolbar;

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
		featureSetTable = new FeatureSubsetTable();
		add(new JScrollPane(featureSetTable), BorderLayout.CENTER);
		
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return;
		
		super.actionPerformed(event);
		String command = event.getActionCommand();
		
		if (command.equals(MainActionCommands.NEW_FEATURE_SUBSET_COMMAND.getName()))
			showNewFeatureSetDialog();
		
		if (command.equals(MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName()))
			createNewFeatureSubset();

		if (command.equals(MainActionCommands.EDIT_FEATURE_SUBSET_COMMAND.getName()))
			showFeatureSetEditor();

		if (command.equals(MainActionCommands.SAVE_CHANGES_TO_FEATURE_SUBSET_COMMAND.getName()))
			saveChangesToFeatureSet();
		
		if (command.equals(MainActionCommands.DELETE_FEATURE_SUBSET_COMMAND.getName()))
			deleteSelectedFeatureSet();
		
		if (command.equals(MainActionCommands.SHOW_FEATURE_EXPORT_DIALOG_COMMAND.getName()))
			showExportDialog();
		
		if (command.equals(MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName()))
			exportSelectedFeatureSet();

//		if (command.equals(MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName()))
//			removeSelectedFeatures();

//		if (command.equals(MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName())
//				|| command
//						.equals(MainActionCommands.ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND.getName())) {
//
//			addFeaturesFromStatsTableToActiveSet(command);
//		}
//		if (command.equals(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName()))
//			linkToFeatureDataPanel();
//
//		if (command.equals(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName()))
//			unlinkFromFeatureDataPanel();
	}

	private void activateDefaultSet() {
		
		MsFeatureSet allfeatureSet = 
				currentExperiment.getAllFeaturesSetFordataPipeline(activeDataPipeline);
		currentExperiment.setActiveFeatureSetForDataPipeline(
				allfeatureSet, activeDataPipeline);
	}

//	private void addFeaturesFromStatsTableToActiveSet(String actionCommand) {
//
//		if (activeSet.isLocked()) {
//
//			MessageDialog.showWarningMsg(
//					"Active set is locked and can't be edited!\n" + 
//					"Please select or create another feature set.");
//			return;
//		}
//		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
//		MsFeature[] features = new MsFeature[0];
//
//		if (actionCommand.equals(MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName()))
//			features = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
//					.getSelectedFeaturesArray();
//
//		if (actionCommand.equals(MainActionCommands.ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND.getName()))
//			features = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
//					.getFilteredFeatures();
//
//		if (activeSet != null && features.length > 0)
//			activeSet.addFeatures(Arrays.asList(features));
//	}
//
//	private void addSelectedFeatures(MsFeatureSet editedSet) {
//
//		if (editedSet.isLocked()) {
//
//			MessageDialog.showWarningMsg(
//					"Active set is locked and can't be edited!\n" + 
//					"Please select or create another feature set.");
//			return;
//		}
//
//		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
//		MsFeature[] selected = new MsFeature[0];
//
//		if (featureSubsetDialog.getFeaturesSelectionToAdd().equals(TableRowSubset.SELECTED))
//			selected = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
//					.getSelectedFeaturesArray();
//
//		if (featureSubsetDialog.getFeaturesSelectionToAdd().equals(TableRowSubset.FILTERED))
//			selected = ((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
//					.getFilteredFeatures();
//
//		if (selected.length > 0)
//			editedSet.addFeatures(Arrays.asList(selected));
//	}

	public void addSetListeners(MsFeatureSet newSet) {

		newSet.addListener(this);
		newSet.addListener((FeatureSetListener) 
				MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA));

		// TODO link other panels if necessary
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
			
			currentExperiment.getMsFeatureSetsForDataPipeline(activeDataPipeline).remove(setToDelete);
			if(setToDelete.isActive())
				activateDefaultSet();
			
			featureSetTable.setModelFromProject(currentExperiment, activeDataPipeline);
		}
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
		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		String libraryName =
				currentProject.getName() + "_" +
				currentProject.getActiveDataPipeline().getSaveSafeName() + "_" +
				setToExport.getName();

		CompoundLibrary currentLibrary = new CompoundLibrary(libraryName);
		LibraryExportDialog libraryExportDialog = 
				new LibraryExportDialog(
						MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName(),
						currentLibrary);
		libraryExportDialog.setDestinationDirectory(currentProject.getExportsDirectory());
		libraryExportDialog.setFeatureSubset(setToExport.getFeatures());
		libraryExportDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		libraryExportDialog.setVisible(true);
	}

	private void saveChangesToFeatureSet() {

		Collection<String>errors = 
				featureSubsetDialog.validateFeatureSubsetData();
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), featureSubsetDialog);
			return;
		}
		MsFeatureSet editedSet = featureSubsetDialog.getActiveSet();
		editedSet.setSuppressEvents(true);
		editedSet.setName(featureSubsetDialog.getSubsetName());
//		if (featureSubsetDialog.getFeaturesSelectionToAdd() != null)
//			addSelectedFeatures(editedSet);
		
		editedSet.setSuppressEvents(false);
		editedSet.fireFeatureSetEvent(ParameterSetStatus.CHANGED);
		featureSubsetDialog.dispose();
	}

//	private void linkToFeatureDataPanel() {
//
//		int column = featureSetTable.getColumnIndex(
//				FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN);
//
//		for (int i = 0; i < featureSetTable.getRowCount(); i++) {
//
//			MsFeatureSet set = (MsFeatureSet) featureSetTable.getValueAt(i, column);
//			addSetListeners(set);
//		}
//		activeSet.fireFeatureSetEvent(ParameterSetStatus.CHANGED);
//	}

//	private void removeSelectedFeatures() {
//
//		Collection<MsFeature> selected = 
//				featureSubsetDialog.getSelectedFeatures();
//		if (selected.isEmpty())
//			return;
//
//		int approve = MessageDialog.showChoiceMsg(
//				"Remove selected features from the subset?",
//				this.getContentPane());
//
//		if (approve == JOptionPane.YES_OPTION) {
//
//			MsFeatureSet setToEdit = featureSubsetDialog.getActiveSet();
//			setToEdit.removeFeatures(selected);
//		}
//	}

//	private void removeSetListeners(MsFeatureSet newSet) {
//		newSet.removeAllListeners();
//	}

	private void showExportDialog() {
		// TODO Auto-generated method stub

	}

	private void showFeatureSetEditor() {
		
		MsFeatureSet setToEdit = featureSetTable.getSelectedMsFeatureSet();		
		if (setToEdit == null) 
			return;

		if (setToEdit.isLocked()) {
			MessageDialog
					.showErrorMsg("Feature subset '" + setToEdit.getName() + 
							"' is locked and can not be edited!", this.getContentPane());
			return;
		}
		featureSubsetDialog = new FeatureSubsetDialog(setToEdit, this);		
		featureSubsetDialog.setLocationRelativeTo(this.getContentPane());
		featureSubsetDialog.setVisible(true);			
	}

	private void showNewFeatureSetDialog() {

		featureSubsetDialog = new FeatureSubsetDialog(null, this);
		featureSubsetDialog.setLocationRelativeTo(this.getContentPane());
		featureSubsetDialog.setVisible(true);
	}
	
	private void createNewFeatureSubset() {

		Collection<String>errors = 
				featureSubsetDialog.validateFeatureSubsetData();
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), featureSubsetDialog);
			return;
		}
		MsFeatureSet newSet = 
				new MsFeatureSet(featureSubsetDialog.getSubsetName());
		
		if (featureSubsetDialog.getFeaturesSelectionToAdd() != null) {
			FeatureDataPanel panel = 
					(FeatureDataPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA);
			newSet.addFeatures(panel.getFeatures(
					featureSubsetDialog.getFeaturesSelectionToAdd()));
			
			addSetListeners(newSet);
			currentExperiment.addFeatureSetForDataPipeline(newSet, activeDataPipeline);
			currentExperiment.setActiveFeatureSetForDataPipeline(newSet, activeDataPipeline);		
			featureSubsetDialog.dispose();
			return;
		}
		if(featureSubsetDialog.getSelectedSourceSubset() != null) {			
			newSet.addFeatures(featureSubsetDialog.getSelectedSourceSubset().getFeatures());
			addSetListeners(newSet);
			currentExperiment.addFeatureSetForDataPipeline(newSet, activeDataPipeline);
			currentExperiment.setActiveFeatureSetForDataPipeline(newSet, activeDataPipeline);		
			featureSubsetDialog.dispose();
			return;
		}
		if(featureSubsetDialog.getFeatureListFile() != null) {
			
			Collection<MsFeature>listFeatures = new HashSet<MsFeature>();
			FilterFeaturesByListTask task = 
					new FilterFeaturesByListTask(
							newSet, featureSubsetDialog.getFeatureListFile(), listFeatures);
			IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
					"Reading feature list ...", this.getContentPane(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
//			
//			if(listFeatures == null || listFeatures.isEmpty()){
//				
//				MessageDialog.showErrorMsg(
//						"No features matching to provided list found in current data set", 
//						featureSubsetDialog);
//				return;
//			}
//			newSet.addFeatures(listFeatures);
		}
	}
	
	class FilterFeaturesByListTask extends LongUpdateTask {
		
		private MsFeatureSet newSet;
		private File featureListFile;
		private Collection<MsFeature>features;
			
		public FilterFeaturesByListTask(
				MsFeatureSet newSet,
				File featureListFile, 
				Collection<MsFeature>features) {
			super();
			this.newSet = newSet;
			this.featureListFile = featureListFile;
			this.features = features;
		}

		@Override
		public Void doInBackground() {
			
			try {
				getFeaturesFromExternalList(featureListFile, features);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	    @Override
	    public void done() {
	    	
	    	super.done();
			if(features == null || features.isEmpty()){
				
				MessageDialog.showErrorMsg(
						"No features matching to provided list found in current data set", 
						featureSubsetDialog);
				return;
			}
			newSet.addFeatures(features);
			addSetListeners(newSet);
			currentExperiment.addFeatureSetForDataPipeline(newSet, activeDataPipeline);
			currentExperiment.setActiveFeatureSetForDataPipeline(newSet, activeDataPipeline);		
			featureSubsetDialog.dispose();
	    }
	}

	private void getFeaturesFromExternalList(
			File featureListFile, Collection<MsFeature>features) throws Exception{

		String[][]featureListData =  null;
		try {
			featureListData = DelimitedTextParser.parseTextFileWithEncoding(
					featureListFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collection<String>featureIds = new TreeSet<String>();
		for(int i=0; i<featureListData.length; i++) {
			
			if(featureListData[i].length >0) {
				
				String fid = featureListData[i][0].trim();
				if(fid != null && !fid.isEmpty())
					featureIds.add(fid);
			}
		}
		Map<String, MsFeature> completeMap = 
				currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline).stream().
			collect(Collectors.toMap(MsFeature::getName, Function.identity()));
		
		for(String fid : featureIds) {

			MsFeature match = completeMap.get(fid);
			if(match != null)
				features.add(match);
		}
	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {

		MsFeatureSet featureSet = (MsFeatureSet) e.getSource();
		ParameterSetStatus status = e.getStatus();

		if (status.equals(ParameterSetStatus.CHANGED)) {

//			featureSetTable.setModelFromProject(
//					currentExperiment, activeDataPipeline);
		}
		else if (status.equals(ParameterSetStatus.CREATED)) {

			if(!activeSet.equals(featureSet)) {
				activeSet = featureSet;
				currentExperiment.setActiveFeatureSetForDataPipeline(activeSet, activeDataPipeline);
			}
		}
		else if (status.equals(ParameterSetStatus.DELETED)) {

			if (activeSet.equals(featureSet))
				activateDefaultSet();
		}
		else if (status.equals(ParameterSetStatus.ENABLED)) {

			if(!activeSet.equals(featureSet)) {
				
				activeSet = featureSet;
				currentExperiment.setActiveFeatureSetForDataPipeline(activeSet, activeDataPipeline);
			}
		}
		//switchDataPipeline(currentExperiment, activeDataPipeline);
		featureSetTable.setModelFromProject(
					currentExperiment, activeDataPipeline);		
	}

//	private void unlinkFromFeatureDataPanel() {
//
//		int column = featureSetTable.getColumnIndex(
//				FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN);
//
//		for (int i = 0; i < featureSetTable.getRowCount(); i++) {
//
//			MsFeatureSet set = (MsFeatureSet) featureSetTable.getValueAt(i, column);
//			removeSetListeners(set);
//		}
//	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newDataPipeline);
		if(currentExperiment == null || activeDataPipeline == null)
			return;
		
		featureSetTable.setModelFromProject(
				currentExperiment, activeDataPipeline);
		activeSet = null;
		if (activeDataPipeline != null) {

			for (MsFeatureSet set : currentExperiment.getMsFeatureSetsForDataPipeline(activeDataPipeline))
				addSetListeners(set);

			activeSet = currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline);
		}
		toolbar.updateGuiFromExperimentAndDataPipeline(
				currentExperiment, activeDataPipeline);
	}
	
	@Override
	public void reloadDesign() {
		switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void closeExperiment() {

		super.closeExperiment();
		clearPanel();
		toolbar.updateGuiFromExperimentAndDataPipeline(
				currentExperiment, activeDataPipeline);
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
