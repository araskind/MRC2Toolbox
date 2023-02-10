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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class SamplePrepManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6098358120818500637L;

	private static final Icon editSamplePrepIcon = GuiUtils.getIcon("editSamplePrep", 32);
	private static final Icon addSamplePrepIcon = GuiUtils.getIcon("addSamplePrep", 32);
	private static final Icon deleteSamplePrepIcon = GuiUtils.getIcon("deleteSamplePrep", 32);

	@SuppressWarnings("unused")
	private JButton
		addSamplePrepButton,
		editSamplePrepButton,
		deleteSamplePrepButton;

	public SamplePrepManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		addSamplePrepButton = GuiUtils.addButton(this, null, addSamplePrepIcon, commandListener,
				MainActionCommands.ADD_SAMPLE_PREP_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SAMPLE_PREP_DIALOG_COMMAND.getName(),
				buttonDimension);

		editSamplePrepButton = GuiUtils.addButton(this, null, editSamplePrepIcon, commandListener,
				MainActionCommands.EDIT_SAMPLE_PREP_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_SAMPLE_PREP_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteSamplePrepButton = GuiUtils.addButton(this, null, deleteSamplePrepIcon, commandListener,
				MainActionCommands.DELETE_SAMPLE_PREP_COMMAND.getName(),
				MainActionCommands.DELETE_SAMPLE_PREP_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
