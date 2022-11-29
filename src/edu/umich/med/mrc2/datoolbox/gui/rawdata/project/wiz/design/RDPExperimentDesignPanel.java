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
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RDPMetadataWizard;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RDPMetadataWizardPanel;

public class RDPExperimentDesignPanel extends RDPMetadataWizardPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;
	private RDPExperimentDesignEditorPanel experimentDesignEditorPanel;

	public RDPExperimentDesignPanel(RDPMetadataWizard wizard) {
		
		super(wizard);
		
		experimentDesignEditorPanel = new RDPExperimentDesignEditorPanel();		
		experimentDesignEditorPanel.setWizardPanel(true);
		add(experimentDesignEditorPanel, gbc_panel);		
		
		completeStageButton.setText(
				MainActionCommands.COMPLETE_SAMPLE_LIST_DEFINITION_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.COMPLETE_SAMPLE_LIST_DEFINITION_COMMAND.getName());
	}

	public synchronized void clearPanel() {
		experimentDesignEditorPanel.clearPanel();
	}

	public ExperimentDesign getExperimentDesign() {
		return experimentDesignEditorPanel.getExperimentDesign();
	}
	
	public void setExperiment(LIMSExperiment experiment2) {
		experiment = experiment2;
		experimentDesignEditorPanel.loadExperiment(experiment);
	}

	public Collection<String> validateExperimentDesign() {
		
		Collection<String>errors = new ArrayList<String>();
		if(experimentDesignEditorPanel.getExperiment().getExperimentDesign().getSamples().isEmpty())
			errors.add("No samples added to the experiment");
		
		return errors;
	}
	
	public void setDesignEditable(boolean editable) {
		experimentDesignEditorPanel.setDesignEditable(editable);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<String> validateInputData() {
		return validateExperimentDesign();
	}
}
