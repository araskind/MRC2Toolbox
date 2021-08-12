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

public enum ThermoCompoundMSnStatus {

	No_MS2(0, "No MS2"),
	DDA_4preferred_ion(1, "DDA for preferred ion"),
	DDA_4other_ion(2, "DDA for other ion"),
	DIA_only(3, "DIA only"),
	;
	
	private final int code;
	private final String name;

	ThermoCompoundMSnStatus(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static ThermoCompoundMSnStatus getThermoCompoundMSnStatusByCode(int code) {

		for(ThermoCompoundMSnStatus p : ThermoCompoundMSnStatus.values()) {
			if(p.getCode() == code)
				return p;
		}
		return null;
	}
}
