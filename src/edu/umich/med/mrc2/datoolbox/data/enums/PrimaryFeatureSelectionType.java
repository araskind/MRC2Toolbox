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

public enum PrimaryFeatureSelectionType {

	AUTOMATIC("Automatic selection"), 
	AS_MARKED("As marked (honor custom assignments)");

	private final String uiName;

	PrimaryFeatureSelectionType(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static PrimaryFeatureSelectionType getOptionByName(String optionName) {

		for(PrimaryFeatureSelectionType o : PrimaryFeatureSelectionType.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static PrimaryFeatureSelectionType getOptionByUIName(String sname) {
		
		for(PrimaryFeatureSelectionType v : PrimaryFeatureSelectionType.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
