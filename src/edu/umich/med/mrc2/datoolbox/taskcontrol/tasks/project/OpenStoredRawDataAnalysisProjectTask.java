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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineProjectLoadCash;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.StoredDataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.StoredProjectFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;
import edu.umich.med.mrc2.datoolbox.utils.zip.ParallelZip;

public class OpenStoredRawDataAnalysisProjectTask extends AbstractTask implements TaskListener {

	private RawDataAnalysisProject project;
	private File projectFile;
	private File xmlProjectDir;
	private int featureFileCount;
	private int processedFiles;
	
	private Set<String>uniqueCompoundIds;
	private Set<String>uniqueMSMSLibraryIds;
	private Set<String>uniqueMSRTLibraryIds;
	private Set<String>uniqueSampleIds;
	
	private ArrayList<String>errors;
	
	public OpenStoredRawDataAnalysisProjectTask(File projectFile) {
		this.projectFile = projectFile;
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
			else {
				featureFiles.add(file);
			}
		}
		featureFileCount = featureFiles.size();
		if(featureFileCount == 0)
			cleanup();
		else {
			for(File ff : featureFiles) {
				
				DataFile dataFile = getDataFileForFeatureFile(ff);
				MsFeatureBundleExtractionTask task = 
						new MsFeatureBundleExtractionTask(dataFile, ff);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
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
		org.jdom2.Document doc = sax.build(projectXmlFile);
		org.jdom2.Element projectElement = doc.getRootElement();
		
		String id = projectElement.getAttributeValue(StoredProjectFields.Id.name()); 
		String name = projectElement.getAttributeValue(StoredProjectFields.Name.name()); 
		String description = projectElement.getAttributeValue(StoredProjectFields.Description.name()); 
		Date dateCreated = ProjectUtils.dateTimeFormat.parse(
				projectElement.getAttributeValue(StoredProjectFields.DateCreated.name())); 
		Date lastModified = ProjectUtils.dateTimeFormat.parse(
				projectElement.getAttributeValue(StoredProjectFields.DateModified.name())); 		
		project = new RawDataAnalysisProject(
				id, 
				name, 
				description, 
				projectFile, 
				projectFile.getParentFile(),
				dateCreated, 
				lastModified);

//		Element projectElement = (Element) projectDocument.getElementsByTagName(
//				StoredProjectFields.IDTrackerRawDataProject.name()).item(0);
		
		String compoundIdList = 
				projectElement.getChild(StoredProjectFields.UniqueCIDList.name()).getText();
		uniqueCompoundIds.addAll(getIdList(compoundIdList));
		
		String msmsLibIdIdList = 
				projectElement.getChild(StoredProjectFields.UniqueMSMSLibIdList.name()).getText();
		uniqueMSMSLibraryIds.addAll(getIdList(msmsLibIdIdList));

		String msRtLibIdIdList = 
				projectElement.getChild(StoredProjectFields.UniqueMSRTLibIdList.name()).getText();
		uniqueMSRTLibraryIds.addAll(getIdList(msRtLibIdIdList));

		String sampleIdIdList = 
				projectElement.getChild(StoredProjectFields.UniqueSampleIdList.name()).getText();
		uniqueSampleIds.addAll(getIdList(sampleIdIdList));
				
		try {
			populateDatabaseCashData();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);			
		}		
		List<org.jdom2.Element> msmsFileList = 
				projectElement.getChild(StoredProjectFields.MsTwoFiles.name()).
				getChildren(StoredDataFileFields.DataFile.name());
		for (org.jdom2.Element msmsFileElement : msmsFileList) {
			DataFile msmsFile = new DataFile(msmsFileElement);
			project.addMSMSDataFile(msmsFile);
		}
		//	MS1 files
		List<org.jdom2.Element> msOneFileList = 
				projectElement.getChild(StoredProjectFields.MsOneFiles.name()).
				getChildren(StoredDataFileFields.DataFile.name());
		for (org.jdom2.Element msOneFileElement : msOneFileList) {
			DataFile msOneFile = new DataFile(msOneFileElement);
			project.addMSMSDataFile(msOneFile);
		}
	}
	
	private void parseProjectFileOld(File projectXmlFile) throws Exception {
		
		taskDescription = "Parsing project file ...";
		total = 100;
		processed = 10;
		
		uniqueCompoundIds = new TreeSet<String>();
		uniqueMSMSLibraryIds = new TreeSet<String>();
		uniqueMSRTLibraryIds = new TreeSet<String>();
		uniqueSampleIds = new TreeSet<String>();
		
		Document projectDocument = null;
		try {
			projectDocument = XmlUtils.readXmlFile(projectXmlFile);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		Element projectElement = (Element) projectDocument.getElementsByTagName(
				StoredProjectFields.IDTrackerRawDataProject.name()).item(0);
		
		String id = projectElement.getAttribute(StoredProjectFields.Id.name()); 
		String name = projectElement.getAttribute(StoredProjectFields.Name.name()); 
		String description = projectElement.getAttribute(StoredProjectFields.Description.name()); 
		Date dateCreated = ProjectUtils.dateTimeFormat.parse(
				projectElement.getAttribute(StoredProjectFields.DateCreated.name())); 
		Date lastModified = ProjectUtils.dateTimeFormat.parse(
				projectElement.getAttribute(StoredProjectFields.DateModified.name())); 		
		project = new RawDataAnalysisProject(
				id, 
				name, 
				description, 
				projectFile, 
				projectFile.getParentFile(),
				dateCreated, 
				lastModified);
		
		Element compoundIdElement = (Element) projectDocument.getElementsByTagName(
				StoredProjectFields.UniqueCIDList.name()).item(0);
		uniqueCompoundIds.addAll(getIdList(compoundIdElement));
		
		Element msmsLibIdElement = (Element) projectDocument.getElementsByTagName(
				StoredProjectFields.UniqueMSMSLibIdList.name()).item(0);
		uniqueMSMSLibraryIds.addAll(getIdList(msmsLibIdElement));
		
		Element msRtLibIdElement = (Element) projectDocument.getElementsByTagName(
				StoredProjectFields.UniqueMSRTLibIdList.name()).item(0);
		uniqueMSRTLibraryIds.addAll(getIdList(msRtLibIdElement));
		
		Element sampleIdElement = (Element) projectDocument.getElementsByTagName(
				StoredProjectFields.UniqueSampleIdList.name()).item(0);
		uniqueSampleIds.addAll(getIdList(sampleIdElement));			
		try {
			populateDatabaseCashData();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);			
		}
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		//	MSMS files
		XPathExpression expr = xpath.compile("//" + 
				StoredProjectFields.IDTrackerRawDataProject.name() + 
				"/" + StoredProjectFields.MsTwoFiles.name() + "/" + 
				StoredDataFileFields.DataFile.name());
		NodeList msmsDataFileNodes = 
				(NodeList) expr.evaluate(projectDocument, XPathConstants.NODESET);	
		for (int i = 0; i < msmsDataFileNodes.getLength(); i++) {
			Element msmsFileElement = (Element) msmsDataFileNodes.item(i);
//			DataFile msmsFile = new DataFile(msmsFileElement);
//			project.addMSMSDataFile(msmsFile);
		}
		//	MS1 files
		expr = xpath.compile("//" + 
				StoredProjectFields.IDTrackerRawDataProject.name() + 
				"/" + StoredProjectFields.MsOneFiles.name() + "/" + 
				StoredDataFileFields.DataFile.name());
		NodeList msOneDataFileNodes = 
				(NodeList) expr.evaluate(projectDocument, XPathConstants.NODESET);	
		for (int i = 0; i < msOneDataFileNodes.getLength(); i++) {
			Element msOneFileElement = (Element) msOneDataFileNodes.item(i);
//			DataFile msOneFile = new DataFile(msOneFileElement);
//			project.addMSOneDataFile(msOneFile);
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
		// TODO Auto-generated method stub
		Collection<IDTExperimentalSample>samples = 
				IDTUtils.getExperimentalSamples(uniqueSampleIds, conn);
	}
	
	private Collection<String>getIdList(Element idListElement){
		
		if(idListElement.getChildNodes().getLength() == 0)
			return Arrays.asList(new String[0]);
		else {
			String listString = idListElement.getChildNodes().item(0).getTextContent();
			return Arrays.asList(listString.split(","));
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
		return new OpenStoredRawDataAnalysisProjectTask(projectFile);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(MsFeatureBundleExtractionTask.class)) {	
				processedFiles++;
				
				MsFeatureBundleExtractionTask task = (MsFeatureBundleExtractionTask)e.getSource();
				project.addMsFeaturesForDataFile(task.getDataFile(), task.getFeatures());
				if(processedFiles == featureFileCount) {
					cleanup();
				}				
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
	
	public RawDataAnalysisProject getProject() {
		return project;
	}
}
