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

public enum QCANVASInputField {
	
	MATCH_GROUP("Match Group"),
	NUM_BATCHES_COVERED("# Batches Covered"),
	NUM_FEATURES("# Features"),
	FEATURE("Feature"),
	AVERAGE_MONOISOTOPIC_MZ("Average Monoisotopic M/Z"),
	AVERAGE_RT("Average RT"),
	AVERAGE_OLD_RT("Average Old RT"),
	MEDIAN_MEDIAN_INTENSITY("Median Median Intensity"),
	;
	
	private final String uiName;

	QCANVASInputField(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
		
	public static QCANVASInputField getOptionByName(String name) {

		for(QCANVASInputField source : QCANVASInputField.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static QCANVASInputField getOptionByUIName(String uiname) {

		for(QCANVASInputField source : QCANVASInputField.values()) {

			if(source.getName().equals(uiname))
				return source;
		}
		return null;
	}
}
