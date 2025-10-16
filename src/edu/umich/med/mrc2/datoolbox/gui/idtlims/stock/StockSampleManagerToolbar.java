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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class StockSampleManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2845445464629073118L;

	private static final Icon rsEditIcon = GuiUtils.getIcon("editStandardSample", 32);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addStandardSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteStandardSample", 32);

	@SuppressWarnings("unused")
	private JButton
		addRefSampleButton,
		editRefSampleButton,
		deleteRefSampleButton;

	public StockSampleManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		addRefSampleButton = GuiUtils.addButton(this, null, addSampleIcon, commandListener,
				MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(), buttonDimension);

		editRefSampleButton = GuiUtils.addButton(this, null, rsEditIcon, commandListener,
				MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(), buttonDimension);

		deleteRefSampleButton = GuiUtils.addButton(this, null, deleteSampleIcon, commandListener,
				MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName(),
				MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
