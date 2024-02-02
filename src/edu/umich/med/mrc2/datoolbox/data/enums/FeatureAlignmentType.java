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

public enum FeatureAlignmentType {

	ALIGN_TO_LIBRARY("Align to library"),
	ALL_FEATURES("All features"),
	IDENTIFIED_ONLY_FEATURES("Identified features only"),
	UNIDENTIFIED_ONLY_FEATURES("Unidentified features only");

	private final String uiName;

	FeatureAlignmentType(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static FeatureAlignmentType getOptionByName(String name) {
		
		for(FeatureAlignmentType s : FeatureAlignmentType.values()) {
			if(s.name().equals(name))
				return s;
		}
		return null;
	}
	
	public static FeatureAlignmentType getOptionByUIName(String dbName) {

		for(FeatureAlignmentType field : FeatureAlignmentType.values()) {

			if(field.getName().equals(dbName))
				return field;
		}
		return null;
	}
}
