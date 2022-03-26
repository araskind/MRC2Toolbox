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
	private static final Icon nistPepMsIcon = GuiUtils.getIcon("NISTMS-pep", 24);
	private static final Icon nistPepMsIconSmall = GuiUtils.getIcon("NISTMS-pep", 16);
	private static final Icon nistPepMsOfflineIcon = GuiUtils.getIcon("NISTMS-pep-offline", 24);
	private static final Icon nistPepMsOfflineUploadIcon = GuiUtils.getIcon("NISTMS-pep-upload", 24);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon exportMSPIconSmall = GuiUtils.getIcon("exportToMSP", 16);
	private static final Icon siriusIcon = GuiUtils.getIcon("sirius", 24);
	private static final Icon exportTrackerDataIcon = GuiUtils.getIcon("saveList", 24);
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
	private static final Icon reloadIcon = GuiUtils.getIcon("rerun", 24);
	private static final Icon findMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 24);
	private static final Icon entropyIcon = GuiUtils.getIcon("spectrumEntropy", 24);

	// Menus
	private JMenu
		databaseSearchMenu,
		librarySearchMenu,
		annotationManagersMenu,
		featureCollectionsMenu,
		graphicsMenu,
		exportMenu;

	// Database search
	private JMenuItem
		databaseSearchMenuItem,
		findMSMSFeaturesMenuItem,
		filterMSMSFeaturesMenuItem,
		resetFeatureFiltersMenuItem;

	// Library search
	private JMenuItem
		pepSearchSetupMenuItem,
		pepSearchOfflineSetupMenuItem,
		pepsearchResultsValidateMenuItem,
		defaultMatchReassignMenuItem,
		entropyMenuItem,
		fdrMenuItem;

	// Annotation managers
	private JMenuItem
		idLevelManagerMenuItem,
		followupStepsManagerMenuItem,
		stdAnnotationManagerMenuItem;
	
	//	Feature collections
	private JMenuItem
		featureCollectionManagerMenuItem;
	
	//	Graphics
	private JMenuItem
		bubblePlotMenuItem;
	
	//	Export
	private JMenuItem
		trackerExportMenuItem,
		mspExportMenuItem,
		siriusExportMenuItem;
	
	public IDWorkbenchMenuBar(ActionListener listener) {

		super(listener);

		// Database search
		databaseSearchMenu = new JMenu("Feature search");
		databaseSearchMenu.setIcon(searchIconSmall);
		
		databaseSearchMenuItem = addItem(databaseSearchMenu, 
			MainActionCommands.SHOW_ID_TRACKER_SEARCH_DIALOG_COMMAND, 
			searchIdTrackerIcon);

		databaseSearchMenu.addSeparator();
		
		filterMSMSFeaturesMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND, 
				filterIcon);
		findMSMSFeaturesMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.SHOW_FEATURE_SEARCH_BY_RT_ID_COMMAND, 
				findMSMSFeaturesIcon);
		
		databaseSearchMenu.addSeparator();
		
		resetFeatureFiltersMenuItem = addItem(databaseSearchMenu, 
				MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES, 
				reloadIcon);

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
		fdrMenuItem.setEnabled(false);
		
		add(librarySearchMenu);
		
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
		
		add(featureCollectionsMenu);
		
		//	Graphics
		graphicsMenu = new JMenu("Visualization");
		graphicsMenu.setIcon(bubblePlotIconSmall);
		
		bubblePlotMenuItem = addItem(graphicsMenu, 
				MainActionCommands.SHOW_ID_TRACKER_DATA_EXPLORER_PLOT, 
				bubblePlotIcon);

		add(graphicsMenu);
		
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
		
		add(exportMenu);
	}

	public void updateMenuFromProject(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
