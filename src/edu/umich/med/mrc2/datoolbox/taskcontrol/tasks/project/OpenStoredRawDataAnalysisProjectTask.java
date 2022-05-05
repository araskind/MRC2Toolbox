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
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineProjectLoadCash;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.FeatureCollectionFields;
import edu.umich.med.mrc2.datoolbox.project.store.LIMSExperimentFields;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectFields;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameters;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.zip.ParallelZip;

public class OpenStoredRawDataAnalysisProjectTask extends AbstractTask implements TaskListener {

	private RawDataAnalysisProject project;
	private File projectFile;
	private boolean loadResults;
	private File xmlProjectDir;
	private int featureFileCount;
	private int processedFiles;
	private boolean featureReadCompleted;
	private boolean chromatogramReadCompleted;
	
	private Set<String>uniqueCompoundIds;
	private Set<String>uniqueMSMSLibraryIds;
	private Set<String>uniqueMSRTLibraryIds;
	private Set<String>uniqueSampleIds;
	
	private ArrayList<String>errors;
	
	public OpenStoredRawDataAnalysisProjectTask(File projectFile, boolean loadResults) {
		this.projectFile = projectFile;
		this.loadResults = loadResults;
		errors = new ArrayList<String>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 0;
		processedFiles = 0;
		xmlProjectDir = Paths.get(projectFile.getParentFile().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.UNCOMPRESSED_PROJECT_FILES_DIRECTORY).toFile();
		File chromatogramsFile = null;
//		try {
//			extractProject();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			setStatus(TaskStatus.ERROR);
//		}
		Iterator<File> i = 
				FileUtils.iterateFiles(xmlProjectDir, new String[] { "xml" }, true);
		Collection<File>featureFiles = new ArrayList<File>();
		while (i.hasNext()) {
			
			File file = i.next();
			if(file.getName().equals(MRC2ToolBoxConfiguration.PROJECT_FILE_NAME)) {
				try {
					parseProjectFile(file);
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
					new OpenChromatogramFileTask(chromatogramsFile, project.getDataFiles());
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		else {
			chromatogramReadCompleted = true;
		}
	}
	
	private DataFile getDataFileForFeatureFile(File featureFile) {
		
		String fileName = FilenameUtils.getBaseName(featureFile.getName());		
		for(DataFile df : project.getDataFiles()) {
			
			String dfName = FilenameUtils.getBaseName(df.getName());
			if(fileName.equals(dfName))
				return df;
		}
		return null;
	}
	
	private void parseProjectFile(File projectXmlFile) throws Exception {
		
		taskDescription = "Parsing project file ...";
		total = 100;
		processed = 10;
		
		uniqueCompoundIds = new TreeSet<String>();
		uniqueMSMSLibraryIds = new TreeSet<String>();
		uniqueMSRTLibraryIds = new TreeSet<String>();
		uniqueSampleIds = new TreeSet<String>();
		
		SAXBuilder sax = new SAXBuilder();
		Document doc = sax.build(projectXmlFile);
		Element projectElement = doc.getRootElement();
		
		String id = projectElement.getAttributeValue(ProjectFields.Id.name()); 
		String name = projectElement.getAttributeValue(ProjectFields.Name.name()); 
		String description = projectElement.getAttributeValue(ProjectFields.Description.name()); 
		Date dateCreated = ProjectUtils.dateTimeFormat.parse(
				projectElement.getAttributeValue(ProjectFields.DateCreated.name())); 
		Date lastModified = ProjectUtils.dateTimeFormat.parse(
				projectElement.getAttributeValue(ProjectFields.DateModified.name())); 		
		project = new RawDataAnalysisProject(
				id, 
				name, 
				description, 
				projectFile, 
				dateCreated, 
				lastModified);
		
		String userId = projectElement.getAttributeValue(ProjectFields.UserId.name()); 
		if(userId != null)
			project.setCreatedBy(IDTDataCash.getUserById(userId)); 
		
		if(project.getCreatedBy() == null)
			project.setCreatedBy(MRC2ToolBoxCore.getIdTrackerUser());
		
		String instrumentId = projectElement.getAttributeValue(ProjectFields.Instrument.name()); 
		if(instrumentId != null)
			project.setInstrument(IDTDataCash.getInstrumentById(instrumentId));
		
		Element msmsParamsElement = 
				projectElement.getChild(MSMSExtractionParameters.MSMSExtractionParameterSet.name());
		if(msmsParamsElement != null) {
			MSMSExtractionParameterSet msmsParamSet = new MSMSExtractionParameterSet(msmsParamsElement);
			project.setMsmsExtractionParameterSet(msmsParamSet);
		}
		
		String compoundIdList = 
				projectElement.getChild(ProjectFields.UniqueCIDList.name()).getText();
		uniqueCompoundIds.addAll(getIdList(compoundIdList));
		
		String msmsLibIdIdList = 
				projectElement.getChild(ProjectFields.UniqueMSMSLibIdList.name()).getText();
		uniqueMSMSLibraryIds.addAll(getIdList(msmsLibIdIdList));

		String msRtLibIdIdList = 
				projectElement.getChild(ProjectFields.UniqueMSRTLibIdList.name()).getText();
		uniqueMSRTLibraryIds.addAll(getIdList(msRtLibIdIdList));

		String sampleIdIdList = 
				projectElement.getChild(ProjectFields.UniqueSampleIdList.name()).getText();
		uniqueSampleIds.addAll(getIdList(sampleIdIdList));
				
		try {
			populateDatabaseCashData();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);			
		}		
		List<Element> msmsFileList = 
				projectElement.getChild(ProjectFields.MsTwoFiles.name()).
				getChildren(DataFileFields.DataFile.name());
		for (Element msmsFileElement : msmsFileList) {
			DataFile msmsFile = new DataFile(msmsFileElement);
			project.addMSMSDataFile(msmsFile);
		}
		//	MS1 files
		List<Element> msOneFileList = 
				projectElement.getChild(ProjectFields.MsOneFiles.name()).
				getChildren(DataFileFields.DataFile.name());
		for (Element msOneFileElement : msOneFileList) {
			DataFile msOneFile = new DataFile(msOneFileElement);
			project.addMSOneDataFile(msOneFile);
		}
		//	project.repopulateInjectionList();
		
		//	Feature collections
		Element featureCollectionElement = 
				projectElement.getChild(ProjectFields.FeatureCollectionList.name());
		if(featureCollectionElement != null) {
			List<Element> featureCollectionList = featureCollectionElement.
					getChildren(FeatureCollectionFields.FeatureCollection.name());
			for (Element fce : featureCollectionList)
				project.addMsFeatureInfoBundleCollection(
						new MsFeatureInfoBundleCollection(fce));
		}
		Element experimentElement = 
				projectElement.getChild(LIMSExperimentFields.limsExperiment.name());
		if(experimentElement != null) {
			
			LIMSExperiment experiment = null;
			try {
				experiment = new LIMSExperiment(experimentElement, project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(experiment != null)
				project.setIdTrackerExperiment(experiment);
		}
	}
	
	private void populateDatabaseCashData() throws Exception {
		
		OfflineProjectLoadCash.reset();
		Connection conn = ConnectionManager.getConnection();
		if(!uniqueCompoundIds.isEmpty()) {
			try {
				getCompoundIdentities(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!uniqueMSMSLibraryIds.isEmpty()) {
			try {
				getMSMSLibraryEntries(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!uniqueMSRTLibraryIds.isEmpty()) {
			try {
				getMSRTLibraryEntries(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!uniqueSampleIds.isEmpty()) {
			try {
				getExperimentalSamples(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void getCompoundIdentities(Connection conn) throws Exception {
		
		taskDescription = "Populating compound data cash ...";
		total = uniqueCompoundIds.size();
		processed = 0;
		
		for(String cid : uniqueCompoundIds) {
			
			CompoundIdentity compId = 
					CompoundDatabaseUtils.getCompoundById(cid, conn);
			if(compId != null)
				OfflineProjectLoadCash.addCompoundIdentity(compId);
			
			processed++;
		}		
	}

	private void getMSMSLibraryEntries(Connection conn) throws Exception {
		
		taskDescription = "Populating MSMS library data cash ...";
		total = uniqueMSMSLibraryIds.size();
		processed = 0;		
		for(String libId : uniqueMSMSLibraryIds) {
			
			MsMsLibraryFeature libFeature = 
					MSMSLibraryUtils.getMsMsLibraryFeatureById(libId, conn);
			if(libFeature != null)
				OfflineProjectLoadCash.addMsMsLibraryFeature(libFeature);
			
			processed++;
		}	
	}
	
	private void getMSRTLibraryEntries(Connection conn) throws Exception {
		
		taskDescription = "Populating MS-RT library data cash ...";
		total = uniqueMSRTLibraryIds.size();
		processed = 0;		
		for(String libId : uniqueMSRTLibraryIds) {
			
			LibraryMsFeatureDbBundle bundle = null;	// TODO Auto-generated method stub
			if(bundle != null)
				OfflineProjectLoadCash.addLibraryMsFeatureDbBundle(bundle);
			
			processed++;
		}
	}
	
	private void getExperimentalSamples(Connection conn) throws Exception {

		Collection<IDTExperimentalSample>samples = 
				IDTUtils.getExperimentalSamples(uniqueSampleIds, conn);
		
		for(IDTExperimentalSample sample :samples)
			OfflineProjectLoadCash.addExperimentalSample(sample);		
	}
	
	private Collection<String>getIdList(Element idListElement){
		
		if(idListElement.getText().isEmpty())
			return Arrays.asList(new String[0]);
		else {
			return Arrays.asList(idListElement.getText().split(","));
		}
	}
	
	private Collection<String>getIdList(String idListString){
		
		if(idListString == null || idListString.isEmpty())
			return Arrays.asList(new String[0]);
		else {
			return Arrays.asList(idListString.split(","));
		}
	}

	private void extractProject() throws Exception {
		taskDescription = "Extracting project files ...";
		total = 100;
		processed = 30;
		ParallelZip.extractZip(
				projectFile.getAbsolutePath(), 
				xmlProjectDir.getAbsolutePath());
	}

	@Override
	public Task cloneTask() {
		return new OpenStoredRawDataAnalysisProjectTask(projectFile, loadResults);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(OpenMsFeatureBundleFileTask.class)) {	
				processedFiles++;
				
				OpenMsFeatureBundleFileTask task = (OpenMsFeatureBundleFileTask)e.getSource();
				project.addMsFeaturesForDataFile(task.getDataFile(), task.getFeatures());
				if(processedFiles == featureFileCount) {
					populateFeatureCollections();
					featureReadCompleted = true;	
				}
			}	
			if (e.getSource().getClass().equals(OpenChromatogramFileTask.class)) {
				chromatogramReadCompleted = true;			
				project.setChromatogramMap(
						((OpenChromatogramFileTask)e.getSource()).getChromatogramMap());
			}			
			if(featureReadCompleted && chromatogramReadCompleted)
				cleanup();
		}
	}

	private void populateFeatureCollections() {

		if(project.getEditableMsFeatureInfoBundleCollections().isEmpty())
			return;
		
		for(MsFeatureInfoBundleCollection fc : project.getEditableMsFeatureInfoBundleCollections())	
			fc.addFeatures(project.getFeatureBundlesForIds(fc.getFeatureIds()));	
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
	
	public RawDataAnalysisProject getProject() {
		return project;
	}
}
