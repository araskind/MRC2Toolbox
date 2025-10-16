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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum MSMSScoringParameter {
	
	NIST_SCORE("NIST score"),
	ENTROPY_SCORE("Entropy based score"),
	DOT_PRODUCT("Dot-product"),
	PROBABILITY("Probability"),
	;

	private final String uiName;

	MSMSScoringParameter(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static MSMSScoringParameter getOptionByName(String name) {
		
		for(MSMSScoringParameter s : MSMSScoringParameter.values()) {
			if(s.name().equals(name))
				return s;
		}
		return null;
	}
	
	public static MSMSScoringParameter getOptionByUIName(String uiName) {
		
		for(MSMSScoringParameter f : MSMSScoringParameter.values()) {
			
			if(f.getName().equals(uiName))
				return f;
		}		
		return null;
	}
}
