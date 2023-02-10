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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class StudyManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2845445464629073118L;

	private static final Icon addStudyIcon = GuiUtils.getIcon("addFactor", 32);
	private static final Icon editStudyIcon = GuiUtils.getIcon("editFactor", 32);	
	private static final Icon deleteStudyIcon = GuiUtils.getIcon("deleteFactor", 32);

	@SuppressWarnings("unused")
	private JButton
		addStudyButton,
		editStudyButton,
		deleteStudyButton;

	public StudyManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		addStudyButton = GuiUtils.addButton(this, null, addStudyIcon, commandListener,
				MainActionCommands.ADD_MOTRPAC_STUDY_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_MOTRPAC_STUDY_DIALOG_COMMAND.getName(), buttonDimension);

		editStudyButton = GuiUtils.addButton(this, null, editStudyIcon, commandListener,
				MainActionCommands.EDIT_MOTRPAC_STUDY_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_MOTRPAC_STUDY_DIALOG_COMMAND.getName(), buttonDimension);

		deleteStudyButton = GuiUtils.addButton(this, null, deleteStudyIcon, commandListener,
				MainActionCommands.DELETE_MOTRPAC_STUDY_COMMAND.getName(),
				MainActionCommands.DELETE_MOTRPAC_STUDY_COMMAND.getName(), buttonDimension);
		deleteStudyButton.setEnabled(false);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
