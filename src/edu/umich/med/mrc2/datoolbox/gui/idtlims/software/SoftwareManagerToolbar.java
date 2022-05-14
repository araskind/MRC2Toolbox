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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.software;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class SoftwareManagerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1649139864778572915L;
	
	private static final Icon addSoftwareIcon = GuiUtils.getIcon("addSoftware", 32);
	private static final Icon editSoftwareIcon = GuiUtils.getIcon("editSoftware", 32);
	private static final Icon deleteSoftwareIcon = GuiUtils.getIcon("deleteSoftware", 32);

	@SuppressWarnings("unused")
	private JButton
		addSoftwareButton,
		editSoftwareButton,
		deleteSoftwareButton;
	
	public SoftwareManagerToolbar(ActionListener commandListener) {
		
		super(commandListener);

		addSoftwareButton = GuiUtils.addButton(this, null, addSoftwareIcon, commandListener,
				MainActionCommands.ADD_SOFTWARE_COMMAND.getName(),
				MainActionCommands.ADD_SOFTWARE_COMMAND.getName(), buttonDimension);

		editSoftwareButton = GuiUtils.addButton(this, null, editSoftwareIcon, commandListener,
				MainActionCommands.EDIT_SOFTWARE_COMMAND.getName(),
				MainActionCommands.EDIT_SOFTWARE_COMMAND.getName(), buttonDimension);

		deleteSoftwareButton = GuiUtils.addButton(this, null, deleteSoftwareIcon, commandListener,
				MainActionCommands.DELETE_SOFTWARE_COMMAND.getName(),
				MainActionCommands.DELETE_SOFTWARE_COMMAND.getName(), buttonDimension);
	}
	
	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
