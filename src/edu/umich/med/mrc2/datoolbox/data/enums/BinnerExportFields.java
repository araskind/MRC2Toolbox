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

public enum BinnerExportFields {

	FEATURE_NAME("Feature name"),
	COMPOUND_NAME("Compound name"),
	METABOLITE_NAME("Metabolite_name"),
	BINNER_NAME("Binner name"),
	NEUTRAL_MASS("Neutral mass"),
	BINNER_MASS("Binner mass"),
	RT_EXPECTED("RT expected"),
	RT_OBSERVED("RT observed"),
	MZ("Monoisotopic M/Z"),
	CHARGE("Charge");

	private final String name;

	BinnerExportFields(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
