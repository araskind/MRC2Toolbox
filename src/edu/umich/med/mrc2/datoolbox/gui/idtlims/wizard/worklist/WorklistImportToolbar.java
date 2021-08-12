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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.worklist;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class WorklistImportToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6098358120818500637L;

	private static final Icon scanDirIcon = GuiUtils.getIcon("scanFolder", 32);
	private static final Icon addFromDirIcon = GuiUtils.getIcon("addFromFolder", 32);
	private static final Icon deleteIcon = GuiUtils.getIcon("delete", 32);
	private static final Icon clearWorklistIcon = GuiUtils.getIcon("clearWorklist", 32);

	private JButton
		scanDirButton,
		addFromDirButton,
		deleteSelectedFilesButton,
		clearWorklistButton;

	public WorklistImportToolbar(ActionListener commandListener) {

		super(commandListener);

		scanDirButton = GuiUtils.addButton(this, null, scanDirIcon, commandListener,
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(), buttonDimension);

		addFromDirButton = GuiUtils.addButton(this, null, addFromDirIcon, commandListener,
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);
		
		deleteSelectedFilesButton = GuiUtils.addButton(this, null, deleteIcon, commandListener,
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(),
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(), buttonDimension);

		clearWorklistButton = GuiUtils.addButton(this, null, clearWorklistIcon, commandListener,
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(),
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

	}

	public void setButtonsEnabled(boolean enabled) {

		scanDirButton.setEnabled(enabled);
		addFromDirButton.setEnabled(enabled);
		deleteSelectedFilesButton.setEnabled(enabled);
		clearWorklistButton.setEnabled(enabled);
	}
}
