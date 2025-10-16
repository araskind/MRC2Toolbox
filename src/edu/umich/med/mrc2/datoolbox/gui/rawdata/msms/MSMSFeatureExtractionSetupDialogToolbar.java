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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msms;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MSMSFeatureExtractionSetupDialogToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1934511837376564647L;
	
	private static final Icon saveSearchQueryIcon = GuiUtils.getIcon("saveWorklist", 32);
	private static final Icon loadSearchQueryIcon = GuiUtils.getIcon("loadList", 32);
	
	@SuppressWarnings("unused")
	private JButton
		loadSearchQueryButton,
		saveSearchQueryButton;

	public MSMSFeatureExtractionSetupDialogToolbar(ActionListener commandListener) {

		super(commandListener);

		loadSearchQueryButton = GuiUtils.addButton(this, null, loadSearchQueryIcon, commandListener,
				MainActionCommands.SHOW_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_LIST_COMMAND.getName(),
				MainActionCommands.SHOW_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_LIST_COMMAND.getName(),
				buttonDimension);

//		saveSearchQueryButton = GuiUtils.addButton(this, null, saveSearchQueryIcon, commandListener,
//				MainActionCommands.SHOW_SAVE_MSMS_FEATURE_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
//				MainActionCommands.SHOW_SAVE_MSMS_FEATURE_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
//				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
