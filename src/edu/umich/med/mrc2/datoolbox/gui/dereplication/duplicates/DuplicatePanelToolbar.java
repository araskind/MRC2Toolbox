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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayToolbar;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DuplicatePanelToolbar extends ClusterDisplayToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -8718240170268519513L;

	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 32);
	private static final Icon exportDuplicatesIcon = GuiUtils.getIcon("saveDuplicates", 32);
	private static final Icon showDuplicatesIcon = GuiUtils.getIcon("findDuplicates", 32);

	@SuppressWarnings("unused")
	private JButton
		showDuplicatesButton,
		clearDuplicatesButton,
		exportDuplicatesButton;

	public DuplicatePanelToolbar(ActionListener commandListener) {

		super(commandListener);

		showDuplicatesButton = GuiUtils.addButton(this, null, showDuplicatesIcon, commandListener,
				MainActionCommands.SHOW_FIND_DUPLICATES_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_FIND_DUPLICATES_DIALOG_COMMAND.getName(), buttonDimension);

		clearDuplicatesButton = GuiUtils.addButton(this, null, clearDuplicatesIcon, commandListener,
				MainActionCommands.SHOW_DUPLICATES_MERGE_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_DUPLICATES_MERGE_DIALOG_COMMAND.getName(), buttonDimension);

		exportDuplicatesButton = GuiUtils.addButton(this, null, exportDuplicatesIcon, commandListener,
				MainActionCommands.EXPORT_DUPLICATES_COMMAND.getName(),
				MainActionCommands.EXPORT_DUPLICATES_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateGuiFromActiveSet(MsFeatureClusterSet integratedSet) {
		// TODO Auto-generated method stub

	}
}
