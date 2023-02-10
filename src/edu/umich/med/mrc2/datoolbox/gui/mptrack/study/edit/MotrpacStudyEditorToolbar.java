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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MotrpacStudyEditorToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 796835553547436979L;
	
	private static final Icon editExperimentsIcon = GuiUtils.getIcon("idExperiment", 32);
	private static final Icon editAssaysIcon = GuiUtils.getIcon("dataAnalysisPipeline", 32);	
	private static final Icon editTissuesIcon = GuiUtils.getIcon("tissueCode", 32);

	@SuppressWarnings("unused")
	private JButton
		editExperimentsButton,
		editAssaysButton,
		editTissuesButton;

	public MotrpacStudyEditorToolbar(ActionListener commandListener) {

		super(commandListener);

		editExperimentsButton = GuiUtils.addButton(this, null, editExperimentsIcon, commandListener,
				MainActionCommands.EDIT_MOTRPAC_STUDY_EXPERIMENTS_COMMAND.getName(),
				MainActionCommands.EDIT_MOTRPAC_STUDY_EXPERIMENTS_COMMAND.getName(),
				buttonDimension);

		editAssaysButton = GuiUtils.addButton(this, null, editAssaysIcon, commandListener,
				MainActionCommands.EDIT_MOTRPAC_STUDY_ASSAYS_COMMAND.getName(),
				MainActionCommands.EDIT_MOTRPAC_STUDY_ASSAYS_COMMAND.getName(),
				buttonDimension);

		editTissuesButton = GuiUtils.addButton(this, null, editTissuesIcon, commandListener,
				MainActionCommands.EDIT_MOTRPAC_STUDY_TISSUES_COMMAND.getName(),
				MainActionCommands.EDIT_MOTRPAC_STUDY_TISSUES_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
