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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.data;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDTrackerMultiFileImportToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3815759313572710153L;
	
	private static final Icon selectLibraryIcon = GuiUtils.getIcon("loadLibrary", 32);
	private static final Icon addDataFilesIcon = GuiUtils.getIcon("addMultifile", 32);
	private static final Icon removeDataFilesIcon = GuiUtils.getIcon("removeMultifile", 32);
	private static final Icon clearDataIcon = GuiUtils.getIcon("clearWorklist", 32);
	private static final Icon editReferenceSamplesIcon = GuiUtils.getIcon("standardSample", 32);
	private static final Icon loadDesignIcon = GuiUtils.getIcon("loadDesign", 32);

	private JButton
		dataAnalysisPipelineButton,
		selectLibraryButton,
		addDataFilesButton,
		removeDataFilesButton,
		clearDataButton,
		editReferenceSamplesButton,
		loadDesignButton;

	public IDTrackerMultiFileImportToolbar(ActionListener commandListener) {
		
		super(commandListener);

		selectLibraryButton = GuiUtils.addButton(this, null, selectLibraryIcon, commandListener,
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName(),
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		addDataFilesButton = GuiUtils.addButton(this, null, addDataFilesIcon, commandListener,
				MainActionCommands.ADD_DATA_FILES_COMMAND.getName(),
				MainActionCommands.ADD_DATA_FILES_COMMAND.getName(), buttonDimension);

		removeDataFilesButton = GuiUtils.addButton(this, null, removeDataFilesIcon, commandListener,
				MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName(),
				MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName(), buttonDimension);

		clearDataButton = GuiUtils.addButton(this, null, clearDataIcon, commandListener,
				MainActionCommands.CLEAR_DATA_COMMAND.getName(),
				MainActionCommands.CLEAR_DATA_COMMAND.getName(), buttonDimension);
		
		addSeparator(buttonDimension);

		editReferenceSamplesButton = GuiUtils.addButton(this, null, editReferenceSamplesIcon, commandListener,
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		loadDesignButton = GuiUtils.addButton(this, null, loadDesignIcon, commandListener,
				MainActionCommands.LOAD_DATA_FILE_SAMPLE_MAP_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FILE_SAMPLE_MAP_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		boolean active = true;
		if(project == null)
			active = false;

		dataAnalysisPipelineButton.setEnabled(active);
		selectLibraryButton.setEnabled(active);
		addDataFilesButton.setEnabled(active);
		removeDataFilesButton.setEnabled(active);
		clearDataButton.setEnabled(active);
		editReferenceSamplesButton.setEnabled(active);
		loadDesignButton.setEnabled(active);
	}
}



