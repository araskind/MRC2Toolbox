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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MainToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -4599318790123180479L;

	private static final Icon saveProjectIcon = GuiUtils.getIcon("save", 32);
	private static final Icon saveProjectCopyIcon = GuiUtils.getIcon("saveAs", 32);
	private static final Icon newDataAnalysisProjectIcon = GuiUtils.getIcon("newProject", 32);
	private static final Icon openProjectIcon = GuiUtils.getIcon("open", 32);
	private static final Icon closeProjectIcon = GuiUtils.getIcon("close", 32);
	private static final Icon idTrackerLoginIcon = GuiUtils.getIcon("idTrackerLogin", 32);
	private static final Icon activeUserIcon = GuiUtils.getIcon("activeUser", 32);
	private static final Icon superUserIcon = GuiUtils.getIcon("superUser", 32);
	private static final Icon loggedOutUserIcon = GuiUtils.getIcon("loggedOutUser", 32);
	private static final Icon manageUsersIcon = GuiUtils.getIcon("manageUsers", 32);
	private static final Icon msToolboxIcon = GuiUtils.getIcon("toolbox", 32);
	private static final Icon chemModIcon = GuiUtils.getIcon("chemModList", 32);
	private static final Icon refSampleIcon = GuiUtils.getIcon("standardSample", 32);
	private static final Icon assayManagerIcon = GuiUtils.getIcon("acqMethod", 32);
	private static final Icon exportResultsIcon = GuiUtils.getIcon("export", 32);
	private static final Icon exportExcelIcon = GuiUtils.getIcon("excel", 32);
	private static final Icon exportMwTabIcon = GuiUtils.getIcon("mwTabReport", 32);
	private static final Icon cleanAndZipIcon = GuiUtils.getIcon("cleanAndZip", 32);
	private static final Icon dataFileToolsIcon = GuiUtils.getIcon("dataFileTools", 32);

	private static final Icon preferencesIcon = GuiUtils.getIcon("preferences", 32);
	private static final Icon helpIcon = GuiUtils.getIcon("help", 32);
	private static final Icon exitIcon = GuiUtils.getIcon("shutDown", 32);

	@SuppressWarnings("unused")
	private JButton
		newProjectButton,
		saveProjectButton,
		saveProjectCopyButton,
		openProjectButton,
		closeProjectButton,
		idTrackerLoginButton,
		idTrackerUserButton,
		exportResultsButton,
		exportExcelButton,
		exportMwTabButton,
		cleanAndZipButton,
		dataFileToolsButton,
		preferencesButton,
		helpButton,
		exitButton,
		toolboxButton,
		showChemModEditorButton,
		showExchangeEditorButton,
		showAssayManagerButton,
		showRefSampleEditorButton,
		dbParserButton,
		manageUsersButton;

	private JComboBox dataPipelineComboBox;
	private ItemListener iListener;

	private JLabel loggedUserLabel;

	@SuppressWarnings("unchecked")
	public MainToolbar(ActionListener commandListener) {

		super(commandListener);

		newProjectButton = GuiUtils.addButton(this, null, newDataAnalysisProjectIcon, commandListener,
				MainActionCommands.NEW_PROJECT_COMMAND.getName(),
				MainActionCommands.NEW_PROJECT_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		openProjectButton = GuiUtils.addButton(this, null, openProjectIcon, commandListener,
				MainActionCommands.OPEN_PROJECT_COMMAND.getName(),
				MainActionCommands.OPEN_PROJECT_COMMAND.getName(),
				buttonDimension);

		closeProjectButton = GuiUtils.addButton(this, null, closeProjectIcon, commandListener,
				MainActionCommands.CLOSE_PROJECT_COMMAND.getName(),
				MainActionCommands.CLOSE_PROJECT_COMMAND.getName(),
				buttonDimension);

		saveProjectButton = GuiUtils.addButton(this, null, saveProjectIcon, commandListener,
				MainActionCommands.SAVE_PROJECT_COMMAND.getName(),
				MainActionCommands.SAVE_PROJECT_COMMAND.getName(),
				buttonDimension);

		saveProjectCopyButton = GuiUtils.addButton(this, null, saveProjectCopyIcon, commandListener,
				MainActionCommands.SAVE_PROJECT_COPY_COMMAND.getName(),
				MainActionCommands.SAVE_PROJECT_COPY_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		JLabel dataPipelineLabel = new JLabel("Assay: ");
		dataPipelineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(dataPipelineLabel);

		iListener = (ItemListener) commandListener;

		dataPipelineComboBox = new JComboBox<DataPipeline>();
		dataPipelineComboBox.setFont(new Font("Tahoma", Font.BOLD, 14));
		dataPipelineComboBox.setModel(
				new SortedComboBoxModel<DataPipeline>(new DataPipeline[0]));
		dataPipelineComboBox.addItemListener(iListener);
		dataPipelineComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		dataPipelineComboBox.setMaximumSize(new Dimension(300, 30));
		dataPipelineComboBox.setMinimumSize(new Dimension(300, 30));
		add(dataPipelineComboBox);

		addSeparator(buttonDimension);

		exportResultsButton = GuiUtils.addButton(this, null, exportResultsIcon, commandListener,
				MainActionCommands.EXPORT_RESULTS_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_COMMAND.getName(), buttonDimension);

		exportExcelButton = GuiUtils.addButton(this, null, exportExcelIcon, commandListener,
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName(), buttonDimension);

		exportMwTabButton = GuiUtils.addButton(this, null, exportMwTabIcon, commandListener,
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName(), buttonDimension);
		
		dataFileToolsButton = GuiUtils.addButton(this, null, dataFileToolsIcon, commandListener,
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName(),
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName(), buttonDimension);

		cleanAndZipButton = GuiUtils.addButton(this, null, cleanAndZipIcon, commandListener,
				MainActionCommands.SHOW_RAWA_DATA_UPLOAD_PREP_DIALOG.getName(),
				MainActionCommands.SHOW_RAWA_DATA_UPLOAD_PREP_DIALOG.getName(), buttonDimension);

		addSeparator(buttonDimension);

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
			idTrackerUserButton.setActionCommand(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName());
			idTrackerUserButton.setToolTipText(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName());
			loggedUserLabel.setText("");
			manageUsersButton.setEnabled(false);
		}
		else {
			idTrackerUserButton.setIcon(activeUserIcon);
			idTrackerUserButton.setActionCommand(MainActionCommands.ID_TRACKER_LOGOUT_COMMAND.getName());
			idTrackerUserButton.setToolTipText(MainActionCommands.ID_TRACKER_LOGOUT_COMMAND.getName());
			loggedUserLabel.setText(user.getFullName());
			if(user.isSuperUser()) {
				manageUsersButton.setEnabled(true);
				idTrackerUserButton.setIcon(superUserIcon);
			}
		}		
	}
	
	private void adjustEnabledButtonsForConfiguration() {
		
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.IDTRACKER)) {
			
			newProjectButton.setEnabled(false);
			openProjectButton.setEnabled(false);
			closeProjectButton.setEnabled(false);
			saveProjectButton.setEnabled(false);
			saveProjectCopyButton.setEnabled(false);
			dataPipelineComboBox.setEnabled(false);
			exportResultsButton.setEnabled(false);
			exportExcelButton.setEnabled(false);
			exportMwTabButton.setEnabled(false);
			showRefSampleEditorButton.setEnabled(false);
			showAssayManagerButton.setEnabled(false);
		}
	}

	private void disableUnimplementedButtons() {

		//preferencesButton.setEnabled(false);
		helpButton.setEnabled(false);
	}

	public void noProject() {

		saveProjectButton.setEnabled(false);
		saveProjectCopyButton.setEnabled(false);
		newProjectButton.setEnabled(true);
		openProjectButton.setEnabled(true);
		closeProjectButton.setEnabled(false);
		exportResultsButton.setEnabled(false);
		exportExcelButton.setEnabled(false);
		dataPipelineComboBox.setEnabled(false);
	}

	public void projectActive() {

		saveProjectButton.setEnabled(true);
		saveProjectCopyButton.setEnabled(true);
		newProjectButton.setEnabled(true);
		openProjectButton.setEnabled(true);
		closeProjectButton.setEnabled(true);
		exportResultsButton.setEnabled(true);
		exportExcelButton.setEnabled(true);
		dataPipelineComboBox.setEnabled(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateGuiFromProjectAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
		if(currentProject == null) {
			noProject();
			return;
		}
		projectActive();
		if (newDataPipeline != null) {
			exportResultsButton.setEnabled(
					currentProject.dataPipelineHasData(newDataPipeline));
			exportExcelButton.setEnabled(
					currentProject.dataPipelineHasData(newDataPipeline));
		}
		else {
			exportResultsButton.setEnabled(false);
			exportExcelButton.setEnabled(false);
		}
		// Assay selector
		dataPipelineComboBox.removeItemListener(iListener);
		DataPipeline[] projectAssays = currentProject.getDataPipelines().
				toArray(new DataPipeline[currentProject.getDataPipelines().size()]);
		dataPipelineComboBox.setModel(new SortedComboBoxModel<DataPipeline>(projectAssays));
		dataPipelineComboBox.setEnabled(true);

		if (newDataPipeline != null)
			dataPipelineComboBox.setSelectedItem(newDataPipeline);
		else
			dataPipelineComboBox.setSelectedIndex(-1);

		if (projectAssays.length == 0)
			dataPipelineComboBox.setEnabled(false);

		dataPipelineComboBox.addItemListener(iListener);
	}
}
