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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureLookupListManagerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1003019834078700037L;
	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon deleteFeatureCollectionIcon = GuiUtils.getIcon("deleteCollection", 32);
	
	@SuppressWarnings("unused")
	private JButton
		addFeatureLookupListButton,
		editFeatureLookupListButton,
		deleteFeatureLookupListButton;

	public FeatureLookupListManagerToolbar(ActionListener commandListener) {
		super(commandListener);

		addFeatureLookupListButton = GuiUtils.addButton(this, null, addFeatureCollectionIcon, commandListener,
				MainActionCommands.ADD_FEATURE_LOOKUP_LIST_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_FEATURE_LOOKUP_LIST_DIALOG_COMMAND.getName(),
				buttonDimension);

		editFeatureLookupListButton = GuiUtils.addButton(this, null, editFeatureCollectionIcon, commandListener,
				MainActionCommands.EDIT_FEATURE_LOOKUP_LIST_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_FEATURE_LOOKUP_LIST_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteFeatureLookupListButton = GuiUtils.addButton(this, null, deleteFeatureCollectionIcon, commandListener,
				MainActionCommands.DELETE_FEATURE_LOOKUP_LIST_COMMAND.getName(),
				MainActionCommands.DELETE_FEATURE_LOOKUP_LIST_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}	
}
