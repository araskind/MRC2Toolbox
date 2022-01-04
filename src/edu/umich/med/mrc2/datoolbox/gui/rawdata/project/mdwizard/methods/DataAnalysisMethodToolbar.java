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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.mdwizard.methods;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataAnalysisMethodToolbar  extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6052615679662350236L;

	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataProcessingMethod", 32);
	private static final Icon linkToDataAcquisitionMethodIcon = GuiUtils.getIcon("linkToDataProcessingMethod", 32);
	private static final Icon deleteMethodIcon = GuiUtils.getIcon("deleteDataProcessingMethod", 32);
		
	@SuppressWarnings("unused")
	private JButton
		addMethodFromDatabaseButton,
		addMethodButton,
		deleteMethodButton;
	
	public DataAnalysisMethodToolbar(ActionListener commandListener) {

		super(commandListener);

		addMethodFromDatabaseButton = GuiUtils.addButton(this, null, linkToDataAcquisitionMethodIcon, commandListener,
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_FROM_DATABASE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_FROM_DATABASE_DIALOG_COMMAND.getName(),
				buttonDimension);

		addMethodButton = GuiUtils.addButton(this, null, addMethodIcon, commandListener,
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		deleteMethodButton = GuiUtils.addButton(this, null, deleteMethodIcon, commandListener,
				MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName(),
				MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}















