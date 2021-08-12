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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum Polarity {

	Positive(+1, "P"),
	Negative(-1, "N"),
	Neutral(0, "U");

	private final int sign;
	private final String code;

	Polarity(int sign, String code) {
		this.sign = sign;
		this.code = code;
	}

	public int getSign() {
		return sign;
	}

	public String getCode() {
		return code;
	}

	public static Polarity getPolarityByCode(String code) {

		for(Polarity p : Polarity.values()) {
			if(p.getCode().equals(code))
				return p;
		}
		return null;
	}
	
	public static Polarity getPolarityBySign(int sign) {

		for(Polarity p : Polarity.values()) {
			if(p.getSign() == sign)
				return p;
		}
		return null;
	}
}
