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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MainMenuBar extends JMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	private static final Dimension preferredSize = new Dimension(80, 20);

	// Icons
	private static final Icon newProjectIcon = GuiUtils.getIcon("newProject", 24);
	private static final Icon openProjectIcon = GuiUtils.getIcon("open", 24);
	private static final Icon saveProjectIcon = GuiUtils.getIcon("save", 24);
	private static final Icon closeProjectIcon = GuiUtils.getIcon("close", 24);
	private static final Icon exitIcon = GuiUtils.getIcon("shutDown", 24);
	private static final Icon loadDesignIcon = GuiUtils.getIcon("loadDesign", 24);
	private static final Icon loadWorklistIcon = GuiUtils.getIcon("loadWorklist", 24);
	private static final Icon loadQuantDataMultifileIcon = GuiUtils.getIcon("importMultifile", 24);
	private static final Icon loadQuantDataIcon = GuiUtils.getIcon("importTextfile", 24);
	private static final Icon LoadLibraryIcon = GuiUtils.getIcon("loadLibrary", 24);
	private static final Icon loadMgfDataIcon = GuiUtils.getIcon("loadMgf", 24);
	private static final Icon findDuplicatesIcon = GuiUtils.getIcon("findDuplicates", 24);
	private static final Icon analyzeCorrelationsIcon = GuiUtils.getIcon("filterCluster", 24);
	private static final Icon exportForRIcon = GuiUtils.getIcon("rScript", 24);
	private static final Icon exportForMPPIcon = GuiUtils.getIcon("MPP", 24);
	private static final Icon exportForBinnertIcon = GuiUtils.getIcon("binnerIcon", 24);
	private static final Icon exportDuplicatesIcon = GuiUtils.getIcon("saveDuplicates", 24);
	private static final Icon integratedReportIcon = GuiUtils.getIcon("excel", 24);

	private ActionListener alistener;
	// Menus
	private JMenu
		projectMenu,
		importMenu,
		actionMenu,
		exportMenu,
		panelsMenu;

	// Project items
	private JMenuItem
		newProjectMenuItem,
		openProjectMenuItem,
		saveProjectMenuItem,
		closeProjectMenuItem,
		exitMenuItem;

	// Import data items
	private JMenuItem
		loadDesignMenuItem,
		loadQuantDataMenuItem,
		loadQuantDataMultiFileMenuItem,
		loadLibraryMenuItem,
		loadWorklistMenuItem,
		loadMgfDataMenuItem;

	// Action items
	private JMenuItem
		findDuplicatesMenuItem,
		analyzeCorrelationsMenuItem;

	// Export data items
	private JMenuItem
		exportForRMenuItem,
		exportForMPPMenuItem,
		exportForBinnerMenuItem,
		integratedReportMenuItem,
		exportDuplicatesMenuItem;

	public MainMenuBar(ActionListener listener) {

		super();
		alistener = listener;

		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX)) {
			// Project
			projectMenu = new JMenu("Project");
			projectMenu.setPreferredSize(preferredSize);

			addItem(projectMenu, newProjectMenuItem, 
					MainActionCommands.NEW_PROJECT_COMMAND, newProjectIcon);
			addItem(projectMenu, openProjectMenuItem, 
					MainActionCommands.OPEN_PROJECT_COMMAND, openProjectIcon);
			addItem(projectMenu, saveProjectMenuItem,
					MainActionCommands.SAVE_PROJECT_COMMAND, saveProjectIcon);
			addItem(projectMenu, closeProjectMenuItem, 
					MainActionCommands.CLOSE_PROJECT_COMMAND, closeProjectIcon);

			projectMenu.addSeparator();

			addItem(projectMenu, exitMenuItem, MainActionCommands.EXIT_COMMAND, exitIcon);

			add(projectMenu);

			// Import Data
//			importMenu = new JMenu("Import data");
//			importMenu.setPreferredSize(preferredSize);
	//
//			addItem(importMenu, loadDesignMenuItem, MainActionCommands.LOAD_DESIGN_COMMAND, loadDesignIcon);
//			addItem(importMenu, loadLibraryMenuItem, MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND, loadQuantDataMultifileIcon);
//			addItem(importMenu, loadQuantDataMenuItem, MainActionCommands.LOAD_DATA_COMMAND, loadQuantDataIcon);
//			addItem(importMenu, loadLibraryMenuItem, MainActionCommands.LOAD_LIBRARY_COMMAND, LoadLibraryIcon);
//			addItem(importMenu, loadWorklistMenuItem, MainActionCommands.LOAD_WORKLIST_COMMAND, loadWorklistIcon);
//			importMenu.addSeparator();
//			addItem(importMenu, loadMgfDataMenuItem, MainActionCommands.LOAD_MGF_COMMAND, loadMgfDataIcon);

//			add(importMenu);

			// Actions
			actionMenu = new JMenu("Analysis");
			actionMenu.setPreferredSize(preferredSize);

//			addItem(actionMenu, findDuplicatesMenuItem, MainActionCommands.FIND_DUPLICATES_COMMAND, findDuplicatesIcon);
//			addItem(actionMenu, analyzeCorrelationsMenuItem, MainActionCommands.FIND_FEATURE_CORRELATIONS_COMMAND,
//					analyzeCorrelationsIcon);

			add(actionMenu);

			// Export Data
			exportMenu = new JMenu("Export data");
			exportMenu.setPreferredSize(preferredSize);

			addItem(exportMenu, exportForRMenuItem, 
					MainActionCommands.EXPORT_RESULTS_4R_COMMAND, exportForRIcon);
			addItem(exportMenu, exportForMPPMenuItem, 
					MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND, exportForMPPIcon);
			addItem(exportMenu, exportForBinnerMenuItem, 
					MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND, exportForBinnertIcon);
			addItem(exportMenu, integratedReportMenuItem, 
					MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND, integratedReportIcon);

//			exportMenu.addSeparator();
	//
//			addItem(exportMenu, exportDuplicatesMenuItem, MainActionCommands.EXPORT_DUPLICATES_COMMAND, exportDuplicatesIcon);

			add(exportMenu);
		}
		// Panels
		panelsMenu = new JMenu("Panels");
		panelsMenu.setPreferredSize(preferredSize);

		add(panelsMenu);
	}

	private void addItem(JMenu menu, JMenuItem item, MainActionCommands command, Icon defaultIcon) {

		item = new JMenuItem(command.getName());
		item.setActionCommand(command.getName());
		item.addActionListener(alistener);
		item.setIcon(defaultIcon);
		menu.add(item);
	}

	public void refreshPanelsMenu(HashMap<PanelList, Boolean> activePanelMap) {

		panelsMenu.removeAll();

		for (PanelList panel : PanelList.getPanelListForConfiguration(BuildInformation.getStartupConfiguration())) {

			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(panel.getName(), activePanelMap.get(panel));
			cbMenuItem.addActionListener(alistener);
			cbMenuItem.setActionCommand(panel.getName());
			panelsMenu.add(cbMenuItem);
		}
	}

	public void updateMenuFromProject(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}

}
