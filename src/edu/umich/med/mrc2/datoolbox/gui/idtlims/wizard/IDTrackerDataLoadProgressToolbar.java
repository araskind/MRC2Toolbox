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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class IDTrackerDataLoadProgressToolbar  extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 228367951475752870L;

	private static final Icon stepCompletedIcon = GuiUtils.getIcon("level", 24);
	private static final Icon stepPendingIcon = GuiUtils.getIcon("levelInactive", 24);
	private static final Dimension largeButtonDimension = new Dimension(26, 26);
	
	@SuppressWarnings("unused")
	private JButton
		createExperimentButton,
		addSampleButton,
		addSamplePrepButton,
		addMethodsButton,
		addWorklistsButton,
		verifyDataForUploadButton,
		finalizeDataUploadButton;

	private Collection<JButton>buttons;
	
	public IDTrackerDataLoadProgressToolbar(ActionListener commandListener) {

		super(commandListener);
		buttons = new ArrayList<JButton>();

		createExperimentButton = GuiUtils.addButton(this, null, stepPendingIcon, null, null,
				IDTrackerDataUploadStage.CREATE_EXPERIMENT.getName(),
				largeButtonDimension);
		buttons.add(createExperimentButton);

		addSampleButton = GuiUtils.addButton(this, null, stepPendingIcon, null, null,
				IDTrackerDataUploadStage.ADD_SAMPLES.getName(),
				largeButtonDimension);
		buttons.add(addSampleButton);
		
		addSamplePrepButton = GuiUtils.addButton(this, null, stepPendingIcon, null, null,
				IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA.getName(),
				largeButtonDimension);
		buttons.add(addSamplePrepButton);

		addMethodsButton = GuiUtils.addButton(this, null, stepPendingIcon, null, null,
				IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS.getName(),
				largeButtonDimension);
		buttons.add(addMethodsButton);
		
		addWorklistsButton = GuiUtils.addButton(this, null, stepPendingIcon, null, null,
				IDTrackerDataUploadStage.ADD_WORKLISTS.getName(),
				largeButtonDimension);
		buttons.add(addWorklistsButton);
		
		verifyDataForUploadButton = GuiUtils.addButton(this, null, stepPendingIcon, null, null,
				IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD.getName(),
				largeButtonDimension);
		buttons.add(verifyDataForUploadButton);
		
//		finalizeDataUploadButton = GuiUtils.addButton(this, null, stepPendingIcon,null, null,
//				IDTrackerDataUploadStage.FINALIZE_DATA_UPLOAD.getName(),
//				largeButtonDimension);
//		buttons.add(finalizeDataUploadButton);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
	
	public void markStageCompletedStatus(IDTrackerDataUploadStage stage, boolean completed) {
		
		for(JButton b : buttons) {
			
			if(b.getToolTipText().equals(stage.getName())) {
				
				if(completed)
					b.setIcon(stepCompletedIcon);
				else
					b.setIcon(stepPendingIcon);
			}
		}
	}
}















