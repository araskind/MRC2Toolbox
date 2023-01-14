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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

public enum NISTmspField {

	CAS("CAS#"),
	COLLISION_ENERGY("Collision_energy"),
	COLLISION_GAS("Collision_gas"),
	COMMENTS("Comments"),
	DB_NUM("DB#"),
	EXACT_MASS("ExactMass"),
	FORMULA("Formula"),
	INCHI_KEY("InChIKey"),
	INSTRUMENT("Instrument"),
	INSTRUMENT_TYPE("Instrument_type"),
	IN_SOURCE_VOLTAGE("In-source_voltage"),
	IONIZATION("Ionization"),
	ION_MODE("Ion_mode"),
	MSN_PATHWAY("msN_pathway"),
	MW("MW"),
	NAME("Name"),
	NIST_NUMBER("NIST#"),
	NOTES("Notes"),
	NUM_PEAKS("Num Peaks"),
	PEPTIDE_SEQUENCE("Peptide_sequence"),
	PEPTIDE_MODS("Peptide_mods"),
	PRECURSORMZ("PrecursorMZ"),
	PRECURSOR_TYPE("Precursor_type"),
	PRESSURE("Pressure"),
	RELATED_CAS("Related_CAS#"),
	SAMPLE_INLET("Sample_inlet"),
	SPECIAL_FRAGMENTATION("Special_fragmentation"),
	SPECTRUM_TYPE("Spectrum_type"),
	SYNONYM("Synon"),
	;

	private final String name;

	NISTmspField(String field) {
		this.name = field;
	}

	public String getName() {
		return name;
	}
}
