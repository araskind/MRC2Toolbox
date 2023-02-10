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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.design;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class RDPExperimentDesignEditorToolbar  extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 228367951475752870L;

	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 32);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 32);
	private static final Icon addStockSampleIcon = GuiUtils.getIcon("addStandardSample", 32);

	@SuppressWarnings("unused")
	private JButton
		addSampleButton,
		deleteSampleButton,
		editSampleButton,
		addRefSampleButton;

	public RDPExperimentDesignEditorToolbar(ActionListener commandListener) {

		super(commandListener);

		addSampleButton = GuiUtils.addButton(this, null, addSampleIcon, commandListener,
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(),
				buttonDimension);

		editSampleButton = GuiUtils.addButton(this, null, editSampleIcon, commandListener,
				MainActionCommands.EDIT_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_SAMPLE_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteSampleButton = GuiUtils.addButton(this, null, deleteSampleIcon, commandListener,
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		addRefSampleButton = GuiUtils.addButton(this, null, addStockSampleIcon, commandListener,
				MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(), buttonDimension);
	}
	
	public void setDesignEditable(boolean editable) {
		
		addSampleButton.setEnabled(editable);
		deleteSampleButton.setEnabled(editable);
		editSampleButton.setEnabled(editable);
		addRefSampleButton.setEnabled(editable);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
