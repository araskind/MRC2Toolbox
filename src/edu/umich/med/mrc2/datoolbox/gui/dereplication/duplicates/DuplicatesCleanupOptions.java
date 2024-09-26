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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

public enum DuplicatesCleanupOptions {

	USE_PRIMARY_AND_FILL_MISSING("Use primary feature data and fill missing from others"),
	USE_HIGHEST_AREA("Use highest area for each sample"),
	TOP_SCORE_ONLY("Leave only top-scoring feature"), 
	;

	private final String uiName;

	DuplicatesCleanupOptions(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static DuplicatesCleanupOptions getOptionByName(String name) {

		for(DuplicatesCleanupOptions source : DuplicatesCleanupOptions.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static DuplicatesCleanupOptions getOptionByUIName(String fieldName) {
		
		for(DuplicatesCleanupOptions f : DuplicatesCleanupOptions.values()) {
			if(f.getName().equals(fieldName))
				return f;
		}
		return null;
	}
}
