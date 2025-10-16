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

package edu.umich.med.mrc2.datoolbox.gui.qc;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class QCPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon calcStatsIconSmall = GuiUtils.getIcon("stats", 16);
	static final Icon calcStatsIcon = GuiUtils.getIcon("stats", 24);
	static final Icon pcaIcon = GuiUtils.getIcon("scatterPlot3D", 24);
	
	// Menus
	private JMenu
		toolsMenu;

	// Tools items
	private JMenuItem
		calculateFileStatisticsMenuItem,
		runPCAMenuItem;

	public QCPanelMenuBar(ActionListener listener) {

		super(listener);

		// Design
		toolsMenu = new JMenu("Analysis");
		toolsMenu.setIcon(calcStatsIconSmall);		
		
		calculateFileStatisticsMenuItem = addItem(toolsMenu, 
				MainActionCommands.CALC_DATASET_STATS_COMMAND, 
				calcStatsIcon);
		runPCAMenuItem = addItem(toolsMenu, 
				MainActionCommands.CALC_DATASET_PCA_COMMAND, 
				pcaIcon);
		
		add(toolsMenu);
	}

	public void updateMenuFromExperiment(
			DataAnalysisProject experiment, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
