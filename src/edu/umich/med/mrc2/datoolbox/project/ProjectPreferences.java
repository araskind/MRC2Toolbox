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

package edu.umich.med.mrc2.datoolbox.project;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ProjectPreferences {

	private double correlationCutoff;
	private double rtWindow;
	private double massAccuracy;
	private double pooledFrequencyWeight;
	private double pooledAreaWeight;
	private char dataDelimiter;

	public ProjectPreferences() {
		super();
		dataDelimiter = MRC2ToolBoxConfiguration.getTabDelimiter();
	}

	public double getCorrelationCutoff() {
		return correlationCutoff;
	}

	public char getDataDelimiter() {
		return dataDelimiter;
	}

	public double getMassAccuracy() {
		return massAccuracy;
	}

	public double getPooledAreaWeight() {
		return pooledAreaWeight;
	}

	public double getPooledFrequencyWeight() {
		return pooledFrequencyWeight;
	}

	public double getRtWindow() {
		return rtWindow;
	}

	public void setCorrelationCutoff(double correlationCutoff) {
		this.correlationCutoff = correlationCutoff;
	}

	public void setDataDelimiter(char dataDelimiter) {
		this.dataDelimiter = dataDelimiter;
	}

	public void setMassAccuracy(double massAccuracy) {
		this.massAccuracy = massAccuracy;
	}

	public void setPooledAreaWeight(double pooledAreaWeight) {
		this.pooledAreaWeight = pooledAreaWeight;
	}

	public void setPooledFrequencyWeight(double pooledFrequencyWeight) {
		this.pooledFrequencyWeight = pooledFrequencyWeight;
	}

	public void setRtWindow(double rtWindow) {
		this.rtWindow = rtWindow;
	}
}
