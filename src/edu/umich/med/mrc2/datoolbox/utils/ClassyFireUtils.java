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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireExternalDescriptor;
import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireObject;
import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireOntologyEntry;
import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireOntologyLevel;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class ClassyFireUtils {
	
	private static final Pattern chebiPattern = 
			Pattern.compile("(\\(CHEBI:(\\d+)\\))$");
	private static final Pattern lmPattern = 
			Pattern.compile("(\\((.+)\\))$");
	
	public static ClassyFireObject parseJsonToClassyFireObject(JSONObject jso){
		
		ClassyFireObject cfObj = new ClassyFireObject();
		Matcher regexMatcher = null;
		
		if(jso.has("inchikey") && !jso.get("inchikey").equals(JSONObject.NULL))
			cfObj.setInchiKey(jso.get("inchikey").toString().replace("InChIKey=",""));
		
		if(jso.has("smiles") && !jso.get("smiles").equals(JSONObject.NULL))
			cfObj.setSmiles(jso.get("smiles").toString());
		
		if(jso.has("description") && !jso.get("description").equals(JSONObject.NULL))
			cfObj.setDescription(jso.get("description").toString());
		
		if(jso.has("molecular_framework") && !jso.get("molecular_framework").equals(JSONObject.NULL))
			cfObj.setMolecularFramework(jso.get("molecular_framework").toString());
		
		if(jso.has("classification_version") && !jso.get("classification_version").equals(JSONObject.NULL))
			cfObj.setVersion(jso.get("classification_version").toString());
		
		//	Ontology
		if(jso.has("kingdom") && !jso.get("kingdom").equals(JSONObject.NULL)) {

			ClassyFireOntologyEntry kingdomEntry = 
					parseEntryFromJson(jso.getJSONObject("kingdom"));
			kingdomEntry.setLevel(ClassyFireOntologyLevel.KINGDOM);
			cfObj.getPrimaryClassification().put(ClassyFireOntologyLevel.KINGDOM, kingdomEntry);
		}
		if(jso.has("superclass") && !jso.get("superclass").equals(JSONObject.NULL)) {

			ClassyFireOntologyEntry superclassEntry = 
					parseEntryFromJson(jso.getJSONObject("superclass"));
			superclassEntry.setLevel(ClassyFireOntologyLevel.SUPERCLASS);
			cfObj.getPrimaryClassification().put(ClassyFireOntologyLevel.SUPERCLASS, superclassEntry);
		}	
		if(jso.has("class") && !jso.get("class").equals(JSONObject.NULL)) {

			ClassyFireOntologyEntry classEntry = 
					parseEntryFromJson(jso.getJSONObject("class"));
			classEntry.setLevel(ClassyFireOntologyLevel.CLASS);
			cfObj.getPrimaryClassification().put(ClassyFireOntologyLevel.CLASS, classEntry);
		}	
		if(jso.has("subclass") && !jso.get("subclass").equals(JSONObject.NULL)) {

			ClassyFireOntologyEntry subclassEntry = 
					parseEntryFromJson(jso.getJSONObject("subclass"));
			subclassEntry.setLevel(ClassyFireOntologyLevel.SUBCLASS);
			cfObj.getPrimaryClassification().put(ClassyFireOntologyLevel.SUBCLASS, subclassEntry);		
		}
		if(jso.has("direct_parent") && !jso.get("direct_parent").equals(JSONObject.NULL)) {

			ClassyFireOntologyEntry directParentEntry = 
					parseEntryFromJson(jso.getJSONObject("direct_parent"));
			directParentEntry.setLevel(ClassyFireOntologyLevel.DIRECT_PARENT);
			cfObj.getPrimaryClassification().put(ClassyFireOntologyLevel.DIRECT_PARENT, directParentEntry);
		}
		if(jso.has("alternative_parents")) {
			
			 JSONArray altParArray = (JSONArray) jso.get("alternative_parents");
			 for(int i=0; i<altParArray.length(); i++) {
				 
				 ClassyFireOntologyEntry apEntry = 
						 	parseEntryFromJson(altParArray.getJSONObject(i));
				 apEntry.setLevel(ClassyFireOntologyLevel.ALTERNATIVE_PARENT);
				 cfObj.getAlternativeParents().add(apEntry);
			 }
		}
		if(jso.has("intermediate_nodes")) {
			
			 JSONArray intermNodeArray = (JSONArray) jso.get("intermediate_nodes");
			 for(int i=0; i<intermNodeArray.length(); i++) {
				 
				 ClassyFireOntologyEntry inEntry = 
						 	parseEntryFromJson(intermNodeArray.getJSONObject(i));
				 inEntry.setLevel(ClassyFireOntologyLevel.INTERMEDIATE_NODE);
				 cfObj.getIntermediateNodes().add(inEntry);
			 }
		}
		if(jso.has("substituents")) {
			
			 JSONArray subsArray = (JSONArray) jso.get("substituents");
			 for(int i=0; i<subsArray.length(); i++) {
				 cfObj.getSubstituents().add(subsArray.getString(i));
			 }
		}
		if(jso.has("ancestors")) {
			
			 JSONArray ancArray = (JSONArray) jso.get("ancestors");
			 for(int i=0; i<ancArray.length(); i++) {
				 cfObj.getAncestors().add(ancArray.getString(i));
			 }
		}
		if(jso.has("predicted_chebi_terms")) {
			
			 JSONArray pctArray = (JSONArray) jso.get("predicted_chebi_terms");
			 for(int i=0; i<pctArray.length(); i++) {
				 
				 String ce = pctArray.getString(i);
					regexMatcher = chebiPattern.matcher(ce);
					
				if(regexMatcher.find()){
					
					String g1 = regexMatcher.group(1);
					String g2 = regexMatcher.group(2);
					String ceDescription = ce.replace(g1, "").trim();
					cfObj.getPredictedChebiTerms().put(g2, ceDescription);
				}
			 }
		}
		if(jso.has("predicted_lipidmaps_terms")) {
			
			 JSONArray plmArray = (JSONArray) jso.get("predicted_lipidmaps_terms");
			 for(int i=0; i<plmArray.length(); i++) {
				 
				 
				 String lm = plmArray.getString(i);
					regexMatcher = lmPattern.matcher(lm);
					
				if(regexMatcher.find()){
					
					String g1 = regexMatcher.group(1);
					String g2 = regexMatcher.group(2);
					String ceDescription = lm.replace(g1, "").trim();
					cfObj.getPredictedLipidMapsTerms().put(g2, ceDescription);
				}
			 }
		}
		if(jso.has("external_descriptors")) {
			
			 JSONArray pedArray = (JSONArray) jso.get("external_descriptors");
			 for(int i=0; i<pedArray.length(); i++) {
				 
				 
				 ClassyFireExternalDescriptor ed = 
						 parseExternalDescriptorFromJson(pedArray.getJSONObject(i));
				 cfObj.getExternalDescriptors().add(ed);
			 }
		}		
		return cfObj;
	}
	
	private static ClassyFireExternalDescriptor parseExternalDescriptorFromJson(JSONObject edJson) {
		
		String source = null;
		String sourceId = null;
		
		if(edJson.has("source"))
			source = edJson.get("source").toString();
		
		if(edJson.has("source_id"))
			sourceId = edJson.get("source_id").toString();
		
		ClassyFireExternalDescriptor ed = 
				new ClassyFireExternalDescriptor(source, sourceId);
		
		if(edJson.has("annotations")) {
			
			 JSONArray subsArray = (JSONArray) edJson.get("annotations");
			 for(int i=0; i<subsArray.length(); i++) {
				 ed.getAnnotations().add(subsArray.getString(i));
			 }
		}
		return ed;
	}
	
	private static ClassyFireOntologyEntry parseEntryFromJson(JSONObject ontJson) {
		
		ClassyFireOntologyEntry ontEntry = new ClassyFireOntologyEntry();
		if(ontJson.has("chemont_id"))
			ontEntry.setId(ontJson.get("chemont_id").toString().replace("CHEMONTID:", "C"));
		
		if(ontJson.has("name"))
			ontEntry.setName(ontJson.get("name").toString());
		
		if(ontJson.has("description"))
			ontEntry.setDescription(ontJson.get("description").toString());
		
		return ontEntry;
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
