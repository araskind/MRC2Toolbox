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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MetabolomicsProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class SavePipelineDataTask extends AbstractTask {

	private DataAnalysisProject project;
	private DataPipeline pipeline;
	
	
	public SavePipelineDataTask(
			DataAnalysisProject project,DataPipeline pipeline) {
		super();
		this.project = project;
		this.pipeline = pipeline;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			saveFeaturesForPipeline();
		} catch (Exception ex) {
			reportErrorAndExit(ex);
			return;
		}
		try {
			saveAveragedFeatureLibraryForPipeline();
		} catch (Exception ex) {
			reportErrorAndExit(ex);
			return;
		} 
		try {
			saveMergedFeatureLibrariesForPipeline();
		} catch (Exception ex) {
			reportErrorAndExit(ex);
			return;
		}
		try {
			saveDataMatrixesForPipeline();
		} catch (Exception ex) {
			reportErrorAndExit(ex);
			return;
		}		
		setStatus(TaskStatus.FINISHED);
	}
	
	private void saveFeaturesForPipeline() {
		
		File featureXmlFile = 
			ProjectUtils.getFeaturesFilePath(project,pipeline).toFile();
		Document msFeatureDocument = new Document();
		Element featureListElement =  
				 new Element(CommonFields.FeatureList.name());
		
		Set<MsFeature> features = project.getMsFeaturesForDataPipeline(pipeline);
		
		taskDescription = "Writing features for " + pipeline.getName();
		total = features.size();
		processed = 0;
		
		for(MsFeature feature : features) {
			featureListElement.addContent(feature.getXmlElement());
			processed++;
		}
		msFeatureDocument.setRootElement(featureListElement);
		
//		System.out.println("***********");
//		System.out.println(Integer.toString(processed) + " features converted for pipeline " + pipeline.getName());
//		System.out.println("***********");
		
		taskDescription = "Saving XML features file for " + pipeline.getName();
		total = 100;
		processed = 80;
		ProjectUtils.createDataDirectoryForProjectIfNotExists(project);
		XmlUtils.writeCompactXMLtoFile(msFeatureDocument, featureXmlFile);		
	}
	
	private void saveAveragedFeatureLibraryForPipeline() {
		
		taskDescription = "Saving averaged feature library file for " + pipeline.getName();
		total = 100;
		processed = 80;
		CompoundLibrary avgLib = 
				project.getAveragedFeatureLibraryForDataPipeline(pipeline);
		if(avgLib == null)
			return;
				
		File avgLibXmlFile = 
				ProjectUtils.getAveragedFeaturesFilePath(project,pipeline).toFile();
		Document avgFeatureLibDocument = new Document();
		Element cpdLibElement =  avgLib.getXmlElement();
		avgFeatureLibDocument.setRootElement(cpdLibElement);
		ProjectUtils.createDataDirectoryForProjectIfNotExists(project);
		XmlUtils.writeCompactXMLtoFile(avgFeatureLibDocument, avgLibXmlFile);		
	}
	
	private void saveMergedFeatureLibrariesForPipeline() {
		
		taskDescription = "Saving merged feature library file for " + pipeline.getName();
		total = 100;
		processed = 80;
		for(DataPipelineAlignmentResults dpAlignResults : project.getDataPipelineAlignmentResults()) {
			
			Matrix mergedDataMatrix = dpAlignResults.getMergedDataMatrix();
			if(mergedDataMatrix == null)
				continue;
					
			Matrix mdFeature = mergedDataMatrix.getMetaDataDimensionMatrix(0);
			Matrix mdFile = mergedDataMatrix.getMetaDataDimensionMatrix(1);
			if(mdFeature == null || mdFile == null)
				continue;
				
			Object[][]featureMetaData =  mdFeature.toObjectArray();
			List<LibraryMsFeature> mergedFeatures =  
					Arrays.asList(featureMetaData[0]).stream().
					filter(LibraryMsFeature.class::isInstance).
					map(LibraryMsFeature.class::cast).
					collect(Collectors.toList());
			if(mergedFeatures.isEmpty())
				continue;

			Object[][]fileMetaData =  mdFile.transpose(Ret.NEW).toObjectArray();
			List<String> fileNameList = 
					Arrays.asList(fileMetaData[0]).stream().
					filter(DataFile.class::isInstance).
					map(DataFile.class::cast).map(f -> f.getName()).
					collect(Collectors.toList());	
			if(fileNameList.isEmpty())
				continue;
				
			CompoundLibrary mergedLibrary = new CompoundLibrary("Merged features for " 
					+ pipeline.getName() + " (" + dpAlignResults.getName() + ")");
			mergedLibrary.setPolarity(pipeline.getAcquisitionMethod().getPolarity());
			mergedLibrary.addFeatures(mergedFeatures);
			
			File mergedLibXmlFile = 
					ProjectUtils.getMergedFeaturesFilePath(project,pipeline, dpAlignResults.getId()).toFile();
			Document mergedFeatureLibDocument = new Document();
			Element root = new Element(ObjectNames.DataPipelineAlignmentResults.name());
			mergedFeatureLibDocument.setRootElement(root);
        	Element orderedFileListElement = 
        			new Element(MetabolomicsProjectFields.FileIdList.name());
			orderedFileListElement.setText(StringUtils.join(fileNameList, ","));
			root.addContent(orderedFileListElement);
			
			List<String>featureIdList = mergedFeatures.stream().
					map(f -> f.getId()).collect(Collectors.toList());	
			Element orderedFeatureListElement = 
        			new Element(MetabolomicsProjectFields.MSFeatureIdList.name());
			orderedFeatureListElement.setText(StringUtils.join(featureIdList, ","));
			root.addContent(orderedFeatureListElement);			
			
			root.addContent(mergedLibrary.getXmlElement());			
			ProjectUtils.createDataDirectoryForProjectIfNotExists(project);
			XmlUtils.writeCompactXMLtoFile(mergedFeatureLibDocument, mergedLibXmlFile);				
		}
	}
	
	private void saveDataMatrixesForPipeline() {
		
		total = 100;
		processed = 10;
		if (project.getDataMatrixForDataPipeline(pipeline) != null) {

			taskDescription = "Saving data matrix for  " 
					+ project.getName() + "(" + pipeline.getName() + ")";
							
			ProjectUtils.saveDataMatrixForPipeline(project, pipeline);
			processed++;
		}
		//	Merged feature matrices 
		for(DataPipelineAlignmentResults dpAlignResults : project.getDataPipelineAlignmentResults()) {

			if(dpAlignResults.getMergedDataMatrix() != null) {
				
				taskDescription = "Saving merged data matrix for  " 
						+ project.getName() + "(" + pipeline.getName() + ")";
								
				ProjectUtils.saveMergedDataMatrixForPipeline(project, pipeline, dpAlignResults.getId());
				processed++;
			}
		}
		taskDescription = "Saving feature matrix for  " 
				+ project.getName() + "(" + pipeline.getName() + ")";
		processed = 30;
		Matrix msFeatureMatrix = project.getFeatureMatrixForDataPipeline(pipeline);
			
		//	If matrix is in memory 
		if(msFeatureMatrix != null) {
			ProjectUtils.saveFeatureMatrixToFile(
					msFeatureMatrix,
					project, 
					pipeline,
					false);
			ProjectUtils.deleteTemporaryFeatureMatrixFile(project,pipeline);
		}
		else {
			//	If temporary matrix exists swap the original for it
			ProjectUtils.saveTemporaryFeatureMatrixFileAsPrimary(project,pipeline);			
		}
		//	project.setFeatureMatrixForDataPipeline(pipeline, null);
		cleanupForNewProjectStructure();		
		processed = 100;		
	}
	
	//	Tmp fix to convert old project format to new one) 
	private void cleanupForNewProjectStructure() {
		
		ProjectUtils.moveDataMatrixFileToNewDefaultLocation(project,pipeline);
		ProjectUtils.moveFeatureMatrixFileToNewDefaultLocation(project,pipeline);
	}

	@Override
	public Task cloneTask() {
		return new SavePipelineDataTask(project, pipeline);
	}
}
