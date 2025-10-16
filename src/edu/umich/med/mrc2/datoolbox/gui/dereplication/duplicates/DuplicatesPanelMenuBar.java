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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DuplicatesPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	protected static final Icon filterIcon = GuiUtils.getIcon("filterClusters", 24);
	protected static final Icon filterIconSmall = GuiUtils.getIcon("filterClusters", 16);
	protected static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 24);
	private static final Icon exportDuplicatesIcon = GuiUtils.getIcon("saveDuplicates", 24);
	private static final Icon exportDuplicatesIconSmall = GuiUtils.getIcon("saveDuplicates", 16);
	private static final Icon showDuplicatesIcon = GuiUtils.getIcon("findDuplicates", 24);
	private static final Icon showDuplicatesIconSmall = GuiUtils.getIcon("findDuplicates", 16);
	private static final Icon checkDuplicateNamesIcon = GuiUtils.getIcon("checkDuplicateNames", 24);

	// Menus
	private JMenu
		analysisMenu,
		searchMenu,
		exportMenu;

	// Analysis items
	private JMenuItem
		findDuplicatesMenuItem,
		mergeDuplicatesMenuItem,
		checkNamesMenuItem;
	
	// Search items
	private JMenuItem
		filterMenuItem,
		resetFilterMenuItem;

	// Export items
	private JMenuItem
		exportDuplicatesMenuItem;
	
	public DuplicatesPanelMenuBar(ActionListener listener) {

		super(listener);

		// Analysis
		analysisMenu = new JMenu("Analysis");
		analysisMenu.setIcon(showDuplicatesIconSmall);
		
		findDuplicatesMenuItem = addItem(analysisMenu, 
				MainActionCommands.SHOW_FIND_DUPLICATES_DIALOG_COMMAND, 
				showDuplicatesIcon);
		mergeDuplicatesMenuItem = addItem(analysisMenu, 
				MainActionCommands.SHOW_DUPLICATES_MERGE_DIALOG_COMMAND, 
				clearDuplicatesIcon);
		
		analysisMenu.addSeparator();
		
		checkNamesMenuItem = addItem(analysisMenu, 
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND, 
				checkDuplicateNamesIcon);

		add(analysisMenu);

		//	Search
		searchMenu = new JMenu("Filter");
		searchMenu.setIcon(filterIconSmall);
		
		filterMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND, 
				filterIcon);
		resetFilterMenuItem = addItem(searchMenu, 
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND, 
				resetFilterIcon);
		
		add(searchMenu);
		
		//	Export
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(exportDuplicatesIconSmall);

		exportDuplicatesMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_DUPLICATES_COMMAND, 
				exportDuplicatesIcon);
		
		add(exportMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
