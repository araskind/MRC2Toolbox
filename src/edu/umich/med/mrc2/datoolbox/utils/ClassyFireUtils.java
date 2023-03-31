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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireObject;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class ClassyFireUtils {
	
	public static ClassyFireObject parseJsonToClassyFireObject(JSONObject json){
		
		ClassyFireObject cso = new ClassyFireObject();
		
		
		return cso;
	}
	
	public static void uploadClassyFireTaxNodesFromJson(File jsonDir) throws Exception {

		//	Get classification by InChi key
		//	http://classyfire.wishartlab.com/entities/PTPBWFSLLGAWPP-DHSNEXAOSA-N.json
		
		File[] files = jsonDir.listFiles((dir, name) -> name.endsWith(".json"));

		Connection conn = ConnectionManager.getConnection();
		String query = "INSERT INTO CLASSYFIRE_TAX_NODES ("
				+ "CHEMONT_ID, NAME, DESCRIPTION, URL, PARENT, NR_OF_ENTITIES)  " + "VALUES (?, ?, ?, ?, ?, ?) ";
		PreparedStatement ps = conn.prepareStatement(query);

		String synonymQuery = "INSERT INTO CLASSYFIRE_TAX_NODE_SYNONYMS ("
				+ "CHEMONT_ID, NAME, SOURCE, SOURCE_ID, MAPPING_SCOPE)  " + "VALUES (?, ?, ?, ?, ?) ";
		PreparedStatement sps = conn.prepareStatement(synonymQuery);

		for (File jsonFile : files) {

			JSONObject json = JSONUtils.readJsonFromFile(jsonFile);
			if(json == null)
				continue;
			
			String chemOntId = null;
			try {
				chemOntId = json.getString("chemont_id");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (chemOntId == null) {
				System.out.println("Missed ID " + jsonFile.getName());
				continue;
			}
			ps.setString(1, chemOntId);
			ps.setString(2, json.getString("name"));
			ps.setString(3, json.getString("description"));
			ps.setString(4, json.getString("url"));
			ps.setString(5, json.getString("parent"));
			ps.setInt(6, json.getInt("nr_of_entities"));
			ps.executeUpdate();

			// Insert synonyms
			JSONArray synonyms = json.getJSONArray("synonyms");
			if (synonyms == null || synonyms.length() == 0)
				continue;

			sps.setString(1, chemOntId);
			for (int j = 0; j < synonyms.length(); j++) {
				JSONObject synonym = synonyms.getJSONObject(j);
				sps.setString(2, synonym.getString("name"));
				sps.setString(3, synonym.getString("source"));
				sps.setString(4, synonym.getString("source_id"));
				sps.setString(5, synonym.getString("mapping_scope"));
				sps.addBatch();
			}
			sps.executeBatch();
		}
		ps.close();
		sps.close();
		ConnectionManager.releaseConnection(conn);
	}
}
