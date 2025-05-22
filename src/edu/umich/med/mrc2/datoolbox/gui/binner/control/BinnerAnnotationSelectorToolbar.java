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
	private static final Icon openListIcon = GuiUtils.getIcon("openCollection", 24);
	private static final Icon newListIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon editListIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon deleteListIcon = GuiUtils.getIcon("deleteCollection", 24);

	@SuppressWarnings("unused")
	private JButton
		openListButton,
		newListButton,
		editListButton,
		deleteListButton;

	public BinnerAnnotationSelectorToolbar(ActionListener commandListener) {
		
		super(commandListener);
		
		openListButton = GuiUtils.addButton(this, null, openListIcon, commandListener,
				MainActionCommands.OPEN_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				MainActionCommands.OPEN_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				buttonDimension);

		newListButton = GuiUtils.addButton(this, null, newListIcon, commandListener,
				MainActionCommands.NEW_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				MainActionCommands.NEW_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				buttonDimension);
		
		editListButton = GuiUtils.addButton(this, null, editListIcon, commandListener,
				MainActionCommands.EDIT_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				MainActionCommands.EDIT_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				buttonDimension);

		deleteListButton = GuiUtils.addButton(this, null, deleteListIcon, commandListener,
				MainActionCommands.DELETE_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				MainActionCommands.DELETE_BINNER_ANNOTATION_LIST_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
