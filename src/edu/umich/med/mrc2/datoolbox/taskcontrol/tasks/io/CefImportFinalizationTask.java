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
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CefImportFinalizationObjest;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class CefImportFinalizationTask extends AbstractTask {

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
		
		calculateFeatureStatistics();
		
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
		try {
			saveDataMatrixes();			
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		removeTempDirectory();
		
		setStatus(TaskStatus.FINISHED);
	}

	//	TODO - deal with standard samples
	//	Move "remove empty features" to a separate task to allow multipart imports
	//	removeEmptyFeatures();
	private void calculateFeatureStatistics() {

		taskDescription = "Calculating Feature Statistics ...";
		total = retentionMap.size();
		
		//	Calculate RT stats 
		Map<String, DescriptiveStatistics>rtStatsMap = 
				new TreeMap<String, DescriptiveStatistics>();
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
		Map<String, DescriptiveStatistics>mzStatsMap = 
				new TreeMap<String, DescriptiveStatistics>();
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
		Map<String, DescriptiveStatistics>peakWidthStatsMap = 
				new TreeMap<String, DescriptiveStatistics>();
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
		
		Map<String,Long>dataFileRowMap = new TreeMap<String,Long>();
		for(int i=0; i<dataFiles.length; i++)
			dataFileRowMap.put(dataFiles[i].getName(), (long)i);
		
		Map<Long,Long>fileToRTRowMap = new TreeMap<Long,Long>();
		for(Entry<String,Long>ent : dataFileRowMap.entrySet()) {
			
			long dfCol = rtMatrix.getRowForLabel(ent.getKey());
			if(dfCol >= 0)
				fileToRTRowMap.put(ent.getValue(), dfCol);
		}
		Object[]featureArray = 
				featureMatrix.getMetaDataDimensionMatrix(0).
				selectRows(Ret.LINK, 0).toObjectArray()[0];
		
		Map<String,Long>featureColumnMap = new TreeMap<String,Long>();
		for(int i=0; i<featureArray.length; i++)
			featureColumnMap.put(((MsFeature)featureArray[i]).getName(), (long)i);
		
		Map<Long,Long>featureToRTColumnMap = new TreeMap<Long,Long>();		
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
		}		
		System.out.println("***");
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
		
		Set<MsFeature> featuresWithAbnormalIsoPattern = new HashSet<MsFeature>();
		for(MsFeature feature : library.getFeatures()) {
			
			if(feature.getStatsSummary() != null 
					&& feature.getStatsSummary().getMzStatistics().getStandardDeviation() > 0.1d) {
				featuresWithAbnormalIsoPattern.add(feature);
			}
			if(Math.abs(feature.getMonoisotopicMz() - feature.getBasePeakMz()) > 0.01)
				featuresWithAbnormalIsoPattern.add(feature);
		}
		if(featuresWithAbnormalIsoPattern.isEmpty())
			return;
			
		ArrayList<Long> rem = new ArrayList<Long>();
		for (MsFeature cf : featuresWithAbnormalIsoPattern)
			rem.add(dataMatrix.getColumnForLabel(cf));

		Matrix newFeatureMatrix = 
				dataMatrix.getMetaDataDimensionMatrix(0).deleteColumns(Ret.NEW, rem);
		Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
		newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMatrix);
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
		
		//	TODO write log with removed features
		ArrayList<String>removedFeaturesLog = new ArrayList<String>();
		ArrayList<String>fLine = new ArrayList<String>();
		removedFeaturesLog.add("The following features with abnormal isotopic patterns were removed:\n");
		for (MsFeature cf : featuresWithAbnormalIsoPattern) {
			
			fLine.clear();
			fLine.add(cf.getId());
			fLine.add(cf.getName());
			fLine.add(MRC2ToolBoxConfiguration.getRtFormat().format(cf.getRetentionTime()));
			fLine.add(MRC2ToolBoxConfiguration.getMzFormat().format(cf.getMonoisotopicMz()));
			removedFeaturesLog.add(StringUtils.join(fLine, "\t"));
		}
		DataAnalysisProject currentExperiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();	
		
		Path logPath = Paths.get(currentExperiment.getExportsDirectory().getAbsolutePath(), 
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

	private void removeEmptyFeatures() {

		taskDescription = "Removing features with no data ...";
		total = 100;
		processed = 20;

		List<LibraryMsFeature> featuresToRemove = library.getFeatures().stream().
				filter(f -> (f.getStatsSummary().getSampleFrequency() == 0.0d)).
				filter(f -> (f.getStatsSummary().getPooledFrequency() == 0.0d)).
				filter(f -> (f.getStatsSummary().getMeanObservedRetention() == 0.0d)).
				collect(Collectors.toList());

		if(!featuresToRemove.isEmpty()) {

			Matrix featureMatrix = dataMatrix.getMetaDataDimensionMatrix(0);

			List<Long> rem = featuresToRemove.stream().
					mapToLong(cf -> dataMatrix.getColumnForLabel(cf)).
					boxed().collect(Collectors.toList());

			processed = 50;

			Matrix newDataMatrix = dataMatrix.deleteColumns(Ret.NEW, rem);
			Matrix newFeatureMatrix = featureMatrix.deleteColumns(Ret.NEW, rem);
			newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMatrix);
			newDataMatrix.setMetaDataDimensionMatrix(1, dataMatrix.getMetaDataDimensionMatrix(1));
			dataMatrix = newDataMatrix;

			library.removeFeatures(featuresToRemove);
		}
		dataMatrix.replace(Ret.ORIG, 0.0, Double.NaN);
		processed = 100;
		setStatus(TaskStatus.FINISHED);
	}
	
	private void addDataToExperiment() {
		
		DataAnalysisProject currentExperiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();		
		currentExperiment.addDataPipeline(dataPipeline);

		//	Attach library
		currentExperiment.setCompoundLibraryForDataPipeline(dataPipeline, library);

		//	Attach data
		currentExperiment.setDataMatrixForDataPipeline(dataPipeline, dataMatrix);
		
		currentExperiment.setFeaturesForDataPipeline(
				dataPipeline, new HashSet<MsFeature>(library.getFeatures()));
		currentExperiment.addDataFilesForAcquisitionMethod(
				dataPipeline.getAcquisitionMethod(), Arrays.asList(dataFiles));		
		currentExperiment.addFeatureMatrixForDataPipeline(dataPipeline, featureMatrix);		

		MsFeatureSet allFeatures = 
				new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),	
						currentExperiment.getMsFeaturesForDataPipeline(dataPipeline));
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		currentExperiment.addFeatureSetForDataPipeline(allFeatures, dataPipeline);
	}
	
	private void saveDataMatrixes() {
		
		DataAnalysisProject experimentToSave = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if (experimentToSave.getDataMatrixForDataPipeline(dataPipeline) != null) {

			taskDescription = "Saving data matrix for  " + experimentToSave.getName() +
					"(" + dataPipeline.getName() + ")";
			processed = 50;			
			ProjectUtils.saveDataMatrixForPipeline(experimentToSave, dataPipeline);
			
			taskDescription = "Saving feature matrix for  " + experimentToSave.getName() +
					"(" + dataPipeline.getName() + ")";
			processed = 70;

			ProjectUtils.saveFeatureMatrixToFile(
					featureMatrix,
					experimentToSave, 
					dataPipeline,
					false);
			
			experimentToSave.setFeatureMatrixForDataPipeline(dataPipeline, null);
			featureMatrix = null;
			System.gc();
		}		
	}
	
	@Override
	public Task cloneTask() {
		return new CefImportFinalizationTask(ciFinObj);
	}

}
