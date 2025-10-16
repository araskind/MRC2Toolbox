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

package edu.umich.med.mrc2.datoolbox.data;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class StoredExtractedIonData extends ExtractedIonData {

	private static final long serialVersionUID = 1L;
	private String featureId;
	private String injectionId;
	private int msLevel;
	private double massErrorValue;
	private MassErrorType massErrorType;
	private Range rtRange;

	public StoredExtractedIonData(
			String name, 
			double extractedMass, 
			double[] timeValues, 
			double[] intensityValues,
			String featureId, 
			String injectionId, 
			int msLevel, 
			double massErrorValue, 
			MassErrorType massErrorType,
			double rtStart,
			double rtEnd) {
		super(name, extractedMass, timeValues, intensityValues);
		this.featureId = featureId;
		this.injectionId = injectionId;
		this.msLevel = msLevel;
		this.massErrorValue = massErrorValue;
		this.massErrorType = massErrorType;
		this.rtRange = new Range(rtStart, rtEnd);
	}

	public String getFeatureId() {
		return featureId;
	}

	public String getInjectionId() {
		return injectionId;
	}

	public int getMsLevel() {
		return msLevel;
	}

	public double getMassErrorValue() {
		return massErrorValue;
	}

	public MassErrorType getMassErrorType() {
		return massErrorType;
	}

	public Range getRtRange() {
		return rtRange;
	}	
}














