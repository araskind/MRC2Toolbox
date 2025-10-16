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
	
	private static final Icon dataSetAlignmentIconSmall = GuiUtils.getIcon("alignment", 16);	
	private static final Icon dataSetAlignmentIcon = GuiUtils.getIcon("newAlignment", 24);
	private static final Icon deleteDataSetAlignmentIcon = GuiUtils.getIcon("deleteAlignment", 24);
	private static final Icon dataSetAlignmentManagerIcon = GuiUtils.getIcon("processingMethod", 24);
	private static final Icon showUnmatchedIcon = GuiUtils.getIcon("showUnknowns", 24);
	private static final Icon mergeIcon = GuiUtils.getIcon("merge", 24);
	private static final Icon showSummaryIcon = GuiUtils.getIcon("calcStats", 24);
	private static final Icon exportSummaryIcon = GuiUtils.getIcon("extractList", 24);
	private static final Icon generateMetabCombinerScriptIcon = GuiUtils.getIcon("rScriptMC", 24);
	private static final Icon clusterSetIcon = GuiUtils.getIcon("clusterFeatureTable", 24);
		
	// Menus
	private JMenu
		integrationMenu,
		alignmentMenu,
		searchMenu;

	// Integration items
	private JMenuItem
		integrationSetupMenuItem,
		acceptListMenuItem,
		deleteIntegrationMenuItem,
		manageDataSetsMenuItem;
	
	// Alignment items
	private JMenuItem
		dataSetAlignmentSetupMenuItem,
		deleteDataSetAlignmentMenuItem,
		dataSetAlignmentManagerMenuItem,
		showUnmatchedFromReferenceMenuItem,
		showAlignmentSummaryMenuItem,
		exportAlignmentSummaryMenuItem,
		mergeSelectedFeaturesMenuItem,
		generateMetabCombinerScriptMenuItem;
	
	// Search items
	private JMenuItem
		filterMenuItem,
		resetFilterMenuItem;
	
	public DataIntegrationPanelMenuBar(ActionListener listener) {

		super(listener);

		// Analysis
		integrationMenu = new JMenu("Data integration");
		integrationMenu.setIcon(collectIDDataIconSmall);
		
		integrationSetupMenuItem = addItem(integrationMenu, 
				MainActionCommands.DATA_INTEGRATION_DIALOG_COMMAND, 
				collectIDDataIcon);
		acceptListMenuItem = addItem(integrationMenu, 
				MainActionCommands.ACCEPT_CLEAN_ID_LIST_COMMAND, 
				acceptListIcon);
		
		integrationMenu.addSeparator();
		
		deleteIntegrationMenuItem = addItem(integrationMenu, 
				MainActionCommands.DELETE_INTEGRATION_SET_COMMAND, 
				deleteDataSetIcon);
		
		integrationMenu.addSeparator();
		
		manageDataSetsMenuItem = addItem(integrationMenu, 
				MainActionCommands.SHOW_FEATURE_CLUSTER_SET_MANAGER, 
				clusterSetIcon);
		
		add(integrationMenu);
				
		alignmentMenu = new JMenu("Data set alignment");
		alignmentMenu.setIcon(dataSetAlignmentIconSmall);
		
		dataSetAlignmentManagerMenuItem = addItem(alignmentMenu, 
				MainActionCommands.SHOW_DATA_PIPELINE_ALIGNMENT_MANAGER_COMMAND, 
				dataSetAlignmentManagerIcon);
		
		alignmentMenu.addSeparator();
		
		dataSetAlignmentSetupMenuItem = addItem(alignmentMenu, 
				MainActionCommands.DATA_SET_ALIGNMENT_SETUP_COMMAND, 
				dataSetAlignmentIcon);	
		mergeSelectedFeaturesMenuItem = addItem(alignmentMenu, 
				MainActionCommands.MERGE_DATA_FOR_MARKED_FEATURES_COMMAND, 
				mergeIcon);
		
		alignmentMenu.addSeparator();
		
		deleteDataSetAlignmentMenuItem = addItem(alignmentMenu, 
				MainActionCommands.DELETE_DATA_PIPELINE_ALIGNMENT_RESULTS_COMMAND, 
				deleteDataSetAlignmentIcon);
		
		alignmentMenu.addSeparator();
		
		showAlignmentSummaryMenuItem = addItem(alignmentMenu, 
				MainActionCommands.SHOW_DATASET_ALIGNMENT_SUMMARY_COMMAND, 
				showSummaryIcon);
		exportAlignmentSummaryMenuItem = addItem(alignmentMenu, 
				MainActionCommands.EXPORT_DATASET_ALIGNMENT_SUMMARY_COMMAND, 
				exportSummaryIcon);
		
		alignmentMenu.addSeparator();
		
		generateMetabCombinerScriptMenuItem = addItem(alignmentMenu, 
				MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND, 
				generateMetabCombinerScriptIcon);
			
		add(alignmentMenu);

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
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
