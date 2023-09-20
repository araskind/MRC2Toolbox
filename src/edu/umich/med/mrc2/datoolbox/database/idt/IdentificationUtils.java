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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IdentificationUtils {
	
	/*
	 * MS1 features library matches
	 */

	public static void addReferenceMS1FeatureLibraryMatch(
			String featureId, MsFeatureIdentity newIdentity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		addReferenceMSFeatureLibraryMatch(featureId, newIdentity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void addReferenceMSFeatureLibraryMatch(
			String featureId, MsFeatureIdentity newIdentity, Connection conn) throws Exception {

		String matchId = SQLUtils.getNextIdFromSequence(conn, 
				"MSRT_LIB_MATCH_SEQ",
				DataPrefix.MSRT_LIBRARY_MATCH,
				"0",
				15);
		newIdentity.setUniqueId(matchId);
		String query =
			"INSERT INTO POOLED_MS1_FEATURE_LIBRARY_MATCH "
			+ "(POOLED_MS_FEATURE_ID, ACCESSION, LIBRARY_ENTRY_ID, ADDUCT_ID, COMPOSITE_ADDUCT_ID, "
			+ "IDENTIFICATION_CONFIDENCE, ID_SOURCE, MATCH_SCORE, IS_PRIMARY, "
			+ "IDENTIFICATION_LEVEL_ID, MATCH_ID) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.setString(2, newIdentity.getCompoundIdentity().getPrimaryDatabaseId());
		String libraryTargetId = null;
		if(newIdentity.getMsRtLibraryMatch() != null) {

			libraryTargetId = newIdentity.getMsRtLibraryMatch().getLibraryTargetId();
//			AdductMatch adductMatch = newIdentity.getMsRtLibraryMatch().getTopAdductMatch();
//			if(adductMatch != null)
//				adductName = adductMatch.getLibraryMatch().getName();
		}
		ps.setString(3, libraryTargetId);
		
		String adductId = null;
		String compositeAdductId = null;
		Adduct adduct = newIdentity.getPrimaryAdduct();
		if(adduct != null) {

			if(adduct instanceof SimpleAdduct)
				adductId = adduct.getId();

			if(adduct instanceof CompositeAdduct)
				compositeAdductId = adduct.getId();
		}
		ps.setString(4, adductId);
		ps.setString(5, compositeAdductId);		
		
		ps.setInt(6, newIdentity.getConfidenceLevel().getLevel());
		ps.setString(7, newIdentity.getIdSource().name());
		ps.setDouble(8, newIdentity.getScore());
		String primary = null;
		if(newIdentity.isPrimary())
			primary = "Y";

		ps.setString(9, primary);
		ps.setString(10, newIdentity.getIdentificationLevel().getId());
		ps.setString(11, newIdentity.getUniqueId());
		ps.executeUpdate();
		ps.close();
	}

	public static Collection<MsFeatureIdentity>getReferenceMS1FeatureLibraryMatches(String featureId) throws Exception {

		Connection msconn = ConnectionManager.getConnection();

		Collection<MsFeatureIdentity> ids =
				getReferenceMS1FeatureLibraryMatches(featureId, msconn);
		ConnectionManager.releaseConnection(msconn);
		return ids;
	}

	public static Collection<MsFeatureIdentity>getReferenceMS1FeatureLibraryMatches(
			String featureId, Connection conn) throws Exception {

		Collection<MsFeatureIdentity>featureIdentities = new ArrayList<MsFeatureIdentity>();
		String query =
			"SELECT I.ACCESSION, I.LIBRARY_ENTRY_ID, I.ADDUCT_ID, I.COMPOSITE_ADDUCT_ID,  " +
			"I.IDENTIFICATION_CONFIDENCE, I.ID_SOURCE, I.MATCH_SCORE,  " +
			"I.IS_PRIMARY, I.IDENTIFICATION_LEVEL_ID, I.MATCH_ID, C.RETENTION_TIME " +
			"FROM POOLED_MS1_FEATURE_LIBRARY_MATCH I " +
			"LEFT JOIN MS_LIBRARY_COMPONENT C ON I.LIBRARY_ENTRY_ID = C.TARGET_ID " +
			"WHERE I.POOLED_MS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			CompoundIdentity compoundIdentity =
					CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
			CompoundIdentificationConfidence confidenceLevel =
					CompoundIdentificationConfidence.getLevelByNumber(rs.getInt("IDENTIFICATION_CONFIDENCE"));
			MsFeatureIdentity id = new MsFeatureIdentity(compoundIdentity, confidenceLevel);
			id.setIdSource(CompoundIdSource.getIdSourceByName(rs.getString("ID_SOURCE")));
			MsRtLibraryMatch match = new MsRtLibraryMatch(rs.getString("LIBRARY_ENTRY_ID"));
			match.setExpectedRetention(rs.getDouble("RETENTION_TIME"));
			match.setScore(rs.getDouble("MATCH_SCORE"));
			String adductName = rs.getString("ADDUCT_NAME");
			if(adductName != null) {
				Adduct adduct = AdductManager.getAdductByName(adductName);
				if(adduct != null)
					match.getAdductScoreMap().add(new AdductMatch(adduct, adduct, match.getScore()));
			}
			if(rs.getString("IS_PRIMARY") != null)
				id.setPrimary(true);
			
			String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
			if(statusId != null) 
				id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));			

			id.setUniqueId(rs.getString("MATCH_ID"));
			id.setMsRtLibraryMatch(match);
			
			String adductId = rs.getString("ADDUCT_ID");
			if(adductId == null)
				adductId = rs.getString("COMPOSITE_ADDUCT_ID");

			if(adductId != null)
				id.setPrimaryAdduct(AdductManager.getAdductById(adductId));
			
			featureIdentities.add(id);
		}
		rs.close();
		ps.close();
		return featureIdentities;
	}

	public static void removeReferenceMS1FeatureLibraryMatch(
			MsFeatureIdentity identity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		removeReferenceMS1FeatureLibraryMatch(identity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void removeReferenceMS1FeatureLibraryMatch(
			MsFeatureIdentity identity, Connection conn) throws Exception {

		String query = "DELETE FROM POOLED_MS1_FEATURE_LIBRARY_MATCH WHERE MATCH_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, identity.getUniqueId());
		ps.executeUpdate();
		ps.close();
	}

	public static void clearReferenceMS1FeatureLibraryMatches(String featureId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		clearReferenceMS1FeatureLibraryMatches(featureId, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void clearReferenceMS1FeatureLibraryMatches(
			String featureId, Connection conn) throws Exception {

		String query = "DELETE FROM POOLED_MS1_FEATURE_LIBRARY_MATCH WHERE POOLED_MS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}
	
	/*
	 *	MS1 features manual identifications
	 */
	
	public static void addReferenceMS1FeatureManualId(
			String featureId, MsFeatureIdentity newIdentity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		addReferenceMS1FeatureManualId(featureId, newIdentity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void addReferenceMS1FeatureManualId(
			String featureId, MsFeatureIdentity newIdentity, Connection conn) throws Exception {

		String query =
			"INSERT INTO POOLED_MS1_FEATURE_ALTERNATIVE_ID (IDENTIFICATION_ID, ACCESSION, ID_CONFIDENCE, "
			+ "IS_PRIMARY, POOLED_MS_FEATURE_ID, ID_SOURCE, ASSIGNED_BY, ASSIGNED_ON, "
			+ "IDENTIFICATION_LEVEL_ID, ADDUCT_ID, COMPOSITE_ADDUCT_ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		String identId = SQLUtils.getNextIdFromSequence(conn, 
				"ALTERNATIVE_ID_SEQ",
				DataPrefix.MS_MSMS_MANUAL_MATCH,
				"0",
				15);
		newIdentity.setUniqueId(identId);
		String isPrimary = null;
		if(newIdentity.isPrimary()) {
			isPrimary = "Y";
			disableReferenceMS1FeaturePrimaryIdentity(featureId, conn);
		}
		ps.setString(1, identId);
		ps.setString(2, newIdentity.getCompoundIdentity().getPrimaryDatabaseId());
		ps.setString(3, newIdentity.getConfidenceLevel().getLevelId());
		ps.setString(4, isPrimary);
		ps.setString(5, featureId);		
		ps.setString(6, newIdentity.getIdSource().name());		
		String userId = null;
		if(newIdentity.getAssignedBy() != null)
			userId = newIdentity.getAssignedBy().getId();
			
		ps.setString(7, userId);
		ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
		
		ps.setString(9, newIdentity.getIdentificationLevel().getId());		
		String adductId = null;
		String compositeAdductId = null;
		Adduct adduct = newIdentity.getPrimaryAdduct();
		if(adduct != null) {

			if(adduct instanceof SimpleAdduct)
				adductId = adduct.getId();

			if(adduct instanceof CompositeAdduct)
				compositeAdductId = adduct.getId();
		}
		ps.setString(10, adductId);
		ps.setString(11, compositeAdductId);
		
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE POOLED_MS1_FEATURE SET ID_DISABLED = NULL WHERE POOLED_MS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}

	public static Collection<MsFeatureIdentity>getReferenceMS1FeatureManualIds(String featureId) throws Exception {

		Connection msconn = ConnectionManager.getConnection();
		Collection<MsFeatureIdentity> ids =
				getReferenceMS1FeatureManualIds(featureId, msconn);
		ConnectionManager.releaseConnection(msconn);
		return ids;
	}

	public static Collection<MsFeatureIdentity>getReferenceMS1FeatureManualIds(
			String featureId, Connection conn) throws Exception {

		Collection<MsFeatureIdentity>featureIdentities = new ArrayList<MsFeatureIdentity>();
		String query =
			"SELECT IDENTIFICATION_ID, ACCESSION, ID_CONFIDENCE, IS_PRIMARY, ID_SOURCE, "
			+ "IDENTIFICATION_LEVEL_ID, ASSIGNED_BY, ASSIGNED_ON, ADDUCT_ID, COMPOSITE_ADDUCT_ID " +
			"FROM POOLED_MS1_FEATURE_ALTERNATIVE_ID WHERE POOLED_MS_FEATURE_ID = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			CompoundIdentity compoundIdentity =
					CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
			CompoundIdentificationConfidence confidenceLevel =
					CompoundIdentificationConfidence.getLevelById(rs.getString("ID_CONFIDENCE"));
			MsFeatureIdentity id = new MsFeatureIdentity(compoundIdentity, confidenceLevel);
			id.setIdSource(CompoundIdSource.getIdSourceByName(rs.getString("ID_SOURCE")));
			if(rs.getString("IS_PRIMARY") != null)
				id.setPrimary(true);

			id.setUniqueId(rs.getString("IDENTIFICATION_ID"));
			LIMSUser assignedBy = IDTDataCache.getUserById(rs.getString("ID_SOURCE"));
			id.setAssignedBy(assignedBy);
			id.setAssignedOn(new Date(rs.getDate("ASSIGNED_ON").getTime()));
			String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
			if(statusId != null) 
				id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
			
			String adductId = rs.getString("ADDUCT_ID");
			if(adductId == null)
				adductId = rs.getString("COMPOSITE_ADDUCT_ID");

			if(adductId != null)
				id.setPrimaryAdduct(AdductManager.getAdductById(adductId));
			
			featureIdentities.add(id);
		}
		rs.close();
		ps.close();
		return featureIdentities;
	}

	public static void removeReferenceMS1FeatureManualId(MsFeatureIdentity identity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		removeReferenceMS1FeatureManualId(identity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void removeReferenceMS1FeatureManualId(
			MsFeatureIdentity identity, Connection conn) throws Exception {

		String query = "DELETE FROM POOLED_MS1_FEATURE_ALTERNATIVE_ID WHERE IDENTIFICATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, identity.getUniqueId());
		ps.executeUpdate();
		ps.close();
	}
	
	public static void clearReferenceMS1FeatureManualIds(String featureId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		clearReferenceMS1FeatureManualIds(featureId, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void clearReferenceMS1FeatureManualIds(
			String featureId, Connection conn) throws Exception {

		String query = "DELETE FROM POOLED_MS1_FEATURE_ALTERNATIVE_ID WHERE POOLED_MS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 *	MS1 features primary ID
	 */

	public static void setReferenceMS1FeaturePrimaryIdentity(
			String featureId, MsFeatureIdentity identity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		setReferenceMS1FeaturePrimaryIdentity(featureId, identity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void setReferenceMS1FeaturePrimaryIdentity(
			String featureId, MsFeatureIdentity newIdentity, Connection conn) throws Exception {
		
		if(newIdentity == null || newIdentity.getCompoundIdentity() == null) {
			disableReferenceMS1FeaturePrimaryIdentity(featureId, conn);
			return;
		}
		String query = "UPDATE POOLED_MS1_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = NULL WHERE POOLED_MS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE POOLED_MS1_FEATURE_ALTERNATIVE_ID SET IS_PRIMARY = NULL WHERE POOLED_MS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();

		if(newIdentity.getMsRtLibraryMatch() != null) {
			
			query =
				"UPDATE POOLED_MS1_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = 'Y' "
				+ "WHERE MATCH_ID = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, newIdentity.getUniqueId());
			ps.executeUpdate();
			ps.close();
		} 
		else {
			query =
				"UPDATE POOLED_MS1_FEATURE_ALTERNATIVE_ID SET IS_PRIMARY = 'Y' "
				+ "WHERE IDENTIFICATION_ID = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, newIdentity.getUniqueId());
			ps.executeUpdate();
			ps.close();
		}
		//	Clear ID disabled flag
		query = "UPDATE POOLED_MS1_FEATURE SET ID_DISABLED = NULL WHERE POOLED_MS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}
	
	public static void disableReferenceMS1FeaturePrimaryIdentity(String featureId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		disableReferenceMS1FeaturePrimaryIdentity(featureId, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void disableReferenceMS1FeaturePrimaryIdentity(
			String featureId, Connection conn) throws Exception {

		String query = "UPDATE POOLED_MS1_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = NULL WHERE POOLED_MS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE POOLED_MS1_FEATURE_ALTERNATIVE_ID SET IS_PRIMARY = NULL WHERE POOLED_MS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE POOLED_MS1_FEATURE SET ID_DISABLED = 'Y' WHERE POOLED_MS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}
	/*
	 * MSMS feature library matches
	 */
	public static Collection<MsFeatureIdentity>getMSMSFeatureLibraryMatches(			
			String featureId) throws Exception {

		Connection msconn = ConnectionManager.getConnection();
		Collection<MsFeatureIdentity> ids =
				getMSMSFeatureLibraryMatches(featureId, msconn);
		ConnectionManager.releaseConnection(msconn);
		return ids;
	}
	
	public static Collection<MsFeatureIdentity>getMSMSFeatureLibraryMatches(
			String msmsFeatureId, Connection conn) throws Exception {
		
		Collection<MsFeatureIdentity>featureIdentities = new ArrayList<MsFeatureIdentity>();
		String query =
			"SELECT MATCH_ID, MRC2_LIB_ID, MATCH_SCORE, FWD_SCORE, REVERSE_SCORE, " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, IS_PRIMARY, IDENTIFICATION_LEVEL_ID, " +
			"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH " +
			"FROM MSMS_FEATURE_LIBRARY_MATCH M " +
			"WHERE MSMS_FEATURE_ID = ? ";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msmsFeatureId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			MsMsLibraryFeature msmsId =
					MSMSLibraryUtils.getMsMsLibraryFeatureById(
							rs.getString("MRC2_LIB_ID"), conn);
			if(msmsId == null)
				continue;

			MsFeatureIdentity id = new MsFeatureIdentity(msmsId.getCompoundIdentity(),
					CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
			id.setIdSource(CompoundIdSource.LIBRARY_MS2);
			id.setUniqueId(rs.getString("MATCH_ID"));
			MSMSMatchType matchType = 
					MSMSMatchType.getMSMSMatchTypeByName(rs.getString("MATCH_TYPE"));
			
			ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
					msmsId,
					rs.getDouble("MATCH_SCORE"), 
					rs.getDouble("FWD_SCORE"),
					rs.getDouble("REVERSE_SCORE"), 
					rs.getDouble("PROBABILITY"), 
					rs.getDouble("DOT_PRODUCT"), 
					rs.getDouble("REVERSE_DOT_PRODUCT"),
					rs.getDouble("HYBRID_DOT_PRODUCT"),
					rs.getDouble("HYBRID_SCORE"),
					rs.getDouble("HYBRID_DELTA_MZ"),
					matchType,
					rs.getString("DECOY_MATCH") != null,
					rs.getString("SEARCH_PARAMETER_SET_ID"));
			
			id.setReferenceMsMsLibraryMatch(match);
			if(rs.getString("IS_PRIMARY") != null)
				id.setPrimary(true);

			String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
			if(statusId != null) 
				id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
			
			featureIdentities.add(id);
		}
		rs.close();
		ps.close();
		return featureIdentities;
	}
	
	public static Collection<MsFeatureIdentity>getMSMSFeatureLibraryMatchesForLibraryId(
			String msmsFeatureId, String msmsLibraryFeatureId, Connection conn) throws Exception {
		
		Collection<MsFeatureIdentity>featureIdentities = new ArrayList<MsFeatureIdentity>();
		String query =
			"SELECT MATCH_ID, MATCH_SCORE, FWD_SCORE, REVERSE_SCORE, " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, IS_PRIMARY, IDENTIFICATION_LEVEL_ID, " +
			"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH " +
			"FROM MSMS_FEATURE_LIBRARY_MATCH M " +
			"WHERE MSMS_FEATURE_ID = ? AND MRC2_LIB_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msmsFeatureId);
		ps.setString(2, msmsLibraryFeatureId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			MsMsLibraryFeature msmsId =
					MSMSLibraryUtils.getMsMsLibraryFeatureById(msmsLibraryFeatureId, conn);
			if(msmsId == null)
				continue;

			MsFeatureIdentity id = new MsFeatureIdentity(msmsId.getCompoundIdentity(),
					CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
			id.setIdSource(CompoundIdSource.LIBRARY_MS2);
			id.setUniqueId(rs.getString("MATCH_ID"));
			MSMSMatchType matchType = 
					MSMSMatchType.getMSMSMatchTypeByName(rs.getString("MATCH_TYPE"));
			ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
					msmsId,
					rs.getDouble("MATCH_SCORE"), 
					rs.getDouble("FWD_SCORE"),
					rs.getDouble("REVERSE_SCORE"), 
					rs.getDouble("PROBABILITY"), 
					rs.getDouble("DOT_PRODUCT"), 
					rs.getDouble("REVERSE_DOT_PRODUCT"),
					rs.getDouble("HYBRID_DOT_PRODUCT"),
					rs.getDouble("HYBRID_SCORE"),
					rs.getDouble("HYBRID_DELTA_MZ"),
					matchType,
					rs.getString("DECOY_MATCH") != null,
					rs.getString("SEARCH_PARAMETER_SET_ID"));
			
			id.setReferenceMsMsLibraryMatch(match);
			if(rs.getString("IS_PRIMARY") != null)
				id.setPrimary(true);

			String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
			if(statusId != null) 
				id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
			
			featureIdentities.add(id);
		}
		rs.close();
		ps.close();
		return featureIdentities;
	}
	
	public static void removeMSMSFeatureLibraryMatch(MsFeatureIdentity identity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		removeMSMSFeatureLibraryMatch(identity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void removeMSMSFeatureLibraryMatch(MsFeatureIdentity identity, Connection conn) throws Exception {

		if(identity.getReferenceMsMsLibraryMatch() != null) {	//	In case MSMS library match is set as primary ID
			
			String query = "DELETE FROM MSMS_FEATURE_LIBRARY_MATCH WHERE MATCH_ID = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, identity.getUniqueId());
			ps.executeUpdate();
			ps.close();
		}
		else {	//	In case manual match is set as primary ID
			
			String query = "DELETE FROM MSMS_FEATURE_ALTERNATIVE_ID WHERE IDENTIFICATION_ID = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, identity.getUniqueId());
			ps.executeUpdate();
			ps.close();
		}
	}

	public static void clearMSMSFeatureLibraryMatches(String featureId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		clearMSMSFeatureLibraryMatches(featureId, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void clearMSMSFeatureLibraryMatches(String featureId, Connection conn) throws Exception {

		String query = "DELETE FROM MSMS_FEATURE_LIBRARY_MATCH WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();		
	}

	/*
	 *	MSMS features manual identifications
	 */
	
	public static void addMSMSFeatureManualId(
			String featureId, MsFeatureIdentity newIdentity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		addMSMSFeatureManualId(featureId, newIdentity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void addMSMSFeatureManualId(
			String featureId, MsFeatureIdentity newIdentity, Connection conn) throws Exception {

		String query =
			"INSERT INTO MSMS_FEATURE_ALTERNATIVE_ID (IDENTIFICATION_ID, ACCESSION, ID_CONFIDENCE, "
			+ "IS_PRIMARY, MSMS_FEATURE_ID, ID_SOURCE, ASSIGNED_BY, ASSIGNED_ON, "
			+ "IDENTIFICATION_LEVEL_ID, ADDUCT_ID, COMPOSITE_ADDUCT_ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		String identId = SQLUtils.getNextIdFromSequence(conn, 
				"ALTERNATIVE_ID_SEQ",
				DataPrefix.MS_MSMS_MANUAL_MATCH,
				"0",
				15);
		newIdentity.setUniqueId(identId);
		
		String isPrimary = null;
		if(newIdentity.isPrimary()) {
			isPrimary = "Y";
			disableMSMSFeaturePrimaryIdentity(featureId, conn);
		}
		ps.setString(1, identId);
		ps.setString(2, newIdentity.getCompoundIdentity().getPrimaryDatabaseId());
		ps.setString(3, newIdentity.getConfidenceLevel().getLevelId());
		ps.setString(4, isPrimary);
		ps.setString(5, featureId);		
		ps.setString(6, newIdentity.getIdSource().name());
		
		String userId = null;
		if(newIdentity.getAssignedBy() != null)
			userId = newIdentity.getAssignedBy().getId();
			
		ps.setString(7, userId);
		ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
		
		ps.setString(9, newIdentity.getIdentificationLevel().getId());	
		String adductId = null;
		String compositeAdductId = null;
		Adduct adduct = newIdentity.getPrimaryAdduct();
		if(adduct != null) {
			
			if(adduct instanceof SimpleAdduct)
				adductId = adduct.getId();
			
			if(adduct instanceof CompositeAdduct)
				compositeAdductId = adduct.getId();
		}	
		ps.setString(10, adductId);
		ps.setString(11, compositeAdductId);		
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE MSMS_FEATURE SET ID_DISABLED = NULL WHERE MSMS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}

	public static Collection<MsFeatureIdentity>getMSMSFeatureManualIds(String featureId) throws Exception {

		Connection msconn = ConnectionManager.getConnection();
		Collection<MsFeatureIdentity> ids =
				getMSMSFeatureManualIds(featureId, msconn);
		ConnectionManager.releaseConnection(msconn);
		return ids;
	}

	public static Collection<MsFeatureIdentity>getMSMSFeatureManualIds(
			String featureId, Connection conn) throws Exception {

		Collection<MsFeatureIdentity>featureIdentities = new ArrayList<MsFeatureIdentity>();
		String query =
			"SELECT IDENTIFICATION_ID, ACCESSION, ID_CONFIDENCE, "
			+ "IS_PRIMARY, ID_SOURCE, ASSIGNED_BY, ASSIGNED_ON, "
			+ "IDENTIFICATION_LEVEL_ID, ADDUCT_ID, COMPOSITE_ADDUCT_ID " +
			"FROM MSMS_FEATURE_ALTERNATIVE_ID WHERE MSMS_FEATURE_ID = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			CompoundIdentity compoundIdentity =
					CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
			CompoundIdentificationConfidence confidenceLevel =
					CompoundIdentificationConfidence.getLevelById(rs.getString("ID_CONFIDENCE"));
			MsFeatureIdentity id = new MsFeatureIdentity(compoundIdentity, confidenceLevel);
			id.setIdSource(CompoundIdSource.getIdSourceByName(rs.getString("ID_SOURCE")));
			if(rs.getString("IS_PRIMARY") != null)
				id.setPrimary(true);

			id.setUniqueId(rs.getString("IDENTIFICATION_ID"));
			LIMSUser assignedBy = IDTDataCache.getUserById(rs.getString("ASSIGNED_BY"));
			id.setAssignedBy(assignedBy);
			id.setAssignedOn(new Date(rs.getDate("ASSIGNED_ON").getTime()));
			String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
			if(statusId != null) 
				id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
			
			String adductId = rs.getString("ADDUCT_ID");
			if(adductId == null)
				adductId = rs.getString("COMPOSITE_ADDUCT_ID");
			
			if(adductId != null) 
				id.setPrimaryAdduct(AdductManager.getAdductById(adductId));
						
			featureIdentities.add(id);
		}
		rs.close();
		ps.close();
		return featureIdentities;
	}

	public static void removeMSMSFeatureManualIdentification(MsFeatureIdentity identity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		removeMSMSFeatureManualIdentification(identity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void removeMSMSFeatureManualIdentification(
			MsFeatureIdentity identity, Connection conn) throws Exception {

		String query = "DELETE FROM MSMS_FEATURE_ALTERNATIVE_ID WHERE IDENTIFICATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		ps.setString(1, identity.getUniqueId());
		ps.executeUpdate();
		ps.close();
	}

	public static void clearMSMSFeatureManualIdentifications(String featureId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		clearMSMSFeatureManualIdentifications(featureId, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void clearMSMSFeatureManualIdentifications(
			String featureId, Connection conn) throws Exception {

		String query = "DELETE FROM MSMS_FEATURE_ALTERNATIVE_ID WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();	
	}
		
	/*
	 *	MSMS features primary ID
	 */
	
	public static void setMSMSFeaturePrimaryIdentity(
			String featureId, MsFeatureIdentity identity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		setMSMSFeaturePrimaryIdentity(featureId, identity, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void setMSMSFeaturePrimaryIdentity(
			String featureId, MsFeatureIdentity newIdentity, Connection conn) throws Exception {
		
		if(newIdentity == null || newIdentity.getCompoundIdentity() == null) {
			disableMSMSFeaturePrimaryIdentity(featureId, conn);
			return;
		}		
		//	Clear all primary IDs
		String query = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = NULL WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE MSMS_FEATURE_ALTERNATIVE_ID SET IS_PRIMARY = NULL WHERE MSMS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
		
		if(newIdentity.getReferenceMsMsLibraryMatch() != null) {	//	In case MSMS library match is set as primary ID

			query =
				"UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = 'Y' "
				+ "WHERE MSMS_FEATURE_ID = ? AND MATCH_ID = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, featureId);
			ps.setString(2, newIdentity.getUniqueId());
			ps.executeUpdate();
			ps.close();			
		}
		else {	//	In case manual match is set as primary ID			
			query =
				"UPDATE MSMS_FEATURE_ALTERNATIVE_ID SET IS_PRIMARY = 'Y' "
				+ "WHERE MSMS_FEATURE_ID = ? AND IDENTIFICATION_ID = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, featureId);
			ps.setString(2, newIdentity.getUniqueId());
			ps.executeUpdate();
			ps.close();
		}	
		//	Clear disabled id flag
		query = "UPDATE MSMS_FEATURE SET ID_DISABLED = NULL WHERE MSMS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}
	
	public static void disableMSMSFeaturePrimaryIdentity(String featureId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		disableMSMSFeaturePrimaryIdentity(featureId, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void disableMSMSFeaturePrimaryIdentity(String featureId, Connection conn) throws Exception {
		
		//	Clear all primary IDs
		String query = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = NULL WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
		
		query = "UPDATE MSMS_FEATURE_ALTERNATIVE_ID SET IS_PRIMARY = NULL WHERE MSMS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();	
		
		query = "UPDATE MSMS_FEATURE SET ID_DISABLED = 'Y' WHERE MSMS_FEATURE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.executeUpdate();
		ps.close();
	}
		
	/*
	 * Next-ID functions
	 */
		
//	public static String getNextAlternativeMatchId(Connection conn) throws SQLException {
//
//		String msmsMatchId = null;
//		String query = "SELECT '" + DataPrefix.MS_MSMS_MANUAL_MATCH.getName() +
//				"' || LPAD(ALTERNATIVE_ID_SEQ.NEXTVAL, 15, '0') AS MATCH_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			msmsMatchId = rs.getString("MATCH_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		return msmsMatchId;
//	}
	
//	public static String getNextMsMsMatchId(Connection conn) throws SQLException {
//
//		String msmsMatchId = null;
//		String query = "SELECT '" + DataPrefix.MSMS_LIBRARY_MATCH.getName() +
//				"' || LPAD(MSMS_LIB_MATCH_SEQ.NEXTVAL, 15, '0') AS MATCH_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			msmsMatchId = rs.getString("MATCH_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		return msmsMatchId;
//	}
	
//	public static String getNextMsRTMatchId(Connection conn) throws SQLException {
//
//		String msmsMatchId = null;
//		String query = "SELECT '" + DataPrefix.MSRT_LIBRARY_MATCH.getName() +
//				"' || LPAD(MSRT_LIB_MATCH_SEQ.NEXTVAL, 15, '0') AS MATCH_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			msmsMatchId = rs.getString("MATCH_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		return msmsMatchId;
//	}
	
	public static boolean polarityMatches(String msmsFeatureId, String libraryId, Connection conn) throws Exception {
		
		String sql = "SELECT POLARITY FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?"  ;
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, libraryId);
		ResultSet rs = ps.executeQuery();
		String libPol = null;
		while(rs.next())
			libPol = rs.getString(1);
		
		rs.close();
		
		sql = 
			"SELECT POLARITY FROM MSMS_FEATURE WHERE MSMS_FEATURE_ID = ?";
		ps = conn.prepareStatement(sql);
		ps.setString(1, msmsFeatureId);
		rs = ps.executeQuery();
		String fPol = null;
		while(rs.next())
			fPol = rs.getString(1);
		
		rs.close();
		ps.close();
				
		return libPol.equals(fPol);
	}
	
	/*
	 * PEP-search related functions
	 */	
	public static void addMSMSFeatureLibraryMatchFromPepeSearch(
			PepSearchOutputObject poo, 
			String searchParametersId, 
			boolean updateDefaultId,
			Connection conn) throws Exception {

		ReferenceMsMsLibrary refLib = IDTDataCache.getReferenceMsMsLibraryByCode(poo.getLibraryName());
		if(refLib == null) {
			//	TODO handle better
			System.out.println("Unknown library " + poo.getLibraryName());
			return;
		}
		String originalLibraryId = null;
		String refLibraryId = refLib.getPrimaryLibraryId();
		if(refLibraryId.equals(NISTReferenceLibraries.nist_msms.name()) || 
				refLibraryId.equals(NISTReferenceLibraries.hr_msms_nist.name()))
			originalLibraryId = poo.getNistRegId();
		else if(refLibraryId.equals(NISTReferenceLibraries.nist_msms2.name()))
			originalLibraryId = poo.getDatabaseNumber();
		else
			originalLibraryId = poo.getPeptide();

		String mrc2id =
			getMrcMSMSLibraryIdForOriginalLibId(originalLibraryId, refLibraryId, conn);
		
		if(mrc2id == null && refLibraryId.equals(NISTReferenceLibraries.hr_msms_nist.name()))
			mrc2id = getMrcMSMSLibraryIdForOriginalLibId(originalLibraryId, NISTReferenceLibraries.nist_msms.name(), conn);
		
		if(mrc2id == null) {

			System.out.println("Unknown library entry ID " + originalLibraryId + " for library " + poo.getLibraryName());
			return;
		}
		if(!polarityMatches(poo.getMsmsFeatureId(), mrc2id, conn)) {
			
			System.out.println("Polarity mismatch between " + originalLibraryId + 
					" for library " + poo.getLibraryName() + " and MSMS feature # " + poo.getMsmsFeatureId());
			return;
		}
		
		//	Check if match exists and get top score
		Map<String, Double> matchScoreMap = getMSMSFeatureMatchScoreMap(poo.getMsmsFeatureId(), conn);
		if(matchScoreMap.keySet().contains(mrc2id))
			return;

		String matchId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_LIB_MATCH_SEQ",
				DataPrefix.MSMS_LIBRARY_MATCH,
				"0",
				15);
		String query =
			"INSERT INTO MSMS_FEATURE_LIBRARY_MATCH (" +
			"MSMS_FEATURE_ID, MATCH_ID, MRC2_LIB_ID, MATCH_SCORE,  " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, "
			+ "REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, "
			+ "HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, poo.getMsmsFeatureId());
		ps.setString(2, matchId);
		ps.setString(3, mrc2id);
		ps.setDouble(4, poo.getScore());
		ps.setDouble(5, poo.getProbablility());
		ps.setDouble(6, poo.getDotProduct());
		ps.setString(7, searchParametersId);

		if (poo.getReverseDotProduct() == 0.0)
			ps.setNull(8, java.sql.Types.NULL);
		else
			ps.setDouble(8, poo.getReverseDotProduct());
		
		if (poo.getHybridDotProduct() == 0.0)
			ps.setNull(9, java.sql.Types.NULL);
		else
			ps.setDouble(9, poo.getHybridDotProduct());
		
		if (poo.getHybridScore() == 0.0)
			ps.setNull(10, java.sql.Types.NULL);
		else
			ps.setDouble(10, poo.getHybridScore());
		
		if (poo.getHybridDeltaMz() == 0.0)
			ps.setNull(11, java.sql.Types.NULL);
		else
			ps.setDouble(11, poo.getHybridDeltaMz());

		ps.setString(12, poo.getMatchType().name());

		if (poo.isDecoy())
			ps.setString(13, "1");
		else
			ps.setNull(13, java.sql.Types.NULL);
		
		ps.executeUpdate();
		ps.close();

		double maxScore = 0.0d;
		if(!matchScoreMap.isEmpty())
			maxScore = Collections.max(matchScoreMap.values());

		if(poo.getScore() > maxScore && updateDefaultId) {

			query = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = NULL WHERE MSMS_FEATURE_ID = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, poo.getMsmsFeatureId());
			ps.executeUpdate();
			ps.close();

			query = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = 'Y' WHERE MATCH_ID = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, matchId);
			ps.executeUpdate();
			ps.close();
		}
	}

	public static Map<String,Double> getMSMSFeatureMatchScoreMap(
			String msmsFeatureId,
			Connection conn) throws Exception {

		Map<String,Double>scoreMap = new TreeMap<String,Double>();
		String query =
			"SELECT MRC2_LIB_ID, MATCH_SCORE FROM MSMS_FEATURE_LIBRARY_MATCH " +
			"WHERE MSMS_FEATURE_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msmsFeatureId);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			scoreMap.put(rs.getString("MRC2_LIB_ID"), rs.getDouble("MATCH_SCORE"));

		rs.close();
		ps.close();
		return scoreMap;
	}

	public static String getMrcMSMSLibraryIdForOriginalLibId(
			String originalLibraryId,
			String libraryName,
			Connection conn) throws Exception {

		String mrc2id = null;
		String query =
			"SELECT C.MRC2_LIB_ID  " +
			"FROM REF_MSMS_LIBRARY_COMPONENT C " +
			"WHERE C.LIBRARY_NAME = ? " +
			"AND C.ORIGINAL_LIBRARY_ID = ? ";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, libraryName);
		ps.setString(2, originalLibraryId);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			mrc2id = rs.getString("MRC2_LIB_ID");

		rs.close();
		ps.close();
		return mrc2id;
	}
	
	public static Collection<NISTPepSearchParameterObject> getNISTPepSearchParameterObjects() throws Exception {
	
		Connection conn = ConnectionManager.getConnection();
		Collection<NISTPepSearchParameterObject> paramSets = getNISTPepSearchParameterObjects(conn);
		ConnectionManager.releaseConnection(conn);
		return paramSets;
	}

	public static Collection<NISTPepSearchParameterObject>getNISTPepSearchParameterObjects(Connection conn) throws Exception {
		
		String query =
				"SELECT PARAMETER_SET_ID, PARAMETER_SET_OBJECT FROM NIST_PEPSEARCH_PARAMETERS";
		
		Collection<NISTPepSearchParameterObject> paramSets = new TreeSet<NISTPepSearchParameterObject>();
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
		   
		   BufferedInputStream bais = new BufferedInputStream(rs.getBinaryStream("PARAMETER_SET_OBJECT"));			   
	       ObjectInputStream oin = new ObjectInputStream(bais); 
	       NISTPepSearchParameterObject paramSet = (NISTPepSearchParameterObject)oin.readObject();
	       paramSet.setId(rs.getString("PARAMETER_SET_ID"));
		   oin.close();
		   bais.close();
		   paramSets.add(paramSet);
		}
		rs.close();
		ps.close();
		return paramSets;
	}
	
	public static void writeNISTPepSearchParameterObjectToTextFile(File textFile) throws Exception {
		
		Collection<NISTPepSearchParameterObject>params = getNISTPepSearchParameterObjects();
		
		String paramString = "";
		//	Header
		String[] header = new String[] {
			"ID",
			"Pre-search type",
			"High-res search type",
			"High-res search option",
			"High-res search threshold",
			"Hit-rejection option",
			"Enable reverse search",
			"Alternative peak matching",
			"Ignore peaks around precursor", //	False or actual window
			"Precursor M/Z error",
			"Fragment M/Z error",
			"M/Z range",
			"Min. intensity",
			"Match polarity",
			"Match charge",
			"Hybrid mass loss",
			"Peptide score option",
			"Min. match factor",
			"Max. hits",
			"Search params hash",
			"Filter params hash",
		};
		paramString += StringUtils.join(header, "\t") + "\n";
		for(NISTPepSearchParameterObject paramSet : params) {
			
			ArrayList<String>values = new ArrayList<String>();
			
//			"ID",
			values.add(paramSet.getId());
//			"Pre-search type",
			values.add(paramSet.getPreSearchType().getDescription());
//			"High-res search type",
			if(paramSet.getHiResSearchType() != null)
				values.add(paramSet.getHiResSearchType().getDescription());
			else
				values.add("");
			
//			"High-res search option",
			values.add(paramSet.getHiResSearchOption().getDescription());
//			"High-res search threshold",
			values.add(paramSet.getHiResSearchThreshold().getDescription());
//			"Hit-rejection option",
			if(paramSet.getHitRejectionOption() != null)
				values.add(paramSet.getHitRejectionOption().getDescription());
			else
				values.add("");
			
//			"Enable reverse search",
			values.add(Boolean.toString(paramSet.isEnableReverseSearch()));
//			"Alternative peak matching",
			values.add(Boolean.toString(paramSet.isEnableAlternativePeakMatching()));
//			"Ignore peaks around precursor", //	False or actual window
			if(!paramSet.isIgnorePeaksAroundPrecursor())
				values.add("");
			else {
				String ignoreAroundPrecursorWindow =
						Double.toString(paramSet.getIgnorePeaksAroundPrecursorWindow()) + " " +
								paramSet.getIgnorePeaksAroundPrecursorAccuracyUnits().name();
				values.add(ignoreAroundPrecursorWindow);
			}
//			"Precursor M/Z error",
			String precursorWindow =
					Double.toString(paramSet.getPrecursorMzErrorValue()) + " " +
							paramSet.getPrecursorMzErrorType().name();
			values.add(precursorWindow);

//			"Fragment M/Z error",
			String fragmentWindow =
					Double.toString(paramSet.getFragmentMzErrorValue()) + " " +
							paramSet.getFragmentMzErrorType().name();
			values.add(fragmentWindow);
			
//			"M/Z range",
			values.add(paramSet.getMzRange().toString());
//			"Min. intensity",
			values.add(Integer.toString(paramSet.getMinimumIntensityCutoff()));
//			"Match polarity",
			values.add(Boolean.toString(paramSet.isMatchPolarity()));
//			"Match charge",
			values.add(Boolean.toString(paramSet.isMatchCharge()));
//			"Hybrid mass loss",
			values.add(Double.toString(paramSet.getHybridSearchMassLoss()));
//			"Peptide score option",
			if(paramSet.getPeptideScoreOption() != null)
				values.add(paramSet.getPeptideScoreOption().getDescription());
			else
				values.add("");
//			"Min. match factor",
			values.add(Integer.toString(paramSet.getMinMatchFactor()));
//			"Max. hits",
			values.add(Integer.toString(paramSet.getMaxNumberOfHits()));
//			"Search params hash",
			values.add(paramSet.getSearchParametersMD5string());
//			"Filter params hash",
			values.add(paramSet.getResultFilteringParametersMD5string());
						
			paramString += StringUtils.join(values, "\t") + "\n";
		}
		FileUtils.writeStringToFile(textFile, paramString, Charset.defaultCharset(), false);		
	}

	public static NISTPepSearchParameterObject 
		getNISTPepSearchParameterObjectById(String id) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		NISTPepSearchParameterObject paramSet = getNISTPepSearchParameterObjectById(id, conn);
		ConnectionManager.releaseConnection(conn);
		return paramSet;
	}
	
	public static NISTPepSearchParameterObject 
		getNISTPepSearchParameterObjectById(String id, Connection conn) throws Exception {
		
		String query =
				"SELECT PARAMETER_SET_OBJECT FROM NIST_PEPSEARCH_PARAMETERS " +
				"WHERE PARAMETER_SET_ID = ? ";
		
		NISTPepSearchParameterObject paramSet = null;
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
		
//		   Blob blob = rs.getBlob("PARAMETER_SET_OBJECT");
		   BufferedInputStream bais = new BufferedInputStream(rs.getBinaryStream("PARAMETER_SET_OBJECT"));			   
           ObjectInputStream oin = new ObjectInputStream(bais); 
           paramSet = (NISTPepSearchParameterObject)oin.readObject();
           paramSet.setId(id);
		   oin.close();
		   bais.close();
//		   blob.free();
		}
		rs.close();
		ps.close();
		return paramSet;
	}
	
	public static String addNewPepSearchParameterSet(
			NISTPepSearchParameterObject parObject) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String paramSetId = addNewPepSearchParameterSet(parObject, conn);
		ConnectionManager.releaseConnection(conn);
		return paramSetId;
	}
	
	public static String addNewPepSearchParameterSet(
			NISTPepSearchParameterObject parObject,
			Connection conn) throws Exception {
		
		//	Check if parameter set already exists
		String paramSetId = getIdIfPepSearchParameterSetExists(parObject, conn);
		if(paramSetId != null) {
			parObject.setId(paramSetId);
			return paramSetId;
		}
		//	Insert new parameter set
		paramSetId = SQLUtils.getNextIdFromSequence(conn, 
				"PEP_SEARCH_PARAMS_SEQ",
				DataPrefix.NIST_PEPSEARCH_PARAM_SET,
				"0",
				4);
		parObject.setId(paramSetId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos); 
		out.writeObject(parObject);
		byte[] ba = baos.toByteArray();
		InputStream bais = new ByteArrayInputStream(ba); 
		
		String query =
			"INSERT INTO NIST_PEPSEARCH_PARAMETERS (PARAMETER_SET_ID, PARAMETER_SET_OBJECT, "
			+ "SEARCH_PARAMETERS_HASH, FILTER_PARAMETERS_HASH) VALUES (?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, paramSetId);
		ps.setBinaryStream(2, bais, ba.length);
		ps.setString(3, parObject.getSearchParametersMD5string());
		ps.setString(4, parObject.getResultFilteringParametersMD5string());
		ps.executeUpdate();
		ps.close();		
		baos.close();
		out.close();
		bais.close();		
		return paramSetId;
	}
	
	public static Collection<String> getPepSearchParameterSetWithSameSearchSettings(
			NISTPepSearchParameterObject parObject, Connection conn) throws Exception {
		
		Collection<String>parSetIds = new TreeSet<String>();
		String query =
				"SELECT PARAMETER_SET_ID FROM NIST_PEPSEARCH_PARAMETERS " +
				"WHERE SEARCH_PARAMETERS_HASH = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, parObject.getSearchParametersMD5string());
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			parSetIds.add(rs.getString("PARAMETER_SET_ID"));

		rs.close();
		ps.close();
		return parSetIds;
	}
	
	public static String getIdIfPepSearchParameterSetExists(
			NISTPepSearchParameterObject parObject,
			Connection conn) throws Exception {
		
		String query =
				"SELECT PARAMETER_SET_ID FROM NIST_PEPSEARCH_PARAMETERS " +
				"WHERE SEARCH_PARAMETERS_HASH = ? " +
				"AND FILTER_PARAMETERS_HASH = ? ";
		
		String paramSetId = null;
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, parObject.getSearchParametersMD5string());
		ps.setString(2, parObject.getResultFilteringParametersMD5string());
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			paramSetId = rs.getString("PARAMETER_SET_ID");

		rs.close();
		ps.close();
		return paramSetId;
	}
	
//	private static String getNextPepSearchParameterSetId(Connection conn) throws Exception {
//		
//		String query  =
//				"SELECT '" + DataPrefix.NIST_PEPSEARCH_PARAM_SET.getName() + 
//				"' || LPAD(PEP_SEARCH_PARAMS_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		
//		String nextId = null;
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next())
//			nextId = rs.getString("NEXT_ID");
//
//		rs.close();
//		ps.close();
//		return nextId;
//	}
	
	//	TODO hard-code tentative level for luck of a better solution
	public static void markNewDefaultLibraryIdsAsTentative() throws Exception {

		String sql = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IDENTIFICATION_LEVEL_ID = "
				+ "'IDS002' WHERE IDENTIFICATION_LEVEL_ID IS NULL AND IS_PRIMARY IS NOT NULL";
		
		ConnectionManager.executeUpdate(sql);
	}
	
	public static void clearAllReferenceMS1FeatureIdentifications(
			String featureId, Connection conn) throws Exception {
		
		clearReferenceMS1FeatureLibraryMatches(featureId, conn);
		clearReferenceMS1FeatureManualIds(featureId, conn);
	}
	
	public static void clearAllMSMSFeatureIdentifications(
			String featureId, Connection conn) throws Exception {
		
		clearMSMSFeatureLibraryMatches(featureId, conn);
		clearMSMSFeatureManualIdentifications(featureId, conn);
	}
}







