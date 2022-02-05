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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AcquisitionMethodManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -5152677450994169636L;
	
	private static final Icon editMethodIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 32);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 32);
	private static final Icon deleteMethodIcon = GuiUtils.getIcon("deleteDataAcquisitionMethod", 32);
	private static final Icon downloadMethodIcon = GuiUtils.getIcon("downloadDataAcquisitionMethod", 32);
	private static final Icon linkToIdExperimentIcon = GuiUtils.getIcon("linkToIdExperiment", 32);

	@SuppressWarnings("unused")
	private JButton
		addMethodButton,
		editMethodButton,
		deleteMethodButton,
		downloadMethodButton,
		linkToIdExperimentButton;

	public AcquisitionMethodManagerToolbar(ActionListener commandListener) {

		super(commandListener);
/*
		linkToIdExperimentButton = GuiUtils.addButton(this, null, linkToIdExperimentIcon, commandListener,
				MainActionCommands.LINK_ACQUISITION_METHOD_TO_EXPERIMENT_COMMAND.getName(),
				MainActionCommands.LINK_ACQUISITION_METHOD_TO_EXPERIMENT_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);*/

		addMethodButton = GuiUtils.addButton(this, null, addMethodIcon, commandListener,
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				buttonDimension);

		editMethodButton = GuiUtils.addButton(this, null, editMethodIcon, commandListener,
				MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteMethodButton = GuiUtils.addButton(this, null, deleteMethodIcon, commandListener,
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(),
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		downloadMethodButton = GuiUtils.addButton(this, null, downloadMethodIcon, commandListener,
				MainActionCommands.DOWNLOAD_ACQUISITION_METHOD_COMMAND.getName(),
				MainActionCommands.DOWNLOAD_ACQUISITION_METHOD_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
