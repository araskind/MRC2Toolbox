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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class LibraryManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -8383291255490141151L;

	private static final Icon newLibraryIcon = GuiUtils.getIcon("newLibrary", 32);
	private static final Icon editLibInfoIcon = GuiUtils.getIcon("editLibrary", 32);
	private static final Icon openLibraryIcon = GuiUtils.getIcon("open", 32);
	private static final Icon deleteLibraryIcon = GuiUtils.getIcon("deleteLibrary", 32);
	private static final Icon duplicateLibraryIcon = GuiUtils.getIcon("duplicateLibrary", 32);

	@SuppressWarnings("unused")
	private JButton
		newLibraryButton,
		editLibraryInfoButton,
		openLibraryButton,
		duplicateLibraryButton,
		deleteLibraryButton;

	public LibraryManagerToolbar(ActionListener commandListener, ActionListener libInfoDialogListener) {

		super(commandListener);

		openLibraryButton = GuiUtils.addButton(this, null, openLibraryIcon, commandListener,
				MainActionCommands.OPEN_LIBRARY_COMMAND.getName(),
				MainActionCommands.OPEN_LIBRARY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		newLibraryButton = GuiUtils.addButton(this, null, newLibraryIcon, libInfoDialogListener,
				MainActionCommands.NEW_LIBRARY_DIALOG_COMMAND.getName(),
				MainActionCommands.NEW_LIBRARY_DIALOG_COMMAND.getName(), buttonDimension);

		editLibraryInfoButton = GuiUtils.addButton(this, null, editLibInfoIcon, libInfoDialogListener,
				MainActionCommands.EDIT_LIBRARY_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_LIBRARY_DIALOG_COMMAND.getName(), buttonDimension);

		duplicateLibraryButton = GuiUtils.addButton(this, null, duplicateLibraryIcon, libInfoDialogListener,
				MainActionCommands.DUPLICATE_LIBRARY_DIALOG_COMMAND.getName(),
				MainActionCommands.DUPLICATE_LIBRARY_DIALOG_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		deleteLibraryButton = GuiUtils.addButton(this, null, deleteLibraryIcon, commandListener,
				MainActionCommands.DELETE_LIBRARY_COMMAND.getName(),
				MainActionCommands.DELETE_LIBRARY_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
