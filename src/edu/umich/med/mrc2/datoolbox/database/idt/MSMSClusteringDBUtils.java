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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSMSClusteringDBUtils {
	
	public static Collection<MSMSClusteringParameterSet> getMSMSClusteringParameterSets() throws Exception {
		Connection conn = ConnectionManager.getConnection();
		Collection<MSMSClusteringParameterSet> paramSets = getMSMSClusteringParameterSets(conn);
		ConnectionManager.releaseConnection(conn);
		return paramSets;
	}
	
	public static Collection<MSMSClusteringParameterSet>getMSMSClusteringParameterSets(Connection conn) throws Exception {
		
		Collection<MSMSClusteringParameterSet> paramSets = new ArrayList<MSMSClusteringParameterSet>();
		String query = 
			"SELECT PAR_SET_ID, PAR_SET_NAME, MZ_ERROR_VALUE,  " +
			"MZ_ERROR_TYPE, RT_ERROR_VALUE, MSMS_SIMILARITY_CUTOFF,  " +
			"PAR_SET_MD5 FROM MSMS_CLUSTERING_PARAMETERS  " +
			"ORDER BY PAR_SET_NAME ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			MSMSClusteringParameterSet parSet = new MSMSClusteringParameterSet(
					rs.getString("PAR_SET_ID"),
					rs.getString("PAR_SET_NAME"),
					rs.getDouble("MZ_ERROR_VALUE"),
					MassErrorType.getTypeByName(rs.getString("MZ_ERROR_TYPE")),
					rs.getDouble("RT_ERROR_VALUE"),
					rs.getDouble("MSMS_SIMILARITY_CUTOFF"),
					rs.getString("PAR_SET_MD5"));
			paramSets.add(parSet);
		}
		rs.close();		
		ps.close();	
		return paramSets;
	}

	public static void addMSMSClusteringParameterSet(
			MSMSClusteringParameterSet params) throws Exception {
		Connection conn = ConnectionManager.getConnection();
		addMSMSClusteringParameterSet(params, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addMSMSClusteringParameterSet(
			MSMSClusteringParameterSet params,
			Connection conn) throws Exception {
		
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_CLUST_PARAMS_SEQ",
				DataPrefix.MSMS_CLUSTERING_PARAM_SET,
				"0",
				5);
		params.setId(newId);
		String md5 = MSMSClusteringUtils.calculateCLusteringParametersMd5(params);
		params.setMd5(md5);
		
		String query = 
				"INSERT INTO MSMS_CLUSTERING_PARAMETERS ( " +
				"PAR_SET_ID, PAR_SET_NAME, MZ_ERROR_VALUE, MZ_ERROR_TYPE,  " +
				"RT_ERROR_VALUE, MSMS_SIMILARITY_CUTOFF, PAR_SET_MD5)  " +
				"VALUES(?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, params.getId());
		ps.setString(2,params.getName());
		ps.setDouble(3, params.getMzErrorValue());
		ps.setString(4, params.getMassErrorType().name());
		ps.setDouble(5, params.getRtErrorValue());
		ps.setDouble(6, params.getMsmsSimilarityCutoff());
		ps.setString(7, params.getMd5());
		ps.executeUpdate();
		ps.close();
	}
	
	public static void deleteMSMSClusteringParameterSet(
			MSMSClusteringParameterSet params) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM MSMS_CLUSTERING_PARAMETERS WHERE PAR_SET_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, params.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<MSMSClusterDataSet> getMSMSClusterDataSets() throws Exception {
		Connection conn = ConnectionManager.getConnection();
		Collection<MSMSClusterDataSet> dataSets = getMSMSClusterDataSets(conn);
		ConnectionManager.releaseConnection(conn);
		return dataSets;
	}
	
	public static Collection<MSMSClusterDataSet>getMSMSClusterDataSets(Connection conn) throws Exception {
		
		Collection<MSMSClusterDataSet> dataSets = new ArrayList<MSMSClusterDataSet>();
		String query = 
			"SELECT CDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, PAR_SET_ID FROM MSMS_CLUSTERED_DATA_SET " +
			"ORDER BY NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			LIMSUser createdBy = IDTDataCash.getUserById(rs.getString("CREATED_BY"));
			MSMSClusterDataSet ds = new MSMSClusterDataSet(
					rs.getString("CDS_ID"), 
					rs.getString("NAME"), 
					rs.getString("DESCRIPTION"), 
					createdBy, 
					new Date(rs.getDate("DATE_CREATED").getTime()));
			
			MSMSClusteringParameterSet parSet = 
					IDTDataCash.getMsmsClusteringParameterSetById(rs.getString("PAR_SET_ID"));
			ds.setParameters(parSet);
			dataSets.add(ds);
		}
		rs.close();		
		ps.close();
		return dataSets;
	}
	
	public static void insertMSMSClusterDataSet(MSMSClusterDataSet newDataSet) throws Exception {
		Connection conn = ConnectionManager.getConnection();
		insertMSMSClusterDataSet(newDataSet, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void insertMSMSClusterDataSet(MSMSClusterDataSet newDataSet, Connection conn) throws Exception {
		
		MSMSClusteringParameterSet parSet = 
				IDTDataCash.getMsmsClusteringParameterSetById(newDataSet.getParameters().getId());
		if(parSet == null) {
			addMSMSClusteringParameterSet(parSet, conn);
			IDTDataCash.getMsmsClusteringParameters().add(parSet);
		}
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_CLUSTERS_DATA_SET_SEQ",
				DataPrefix.MSMS_CLUSTER_DATA_SET,
				"0",
				5);
		newDataSet.setId(newId);
		String query = 
			"INSERT INTO MSMS_CLUSTERED_DATA_SET " +
			"(CDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, PAR_SET_ID) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newDataSet.getId());
		ps.setString(2, newDataSet.getName());
		if(newDataSet.getDescription() != null)
			ps.setString(3, newDataSet.getDescription());
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		ps.setString(4, newDataSet.getCreatedBy().getId());
		ps.setDate(5, new java.sql.Date(newDataSet.getDateCreated().getTime()));	
		ps.setString(6, newDataSet.getParameters().getId());
		ps.executeUpdate();
		
		//	Add assays
		Collection<String>injectionIds = newDataSet.getInjectionIds();		
		Collection<String>methodIds = 
				newDataSet.getDataExtractionMethods().stream().
				map(m -> m.getId()).collect(Collectors.toSet());
		
		Collection<String>daIds = new TreeSet<String>();
		query = "SELECT DATA_ANALYSIS_ID FROM DATA_ANALYSIS_MAP " +
				"WHERE EXTRACTION_METHOD_ID = ? AND INJECTION_ID = ?";
		ps = conn.prepareStatement(query);
		ResultSet rs = null;		
		for(String methodId : methodIds) {
			
			for(String injectionId : injectionIds) {
				
				ps.setString(1, methodId);
				ps.setString(2, injectionId);
				rs = ps.executeQuery();
				while(rs.next())
					daIds.add(rs.getString("DATA_ANALYSIS_ID"));
				
				rs.close();
			}
		}
		query = "INSERT INTO MSMS_CLUSTERED_DATA_SET_DA_COMPONENT "
				+ "(CDS_ID, DATA_ANALYSIS_ID) VALUES (?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(1, newDataSet.getId());
		for(String daId : daIds) {
			ps.setString(2, daId);
			ps.addBatch();
		}
		ps.executeBatch();
					
		//	Add clusters
		query = "INSERT INTO MSMS_CLUSTER (CLUSTER_ID, PAR_SET_ID, "
				+ "MZ, RT, COMPOUND_ID, IS_LOCKED) VALUES (?, ?, ?, ?, ?, ?)";
		ps = conn.prepareStatement(query);
		
		String featureQuery = "INSERT INTO MSMS_CLUSTER_COMPONENT "
				+ "(CLUSTER_ID, MSMS_FEATURE_ID) VALUES (?, ?)";
		PreparedStatement featurePs = conn.prepareStatement(featureQuery);
		
		for(MsFeatureInfoBundleCluster cluster : newDataSet.getClusters()) {
			
			String clusterId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_CLUSTER_SEQ",
					DataPrefix.MSMS_CLUSTER,
					"0",
					12);
			cluster.setId(clusterId);
			ps.setString(1, clusterId);
			ps.setString(2, parSet.getId());
			ps.setDouble(3, cluster.getMz());
			ps.setDouble(4, cluster.getRt());
			CompoundIdentity cid = cluster.getPrimaryIdentity();
			if(cid != null)
				ps.setString(5, cid.getPrimaryDatabaseId());
			else
				ps.setNull(5, java.sql.Types.NULL);
			
			if(cluster.isLocked())
				ps.setString(6, "Y");
			else
				ps.setNull(6, java.sql.Types.NULL);
			
			ps.executeUpdate();
			
			//	Add cluster features
			featurePs.setString(1, clusterId);
			for(MsFeatureInfoBundle feature : cluster.getComponents()) {				
				featurePs.setString(2, feature.getMSMSFeatureId());
				featurePs.addBatch();
			}
			featurePs.executeBatch();
		}
	}
}




























