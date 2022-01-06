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

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.experiment.IDTExperimentDefinitionPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class WizardExperimentDefinitionPanel extends IDTrackerDataLoadWizardPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;
	private IDTExperimentDefinitionPanel experimentDefinitionPanel;

	public WizardExperimentDefinitionPanel(IDTrackerDataLoadWizard wizard) {
		
		super(wizard);
		
		experimentDefinitionPanel = new IDTExperimentDefinitionPanel(null);
		add(experimentDefinitionPanel, gbc_panel);		
		completeStageButton.setText(
				MainActionCommands.COMPLETE_EXPERIMENT_DEFINITION_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.COMPLETE_EXPERIMENT_DEFINITION_COMMAND.getName());
	}

	public LIMSExperiment getExperiment() {
		return experimentDefinitionPanel.getExperiment();
	}

	public LIMSProject getExperimentProject() {
		return experimentDefinitionPanel.getExperimentProject();
	}

	public String getExperimentName() {
		return experimentDefinitionPanel.getExperimentName();
	}

	public String getExperimentDescription() {
		return experimentDefinitionPanel.getExperimentDescription();
	}

	public String getExperimentNotes() {
		return experimentDefinitionPanel.getExperimentNotes();
	}
	
	public void loadExperiment(LIMSExperiment newExperiment) {
		experimentDefinitionPanel.loadExperiment(newExperiment);
	}
	
	public Collection<String>validateExperimentDefinition(){
		return experimentDefinitionPanel.validateExperimentDefinition();
	}
	
	public LIMSInstrument getInstrument() {
		return experimentDefinitionPanel.getInstrument();
	}
}
