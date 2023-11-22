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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class FeatureLookupDataSetUtils {

	
	public static void addFeatureLookupDataSet(
			FeatureLookupDataSet newDataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		addFeatureLookupDataSet(newDataSet, conn);
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void addFeatureLookupDataSet(
			FeatureLookupDataSet dataSet, Connection conn) throws Exception {
		
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"FEATURE_LOOKUP_DATA_SET_SEQ",
				DataPrefix.LOOKUP_FEATURE_DATA_SET,
				"0",
				6);
		dataSet.setId(newId);

		String query = 
			"INSERT INTO FEATURE_LOOKUP_DATA_SET " +
			"(FLDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, LAST_MODIFIED) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, dataSet.getId());
		ps.setString(2, dataSet.getName());
		if(dataSet.getDescription() != null)
			ps.setString(3, dataSet.getDescription());
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		ps.setString(4, dataSet.getCreatedBy().getId());
		ps.setTimestamp(5, new java.sql.Timestamp(
				dataSet.getDateCreated().getTime()));
		ps.setTimestamp(6, new java.sql.Timestamp(
				dataSet.getLastModified().getTime()));	
		ps.executeUpdate();
		
		//	Add features
		query = "INSERT INTO FEATURE_LOOKUP_DATA_SET_COMPONENT "
				+ "(COMPONENT_ID, FLDS_ID, NAME, MZ, RT, RANK, "
				+ "SMILES, INCHI_KEY, FOLD_CHANGE, P_VALUE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		ps = conn.prepareStatement(query);
		ps.setString(2, dataSet.getId());
		int counter = 0;
		for(MinimalMSOneFeature f : dataSet.getFeatures()) {
			
			String fId = SQLUtils.getNextIdFromSequence(conn, 
					"FEATURE_LOOKUP_DATA_SET_COMPONENT_SEQ",
					DataPrefix.LOOKUP_FEATURE,
					"0",
					12);
			f.setId(fId);
			ps.setString(1, fId);
			ps.setString(3, f.getName());
			ps.setDouble(4, f.getMz());
			ps.setDouble(5, f.getRt());
			ps.setDouble(6, f.getRank());
			
			if(f.getSmiles() != null)
				ps.setString(7, f.getSmiles());
			else
				ps.setNull(7,  java.sql.Types.NULL);
			
			if(f.getInchiKey() != null)
				ps.setString(8, f.getInchiKey());
			else
				ps.setNull(8,  java.sql.Types.NULL);
			
			ps.setDouble(9, f.getFoldChange());
			ps.setDouble(10, f.getpValue());
			ps.addBatch();
			counter++;
			
			if(counter % 100 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		ps.close();		
	}
	
	public static void editFeatureLookupDataSetMetadata(
			FeatureLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE FEATURE_LOOKUP_DATA_SET " +
				"SET NAME = ?, DESCRIPTION = ?, LAST_MODIFIED = ? "
				+ "WHERE FLDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, dataSet.getName());
		ps.setString(2, dataSet.getDescription());
		ps.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
		ps.setString(4, dataSet.getId());	
		ps.executeUpdate();
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void deleteFeatureLookupDataSet(
			FeatureLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM FEATURE_LOOKUP_DATA_SET WHERE FLDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, dataSet.getId());	
		ps.executeUpdate();
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void addFeaturesToFeatureLookupDataSet(
			FeatureLookupDataSet dataSet, Collection<MinimalMSOneFeature>featuresToAdd) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = "INSERT INTO FEATURE_LOOKUP_DATA_SET_COMPONENT "
				+ "(COMPONENT_ID, FLDS_ID, NAME, MZ, RT, RANK, "
				+ "SMILES, INCHI_KEY, FOLD_CHANGE, P_VALUE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(2, dataSet.getId());
		int counter = 0;
		for(MinimalMSOneFeature f : featuresToAdd) {
			
			String fId = SQLUtils.getNextIdFromSequence(conn, 
					"FEATURE_LOOKUP_DATA_SET_COMPONENT_SEQ",
					DataPrefix.LOOKUP_FEATURE,
					"0",
					12);
			f.setId(fId);
			ps.setString(1, fId);
			ps.setString(3, f.getName());
			ps.setDouble(4, f.getMz());
			ps.setDouble(5, f.getRt());
			ps.setDouble(6, f.getRank());
			
			if(f.getSmiles() != null)
				ps.setString(7, f.getSmiles());
			else
				ps.setNull(7,  java.sql.Types.NULL);
			
			if(f.getInchiKey() != null)
				ps.setString(8, f.getInchiKey());
			else
				ps.setNull(8,  java.sql.Types.NULL);
			
			ps.setDouble(9, f.getFoldChange());
			ps.setDouble(10, f.getpValue());
			ps.addBatch();
			counter++;
			
			if(counter % 100 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		ps.close();	
		dataSet.getFeatures().addAll(featuresToAdd);
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void removeFeaturesFromFeatureLookupDataSet(
			FeatureLookupDataSet dataSet, Collection<MinimalMSOneFeature>featuresToRemove) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM FEATURE_LOOKUP_DATA_SET_COMPONENT WHERE COMPONENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		int counter = 0;
		for(MinimalMSOneFeature f : featuresToRemove) {
			
			ps.setString(1, f.getId());
			ps.addBatch();
			counter++;
			
			if(counter % 100 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		ps.close();	
		dataSet.getFeatures().removeAll(featuresToRemove);
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static Collection<FeatureLookupDataSet>getFeatureLookupDataSetList() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<FeatureLookupDataSet>dataSets = getFeatureLookupDataSetList(conn);
		ConnectionManager.releaseConnection(conn);	
		return dataSets;
	}
	
	public static Collection<FeatureLookupDataSet>getFeatureLookupDataSetList(Connection conn) throws Exception {
	
		Collection<FeatureLookupDataSet>dataSets = new TreeSet<FeatureLookupDataSet>();
		String query = 
				"SELECT FLDS_ID, NAME, DESCRIPTION, CREATED_BY, "
				+ "DATE_CREATED, LAST_MODIFIED "
				+ "FROM FEATURE_LOOKUP_DATA_SET ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			FeatureLookupDataSet ds = new FeatureLookupDataSet(
					rs.getString("FLDS_ID"), 
					rs.getString("NAME"), 
					rs.getString("DESCRIPTION"), 
					IDTDataCache.getUserById(rs.getString("CREATED_BY")), 
					new Date(rs.getTimestamp("DATE_CREATED").getTime()),
					new Date(rs.getTimestamp("LAST_MODIFIED").getTime()));
			
//			getFeaturesForFeatureLookupDataSet(ds, conn);			
			dataSets.add(ds);
		}
		rs.close();
		ps.close();
		return dataSets;
	}
	
	public static void getFeaturesForFeatureLookupDataSet(
			FeatureLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		getFeaturesForFeatureLookupDataSet(dataSet, conn);
		ConnectionManager.releaseConnection(conn);	

	}
	
	public static void getFeaturesForFeatureLookupDataSet(
			FeatureLookupDataSet dataSet, Connection conn) throws Exception {
		
		String query = 
				"SELECT COMPONENT_ID, NAME, MZ, RT, RANK, "
				+ "SMILES, INCHI_KEY, FOLD_CHANGE, P_VALUE "
				+ "FROM FEATURE_LOOKUP_DATA_SET_COMPONENT "
				+ "WHERE FLDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);	
		ps.setString(1, dataSet.getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			MinimalMSOneFeature feature = 
					new MinimalMSOneFeature(
							rs.getString("COMPONENT_ID"), 
							rs.getString("NAME"), 
							rs.getDouble("MZ"), 
							rs.getDouble("RT"), 
							rs.getDouble("RANK"),
							rs.getString("SMILES"),
							rs.getString("INCHI_KEY"));	
			
			feature.setFoldChange(rs.getDouble("FOLD_CHANGE"));
			feature.setpValue(rs.getDouble("P_VALUE"));
			dataSet.getFeatures().add(feature);
		}
		rs.close();
		ps.close();
	}
}
