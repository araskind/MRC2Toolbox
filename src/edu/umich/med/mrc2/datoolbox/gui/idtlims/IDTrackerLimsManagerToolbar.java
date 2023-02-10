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

package edu.umich.med.mrc2.datoolbox.gui.idtlims;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDTrackerLimsManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 7736408583080084241L;

	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 32);
	private static final Icon newCdpIdProjectIcon = GuiUtils.getIcon("newIdProject", 32);
	private static final Icon newCdpIdExperimentIcon = GuiUtils.getIcon("newIdExperiment", 32);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 32);
	private static final Icon loadAvgMS1DataFileIcon = GuiUtils.getIcon("importTextfile", 32);
	private static final Icon importBinnerDataIcon = GuiUtils.getIcon("importBins", 32);
	private static final Icon scanCefIcon = GuiUtils.getIcon("scanCef", 32);
	private static final Icon wizardIcon = GuiUtils.getIcon("wizard", 32);

	@SuppressWarnings("unused")
	private JButton
		refreshDataButton,
		wizzardButton,
		newCpdIdProjectButton,
		newCdpIdExperimentButton,
		loadMsFeaturesButton,
		loadAvgMS1DataFileButton,
		importBinnerDataButton,
		iddaButton,
		scanCefButton;

	public IDTrackerLimsManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		refreshDataButton = GuiUtils.addButton(this, null, refreshDataIcon, commandListener,
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);
		
		wizzardButton = GuiUtils.addButton(this, null, wizardIcon, commandListener,
				MainActionCommands.SHOW_DATA_UPLOAD_WIZARD_COMMAND.getName(),
				MainActionCommands.SHOW_DATA_UPLOAD_WIZARD_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);

		newCpdIdProjectButton = GuiUtils.addButton(this, null, newCdpIdProjectIcon, commandListener,
				MainActionCommands.NEW_IDTRACKER_PROJECT_DIALOG_COMMAND.getName(),
				MainActionCommands.NEW_IDTRACKER_PROJECT_DIALOG_COMMAND.getName(),
				buttonDimension);

		newCdpIdExperimentButton = GuiUtils.addButton(this, null, newCdpIdExperimentIcon, commandListener,
				MainActionCommands.NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		loadAvgMS1DataFileButton = GuiUtils.addButton(this, null, loadAvgMS1DataFileIcon, commandListener,
				MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName(),
				MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName(),
				buttonDimension);
		loadAvgMS1DataFileButton.setEnabled(false);
		
		importBinnerDataButton = GuiUtils.addButton(this, null, importBinnerDataIcon, commandListener,
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(),
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(),
				buttonDimension);
		importBinnerDataButton.setEnabled(false);

		addSeparator(buttonDimension);
		
		scanCefButton = GuiUtils.addButton(this, null, scanCefIcon, commandListener,
				MainActionCommands.CEF_MSMS_SCAN_SETUP_COMMAND.getName(),
				MainActionCommands.CEF_MSMS_SCAN_SETUP_COMMAND.getName(),
				buttonDimension);

		loadMsFeaturesButton = GuiUtils.addButton(this, null, loadMultiFileIcon, commandListener,
				MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName(),
				MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
