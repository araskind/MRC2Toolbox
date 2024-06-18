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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.cpdid;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MultipleCompoundIdSearchToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1934511837376564647L;
	
	private static final Icon loadFilterIcon = GuiUtils.getIcon("openCompoundCollection", 32);
	private static final Icon saveFilterIcon = GuiUtils.getIcon("saveWorklist", 32);
	private static final Icon clearFilterIcon = GuiUtils.getIcon("clearWorklist", 32);	
	
	@SuppressWarnings("unused")
	private JButton
		loadFilterButton,
		saveFilterButton,
		clearFilterButton;

	public MultipleCompoundIdSearchToolbar(ActionListener commandListener) {

		super(commandListener);

		 loadFilterButton = GuiUtils.addButton(this, null, loadFilterIcon, commandListener,
				MainActionCommands.LOAD_COMPOUND_IDENTIFIERS_FILTER_FROM_DATABASE_COMMAND.getName(),
				MainActionCommands.LOAD_COMPOUND_IDENTIFIERS_FILTER_FROM_DATABASE_COMMAND.getName(),
				buttonDimension);

		 saveFilterButton = GuiUtils.addButton(this, null, saveFilterIcon, commandListener,
				MainActionCommands.SAVE_COMPOUND_IDENTIFIERS_FILTER_TO_DATABASE_COMMAND.getName(),
				MainActionCommands.SAVE_COMPOUND_IDENTIFIERS_FILTER_TO_DATABASE_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);
		
		clearFilterButton = GuiUtils.addButton(this, null, clearFilterIcon, commandListener,
				MainActionCommands.CLEAR_COMPOUND_IDENTIFIERS_FILTER_COMMAND.getName(),
				MainActionCommands.CLEAR_COMPOUND_IDENTIFIERS_FILTER_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
