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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExcelImportToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3073109513983956403L;
	private static final Icon openExcelIcon = GuiUtils.getIcon("excelOpen", 32);
	private static final Icon matchIcon = GuiUtils.getIcon("match", 32);
	private static final Icon clearIcon = GuiUtils.getIcon("clear", 32);

	@SuppressWarnings("unused")
	private JButton
		matchButton,
		loadExcelButton,
		clearButton;

	public ExcelImportToolbar(ActionListener commandListener) {

		super(commandListener);

		loadExcelButton = GuiUtils.addButton(this, null, openExcelIcon, commandListener,
				MainActionCommands.LOAD_EXCEL_DATA_FOR_PREVIEW_COMMAND.getName(),
				MainActionCommands.LOAD_EXCEL_DATA_FOR_PREVIEW_COMMAND.getName(), buttonDimension);

		matchButton = GuiUtils.addButton(this, null, matchIcon, commandListener,
				MainActionCommands.MATCH_IMPORTED_TO_DESIGN_COMMAND.getName(),
				MainActionCommands.MATCH_IMPORTED_TO_DESIGN_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		clearButton = GuiUtils.addButton(this, null, clearIcon, commandListener,
				MainActionCommands.CLEAR_EXCEL_IMPORT_WIZARD_COMMAND.getName(),
				MainActionCommands.CLEAR_EXCEL_IMPORT_WIZARD_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}













