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

import org.json.JSONObject;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class CASUtils {

	public static final String casApiUrl = "https://commonchemistry.cas.org/api/detail?cas_rn=";
	
	public static CompoundIdentity getCompoundByCASnumber(String casNumber) {
		
		CompoundIdentity cid = null;
		JSONObject casJson = null;		
		try {
			casJson = JSONUtils.readJsonFromUrl(casApiUrl + casNumber);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(casJson != null) {
			
			String name = null;
			try {
				name = casJson.getString("name");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(name == null)
				return null;
			
			String inchi = null;
			try {
				inchi = casJson.getString("inchi");
			} catch (Exception e) {
				e.printStackTrace();
			}
			String inchiKey = null;
			try {
				inchiKey = casJson.getString("inchiKey");
			} catch (Exception e) {
				e.printStackTrace();
			}
			String smile = null;
			try {
				smile = casJson.getString("smile");
			} catch (Exception e) {
				e.printStackTrace();
			}
//			String canonicalSmile = null;
//			try {
//				canonicalSmile = casJson.getString("canonicalSmile");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			cid = new CompoundIdentity(CompoundDatabaseEnum.CAS, casNumber);
			cid.setCommonName(name);
			cid.setInChi(inchi);
			cid.setInChiKey(inchiKey);
			cid.setSmiles(smile);
		}
		return cid;
	}
}
