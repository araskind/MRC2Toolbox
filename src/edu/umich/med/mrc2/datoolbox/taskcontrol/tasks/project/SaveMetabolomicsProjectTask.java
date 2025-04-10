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
import java.io.FileWriter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class SaveMetabolomicsProjectTask extends AbstractTask implements TaskListener {

	private DataAnalysisProject projectToSave;
	private Document projectXmlDocument;
		
	public SaveMetabolomicsProjectTask(DataAnalysisProject projectToSave) {
		super();
		this.projectToSave = projectToSave;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			createNewProjectDocument();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		saveProjectToFile();
		setStatus(TaskStatus.FINISHED);
	}

	private void createNewProjectDocument() {

		taskDescription = "Creating project XML file ... ";
		total = 100;
		processed = 20;
		projectXmlDocument = new Document();
		Element experimentRoot = projectToSave.getXmlElement();

		projectXmlDocument.setRootElement(experimentRoot);
	}

	private void saveProjectToFile() {

		File xmlFile = FIOUtils.changeExtension(
				projectToSave.getExperimentFile(), "xml");

		try (FileWriter writer = new FileWriter(xmlFile, false)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getCompactFormat());
			outputter.output(projectXmlDocument, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {
		return new SaveMetabolomicsProjectTask(projectToSave);
	}
}
