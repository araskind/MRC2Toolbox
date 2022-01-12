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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.ms;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class WizardMsDataFileImportToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3815759313572710153L;
	
	private static final Icon addDataFilesIcon = GuiUtils.getIcon("addMultifile", 32);
	private static final Icon removeDataFilesIcon = GuiUtils.getIcon("removeMultifile", 32);
	private static final Icon clearDataIcon = GuiUtils.getIcon("clearWorklist", 32);
	private static final Icon scanCefIcon = GuiUtils.getIcon("idTrackerDatabase", 32);

	private JButton
		addDataFilesButton,
		removeDataFilesButton,
		clearDataButton,
		scanCefButton;

	public WizardMsDataFileImportToolbar(ActionListener commandListener) {
		
		super(commandListener);

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
		
		scanCefButton = GuiUtils.addButton(this, null, scanCefIcon, commandListener,
				MainActionCommands.CEF_MSMS_SCAN_RUN_COMMAND.getName(),
				MainActionCommands.CEF_MSMS_SCAN_RUN_COMMAND.getName(),
				buttonDimension);		
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

	}
}



