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

public enum MoTrPACRawDataManifestFields {

	MOTRPAC_SAMPLE_ID("sample_id"),
	MOTRPAC_SAMPLE_TYPE("sample_type"),
	MOTRPAC_SAMPLE_ORDER("sample_order"),
	MOTRPAC_MS_MODE("ms_mode"),
	MOTRPAC_RAW_FILE("raw_file"),
	;
		
	private final String uiName;

	MoTrPACRawDataManifestFields(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static MoTrPACRawDataManifestFields getOptionByName(String optionName) {

		for(MoTrPACRawDataManifestFields o : MoTrPACRawDataManifestFields.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static MoTrPACRawDataManifestFields getOptionByUIName(String name) {
		
		for(MoTrPACRawDataManifestFields type : MoTrPACRawDataManifestFields.values()) {
			
			if(type.getName().equals(name))
				return type;
		}		
		return null;
	}
}
