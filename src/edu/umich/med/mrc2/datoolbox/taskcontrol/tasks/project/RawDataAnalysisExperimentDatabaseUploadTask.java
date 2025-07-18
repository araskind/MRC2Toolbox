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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.MSMSClusterTask;

public class RawDataAnalysisExperimentDatabaseUploadTask extends MSMSClusterTask implements TaskListener {

	private RawDataAnalysisProject experiment;
	private double msOneMZWindow;
	private int processedFiles;
	private Map<String,String>featureIdMap;
	private DataExtractionMethod deMethod;
	private Map<String, MsFeatureChromatogramBundle> newChromatogramMap;
	
	public RawDataAnalysisExperimentDatabaseUploadTask(
			RawDataAnalysisProject experiment,
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
			return;
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
			return;
		}
	}
	
	private void clearUploadedMetadata() {

		taskDescription = "Rolling back data upload due to error ...";
		total = 100;
		processed = 20;
		
		String expId = experiment.getLimsExperiment().getId();
		IDTDataCache.refreshExperimentList();
		IDTDataCache.refreshExperimentSamplePrepMap();
		if(IDTDataCache.getExperimentById(expId) != null) {
			
			try {
				IDTUtils.deleteExperiment(experiment.getLimsExperiment());
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
		experiment.getLimsExperiment().getSamplePreps().
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
				experiment.getLimsExperiment().getExperimentDesign().getSamples().stream().
				filter(IDTExperimentalSample.class::isInstance).
				map(IDTExperimentalSample.class::cast).
				collect(Collectors.toList());
		
		for(LIMSSamplePreparation prep : experiment.getLimsExperiment().getSamplePreps()) {
			
			if(prep.getId() != null 
					&& IDTDataCache.getSamplePrepById(prep.getId()) != null) {
				continue;
			}
			try {
				IDTUtils.addNewSamplePrepWithSopsAndAnnotations(prep, samples);	
				IDTDataCache.getSamplePreps().add(prep);
				if(IDTDataCache.getExperimentSamplePrepMap().get(experiment.getLimsExperiment()) == null)
					IDTDataCache.getExperimentSamplePrepMap().
						put(experiment.getLimsExperiment(), new TreeSet<LIMSSamplePreparation>());
				
				IDTDataCache.getExperimentSamplePrepMap().get(experiment.getLimsExperiment()).add(prep);
					
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
				experiment.getLimsExperiment().getExperimentDesign().getSamples();
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
							(IDTExperimentalSample) sample, experiment.getLimsExperiment());
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
		
		LIMSExperiment newExperiment = experiment.getLimsExperiment();
		
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

	public RawDataAnalysisProject getExperiment() {
		return experiment;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(RawDataAnalysisMSFeatureDatabaseUploadTask.class))
				 finalizeRawDataAnalysisMSFeatureDatabaseUploadTask(
						 (RawDataAnalysisMSFeatureDatabaseUploadTask)e.getSource());
		}		
	}
	
	private synchronized void finalizeRawDataAnalysisMSFeatureDatabaseUploadTask(
			RawDataAnalysisMSFeatureDatabaseUploadTask task) {
		
		featureIdMap.putAll(task.getFeatureIdMap());
		newChromatogramMap.putAll(task.getChromatogramMap());
		processedFiles++;
		
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
		for(IMSMSClusterDataSet dataSet : experiment.getMsmsClusterDataSets()) {
			
			total = dataSet.getClusters().size();
			processed = 0;	
			insertNewMSMSClusterDataSet(dataSet, conn);
		}
		ConnectionManager.releaseConnection(conn);	
	}

	private void uploadFeatureCollections() throws Exception {
		
		taskDescription = "Uploading feature collections ...";
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















