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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTMSMSClusterDataPullTask extends IDTMSMSFeatureDataPullTask {

	private Map<String, Collection<String>>clusterFeatureIdMap;
	private MSMSClusterDataSet dataSet;
	private Set<MsFeatureInfoBundleCluster>clusters;
	private Map<MsFeatureInfoBundleCluster,String>defaultClusterMSMSLibMatchesMap;
	private Map<MsFeatureInfoBundleCluster,String>defaultClusterAltIdMap;
	
	public IDTMSMSClusterDataPullTask(MSMSClusterDataSet dataSet) {
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
			getChashedFeatures();
		}
		catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		if(!featureIds.isEmpty()) {
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
		for(MsFeatureInfoBundleCluster cluster : clusters) {
			
			Collection<String> fids = clusterFeatureIdMap.get(cluster.getId());
			features.stream().
				filter(f -> fids.contains(f.getMSMSFeatureId())).
				forEach(f -> cluster.addComponent(f));
			processed++;
		}
	}

	private void attachCusterPrimaryIdentifications() {
		
		taskDescription = "Adding MSMS primary IDs for clusters ...";
		total = defaultClusterMSMSLibMatchesMap.size();
		processed = 0;
		for(Entry<MsFeatureInfoBundleCluster, String> entry : defaultClusterMSMSLibMatchesMap.entrySet()) {

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
		for(Entry<MsFeatureInfoBundleCluster, String> entry : defaultClusterAltIdMap.entrySet()) {
			
			MsFeatureIdentity manualMatch = entry.getKey().getComponents().
					stream().flatMap(c -> c.getMsFeature().getIdentifications().stream()).
					filter(i -> i.getUniqueId().equals(entry.getValue())).findFirst().orElse(null);
				entry.getKey().setPrimaryIdentity(manualMatch);
			processed++;
		}
	}

	private void getMSMSClusters() throws Exception {

		taskDescription = "Getting MSMS clusters ...";
		clusters = new HashSet<MsFeatureInfoBundleCluster>();
		defaultClusterMSMSLibMatchesMap = new HashMap<MsFeatureInfoBundleCluster,String>();
		defaultClusterAltIdMap = new HashMap<MsFeatureInfoBundleCluster,String>();
		clusterFeatureIdMap = new TreeMap<String, Collection<String>>();
		
		Connection conn = ConnectionManager.getConnection();
		String clusterQuery = 
				"SELECT CLUSTER_ID, PAR_SET_ID, MZ, RT, IS_LOCKED,  " +
				"MSMS_LIB_MATCH_ID, MSMS_ALT_ID " +
				"FROM MSMS_CLUSTER WHERE CDS_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(clusterQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		
		String featureQuery = 
				"SELECT MSMS_FEATURE_ID FROM MSMS_CLUSTER_COMPONENT WHERE CLUSTER_ID = ?";
		PreparedStatement fps = conn.prepareStatement(featureQuery);

		ps.setString(1, dataSet.getId());
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		while(rs.next()) {
			
			MsFeatureInfoBundleCluster cluster = 
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
			
			Set<String>clusterFeatureIds = new TreeSet<String>();		
			fps.setString(1, cluster.getId());
			ResultSet frs = fps.executeQuery();
			while(frs.next())
				clusterFeatureIds.add(frs.getString(1));
						
			frs.close();
			clusterFeatureIdMap.put(cluster.getId(), clusterFeatureIds);
			clusters.add(cluster);
			processed++;
		}
		rs.close();
		ps.close();
		fps.close();
		ConnectionManager.releaseConnection(conn);
		
		featureIds = clusterFeatureIdMap.values().stream().
				flatMap(v -> v.stream()).collect(Collectors.toSet());
	}

	protected void getMsMsFeatures() throws Exception {
		
		taskDescription = "Fetching MS2 features ...";
		total = featureIds.size();
		processed = 0;	
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT F.FEATURE_ID, F.POLARITY, F.MZ_OF_INTEREST, F.RETENTION_TIME, " +
				"I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, S.EXPERIMENT_ID, S.SAMPLE_ID, " +
				"T.STOCK_SAMPLE_ID, I.INJECTION_ID, F2.COLLISION_ENERGY " +
				"FROM MSMS_PARENT_FEATURE F, " +
				"DATA_ANALYSIS_MAP M, " +
				"DATA_ACQUISITION_METHOD A, " +
				"INJECTION I, " +
				"PREPARED_SAMPLE P, " +
				"SAMPLE S, " +
				"STOCK_SAMPLE T, " +
				"MSMS_FEATURE F2 " +
				"WHERE F2.MSMS_FEATURE_ID = ? " +
				"AND F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID " +
				"AND  F2.PARENT_FEATURE_ID = F.FEATURE_ID " +
				"AND M.INJECTION_ID = I.INJECTION_ID " +
				"AND A.ACQ_METHOD_ID = I.ACQUISITION_METHOD_ID " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID " +
				"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID " +
				"AND F.BASE_PEAK IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		String msOneQuery =
				"SELECT ADDUCT_ID, COMPOSITE_ADDUCT_ID, MZ, HEIGHT "
				+ "FROM MSMS_PARENT_FEATURE_PEAK WHERE FEATURE_ID = ?";
		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);
		
		for(String msmsId : featureIds) {
			
			ps.setString(1, msmsId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				String id = rs.getString("FEATURE_ID");
				double rt = rs.getDouble("RETENTION_TIME");
				double mz = rs.getDouble("MZ_OF_INTEREST");
				String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
						MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" + 
						MRC2ToolBoxConfiguration.getRtFormat().format(rt);

				MsFeature f = new MsFeature(id, name, rt);
				polarity = Polarity.getPolarityByCode(rs.getString("POLARITY"));
				f.setPolarity(polarity);
				f.setAnnotatedObjectType(AnnotatedObjectType.MSMS_FEATURE);
				
				MassSpectrum spectrum = new MassSpectrum();
				Map<Adduct, Collection<MsPoint>> adductMap =
						new TreeMap<Adduct,Collection<MsPoint>>();
				
				msOnePs.setString(1, id);
				ResultSet msOneRs = msOnePs.executeQuery();
				while(msOneRs.next()) {
					
					String adductId = msOneRs.getString("ADDUCT_ID");
					if(adductId == null)
						adductId = msOneRs.getString("COMPOSITE_ADDUCT_ID");

					Adduct adduct =
							AdductManager.getAdductById(adductId);

					if(adduct == null)
						continue;

					if(!adductMap.containsKey(adduct))
						adductMap.put(adduct, new ArrayList<MsPoint>());

					adductMap.get(adduct).add(
							new MsPoint(msOneRs.getDouble("MZ"), msOneRs.getDouble("HEIGHT")));
				}
				msOneRs.close();
				adductMap.entrySet().stream().
					forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

				f.setSpectrum(spectrum);
				
				MsFeatureInfoBundle bundle = new MsFeatureInfoBundle(f);
				bundle.setAcquisitionMethod(
					IDTDataCash.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID")));
				bundle.setDataExtractionMethod(
					IDTDataCash.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID")));
				bundle.setExperiment(
					IDTDataCash.getExperimentById(rs.getString("EXPERIMENT_ID")));
				StockSample stockSample =
					IDTDataCash.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
				bundle.setStockSample(stockSample);
				IDTExperimentalSample sample =
					IDTUtils.getExperimentalSampleById(rs.getString("SAMPLE_ID"), conn);
				bundle.setSample(sample);
				bundle.setInjectionId(rs.getString("INJECTION_ID"));
				features.add(bundle);
			}			
			rs.close();
			processed++;
		}		
		ps.close();
		msOnePs.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Remove redundant
		features = features.stream().distinct().collect(Collectors.toSet());
	}
		
	@Override
	public Task cloneTask() {
		return new IDTMSMSClusterDataPullTask(dataSet);
	}

	public MSMSClusterDataSet getDataSet() {
		return dataSet;
	}
}
