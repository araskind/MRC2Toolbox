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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.column;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ChromatographicColumnManagerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6098358120818500637L;

	private static final Icon editColumnIcon = GuiUtils.getIcon("editColumn", 32);
	private static final Icon addColumnIcon = GuiUtils.getIcon("addColumn", 32);
	private static final Icon deleteColumnIcon = GuiUtils.getIcon("deleteColumn", 32);

	@SuppressWarnings("unused")
	private JButton
		addColumnButton,
		editColumnButton,
		deleteColumnButton;

	public ChromatographicColumnManagerToolbar(ActionListener commandListener) {

		super(commandListener);

		addColumnButton = GuiUtils.addButton(this, null, addColumnIcon, commandListener,
				MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(),
				buttonDimension);

		editColumnButton = GuiUtils.addButton(this, null, editColumnIcon, commandListener,
				MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteColumnButton = GuiUtils.addButton(this, null, deleteColumnIcon, commandListener,
				MainActionCommands.DELETE_CHROMATOGRAPHIC_COLUMN_COMMAND.getName(),
				MainActionCommands.DELETE_CHROMATOGRAPHIC_COLUMN_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
