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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureLookupDataSetUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class RawDataAnalysisExperimentDatabaseUploadTask extends AbstractTask implements TaskListener {

	private RawDataAnalysisExperiment experiment;
	private double msOneMZWindow;
	private int processedFiles;
	private Map<String,String>featureIdMap;
	private DataExtractionMethod deMethod;
	private Map<String, MsFeatureChromatogramBundle> newChromatogramMap;
	
	public RawDataAnalysisExperimentDatabaseUploadTask(
			RawDataAnalysisExperiment experiment,
			double msOneMZWindow) {
		super();
		this.experiment = experiment;
		this.msOneMZWindow = msOneMZWindow;
		processedFiles = 0;
		featureIdMap = new HashMap<String,String>();
		newChromatogramMap = 
				new HashMap<String, MsFeatureChromatogramBundle>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Refreshing method list ...";
		total = 100;
		processed = 20;
		IDTDataCache.refreshDataExtractionMethodList();
		//	String metodId = experiment.getMsmsExtractionParameterSet().getId();
		deMethod = IDTDataCache.getDataExtractionMethodByMd5(
				experiment.getMsmsExtractionParameterSet().getParameterSetHash());
		if(deMethod == null) {
			errorMessage = "Data extraction method not defined!";
			setStatus(TaskStatus.ERROR);
			return;
		}
		boolean metadataUploaded = false;
		try {
			metadataUploaded = uploadExperimentMetadata();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(!metadataUploaded) {
			clearUploadedMetadata();
			errorMessage = "Experiment upload failed due to database error.";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = "Uploading MSMS analysis results ...";
		processed = 50;
		try {
			initFeatureDataUpload();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}
	
	private void clearUploadedMetadata() {

		taskDescription = "Rolling back data upload due to error ...";
		total = 100;
		processed = 20;
		
		String expId = experiment.getIdTrackerExperiment().getId();
		IDTDataCache.refreshExperimentList();
		IDTDataCache.refreshExperimentSamplePrepMap();
		if(IDTDataCache.getExperimentById(expId) != null) {
			
			try {
				IDTUtils.deleteExperiment(experiment.getIdTrackerExperiment());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean uploadExperimentMetadata() {
		
		taskDescription = "Uploading metadata ...";
		total = 100;
		processed = 35;
		
		//	Add experiment
		if(!insertNewExperiment())
			return false;
		
		if(!insertSamples())
			return false;
		
		if(!insertSampleprep())
			return false;
		
		if(!insertWorklist())
			return false;		
		
		return true;
	}

	private boolean insertWorklist() {

		Worklist newWorklist =  experiment.getWorklist();
		Map<String, String> prepItemMap = new TreeMap<String, String>();	
		experiment.getIdTrackerExperiment().getSamplePreps().
			forEach(p -> prepItemMap.putAll(p.getPrepItemMap()));
		newWorklist.getWorklistItems().stream().
			filter(LIMSWorklistItem.class::isInstance).
			map(LIMSWorklistItem.class::cast).
			forEach(i -> i.setPrepItemId(prepItemMap.get(i.getSample().getId())));	
		try {
			IDTUtils.uploadInjectionData(newWorklist);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		IDTDataCache.refreshSamplePrepDataPipelineMap();
		return true;
	}

	private boolean insertSampleprep() {

		Collection<IDTExperimentalSample>samples = 
				experiment.getIdTrackerExperiment().getExperimentDesign().getSamples().stream().
				filter(IDTExperimentalSample.class::isInstance).
				map(IDTExperimentalSample.class::cast).
				collect(Collectors.toList());
		
		for(LIMSSamplePreparation prep : experiment.getIdTrackerExperiment().getSamplePreps()) {
			
			if(prep.getId() != null 
					&& IDTDataCache.getSamplePrepById(prep.getId()) != null) {
				continue;
			}
			try {
				IDTUtils.addNewSamplePrepWithSopsAndAnnotations(prep, samples);	
				IDTDataCache.getSamplePreps().add(prep);
				if(IDTDataCache.getExperimentSamplePrepMap().get(experiment.getIdTrackerExperiment()) == null)
					IDTDataCache.getExperimentSamplePrepMap().
						put(experiment.getIdTrackerExperiment(), new TreeSet<LIMSSamplePreparation>());
				
				IDTDataCache.getExperimentSamplePrepMap().get(experiment.getIdTrackerExperiment()).add(prep);
					
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}		
		}
		return true;
	}

	private boolean insertSamples() {
		
		TreeSet<ExperimentalSample> samples = 
				experiment.getIdTrackerExperiment().getExperimentDesign().getSamples();
		for(ExperimentalSample sample : samples) {
			
			ExperimentalSample existingSample = null;
			if(sample.getId() != null) {
				try {
					existingSample = IDTUtils.getExperimentalSampleById(sample.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(existingSample == null) {
				
				try {
					String sampleId = IDTUtils.addNewIDTSample(
							(IDTExperimentalSample) sample, experiment.getIdTrackerExperiment());
					sample.setId(sampleId);
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}		
		}		
		return true;
	}
	
	private boolean insertNewExperiment() {
		
		LIMSExperiment newExperiment = experiment.getIdTrackerExperiment();
		
		//	If experiment already in the database
		if(newExperiment.getId() != null) {
			
			LIMSExperiment existing = 
					IDTDataCache.getExperimentById(newExperiment.getId());
			if(existing != null)
				return true;
		}			
		String experimentId = null;
		try {
			experimentId = IDTUtils.addNewExperiment(newExperiment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(experimentId != null) {
			newExperiment.setId(experimentId);
			IDTDataCache.getExperiments().add(newExperiment);
			newExperiment.getProject().getExperiments().add(newExperiment);
			return true;
		}
		else {
			return false;
		}		
	}
	
	private void initFeatureDataUpload() {
		
		taskDescription = "Uploading MSMS features and chromatograms ...";
		total = 100;
		processed = 40;		
		for(DataFile df : experiment.getMSMSDataFiles()) {
			
			RawDataAnalysisMSFeatureDatabaseUploadTask task = 				
					new RawDataAnalysisMSFeatureDatabaseUploadTask(
							experiment, df, deMethod, msOneMZWindow);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	@Override
	public Task cloneTask() {
		return new RawDataAnalysisExperimentDatabaseUploadTask(
				experiment, msOneMZWindow);
	}

	public RawDataAnalysisExperiment getExperiment() {
		return experiment;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(RawDataAnalysisMSFeatureDatabaseUploadTask.class)) {	
				RawDataAnalysisMSFeatureDatabaseUploadTask task = 
						(RawDataAnalysisMSFeatureDatabaseUploadTask)e.getSource();
				featureIdMap.putAll(task.getFeatureIdMap());
				newChromatogramMap.putAll(task.getChromatogramMap());
				processedFiles++;
			}
			if(processedFiles == experiment.getMSMSDataFiles().size()) {
					 
				 experiment.getChromatogramMap().clear();
				 experiment.getChromatogramMap().putAll(newChromatogramMap);
				
				if(!experiment.getEditableMsFeatureInfoBundleCollections().isEmpty() 
						|| !experiment.getMsmsClusterDataSets().isEmpty())
					uploadFeatureAndClusterCollections();
				else
					setStatus(TaskStatus.FINISHED);
			}
		}		
	}

	private void uploadFeatureAndClusterCollections() {
		
		if(!experiment.getEditableMsFeatureInfoBundleCollections().isEmpty()) {
			try {
				uploadFeatureCollections();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(!experiment.getMsmsClusterDataSets().isEmpty()) {
			try {
				uploadMSMSClusterCollections();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		setStatus(TaskStatus.FINISHED);
	}

	private void uploadMSMSClusterCollections() throws Exception {
		
		taskDescription = "Uploading MSMS cluster data sets ...";
		Connection conn = ConnectionManager.getConnection();
		
		for(MSMSClusterDataSet dataSet : experiment.getMsmsClusterDataSets()) {
			
			total = dataSet.getClusters().size();
			processed = 0;	
			
			//	Insert data set
			MSMSClusteringParameterSet parSet = null;
			try {
				parSet = MSMSClusteringDBUtils.insertMSMSClusterDataSet(dataSet, conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ConnectionManager.releaseConnection(conn);	
				return;
			}
			if(dataSet.getFeatureLookupDataSet() != null) {
					
				FeatureLookupDataSet flds = FeatureLookupDataSetManager.getFeatureLookupDataSetById(
						dataSet.getFeatureLookupDataSet().getId());
				if(flds == null)
					FeatureLookupDataSetUtils.addFeatureLookupDataSet(
							dataSet.getFeatureLookupDataSet(), conn);
				
				FeatureLookupDataSetManager.getFeatureLookupDataSetList().add(
						dataSet.getFeatureLookupDataSet());			
			}
			//	Insert cluster data
			addClustersForDataSet(dataSet, conn);
		}
		ConnectionManager.releaseConnection(conn);	
	}
	
	private void addClustersForDataSet(
			MSMSClusterDataSet dataSet,
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
		ps.setString(2, dataSet.getParameters().getId());
		
		for(MsFeatureInfoBundleCluster cluster : dataSet.getClusters()) {
			
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
			
			//	Debug only 
//			Collection<String> matchIds = cluster.getComponents().stream().
//					flatMap(c -> c.getMsFeature().getIdentifications().stream()).
//					filter(i -> i.getReferenceMsMsLibraryMatch() != null).
//					map(i -> i.getUniqueId()).collect(Collectors.toCollection(TreeSet::new));
//			if(!matchIds.isEmpty())
//				System.err.print(StringUtils.join(matchIds, "\n"));
			
			if(cluster.getPrimaryIdentity() != null) {
				
				if(cluster.getPrimaryIdentity().getReferenceMsMsLibraryMatch() != null) {
					msmsLibMatchId = cluster.getPrimaryIdentity().getUniqueId();
					//	Debug only 
					//	System.err.println(msmsLibMatchId);
				}
				
				if(cluster.getPrimaryIdentity().getIdSource().equals(CompoundIdSource.MANUAL))
					altId = cluster.getPrimaryIdentity().getUniqueId();
			}			
			ps.setString(1, clusterId);			
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

	private void uploadFeatureCollections() throws Exception {
		
		taskDescription = "Uploading MSMS cluster data sets ...";
		total = experiment.getEditableMsFeatureInfoBundleCollections().size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		for(MsFeatureInfoBundleCollection fColl : experiment.getEditableMsFeatureInfoBundleCollections()) {
			
			FeatureCollectionUtils.addNewMsFeatureInformationBundleCollection(fColl, conn);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);		
	}
}















