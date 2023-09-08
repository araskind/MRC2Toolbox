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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MzFequencyResultsToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -7270184336598621941L;

	private static final Icon saveIcon = GuiUtils.getIcon("save", 32);
	private static final Icon createNewCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon addToExistingCollectionIcon = GuiUtils.getIcon("addCollection", 32);
	private static final Icon idFollowupStepManagerIcon = GuiUtils.getIcon("idFollowupStepManager", 32);
	private static final Icon standardFeatureAnnotationManagerIcon = GuiUtils.getIcon("editCollection", 32);


	@SuppressWarnings("unused")
	private JButton
		saveResultsToFileButton,
		createNewCollectionButton,
		addToExistingCollectionButton,
		assignIdFollowUpStepsButton,
		assignStandardAnnotationButton;

	public MzFequencyResultsToolbar(ActionListener listener, boolean enableTrackerCommands) {

		super(listener);

		saveResultsToFileButton = GuiUtils.addButton(this, null, saveIcon, commandListener,
				MainActionCommands.SAVE_MZ_FREQUENCY_ANALYSIS_RESULTS_COMMAND.getName(),
				MainActionCommands.SAVE_MZ_FREQUENCY_ANALYSIS_RESULTS_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		createNewCollectionButton = GuiUtils.addButton(
				this, null, createNewCollectionIcon, commandListener,
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(),
				MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName(),
				buttonDimension);

		addToExistingCollectionButton = GuiUtils.addButton(
				this, null, addToExistingCollectionIcon, commandListener,
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(),
				MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName(),
				buttonDimension);

		if(enableTrackerCommands) {
			
			addSeparator(buttonDimension);

			assignIdFollowUpStepsButton = GuiUtils.addButton(
					this, null, idFollowupStepManagerIcon, commandListener,
					MainActionCommands.ADD_ID_FOLLOWUP_STEP_DIALOG_COMMAND.getName(),
					MainActionCommands.ADD_ID_FOLLOWUP_STEP_DIALOG_COMMAND.getName(),
					buttonDimension);

			assignStandardAnnotationButton = GuiUtils.addButton(
					this, null, standardFeatureAnnotationManagerIcon, commandListener,
					MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND.getName(),
					MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND.getName(),
					buttonDimension);
		}
	} 

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
