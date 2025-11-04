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

package edu.umich.med.mrc2.datoolbox.gui.rgen.mcr;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MetabCombinerScriptDialogToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -1550999649378675670L;

	private static final Icon collectIDDataIcon = GuiUtils.getIcon("createIntegration", 32);
	private static final Icon deleteDataSetIcon = GuiUtils.getIcon("deleteIntegration", 32);
	private static final Icon acceptListIcon = GuiUtils.getIcon("acceptList", 32);

	private JButton
		collectIdentifiedCompoundDataButton,
		deleteDataSetButton,
		acceptListButton;

	private JComboBox activeSetcomboBox;

	public MetabCombinerScriptDialogToolbar(ActionListener commandListener) {

		super(commandListener);


	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		
	}
}
