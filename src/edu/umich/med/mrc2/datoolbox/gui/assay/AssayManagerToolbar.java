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

package edu.umich.med.mrc2.datoolbox.gui.assay;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AssayManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3647740149639770157L;

	private static final Icon addMethodIcon = GuiUtils.getIcon("addAcqMethod", 32);
	private static final Icon editMethodIcon = GuiUtils.getIcon("editAcqMethod", 32);
	private static final Icon deleteMethodIcon = GuiUtils.getIcon("deleteAcqMethod", 32);

	@SuppressWarnings("unused")
	private JButton
		addMethodButton,
		editMethodButton,
		deleteMethodButton;

	public AssayManagerToolbar(ActionListener commandListener) {
		super(commandListener);

		addMethodButton = GuiUtils.addButton(this, null, addMethodIcon, commandListener,
				MainActionCommands.ADD_ASSAY_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_ASSAY_METHOD_DIALOG_COMMAND.getName(), buttonDimension);

		editMethodButton = GuiUtils.addButton(this, null, editMethodIcon, commandListener,
				MainActionCommands.EDIT_ASSAY_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_ASSAY_METHOD_DIALOG_COMMAND.getName(), buttonDimension);

		deleteMethodButton = GuiUtils.addButton(this, null, deleteMethodIcon, commandListener,
				MainActionCommands.DELETE_ASSAY_METHOD_COMMAND.getName(),
				MainActionCommands.DELETE_ASSAY_METHOD_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}


}





















