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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MGFPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon importMgfIcon = GuiUtils.getIcon("loadMgf", 24);
	private static final Icon exportMsMsIcon = GuiUtils.getIcon("exportMsMs", 24);
	private static final Icon importMgfIconSmall = GuiUtils.getIcon("loadMgf", 16);
	private static final Icon exportMsMsIconSmall = GuiUtils.getIcon("exportMsMs", 16);
	
	// Menus
	private JMenu
		importMenu,
		exportMenu;

	private JMenuItem
		importMGFMenuItem,
		exportMGFMenuItem;
	
	public MGFPanelMenuBar(ActionListener listener) {

		super(listener);

		// Import
		importMenu = new JMenu("Import");
		importMenu.setIcon(importMgfIconSmall);		
		
		importMGFMenuItem = addItem(importMenu, 
				MainActionCommands.IMPORT_MGF_COMMAND, 
				importMgfIcon);
		
		add(importMenu);
		
		// Export
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(exportMsMsIconSmall);		
		
		exportMGFMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_MSMS_COMMAND, 
				exportMsMsIcon);
		
		add(exportMenu);
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
