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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class InstrunmentManagerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 796835553547436979L;
	
	private static final Icon addInstrumentIcon = GuiUtils.getIcon("addInstrument", 32);
	private static final Icon editInstrumentIcon = GuiUtils.getIcon("editInstrument", 32);	
	private static final Icon deleteInstrumentIcon = GuiUtils.getIcon("deleteInstrument", 32);

	@SuppressWarnings("unused")
	private JButton
		addInstrumentButton,
		editInstrumentButton,
		deleteInstrumentButton;

	public InstrunmentManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		addInstrumentButton = GuiUtils.addButton(this, null, addInstrumentIcon, commandListener,
				MainActionCommands.ADD_INSTRUMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_INSTRUMENT_DIALOG_COMMAND.getName(),
				buttonDimension);

		editInstrumentButton = GuiUtils.addButton(this, null, editInstrumentIcon, commandListener,
				MainActionCommands.EDIT_INSTRUMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_INSTRUMENT_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteInstrumentButton = GuiUtils.addButton(this, null, deleteInstrumentIcon, commandListener,
				MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName(),
				MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
