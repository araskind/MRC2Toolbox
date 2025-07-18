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

import java.util.Collection;
import java.util.Set;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class MsFeatureAveragingTask extends AbstractTask {

	private DataPipeline pipeline;
	private Set<DataFile>dataFiles;
	private Collection<LibraryMsFeature>averagedFeatures;
	private DataAnalysisProject currentExperiment;
	private Matrix featureDataMatrix;
	
	public MsFeatureAveragingTask(DataPipeline pipeline, Set<DataFile> dataFiles) {
		super();
		this.pipeline = pipeline;
		this.dataFiles = dataFiles;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
	}

	public MsFeatureAveragingTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			loadFeatureMatrix();
		}
		catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			if(e.getCause() != null && e.getCause().getMessage() != null)
				errorMessage += "\n" + e.getCause().getMessage();
			
			setStatus(TaskStatus.ERROR);
		}
		try {
			generateAverageFeatures();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			if(e.getCause() != null && e.getCause().getMessage() != null)
				errorMessage += "\n" + e.getCause().getMessage();
			
			setStatus(TaskStatus.ERROR);
		}
	}
	
	private void generateAverageFeatures() {
		
		taskDescription = "Generating averaged features ...";
		Matrix featureMatrix = featureDataMatrix.getMetaDataDimensionMatrix(0);
		total = Math.toIntExact(featureMatrix.getColumnCount());
		processed = 0;
		
		
		long[]coord = new long[] {0,0};
		for(long i=0; i<featureMatrix.getColumnCount(); i++) {
			
			coord[1] = i;
			MsFeature msf = (MsFeature)featureDataMatrix.getAsObject(coord);
			Object[]selectedFeatureData = 
					featureDataMatrix.selectColumns(Ret.LINK, i).transpose().toObjectArray()[0];
		}		
	}
	
	private void loadFeatureMatrix() {
		
		taskDescription = "Loading feature matrix ...";
		total = 100;
		processed = 20;
		
		featureDataMatrix = currentExperiment.getFeatureMatrixForDataPipeline(pipeline);
		if(featureDataMatrix != null)
			return;
		else {
			featureDataMatrix = ProjectUtils.readFeatureMatrix(
					currentExperiment, pipeline, false);
			currentExperiment.setFeatureMatrixForDataPipeline(pipeline, featureDataMatrix);
		}
	}

	public DataPipeline getPipeline() {
		return pipeline;
	}

	public Set<DataFile> getDataFiles() {
		return dataFiles;
	}

	public Collection<LibraryMsFeature> getAveragedFeatures() {
		return averagedFeatures;
	}

	@Override
	public Task cloneTask() {
		return new MsFeatureAveragingTask(pipeline, dataFiles);
	}
}
