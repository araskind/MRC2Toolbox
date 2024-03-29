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

public enum MGFFields {

	BEGIN_BLOCK("BEGIN IONS"),
	PEPMASS("PEPMASS"),
	CHARGE("CHARGE"),
	TITLE("TITLE"),
	FILENAME("FILENAME"),
	RTINSECONDS("RTINSECONDS"),
	UNPD_ID("UNPD_ID"),
	SCANS("SCANS"),
	END_BLOCK("END_BLOCK"),
	END_IONS("END IONS"),
	IONMODE("IONMODE"),
	MOLECULAR_FORMULA("MOLECULAR_FORMULA"),
	NAME("NAME"),
	EXACTMASS("EXACTMASS"),
	SMILES("SMILES"),
	INCHI("INCHI"),
	INCHI_KEY("INCHIAUX"),
	SEQ("SEQ"),
	MSLEVEL("MSLEVEL"),
	;

	private final String field;

	MGFFields(String field) {
		this.field = field;
	}

	public String getName() {
		return field;
	}

	@Override
	public String toString() {
		return field;
	}
	
	public static MGFFields getOptionByName(String name) {
		
		for(MGFFields type : MGFFields.values()) {
			
			if(type.name().equals(name))
				return type;
		}	
		return null;
	}
	
	public static MGFFields getOptionByUIName(String sname) {
		
		for(MGFFields v : MGFFields.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
