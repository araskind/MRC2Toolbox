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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.methods;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AcquisitionMethodToolbar  extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6052615679662350236L;
	private static final Icon scanDirIcon = GuiUtils.getIcon("scanFolder", 32);
	private static final Icon linkToDataAcquisitionMethodIcon = GuiUtils.getIcon("linkToDataAcquisitionMethod", 32);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 32);
	private static final Icon deleteMethodIcon = GuiUtils.getIcon("deleteDataAcquisitionMethod", 32);
		
	@SuppressWarnings("unused")
	private JButton
		scanDirButton,
		linkExistingMethodButton,
		addMethodButton,
		deleteMethodButton;
	
	public AcquisitionMethodToolbar(ActionListener commandListener) {

		super(commandListener);

		scanDirButton = GuiUtils.addButton(this, null, scanDirIcon, commandListener,
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(), buttonDimension);
		
		linkExistingMethodButton = GuiUtils.addButton(this, null, linkToDataAcquisitionMethodIcon, commandListener,
				MainActionCommands.SHOW_DATA_ACQUISITION_SELECTOR_COMMAND.getName(),
				MainActionCommands.SHOW_DATA_ACQUISITION_SELECTOR_COMMAND.getName(), buttonDimension);

		addMethodButton = GuiUtils.addButton(this, null, addMethodIcon, commandListener,
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		deleteMethodButton = GuiUtils.addButton(this, null, deleteMethodIcon, commandListener,
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(),
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}















