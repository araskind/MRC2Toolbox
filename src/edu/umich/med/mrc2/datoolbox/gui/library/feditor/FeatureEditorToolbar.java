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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureEditorToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3148449059909652202L;

	private static final Icon saveFeatureIcon = GuiUtils.getIcon("save", 32);
	private static final Icon resetIcon = GuiUtils.getIcon("rerun", 32);
	private static final Icon duplicateIcon = GuiUtils.getIcon("duplicates", 32);

	@SuppressWarnings("unused")
	private JButton
		saveFeatureButton,
		resetButton,
		duplicateButton;

	public FeatureEditorToolbar(ActionListener commandListener) {

		super(commandListener);

		saveFeatureButton = GuiUtils.addButton(this, null, saveFeatureIcon, commandListener,
				MainActionCommands.EDIT_LIBRARY_FEATURE_COMMAND.getName(),
				MainActionCommands.EDIT_LIBRARY_FEATURE_COMMAND.getName(),
				buttonDimension);

		resetButton = GuiUtils.addButton(this, null, resetIcon, commandListener,
				MainActionCommands.UNDO_LIBRARY_FEATURE_EDIT_COMMAND.getName(),
				MainActionCommands.UNDO_LIBRARY_FEATURE_EDIT_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		duplicateButton = GuiUtils.addButton(this, null, duplicateIcon, commandListener,
				MainActionCommands.DUPLICATE_LIBRARY_FEATURE_COMMAND.getName(),
				MainActionCommands.DUPLICATE_LIBRARY_FEATURE_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);
		//duplicateButton.setEnabled(false);
	}

	public void setDuplicateButtonStatus(boolean enabled) {
		duplicateButton.setEnabled(enabled);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
