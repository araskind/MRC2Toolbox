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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.project.store.ChromatographicGradientFields;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class ChromatographicGradient implements Serializable, XmlStorable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 316963583786972314L;
	private String id;
	private String name;
	private String description;
	private double columnCompartmentTemperature;
	private double stopTime;
	
	private Set<ChromatographicGradientStep>gradientSteps;
	private MobilePhase[] mobilePhases;
	
	public ChromatographicGradient() {
		super();
		id = DataPrefix.CROMATOGRAPHIC_GRADIENT.getName() + 
				UUID.randomUUID().toString().substring(0, 6);
		//	name = "ChromGradient-" + FIOUtils.getTimestamp();
		mobilePhases = new MobilePhase[4];
		gradientSteps = new TreeSet<ChromatographicGradientStep>();
	}

	public ChromatographicGradient(
			String id, 
			String name, 
			String description, 
			double columnCompartmentTemperature,
			double stopTime) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.columnCompartmentTemperature = columnCompartmentTemperature;
		this.stopTime = stopTime;
		
		mobilePhases = new MobilePhase[4];
		gradientSteps = new TreeSet<ChromatographicGradientStep>();
	}
	
	public ChromatographicGradient(
			String name, 
			String description, 
			double columnCompartmentTemperature,
			double stopTime) {
		this(null, name, description, columnCompartmentTemperature, stopTime);
	}
	
	public void setMobilePhase(MobilePhase phase, int order) {
		
		if(order > 3)
			throw new IllegalArgumentException("Mobile phase number can not exceede 4");
		
		mobilePhases[order] = phase;
	}
	
	public void addChromatographicGradientStep(ChromatographicGradientStep newStep) {
		gradientSteps.add(newStep);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the columnCompartmentTemperature
	 */
	public double getColumnCompartmentTemperature() {
		return columnCompartmentTemperature;
	}

	/**
	 * @return the gradientSteps
	 */
	public Set<ChromatographicGradientStep> getGradientSteps() {
		return gradientSteps;
	}
	
	public ChromatographicGradientStep[]getGradientStepsArray() {
		return gradientSteps.toArray(
				new ChromatographicGradientStep[gradientSteps.size()]);
	}

	/**
	 * @return the mobilePhases
	 */
	public MobilePhase[] getMobilePhases() {
		return mobilePhases;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param columnCompartmentTemperature the columnCompartmentTemperature to set
	 */
	public void setColumnCompartmentTemperature(double columnCompartmentTemperature) {
		this.columnCompartmentTemperature = columnCompartmentTemperature;
	}

	/**
	 * @return the stopTime
	 */
	public double getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(double stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isTimeTableDefined() {
		
		if(gradientSteps.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean areMobilePhasesDefined() {
		
		int mpCount = 0;
		for(MobilePhase m : mobilePhases) {
			
			if(m != null)
				mpCount++;
		}
		if(mpCount == 0)
			return false;
		
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!ChromatographicGradient.class.isAssignableFrom(obj.getClass()))
			return false;

		final ChromatographicGradient other = (ChromatographicGradient) obj;

		if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
			return false;

		return true;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    public ChromatographicGradient(Element chromatographicGradientElement) {
    	
    	this();
    	id = chromatographicGradientElement.getAttributeValue(
    			CommonFields.Id.name());
    	
    	//	TODO remove
    	name = chromatographicGradientElement.getAttributeValue(
    			CommonFields.Name.name());
    	if(name == null)
    		name = ProjectStoreUtils.getTextFromElement(
    				chromatographicGradientElement, CommonFields.Name);
    	
    	//	TODO remove
    	description = chromatographicGradientElement.getAttributeValue(
    			CommonFields.Description.name());
    	if(description == null)
    		description = 
    			ProjectStoreUtils.getDescriptionFromElement(chromatographicGradientElement);
    	
    	columnCompartmentTemperature = Double.parseDouble(
    			chromatographicGradientElement.getAttributeValue(
    				ChromatographicGradientFields.ccTemp.name()));
    	stopTime = Double.parseDouble(
    			chromatographicGradientElement.getAttributeValue(
    				ChromatographicGradientFields.stopTime.name()));
    	
		List<Element> stepList = 
				chromatographicGradientElement.getChild(
						ChromatographicGradientFields.StepList.name()).
						getChildren(ObjectNames.ChromatographicGradientStep.name());
		
		for(Element stepElement : stepList)			
			gradientSteps.add(new ChromatographicGradientStep(stepElement));
		
		List<Element> mobilePhaseList = 
				chromatographicGradientElement.getChild(
						ChromatographicGradientFields.MobPhaseList.name()).
						getChildren(ObjectNames.MobilePhase.name());
		for(int i=0; i<4; i++) {
			
			if(i >= mobilePhaseList.size()) {
				mobilePhases[i] = null;
				break;
			}
			Element mobilePhaseElement = mobilePhaseList.get(i);
			if(mobilePhaseElement.getAttributeValue(
					CommonFields.Id.name()).equals(CommonFields.NULL.name())){
				mobilePhases[i] = null;
			}
			else {
				mobilePhases[i] = new MobilePhase(mobilePhaseElement);
			}
		}		
    }

	@Override
	public Element getXmlElement() {

		Element chromatographicGradientElement = 
				new Element(ObjectNames.ChromatographicGradient.name());
		chromatographicGradientElement.setAttribute(CommonFields.Id.name(), id);
		ProjectStoreUtils.addTextElement(
				name, chromatographicGradientElement, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(description, chromatographicGradientElement);
		
		chromatographicGradientElement.setAttribute(
				ChromatographicGradientFields.ccTemp.name(), 
				String.format("%.2f", columnCompartmentTemperature));
		chromatographicGradientElement.setAttribute(
				ChromatographicGradientFields.stopTime.name(), 
				String.format("%.3f", stopTime));
		
		Element stepListElement = 
				new Element(ChromatographicGradientFields.StepList.name());
		for(ChromatographicGradientStep step : gradientSteps)			
			stepListElement.addContent(step.getXmlElement());
		
		chromatographicGradientElement.addContent(stepListElement);	
		
		Element mobPhaseListElement = 
				new Element(ChromatographicGradientFields.MobPhaseList.name());
		
		for(int i=0; i<4; i++) {
			
			if(mobilePhases[i] == null) {
				
				Element mobPhaseElement = 
						new Element(ObjectNames.MobilePhase.name());
				mobPhaseElement.setAttribute(
						CommonFields.Id.name(), CommonFields.NULL.name());
				mobPhaseListElement.addContent(mobPhaseElement);
			}
			else {
				mobPhaseListElement.addContent(mobilePhases[i].getXmlElement());
			}
		}
		chromatographicGradientElement.addContent(mobPhaseListElement);	

		return chromatographicGradientElement;
	}
}










