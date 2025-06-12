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

package edu.umich.med.mrc2.datoolbox.gui.binner.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileExtensions;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class SilhouetteTest {
	
	private File projectFile;
	private String methodPrefix;
	private DataAnalysisProject project;
	private Map<DataPipeline,String[]>orderedDataFileNamesMap;
	private Map<DataPipeline,String[]>orderedMSFeatureIdMap;
	private String[]orderedDataFileNames;
	private String[]orderedMSFeatureIds;

	public SilhouetteTest(File projectFile, String methodPrefix) {
		super();
		this.projectFile = projectFile;
		this.methodPrefix = methodPrefix;
	}

	public void parseProject() {
		
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
			return;						
		}
		Element projectElement = null;
		try {
			projectElement = doc.getRootElement();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(projectElement == null)
			return;
		
		project = new DataAnalysisProject(projectElement);
		project.setProjectFile(projectFile);
		project.setProjectType(ProjectType.DATA_ANALYSIS_NEW_FORMAT);
		
		if(project.getDataPipelines().isEmpty()) {
			parseExperimentDesign(projectElement);
			return;
		}
		parseAcquisitionMethodDataFileMap(projectElement);
		
		parseExperimentDesign(projectElement);
		parseOrderedFileNameMap(projectElement);
		parseOrderedMSFeatureIdMap(projectElement);
		recreateWorklists(projectElement);
				
		DataPipeline pipeline = project.getDataPipelines().stream().
			filter(p -> p.getSaveSafeName().equals(methodPrefix)).
			findFirst().orElse(null);
		
		if(pipeline == null) {
			System.err.println("No pipeline for " + methodPrefix);
			return;
		}
		readFeatureData(pipeline);
	}
	
	private void readFeatureData(DataPipeline pipeline) {
		
		File featureXmlFile = 
				Paths.get(project.getDataDirectory().getAbsolutePath(),
				DataPrefix.MS_FEATURE.getName() + pipeline.getSaveSafeName() 
				+ "." + DataFileExtensions.FEATURE_LIST_EXTENSION.getExtension()).toFile();
		
		if(!featureXmlFile.exists())
			return;
		
		Set<MsFeature>featureSet = new HashSet<MsFeature>();
		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(featureXmlFile);
			Element rootNode = doc.getRootElement();

			List<Element> featureElementList = 
					rootNode.getChildren(ObjectNames.LibraryMsFeature.name());					
			for (Element featureElement : featureElementList)
				featureSet.add(new LibraryMsFeature(featureElement));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		project.setFeaturesForDataPipeline(pipeline, featureSet);

		MsFeatureSet allFeatures = 
				new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),	
						project.getMsFeaturesForDataPipeline(pipeline));
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		project.addFeatureSetForDataPipeline(allFeatures, pipeline);
		createDataMatrix(pipeline);
	}
	
	private void createDataMatrix(DataPipeline pipeline) {
		
		orderedDataFileNames = orderedDataFileNamesMap.get(pipeline);
		orderedMSFeatureIds = orderedMSFeatureIdMap.get(pipeline);
		
		Matrix featureMetaDataMatrix = createFeatureMetaDataMatrix(pipeline);
		Matrix dataFileMetaDataMatrix = createDataFileMetaDataMatrix(pipeline);
		Matrix dataMatrix = ProjectUtils.loadDataMatrixForPipelineWitoutMetaData(
				project, pipeline);
		if(dataMatrix != null) {
			
			dataMatrix.setMetaDataDimensionMatrix(0, featureMetaDataMatrix);
			dataMatrix.setMetaDataDimensionMatrix(1, dataFileMetaDataMatrix);
			project.setDataMatrixForDataPipeline(pipeline, dataMatrix);
		}
	}
	
	private Matrix createFeatureMetaDataMatrix(DataPipeline pipeline) {
		
		MsFeature[]featureArray = new MsFeature[orderedMSFeatureIds.length];
		Set<MsFeature>featureSet = project.getMsFeaturesForDataPipeline(pipeline);
		for(int i=0; i<orderedMSFeatureIds.length; i++) {
			
			String featureId = orderedMSFeatureIds[i];
			MsFeature feature = featureSet.stream().
					filter(f -> f.getId().equals(featureId)).
					findFirst().orElse(null);
			featureArray[i] = feature;
		}
		return Matrix.Factory.linkToArray((Object[])featureArray);
	}
	
	private Matrix createDataFileMetaDataMatrix(DataPipeline pipeline) {
		
		DataFile[]dataFileArray = new DataFile[orderedDataFileNames.length];
		Set<DataFile>dataFileSet = 
				project.getDataFilesForAcquisitionMethod(pipeline.getAcquisitionMethod());
		for(int i=0; i<orderedDataFileNames.length; i++) {
			
			String fileName = orderedDataFileNames[i];
			DataFile df = dataFileSet.stream().
					filter(f -> f.getName().equals(fileName)).
					findFirst().orElse(null);
			dataFileArray[i] = df;
		}
		return Matrix.Factory.linkToArray((Object[])dataFileArray).transpose(Ret.NEW);
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
    		if(method == null)
				return;
    		
    		List<Element> dataFileElementList = 
    				methodDataFileMapElement.getChildren(ObjectNames.DataFile.name());
    		List<DataFile>dataFileList = new ArrayList<DataFile>();
    		for(Element dfElement : dataFileElementList)
    			dataFileList.add(new DataFile(dfElement));
    		   		
    		project.addDataFilesForAcquisitionMethod(method, dataFileList);
    	}
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
	
	private Matrix createTestMatrix() {
		
		 //	return Matrix.Factory.rand(new long[] {30,100});
		
		return null;
	}

}
