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

public enum BinnerPostProcessorField {

	ADDUCT_NL("Adduct/NL"),
		ANNOTATIONS("Annotations"),
		BIN("Bin"),
		BROAD_NAME("Broad_name"),
		CAS("CAS"),
		CHARGE_CARRIER("Charge Carrier"),
		CHEBI("ChEBI"),
		COMPOUND_MASS("Compound Mass"),
		COMPOUND_NAME("Compound Name"),
	COMPOUND_RT("Compound RT"),
		CORR_CLUSTER("Corr Cluster"),
		DELTA_MASS("Delta Mass"),
		DELTA_RT("Delta RT"),
		DERIVATIONS("Derivations"),
	DERIVED_MOLECULAR_MASS("Derived Molecular Mass"),
		FEATURE("Feature"),
		FEATURE_GROUP_NUMBER("Feature Group Number"),
		FORMULA("Formula"),
		FURTHER_ANNOTATION("Further Annotation"),
		HMDB_ID("HMDB_id"),
		HMDB_NAME("HMDB_name"),
		INCHI_KEY("Inchi_key"),
	INDEX("Index"),
		ISOTOPES("Isotopes"),
		KEGGS("KEGGs"),
		KMD("KMD"),
		LIPIDMAPS_ID("LipidMaps_id"),
		MAIN_CLASS("main_class"),
		MASS("Mass"),
		MASS_ERROR("Mass Error"),
	MATCH_REPLICATE("Match Replicate"),
	MEDIAN_INTENSITY("Median Intensity"),
		MOTRPAC_COMPOUND_NAME_OLD("MoTrPac_compound_name_OLD"),
		MOTRPAC_ID("MoTrPac_id"),
		MSI_ID_LEVEL("MSI ID Level"),
		NOTE("note"),
		OTHER_ANNOTATIONS_IN_GROUP("Other Annotations in Group"),
		OTHER_ISOPTOES_IN_GROUP("Other Isoptoes In Group"),
		OTHER_NAME1("Other_name1"),
		OTHER_NAME2("Other_name2"),
		OTHER_NAME3("Other_name3"),
		OTHER_NAME4("Other_name4"),
		PUBMED_ID("Pubmed_ID"),
		REBIN_SUBCLUSTER("Rebin Subcluster"),
		REFMET_ID("REFMET_id"),
		RT("RT"),
		RT_SUBCLUSTER("RT Subcluster"),
		SUB_CLASS("sub_class"),
		SUPER_CLASS("super_class"),
		TEMPORARY_NAME_FROM_PILOT("TEMPORARY: Name from pilot"),
		VERSION_INFO("Version Info"),
	;

	private final String uiName;

	BinnerPostProcessorField(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
}
