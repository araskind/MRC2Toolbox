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

package edu.umich.med.mrc2.datoolbox.gui.lims;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class METLIMSMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon componentIcon = GuiUtils.getIcon("experimentDatabase", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);
	private static final Icon sendDesignToProjectIcon = GuiUtils.getIcon("sendDesignToProject", 24);
	private static final Icon sendDesignToProjectIconSmall = GuiUtils.getIcon("sendDesignToProject", 16);
	private static final Icon syncDbIcon = GuiUtils.getIcon("synchronizeDb", 24);
	private static final Icon createExperimentDirIcon = GuiUtils.getIcon("newProject", 24);
	private static final Icon deleteExperimentIcon = GuiUtils.getIcon("deleteCollection", 24);
	private static final Icon utilsIconSmall = GuiUtils.getIcon("preferences", 16);

	// Menus
	private JMenu
		limsMenu,
		utilitiesMenu,
		dataRetrievalMenu;

	// LIMS items
	private JMenuItem
		refreshLimsDataMenuItem,
		syncDatabaseMenuItem;
	
	// Utils items
	private JMenuItem
		createExperimentDirectoryMenuItem,
		deleteExperimentMenuItem;
	
	// Data retr items
	private JMenuItem
		limsExpToExperimentMenuItem;

	public METLIMSMenuBar(ActionListener listener) {

		super(listener);

		// Analysis
		limsMenu = new JMenu("Database");
		limsMenu.setIcon(componentIcon);
		
		refreshLimsDataMenuItem = addItem(limsMenu, 
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND, 
				refreshDataIcon);
		syncDatabaseMenuItem = addItem(limsMenu, 
				MainActionCommands.SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND, 
				syncDbIcon);
		
		add(limsMenu);
		
		utilitiesMenu = new JMenu("Utilities");
		utilitiesMenu.setIcon(utilsIconSmall);
		
		createExperimentDirectoryMenuItem = addItem(utilitiesMenu, 
				MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND, 
				createExperimentDirIcon);
		
		utilitiesMenu.addSeparator();
		
		deleteExperimentMenuItem = addItem(utilitiesMenu, 
				MainActionCommands.DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND, 
				deleteExperimentIcon);
		
		add(utilitiesMenu);	
		
		dataRetrievalMenu = new JMenu("Data retrieval");
		dataRetrievalMenu.setIcon(sendDesignToProjectIconSmall);
		
		limsExpToExperimentMenuItem = addItem(dataRetrievalMenu, 
				MainActionCommands.SEND_DESIGN_TO_EXPERIMENT_COMMAND, 
				sendDesignToProjectIcon);
		
		add(dataRetrievalMenu);	
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
