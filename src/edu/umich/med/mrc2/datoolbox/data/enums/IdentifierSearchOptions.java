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

public enum IdentifierSearchOptions {

	NAME_EQUALS("Name is (exact match)"),
	NAME_STARTS_WITH("Name starts with"),
	NAME_CONTAINS("Name contains"),
	COMPOUND_ID("Compound id is (exact match)");

	private final String name;

	IdentifierSearchOptions(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public static IdentifierSearchOptions getOptionByName(String optionName) {

		for(IdentifierSearchOptions o : IdentifierSearchOptions.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
}











