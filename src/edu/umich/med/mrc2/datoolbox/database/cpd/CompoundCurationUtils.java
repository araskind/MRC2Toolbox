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

package edu.umich.med.mrc2.datoolbox.database.cpd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.integrate.HMDBIntegration;

public class CompoundCurationUtils {
	
	public static void setCompoundTautomerGroupCuratedFlag(
			String primaryCompoundAccession, boolean isCurated) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOUNDDB.COMPOUND_GROUP "
				+ "SET CURATED = ? WHERE PRIMARY_ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		if(isCurated)
			ps.setString(1, "Y");
		else
			ps.setNull(1, java.sql.Types.NULL);

		ps.setString(2, primaryCompoundAccession);
		ps.executeQuery();	 
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void removeTautomerFromCompoundGroup(
			String primaryCompoundAccession, String tautomerAccession) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM COMPOUNDDB.COMPOUND_GROUP "
				+ "WHERE PRIMARY_ACCESSION = ? AND SECONDARY_ACCESSION = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, primaryCompoundAccession);
		ps.setString(2, tautomerAccession);
		ps.executeQuery();	 
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addSelectedTautomerAsNewCompound(
			CompoundIdentity primaryCompound, CompoundIdentity tautomer) {
		
		if(primaryCompound == null || tautomer == null)
			return;
		
		if(tautomer.getPrimaryDatabase().equals(CompoundDatabaseEnum.HMDB)) {
			try {
				HMDBIntegration.copyHMDBcompoundToCompounds(tautomer.getPrimaryDatabaseId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			removeTautomerFromCompoundGroup(
					primaryCompound.getPrimaryDatabaseId(), tautomer.getPrimaryDatabaseId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void replacePrimaryCompoundWithSelectedTautomer(
			CompoundIdentity primaryCompound, CompoundIdentity tautomer) {

		if(primaryCompound == null || tautomer == null)
			return;

		try {
			removeCompound(primaryCompound.getPrimaryDatabaseId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(tautomer.getPrimaryDatabase().equals(CompoundDatabaseEnum.HMDB)) {
			try {
				HMDBIntegration.copyHMDBcompoundToCompounds(tautomer.getPrimaryDatabaseId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			replacePrimaryCompoundWithSelectedTautomerInCompoundGroups(
					primaryCompound.getPrimaryDatabaseId(), 
					tautomer.getPrimaryDatabaseId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void replacePrimaryCompoundWithSelectedTautomerInCompoundGroups(
			String oldPrimaryAccession, String newPrimaryAccession) throws Exception{
		
		Map<String,String>accessionDbMap = new TreeMap<String,String>();
		String oldPrimarySourceDb = null; 
		String newPrimarySourceDb = null;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT PRIMARY_ACCESSION, PRIMARY_SOURCE_DB, SECONDARY_ACCESSION, "
				+ "SECONDARY_SOURCE_DB FROM COMPOUNDDB.COMPOUND_GROUP WHERE PRIMARY_ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, oldPrimaryAccession);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			oldPrimarySourceDb = rs.getString("PRIMARY_SOURCE_DB");
			if(rs.getString("SECONDARY_ACCESSION").equals(newPrimaryAccession))
				newPrimarySourceDb = rs.getString("SECONDARY_SOURCE_DB");
			else
				accessionDbMap.put(
						rs.getString("SECONDARY_ACCESSION"), 
						rs.getString("SECONDARY_SOURCE_DB"));
		}
		rs.close(); 
		accessionDbMap.put(
				oldPrimaryAccession, 
				oldPrimarySourceDb);
		query = "DELETE FROM COMPOUNDDB.COMPOUND_GROUP WHERE PRIMARY_ACCESSION = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, oldPrimaryAccession);
		ps.executeUpdate();
		
		String insertGroupQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_GROUP "
				+ "(PRIMARY_ACCESSION, PRIMARY_SOURCE_DB, "
				+ "SECONDARY_ACCESSION, SECONDARY_SOURCE_DB) VALUES(?, ?, ?, ?)";
		PreparedStatement insertGroupPs = conn.prepareStatement(insertGroupQuery);
		insertGroupPs.setString(1, newPrimaryAccession);		
		insertGroupPs.setString(2, newPrimarySourceDb);
		for(Entry<String,String>e : accessionDbMap.entrySet()) {
			insertGroupPs.setString(3, e.getKey());
			insertGroupPs.setString(4, e.getValue());
			insertGroupPs.addBatch();
		}
		insertGroupPs.executeBatch();
		insertGroupPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	
	public static void removeCompound(String compoundAccession) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM COMPOUNDDB.COMPOUND_DATA WHERE ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, compoundAccession);
		ps.executeQuery();	 
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
}


















