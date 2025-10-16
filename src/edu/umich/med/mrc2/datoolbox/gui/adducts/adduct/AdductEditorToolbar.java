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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AdductEditorToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -7270184336598621941L;

	private static final Icon newModificationIcon = GuiUtils.getIcon("newModification", 32);
	private static final Icon newCompositeModificationIcon = GuiUtils.getIcon("newCompositeModification", 32);
	private static final Icon editModificationIcon = GuiUtils.getIcon("editModification", 32);
	private static final Icon deleteModificationIcon = GuiUtils.getIcon("deleteModification", 32);
	private static final Icon importModificationIcon = GuiUtils.getIcon("loadList", 32);
	private static final Icon exportModificationIcon = GuiUtils.getIcon("saveDuplicates", 32);


	@SuppressWarnings("unused")
	private JButton
		newModificationButton,
		newCompositeModificationButton,
		editModificationButton,
		deleteModificationButton,
		importModificationsButton,
		exportModificationsButton;

	public AdductEditorToolbar(ActionListener listener) {

		super(listener);

		newModificationButton = GuiUtils.addButton(this, null, newModificationIcon, listener,
				MainActionCommands.NEW_MODIFICATION_COMMAND.getName(),
				MainActionCommands.NEW_MODIFICATION_COMMAND.getName(),
				buttonDimension);
		
		newCompositeModificationButton = GuiUtils.addButton(this, null, newCompositeModificationIcon, listener,
				MainActionCommands.NEW_COMPOSITE_MODIFICATION_COMMAND.getName(),
				MainActionCommands.NEW_COMPOSITE_MODIFICATION_COMMAND.getName(),
				buttonDimension);

		editModificationButton = GuiUtils.addButton(this, null, editModificationIcon, listener,
				MainActionCommands.EDIT_MODIFICATION_COMMAND.getName(),
				MainActionCommands.EDIT_MODIFICATION_COMMAND.getName(),
				buttonDimension);

		deleteModificationButton = GuiUtils.addButton(this, null, deleteModificationIcon, listener,
				MainActionCommands.DELETE_MODIFICATION_COMMAND.getName(),
				MainActionCommands.DELETE_MODIFICATION_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		importModificationsButton = GuiUtils.addButton(this, null, importModificationIcon, listener,
				MainActionCommands.IMPORT_MODIFICATIONS_COMMAND.getName(),
				MainActionCommands.IMPORT_MODIFICATIONS_COMMAND.getName(),
				buttonDimension);

		exportModificationsButton = GuiUtils.addButton(this, null, exportModificationIcon, listener,
				MainActionCommands.EXPORT_MODIFICATIONS_COMMAND.getName(),
				MainActionCommands.EXPORT_MODIFICATIONS_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
