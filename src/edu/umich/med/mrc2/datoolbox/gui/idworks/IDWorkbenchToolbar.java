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
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDWorkbenchToolbar extends CommonToolbar{

	/**
	 *
	 */
	private static final long serialVersionUID = 7850746850300661879L;

	private static final Icon searchIdTrackerIcon = GuiUtils.getIcon("searchDatabase", 32);
	private static final Icon openCdpIdProjectIcon = GuiUtils.getIcon("openIdExperiment", 32);
	private static final Icon loadLibraryIcon = GuiUtils.getIcon("loadLibrary", 32);
	private static final Icon idSetupIcon = GuiUtils.getIcon("searchCompounds", 32);
	private static final Icon nistMsIcon = GuiUtils.getIcon("NISTMS", 32);
	private static final Icon nistPepMsIcon = GuiUtils.getIcon("NISTMS-pep", 32);
	private static final Icon nistPepMsOfflineIcon = GuiUtils.getIcon("NISTMS-pep-offline", 32);
	private static final Icon nistPepMsOfflineUploadIcon = GuiUtils.getIcon("NISTMS-pep-upload", 32);
	private static final Icon validateNistPepMsIcon = GuiUtils.getIcon("acceptMsMs", 32);
	private static final Icon iddaIcon = GuiUtils.getIcon("importIDDAdata", 32);
	private static final Icon trackerManegerIcon = GuiUtils.getIcon("experimentDatabase", 32);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 32);
	private static final Icon siriusIcon = GuiUtils.getIcon("sirius", 32);
	private static final Icon exportSiriusMSIcon = GuiUtils.getIcon("exportSiriusMs", 32);
	private static final Icon exportTrackerDataIcon = GuiUtils.getIcon("saveList", 32);
	private static final Icon idStatusManagerIcon = GuiUtils.getIcon("idStatusManager", 32);
	private static final Icon idFollowupStepManagerIcon = GuiUtils.getIcon("idFollowupStepManager", 32);
	private static final Icon standardFeatureAnnotationManagerIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon openMsMsDataFileIcon = GuiUtils.getIcon("openMsMsDataFile", 32);
	private static final Icon indexRawFilesIcon = GuiUtils.getIcon("indexRawFiles", 32);
	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 32);	
	private static final Icon libraryExportIcon = GuiUtils.getIcon("exportLibrary", 32);
	private static final Icon libraryImportIcon = GuiUtils.getIcon("importLibraryToDb", 32);
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 32);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("clusterFeatureTable", 32);
	private static final Icon fdrIcon = GuiUtils.getIcon("fdr", 32);	
	private static final Icon reassignTopHitsIcon = GuiUtils.getIcon("recalculateScores", 32);
	
	@SuppressWarnings("unused")
	private JButton
		searchIdTrackerButton,
		newCpdIdProjectButton,
		loadLibraryButton,
		idSetupButton,
		iddaButton,
		nistMsButon,		
		nistMsPepButon,
		nistMsPepOfflineButon,
		nistPepSearchOfflineUploadButton,
		trackerManagerButon,
		exportMSPButton,
		exportSiriusMSButton,
		exportTrackerDataButton,
		idStatusManagerButton,
		idFollowupStepManagerButton,
		standardFeatureAnnotationManagerButton,
		indexRawFilesButton,
		openMsMsDataFilesButton,
		clearDuplicatesButton,
		exportRefMSMSLibraryButton,
		importDecoyRefMSMSLibraryButton,
		bubblePlotButton,
		editFeatureCollectionButton,
		fdrButton,
		reassignTopHitsButton;

	public IDWorkbenchToolbar(ActionListener commandListener) {

		super(commandListener);
/*
		newCpdIdProjectButton = GuiUtils.addButton(this, null, openCdpIdProjectIcon, commandListener,
				MainActionCommands.SHOW_OPEN_CPD_ID_PROJECT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_OPEN_CPD_ID_PROJECT_DIALOG_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		loadLibraryButton = GuiUtils.addButton(this, null, loadLibraryIcon, commandListener,
				MainActionCommands.LOAD_FEATURES_FOR_ID_COMMAND.getName(),
				MainActionCommands.LOAD_FEATURES_FOR_ID_COMMAND.getName(),
				buttonDimension);

		iddaButton = GuiUtils.addButton(this, null, iddaIcon, commandListener,
				MainActionCommands.IDDA_SETUP_DIALOG_COMMAND.getName(),
				MainActionCommands.IDDA_SETUP_DIALOG_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		idSetupButton = GuiUtils.addButton(this, null, idSetupIcon, commandListener,
				MainActionCommands.ID_SETUP_DIALOG_COMMAND.getName(),
				MainActionCommands.ID_SETUP_DIALOG_COMMAND.getName(),
				buttonDimension);
*/
		
		searchIdTrackerButton = GuiUtils.addButton(this, null, searchIdTrackerIcon, commandListener,
				MainActionCommands.SHOW_ID_TRACKER_SEARCH_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_ID_TRACKER_SEARCH_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		exportMSPButton = GuiUtils.addButton(this, null, exportMSPIcon, commandListener,
				MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND.getName(),
				MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND.getName(),
				buttonDimension);
		
		exportSiriusMSButton = GuiUtils.addButton(this, null, siriusIcon, commandListener,
				MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		exportTrackerDataButton = GuiUtils.addButton(this, null, exportTrackerDataIcon, commandListener,
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);
		
		nistMsPepButon = GuiUtils.addButton(this, null, nistPepMsIcon, commandListener,
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName(),
				buttonDimension);
		
		nistMsPepOfflineButon = GuiUtils.addButton(this, null, nistPepMsOfflineIcon, commandListener,
				MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND.getName(),
				buttonDimension);
		
		nistMsButon = GuiUtils.addButton(this, null, nistMsIcon, commandListener,
				MainActionCommands.NIST_MS_SEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.NIST_MS_SEARCH_SETUP_COMMAND.getName(),
				buttonDimension);	
		nistMsButon.setEnabled(false);
		
		nistPepSearchOfflineUploadButton = GuiUtils.addButton(this, null, nistPepMsOfflineUploadIcon, commandListener,
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName(),
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName(),
				buttonDimension);
		
		fdrButton = GuiUtils.addButton(this, null, fdrIcon, commandListener,
				MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES.getName(),
				MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES.getName(),
				buttonDimension);
		fdrButton.setEnabled(false);
		
		reassignTopHitsButton = GuiUtils.addButton(this, null, reassignTopHitsIcon, commandListener,
				MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT.getName(),
				MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		idStatusManagerButton = GuiUtils.addButton(this, null, idStatusManagerIcon, commandListener,
				MainActionCommands.SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		idFollowupStepManagerButton = GuiUtils.addButton(this, null, idFollowupStepManagerIcon, commandListener,
				MainActionCommands.SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		standardFeatureAnnotationManagerButton = GuiUtils.addButton(this, null, standardFeatureAnnotationManagerIcon, commandListener,
				MainActionCommands.SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		indexRawFilesButton = GuiUtils.addButton(this, null, indexRawFilesIcon, commandListener,
				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName(),
				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName(),
				buttonDimension);
		
		openMsMsDataFilesButton = GuiUtils.addButton(this, null, openMsMsDataFileIcon, commandListener,
				MainActionCommands.LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND.getName(),
				MainActionCommands.LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		exportRefMSMSLibraryButton = GuiUtils.addButton(this, null, libraryExportIcon, commandListener,
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName(), buttonDimension);
//		exportRefMSMSLibraryButton.setEnabled(false);
		
		importDecoyRefMSMSLibraryButton = GuiUtils.addButton(this, null, libraryImportIcon, commandListener,
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName(),
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName(), buttonDimension);
//		importDecoyRefMSMSLibraryButton.setEnabled(false);
		
		addSeparator(buttonDimension);
		
		bubblePlotButton = GuiUtils.addButton(this, null, bubblePlotIcon, commandListener,
				MainActionCommands.SHOW_ID_TRACKER_DATA_EXPLORER_PLOT.getName(),
				MainActionCommands.SHOW_ID_TRACKER_DATA_EXPLORER_PLOT.getName(), buttonDimension);
		
		editFeatureCollectionButton = GuiUtils.addButton(this, null, editFeatureCollectionIcon, commandListener,
				MainActionCommands.SHOW_FEATURE_COLLECTION_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_FEATURE_COLLECTION_MANAGER_DIALOG_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
