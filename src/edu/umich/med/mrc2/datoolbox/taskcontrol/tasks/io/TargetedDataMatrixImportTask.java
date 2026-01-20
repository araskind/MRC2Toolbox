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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.TargetedDataMatrixImportSettingsObject;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.CalculateStatisticsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.RemoveEmptyFeaturesTask;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class TargetedDataMatrixImportTask extends AbstractTask implements TaskListener {

	private Set<SampleDataResultObject> dataToImport;
	private DataPipeline dataPipeline;
	private File inputDataFile;
	private int linesToSkipAfterHeader;
	private String featureColumn;
	private String retentionColumn;
	private Map<String,LibraryMsFeature>nameFeatureMap;
	
	private String[][] inputDataArray;
	private Map<LibraryMsFeature,Integer>featureLineMap;
	private Map<DataFile,Integer>fileColumnMap;
	private Matrix dataMatrix;
	private LibraryMsFeature[] featureArray;
	private DataFile[]dataFileArray;
		
	public TargetedDataMatrixImportTask(TargetedDataMatrixImportSettingsObject importettings) {
		super();
		this.dataToImport = importettings.getDataToImport();
		this.dataPipeline = importettings.getImportPipeline();
		this.inputDataFile = importettings.getInputDataFile();
		this.linesToSkipAfterHeader = importettings.getLinesToSkipAfterHeader();
		this.featureColumn = importettings.getFeatureColumn();
		this.retentionColumn = importettings.getRetentionColumn();
		this.nameFeatureMap = importettings.getNameFeatureMap();
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		try {
			parseInputFile();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		initDataMatrix();
		populateDataMatrix();
		addDataToExperiment();
	}

	private void parseInputFile() {

		taskDescription = "Populating file and feature maps ...";
		total = 100;
		processed = 20;
		
		inputDataArray = 
				DelimitedTextParser.parseDataFileBasedOnExtension(inputDataFile);

		int featureColumnIndex = -1;
		int retentionColumnIndex = -1;
		// Create file / column map
		fileColumnMap = new TreeMap<>();
		for (int i = 0; i < inputDataArray[0].length; i++) {

			String colName = inputDataArray[0][i].trim();
			if (featureColumn.trim().equals(colName))
				featureColumnIndex = i;
			
			if (retentionColumn != null && retentionColumn.trim().equals(colName))
				retentionColumnIndex = i;

			SampleDataResultObject sdro = dataToImport.stream().
					filter(o -> o.getDataFile().getName().equals(colName))
					.findFirst().orElse(null);
			if (sdro != null)
				fileColumnMap.put(sdro.getDataFile(), i);
		}
		// Create feature / line map
		featureLineMap = new HashMap<>();
		Polarity ppPoplarity = dataPipeline.getAcquisitionMethod().getPolarity();
		for (int i = 1 + linesToSkipAfterHeader; i < inputDataArray.length; i++) {

			String featureName = inputDataArray[i][featureColumnIndex].trim();
			LibraryMsFeature libFeature = nameFeatureMap.get(featureName);
			if (libFeature != null) {
				libFeature.setPolarity(ppPoplarity);
				if(retentionColumnIndex > -1) {
					
					String rtString = inputDataArray[i][retentionColumnIndex].trim();
					if(NumberUtils.isCreatable(rtString))
						libFeature.setRetentionTime(NumberUtils.createDouble(rtString));
				}
				featureLineMap.put(libFeature, i);
			}
		}
	}

	private void initDataMatrix() {

		double[][] quantitativeMatrix = 
				new double[fileColumnMap.size()][featureLineMap.size()];
		dataMatrix = Matrix.Factory.linkToArray(quantitativeMatrix);
		
		featureArray = 
				featureLineMap.keySet().toArray(new LibraryMsFeature[featureLineMap.size()]);
		dataMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])featureArray));
		
		dataFileArray = 
				fileColumnMap.keySet().toArray(new DataFile[fileColumnMap.size()]);
		dataMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])dataFileArray).transpose(Ret.NEW));
	}

	private void populateDataMatrix() {

		taskDescription = "Populating data matrix ...";
		total = featureArray.length;
		processed = 0;
		
		long[]dataMatrixCoordinates = new long[2];
		
		for(int i=0; i<featureArray.length; i++) {

			dataMatrixCoordinates[1] = i;
			for(int j=0; j<dataFileArray.length; j++) {
				
				dataMatrixCoordinates[0] = j;
				String measure =  inputDataArray
						[featureLineMap.get(featureArray[i])]
						[fileColumnMap.get(dataFileArray[j])];
				Double value = Double.NaN;
				if(NumberUtils.isParsable(measure))
					value = Double.parseDouble(measure);
				
				dataMatrix.setAsDouble(value, dataMatrixCoordinates);
			}
			processed++;
		}
	}
	
	
	private void addDataToExperiment() {
		
		DataAnalysisProject currentExperiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();		
		currentExperiment.addDataPipeline(dataPipeline);

		//	Attach data
		currentExperiment.setDataMatrixForDataPipeline(dataPipeline, dataMatrix);
		
		currentExperiment.setFeaturesForDataPipeline(
				dataPipeline, new HashSet<>(nameFeatureMap.values()));
		currentExperiment.addDataFilesForAcquisitionMethod(
				dataPipeline.getAcquisitionMethod(), fileColumnMap.keySet());		

		MsFeatureSet allFeatures = 
				new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),	
						currentExperiment.getMsFeaturesForDataPipeline(dataPipeline));
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		currentExperiment.addFeatureSetForDataPipeline(allFeatures, dataPipeline);
		
		CalculateStatisticsTask statsTask = new CalculateStatisticsTask(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(), 
				 dataPipeline, true);
		statsTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(statsTask);
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(CalculateStatisticsTask.class))
				finalizeCalculateStatisticsTask((CalculateStatisticsTask)e.getSource());
			
			if (e.getSource().getClass().equals(RemoveEmptyFeaturesTask.class))
				finalizeDataImport((RemoveEmptyFeaturesTask)e.getSource());
		}	
	}

	private synchronized void finalizeCalculateStatisticsTask(CalculateStatisticsTask task) {

		RemoveEmptyFeaturesTask cleanupTask = 
				new RemoveEmptyFeaturesTask(MRC2ToolBoxCore.getActiveMetabolomicsExperiment(), dataPipeline);
		cleanupTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(cleanupTask);
	}
	
	private void finalizeDataImport(RemoveEmptyFeaturesTask source) {
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return null;
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}
}
