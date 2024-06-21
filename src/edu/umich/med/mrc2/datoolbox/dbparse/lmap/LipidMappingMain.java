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

package edu.umich.med.mrc2.datoolbox.dbparse.lmap;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassification;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassificationObject;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsParser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class LipidMappingMain {

	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			populateLipidMapswithBulkAccessions();
			//	System.out.println("***");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			String category = rs.getString("CATEGORY");
			String mainClass = rs.getString("MAIN_CLASS");
			String subClass = rs.getString("SUB_CLASS");
			String classLevel4 = rs.getString("CLASS_LEVEL4");
			if(category != null) {
				
				LipidMapsClassificationObject categoryObject = 
						ipidMapsClassesMap.get(LipidMapsClassification.CATEGORY).
							stream().filter(o -> o.getCode().equals(category)).
							findFirst().orElse(null);
				if(categoryObject != null)
					record.getLmTaxonomy().add(categoryObject);
			}
			if(mainClass != null) {
				
				LipidMapsClassificationObject mainClassObject = 
						ipidMapsClassesMap.get(LipidMapsClassification.MAIN_CLASS).
							stream().filter(o -> o.getCode().equals(mainClass)).
							findFirst().orElse(null);
				if(mainClassObject != null)
					record.getLmTaxonomy().add(mainClassObject);
			}
			if(subClass != null) {
				
				LipidMapsClassificationObject subClassObject = 
						ipidMapsClassesMap.get(LipidMapsClassification.SUB_CLASS).
							stream().filter(o -> o.getCode().equals(subClass)).
							findFirst().orElse(null);
				if(subClassObject != null)
					record.getLmTaxonomy().add(subClassObject);
			}
			if(classLevel4 != null) {
				
				LipidMapsClassificationObject classLevel4Object = 
						ipidMapsClassesMap.get(LipidMapsClassification.CLASS_LEVEL4).
							stream().filter(o -> o.getCode().equals(classLevel4)).
							findFirst().orElse(null);
				if(classLevel4Object != null)
					record.getLmTaxonomy().add(classLevel4Object);
			}
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
