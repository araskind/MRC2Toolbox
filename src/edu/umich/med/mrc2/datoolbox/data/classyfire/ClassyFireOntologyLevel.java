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

package edu.umich.med.mrc2.datoolbox.data.classyfire;

public enum ClassyFireOntologyLevel {

	KINGDOM("kingdom"),
	SUPERCLASS("superclass"),
	CLASS("class"),
	SUBCLASS("subclass"),
	INTERMEDIATE_NODE("intermediate_node"),
	DIRECT_PARENT("direct_parent"),
	ALTERNATIVE_PARENT("alternative_parent"),
	;
	
	private final String name;

	ClassyFireOntologyLevel(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public static ClassyFireOntologyLevel getOntologyLevelByName(String name) {
		
		for(ClassyFireOntologyLevel cat :ClassyFireOntologyLevel.values()) {
			
			if(cat.name().equals(name))
				return cat;
		}		
		return null;
	}
}
