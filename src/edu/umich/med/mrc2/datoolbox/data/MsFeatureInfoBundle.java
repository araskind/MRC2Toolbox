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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineProjectLoadCash;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureInfoBundleFields;
import edu.umich.med.mrc2.datoolbox.project.store.StoredMsFeatureFields;

public class MsFeatureInfoBundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -640157593085281408L;
	
	private MsFeature msFeature;
	private DataAcquisitionMethod acquisitionMethod;
	private DataExtractionMethod dataExtractionMethod;
	private LIMSExperiment experiment;
	private StockSample stockSample;
	private IDTExperimentalSample sample;
	private String injectionId;
	private Collection<MSFeatureIdentificationFollowupStep>idFollowupSteps;
	private Collection<StandardFeatureAnnotation>standadAnnotations;
	
	private DataFile dataFile;

	public MsFeatureInfoBundle(MsFeature msFeature) {
		super();
		this.msFeature = msFeature;
		idFollowupSteps =  
				new TreeSet<MSFeatureIdentificationFollowupStep>();
		standadAnnotations = 
				new TreeSet<StandardFeatureAnnotation>();
	}

	/**
	 * @return the msFeature
	 */
	public MsFeature getMsFeature() {
		return msFeature;
	}

	/**
	 * @return the acquisitionMethod
	 */
	public DataAcquisitionMethod getAcquisitionMethod() {
		return acquisitionMethod;
	}

	/**
	 * @return the dataExtractionMethod
	 */
	public DataExtractionMethod getDataExtractionMethod() {
		return dataExtractionMethod;
	}

	/**
	 * @return the experiment
	 */
	public LIMSExperiment getExperiment() {
		return experiment;
	}

	/**
	 * @param acquisitionMethod the acquisitionMethod to set
	 */
	public void setAcquisitionMethod(DataAcquisitionMethod acquisitionMethod) {
		this.acquisitionMethod = acquisitionMethod;
	}

	/**
	 * @param dataExtractionMethod the dataExtractionMethod to set
	 */
	public void setDataExtractionMethod(DataExtractionMethod dataExtractionMethod) {
		this.dataExtractionMethod = dataExtractionMethod;
	}

	/**
	 * @param experiment the experiment to set
	 */
	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}

	/**
	 * @return the stockSample
	 */
	public StockSample getStockSample() {
		return stockSample;
	}

	/**
	 * @return the sample
	 */
	public IDTExperimentalSample getSample() {
		return sample;
	}

	/**
	 * @param stockSample the stockSample to set
	 */
	public void setStockSample(StockSample stockSample) {
		this.stockSample = stockSample;
	}

	/**
	 * @param sample the sample to set
	 */
	public void setSample(IDTExperimentalSample sample) {
		this.sample = sample;
		if(sample != null)
			this.stockSample = sample.getParentStockSample();
	}

	/**
	 * @return the injectionId
	 */
	public String getInjectionId() {
		return injectionId;
	}

	/**
	 * @param injectionId the injectionId to set
	 */
	public void setInjectionId(String injectionId) {
		this.injectionId = injectionId;
	}

	/**
	 * @return the idFollowupSteps
	 */
	public Collection<MSFeatureIdentificationFollowupStep> getIdFollowupSteps() {
		return idFollowupSteps;
	}

	public void addIdFollowupStep(MSFeatureIdentificationFollowupStep newStep) {
		
		if(idFollowupSteps == null)
			idFollowupSteps =  new TreeSet<MSFeatureIdentificationFollowupStep>();
		
		idFollowupSteps.add(newStep);
	}

	public void removeIdFollowupStep(MSFeatureIdentificationFollowupStep toRemove) {
		
		if(idFollowupSteps == null) {
			idFollowupSteps =  new TreeSet<MSFeatureIdentificationFollowupStep>();
			return;
		}		
		idFollowupSteps.remove(toRemove);
	}
	
	public Collection<StandardFeatureAnnotation> getStandadAnnotations() {
		return standadAnnotations;
	}
	
	public void addStandardFeatureAnnotation(StandardFeatureAnnotation newAnnotation) {
		
		if(standadAnnotations == null)
			standadAnnotations =  new TreeSet<StandardFeatureAnnotation>();
		
		standadAnnotations.add(newAnnotation);
	}

	public void removeStandardFeatureAnnotation(StandardFeatureAnnotation toRemove) {
		
		if(standadAnnotations == null) {
			standadAnnotations =  new TreeSet<StandardFeatureAnnotation>();
			return;
		}		
		standadAnnotations.remove(toRemove);
	}
	
	public Double getPrecursorMz() {
		
		if(msFeature.getSpectrum() == null)
			return null;
		
		if(msFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL) == null)
			return null;
		
		if(msFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL).getParent() == null)
			return null;
		
		return msFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL).getParent().getMz();
	}
	
	public double getRetentionTime() {
		return msFeature.getRetentionTime();
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureInfoBundle.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureInfoBundle other = (MsFeatureInfoBundle) obj;

        if ((this.msFeature == null) ? (other.getMsFeature() != null) : !this.msFeature.equals(other.getMsFeature()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.msFeature != null ? this.msFeature.hashCode() : 0);
        return hash;
    }

	public DataFile getDataFile() {
		return dataFile;
	}

	public void setDataFile(DataFile dataFile) {
		this.dataFile = dataFile;
	}
	
	public String getMSMSFeatureId() {
		
		TandemMassSpectrum msms = 
				msFeature.getSpectrum().getExperimentalTandemSpectrum();
		if (msms != null)
			return msms.getId();
		else
			return null;
	}
	
	public Element getXmlElement(Document parentDocument) {
		
		Element msFeatureInfoBundleElement = parentDocument.createElement(
				MsFeatureInfoBundleFields.MFIB.name());
		
		if(acquisitionMethod != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.AcqMethod.name(), 
					acquisitionMethod.getId());
		
		if(dataExtractionMethod != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.DaMethod.name(), 
					dataExtractionMethod.getId());
		
		if(experiment != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.Exp.name(), 
					experiment.getId());
		
		if(sample != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.Sample.name(), 
					sample.getId());
		
		if(injectionId != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.Inj.name(), 
					injectionId);
		
		msFeatureInfoBundleElement.appendChild(
				msFeature.getXmlElement(parentDocument));	
		
		if(idFollowupSteps != null && !idFollowupSteps.isEmpty()) {
			
			List<String> fusList = idFollowupSteps.stream().
					map(s -> s.getId()).collect(Collectors.toList());
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.FUS.name(), 
					StringUtils.join(fusList, "@"));
		}
		if(standadAnnotations != null && !standadAnnotations.isEmpty()) {
			
			List<String> stanList = standadAnnotations.stream().
					map(s -> s.getId()).collect(Collectors.toList());
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.StAn.name(), 
					StringUtils.join(stanList, "@"));
		}				
		return msFeatureInfoBundleElement;		
	}
	
	public org.jdom2.Element getXmlElement() {
		
		org.jdom2.Element msFeatureInfoBundleElement = 
				new org.jdom2.Element(MsFeatureInfoBundleFields.MFIB.name());
		
		if(acquisitionMethod != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.AcqMethod.name(), 
					acquisitionMethod.getId());
		
		if(dataExtractionMethod != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.DaMethod.name(), 
					dataExtractionMethod.getId());
		
		if(experiment != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.Exp.name(), 
					experiment.getId());
		
		if(sample != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.Sample.name(), 
					sample.getId());
		
		if(injectionId != null)
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.Inj.name(), 
					injectionId);
		
		msFeatureInfoBundleElement.addContent(
				msFeature.getXmlElement());
		
		if(idFollowupSteps != null && !idFollowupSteps.isEmpty()) {
			
			List<String> fusList = idFollowupSteps.stream().
					map(s -> s.getId()).collect(Collectors.toList());
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.FUS.name(), 
					StringUtils.join(fusList, "@"));
		}
		if(standadAnnotations != null && !standadAnnotations.isEmpty()) {
			
			List<String> stanList = standadAnnotations.stream().
					map(s -> s.getId()).collect(Collectors.toList());
			msFeatureInfoBundleElement.setAttribute(
					MsFeatureInfoBundleFields.StAn.name(), 
					StringUtils.join(stanList, "@"));
		}	
		return msFeatureInfoBundleElement;
	}
	
	public MsFeatureInfoBundle(org.jdom2.Element featureElement) {
		
		msFeature = new MsFeature(
				featureElement.getChild(StoredMsFeatureFields.MsFeature.name()));
		
		String acqMethodId = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.AcqMethod.name());
		if(acqMethodId != null)
			acquisitionMethod = IDTDataCash.getAcquisitionMethodById(acqMethodId);

		String daMethodId = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.DaMethod.name());
		if(daMethodId != null)
			dataExtractionMethod = IDTDataCash.getDataExtractionMethodById(daMethodId);
		
		String experimentId = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.Exp.name());
		if(experimentId != null)
			experiment = IDTDataCash.getExperimentById(experimentId);
		
		String sampleId = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.Sample.name());
		if(sampleId != null)
			sample = (IDTExperimentalSample)OfflineProjectLoadCash.getExperimentalSampleById(sampleId);
		if(sample != null)
			stockSample = sample.getParentStockSample();
		
		injectionId = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.Inj.name());
		
		idFollowupSteps =  
				new TreeSet<MSFeatureIdentificationFollowupStep>();
		String idFollowupStepsString = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.FUS.name());
		if(idFollowupStepsString != null) {
			String[]fusList = idFollowupStepsString.split("@");
			for(String fusId : fusList) {
				MSFeatureIdentificationFollowupStep fus = 
						IDTDataCash.getMSFeatureIdentificationFollowupStepById(fusId);
				if(fus != null)
					idFollowupSteps.add(fus);
			}		
		}
		standadAnnotations = 
				new TreeSet<StandardFeatureAnnotation>();
		String stAnStepsString = 
				featureElement.getAttributeValue(MsFeatureInfoBundleFields.StAn.name());
		if(stAnStepsString != null) {
			String[]stAnList = stAnStepsString.split("@");
			for(String stAnId : stAnList) {
				StandardFeatureAnnotation stan = 
						IDTDataCash.getStandardFeatureAnnotationById(stAnId);
				if(stan != null)
					standadAnnotations.add(stan);
			}		
		}
	}
}
