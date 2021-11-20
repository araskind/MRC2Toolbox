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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.StoredDataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.StoredProjectFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.zip.ParallelZip;

public class SaveStoredRawDataAnalysisProjectTask extends AbstractTask implements TaskListener {

	private RawDataAnalysisProject projectToSave;
	private File xmlFile;
	private Document projectDocument;
	private File xmlTmpDir;
	private int fileFeatureCount;
	private int processedFiles;
	
	private Set<String>uniqueCompoundIds;
	private Set<String>uniqueMSMSLibraryIds;
	private Set<String>uniqueMSRTLibraryIds;	
	
	public SaveStoredRawDataAnalysisProjectTask(RawDataAnalysisProject rawDataAnalyzerProject) {

		this.projectToSave = rawDataAnalyzerProject;		
		total = 100;
		processed = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		projectToSave.setLastModified(new Date());
		fileFeatureCount = getFeatureFileCount();
		processedFiles = 0;
		try {
			Path tmpDir = Paths.get(projectToSave.getProjectDirectory().getAbsolutePath(), "xmlpParts");
			xmlTmpDir = tmpDir.toFile();
			xmlTmpDir.mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		extractDatabaseReferences();
		try {
			createProjectXml();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(fileFeatureCount == 0)
			compressAndCleanup();
	}
	
	private void extractDatabaseReferences() {
		taskDescription = "Collecting common database references ...";
		processed = 10;		
		List<MsFeatureIdentity> idList = projectToSave.getMsMsFeatureBundles().stream().
			filter(p -> p.getMsFeature().getPrimaryIdentity() != null).
			flatMap(p -> p.getMsFeature().getIdentifications().stream()).
			collect(Collectors.toList());
			
		uniqueCompoundIds = idList.stream().
				map(i -> i.getCompoundIdentity().getPrimaryDatabaseId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSMSLibraryIds = idList.stream().
				filter(i -> i.getReferenceMsMsLibraryMatch() != null).
				map(i -> i.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getUniqueId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSRTLibraryIds = idList.stream().
				filter(i -> i.getMsRtLibraryMatch() != null).
				map(i -> i.getMsRtLibraryMatch().getLibraryTargetId()).
				collect(Collectors.toCollection(TreeSet::new));
	}

	private void createProjectXml() throws Exception {
		taskDescription = "Creating XML file";
		processed = 10;
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		projectDocument = dBuilder.newDocument();
		Element projectRoot = 
				projectDocument.createElement(
						StoredProjectFields.IDTrackerRawDataProject.name());
		projectRoot.setAttribute("version", "1.0.0.0");
		projectRoot.setAttribute(StoredProjectFields.Id.name(), 
				projectToSave.getId());
		projectRoot.setAttribute(StoredProjectFields.Name.name(), 
				projectToSave.getDescription());
		projectRoot.setAttribute(StoredProjectFields.ProjectFile.name(), 
				projectToSave.getProjectFile().getAbsolutePath());
		projectRoot.setAttribute(StoredProjectFields.ProjectDir.name(), 
				projectToSave.getProjectDirectory().getAbsolutePath());	
		projectRoot.setAttribute(StoredProjectFields.DateCreated.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getDateCreated()));
		projectRoot.setAttribute(StoredProjectFields.DateModified.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getLastModified()));	
		projectDocument.appendChild(projectRoot);

		//	Add common ID lists
		Element uniqueCIDListElement = projectDocument.createElement(
				StoredProjectFields.UniqueCIDList.name());
		Text uniqueCompoundIdList = 
				projectDocument.createTextNode(StringUtils.join(uniqueCompoundIds, ","));
		uniqueCIDListElement.appendChild(uniqueCompoundIdList);
		projectRoot.appendChild(uniqueCIDListElement);
		
		Element uniqueMSMSLibIdListElement = projectDocument.createElement(
				StoredProjectFields.UniqueMSMSLibIdList.name());
		Text uniqueMSMSLibIdList = 
				projectDocument.createTextNode(StringUtils.join(uniqueMSMSLibraryIds, ","));
		uniqueMSMSLibIdListElement.appendChild(uniqueMSMSLibIdList);
		projectRoot.appendChild(uniqueMSMSLibIdListElement);
		
		Element uniqueMSRTLibIdListElement = projectDocument.createElement(
				StoredProjectFields.UniqueMSRTLibIdList.name());
		Text uniqueMSRTLibIdList = 
				projectDocument.createTextNode(StringUtils.join(uniqueMSRTLibraryIds, ","));
		uniqueMSRTLibIdListElement.appendChild(uniqueMSRTLibIdList);
		projectRoot.appendChild(uniqueMSRTLibIdListElement);
		
		//	MS2 file list
		Element msTwoFileListElement = projectDocument.createElement(
				StoredProjectFields.MsTwoFiles.name());
		projectRoot.appendChild(msTwoFileListElement);
		for(DataFile ms2dataFile : projectToSave.getMSMSDataFiles()) {
			
			Element dataFileElement =  ms2dataFile.getXmlElement(projectDocument);
			msTwoFileListElement.appendChild(dataFileElement);	
			Collection<MsFeatureInfoBundle>fileFeatures = 
					projectToSave.getMsFeaturesForDataFile(ms2dataFile);
			if(fileFeatures.size() > 0) {
				SaveFileMsFeaturesTask task = 
						new SaveFileMsFeaturesTask(ms2dataFile, xmlTmpDir, fileFeatures);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}			
		}		
		// MS1 file list
		Element msOneFileListElement = projectDocument.createElement(
				StoredProjectFields.MsOneFiles.name());
		projectRoot.appendChild(msOneFileListElement);
		for(DataFile msOnedataFile : projectToSave.getMSOneDataFiles()) {
			
			Element dataFileElement =  msOnedataFile.getXmlElement(projectDocument);
			msOneFileListElement.appendChild(dataFileElement);			
			Collection<MsFeatureInfoBundle>fileFeatures = 
					projectToSave.getMsFeaturesForDataFile(msOnedataFile);
			if(fileFeatures.size() > 0) {
				SaveFileMsFeaturesTask task = 
						new SaveFileMsFeaturesTask(msOnedataFile, xmlTmpDir, fileFeatures);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
		//	Save XML document
		xmlFile = Paths.get(xmlTmpDir.getAbsolutePath(), "Project.xml").toFile();
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer transformer = transfac.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
		DOMSource source = new DOMSource(projectDocument);
		transformer.transform(source, result);
		result.getOutputStream().close();
	}
	
	private int getFeatureFileCount() {
		
		long msmsCount = projectToSave.getMSMSDataFiles().stream().				
				filter(f -> !projectToSave.getMsFeaturesForDataFile(f).isEmpty()).count();
		long msOneCount = projectToSave.getMSOneDataFiles().stream().				
				filter(f -> !projectToSave.getMsFeaturesForDataFile(f).isEmpty()).count();
				
		return Long.valueOf(msmsCount + msOneCount).intValue();
	}
	
	private void createProjectXmlOld() throws Exception {
		taskDescription = "Creating XML file";
		processed = 10;
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		projectDocument = dBuilder.newDocument();
		Element projectRoot = 
				projectDocument.createElement(
						StoredProjectFields.IDTrackerRawDataProject.name());
		projectRoot.setAttribute("version", "1.0.0.0");
		projectRoot.setAttribute(StoredProjectFields.Id.name(), 
				projectToSave.getId());
		projectRoot.setAttribute(StoredProjectFields.Name.name(), 
				projectToSave.getDescription());
		projectRoot.setAttribute(StoredProjectFields.ProjectFile.name(), 
				projectToSave.getProjectFile().getAbsolutePath());
		projectRoot.setAttribute(StoredProjectFields.ProjectDir.name(), 
				projectToSave.getProjectDirectory().getAbsolutePath());	
		projectRoot.setAttribute(StoredProjectFields.DateCreated.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getDateCreated()));
		projectRoot.setAttribute(StoredProjectFields.DateModified.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getLastModified()));	
		projectDocument.appendChild(projectRoot);		
		
		//	MS2 file list
		Element msTwoFileListElement = projectDocument.createElement(
				StoredProjectFields.MsTwoFiles.name());
		projectRoot.appendChild(msTwoFileListElement);
		for(DataFile ms2dataFile : projectToSave.getMSMSDataFiles()) {
			
			Element dataFileElement =  ms2dataFile.getXmlElement(projectDocument);
			msTwoFileListElement.appendChild(dataFileElement);
			
			Collection<MsFeatureInfoBundle>fileFeatures = projectToSave.getMsFeaturesForDataFile(ms2dataFile);
			if(fileFeatures.size() > 0) {
				taskDescription = "Processing MSMS features for " + ms2dataFile.getName();
				total = fileFeatures.size();
				processed = 0;	
				Element featureListElement =  
						projectDocument.createElement(StoredDataFileFields.FeatureList.name());
				dataFileElement.appendChild(featureListElement);
				for(MsFeatureInfoBundle msf : fileFeatures) {
					
					Element featureElement =  msf.getXmlElement(projectDocument);
					featureListElement.appendChild(featureElement);
					processed++;
				}
			}			
		}		
		// MS1 file list
		Element msOneFileListElement = projectDocument.createElement(
				StoredProjectFields.MsOneFiles.name());
		projectRoot.appendChild(msOneFileListElement);
		for(DataFile msOnedataFile : projectToSave.getMSOneDataFiles()) {
			
			Element dataFileElement =  msOnedataFile.getXmlElement(projectDocument);
			msOneFileListElement.appendChild(dataFileElement);
			
			Collection<MsFeatureInfoBundle>fileFeatures = projectToSave.getMsFeaturesForDataFile(msOnedataFile);
			if(fileFeatures.size() > 0) {
				taskDescription = "Processing MSMS features for " + msOnedataFile.getName();
				total = fileFeatures.size();
				processed = 0;	
				
				Element featureListElement =  
						projectDocument.createElement(StoredDataFileFields.FeatureList.name());
				dataFileElement.appendChild(featureListElement);
				for(MsFeatureInfoBundle msf : fileFeatures) {
					
					Element featureElement =  msf.getXmlElement(projectDocument);
					featureListElement.appendChild(featureElement);
					processed++;
				}
			}
		}
		//	Save XML document
		xmlFile = Paths.get(projectToSave.getProjectDirectory().getAbsolutePath(), 
				projectToSave.getName() + ".xml").toFile();
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer transformer = transfac.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
		DOMSource source = new DOMSource(projectDocument);
		transformer.transform(source, result);
		result.getOutputStream().close();
	}

	private void compressProjectFile() throws Exception {
		
		taskDescription = "Compressing XML file";
		processed = 50;		
		if(xmlFile != null && xmlFile.exists()) {		
			
			processed = 60;
	        OutputStream archiveStream = new FileOutputStream(projectToSave.getProjectFile());
	        ZipArchiveOutputStream archive =
	        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
	        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
	        archive.setUseZip64(Zip64Mode.Always);
	        ZipArchiveEntry entry = new ZipArchiveEntry(projectToSave.getName());
	        archive.putArchiveEntry(entry);

	        BufferedInputStream input = new BufferedInputStream(new FileInputStream(xmlFile));
	        org.apache.commons.io.IOUtils.copy(input, archive);
	        input.close();
	        archive.closeArchiveEntry();
	        archive.finish();
	        archive.close();
	        archiveStream.close();	        
	        xmlFile.delete();
	        processed = 100;
		}
	}

	@Override
	public Task cloneTask() {
		return new SaveStoredRawDataAnalysisProjectTask(projectToSave);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(SaveFileMsFeaturesTask.class)) {	
				processedFiles++;
				if(processedFiles == fileFeatureCount) {
					compressAndCleanup();
				}				
			}
		}
	}

	private void compressAndCleanup() {
		
		taskDescription = "Compressing project ...";
		total = 100;
		processed = 70;	
		this.fireTaskEvent();
		ParallelZip.compressFolder(
				xmlTmpDir.getAbsolutePath(), 
				projectToSave.getProjectFile().getAbsolutePath(), 
				projectToSave.getProjectDirectory(), "xml");		
		if(xmlTmpDir != null) {
			try {
				FileUtils.deleteDirectory(xmlTmpDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
}
