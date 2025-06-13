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

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CorrelationPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon actionsIcon = GuiUtils.getIcon("actions", 24);
	private static final Icon clusteringIcon = GuiUtils.getIcon("cluster", 24);
	private static final Icon clusteringIconSmall = GuiUtils.getIcon("cluster", 16);
	protected static final Icon filterIcon = GuiUtils.getIcon("filterClusters", 24);
	protected static final Icon filterIconSmall = GuiUtils.getIcon("filterClusters", 16);
	protected static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon importBinnerDataIcon = GuiUtils.getIcon("importBins", 24);
	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 24);
	private static final Icon heatmapIconSmall = GuiUtils.getIcon("heatmap", 16);
	private static final Icon exploreDeltasIcon = GuiUtils.getIcon("exploreDeltas", 24);
	private static final Icon removeUnexplainedFromClustersIcon = GuiUtils.getIcon("clearUnassigned", 24);
	private static final Icon rejectUnexplainedIcon = GuiUtils.getIcon("rejectUnexplained", 24);
	private static final Icon molIonOnlyIcon = GuiUtils.getIcon("molIon", 24);
	private static final Icon restoreAllIcon = GuiUtils.getIcon("markAll", 24);
	private static final Icon annotationIcon = GuiUtils.getIcon("msAnnotation", 24);
	private static final Icon annotationIconSmall = GuiUtils.getIcon("msAnnotation", 16);
	private static final Icon deltaAnnotationIcon = GuiUtils.getIcon("assignDeltas", 24);
	private static final Icon batchAssignAnnotationsIcon = GuiUtils.getIcon("calculateAnnotation", 24);
	private static final Icon recalculateCorrelationsIcon = GuiUtils.getIcon("recalculateCorrelations", 24);
	private static final Icon binnerAnalysisIcon = GuiUtils.getIcon("setupBinnerAnnotations", 24);


	// Menus
	private JMenu
		clusteringMenu,
		searchMenu,
		annotationMenu,
		graphicsMenu;

	// Clustering
	private JMenuItem
		binnerAnalysisMenuItem,
		clusteringSetupMenuItem;

	// Search
	private JMenuItem
		filterClustersMenuItem,
		resetClusterFilteringMenuItem;

	// Annotation
	private JMenuItem
		annotateClusterMenuItem,
		annotateMassDifferencesMenuItem,
		batchAnnotationPreferencesMenuItem,
		binnerImportMenuItemMenuItem,
		removeUnexplainedMenuItem,
		rejectUnexplainedMenuItem,
		rejactAllButMolIonMenuItem,
		restoreAllRejectedMenuItem;

	// Graphics
	private JMenuItem
		heatmapMenuItem,
		massDiffsInBinnerMenuItem;
	
	public CorrelationPanelMenuBar(ActionListener listener) {

		super(listener);
		
		//	Clustering
		clusteringMenu = new JMenu("Clustering");
		clusteringMenu.setIcon(clusteringIconSmall);
		
		binnerAnalysisMenuItem = addItem(clusteringMenu,
				MainActionCommands.BINNER_ANALYSIS_SETUP_COMMAND, 
				binnerAnalysisIcon);

		clusteringSetupMenuItem = addItem(clusteringMenu, 
				MainActionCommands.SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND, 
				recalculateCorrelationsIcon);
		
		add(clusteringMenu);
		
		// Search
		searchMenu = new JMenu("Filter");
		searchMenu.setIcon(filterIconSmall);

		filterClustersMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND, 
				filterIcon);
		resetClusterFilteringMenuItem = addItem(searchMenu, 
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND, 
				resetFilterIcon);
		
		add(searchMenu);

		// Annotation
		annotationMenu = new JMenu("Annotation");
		annotationMenu.setIcon(annotationIconSmall);

		annotateClusterMenuItem = addItem(annotationMenu, 
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND, 
				annotationIcon);
		annotateMassDifferencesMenuItem = addItem(annotationMenu, 
				MainActionCommands.ANNOTATE_MASS_DIFFERENCES_COMMAND, 
				deltaAnnotationIcon);
		batchAnnotationPreferencesMenuItem = addItem(annotationMenu, 
				MainActionCommands.SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND, 
				batchAssignAnnotationsIcon);
		
		annotationMenu.addSeparator();
		
		binnerImportMenuItemMenuItem = addItem(annotationMenu, 
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG, 
				importBinnerDataIcon);
		
		annotationMenu.addSeparator();
		
		removeUnexplainedMenuItem = addItem(annotationMenu, 
				MainActionCommands.REMOVE_UNEXPLAINED_FEATURES_COMMAND, 
				removeUnexplainedFromClustersIcon);
		rejectUnexplainedMenuItem = addItem(annotationMenu, 
				MainActionCommands.REJECT_UNEXPLAINED_FEATURES_COMMAND, 
				rejectUnexplainedIcon);
		rejactAllButMolIonMenuItem = addItem(annotationMenu, 
				MainActionCommands.REJECT_ALL_BUT_MOLION_COMMAND, 
				molIonOnlyIcon);
		restoreAllRejectedMenuItem = addItem(annotationMenu, 
				MainActionCommands.RESTORE_ALL_REJECTED_FEATURES_COMMAND, 
				restoreAllIcon);
		
		add(annotationMenu);
		
		// Graphics
		graphicsMenu = new JMenu("Visualization");
		graphicsMenu.setIcon(heatmapIconSmall);
		
		heatmapMenuItem = addItem(graphicsMenu, 
				MainActionCommands.SHOW_HEATMAP_COMMAND, 
				heatmapIcon);
		massDiffsInBinnerMenuItem = addItem(graphicsMenu, 
				MainActionCommands.EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND, 
				exploreDeltasIcon);
		
		add(graphicsMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
