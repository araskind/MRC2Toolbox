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
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.StoredDataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.StoredProjectFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class SaveStoredRawDataAnalysisProjectTask extends AbstractTask {

	private RawDataAnalysisProject projectToSave;
	private File xmlFile;
	private Document projectDocument;
	
	public SaveStoredRawDataAnalysisProjectTask(RawDataAnalysisProject rawDataAnalyzerProject) {

		this.projectToSave = rawDataAnalyzerProject;
		total = 100;
		processed = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		projectToSave.setLastModified(new Date());
		try {
			createProjectXml();
		} catch (Exception ex) {

			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			compressProjectFile();
		} catch (Exception ex) {

			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
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
}
