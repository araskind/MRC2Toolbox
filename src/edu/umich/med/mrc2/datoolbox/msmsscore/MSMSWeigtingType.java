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

package edu.umich.med.mrc2.datoolbox.msmsscore;

public enum MSMSWeigtingType {

	NIST_EI("NIST EI", 0.6d, 3.0d),
	MASS_BANK("MASSBANK", 0.5d, 2.0d),
	;
	
	private final String name;
	private final double intensityPower;
	private final double mzPower;

	MSMSWeigtingType(String name, double intensityPower, double mzPower) {
		this.name = name;
		this.intensityPower = intensityPower;
		this.mzPower = mzPower;
	}

	public String getName() {
		return name;
	}

	public double getIntensityPower() {
		return intensityPower;
	}

	public double getMzPower() {
		return mzPower;
	}
}
