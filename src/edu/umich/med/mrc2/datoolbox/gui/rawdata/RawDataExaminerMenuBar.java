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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class RawDataExaminerMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon extractMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 24);
	private static final Icon extractMSMSFeaturesIconSmall = GuiUtils.getIcon("findMSMSFeatures", 16);
	private static final Icon sendToIDTrackerIcon = GuiUtils.getIcon("sendToIDTracker", 24);
	private static final Icon openDataFileIcon = GuiUtils.getIcon("openDataFile", 24);
	private static final Icon closeDataFileIcon = GuiUtils.getIcon("closeDataFile", 24);
	private static final Icon msConvertIcon = GuiUtils.getIcon("msConvert", 24);
	private static final Icon indexRawFilesIcon = GuiUtils.getIcon("indexRawFiles", 24);
	private static final Icon dataFileToolsIcon = GuiUtils.getIcon("dataFileTools", 24);
	private static final Icon dataFileToolsIconSmall= GuiUtils.getIcon("dataFileTools", 16);
	private static final Icon addMetaDataIcon = GuiUtils.getIcon("addMetadata", 24);
	private static final Icon sendProjectToDatabaseIcon = GuiUtils.getIcon("xml2Database", 24);
	private static final Icon sendProjectToDatabaseIconSmall = GuiUtils.getIcon("xml2Database", 16);

	// Menus
	private JMenu
		msmsMenu,
		dbLinkageMenu,
		rawDataMenu;

	// MSMS items
	private JMenuItem
		extractMSMSMenuItem,
		sendMSMS2TrackerMenuItem;

	// DB link items
	private JMenuItem
		addMetadataMenuItem,
		sendProjectToTrackerMenuItem;

	// Raw data
	private JMenuItem
		openRawFilesMenuItem,
		closeRawFilesMenuItem,
		msConvertSetupMenuItem,
		indexRawDataRepositoryMenuItem,
		rawDataToolsMenuItem;

	public RawDataExaminerMenuBar(ActionListener listener) {

		super(listener);


		// MSMS
		msmsMenu = new JMenu("MSMS analysis");
		msmsMenu.setIcon(extractMSMSFeaturesIconSmall);
		
		extractMSMSMenuItem = addItem(msmsMenu, 
				MainActionCommands.MSMS_FEATURE_EXTRACTION_SETUP_COMMAND, 
				extractMSMSFeaturesIcon);
		sendMSMS2TrackerMenuItem = addItem(msmsMenu, 
				MainActionCommands.SEND_MSMS_FEATURES_TO_IDTRACKER_WORKBENCH, 
				sendToIDTrackerIcon);

		add(msmsMenu);
		
		//	DB linkage
		dbLinkageMenu = new JMenu("IDTracker integration");
		dbLinkageMenu.setIcon(sendProjectToDatabaseIconSmall);
		
		addMetadataMenuItem = addItem(dbLinkageMenu, 
				MainActionCommands.ADD_EXPERIMENT_METADATA_COMMAND, 
				addMetaDataIcon);
		sendProjectToTrackerMenuItem = addItem(dbLinkageMenu, 
				MainActionCommands.SET_EXPERIMENT_DATA_UPLOAD_PARAMETERS_COMMAND, 
				sendProjectToDatabaseIcon);
		
		add(dbLinkageMenu);
		
		rawDataMenu = new JMenu("Raw data tools");
		
		openRawFilesMenuItem = addItem(rawDataMenu, 
				MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND, 
				openDataFileIcon);
		closeRawFilesMenuItem = addItem(rawDataMenu, 
				MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND, 
				closeDataFileIcon);
		
		rawDataMenu.addSeparator();
		rawDataMenu.setIcon(dataFileToolsIconSmall);
		
		//	RAw data
		msConvertSetupMenuItem = addItem(rawDataMenu, 
				MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND, 
				msConvertIcon);
		indexRawDataRepositoryMenuItem = addItem(rawDataMenu, 
				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND, 
				indexRawFilesIcon);
		rawDataToolsMenuItem = addItem(rawDataMenu, 
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND, 
				dataFileToolsIcon);
		
		add(rawDataMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
