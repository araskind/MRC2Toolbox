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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsObject;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class MsFeatureAveragingTask extends AbstractTask {

	private DataPipeline pipeline;
	private Collection<DataFile>dataFiles;
	private CompoundLibrary averagedLibrary;
	private DataAnalysisProject currentExperiment;
	private Matrix featureDataMatrix;
	
	public MsFeatureAveragingTask(
			DataPipeline pipeline, 
			Collection<DataFile> dataFiles) {
		super();
		this.pipeline = pipeline;
		this.dataFiles = dataFiles;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			loadFeatureMatrix();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
			return;
		}
		List<MsFeatureStatsObject>statObjectList = null;
		try {
			statObjectList = collectFeatureData();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
			return;
		}
		if(!statObjectList.isEmpty()) {			
			try {				
				generateAverageFeatures(statObjectList);		
			}
			catch (Exception e) {
				reportErrorAndExit(e);
				return;
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private List<MsFeatureStatsObject>collectFeatureData() {
		
		taskDescription = "Collecting feature data ...";
		Matrix featureMatrix = featureDataMatrix.getMetaDataDimensionMatrix(0);
		Object[]featureArray = 
				featureMatrix.selectRows(Ret.LINK, 0).toObjectArray()[0];
		total = featureArray.length;
		processed = 0;
		
		List<MsFeatureStatsObject>statObjectList = new ArrayList<MsFeatureStatsObject>();
		for(int i=0; i<featureArray.length; i++) {

			LibraryMsFeature msf = (LibraryMsFeature)featureArray[i];
			MsFeatureStatsObject statObject = new MsFeatureStatsObject(msf);
			Object[][]selectedFeatureData = 
				featureDataMatrix.selectColumns(Ret.LINK, i).toObjectArray();
			
			for(int j=0; j<selectedFeatureData.length; j++) {

				if(selectedFeatureData[j][0] == null)
					continue;
				
				SimpleMsFeature sFeature = (SimpleMsFeature)selectedFeatureData[j][0];
				statObject.addRtValue(sFeature.getRetentionTime());
				statObject.addRtRange(sFeature.getRtRange());
				statObject.addSpectrum(sFeature.getObservedSpectrum());
			}
			statObjectList.add(statObject);
			processed++;			
		}
		return statObjectList;
	}
	
	private void generateAverageFeatures(List<MsFeatureStatsObject>statObjectList) {
		
		taskDescription = "Generating averaged features ...";
		total = statObjectList.size();
		processed = 0;
		
		averagedLibrary = new CompoundLibrary("Averaged features for " + pipeline.getName());
		averagedLibrary.setPolarity(pipeline.getAcquisitionMethod().getPolarity());
		
		for(MsFeatureStatsObject statObject : statObjectList) {

			LibraryMsFeature newLibFeature = 
					new LibraryMsFeature(statObject.getMsFeature());
			newLibFeature.setRetentionTime(statObject.getMedianRt());
			if(statObject.getMedianRtRange() != null)
				newLibFeature.setRtRange(statObject.getMedianRtRange());
			
			MassSpectrum avgSpectrum = statObject.getAverageScaledMassSpectrum();
			if(avgSpectrum != null && avgSpectrum.getBasePeak() != null) {			
				newLibFeature.setSpectrum(avgSpectrum);		
				averagedLibrary.addFeature(newLibFeature);
			}
			processed++;
		}				
	}
	
	private void loadFeatureMatrix() {
		
		taskDescription = "Loading feature matrix ...";
		total = 100;
		processed = 20;
		
		Matrix completeFeatureDataMatrix = 
				currentExperiment.getFeatureMatrixForDataPipeline(pipeline);
		if(completeFeatureDataMatrix == null){
			completeFeatureDataMatrix = 
					ProjectUtils.readFeatureMatrix(currentExperiment, pipeline, false);
			currentExperiment.setFeatureMatrixForDataPipeline(
					pipeline, completeFeatureDataMatrix);
		}
		featureDataMatrix = DataSetUtils.subsetDataMatrixByDataFiles(
					completeFeatureDataMatrix, dataFiles);
		System.out.println("***");
	}

	public DataPipeline getPipeline() {
		return pipeline;
	}

	public Collection<DataFile> getDataFiles() {
		return dataFiles;
	}

	public CompoundLibrary getAveragedFeaturesLibrary() {
		return averagedLibrary;
	}

	@Override
	public Task cloneTask() {
		return new MsFeatureAveragingTask(pipeline, dataFiles);
	}
}
