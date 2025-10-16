/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class SaveFileMsFeaturesTask extends AbstractTask {

	private DataFile file;
	private File xmlTmpDir;	
	private Collection<MSFeatureInfoBundle>features;

	public SaveFileMsFeaturesTask(
			DataFile file, 
			File xmlTmpDir, 
			Collection<MSFeatureInfoBundle> features) {
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
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void createXmlForFeatureList() throws Exception {

		taskDescription = "Creating XML for " + file.getName();
		total = features.size();
		processed = 0;
		
        Document dataFileDocument = new Document();
        Element dataFileElement = 
        		new Element(ObjectNames.DataFile.name());
		dataFileElement.setAttribute("version", "1.0.0.0");
		dataFileElement.setAttribute(CommonFields.Name.name(), file.getName());
		Element featureListElement =  
				 new Element(DataFileFields.FeatureList.name());

		for(MSFeatureInfoBundle msf : features) {
			featureListElement.addContent(msf.getXmlElement());
			processed++;
		}
		dataFileElement.addContent(featureListElement);
		dataFileDocument.addContent(dataFileElement);
        
		//	Save XML document
		taskDescription = "Saving XML for " + file.getName();
		total = 100;
		processed = 80;
		File xmlFile = Paths.get(
				xmlTmpDir.getAbsolutePath(), 
				FilenameUtils.getBaseName(file.getName()) + ".xml").toFile();
        try {
            FileWriter writer = new FileWriter(xmlFile, false);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            outputter.output(dataFileDocument, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        processed = 100;
	}
	
	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
