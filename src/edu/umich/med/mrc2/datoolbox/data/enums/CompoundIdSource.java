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

public enum CompoundIdSource {

	LIBRARY("Library (MS1/RT)"),
	LIBRARY_MS2_RT("Library (MS1/MS2/RT)"),
	LIBRARY_MS2("MSMS Library"),
	DATABASE("Database"),
	MANUAL("Manual assignment"),
	FORMULA_GENERATOR("Mol. formula generator"),
	UNKNOWN("Unknown");

	private final String uiName;

	CompoundIdSource(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}

	public static CompoundIdSource getOptionByName(String name) {

		for(CompoundIdSource source : CompoundIdSource.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static CompoundIdSource getOptionByUIName(String uiname) {
		
		for(CompoundIdSource cat :CompoundIdSource.values()) {
			
			if(cat.getName().equals(uiname))
				return cat;
		}		
		return null;
	}
}
