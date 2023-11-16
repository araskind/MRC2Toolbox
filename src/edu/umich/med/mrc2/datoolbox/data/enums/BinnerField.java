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

public enum BinnerField {

		INDEX("Index"),
		FEATURE("Feature"),
		MZ("m/z"),
		RT("RT"),
		INTENSITY("Median Intensity"),
		MASS_ERROR("Mass Error"),
		RMD("RMD"),
		ANNOTATION("Annotations"),
		OTHER_ANNOTATION_IN_GROUP("Other Annotations In Group"),
		DERIVATIONS("Derivations"),
		FEATURE_GROUP_NUMBER("Feature Group Number"),
		CHARGE_CARRIER("Charge Carrier"),
		ADDITIONAL_ADDUCTS("Adduct/NL"),
		ISOTOPES("Isotopes"),
		OTHER_ISOTOPES_IN_GROUP("Other Isotopes In Group"),
		DERIVED_MW("Derived Molecular Mass"),
		BIN("Bin"),
		CLUSTER("Corr Cluster"),
		REBIN_SUBCLUSTER("Rebin Subcluster"),
		RT_SUBCLUSTER("RT Subcluster"),
//		KMD("KMD"),
//		METABOLITE_NAME("Metabolite_name"),
//		BINNER_NAME("Binner name"),
//		NEUTRAL_MASS("Neutral mass"),
//		RT_EXPECTED("RT expected"),
//		MONOISOTOPIC_MZ("Monoisotopic M/Z"),
//		CHARGE("Charge"),
	;

	private final String uiName;

	BinnerField(String uiName) {
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
