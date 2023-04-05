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

package edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class LipidMapsParser {

	public static Map<String, String>getCompoundDataMap(){

		Map<String, String>compoundDataMap = new TreeMap<String, String>();
		for(LipidMapsFields f : LipidMapsFields.values())
			compoundDataMap.put(f.name(), "");

		return compoundDataMap;
	}

	public static Map<String, String>getCrossRefMap(){

		Map<String, String>crossRefMap = new TreeMap<String, String>();
		for(LipidMapsCrossRefFields f : LipidMapsCrossRefFields.values())
			crossRefMap.put(f.getName(), "");

		return crossRefMap;
	}
	
	public static LipidMapsRecord parseMoleculeToLipidMapsRecord(
			IAtomContainer molecule) throws Exception {
		
		Map<String, String>lipidMapsDataMap = getCompoundDataMap();
		Map<String, String>lipidMapsCrossRefmap = getCrossRefMap();
		
		molecule.getProperties().forEach((k,v)->{

			if(lipidMapsDataMap.containsKey(k.toString()))
				lipidMapsDataMap.put(k.toString(), v.toString());

			if(lipidMapsCrossRefmap.containsKey(k.toString()))
				lipidMapsCrossRefmap.put(k.toString(), v.toString());
		});
		String lmid = lipidMapsDataMap.get(LipidMapsFields.LM_ID.name());
		String commonName = lipidMapsDataMap.get(LipidMapsFields.NAME.name());
		LipidMapsRecord record = new LipidMapsRecord(lmid, commonName);
		record.setSysName(lipidMapsDataMap.get(LipidMapsFields.SYSTEMATIC_NAME.name()));
		record.setAbbreviation(lipidMapsDataMap.get(LipidMapsFields.ABBREVIATION.name()));
		
		//	Compound identity
		record.getCompoundIdentity().setCommonName(commonName);
		record.getCompoundIdentity().setSysName(record.getSysName());

		record.getCompoundIdentity().setFormula(
				lipidMapsDataMap.get(LipidMapsFields.FORMULA.name()));
		record.getCompoundIdentity().setSmiles(
				lipidMapsDataMap.get(LipidMapsFields.SMILES.name()));
		record.getCompoundIdentity().setInChi(
				lipidMapsDataMap.get(LipidMapsFields.INCHI.name()));
		record.getCompoundIdentity().setInChiKey(
				lipidMapsDataMap.get(LipidMapsFields.INCHI_KEY.name()));		
		String massString = lipidMapsDataMap.get(LipidMapsFields.EXACT_MASS.name());
		if(massString != null && !massString.isEmpty()) {
			double mass = Double.parseDouble(massString);
			record.getCompoundIdentity().setExactMass(mass);
		}
		//	Database cross-reference
		record.getCompoundIdentity().addDbId(
				CompoundDatabaseEnum.LIPIDMAPS, lmid);
		for (Map.Entry<String, String> entry : lipidMapsCrossRefmap.entrySet()) {

			if(!entry.getValue().isEmpty()) {

				record.getCompoundIdentity().addDbId(
						LipidMapsCrossRefFields.valueOf(entry.getKey()).getDatabase(), 
						entry.getValue());
			}
		}		
		//Synonyms
		String synonyms = 
				lipidMapsDataMap.get(LipidMapsFields.SYNONYMS.name());
		if(!synonyms.isEmpty()) {

			String[] synonymsArray = 
					StringUtils.splitByWholeSeparator(synonyms, "; ");
			for(String syn : synonymsArray)
				record.getSynonyms().add(syn);			
		}
		//	Classification
		String category = lipidMapsDataMap.get(LipidMapsFields.CATEGORY.name());
		if(category != null && !category.isEmpty()) {
			
			String code = category.substring(category.length() - 3, category.length() - 1);
			String taxName = category.substring(0, category.length() - 4).trim();
			LipidMapsClassificationObject lmco = 
					new LipidMapsClassificationObject(LipidMapsClassification.CATEGORY, code, taxName);		    			
			record.addTaxonomyLevel(lmco);
		}
		String mainClass = lipidMapsDataMap.get(LipidMapsFields.MAIN_CLASS.name());
		if(mainClass != null && !mainClass.isEmpty()) {

			String code = mainClass.substring(mainClass.length() - 5, mainClass.length() - 1);
			String taxName = mainClass.substring(0, mainClass.length() - 6).trim();
			LipidMapsClassificationObject lmco = 
					new LipidMapsClassificationObject(LipidMapsClassification.MAIN_CLASS, code, taxName);				
			record.addTaxonomyLevel(lmco);
		}
		String subClass = lipidMapsDataMap.get(LipidMapsFields.SUB_CLASS.name());
		if(subClass != null && !subClass.isEmpty()) {
			
			String code = subClass.substring(subClass.length() - 7, subClass.length() - 1);
			String taxName = subClass.substring(0, subClass.length() - 8).trim();
			LipidMapsClassificationObject lmco = 
					new LipidMapsClassificationObject(LipidMapsClassification.SUB_CLASS, code, taxName);				
			record.addTaxonomyLevel(lmco);
		}		
		String classLevel4 = lipidMapsDataMap.get(LipidMapsFields.CLASS_LEVEL4.name());
		if(classLevel4 != null && !classLevel4.isEmpty()) {

			String code = classLevel4.substring(classLevel4.length() - 9, classLevel4.length() - 1);
			String taxName = classLevel4.substring(0, classLevel4.length() - 10).trim();
			LipidMapsClassificationObject lmco = 
					new LipidMapsClassificationObject(LipidMapsClassification.CLASS_LEVEL4, code, taxName);				
			record.addTaxonomyLevel(lmco);
		}		
		return record;
	}

	public static void insertLipidMapsRecord(IAtomContainer molecule, Connection conn) throws SQLException {

		PreparedStatement ps = null;
		Map<String, String>lipidMapsDataMap = getCompoundDataMap();
		Map<String, String>lipidMapsCrossRefmap = getCrossRefMap();
		molecule.getProperties().forEach((k,v)->{

			if(lipidMapsDataMap.containsKey(k.toString()))
				lipidMapsDataMap.put(k.toString(), v.toString());

			if(lipidMapsCrossRefmap.containsKey(k.toString()))
				lipidMapsCrossRefmap.put(k.toString(), v.toString());
		});
//		String smiles = "";
//		try {
//			smiles = smilesGenerator.create(molecule);
//		} catch (CDKException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String lmid = lipidMapsDataMap.get(LipidMapsFields.LM_ID.name());
		String commonName = lipidMapsDataMap.get(LipidMapsFields.NAME.name());
		String sysName = lipidMapsDataMap.get(LipidMapsFields.SYSTEMATIC_NAME.name());
		
		String category = lipidMapsDataMap.get(LipidMapsFields.CATEGORY.name());
		if(category != null && !category.isEmpty())
			category = category.substring(category.length() - 3, category.length() - 1);
		
		String mainClass = lipidMapsDataMap.get(LipidMapsFields.MAIN_CLASS.name());
		if(mainClass != null && !mainClass.isEmpty())
			mainClass = mainClass.substring(mainClass.length() - 5, mainClass.length() - 1);
		
		String subClass = lipidMapsDataMap.get(LipidMapsFields.SUB_CLASS.name());
		if(subClass != null && !subClass.isEmpty())
			subClass = subClass.substring(subClass.length() - 7, subClass.length() - 1);
		
		String classLevel4 = lipidMapsDataMap.get(LipidMapsFields.CLASS_LEVEL4.name());
		if(classLevel4 != null && !classLevel4.isEmpty())
			classLevel4 = classLevel4.substring(classLevel4.length() - 9, classLevel4.length() - 1);

		//	Insert primary data
		String dataQuery =
			"INSERT INTO LIPIDMAPS_COMPOUND_DATA " +
			"(LM_ID, SYSTEMATIC_NAME, COMMON_NAME, FORMULA, EXACT_MASS, " +
			"INCHI, INCHI_KEY, SMILES, CATEGORY, MAIN_CLASS, SUB_CLASS, CLASS_LEVEL4, ABBREVIATION) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, lmid);
		ps.setString(2, sysName);
		ps.setString(3, commonName);
		ps.setString(4, lipidMapsDataMap.get(LipidMapsFields.FORMULA.name()));
		ps.setString(5, lipidMapsDataMap.get(LipidMapsFields.EXACT_MASS.name()));
		ps.setString(6, lipidMapsDataMap.get(LipidMapsFields.INCHI.name()));
		ps.setString(7, lipidMapsDataMap.get(LipidMapsFields.INCHI_KEY.name()));
		ps.setString(8, lipidMapsDataMap.get(LipidMapsFields.SMILES.name()));
		ps.setString(9, category);
		ps.setString(10, mainClass);
		ps.setString(11, subClass);
		ps.setString(12, classLevel4);
		ps.setString(13, lipidMapsDataMap.get(LipidMapsFields.ABBREVIATION.name()));

		ps.executeUpdate();
		ps.close();

		//	Insert primary and systematic name(s)
		dataQuery = "INSERT INTO LIPIDMAPS_SYNONYMS (LM_ID, NAME, NTYPE) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, lmid);

		if(commonName.isEmpty())
			ps.setString(2, sysName);
		else
			ps.setString(2, commonName);

		ps.setString(3, "PRI");
		ps.executeUpdate();

		if(!commonName.isEmpty() && !sysName.isEmpty()) {
			ps.setString(2, sysName);
			ps.setString(3, "SYS");
			ps.executeUpdate();
		}
		//	Insert synonyms
		String synonyms = lipidMapsDataMap.get(LipidMapsFields.SYNONYMS.name());
		if(!synonyms.isEmpty()) {

			String[] synonymsArray = StringUtils.splitByWholeSeparator(synonyms, "; ");

			for(String syn : synonymsArray) {

				ps.setString(2, syn);
				ps.setString(3, "SYN");
				ps.executeUpdate();
			}
		}
		ps.close();

		//	Insert database references
		dataQuery = "INSERT INTO LIPIDMAPS_CROSSREF (LM_ID, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, lmid);
		for (Map.Entry<String, String> entry : lipidMapsCrossRefmap.entrySet()) {

			if(!entry.getValue().isEmpty()) {

				ps.setString(2, LipidMapsCrossRefFields.valueOf(entry.getKey()).getDatabase().name());
				ps.setString(3, entry.getValue());
				ps.executeUpdate();
			}
		}
		ps.close();
	}
	
	public static Collection<LipidMapsClassificationObject>getLipidMapsClasses() throws Exception {
				
		Collection<LipidMapsClassificationObject>lipidMapsClasses = 
				new ArrayList<LipidMapsClassificationObject>();
		
		Connection conn = ConnectionManager.getConnection();		
//		String query = 
//				"SELECT ID, CLASS_NAME, CLASS_LEVEL FROM LIPIDMAPS_CLASS";	
		String query = "SELECT CLASS_ID, NAME, CLASS_LEVEL FROM COMPOUNDDB.LIPIDMAPS_CLASSES";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
//
//			LipidMapsClassificationObject lco = new LipidMapsClassificationObject(
//					LipidMapsClassification.getLipidMapsClassificationLevelByName(rs.getString("CLASS_LEVEL")), 
//					rs.getString("ID"),
//					rs.getString("CLASS_NAME"));
			
			LipidMapsClassificationObject lco = new LipidMapsClassificationObject(
					LipidMapsClassification.getLipidMapsClassificationLevelByName(rs.getString("CLASS_LEVEL")), 
					rs.getString("CLASS_ID"),
					rs.getString("NAME"));
			lipidMapsClasses.add(lco);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return lipidMapsClasses;
	}
}





