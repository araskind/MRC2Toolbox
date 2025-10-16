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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist;

public enum NISTPepSearchOutputFields {

	NUM("Num"),
	UNKNOWN("Unknown"),
	UNKNOWN_ID("uID"),
	UNKNOWN_CAS("uCAS"),
	UNKNOWN_NIST_RN("uNIST r.n."),
	UNKNOWN_FORMULA("uFormula"),
	UNKNOWN_INCHIKEY("uInChIKey"),
	UNKNOWN_NUMPEAKS("uNumPeaks"),
	UNKNOWN_COMMENT("uComment"),
	UNKNOWN_PRECURSOR_MZ("Precursor m/z"),
	UNKNOWN_MW("uMW"),
	UNKNOWN_CE("uCE"),
	RANK("Rank"),
	LIBRARY("Library"),
	ID("Id"),
	MASS("Mass"),
	DELTA_MZ("Delta(m/z)"),
	DELTA_MW("DeltaMW"),
	HYBRID_DELTA_MZ("DeltaMass"),
	LIB_PRECURSOR_MZ("Lib Precursor m/z"),
	LIB_MW("Lib MW"),
	MAXSCORE("MaxScore"),
	SCORE("Score"),
	HYBRID_SCORE("o.Score"),
	DOT_PRODUCT("Dot Product"),
	REVERSE_DOT_PRODUCT("Rev-Dot"),
	HYBRID_DOT_PRODUCT("o.DotProd"),
	PROB("Prob(%)"),
	NUM_MP("NumMP"),
	REF_PEAK("RefPeak"),
	NUM_PEAKS("Num.Peaks"),
	PEPTIDE("Peptide"),
	CHARGE("Charge"),
	FORMULA("Formula"),
	PREC_TYPE("Prec.Type"),
	CE("CE"),
	INSTR_TYPE("Instr.Type"),
	FLANK_RES("FlankRes"),
	MODS("Mods"),
	PEP("Pep"),
	CAS("CAS"),
	NIST_RN("NIST r.n."),
	INCHI_KEY("InChIKey"),
	EVAL_LIB("evalLib"),
	EVAL_ID("evalID"),
	EVAL_STAT("evalStat"),
	N_REPS("Nreps"),
	TF_RATIO("Tfratio"),
	PROTEIN("Protein"),
	NUM_COMP("Num.Comp"),
	;

	private final String name;

	NISTPepSearchOutputFields(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public static NISTPepSearchOutputFields getFieldByColumnName(String columnName) {

		for(NISTPepSearchOutputFields f : NISTPepSearchOutputFields.values()) {

			if(f.getName().equals(columnName))
				return f;
		}
		return null;
	}
}














