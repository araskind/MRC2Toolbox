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

package edu.umich.med.mrc2.datoolbox.gui.lims;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class METLIMSToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2016997747952680668L;

	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 32);
	private static final Icon resyncExperimentToLimsIcon = GuiUtils.getIcon("resyncExperimentToLims", 32);
	private static final Icon sendDesignToProjectIcon = GuiUtils.getIcon("sendDesignToProject", 32);
	private static final Icon syncDbIcon = GuiUtils.getIcon("synchronizeDb", 32);
	private static final Icon createExperimentDirIcon = GuiUtils.getIcon("newProject", 32);
	private static final Icon deleteExperimentIcon = GuiUtils.getIcon("deleteCollection", 32);
	
	@SuppressWarnings("unused")
	private JButton
		refreshDataButton,
		syncLimsButton,
		reloadExperimentFromLimsButton,
		deleteExperimentButton,
		sendDesignToExperimentButton,
		createExperimentDirButton;

	public METLIMSToolbar(ActionListener commandListener) {

		super(commandListener);

		refreshDataButton = GuiUtils.addButton(this, null, refreshDataIcon, commandListener,
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				buttonDimension);

		syncLimsButton = GuiUtils.addButton(this, null, syncDbIcon, commandListener,
				MainActionCommands.SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND.getName(),
				MainActionCommands.SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		deleteExperimentButton = GuiUtils.addButton(this, null, deleteExperimentIcon, commandListener,
				MainActionCommands.DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND.getName(),
				MainActionCommands.DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		sendDesignToExperimentButton = GuiUtils.addButton(this, null, sendDesignToProjectIcon, commandListener,
				MainActionCommands.SEND_DESIGN_TO_EXPERIMENT_COMMAND.getName(),
				MainActionCommands.SEND_DESIGN_TO_EXPERIMENT_COMMAND.getName(),
				buttonDimension);

		createExperimentDirButton = GuiUtils.addButton(this, null, createExperimentDirIcon, commandListener,
				MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND.getName(),
				MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
