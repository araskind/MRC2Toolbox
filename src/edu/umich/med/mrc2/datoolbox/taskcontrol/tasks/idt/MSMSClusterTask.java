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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSetType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public abstract class MSMSClusterTask extends AbstractTask {

	protected void insertNewMSMSClusterDataSet(
			IMSMSClusterDataSet dataSet,
			Connection conn) throws Exception {
		
		taskDescription = "Inserting MSMS cluster data set ... ";
		total = 100;
		processed = 20;
		try {
			MSMSClusteringDBUtils.insertMSMSClusterDataSet(dataSet, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ConnectionManager.releaseConnection(conn);	
			return;
		}
		taskDescription = "Inserting individual MSMS cluster data ... ";
		total = dataSet.getClusters().size();
		processed = 0;
		
		String query = 
				"INSERT INTO MSMS_CLUSTER (CLUSTER_ID, PAR_SET_ID, "
				+ "MZ, RT, MSMS_LIB_MATCH_ID, MSMS_ALT_ID, "
				+ "IS_LOCKED, CDS_ID, LOOKUP_FEATURE_ID, BA_CLUSTER_ID) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String featureQuery = "INSERT INTO MSMS_CLUSTER_COMPONENT "
				+ "(CLUSTER_ID, MS_FEATURE_ID, BCC_ID, IS_LIB_REF) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement featurePs = conn.prepareStatement(featureQuery);	
		
		ps.setString(2, dataSet.getParameters().getId());
		
		for(IMsFeatureInfoBundleCluster cluster : dataSet.getClusters()) {
			
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
				
				if(cluster.getPrimaryIdentity().getReferenceMsMsLibraryMatch() != null) {
					msmsLibMatchId = cluster.getPrimaryIdentity().getUniqueId();
				}				
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
					if(feature.isUsedAsLibraryReference())
						featurePs.setString(4, "Y");
					else
						featurePs.setNull(4, java.sql.Types.NULL);
					
					featurePs.addBatch();
				}
			}
			featurePs.executeBatch();
			processed++;
		}		
		ps.close();
		featurePs.close();
	}
}
