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

package edu.umich.med.mrc2.datoolbox.gui.mptrack;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MoTrPACDataTrackingToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2016997747952680668L;

	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 32);
	private static final Icon showMetadataIcon = GuiUtils.getIcon("metadata", 32);
	private static final Icon createFilesIcon = GuiUtils.getIcon("addMultifile", 32);
	private static final Icon uploadReportIcon = GuiUtils.getIcon("addSop", 32);

	@SuppressWarnings("unused")
	private JButton
		refreshDataButton,
		showMetadataButton,
		createEmtyFilesButton,
		uploadReportButton;

	public MoTrPACDataTrackingToolbar(ActionListener commandListener) {

		super(commandListener);

		refreshDataButton = GuiUtils.addButton(this, null, refreshDataIcon, commandListener,
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				buttonDimension);

		showMetadataButton = GuiUtils.addButton(this, null, showMetadataIcon, commandListener,
				MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND.getName(),
				MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND.getName(),
				buttonDimension);
		
		createEmtyFilesButton = GuiUtils.addButton(this, null, createFilesIcon, commandListener,
				MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName(),
				MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName(),
				buttonDimension);
		
//		addSeparator(buttonDimension);
//		
//		uploadReportButton = GuiUtils.addButton(this, null, uploadReportIcon, commandListener,
//				MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName(),
//				MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName(),
//				buttonDimension);

//		addSeparator(buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
