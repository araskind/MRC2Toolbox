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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DesignEditorToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 228367951475752870L;

	private static final Icon loadDesignIcon = GuiUtils.getIcon("loadDesign", 32);
	private static final Icon appendDesignIcon = GuiUtils.getIcon("appendDesign", 32);
	private static final Icon clearDesignIcon = GuiUtils.getIcon("clearDesign", 32);
	private static final Icon exportDesignIcon = GuiUtils.getIcon("exportDesign", 32);
	private static final Icon addFactorIcon = GuiUtils.getIcon("addFactor", 32);
	private static final Icon editFactorIcon = GuiUtils.getIcon("editFactor", 32);
	private static final Icon deleteFactorIcon = GuiUtils.getIcon("deleteFactor", 32);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 32);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 32);
	private static final Icon editReferenceSamplesIcon = GuiUtils.getIcon("standardSample", 32);

	@SuppressWarnings("unused")
	private JButton
		loadDesignButton,
		appendDesignButton,
		clearDesignButton,
		exportDesignButton,
		addFactorButton,
		editFactorButton,
		deleteFactorButton,
		addSampleButton,
		editSampleButton,
		deleteSampleButton,
		editReferenceSamplesButton;

	public DesignEditorToolbar(ActionListener commandListener) {

		super(commandListener);

		loadDesignButton = GuiUtils.addButton(this, null, loadDesignIcon, commandListener,
				MainActionCommands.LOAD_DESIGN_COMMAND.getName(),
				MainActionCommands.LOAD_DESIGN_COMMAND.getName(),
				buttonDimension);

		appendDesignButton = GuiUtils.addButton(this, null, appendDesignIcon, commandListener,
				MainActionCommands.APPEND_DESIGN_COMMAND.getName(),
				MainActionCommands.APPEND_DESIGN_COMMAND.getName(),
				buttonDimension);

		clearDesignButton = GuiUtils.addButton(this, null, clearDesignIcon, commandListener,
				MainActionCommands.CLEAR_DESIGN_COMMAND.getName(),
				MainActionCommands.CLEAR_DESIGN_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		addFactorButton = GuiUtils.addButton(this, null, addFactorIcon, commandListener,
				MainActionCommands.ADD_FACTOR_COMMAND.getName(),
				MainActionCommands.ADD_FACTOR_COMMAND.getName(),
				buttonDimension);

		editFactorButton = GuiUtils.addButton(this, null, editFactorIcon, commandListener,
				MainActionCommands.EDIT_FACTOR_COMMAND.getName(),
				MainActionCommands.EDIT_FACTOR_COMMAND.getName(),
				buttonDimension);

		deleteFactorButton = GuiUtils.addButton(this, null, deleteFactorIcon, commandListener,
				MainActionCommands.DELETE_FACTOR_COMMAND.getName(),
				MainActionCommands.DELETE_FACTOR_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		editReferenceSamplesButton = GuiUtils.addButton(this, null, editReferenceSamplesIcon, commandListener,
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		addSampleButton = GuiUtils.addButton(this, null, addSampleIcon, commandListener,
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteSampleButton = GuiUtils.addButton(this, null, deleteSampleIcon, commandListener,
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		exportDesignButton = GuiUtils.addButton(this, null, exportDesignIcon, commandListener,
				MainActionCommands.EXPORT_DESIGN_COMMAND.getName(),
				MainActionCommands.EXPORT_DESIGN_COMMAND.getName(),
				buttonDimension);
	}

	public void setAcceptDesignStatus(boolean enabled) {
		exportDesignButton.setEnabled(enabled);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		if(project == null) {

			appendDesignButton.setEnabled(false);
			clearDesignButton.setEnabled(false);
			exportDesignButton.setEnabled(false);
			addFactorButton.setEnabled(false);
			editFactorButton.setEnabled(false);
			deleteFactorButton.setEnabled(false);
			addSampleButton.setEnabled(false);
			editReferenceSamplesButton.setEnabled(false);
			deleteSampleButton.setEnabled(false);
		}
		else {
			ExperimentDesign design = project.getExperimentDesign();
			addSampleButton.setEnabled(true);
			appendDesignButton.setEnabled(true);
			clearDesignButton.setEnabled(true);
			exportDesignButton.setEnabled(design.getSamples().size() > 0);
			addFactorButton.setEnabled(true);
			editFactorButton.setEnabled(design.getFactors().size() > 1);
			deleteFactorButton.setEnabled(design.getFactors().size() > 1);
			editReferenceSamplesButton.setEnabled(true);
			deleteSampleButton.setEnabled(design.getSamples().size() > 0);
		}
	}
}
