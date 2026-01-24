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

public enum SummaryIdentificationProperties {
	
	NUMBER_OF_HITS("Number of hits"),
	MEDIAN_TOP_ENTROPY_SCORE("Median top entropy score"),
	RT_RANGE("RT range"),
	FRAGMENTATION_ENERGIES_NUMBER("Number of fragmentation energies"),
	FRAGMENTATION_ENERGIES("List of fragmentation energies"),
	PARENT_IONS_NUMBER("Number of parent ions"),
	PARENT_IONS("List of parent ions"),
	LIB_ADDUCTS_NUMBER("Number of adducts (from library)"),
	LIB_ADDUCTS("List of adducts"),
	MATCH_TYPES("MSMS match types"),
	;
	
	private final String uiName;

	SummaryIdentificationProperties(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static SummaryIdentificationProperties getOptionByName(String optionName) {

		for(SummaryIdentificationProperties o : SummaryIdentificationProperties.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static SummaryIdentificationProperties getOptionByUIName(String sname) {
		
		for(SummaryIdentificationProperties v : SummaryIdentificationProperties.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
