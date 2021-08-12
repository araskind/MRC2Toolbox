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

import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.design.WizardExperimentDesignPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.methods.WizardMethodsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.ms.WizardMsDataVerifierPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.worklist.WizardWorklistPanel;

public enum IDTrackerDataUploadStage {

	CREATE_EXPERIMENT("Create experiment", WizardExperimentDefinitionPanel.class),
	ADD_SAMPLES("Add samples", WizardExperimentDesignPanel.class),
	ADD_SAMPLE_PREPARATION_DATA("Add sample preparation data", WizardSamplePrepPanel.class),
	ADD_ACQ_DA_METHODS("Add acquisition and data analysis methods", WizardMethodsPanel.class),
	ADD_WORKLISTS("Add instrument worklists", WizardWorklistPanel.class),
	VERIFY_DATA_FOR_UPLOAD("Verify MS/MSMS data for upload", WizardMsDataVerifierPanel.class),
	//	FINALIZE_DATA_UPLOAD("Finalize data upload", WizardFinalizePanel.class),
	;

	private final String name;
	private final Class<?> panelClass;

	IDTrackerDataUploadStage(String name, Class<?> pClass) {

		this.name = name;
		this.panelClass = pClass;		
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public Class<?> getPanelClass() {
		return panelClass;
	}
}
