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

import java.io.UnsupportedEncodingException;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.ExtractedIonDataFields;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;

public class ExtractedIonData {

	protected String name;
	protected double extractedMass;
	protected double[] timeValues;
	protected double[] intensityValues;
	protected Adduct adduct;
	
	public ExtractedIonData(
			String name, 
			double extractedMass, 
			double[] timeValues, 
			double[] intensityValues) {
		super();
		this.name = name;
		this.extractedMass = extractedMass;
		this.timeValues = timeValues;
		this.intensityValues = intensityValues;
	}
	
	public ExtractedIonData(
			double extractedMass, 
			double[] timeValues, 
			double[] intensityValues) {
		super();
		this.extractedMass = extractedMass;
		this.timeValues = timeValues;
		this.intensityValues = intensityValues;
		this.name = MRC2ToolBoxConfiguration.getMzFormat().format(extractedMass);
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		
		String returnName = name;
		if(adduct != null)
			returnName += " (" + adduct.getName() + ")";
		
		return returnName;		
	}

	public double getExtractedMass() {
		return extractedMass;
	}

	public double[] getTimeValues() {
		return timeValues;
	}

	public double[] getIntensityValues() {
		return intensityValues;
	}

	public Adduct getAdduct() {
		return adduct;
	}

	public void setAdduct(Adduct adduct) {
		this.adduct = adduct;
	}
	
	public String getEncodedTimeString() {
		
		double[]times = new double[timeValues.length];
		for(int i=0; i<timeValues.length; i++)
			times[i] =  Math.floor(timeValues[i] * 1000) / 1000;
		
		String timeString = "";
		try {
			timeString = NumberArrayUtils.encodeNumberArray(times);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeString;
	}
	
	public String getEncodedIntensityString() {
		
		double[]intensities = new double[intensityValues.length];
		for(int i=0; i<intensityValues.length; i++)
			intensities[i] =  Math.floor(intensityValues[i] * 100) / 100;

		String intensityString = "";
		try {
			intensityString = NumberArrayUtils.encodeNumberArray(intensities);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return intensityString;
	}

	public Element getXmlElement() {
		
		Element extractedIonDataElement = 
				new Element(ExtractedIonDataFields.XICData.name());
		
		if(name != null)
			extractedIonDataElement.setAttribute(
					ExtractedIonDataFields.Name.name(), name);
		
		if(adduct != null)
			extractedIonDataElement.setAttribute(
					ExtractedIonDataFields.Adduct.name(), adduct.getId());
		
		String targetMz = 
				Double.toString(Math.floor(extractedMass * 1000000) / 1000000);
		extractedIonDataElement.setAttribute(
				ExtractedIonDataFields.Target.name(), targetMz);	
		
		String timeString = getEncodedTimeString();
		Element timeElement = 
				new Element(ExtractedIonDataFields.Time.name()).setText(timeString);			
		extractedIonDataElement.addContent(timeElement);

		String intensityString = getEncodedIntensityString();
		Element intensityElement = 
				new Element(ExtractedIonDataFields.Intensity.name()).setText(intensityString);			
		extractedIonDataElement.addContent(intensityElement);
				
		return extractedIonDataElement;
	}
	
	public ExtractedIonData(Element xicElement) {
		
		name = xicElement.getAttributeValue(ExtractedIonDataFields.Name.name());		
		String targetMass = 
				xicElement.getAttributeValue(ExtractedIonDataFields.Target.name());
		if(targetMass != null)
			extractedMass = Double.parseDouble(targetMass);
		
		String adductId = 
				xicElement.getAttributeValue(ExtractedIonDataFields.Adduct.name());
		if (adductId != null)
			adduct = AdductManager.getAdductById(adductId);
	
		String timeText =  
				xicElement.getChild(ExtractedIonDataFields.Time.name()).
					getContent().get(0).getValue();
		try {
			timeValues = NumberArrayUtils.decodeNumberArray(timeText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String intensityText =  
				xicElement.getChild(ExtractedIonDataFields.Intensity.name()).
					getContent().get(0).getValue();
		try {
			intensityValues = NumberArrayUtils.decodeNumberArray(intensityText);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}














