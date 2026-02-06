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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSetType;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.BinnerAnnotationDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupListManager;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSSearchParameterSet;
import edu.umich.med.mrc2.datoolbox.project.store.MSMSClusteringParameterSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSMSClusteringDBUtils {
	
	public static Collection<MSMSClusteringParameterSet> 
			getMSMSClusteringParameterSets() throws Exception {
		Connection conn = ConnectionManager.getConnection();
		Collection<MSMSClusteringParameterSet> paramSets = 
				getMSMSClusteringParameterSets(conn);
		ConnectionManager.releaseConnection(conn);
		return paramSets;
	}
	
	public static Collection<MSMSClusteringParameterSet> getMSMSClusteringParameterSets(Connection conn)
			throws Exception {

		Collection<MSMSClusteringParameterSet> paramSets = new ArrayList<MSMSClusteringParameterSet>();
		String query = 
				"SELECT PAR_SET_ID, PAR_SET_XML,  "
				+ "PAR_SET_MD5 FROM MSMS_CLUSTERING_PARAMETERS_XML " 
				+ "ORDER BY PAR_SET_NAME ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			MSMSClusteringParameterSet parSet = null;
			String xmlString = rs.getString("PAR_SET_XML");				
			parSet = createMSMSClusteringParameterSetFromXML(xmlString);
			if(parSet != null) {
				
				parSet.setMd5(rs.getString("PAR_SET_MD5"));
				paramSets.add(parSet);			
			}
			else
				System.err.println("Could not recreate MSMSClusteringParameterSet "
						+ "for ID " + rs.getString("PAR_SET_ID"));
		}
		rs.close();
		ps.close();
		return paramSets;
	}
	
	public static Collection<MSMSClusteringParameterSet>
			getMSMSClusteringParameterSetsOld(Connection conn) throws Exception {
		
		Collection<MSMSClusteringParameterSet> paramSets = 
				new ArrayList<MSMSClusteringParameterSet>();
		String query = 
			"SELECT PAR_SET_ID, PAR_SET_NAME, MZ_ERROR_VALUE,  " +
			"MZ_ERROR_TYPE, RT_ERROR_VALUE, MSMS_SIMILARITY_CUTOFF,  " +
			"PAR_SET_MD5 FROM MSMS_CLUSTERING_PARAMETERS  " +
			"ORDER BY PAR_SET_ID ";
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
		
		if(params == null)
			throw new Exception("Clustering parameters missing");
			
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_CLUST_PARAMS_SEQ",
				DataPrefix.MSMS_CLUSTERING_PARAM_SET,
				"0",
				5);
		params.setId(newId);
		
		if(params.getMd5() == null) {
			String md5 = MSMSClusteringUtils.calculateClusteringParametersMd5(params);
			params.setMd5(md5);
		}
		String paramsXml = getXMLStringForMSMSClusteringParameterSet(params);
		if(paramsXml == null) {
			throw new InvalidArgumentException(
					"Unable to create XML string for MSMS clustering parameters object");
		}		
		String query = 
				"INSERT INTO MSMS_CLUSTERING_PARAMETERS_XML ( " +
				"PAR_SET_ID, PAR_SET_NAME, PAR_SET_XML, PAR_SET_MD5)  " +
				"VALUES(?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
		
			ps.setString(1, params.getId());
			ps.setString(2,params.getName());		
			ps.setString(3, paramsXml);
			ps.setString(4, params.getMd5());
			ps.executeUpdate();
		}
	}
	
	public static void addMSMSClusteringParameterSetOld(
			MSMSClusteringParameterSet params,
			Connection conn) throws Exception {
		
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_CLUST_PARAMS_SEQ",
				DataPrefix.MSMS_CLUSTERING_PARAM_SET,
				"0",
				5);
		params.setId(newId);
		String md5 = MSMSClusteringUtils.calculateClusteringParametersMd5(params);
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
				"DELETE FROM MSMS_CLUSTERING_PARAMETERS_XML WHERE PAR_SET_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, params.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMSMSClusteringParameterSetOld(
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
	
	public static Map<IMSMSClusterDataSet, Set<String>> 
			getMSMSClusterDataSets() throws Exception {
		Connection conn = ConnectionManager.getConnection();
		Map<IMSMSClusterDataSet, Set<String>> dataSets = 
				getMSMSClusterDataSets(conn);
		ConnectionManager.releaseConnection(conn);
		return dataSets;
	}
	
	public static Map<IMSMSClusterDataSet, Set<String>>
			getMSMSClusterDataSets(Connection conn) throws Exception {
		
		FeatureLookupListManager.refreshFeatureLookupListCollection();
		BinnerAnnotationDataSetManager.refreshBinnerAnnotationLookupDataSetList();
		
		Map<IMSMSClusterDataSet, Set<String>> dataSets = 
				new HashMap<IMSMSClusterDataSet, Set<String>>();
		String query = 
			"SELECT CDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, LAST_MODIFIED, PAR_SET_ID, " + 
			"FLDS_ID, BALDS_ID, DATA_SET_TYPE " + 
			"FROM MSMS_CLUSTERED_DATA_SET " +
			"ORDER BY NAME";
		
		PreparedStatement ps = conn.prepareStatement(query);
		
		String clusterIdQuery = 
				"SELECT CLUSTER_ID FROM MSMS_CLUSTER WHERE CDS_ID = ?";
		PreparedStatement csidPs = conn.prepareStatement(clusterIdQuery);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			LIMSUser createdBy = IDTDataCache.getUserById(rs.getString("CREATED_BY"));
			IMSMSClusterDataSet ds = new MSMSClusterDataSet(
					rs.getString("CDS_ID"), 
					rs.getString("NAME"), 
					rs.getString("DESCRIPTION"), 
					createdBy, 
					new Date(rs.getTimestamp("DATE_CREATED").getTime()),
					new Date(rs.getTimestamp("LAST_MODIFIED").getTime()));

			String dstString = rs.getString("DATA_SET_TYPE");
			if(dstString != null && !dstString.isEmpty()) {
				
				MSMSClusterDataSetType dst = MSMSClusterDataSetType.valueOf(dstString);
				ds.setDataSetType(dst);
				ds.getDataSetType();
			}			
			MSMSClusteringParameterSet parSet = 
					MSMSClusterDataSetManager.getMsmsClusteringParameterSetById(
							rs.getString("PAR_SET_ID"));
			ds.setParameters(parSet);
			
			String fldsId = rs.getString("FLDS_ID");
			if(fldsId != null) {
				FeatureLookupList flds = 
						FeatureLookupListManager.getFeatureLookupListById(fldsId);
				ds.setFeatureLookupDataSet(flds);
			}
			String baldsId = rs.getString("BALDS_ID");
			if(baldsId != null) {
				BinnerAnnotationLookupDataSet balds = 
						BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetById(baldsId);
				ds.setBinnerAnnotationDataSet(balds);
			}			
			Set<String>clusterIds = new TreeSet<String>();
			csidPs.setString(1, ds.getId());
			ResultSet csidrs = csidPs.executeQuery();
			while(csidrs.next())
				clusterIds.add(csidrs.getString(1));
			
			csidrs.close();
			dataSets.put(ds, clusterIds);
		}
		rs.close();		
		ps.close();
		csidPs.close();
		return dataSets;
	}

	public static void insertDataAnalysisIdsForDataSet(
			IMSMSClusterDataSet newDataSet, 
			Collection<String>daIds, 
			Connection conn) throws Exception {
		
		String query = "INSERT INTO MSMS_CLUSTERED_DATA_SET_DA_COMPONENT "
				+ "(CDS_ID, DATA_ANALYSIS_ID) VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newDataSet.getId());
		for(String daId : daIds) {
			ps.setString(2, daId);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}

	public static Set<String>getAnalysisIdsForClusterCollection(
			Collection<IMsFeatureInfoBundleCluster>clusterCollection, Connection conn) throws Exception {
		
		Set<String>analysisIds = new TreeSet<String>();
		
		Set<String>injectionIds = 
				clusterCollection.stream().flatMap(c -> c.getComponents().stream()).
				filter(b -> Objects.nonNull(b.getInjectionId())).
				map(b -> b.getInjectionId()).
				collect(Collectors.toSet());
		Set<String>dataExtractionMethodIds = 
			clusterCollection.stream().flatMap(c -> c.getComponents().stream()).
				filter(b -> Objects.nonNull(b.getDataExtractionMethod())).
				map(b -> b.getDataExtractionMethod().getId()).
				collect(Collectors.toSet());
		
		String query = "SELECT DATA_ANALYSIS_ID FROM DATA_ANALYSIS_MAP " +
				"WHERE EXTRACTION_METHOD_ID = ? AND INJECTION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;		
		for(String methodId : dataExtractionMethodIds) {
			
			for(String injectionId : injectionIds) {
				
				ps.setString(1, methodId);
				ps.setString(2, injectionId);
				rs = ps.executeQuery();
				while(rs.next())
					analysisIds.add(rs.getString("DATA_ANALYSIS_ID"));
				
				rs.close();
			}
		}
		ps.close();		
		return analysisIds;
	}
	
	public static void updateMSMSClusterDataSetMetadata(
			IMSMSClusterDataSet edited) throws Exception {
		Connection conn = ConnectionManager.getConnection();
		updateMSMSClusterDataSetMetadata(edited, conn);
		ConnectionManager.releaseConnection(conn);		
	}
	
	public static void updateMSMSClusterDataSetMetadata(
			IMSMSClusterDataSet edited, Connection conn) throws Exception {

		String query = 
				"UPDATE MSMS_CLUSTERED_DATA_SET SET NAME = ?, "
				+ "DESCRIPTION = ?, LAST_MODIFIED = ? WHERE CDS_ID = ?";
		edited.setLastModified(new Date());
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, edited.getName());
		ps.setString(2, edited.getDescription());
		ps.setTimestamp(3, new java.sql.Timestamp(edited.getLastModified().getTime()));
		ps.setString(4, edited.getId());
		ps.executeUpdate();
		ps.close();
	}

	public static void addClustersToDataSet(
			IMSMSClusterDataSet dataSet, 
			Collection<IMsFeatureInfoBundleCluster> newClusters) throws Exception {
		Connection conn = ConnectionManager.getConnection();
		addClustersToDataSet(dataSet, newClusters, conn);
		ConnectionManager.releaseConnection(conn);		
	}

	public static void addClustersToDataSet(
			IMSMSClusterDataSet dataSet, 
			Collection<IMsFeatureInfoBundleCluster> newClusters, 
			Connection conn) throws Exception{
		Set<String> existingIds = dataSet.getClusterIds();
		List<IMsFeatureInfoBundleCluster> clustersToAdd = newClusters.stream().
				filter(c -> !existingIds.contains(c.getId())).
				collect(Collectors.toList());
		if(clustersToAdd.isEmpty())
			return;		
		 
		String query = 
				"INSERT INTO MSMS_CLUSTER (CLUSTER_ID, PAR_SET_ID, "
				+ "MZ, RT, MSMS_LIB_MATCH_ID, MSMS_ALT_ID, "
				+ "IS_LOCKED, CDS_ID, LOOKUP_FEATURE_ID, BA_CLUSTER_ID) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String featureQuery = "INSERT INTO MSMS_CLUSTER_COMPONENT "
				+ "(CLUSTER_ID, MS_FEATURE_ID, BCC_ID, IS_MATCHING_TARGET) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement featurePs = conn.prepareStatement(featureQuery);	
		
		ps.setString(2, dataSet.getParameters().getId());
		
		for(IMsFeatureInfoBundleCluster cluster : newClusters) {
			
			//	Set correct database feature IDs 
			cluster.getFeatureIds().clear();
			cluster.getFeatureIds().addAll(
					cluster.getComponents().stream().
					map(c -> c.getMSFeatureId()).
					collect(Collectors.toSet()));
			
			String clusterId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_CLUSTER_SEQ",
					DataPrefix.MSMS_CLUSTER,
					"0",
					12);
			cluster.setId(clusterId);
			String msmsLibMatchId = null;
			String altId = null;
			
			if(cluster.getPrimaryIdentity() != null) {
				
				if(cluster.getPrimaryIdentity().getReferenceMsMsLibraryMatch() != null)
					msmsLibMatchId = cluster.getPrimaryIdentity().getUniqueId();
								
				if(cluster.getPrimaryIdentity().getIdSource().equals(CompoundIdSource.MANUAL))
					altId = cluster.getPrimaryIdentity().getUniqueId();
			}			
			ps.setString(1, clusterId);			
			ps.setDouble(3, cluster.getMz());
			ps.setDouble(4, cluster.getRt());
			ps.setString(5, msmsLibMatchId);
			ps.setString(6, altId);
			
			if(cluster.isLocked())
				ps.setString(7, "Y");
			else
				ps.setNull(7, java.sql.Types.NULL);
			
			ps.setString(8, dataSet.getId());
			
			if(cluster.getLookupFeature() != null)
				ps.setString(9, cluster.getLookupFeature().getId());
			else
				ps.setNull(9, java.sql.Types.NULL);
			
			if(cluster.getBinnerAnnotationCluster() != null)
				ps.setString(10, cluster.getBinnerAnnotationCluster().getId());
			else
				ps.setNull(10, java.sql.Types.NULL);
			
			ps.executeUpdate();
			
			//	Add cluster features
			featurePs.setString(1, clusterId);
			
			if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.FEATURE_BASED)) {
				
				for(MSFeatureInfoBundle feature : cluster.getComponents()) {				
					featurePs.setString(2, feature.getMSFeatureId());
					featurePs.setNull(3, java.sql.Types.NULL);
					featurePs.setNull(4, java.sql.Types.NULL);
					featurePs.addBatch();
				}
			}
			if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.BINNER_ANNOTATION_BASED)) {
				
				Map<BinnerAnnotation, Set<MSFeatureInfoBundle>> componentMap = 
						((BinnerBasedMsFeatureInfoBundleCluster)cluster).getComponentMap();
				for(Entry<BinnerAnnotation, Set<MSFeatureInfoBundle>>cme : componentMap.entrySet()) {
					
					featurePs.setString(3, cme.getKey().getId());
					featurePs.setNull(4, java.sql.Types.NULL);
					for(MSFeatureInfoBundle feature : cme.getValue()) {				
						featurePs.setString(2, feature.getMSFeatureId());
						featurePs.addBatch();
					}
				}
			}
			if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.MSMS_SEARCH_BASED)) {
				
				for(MSFeatureInfoBundle feature : cluster.getComponents()) {				
					featurePs.setString(2, feature.getMSFeatureId());
					featurePs.setNull(3, java.sql.Types.NULL);
					if(feature.isUsedAsMatchingTarget())
						featurePs.setString(4, "Y");
					else
						featurePs.setNull(4, java.sql.Types.NULL);
					
					featurePs.addBatch();
				}
			}
			featurePs.executeBatch();
		}		
		ps.close();
		featurePs.close();	
		
		Collection<String>daIds = 
				getAnalysisIdsForClusterCollection(newClusters, conn);
		Collection<String>existingDaIds = 
				getDataAnalysisIdsForMSMSClusterDataSet(dataSet, conn);
		
		Set<String> daIdsToInsert = daIds.stream().
				filter(i -> !existingDaIds.contains(i)).
				collect(Collectors.toSet());
		
		if(!daIdsToInsert.isEmpty())
			insertDataAnalysisIdsForDataSet(dataSet, daIdsToInsert, conn);		
	}
	
	public static Collection<String>getDataAnalysisIdsForMSMSClusterDataSet(
			IMSMSClusterDataSet dataSet, 
			Connection conn) throws Exception {
		
		Collection<String>daIds = new TreeSet<String>();
		String query = 
				"SELECT DATA_ANALYSIS_ID FROM "
				+ "MSMS_CLUSTERED_DATA_SET_DA_COMPONENT WHERE CDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, dataSet.getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			daIds.add(rs.getString(1));
		
		rs.close();
		ps.close();
		return daIds;
	}

	public static void deleteMSMSClusterDataSet(IMSMSClusterDataSet toDelete) throws Exception {
		Connection conn = ConnectionManager.getConnection();
		deleteMSMSClusterDataSet(toDelete, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteMSMSClusterDataSet(IMSMSClusterDataSet toDelete, Connection conn) throws Exception {

		String query = "DELETE FROM MSMS_CLUSTER WHERE CLUSTER_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		int counter = 0;
		for(String clusterId : toDelete.getClusterIds()) {
			ps.setString(1, clusterId);
			ps.addBatch();
			counter++;
			if(counter % 200 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		
		query = "DELETE FROM MSMS_CLUSTERED_DATA_SET WHERE CDS_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, toDelete.getId());
		ps.executeUpdate();
		ps.close();		
	}
	
	public static void updateMSMSClusterPrimaryIdentity(
			IMsFeatureInfoBundleCluster edited) throws Exception {
		Connection conn = ConnectionManager.getConnection();
		updateMSMSClusterPrimaryIdentity(edited, conn);
		ConnectionManager.releaseConnection(conn);		
	}
	
	public static void updateMSMSClusterPrimaryIdentity(
			IMsFeatureInfoBundleCluster edited, 
			Connection conn) throws Exception {

		String query = 
				"UPDATE MSMS_CLUSTER SET MSMS_LIB_MATCH_ID =?,"
				+ "MSMS_ALT_ID = ? WHERE CLUSTER_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String libMatchId = null;
		String libAltId = null;
		if(edited.getPrimaryIdentity() != null) {
			
			if(edited.getPrimaryIdentity().getReferenceMsMsLibraryMatch() != null)
				libMatchId = edited.getPrimaryIdentity().getUniqueId();
			else {
				if(edited.getPrimaryIdentity().getIdSource().equals(CompoundIdSource.MANUAL))
					libAltId = edited.getPrimaryIdentity().getUniqueId();
			}
		}		
		if(libMatchId != null)
			ps.setString(1, libMatchId);
		else
			ps.setNull(1, java.sql.Types.NULL);
		
		if(libAltId != null)
			ps.setString(2, libAltId);
		else
			ps.setNull(2, java.sql.Types.NULL);
		
		ps.setString(3, edited.getId());
		ps.executeUpdate();
		ps.close();
	}
	
	public static void deleteClusters(Collection<IMsFeatureInfoBundleCluster>toDelete) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		deleteClusters(toDelete, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void deleteClusters(
			Collection<IMsFeatureInfoBundleCluster> toDelete, Connection conn) throws Exception {

		String query = "DELETE FROM MSMS_CLUSTER WHERE CLUSTER_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		for(IMsFeatureInfoBundleCluster cluster : toDelete) {
			
			ps.setString(1, cluster.getId());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}

	public static MSMSClusteringParameterSet insertMSMSClusterDataSet(
			MSMSClusterDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		MSMSClusteringParameterSet parSet = 
				insertMSMSClusterDataSet(dataSet, conn);
		ConnectionManager.releaseConnection(conn);	
		return parSet;
	}
	
	public static MSMSClusteringParameterSet insertMSMSClusterDataSet(
			IMSMSClusterDataSet dataSet, Connection conn) throws Exception {
				
		MSMSClusterDataSetManager.refreshMsmsClusteringParameters();
		MSMSClusteringParameterSet parSet = 
				MSMSClusterDataSetManager.getMsmsClusteringParameterSetById(
						dataSet.getParameters().getId());
		if(parSet == null)
			parSet = MSMSClusterDataSetManager.getMsmsClusteringParameterSetByMd5(
					dataSet.getParameters().getMd5());
			
		if(parSet == null && dataSet.getParameters() != null) {
			addMSMSClusteringParameterSet(dataSet.getParameters(), conn);
			MSMSClusterDataSetManager.getMsmsClusteringParameters().add(dataSet.getParameters());
		}
		else {
			if(parSet != null)
				dataSet.setParameters(parSet);
		}
		if(dataSet.getParameters() == null)
			throw new Exception("Clustering parameters missing");
					
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_CLUSTERS_DATA_SET_SEQ",
				DataPrefix.MSMS_CLUSTER_DATA_SET,
				"0",
				5);
		dataSet.setId(newId);
		String query = 
			"INSERT INTO MSMS_CLUSTERED_DATA_SET " +
			"(CDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, LAST_MODIFIED, PAR_SET_ID, "
			+ "FLDS_ID, BALDS_ID, DATA_SET_TYPE) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, dataSet.getId());
		ps.setString(2, dataSet.getName());
		if(dataSet.getDescription() != null)
			ps.setString(3, dataSet.getDescription());
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		ps.setString(4, dataSet.getCreatedBy().getId());
		ps.setTimestamp(5, new java.sql.Timestamp(dataSet.getDateCreated().getTime()));
		ps.setTimestamp(6, new java.sql.Timestamp(dataSet.getLastModified().getTime()));	
		ps.setString(7, dataSet.getParameters().getId());
		if(dataSet.getFeatureLookupDataSet() != null) {
			
			FeatureLookupListManager.refreshFeatureLookupListCollection();
			FeatureLookupList flds = FeatureLookupListManager.getFeatureLookupListById(
					dataSet.getFeatureLookupDataSet().getId());
			if(flds == null) {
				flds = dataSet.getFeatureLookupDataSet();
				FeatureLookupListUtils.addFeatureLookupDataSet(flds, conn);			
				FeatureLookupListManager.getFeatureLookupListCollection().add(flds);
			}
			ps.setString(8, flds.getId());
		}
		else {
			ps.setNull(8, java.sql.Types.NULL);	
		}
		if(dataSet.getBinnerAnnotationDataSet() != null) {
			
			BinnerAnnotationDataSetManager.refreshBinnerAnnotationLookupDataSetList();
			BinnerAnnotationLookupDataSet balds = 
					BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetById(
							dataSet.getBinnerAnnotationDataSet().getId());
			if(balds == null) {				
				balds = dataSet.getBinnerAnnotationDataSet();
				BinnerUtils.addBinnerAnnotationLookupDataSet(balds, conn);				
				BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList().add(balds);
				ps.setString(9, balds.getId());
			}
			else {
				ps.setString(9, balds.getId());
			}
		} 
		else {
			ps.setNull(9, java.sql.Types.NULL);	
		}
		ps.setString(10, dataSet.getDataSetType().name());
	
		ps.executeUpdate();
		
		//	Add assays		
		Collection<String>daIds = 
				getAnalysisIdsForClusterCollection(dataSet.getClusters(), conn);	
		query = "INSERT INTO MSMS_CLUSTERED_DATA_SET_DA_COMPONENT "
				+ "(CDS_ID, DATA_ANALYSIS_ID) VALUES (?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(1, dataSet.getId());
		for(String daId : daIds) {
			ps.setString(2, daId);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		return parSet;		
	}
	
	public static String getXMLStringForMSMSClusteringParameterSet(MSMSClusteringParameterSet params) {
		
		String output = null;
        Document document = new Document();
        Element documentRoot = new Element("MCP");
        documentRoot.setAttribute("version", "1.0.0.0");     
        documentRoot.addContent(params.getXmlElement());
        document.setContent(documentRoot);       
        try {
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            output = outputter.outputString(document);
         } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
	}
	
	public static MSMSClusteringParameterSet createMSMSClusteringParameterSetFromXML(String xmlString) {
		
		MSMSClusteringParameterSet params = null;
		
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(IOUtils.toInputStream(xmlString, Charset.forName("UTF-8")));
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;						
		}
		Element rootElement = doc.getRootElement();
		Element paramsElement = rootElement.getChild(
				ObjectNames.MSMSClusteringParameterSet.name());
		if(paramsElement == null)
			return null;
		
		String emet = paramsElement.getAttributeValue(
				MSMSClusteringParameterSetFields.EntropyScoreMassErrorType.name());
		if(emet == null) {
			try {
				params = new MSMSClusteringParameterSet(paramsElement);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				params = new MSMSSearchParameterSet(paramsElement);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return params;
	}
}




























