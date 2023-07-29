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

package edu.umich.med.mrc2.datoolbox.dbparse.integrate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.WebUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class HMDBIntegration {

	public static void copyHMDBdata2compounds() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Map<String,String>accessionKeyList = new TreeMap<String,String>();
		Map<String,Set<String>>compoundGroups = new TreeMap<String,Set<String>>();
		try {
			accessionKeyList = getAccessionList(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sameCompoundQuery = 
				"SELECT ACCESSION FROM COMPOUNDDB.COMPOUND_TAUTOMERS "
				+ "WHERE DB_SOURCE = 'HMDB' AND ACCESSION != ? AND TAUTOMER_INCHI_KEY = ?";
		PreparedStatement sameCompoundPs = conn.prepareStatement(sameCompoundQuery);
		
		String compoundCopyQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_DATA "
				+ "(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, SMILES, "
				+ "INCHI, INCHI_KEY, INCHI_KEY2D, CHARGE, MS_READY_MOL_FORMULA, "
				+ "MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, MS_READY_INCHI_KEY2D) "
				+ "(SELECT ACCESSION, 'HMDB', NAME, FORMULA_FROM_SMILES, MASS_FROM_SMILES, "
				+ "SMILES, INCHI, INCHI_KEY_FROM_SMILES, INCHI_KEY_FS_CONNECT, CHARGE, "
				+ "MS_READY_MOL_FORMULA, MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, "
				+ "MS_READY_INCHI_KEY2D FROM COMPOUNDDB.HMDB_COMPOUND_DATA WHERE ACCESSION = ?)";
		PreparedStatement compoundCopyPs = conn.prepareStatement(compoundCopyQuery);
		
		String insertGroupQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_GROUP "
				+ "(PRIMARY_ACCESSION, PRIMARY_SOURCE_DB, "
				+ "SECONDARY_ACCESSION, SECONDARY_SOURCE_DB) VALUES(?, ?, ?, ?)";
		PreparedStatement insertGroupPs = conn.prepareStatement(insertGroupQuery);
		insertGroupPs.setString(2, "HMDB");
		insertGroupPs.setString(4, "HMDB");
		
		int counter = 1;
		Set<String>matchingCompoundAccessions = new TreeSet<String>();
		Set<String>excludedAccessions = new TreeSet<String>();
		Set<String>deprecatedAccessions = new TreeSet<String>();
		for(Entry<String, String> accessionEntry : accessionKeyList.entrySet()) {
		
			matchingCompoundAccessions.clear();
			if(excludedAccessions.contains(accessionEntry.getKey()))
				continue;
			
			sameCompoundPs.setString(1,accessionEntry.getKey());
			sameCompoundPs.setString(2, accessionEntry.getValue());
			ResultSet rs = sameCompoundPs.executeQuery();
			while(rs.next()) 
				matchingCompoundAccessions.add(rs.getString(1));
			
			if(matchingCompoundAccessions.isEmpty()) {
				
				compoundCopyPs.setString(1, accessionEntry.getKey());
				compoundCopyPs.executeUpdate();
			}
			else {
				excludedAccessions.addAll(matchingCompoundAccessions);				
				insertGroupPs.setString(1, accessionEntry.getKey());
				for(String mid : matchingCompoundAccessions) {
					
					if(hmdbEntryActive(mid)) {
						insertGroupPs.setString(3, mid);
						insertGroupPs.addBatch();
					}
					else {
						deprecatedAccessions.add(mid);
					}
				}
				insertGroupPs.executeBatch();
//				compoundGroups.put(accessionEntry.getKey(), matchingCompoundAccessions);
//				System.out.println("***");
			}			
			counter++;
			if(counter % 100 == 0)
				System.out.print(".");
			if(counter % 10000 == 0)
				System.out.print("\n");
		}	
		sameCompoundPs.close();
		compoundCopyPs.close();
		insertGroupPs.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Write out deprecated accessions
		Path logPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\_LATEST\\HMDB-5-2022-11-17\\deprecated_accessions_20230727.txt");
		try {
			Files.write(logPath, 
					deprecatedAccessions, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean hmdbEntryActive(String accession) {

		InputStream hmdbXmlStream = null;
		try {
			hmdbXmlStream = WebUtils.getInputStreamFromURL("https://hmdb.ca/metabolites/" + accession + ".xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(hmdbXmlStream == null)
			return false;
		
		Document xmlDocument = XmlUtils.readXmlStream(hmdbXmlStream);
		Element errorElement = xmlDocument.getRootElement().getChild("error");		
		return (errorElement == null);
	}
	
	private static Map<String,String>getAccessionList(Connection conn) throws SQLException{
		
		Map<String,String>accessionList = new TreeMap<String,String>();
		String query = "SELECT ACCESSION, MS_READY_INCHI_KEY FROM COMPOUNDDB.HMDB_COMPOUND_DATA ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			accessionList.put(rs.getString("ACCESSION"), rs.getString("MS_READY_INCHI_KEY"));
		
		rs.close();
		return accessionList;
	}
	
	public static void copyHMDBcompoundToCompounds(String accession) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String compoundCopyQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_DATA "
				+ "(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, SMILES, "
				+ "INCHI, INCHI_KEY, INCHI_KEY2D, CHARGE, MS_READY_MOL_FORMULA, "
				+ "MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, MS_READY_INCHI_KEY2D) "
				+ "(SELECT ACCESSION, 'HMDB', NAME, FORMULA_FROM_SMILES, MASS_FROM_SMILES, "
				+ "SMILES, INCHI, INCHI_KEY_FROM_SMILES, INCHI_KEY_FS_CONNECT, CHARGE, "
				+ "MS_READY_MOL_FORMULA, MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, "
				+ "MS_READY_INCHI_KEY2D FROM COMPOUNDDB.HMDB_COMPOUND_DATA WHERE ACCESSION = ?)";
		PreparedStatement compoundCopyPs = conn.prepareStatement(compoundCopyQuery);
		compoundCopyPs.setString(1, accession);
		compoundCopyPs.executeQuery();		 
		compoundCopyPs.close();
		ConnectionManager.releaseConnection(conn);
	}
}
