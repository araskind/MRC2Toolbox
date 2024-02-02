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

public enum InChiKeyCharge {

	N(0),
	M(-1),
	L(-2),
	K(-3),
	J(-4),
	I(-5),
	O(1),
	P(2),
	Q(3),
	R(4),
	S(5),
	;

	private final int charge;

	InChiKeyCharge(int charge) {
		this.charge = charge;
	}

	public int getCharge() {
		return charge;
	}
	
	public static int getChargeByCode(String code) {
		
		for(InChiKeyCharge v : InChiKeyCharge.values()) {
			if(v.name().equals(code))
				return v.getCharge();
		}
		return 0;
	}
	
	public static String getCodeByCharge(int charge) {
		
		for(InChiKeyCharge v : InChiKeyCharge.values()) {
			if(v.getCharge() == charge)
				return v.name();
		}
		return null;
	}
}
