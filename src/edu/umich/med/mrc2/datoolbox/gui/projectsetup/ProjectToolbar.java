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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ProjectToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5411471054983549523L;
	
	private static final Icon saveProjectIcon = GuiUtils.getIcon("save", 32);
	private static final Icon saveProjectCopyIcon = GuiUtils.getIcon("saveAs", 32);
	private static final Icon newDataAnalysisProjectIcon = GuiUtils.getIcon("newProject", 32);
	private static final Icon openProjectIcon = GuiUtils.getIcon("open", 32);
	private static final Icon closeProjectIcon = GuiUtils.getIcon("close", 32);
	
	@SuppressWarnings("unused")
	private JButton
		newProjectButton,
		saveProjectButton,
		saveProjectCopyButton,
		openProjectButton,
		closeProjectButton;

	public ProjectToolbar(ActionListener commandListener) {
		
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
		
		adjustEnabledButtonsForConfiguration();
	}
	
	private void adjustEnabledButtonsForConfiguration() {
		
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.IDTRACKER)) {
			
			newProjectButton.setEnabled(false);
			openProjectButton.setEnabled(false);
			closeProjectButton.setEnabled(false);
			saveProjectButton.setEnabled(false);
			saveProjectCopyButton.setEnabled(false);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateGuiFromProjectAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project == null) {
			noProject();
			return;
		}
		projectActive();
	}
	
	public void noProject() {

		saveProjectButton.setEnabled(false);
		saveProjectCopyButton.setEnabled(false);
		newProjectButton.setEnabled(true);
		openProjectButton.setEnabled(true);
		closeProjectButton.setEnabled(false);
	}

	public void projectActive() {

		saveProjectButton.setEnabled(true);
		saveProjectCopyButton.setEnabled(true);
		newProjectButton.setEnabled(true);
		openProjectButton.setEnabled(true);
		closeProjectButton.setEnabled(true);
	}

	public void setActionListener(ActionListener listener) {

		newProjectButton.addActionListener(listener);
		saveProjectButton.addActionListener(listener);
		saveProjectCopyButton.addActionListener(listener);
		openProjectButton.addActionListener(listener);
		closeProjectButton.addActionListener(listener);
	}
}



