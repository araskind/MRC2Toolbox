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

package edu.umich.med.mrc2.datoolbox.gui.mptrack;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MoTrPACPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon databaseIconSmall = GuiUtils.getIcon("experimentDatabase", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);
	private static final Icon showMetadataIcon = GuiUtils.getIcon("metadata", 24);
	private static final Icon showMetadataIconSmall = GuiUtils.getIcon("metadata", 16);
	private static final Icon createReportDirIcon = GuiUtils.getIcon("newProject", 24);
	private static final Icon createFilesIcon = GuiUtils.getIcon("addMultifile", 24);
	private static final Icon createFilesIconSmall = GuiUtils.getIcon("addMultifile", 16);
	private static final Icon uploadReportIcon = GuiUtils.getIcon("addSop", 24);
	private static final Icon uploadUtilsMenuIcon = GuiUtils.getIcon("cog", 16);
	private static final Icon zipIcon = GuiUtils.getIcon("zip", 24);
	private static final Icon md5Icon = GuiUtils.getIcon("hashMd5", 24);
		
	// Menus
	private JMenu
		databaseMenu,
		referenceMenu,
		reportMenu,
		dataUploadUtilsMenu;

	// Database items
	private JMenuItem
		refreshDatabaseDataMenuItem;
	
	// Reference items
	private JMenuItem
		showReferenceDataMenuItem;

	// Report items
	private JMenuItem
		createReportTemplateFilesMenuItem,
		createReportFolderStructureMenuItem;
	
	//	Upload items
	private JMenuItem
		compressRawFilesMenuItem,
		createManifestFileMenuItem;
	
	public MoTrPACPanelMenuBar(ActionListener listener) {

		super(listener);

		// Database
		databaseMenu = new JMenu("Database");
		databaseMenu.setIcon(databaseIconSmall);		
		
		refreshDatabaseDataMenuItem = addItem(databaseMenu, 
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND, 
				refreshDataIcon);
		
		add(databaseMenu);
		
		// Reference
		referenceMenu = new JMenu("Reference");
		referenceMenu.setIcon(showMetadataIconSmall);		
		
		showReferenceDataMenuItem = addItem(referenceMenu,
				MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND, 
				showMetadataIcon);
		
		add(referenceMenu);
		
		// Report
		reportMenu = new JMenu("Report");
		reportMenu.setIcon(createFilesIconSmall);		
		
		createReportFolderStructureMenuItem = addItem(reportMenu, 
				MainActionCommands.CREATE_DIRECTORY_STRUCTURE_FOR_BIC_UPLOAD, 
				createReportDirIcon);
		createReportTemplateFilesMenuItem = addItem(reportMenu, 
				MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND, 
				createFilesIcon);
		
		add(reportMenu);
		
		dataUploadUtilsMenu = new JMenu("Upload utilities");
		dataUploadUtilsMenu.setIcon(uploadUtilsMenuIcon);
		
		compressRawFilesMenuItem = addItem(dataUploadUtilsMenu, 
				MainActionCommands.SET_UP_AGILENT_DOTD_FILES_COMPRESSION_COMMAND, 
				zipIcon);
		createManifestFileMenuItem = addItem(dataUploadUtilsMenu, 
				MainActionCommands.SET_UP_UPLOAD_MANIFEST_GENERATION_COMMAND, 
				md5Icon);
		
		add(dataUploadUtilsMenu);
	}

	public void updateMenuFromExperiment(
			DataAnalysisProject experiment, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
