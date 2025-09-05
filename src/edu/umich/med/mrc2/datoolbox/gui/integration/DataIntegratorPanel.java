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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.DockableClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.integration.dpalign.DataSetAlignmentManager;
import edu.umich.med.mrc2.datoolbox.gui.integration.dpalign.DataSetAlignmentSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration.CreateMergedFeaturesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration.DataPipelineAlignmentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration.IdentifiedFeatureIntegrationTask;

public class DataIntegratorPanel extends ClusterDisplayPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("createIntegration", 16);
	private static final Icon collectIDDataIcon = GuiUtils.getIcon("createIntegration", 24);
	private static final Icon deleteDataSetIcon = GuiUtils.getIcon("deleteIntegration", 24);
	private static final Icon acceptListIcon = GuiUtils.getIcon("acceptList", 24);

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "DataIntegratorPanel.layout");
	
	private DataIntegrationSetupDialog dataIntegrationSetupDialog;
	private String dataSetName;
	private MsFeatureClusterSet integratedSet;
	private DataPipelineAlignmentResults alignmentResults;
	
	private DockableDataIntegrationFeatureSelectionTable featureSelectionTable;
	private DockableAlignedDataSetSummaryPanel alignedDataSetSummaryPanel;
	private DataSetAlignmentSetupDialog dataSetAlignmentSetupDialog;
	private DataSetAlignmentManager dataSetAlignmentManager;
	
	public static final String DATA_INTEGRATOR_FEATURE_SET = "DATA_INTEGRATOR_FEATURE_SET";
	private MsFeatureSet activeMsFeatureSet;

	public DataIntegratorPanel() {

		super("DataIntegratorPanelClusterDisplayPanel", PanelList.INTEGRATION.getName(), componentIcon);

		featureSelectionTable = new DockableDataIntegrationFeatureSelectionTable(this);
		featureDataTable = featureSelectionTable.getTable();
		alignedDataSetSummaryPanel = new DockableAlignedDataSetSummaryPanel(this);
		
		dataIntegrationSetupDialog = new DataIntegrationSetupDialog(this);

		createPanelLayout();
		finalizeLayout();
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		activeMsFeatureSet = new MsFeatureSet(DATA_INTEGRATOR_FEATURE_SET);
	}

	@Override
	protected void createPanelLayout() {

		menuBar = new DataIntegrationPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);
		clusterTree = new DockableClusterTree("DataIntegratorPanelDockableClusterTree", "Feature clusters", this, this);
		clusterTree.getTree().setFeaturePopupMenu(new IntegrationFeaturePopupMenu(this));

		grid.add(0, 0, 80, 30, featureSelectionTable, alignedDataSetSummaryPanel);
		grid.add(80, 0, 20, 30, molStructurePanel);
		grid.add(0, 30, 100, 20, idTable);
		grid.add(0, 50, 50, 50, dataPlot, featureIntensitiesTable, correlationPanel);
		grid.add(50, 50, 50, 50, spectrumPlot, spectrumTable, featureAnnotationPanel);

		grid.add(-25, 0, 25, 100, clusterTree);
		grid.select(-25, 0, 25, 100, clusterTree);
	}

	@Override
	protected void initActions() {
			
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND.getName(),
				MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND.getName(), 
				collectIDDataIcon, this));

		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND.getName(),
				MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND.getName(), 
				acceptListIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_INTEGRATION_SET_COMMAND.getName(),
				MainActionCommands.DELETE_INTEGRATION_SET_COMMAND.getName(), 
				deleteDataSetIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName(), 
				filterIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(),
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(), 
				resetFilterIcon, this));		
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		super.actionPerformed(event);

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.COLLECT_IDENTIFIED_CPD_COMMAND.getName()))
			collectIdentifiedCompoundData();

		if (command.equals(MainActionCommands.DELETE_INTEGRATION_SET_COMMAND.getName()))
			dleleteActiveIntegrationSet();

		if (command.equals(MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND.getName())) {

			if (integratedSet != null)
				dataIntegrationSetupDialog.setDataSetName(integratedSet.getName());

			dataIntegrationSetupDialog.setLocationRelativeTo(this.getContentPane());
			dataIntegrationSetupDialog.setVisible(true);
		}

		if (command.equals(MainActionCommands.FILTER_CLUSTERS_COMMAND.getName())  
				&& integratedSet != null)
			filterClusterTree(integratedSet.getClusters());

		if (command.equals(MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName()) 
				&& integratedSet != null)
			resetClusterTreeFilter(integratedSet.getClusters());
		
		if (command.equals(MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND.getName()))
			acceptIntegratedCompoundList();

		if (command.equals(MainActionCommands.NEW_CLUSTER_FROM_SELECTED_COMMAND.getName()))
			createNewClusterFromSelectedFeatures();

		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName()))
			removeSelectedFeaturesFromCluster();
		
		if (command.equals(MainActionCommands.DATA_SET_ALIGNMENT_SETUP_COMMAND.getName()))
			setupDataSetAlignment();
			
		if (command.equals(MainActionCommands.DATA_SET_ALIGNMENT_RUN_COMMAND.getName()))
			alignDataSets();
		
		if (command.equals(MainActionCommands.SHOW_DATA_PIPELINE_ALIGNMENT_MANAGER_COMMAND.getName()))
			showDataAlignmentResultsManager();
		if (command.equals(MainActionCommands.DELETE_DATA_PIPELINE_ALIGNMENT_RESULTS_COMMAND.getName()))
			deleteSelectedDataAlignmentResults();
		
		if (command.equals(MainActionCommands.LOAD_DATA_PIPELINE_ALIGNMENT_RESULTS_COMMAND.getName()))
			loadSelectedDataAlignmentResults();
		
		if (command.equals(MainActionCommands.SHOW_UNMATCHED_FEATURES_FROM_REFERENCE_DATASET_COMMAND.getName()))
			showUnmatchedFeaturesFromRefLibInAlignedDataSet();
		
		if (command.equals(MainActionCommands.MERGE_DATA_FOR_MARKED_FEATURES_COMMAND.getName()))
			mergeDataForMarkedFeatures();
		
		if (command.equals(MainActionCommands.SHOW_DATASET_ALIGNMENT_SUMMARY_COMMAND.getName()))
			showDatasetAlignmentSummary();
		
		if (command.equals(MainActionCommands.EXPORT_DATASET_ALIGNMENT_SUMMARY_COMMAND.getName()))
			exportDatasetAlignmentSummary();
		
	}
	
	private void showDatasetAlignmentSummary() {
		
		if(alignmentResults == null)
			return;
		
		//	TODO
	}
	
	private void exportDatasetAlignmentSummary() {
		
		if(alignmentResults == null)
			return;
		
		//	TODO
	}
	
	private void mergeDataForMarkedFeatures() {
		
		if(alignmentResults == null)
			return;
		
		List<MsFeatureCluster> clustersWithMergeInput =  
				alignmentResults.getClusters().stream().
					filter(c -> c.getMarkedForMerge().size() > 1).
					collect(Collectors.toList());
		if(clustersWithMergeInput.isEmpty()) {
			MessageDialog.showWarningMsg(
					"None of the clusters contain multiple features marked for merging", 
					this.getContentPane());
			return;
		}		
		CreateMergedFeaturesTask task = new CreateMergedFeaturesTask(
				currentExperiment, 
				alignmentResults,
				alignmentResults.getAlignmentSettings().getMassWindow(), 
				alignmentResults.getAlignmentSettings().getMassErrorType());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private void showUnmatchedFeaturesFromRefLibInAlignedDataSet() {
		
		if(alignmentResults == null)
			return;

		Collection<LibraryMsFeature> unmatchedFeatures = 
				alignmentResults.getUnmatchedReferenceFeatures();
		if(unmatchedFeatures.isEmpty()) {
			MessageDialog.showWarningMsg("No unmatched features found", this.getContentPane());
			return;
		}
		Map<DataPipeline,Collection<MsFeature>>unmatchedMap = 
				new HashMap<DataPipeline,Collection<MsFeature>>();
		unmatchedMap.put(
				alignmentResults.getAlignmentSettings().getReferencePipeline(), 
				alignmentResults.getUnmatchedReferenceFeaturesAsMsFetures());
		alignedDataSetSummaryPanel.setTableModelFromFeatureMap(unmatchedMap);
	}
	
	private void deleteSelectedDataAlignmentResults() {
		
		DataPipelineAlignmentResults results = null;
		if(dataSetAlignmentManager.isVisible() 
				&& dataSetAlignmentManager.getSelectedDataPipelineAlignmentResults() != null) {
			results = dataSetAlignmentManager.getSelectedDataPipelineAlignmentResults();
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to delete selected data set alignment?", this.getContentPane());
			if(res == JOptionPane.YES_OPTION) {
				deleteDataAlignmentResultsFromProject(results);
				dataSetAlignmentManager.dispose();
				return;
			}			
		}
		if(!dataSetAlignmentManager.isVisible() && alignmentResults != null) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to delete currently displayed data set alignment?", 
					this.getContentPane());
			if(res == JOptionPane.YES_OPTION)
				deleteDataAlignmentResultsFromProject(alignmentResults);
		}
	}
	
	private void deleteDataAlignmentResultsFromProject(DataPipelineAlignmentResults results) {
		currentExperiment.deleteDataPipelineAlignmentResult(results);
		if(results.equals(alignmentResults)) {
			clearPanel();
			alignmentResults = null;
		}
	}

	private void showDataAlignmentResultsManager() {
		// TODO Auto-generated method stub
		if(currentExperiment.getDataPipelineAlignmentResults().isEmpty())
			return;
		
		dataSetAlignmentManager =
				new DataSetAlignmentManager(this,currentExperiment);
		dataSetAlignmentManager.setLocationRelativeTo(this.getContentPane());
		dataSetAlignmentManager.setVisible(true);		
	}

	private void loadSelectedDataAlignmentResults() {
		
		DataPipelineAlignmentResults results = 
				dataSetAlignmentManager.getSelectedDataPipelineAlignmentResults();
		if(results != null) {
			loadDataPipelineAlignmentResults(results);		
			dataSetAlignmentManager.dispose();
		}
	}

	private void setupDataSetAlignment() {;

		if(currentExperiment == null || activeDataPipeline == null)
			return;
		
		Collection<DataPipeline>pipelinesForAcqMethod = 
				currentExperiment.getPipelinesForDataAcquisitionMethod(
						activeDataPipeline.getAcquisitionMethod());
		
		if(pipelinesForAcqMethod.size() < 2) {
			MessageDialog.showWarningMsg(
					"You need at least 2 data pipleines\n"
					+ "for the same data acquisition method\n"
					+ "to align th data sets", this.getContentPane());
			return;
		}
		dataSetAlignmentSetupDialog = 
				new DataSetAlignmentSetupDialog(pipelinesForAcqMethod, this);
		dataSetAlignmentSetupDialog.setLocationRelativeTo(this.getContentPane());
		dataSetAlignmentSetupDialog.setVisible(true);
	}
	
	private void alignDataSets() {
		
		Collection<String> errors = dataSetAlignmentSetupDialog.validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), dataSetAlignmentSetupDialog);
		    return;
		}
		DataPipelineAlignmentTask task = new DataPipelineAlignmentTask(
				currentExperiment,
				dataSetAlignmentSetupDialog.getSelectedDataPipelines().get(0), 
				dataSetAlignmentSetupDialog.getSelectedDataPipelines().get(1), 
				dataSetAlignmentSetupDialog.getMassWindow(),
				dataSetAlignmentSetupDialog.getMassErrorType(), 
				dataSetAlignmentSetupDialog.getRetentionWindow(),
				dataSetAlignmentSetupDialog.excludeUndetected());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		dataSetAlignmentSetupDialog.dispose();
	}

	private void createNewClusterFromSelectedFeatures() {

		// Do not allow to remove all features
		if (featureDataTable.getSelectedRows().length == activeCluster.getFeatures().size()) {
			MessageDialog.showErrorMsg("You are trying to move all features to the new cluster!");
			return;
		}
		if (featureDataTable.getSelectedRows().length > 0) {
			
			MsFeatureCluster newCluster = new MsFeatureCluster();
			for (Entry<DataPipeline, Collection<MsFeature>> entry : featureDataTable.getSelectedFeaturesMap().entrySet()) {
				
				for(MsFeature cf : entry.getValue()) {
					activeCluster.removeFeature(cf);
					newCluster.addFeature(cf, entry.getKey());
				}
			}
			newCluster.setCorrelationMatrix(newCluster.createCorrelationMatrix(false));
			integratedSet.addCluster(newCluster);
			clusterTree.getModel().addObject(newCluster);
			clusterTree.resortTree();
			clusterTree.selectFeatureCluster(newCluster);
		}
	}

	private void dleleteActiveIntegrationSet() {

		if (integratedSet != null) {

			MRC2ToolBoxCore.getActiveMetabolomicsExperiment().deleteDataIntegrationSet(integratedSet);
			integratedSet = null;
			clearPanel();
			//	TODO find new place for this functionality?
			//	toolbar.updateGuiFromActiveSet(null);
		}
	}

	private void collectIdentifiedCompoundData() {

		Collection<DataPipeline> selectedDataPipelines = 
				dataIntegrationSetupDialog.getSelectedDataPipelines();
		dataSetName = dataIntegrationSetupDialog.getDataSetName();
		if (integratedSet == null)
			integratedSet = new MsFeatureClusterSet(dataSetName);

		ArrayList<String> warnings = new ArrayList<String>();

		if (selectedDataPipelines.isEmpty())
			warnings.add("Please select at least one assay method!");

		if (dataSetName.isEmpty())
			warnings.add("Please define a name for the dataset!");

		MsFeatureClusterSet nm = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
					getDataIntegrationSets().stream().
					filter(s -> s.getName().equalsIgnoreCase(dataSetName)).
					findFirst().orElse(null);

		if (nm != null && !nm.equals(integratedSet))
				warnings.add("Integrated set with name \"" + dataSetName + "\" alredy exists!");
		else
			integratedSet = new MsFeatureClusterSet(dataSetName);

		if (!warnings.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(warnings, "\n"));
			return;
		}
		dataIntegrationSetupDialog.setVisible(false);
		IdentifiedFeatureIntegrationTask task = 
				new IdentifiedFeatureIntegrationTask(selectedDataPipelines, integratedSet);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void clearIdentifiedCompoundData() {

		if (MessageDialog.showChoiceMsg("Do you want to clear data intergation results?",
				this.getContentPane()) == JOptionPane.YES_OPTION) {

			if (integratedSet != null)
				integratedSet.clearClusters();

			clusterTree.resetTree();
			clearClusterDataPanel();
		}
	}

	private void acceptIntegratedCompoundList() {

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		if (integratedSet.getClusters().isEmpty())
			MessageDialog.showErrorMsg("No identified compounds found.", this.getContentPane());

		integratedSet.setActive(true);
		currentExperiment.addDataIntegrationSet(integratedSet);
//		TODO find new place for this functionality?
//		toolbar.updateGuiFromActiveSet(integratedSet);
		// MessageDialogue.showInfoMsg(dataSetName + " set as integrated identified data
		// list", this);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		super.statusChanged(e);

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(IdentifiedFeatureIntegrationTask.class))
				finalizeIdentifiedFeatureIntegrationTask((IdentifiedFeatureIntegrationTask) e.getSource());
			
			if (e.getSource().getClass().equals(DataPipelineAlignmentTask.class))
				finalizeDataPipelineAlignmentTask((DataPipelineAlignmentTask) e.getSource());
			
			if (e.getSource().getClass().equals(CreateMergedFeaturesTask.class))
				finalizeCreateMergedFeaturesTask((CreateMergedFeaturesTask) e.getSource());
		}
	}
	
	private void finalizeCreateMergedFeaturesTask(CreateMergedFeaturesTask task) {

		Collection<MsFeatureCluster> updated = task.getUpdatedFeatureClusters();
		if(!updated.isEmpty())
			loadFeatureClusters(alignmentResults.getClusters());		
		else 
			MessageDialog.showWarningMsg("No features merged", this.getContentPane());		
	}

	private void finalizeDataPipelineAlignmentTask(DataPipelineAlignmentTask task) {
		
		DataPipelineAlignmentResults res = task.getAlignmentResults();
		res.setActive(true);
		currentExperiment.addDataPipelineAlignmentResult(res);
		loadDataPipelineAlignmentResults(res);
	}
	
	public void loadDataPipelineAlignmentResults(DataPipelineAlignmentResults results) {
		
		alignmentResults = results;
		loadFeatureClusters(alignmentResults.getClusters());
	}

	private synchronized void finalizeIdentifiedFeatureIntegrationTask(IdentifiedFeatureIntegrationTask task) {
		
		integratedSet = task.getIdClusterSet();
		
//		TODO find new place for this functionality?
//		toolbar.updateGuiFromActiveSet(integratedSet);
		// currentProject.addIntegratedFeatureClusterSet(integratedSet);

		loadFeatureClusters(integratedSet.getClusters());
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.INTEGRATION);

		if (integratedSet.getClusters().isEmpty())
			MessageDialog.showInfoMsg("No identified feature clusters found using current settings");
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			featureSelectionSource = null;

			if (e.getSource() instanceof DefaultListSelectionModel) {

				featureSelectionSource = ((DefaultListSelectionModel) e.getSource()).getListSelectionListeners();

				for (ListSelectionListener l : featureSelectionSource) {

					if (l instanceof BasicFeatureTable)
						showFeatureData(((BasicFeatureTable)l).getSelectedFeaturesMap());
				}
			}
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		super.valueChanged(e);
		showClusterData(activeCluster);

		if (!clusterTree.getSelectedFeatures().isEmpty())
			selectFeatures(clusterTree.getSelectedFeatures());
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		//	super.switchDataPipeline(project, newDataPipeline);
		currentExperiment = project;
		activeDataPipeline = newDataPipeline;
		if (currentExperiment == null) {
			clearPanel();
			return;
		}
//		TODO find new place for this functionality?
//		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
//		TODO find new place for this functionality?
//		toolbar.updateGuiFromActiveSet(integratedSet);
		
		if (integratedSet != null) 			
			loadFeatureClusters(integratedSet.getClusters());
		else if(alignmentResults != null) 
			loadFeatureClusters(alignmentResults.getClusters());
		else
			clearPanel();
	}	

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void closeExperiment() {
		
		clearPanel();
		super.closeExperiment();
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
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof MsFeatureClusterSet) {

			integratedSet = (MsFeatureClusterSet) e.getItem();
			showClusterData(null);
			loadFeatureClusters(integratedSet.getClusters());
//			TODO find new place for this functionality?
//			toolbar.updateGuiFromActiveSet(integratedSet);
		}
	}

	@Override
	protected void deleteCluster() {

		if (clusterTree.getSelectedClusters().length > 0) {

			clearClusterDataPanel();
			MsFeatureCluster[] selected = clusterTree.getSelectedClusters();

			if (selected.length > 0) {

				String message = "Selected cluster will be deleted,\n"
						+ "its component features will not be available for data integration review.\n"
						+ "\nDo you want to proceed?";

				if (MessageDialog.showChoiceMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {

					for (MsFeatureCluster c : selected) {

						integratedSet.removeCluster(c);
						clusterTree.removeFeatureCluster(c);
					}
				}
			}
		}
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

	public MsFeatureSet getActiveMsFeatureSet() {
		return activeMsFeatureSet;
	}
}
