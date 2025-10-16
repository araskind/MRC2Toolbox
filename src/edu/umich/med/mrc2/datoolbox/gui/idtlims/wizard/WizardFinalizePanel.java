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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class WizardFinalizePanel extends IDTrackerDataLoadWizardPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;

	public WizardFinalizePanel(IDTrackerDataLoadWizard wizard) {
		
		super(wizard);
		
		JPanel placeholder = new JPanel();
		add(placeholder, gbc_panel);
		
		completeStageButton.setText(
				MainActionCommands.FINALIZE_DATA_UPLOAD_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.FINALIZE_DATA_UPLOAD_COMMAND.getName());
	}

	public Collection<String> validateDataSeyForUpload() {
		
		Collection<String>errors = new ArrayList<String>();
		
		return errors;
	}

}
