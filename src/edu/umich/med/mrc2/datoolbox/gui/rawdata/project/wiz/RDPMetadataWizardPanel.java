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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;

public abstract class RDPMetadataWizardPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6942217820551753695L;
	
	protected LIMSSamplePreparation samplePrep;
	protected LIMSExperiment experiment;
	
	protected RDPMetadataDefinitionStage stage;
	protected RDPMetadataWizard wizard;
	protected JButton completeStageButton;
	protected GridBagConstraints gbc_panel;

	public RDPMetadataWizardPanel(RDPMetadataWizard wizard) {
		
		super();
		this.wizard = wizard;
		
		GridBagLayout gbl_stagePanel = new GridBagLayout();
		gbl_stagePanel.columnWidths = new int[]{0, 0};
		gbl_stagePanel.rowHeights = new int[]{0, 0, 0};
		gbl_stagePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_stagePanel.rowWeights = new double[]{1.0, 0.1, Double.MIN_VALUE};
		setLayout(gbl_stagePanel);

		gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		
		//add(new JPanel(), gbc_panel);
		
		completeStageButton = new JButton("");
		completeStageButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		completeStageButton.addActionListener(wizard); 
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 1;
		add(completeStageButton, gbc_btnNewButton);
	}
	
	public void setStage(RDPMetadataDefinitionStage stage) {
		this.stage = stage;
	}

	public RDPMetadataDefinitionStage getStage() {
		return stage;
	}

	public LIMSSamplePreparation getSamplePrep() {
		return samplePrep;
	}

	public void setSamplePrep(LIMSSamplePreparation samplePrep) {
		this.samplePrep = samplePrep;
	}

	public LIMSExperiment getExperiment() {
		return experiment;
	}

	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}
	
	public abstract void clearPanel();
}
