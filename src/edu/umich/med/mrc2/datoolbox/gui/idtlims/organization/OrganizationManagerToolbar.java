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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.organization;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class OrganizationManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6399678438757605837L;

	private static final Icon editOrganizationIcon = GuiUtils.getIcon("editOrganization", 32);
	private static final Icon addOrganizationIcon = GuiUtils.getIcon("newOrganization", 32);
	private static final Icon deleteOrganizationIcon = GuiUtils.getIcon("deleteOrganization", 32);

	@SuppressWarnings("unused")
	private JButton
		addOrganizationButton,
		editOrganizationButton,
		deleteOrganizationButton;

	public OrganizationManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		addOrganizationButton = GuiUtils.addButton(this, null, addOrganizationIcon, commandListener,
				MainActionCommands.ADD_ORGANIZATION_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_ORGANIZATION_DIALOG_COMMAND.getName(),
				buttonDimension);

		editOrganizationButton = GuiUtils.addButton(this, null, editOrganizationIcon, commandListener,
				MainActionCommands.EDIT_ORGANIZATION_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_ORGANIZATION_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteOrganizationButton = GuiUtils.addButton(this, null, deleteOrganizationIcon, commandListener,
				MainActionCommands.DELETE_ORGANIZATION_COMMAND.getName(),
				MainActionCommands.DELETE_ORGANIZATION_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
