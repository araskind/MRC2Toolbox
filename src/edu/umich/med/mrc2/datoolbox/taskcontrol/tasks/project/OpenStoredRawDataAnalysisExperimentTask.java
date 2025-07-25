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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.IDTrackerProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameters;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class OpenStoredRawDataAnalysisExperimentTask extends OpenStandaloneProjectAbstractTask implements TaskListener {

	private RawDataAnalysisProject experiment;
	private File experimentFile;
	private boolean loadResults;
	private File xmlExperimentFileDir;
	private int featureFileCount;
	private int processedFiles;
	private boolean featureReadCompleted;
	private boolean chromatogramReadCompleted;

	private ArrayList<String>errors;
	
	public OpenStoredRawDataAnalysisExperimentTask(
			File experimentFile, 
			boolean loadResults) {
		this.experimentFile = experimentFile;
		this.loadResults = loadResults;
		errors = new ArrayList<String>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 0;
		processedFiles = 0;
		xmlExperimentFileDir = 
				Paths.get(experimentFile.getParentFile().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.UNCOMPRESSED_EXPERIMENT_FILES_DIRECTORY).toFile();
		File chromatogramsFile = null;
		Iterator<File> i = 
				FileUtils.iterateFiles(xmlExperimentFileDir, new String[] { "xml" }, true);
		Collection<File>featureFiles = new ArrayList<File>();
		while (i.hasNext()) {
			
			File file = i.next();
			if(file.getName().equals(MRC2ToolBoxConfiguration.PROJECT_FILE_NAME)) {
				try {
					parseExperimentFile(file);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
			else if(file.getName().equals(MRC2ToolBoxConfiguration.FEATURE_CHROMATOGRAMS_FILE_NAME)) {
				chromatogramsFile = file;
			}		
			else {
				featureFiles.add(file);
			}
		}
		if(!loadResults) {
			cleanup();
			return;
		}
		featureFileCount = featureFiles.size();
		if(featureFileCount == 0 && chromatogramsFile == null) {
			cleanup();
			return;
		}
		if(featureFileCount > 0) {
			
			taskDescription = "Loading feature data ...";
			total = 100;
			processed = 20;
			for(File ff : featureFiles) {
				
				DataFile dataFile = getDataFileForFeatureFile(ff);
				OpenMsFeatureBundleFileTask task = 
						new OpenMsFeatureBundleFileTask(dataFile, ff);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
		if(chromatogramsFile != null) {
			
			OpenChromatogramFileTask task = 
					new OpenChromatogramFileTask(chromatogramsFile, experiment.getDataFiles());
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		else {
			chromatogramReadCompleted = true;
		}
	}
	
	private DataFile getDataFileForFeatureFile(File featureFile) {
		
		String fileName = FilenameUtils.getBaseName(featureFile.getName());		
		for(DataFile df : experiment.getDataFiles()) {
			
			String dfName = FilenameUtils.getBaseName(df.getName());
			if(fileName.equals(dfName))
				return df;
		}
		return null;
	}
	
	private void parseExperimentFile(File experimentXmlFile) throws Exception {
		
		taskDescription = "Parsing experiment file ...";
		total = 100;
		processed = 10;
		
		uniqueCompoundIds = new TreeSet<String>();
		uniqueMSMSLibraryIds = new TreeSet<String>();
		uniqueMSRTLibraryIds = new TreeSet<String>();
		uniqueSampleIds = new TreeSet<String>();
		
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(experimentXmlFile);
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;						
		}
		Element experimentElement = doc.getRootElement();
		
		String id = experimentElement.getAttributeValue(CommonFields.Id.name()); 
		
		//	TODO remove
		String name = experimentElement.getAttributeValue(CommonFields.Name.name());
		if(name == null)
			name = ProjectStoreUtils.getTextFromElement(experimentElement, CommonFields.Name);
		
		String description = experimentElement.getAttributeValue(CommonFields.Description.name()); 
		if(description == null)
			description = ProjectStoreUtils.getDescriptionFromElement(experimentElement);
		
		Date dateCreated = ProjectStoreUtils.getDateFromAttribute(
				experimentElement, CommonFields.DateCreated);
		Date lastModified = ProjectStoreUtils.getDateFromAttribute(
				experimentElement, CommonFields.LastModified);
		if(dateCreated == null)
			dateCreated = new Date();
		
		if(lastModified == null)
			lastModified = dateCreated;
		
		experiment = new RawDataAnalysisProject(
				id, 
				name, 
				description, 
				experimentFile, 
				dateCreated, 
				lastModified);
		
		LIMSUser createdBy = ProjectStoreUtils.getUserFromAttribute(experimentElement);
		if(createdBy == null)
			createdBy = MRC2ToolBoxCore.getIdTrackerUser();

		experiment.setCreatedBy(createdBy);
		
		String instrumentId = 
				experimentElement.getAttributeValue(IDTrackerProjectFields.Instrument.name()); 
		if(instrumentId != null)
			experiment.setInstrument(IDTDataCache.getInstrumentById(instrumentId));
		
		Element msmsParamsElement = 
				experimentElement.getChild(MSMSExtractionParameters.MSMSExtractionParameterSet.name());
		if(msmsParamsElement != null) {
			MSMSExtractionParameterSet msmsParamSet = new MSMSExtractionParameterSet(msmsParamsElement);
			experiment.setMsmsExtractionParameterSet(msmsParamSet);
		}		
		collectIdsForRetrievalFromDatabase(experimentElement);				
		try {
			populateDatabaseCacheData();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;		
		}	
		Element limsExperimentElement = 
				experimentElement.getChild(ObjectNames.limsExperiment.name());
		if(limsExperimentElement != null) {
			
			LIMSExperiment limsExperiment = null;
			try {
				limsExperiment = new LIMSExperiment(limsExperimentElement, experiment);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(limsExperiment != null)
				experiment.setLimsExperiment(limsExperiment);
		}
		if(experimentElement.getChild(IDTrackerProjectFields.Injections.name()) != null) {
			
			List<Element> injectionsList = 
					experimentElement.getChild(IDTrackerProjectFields.Injections.name()).
					getChildren(ObjectNames.Injection.name());			
			
			for (Element injectionElement : injectionsList) {
				Injection injection = new Injection(injectionElement);
				experiment.getInjections().add(injection);			
			}
		}
		List<Element> msmsFileList = 
				experimentElement.getChild(IDTrackerProjectFields.MsTwoFiles.name()).
				getChildren(ObjectNames.DataFile.name());
		for (Element msmsFileElement : msmsFileList) {
			DataFile msmsFile = new DataFile(msmsFileElement);
			experiment.addMSMSDataFile(msmsFile);
		}
		//	MS1 files
		List<Element> msOneFileList = 
				experimentElement.getChild(IDTrackerProjectFields.MsOneFiles.name()).
				getChildren(ObjectNames.DataFile.name());
		for (Element msOneFileElement : msOneFileList) {
			DataFile msOneFile = new DataFile(msOneFileElement);
			experiment.addMSOneDataFile(msOneFile);
		}
		//	Feature collections
		Element featureCollectionElement = 
				experimentElement.getChild(IDTrackerProjectFields.FeatureCollectionList.name());
		if(featureCollectionElement != null) {
			List<Element> featureCollectionList = featureCollectionElement.
					getChildren(ObjectNames.FeatureCollection.name());
			for (Element fce : featureCollectionList)
				experiment.addMsFeatureInfoBundleCollection(
						new MsFeatureInfoBundleCollection(fce));
		}
		Element msmsClusterListElement = 
				experimentElement.getChild(IDTrackerProjectFields.MSMSClusterDataSetList.name());
		if(msmsClusterListElement != null) {
			List<Element> msmsClusterDataSetList = msmsClusterListElement.
					getChildren(ObjectNames.MSMSClusterDataSet.name());
			for (Element mcds : msmsClusterDataSetList)
				experiment.getMsmsClusterDataSets().add(new MSMSClusterDataSet(mcds));
		}
	}
	

	@Override
	public Task cloneTask() {
		return new OpenStoredRawDataAnalysisExperimentTask(experimentFile, loadResults);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(OpenMsFeatureBundleFileTask.class))
				finalizeOpenMsFeatureBundleFileTask((OpenMsFeatureBundleFileTask)e.getSource());
				
			if (e.getSource().getClass().equals(OpenChromatogramFileTask.class))
				finalizeOpenChromatogramFileTask((OpenChromatogramFileTask)e.getSource());
						
			if(featureReadCompleted && chromatogramReadCompleted)
				cleanup();
		}
	}
	
	private synchronized void finalizeOpenMsFeatureBundleFileTask(OpenMsFeatureBundleFileTask task) {
		
		processedFiles++;

		LIMSExperiment limsExperiment = experiment.getLimsExperiment();
		if(limsExperiment != null)
			task.getFeatures().stream().forEach(f -> f.setExperiment(limsExperiment));
		
		experiment.addMsFeaturesForDataFile(task.getDataFile(), task.getFeatures());
		if(processedFiles == featureFileCount) {
			populateFeatureCollections();
			populateMSMSClusters();
			featureReadCompleted = true;	
		}
	}
	
	private synchronized void finalizeOpenChromatogramFileTask(OpenChromatogramFileTask task) {
		
		chromatogramReadCompleted = true;			
		experiment.setChromatogramMap(task.getChromatogramMap());
	}
	

	private void populateFeatureCollections() {

		if(experiment.getEditableMsFeatureInfoBundleCollections().isEmpty())
			return;
		
		taskDescription = "Populating feature collections ... ";
		total = experiment.getEditableMsFeatureInfoBundleCollections().size();
		processed = 0;
		for(MsFeatureInfoBundleCollection fc : experiment.getEditableMsFeatureInfoBundleCollections())	{
			fc.addFeatures(experiment.getFeatureBundlesForIds(fc.getFeatureIds()));	
			processed++;
		}
	}
	
	private void populateMSMSClusters() {

		if(experiment.getMsmsClusterDataSets().isEmpty())
			return;
		
		taskDescription = "Populating MSMS feature clusters ... ";
		for(IMSMSClusterDataSet ds : experiment.getMsmsClusterDataSets()) {	
			
			total = ds.getClusters().size();
			processed = 0;
			for(IMsFeatureInfoBundleCluster cluster : ds.getClusters()) {
				
				cluster.setFeatures(experiment.getFeatureBundlesForIds(cluster.getFeatureIds()));	
				cluster.replaceStoredPrimaryIdentityFromFeatures();
				processed++;
			}		
		}		
	}

	private void cleanup() {
				
//		if(xmlProjectDir != null) {
//			try {
//				FileUtils.deleteDirectory(xmlProjectDir);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		setStatus(TaskStatus.FINISHED);
	}
	
	public ArrayList<String> getErrors() {
		return errors;
	}
	
	public RawDataAnalysisProject getExperiment() {
		return experiment;
	}
}
