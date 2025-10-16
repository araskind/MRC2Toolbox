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

package edu.umich.med.mrc2.datoolbox.gui.lims.experiment.design;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class LIMSDesignEditorToolbar  extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 228367951475752870L;

	private static final Icon exportDesignIcon = GuiUtils.getIcon("exportDesign", 32);
	private static final Icon addFactorIcon = GuiUtils.getIcon("addFactor", 32);
	private static final Icon editFactorIcon = GuiUtils.getIcon("editFactor", 32);
	private static final Icon deleteFactorIcon = GuiUtils.getIcon("deleteFactor", 32);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 32);
	private static final Icon saveDesignToLimsIcon = GuiUtils.getIcon("importLibraryToDb", 32);

	@SuppressWarnings("unused")
	private JButton
		exportDesignButton,
		addFactorButton,
		editFactorButton,
		deleteFactorButton,
		addSampleButton,
		deleteSampleButton,
		saveDesignToLimsButton;

	public LIMSDesignEditorToolbar(ActionListener commandListener) {

		super(commandListener);

		addFactorButton = GuiUtils.addButton(this, null, addFactorIcon, commandListener,
				MainActionCommands.ADD_FACTOR_COMMAND.getName(), MainActionCommands.ADD_FACTOR_COMMAND.getName(),
				buttonDimension);

		editFactorButton = GuiUtils.addButton(this, null, editFactorIcon, commandListener,
				MainActionCommands.EDIT_FACTOR_COMMAND.getName(), MainActionCommands.EDIT_FACTOR_COMMAND.getName(),
				buttonDimension);

		deleteFactorButton = GuiUtils.addButton(this, null, deleteFactorIcon, commandListener,
				MainActionCommands.DELETE_FACTOR_COMMAND.getName(), MainActionCommands.DELETE_FACTOR_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		addSampleButton = GuiUtils.addButton(this, null, addSampleIcon, commandListener,
				MainActionCommands.ADD_SAMPLE_COMMAND.getName(), MainActionCommands.ADD_SAMPLE_COMMAND.getName(),
				buttonDimension);

		deleteSampleButton = GuiUtils.addButton(this, null, deleteSampleIcon, commandListener,
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(), MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		exportDesignButton = GuiUtils.addButton(this, null, exportDesignIcon, commandListener,
				MainActionCommands.EXPORT_DESIGN_COMMAND.getName(), MainActionCommands.EXPORT_DESIGN_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		saveDesignToLimsButton = GuiUtils.addButton(this, null, saveDesignToLimsIcon, commandListener,
				MainActionCommands.SAVE_DESIGN_TO_LIMS_COMMAND.getName(), MainActionCommands.SAVE_DESIGN_TO_LIMS_COMMAND.getName(),
				buttonDimension);

		//	Temp until implemented
		addFactorButton.setEnabled(false);
		editFactorButton.setEnabled(false);
		deleteFactorButton.setEnabled(false);
		addSampleButton.setEnabled(false);
		deleteSampleButton.setEnabled(false);
		saveDesignToLimsButton.setEnabled(false);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
