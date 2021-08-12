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

public enum ThermoMSOrderType {

	MS1(1, "MS1"),
	MS2(2, "MS2"),
	MS3(3, "MS3"),
	MS4(4, "MS4"),
	MS5(5, "MS5"),
	MS6(6, "MS6"),
	MS7(7, "MS7"),
	MS8(8, "MS8"),
	MS9(9, "MS9"),
	MS10(10, "MS10"),
	;
	
	private final int code;
	private final String name;

	ThermoMSOrderType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static ThermoMSOrderType getThermoMSOrderTypeByCode(int code) {

		for(ThermoMSOrderType p : ThermoMSOrderType.values()) {
			if(p.getCode() == code)
				return p;
		}
		return null;
	}
}
