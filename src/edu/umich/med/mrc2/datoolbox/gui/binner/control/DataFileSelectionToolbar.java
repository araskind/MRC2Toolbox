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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataFileSelectionToolbar extends CommonToolbar {
	
	private static final long serialVersionUID = 1L;
	
	private static final Icon disableControlsIcon = GuiUtils.getIcon("deleteStandardSample", 24);
	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 24);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 24);
	private static final Icon invertEnabledIcon = GuiUtils.getIcon("invertSelection", 24);	
	
	private JButton disabbleControlsButton;
	private JButton enableSelectedButton;
	private JButton disableSelectedButton;
	private JButton invertSelectedButton;

	public DataFileSelectionToolbar(ActionListener listener) {
		
		super(listener);

		disabbleControlsButton = GuiUtils.addButton(this, null, disableControlsIcon, listener,
				MainActionCommands.DISABLE_REFERENCE_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_REFERENCE_SAMPLES_COMMAND.getName(),
				buttonDimension);
		
		enableSelectedButton = GuiUtils.addButton(this, null, enableAllIcon, listener,
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(),
				buttonDimension);
		
		disableSelectedButton = GuiUtils.addButton(this, null, disableAllIcon, listener,
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(),
				buttonDimension);
		
		invertSelectedButton = GuiUtils.addButton(this, null, invertEnabledIcon, listener,
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(),
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
