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
import java.util.Set;
import java.util.TreeSet;

public class ChromatographicGradient implements Serializable {

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
		super();
		this.id = null;
		this.name = name;
		this.description = description;
		this.columnCompartmentTemperature = columnCompartmentTemperature;
		this.stopTime = stopTime;
		
		mobilePhases = new MobilePhase[4];
		gradientSteps = new TreeSet<ChromatographicGradientStep>();
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
}
