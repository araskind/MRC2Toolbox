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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class WorklistPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon saveWorklistIconSmall = GuiUtils.getIcon("saveWorklist", 16);
	private static final Icon utilsIconSmall= GuiUtils.getIcon("dataFileTools", 16);
	private static final Icon loadWorklistFromFileIconSmall = GuiUtils.getIcon("loadWorklist", 16);	
	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	private static final Icon loadWorklistFromFileIcon = GuiUtils.getIcon("loadWorklist", 24);
	private static final Icon addWorklistFromFileIcon = GuiUtils.getIcon("addWorklist", 24);
	private static final Icon scanDirIcon = GuiUtils.getIcon("scanFolder", 24);
	private static final Icon addFromDirIcon = GuiUtils.getIcon("addFromFolder", 24);
	private static final Icon clearWorklistIcon = GuiUtils.getIcon("clearWorklist", 24);
	private static final Icon saveWorklistIcon = GuiUtils.getIcon("saveWorklist", 24);
	private static final Icon copyWorklistToClipboardIcon = GuiUtils.getIcon("copyWorklistToClipboard", 24);
	private static final Icon manifestIcon = GuiUtils.getIcon("manifest", 24);
	private static final Icon refreshIcon = GuiUtils.getIcon("rerun", 24);	
	private static final Icon extractWorklistIcon = GuiUtils.getIcon("extractList", 24);
	private static final Icon sampleWarningIcon = GuiUtils.getIcon("sampleWarning", 24);
	
	// Menus
	private JMenu
		loadMenu,
		exportMenu,
		utilsMenu;

	// Load items
	private JMenuItem
		scanDirMenuItem,
		scanDirAddMenuItem,
		loadWorklistMenuItem,
		addWorklistMenuItem,
		clearWorklistMenuItem;

	// Export items
	private JMenuItem
		saveWorklistMenuItem,
		copyWorklistMenuItem,
		saveManifestMenuItem;
	
	// Utils items
	private JMenuItem
		checkMissingDataMenuItem,
		extractWorklistMenuItem;

	public WorklistPanelMenuBar(ActionListener listener) {

		super(listener);

		// Design
		loadMenu = new JMenu("Load");
		loadMenu.setIcon(loadWorklistFromFileIconSmall);		
		
		scanDirMenuItem = addItem(loadMenu, 
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND, 
				scanDirIcon);
		scanDirAddMenuItem = addItem(loadMenu, 
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND, 
				addFromDirIcon);
		
		loadMenu.addSeparator();
		
		loadWorklistMenuItem = addItem(loadMenu, 
				MainActionCommands.LOAD_WORKLIST_COMMAND, 
				loadWorklistFromFileIcon);
		addWorklistMenuItem = addItem(loadMenu, 
				MainActionCommands.ADD_WORKLIST_COMMAND, 
				addWorklistFromFileIcon);
		
		loadMenu.addSeparator();
		
		clearWorklistMenuItem = addItem(loadMenu, 
				MainActionCommands.CLEAR_WORKLIST_COMMAND, 
				clearWorklistIcon);
		
		add(loadMenu);

		exportMenu = new JMenu("Export");
		exportMenu.setIcon(saveWorklistIconSmall);		
		
		saveWorklistMenuItem = addItem(exportMenu, 
				MainActionCommands.SAVE_WORKLIST_COMMAND, 
				saveWorklistIcon);
		copyWorklistMenuItem = addItem(exportMenu, 
				MainActionCommands.COPY_WORKLIST_COMMAND, 
				copyWorklistToClipboardIcon);
		saveManifestMenuItem = addItem(exportMenu, 
				MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND, 
				manifestIcon);
		
		add(exportMenu);
		
		utilsMenu = new JMenu("Utilities");
		utilsMenu.setIcon(utilsIconSmall);		
		
		checkMissingDataMenuItem = addItem(utilsMenu, 
				MainActionCommands.CHECK_WORKLIST_FOR_MISSING_DATA, 
				sampleWarningIcon);
		
		utilsMenu.addSeparator();
		
		extractWorklistMenuItem = addItem(utilsMenu, 
				MainActionCommands.EXTRACT_WORKLIST_COMMAND, 
				extractWorklistIcon);
		
		add(utilsMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
