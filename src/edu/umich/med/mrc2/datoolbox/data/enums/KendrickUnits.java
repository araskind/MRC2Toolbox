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

public enum KendrickUnits {

	NONE("No adjustment", 1.0d),
	RELATIVE("Relative", Double.NaN),
	METHYLENE("Methylene", 0.9988834),
	ETHYLENE_OXIDE("Ethylene oxide (PEG)", 0.9994049),
	PROPYLENE_OXIDE("Propylene oxide (PPG)", 0.9992781),
	OXYDATION("Oxydation", 1.00025),
	DOUBLE_BOND("Double bond", 0.9922111),
	;

	private final String uiName;
	private final double multiplier;

	KendrickUnits(String uiName, double multiplier) {
		this.uiName = uiName;
		this.multiplier = multiplier;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
	
	public static KendrickUnits getKendrickUnitsByName(String name) {
		
		for(KendrickUnits u : KendrickUnits.values()) {
			
			if(u.name().equals(name))
				return u;				
		}		
		return null;
	}
}
