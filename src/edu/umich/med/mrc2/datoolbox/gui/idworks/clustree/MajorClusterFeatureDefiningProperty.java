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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

public enum MajorClusterFeatureDefiningProperty {

	LARGEST_AREA("Largest total peak area"),
	HIGHEST_MSMS_SCORE("Highest MSMS score (regular matches only)"),
	HIGHEST_MSMS_SCORE_WITH_IN_SOURCE("Highest MSMS score (regular and in-source matches)"),
	SMALLEST_MASS_ERROR("Smallest mass error for parent ion"),
	;

	private final String name;

	MajorClusterFeatureDefiningProperty(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public static MajorClusterFeatureDefiningProperty getPropertyByName(String propName) {
		
		for(MajorClusterFeatureDefiningProperty p : MajorClusterFeatureDefiningProperty.values()) {
			
			if(p.name().equals(propName))
				return p;
		}
		return null;
	}
}
