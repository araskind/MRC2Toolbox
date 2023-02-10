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

package edu.umich.med.mrc2.datoolbox.gui.adducts.exchange;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AdductExchangeEditorToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 9157313728143958113L;

	private static final Icon newExchangeIcon = GuiUtils.getIcon("addExchange", 32);
	private static final Icon editExchangeIcon = GuiUtils.getIcon("editExchange", 32);
	private static final Icon deleteExchangeIcon = GuiUtils.getIcon("deleteExchange", 32);
	private static final Icon importExchangeIcon = GuiUtils.getIcon("loadList", 32);
	private static final Icon exportExchangeIcon = GuiUtils.getIcon("saveDuplicates", 32);

	@SuppressWarnings("unused")
	private JButton
		newExchangeButton,
		editExchangeButton,
		deleteExchangeButton,
		importExchangesButton,
		exportExchangesButton;

	public AdductExchangeEditorToolbar(ActionListener listener) {

		super(listener);

		newExchangeButton = GuiUtils.addButton(this, null, newExchangeIcon, listener,
				MainActionCommands.NEW_EXCHANGE_COMMAND.getName(),
				MainActionCommands.NEW_EXCHANGE_COMMAND.getName(),
				buttonDimension);

		 editExchangeButton = GuiUtils.addButton(this, null, editExchangeIcon, listener,
				 MainActionCommands.EDIT_EXCHANGE_COMMAND.getName(),
				 MainActionCommands.EDIT_EXCHANGE_COMMAND.getName(),
				 buttonDimension);

		deleteExchangeButton = GuiUtils.addButton(this, null, deleteExchangeIcon, listener,
				MainActionCommands.DELETE_EXCHANGE_COMMAND.getName(),
				MainActionCommands.DELETE_EXCHANGE_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		importExchangesButton = GuiUtils.addButton(this, null, importExchangeIcon, listener,
				MainActionCommands.IMPORT_EXCHANGE_LIST_COMMAND.getName(),
				MainActionCommands.IMPORT_EXCHANGE_LIST_COMMAND.getName(),
				buttonDimension);
		importExchangesButton.setEnabled(false);

		exportExchangesButton = GuiUtils.addButton(this, null, exportExchangeIcon, listener,
				MainActionCommands.EXPORT_EXCHANGE_LIST_COMMAND.getName(),
				MainActionCommands.EXPORT_EXCHANGE_LIST_COMMAND.getName(),
				buttonDimension);
		exportExchangesButton.setEnabled(false);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
