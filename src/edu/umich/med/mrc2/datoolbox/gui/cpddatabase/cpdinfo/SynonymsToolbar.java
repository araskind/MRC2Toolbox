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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class SynonymsToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6769971456346089051L;
	private static final Icon editSynonymIcon = GuiUtils.getIcon("editSynonym", 24);
	private static final Icon addSynonymIcon = GuiUtils.getIcon("addSynonym", 24);
	private static final Icon deleteSynonymIcon = GuiUtils.getIcon("deleteSynonym", 24);

	@SuppressWarnings("unused")
	private JButton
		addSynonymButton,
		editSynonymButton,
		deleteSynonymButton;

	public SynonymsToolbar(ActionListener commandListener) {
		super(commandListener);

		addSynonymButton = GuiUtils.addButton(this, null, addSynonymIcon, commandListener,
				MainActionCommands.ADD_SYNONYM_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SYNONYM_DIALOG_COMMAND.getName(),
				buttonDimension);

		editSynonymButton = GuiUtils.addButton(this, null, editSynonymIcon, commandListener,
				MainActionCommands.EDIT_SYNONYM_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_SYNONYM_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteSynonymButton = GuiUtils.addButton(this, null, deleteSynonymIcon, commandListener,
				MainActionCommands.DELETE_SYNONYM_COMMAND.getName(),
				MainActionCommands.DELETE_SYNONYM_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
