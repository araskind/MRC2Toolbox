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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class WorklistToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2988029284847867573L;

	private static final Icon loadWorklistFromFileIcon = GuiUtils.getIcon("loadWorklist", 32);
	private static final Icon addWorklistFromFileIcon = GuiUtils.getIcon("addWorklist", 32);
	private static final Icon scanDirIcon = GuiUtils.getIcon("scanFolder", 32);
	private static final Icon addFromDirIcon = GuiUtils.getIcon("addFromFolder", 32);
	private static final Icon clearWorklistIcon = GuiUtils.getIcon("clearWorklist", 32);
	private static final Icon saveWorklistIcon = GuiUtils.getIcon("saveWorklist", 32);
	private static final Icon copyWorklistToClipboardIcon = GuiUtils.getIcon("copyWorklistToClipboard", 32);
	private static final Icon manifestIcon = GuiUtils.getIcon("manifest", 32);
	private static final Icon refreshIcon = GuiUtils.getIcon("rerun", 32);	
	private static final Icon extractWorklistIcon = GuiUtils.getIcon("extractList", 32);
	private static final Icon sampleWarningIcon = GuiUtils.getIcon("sampleWarning", 32);
	
	private JButton
		loadWorklistButton,
		addWorklistButton,
		scanDirButton,
		addFromDirButton,
		clearWorklistButton,
		refreshButton,
		saveWorklistButton,
		copyWorklistToClipboardButton,
		manifestButton,
		extractWorklistFromFolderButton,
		sampleWarningButton;

	public WorklistToolbar(ActionListener commandListener) {

		super(commandListener);

		scanDirButton = GuiUtils.addButton(this, null, scanDirIcon, commandListener,
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(), buttonDimension);

		addFromDirButton = GuiUtils.addButton(this, null, addFromDirIcon, commandListener,
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		loadWorklistButton = GuiUtils.addButton(this, null, loadWorklistFromFileIcon, commandListener,
				MainActionCommands.LOAD_WORKLIST_COMMAND.getName(),
				MainActionCommands.LOAD_WORKLIST_COMMAND.getName(),
				buttonDimension);

		addWorklistButton = GuiUtils.addButton(this, null, addWorklistFromFileIcon, commandListener,
				MainActionCommands.ADD_WORKLIST_COMMAND.getName(),
				MainActionCommands.ADD_WORKLIST_COMMAND.getName(),
				buttonDimension);

		loadWorklistButton.setEnabled(false);
		addWorklistButton.setEnabled(false);

		addSeparator(buttonDimension);

//		refreshButton = GuiUtils.addButton(this, null, refreshIcon, commandListener,
//				MainActionCommands.REFRESH_WORKLIST.getName(),
//				MainActionCommands.REFRESH_WORKLIST.getName(),
//				buttonDimension);
		
		sampleWarningButton = GuiUtils.addButton(this, null, sampleWarningIcon, commandListener,
				MainActionCommands.CHECK_WORKLIST_FOR_MISSING_DATA.getName(),
				MainActionCommands.CHECK_WORKLIST_FOR_MISSING_DATA.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		saveWorklistButton = GuiUtils.addButton(this, null, saveWorklistIcon, commandListener,
				MainActionCommands.SAVE_WORKLIST_COMMAND.getName(),
				MainActionCommands.SAVE_WORKLIST_COMMAND.getName(),
				buttonDimension);

		copyWorklistToClipboardButton = GuiUtils.addButton(this, null, copyWorklistToClipboardIcon, commandListener,
				MainActionCommands.COPY_WORKLIST_COMMAND.getName(),
				MainActionCommands.COPY_WORKLIST_COMMAND.getName(),
				buttonDimension);

		manifestButton = GuiUtils.addButton(this, null, manifestIcon, commandListener,
				MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND.getName(),
				MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		clearWorklistButton = GuiUtils.addButton(this, null, clearWorklistIcon, commandListener,
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(),
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(), buttonDimension);
		
		addSeparator(buttonDimension);
		
		extractWorklistFromFolderButton = GuiUtils.addButton(this, null, extractWorklistIcon, commandListener,
				MainActionCommands.EXTRACT_WORKLIST_COMMAND.getName(),
				MainActionCommands.EXTRACT_WORKLIST_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project != null) {

			if(newDataPipeline != null) {

				if(project.dataPipelineHasData(newDataPipeline))
					setButtonsEnabled(true);
				else
					setButtonsEnabled(false);
			}
			else
				setButtonsEnabled(false);
		}
		else
			setButtonsEnabled(false);
	}

	private void setButtonsEnabled(boolean enabled) {

		loadWorklistButton.setEnabled(enabled);
		addWorklistButton.setEnabled(enabled);
		scanDirButton.setEnabled(enabled);
		addFromDirButton.setEnabled(enabled);
		clearWorklistButton.setEnabled(enabled);
		copyWorklistToClipboardButton.setEnabled(enabled);
	}
}














