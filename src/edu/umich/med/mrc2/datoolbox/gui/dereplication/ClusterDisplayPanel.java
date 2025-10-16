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

package edu.umich.med.mrc2.datoolbox.gui.dereplication;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.annotation.DockableObjectAnnotationPanel;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.ClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.DockableClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.FilterTreeDialog;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.ClusterFeatureSelectionTableModel;
import edu.umich.med.mrc2.datoolbox.gui.fdata.DockableFeatureIntensitiesTable;
import edu.umich.med.mrc2.datoolbox.gui.fdata.corr.DockableCorrelationDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtable.DockableUniversalIdentificationResultsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DockableDataPlot;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public abstract class ClusterDisplayPanel extends DockableMRC2ToolboxPanel
	implements TreeSelectionListener, ListSelectionListener, ItemListener{

	protected DockableClusterTree clusterTree;
	protected DockableDataPlot dataPlot;
	protected DockableFeatureIntensitiesTable featureIntensitiesTable;
	protected DockableSpectumPlot spectrumPlot;
	protected DockableMsTable spectrumTable;
	protected DockableUniversalIdentificationResultsTable idTable;
	protected DockableMolStructurePanel molStructurePanel;
	protected DockableObjectAnnotationPanel featureAnnotationPanel;
	protected DockableCorrelationDataPanel correlationPanel;
	protected FilterTreeDialog filterTreeDialog;
	protected MsFeatureCluster activeCluster;
	protected ListSelectionListener[] featureSelectionSource;
	protected BasicFeatureTable featureDataTable;
	
	protected static final Icon filterIcon = GuiUtils.getIcon("filterClusters", 24);
	protected static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);

	public ClusterDisplayPanel(String id, String title, Icon icon) {

		super(id, title, icon);
		setLayout(new BorderLayout(0, 0));

		activeCluster = null;

		dataPlot = new DockableDataPlot(
				"ClusterDisplayPanelDockableDataPlot", "Data plots");
		featureIntensitiesTable = new DockableFeatureIntensitiesTable(
				"ClusterDisplayPanelDockableFeatureIntensitiesTable", "Signal intensity table");
		spectrumPlot = new DockableSpectumPlot(
				"ClusterDisplayPanelDockableSpectumPlot", "Spectrum plot");
		correlationPanel = new DockableCorrelationDataPanel(
				"ClusterDisplayPanelCorrelationDataPanel", "Feature correlation");
		spectrumTable = new DockableMsTable(
				"ClusterDisplayPanelDockableMsTableMS1", "MS1 table");
		idTable = new DockableUniversalIdentificationResultsTable(
				"ClusterDisplayPanelDockableIdentificationResultsTable", "Identifications");
		molStructurePanel = new DockableMolStructurePanel(
				"ClusterDisplayPanelDockableMolStructurePanel");
		featureAnnotationPanel = new DockableObjectAnnotationPanel(
				"ClusterDisplayPanelAnnotations", "Annotations", 80);
	}

	protected abstract void createPanelLayout();

	protected void finalizeLayout() {

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(event);
		
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.EXPAND_CLUSTERS_COMMAND.getName()))
			toggleClusterTree(true);

		if (command.equals(MainActionCommands.COLLAPSE_CLUSTERS_COMMAND.getName()))
			toggleClusterTree(false);

		if (command.equals(MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND.getName()))
			showFilterTreeDialog();

		if (command.equals(MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName()))
			searchSelectedFeatureAgainstLibrary();

		if (command.equals(MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName()))
			searchSelectedFeatureAgainstDatabase();

		if (command.equals(MainActionCommands.EDIT_FEATURE_METADATA_COMMAND.getName()))
			editFeatureMetaData();

		if (command.equals(MainActionCommands.DELETE_CLUSTER_COMMAND.getName()))
			deleteCluster();

		if (command.equals(MainActionCommands.SHOW_ALL_CLUSTER_SPECTRA_COMMAND.getName()))
			showCompleteMsForCluster(activeCluster);
	}
	
	private void showFilterTreeDialog() {
		
		filterTreeDialog = new FilterTreeDialog(this);
		filterTreeDialog.setLocationRelativeTo(this.getContentPane());
		filterTreeDialog.setVisible(true);
	}

	private void showCompleteMsForCluster(MsFeatureCluster activeCluster2) {
		spectrumPlot.showMsForFeatureList(activeCluster.getFeatures());
	}

	public void clearClusterDataPanel() {

		if(featureDataTable != null) {
			featureDataTable.getSelectionModel().removeListSelectionListener(this);
			featureDataTable.clearTable();
			featureDataTable.getSelectionModel().addListSelectionListener(this);
		}
		dataPlot.clearPlotPanel();
		correlationPanel.clearPanel();
		featureIntensitiesTable.clearTable();
		spectrumPlot.removeAllDataSets();
		spectrumTable.clearTable();
		idTable.clearTable();
		molStructurePanel.clearPanel();
		featureAnnotationPanel.clearPanel();
	}

	@Override
	public synchronized void clearPanel() {

		Runnable swingCode = new Runnable() {

			public void run() {
				clearClusterDataPanel();
				clusterTree.getTree().removeTreeSelectionListener(ClusterDisplayPanel.this);
				clusterTree.resetTree();
				clusterTree.getTree().addTreeSelectionListener(ClusterDisplayPanel.this);
				//	toolbar.updateGuiFromActiveSet(null);
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void clearSelection() {

		Runnable swingCode = new Runnable() {

			public void run() {
				featureDataTable.clearSelection();
				clearClusterDataPanel();
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void toggleClusterTree(boolean expand) {

		if(expand)
			clusterTree.expandTree();
		else
			clusterTree.collapseTree();
	}

	protected abstract void deleteCluster();

	protected void editFeatureMetaData() {

		int featureCol = featureDataTable.getColumnIndex(
				ClusterFeatureSelectionTableModel.FEATURE_COLUMN);
		MsFeature feature = (MsFeature) 
				featureDataTable.getValueAt(featureDataTable.getPopupRow(), featureCol);
		feature.addListener(this);

		if (feature != null) {

			feature.addListener(this);
			//	TODO write new functionality

//			MainWindow.getFeatureDataEditor().loadFeatureData(feature);
//			MainWindow.getFeatureDataEditor().setVisible(true);
		}
	}

	protected void filterClusterTree(Collection<MsFeatureCluster> clusters) {

		HashSet<MsFeatureCluster> filtered = new HashSet<MsFeatureCluster>();
		boolean append;
		boolean multIdOnly = filterTreeDialog.multIdOnly();
		String featureName = filterTreeDialog.getFeatureNameSubstring();
		Pattern nameMatch = null;

		if (featureName != null)
			nameMatch = Pattern.compile(Pattern.quote(featureName), Pattern.CASE_INSENSITIVE);

		Range rtRange = filterTreeDialog.getRtRange();
		Range mzRange = filterTreeDialog.getMzRange();

		for (MsFeatureCluster cluster : clusters) {

			for (MsFeature feature : cluster.getFeatures()) {

				append = true;

				if (nameMatch != null && !nameMatch.matcher(feature.getName()).find())
					append = false;

				if (rtRange != null && !rtRange.contains(feature.getRetentionTime()))
					append = false;

				if (mzRange != null && !mzRange.contains(feature.getMonoisotopicMz()))
					append = false;

				if (multIdOnly && cluster.getNumberOfNamed() <= 1)
					append = false;

				if (append) {

					filtered.add(cluster);
					break;
				}
			}
		}
		if (filtered.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No clusters found matching all the criteria.");
		}
		else {
			clearPanel();
			loadFeatureClusters(filtered);
			if (featureName != null)
				toggleClusterTree(true);
		}
		filterTreeDialog.setVisible(false);
	}

	public void findClusterByFeature(MsFeature selectedFeature) {

		TreePath clusterPath = clusterTree.findfeaturePath(selectedFeature);

		if (clusterPath == null)
			MessageDialog.showWarningMsg("Feature " + selectedFeature.getName() + 
					" not found in any cluster");
		else {
			clusterTree.highlightPath(clusterPath);
			MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.CORRELATIONS);
		}
	}

	public void loadFeatureClusters(Collection<MsFeatureCluster> clusterList) {


		clearClusterDataPanel();
		if(clusterList != null) {

			clusterTree.loadFeatureClusters(clusterList);
			clusterTree.expandClusterBranch();
			clusterTree.treeExpanded(false);
		}
	}

	public void refreshClusterData() {

		clearClusterDataPanel();

		if(activeCluster != null)
			showClusterData(activeCluster);
	}

	protected void removeSelectedFeaturesFromCluster() {

		Collection<MsFeature>selected = featureDataTable.getSelectedFeatures();

		// Do not allow to remove all features
		if (selected.size() == activeCluster.getFeatures().size()) {

			MessageDialog.showErrorMsg(
					"You are trying to remove all features from the cluster!\n"
					+ "Please use one of the \"Delete cluster ...\" "
					+ "options in the cluster tree contextual menu instead.");
			return;
		}
		// Do not allow single feature left in cluster
		if (selected.size() == activeCluster.getFeatures().size() - 1) {

			MessageDialog.showErrorMsg(
					"You are trying to leave a single feature in the cluster!\n"
					+ "Please use either \"Delete cluster ...\" or \"Dissolve cluster\" option\n"
					+ "in the cluster tree contextual menu instead.");
			return;
		}
		if (!selected.isEmpty()) {

			int approve = MessageDialog.showChoiceMsg(
					"Remove selected feature(s) from cluster?\n"
					+ "(Removed feature(s) still WILL be included in the report)\n"
					+ "(NO UNDO!)");

			if (approve == JOptionPane.YES_OPTION) {

				for (MsFeature f : selected) {

					activeCluster.removeFeature(f);
					clusterTree.removeFeature(f);
				}
				clusterTree.updateElement(activeCluster);
				showClusterData(activeCluster);
			}
		}
	}

	protected void resetClusterTreeFilter(Collection<MsFeatureCluster> clusters) {

		clearPanel();
		loadFeatureClusters(clusters);
	}

	protected void resortTree() {

		Runnable swingCode = new Runnable() {

			public void run() {
				clusterTree.resortTree();
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void searchSelectedFeatureAgainstDatabase() {
		// TODO Auto-generated method stub

	}

	protected void searchSelectedFeatureAgainstLibrary() {
		// TODO Auto-generated method stub

	}

	public void selectFeatures(Collection<MsFeature> selected) {

		clearClusterDataPanel();
		showClusterData(activeCluster);
		ListSelectionModel model = featureDataTable.getSelectionModel();
		model.clearSelection();
		int row;
		model.setValueIsAdjusting(true);

		for (MsFeature f : selected) {

			row = featureDataTable.getFeatureRow(f);
			if (row > -1)
				model.addSelectionInterval(row, row);
		}
		model.setValueIsAdjusting(false);
	}

	public void showClusterData(MsFeatureCluster selectedCluster) {

		if(selectedCluster == null)
			return;
		
		featureDataTable.setTableModelFromFeatureCluster(selectedCluster);
		showFeatureData(selectedCluster.getFeatureMap());
	}

	protected void showFeatureData(Map<DataPipeline,Collection<MsFeature>>featureMap) {

		if(featureMap.isEmpty())
			return;

		dataPlot.loadMultipleFeatureData(featureMap);		
		featureIntensitiesTable.setTableModelFromFeatureMap(featureMap);
		List<MsFeature>allFeatures =  featureMap.values().
				stream().flatMap(c -> c.stream()).collect(Collectors.toList());
		
		MsFeature firstSelected = allFeatures.get(0);	
		spectrumPlot.showMsForFeatureList(allFeatures);
		spectrumTable.setTableModelFromMsFeature(firstSelected);
		idTable.setModelFromMsFeature(firstSelected);		
		featureAnnotationPanel.loadFeatureData(firstSelected);
		if (allFeatures.size() > 1) {
			MsFeature fOne = allFeatures.get(0);
			MsFeature fTwo = allFeatures.get(1);
			DataPipeline dpOne = null;
			DataPipeline dpTwo = null;
			for (Entry<DataPipeline, Collection<MsFeature>> entry : featureMap.entrySet()) {
				
				if(entry.getValue().contains(fOne))
					dpOne = entry.getKey();
				
				if(entry.getValue().contains(fTwo))
					dpTwo = entry.getKey();
			}			
			correlationPanel.createCorrelationPlot(fOne, dpOne, fTwo, dpTwo);
		}
		if(!firstSelected.isIdentified()) 
			return;

		try {
			molStructurePanel.showStructure(
					firstSelected.getPrimaryIdentity().
					getCompoundIdentity().getSmiles());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {

		if (e.getStatus() != null) {

			if (e.getStatus().equals(ParameterSetStatus.CHANGED)) {

				((BasicTableModel) 
						featureDataTable.getModel()).fireTableDataChanged();

				if (e.getSource().equals(featureAnnotationPanel.getCurrentAnnotatedObject()))
					featureAnnotationPanel.loadFeatureData(e.getSource());

				e.getSource().setStatus(null);
			}
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}

	@Override
	public abstract void valueChanged(ListSelectionEvent e);

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		clearClusterDataPanel();

		if(e.getSource() instanceof ClusterTree) {

			ClusterTree tree = (ClusterTree)e.getSource();

			if (tree.getClickedObject() instanceof MsFeatureCluster)
				activeCluster = (MsFeatureCluster) tree.getClickedObject();

			if (tree.getClickedObject() instanceof MsFeature)
				activeCluster = (MsFeatureCluster) tree.getClusterForSelectedFeature();
		}
	}

	@Override
	public void switchDataPipeline(
			DataAnalysisProject project, DataPipeline newPipeline) {

		super.switchDataPipeline(project, newPipeline);
		clearPanel();
	}
}
































