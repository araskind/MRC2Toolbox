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

public enum MSFeatureSetStatisticalParameters {

	TOTAL_MEDIAN("Median area, all samples"), 
	SAMPLE_MEDIAN("Median area, experimental samples only"),
	POOLED_MEDIAN("Median area, pooled samples only"),
	PERCENT_MISSING_IN_SAMPLES("% missing, experimental samples only"),
	PERCENT_MISSING_IN_POOLS("% missing, pooled samples only"),
	AREA_RSD_SAMPLES("Area %RDS, experimental samples only"),
	AREA_RSD_POOLS("Area %RDS, pooled samples only"),
	RT_RSD("RT, %RSD"),
	MZ_RSD("M/Z, %RSD"),
	;

	private final String uiName;

	MSFeatureSetStatisticalParameters(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static MSFeatureSetStatisticalParameters getOptionByName(String optionName) {

		for(MSFeatureSetStatisticalParameters o : MSFeatureSetStatisticalParameters.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static MSFeatureSetStatisticalParameters getOptionByUIName(String name) {
		
		for(MSFeatureSetStatisticalParameters type : MSFeatureSetStatisticalParameters.values()) {
			
			if(type.getName().equals(name))
				return type;
		}		
		return null;
	}
}
