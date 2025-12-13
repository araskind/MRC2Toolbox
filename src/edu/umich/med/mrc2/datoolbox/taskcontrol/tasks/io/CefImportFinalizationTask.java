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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CefImportFinalizationObjest;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.CalculateStatisticsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.RemoveEmptyFeaturesTask;

public class CefImportFinalizationTask extends AbstractTask implements TaskListener{

	private CefImportFinalizationObjest ciFinObj;

	private DataFile[] dataFiles;
	private DataPipeline dataPipeline;
	private CompoundLibrary library;
	private Matrix featureMatrix;
	private Matrix dataMatrix;
	private Matrix rtMatrix;
	private Map<String, List<Double>>retentionMap;
	private Map<String, List<Double>>mzMap;
	private Map<String, List<Double>> peakWidthMap;
	private boolean removeAbnormalIsoPatterns;
	private File tmpCefDirectory;
	
	public CefImportFinalizationTask(CefImportFinalizationObjest ciFinObj) {
		super();
		this.ciFinObj = ciFinObj;
		this.dataFiles = ciFinObj.getDataFiles();
		this.dataPipeline = ciFinObj.getDataPipeline();
		this.library = ciFinObj.getLibrary();
		this.featureMatrix = ciFinObj.getFeatureMatrix();
		this.dataMatrix = ciFinObj.getDataMatrix();
		this.rtMatrix = ciFinObj.getRtMatrix();
		this.retentionMap = ciFinObj.getRetentionMap();
		this.mzMap = ciFinObj.getMzMap();
		this.peakWidthMap = ciFinObj.getPeakWidthMap();
		this.removeAbnormalIsoPatterns = ciFinObj.isRemoveAbnormalIsoPatterns();
		this.tmpCefDirectory = ciFinObj.getTmpCefDirectory();
	}	

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Finalizing CEF import ...";
		
		calculateFeaturePeakQualityStatistics();
		
		if(rtMatrix != null)
			updateRTvaluesForFeatures();
			
		if(removeAbnormalIsoPatterns)
			removeFeaturesWithAbnormalIsoPattern();	
			
		try {
			addDataToExperiment();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void calculateFeaturePeakQualityStatistics() {

		taskDescription = "Calculating Feature Statistics ...";
		total = retentionMap.size();
		processed = 0;
		
		//	Calculate RT stats 
		Map<String, DescriptiveStatistics>rtStatsMap = new TreeMap<>();
		for(Entry<String, List<Double>> rtCollection : retentionMap.entrySet()) {
			
			double[] rtValues = new double[0];
			try {
				rtValues = rtCollection.getValue().stream().
						filter(Objects::nonNull).
						mapToDouble(Double::doubleValue).toArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rtStatsMap.put(rtCollection.getKey(), new DescriptiveStatistics(rtValues));
			processed++;
		}
		processed = 0;
		
		//	Calculate M/Z stats
		Map<String, DescriptiveStatistics>mzStatsMap = new TreeMap<>();
		for(Entry<String, List<Double>> mzCollection : mzMap.entrySet()) {
			
			double[] mzValues = new double[0];
			try {
				mzValues = mzCollection.getValue().stream().
						filter(Objects::nonNull).
						mapToDouble(Double::doubleValue).toArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mzStatsMap.put(mzCollection.getKey(), new DescriptiveStatistics(mzValues));
			processed++;
		}
		processed = 0;
		
		// Calculate peak width stats
		Map<String, DescriptiveStatistics>peakWidthStatsMap = new TreeMap<>();
		for(Entry<String, List<Double>> pwCollection : peakWidthMap.entrySet()) {
			
			double[] pwValues = new double[0];
			try {
				pwValues = pwCollection.getValue().stream().
						filter(Objects::nonNull).
						mapToDouble(Double::doubleValue).toArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			peakWidthStatsMap.put(pwCollection.getKey(), new DescriptiveStatistics(pwValues));
			processed++;
		}
		processed = 0;
		
		library.getFeatures().stream().forEach(f -> {
			f.getStatsSummary().setRtStatistics(rtStatsMap.get(f.getId()));
			f.getStatsSummary().setPeakWidthStatistics(peakWidthStatsMap.get(f.getId()));
			f.getStatsSummary().setMzStatistics(mzStatsMap.get(f.getId()));
		});
	}
	
	private void updateRTvaluesForFeatures() {
		
		taskDescription = "Updating RT values for individual features ...";
		total = dataFiles.length;
		processed = 0;
		
		Map<String,Long>dataFileRowMap = new TreeMap<>();
		for(int i=0; i<dataFiles.length; i++)
			dataFileRowMap.put(dataFiles[i].getName(), (long)i);
		
		Map<Long,Long>fileToRTRowMap = new TreeMap<>();
		for(Entry<String,Long>ent : dataFileRowMap.entrySet()) {
			
			long dfCol = rtMatrix.getRowForLabel(ent.getKey());
			if(dfCol >= 0)
				fileToRTRowMap.put(ent.getValue(), dfCol);
		}
		Object[]featureArray = 
				featureMatrix.getMetaDataDimensionMatrix(0).
				selectRows(Ret.LINK, 0).toObjectArray()[0];
		
		Map<String,Long>featureColumnMap = new TreeMap<>();
		for(int i=0; i<featureArray.length; i++)
			featureColumnMap.put(((MsFeature)featureArray[i]).getName(), (long)i);
		
		Map<Long,Long>featureToRTColumnMap = new TreeMap<>();		
		for(Entry<String,Long>ent : featureColumnMap.entrySet()) {
			
			long rtCol = rtMatrix.getColumnForLabel(ent.getKey());
			if(rtCol >= 0)
				featureToRTColumnMap.put(ent.getValue(), rtCol);
		}
		long[]featureCoordinates = new long[2];
		long[]rtCoordinates = new long[2];
		
		for(Entry<Long,Long>fileMapEntry : fileToRTRowMap.entrySet()) {
			
			for(Entry<Long,Long>featureMapEntry : featureToRTColumnMap.entrySet()) {
				
				featureCoordinates[0] = fileMapEntry.getKey();
				featureCoordinates[1] = featureMapEntry.getKey();
				rtCoordinates[0] = fileMapEntry.getValue();
				rtCoordinates[1] = featureMapEntry.getValue();
				
				SimpleMsFeature sf = (SimpleMsFeature)featureMatrix.getAsObject(featureCoordinates);
				double rt = rtMatrix.getAsDouble(rtCoordinates);
				if(sf != null && rt > 0.0d)
					sf.setRetentionTime(rt);
			}
			processed++;
		}		
	}
	
	private void removeTempDirectory() {
		
		if(tmpCefDirectory != null && tmpCefDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(tmpCefDirectory);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void removeFeaturesWithAbnormalIsoPattern() {
		
		taskDescription = "Removing features with abnormal isotopic patterns";
		total = 100;
		processed = 20;
		
		Set<MsFeature> featuresWithAbnormalIsoPattern = findFeaturesWithAbnormalIsoPattern();
		if(featuresWithAbnormalIsoPattern.isEmpty())
			return;
			
		ArrayList<Long> rem = new ArrayList<>();
		for (MsFeature cf : featuresWithAbnormalIsoPattern)
			rem.add(dataMatrix.getColumnForLabel(cf));

		Matrix newFeatureMetadataMatrix = 
				dataMatrix.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, rem);
		Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
		newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMetadataMatrix);
		newDataMatrix.setMetaDataDimensionMatrix(1, dataMatrix.getMetaDataDimensionMatrix(1));
		dataMatrix = newDataMatrix;
		
		if(featureMatrix != null) {
			
			Matrix newMsFeatureLabelMatrix = 
					featureMatrix.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, rem);			
			Matrix newMsFeatureMatrix = featureMatrix.deleteColumns(Ret.NEW, rem);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(0, newMsFeatureLabelMatrix);
			newMsFeatureMatrix.setMetaDataDimensionMatrix(1, featureMatrix.getMetaDataDimensionMatrix(1));
			featureMatrix = newMsFeatureMatrix;
		}
		library.getFeatures().removeAll(featuresWithAbnormalIsoPattern);
		writeFeatureRemovalLog(featuresWithAbnormalIsoPattern);
		processed = 100;
	}
	
	private Set<MsFeature> findFeaturesWithAbnormalIsoPattern(){
		
		Set<MsFeature> featuresWithAbnormalIsoPattern = new HashSet<>();
		for(MsFeature feature : library.getFeatures()) {
			
			if(feature.getStatsSummary() != null 
					&& feature.getStatsSummary().getMzStatistics().getStandardDeviation() > 0.1d) {
				featuresWithAbnormalIsoPattern.add(feature);
			}
			if(Math.abs(feature.getMonoisotopicMz() - feature.getBasePeakMz()) > 0.01)
				featuresWithAbnormalIsoPattern.add(feature);
		}
		return featuresWithAbnormalIsoPattern;
	}
	
	private void writeFeatureRemovalLog( Set<MsFeature> featuresWithAbnormalIsoPattern) {
		
		ArrayList<String>removedFeaturesLog = new ArrayList<>();
		ArrayList<String>fLine = new ArrayList<>();
		removedFeaturesLog.add("The following features with abnormal isotopic patterns were removed:\n");
		for (MsFeature cf : featuresWithAbnormalIsoPattern) {
			
			fLine.clear();
			fLine.add(cf.getId());
			fLine.add(cf.getName());
			fLine.add(MRC2ToolBoxConfiguration.getRtFormat().format(cf.getRetentionTime()));
			fLine.add(MRC2ToolBoxConfiguration.getMzFormat().format(cf.getMonoisotopicMz()));
			removedFeaturesLog.add(StringUtils.join(fLine, "\t"));
		}
		Path logPath = Paths.get(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory().getAbsolutePath(), 
				dataPipeline.getName() + "_featureRemovalLog.txt");
		try {
		    Files.write(logPath, 
		    		removedFeaturesLog,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private void addDataToExperiment() {
		
		DataAnalysisProject currentExperiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();		
		currentExperiment.addDataPipeline(dataPipeline);

		//	Attach data
		currentExperiment.setDataMatrixForDataPipeline(dataPipeline, dataMatrix);
		
		currentExperiment.setFeaturesForDataPipeline(
				dataPipeline, new HashSet<>(library.getFeatures()));
		currentExperiment.addDataFilesForAcquisitionMethod(
				dataPipeline.getAcquisitionMethod(), Arrays.asList(dataFiles));		
		currentExperiment.addFeatureMatrixForDataPipeline(dataPipeline, featureMatrix);		

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

	private synchronized void finalizeDataImport(RemoveEmptyFeaturesTask source) {

		removeTempDirectory();		
		setStatus(TaskStatus.FINISHED);
	}

	private synchronized void finalizeCalculateStatisticsTask(CalculateStatisticsTask task) {

		RemoveEmptyFeaturesTask cleanupTask = 
				new RemoveEmptyFeaturesTask(MRC2ToolBoxCore.getActiveMetabolomicsExperiment(), dataPipeline);
		cleanupTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(cleanupTask);
	}
		
	@Override
	public Task cloneTask() {
		return new CefImportFinalizationTask(ciFinObj);
	}

}
