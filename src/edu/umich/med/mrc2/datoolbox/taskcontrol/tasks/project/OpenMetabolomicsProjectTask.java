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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class OpenMetabolomicsProjectTask extends OpenStandaloneProjectAbstractTask implements TaskListener {
	
	private File projectFile;
	private DataAnalysisProject project;
	private Map<DataPipeline,String[]>orderedDataFileNamesMap;
	private Map<DataPipeline,String[]>orderedMSFeatureIdMap;
	private int pipelineCount;
	private int loadedPipelineCount;
	
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
		if(!project.getDataPipelines().isEmpty())
			initPipelineDataLoad();
		else
			setStatus(TaskStatus.FINISHED);
	}

	private void initPipelineDataLoad() {
		
		pipelineCount = project.getDataPipelines().size();
		if(pipelineCount == 0) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		pipelineCount = orderedDataFileNamesMap.size();
		loadedPipelineCount = 0;
		for(DataPipeline dp : project.getDataPipelines()) {
			
			if(orderedDataFileNamesMap.get(dp) != null 
					&& orderedMSFeatureIdMap.get(dp) != null) {

				LoadPipelineDataTask task = new LoadPipelineDataTask(
						project, 
						dp, 
						orderedDataFileNamesMap.get(dp),
						orderedMSFeatureIdMap.get(dp));
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
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
		project.setProjectType(ProjectType.DATA_ANALYSIS_NEW_FORMAT);
		
		if(project.getDataPipelines().isEmpty()) {
			parseExperimentDesign(projectElement);
			return;
		}
		
		//	That is necessary to correctly associate samples with data files in the design
		parseAcquisitionMethodDataFileMap(projectElement);		
		parseExperimentDesign(projectElement);
		parseOrderedFileNameMap(projectElement);
		parseOrderedMSFeatureIdMap(projectElement);
		setActivePipeline(projectElement);
		recreateWorklists(projectElement);
		parseCustomFeatureSets(projectElement);
		
		collectIdsForRetrievalFromDatabase(projectElement);				
		try {
			populateDatabaseCacheData();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;		
		}	
	}
	
	private void parseCustomFeatureSets(Element projectElement) {

    	List<Element> cutomFeatureSetElementMap = 
    			projectElement.getChild(MetabolomicsProjectFields.MSFeatureSetMap.name()).
    			getChildren(MetabolomicsProjectFields.MSFeatureSetList.name());
		
    	for(Element cutomFeatureSetListElement : cutomFeatureSetElementMap) {
    		
    		List<Element> cutomFeatureSetList = 
    				cutomFeatureSetListElement.getChildren(ObjectNames.MsFeatureSet.name());
    		if(!cutomFeatureSetList.isEmpty()) {
    			
        		String pipelineName = cutomFeatureSetListElement.getAttributeValue(
        				MetabolomicsProjectFields.DataPipelineId.name());
        		if(pipelineName != null && !pipelineName.isEmpty()) {
        			
        			DataPipeline dp = project.getDataPipelines().stream().
        				filter(p -> p.getSaveSafeName().equals(pipelineName)).
        				findFirst().orElse(null);
        			
        			for(Element cutomFeatureSetElement : cutomFeatureSetList) {
        				
        				MsFeatureSet customSet = new MsFeatureSet(cutomFeatureSetElement);
        				project.addFeatureSetForDataPipeline(customSet, dp);      				
        				if(customSet.isActive())
        					project.setActiveFeatureSetForDataPipeline(customSet, dp);
        			}
        		}
    		}
    	}
	}
	
	private void recreateWorklists(Element projectElement) {

    	Set<DataAcquisitionMethod>acquisitionMethods = 
    			project.getDataPipelines().stream().
    			map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
		List<Element>worklistMapElementList = 
				projectElement.getChild(MetabolomicsProjectFields.WorklistMap.name()).
				getChildren(ObjectNames.Worklist.name());
		for(Element worklistMapElement : worklistMapElementList) {
			
			String methodId = worklistMapElement.getAttributeValue(
					MetabolomicsProjectFields.DataAcquisitionMethodId.name());
			DataAcquisitionMethod method = acquisitionMethods.stream().
				filter(m -> m.getId().equals(methodId)).findFirst().orElse(null);
			Worklist wkl = new Worklist(worklistMapElement);
			for(WorklistItem item : wkl.getWorklistItems()) {
				
				DataFile df = project.getDataFilesForAcquisitionMethod(method).
					stream().filter(f -> f.getName().equals(item.getDataFileName())).
					findFirst().orElse(null);
				item.setDataFile(df);
			}
			project.setWorklistForAcquisitionMethod(method, wkl);
		}		
	}
		
	private void setActivePipeline(Element projectElement) {
		
		DataPipeline activePipeline = null;
		String activePipelineName = projectElement.getAttributeValue(
				MetabolomicsProjectFields.ActiveDataPipeline.name());
		
		if(activePipelineName != null && !activePipelineName.isEmpty())
			activePipeline = project.getDataPipelines().stream().
					filter(p -> p.getSaveSafeName().equals(activePipelineName)).
					findFirst().orElse(activePipeline);

		if(activePipeline == null && !project.getDataPipelines().isEmpty())
			activePipeline = project.getDataPipelines().iterator().next();
		
		if(activePipeline != null)
			project.setActiveDataPipeline(activePipeline);
	}
	
	private void parseOrderedFileNameMap(Element projectElement) {
		
		orderedDataFileNamesMap = new TreeMap<DataPipeline,String[]>();
    	List<Element> orderedFileListElementList = 
    			projectElement.getChild(MetabolomicsProjectFields.FileIdMap.name()).
    			getChildren(MetabolomicsProjectFields.FileIdList.name());
		
    	for(Element orderedFileListElement : orderedFileListElementList) {
    		
    		String pipelineName = orderedFileListElement.getAttributeValue(
    				MetabolomicsProjectFields.DataPipelineId.name());
    		if(pipelineName != null && !pipelineName.isEmpty()) {
    			
    			DataPipeline dp = project.getDataPipelines().stream().
    				filter(p -> p.getSaveSafeName().equals(pipelineName)).
    				findFirst().orElse(null);
    			
    			String[]fileNames = orderedFileListElement.getText().split(",");
    			orderedDataFileNamesMap.put(dp, fileNames);
    		}
    	}
	}
	
	private void parseOrderedMSFeatureIdMap(Element projectElement) {
		
		orderedMSFeatureIdMap = new  TreeMap<DataPipeline,String[]>();
    	List<Element> orderedMSFeatureIdElementList = 
    			projectElement.getChild(MetabolomicsProjectFields.MSFeatureIdMap.name()).
    			getChildren(MetabolomicsProjectFields.MSFeatureIdList.name());
		
    	for(Element orderedMSFeatureIdElement : orderedMSFeatureIdElementList) {
    		
    		String pipelineName = orderedMSFeatureIdElement.getAttributeValue(
    				MetabolomicsProjectFields.DataPipelineId.name());
    		if(pipelineName != null && !pipelineName.isEmpty()) {
    			
    			DataPipeline dp = project.getDataPipelines().stream().
    				filter(p -> p.getSaveSafeName().equals(pipelineName)).
    				findFirst().orElse(null);
    			
    			String[]featureIds = orderedMSFeatureIdElement.getText().split(",");
    			orderedMSFeatureIdMap.put(dp, featureIds);
    		}
    	}
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
		if(limsExperimentElement != null) {
			limsExperiment = new LIMSExperiment(limsExperimentElement, project);
			project.setLimsExperiment(limsExperiment);
		}
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(LoadPipelineDataTask.class))
				finalizeLoadPipelineDataTask((LoadPipelineDataTask)e.getSource());
		}
	}
	
	private synchronized void finalizeLoadPipelineDataTask(LoadPipelineDataTask task) {
		
		loadedPipelineCount++;
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(task);
		if(loadedPipelineCount == pipelineCount)
			setStatus(TaskStatus.FINISHED);		
	}

	@Override
	public Task cloneTask() {
		return new OpenMetabolomicsProjectTask(projectFile);
	}

	public DataAnalysisProject getProject() {
		return project;
	}
}
