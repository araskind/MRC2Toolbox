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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.report;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ReportTableToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2845445464629073118L;

	private static final Icon uploadReportIcon = GuiUtils.getIcon("addSop", 32);
	private static final Icon editStudyIcon = GuiUtils.getIcon("editSop", 32);	
	private static final Icon deleteStudyIcon = GuiUtils.getIcon("deleteSop", 32);

	@SuppressWarnings("unused")
	private JButton
		uploadReportButton,
		editReportMetadataButton,
		deleteReportButton;

	public ReportTableToolbar(ActionListener commandListener) {

		super(commandListener);

		uploadReportButton = GuiUtils.addButton(this, null, uploadReportIcon, commandListener,
				MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		editReportMetadataButton = GuiUtils.addButton(this, null, editStudyIcon, commandListener,
				MainActionCommands.EDIT_MOTRPAC_REPORT_METADATA_COMMAND.getName(),
				MainActionCommands.EDIT_MOTRPAC_REPORT_METADATA_COMMAND.getName(), buttonDimension);

		deleteReportButton = GuiUtils.addButton(this, null, deleteStudyIcon, commandListener,
				MainActionCommands.CONFIRM_DELETE_MOTRPAC_REPORT_COMMAND.getName(),
				MainActionCommands.CONFIRM_DELETE_MOTRPAC_REPORT_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}

