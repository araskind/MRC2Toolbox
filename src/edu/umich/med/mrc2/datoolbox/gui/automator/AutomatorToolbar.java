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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AutomatorToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2367262613952160648L;

	private static final Icon runIcon = GuiUtils.getIcon("run", 32);
	private static final Icon reRunIcon = GuiUtils.getIcon("rerun", 32);
	private static final Icon stopIcon = GuiUtils.getIcon("stop", 32);

	@SuppressWarnings("unused")
	private JButton runButton, rerunButton, stopRunButton;

	public AutomatorToolbar(ActionListener commandListener) {

		super(commandListener);

		runButton = GuiUtils.addButton(this, null, runIcon, commandListener,
				MainActionCommands.RUN_AUTOMATOR_COMMAND.getName(),
				MainActionCommands.RUN_AUTOMATOR_COMMAND.getName(), buttonDimension);

		rerunButton = GuiUtils.addButton(this, null, reRunIcon, commandListener,
				MainActionCommands.RERUN_FAILED_ASSAY_COMMAND.getName(),
				MainActionCommands.RERUN_FAILED_ASSAY_COMMAND.getName(), buttonDimension);

		stopRunButton = GuiUtils.addButton(this, null, stopIcon, commandListener,
				MainActionCommands.STOP_AUTOMATOR_COMMAND.getName(),
				MainActionCommands.STOP_AUTOMATOR_COMMAND.getName(), buttonDimension);
	}

	public void disableRerunButton() {

		rerunButton.setEnabled(false);
	}

	public void enableRerunButton() {

		rerunButton.setEnabled(true);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
