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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.assay;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AssayDesignToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 5936373806717737221L;

	private static final Icon editDesignIcon = GuiUtils.getIcon("editDesignSubset", 32);
	private static final Icon linkFilesIcon = GuiUtils.getIcon("link", 32);
	private static final Icon enableSelectedIcon = GuiUtils.getIcon("checkboxFull", 32);
	private static final Icon disableSelectedIcon = GuiUtils.getIcon("checkboxEmpty", 32);
	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 32);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 32);
	private static final Icon invertEnabledIcon = GuiUtils.getIcon("invertSelection", 32);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 32);
	private static final Icon deleteFilesIcon = GuiUtils.getIcon("deleteDataFile", 32);

	private JButton
		editDesignButton,
		linkFilesButton,
		enableSelectedButton,
		disableSelectedButton,
		enableAllButton,
		disableAllButton,
		invertEnabledButton,
		resetFilterButton,
		deleteFilesButton;

	public AssayDesignToolbar(ActionListener commandListener) {

		super(commandListener);

/*		editDesignButton = GuiUtils.addButton(this, null, editDesignIcon, commandListener,
				MainActionCommands.SHOW_DESIGN_TABLE_EDITOR_COMMAND.getName(),
				MainActionCommands.SHOW_DESIGN_TABLE_EDITOR_COMMAND.getName(), buttonDimension);

		linkFilesButton = GuiUtils.addButton(this, null, linkFilesIcon, commandListener,
				MainActionCommands.LINK_SAMPLES_TO_FILES_COMMAND.getName(),
				MainActionCommands.LINK_SAMPLES_TO_FILES_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);*/

		enableSelectedButton = GuiUtils.addButton(this, null, enableSelectedIcon, commandListener,
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(), buttonDimension);
		disableSelectedButton = GuiUtils.addButton(this, null, disableSelectedIcon, commandListener,
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(), buttonDimension);
		enableAllButton = GuiUtils.addButton(this, null, enableAllIcon, commandListener,
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName(), buttonDimension);
		disableAllButton = GuiUtils.addButton(this, null, disableAllIcon, commandListener,
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName(), buttonDimension);
		invertEnabledButton = GuiUtils.addButton(this, null, invertEnabledIcon, commandListener,
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(),
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(), buttonDimension);
		resetFilterButton = GuiUtils.addButton(this, null, resetFilterIcon, commandListener,
				MainActionCommands.CLEAR_SAMPLES_FILTER_COMMAND.getName(),
				MainActionCommands.CLEAR_SAMPLES_FILTER_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		deleteFilesButton = GuiUtils.addButton(this, null, deleteFilesIcon, commandListener,
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(),
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project == null) {

			editDesignButton.setEnabled(false);
			linkFilesButton.setEnabled(false);
			enableSelectedButton.setEnabled(false);
			disableSelectedButton.setEnabled(false);
			enableAllButton.setEnabled(false);
			disableAllButton.setEnabled(false);
			invertEnabledButton.setEnabled(false);
			resetFilterButton.setEnabled(false);
			deleteFilesButton.setEnabled(false);
		}
		else {
			editDesignButton.setEnabled(true);

			if(newDataPipeline != null) {

				linkFilesButton.setEnabled(true);
				enableSelectedButton.setEnabled(true);
				disableSelectedButton.setEnabled(true);
				enableAllButton.setEnabled(true);
				disableAllButton.setEnabled(true);
				invertEnabledButton.setEnabled(true);
				resetFilterButton.setEnabled(true);
				deleteFilesButton.setEnabled(true);
			}
			else {
				linkFilesButton.setEnabled(false);
				enableSelectedButton.setEnabled(false);
				disableSelectedButton.setEnabled(false);
				enableAllButton.setEnabled(false);
				disableAllButton.setEnabled(false);
				invertEnabledButton.setEnabled(false);
				resetFilterButton.setEnabled(false);
				deleteFilesButton.setEnabled(false);
			}
		}
	}
}


























