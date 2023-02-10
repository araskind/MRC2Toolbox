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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.stucturedit;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExpDesignToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -4654617025349286263L;

	private static final Icon saveIcon = GuiUtils.getIcon("save", 32);

	private JButton saveButton;

	public ExpDesignToolbar(ActionListener commandListener) {

		super(commandListener);

		saveButton = GuiUtils.addButton(this, null, saveIcon, commandListener,
				MainActionCommands.SAVE_DESIGN_SUBSET_EDIT_COMMAND.getName(),
				MainActionCommands.SAVE_DESIGN_SUBSET_EDIT_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		boolean active = true;
		if(project == null)
			active = false;

		saveButton.setEnabled(active);
	}
}
