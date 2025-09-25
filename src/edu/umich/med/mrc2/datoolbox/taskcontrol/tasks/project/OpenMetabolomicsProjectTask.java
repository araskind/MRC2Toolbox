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
import java.nio.file.Path;
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
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
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
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class OpenMetabolomicsProjectTask extends OpenStandaloneProjectAbstractTask implements TaskListener {
	
	private File projectFile;
	private DataAnalysisProject project;
	private Map<DataPipeline,String[]>orderedDataFileNamesMap;
	private Map<DataPipeline,String[]>orderedMSFeatureIdMap;
	private int pipelineCount;
	private int loadedPipelineCount;
	private Element projectXMLElement;
	
	public OpenMetabolomicsProjectTask(File projectFile) {
		super();
		this.projectFile = projectFile;		
	}

	@Override
	public void run() {

		projectXMLElement = null;
		setStatus(TaskStatus.PROCESSING);
		try {
			projectXMLElement = parseProjectFile();
		} catch (Exception ex) {
			reportErrorAndExit(ex);
			return;
		}
		if(projectXMLElement == null) {
			errorMessage = "Failed to parse project file";
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

	private Element parseProjectFile() {
		
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
			return null;						
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
			return null;
		}
		
		project = new DataAnalysisProject(projectElement, projectFile);
		project.setProjectType(ProjectType.DATA_ANALYSIS_NEW_FORMAT);		
		if(project.getDataPipelines().isEmpty()) {
			parseExperimentDesign(projectElement);
			return projectElement;
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
			return null;		
		}
		return projectElement;
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
		
		orderedDataFileNamesMap = new TreeMap<>();
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
		
		orderedMSFeatureIdMap = new  TreeMap<>();
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
		if(loadedPipelineCount == pipelineCount) {
			
			loadDataIntegrationResults();
			setStatus(TaskStatus.FINISHED);	
		}
	}

	private void loadDataIntegrationResults() {
		
		taskDescription = "Parsing data integration results ...";
		total = 100;
		processed = 50;

		Element dataIntegrationSetsListElement = 
				projectXMLElement.getChild(
						MetabolomicsProjectFields.DataIntegrationSetsList.name());
		if(dataIntegrationSetsListElement == null) {
			
			//	Debug only fix
			dataIntegrationSetsListElement = 
					projectXMLElement.getChild("DataPipelineAlignmentResultSet");
			
			if(dataIntegrationSetsListElement == null)
				return;
		}		
		parseDataIntegrationSetElements(dataIntegrationSetsListElement);
		parseDataPipelineAlignmentElements(dataIntegrationSetsListElement);
		attachMergedDataToIntegratedSets();
		processed = 70;
	}
	
	private void parseDataIntegrationSetElements(Element dataIntegrationSetsListElement) {
		
		List<Element>dpaElementList = 
				dataIntegrationSetsListElement.getChildren(ObjectNames.MsFeatureClusterSet.name());
		if(dpaElementList.isEmpty())
			return;
			
		for(Element dpaElement : dpaElementList) {
			
			MsFeatureClusterSet dpaRes = null;
			try {
				dpaRes = new MsFeatureClusterSet(dpaElement,project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dpaRes != null)
				project.addDataIntegrationSet(dpaRes);			
		}		
	}
		
	private void parseDataPipelineAlignmentElements(Element dataIntegrationSetsListElement) {
		
		List<Element>dpaElementList = 
				dataIntegrationSetsListElement.getChildren(ObjectNames.DataPipelineAlignmentResults.name());
		if(dpaElementList.isEmpty())
			return;
			
		for(Element dpaElement : dpaElementList) {
			
			DataPipelineAlignmentResults dpaRes = null;
			try {
				dpaRes = new DataPipelineAlignmentResults(dpaElement,project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dpaRes != null)
				project.addDataPipelineAlignmentResult(dpaRes);
		}		
	}
	
	private void attachMergedDataToIntegratedSets() {
		
		for(MsFeatureClusterSet dis : project.getDataIntegrationSets()) {
			
			Path mergedFeaturesFilePath = 
					ProjectUtils.getMergedFeaturesFilePath(project, dis.getId());
			Path mergedDataMatrixFilePath = 
					ProjectUtils.getMergedDataMatrixFilePath(project, dis.getId());
			if(!mergedFeaturesFilePath.toFile().exists() || !mergedDataMatrixFilePath.toFile().exists())
				continue;
			
			SAXBuilder sax = new SAXBuilder();
			Document doc = null;
			CompoundLibrary mergedFeatureLibrary = null;
			List<String>orderedFileNames = new ArrayList<>();
			List<String>orderedFeatureIds = new ArrayList<>();
			
			try {			
				doc = sax.build(mergedFeaturesFilePath.toFile());
			} catch (Exception e) {
				reportErrorAndExit(e);
			}
			if(doc != null) {
				
				Element rootNode = doc.getRootElement();
				
				Element compoundLibraryElement = 
						rootNode.getChild(ObjectNames.CompoundLibrary.name());
				try {
					mergedFeatureLibrary = new CompoundLibrary(compoundLibraryElement);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Element orderedFileListElement =  
						rootNode.getChild(MetabolomicsProjectFields.FileIdList.name());
				if(orderedFileListElement != null)
					orderedFileNames = ProjectUtils.getIdList(orderedFileListElement.getText());
								
				Element orderedFeatureListElement =  
						rootNode.getChild(MetabolomicsProjectFields.MSFeatureIdList.name());
				if(orderedFeatureListElement != null)
					orderedFeatureIds = ProjectUtils.getIdList(orderedFeatureListElement.getText());
			}
			Matrix mergedDataMatrix = null;
			try {
				mergedDataMatrix = Matrix.Factory.load(mergedDataMatrixFilePath.toFile());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return;
			}
			if(mergedDataMatrix != null && mergedFeatureLibrary != null
					&& !orderedFileNames.isEmpty() && !orderedFeatureIds.isEmpty()) {
				
				Matrix featureMetaDataMatrix = 
						ProjectUtils.createLibraryFeatureMetaDataMatrix(orderedFeatureIds,
								mergedFeatureLibrary.getFeatures());
				Matrix dataFileMetaDataMatrix = 
						ProjectUtils.createDataFileMetaDataMatrix(orderedFileNames,
						project.getAllDataFiles());
						
				mergedDataMatrix.setMetaDataDimensionMatrix(0, featureMetaDataMatrix);
				mergedDataMatrix.setMetaDataDimensionMatrix(1, dataFileMetaDataMatrix);
				dis.setMergedDataMatrix(mergedDataMatrix);
			}
		}		
	}

	@Override
	public Task cloneTask() {
		return new OpenMetabolomicsProjectTask(projectFile);
	}

	public DataAnalysisProject getProject() {
		return project;
	}
}
