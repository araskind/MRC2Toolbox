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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DesignSubsetToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -5191384121985987367L;

	private static final Icon newSubsetIcon = GuiUtils.getIcon("newDesignSubset", 32);
	private static final Icon editSubsetIcon = GuiUtils.getIcon("rename", 32);
	private static final Icon deleteIcon = GuiUtils.getIcon("deleteDesignSubset", 32);
	private static final Icon linkIcon = GuiUtils.getIcon("link", 32);
	private static final Icon unlinktIcon = GuiUtils.getIcon("unlink", 32);
	private static final Icon lockedIcon = GuiUtils.getIcon("locked", 32);
	private static final Icon unlockedIcon = GuiUtils.getIcon("unlocked", 32);
	private static final Icon copySubsetIcon = GuiUtils.getIcon("copy", 32);
	private static final Icon viewSubsetIcon = GuiUtils.getIcon("viewDesign", 32);

	private JButton
		newSubsetButton,
		viewSubsetButton,
		copySubsetButton,
		editSubsetButton,
		deleteButton,
		linkButton,
		lockButton;

	private boolean linked;

	public DesignSubsetToolbar(ActionListener commandListener) {

		super(commandListener);

		newSubsetButton = GuiUtils.addButton(this, null, newSubsetIcon, commandListener,
				MainActionCommands.SHOW_DESIGN_SUBSET_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_DESIGN_SUBSET_DIALOG_COMMAND.getName(), buttonDimension);

		viewSubsetButton = GuiUtils.addButton(this, null, viewSubsetIcon, commandListener,
				MainActionCommands.VIEW_DESIGN_SUBSET_COMMAND.getName(),
				MainActionCommands.VIEW_DESIGN_SUBSET_COMMAND.getName(), buttonDimension);

		copySubsetButton = GuiUtils.addButton(this, null, copySubsetIcon, commandListener,
				MainActionCommands.COPY_DESIGN_SUBSET_COMMAND.getName(),
				MainActionCommands.COPY_DESIGN_SUBSET_COMMAND.getName(), buttonDimension);

		editSubsetButton = GuiUtils.addButton(this, null, editSubsetIcon, commandListener,
				MainActionCommands.RENAME_DESIGN_SUBSET_COMMAND.getName(),
				MainActionCommands.RENAME_DESIGN_SUBSET_COMMAND.getName(), buttonDimension);

		lockButton = GuiUtils.addButton(this, null, unlockedIcon, commandListener,
				MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName(),
				MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		deleteButton = GuiUtils.addButton(this, null, deleteIcon, commandListener,
				MainActionCommands.DELETE_DESIGN_SUBSET_COMMAND.getName(),
				MainActionCommands.DELETE_DESIGN_SUBSET_COMMAND.getName(), buttonDimension);

//		addSeparator(buttonDimension);
//
//		linkButton = GuiUtils.addButton(this, null, unlinktIcon, commandListener,
//				MainActionCommands.LINK_DESIGN_SUBSET_COMMAND.getName(),
//				MainActionCommands.LINK_DESIGN_SUBSET_COMMAND.getName(), buttonDimension);

	}

	public void setToolbarState(ExperimentDesignSubset subset) {

		if(subset.getName().equals(GlobalDefaults.ALL_SAMPLES.getName())) {

			lockButton.setIcon(lockedIcon);
			lockButton.setEnabled(false);
			editSubsetButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
		else {
			lockButton.setEnabled(true);

			if(subset.isLocked()) {

				editSubsetButton.setEnabled(false);
				deleteButton.setEnabled(false);
				lockButton.setActionCommand(MainActionCommands.UNLOCK_DESIGN_SUBSET_COMMAND.getName());
				lockButton.setToolTipText(MainActionCommands.UNLOCK_DESIGN_SUBSET_COMMAND.getName());
				lockButton.setIcon(lockedIcon);
			}
			else {
				editSubsetButton.setEnabled(true);
				deleteButton.setEnabled(true);
				lockButton.setActionCommand(MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName());
				lockButton.setToolTipText(MainActionCommands.LOCK_DESIGN_SUBSET_COMMAND.getName());
				lockButton.setIcon(unlockedIcon);
			}
		}
	}

	public void setLinkedState(boolean linkedState) {

		linked = linkedState;

		if (linked) {

			linkButton.setActionCommand(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName());
			linkButton.setToolTipText(MainActionCommands.UNLINK_FEATURE_SUBSET_COMMAND.getName());
			linkButton.setIcon(linkIcon);
		} else {
			linkButton.setActionCommand(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName());
			linkButton.setToolTipText(MainActionCommands.LINK_FEATURE_SUBSET_COMMAND.getName());
			linkButton.setIcon(unlinktIcon);
		}
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project == null) {

			newSubsetButton.setEnabled(false);
			lockButton.setEnabled(false);
			viewSubsetButton.setEnabled(false);
			copySubsetButton.setEnabled(false);
			editSubsetButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
		else {
			if(project.getExperimentDesign().getActiveDesignSubset() != null)
				setToolbarState(project.getExperimentDesign().getActiveDesignSubset());
			else {
				newSubsetButton.setEnabled(true);
				viewSubsetButton.setEnabled(true);
				copySubsetButton.setEnabled(true);
			}
		}
	}
}






















