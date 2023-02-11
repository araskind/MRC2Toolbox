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

package edu.umich.med.mrc2.datoolbox.gui.qc;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class QCToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6774926739017316437L;

	static final Icon calcStatsIcon = GuiUtils.getIcon("stats", 32);
	static final Icon pcaIcon = GuiUtils.getIcon("scatterPlot3D", 32);

	private JButton
		calcStatsButton,
		calcPcaButton;

	public QCToolbar(ActionListener commandListener) {

		super(commandListener);

		calcStatsButton = GuiUtils.addButton(this, null, calcStatsIcon, commandListener,
				MainActionCommands.CALC_DATASET_STATS_COMMAND.getName(),
				MainActionCommands.CALC_DATASET_STATS_COMMAND.getName(), buttonDimension);

		calcPcaButton = GuiUtils.addButton(this, null, pcaIcon, commandListener,
				MainActionCommands.CALC_DATASET_PCA_COMMAND.getName(),
				MainActionCommands.CALC_DATASET_PCA_COMMAND.getName(), buttonDimension);

		// addSeparator(buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub
		if(experiment != null) {

			if(newDataPipeline != null) {

				if(!experiment.getMsFeatureSetsForDataPipeline(newDataPipeline).isEmpty())
					setToolbarActive(true);
			}
			else
				setToolbarActive(false);
		}
		else
			setToolbarActive(false);
	}

	private void setToolbarActive(boolean active) {

		calcStatsButton.setEnabled(active);
		calcPcaButton.setEnabled(active);
	}


}
