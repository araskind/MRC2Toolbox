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

package edu.umich.med.mrc2.datoolbox.dbparse.load.massbank;

public enum MassBankDataField {

	ACCESSION("ACCESSION", true, true, true, "Record identifier", false),
	RECORD_TITLE("RECORD_TITLE", true, true, true, "Short title of the record", false),
	DATE("DATE", true, true, true, "Date of creation or last modification of record", false),
	AUTHORS("AUTHORS", true, true, true, "Name and affiliation of authors", false),
	LICENSE("LICENSE", true, true, true, "Creative Commons License or its compatible terms", false),
	COPYRIGHT("COPYRIGHT", false, true, true, "Copyright", false),
	PUBLICATION("PUBLICATION", false, true, true, "Bibliographic information of reference", false),
	PROJECT("PROJECT", false, true, true, "Information on a related project)", false),
	COMMENT("COMMENT", false, false, true, "Comments", false),
	CH_NAME("CH$NAME", true, false, true, "Chemical name", false),
	CH_COMPOUND_CLASS("CH$COMPOUND_CLASS", true, true, true, "Chemical category", false),
	CH_FORMULA("CH$FORMULA", true, true, true, "Chemical formula", false),
	CH_EXACT_MASS("CH$EXACT_MASS", true, true, true, "Exact mass", false),
	CH_SMILES("CH$SMILES", true, true, true, "SMILES code", false),
	CH_IUPAC("CH$IUPAC", true, true, true, "InChI code", false),
	CH_LINK("CH$LINK", false, false, true, "External database name with identifier", true),
	SP_SCIENTIFIC_NAME("SP$SCIENTIFIC_NAME", false, true, true, "Scientific name of biological species", false),
	SP_LINEAGE("SP$LINEAGE", false, true, true, "Lineage of species", false),
	SP_LINK("SP$LINK", false, false, true, "External database name with identifier", true),
	SP_SAMPLE("SP$SAMPLE", false, false, true, "Information of sample preparation", false),
	AC_INSTRUMENT("AC$INSTRUMENT", true, true, true, "Commercial name and manufacturer of instrument", false),
	AC_INSTRUMENT_TYPE("AC$INSTRUMENT_TYPE", true, true, true, "Type of instrument", false),
	AC_MASS_SPECTROMETRY_MS_TYPE("AC$MASS_SPECTROMETRY: MS_TYPE", true, true, true, "MSn type of data", false),
	AC_MASS_SPECTROMETRY_ION_MODE("AC$MASS_SPECTROMETRY: ION_MODE", true, true, true, "Positive or negative mode of ion detection", false),
	AC_MASS_SPECTROMETRY("AC$MASS_SPECTROMETRY", false, true, true, "Analytical conditions of mass spectrometry", true),
	AC_CHROMATOGRAPHY("AC$CHROMATOGRAPHY", false, false, true, "Analytical conditions of chromatographic separation", true),
	AC_GENERAL("AC$GENERAL", false, false, true, "General analytical conditions and information", true),
	MS_FOCUSED_ION("MS$FOCUSED_ION", false, true, true, "Precursor ion and m/z", true),
	MS_DATA_PROCESSING("MS$DATA_PROCESSING", false, true, true, "DATA processing method", true),
	PK_SPLASH("PK$SPLASH", true, true, true, "Hashed identifier of mass spectra", false),
	PK_ANNOTATION("PK$ANNOTATION", false, true, false, "Chemical annotation of peaks by molecular formula", false),
	PK_NUM_PEAK("PK$NUM_PEAK", true, true, true, "Total number of peaks", false),
	PK_PEAK("PK$PEAK", true, true, false, "Peak(m/z, intensity and relative intensity)", false),
	;

	private final String name;
	private final boolean mandatory;
	private final boolean unique;
	private final boolean singleLine;	
	private final String description;
	private final boolean hasSubtag;

	private MassBankDataField(
			String name, 
			boolean mandatory, 
			boolean unique, 
			boolean singleLine, 
			String description, 
			boolean hasSubtag) {
		this.name = name;
		this.mandatory = mandatory;
		this.unique = unique;
		this.singleLine = singleLine;
		this.description = description;
		this.hasSubtag = hasSubtag;
	}

	public String getName() {
		return name;
	}
	
	public boolean isMandatory() {
		return unique;
	}
	
	public boolean isSingleLine() {
		return singleLine;
	}
	
	public boolean mayHaveSubTag() {
		return hasSubtag;
	}
	
	public boolean isUnique() {
		return mandatory;
	}
	public String getDescription() {
		return description;
	}
}
