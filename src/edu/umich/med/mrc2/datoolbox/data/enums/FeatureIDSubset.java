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

public enum FeatureIDSubset {
	
	PRIMARY_ONLY("Primary ID only"),
	BEST_SCORING_ONLY("Best scoring for selected match type"),
	BEST_FOR_EACH_COMPOUND("Top-scoring ID for each compound"),
	ALL("All identifications"),
	;

	private final String uiName;

	FeatureIDSubset(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static FeatureIDSubset getOptionByName(String name) {
		
		for(FeatureIDSubset s : FeatureIDSubset.values()) {
			if(s.name().equals(name))
				return s;
		}
		return null;
	}
	
	public static FeatureIDSubset getOptionByUIName(String dbName) {

		for(FeatureIDSubset field : FeatureIDSubset.values()) {

			if(field.getName().equals(dbName))
				return field;
		}
		return null;
	}
}
