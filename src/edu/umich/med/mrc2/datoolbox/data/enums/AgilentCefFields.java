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

public enum AgilentCefFields {

	MS1_SPECTRUM("TOF-MS1"),
	MS2_SPECTRUM("TOF-MS2"),
	LIBRARY_MS2_SPECTRUM("Lib-MS2"),
	SURVEY_SCAN("Scan"),
	PRODUCT_ION_SCAN("ProductIon"),
	FBF_SPECTRUM("FbF"),
	MFE_SPECTRUM("MFE"),
	;

	private final String uiName;

	AgilentCefFields(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static AgilentCefFields getOptionByName(String name) {

		for(AgilentCefFields field : AgilentCefFields.values()) {

			if(field.name().equals(name))
				return field;
		}
		return null;
	}
	
	public static AgilentCefFields getOptionByUIName(String dbName) {

		for(AgilentCefFields field : AgilentCefFields.values()) {

			if(field.getName().equals(dbName))
				return field;
		}
		return null;
	}
}
