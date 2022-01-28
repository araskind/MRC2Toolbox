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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.DockableClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.FindDuplicateFeaturesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.FindDuplicateNamesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.MergeDuplicateFeaturesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.CalculateStatisticsTask;

public class DuplicatesPanel extends ClusterDisplayPanel {

	private DuplicateMergeDialog duplicateMergeDialog;
	private DuplicateFindDialog duplicateFindDialog;
	private DockableDuplicateSelectionTable duplicateSelectionTable;
	private DuplicatesPanelMenuBar menuBar;

	private static final Icon componentIcon = GuiUtils.getIcon("duplicates", 16);
	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 24);
	private static final Icon exportDuplicatesIcon = GuiUtils.getIcon("saveDuplicates", 24);
	private static final Icon showDuplicatesIcon = GuiUtils.getIcon("findDuplicates", 24);
	private static final Icon checkDuplicateNamesIcon = GuiUtils.getIcon("checkDuplicateNames", 24);

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "DuplicatesPanel.layout");

	public DuplicatesPanel() {

		super("DuplicatesPanelClusterDisplayPanel", PanelList.DUPLICATES.getName(), componentIcon);

		clusterTree = new DockableClusterTree("DuplicatesPanelDockableClusterTree", "Feature clusters", this, this);
		clusterTree.getTree().setFeaturePopupMenu(new DuplicateFeaturePopupMenu(this));
		duplicateSelectionTable =  new DockableDuplicateSelectionTable(this);
		duplicateFindDialog = new DuplicateFindDialog(this);
		duplicateMergeDialog = new DuplicateMergeDialog(this);
		featureDataTable = duplicateSelectionTable.getTable();

		createPanelLayout();
		finalizeLayout();
		initActions();
		loadLayout(layoutConfigFile);
	}
	
	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_FIND_DUPLICATES_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_FIND_DUPLICATES_DIALOG_COMMAND.getName(), 
				showDuplicatesIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_DUPLICATES_MERGE_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_DUPLICATES_MERGE_DIALOG_COMMAND.getName(), 
				clearDuplicatesIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName(),
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName(), 
				checkDuplicateNamesIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName(), 
				filterIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(),
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(), 
				resetFilterIcon, this));
		
		menuActions.addSeparator();		
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_DUPLICATES_COMMAND.getName(),
				MainActionCommands.EXPORT_DUPLICATES_COMMAND.getName(), 
				exportDuplicatesIcon, this));
	}

	@Override
	protected void createPanelLayout() {

		menuBar = new DuplicatesPanelMenuBar(this);
//		toolbar = new DuplicatePanelToolbar(this);
		add(menuBar, BorderLayout.NORTH);

		grid.add(0, 0, 80, 30, duplicateSelectionTable);
		grid.add(80, 0, 20, 30, molStructurePanel);
		grid.add(0, 30, 100, 20, idTable);
		grid.add(0, 50, 50, 50, correlationPanel, featureIntensitiesTable, dataPlot);
		grid.add(50, 50, 50, 50, featureAnnotationPanel, spectrumTable, spectrumPlot);

		grid.add(-25, 0, 25, 100, clusterTree);
		grid.select(-25, 0, 25, 100, clusterTree);
	}

	public void actionPerformed(ActionEvent event) {

		super.actionPerformed(event);

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SHOW_FIND_DUPLICATES_DIALOG_COMMAND.getName())) {

			duplicateFindDialog.setLocationRelativeTo(this.getContentPane());
			duplicateFindDialog.setVisible(true);
		}

		if (command.equals(MainActionCommands.FIND_DUPLICATES_COMMAND.getName()))
			findDuplicateFeatures();

		if (command.equals(MainActionCommands.MERGE_DUPLICATES_COMMAND.getName()))
			mergeDuplicateFeatures();

		if (command.equals(MainActionCommands.SHOW_DUPLICATES_MERGE_DIALOG_COMMAND.getName())) {

			duplicateMergeDialog.setLocationRelativeTo(this.getContentPane());
			duplicateMergeDialog.setVisible(true);
		}

		if (command.equals(MainActionCommands.FILTER_CLUSTERS_COMMAND.getName()))
			filterClusters();

		if (command.equals(MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName()))
			resetClusterFilter();	

		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName()))
			removSelectedFeaturesFromCluster();
		
		if (command.equals(MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName()))
			checkForDuplicateNames();		
	}
	
	private void checkForDuplicateNames() {
		
		if(currentProject == null || activeDataPipeline == null)
			return;
			
		FindDuplicateNamesTask task = 
			new FindDuplicateNamesTask(currentProject, activeDataPipeline);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);;
	}

	private void filterClusters() {
		
		Set<MsFeatureCluster> clusters = currentProject
				.getDuplicateClustersForDataPipeline(activeDataPipeline);
		filterClusterTree(clusters);
	}
	
	private void resetClusterFilter() {
		
		Set<MsFeatureCluster> clusters = currentProject
				.getDuplicateClustersForDataPipeline(activeDataPipeline);
		resetClusterTreeFilter(clusters);
	}

	private void removSelectedFeaturesFromCluster() {

		Collection<MsFeature> features = featureDataTable.getSelectedFeatures();
		if (features.isEmpty())
			return;

		if (features.size() >= activeCluster.getFeatures().size() - 1) {

			String yesNoQuestion = "This operation is equivalent to removing "
					+ "the whole feature cluster.\n" + "Proceed?";
			if (MessageDialog.showChoiceMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {

				clearClusterDataPanel();
				currentProject.getDuplicateClustersForDataPipeline(activeDataPipeline).remove(activeCluster);
				clusterTree.removeFeatureCluster(activeCluster);
			}
		} else {
			String yesNoQuestion = "Do you want to remove selected features from cluster?";
			if (MessageDialog.showChoiceMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {

				activeCluster.removeFeatures(features);
				for (MsFeature f : features)
					clusterTree.removeFeature(f);
			}
		}
	}

	private void findDuplicateFeatures() {

		duplicateFindDialog.setVisible(false);

		if (currentProject != null) {

			if (currentProject.dataPipelineHasData(activeDataPipeline)) {

				if (!currentProject.statsCalculetedForDataPipeline(activeDataPipeline)) {

					MessageDialog.showWarningMsg("Please calculate descriptive statistics first!");
					return;
				}
				FindDuplicateFeaturesTask fdt = new FindDuplicateFeaturesTask(
						currentProject,
						activeDataPipeline, 
						duplicateFindDialog.getMassWindow(),
						duplicateFindDialog.getRetentionWindow());

				fdt.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(fdt);
			}
		}
	}

	private void mergeDuplicateFeatures() {

		if(currentProject == null || activeDataPipeline == null)
			return;

		if (currentProject.dataPipelineHasData(activeDataPipeline)
				&& currentProject.getDuplicateClustersForDataPipeline(activeDataPipeline) != null) {

			if (!currentProject.getDuplicateClustersForDataPipeline(activeDataPipeline).isEmpty()) {

				clearPanel();
				clearPanel();
				duplicateMergeDialog.setVisible(false);
				MergeDuplicateFeaturesTask ddt = 
						new MergeDuplicateFeaturesTask(
								currentProject,
								activeDataPipeline, 
								duplicateMergeDialog.getMergeOption());
				ddt.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(ddt);
			}
		}		
	}

	@Override
	public void statusChanged(TaskEvent e) {

		super.statusChanged(e);

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(FindDuplicateFeaturesTask.class))
				showDuplicateFeatureClusters((FindDuplicateFeaturesTask) e.getSource());

			if (e.getSource().getClass().equals(MergeDuplicateFeaturesTask.class))
				finalizeDuplicatesMerge();
			
			if (e.getSource().getClass().equals(FindDuplicateNamesTask.class))
				finalizeDuplicateNameSearch((FindDuplicateNamesTask) e.getSource());
		}
	}

	private void finalizeDuplicateNameSearch(FindDuplicateNamesTask task) {

		if(task.getDuplicateNameList().isEmpty()) {
			MessageDialog.showInfoMsg(
					"No duplicate feature names found.", 
					this.getContentPane());
			return;
		}
		Collection<String>dupNames = new TreeSet<String>();
		for(MsFeatureCluster cluster : task.getDuplicateNameList())		
			dupNames.add(cluster.getPrimaryFeature().getName());

		InformationDialog info = new InformationDialog(
				"Duplicate feature names", 
				"Found the following duplicate feature names",
				StringUtils.join(dupNames, "\n"),
				this.getContentPane());
	}

	private void showDuplicateFeatureClusters(FindDuplicateFeaturesTask fdt) {

		Collection<MsFeatureCluster> duplicates = fdt.getDuplicateList();
		if (duplicates.size() > 0) {

			currentProject.setDuplicateClustersForDataPipeline(activeDataPipeline, duplicates);
			loadFeatureClusters(currentProject.getDuplicateClustersForDataPipeline(activeDataPipeline));
			MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.DUPLICATES);

		} else {
			MessageDialog.showInfoMsg(
					"No duplicates found using current windows for mass and retention", 
					this.getContentPane());
		}
	}

	private void finalizeDuplicatesMerge() {

//		((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA))
//				.setTableModelFromFeatureSet(currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline));

		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
		MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().
			switchDataPipeline(currentProject, activeDataPipeline);
		
		CalculateStatisticsTask cst = 
				new CalculateStatisticsTask(currentProject, activeDataPipeline);
		cst.addTaskListener(((FeatureDataPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA)));
		MRC2ToolBoxCore.getTaskController().addTask(cst);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			featureSelectionSource = null;
			if (e.getSource() instanceof DefaultListSelectionModel) {
				featureSelectionSource = 
						((DefaultListSelectionModel) e.getSource()).getListSelectionListeners();
				for (ListSelectionListener l : featureSelectionSource) {

					if (l instanceof BasicFeatureTable)
						showFeatureData(((BasicFeatureTable)l).getSelectedFeaturesMap());
				}
			}
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {

		super.valueChanged(event);
		showClusterData(activeCluster);

		if (!clusterTree.getSelectedFeatures().isEmpty())
			selectFeatures(clusterTree.getSelectedFeatures());
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newPipeline);
//		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		if (currentProject != null && activeDataPipeline != null) {

			Set<MsFeatureCluster> clusterList = 
					currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline);
			if (clusterList != null)
				loadFeatureClusters(clusterList);
		}
	}

	@Override
	protected void deleteCluster() {

		if (clusterTree.getSelectedClusters().length > 0) {

			clearClusterDataPanel();
			MsFeatureCluster[] selected = clusterTree.getSelectedClusters();
			currentProject = MRC2ToolBoxCore.getCurrentProject();
			if (selected.length > 0) {

				String message = "Selected cluster will be deleted,\n"
						+ "its component features will not be included in duplicate removal.\n"
						+ "\nDo you want to proceed?";

				if (MessageDialog.showChoiceMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {

					for (MsFeatureCluster c : selected) {
						currentProject.getDuplicateClustersForDataPipeline(activeDataPipeline).remove(c);
						clusterTree.removeFeatureCluster(c);
					}
				}
			}
		}
	}

	@Override
	public void closeProject() {
		// TODO Auto-generated method stub
		super.closeProject();
		clearPanel();
//		toolbar.updateGuiFromProjectAndDataPipeline(null, null);
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
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}
