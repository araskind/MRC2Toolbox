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

package edu.umich.med.mrc2.datoolbox.data.thermo;

public enum ThermoBestHitType {

	Unknown(0, "Unknown type"),
	Best_MS1(1, "Best MS1"),
	Best_MS2(2, "Best MS2"),
	Best_Deconvoluted_MS(4, "Best Deconvoluted MS"),
	;
	
	private final int code;
	private final String name;

	ThermoBestHitType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static ThermoBestHitType getThermoBestHitTypeByCode(int code) {

		for(ThermoBestHitType p : ThermoBestHitType.values()) {
			if(p.getCode() == code)
				return p;
		}
		return null;
	}
}
