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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class NISTIntegration {

	public static void addCrossrefForNISTMSMSBasedOnInchiKeys() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT D.ACCESSION, L.PUBCHEM_ID " +
				"FROM COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA L, " +
				"COMPOUNDDB.COMPOUND_DATA D " +
				"WHERE L.PUBCHEM_ID NOT IN ( " +
				"SELECT SOURCE_DB_ID FROM COMPOUNDDB.COMPOUND_CROSSREF  " +
				"WHERE SOURCE_DB = 'PUBCHEM')  " +
				"AND L.INCHI_KEY = D.INCHI_KEY " + 
				"AND D.SOURCE_DB != 'PUBCHEM'";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Map<String,String>crossrefList = new TreeMap<String,String>();
		while(rs.next()) 
			crossrefList.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		query = "INSERT INTO COMPOUNDDB.COMPOUND_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID ) VALUES (?,?,?)";
		ps = conn.prepareStatement(query);
		ps.setString(2, "PUBCHEM");	
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
	
	public static void addCrossrefForNISTMSMSBasedOnMsReadyInchiKeys() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT D.ACCESSION, L.PUBCHEM_ID " +
				"FROM COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA L, " +
				"COMPOUNDDB.COMPOUND_DATA D " +
				"WHERE L.PUBCHEM_ID NOT IN ( " +
				"SELECT SOURCE_DB_ID FROM COMPOUNDDB.COMPOUND_CROSSREF  " +
				"WHERE SOURCE_DB = 'PUBCHEM')  " +
				"AND L.MS_READY_INCHI_KEY = D.MS_READY_INCHI_KEY " + 
				"AND D.SOURCE_DB != 'PUBCHEM'";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Map<String,String>crossrefList = new TreeMap<String,String>();
		while(rs.next()) 
			crossrefList.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		query = "INSERT INTO COMPOUNDDB.COMPOUND_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID ) VALUES (?,?,?)";
		ps = conn.prepareStatement(query);
		ps.setString(2, "PUBCHEM");	
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
	
	public static void addCrossrefForNISTMSMSBasedOnTautomerInchiKeys() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT D.ACCESSION, L.PUBCHEM_ID " +
				"FROM COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA L,  " +
				"COMPOUNDDB.COMPOUND_DATA D,  " +
				"COMPOUNDDB.COMPOUND_TAUTOMERS_INDIGO T1,  " +
				"COMPOUNDDB.COMPOUND_TAUTOMERS_INDIGO T2  " +
				"WHERE L.PUBCHEM_ID NOT IN (  " +
				"SELECT C.SOURCE_DB_ID FROM COMPOUNDDB.COMPOUND_CROSSREF C  " +
				"WHERE C.SOURCE_DB = 'PUBCHEM')  " +
				"AND T1.ACCESSION = L.PUBCHEM_ID " +
				"AND T2.ACCESSION = D.ACCESSION  " +
				"AND T1.TAUTOMER_INCHI_KEY = T2.TAUTOMER_INCHI_KEY " +
				"AND D.SOURCE_DB != 'PUBCHEM'";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Map<String,String>crossrefList = new TreeMap<String,String>();
		while(rs.next()) 
			crossrefList.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		query = "INSERT INTO COMPOUNDDB.COMPOUND_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID ) VALUES (?,?,?)";
		ps = conn.prepareStatement(query);
		ps.setString(2, "PUBCHEM");	
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
	
	public static void copyNISTMSMSData2compounds() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Set<String>nonReferencedAccessions = getNonReferencedAccessionList(conn);
		
		String compoundCopyQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_DATA "
				+ "(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, SMILES, "
				+ "INCHI_KEY, INCHI_KEY2D, CHARGE, MS_READY_MOL_FORMULA, "
				+ "MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY, "
				+ "MS_READY_INCHI_KEY2D, MS_READY_CHARGE) " +
				"(SELECT PUBCHEM_ID, 'PUBCHEM', NAME,  " +
				"FORMULA_FROM_SMILES, MASS_FROM_SMILES,  " +
				"CANONICAL_SMILES, INCHI_KEY, INCHI_KEY_FS2D, CHARGE,  " +
				"MS_READY_MOL_FORMULA, MS_READY_EXACT_MASS, MS_READY_SMILES, MS_READY_INCHI_KEY,  " +
				"MS_READY_INCHI_KEY2D, MS_READY_CHARGE  " +
				"FROM COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA WHERE PUBCHEM_ID = ? AND ROWNUM = 1) ";
		PreparedStatement compoundCopyPs = conn.prepareStatement(compoundCopyQuery);		
		int counter = 0;
		for(String lmid : nonReferencedAccessions) {
			
			compoundCopyPs.setString(1, lmid);
			compoundCopyPs.executeQuery();
			counter++;
			if(counter % 100 == 0)
				System.out.print(".");
			if(counter % 10000 == 0)
				System.out.print(counter + "\n");
		}
		compoundCopyPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static Set<String>getNonReferencedAccessionList(Connection conn) throws SQLException{
		
		String query = 
				"SELECT DISTINCT PUBCHEM_ID FROM COMPOUNDDB.NIST_UNIQUE_COMPOUND_DATA  " +
				"WHERE PUBCHEM_ID NOT IN ( " +
				"SELECT SOURCE_DB_ID FROM COMPOUNDDB.COMPOUND_CROSSREF  " +
				"WHERE SOURCE_DB = 'PUBCHEM') " +
				"AND PUBCHEM_ID NOT IN ( " +
				"SELECT ACCESSION FROM COMPOUNDDB.COMPOUND_DATA  " +
				"WHERE SOURCE_DB = 'PUBCHEM')";
		PreparedStatement ps = conn.prepareStatement(query);
		
		TreeSet<String>validAccessions = new TreeSet<String>();
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			validAccessions.add(rs.getString(1));
		
		rs.close();
		ps.close();
		
		return validAccessions;
	}
}
