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
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.project.store.StoredDataFileFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class SaveFileMsFeaturesTask extends AbstractTask {
	
	private DataFile file;
	private File xmlTmpDir;	
	private Collection<MsFeatureInfoBundle>features;
	private Document dataFileDocument;
	
	public SaveFileMsFeaturesTask(
			DataFile file, 
			File xmlTmpDir, 
			Collection<MsFeatureInfoBundle> features) {
		super();
		this.file = file;
		this.xmlTmpDir = xmlTmpDir;
		this.features = features;
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		try {
			createXmlForFeatureList();
		} catch (Exception ex) {

			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void createXmlForFeatureList() throws Exception {

		taskDescription = "Creating XML for " + file.getName();
		total = features.size();
		processed = 0;
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		dataFileDocument = dBuilder.newDocument();
		Element dataFileElement = 
				dataFileDocument.createElement(
						StoredDataFileFields.DataFile.name());
		dataFileElement.setAttribute("version", "1.0.0.0");
		dataFileElement.setAttribute(StoredDataFileFields.Name.name(), file.getName());
		dataFileDocument.appendChild(dataFileElement);
		Element featureListElement =  
				dataFileDocument.createElement(StoredDataFileFields.FeatureList.name());
		dataFileElement.appendChild(featureListElement);
		for(MsFeatureInfoBundle msf : features) {
			
			Element featureElement =  msf.getXmlElement(dataFileDocument);
			featureListElement.appendChild(featureElement);
			processed++;
		}
		//	Save XML document
		taskDescription = "Saving XML for " + file.getName();
		total = 100;
		processed = 80;
		File xmlFile = Paths.get(
				xmlTmpDir.getAbsolutePath(), 
				FilenameUtils.getBaseName(file.getName()) + ".xml").toFile();
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer transformer = transfac.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
		DOMSource source = new DOMSource(dataFileDocument);
		transformer.transform(source, result);
		result.getOutputStream().close();
	}

	@Override
	public Task cloneTask() {
		return new SaveFileMsFeaturesTask(file, xmlTmpDir, features);
	}
}
