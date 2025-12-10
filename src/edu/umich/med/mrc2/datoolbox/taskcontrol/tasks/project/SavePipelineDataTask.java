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
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
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
	
	private void saveDataMatrixesForPipeline() {
		
		total = 100;
		processed = 10;
		if (project.getDataMatrixForDataPipeline(pipeline) != null) {

			taskDescription = "Saving data matrix for  " 
					+ project.getName() + "(" + pipeline.getName() + ")";
							
			ProjectUtils.saveDataMatrixForPipeline(project, pipeline);
			processed++;
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
		processed = 100;		
	}
	
	@Override
	public Task cloneTask() {
		return new SavePipelineDataTask(project, pipeline);
	}
}
