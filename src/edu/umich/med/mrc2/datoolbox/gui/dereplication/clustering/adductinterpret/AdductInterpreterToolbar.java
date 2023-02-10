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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.adductinterpret;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AdductInterpreterToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6930248345987438576L;

	private static final Icon addRepeatIcon = GuiUtils.getIcon("addRepeat", 32);
	private static final Icon addExchangeIcon = GuiUtils.getIcon("addExchange", 32);
	private static final Icon recalculateIcon = GuiUtils.getIcon("calculateAnnotation", 32);
	private static final Icon acceptResultsIcon = GuiUtils.getIcon("saveAnnotation", 32);
	private static final Icon clearModificationsIcon = GuiUtils.getIcon("clearAnnotation", 32);

	@SuppressWarnings("unused")
	private JButton
		addRepeatButton,
		addExchangeButton,
		recalculateButton,
		acceptResultsButton,
		clearModificationsButton;

	public AdductInterpreterToolbar(ActionListener commandListener) {

		super(commandListener);

		recalculateButton = GuiUtils.addButton(this, null, recalculateIcon, commandListener,
				AdductInterpreterDialog.RECALCULATE_COMMAND, AdductInterpreterDialog.RECALCULATE_COMMAND,
				buttonDimension);

		clearModificationsButton = GuiUtils.addButton(this, null, clearModificationsIcon, commandListener,
				AdductInterpreterDialog.CLEAR_MODS_COMMAND, AdductInterpreterDialog.CLEAR_MODS_COMMAND,
				buttonDimension);

		acceptResultsButton = GuiUtils.addButton(this, null, acceptResultsIcon, commandListener,
				AdductInterpreterDialog.ACCEPT_RESULTS_COMMAND, AdductInterpreterDialog.ACCEPT_RESULTS_COMMAND,
				buttonDimension);

		addSeparator(buttonDimension);

		addRepeatButton = GuiUtils.addButton(this, null, addRepeatIcon, commandListener,
				AdductInterpreterDialog.ADD_REPEAT_COMMAND, AdductInterpreterDialog.ADD_REPEAT_COMMAND,
				buttonDimension);
		addRepeatButton.setEnabled(false);

		addExchangeButton = GuiUtils.addButton(this, null, addExchangeIcon, commandListener,
				AdductInterpreterDialog.ADD_EXCHANGE_COMMAND, AdductInterpreterDialog.ADD_EXCHANGE_COMMAND,
				buttonDimension);
		addExchangeButton.setEnabled(false);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
