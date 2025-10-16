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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum BinnerField {

		INDEX("Index", true),
		FEATURE("Feature", true),
		MZ("m/z", true),
		RT("RT", true),
		INTENSITY("Median Intensity", true),
		MASS_ERROR("Mass Error", true),
		RMD("RMD", false),
		ANNOTATION("Annotations", true),
		OTHER_ANNOTATION_IN_GROUP("Other Annotations In Group", false),
		DERIVATIONS("Derivations", true),
		FEATURE_GROUP_NUMBER("Feature Group Number", true),
		CHARGE_CARRIER("Charge Carrier", true),
		ADDITIONAL_ADDUCTS("Adduct/NL", true),
		ISOTOPES("Isotopes", true),
		OTHER_ISOTOPES_IN_GROUP("Other Isotopes In Group", false),
		DERIVED_MW("Derived Molecular Mass", true),
		BIN("Bin", true),
		CLUSTER("Corr Cluster", true),
		REBIN_SUBCLUSTER("Rebin Subcluster", true),
		RT_SUBCLUSTER("RT Subcluster", true),
	;

	private final String uiName;
	private final boolean obligatory;

	BinnerField(String uiName, boolean obligatory) {
		this.uiName = uiName;
		this.obligatory = obligatory;
	}

	public String getName() {
		return uiName;
	}
	
	public boolean isObligatory() {
		return obligatory;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static BinnerField getOptionByName(String name) {

		for(BinnerField source : BinnerField.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static BinnerField getOptionByUIName(String uiname) {

		for(BinnerField source : BinnerField.values()) {

			if(source.getName().equals(uiname))
				return source;
		}
		return null;
	}
}
