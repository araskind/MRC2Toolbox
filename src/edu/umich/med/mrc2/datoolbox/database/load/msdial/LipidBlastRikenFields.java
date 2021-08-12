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

package edu.umich.med.mrc2.datoolbox.database.load.msdial;

public enum LipidBlastRikenFields {
	
	NAME("NAME"),
	PRECURSOR_MZ("PRECURSORMZ"),
	PRECURSOR_TYPE("PRECURSORTYPE"),
	SMILES("SMILES"),
	INCHI_KEY("INCHIKEY"),
	FORMULA("FORMULA"),
	ONTOLOGY("Ontology"),
	RETENTION_TIME("RETENTIONTIME"),
	CCS("CCS"),
	IONMODE("IONMODE"),
	COLLISION_ENERGY("COLLISIONENERGY"),
	COMPOUND_CLASS("COMPOUNDCLASS"),
	COMMENT("Comment"),
	NUM_PEAKS("Num Peaks"),
	;
	
	private final String name;

	LipidBlastRikenFields(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
