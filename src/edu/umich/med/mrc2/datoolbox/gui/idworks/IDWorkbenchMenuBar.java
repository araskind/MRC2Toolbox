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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDWorkbenchMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	
	private static final Icon searchIconSmall = GuiUtils.getIcon("searchFeatures", 16);
	private static final Icon searchIdTrackerIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon searchExperimentIcon = GuiUtils.getIcon("searchIdExperiment", 24);
	private static final Icon searchIdActiveDataSetIcon = GuiUtils.getIcon("searchIdActiveDataSet", 24);
	private static final Icon nistPepMsIcon = GuiUtils.getIcon("NISTMS-pep", 24);
	private static final Icon nistPepMsIconSmall = GuiUtils.getIcon("NISTMS-pep", 16);
	private static final Icon nistPepMsOfflineIcon = GuiUtils.getIcon("NISTMS-pep-offline", 24);
	private static final Icon nistPepMsOfflineUploadIcon = GuiUtils.getIcon("NISTMS-pep-upload", 24);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon exportMSPIconSmall = GuiUtils.getIcon("exportToMSP", 16);
	private static final Icon siriusIcon = GuiUtils.getIcon("sirius", 24);
	private static final Icon siriusClusterIcon = GuiUtils.getIcon("siriusCluster", 24);	
	private static final Icon exportTrackerDataIcon = GuiUtils.getIcon("saveList", 24);
	private static final Icon exportMSMSClustersIcon = GuiUtils.getIcon("exportClusterSet", 24);
	private static final Icon idStatusManagerIcon = GuiUtils.getIcon("idStatusManager", 24);
	private static final Icon idStatusManagerIconSmall = GuiUtils.getIcon("idStatusManager", 16);
	private static final Icon idFollowupStepManagerIcon = GuiUtils.getIcon("idFollowupStepManager", 24);
	private static final Icon standardFeatureAnnotationManagerIcon = GuiUtils.getIcon("editCollection", 24);	
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 24);
	private static final Icon bubblePlotIconSmall = GuiUtils.getIcon("bubble", 16);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("clusterFeatureTable", 24);
	private static final Icon editFeatureCollectionIconSmall = GuiUtils.getIcon("clusterFeatureTable", 16);
	private static final Icon fdrIcon = GuiUtils.getIcon("fdr", 24);	
	private static final Icon reassignTopHitsIcon = GuiUtils.getIcon("recalculateScores", 24);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);	
	private static final Icon reloadIcon = GuiUtils.getIcon("rerun", 24);
	private static final Icon reloadClusterSetFeaturesIcon = GuiUtils.getIcon("reloadClusterSetFeatures", 24);
	private static final Icon findMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 24);
	private static final Icon entropyIcon = GuiUtils.getIcon("spectrumEntropy", 24);
	private static final Icon dsSummaryIcon = GuiUtils.getIcon("infoGreen", 24);
	private static final Icon saveClusterDataSetIcon = GuiUtils.getIcon("saveCluster", 24);
	private static final Icon clearWorkbenchIcon = GuiUtils.getIcon("clearWorklist", 24);
	private static final Icon calcStatsIcon = GuiUtils.getIcon("calcStats", 24);
	private static final Icon clusterIconSmall = GuiUtils.getIcon("cluster", 16);
	private static final Icon clusterIcon = GuiUtils.getIcon("cluster", 24);
	private static final Icon filterClustersIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon reloadClusterTreeIcon = GuiUtils.getIcon("rerun", 24);
	private static final Icon clusteringSummaryIcon = GuiUtils.getIcon("summary", 24);	

	// Menus
	private JMenu
		databaseSearchMenu,
		librarySearchMenu,
		clusterMenu,
		annotationManagersMenu,
		featureCollectionsMenu,
		explorationMenu,
		exportMenu;

	// Database search
	private JMenuItem
		databaseSearchMenuItem,
		experimentSearchMenuItem,
		findMSMSFeaturesMenuItem,
		filterMSMSFeaturesMenuItem,
		resetFeatureFiltersMenuItem,
		reloadCompleteFeatureSetMenuItem,
		clearWorkbenchMenuItem;

	// Library search
	private JMenuItem
		pepSearchSetupMenuItem,
		pepSearchOfflineSetupMenuItem,
		pepsearchResultsValidateMenuItem,
		defaultMatchReassignMenuItem,
		entropyMenuItem,
		fdrMenuItem;
	
	//	Clustering
	private JMenuItem
		activeDataSetSearchMenuItem,
		reloadActiveClusterSetFeaturesMenuItem,
		extractMajorClusterFeaturesMenuItem,
		filterClustersMenuItem,
		reloadClusterTreeMenuItem,
		clusteringSummaryMenuItem;

	// Annotation managers
	private JMenuItem
		idLevelManagerMenuItem,
		followupStepsManagerMenuItem,
		stdAnnotationManagerMenuItem;
	
	//	Feature collections
	private JMenuItem
		featureCollectionManagerMenuItem,
		saveClusterDataSetMenuItem;
	
	//	Explore
	private JMenuItem
		bubblePlotMenuItem,
		dataSetSummaryMenuItem;
	
	//	Export
	private JMenuItem
		trackerExportMenuItem,
		mspExportMenuItem,
		siriusExportMenuItem,
		exportMSMSClustersMenuItem,
		exportMSMSClustersForSIRIUSMenuItem;
	
	public IDWorkbenchMenuBar(ActionListener listener) {

		super(listener);

		// Database search
		databaseSearchMenu = new JMenu("Feature search");
		databaseSearchMenu.setIcon(searchIconSmall);
		
		databaseSearchMenuItem = addItem(databaseSearchMenu, 
			MainActionCommands.SHOW_IDTRACKER_SEARCH_DIALOG_COMMAND, 
			searchIdTrackerIcon);
		
		experimentSearchMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.SHOW_IDTRACKER_BY_EXPERIMENT_MZ_RT_SEARCH_DIALOG_COMMAND, 
				searchExperimentIcon);

		databaseSearchMenu.addSeparator();
		
		filterMSMSFeaturesMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND, 
				filterIcon);
		
//		findMSMSFeaturesMenuItem = addItem(databaseSearchMenu, 
//				MainActionCommands.SHOW_FEATURE_SEARCH_BY_RT_ID_COMMAND, 
//				findMSMSFeaturesIcon);
		
		databaseSearchMenu.addSeparator();
		
		reloadCompleteFeatureSetMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.RELOAD_COMPLETE_DATA_SET_COMMAND, 
				reloadIcon);
		
		resetFeatureFiltersMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES, 
				resetFilterIcon);
		
		clearWorkbenchMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.CLEAR_IDTRACKER_WORKBENCH_PANEL, 
				clearWorkbenchIcon);

		add(databaseSearchMenu);

		// Library search
		librarySearchMenu = new JMenu("MSMS library search");
		librarySearchMenu.setIcon(nistPepMsIconSmall);
		
		pepSearchSetupMenuItem = addItem(librarySearchMenu, 
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND, 
				nistPepMsIcon);
		pepSearchOfflineSetupMenuItem = addItem(librarySearchMenu, 
				MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND, 
				nistPepMsOfflineIcon);

		librarySearchMenu.addSeparator();

		pepsearchResultsValidateMenuItem = addItem(librarySearchMenu, 
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND, 
				nistPepMsOfflineUploadIcon);
		defaultMatchReassignMenuItem = addItem(librarySearchMenu, 
				MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT, 
				reassignTopHitsIcon);
		
		librarySearchMenu.addSeparator();
		
		entropyMenuItem = addItem(librarySearchMenu, 
				MainActionCommands.SETUP_SPECTRUM_ENTROPY_SCORING, 
				entropyIcon);
		
		librarySearchMenu.addSeparator();

		fdrMenuItem = addItem(librarySearchMenu, 
				MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES, 
				fdrIcon);
		//	fdrMenuItem.setEnabled(false);
		
		add(librarySearchMenu);
		
		clusterMenu = new JMenu("Clustering");
		clusterMenu.setIcon(clusterIconSmall);
				
		activeDataSetSearchMenuItem = addItem(clusterMenu, 
				MainActionCommands.SHOW_ACTIVE_DATA_SET_MZ_RT_SEARCH_DIALOG_COMMAND, 
				clusterIcon);
		
		extractMajorClusterFeaturesMenuItem = addItem(clusterMenu, 
				MainActionCommands.SETUP_MAJOR_CLUSTER_FEATURE_EXTRACTION_COMMAND, 
				searchIdActiveDataSetIcon);
		
		clusterMenu.addSeparator();
		
		filterClustersMenuItem = addItem(clusterMenu, 
				MainActionCommands.SHOW_MSMS_CLUSTER_FILTER_COMMAND, 
				filterClustersIcon);
		
		clusteringSummaryMenuItem = addItem(clusterMenu, 
				MainActionCommands.SHOW_MSMS_CLUSTERS_SUMMARY_COMMAND, 
				clusteringSummaryIcon);
		
		clusterMenu.addSeparator();
		
		reloadClusterTreeMenuItem = addItem(clusterMenu, 
				MainActionCommands.RELOAD_ACTIVE_MSMS_CLUSTERS_SET_COMMAND, 
				reloadClusterSetFeaturesIcon);
		
		reloadActiveClusterSetFeaturesMenuItem = addItem(clusterMenu, 
				MainActionCommands.RELOAD_ACTIVE_MSMS_CLUSTER_SET_FEATURES, 
				reloadClusterSetFeaturesIcon);
		
		add(clusterMenu);
		
		// Annotation managers
		annotationManagersMenu = new JMenu("Manage annotations");
		annotationManagersMenu.setIcon(idStatusManagerIconSmall);
		
		idLevelManagerMenuItem = addItem(annotationManagersMenu, 
				MainActionCommands.SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND, 
				idStatusManagerIcon);
		followupStepsManagerMenuItem = addItem( annotationManagersMenu, 
				MainActionCommands.SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND, 
				idFollowupStepManagerIcon);
		stdAnnotationManagerMenuItem = addItem(annotationManagersMenu, 
				MainActionCommands.SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND, 
				standardFeatureAnnotationManagerIcon);
		
		add(annotationManagersMenu);
		
		//	Feature collections
		featureCollectionsMenu = new JMenu("Feature collections");
		featureCollectionsMenu.setIcon(editFeatureCollectionIconSmall);

		featureCollectionManagerMenuItem = addItem(featureCollectionsMenu, 
				MainActionCommands.SHOW_FEATURE_COLLECTION_MANAGER_DIALOG_COMMAND, 
				editFeatureCollectionIcon);
		
		featureCollectionsMenu.addSeparator();
		
		saveClusterDataSetMenuItem = addItem(featureCollectionsMenu, 
				MainActionCommands.ADD_MSMS_CLUSTER_DATASET_DIALOG_COMMAND, 
				saveClusterDataSetIcon);
		
		add(featureCollectionsMenu);
		
		//	Explore
		explorationMenu = new JMenu("Explore data");
		explorationMenu.setIcon(bubblePlotIconSmall);
		
		bubblePlotMenuItem = addItem(explorationMenu, 
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPLORER_PLOT, 
				bubblePlotIcon);
		
		dataSetSummaryMenuItem = addItem(explorationMenu, 
				MainActionCommands.SHOW_ACTIVE_DATA_SET_SUMMARY_COMMAND, 
				calcStatsIcon);

		add(explorationMenu);
		
		//	Export
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(exportMSPIconSmall);
		
		trackerExportMenuItem = addItem(exportMenu, 
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND, 
				exportTrackerDataIcon);
		mspExportMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND, 
				exportMSPIcon);
		siriusExportMenuItem = addItem(exportMenu, 
				MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND, 
				siriusIcon);		
		exportMSMSClustersMenuItem = addItem(exportMenu, 
				MainActionCommands.SHOW_MSMS_CLUSTER_DATA_EXPORT_DIALOG_COMMAND, 
				exportMSMSClustersIcon);
		
		exportMSMSClustersForSIRIUSMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_MSMS_CLUSTER_DATA_FOR_SIRIUS_COMMAND, 
				siriusClusterIcon);
		
		add(exportMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
