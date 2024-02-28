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

public enum MSPField {

	CAS("CAS#", false),
	COLLISION_ENERGY("Collision_energy", false),	//	String
	COLLISION_GAS("Collision_gas", false),
	COMMENTS("Comments", false),
	DB_NUM("DB#", false),
	EXACT_MASS("ExactMass", false),
	FORMULA("Formula", false),
	INCHI_KEY("InChIKey", false),
	INSTRUMENT("Instrument", false),	//	Model
	INSTRUMENT_TYPE("Instrument_type", false),	//	e.g. IT/ion trap
	IN_SOURCE_VOLTAGE("In-source_voltage", false),
	IONIZATION("Ionization", false),	//	e.g.ESI
	ION_MODE("Ion_mode", false),	//	P or N
	MSN_PATHWAY("msN_pathway", false),
	MW("MW", false),
	NAME("Name", true),
	NIST_NUM("NIST#", false),
	NOTES("Notes", false),
	NUM_PEAKS("Num Peaks", true),
	PEPTIDE_MODS("Peptide_mods", false),
	PEPTIDE_SEQUENCE("Peptide_sequence", false),
	PRECURSORMZ("PrecursorMZ", false),
	PRECURSOR_TYPE("Precursor_type", false), //	e.g. [M+H]+
	PRESSURE("Pressure", false),
	RELATED_CAS("Related_CAS#", false),
	RETENTION_INDEX("Retention_index", false),
	SAMPLE_INLET("Sample_inlet", false),	//	e.g. HPLC
	SPECIAL_FRAGMENTATION("Special_fragmentation", false),	// e.g. wideband
	SPECTRUM_TYPE("Spectrum_type", false),	//	MS2, MS3, MS4 etc
	SYNONYM("Synon", false),
	;

	private final String uiName;
	private final boolean isObligatory;

	MSPField(String uiName, boolean obligatory) {

		this.uiName = uiName;
		this.isObligatory = obligatory;
	}

	public boolean isRequired() {
		return isObligatory;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static MSPField getOptionByName(String optionName) {

		for(MSPField o : MSPField.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static MSPField getOptionByUIName(String uiName) {
		
		for(MSPField f : MSPField.values()) {
			
			if(f.getName().equals(uiName))
				return f;
		}		
		return null;
	}
}
