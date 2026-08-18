/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class FeatureCollectionUtils {
	
	private static final Logger logger = LogManager.getLogger(FeatureCollectionUtils.class);

	private FeatureCollectionUtils() {
		/* This utility class should not be instantiated */
	}

	public static Set<MsFeatureInfoBundleCollection>
			getMsFeatureInformationBundleCollections() throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		Set<MsFeatureInfoBundleCollection> featureCollectionsSet = 
				getMsFeatureInformationBundleCollections(conn);
		ConnectionManager.releaseConnection(conn);
		return featureCollectionsSet;
	}
	
	private static Set<MsFeatureInfoBundleCollection> 
			getMsFeatureInformationBundleCollections(Connection conn) throws SQLException {

		Set<MsFeatureInfoBundleCollection>featureCollectionsSet = 
				new TreeSet<MsFeatureInfoBundleCollection>(
						new MsFeatureInfoBundleCollectionComparator(SortProperty.Name));
		String query =
				"SELECT C.COLLECTION_ID, C.COLLECTION_NAME, C.DESCRIPTION, "
				+ "C.OWNER, C.DATE_CREATED, C.DATE_MODIFIED, COUNT(O.MS_FEATURE_ID) AS COLLECTION_SIZE "
				+ "FROM MSMS_FEATURE_COLLECTION C "
				+ "LEFT JOIN MSMS_FEATURE_COLLECTION_COMPONENT O "
				+ "ON C.COLLECTION_ID = O.COLLECTION_ID "
				+ "GROUP BY C.COLLECTION_ID, C.COLLECTION_NAME, "
				+ "C.DESCRIPTION, C.OWNER, C.DATE_CREATED, C.DATE_MODIFIED "
				+ "ORDER BY C.COLLECTION_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				LIMSUser owner = null;
				if(rs.getString("OWNER") != null)
					owner = IDTDataCache.getUserById(rs.getString("OWNER"));
				
				MsFeatureInfoBundleCollection newCollection = 
						new MsFeatureInfoBundleCollection(
							rs.getString("COLLECTION_ID"),
							rs.getString("COLLECTION_NAME"),
							rs.getString("DESCRIPTION"),
							new Date(rs.getDate("DATE_CREATED").getTime()),
							new Date(rs.getDate("DATE_MODIFIED").getTime()),
							owner);
				newCollection.setCollectionSize(rs.getInt("COLLECTION_SIZE"));
			
				featureCollectionsSet.add(newCollection);
			}
			rs.close();
		}
		return featureCollectionsSet;
	}
	
	public static Set<String>getFeatureIdsForMsFeatureInfoBundleCollection(
			String collectionId) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		Set<String>idSet = getFeatureIdsForMsFeatureInfoBundleCollection(collectionId, conn);
		ConnectionManager.releaseConnection(conn);
		return idSet;
	}
	
	public static Set<String>getFeatureIdsForMsFeatureInfoBundleCollection(
			String collectionId, Connection conn) throws SQLException {
		
		Set<String>featureIdSet = new TreeSet<String>();
		String compQuery = 
				"SELECT MS_FEATURE_ID FROM MSMS_FEATURE_COLLECTION_COMPONENT "
				+ "WHERE COLLECTION_ID = ?";
		
		try(PreparedStatement compPs = conn.prepareStatement(compQuery)){
			compPs.setString(1, collectionId);
			ResultSet compRs = compPs.executeQuery();
			while(compRs.next())
				featureIdSet.add(compRs.getString("MS_FEATURE_ID"));
			
			compRs.close();	
		}
		return featureIdSet;
	}
	
	public static String addNewMsFeatureInformationBundleCollection(
			MsFeatureInfoBundleCollection newCollection) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String newId = addNewMsFeatureInformationBundleCollection(newCollection, conn);
		ConnectionManager.releaseConnection(conn);
		return newId;
	}
	
	public static String addNewMsFeatureInformationBundleCollection(
			MsFeatureInfoBundleCollection newCollection, Connection conn) throws SQLException {
		
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_FEATURE_COLLECTION_SEQ",
				DataPrefix.MSMS_FEATURE_COLLECTION,
				"0",
				6);
		newCollection.setId(nextId);
		
		String query =
				"INSERT INTO MSMS_FEATURE_COLLECTION ("
				+ "COLLECTION_ID, COLLECTION_NAME, DESCRIPTION, "
				+ "OWNER, DATE_CREATED, DATE_MODIFIED) "
				+ "VALUES (?,?,?,?,?,?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, newCollection.getId());
			ps.setString(2, newCollection.getName());
			ps.setString(3, newCollection.getDescription());
			ps.setString(4, MRC2ToolBoxCore.getIdTrackerUser().getId());
			ps.setTimestamp(5, new Timestamp(newCollection.getDateCreated().getTime()));
			ps.setTimestamp(6, new Timestamp(newCollection.getLastModified().getTime()));		
			ps.executeUpdate();
		}		
		if(!newCollection.getFeatures().isEmpty()) {
			
			query = "INSERT INTO MSMS_FEATURE_COLLECTION_COMPONENT ("
					+ "COLLECTION_ID, MS_FEATURE_ID) "
					+ "VALUES (?,?)";
			try(PreparedStatement ps = conn.prepareStatement(query)){
				ps.setString(1, nextId);
				for(MSFeatureInfoBundle feature : newCollection.getFeatures()) {
					ps.setString(2, feature.getMSFeatureId());
					ps.addBatch();
				}
				ps.executeBatch();
			}	
		}		
		return nextId;
	}
	
	public static void deleteMsFeatureInformationBundleCollection(
			MsFeatureInfoBundleCollection toDelete) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String query =
				"DELETE FROM MSMS_FEATURE_COLLECTION WHERE COLLECTION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, toDelete.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateMsFeatureInformationBundleCollectionMetadata(
			MsFeatureInfoBundleCollection toUpdate) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		updateMsFeatureInformationBundleCollectionMetadata(toUpdate, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateMsFeatureInformationBundleCollectionMetadata(
			MsFeatureInfoBundleCollection toUpdate, Connection conn) throws SQLException {
		
		String query =
				"UPDATE MSMS_FEATURE_COLLECTION "
				+ "SET COLLECTION_NAME = ?, DESCRIPTION = ?, DATE_MODIFIED = ? "
				+ "WHERE COLLECTION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, toUpdate.getName());
			ps.setString(2, toUpdate.getDescription());
			ps.setTimestamp(3, new Timestamp(new Date().getTime()));
			ps.setString(4, toUpdate.getId());
			ps.executeUpdate();
		}
	}
	
	public static void addFeaturesToCollection(
			String collectionId, 
			Set<String>featureIdsToAdd) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		addFeaturesToCollection(collectionId, featureIdsToAdd, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addFeaturesToCollection(
			String collectionId, 
			Set<String>featureIdsToAdd,
			Connection conn) throws SQLException {

		if(collectionId == null || featureIdsToAdd.isEmpty())
			return;
		
		String 	query = 
				"INSERT INTO MSMS_FEATURE_COLLECTION_COMPONENT "
				+ "(COLLECTION_ID, MS_FEATURE_ID) VALUES (?,?)";		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, collectionId);		
			for(String id : featureIdsToAdd) {
				ps.setString(2, id);
				ps.addBatch();
			}
			ps.executeBatch();
		}	
		query =
			"UPDATE MSMS_FEATURE_COLLECTION " +
			"SET DATE_MODIFIED = ? WHERE COLLECTION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setString(2, collectionId);	
			ps.executeUpdate();
		}	
	}
	
	public static void removeFeaturesFromCollection(
			String collectionId, 
			Set<String>featureIdsToRemove) throws SQLException {

		if(collectionId == null || featureIdsToRemove.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		String 	query = 
				"DELETE FROM MSMS_FEATURE_COLLECTION_COMPONENT "
				+ "WHERE COLLECTION_ID = ? AND MS_FEATURE_ID = ?";		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, collectionId);		
			for(String id : featureIdsToRemove) {
				ps.setString(2, id);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		query =
			"UPDATE MSMS_FEATURE_COLLECTION " +
			"SET DATE_MODIFIED = ? WHERE COLLECTION_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setString(2, collectionId);		
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
    
	public static Set<String>validateMSMSIDlist(Set<String>idsToValidate) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		Set<String>validIds = validateMSMSIDlist(idsToValidate, conn);
		ConnectionManager.releaseConnection(conn);
		return validIds;
	}
	
	public static Set<String>validateMSMSIDlist(Set<String>idsToValidate, Connection conn) throws SQLException {

		Set<String>validIds = new TreeSet<String>();
		String query = "SELECT PARENT_FEATURE_ID FROM MSMS_FEATURE WHERE MSMS_FEATURE_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(String id : idsToValidate) {
				
				ps.setString(1, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
					validIds.add(rs.getString("PARENT_FEATURE_ID"));
				
				rs.close();
			}
		}
		return validIds;
	}
}















