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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class SamplePrepSopToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -5449850073679206046L;
	private static final Icon addProtocolIcon = GuiUtils.getIcon("addSop", 32);
	private static final Icon deleteProtocolIcon = GuiUtils.getIcon("deleteSop", 32);

	@SuppressWarnings("unused")
	private JButton
		addProtocolButton,
		deleteProtocolButton;

	public SamplePrepSopToolbar(ActionListener commandListener) {

		super(commandListener);

		addProtocolButton = GuiUtils.addButton(this, null, addProtocolIcon, commandListener,
				MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteProtocolButton = GuiUtils.addButton(this, null, deleteProtocolIcon, commandListener,
				MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName(),
				MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName(),
				buttonDimension);
	}
	
	public void setEditable(boolean b) {
		addProtocolButton.setEnabled(b);
		deleteProtocolButton.setEnabled(b);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
