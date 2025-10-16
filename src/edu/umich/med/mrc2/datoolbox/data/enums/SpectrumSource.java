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

public enum SpectrumSource {

	EXPERIMENTAL("Experimental"),
	LIBRARY("Library"),
	DATABASE("Database"),
	THEORETICAL("Theoretical");

	private final String uiName;

	SpectrumSource(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}

	public static SpectrumSource getOptionByName(String name) {

		for(SpectrumSource s : SpectrumSource.values()) {

			if(s.name().equals(name))
				return s;
		}
		return null;
	}
	
	SpectrumSource getOptionByUIName(String fieldName) {
		
		for(SpectrumSource f : SpectrumSource.values()) {
			if(f.getName().equals(fieldName))
				return f;
		}
		return null;
	}
}
