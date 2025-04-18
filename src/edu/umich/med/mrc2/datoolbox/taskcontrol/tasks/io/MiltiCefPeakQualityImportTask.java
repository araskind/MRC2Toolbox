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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.utils.FileNameUtils;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class MiltiCefPeakQualityImportTask extends AbstractTask implements TaskListener {

	private DataAnalysisProject currentExperiment;
	private DataPipeline dataPipeline;
	private Collection<File>sourceFiles;
	private Matrix featureMatrix;
	private Map<String,Long>featureCoordinateMap;
	private int taskCount, processedCount;
		
	public MiltiCefPeakQualityImportTask(
			DataAnalysisProject currentExperiment, 
			DataPipeline dataPipeline,
			Collection<File> sourceFiles) {
		super();
		this.currentExperiment = currentExperiment;
		this.dataPipeline = dataPipeline;
		this.sourceFiles = sourceFiles;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		readFeatureMatrix();
		createFeatureCoordinateMap();
		initDataLoad();
	}

	private void createFeatureCoordinateMap(){
		
		featureCoordinateMap = new HashMap<String,Long>();
		Matrix featureMetaData = featureMatrix.getMetaDataDimensionMatrix(0);
		long[] coordinates = new long[2];
		coordinates[0] = 0;
		for (int i = 0; i < featureMetaData.getColumnCount(); i++) {
			coordinates[1] = i;
			SimpleMsFeature labelFeature = (SimpleMsFeature) featureMatrix.getAsObject(coordinates);
			if(labelFeature != null && labelFeature.getLibraryTargetId() != null)
				featureCoordinateMap.put(labelFeature.getLibraryTargetId(), coordinates[1]);
		}		
	}
	
	private void initDataLoad() {
		
		taskCount = 0;
		processedCount = 0;
		Set<DataFile> dataFiles = 
				currentExperiment.getDataFilesWithDataForPipeline(dataPipeline);
		for(DataFile df : dataFiles){
			
			File inputCef = sourceFiles.stream().
					filter(f -> FileNameUtils.getBaseName(f.getName()).equals(df.getBaseName())).
					findFirst().orElse(null);
			if(inputCef != null) {
				
				CefPeakQualityImportTask task = new CefPeakQualityImportTask(
						df, inputCef, featureMatrix,featureCoordinateMap);
				task.addTaskListener(this);
				taskCount++;
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
	}
		
	private void readFeatureMatrix() {
		
		taskDescription = "Reading feature data matrix ...";
		featureMatrix = 
				ProjectUtils.readFeatureMatrix(currentExperiment, dataPipeline, false);
		if(featureMatrix == null) {
			errorMessage = "Unable to read feature data matrix file";
			setStatus(TaskStatus.ERROR);
			return;
		}
	}
		
	private void saveFeatureMatrix() {
		// TODO Auto-generated method stub
		taskDescription = "Saving updated feature data matrix ...";
		ProjectUtils.saveFeatureMatrixToFile(
				featureMatrix,
				currentExperiment, 
				dataPipeline,
				false);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(CefPeakQualityImportTask.class)) {
				
				processedCount++;
				System.out.println(Integer.toString(processedCount) + " out of " + Integer.toString(taskCount));
				if(taskCount == processedCount) {					
					
					System.out.println("**********\nSaving updated matrix ...");
					saveFeatureMatrix();
					System.out.println("Matrix saved");
					setStatus(TaskStatus.FINISHED);
					return;
				}
			}
		}
		if (e.getStatus() == TaskStatus.CANCELED || e.getStatus() == TaskStatus.ERROR) {
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			MainWindow.hideProgressDialog();
		}
	}

	@Override
	public Task cloneTask() {

		return new MiltiCefPeakQualityImportTask(
				currentExperiment, dataPipeline, sourceFiles);
	}
}
