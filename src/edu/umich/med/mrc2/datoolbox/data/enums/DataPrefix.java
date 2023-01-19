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

public enum DataPrefix {

	ADDUCT("ADDUCT"),
	ADDUCT_EXCHANGE("ADEX"),
	ANNOTATION("ANNOT_"),
	ASSAY_METHOD("A"),
	BATCH("Batch-"),
	BIO_LOCATION("BIOLOC"),
	BINNER_ANNOTATION("BIAN"),
	BINNER_FIELD("BINF"),
	BINNER_MASS_DIFFERENCE("BINMD"),
	BINNER_PAGE("BPAGE"),
	BIOCHEMICAL_PATHWAY("BCPATH"),
	COMPOSITE_ADDUCT("CMPADCT"),
	COMPOUND_ID_CLUSTER("CIDCL"),
	COMPOUND_ID_UNIT("CIDU"),
	COMPOUND_PROPERTY("CPROP"),
	CONCENTRATION("CONC"),
	CROMATOGRAPHIC_COLUMN("CCOL"),
	CROMATOGRAPHIC_GRADIENT("GRAD"),
	DATA_ACQUISITION_METHOD("DQM"),
	DATA_ANALYSIS("DA"),
	DATA_EXTRACTION_METHOD("DXM"),
	DATA_MATRIX("DMAT_"),
	DESEASE("DES"),
	DOCUMENT("DOC"),
	DRUG_TARGET("DRTGT_"),
	EXPERIMENTAL_FACTOR("EF_"),
	EXPERIMENTAL_FACTOR_LEVEL("EL_"),
	FEATURE_MATRIX("FMAT_"),
	HMDB_CITATION("REF_"),
	HMDB_CONCENTRATION("CONC_"),
	HMDB_DESEASE("DES_"),
	ID_EXPERIMENT("IDX"),
	ID_PROJECT("IDP"),
	ID_SAMPLE("IDS"),
	ID_TRACKER_SEARCH_QUERY("TSQ"),
	IDENTIFICATION_FOLLOWUP_STEP("FUS"),
	IDENTIFICATION_LEVEL("IDS"),	
	INJECTION("INJ"),
	INSTRUMENT("IN"),
	INSTRUMENT_MAINT_LOG("IMN"),
	LIMS_ORGANIZATION("OR"),
	LIMS_USER("U"),
	LIPID_BLAST("LB"),
	LIPID_BLAST_RIKEN("LBR"),
	LITERATURE_REFERENCE("LREF"),
	LOOKUP_FEATURE("LUF"),
	LOOKUP_FEATURE_DATA_SET("LUDS"),
	MANUFACTURER("MNF"),
	MOBILE_PHASE("MOPH"),
	MOTRPAC_STUDY("MPST"),
	MOTRPAC_REPORT("MPR"),
	MRC2_COMPOUND("MRC2CPD"),	//	MRC2_COMPOUND_SEQ 
	MSDIAL_METABOLITE("MSDM"),
	MSMS_DECOY_GENERATION_METHOD("DGM"),
	MSMS_EXTRACTION_PARAMETER_SET("MS2EPS"),
	MSMS_LIBRARY("MSMSLIB"),
	MSMS_LIBRARY_ENTRY("MSL"),
	MSMS_LIBRARY_MATCH("LM"),
	MSRT_LIBRARY_MATCH("MR"),
	MS_MSMS_MANUAL_MATCH("MM"),
	MSMS_SPECTRUM("MSN_"),
	MSMS_FEATURE_COLLECTION("MSFC"),
	MSMS_CLUSTER("M2C"),
	MSMS_CLUSTER_DATA_SET("M2CDS"),
	MSMS_CLUSTERING_PARAM_SET("M2CP"),
	MS_FEATURE("MSF_"),
	MS_FEATURE_CLUSTER("MFC_"),
	MS_FEATURE_IDENTITY("FID_"),
	MS_FEATURE_POOLED("MSFP_"),
	MS_LIBRARY("CPDLIB_"),
	MS_LIBRARY_TARGET("TGT_"),
	MS_LIBRARY_UNKNOWN_TARGET("UNK_"),
	MS_PATTERN("MSP_"),
	NEUTRAL_LOSS("NULOSS"),
	NIST_PEPSEARCH_PARAM_SET("NPSP"),
	OBJECT_ANNOTATION("OAN"),
	PREPARED_SAMPLE("PI"),
	REFMET("REFMET"),
	REF_MS_DATA_BUNDLE("RMSD"),
	REPEAT("REPEAT"),
	SAMPLE_PREPARATION("SPR"),
	SOFTWARE("SW"),
	SOLVENT("SOLV"),
	SOP_PROTOCOL("SOP"),
	SOP_PROTOCOL_GROUP("SPG"),
	STANDARD_FEATURE_ANNOTATION("STAN"),
	STOCK_SAMPLE("SSID"),
	VERSION("V"),
	;

	private final String name;

	DataPrefix(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}
}












