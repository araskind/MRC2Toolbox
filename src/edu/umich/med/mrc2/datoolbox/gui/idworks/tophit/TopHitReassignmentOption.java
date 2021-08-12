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

package edu.umich.med.mrc2.datoolbox.gui.idworks.tophit;

public enum TopHitReassignmentOption {

	PREFER_METLIN("Prefer METLIN matches to other libraries"),
	PREFER_NORMAL_HITS("Prefer normal hits (consider parent M/Z)"),
	ALLOW_IN_SOURCE_HITS("Allow in-source search results as top hit"),
	ALLOW_HYBRID_HITS("Allow hybrid search results as top hit");

	private final String name;

	TopHitReassignmentOption(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static TopHitReassignmentOption getTopHitReassignmentOptionByName(String name) {
		
		for(TopHitReassignmentOption o : TopHitReassignmentOption.values()) {
			if(o.name().equals(name))
				return o;
		}		
		return null;
	}
}
