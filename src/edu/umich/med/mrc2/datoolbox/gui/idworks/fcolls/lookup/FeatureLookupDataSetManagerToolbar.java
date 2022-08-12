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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureLookupDataSetManagerToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1003019834078700037L;
	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon deleteFeatureCollectionIcon = GuiUtils.getIcon("deleteCollection", 32);
	private static final Icon loadFeatureCollectionIcon = GuiUtils.getIcon("openCollection", 32);
	
	@SuppressWarnings("unused")
	private JButton
		addFeatureCollectionButton,
		editFeatureCollectionButton,
		deleteFeatureCollectionButton,
		loadFeatureCollectionButton;

	public FeatureLookupDataSetManagerToolbar(ActionListener commandListener) {
		super(commandListener);

		addFeatureCollectionButton = GuiUtils.addButton(this, null, addFeatureCollectionIcon, commandListener,
				MainActionCommands.ADD_FEATURE_COLLECTION_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_FEATURE_COLLECTION_DIALOG_COMMAND.getName(),
				buttonDimension);

		editFeatureCollectionButton = GuiUtils.addButton(this, null, editFeatureCollectionIcon, commandListener,
				MainActionCommands.EDIT_FEATURE_COLLECTION_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_FEATURE_COLLECTION_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteFeatureCollectionButton = GuiUtils.addButton(this, null, deleteFeatureCollectionIcon, commandListener,
				MainActionCommands.DELETE_FEATURE_COLLECTION_COMMAND.getName(),
				MainActionCommands.DELETE_FEATURE_COLLECTION_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		loadFeatureCollectionButton = GuiUtils.addButton(this, null, loadFeatureCollectionIcon, commandListener,
				MainActionCommands.LOAD_FEATURE_COLLECTION_COMMAND.getName(),
				MainActionCommands.LOAD_FEATURE_COLLECTION_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}	
}
