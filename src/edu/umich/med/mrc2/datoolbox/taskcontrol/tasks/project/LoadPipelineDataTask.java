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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileExtensions;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class LoadPipelineDataTask extends AbstractTask {
	
	private DataAnalysisProject project;
	private DataPipeline pipeline;
	private String[]orderedDataFileNames;
	private String[]orderedMSFeatureIds;

	public LoadPipelineDataTask(
			DataAnalysisProject project, 
			DataPipeline pipeline, 
			String[] orderedDataFileNames,
			String[] orderedMSFeatureIds) {
		super();
		this.project = project;
		this.pipeline = pipeline;
		this.orderedDataFileNames = orderedDataFileNames;
		this.orderedMSFeatureIds = orderedMSFeatureIds;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		try {
			readFeaturesFromFile();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(project.getMsFeaturesForDataPipeline(pipeline) == null 
				|| project.getMsFeaturesForDataPipeline(pipeline).isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		try {
			createDataMatrix();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void createDataMatrix() {
		
		Matrix featureMetaDataMatrix = createFeatureMetaDataMatrix();
		Matrix dataFileMetaDataMatrix = createDataFileMetaDataMatrix();
		Matrix dataMatrix = ProjectUtils.loadDataMatrixForPipelineWitoutMetaData(
				project, pipeline);
		if(dataMatrix != null) {
			
			dataMatrix.setMetaDataDimensionMatrix(0, featureMetaDataMatrix);
			dataMatrix.setMetaDataDimensionMatrix(1, dataFileMetaDataMatrix);
			project.setDataMatrixForDataPipeline(pipeline, dataMatrix);
		}
	}
	
	private Matrix createFeatureMetaDataMatrix() {
		
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
	
	private Matrix createDataFileMetaDataMatrix() {
		
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

	private void readFeaturesFromFile() {
		
		taskDescription = "Writing features for " + pipeline.getName();
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
			taskDescription = "Extracting MS features for data pipeline " + pipeline.getName();
			total = featureElementList.size();
			processed = 0;			
			for (Element featureElement : featureElementList) {

				featureSet.add(new LibraryMsFeature(featureElement));
				processed++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		recreateDefaultDataSet(featureSet);
	}
	
	private void recreateDefaultDataSet(Set<MsFeature>featureSet) {
		
		project.setFeaturesForDataPipeline(pipeline, featureSet);

		MsFeatureSet allFeatures = 
				new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),	
						project.getMsFeaturesForDataPipeline(pipeline));
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		project.addFeatureSetForDataPipeline(allFeatures, pipeline);
	}
	
	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return new LoadPipelineDataTask(
				project, pipeline, orderedDataFileNames, orderedMSFeatureIds);
	}
}
