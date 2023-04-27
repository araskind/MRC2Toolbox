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

package edu.umich.med.mrc2.datoolbox.data.thermo.raw;

import org.json.JSONArray;
import org.json.JSONObject;

public class ThermoUtils {
	
	public static ThermoRawMetadata parseMetadataObjectFromJson(String fileName, JSONObject jso){
				
		ThermoRawMetadata md = new ThermoRawMetadata(fileName);
		
		for(ThermoMetadataSection section : ThermoMetadataSection.values()) {
			
			if(jso.has(section.name())) {
				
				 JSONArray parArray = (JSONArray) jso.get(section.name());
				 
				 for(int i=0; i<parArray.length(); i++) {
					 
					 ThermoRawMetadataEntry de = 
							 parseJsonEntry(parArray.getJSONObject(i));
					 if(de != null)
						 md.getEntries().add(de);
				 }
			}
		}
		return md;
	}
	
	public static ThermoRawMetadataEntry parseJsonEntry(JSONObject entry) {
		
		if(entry.has("accession")) {
			
			ThermoCvParams cvPar = 
					ThermoCvParams.getThermoCvParamByAccession(entry.getString("accession"));
			
			if(cvPar != null && entry.has("value"))				
				return new ThermoRawMetadataEntry(cvPar, entry.getString("value"));			
		}		
		return null;
	}
}
