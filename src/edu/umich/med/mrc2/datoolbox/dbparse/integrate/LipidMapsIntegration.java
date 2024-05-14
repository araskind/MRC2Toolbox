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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class LipidMapsIntegration {
	
	public static void addCrossrefForLipidMapsBasedOnSupplied() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT C.ACCESSION, L.LMID " +
				"FROM COMPOUNDDB.LIPIDMAPS_CROSSREF L, " +
				"COMPOUNDDB.COMPOUND_CROSSREF C " +
				"WHERE L.SOURCE_DB = C.SOURCE_DB " +
				"AND L.SOURCE_DB_ID = C.SOURCE_DB_ID  " +
				"ORDER BY 1,2 ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Map<String,String>crossrefList = new TreeMap<String,String>();
		while(rs.next()) 
			crossrefList.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		query = "INSERT INTO COMPOUNDDB.COMPOUND_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID ) VALUES (?,?,?)";
		ps = conn.prepareStatement(query);
		ps.setString(2, "LIPIDMAPS");	
		int count = 0;
		for(Entry<String, String> pair : crossrefList.entrySet()) {
			
			ps.setString(1, pair.getKey());
			ps.setString(3, pair.getValue());
			ps.addBatch();
			count++;
			if(count % 100 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addCrossrefForLipidMapsBasedOnHMDBmapping() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT LMID, SOURCE_DB_ID " +
				"FROM COMPOUNDDB.LIPIDMAPS_CROSSREF " +
				"WHERE SOURCE_DB = 'HMDB' " +
				"ORDER BY 1,2 ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		Map<String,String>lipidMapsToHMDBcrossrefList = new TreeMap<String,String>();
		ResultSet rs = ps.executeQuery();		
		while(rs.next()) 
			lipidMapsToHMDBcrossrefList.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		query = 
			"SELECT ACCESSION FROM COMPOUNDDB.COMPOUND_CROSSREF " +
			"WHERE SOURCE_DB = 'LIPIDMAPS' " +
			"AND SOURCE_DB_ID = ? ";
		ps = conn.prepareStatement(query);
		
		String insertQuery = "INSERT INTO COMPOUNDDB.COMPOUND_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID ) VALUES (?,?,?)";
		PreparedStatement insertPs = conn.prepareStatement(insertQuery);		
		insertPs.setString(2, "LIPIDMAPS");	
		
		int count = 0;
		TreeSet<String>existingLipidMapsIds = new TreeSet<String>();
		for(Entry<String, String> pair : lipidMapsToHMDBcrossrefList.entrySet()) {
			
			existingLipidMapsIds.clear();
			ps.setString(1, pair.getKey());
			rs = ps.executeQuery();
			while(rs.next()) 
				existingLipidMapsIds.add(rs.getString(1));
			
			if(existingLipidMapsIds.isEmpty()) {
				
				insertPs.setString(1, pair.getValue());
				insertPs.setString(3, pair.getKey());
				insertPs.addBatch();
				count++;
				if(count % 100 == 0)
					insertPs.executeBatch();
			}
		}
		insertPs.executeBatch();
		insertPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void copyLipidMapsData2compounds() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Map<String,String>accessionKeyList = new TreeMap<String,String>();
		try {
			accessionKeyList = getAccessionList(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String lmCrossrefQuery = 
				"SELECT SOURCE_DB, SOURCE_DB_ID "
				+ "FROM LIPIDMAPS_CROSSREF WHERE LMID = ?";
		PreparedStatement lmCrossrefPs = conn.prepareStatement(lmCrossrefQuery);
		
		String sameCompoundQuery = 
				"SELECT ACCESSION FROM COMPOUNDDB.COMPOUND_TAUTOMERS "
				+ "WHERE SOURCE_DB = 'LIPIDMAPS' AND ACCESSION != ? AND TAUTOMER_INCHI_KEY = ?";
		PreparedStatement sameCompoundPs = conn.prepareStatement(sameCompoundQuery);
		
		String compoundCopyQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_DATA "
				+ "(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, SMILES, "
				+ "INCHI, INCHI_KEY, INCHI_KEY2D, CHARGE, MS_READY_MOL_FORMULA, "
				+ "MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, "
				+ "MS_READY_INCHI_KEY2D, MS_READY_CHARGE) "
				+ "(SELECT ACCESSION, 'LIPIDMAPS', COALESCE ( COMMON_NAME , SYSTEMATIC_NAME ) AS CPD_NAME, "
				+ "FORMULA_FROM_SMILES, MASS_FROM_SMILES, "
				+ "SMILES, INCHI, INCHI_KEY_FROM_SMILES, INCHI_KEY_FS2D, CHARGE, "
				+ "MS_READY_MOL_FORMULA, MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, "
				+ "MS_READY_INCHI_KEY2D, MS_READY_CHARGE "
				+ "FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA WHERE LMID = ?)";
		PreparedStatement compoundCopyPs = conn.prepareStatement(compoundCopyQuery);
		
		sameCompoundPs.close();
		compoundCopyPs.close();
		//	insertGroupPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static Map<String,String>getAccessionList(Connection conn) throws SQLException{
		
		Map<String,String>accessionList = new TreeMap<String,String>();
		String query = "SELECT LMID, MS_READY_INCHI_KEY "
				+ "FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			accessionList.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		return accessionList;
	}
	
	public static Map<CompoundDatabaseEnum,String>getCrossrefsForAccession(
			String primaryAccession) throws Exception{
		
		Map<CompoundDatabaseEnum,String>crossrefData = 
				new TreeMap<CompoundDatabaseEnum,String>();
		
		
		return crossrefData;
	}
}
