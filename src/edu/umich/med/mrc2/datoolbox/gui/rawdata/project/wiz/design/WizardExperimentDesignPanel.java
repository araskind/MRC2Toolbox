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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.design;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RawDataProjectMetadataWizard;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RawDataProjectMetadataWizardPanel;

public class WizardExperimentDesignPanel extends RawDataProjectMetadataWizardPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;
	private WizardExperimentDesignEditorPanel experimentDesignEditorPanel;

	public WizardExperimentDesignPanel(RawDataProjectMetadataWizard wizard) {
		
		super(wizard);
		
		experimentDesignEditorPanel = new WizardExperimentDesignEditorPanel();		
		add(experimentDesignEditorPanel, gbc_panel);		
		
		completeStageButton.setText(
				MainActionCommands.COMPLETE_SAMPLE_LIST_DEFINITION_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.COMPLETE_SAMPLE_LIST_DEFINITION_COMMAND.getName());
	}
	
	public void showExperimentDesign(ExperimentDesign newDesign) {
		experimentDesignEditorPanel.showExperimentDesign(newDesign);
	}

	public void reloadDesign() {
		experimentDesignEditorPanel.reloadDesign();
	}

	public synchronized void clearPanel() {
		experimentDesignEditorPanel.clearPanel();
	}

	public ExperimentDesign getExperimentDesign() {
		return experimentDesignEditorPanel.getExperimentDesign();
	}
	
	public void loadExperiment(LIMSExperiment experiment) {
		experimentDesignEditorPanel.loadExperiment(experiment);
	}

	public LIMSExperiment getExperiment() {
		return experimentDesignEditorPanel.getExperiment();
	}

	public Collection<String> validateExperimentDesign() {
		
		Collection<String>errors = new ArrayList<String>();
		if(experimentDesignEditorPanel.getExperiment().getExperimentDesign().getSamples().isEmpty())
			errors.add("No samples added to the experiment");
		
		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
