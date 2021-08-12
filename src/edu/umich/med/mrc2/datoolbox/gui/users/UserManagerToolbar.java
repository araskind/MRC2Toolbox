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

package edu.umich.med.mrc2.datoolbox.gui.users;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class UserManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3647740149639770157L;

	private static final Icon addUserIcon = GuiUtils.getIcon("addUser", 32);
	private static final Icon editUserIcon = GuiUtils.getIcon("editUser", 32);
	private static final Icon deleteUserIcon = GuiUtils.getIcon("deleteUser", 32);

	@SuppressWarnings("unused")
	private JButton
		addUserButton,
		editUserButton,
		deleteUserButton;

	public UserManagerToolbar(ActionListener commandListener) {
		super(commandListener);

		addUserButton = GuiUtils.addButton(this, null, addUserIcon, commandListener,
				MainActionCommands.ADD_USER_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_USER_DIALOG_COMMAND.getName(), buttonDimension);

		editUserButton = GuiUtils.addButton(this, null, editUserIcon, commandListener,
				MainActionCommands.EDIT_USER_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_USER_DIALOG_COMMAND.getName(), buttonDimension);

		deleteUserButton = GuiUtils.addButton(this, null, deleteUserIcon, commandListener,
				MainActionCommands.DELETE_USER_COMMAND.getName(),
				MainActionCommands.DELETE_USER_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated User stub

	}


}





















