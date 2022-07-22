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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class IDTMSMSFeatureDataPullWithFilteringTask extends IDTMSMSFeatureDataPullTask {

	private Collection<LIMSExperiment>selectedExperiments;
	private Collection<DataPipeline> dataPipelines;
	private Collection<MinimalMSOneFeature>lookupFeatures;
	private MSMSClusteringParameterSet clusteringParams;
	
	private Collection<MsFeatureInfoBundle> filteredMsmsFeatures;
	private Collection<MsFeatureInfoBundleCluster>featureClusters;
	private MSMSClusterDataSet msmsClusterDataSet;
	private double rtError;
	private double mzError;
	private MassErrorType mzErrorType;
	private double minMsMsScore;
	private static final double SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT = 0.01d;
		
	public IDTMSMSFeatureDataPullWithFilteringTask(
			Collection<LIMSExperiment> selectedExperiments, 
			Collection<DataPipeline> dataPipelines,
			Collection<MinimalMSOneFeature> mzrtFeatureList, 
			MSMSClusteringParameterSet clusteringParams) {
		super(null);
		this.selectedExperiments = selectedExperiments;
		this.dataPipelines = dataPipelines;
		this.lookupFeatures = mzrtFeatureList;
		this.clusteringParams = clusteringParams;
		
		features = new ArrayList<MsFeatureInfoBundle>();
		msmsClusterDataSet = new MSMSClusterDataSet(
				"Active data set", 
				"", 
				MRC2ToolBoxCore.getIdTrackerUser());
		msmsClusterDataSet.setParameters(clusteringParams);	
		featureClusters = msmsClusterDataSet.getClusters();
		rtError = clusteringParams.getRtErrorValue();
		mzError = clusteringParams.getMzErrorValue();
		mzErrorType = clusteringParams.getMassErrorType();
		minMsMsScore = clusteringParams.getMsmsSimilarityCutoff();
	}

	@Override
	public void run() {
		
		taskDescription = "Getting feature data from IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			getMsMsFeaturesByExperimentAndDataPipeline();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		if(lookupFeatures != null && !lookupFeatures.isEmpty()) {
			
			try {
				clusterFilteredFeatures();
			}
			catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
		else {
			clusterAllFeatures();
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void getMsMsFeaturesByExperimentAndDataPipeline() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT F.FEATURE_ID, F2.MSMS_FEATURE_ID, F.POLARITY,  " +
				"F.MZ_OF_INTEREST, F.RETENTION_TIME, S.SAMPLE_ID,  " +
				"T.STOCK_SAMPLE_ID, I.INJECTION_ID, F2.COLLISION_ENERGY  " +
				"FROM MSMS_PARENT_FEATURE F,  " +
				"DATA_ANALYSIS_MAP M,  " +
				"DATA_ACQUISITION_METHOD A,  " +
				"INJECTION I,  " +
				"PREPARED_SAMPLE P,  " +
				"SAMPLE S,  " +
				"STOCK_SAMPLE T,  " +
				"MSMS_FEATURE F2  " +
				"WHERE F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID  " +
				"AND  F2.PARENT_FEATURE_ID = F.FEATURE_ID  " +
				"AND M.INJECTION_ID = I.INJECTION_ID  " +
				"AND A.ACQ_METHOD_ID = ?  " +
				"AND M.EXTRACTION_METHOD_ID = ? " +
				"AND S.EXPERIMENT_ID = ? " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID  " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID  " +
				"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID  " +
				"AND F.BASE_PEAK IS NOT NULL ";
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		
		String msOneQuery =
				"SELECT ADDUCT_ID, COMPOSITE_ADDUCT_ID, MZ, HEIGHT "
				+ "FROM MSMS_PARENT_FEATURE_PEAK WHERE FEATURE_ID = ?";
		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);		

		for(LIMSExperiment experiment : selectedExperiments) {
			
			for(DataPipeline pipeline : dataPipelines){
			
				taskDescription = "Fetching MS2 features for experiment " + 
						experiment.getId() + " (DP " + pipeline.getCode() +")";
				
				ps.setString(1, pipeline.getAcquisitionMethod().getId());
				ps.setString(2, pipeline.getDataExtractionMethod().getId());
				ps.setString(3, experiment.getId());
				
				ResultSet rs = ps.executeQuery();
				total = 100;
				processed = 0;
				if (rs.last()) {
					total = rs.getRow();
				  rs.beforeFirst();
				}
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
					bundle.setAcquisitionMethod(pipeline.getAcquisitionMethod());
					bundle.setDataExtractionMethod(pipeline.getDataExtractionMethod());
					bundle.setExperiment(experiment);
					StockSample stockSample =
						IDTDataCash.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
					bundle.setStockSample(stockSample);
					IDTExperimentalSample sample =
						IDTUtils.getExperimentalSampleById(rs.getString("SAMPLE_ID"), conn);
					bundle.setSample(sample);
					bundle.setInjectionId(rs.getString("INJECTION_ID"));
					features.add(bundle);
					
					processed++;
				}			
				rs.close();
			}
		}
		ps.close();
		msOnePs.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Remove redundant
		features = features.stream().distinct().collect(Collectors.toSet());
	}

	private void clusterFilteredFeatures() {

		taskDescription = "Clustering MSMS features based on filter list ...";
		total = lookupFeatures.size();
		processed = 0;
		filteredMsmsFeatures = new HashSet<MsFeatureInfoBundle>();
		
		for(MinimalMSOneFeature b : lookupFeatures) {
			
			Range rtRange = new Range(b.getRt() - rtError, b.getRt() + rtError);
			Range mzRange = MsUtils.createMassRange(b.getMz(), mzError, mzErrorType);
			List<MsFeatureInfoBundle> clusterFeatures = features.stream().
				filter(f -> f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum() != null).
				filter(f -> rtRange.contains(f.getRetentionTime())).
				filter(f -> mzRange.contains(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent().getMz())).
				collect(Collectors.toList());
			if(clusterFeatures.isEmpty()) {
				processed++;
				continue;
			}	
			while(!clusterFeatures.isEmpty()) {
				MsFeatureInfoBundleCluster newCluster = 
						clusterBasedOnMSMSSimilarity(b, clusterFeatures);
				featureClusters.add(newCluster);				
			}
			processed++;
		}
	}

	private void clusterAllFeatures() {
		// TODO Auto-generated method stub
		taskDescription = "Clustering all MSMS features ...";
		total = features.size();
		processed = 0;
		filteredMsmsFeatures = new HashSet<MsFeatureInfoBundle>();
		
		for(MsFeatureInfoBundle feature : features) {
			
			for(MsFeatureInfoBundleCluster cluster : featureClusters) {
				
			}
			processed++;
		}		
	}
	
	private MsFeatureInfoBundleCluster clusterBasedOnMSMSSimilarity(
			MinimalMSOneFeature b,
			List<MsFeatureInfoBundle> featuresToCluster) {
		
		if(featuresToCluster.isEmpty())
			return null;
		
		if(featuresToCluster.size() == 1) {		
			MsFeatureInfoBundleCluster newCluster = new MsFeatureInfoBundleCluster(b);
			newCluster.addComponent(featuresToCluster.get(0));
			featuresToCluster.clear();
			return newCluster;
		}
		if(featuresToCluster.size() > 1) {
			
			List<MsFeatureInfoBundle> featuresToRemove = 
					new ArrayList<MsFeatureInfoBundle>();
			MsFeatureInfoBundleCluster newCluster = new MsFeatureInfoBundleCluster(b);
			MsFeatureInfoBundle maxInt = featuresToCluster.get(0);
			double maxArea = maxInt.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getTotalIntensity();
			for(int i=1; i<featuresToCluster.size(); i++) {
				double area = featuresToCluster.get(i).getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getTotalIntensity();
				if(area > maxArea) {
					maxArea = area;
					maxInt = featuresToCluster.get(i);
				}
			}
			newCluster.addComponent(maxInt);
			Collection<MsPoint> refMsMs = maxInt.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getSpectrum();
			featuresToRemove.add(maxInt);
			for(int i=0; i<featuresToCluster.size(); i++) {
				
				MsFeatureInfoBundle f = featuresToCluster.get(i);
				if(f.equals(maxInt))
					continue;
				
				Collection<MsPoint>msms = f.getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getSpectrum();
				
				double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
						msms, refMsMs, mzError, mzErrorType, SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT);
				if(score >= minMsMsScore) {
					newCluster.addComponent(f);
					featuresToRemove.add(f);
				}
			}
			featuresToCluster.removeAll(featuresToRemove);
			return newCluster;
		}		
		return null;
	}
	
	public Collection<MinimalMSOneFeature> getLookupFeatures() {
		return lookupFeatures;
	}

	public MSMSClusterDataSet getMsmsClusterDataSet() {
		return msmsClusterDataSet;
	}
	
	@Override
	public Task cloneTask() {
		return new IDTMSMSFeatureDataPullWithFilteringTask(
				selectedExperiments, 
				dataPipelines,
				lookupFeatures,
				clusteringParams);
	}
}
