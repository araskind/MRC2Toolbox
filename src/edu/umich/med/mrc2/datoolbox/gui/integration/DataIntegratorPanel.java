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
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.DockableClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration.IdentifiedFeatureIntegrationTask;

public class DataIntegratorPanel extends ClusterDisplayPanel {

	private DataIntegrationSetupDialog dataIntegrationSetupDialog;
	private String dataSetName;
	private MsFeatureClusterSet integratedSet;
	private DockableDataIntegrationFeatureSelectionTable featureSelectionTable;

	private static final Icon componentIcon = GuiUtils.getIcon("createIntegration", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "DataIntegratorPanel.layout");

	public DataIntegratorPanel() {

		super("DataIntegratorPanelClusterDisplayPanel", PanelList.INTEGRATION.getName(), componentIcon);

		featureSelectionTable = new DockableDataIntegrationFeatureSelectionTable(this);
		featureDataTable = featureSelectionTable.getTable();
		dataIntegrationSetupDialog = new DataIntegrationSetupDialog(this);

		createPanelLayout();
		finalizeLayout();
		initActions();
		loadLayout(layoutConfigFile);
	}

	@Override
	protected void createPanelLayout() {

		toolbar = new DataIntegratorToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		clusterTree = new DockableClusterTree("DataIntegratorPanelDockableClusterTree", "Feature clusters", this, this);
		clusterTree.getTree().setFeaturePopupMenu(new IntegrationFeaturePopupMenu(this));

		grid.add(0, 0, 80, 30, featureSelectionTable);
		grid.add(80, 0, 20, 30, molStructurePanel);
		grid.add(0, 30, 100, 20, idTable);
		grid.add(0, 50, 50, 50, dataPlot, featureIntensitiesTable, correlationPanel);
		grid.add(50, 50, 50, 50, spectrumPlot, spectrumTable, featureAnnotationPanel);

		grid.add(-25, 0, 25, 100, clusterTree);
		grid.select(-25, 0, 25, 100, clusterTree);
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

		if (command.equals(MainActionCommands.FILTER_CLUSTERS_COMMAND.getName())) {

			if (integratedSet != null)
				filterClusterTree(integratedSet.getClusters());
		}

		if (command.equals(MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName())) {

			if (integratedSet != null)
				resetClusterTreeFilter(integratedSet.getClusters());
		}
		if (command.equals(MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND.getName()))
			acceptIntegratedCompoundList();

		if (command.equals(MainActionCommands.NEW_CLUSTER_FROM_SELECTED_COMMAND.getName()))
			createNewClusterFromSelectedFeatures();

		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName()))
			removeSelectedFeaturesFromCluster();
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
			newCluster.setClusterCorrMatrix(newCluster.createClusterCorrelationMatrix(false));
			integratedSet.addCluster(newCluster);
			clusterTree.getModel().addObject(newCluster);
			clusterTree.resortTree();
			clusterTree.selectFeatureCluster(newCluster);
		}
	}

	private void dleleteActiveIntegrationSet() {

		if (integratedSet != null) {

			MRC2ToolBoxCore.getCurrentProject().deleteIntegratedFeatureClusterSet(integratedSet);
			integratedSet = null;
			clearPanel();
			toolbar.updateGuiFromActiveSet(null);
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

		Optional<MsFeatureClusterSet> nm = 
				MRC2ToolBoxCore.getCurrentProject().
				getDataIntegrationClusterSets().stream()
				.filter(s -> s.getName().equalsIgnoreCase(dataSetName)).
				findFirst();

		if (nm.isPresent()) {

			if (!nm.get().equals(integratedSet))
				warnings.add("Integrated set with name \"" + 
						dataSetName + "\" alredy exists!");
		} else
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

	/*
	 * @Override public void clearClusterDataPanel() {
	 *
	 * Runnable swingCode = new Runnable() {
	 *
	 * public void run() {
	 *
	 * if(featureDataTable != null) featureDataTable.clearTable();
	 *
	 * featureDataPanel.clearPanel(); } }; try { if
	 * (SwingUtilities.isEventDispatchThread()) swingCode.run(); else
	 * SwingUtilities.invokeAndWait(swingCode); } catch (Exception e) {
	 * e.printStackTrace(); }
	 *
	 * // if(featureDataTable != null) // featureDataTable.clearTable(); // //
	 * featureDataPanel.clearPanel(); }
	 */

	private void acceptIntegratedCompoundList() {

		currentProject = MRC2ToolBoxCore.getCurrentProject();

		if (integratedSet.getClusters().isEmpty())
			MessageDialog.showErrorMsg("No identified compounds found.", this.getContentPane());

		integratedSet.setActive(true);
		currentProject.addIntegratedFeatureClusterSet(integratedSet);
		toolbar.updateGuiFromActiveSet(integratedSet);
		// MessageDialogue.showInfoMsg(dataSetName + " set as integrated identified data
		// list", this);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		super.statusChanged(e);

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(IdentifiedFeatureIntegrationTask.class)) {

				IdentifiedFeatureIntegrationTask eTask = (IdentifiedFeatureIntegrationTask) e.getSource();

				integratedSet = eTask.getIdClusterSet();
				toolbar.updateGuiFromActiveSet(integratedSet);
				// currentProject.addIntegratedFeatureClusterSet(integratedSet);

				loadFeatureClusters(integratedSet.getClusters());
				MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.INTEGRATION);

				if (integratedSet.getClusters().isEmpty())
					MessageDialog.showInfoMsg("No identified feature clusters found using current settings");
			}
		}
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

		if (clusterTree.getSelectedFeatures().size() > 0)
			selectFeatures(clusterTree.getSelectedFeatures());
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newDataPipeline);
		super.clearClusterDataPanel();
		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		if (currentProject != null) {

			integratedSet = currentProject.getActiveIntegratedFeatureSet();
			if (integratedSet != null) {
				loadFeatureClusters(integratedSet.getClusters());
				toolbar.updateGuiFromActiveSet(integratedSet);
			} else {
				clearPanel();
			}
		} 
	}	

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void closeProject() {
		// TODO Auto-generated method stub

		super.closeProject();
		clearPanel();
		toolbar.updateGuiFromProjectAndDataPipeline(null, null);
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
			toolbar.updateGuiFromActiveSet(integratedSet);
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
	protected void initActions() {
		// TODO Auto-generated method stub
		
	}
}
