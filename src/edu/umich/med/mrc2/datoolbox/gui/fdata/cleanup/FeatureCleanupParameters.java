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

package edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.project.store.FeatureCleanupParameterFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class FeatureCleanupParameters implements XmlStorable{

	private boolean filterByPooledFrequency;
	private int pooledFrequencyCutoff;
	private Collection<ExperimentalSample> selectedPooledSamples;
	
	private boolean filterByMassDefect;
	private double massDefectFilterRTCutoff;
	private double mdFilterMassDefectValue;
	
	private boolean filterHighMassBelowRT;
	private double highMassFilterRTCutoff;
	private double highMassFilterMassValue;	
	
	public FeatureCleanupParameters(
			boolean filterByPooledFrequency, 
			int pooledFrequencyCutoff,
			Collection<ExperimentalSample> selectedPooledSamples, 
			boolean filterByMassDefect,
			double massDefectFilterRTCutoff, 
			double mdFilterMassDefectValue, 
			boolean filterHighMassBelowRT,
			double highMassFilterRTCutoff, 
			double highMassFilterMassValue) {
		super();
		this.filterByPooledFrequency = filterByPooledFrequency;
		this.pooledFrequencyCutoff = pooledFrequencyCutoff;
		this.selectedPooledSamples = selectedPooledSamples;
		this.filterByMassDefect = filterByMassDefect;
		this.massDefectFilterRTCutoff = massDefectFilterRTCutoff;
		this.mdFilterMassDefectValue = mdFilterMassDefectValue;
		this.filterHighMassBelowRT = filterHighMassBelowRT;
		this.highMassFilterRTCutoff = highMassFilterRTCutoff;
		this.highMassFilterMassValue = highMassFilterMassValue;
	}

	public FeatureCleanupParameters() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean isFilterByPooledFrequency() {
		return filterByPooledFrequency;
	}

	public void setFilterByPooledFrequency(boolean filterByPooledFrequency) {
		this.filterByPooledFrequency = filterByPooledFrequency;
	}

	public int getPooledFrequencyCutoff() {
		return pooledFrequencyCutoff;
	}

	public void setPooledFrequencyCutoff(int pooledFrequencyCutoff) {
		this.pooledFrequencyCutoff = pooledFrequencyCutoff;
	}

	public Collection<ExperimentalSample> getSelectedPooledSamples() {
		return selectedPooledSamples;
	}

	public void setSelectedPooledSamples(Collection<ExperimentalSample> selectedPooledSamples) {
		this.selectedPooledSamples = selectedPooledSamples;
	}

	public boolean isFilterByMassDefect() {
		return filterByMassDefect;
	}

	public void setFilterByMassDefect(boolean filterByMassDefect) {
		this.filterByMassDefect = filterByMassDefect;
	}

	public double getMassDefectFilterRTCutoff() {
		return massDefectFilterRTCutoff;
	}

	public void setMassDefectFilterRTCutoff(double massDefectFilterRTCutoff) {
		this.massDefectFilterRTCutoff = massDefectFilterRTCutoff;
	}

	public double getMdFilterMassDefectValue() {
		return mdFilterMassDefectValue;
	}

	public void setMdFilterMassDefectValue(double mdFilterMassDefectValue) {
		this.mdFilterMassDefectValue = mdFilterMassDefectValue;
	}

	public boolean isFilterHighMassBelowRT() {
		return filterHighMassBelowRT;
	}

	public void setFilterHighMassBelowRT(boolean filterHighMassBelowRT) {
		this.filterHighMassBelowRT = filterHighMassBelowRT;
	}

	public double getHighMassFilterRTCutoff() {
		return highMassFilterRTCutoff;
	}

	public void setHighMassFilterRTCutoff(double highMassFilterRTCutoff) {
		this.highMassFilterRTCutoff = highMassFilterRTCutoff;
	}

	public double getHighMassFilterMassValue() {
		return highMassFilterMassValue;
	}

	public void setHighMassFilterMassValue(double highMassFilterMassValue) {
		this.highMassFilterMassValue = highMassFilterMassValue;
	}
	
	public FeatureCleanupParameters(Element featureCleanupParametersElement) {
		
		filterByPooledFrequency = Boolean.parseBoolean(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.filterByPooledFrequency.name()));
		String co = featureCleanupParametersElement.getAttributeValue(
				FeatureCleanupParameterFields.pooledFrequencyCutoff.name());
		pooledFrequencyCutoff = Integer.parseInt(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.pooledFrequencyCutoff.name()));
		
		selectedPooledSamples = new TreeSet<ExperimentalSample>();
		List<Element>selectedPooledSamplesList = 
				featureCleanupParametersElement.getChild(
						FeatureCleanupParameterFields.selectedPooledSamples.name()).
				getChildren(ObjectNames.ExperimentalSample.name());
		for(Element sampleElement : selectedPooledSamplesList)
			selectedPooledSamples.add(new ExperimentalSample(sampleElement, null, null));
		
		filterByMassDefect = Boolean.parseBoolean(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.filterByMassDefect.name()));
		massDefectFilterRTCutoff = Double.parseDouble(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.massDefectFilterRTCutoff.name()));
		mdFilterMassDefectValue = Double.parseDouble(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.mdFilterMassDefectValue.name()));
		
		filterHighMassBelowRT = Boolean.parseBoolean(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.filterHighMassBelowRT.name()));
		highMassFilterRTCutoff = Double.parseDouble(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.highMassFilterRTCutoff.name()));
		highMassFilterMassValue = Double.parseDouble(
				featureCleanupParametersElement.getAttributeValue(
						FeatureCleanupParameterFields.highMassFilterMassValue.name()));
	}
			
	@Override
	public Element getXmlElement() {
		
		Element featureCleanupParametersElement = 
				new Element(ObjectNames.FeatureCleanupParameters.name());
		
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.filterByPooledFrequency.name(), 
				Boolean.toString(filterByPooledFrequency));
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.pooledFrequencyCutoff.name(), 
				Integer.toString(pooledFrequencyCutoff));
		Element selectedPooledSamplesElement = 
				new Element(FeatureCleanupParameterFields.selectedPooledSamples.name());
		for(ExperimentalSample ps : selectedPooledSamples)
			selectedPooledSamplesElement.addContent(ps.getXmlElement());
		
		featureCleanupParametersElement.addContent(selectedPooledSamplesElement);
		
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.filterByMassDefect.name(), 
				Boolean.toString(filterByMassDefect));
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.massDefectFilterRTCutoff.name(), 
				Double.toString(massDefectFilterRTCutoff));
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.mdFilterMassDefectValue.name(), 
				Double.toString(mdFilterMassDefectValue));
		
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.filterHighMassBelowRT.name(), 
				Boolean.toString(filterHighMassBelowRT));
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.highMassFilterRTCutoff.name(), 
				Double.toString(highMassFilterRTCutoff));
		featureCleanupParametersElement.setAttribute(
				FeatureCleanupParameterFields.highMassFilterMassValue.name(), 
				Double.toString(highMassFilterMassValue));
		
		return featureCleanupParametersElement;
	}
}

