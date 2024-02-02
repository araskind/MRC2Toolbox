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

public enum ElementaryAdducts {

	PROTON_GAIN("H", 1, false),
	PROTON_LOSS("H", -1, true),
	SODIUM("Na", 1, false),
	POTASSIUM("K", 1, false),
	AMMONIUM("NH4", 1, false),
	CHLORINE("Cl", -1, false),
	FORMATE("HCOO", -1, false),
	ACETATE("CH3COO", -1, false),
	TRIFLUOROACETATE("CF3COO", -1, false);

	private final String uiName;
	private final int charge;
	private final boolean allowRemoval;

	ElementaryAdducts(
			String uiName, 
			int charge, 
			boolean allowRemoval) {

		this.uiName = uiName;
		this.charge = charge;
		this.allowRemoval = allowRemoval;
	}

	public boolean allowToRemove() {
		return allowRemoval;
	}

	public int getCharge() {
		return charge;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
		
	public static ElementaryAdducts getOptionByName(String name) {
		
		for(ElementaryAdducts s : ElementaryAdducts.values()) {
			if(s.name().equals(name))
				return s;
		}
		return null;
	}
	
	public static ElementaryAdducts getOptionByUIName(String dbName) {

		for(ElementaryAdducts field : ElementaryAdducts.values()) {

			if(field.getName().equals(dbName))
				return field;
		}
		return null;
	}
}
