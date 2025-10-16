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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz;

import java.util.Collection;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.prep.SamplePrepEditorPanel;

public class RDPSamplePrepPanel extends RDPMetadataWizardPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;
	private SamplePrepEditorPanel samplePrepEditorPanel;

	public RDPSamplePrepPanel(RDEMetadataWizard wizard) {
		
		super(wizard);
		
		samplePrepEditorPanel = new SamplePrepEditorPanel(true);
		samplePrepEditorPanel.setWizardStep(true);
		add(samplePrepEditorPanel, gbc_panel);
	}
	
	public void loadPrepDataForExperiment(
			LIMSSamplePreparation samplePrep2, 
			LIMSExperiment newExperiment) {
		
		samplePrep = samplePrep2;
		experiment = newExperiment;
		samplePrepEditorPanel.loadPrepDataForExperiment(samplePrep, experiment);		
	}
	
	@Override
	public void setExperiment(LIMSExperiment newExperiment) {
		experiment = newExperiment;
		samplePrepEditorPanel.setExperiment(experiment);		
	}
	
	@Override
	public void setSamplePrep(LIMSSamplePreparation samplePrep2) {
		this.samplePrep = samplePrep2;
		samplePrepEditorPanel.setExperiment(experiment);
		samplePrepEditorPanel.loadPrepData(samplePrep);
	}
	
	public LIMSSamplePreparation getSamplePrep() {
		samplePrep = samplePrepEditorPanel.getSamplePrep();
		return samplePrep;
	}
	
	public String getPrepName() {
		return samplePrepEditorPanel.getPrepName();
	}

	public Date getPrepDate() {
		return samplePrepEditorPanel.getPrepDate();
	}

	public LIMSUser getPrepUser() {
		return samplePrepEditorPanel.getPrepUser();
	}

	public Collection<LIMSProtocol> getPrepSops(){
		return samplePrepEditorPanel.getPrepSops();
	}

	public Collection<ObjectAnnotation> getPrepAnnotations(){
		return samplePrepEditorPanel.getPrepAnnotations();
	}

	@Override
	public Collection<String> validateInputData() {
		return validateSamplePrepDefinition();
	}
	
	public Collection<String> validateSamplePrepDefinition() {
		return samplePrepEditorPanel.vaidateSamplePrepData();
	}

	public SamplePrepEditorPanel getSamplePrepEditorPanel() {
		return samplePrepEditorPanel;
	}

	public void setPrepEditable(boolean b) {
		samplePrepEditorPanel.setPrepEditable(b);
	}

	@Override
	public void clearPanel() {
		samplePrepEditorPanel.clearPanel();
	}
}
