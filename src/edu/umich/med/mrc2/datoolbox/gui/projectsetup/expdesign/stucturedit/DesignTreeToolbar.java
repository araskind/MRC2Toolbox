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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.stucturedit;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DesignTreeToolbar  extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -4386626090499956041L;

	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 24);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 24);

	@SuppressWarnings("unused")
	private JButton
		enableAllButton,
		disableAllButton;

	public DesignTreeToolbar(ActionListener commandListener) {

		super(commandListener);

		enableAllButton = GuiUtils.addButton(this, null, enableAllIcon, commandListener,
				MainActionCommands.ENABLE_ALL_LEVELS_COMMAND.getName(),
				MainActionCommands.ENABLE_ALL_LEVELS_COMMAND.getName(), buttonDimension);

		disableAllButton = GuiUtils.addButton(this, null, disableAllIcon, commandListener,
				MainActionCommands.DISABLE_ALL_LEVELS_COMMAND.getName(),
				MainActionCommands.DISABLE_ALL_LEVELS_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}


}
