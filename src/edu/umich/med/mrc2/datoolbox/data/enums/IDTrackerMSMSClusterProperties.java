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

public enum IDTrackerMSMSClusterProperties {

	CLUSTER_ID("Cluster ID"),
	LOOKUP_FEATURE_NAME("Lookup feature"),
	MEDIAN_MZ("Median M/Z"),
	LOOKUP_FEATURE_MZ("Lookup M/Z"),	
	MZ_ERROR_PPM("M/Z error, ppm"),
	MEDIAN_RETENTION_TIME("Median RT"),
	LOOKUP_FEATURE_RT("Lookup RT"),
	RT_ERROR("RT error, min"),
	RANK("Rank"),
	PRIMARY_ID_RAW_DATA_FILE("Raw data file for prim. ID"),
	;

	private final String uiName;

	IDTrackerMSMSClusterProperties(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	public String toString() {
		return uiName;
	}
	
	public static IDTrackerMSMSClusterProperties getOptionByName(String optionName) {

		for(IDTrackerMSMSClusterProperties o : IDTrackerMSMSClusterProperties.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static IDTrackerMSMSClusterProperties getOptionByUIName(String sname) {
		
		for(IDTrackerMSMSClusterProperties v : IDTrackerMSMSClusterProperties.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
