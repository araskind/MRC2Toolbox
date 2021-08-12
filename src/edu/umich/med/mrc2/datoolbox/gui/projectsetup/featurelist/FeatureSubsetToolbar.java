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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.featurelist;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureSubsetToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3682733503030220337L;

	private static final Icon newSubsetIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon addSelectedFeaturesToSubsetIcon = GuiUtils.getIcon("addSelectedToCollection", 32);
	private static final Icon addFilteredFeaturesToSubsetIcon = GuiUtils.getIcon("addFilteredToCollection", 32);
	private static final Icon editSubsetIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon deleteIcon = GuiUtils.getIcon("deleteCollection", 32);
	private static final Icon exportIcon = GuiUtils.getIcon("exportCollection", 32);
	private static final Icon linkIcon = GuiUtils.getIcon("link", 32);
	private static final Icon unlinktIcon = GuiUtils.getIcon("unlink", 32);
	private static final Icon exportFilteredLibraryIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 32);

	@SuppressWarnings("unused")
	private JButton
		newSubsetButton,
		addSelectedFeaturesToSubsetButton,
		addFilteredFeaturesToSubsetButton,
		editSubsetButton,
		exportSubserButton,
		deleteButton,
		linkButton;

	private boolean linked;

	public FeatureSubsetToolbar(ActionListener commandListener) {

		super(commandListener);

		linked = false;

		newSubsetButton = GuiUtils.addButton(this, null, newSubsetIcon, commandListener,
				MainActionCommands.NEW_FEATURE_SUBSET_COMMAND.getName(),
				MainActionCommands.NEW_FEATURE_SUBSET_COMMAND.getName(), buttonDimension);

		// addSelectedFeaturesToSubsetButton = GuiUtils.addButton(this, null,
		// addSelectedFeaturesToSubsetIcon, commandListener,
		// MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName(),
		// MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName(),
		// buttonDimension);
		//
		// addFilteredFeaturesToSubsetButton = GuiUtils.addButton(this, null,
		// addFilteredFeaturesToSubsetIcon, commandListener,
		// MainActionCommands.ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND.getName(),
		// MainActionCommands.ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND.getName(),
		// buttonDimension);

		editSubsetButton = GuiUtils.addButton(this, null, editSubsetIcon, commandListener,
				MainActionCommands.EDIT_FEATURE_SUBSET_COMMAND.getName(),
				MainActionCommands.EDIT_FEATURE_SUBSET_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		exportSubserButton = GuiUtils.addButton(this, null, exportIcon,
		commandListener,
		MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName(),
		MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		deleteButton = GuiUtils.addButton(this, null, deleteIcon, commandListener,
				MainActionCommands.DELETE_FEATURE_SUBSET_COMMAND.getName(),
				MainActionCommands.DELETE_FEATURE_SUBSET_COMMAND.getName(), buttonDimension);

		// addSeparator(buttonDimension);
		//
		// linkButton = GuiUtils.addButton(this, null, unlinktIcon,
		// commandListener,
		// MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName(),
		// MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName(),
		// buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project == null || newDataPipeline == null) {
			setEditorActive(false);
			return;
		}
		if(project.getMsFeatureSetsForDataPipeline(newDataPipeline).isEmpty()) {
			setEditorActive(false);
			return;
		}
		setEditorActive(true);
	}

	private void setEditorActive(boolean active) {

		newSubsetButton.setEnabled(active);
		editSubsetButton.setEnabled(active);
		exportSubserButton.setEnabled(active);
		deleteButton.setEnabled(active);
	}

	// public void setLinkedState(boolean linkedState){
	//
	// linked = linkedState;
	//
	// if(linked){
	//
	// linkButton.setActionCommand(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName());
	// linkButton.setToolTipText(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName());
	// linkButton.setIcon(linkIcon);
	// }
	// else{
	// linkButton.setActionCommand(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName());
	// linkButton.setToolTipText(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName());
	// linkButton.setIcon(unlinktIcon);
	// }
	// }
}
