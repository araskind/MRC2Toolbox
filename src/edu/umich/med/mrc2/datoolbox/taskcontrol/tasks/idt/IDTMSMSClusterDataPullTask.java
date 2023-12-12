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
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSetType;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureLookupDataSetUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTMSMSClusterDataPullTask extends IDTMSMSFeatureDataPullTask {

	private Map<String, Map<String,String>>clusterFeatureIdMap;
	private IMSMSClusterDataSet dataSet;
	private Set<IMsFeatureInfoBundleCluster>clusters;
	private Map<IMsFeatureInfoBundleCluster,String>defaultClusterMSMSLibMatchesMap;
	private Map<IMsFeatureInfoBundleCluster,String>defaultClusterAltIdMap;
	
	public IDTMSMSClusterDataPullTask(IMSMSClusterDataSet dataSet) {
		super(null);
		this.dataSet = dataSet;
	}

	@Override
	public void run() {
		
		taskDescription = "Getting feature data from IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			getMSMSClusters();
		} catch (Exception e1) {
			errorMessage = e1.getMessage();
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			getCachedFeatures();
		}
		catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		if(featureIds != null && !featureIds.isEmpty()) {
			try {
				getMsMsFeatures();
			} catch (Exception e1) {
				errorMessage = e1.getMessage();
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}		
			try {			
				if(!features.isEmpty()) {
					
					attachExperimentalTandemSpectra();
					attachMsMsLibraryIdentifications();
					attachMsMsManualIdentities();
					retievePepSearchParameters();
					attachAnnotations();
					attachFollowupSteps();
					putDataInCache();
					attachChromatograms();
				}		
			}
			catch (Exception e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);			
			}
		}
		finalizeFeatureList();
		setFeaturesForClusters();
		attachCusterPrimaryIdentifications();
		dataSet.getClusters().clear();
		dataSet.getClusters().addAll(clusters);
		setStatus(TaskStatus.FINISHED);
	}
	
	private void setFeaturesForClusters() {

		taskDescription = "Adding MSMS features to clusters ...";
		total = clusters.size();
		processed = 0;
		if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.FEATURE_BASED)) {
			
			for(IMsFeatureInfoBundleCluster cluster : clusters) {
				
				Map<String,String> fids = clusterFeatureIdMap.get(cluster.getId());
				features.stream().
					filter(f -> fids.containsKey(f.getMSFeatureId())).
					forEach(f -> cluster.addComponent(null, f));
				processed++;
			}
			return;
		}
		if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.BINNER_ANNOTATION_BASED)) {
			
			for(IMsFeatureInfoBundleCluster cluster : clusters) {
				
				Map<String,String> fids = clusterFeatureIdMap.get(cluster.getId());
				for(Entry<String,String>pair : fids.entrySet()) {
					
					MSFeatureInfoBundle fb = features.stream().
							filter(f -> f.getMSFeatureId().equals(pair.getKey())).
							findFirst().orElse(null);

					if(cluster.getBinnerAnnotationCluster() != null && pair.getValue() != null) {
						
						BinnerAnnotation ba = 
								cluster.getBinnerAnnotationCluster().getBinnerAnnotationById(pair.getValue());					
						if(ba != null)
							cluster.addComponent(ba, fb);
					}
				}
				processed++;
			}
			return;
		}
	}

	private void attachCusterPrimaryIdentifications() {
		
		taskDescription = "Adding MSMS primary IDs for clusters ...";
		total = defaultClusterMSMSLibMatchesMap.size();
		processed = 0;
		for(Entry<IMsFeatureInfoBundleCluster, String> entry : defaultClusterMSMSLibMatchesMap.entrySet()) {

			MsFeatureIdentity msmsMatch = entry.getKey().getComponents().
				stream().flatMap(c -> c.getMsFeature().getIdentifications().stream()).
				filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch())).
				filter(i -> i.getUniqueId().equals(entry.getValue())).findFirst().orElse(null);
			entry.getKey().setPrimaryIdentity(msmsMatch);
			processed++;
		}
		taskDescription = "Adding manua; primary IDs for clusters ...";
		total = defaultClusterAltIdMap.size();
		processed = 0;
		for(Entry<IMsFeatureInfoBundleCluster, String> entry : defaultClusterAltIdMap.entrySet()) {
			
			MsFeatureIdentity manualMatch = entry.getKey().getComponents().
					stream().flatMap(c -> c.getMsFeature().getIdentifications().stream()).
					filter(i -> i.getUniqueId().equals(entry.getValue())).findFirst().orElse(null);
				entry.getKey().setPrimaryIdentity(manualMatch);
			processed++;
		}
	}

	private void getMSMSClusters() throws Exception {

		taskDescription = "Getting MSMS clusters ...";
		clusters = new HashSet<IMsFeatureInfoBundleCluster>();
		defaultClusterMSMSLibMatchesMap = new HashMap<IMsFeatureInfoBundleCluster,String>();
		defaultClusterAltIdMap = new HashMap<IMsFeatureInfoBundleCluster,String>();
		clusterFeatureIdMap = new TreeMap<String, Map<String,String>>();
		
		Connection conn = ConnectionManager.getConnection();		
		
		FeatureLookupDataSet flDataSet = dataSet.getFeatureLookupDataSet();
		if(flDataSet != null && flDataSet.getFeatures().isEmpty()) {
			try {
				FeatureLookupDataSetUtils.getFeaturesForFeatureLookupDataSet(flDataSet, conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BinnerAnnotationLookupDataSet balds = dataSet.getBinnerAnnotationDataSet();
		if(balds != null && balds.getBinnerAnnotationClusters().isEmpty()) {			
			try {
				BinnerUtils.getClustersForBinnerAnnotationLookupDataSet(balds, conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String clusterQuery = 
				"SELECT CLUSTER_ID, PAR_SET_ID, MZ, RT, IS_LOCKED,  " +
				"MSMS_LIB_MATCH_ID, MSMS_ALT_ID, LOOKUP_FEATURE_ID, BA_CLUSTER_ID " +
				"FROM MSMS_CLUSTER WHERE CDS_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(clusterQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		
		String featureQuery = 
				"SELECT MS_FEATURE_ID, BCC_ID "
				+ "FROM MSMS_CLUSTER_COMPONENT WHERE CLUSTER_ID = ?";
		PreparedStatement fps = conn.prepareStatement(featureQuery);

		ps.setString(1, dataSet.getId());
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.FEATURE_BASED)) {
			
			while(rs.next()) {
				
				IMsFeatureInfoBundleCluster cluster = 
						new MsFeatureInfoBundleCluster(
							rs.getString("CLUSTER_ID"), 
							rs.getDouble("MZ"), 
							rs.getDouble("RT"), 
							null);
				
				if(rs.getString("MSMS_LIB_MATCH_ID") != null)
					defaultClusterMSMSLibMatchesMap.put(
							cluster, rs.getString("MSMS_LIB_MATCH_ID"));
				
				if(rs.getString("MSMS_ALT_ID") != null)
					defaultClusterAltIdMap.put(
							cluster, rs.getString("MSMS_ALT_ID"));
				
				String lfId = rs.getString("LOOKUP_FEATURE_ID");
				if(lfId != null && flDataSet != null)
					cluster.setLookupFeature(flDataSet.getMinimalMSOneFeatureById(lfId));
				
				Map<String,String>clusterFeatureIds = new TreeMap<String,String>();		
				fps.setString(1, cluster.getId());
				ResultSet frs = fps.executeQuery();
				while(frs.next())
					clusterFeatureIds.put(frs.getString(1), null);
							
				frs.close();
				clusterFeatureIdMap.put(cluster.getId(), clusterFeatureIds);
				clusters.add(cluster);
				processed++;
			}
		}
		if(dataSet.getDataSetType().equals(MSMSClusterDataSetType.BINNER_ANNOTATION_BASED)) {
			
			while(rs.next()) {
				
				String bacId = rs.getString("BA_CLUSTER_ID");
				if(bacId != null && balds != null) {

					IMsFeatureInfoBundleCluster cluster = 
							new BinnerBasedMsFeatureInfoBundleCluster(
									balds.getBinnerAnnotationClusterById(bacId));
					cluster.setId(rs.getString("CLUSTER_ID"));
					
					if(rs.getString("MSMS_LIB_MATCH_ID") != null)
						defaultClusterMSMSLibMatchesMap.put(
								cluster, rs.getString("MSMS_LIB_MATCH_ID"));
					
					if(rs.getString("MSMS_ALT_ID") != null)
						defaultClusterAltIdMap.put(
								cluster, rs.getString("MSMS_ALT_ID"));
										
					Map<String,String>clusterFeatureIds = new TreeMap<String,String>();		
					fps.setString(1, cluster.getId());
					ResultSet frs = fps.executeQuery();
					while(frs.next())
						clusterFeatureIds.put(frs.getString(1), frs.getString(2));
								
					frs.close();
					clusterFeatureIdMap.put(cluster.getId(), clusterFeatureIds);
					clusters.add(cluster);
					processed++;
				}
			}
		}
		rs.close();
		ps.close();
		fps.close();
		ConnectionManager.releaseConnection(conn);
		
		featureIds = clusterFeatureIdMap.values().stream().
				flatMap(v -> v.keySet().stream()).collect(Collectors.toSet());
	}

	@Override
	public Task cloneTask() {
		return new IDTMSMSClusterDataPullTask(dataSet);
	}

	public IMSMSClusterDataSet getDataSet() {
		return dataSet;
	}
}
