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

package edu.umich.med.mrc2.datoolbox.data.thermo;

public enum ThermoIonizationType {

	Unknown(0, "Unknown"),
	ESI(1, "Electrospray"),
	Nanospray(2, "Nanospray"),
	Thermospray(3, "Thermospray"),
	EI(4, "Electron Impact"),
	APCI(5, "Atmospheric Pressure Chemical Ionization"),
	MALDI(6, "Matrix Assisted Laser Desorption Ionization"),
	CI(7, "Chemical Ionization"),
	FAB(8, "Fast Atom Bombardment"),
	FD(9, "Field Desorption"),
	GD(10, "Glow Discharge"),
	;
	
	private final int code;
	private final String name;

	ThermoIonizationType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static ThermoIonizationType getThermoIonizationTypeByCode(int code) {

		for(ThermoIonizationType p : ThermoIonizationType.values()) {
			if(p.getCode() == code)
				return p;
		}
		return null;
	}
}
