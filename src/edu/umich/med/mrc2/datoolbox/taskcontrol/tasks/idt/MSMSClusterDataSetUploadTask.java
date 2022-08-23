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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureLookupDataSetUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSMSClusterDataSetUploadTask extends AbstractTask {

	private MSMSClusterDataSet dataSet;
	
	public MSMSClusterDataSetUploadTask(MSMSClusterDataSet dataSet) {
		super();
		this.dataSet = dataSet;
	}

	@Override
	public void run() {

		taskDescription = "Uploading data for MSMS cluster data set " + dataSet.getName();
		setStatus(TaskStatus.PROCESSING);
		Connection conn = null;
		MSMSClusteringParameterSet parSet = null;
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			//	parSet = insertMSMSClusterDataSet(conn);
			parSet = MSMSClusteringDBUtils.insertMSMSClusterDataSet(dataSet, conn);
		} catch (Exception e1) {
			errorMessage = e1.getMessage();
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			addClustersForDataSet(parSet, conn);
		} catch (Exception e1) {
			errorMessage = e1.getMessage();
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}	
		if(conn != null) {
			try {
				ConnectionManager.releaseConnection(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		taskDescription = "Refreshing data set list ... ";
		total = 100;
		processed = 80;
		MSMSClusterDataSetManager.refreshMSMSClusterDataSetList();
		setStatus(TaskStatus.FINISHED);
	}
	
	private MSMSClusteringParameterSet insertMSMSClusterDataSet(Connection conn) throws Exception {
		
		total = 100;
		processed = 20;
		
		MSMSClusteringParameterSet parSet = 
				MSMSClusterDataSetManager.getMsmsClusteringParameterSetById(dataSet.getParameters().getId());
		if(parSet == null) {
			MSMSClusteringDBUtils.addMSMSClusteringParameterSet(parSet, conn);
			MSMSClusterDataSetManager.getMsmsClusteringParameters().add(parSet);
		}
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_CLUSTERS_DATA_SET_SEQ",
				DataPrefix.MSMS_CLUSTER_DATA_SET,
				"0",
				5);
		dataSet.setId(newId);
		String query = 
			"INSERT INTO MSMS_CLUSTERED_DATA_SET " +
			"(CDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, LAST_MODIFIED, PAR_SET_ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
			
		ps.setString(1, dataSet.getId());
		ps.setString(2, dataSet.getName());
		if(dataSet.getDescription() != null)
			ps.setString(3, dataSet.getDescription());
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		ps.setString(4, dataSet.getCreatedBy().getId());
		ps.setDate(5, new java.sql.Date(dataSet.getDateCreated().getTime()));
		ps.setDate(6, new java.sql.Date(dataSet.getLastModified().getTime()));	
		ps.setString(7, dataSet.getParameters().getId());
		if(dataSet.getFeatureLookupDataSet() != null) {
			
			FeatureLookupDataSet flds = FeatureLookupDataSetManager.getFeatureLookupDataSetById(
					dataSet.getFeatureLookupDataSet().getId());
			if(flds == null)
				FeatureLookupDataSetUtils.addFeatureLookupDataSet(
						dataSet.getFeatureLookupDataSet(), conn);
			
			FeatureLookupDataSetManager.getFeatureLookupDataSetList().add(
					dataSet.getFeatureLookupDataSet());
			ps.setString(8, dataSet.getFeatureLookupDataSet().getId());
		}
		else {
			ps.setNull(8, java.sql.Types.NULL);	
		}		
		ps.executeUpdate();
		
		//	Add assays		
		Collection<String>daIds = 
				MSMSClusteringDBUtils.getAnalysisIdsForClusterCollection(
						dataSet.getClusters(), conn);
		
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
	
	private void addClustersForDataSet(
			MSMSClusteringParameterSet parSet,
			Connection conn) throws Exception {
		
		taskDescription = "Uploading data for individual clusters ... ";
		total = dataSet.getClusters().size();
		processed = 0;
		
		String query = 
				"INSERT INTO MSMS_CLUSTER (CLUSTER_ID, PAR_SET_ID, "
				+ "MZ, RT, MSMS_LIB_MATCH_ID, MSMS_ALT_ID, "
				+ "IS_LOCKED, CDS_ID, LOOKUP_FEATURE_ID) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String featureQuery = "INSERT INTO MSMS_CLUSTER_COMPONENT "
				+ "(CLUSTER_ID, MS_FEATURE_ID) VALUES (?, ?)";
		PreparedStatement featurePs = conn.prepareStatement(featureQuery);
		
		if(dataSet.getFeatureLookupDataSet() != null 
				&& dataSet.getFeatureLookupDataSet().getFeatures().isEmpty()) {
			try {
				FeatureLookupDataSetUtils.getFeaturesForFeatureLookupDataSet(
						dataSet.getFeatureLookupDataSet(), conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(MsFeatureInfoBundleCluster cluster : dataSet.getClusters()) {
			
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
			ps.setString(2, parSet.getId());
			ps.setDouble(3, cluster.getMz());
			ps.setDouble(4, cluster.getRt());

			if(msmsLibMatchId != null)
				ps.setString(5, msmsLibMatchId);
			else
				ps.setNull(5, java.sql.Types.NULL);
			
			if(altId != null)
				ps.setString(6, altId);
			else
				ps.setNull(6, java.sql.Types.NULL);
			
			if(cluster.isLocked())
				ps.setString(7, "Y");
			else
				ps.setNull(7, java.sql.Types.NULL);
			
			ps.setString(8, dataSet.getId());
			
			if(cluster.getLookupFeature() != null)
				ps.setString(9, cluster.getLookupFeature().getId());
			else
				ps.setNull(9, java.sql.Types.NULL);
			
			ps.executeUpdate();
			
			//	Add cluster features
			featurePs.setString(1, clusterId);
			for(MSFeatureInfoBundle feature : cluster.getComponents()) {				
				featurePs.setString(2, feature.getMSFeatureId());
				featurePs.addBatch();
			}
			featurePs.executeBatch();
			processed++;
		}		
		ps.close();
		featurePs.close();
	}
	
	@Override
	public Task cloneTask() {
		return new MSMSClusterDataSetUploadTask(dataSet);
	}

	public MSMSClusterDataSet getDataSet() {
		return dataSet;
	}


}
