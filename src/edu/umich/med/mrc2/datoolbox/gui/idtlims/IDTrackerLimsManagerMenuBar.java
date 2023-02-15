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

package edu.umich.med.mrc2.datoolbox.gui.idtlims;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDTrackerLimsManagerMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	
	private static final Icon limsIconSmall = GuiUtils.getIcon("experimentDatabase", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);
	private static final Icon newCdpIdProjectIcon = GuiUtils.getIcon("newIdProject", 24);
	private static final Icon newCdpIdExperimentIcon = GuiUtils.getIcon("newIdExperiment", 24);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 24);
	private static final Icon loadMultiFileIconSmall = GuiUtils.getIcon("importMultifile", 16);
	private static final Icon loadAvgMS1DataFileIcon = GuiUtils.getIcon("importTextfile", 24);
	private static final Icon importBinnerDataIcon = GuiUtils.getIcon("importBins", 24);
	private static final Icon scanCefIcon = GuiUtils.getIcon("scanCef", 24);
	private static final Icon wizardIcon = GuiUtils.getIcon("wizard", 24);
	// Menus
	private JMenu
		limsMenu,
		dataLoadMenu;

	// LIMS items
	private JMenuItem
		refreshLimsDataMenuItem,
		newProjectMenuItem,
		newExperimentMenuItem;
	
	// Load items
	private JMenuItem
		wizardMenuItem,
		prescanCefMenuItem,
		uploadMSMSFromCefMenuItem,
		uploadMS1FromCefMenuItem,
		uploadBinnerDataMenuItem;

	public IDTrackerLimsManagerMenuBar(ActionListener listener) {

		super(listener);

		// Analysis
		limsMenu = new JMenu("LIMS");
		limsMenu.setIcon(limsIconSmall);
		
		refreshLimsDataMenuItem = addItem(limsMenu, 
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND, 
				refreshDataIcon);
		
		limsMenu.addSeparator();

		newProjectMenuItem = addItem(limsMenu, 
				MainActionCommands.NEW_IDTRACKER_PROJECT_DIALOG_COMMAND, 
				newCdpIdProjectIcon);
		newExperimentMenuItem = addItem(limsMenu, 
				MainActionCommands.NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND, 
				newCdpIdExperimentIcon);
		
		add(limsMenu);
		
		dataLoadMenu = new JMenu("Data upload");
		dataLoadMenu.setIcon(loadMultiFileIconSmall);
		
		wizardMenuItem = addItem(dataLoadMenu, 
				MainActionCommands.SHOW_DATA_UPLOAD_WIZARD_COMMAND, 
				wizardIcon);
		
		dataLoadMenu.addSeparator();
		
		prescanCefMenuItem = addItem(dataLoadMenu, 
				MainActionCommands.CEF_MSMS_SCAN_SETUP_COMMAND, 
				scanCefIcon);
		uploadMSMSFromCefMenuItem = addItem(dataLoadMenu, 
				MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND, 
				loadMultiFileIcon);
		uploadMS1FromCefMenuItem = addItem(dataLoadMenu, 
				MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND, 
				loadAvgMS1DataFileIcon);
		
		dataLoadMenu.addSeparator();
		
		uploadBinnerDataMenuItem = addItem(dataLoadMenu, 
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG, 
				importBinnerDataIcon);
		uploadBinnerDataMenuItem.setEnabled(false);
		
		//	add(dataLoadMenu);		
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
