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

package edu.umich.med.mrc2.datoolbox.database.load.mine;

public enum MINEMSPFields {

	SMILES("SMILES"),
	INCHI_KEY("Inchikey"),
	GENERATION("Generation"),
	MINE_ID("MINE_id"),
	SOURCES("Sources"),
	NAME("Name"),
	MASS("Mass"),
	FORMULA("Formula"),
	ID("_id"),
	INSTRUMENT("Instrument"),
	IONIZATION_MODE("Ionization Mode"),
	ENERGY("Energy"),
	NUM_PEAKS("Num Peaks"),
	;

	private final String field;

	MINEMSPFields(String field) {
		this.field = field;
	}

	public String getName() {
		return field;
	}

	@Override
	public String toString() {
		return field;
	}
}
