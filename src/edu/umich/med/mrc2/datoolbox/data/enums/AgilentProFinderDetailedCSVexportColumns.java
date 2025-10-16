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

public enum AgilentProFinderDetailedCSVexportColumns {

	NAME("Name"),
	FORMULA("Formula"),
	MASS_AVG("Mass (avg)"),
	RSD_MASS_PPM("RSD (Mass, ppm)"),
	SATURATED("Saturated"),
	RT_AVG("RT (avg)"),
	RT_SPAN("RT (span)"),
	HEIGHT_AVG("Height (avg)"),
	FOUND("Found"),
	MISSED("Missed"),
	SCORE_TGT_MAX("Score (Tgt, max)"),
	PERCENT_RSD_TGT("%RSD (Tgt)"),
	IONS_MIN("Ions (min)"),
	IONS_MAX("Ions (max)"),
	MASS_PREFIX("[Mass]"),
	RT_PREFIX("[RT]"),
	AREA_PREFIX("[Area]"),
	SCORE_MFE_PREFIX("[Score (MFE)]"),
	SCORE_TGT_PREFIX("[Score (Tgt)]"),
	IONS_PREFIX("[Ions]"),
	;

	private final String uiName;

	AgilentProFinderDetailedCSVexportColumns(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}	
	
	public static AgilentProFinderDetailedCSVexportColumns getOptionByName(String name) {

		for(AgilentProFinderDetailedCSVexportColumns field : AgilentProFinderDetailedCSVexportColumns.values()) {

			if(field.name().equals(name))
				return field;
		}
		return null;
	}
	
	public static AgilentProFinderDetailedCSVexportColumns getOptionByUIName(String dbName) {

		for(AgilentProFinderDetailedCSVexportColumns field : AgilentProFinderDetailedCSVexportColumns.values()) {

			if(field.getName().equals(dbName))
				return field;
		}
		return null;
	}
	
	public static AgilentProFinderDetailedCSVexportColumns[]getQuantitativeFiedldPrefixes(){
		return new AgilentProFinderDetailedCSVexportColumns[] {
				
				AgilentProFinderDetailedCSVexportColumns.MASS_PREFIX,
				AgilentProFinderDetailedCSVexportColumns.RT_PREFIX,
				AgilentProFinderDetailedCSVexportColumns.AREA_PREFIX,
				AgilentProFinderDetailedCSVexportColumns.SCORE_MFE_PREFIX,
				AgilentProFinderDetailedCSVexportColumns.SCORE_TGT_PREFIX,
				AgilentProFinderDetailedCSVexportColumns.IONS_PREFIX,
		};
	}
}
