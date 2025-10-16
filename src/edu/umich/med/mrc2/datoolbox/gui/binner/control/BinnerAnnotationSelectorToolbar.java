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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class BinnerAnnotationSelectorToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon openManagerIcon = GuiUtils.getIcon("editLibrary", 24);
	private static final Icon clearListIcon = GuiUtils.getIcon("clearWorklist", 24);

	@SuppressWarnings("unused")
	private JButton
		openManagerButton,
		clearListButton;

	public BinnerAnnotationSelectorToolbar(ActionListener commandListener) {
		
		super(commandListener);
		
		openManagerButton = GuiUtils.addButton(this, null, openManagerIcon, commandListener,
				MainActionCommands.SHOW_BINNER_ANNOTATION_LIST_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_BINNER_ANNOTATION_LIST_MANAGER_COMMAND.getName(),
				buttonDimension);

		clearListButton = GuiUtils.addButton(this, null, clearListIcon, commandListener,
				MainActionCommands.CLEAR_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				MainActionCommands.CLEAR_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
