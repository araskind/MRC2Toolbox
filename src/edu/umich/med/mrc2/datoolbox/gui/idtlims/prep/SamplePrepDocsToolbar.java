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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class SamplePrepDocsToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -5449850073679206046L;

	private static final Icon addDocumentIcon = GuiUtils.getIcon("addCollection", 32);
	private static final Icon deleteDocumentIcon = GuiUtils.getIcon("deleteCollection", 32);

	private JButton
		addDocumentButton,
		deleteDocumentButton;

	public SamplePrepDocsToolbar(ActionListener commandListener) {

		super(commandListener);

		addDocumentButton = GuiUtils.addButton(this, null, addDocumentIcon, commandListener,
				MainActionCommands.ADD_DOCUMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_DOCUMENT_DIALOG_COMMAND.getName(),
				buttonDimension);

		deleteDocumentButton = GuiUtils.addButton(this, null, deleteDocumentIcon, commandListener,
				MainActionCommands.DELETE_DOCUMENT_COMMAND.getName(),
				MainActionCommands.DELETE_DOCUMENT_COMMAND.getName(),
				buttonDimension);
	}
	
	public void setEditable(boolean b) {

		addDocumentButton.setEnabled(b);
		deleteDocumentButton.setEnabled(b);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}

