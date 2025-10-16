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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureSubsetDialogToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -7038541792222478852L;

	private static final Icon saveEditIcon = GuiUtils.getIcon("save", 32);
	private static final Icon deleteIcon = GuiUtils.getIcon("deleteFeature", 32);

	@SuppressWarnings("unused")
	private JButton
		saveChangesButton,
		deleteFeatureButton;

	public FeatureSubsetDialogToolbar(ActionListener commandListener) {

		super(commandListener);

		saveChangesButton = GuiUtils.addButton(this, null, saveEditIcon, commandListener,
				MainActionCommands.SAVE_CHANGES_TO_FEATURE_SUBSET_COMMAND.getName(),
				MainActionCommands.SAVE_CHANGES_TO_FEATURE_SUBSET_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		deleteFeatureButton = GuiUtils.addButton(this, null, deleteIcon, commandListener,
				MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName(),
				MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
