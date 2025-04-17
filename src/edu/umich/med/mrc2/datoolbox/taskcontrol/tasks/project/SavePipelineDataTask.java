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
import java.nio.file.Paths;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileExtensions;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;
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
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			saveDataMatrixesForPipeline();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void saveFeaturesForPipeline() {
		
		File featureXmlFile = 
				Paths.get(project.getDataDirectory().getAbsolutePath(),
				DataPrefix.MS_FEATURE.getName() + pipeline.getSaveSafeName() 
				+ "." + DataFileExtensions.FEATURE_LIST_EXTENSION.getExtension()).toFile();
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
		ExperimentUtils.createDataDirectoryForProjectIfNotExists(project);
		XmlUtils.writeCompactXMLtoFile(msFeatureDocument, featureXmlFile);		
	}
	
	private void saveDataMatrixesForPipeline() {
		
		total = 100;
		processed = 10;
		if (project.getDataMatrixForDataPipeline(pipeline) != null) {

			taskDescription = "Saving data matrix for  " 
					+ project.getName() + "(" + pipeline.getName() + ")";
							
			ExperimentUtils.saveDataMatrixForPipeline(project, pipeline);
			processed++;
		}
		taskDescription = "Saving feature matrix for  " 
				+ project.getName() + "(" + pipeline.getName() + ")";
		processed = 30;
		Matrix msFeatureMatrix = project.getFeatureMatrixForDataPipeline(pipeline);
			
		//	If matrix is in memory 
		if(msFeatureMatrix != null) {
			ExperimentUtils.saveFeatureMatrixToFile(
					msFeatureMatrix,
					project, 
					pipeline,
					false);
			ExperimentUtils.deleteTemporaryFeatureMatrixFile(project,pipeline);
		}
		else {
			//	If temporary matrix exists swap the original for it
			ExperimentUtils.saveTemporaryFeatureMatrixFileAsPrimary(project,pipeline);			
		}
		project.setFeatureMatrixForDataPipeline(pipeline, null);	//	TODO is that necessary?
		cleanupForNewProjectStructure();		
		processed = 100;		
	}
	
	//	Tmp fix to convert old project format to new one) 
	private void cleanupForNewProjectStructure() {
		
		ExperimentUtils.moveDataMatrixFileToNewDefaultLocation(project,pipeline);
		ExperimentUtils.moveFeatureMatrixFileToNewDefaultLocation(project,pipeline);
	}

	@Override
	public Task cloneTask() {
		return new SavePipelineDataTask(project, pipeline);
	}
}
