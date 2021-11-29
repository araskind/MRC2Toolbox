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

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ExtractedIonData {

	private String name;
	private double extractedMass;
	private double[] timeValues;
	private double[] intensityValues;
	private Adduct adduct;
	
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
}
