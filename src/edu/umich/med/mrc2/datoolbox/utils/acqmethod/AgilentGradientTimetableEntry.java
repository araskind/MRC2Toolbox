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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

public class AgilentGradientTimetableEntry {

	private AgilentGradientTimetableEntryType type;
	private String timeCode;
	private double flow;
	private double highPressureLimit;
	private double percentA;
	private double percentB;
	private double percentC;
	private double percentD;
	
	public AgilentGradientTimetableEntry(
			AgilentGradientTimetableEntryType type, 
			String timeCode) {
		super();
		this.type = type;
		this.timeCode = timeCode;
	}

	public double getFlow() {
		return flow;
	}

	public void setFlow(double flow) {
		this.flow = flow;
	}

	public double getHighPressureLimit() {
		return highPressureLimit;
	}

	public void setHighPressureLimit(double highPressureLimit) {
		this.highPressureLimit = highPressureLimit;
	}

	public double getPercentA() {
		return percentA;
	}

	public void setPercentA(double percentA) {
		this.percentA = percentA;
	}

	public double getPercentB() {
		return percentB;
	}

	public void setPercentB(double percentB) {
		this.percentB = percentB;
	}

	public double getPercentC() {
		return percentC;
	}

	public void setPercentC(double percentC) {
		this.percentC = percentC;
	}

	public double getPercentD() {
		return percentD;
	}

	public void setPercentD(double percentD) {
		this.percentD = percentD;
	}

	public AgilentGradientTimetableEntryType getType() {
		return type;
	}

	public String getTimeCode() {
		return timeCode;
	}
}


