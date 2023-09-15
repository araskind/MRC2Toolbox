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
import java.sql.SQLException;
import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IdLevelUtils {
	
	public static Collection<MSFeatureIdentificationLevel> 
			getMSFeatureIdentificationLevelList() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<MSFeatureIdentificationLevel> statusList = 
				getMSFeatureIdentificationLevelList(conn);
		ConnectionManager.releaseConnection(conn);
		return statusList;
	}
	
	private static Collection<MSFeatureIdentificationLevel> 
			getMSFeatureIdentificationLevelList(Connection conn) throws Exception {

		Collection<MSFeatureIdentificationLevel>levelList = 
				new TreeSet<MSFeatureIdentificationLevel>();
		String query =
				"SELECT IDENTIFICATION_LEVEL_ID, NAME, RANK_ORDER, "
				+ "COLOR_CODE, ALLOW_TO_REPLACE_AS_DEFAULT, SHORTCUT, IS_LOCKED "
				+ "FROM IDENTIFICATION_LEVEL ORDER BY RANK_ORDER";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			MSFeatureIdentificationLevel level = new MSFeatureIdentificationLevel(
					rs.getString("IDENTIFICATION_LEVEL_ID"),
					rs.getString("NAME"),
					rs.getInt("RANK_ORDER"),
					rs.getString("COLOR_CODE"),
					rs.getBoolean("ALLOW_TO_REPLACE_AS_DEFAULT"),
					(rs.getString("IS_LOCKED") != null));
			
			level.setShorcut(rs.getString("SHORTCUT"));
			levelList.add(level);
		}
		rs.close();
		ps.close();
		return levelList;
	}

	public static void addNewMSFeatureIdentificationLevel(
			MSFeatureIdentificationLevel newLevel) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_STATUS_SEQ",
				DataPrefix.IDENTIFICATION_LEVEL,
				"0",
				3);
		newLevel.setId(nextId);
		if(newLevel.getRank() == 0) {
			int nextRank = getNextMSFeatureIdentificationLevelRank(conn);
			newLevel.setRank(nextRank);
		}		
		String query =
			"INSERT INTO IDENTIFICATION_LEVEL " + 
			"(IDENTIFICATION_LEVEL_ID, NAME, RANK_ORDER, COLOR_CODE, "
			+ "ALLOW_TO_REPLACE_AS_DEFAULT, SHORTCUT) VALUES(?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newLevel.getId());
		ps.setString(2, newLevel.getName());
		ps.setInt(3, newLevel.getRank());
		ps.setString(4, newLevel.getHexColorCode());
		
		String allowRepDefault = "0";
		if(newLevel.isAllowToReplaceAsDefault())
			allowRepDefault = "1";
		
		ps.setString(5, allowRepDefault);
		
		if(newLevel.getShorcut() != null && !newLevel.getShorcut().isEmpty())
			ps.setString(6, newLevel.getShorcut());
		else
			ps.setNull(6, java.sql.Types.NULL);
		
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editMSFeatureIdentificationLevel(
			MSFeatureIdentificationLevel levelToUpdate) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
				"UPDATE IDENTIFICATION_LEVEL SET NAME = ?, RANK_ORDER = ?, "
				+ "COLOR_CODE = ?, ALLOW_TO_REPLACE_AS_DEFAULT = ?, SHORTCUT = ? "
				+ "WHERE IDENTIFICATION_LEVEL_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, levelToUpdate.getName());
		ps.setInt(2, levelToUpdate.getRank());
		ps.setString(3, levelToUpdate.getHexColorCode());
		String allowRepDefault = "0";
		if(levelToUpdate.isAllowToReplaceAsDefault())
			allowRepDefault = "1";
		
		ps.setString(4, allowRepDefault);
		
		if(levelToUpdate.getShorcut() != null && !levelToUpdate.getShorcut().isEmpty())
			ps.setString(5, levelToUpdate.getShorcut());
		else
			ps.setNull(5, java.sql.Types.NULL);
		
		ps.setString(6, levelToUpdate.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static Integer getNextMSFeatureIdentificationLevelRank(Connection conn) throws SQLException {

		Integer nextRank = null;
		String query = "SELECT MAX(RANK_ORDER) + 10 AS NEW_RANK FROM IDENTIFICATION_LEVEL";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			nextRank = rs.getInt("NEW_RANK");
			break;
		}
		rs.close();
		ps.close();
		return nextRank;
	}

	public static void deleteMSFeatureIdentificationLevel(MSFeatureIdentificationLevel level) throws Exception {
		
		// Clear all references first
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE MSMS_FEATURE_ALTERNATIVE_ID SET IDENTIFICATION_LEVEL_ID = NULL "
				+ "WHERE IDENTIFICATION_LEVEL_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, level.getId());
		ps.executeUpdate();
		
		query = 
			"UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IDENTIFICATION_LEVEL_ID = NULL "
			+ "WHERE IDENTIFICATION_LEVEL_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, level.getId());
		ps.executeUpdate();

		query = 
			"UPDATE POOLED_MS1_FEATURE_ALTERNATIVE_ID SET IDENTIFICATION_LEVEL_ID = NULL "
			+ "WHERE IDENTIFICATION_LEVEL_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, level.getId());
		ps.executeUpdate();
			
		query = 
			"UPDATE POOLED_MS1_FEATURE_LIBRARY_MATCH SET IDENTIFICATION_LEVEL_ID = NULL "
			+ "WHERE IDENTIFICATION_LEVEL_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, level.getId());
		ps.executeUpdate();
		
		//	Delete status
		query = 
			"DELETE FROM IDENTIFICATION_LEVEL WHERE IDENTIFICATION_LEVEL_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, level.getId());
		ps.executeUpdate();
				
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setIdLevelForReferenceMS1FeatureIdentification(
			MsFeatureIdentity newIdentity) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		
		//	TODO deal with unknowns
		if(newIdentity.getCompoundIdentity() == null) {
			
		}
		
		//	Modify manuallu assigned
		if(newIdentity.getIdSource().equals(CompoundIdSource.LIBRARY))
			setIdLevelForReferenceMS1FeatureMSRTLibraryMatch(newIdentity, conn);
		
		//	Modify MSMS library match
		if(newIdentity.getIdSource().equals(CompoundIdSource.MANUAL))
			setIdLevelForReferenceMS1FeatureManualId(newIdentity, conn);
		
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setIdLevelForReferenceMS1FeatureIdentification(
			MsFeatureIdentity newIdentity, Connection conn) throws Exception {

		//	TODO deal with unknowns
		if(newIdentity.getCompoundIdentity() == null) {
			
		}
		
		//	Modify manuallu assigned
		if(newIdentity.getIdSource().equals(CompoundIdSource.LIBRARY))
			setIdLevelForReferenceMS1FeatureMSRTLibraryMatch(newIdentity, conn);
		
		//	Modify MSMS library match
		if(newIdentity.getIdSource().equals(CompoundIdSource.MANUAL))
			setIdLevelForReferenceMS1FeatureManualId(newIdentity, conn);
	}
	
	public static void setIdLevelForReferenceMS1FeatureMSRTLibraryMatch(
			MsFeatureIdentity newIdentity, Connection conn) throws Exception {
		
		String query = "UPDATE POOLED_MS1_FEATURE_LIBRARY_MATCH "
				+ "SET IDENTIFICATION_LEVEL_ID = ? WHERE MATCH_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		MSFeatureIdentificationLevel level = newIdentity.getIdentificationLevel();
		String levelId = null;
		if(level != null)
			levelId = level.getId();
		
		if(levelId != null)
			ps.setString(1, levelId);
		else
			ps.setNull(1, java.sql.Types.NULL);
		
		ps.setString(2, newIdentity.getUniqueId());
		ps.executeUpdate();
		ps.close();		
	}
	
	public static void setIdLevelForReferenceMS1FeatureManualId(
			MsFeatureIdentity newIdentity, Connection conn) throws Exception {
		
		String query = "UPDATE POOLED_MS1_FEATURE_ALTERNATIVE_ID "
				+ "SET IDENTIFICATION_LEVEL_ID = ? WHERE IDENTIFICATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		MSFeatureIdentificationLevel level = newIdentity.getIdentificationLevel();
		String levelId = null;
		if(level != null)
			levelId = level.getId();
		
		if(levelId != null)
			ps.setString(1, levelId);
		else
			ps.setNull(1, java.sql.Types.NULL);
		
		ps.setString(2, newIdentity.getUniqueId());
		ps.executeUpdate();
		ps.close();	
	}
	
	public static void setIdLevelForMSMSFeatureIdentification(
			MsFeatureIdentity newIdentity, String msmsFeatureId) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		
		//	If unknown
		if(newIdentity.getCompoundIdentity() == null) {
			setIdLevelForUnknownMSMSIdentity(newIdentity, msmsFeatureId, conn);
		}
		else {
			//	Modify manually assigned
			if(newIdentity.getIdSource().equals(CompoundIdSource.LIBRARY_MS2))
				setIdLevelForMSMSFeatureLibraryMatch(newIdentity, conn);
			
			//	Modify MSMS library match
			if(newIdentity.getIdSource().equals(CompoundIdSource.MANUAL))
				setIdLevelForMSMSFeatureManualId(newIdentity, conn);
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setIdLevelForMSMSFeatureIdentification(
			MsFeatureIdentity newIdentity, String msmsFeatureId, Connection conn) throws Exception {
		
		//	If unknown
		if(newIdentity.getCompoundIdentity() == null) {
			setIdLevelForUnknownMSMSIdentity(newIdentity, msmsFeatureId, conn);
		}
		else {
			//	Modify manually assigned
			if(newIdentity.getIdSource().equals(CompoundIdSource.LIBRARY_MS2))
				setIdLevelForMSMSFeatureLibraryMatch(newIdentity, conn);
			
			//	Modify MSMS library match
			if(newIdentity.getIdSource().equals(CompoundIdSource.MANUAL))
				setIdLevelForMSMSFeatureManualId(newIdentity, conn);
		}
	}
	
	private static void setIdLevelForUnknownMSMSIdentity(
			MsFeatureIdentity newIdentity, String msmsFeatureId, Connection conn)  throws Exception {
		
		String query = "UPDATE MSMS_FEATURE "
				+ "SET IDENTIFICATION_LEVEL_ID = ? "
				+ "WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		MSFeatureIdentificationLevel level = newIdentity.getIdentificationLevel();
		String levelId = null;
		if(level != null)
			levelId = level.getId();
		
		if(levelId != null)
			ps.setString(1, levelId);
		else
			ps.setNull(1, java.sql.Types.NULL);
		
		ps.setString(2, msmsFeatureId);
		ps.executeUpdate();
		ps.close();		
	}

	public static void setIdLevelForMSMSFeatureLibraryMatch(
			MsFeatureIdentity newIdentity, Connection conn) throws Exception {
		
		String query = "UPDATE MSMS_FEATURE_LIBRARY_MATCH "
				+ "SET IDENTIFICATION_LEVEL_ID = ? WHERE MATCH_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		MSFeatureIdentificationLevel level = newIdentity.getIdentificationLevel();
		String levelId = null;
		if(level != null)
			levelId = level.getId();
		
		if(levelId != null)
			ps.setString(1, levelId);
		else
			ps.setNull(1, java.sql.Types.NULL);
		
		ps.setString(2, newIdentity.getUniqueId());
		ps.executeUpdate();
		ps.close();		
	}
	
	public static void setIdLevelForMSMSFeatureManualId(
			MsFeatureIdentity newIdentity, Connection conn) throws Exception {
		
		String query = "UPDATE MSMS_FEATURE_ALTERNATIVE_ID "
				+ "SET IDENTIFICATION_LEVEL_ID = ? WHERE IDENTIFICATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		MSFeatureIdentificationLevel level = newIdentity.getIdentificationLevel();
		String levelId = null;
		if(level != null)
			levelId = level.getId();
		
		if(levelId != null)
			ps.setString(1, levelId);
		else
			ps.setNull(1, java.sql.Types.NULL);
		
		ps.setString(2, newIdentity.getUniqueId());
		ps.executeUpdate();
		ps.close();	
	}
}






















