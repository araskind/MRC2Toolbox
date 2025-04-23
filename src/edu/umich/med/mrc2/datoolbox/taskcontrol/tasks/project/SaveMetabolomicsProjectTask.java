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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.project.store.IDTrackerProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class SaveMetabolomicsProjectTask extends AbstractTask implements TaskListener {

	private DataAnalysisProject projectToSave;
	private Document projectXmlDocument;
	private int numberOfSavedDataPipelines;
	private Set<String>uniqueCompoundIds;
	private Set<String>uniqueMSRTLibraryIds;
		
	public SaveMetabolomicsProjectTask(DataAnalysisProject projectToSave) {
		super();
		this.projectToSave = projectToSave;
		numberOfSavedDataPipelines = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		extractDatabaseReferences();
		try {
			createNewProjectDocument();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(!projectToSave.getDataPipelines().isEmpty())
			saveDataForPipelines();
	}
	
	private void extractDatabaseReferences() {
		taskDescription = "Collecting common database references ...";
		processed = 10;	
		List<MsFeatureIdentity> idList = new ArrayList<MsFeatureIdentity>();
		for(DataPipeline dp : projectToSave.getDataPipelines()) {
			
			Set<MsFeature> dpFeatures = projectToSave.getMsFeaturesForDataPipeline(dp);
			if(dpFeatures != null && ! dpFeatures.isEmpty()) {
				
				List<MsFeatureIdentity> dpIdList = dpFeatures.stream().
					filter(p -> Objects.nonNull(p.getPrimaryIdentity())).
					flatMap(p -> p.getIdentifications().stream()).
					collect(Collectors.toList());
				if(!dpIdList.isEmpty())
					idList.addAll(dpIdList);
			}			
		}			
		uniqueCompoundIds = idList.stream().
				filter(i -> Objects.nonNull(i.getCompoundIdentity())).
				filter(i -> Objects.nonNull(i.getCompoundIdentity().getPrimaryDatabaseId())).
				map(i -> i.getCompoundIdentity().getPrimaryDatabaseId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSRTLibraryIds = idList.stream().
				filter(i -> Objects.nonNull(i.getMsRtLibraryMatch())).
				map(i -> i.getMsRtLibraryMatch().getLibraryTargetId()).
				collect(Collectors.toCollection(TreeSet::new));
	}

	private void createNewProjectDocument() {

		taskDescription = "Creating project XML file ... ";
		total = 100;
		processed = 20;
		projectXmlDocument = new Document();
		Element projectRoot = projectToSave.getXmlElement();
		if(projectToSave.getDataPipelines().isEmpty()) {
			
			projectXmlDocument.setRootElement(projectRoot);
			File projectFile = FIOUtils.changeExtension(projectToSave.getExperimentFile(), 
					ProjectType.DATA_ANALYSIS_NEW_FORMAT.getExtension());
			
			XmlUtils.writeCompactXMLtoFile(
					projectXmlDocument, projectFile);
			projectToSave.setProjectFile(projectFile);
			setStatus(TaskStatus.FINISHED);
			return;
		}
		projectRoot.addContent(addAcquisitionMethodDataFileMap());
		projectRoot.addContent(addOrderedFileNameMap());
		projectRoot.addContent(addOrderedMSFeatureIdMap());
		projectRoot.addContent(addWorklistMap());
		projectRoot.addContent(addCustomFeatureSets());
		
		DataPipeline activePipeline = projectToSave.getActiveDataPipeline();
		if(activePipeline == null && !projectToSave.getDataPipelines().isEmpty())
			activePipeline = projectToSave.getDataPipelines().iterator().next();
		
		if(activePipeline != null)
			projectRoot.setAttribute(MetabolomicsProjectFields.ActiveDataPipeline.name(), 
					activePipeline.getSaveSafeName());
		
		projectRoot.addContent(       		
        		new Element(IDTrackerProjectFields.UniqueCIDList.name()).
        		setText(StringUtils.join(uniqueCompoundIds, ",")));
		projectRoot.addContent(       		
        		new Element(IDTrackerProjectFields.UniqueMSRTLibIdList.name()).
        		setText(StringUtils.join(uniqueMSRTLibraryIds, ",")));
		
		projectXmlDocument.setRootElement(projectRoot);
		File projectFile = FIOUtils.changeExtension(projectToSave.getExperimentFile(), 
				ProjectType.DATA_ANALYSIS_NEW_FORMAT.getExtension());
		XmlUtils.writeCompactXMLtoFile(
				projectXmlDocument, projectFile);
		projectToSave.setProjectFile(projectFile);
	}
	
	private Element addCustomFeatureSets(){
		
		Element msFeatureSetMapElement = 
				new Element(MetabolomicsProjectFields.MSFeatureSetMap.name());
		
		for(DataPipeline dp : projectToSave.getDataPipelines()) {
			
			Element msFeatureSetListElement = 
					new Element(MetabolomicsProjectFields.MSFeatureSetList.name());
			msFeatureSetListElement.setAttribute(
					MetabolomicsProjectFields.DataPipelineId.name(), dp.getSaveSafeName());
			Set<MsFeatureSet> customSets = 
					projectToSave.getCustomSetsForDataPipeline(dp);
			if(!customSets.isEmpty()) {
				
				for(MsFeatureSet cs : customSets)
					msFeatureSetListElement.addContent(cs.getXmlElement());				
			}			
			msFeatureSetMapElement.addContent(msFeatureSetListElement);
		}		
		return msFeatureSetMapElement;
	}
	
	private Element addAcquisitionMethodDataFileMap() {
		
    	Element methodDataFileMapElement = 
    			new Element(MetabolomicsProjectFields.MethodDataFileMap.name());
    	
    	Set<DataAcquisitionMethod>acquisitionMethods = 
    			projectToSave.getDataPipelines().stream().
    			map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
    	
    	for(DataAcquisitionMethod method : acquisitionMethods) {
    		
        	Element methodDataFileMapItemElement = 
        			new Element(MetabolomicsProjectFields.MethodDataFileMapItem.name());
        	methodDataFileMapItemElement.setAttribute(
        			MetabolomicsProjectFields.DataAcquisitionMethodId.name(), method.getId());
        	
        	for(DataFile df : projectToSave.getDataFilesForAcquisitionMethod(method))     		
        		methodDataFileMapItemElement.addContent(df.getXmlElement());
        	        	
    		methodDataFileMapElement.addContent(methodDataFileMapItemElement);
    	}
    	return methodDataFileMapElement;
	}
	
	private Element addOrderedFileNameMap() {
		
		taskDescription = "Adding filenae map ... ";
		processed = 30;
		
    	Element fileMapElement = 
    			new Element(MetabolomicsProjectFields.FileIdMap.name());
    	
    	for(DataPipeline dp : projectToSave.getDataPipelines()) {
    		
        	Element orderedFileListElement = 
        			new Element(MetabolomicsProjectFields.FileIdList.name());
        	orderedFileListElement.setAttribute(
        			MetabolomicsProjectFields.DataPipelineId.name(), dp.getSaveSafeName());
        	
			//	Ordered file list from data matrix metadata
			Matrix mdFile = projectToSave.getMetaDataMatrixForDataPipeline(dp, 1);
			if(mdFile != null) {
				
				Object[][]fileMetaData =  mdFile.transpose(Ret.NEW).toObjectArray();
				List<Object>dfList = Arrays.asList(fileMetaData[0]);
				List<String> fileNameList = dfList.stream().
						filter(DataFile.class::isInstance).
						map(DataFile.class::cast).map(f -> f.getName()).
						collect(Collectors.toList());
				orderedFileListElement.setText(StringUtils.join(fileNameList, ","));
				
			} 
			fileMapElement.addContent(orderedFileListElement);
    	}
    	return fileMapElement;
	}
	
	private Element addOrderedMSFeatureIdMap() {
		
		taskDescription = "Adding feature ID map ... ";
		processed = 40;
		
    	Element msFeatureMapElement = 
    			new Element(MetabolomicsProjectFields.MSFeatureIdMap.name());
    	
    	for(DataPipeline dp : projectToSave.getDataPipelines()) {
    		
        	Element orderedFeatureListElement = 
        			new Element(MetabolomicsProjectFields.MSFeatureIdList.name());
        	orderedFeatureListElement.setAttribute(
        			MetabolomicsProjectFields.DataPipelineId.name(), dp.getSaveSafeName());
        	
			//	Ordered file list from data matrix metadata
			Matrix mdFeature = projectToSave.getMetaDataMatrixForDataPipeline(dp, 0);
			if(mdFeature != null) {
				
				Object[][]featureMetaData =  mdFeature.toObjectArray();
				List<Object>dfList = Arrays.asList(featureMetaData[0]);
				List<String> featureIdList = dfList.stream().
						filter(MsFeature.class::isInstance).
						map(MsFeature.class::cast).map(f -> f.getId()).
						collect(Collectors.toList());
				orderedFeatureListElement.setText(StringUtils.join(featureIdList, ","));
				
			} 
			msFeatureMapElement.addContent(orderedFeatureListElement);
    	}
    	return msFeatureMapElement;
	}
	
	private Element addWorklistMap() {
		
		taskDescription = "Adding worklist map ... ";
		processed = 40;
		
    	Element worklistMapElement = 
    			new Element(MetabolomicsProjectFields.WorklistMap.name());
    	
    	Set<DataAcquisitionMethod>acquisitionMethods = 
    			projectToSave.getDataPipelines().stream().
    			map(p -> p.getAcquisitionMethod()).collect(Collectors.toSet());
    	
    	for(DataAcquisitionMethod method : acquisitionMethods) {
    		
    		Worklist wkl = projectToSave.getWorklistForDataAcquisitionMethod(method);
    		if(wkl != null) {
    			
    			Element worklistElement = wkl.getXmlElement();
    			worklistElement.setAttribute(
    					MetabolomicsProjectFields.DataAcquisitionMethodId.name(), 
    					method.getId());
    			worklistMapElement.addContent(worklistElement);
    		}
    	}
     	return worklistMapElement;   	
	}

	private void saveDataForPipelines() {
		
		if(projectToSave.getDataPipelines().isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		//	TODO TMP fix for old projects
		ProjectUtils.moveCEFLibraryFilesToNewDefaultLocation(projectToSave);
		
		for(DataPipeline dp : projectToSave.getDataPipelines()) {
			
			SavePipelineDataTask task = new SavePipelineDataTask(projectToSave, dp);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(SavePipelineDataTask.class)) {
				
				MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(e.getSource());
				numberOfSavedDataPipelines++;
				if(numberOfSavedDataPipelines == projectToSave.getDataPipelines().size())
					setStatus(TaskStatus.FINISHED);				
			}				
		}
	}

	@Override
	public Task cloneTask() {
		return new SaveMetabolomicsProjectTask(projectToSave);
	}

	public DataAnalysisProject getProject() {
		return projectToSave;
	}
}
