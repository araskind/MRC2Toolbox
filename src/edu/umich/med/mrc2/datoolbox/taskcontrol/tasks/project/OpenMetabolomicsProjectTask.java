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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class OpenMetabolomicsProjectTask extends AbstractTask implements TaskListener {
	
	private File projectFile;
	private DataAnalysisProject project;
	
	public OpenMetabolomicsProjectTask(File projectFile) {
		super();
		this.projectFile = projectFile;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			parseProjectFile();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void parseProjectFile() {
		
		taskDescription = "Parsing project file ...";
		total = 100;
		processed = 10;
		
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(projectFile);
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
		Element projectElement = null;
		try {
			projectElement = doc.getRootElement();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(projectElement == null) {
			setStatus(TaskStatus.ERROR);
			return;
		}
		project = new DataAnalysisProject(projectElement);
		project.setProjectFile(projectFile);
		parseAcquisitionMethodDataFileMap(projectElement);
		parseExperimentDesign(projectElement);
	}
	
	private void parseAcquisitionMethodDataFileMap(Element projectElement) {
		
    	Set<DataAcquisitionMethod>acquisitionMethods = 
    			project.getDataPipelines().stream().
    			map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
    	
    	List<Element> methodDataFileMapElementList = 
    			projectElement.getChild(MetabolomicsProjectFields.MethodDataFileMap.name()).
    			getChildren(MetabolomicsProjectFields.MethodDataFileMapItem.name());
    	for(Element methodDataFileMapElement : methodDataFileMapElementList) {
    		
    		String methodId = methodDataFileMapElement.getAttributeValue(
    				MetabolomicsProjectFields.DataAcquisitionMethodId.name());
    		DataAcquisitionMethod method = acquisitionMethods.stream().
    				filter(m -> m.getId().equals(methodId)).
    				findFirst().orElse(null);
    		if(method == null) {
    			errorMessage = "Data Acquisition Method undefined for file list";
				setStatus(TaskStatus.ERROR);
				return;
    		}
    		List<Element> dataFileElementList = 
    				methodDataFileMapElement.getChildren(ObjectNames.DataFile.name());
    		List<DataFile>dataFileList = new ArrayList<DataFile>();
    		for(Element dfElement : dataFileElementList)
    			dataFileList.add(new DataFile(dfElement));
    		   		
    		project.addDataFilesForAcquisitionMethod(method, dataFileList);
    	}
	}
	
	private void parseExperimentDesign(Element projectElement) {
		
		Element designElement = projectElement.getChild(
				ObjectNames.ExperimentDesign.name());
		
		ExperimentDesign experimentDesign = null;
		if(designElement != null)
			experimentDesign  = new ExperimentDesign(designElement, project);
		
		if(experimentDesign != null)
			project.setExperimentDesign(experimentDesign);
		else
			project.setExperimentDesign(new ExperimentDesign());
		
		Element limsExperimentElement = projectElement.getChild(
				ObjectNames.limsExperiment.name());
		
		LIMSExperiment limsExperiment = null;
		if(limsExperimentElement != null)
			limsExperiment = new LIMSExperiment(limsExperimentElement, project);
	}
	
	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {
		return new OpenMetabolomicsProjectTask(projectFile);
	}
}
