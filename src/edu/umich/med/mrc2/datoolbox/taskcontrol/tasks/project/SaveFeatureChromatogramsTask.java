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
import java.nio.file.Paths;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class SaveFeatureChromatogramsTask extends AbstractTask {
	
	private RawDataAnalysisExperiment experimentToSave;

	public SaveFeatureChromatogramsTask(RawDataAnalysisExperiment experiment) {
		super();
		this.experimentToSave = experiment;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			createChoromatogramsXml();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		
	}

	private void createChoromatogramsXml() {

		taskDescription = "Creating chromatogram XML file";
		total = experimentToSave.getChromatogramMap().size();
		processed = 0;
		
        Document document = new Document();
        Element chromatogramListRoot = 
        		new Element(ExperimentFields.FeatureChromatogramList.name());
		chromatogramListRoot.setAttribute("version", "1.0.0.0");

        for(Entry<String, MsFeatureChromatogramBundle> ce : experimentToSave.getChromatogramMap().entrySet()) {   	
        	chromatogramListRoot.addContent(ce.getValue().getXmlElement(ce.getKey()));
        	processed++;
        }
        document.addContent(chromatogramListRoot);
        
		//	Save XML document
        taskDescription = "Saving chromatogram XML file";
		total = 100;
		processed = 30;
		File xmlFile = Paths.get(
				experimentToSave.getUncompressedExperimentFilesDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.FEATURE_CHROMATOGRAMS_FILE_NAME).toFile();
        try {
            FileWriter writer = new FileWriter(xmlFile, false);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            outputter.output(document, writer);
            processed = 100;
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(TaskStatus.ERROR);
        }
        setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new SaveFeatureChromatogramsTask(experimentToSave);
	}
}
