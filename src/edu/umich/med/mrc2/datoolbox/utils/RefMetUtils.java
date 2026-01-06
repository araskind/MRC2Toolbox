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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

import edu.umich.med.mrc2.datoolbox.dbparse.load.refmet.RefMetFields;

public class RefMetUtils {

	public static final String refMetMatchUrl = 
			"https://www.metabolomicsworkbench.org/rest/refmet/match/";
	public static final String encoding = StandardCharsets.UTF_8.toString();
	
	public static Map<RefMetFields,String>getRefMetRecordByMatchingName(String nameToMatch) throws UnsupportedEncodingException {
				
		Map<RefMetFields,String>record = new TreeMap<>();
		JSONObject jso = JSONUtils.readJsonFromUrl(
				refMetMatchUrl + URLEncoder.encode(nameToMatch.replace("/", " "), encoding).replace("+", "%20"));

		if(jso != null) {
			
			for(RefMetFields field : RefMetFields.values()) {
				
				if(jso.has(field.getName()) && !jso.get(field.getName()).equals(JSONObject.NULL))
					record.put(field, jso.get(field.getName()).toString());
			}			
		}
		return record;
	}
}
