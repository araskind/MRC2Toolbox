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

package edu.umich.med.mrc2.datoolbox.dbparse.lmap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassification;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassificationObject;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsParser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.JSONUtils;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.PubChemUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class LipidMappingMain {

	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {			
			updateBulkIdsFromLipidMaps();
//			Collection<String>idSet = getLMIDsByAbbreviation("SM(d41:1)");
//			System.out.println("***");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void updateBulkIDFromHeadGroupAndRefMetFormula() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String selectQuery = 
				"SELECT LOOKUP_NAME, REFMET_FORMULA " +
				"FROM COMPOUNDDB.RO3_LIPID_REMAP " +
				"WHERE BULK_LIPID_ID IS NULL";
		PreparedStatement selectPs = conn.prepareStatement(selectQuery);
		Map<String,String>nameFormulaMap = new TreeMap<String,String>();
		ResultSet rs = selectPs.executeQuery();
		while(rs.next()) 
			nameFormulaMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		selectQuery = 
				"SELECT LM_BULK_ID FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS "
				+ "WHERE HEAD_GROUP = ? AND MOL_FORMULA = ?";
		selectPs = conn.prepareStatement(selectQuery);
		
		String updQuery = "UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET BULK_LIPID_ID = ? WHERE LOOKUP_NAME = ?";			
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		for(Entry<String, String> pair : nameFormulaMap.entrySet()) {
			
			String headGroupString = pair.getKey().split("\\(")[0].trim();
			selectPs.setString(1, headGroupString);
			selectPs.setString(2, pair.getValue());
			rs = selectPs.executeQuery();
			String bulkId = null;
			while(rs.next()) 
				bulkId =  rs.getString(1);
			
			rs.close();
			
			if(bulkId != null) {
				updPs.setString(1, bulkId);
				updPs.setString(2, pair.getKey());
				updPs.executeUpdate();					
			}
		}
		selectPs.close();		
		updPs.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void updateRefMetNameFromManualLookups() throws Exception {
		
		File inputFile = new File("E:\\DataAnalysis\\Lipidomics\\_RO3 Lipidomics Data for Remapping\\refmetRemap_extra2.txt");
		String[][] compoundData = null;
		try {
			compoundData = DelimitedTextParser.parseTextFileWithEncoding(
					inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection conn = ConnectionManager.getConnection();		
		String updQuery = "UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET REFMET_NAME = ? WHERE LOOKUP_NAME = ? "
				//+ "AND REFMET_NAME IS NULL"
				;
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		if (compoundData != null) {
			
			for(int i=1; i<compoundData.length; i++) {
				
				String lookupName = compoundData[i][0];
				String refMetName = compoundData[i][1];
				updPs.setString(1, refMetName);
				updPs.setString(2, lookupName);
				updPs.executeUpdate();					
			}
		}		
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateRefMetNameFromStudyLipidNameManualLookups() throws Exception {
		
		File inputFile = new File("E:\\DataAnalysis\\Lipidomics\\_RO3 Lipidomics Data for Remapping\\refmetRemap2studyName.txt");
		String[][] compoundData = null;
		try {
			compoundData = DelimitedTextParser.parseTextFileWithEncoding(
					inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection conn = ConnectionManager.getConnection();		
		String updQuery = "UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET REFMET_NAME = ? WHERE LIPID_ID_FROM_STUDY = ? "
				+ "AND REFMET_NAME IS NULL";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		if (compoundData != null) {
			
			for(int i=1; i<compoundData.length; i++) {
				
				String lookupName = compoundData[i][0];
				String refMetName = compoundData[i][1];
				updPs.setString(1, refMetName);
				updPs.setString(2, lookupName);
				updPs.executeUpdate();					
			}
		}		
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateAbbreviationsFromManualLookups() throws Exception {
		
		File inputFile = new File("E:\\DataAnalysis\\Lipidomics\\_RO3 Lipidomics Data for Remapping\\manualLookups4.txt");
		String[][] compoundData = null;
		try {
			compoundData = DelimitedTextParser.parseTextFileWithEncoding(
					inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection conn = ConnectionManager.getConnection();
		String selectQuery = 
				"SELECT C.LM_BULK_ID " +
				"FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS C, " +
				"COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D " +
				"WHERE D.LMID = ? " +
				"AND C.ABBREVIATION IS NOT NULL " +
				"AND D.ABBREVIATION = C.ABBREVIATION ";
		PreparedStatement selectPs = conn.prepareStatement(selectQuery);
		
		String updQuery = "UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET BULK_LIPID_ID = ? WHERE LOOKUP_NAME = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		if (compoundData != null) {
			
			for(int i=1; i<compoundData.length; i++) {
				
				String lookupName = compoundData[i][0];
				String lmid = compoundData[i][1];
				String bulkId = null;
				
				selectPs.setString(1, lmid);
				ResultSet rs = selectPs.executeQuery();
				while(rs.next())
					bulkId = rs.getString(1);
				
				if(bulkId != null) {
					updPs.setString(1, bulkId);
					updPs.setString(2, lookupName);
					updPs.executeUpdate();					
				}
				else {
					System.out.println("No abbreviation for " + lmid);
				}
			}
		}
		selectPs.close();		
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void getAbbreviationsFromLipidMapsHTML() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT LOOKUP_NAME FROM "
				+ "COMPOUNDDB.RO3_LIPID_REMAP "
				+ "WHERE BULK_LIPID_ID IS NULL ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		Set<String>abbreviationLookupSet = new TreeSet<String>();
		
		String selectQuery = 
				"SELECT C.LM_BULK_ID " +
				"FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS C, " +
				"COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D " +
				"WHERE D.LMID = ? " +
				"AND C.ABBREVIATION IS NOT NULL " +
				"AND D.ABBREVIATION = C.ABBREVIATION ";
		PreparedStatement selectPs = conn.prepareStatement(selectQuery);
		
		String updQuery = "UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET BULK_LIPID_ID = ? WHERE LOOKUP_NAME = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);

		ResultSet rs = ps.executeQuery();
		while(rs.next())
			abbreviationLookupSet.add(rs.getString(1));
		
		rs.close();
		
		Pattern numPattern = Pattern.compile("/databases/lmsd/([A-Z]+\\d+)");
		Matcher regexMatcher = null;;	
		String lipidMapsQuickSearchUrl = "https://lipidmaps.org/quick_search?q=";
		
		Set<String>lmidSet = new TreeSet<String>();
		List<String>lines = new ArrayList<String>();
		lines.add("LookupName\tURL");
		for(String abbr : abbreviationLookupSet) {
			
			lmidSet.clear();
			String abbrNoBrackets = abbr.replace("(", " ").replace(")", "");
			String requestUrl = lipidMapsQuickSearchUrl + urlEncodeAbbr4chains(abbrNoBrackets);
			lines.add(abbr + "\t" + requestUrl);
			
//			String htmlString = WebUtils.readGetResponseAsHTMLpageFromURL(requestUrl);
//			
//			if(htmlString == null || htmlString.isEmpty()){
//				
//				System.out.println("Data for " + abbr + " not found");
//				continue;
//			}
//			regexMatcher = numPattern.matcher(htmlString);
//			while (regexMatcher.find())
//				lmidSet.add(regexMatcher.group(1));	
//			
//			if(!lmidSet.isEmpty()) {
//				
//				String bulkId = null;
//				for(String lmid : lmidSet) {
//					
//					selectPs.setString(1, lmid);
//					rs = selectPs.executeQuery();
//					while(rs.next())
//						bulkId = rs.getString(1);
//					
//					if(bulkId != null)
//						break;
//				}	
//				if(bulkId != null) {
//					
//					updPs.setString(1, bulkId);
//					updPs.setString(1, abbr);
//					updPs.executeUpdate();
//				}					
//			}			
		}
		ps.close();
		selectPs.close();		
		updPs.close();
		ConnectionManager.releaseConnection(conn);
		
		Path outputPath = Paths.get("E:\\DataAnalysis\\Lipidomics\\"
				+ "_RO3 Lipidomics Data for Remapping\\LipidMapsNameLookups2.txt");
		try {
			Files.write(outputPath, 
					lines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void remapToBulkIdRemovingBrackets2() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT LOOKUP_NAME FROM "
				+ "COMPOUNDDB.RO3_LIPID_REMAP "
				+ "WHERE BULK_LIPID_ID IS NULL ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		Map<String,String>abbreviationLookupMap = new TreeMap<String,String>();

		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String lookupName = rs.getString(1);
			String correctedName = lookupName.replace("(", " ").replace(")", "").
					replaceAll("\\s+", " ").trim().toUpperCase();
			abbreviationLookupMap.put(lookupName, correctedName);		
		}
		rs.close();
		
		String updQuery =  
				"UPDATE COMPOUNDDB.RO3_LIPID_REMAP R SET R.BULK_LIPID_ID =  " +
				"(SELECT B.LM_BULK_ID FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS B " +
				"WHERE B.ABBREVIATION = ?) " +
				"WHERE R.LOOKUP_NAME = ? AND R.BULK_LIPID_ID IS NULL ";		
		PreparedStatement updPs = conn.prepareStatement(updQuery);	
		for(Entry<String, String> pair : abbreviationLookupMap.entrySet()) {
			
			updPs.setString(1, pair.getValue());
			updPs.setString(2, pair.getKey());
			updPs.executeUpdate();
		}
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);				
	}
	
	private static void updateBulkIdsFromLipidMaps() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();	
		String query = 
				"SELECT LOOKUP_NAME, MOL_FORMULA  " +
				"FROM COMPOUNDDB.RO3_LIPID_REMAP " +
				"WHERE BULK_LIPID_ID IS NULL  " +
				//	"AND MOL_FORMULA IS NOT NULL AND MOL_FORMULA NOT LIKE '%D%' " +
				"ORDER BY 1,2";
		PreparedStatement ps = conn.prepareStatement(query);		
		String selectQuery = 
				"SELECT C.LM_BULK_ID " +
				"FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS C, " +
				"COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D " +
				"WHERE D.LMID = ? " +
				"AND D.ABBREVIATION = C.ABBREVIATION ";
		PreparedStatement selectPs = conn.prepareStatement(selectQuery);
		
		String updQuery = "UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET BULK_LIPID_ID = ? WHERE LOOKUP_NAME = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		Map<String,String>abrFormulaMap = new TreeMap<String,String>();
		Map<String,String>abrFixedMap = new TreeMap<String,String>();	
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String abbr = rs.getString(1);
			if(abrFormulaMap.containsKey(abbr)) {	//|| !abbr.contains("(")
				//	System.out.println(abbr + " | F1: " + abrFormulaMap.get(abbr) + " | F2: " + rs.getString(2));
			}
			else {
				abrFormulaMap.put(abbr, rs.getString(2));				
			}
		}
		rs.close();
		
		for(String abbr : abrFormulaMap.keySet()) {
			
			//	String abbrFixed = abbr.replace("(", " ").replace(")", "").replaceAll("\\s+", " ").trim().toUpperCase();
//			String abbrFixed = abbr.replaceAll("\\s+", " ").trim().toUpperCase();
//			Collection<String>lmIds = getLMIDsByAbbreviation(abbrFixed);	
			
			String abbrFixed = abbr.replaceAll("\\s+", " ").trim();
			Collection<String>lmIds = getLMIDsByAbbreviationChain(abbrFixed);	
			if(!lmIds.isEmpty()) {
				
				String bulkId = null;
				for(String lmid : lmIds) {
					
					selectPs.setString(1, abbrFixed);
					rs = ps.executeQuery();
					while(rs.next())
						bulkId = rs.getString(1);
					
					rs.close();
					if(bulkId != null) {
						
						updPs.setString(1, bulkId);
						updPs.setString(2, abbr);
						updPs.executeUpdate();
						break;
					}					
				}			
			}
		}
		selectPs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static Collection<String>getLMIDsByAbbreviation(String abbreviation){
		
		Collection<String>idSet = new TreeSet<String>();
		String lipidMapsRestUrl = "https://lipidmaps.org/rest/compound/abbrev/";
		JSONObject casJson = null;	
		String req = lipidMapsRestUrl + PubChemUtils.urlEncodeSmiles(abbreviation) + "/lm_id";
		System.out.println(req);
		try {
			casJson = JSONUtils.readJsonFromUrl(lipidMapsRestUrl + 
					PubChemUtils.urlEncodeSmiles(abbreviation) + "/lm_id");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(casJson == null)
			return idSet;
		
		for(String key : casJson.keySet()) {
			
			String lmid = casJson.getJSONObject(key).get("lm_id").toString();
			idSet.add(lmid);
		}
		return idSet;
	}
	
	private static Collection<String>getLMIDsByAbbreviationChain(String abbreviation){
		
		Collection<String>idSet = new TreeSet<String>();
		String lipidMapsRestUrl = "https://lipidmaps.org/rest/compound/abbrev_chains/";
		JSONObject casJson = null;	
		String req = lipidMapsRestUrl + urlEncodeAbbr4chains(abbreviation) + "/lm_id";
		
		if(abbreviation.startsWith("Cer"))
			System.out.println(req);
		
		try {
			casJson = JSONUtils.readJsonFromUrl(req);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(casJson == null)
			return idSet;

		String lmid = null;
		try {
			lmid = casJson.getString("lm_id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(lmid != null)
			idSet.add(lmid);
		
		return idSet;
	}
	
	public static String urlEncodeAbbr4chains(String abbr) {
		
		String result = null;
	    try {
	        result = URLEncoder.encode(abbr, "UTF-8")
	        		.replaceAll("\\+", "%20")
	                .replaceAll("\\%2F", "/")
	                .replaceAll("\\%3A", ":")
	                .replaceAll("\\%3B", ";");
	    } catch (UnsupportedEncodingException e) {
	        result = abbr;
	    }
	    //	System.out.println(result);
	    return result;
	}

	private static void addBulkIdsFromCompoundTable() throws Exception{
			
		Connection conn = ConnectionManager.getConnection();	
		String query = 
				"SELECT DISTINCT L.ABBREVIATION, L.MOLECULAR_FORMULA " +
				"FROM  COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA L " +
				"LEFT JOIN COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS  I ON L.ABBREVIATION = I.ABBREVIATION " +
				"WHERE I.ABBREVIATION IS NULL AND L.ABBREVIATION IS NOT NULL " +
				"ORDER BY 1,2 ";
		PreparedStatement ps = conn.prepareStatement(query);
		Map<String,String>abrFormulaMap = new TreeMap<String,String>();
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String abbr = rs.getString(1);
			if(abrFormulaMap.containsKey(abbr))
				System.out.println(abbr + " | F1: " + abrFormulaMap.get(abbr) + " | F2: " + rs.getString(2));
			else
				abrFormulaMap.put(abbr, rs.getString(2));
		}
		rs.close();
		ps.close();
		Pattern numPattern = Pattern.compile("([A-Za-z\\-*]+[ ]*)(\\d+:\\d+)");
		Matcher regexMatcher;
		String insertQuery = 
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS ("
				+ "LM_BULK_ID, HEAD_GROUP, ABBREVIATION, MASS, "
				+ "MOL_FORMULA, CHAIN_TYPE, NUM_CARBONS, NUM_DOUBLE_BONDS) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";		
		PreparedStatement insertPs = conn.prepareStatement(insertQuery);
		for(Entry<String, String> pair : abrFormulaMap.entrySet()) {
			
			String abbreviation = pair.getKey();		
			regexMatcher = numPattern.matcher(abbreviation);
			int numC = -1;
			int numDouble = -1;		
			String headGroup = null;
			String chainType = "odd";
			if(regexMatcher.find()) {
				
				headGroup = regexMatcher.group(1);
				String[]counts = regexMatcher.group(2).split(":");
				numC = Integer.parseInt(counts[0]);
				numDouble = Integer.parseInt(counts[1]);
				if(numC % 2 == 0)
					chainType = "even";				
			}			
			String formula = pair.getValue();			
			double mass = MolFormulaUtils.calculateExactMonoisotopicMass(formula);
			if(headGroup != null) {
				String nextId = null;
				try {
					nextId = SQLUtils.getNextIdFromSequence(
							"COMPOUNDDB.LIPIDMAPS_BULK_SEQ", 
							DataPrefix.LIPID_MAPS_BULK, "0", 7);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				insertPs.setString(1, nextId);
				insertPs.setString(2, headGroup);
				insertPs.setString(3, abbreviation);
				insertPs.setDouble(4, mass);
				insertPs.setString(5, formula);
				insertPs.setString(6, chainType);
				if(numC > 0)
					insertPs.setInt(7, numC);
				else
					insertPs.setNull(7, java.sql.Types.NULL);
				
				if(numDouble >= 0)
					insertPs.setInt(8, numDouble);
				else
					insertPs.setNull(8, java.sql.Types.NULL);
				
				insertPs.executeUpdate();
			}
		}
		insertPs.close();
		
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void remapToBulkIdRemovingBrackets() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT LOOKUP_NAME FROM "
				+ "COMPOUNDDB.RO3_LIPID_REMAP "
				+ "WHERE BULK_LIPID_ID IS NULL ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		Map<String,String>abbreviationLookupMap = new TreeMap<String,String>();
		Pattern numPattern = Pattern.compile("^([A-Za-z]+[ ]*)\\((\\d+:\\d+.*)\\)");
		Matcher regexMatcher;
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String lookupName = rs.getString(1);
			regexMatcher = numPattern.matcher(lookupName);
			if(regexMatcher.find()) {
				
				String correctedName = 
						regexMatcher.group(1).trim() + " " + regexMatcher.group(2);
				abbreviationLookupMap.put(lookupName, correctedName);
			}
		}
		rs.close();
		
		String updQuery =  
				"UPDATE COMPOUNDDB.RO3_LIPID_REMAP R SET R.BULK_LIPID_ID = " +
				"(SELECT DISTINCT C.LM_BULK_ID FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS C  " +
				"WHERE C.ABBREVIATION = ?) " +
				"WHERE R.LOOKUP_NAME = ? AND  R.BULK_LIPID_ID IS NULL ";		
		PreparedStatement updPs = conn.prepareStatement(updQuery);	
		for(Entry<String, String> pair : abbreviationLookupMap.entrySet()) {
			
			updPs.setString(1, pair.getValue());
			updPs.setString(2, pair.getKey());
			updPs.executeUpdate();
		}
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);				
	}
	
	private static void assignLowestUniqueTaxNodeToBulkId() throws Exception{
		
		Map<String,String>bulkIdAbbreviationMap = new TreeMap<String,String>();
		Map<LipidMapsClassification,List<String>>bulkIdTaxMap = 
				new TreeMap<LipidMapsClassification,List<String>>();
		for(LipidMapsClassification c : LipidMapsClassification.values())
			bulkIdTaxMap.put(c, new ArrayList<String>());
		
		String bulkId = null;		
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT LM_BULK_ID, ABBREVIATION FROM "
				+ "COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS WHERE LM_BULK_ID ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet rs = ps.executeQuery();
		while(rs.next())
			bulkIdAbbreviationMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		query = 
				"SELECT CATEGORY, MAIN_CLASS, SUB_CLASS, CLASS_LEVEL4 FROM "
				+ "COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS_TAXONOMY_MAP WHERE LM_BULK_ID = ?";
		ps = conn.prepareStatement(query);
		
		String updQuery = 
				"UPDATE COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS "
				+ "SET UNIQUE_TAX_NODE = ? WHERE LM_BULK_ID = ?" ;		
		PreparedStatement updPs = conn.prepareStatement(updQuery);	

		for(Entry<String, String> pair : bulkIdAbbreviationMap.entrySet()) {
			
			ps.setString(1, pair.getKey());
			rs = ps.executeQuery();
			while(rs.next()) {
				
				for(Entry<LipidMapsClassification,List<String>> me : bulkIdTaxMap.entrySet())
					me.getValue().clear();
				
				if(rs.getString(1) != null)
					bulkIdTaxMap.get(LipidMapsClassification.CATEGORY).add(rs.getString(1));
				
				if(rs.getString(2) != null)
					bulkIdTaxMap.get(LipidMapsClassification.MAIN_CLASS).add(rs.getString(2));
				
				if(rs.getString(3) != null)
					bulkIdTaxMap.get(LipidMapsClassification.SUB_CLASS).add(rs.getString(3));
				
				if(rs.getString(4) != null)
					bulkIdTaxMap.get(LipidMapsClassification.CLASS_LEVEL4).add(rs.getString(4));			
			}
			rs.close();
			
			String uniqueTaxId = null;
			if(bulkIdTaxMap.get(LipidMapsClassification.CLASS_LEVEL4).size() == 1)
				uniqueTaxId = bulkIdTaxMap.get(LipidMapsClassification.CLASS_LEVEL4).get(0);
			
			if(uniqueTaxId == null && bulkIdTaxMap.get(LipidMapsClassification.SUB_CLASS).size() == 1)
				uniqueTaxId = bulkIdTaxMap.get(LipidMapsClassification.SUB_CLASS).get(0);
			
			if(uniqueTaxId == null && bulkIdTaxMap.get(LipidMapsClassification.MAIN_CLASS).size() == 1)
				uniqueTaxId = bulkIdTaxMap.get(LipidMapsClassification.MAIN_CLASS).get(0);
			
			if(uniqueTaxId == null && bulkIdTaxMap.get(LipidMapsClassification.CATEGORY).size() == 1)
				uniqueTaxId = bulkIdTaxMap.get(LipidMapsClassification.CATEGORY).get(0);
			
			if(uniqueTaxId != null) {
				updPs.setString(1, uniqueTaxId);
				updPs.setString(2, pair.getKey());
				updPs.executeUpdate();
			}
		}
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);	
	}
	
	private static void populateLipidBulkWithNumCarbNumDoubleBonds() throws Exception{
		
		Map<String,String>bulkIdAbbreviationMap = new TreeMap<String,String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT LM_BULK_ID, ABBREVIATION FROM "
				+ "COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet rs = ps.executeQuery();
		while(rs.next())
			bulkIdAbbreviationMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		Pattern numPattern = Pattern.compile("\\d+:\\d+");
		Matcher regexMatcher;
		
		String updQuery = 
				"UPDATE COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS "
				+ "SET NUM_CARBONS = ?, NUM_DOUBLE_BONDS = ? "
				+ "WHERE LM_BULK_ID = ?" ;		
		PreparedStatement updPs = conn.prepareStatement(updQuery);		
		for(Entry<String, String> pair : bulkIdAbbreviationMap.entrySet()) {
				
			regexMatcher = numPattern.matcher(pair.getValue());
			if(regexMatcher.find()) {
				String[]counts = regexMatcher.group(0).split(":");
				updPs.setInt(1, Integer.parseInt(counts[0]));
				updPs.setInt(2, Integer.parseInt(counts[1]));
				updPs.setString(3, pair.getKey());
				updPs.executeUpdate();
			}	
		}
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);		
	}

	private static void populateLipidBulkToLipidOntologyMap() throws Exception{
		
		Map<String,String>bulkIdAbbreviationMap = new TreeMap<String,String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT LM_BULK_ID, ABBREVIATION FROM "
				+ "COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet rs = ps.executeQuery();
		while(rs.next())
			bulkIdAbbreviationMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		query = 
			"SELECT DISTINCT CATEGORY, MAIN_CLASS, SUB_CLASS, CLASS_LEVEL4  " +
			"FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA WHERE ABBREVIATION = ?";
		ps = conn.prepareStatement(query);
		
		String inserQuery = 
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS_TAXONOMY_MAP("
				+ "LM_BULK_ID, CATEGORY, MAIN_CLASS, SUB_CLASS, CLASS_LEVEL4) "
				+ "VALUES(?,?,?,?,?)";
		PreparedStatement insertPs = conn.prepareStatement(inserQuery);
		
		for(Entry<String, String> pair : bulkIdAbbreviationMap.entrySet()) {
						
			ps.setString(1, pair.getValue());
			rs = ps.executeQuery();
			while(rs.next()) {
				
				insertPs.setString(1, pair.getKey());
				
				//	Category
				if(rs.getString(1) != null)
					insertPs.setString(2, rs.getString(1));
				else
					insertPs.setNull(2, java.sql.Types.NULL);

				//	Main class
				if(rs.getString(2) != null)
					insertPs.setString(3, rs.getString(2));
				else
					insertPs.setNull(3, java.sql.Types.NULL);
				
				//	Subclass
				if(rs.getString(3) != null)
					insertPs.setString(4, rs.getString(3));
				else
					insertPs.setNull(4, java.sql.Types.NULL);
				
				//	Class level 4
				if(rs.getString(4) != null)
					insertPs.setString(5, rs.getString(4));
				else
					insertPs.setNull(5, java.sql.Types.NULL);
				
				insertPs.executeUpdate();
			}
			rs.close();
		}
		ps.close();
		insertPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	private static void gatherStatsOnLipidOntology() throws Exception{
		
		Set<String>abbreviations = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT ABBREVIATION FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet rs = ps.executeQuery();
		while(rs.next())
			abbreviations.add(rs.getString(1));
		
		rs.close();
		int maxCategoryCount = 0;
		int maxMainClassCount = 0;
		int maxSubClassCount = 0;
		int maxClass4Count = 0;
		
		Set<String>categories = new TreeSet<String>();
		Set<String>mainClasses = new TreeSet<String>();
		Set<String>subClasses = new TreeSet<String>();
		Set<String>level4s = new TreeSet<String>();
		
		query = 
			"SELECT DISTINCT CATEGORY, MAIN_CLASS, SUB_CLASS, CLASS_LEVEL4  " +
			"FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA WHERE ABBREVIATION = ?";
		ps = conn.prepareStatement(query);
		for(String abbrev : abbreviations) {
			
			categories.clear();
			mainClasses.clear();
			subClasses.clear();
			level4s.clear();
			
			ps.setString(1, abbrev);
			rs = ps.executeQuery();
			while(rs.next()) {
				
				if(rs.getString(1) != null)
					categories.add(rs.getString(1));

				if(rs.getString(2) != null)
					mainClasses.add(rs.getString(2));
				
				if(rs.getString(3) != null)
					subClasses.add(rs.getString(3));
				
				if(rs.getString(4) != null)
					level4s.add(rs.getString(4));
				
			}
			rs.close();
			
			if(categories.size() > maxCategoryCount)
				maxCategoryCount = categories.size();
			
			if(mainClasses.size() > maxMainClassCount)
				maxMainClassCount = mainClasses.size();
			
			if(subClasses.size() > maxSubClassCount)
				maxSubClassCount = subClasses.size();
			
			if(level4s.size() > maxClass4Count)
				maxClass4Count = level4s.size();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		System.out.println("Max categories " + maxCategoryCount);
		System.out.println("Max main classes " + maxMainClassCount);
		System.out.println("Max sub-classes " + maxSubClassCount);
		System.out.println("Max level 4 classes " + maxClass4Count);
	}
		
	private static void populateLipidMapswithBulkAccessions() throws Exception{
		
		Map<LipidMapsClassification, List<LipidMapsClassificationObject>>ipidMapsClassesMap = 
				LipidMapsParser.getLipidMapsClasses().stream()
			      .collect(Collectors.groupingBy(LipidMapsClassificationObject::getGroup));
		
		Collection<LipidMapsTaxonomyRecord>taxRecords = 
				new ArrayList<LipidMapsTaxonomyRecord>();
		Connection conn = ConnectionManager.getConnection();
		String selectQuery = 
				"SELECT LMID, CATEGORY, MAIN_CLASS, SUB_CLASS, "
				+ "CLASS_LEVEL4, MS_READY_MOL_FORMULA "
				+ "FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA";
		PreparedStatement selectPs = conn.prepareStatement(selectQuery);
		ResultSet rs = selectPs.executeQuery();
		while(rs.next()) {			

			LipidMapsTaxonomyRecord record = 
					new LipidMapsTaxonomyRecord(
							rs.getString("LMID"),
							rs.getString("MS_READY_MOL_FORMULA"));			
			record.addTaxonomyLevel(
					LipidMapsClassification.CATEGORY,
					rs.getString("CATEGORY"));
			record.addTaxonomyLevel(
					LipidMapsClassification.MAIN_CLASS,
					rs.getString("MAIN_CLASS"));
			record.addTaxonomyLevel(
					LipidMapsClassification.SUB_CLASS,
					rs.getString("SUB_CLASS"));
			record.addTaxonomyLevel(
					LipidMapsClassification.CLASS_LEVEL4,
					rs.getString("CLASS_LEVEL4"));
			taxRecords.add(record);
		}
		rs.close();
		
		selectQuery = 
				"SELECT LM_BULK_ID, MOL_FORMULA " +
				"FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS";
		selectPs = conn.prepareStatement(selectQuery);	
		Map<String,String>bulkFormulaMap = new TreeMap<String,String>();
		rs = selectPs.executeQuery();
		while(rs.next())			
			bulkFormulaMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		String updQuery = 
				"UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET BULK_LIPID_ID = ? WHERE ACCESSION = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
			
		for(Entry<String, String> pair : bulkFormulaMap.entrySet()) {
			
			String formula = pair.getValue();
			List<LipidMapsTaxonomyRecord> sameFormulaRecords = 
					taxRecords.stream().filter(r -> r.getFormula().equals(formula)).
					collect(Collectors.toList());
		}
		selectPs.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateLipidBulkIdFromAccession() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String selectQuery = 
				"SELECT LOOKUP_NAME, ACCESSION "
				+ "FROM COMPOUNDDB.RO3_LIPID_REMAP "
				+ "WHERE BULK_LIPID_ID IS NULL";
		PreparedStatement selectPs = conn.prepareStatement(selectQuery);
		
		String selectQuery2 = 
				"SELECT B.LM_BULK_ID " +
				"FROM COMPOUNDDB.LIPIDMAPS_BULK_LIPIDS B, " +
				"COMPOUNDDB.COMPOUND_DATA D " +
				"WHERE B.MOL_FORMULA = D.MOL_FORMULA " +
				"AND B.HEAD_GROUP = ? " +
				"AND  D.ACCESSION = ? ";
		PreparedStatement selectPs2 = conn.prepareStatement(selectQuery2);
				
		String updQuery = 
				"UPDATE COMPOUNDDB.RO3_LIPID_REMAP "
				+ "SET BULK_LIPID_ID = ? WHERE ACCESSION = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		ResultSet rs = selectPs.executeQuery();
		while(rs.next()) {
			
			String headGroupString = rs.getString(1).split(" ")[0];
			String accession = rs.getString(2);
			
			selectPs2.setString(1, headGroupString);
			selectPs2.setString(2, accession);
			ResultSet rs2 = selectPs2.executeQuery();
			while(rs2.next()) {
				
				updPs.setString(1, rs2.getString(1));
				updPs.setString(2, accession);
				updPs.executeUpdate();
			}
			rs2.close();
		}
		rs.close();
		selectPs.close();
		selectPs2.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
}
