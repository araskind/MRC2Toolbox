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

package edu.umich.med.mrc2.datoolbox.cpdmatch;

public enum DataIntegratorOutputSheets {
	
	SUMMARY("Summary"),
	MATCH_GROUP_SUMMARY("Match Group Summary"),
	UNAMBIGUOUS_MATCH_GROUPS("Unambiguous Match Groups"),
	AMBIGUOUS_MATCH_GROUPS("Ambiguous Match Groups"),
	;
	
	private final String uiName;

	DataIntegratorOutputSheets(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
		
	public static DataIntegratorOutputSheets getOptionByName(String name) {

		for(DataIntegratorOutputSheets source : DataIntegratorOutputSheets.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static DataIntegratorOutputSheets getOptionByUIName(String uiname) {

		for(DataIntegratorOutputSheets source : DataIntegratorOutputSheets.values()) {

			if(source.getName().equals(uiname))
				return source;
		}
		return null;
	}
}
