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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MsMsToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 9119755898839111343L;

	private static final Icon importFromXMLIcon = GuiUtils.getIcon("importFromXML", 32);
	private static final Icon importFromMSPIcon = GuiUtils.getIcon("importFromMSP", 32);
	private static final Icon filterMsMsIcon = GuiUtils.getIcon("filterMsMs", 32);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 32);
	private static final Icon acceptMsMsIcon = GuiUtils.getIcon("acceptMsMs", 32);
	private static final Icon deleteMsMsIcon = GuiUtils.getIcon("deleteMsMs", 32);

	@SuppressWarnings("unused")
	private JButton
		importFromXMLButton,
		importFromMSPButton,
		filterMsMsButton,
		resetFilterButton,
		acceptMsMsButton,
		deleteMsMsButton;

	public MsMsToolbar(ActionListener commandListener) {

		super(commandListener);

		importFromXMLButton = GuiUtils.addButton(this, null, importFromXMLIcon, commandListener,
				MainActionCommands.SHOW_MSMS_IMPORT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_MSMS_IMPORT_DIALOG_COMMAND.getName(),
				buttonDimension);

//		importFromMSPButton = GuiUtils.addButton(this, null, importFromMSPIcon, commandListener,
//				MainActionCommands.IMPORT_MSMS_FROM_MSP_COMMAND.getName(),
//				MainActionCommands.IMPORT_MSMS_FROM_MSP_COMMAND.getName(),
//				buttonDimension);

		addSeparator(buttonDimension);

		filterMsMsButton = GuiUtils.addButton(this, null, filterMsMsIcon, commandListener,
				MainActionCommands.SHOW_MSMS_DATA_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_MSMS_DATA_FILTER_COMMAND.getName(),
				buttonDimension);

		resetFilterButton = GuiUtils.addButton(this, null, resetFilterIcon, commandListener,
				MainActionCommands.RESET_MSMS_FILTER_COMMAND.getName(),
				MainActionCommands.RESET_MSMS_FILTER_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		acceptMsMsButton = GuiUtils.addButton(this, null, acceptMsMsIcon, commandListener,
				MainActionCommands.ACCEPT_MSMS_COMMAND.getName(),
				MainActionCommands.ACCEPT_MSMS_COMMAND.getName(),
				buttonDimension);

		deleteMsMsButton = GuiUtils.addButton(this, null, deleteMsMsIcon, commandListener,
				MainActionCommands.DELETE_MSMS_COMMAND.getName(),
				MainActionCommands.DELETE_MSMS_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
