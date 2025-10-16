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

import java.util.Collection;
import java.util.TreeSet;

public enum CompoundIdSource {

	MANUAL("Manual assignment", 1),
	LIBRARY_MS2_RT("Library (MS1/MS2/RT)", 2), 
	LIBRARY("Library (MS1/RT)", 3),
	LIBRARY_MS2("Library (MS1/MS2", 4),
	LIBRARY_MS_ONLY("Library (MS1, NO RT)", 5), 	
	DATABASE("Database", 7),
	FORMULA_GENERATOR("Mol. formula generator", 8),
	UNKNOWN("Unknown", 9);

	private final String uiName;
	private final int rank;

	CompoundIdSource(String uiName, int rank) {
		this.uiName = uiName;
		this.rank = rank;
	}

	public String getName() {
		return uiName;
	}
	
	public int getRank() {
		return rank;
	}
	
	public String toString() {
		return uiName;
	}
	
	public static CompoundIdSource getOptionByName(String name) {
		
		if(name == null)
			return null;
		
		for(CompoundIdSource source : CompoundIdSource.values()) {
			
			if(source.name().equals(name))
				return source;
		}		
		return null;
	}
	
	public static Collection<CompoundIdSource>getLibraryIdSources(){
		
		 Collection<CompoundIdSource>libSources = new TreeSet<CompoundIdSource>();
		 libSources.add(LIBRARY_MS2);
		 libSources.add(LIBRARY);
		 libSources.add(LIBRARY_MS_ONLY);		 
		 return libSources;
	}
}
