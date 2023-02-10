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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.assay;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AssayDesignEditorMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon editDesignIcon = GuiUtils.getIcon("editDesignSubset", 24);
	private static final Icon linkFilesIcon = GuiUtils.getIcon("link", 24);
	private static final Icon enableSelectedIcon = GuiUtils.getIcon("checkboxFull", 24);
	private static final Icon disableSelectedIcon = GuiUtils.getIcon("checkboxEmpty", 24);
	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 24);
	private static final Icon enableAllIconSmall = GuiUtils.getIcon("enableAll", 16);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 24);
	private static final Icon invertEnabledIcon = GuiUtils.getIcon("invertSelection", 24);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon deleteFilesIcon = GuiUtils.getIcon("deleteDataFile", 24);
	private static final Icon editIconSmall = GuiUtils.getIcon("editSop", 16);
	
	
	// Menus
	private JMenu
		selectMenu,
		editMenu;

	// Select items
	private JMenuItem
		enableSelectedMenuItem,
		disableSelectedMenuItem,
		enableAllMenuItem,
		disableAllMenuItem,
		invertSelectionMenuItem,
		clearFiltersMenuItem;

	// Edit items
	private JMenuItem
		deleteDataFilesMenuItem;

	public AssayDesignEditorMenuBar(ActionListener listener) {

		super(listener);

		// Design
		selectMenu = new JMenu("Selection");
		selectMenu.setIcon(enableAllIconSmall);
		
		enableSelectedMenuItem = addItem(selectMenu, 
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND, 
				enableSelectedIcon);
		disableSelectedMenuItem = addItem(selectMenu, 
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND, 
				disableSelectedIcon);
		enableAllMenuItem = addItem(selectMenu, 
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND, 
				enableAllIcon);
		disableAllMenuItem = addItem(selectMenu, 
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND, 
				disableAllIcon);
		invertSelectionMenuItem = addItem(selectMenu, 
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND, 
				invertEnabledIcon);
		
		selectMenu.addSeparator();
		
		clearFiltersMenuItem = addItem(selectMenu, 
				MainActionCommands.CLEAR_SAMPLES_FILTER_COMMAND, 
				resetFilterIcon);
		
		add(selectMenu);

		editMenu = new JMenu("Edit");
		editMenu.setIcon(editIconSmall);
		
		deleteDataFilesMenuItem = addItem(editMenu, 
				MainActionCommands.DELETE_DATA_FILES_COMMAND, 
				deleteFilesIcon);
		
		add(editMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
