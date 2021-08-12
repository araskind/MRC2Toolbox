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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist;

public enum NISTPepSearchOutputColumnCode {
	
	BEST_HITS_ONLY_COLUMN("Output best hits only", "bh", false),
	PRECURSOR_MZ_COLUMN("Precursor m/z (hit and search)", "pz", true),
	HIT_SEARCH_MZ_DIFF_COLUMN("Hit-Search spectrum precursor m/z difference", "dz", true),
	PRECURSOR_TYPE_COLUMN("Precursor type (e.g. [M+H]+)", "tz", false),
	NOMINAL_MW_COLUMN("Nominal MW", "mw", false),
	MOL_FORMULA_COLUMN("Mol. formula", "cf", true),
	HIT_SEARCH_NOMINAL_MW_DIFF_COLUMN("Hit-Search spectrum nominal MW difference", "dw", false),
	SEARCH_SPECTRUM_NUMBER_COLUMN("Search spectrum number", "sn", true),
	CAS_MATCH_COLUMN("CAS registry number", "cn", true),
	NIST_REG_COLUMN("NIST registry number", "nn", false),
	NUM_COMPARED_SPECTRA_COLUMN("Number of compared spectra", "nc", false),
	INSTRUMENT_TYPE_COLUMN("Instrument Type", "it", false),
	COLLISION_ENERGY_COLUMN("Collision energy", "ce", false),
	NUM_MATCHED_PEAKS_COLUMN("Number of matched peaks", "nm", false),
	ADD_MAX_SCORE_COLUMN("Add hit list max score to each hit line", "xs", false),
	INCHI_KEY_COLUMN("InChIKey", "ik", false),
	TFQRY_COLUMN("T/F-qry (peptide search P in NIST Peptide library)", "tq", false),
	EVAL_LIB_AND_ID_COLUMN("Eval Library and ID", "el", false),
	SEARCH_EVAL_LIB_AND_ID_COLUMN("Search Eval Library and ID", "eq", false),
	NUM_PEAKS_COLUMN("Number of peaks", "np", false),
	SEARCH_CAS_COLUMN("Search spectrum CAS", "qc", false),
	SEARCH_NIST_REG_COLUMN("Search spectrum NIST registry number", "qn", false),
	SEARCH_ID_COLUMN("Search spectrum ID", "qi", false),
	SEARCH_MOL_FORMULA_COLUMN("Search spectrum mol. formula", "qf", false),
	SEARCH_NOMINAL_MW_COLUMN("Search spectrum nominal MW", "qw", false),
	SEARCH_COLLISION_ENERGY_COLUMN("Search spectrum collisional energy", "qe", false),
	SEARCH_COMMENTS_COLUMN("Comment from the search spectrum", "qo", false),
	REF_PEAK_FRACTION_COLUMN("Reference Peak Fraction", "rf", false),
	NUM_PEAKS_IN_SEARCH_SPECTRUM_COLUMN("Number of peaks in the search spectrum", "qp", false),
	PEP_DIFF_LOCALIZATION_COLUMN("Output peptide difference localization", "pl", false),
	;

	private final String title;
	private final String code;
	private final boolean isDefault;

	NISTPepSearchOutputColumnCode(String title, String code, boolean isDefault) {
		this.title = title;
		this.code = code;
		this.isDefault = isDefault;
	}
	
	public String getTitle() {
		return title;
	}

	public String getCode() {
		return code;
	}
	
	public boolean includeByDefault() {
		return isDefault;
	}
}
