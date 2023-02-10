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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayToolbar;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CorrelationPanelToolbar extends ClusterDisplayToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 353649032245193732L;

	private static final Icon actionsIcon = GuiUtils.getIcon("actions", 32);
	private static final Icon clusteringIcon = GuiUtils.getIcon("cluster", 32);
	private static final Icon rerunBinningIcon = GuiUtils.getIcon("rerun", 32);
	private static final Icon importBinnerDataIcon = GuiUtils.getIcon("importBins", 32);
	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 32);
	private static final Icon exploreDeltasIcon = GuiUtils.getIcon("exploreDeltas", 32);
	private static final Icon addSelectedFeaturesToSubsetIcon = GuiUtils.getIcon("addSelectedToCollection", 32);
	private static final Icon addUnexplainedFeaturesToSubsetIcon = GuiUtils.getIcon("addInactiveToCollection", 32);
	private static final Icon removeUnexplainedFromClustersIcon = GuiUtils.getIcon("clearUnassigned", 32);
	private static final Icon rejectUnexplainedIcon = GuiUtils.getIcon("rejectUnexplained", 32);
	private static final Icon molIonOnlyIcon = GuiUtils.getIcon("molIon", 32);
	private static final Icon restoreAllIcon = GuiUtils.getIcon("markAll", 32);
	private static final Icon annotationIcon = GuiUtils.getIcon("msAnnotation", 32);
	private static final Icon deltaAnnotationIcon = GuiUtils.getIcon("assignDeltas", 32);
	private static final Icon batchAssignAnnotationsIcon = GuiUtils.getIcon("calculateAnnotation", 32);
	private static final Icon recalculateCorrelationsIcon = GuiUtils.getIcon("recalculateCorrelations", 32);

	@SuppressWarnings("unused")
	private JButton
		runCorrelationAnalysisButton,
		exploreDeltasButton,
		rerunBinningButton,
		heatMapButton,
		importBinnerDataButton,
		rejectUnexplainedButton,
		molIonOnlyButton,
		restoreAllButton,
		addActiveFeaturesToSubset,
		addUnassignedFeaturesToSubsetButton,
		removeUnexplainedFromClustersButton,
		createAnnotationsButton,
		annotateMassDifferencesButton,
		batchAnnotateMassDifferencesButton,
		actionsButton;

	private JPopupMenu actions;

	@SuppressWarnings("unused")
	private JMenuItem
		createAnnotationsMenuItem,
		annotateMassDifferencesMenuItem,
		batchAnnotateMassDifferencesMenuItem,
		rejectUnexplainedMenuItem,
		molIonOnlyMenuItem,
		restoreAllMenuItem,
		addActiveFeaturesToSubsetMenuItem,
		addUnassignedFeaturesToSubsetMenuItem,
		removeUnexplainedFromClustersMenuItem;

	public CorrelationPanelToolbar(ActionListener commandListener) {

		super(commandListener);

		runCorrelationAnalysisButton = GuiUtils.addButton(this, null, recalculateCorrelationsIcon, commandListener,
				MainActionCommands.SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND.getName(),
				MainActionCommands.SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND.getName(),
				buttonDimension);

		importBinnerDataButton = GuiUtils.addButton(this, null, importBinnerDataIcon, commandListener,
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(),
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(),
				buttonDimension);

		exploreDeltasButton = GuiUtils.addButton(this, null, exploreDeltasIcon, commandListener,
				MainActionCommands.EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND.getName(),
				MainActionCommands.EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		actions = new JPopupMenu("Actions");

		createAnnotationsMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName(), commandListener,
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName(),
				annotationIcon);

		annotateMassDifferencesMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.ANNOTATE_MASS_DIFFERENCES_COMMAND.getName(), commandListener,
				MainActionCommands.ANNOTATE_MASS_DIFFERENCES_COMMAND.getName(),
				deltaAnnotationIcon);

		batchAnnotateMassDifferencesMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND.getName(), commandListener,
				MainActionCommands.SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND.getName(),
				batchAssignAnnotationsIcon);

		actions.addSeparator();

		rejectUnexplainedMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.REMOVE_UNEXPLAINED_FEATURES_COMMAND.getName(), commandListener,
				MainActionCommands.REMOVE_UNEXPLAINED_FEATURES_COMMAND.getName(),
				removeUnexplainedFromClustersIcon);

		removeUnexplainedFromClustersMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.REJECT_UNEXPLAINED_FEATURES_COMMAND.getName(), commandListener,
				MainActionCommands.REJECT_UNEXPLAINED_FEATURES_COMMAND.getName(),
				rejectUnexplainedIcon);

		molIonOnlyMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.REJECT_ALL_BUT_MOLION_COMMAND.getName(), commandListener,
				MainActionCommands.REJECT_ALL_BUT_MOLION_COMMAND.getName(),
				molIonOnlyIcon);

		restoreAllMenuItem = GuiUtils.addMenuItem(actions,
				MainActionCommands.RESTORE_ALL_REJECTED_FEATURES_COMMAND.getName(), commandListener,
				MainActionCommands.RESTORE_ALL_REJECTED_FEATURES_COMMAND.getName(),
				restoreAllIcon);

		actionsButton = GuiUtils.addButton(this, "Actions", actionsIcon, null, null, null, new Dimension(105, 35));
		actionsButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				actions.show(e.getComponent(), e.getX(), e.getY());
			}
		});

		addSeparator(buttonDimension);

		heatMapButton = GuiUtils.addButton(this, null, heatmapIcon, commandListener,
				MainActionCommands.SHOW_HEATMAP_COMMAND.getName(),
				MainActionCommands.SHOW_HEATMAP_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project != null && newDataPipeline != null) {

			if(project.getMsFeatureClustersForDataPipeline(newDataPipeline) != null) {

				createAnnotationsMenuItem.setEnabled(true);
				annotateMassDifferencesMenuItem.setEnabled(true);
				batchAnnotateMassDifferencesMenuItem.setEnabled(true);
				rejectUnexplainedMenuItem.setEnabled(true);
				molIonOnlyMenuItem.setEnabled(true);
				restoreAllMenuItem.setEnabled(true);
				removeUnexplainedFromClustersMenuItem.setEnabled(true);

				//addActiveFeaturesToSubsetMenuItem.setEnabled(true);
				//addUnassignedFeaturesToSubsetMenuItem.setEnabled(true);
			}
		}
		else {
			createAnnotationsMenuItem.setEnabled(false);
			annotateMassDifferencesMenuItem.setEnabled(false);
			batchAnnotateMassDifferencesMenuItem.setEnabled(false);
			rejectUnexplainedMenuItem.setEnabled(false);
			molIonOnlyMenuItem.setEnabled(false);
			restoreAllMenuItem.setEnabled(false);
			removeUnexplainedFromClustersMenuItem.setEnabled(false);

			//addActiveFeaturesToSubsetMenuItem.setEnabled(false);
			//addUnassignedFeaturesToSubsetMenuItem.setEnabled(false);
		}
	}

	@Override
	public void updateGuiFromActiveSet(MsFeatureClusterSet integratedSet) {
		// TODO Auto-generated method stub

	}
}
