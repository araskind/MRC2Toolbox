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

package edu.umich.med.mrc2.datoolbox.database.load.mona;

public enum MonaNameFields {

	ID("ID"),
	NAME("NAME"),
	FORMULA("FORMULA"),
	INCHIKEY("INCHIKEY"),
	SMILES("SMILES"),
	EXACT_MASS("EXACT MASS"),
	PRECURSOR_MZ("PRECURSOR M/Z"),
	NUM_PEAKS("NUM PEAKS"),
	PRECURSOR_TYPE("PRECURSOR TYPE"),
	COLLISION_ENERGY("COLLISION ENERGY"),
	CONTRIBUTOR("CONTRIBUTOR"),
	INSTRUMENT("INSTRUMENT"),
	INSTRUMENT_TYPE("INSTRUMENT TYPE"),
	ION_MODE("ION MODE"),
	SPECTRUM_TYPE("SPECTRUM TYPE"),
	SYNONYMS("SYNONYMS"),
	SPLASH("SPLASH"),
	;

	private final String name;

	MonaNameFields(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
