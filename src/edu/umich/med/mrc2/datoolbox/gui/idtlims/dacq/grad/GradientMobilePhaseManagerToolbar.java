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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class GradientMobilePhaseManagerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8245791796145041236L;
	
	private static final Icon addMobilePhaseIcon = GuiUtils.getIcon("newMobilePhase", 32);
	private static final Icon editMobilePhaseIcon = GuiUtils.getIcon("editMobilePhase", 32);
	private static final Icon deleteMobilePhaseIcon = GuiUtils.getIcon("deleteMobilePhase", 32);

	@SuppressWarnings("unused")
	private JButton
		addMobilePhaseButton,
		editMobilePhaseButton,
		deleteMobilePhaseButton;

	public GradientMobilePhaseManagerToolbar(ActionListener commandListener) {

		super(commandListener);

//		addMobilePhaseButton = GuiUtils.addButton(this, null, addMobilePhaseIcon, commandListener,
//				MainActionCommands.ADD_MOBILE_PHASE_DIALOG_COMMAND.getName(),
//				MainActionCommands.ADD_MOBILE_PHASE_DIALOG_COMMAND.getName(),
//				buttonDimension);

		editMobilePhaseButton = GuiUtils.addButton(this, null, editMobilePhaseIcon, commandListener,
				MainActionCommands.SET_MOBILE_PHASE_FOR_GRADIENT_CHANEL_DIALOG_COMMAND.getName(),
				MainActionCommands.SET_MOBILE_PHASE_FOR_GRADIENT_CHANEL_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteMobilePhaseButton = GuiUtils.addButton(this, null, deleteMobilePhaseIcon, commandListener,
				MainActionCommands.CLEAR_MOBILE_PHASE_FOR_GRADIENT_CHANEL_COMMAND.getName(),
				MainActionCommands.CLEAR_MOBILE_PHASE_FOR_GRADIENT_CHANEL_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
