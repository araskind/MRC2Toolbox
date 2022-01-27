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
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDWorkbenchMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon searchIdTrackerIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon openCdpIdProjectIcon = GuiUtils.getIcon("openIdExperiment", 24);
	private static final Icon loadLibraryIcon = GuiUtils.getIcon("loadLibrary", 24);
	private static final Icon idSetupIcon = GuiUtils.getIcon("searchCompounds", 24);
	private static final Icon nistMsIcon = GuiUtils.getIcon("NISTMS", 24);
	private static final Icon nistPepMsIcon = GuiUtils.getIcon("NISTMS-pep", 24);
	private static final Icon nistPepMsOfflineIcon = GuiUtils.getIcon("NISTMS-pep-offline", 24);
	private static final Icon nistPepMsOfflineUploadIcon = GuiUtils.getIcon("NISTMS-pep-upload", 24);
	private static final Icon validateNistPepMsIcon = GuiUtils.getIcon("acceptMsMs", 24);
	private static final Icon iddaIcon = GuiUtils.getIcon("importIDDAdata", 24);
	private static final Icon trackerManegerIcon = GuiUtils.getIcon("experimentDatabase", 24);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon siriusIcon = GuiUtils.getIcon("sirius", 24);
	private static final Icon exportSiriusMSIcon = GuiUtils.getIcon("exportSiriusMs", 24);
	private static final Icon exportTrackerDataIcon = GuiUtils.getIcon("saveList", 24);
	private static final Icon idStatusManagerIcon = GuiUtils.getIcon("idStatusManager", 24);
	private static final Icon idFollowupStepManagerIcon = GuiUtils.getIcon("idFollowupStepManager", 24);
	private static final Icon standardFeatureAnnotationManagerIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon openMsMsDataFileIcon = GuiUtils.getIcon("openMsMsDataFile", 24);
//	private static final Icon indexRawFilesIcon = GuiUtils.getIcon("indexRawFiles", 24);
	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 24);	
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 24);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("clusterFeatureTable", 24);
	private static final Icon fdrIcon = GuiUtils.getIcon("fdr", 24);	
	private static final Icon reassignTopHitsIcon = GuiUtils.getIcon("recalculateScores", 24);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 24);

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
		databaseSearchMenuItem;

	// Library search
	private JMenuItem
		pepSearchSetupMenuItem,
		pepSearchOfflineSetupMenuItem,
		pepsearchResultsValidateMenuItem,
		defaultMatchReassignMenuItem,
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


		// MSMS
//		databaseSearchMenu = new JMenu("MSMS analysis");
//		databaseSearchMenu.setIcon(extractMSMSFeaturesIconSmall);
//		
//		databaseSearchMenuItem = addItem(databaseSearchMenu, databaseSearchMenuItem, 
//				MainActionCommands.SHOW_ID_TRACKER_SEARCH_DIALOG_COMMAND, 
//				searchIdTrackerIcon);
//
//
//		add(databaseSearchMenu);
//		
//		//	DB linkage
//		dbLinkageMenu = new JMenu("IDTracker integration");
//		dbLinkageMenu.setIcon(sendProjectToDatabaseIconSmall);
//		
//		addMetadataMenuItem = addItem(dbLinkageMenu, addMetadataMenuItem, 
//				MainActionCommands.ADD_PROJECT_METADATA_COMMAND, 
//				addMetaDataIcon);
//		sendProjectToTrackerMenuItem = addItem(dbLinkageMenu, sendProjectToTrackerMenuItem, 
//				MainActionCommands.SEND_PROJECT_DATA_TO_DATABASE_COMMAND, 
//				sendProjectToDatabaseIcon);
//		
//		add(dbLinkageMenu);
//		
//		rawDataMenu = new JMenu("Raw data tools");
//		
//		openRawFilesMenuItem = addItem(rawDataMenu, openRawFilesMenuItem, 
//				MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND, 
//				openDataFileIcon);
//		closeRawFilesMenuItem = addItem(rawDataMenu, closeRawFilesMenuItem, 
//				MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND, 
//				closeDataFileIcon);
//		
//		rawDataMenu.addSeparator();
//		rawDataMenu.setIcon(dataFileToolsIconSmall);
//		
//		//	RAw data
//		msConvertSetupMenuItem = addItem(rawDataMenu, msConvertSetupMenuItem, 
//				MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND, 
//				msConvertIcon);
//		indexRawDataRepositoryMenuItem = addItem(rawDataMenu, indexRawDataRepositoryMenuItem, 
//				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND, 
//				indexRawFilesIcon);
//		rawDataToolsMenuItem = addItem(rawDataMenu, rawDataToolsMenuItem, 
//				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND, 
//				dataFileToolsIcon);
//		
//		add(rawDataMenu);
	}

	public void updateMenuFromProject(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
