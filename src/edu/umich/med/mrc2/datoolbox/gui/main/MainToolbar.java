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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MainToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -4599318790123180479L;

	private static final Icon idTrackerLoginIcon = GuiUtils.getIcon("idTrackerLogin", 32);
	private static final Icon activeUserIcon = GuiUtils.getIcon("activeUser", 32);
	private static final Icon superUserIcon = GuiUtils.getIcon("superUser", 32);
	private static final Icon loggedOutUserIcon = GuiUtils.getIcon("loggedOutUser", 32);
	private static final Icon manageUsersIcon = GuiUtils.getIcon("manageUsers", 32);
	private static final Icon organizationIcon = GuiUtils.getIcon("organization", 32);
	
	private static final Icon msToolboxIcon = GuiUtils.getIcon("toolbox", 32);
	private static final Icon chemModIcon = GuiUtils.getIcon("chemModList", 32);
	private static final Icon refSampleIcon = GuiUtils.getIcon("standardSample", 32);
	private static final Icon assayManagerIcon = GuiUtils.getIcon("acqMethod", 32);
	
	private static final Icon preferencesIcon = GuiUtils.getIcon("preferences", 32);
	private static final Icon helpIcon = GuiUtils.getIcon("help", 32);
	private static final Icon exitIcon = GuiUtils.getIcon("shutDown", 32);

	@SuppressWarnings("unused")
	private JButton
		idTrackerLoginButton,
		idTrackerUserButton,
		preferencesButton,
		helpButton,
		exitButton,
		toolboxButton,
		showChemModEditorButton,
		showExchangeEditorButton,
		showAssayManagerButton,
		showRefSampleEditorButton,
		dbParserButton,
		manageUsersButton,
		manageOrganizationsButton;

	private JLabel loggedUserLabel;

	@SuppressWarnings("unchecked")
	public MainToolbar(ActionListener commandListener) {

		super(commandListener);
		toolboxButton = GuiUtils.addButton(this, null, msToolboxIcon, commandListener,
				MainActionCommands.SHOW_MS_TOOLBOX_COMMAND.getName(),
				MainActionCommands.SHOW_MS_TOOLBOX_COMMAND.getName(), buttonDimension);

		showChemModEditorButton = GuiUtils.addButton(this, null, chemModIcon, commandListener,
				MainActionCommands.SHOW_CHEM_MOD_EDITOR_COMMAND.getName(),
				MainActionCommands.SHOW_CHEM_MOD_EDITOR_COMMAND.getName(), buttonDimension);

		showRefSampleEditorButton = GuiUtils.addButton(this, null, refSampleIcon, commandListener,
				MainActionCommands.SHOW_REFERENCE_SAMPLE_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_REFERENCE_SAMPLE_MANAGER_COMMAND.getName(), buttonDimension);

		showAssayManagerButton = GuiUtils.addButton(this, null, assayManagerIcon, commandListener,
				MainActionCommands.SHOW_ASSAY_METHOD_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_ASSAY_METHOD_MANAGER_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		preferencesButton = GuiUtils.addButton(this, null, preferencesIcon, commandListener,
				MainActionCommands.EDIT_PREFERENCES_COMMAND.getName(),
				MainActionCommands.EDIT_PREFERENCES_COMMAND.getName(), buttonDimension);

		helpButton = GuiUtils.addButton(this, null, helpIcon, commandListener,
				MainActionCommands.SHOW_HELP_COMMAND.getName(),
				MainActionCommands.SHOW_HELP_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		exitButton = GuiUtils.addButton(this, null, exitIcon, commandListener,
				MainActionCommands.EXIT_COMMAND.getName(),
				MainActionCommands.EXIT_COMMAND.getName(), buttonDimension);

		Component horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);
		
		manageOrganizationsButton = GuiUtils.addButton(this, null, organizationIcon, commandListener,
				MainActionCommands.SHOW_ORGANIZATION_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_ORGANIZATION_MANAGER_COMMAND.getName(), buttonDimension);
		
		manageUsersButton = GuiUtils.addButton(this, null, manageUsersIcon, commandListener,
				MainActionCommands.SHOW_USER_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_USER_MANAGER_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);
		
		idTrackerUserButton =
				GuiUtils.addButton(this, null, loggedOutUserIcon, commandListener, null, null, buttonDimension);
		loggedUserLabel = new JLabel("");
		add(loggedUserLabel);

		adjustEnabledButtonsForConfiguration();
		disableUnimplementedButtons();
	}

	public void setIdTrackerUser(LIMSUser user) {

		if(user == null) {
			idTrackerUserButton.setIcon(idTrackerLoginIcon);
			idTrackerUserButton.setActionCommand(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			idTrackerUserButton.setToolTipText(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName());
			loggedUserLabel.setText("");
			manageOrganizationsButton.setEnabled(false);
			manageUsersButton.setEnabled(false);
		}
		else {
			idTrackerUserButton.setIcon(activeUserIcon);
			idTrackerUserButton.setActionCommand(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName());
			idTrackerUserButton.setToolTipText(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName());
			loggedUserLabel.setText(user.getFullName());
			if(user.isSuperUser()) {
				manageUsersButton.setEnabled(true);
				manageOrganizationsButton.setEnabled(true);
				idTrackerUserButton.setIcon(superUserIcon);
			}
		}		
	}
	
	private void adjustEnabledButtonsForConfiguration() {
		
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.IDTRACKER)) {			
			showRefSampleEditorButton.setEnabled(false);
			showAssayManagerButton.setEnabled(false);
		}
	}

	private void disableUnimplementedButtons() {

		//preferencesButton.setEnabled(false);
		helpButton.setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {

	}
}
