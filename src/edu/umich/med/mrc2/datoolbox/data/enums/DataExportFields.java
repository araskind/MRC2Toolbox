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

public enum DataExportFields {

	SAMPLE_NAME("SAMPLE"),
	SAMPLE_TYPE("TYPE"),
	START_TIME("TIME"),
	DATA_FILE("DataFile"),
	SAMPLE_ID("SampleID"),
	SAMPLE_EXPORT_NAME("Sample name"),
	SAMPLE_EXPORT_ID("Sample ID"),
	DATA_FILE_EXPORT("Data file"),
	FEATURE_EXPORT_NAME("Feature name"),
	INJECTION_TIME("Injection time"),
	MRC2_SAMPLE_ID("MRC2 sample ID"),
	CLIENT_SAMPLE_ID("Client sample ID"),
	;

	private final String uiName;

	DataExportFields(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static DataExportFields getOptionByName(String name) {
		
		for(DataExportFields field : DataExportFields.values()) {
			
			if(field.name().equals(name))
				return field;
		}	
		return null;
	}
	
	
	public static DataExportFields getOptionByUIName(String name) {
		
		for(DataExportFields s : DataExportFields.values()) {
			
			if(s.getName().equals(name))
				return s;
		}		
		return null;
	}
}
