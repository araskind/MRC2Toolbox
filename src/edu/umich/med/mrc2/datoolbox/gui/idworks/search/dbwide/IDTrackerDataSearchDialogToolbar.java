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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDTrackerDataSearchDialogToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1934511837376564647L;
	
	private static final Icon saveSearchQueryIcon = GuiUtils.getIcon("saveWorklist", 32);
	private static final Icon loadSearchQueryIcon = GuiUtils.getIcon("loadList", 32);
	
	@SuppressWarnings("unused")
	private JButton
		loadSearchQueryButton,
		saveSearchQueryButton;

	public IDTrackerDataSearchDialogToolbar(ActionListener commandListener) {

		super(commandListener);

		loadSearchQueryButton = GuiUtils.addButton(this, null, loadSearchQueryIcon, commandListener,
				MainActionCommands.SHOW_IDTRACKER_SAVED_QUERIES_LIST_COMMAND.getName(),
				MainActionCommands.SHOW_IDTRACKER_SAVED_QUERIES_LIST_COMMAND.getName(),
				buttonDimension);

		saveSearchQueryButton = GuiUtils.addButton(this, null, saveSearchQueryIcon, commandListener,
				MainActionCommands.SHOW_IDTRACKER_SAVE_QUERY_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_IDTRACKER_SAVE_QUERY_DIALOG_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
