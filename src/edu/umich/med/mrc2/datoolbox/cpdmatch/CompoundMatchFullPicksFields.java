/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.cpdmatch;

public enum CompoundMatchFullPicksFields {

	PICK("Pick"),
	BATCH("Batch"),
	MATCH_GROUP("Match Group"),
	FEATURE_NAME("Feature Name"),
	MONOISOTOPIC_MZ("Monoisotopic M/Z"),
	UNNAMED_RT("Unnamed RT"),
	MEDIAN_INTENSITY("Median Intensity"),
	BINNER_ANNOTATION("Binner Annotation"),
	ISOTOPE("Isotope"),
	PREV_ROW_DIFF_MZ("Prev Row Diff - M/Z"),
	PREV_ROW_DIFF_UNNAMED_RT("Prev Row Diff - Unnamed RT"),
	PROJECTED_MATCH_RT("Projected Match RT"),
	PREV_ROW_DIFF_MATCH_RT("Prev Row Diff - Match RT"),
	RT_MATCH_SHIFT("RT Match Shift"),
	MAPPED_COMPOUND("Mapped Compound"),
	NAMED_MASS("Named Mass"),
	NAMED_RT("Named RT"),
	DELTA_MASS_NAMED_TO_UNNAMED("Delta Mass Named to Unnamed"),
	DELTA_RT_NAMED_TO_UNNAMED("Delta RT Named to Unnamed"),
	NAMED_UNNAMED_CORRELATION("Named-Unnamed Correlation"),
	;
	
	private final String uiName;

	CompoundMatchFullPicksFields(String pcdlName) {
		this.uiName = pcdlName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static CompoundMatchFullPicksFields getOptionByName(String optionName) {

		for(CompoundMatchFullPicksFields o : CompoundMatchFullPicksFields.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static CompoundMatchFullPicksFields getOptionByUIName(String uiName) {
		
		for(CompoundMatchFullPicksFields f : CompoundMatchFullPicksFields.values()) {
			
			if(f.getName().equals(uiName))
				return f;
		}		
		return null;
	}	
}













