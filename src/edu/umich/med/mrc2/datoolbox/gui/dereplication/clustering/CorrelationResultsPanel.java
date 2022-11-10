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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.ClusteringMode;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.enums.PrimaryFeatureSelectionType;
import edu.umich.med.mrc2.datoolbox.data.enums.SlidingWindowUnit;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.DockableClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.adductinterpret.AdductInterpreterDialog;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.mzdiff.MassDifferenceExplorerDialog;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.ClusterVisFrame;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.AdductAssignmentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.BinnerClustersImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.ClusterCorrelationMatrixTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.FeatureClusteringTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.MassDifferenceAssignmentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.SlidingWindowClusteringTask;

public class CorrelationResultsPanel extends ClusterDisplayPanel implements ChartMouseListener {

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "CorrelationResultsPanel.layout");
	
	private static final Icon componentIcon = 
			GuiUtils.getIcon("recalculateCorrelations", 16);
	private static final Icon importBinnerDataIcon = GuiUtils.getIcon("importBins", 24);
	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 24);
	private static final Icon exploreDeltasIcon = GuiUtils.getIcon("exploreDeltas", 24);
	private static final Icon removeUnexplainedFromClustersIcon = GuiUtils.getIcon("clearUnassigned", 24);
	private static final Icon rejectUnexplainedIcon = GuiUtils.getIcon("rejectUnexplained", 24);
	private static final Icon molIonOnlyIcon = GuiUtils.getIcon("molIon", 24);
	private static final Icon restoreAllIcon = GuiUtils.getIcon("markAll", 24);
	private static final Icon annotationIcon = GuiUtils.getIcon("msAnnotation", 24);
	private static final Icon deltaAnnotationIcon = GuiUtils.getIcon("assignDeltas", 24);
	private static final Icon batchAssignAnnotationsIcon = GuiUtils.getIcon("calculateAnnotation", 24);
	private static final Icon recalculateCorrelationsIcon = GuiUtils.getIcon("recalculateCorrelations", 24);
	
	private ClusteringMode clusteringMode;
	private ClusterVisFrame heatMapFrame;
	private AdductInterpreterDialog adductInterpreterDialog;
	private BatchAnnotationPreferencesDialog batchAnnotationPreferencesDialog;
	private ClusteringParametersDialog clusteringParametersDialog;
	private DockableMassDifferenceTable massDifferenceTable;
	private DockableClusterFeatureSelectionTable clusterFeatureSelectionTable;
	private MassDifferenceExplorerDialog mdExplorerDialog;
	private BinnerDataImportDialog binnerDataImportDialog;

	public CorrelationResultsPanel() {

		super("CorrelationResultsPanelClusterDisplayPanel", PanelList.CORRELATIONS.getName(), componentIcon);

		clusterFeatureSelectionTable = new DockableClusterFeatureSelectionTable(this);
		featureDataTable = clusterFeatureSelectionTable.getTable();
		massDifferenceTable = new DockableMassDifferenceTable(this);

		heatMapFrame = new ClusterVisFrame(this);
		adductInterpreterDialog = new AdductInterpreterDialog();
		batchAnnotationPreferencesDialog = new BatchAnnotationPreferencesDialog(this);
		clusteringParametersDialog = new ClusteringParametersDialog(this);

		createPanelLayout();
		finalizeLayout();
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		
		mdExplorerDialog = new MassDifferenceExplorerDialog();
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND.getName(),
				MainActionCommands.SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND.getName(), 
				recalculateCorrelationsIcon, this));
		
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
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName(),
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName(), 
				annotationIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ANNOTATE_MASS_DIFFERENCES_COMMAND.getName(),
				MainActionCommands.ANNOTATE_MASS_DIFFERENCES_COMMAND.getName(), 
				deltaAnnotationIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND.getName(),
				MainActionCommands.SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND.getName(), 
				batchAssignAnnotationsIcon, this));
		
		menuActions.addSeparator();		

		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(),
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(), 
				importBinnerDataIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND.getName(),
				MainActionCommands.EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND.getName(), 
				exploreDeltasIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REMOVE_UNEXPLAINED_FEATURES_COMMAND.getName(),
				MainActionCommands.REMOVE_UNEXPLAINED_FEATURES_COMMAND.getName(), 
				removeUnexplainedFromClustersIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REJECT_UNEXPLAINED_FEATURES_COMMAND.getName(),
				MainActionCommands.REJECT_UNEXPLAINED_FEATURES_COMMAND.getName(), 
				rejectUnexplainedIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REJECT_ALL_BUT_MOLION_COMMAND.getName(),
				MainActionCommands.REJECT_ALL_BUT_MOLION_COMMAND.getName(), 
				molIonOnlyIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.RESTORE_ALL_REJECTED_FEATURES_COMMAND.getName(),
				MainActionCommands.RESTORE_ALL_REJECTED_FEATURES_COMMAND.getName(), 
				restoreAllIcon, this));
		
		menuActions.addSeparator();	

		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_HEATMAP_COMMAND.getName(),
				MainActionCommands.SHOW_HEATMAP_COMMAND.getName(), 
				heatmapIcon, this));
	}
	
	@Override
	protected void createPanelLayout() {

		menuBar = new CorrelationPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		clusterTree = new DockableClusterTree("CorrelationResultsPanelDockableClusterTree", "Feature clusters", this, this);
		clusterTree.getTree().setFeaturePopupMenu(new CorrelationClusterFeaturePopupMenu(this));

		grid.add(0, 0, 80, 30, clusterFeatureSelectionTable, massDifferenceTable);
		grid.add(80, 0, 20, 30, molStructurePanel);
		grid.add(0, 30, 100, 20, idTable);
		grid.add(0, 50, 50, 50, dataPlot, featureIntensitiesTable, correlationPanel);
		grid.add(50, 50, 50, 50, spectrumPlot, spectrumTable, featureAnnotationPanel);
		grid.add(-25, 0, 25, 100, clusterTree);
		grid.select(-25, 0, 25, 100, clusterTree);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if(currentProject == null || activeDataPipeline == null)
			return;
		
		super.actionPerformed(event);
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SHOW_HEATMAP_COMMAND.getName()))
			showHeatmap();

		if (command.equals(MainActionCommands.FIND_FEATURE_CORRELATIONS_COMMAND.getName()))
			runCorrelationAnalysis();

		if (command.equals(MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName()))
			showBinnerResultsImportDialog();

		if (command.equals(MainActionCommands.IMPORT_BINNER_DATA_COMMAND.getName()))
			importBinnerClusters();

		if (command.equals(MainActionCommands.EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND.getName())) {
			mdExplorerDialog.loadPreferences();
			mdExplorerDialog.setLocationRelativeTo(this.getContentPane());
			mdExplorerDialog.setVisible(true);
		}
		if (command.equals(MainActionCommands.SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND.getName()))
			setUpCorrelationAnalysis();

		if (command.equals(MainActionCommands.RACALCULATE_CLUSTER_CORR_MATRIX_COMMAND.getName()))
			racalculateClusterCorrMatrixes();

		if (command.equals(MainActionCommands.FILTER_CLUSTERS_COMMAND.getName()))
			filterClusterTree(currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline));

		if (command.equals(MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName()))
			resetClusterTreeFilter(currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline));

		if (command.equals(MainActionCommands.REJECT_UNEXPLAINED_FEATURES_COMMAND.getName()))
			rejectUnexplainedFeatures();

		if (command.equals(MainActionCommands.REJECT_ALL_BUT_MOLION_COMMAND.getName()))
			rejectAllButMolionFeatures();

		if (command.equals(MainActionCommands.RESTORE_ALL_REJECTED_FEATURES_COMMAND.getName()))
			restoreRejectedFeatures();

		if (command.equals(MainActionCommands.ADD_ACTIVE_FEATURES_TO_SUBSET_COMMAND.getName()))
			addActiveFeaturesToSubset();

		if (command.equals(MainActionCommands.REMOVE_UNEXPLAINED_FEATURES_COMMAND.getName()))
			removeUnexplainedFeaturesFromClusters();

		if (command.equals(MainActionCommands.ASSIGN_FEATURE_ANNOTATION_COMMAND.getName()))
			assignFeatureAnnotation();

		if (command.equals(MainActionCommands.NEW_CLUSTER_FROM_SELECTED_COMMAND.getName()))
			createNewClusterFromSelectedFeatures();

		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName()))
			removeSelectedFeaturesFromCluster();

		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_AND_LIST_COMMAND.getName()))
			removeSelectedFeaturesFromClusterAndList();

		if (command.equals(MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName())) {

			if (activeCluster != null)
				showAdductInterpreter(activeCluster);
		}

		if (command.equals(MainActionCommands.ANNOTATE_MASS_DIFFERENCES_COMMAND.getName()))
			annotateMassDifferences();

		if (command.equals(MainActionCommands.SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND.getName()))
			showBatchAnnotationPreferences();

		if (command.equals(MainActionCommands.BATCH_ANNOTATE_CLUSTERS_COMMAND.getName()))
			batchAnnotateClusterFeatures();

		if (command.equals(MainActionCommands.DISSOLVE_CLUSTER_COMMAND.getName()))
			dissolveCluster();

		if (command.equals(MainActionCommands.RECALCULATE_CORRRELATIONS_4CLUSTER_COMMAND.getName())) {

			activeCluster.setClusterCorrMatrix(activeCluster.createClusterCorrelationMatrix(false));
			showClusterData(activeCluster);
		}
	}

	private void showBinnerResultsImportDialog() {
		binnerDataImportDialog = new BinnerDataImportDialog(this);
		binnerDataImportDialog.setLocationRelativeTo(this.getContentPane());
		binnerDataImportDialog.setVisible(true);
	}


	private void importBinnerClusters() {

		File binnerDataFile = binnerDataImportDialog.getBinnerReportFile();
		File postprocessorDataFile = binnerDataImportDialog.getPostProcessorReportFile();

		if(binnerDataFile == null && postprocessorDataFile == null)
			return;

		clearPanel();
		BinnerClustersImportTask ibct = new BinnerClustersImportTask(binnerDataFile, postprocessorDataFile);
		ibct.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(ibct);
		binnerDataImportDialog.dispose();
	}

	private boolean panelStateValid() {

		if (currentProject == null)
			return false;

		if (activeDataPipeline == null)
			return false;

		if (!currentProject.dataPipelineHasData(activeDataPipeline))
			return false;

		if (!currentProject.statsCalculetedForDataPipeline(activeDataPipeline)) {

			MessageDialog.showWarningMsg("Please calculate descriptive statistics first!");
			return false;
		}
		return true;
	}

	private void showBatchAnnotationPreferences() {

		if (panelStateValid())
			batchAnnotationPreferencesDialog.setVisible(true);
	}

	private void setUpCorrelationAnalysis() {

		if (!panelStateValid())
			return;

		if (currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline) != null) {

			if (MessageDialog.showChoiceMsg("Existing results will be erased, do you want to proceed?",
					this.getContentPane()) == JOptionPane.YES_OPTION) {

				clusteringParametersDialog.loadPreferences();
				clusteringParametersDialog.setLocationRelativeTo(this.getContentPane());
				clusteringParametersDialog.setVisible(true);
				return;
			}
		}
		clusteringParametersDialog.loadPreferences();
		clusteringParametersDialog.setLocationRelativeTo(this.getContentPane());
		clusteringParametersDialog.setVisible(true);
	}

	private ArrayList<String> validateClusteringSetupInput() {

		ArrayList<String> errors = new ArrayList<String>();

		if (clusteringParametersDialog.getCorrelationCutoff() < 0.0d
				|| clusteringParametersDialog.getCorrelationCutoff() > 1.0d)
			errors.add("Invalid correlation cutoff, should be between 0 and 1.");

		if (clusteringParametersDialog.limitRtRange() && clusteringParametersDialog.getRetentionRange() == null)
			errors.add("Invalid retention time range.");

		if (clusteringParametersDialog.filterMissing() && (clusteringParametersDialog.getMaxMissingPercent() < 0
				|| clusteringParametersDialog.getMaxMissingPercent() > 100))
			errors.add("Invalid missing % cutoff, should be between 0 and 100.");

		if (clusteringParametersDialog.getMaxClusterWidth() <= 0.0d)
			errors.add("Minimal cluster width should be greatet than 0.");

		if (clusteringParametersDialog.getWindowSlidingUnit().equals(SlidingWindowUnit.Seconds)
				&& clusteringParametersDialog.getFeatureTimeWindow() <= 0.0d)
			errors.add("Window width should be greatet than 0.");

		return errors;
	}

	private void runCorrelationAnalysis() {

		ArrayList<String> errors = validateClusteringSetupInput();

		if (validateClusteringSetupInput().isEmpty()) {

			clusteringParametersDialog.savePreferences();
			clusteringParametersDialog.setVisible(false);
			clearPanel();

			Task createCorrClustersTask = new SlidingWindowClusteringTask(currentProject, activeDataPipeline,
					clusteringParametersDialog.limitRtRange(), clusteringParametersDialog.getRetentionRange(),
					clusteringParametersDialog.filterMissing(), clusteringParametersDialog.getMaxMissingPercent(),
					clusteringParametersDialog.imputeMissing(), clusteringParametersDialog.getImputationMethod(),
					clusteringParametersDialog.getKnnClusterNumber(),
					clusteringParametersDialog.getCorrelationAlgoritmh(),
					clusteringParametersDialog.getCorrelationCutoff(), clusteringParametersDialog.getMaxClusterWidth(),
					clusteringParametersDialog.getWindowSlidingUnit(),
					clusteringParametersDialog.getFeatureNumberWindow(),
					clusteringParametersDialog.getFeatureTimeWindow());

			createCorrClustersTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(createCorrClustersTask);
		} else {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), clusteringParametersDialog);
			return;
		}
	}

	private void addActiveFeaturesToSubset() {

		MsFeatureSet features = 
				currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline);
		if (features.isLocked()) {

			MessageDialog.showErrorMsg("Feature subset '" + features.getName()
					+ "' is locked and can not be modified!\n" + 
					"Please select or create another feature set.");
			return;
		} else {
			currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline).
				stream().flatMap(c -> c.getActiveFeatures().stream()).
				forEach(f -> features.addFeature(f));

			MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().
				setActiveFeatureSubset(features);
		}
	}

	private void annotateMassDifferences() {

		double massAccuracy = MRC2ToolBoxConfiguration.getMassAccuracy();
		MassDifferenceAssignmentTask mdaTask = new MassDifferenceAssignmentTask(massAccuracy);
		mdaTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(mdaTask);
	}

	private void assignFeatureAnnotation() {

		int featureCol = featureDataTable.getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN);
		MsFeature f = (MsFeature) featureDataTable.getValueAt(featureDataTable.getPopupRow(), featureCol);
		MessageDialog.showInfoMsg(f.getName());
	}

	private void batchAnnotateClusterFeatures() {

		batchAnnotationPreferencesDialog.setVisible(false);
		double massError = batchAnnotationPreferencesDialog.getMassError();
		PrimaryFeatureSelectionType type = batchAnnotationPreferencesDialog.getPrimaryFeatureSelectionType();
		int maxClusterSize = batchAnnotationPreferencesDialog.getMaxAnnotatedClusterSize();

		boolean generateAdducts = batchAnnotationPreferencesDialog.autoGenerateAdducts();
		int maxAdductCharge = batchAnnotationPreferencesDialog.getMaxChargeForGeneratedAdducts();
		int maxOligomer = batchAnnotationPreferencesDialog.getMaxOligomerForGeneratedAdducts();

		AdductAssignmentTask aatask = new AdductAssignmentTask(type, massError, maxClusterSize, generateAdducts,
				maxAdductCharge, maxOligomer);
		aatask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(aatask);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {

/*		try {
			XYItemEntity entity = (XYItemEntity) event.getEntity();
			MsFeature[] labels = ((CorrelationMapDataSet) entity.getDataset()).getLabels()[entity.getItem()];
			MsFeature fOne, fTwo;

			for (int i = 0; i < massDifferenceTable.getRowCount(); i++) {

				fOne = (MsFeature) massDifferenceTable.getValueAt(i,
						massDifferenceTable.getColumnIndex(MassDifferenceTableModel.FEATURE_ONE_COLUMN));
				fTwo = (MsFeature) massDifferenceTable.getValueAt(i,
						massDifferenceTable.getColumnIndex(MassDifferenceTableModel.FEATURE_TWO_COLUMN));

				if (fOne.equals(labels[0]) && fTwo.equals(labels[1])) {

					massDifferenceTable.getSelectionModel().addSelectionInterval(i, i);
					break;
				}
			}
			tablesTabPane.setSelectedComponent(massDiffsTableScrollPane);
			featureDataPanel.createCorrelationPlot(labels[0], labels[1]);
			featureDataPanel.showCorrelationPlot();
		} catch (Exception e) {

		}*/
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearClusterDataPanel() {

		super.clearClusterDataPanel();
		massDifferenceTable.clearTable();
	}

	@Override
	public synchronized void clearPanel() {

		clusterTree.resetTree();
		clearClusterDataPanel();
	}

	@Override
	public void clearSelection() {

		super.clearSelection();

		massDifferenceTable.setTableModelFromFeatureCluster(activeCluster);
	}

	private void createNewClusterFromSelectedFeatures() {

		if(activeCluster.getFeatures().size() <= 2 ||
				featureDataTable.getSelectedRows().length >= activeCluster.getFeatures().size() - 1) {

			MessageDialog.showErrorMsg(
					"You can't remove selected features from the cluster,\n "
					+ "it has to contain at least 2 features!",
					this.getContentPane());
			return;
		}
		Map<DataPipeline, Collection<MsFeature>> fMap = 
				featureDataTable.getSelectedFeaturesMap();
		if (fMap.size() > 0) {

			MsFeatureCluster newCluster = new MsFeatureCluster();
			fMap.forEach((dataPipeline,featureCollection) -> {
				featureCollection.stream().forEach(f -> {
					activeCluster.removeFeature(f);
					newCluster.addFeature(f, dataPipeline);
				});
			});
			newCluster.setClusterCorrMatrix(newCluster.createClusterCorrelationMatrix(false));
			currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline).add(newCluster);
			clusterTree.getModel().addObject(newCluster);
			clusterTree.resortTree();
			clusterTree.selectFeatureCluster(newCluster);
		}
	}

	protected void deleteCluster() {

		if (clusterTree.getSelectedClusters().length > 0) {

			clearClusterDataPanel();
			heatMapFrame.clearPanel();
			adductInterpreterDialog.clearPanel();
			MsFeatureCluster[] selected = clusterTree.getSelectedClusters();
			MsFeatureSet activeSet = 
					currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline);

			if (selected.length > 0) {

				String message = "Selected cluster will be deleted, "
						+ "its component features will remain part of the project";

				if (!activeSet.isLocked())
					message = message + "\nbut they will be removed from the \"" + 
							activeSet.getName() + "\" feature set";

				message = message + "\nDo you want to proceed?";

				int selectedValue = 
						MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
				if (selectedValue == JOptionPane.YES_OPTION) {

					for (MsFeatureCluster c : selected) {

						if (!activeSet.isLocked())
							activeSet.removeFeatures(c.getFeatures());

						currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline).remove(c);
						clusterTree.removeFeatureCluster(c);
					}
					activeSet.fireFeatureSetEvent(ParameterSetStatus.CHANGED);
				}
			}
		}
	}

	private void dissolveCluster() {

		MsFeatureCluster[] selected = clusterTree.getSelectedClusters();
		if (selected.length > 0) {

			String message = 
					"Selected cluster will be deleted, but its component features\n"
					+ "will remain part of the project and all custom feature sets\n"
					+ "Do you want to proceed?";
		
			int selectedValue = 
					MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
			if (selectedValue == JOptionPane.YES_OPTION) {

				for (MsFeatureCluster c : selected) {

					currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline).remove(c);
					clusterTree.removeFeatureCluster(c);
				}
				clearClusterDataPanel();
				heatMapFrame.clearPanel();
				adductInterpreterDialog.clearPanel();
			}
		}
	}

	public ClusteringMode getClusteringMode() {

		return clusteringMode;
	}

	private void racalculateClusterCorrMatrixes() {

		ClusterCorrelationMatrixTask task = new ClusterCorrelationMatrixTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void rejectAllButMolionFeatures() {

		Set<MsFeatureCluster> clusters = 
				currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline);

		for (MsFeatureCluster cluster : clusters) {

			for (MsFeature f : cluster.getFeatures()) {

				if (!f.equals(cluster.getPrimaryFeature()))
					f.setActive(false);
			}
		}
		clearPanel();
		loadFeatureClusters(clusters);
	}

	private void rejectUnexplainedFeatures() {

		Set<MsFeatureCluster> clusters = 
				currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline);
		for (MsFeatureCluster cluster : clusters) {

			for (MsFeature f : cluster.getFeatures()) {

				if (f.getBinnerAnnotation() == null)
					f.setActive(false);
			}
		}
		clearPanel();
		loadFeatureClusters(clusters);
	}

	private void removeSelectedFeaturesFromClusterAndList() {

		Collection<MsFeature> selected = featureDataTable.getSelectedFeatures();

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

			int result = MessageDialog.showChoiceMsg(
					"You are trying to leave a single feature in the cluster.\n"
					+ "The cluster will be removed and all selecte dfeatures will also be removed\n from the \""
					+ GlobalDefaults.BINNER_CLUSTERED_FEATURES + "\" data set.");

			if (result == JOptionPane.YES_OPTION) {

				this.clearClusterDataPanel();
				clusterTree.removeFeatureCluster(activeCluster);
				currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline).remove(activeCluster);
				MsFeatureSet activeSet = currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline);

				if (!activeSet.isLocked())
					activeSet.removeFeatures(selected);

				MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().
					switchDataPipeline(currentProject, activeDataPipeline);
			}
			return;
		}
		if (!selected.isEmpty()) {

			int approve = MessageDialog
					.showChoiceMsg("Remove selected feature(s) from cluster AND custom feature list?\n"
							+ "(Removed feature(s) WILL NOT be included in the report)\n(NO UNDO!)");

			if (approve == JOptionPane.YES_OPTION) {

				for (MsFeature f : selected) {
					activeCluster.removeFeature(f);
					clusterTree.removeFeature(f);
				}
				MsFeatureSet activeSet = 
						currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline);

				if (!activeSet.isLocked())
					activeSet.removeFeatures(selected);

				showClusterData(activeCluster);
				clusterTree.updateElement(activeCluster);
				MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().
					switchDataPipeline(currentProject, activeDataPipeline);
			}
		}
	}

	private void removeUnexplainedFeaturesFromClusters() {

		int approve = MessageDialog.showChoiceMsg(
				"Remove all unexplained features from clusters?" + "All features will remain in the list.\n"
						+ "CLusters with a single explained feature will be removed from the tree.\n(NO UNDO!)");

		if (approve == JOptionPane.YES_OPTION) {

			Set<MsFeatureCluster> clusters = 
					currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline);

			for (MsFeatureCluster cluster : clusters) {

				for (MsFeature feature : cluster.getFeatures()) {

					if (feature.getBinnerAnnotation() == null)
						cluster.removeFeature(feature);
				}
			}
			clearPanel();
			loadFeatureClusters(clusters);
		}
	}

	private void restoreRejectedFeatures() {

		currentProject = MRC2ToolBoxCore.getCurrentProject();
		Set<MsFeatureCluster> clusters = currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline);
		clusters.stream().forEach(c -> c.enableAllFeatures());
		clearPanel();
		loadFeatureClusters(clusters);
	}

	private File selectBinnerDataFile() {
		
		JnaFileChooser fc = new JnaFileChooser(
				MRC2ToolBoxConfiguration.getDefaultProjectsDirectory());
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Excel files", "xlsx", "XLSX");
		fc.setTitle("Select Binner results file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane())))			
			return fc.getSelectedFile();
		
		return null;
						
//		JFileChooser chooser = new ImprovedFileChooser();
//		File inputFile = null;
//
//		File projectsDir = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory());
//		chooser.setCurrentDirectory(projectsDir);
//
//		chooser.setDialogTitle("Select Binner results file");
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		FileNameExtensionFilter projectFileFilter = new FileNameExtensionFilter("Excel files", "xlsx");
//		chooser.setFileFilter(projectFileFilter);
//
//		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION)
//			inputFile = chooser.getSelectedFile();
//
//		return inputFile;
	}

	@Override
	public void selectFeatures(Collection<MsFeature> selectedFeatures) {

		super.selectFeatures(selectedFeatures);
		massDifferenceTable.setTableModelFromFeatures(selectedFeatures, activeCluster);
	}

	public void selectFeaturesGlobally(Collection<MsFeature> selectedFeatures) {

		selectFeatures(selectedFeatures);
		adductInterpreterDialog.highlightFeatures(selectedFeatures);
	}

	public void showAdductInterpreter(MsFeatureCluster cluster) {

		if (adductInterpreterDialog == null)
			adductInterpreterDialog = new AdductInterpreterDialog();

		adductInterpreterDialog.loadCluster(cluster);
		adductInterpreterDialog.setVisible(true);
	}

	@Override
	public void showClusterData(MsFeatureCluster selectedCluster) {

		super.showClusterData(selectedCluster);

		massDifferenceTable.setTableModelFromFeatureCluster(selectedCluster);

		if (selectedCluster.getFeatures().size() < 100)
			showCorrelationMatrix(selectedCluster);
		else
			heatMapFrame.clearPanel();

		adductInterpreterDialog.loadCluster(selectedCluster);
	}

	public void showCorrelationMatrix(MsFeatureCluster cluster) {

		heatMapFrame.showCorrelationData(cluster);
	}

	private void showHeatmap() {

		if (heatMapFrame.isVisible())
			heatMapFrame.setVisible(false);
		else
			heatMapFrame.setVisible(true);
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {

		super.msFeatureStatusChanged(e);

		if (e.getStatus() != null) {

			if (e.getStatus().equals(ParameterSetStatus.CHANGED))
				((MassDifferenceTableModel) massDifferenceTable.getTable().getModel()).fireTableDataChanged();
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		super.statusChanged(e);

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			// Correlation analysis
			if (e.getSource().getClass().equals(FeatureClusteringTask.class)) {

				FeatureClusteringTask eTask = (FeatureClusteringTask) e.getSource();

				if (!eTask.getFeatureClusters().isEmpty()) {

					clusteringMode = ClusteringMode.INTERNAL;
					currentProject.setFeatureClustersForDataPipeline(activeDataPipeline, eTask.getFeatureClusters());
					currentProject.setCorrelationMatrixForDataPipeline(activeDataPipeline, eTask.getCorrMatrix());
					loadFeatureClusters(currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline));
					MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.CORRELATIONS);
				} else {
					MessageDialog.showInfoMsg("No correlation clusters found using current settings", this.getContentPane());
				}
			}
			// Binner data import
			if (e.getSource().getClass().equals(BinnerClustersImportTask.class)) {

				BinnerClustersImportTask eTask = (BinnerClustersImportTask) e.getSource();

				if (!eTask.getFeatureClusters().isEmpty()) {

					clusteringMode = ClusteringMode.BINNER;
					currentProject.setFeatureClustersForDataPipeline(activeDataPipeline, eTask.getFeatureClusters());
					loadFeatureClusters(currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline));
					MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.CORRELATIONS);
					MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().switchDataPipeline(currentProject, activeDataPipeline);

					if (!eTask.getUnassignedFeatures().isEmpty()) {

						@SuppressWarnings("unused")
						InformationDialog id = new InformationDialog(
								"Unmatched features",
								"Not all binned features were matched to the project features.\n"
								+ "Below is the list of unmatched features.",
								StringUtils.join(eTask.getUnassignedFeatures(), "\n"),
								this.getContentPane());
					}
				} else {
					MessageDialog.showInfoMsg("No correlation clusters found using current settings", this.getContentPane());
				}
			}
			if (e.getSource().getClass().equals(MassDifferenceAssignmentTask.class)
					|| e.getSource().getClass().equals(AdductAssignmentTask.class)) {

				MessageDialog.showInfoMsg("Mass differences has been analyzed");
				clearClusterDataPanel();
				resortTree();
			}
			if (e.getSource().getClass().equals(ClusterCorrelationMatrixTask.class)) {

				MessageDialog.showInfoMsg(
						"Correlation matrixes for all clusters have been re-calculated", 
						this.getContentPane());
				clearClusterDataPanel();
				resortTree();
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

					if (l instanceof MassDifferenceTable)
						showFeatureData(((MassDifferenceTable)l).getSelectedFeaturesMap());
				}
			}
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {

		if(currentProject == null || activeDataPipeline == null)
			return;

		super.valueChanged(event);
		showClusterData(activeCluster);

		if (clusterTree.getSelectedFeatures().size() > 0)
			selectFeatures(clusterTree.getSelectedFeatures());
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newAssay) {

		clearPanel();
		super.switchDataPipeline(project, newAssay);
//		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		if (currentProject != null && activeDataPipeline != null) {
			Set<MsFeatureCluster> clusterList = 
					currentProject.getMsFeatureClustersForDataPipeline(activeDataPipeline);
			if (clusterList != null)
				loadFeatureClusters(clusterList);
		}
	}

	@Override
	public void closeProject() {

		super.closeProject();
		clearPanel();
//		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
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

	@Override
	public void loadLayout(File layoutFile) {

		super.loadLayout(layoutFile);
		if(mdExplorerDialog != null)
			mdExplorerDialog.loadLayout(MassDifferenceExplorerDialog.getLayoutconfigfile());
	}

	@Override
	public void saveLayout(File layoutFile) {

		super.saveLayout(layoutFile);
		if(mdExplorerDialog != null)
			mdExplorerDialog.saveLayout(MassDifferenceExplorerDialog.getLayoutconfigfile());
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
}
