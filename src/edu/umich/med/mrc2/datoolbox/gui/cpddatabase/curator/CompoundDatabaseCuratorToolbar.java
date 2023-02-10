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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CompoundDatabaseCuratorToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1934511837376564647L;
	
	private static final Icon simpleDataPullIcon = GuiUtils.getIcon("dbLookup", 32);
//	private static final Icon editIdStatusIcon = GuiUtils.getIcon("editIdStatus", 32);
//	private static final Icon deleteIdStatusIcon = GuiUtils.getIcon("deleteIdStatus", 32);	
	
	@SuppressWarnings("unused")
	private JButton
		simpleDataPullButton,
		editIdStatusButton,
		deleteIdStatusButton;

	public CompoundDatabaseCuratorToolbar(ActionListener commandListener) {

		super(commandListener);

		simpleDataPullButton = GuiUtils.addButton(this, null, simpleDataPullIcon, commandListener,
				MainActionCommands.SHOW_SIMPLE_REDUNDANT_COMPOUND_DATA_PULL_DIALOG.getName(),
				MainActionCommands.SHOW_SIMPLE_REDUNDANT_COMPOUND_DATA_PULL_DIALOG.getName(),
				buttonDimension);

//		editIdStatusButton = GuiUtils.addButton(this, null, editIdStatusIcon, commandListener,
//				MainActionCommands.EDIT_ID_LEVEL_DIALOG_COMMAND.getName(),
//				MainActionCommands.EDIT_ID_LEVEL_DIALOG_COMMAND.getName(),
//				buttonDimension);
//
//		deleteIdStatusButton = GuiUtils.addButton(this, null, deleteIdStatusIcon, commandListener,
//				MainActionCommands.DELETE_ID_LEVEL_COMMAND.getName(),
//				MainActionCommands.DELETE_ID_LEVEL_COMMAND.getName(),
//				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
