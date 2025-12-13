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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public abstract class FeatureSetAlteringTask extends AbstractTask {


	protected void saveFeaturesForPipeline(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline) {
		
		File featureXmlFile = 
			ProjectUtils.getFeaturesFilePath(experiment,dataPipeline).toFile();
		Document msFeatureDocument = new Document();
		Element featureListElement =  
				 new Element(CommonFields.FeatureList.name());
		
		Set<MsFeature> features = experiment.getMsFeaturesForDataPipeline(dataPipeline);
		
		taskDescription = "Writing features for " + dataPipeline.getName();
		total = features.size();
		processed = 0;
		
		for(MsFeature feature : features) {
			featureListElement.addContent(feature.getXmlElement());
			processed++;
		}
		msFeatureDocument.setRootElement(featureListElement);
		
		taskDescription = "Saving XML features file for " + dataPipeline.getName();
		total = 100;
		processed = 80;
		ProjectUtils.createDataDirectoryForProjectIfNotExists(experiment);
		XmlUtils.writeCompactXMLtoFile(msFeatureDocument, featureXmlFile);		
	}
	
	protected void cleanAndSaveDataMatrix(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline,
			List<Long> featureIndices) {
		
		taskDescription = "Removing features with no data from data matrix";
		total = 100;
		processed = 20;

		Matrix cleanDataMatrix = 
				ProjectUtils.removeFeaturesFromMatrixWithMetadata(
						experiment.getDataMatrixForDataPipeline(dataPipeline), 
						featureIndices);
		
		taskDescription = "Saving data matrix for " + dataPipeline.getName();
		total = 100;
		processed = 80;
		experiment.setDataMatrixForDataPipeline(dataPipeline, cleanDataMatrix);	
		
		taskDescription = "Saving cleaned data matrix";
		ProjectUtils.saveDataMatrixForPipeline(
				experiment, 
				dataPipeline);	
		processed = 100;
	}
	
	protected void cleanAndSaveFeatureMatrix(
			DataAnalysisProject experiment,
			DataPipeline dataPipeline,
			List<Long> featureIndices) {
		
		taskDescription = "Removing features with no data from feature matrix";
		total = 100;
		processed = 20;
		
		Matrix featureMatrix = experiment.getFeatureMatrixForDataPipeline(dataPipeline);
		if(featureMatrix == null)
			featureMatrix = ProjectUtils.readFeatureMatrix(experiment, dataPipeline, false);
		
		if(featureMatrix != null) {
			
			Matrix cleanFeatureMatrix = 
					ProjectUtils.removeFeaturesFromMatrixWithMetadata(featureMatrix, featureIndices);

			taskDescription = "Saving feature matrix for " + dataPipeline.getName();
			total = 100;
			processed = 50;
			experiment.setFeatureMatrixForDataPipeline(dataPipeline, cleanFeatureMatrix);			
			
			taskDescription = "Saving cleaned feature matrix";
			ProjectUtils.saveFeatureMatrixToFile(
					cleanFeatureMatrix,
					experiment, 
					dataPipeline,
					false);	
			processed = 100;
		}
	}
}
