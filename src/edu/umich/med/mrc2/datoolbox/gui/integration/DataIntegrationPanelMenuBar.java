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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataIntegrationPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	protected static final Icon filterIcon = GuiUtils.getIcon("filterClusters", 24);
	protected static final Icon filterIconSmall = GuiUtils.getIcon("filterClusters", 16);
	protected static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon collectIDDataIcon = GuiUtils.getIcon("createIntegration", 24);
	private static final Icon collectIDDataIconSmall = GuiUtils.getIcon("createIntegration", 16);

	private static final Icon deleteDataSetIcon = GuiUtils.getIcon("deleteIntegration", 24);
	private static final Icon acceptListIcon = GuiUtils.getIcon("acceptList", 24);

	// Menus
	private JMenu
		integrationMenu,
		searchMenu;

	// Integration items
	private JMenuItem
		integrationSetupMenuItem,
		acceptListMenuItem,
		deleteIntegrationMenuItem;
	
	// Search items
	private JMenuItem
		filterMenuItem,
		resetFilterMenuItem;
	
	public DataIntegrationPanelMenuBar(ActionListener listener) {

		super(listener);

		// Analysis
		integrationMenu = new JMenu("Data integration");
		integrationMenu.setIcon(collectIDDataIconSmall);
		
		integrationSetupMenuItem = addItem(integrationMenu, integrationSetupMenuItem, 
				MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND, 
				collectIDDataIcon);
		acceptListMenuItem = addItem(integrationMenu, acceptListMenuItem, 
				MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND, 
				acceptListIcon);
		
		integrationMenu.addSeparator();
		
		deleteIntegrationMenuItem = addItem(integrationMenu, deleteIntegrationMenuItem, 
				MainActionCommands.DELETE_INTEGRATION_SET_COMMAND, 
				deleteDataSetIcon);

		add(integrationMenu);

		//	Search
		searchMenu = new JMenu("Filter");
		searchMenu.setIcon(filterIconSmall);
		
		filterMenuItem = addItem(searchMenu, filterMenuItem, 
				MainActionCommands.SHOW_CLUSTER_FILTER_COMMAND, 
				filterIcon);
		resetFilterMenuItem = addItem(searchMenu, resetFilterMenuItem, 
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND, 
				resetFilterIcon);
		
		add(searchMenu);
	}

	public void updateMenuFromProject(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
