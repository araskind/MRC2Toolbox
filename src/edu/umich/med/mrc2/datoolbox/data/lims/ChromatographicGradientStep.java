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

public class ChromatographicGradientStep implements Serializable, Comparable<ChromatographicGradientStep>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7515195422493600374L;
	
	private double startTime;
	private double flowRate;
	private double[] mobilePhaseStartingPercent;

	public ChromatographicGradientStep(
			double startTime, 
			double mobilePhaseBpercent, 
			double flowRate) {
		this(startTime, 
			flowRate,
			100.0d - mobilePhaseBpercent,
			mobilePhaseBpercent,
			0.0d,
			0.0d);
	}

	public ChromatographicGradientStep(
			double startTime, 
			double flowRate, 
			double mobilePhaseApercent,
			double mobilePhaseBpercent, 
			double mobilePhaseCpercent, 
			double mobilePhaseDpercent) {
		super();
		this.startTime = startTime;
		this.flowRate = flowRate;
		mobilePhaseStartingPercent = new double[]{
				mobilePhaseApercent,
				mobilePhaseBpercent,
				mobilePhaseCpercent,
				mobilePhaseDpercent
			};
	}

	/**
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
	}

	/**
	 * @return the flowRate
	 */
	public double getFlowRate() {
		return flowRate;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * @param flowRate the flowRate to set
	 */
	public void setFlowRate(double flowRate) {
		this.flowRate = flowRate;
	}

	@Override
	public int compareTo(ChromatographicGradientStep o) {
		return Double.compare(startTime, o.getStartTime());
	}

	/**
	 * @return the mobilePhaseStertingPercent
	 */
	public double[] getMobilePhaseStartingPercent() {
		return mobilePhaseStartingPercent;
	}
}
