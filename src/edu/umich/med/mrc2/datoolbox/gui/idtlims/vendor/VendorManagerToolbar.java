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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.vendor;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class VendorManagerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1649139864778572915L;
	
	private static final Icon addVendorIcon = GuiUtils.getIcon("addVendor", 32);
	private static final Icon editVendorIcon = GuiUtils.getIcon("editVendor", 32);
	private static final Icon deleteVendorIcon = GuiUtils.getIcon("deleteVendor", 32);

	@SuppressWarnings("unused")
	private JButton
		addVendorButton,
		editVendorButton,
		deleteVendorButton;
	
	public VendorManagerToolbar(ActionListener commandListener) {
		
		super(commandListener);

		addVendorButton = GuiUtils.addButton(this, null, addVendorIcon, commandListener,
				MainActionCommands.ADD_VENDOR_COMMAND.getName(),
				MainActionCommands.ADD_VENDOR_COMMAND.getName(), buttonDimension);

		editVendorButton = GuiUtils.addButton(this, null, editVendorIcon, commandListener,
				MainActionCommands.EDIT_VENDOR_COMMAND.getName(),
				MainActionCommands.EDIT_VENDOR_COMMAND.getName(), buttonDimension);

		deleteVendorButton = GuiUtils.addButton(this, null, deleteVendorIcon, commandListener,
				MainActionCommands.DELETE_VENDOR_COMMAND.getName(),
				MainActionCommands.DELETE_VENDOR_COMMAND.getName(), buttonDimension);
	}
	
	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
