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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CefImportSettingsObject;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureAlignmentType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MultiCefDataAddTask extends AbstractTask implements TaskListener{
	
	private DataAnalysisProject currentExperiment;
	private DataPipeline dataPipeline;
	private DataFile[] addedDataFiles;
	private MsFeature[] features;
	private FeatureAlignmentType alignmentType;
	private TreeSet<String> unmatchedAdducts;
	private Matrix featureMatrix;
	private Matrix addedDataMatrix;
	boolean dataParsed;
	private Map<String,Integer>featureCoordinateMap;
	private int fileCounter;
	private Map<String, List<Double>>retentionMap;
	private Map<String, List<Double>> peakWidthMap;
	private Map<String, List<Double>>mzMap;
	
	public MultiCefDataAddTask(
			DataPipeline dataPipeline, 
			FeatureAlignmentType alignmentType) {
		this.dataPipeline = dataPipeline;
		this.alignmentType = alignmentType;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		fileCounter = 0;
		addedDataFiles = new DataFile[0];
	}
	
	public MultiCefDataAddTask(
			DataFile[] dataFiles, 
			DataPipeline dataPipeline, 
			FeatureAlignmentType alignmentType) {

		this(dataPipeline, alignmentType);
		addedDataFiles = dataFiles;
	}

	public MultiCefDataAddTask(
			Set<SampleDataResultObject> dataToImport, 
			DataPipeline dataPipeline,
			FeatureAlignmentType alignmentType) {
		
		this(dataPipeline, alignmentType);		
		addedDataFiles = dataToImport.stream().
				map(s -> s.getDataFile()).
				toArray(size -> new DataFile[size]);
		fileCounter = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		dataParsed = false;
		initDataMatrixes();
		initDataLoad();
	}

	private void initDataMatrixes() {

		featureCoordinateMap = new ConcurrentHashMap<>();
		retentionMap = new ConcurrentHashMap<>();
		peakWidthMap = new ConcurrentHashMap<>();
		mzMap = new ConcurrentHashMap<>();
		AtomicInteger counter = new AtomicInteger(0);
		features = currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).
			stream().sorted(new MsFeatureComparator(SortProperty.ID)).
			map(f -> {
				featureCoordinateMap.put(f.getId(), counter.getAndIncrement());
				retentionMap.put(f.getId(), new CopyOnWriteArrayList<>());
				peakWidthMap.put(f.getId(), new CopyOnWriteArrayList<>());
				mzMap.put(f.getId(), new CopyOnWriteArrayList<>());
				return f;
			}).
			toArray(size -> new MsFeature[size]);

		// Create feature matrix
		Object[][] featureArray = new Object[addedDataFiles.length][features.length];
		featureMatrix = Matrix.Factory.linkToArray(featureArray);
		featureMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])features));
		featureMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])addedDataFiles).transpose(Ret.NEW));

		double[][] quantitativeMatrix = 
				new double[addedDataFiles.length][currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).size()];
		addedDataMatrix = Matrix.Factory.linkToArray(quantitativeMatrix);
		addedDataMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])features));
		addedDataMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])addedDataFiles).transpose(Ret.NEW));
	}

	private void initDataLoad() {

		taskDescription = "Loading individual data files ...";
		total = addedDataFiles.length;
		processed = 0;

		DataAcquisitionMethod acqMethod = dataPipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = dataPipeline.getDataExtractionMethod();
		for(int i=0; i<addedDataFiles.length; i++) {

			addedDataFiles[i].setDataAcquisitionMethod(acqMethod);
			
			CefImportSettingsObject ciso = new CefImportSettingsObject();
			ciso.setDataFile(addedDataFiles[i]);
			ciso.setResultsFile(addedDataFiles[i].getResultForDataExtractionMethod(daMethod));
			ciso.setFileIndex(i);
			ciso.setFeatureMatrix(featureMatrix);
			ciso.setDataMatrix(addedDataMatrix);
			ciso.setFeatureCoordinateMap(featureCoordinateMap);
			ciso.setRetentionMap(retentionMap);
			ciso.setMzMap(mzMap);
			ciso.setPeakWidthMap(peakWidthMap);
			
			CefDataImportTask cdit = new CefDataImportTask(ciso);
			cdit.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(cdit);
		}
	}

	private synchronized void finalizeCefImportTask(CefDataImportTask cdit) {
		
		fileCounter++;
		if(!cdit.getUnmatchedAdducts().isEmpty()) {

			//	TODO this needs change, handle in the calling panel
			InformationDialog id = new InformationDialog(
				"Unmatched adducts",
				"Not all adducts were matched to the database.\n"
				+ "Below is the list of unmatched adducts.",
				StringUtils.join(unmatchedAdducts, "\n"),
				null);
			id.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			id.setVisible(true);
			
			setStatus(TaskStatus.ERROR);
			MRC2ToolBoxCore.getTaskController().cancelAllTasks();
			MainWindow.hideProgressDialog();
			return;
		}
		//	Process
		if(fileCounter == addedDataFiles.length) {

			dataParsed = true;
			try {
				mergeDataMatrixes();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e1) {
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
	}

	private void mergeDataMatrixes() {
		
		taskDescription = "Adding imported data to experiment ...";
		total = features.length;
		processed = 0;
		
		Matrix loadedDataMatrix = currentExperiment.getDataMatrixForDataPipeline(dataPipeline);
		
		//	Data file
		TreeSet<DataFile>allFiles = new TreeSet<DataFile>();
		Set<DataFile> loadedFiles = 
				currentExperiment.getDataFilesForAcquisitionMethod(dataPipeline.getAcquisitionMethod());		
		allFiles.addAll(loadedFiles);
		allFiles.addAll(Arrays.asList(addedDataFiles));
		DataFile[]fileArray = allFiles.toArray(new DataFile[allFiles.size()]);
		
		//	New quant matrix
		double[][] quantitativeMatrix = 
				new double[fileArray.length][currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).size()];
		Matrix newDataMatrix = Matrix.Factory.linkToArray(quantitativeMatrix);
		newDataMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])features));
		newDataMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])fileArray).transpose(Ret.NEW));
		
		//	File coordinates
		HashMap<DataFile,Long>loadedFileCoordinates = new HashMap<>();
		for(DataFile f : loadedFiles)
			loadedFileCoordinates.put(f, loadedDataMatrix.getRowForLabel(f));
		
		HashMap<DataFile,Long>addedFileCoordinates = new HashMap<>();
		for(DataFile f : addedDataFiles)
			addedFileCoordinates.put(f, addedDataMatrix.getRowForLabel(f));
		
		HashMap<DataFile,Long>newFileCoordinates = new HashMap<>();
		for(DataFile f : fileArray)
			newFileCoordinates.put(f, newDataMatrix.getRowForLabel(f));
		
		//	Feature coordinates
		HashMap<MsFeature,Long>loadedFeatureCoordinates = new HashMap<>();
		for(MsFeature msf : features)
			loadedFeatureCoordinates.put(msf, loadedDataMatrix.getColumnForLabel(msf));
		
		HashMap<MsFeature,Long>addedFeatureCoordinates = new HashMap<>();
		for(MsFeature msf : features)
			addedFeatureCoordinates.put(msf, addedDataMatrix.getColumnForLabel(msf));
		
		HashMap<MsFeature,Long>newFeatureCoordinates = new HashMap<>();
		for(MsFeature msf : features)
			newFeatureCoordinates.put(msf, newDataMatrix.getColumnForLabel(msf));
		
		long[] loadedCoordinates = new long[2];
		long[] addedCoordinates = new long[2];
		long[] newCoordinates = new long[2];
		
		//	Calculate RT stats 
		Map<String, DescriptiveStatistics>rtStatsMap = new TreeMap<>();
		for(Entry<String, List<Double>> rtCollection : retentionMap.entrySet()) {
			
			double[] rtValues = new double[0];
			try {
				rtValues = rtCollection.getValue().stream().
						filter(rt -> Objects.nonNull(rt)).
						mapToDouble(Double::doubleValue).toArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rtStatsMap.put(rtCollection.getKey(), new DescriptiveStatistics(rtValues));
		}	
		//	Calculate M/Z stats
		Map<String, DescriptiveStatistics>mzStatsMap = new TreeMap<>();
		for(Entry<String, List<Double>> mzCollection : mzMap.entrySet()) {
			
			double[] mzValues = new double[0];
			try {
				mzValues = mzCollection.getValue().stream().
						filter(mz -> Objects.nonNull(mz)).
						mapToDouble(Double::doubleValue).toArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mzStatsMap.put(mzCollection.getKey(), new DescriptiveStatistics(mzValues));
		}
		// Calculate peak width stats
		Map<String, DescriptiveStatistics>peakWidthStatsMap = new TreeMap<>();
		for(Entry<String, List<Double>> pwCollection : peakWidthMap.entrySet()) {
			
			double[] pwValues = new double[0];
			try {
				pwValues = pwCollection.getValue().stream().
						filter(pw -> Objects.nonNull(pw)).
						mapToDouble(Double::doubleValue).toArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			peakWidthStatsMap.put(pwCollection.getKey(), new DescriptiveStatistics(pwValues));
		}
		for(MsFeature msf : features) {
			
			//	Set feature coordinate
			loadedCoordinates[1] = loadedFeatureCoordinates.get(msf);
			addedCoordinates[1] = addedFeatureCoordinates.get(msf);
			newCoordinates[1] = newFeatureCoordinates.get(msf);
			
			//	Copy loaded data
			for(DataFile ldf : loadedFiles) {
				
				loadedCoordinates[0] = loadedFileCoordinates.get(ldf);
				newCoordinates[0] = newFileCoordinates.get(ldf);
				double loadedValue = loadedDataMatrix.getAsDouble(loadedCoordinates);
				newDataMatrix.setAsDouble(loadedValue, newCoordinates);				
			}
			//	Copy new data
			for(DataFile adf : addedDataFiles) {
				
				addedCoordinates[0] = addedFileCoordinates.get(adf);
				newCoordinates[0] = newFileCoordinates.get(adf);
				double addedValue = addedDataMatrix.getAsDouble(addedCoordinates);
				newDataMatrix.setAsDouble(addedValue, newCoordinates);	
			}
			MsFeatureStatisticalSummary statSummary = msf.getStatsSummary();
			
			// Update RT stats;
			DescriptiveStatistics newRtStats = rtStatsMap.get(msf.getId());
			if(newRtStats != null) {
				for(double rt : newRtStats.getValues())
					statSummary.getRtStatistics().addValue(rt);
			}
			// Update MZ stats;
			DescriptiveStatistics newMzStats = mzStatsMap.get(msf.getId());
			if(newMzStats != null) {
				for(double mz : newMzStats.getValues())
					statSummary.getMzStatistics().addValue(mz);
			}
			// Update peak width stats;
			DescriptiveStatistics newPwStats = peakWidthStatsMap.get(msf.getId());
			if(newPwStats != null) {
				for(double pw : newPwStats.getValues())
					statSummary.getPeakWidthStatistics().addValue(pw);
			}
			processed++;
		}
		addedDataMatrix = newDataMatrix;
		addedDataMatrix.replace(Ret.ORIG, 0.0, Double.NaN);
	}
	
//	private void removeEmptyFeatures() {
//
//		taskDescription = "Removing features with no data ...";
//		total = 100;
//		processed = 20;
//
//		List<LibraryMsFeature> featuresToRemove = library.getFeatures().stream().
//				filter(f -> (f.getStatsSummary().getSampleFrequency() == 0.0d)).
//				filter(f -> (f.getStatsSummary().getPooledFrequency() == 0.0d)).
//				filter(f -> (f.getStatsSummary().getMeanObservedRetention() == 0.0d)).
//				collect(Collectors.toList());
//
//		if(!featuresToRemove.isEmpty()) {
//
//			Matrix featureMatrix = addedDataMatrix.getMetaDataDimensionMatrix(0);
//
//			List<Long> rem = featuresToRemove.stream().
//					mapToLong(cf -> addedDataMatrix.getColumnForLabel(cf)).
//					boxed().collect(Collectors.toList());
//
//			processed = 50;
//
//			Matrix newDataMatrix = addedDataMatrix.deleteColumns(Ret.NEW, rem);
//			Matrix newFeatureMatrix = featureMatrix.deleteColumns(Ret.NEW, rem);
//			newDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMatrix);
//			newDataMatrix.setMetaDataDimensionMatrix(1, addedDataMatrix.getMetaDataDimensionMatrix(1));
//			addedDataMatrix = newDataMatrix;
//
//			library.removeFeatures(featuresToRemove);
//		}
//		addedDataMatrix.replace(Ret.ORIG, 0.0, Double.NaN);
//		processed = 100;
//		setStatus(TaskStatus.FINISHED);
//	}
	
	/**
	 * @return the method
	 */
	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Set<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

	public Matrix getFeatureMatrix() {
		return featureMatrix;
	}

	public Matrix getDataMatrix() {
		return addedDataMatrix;
	}

	public Collection<DataFile> getDataFiles() {		
		return Arrays.asList(addedDataFiles);
	}
	
	@Override
	public Task cloneTask() {
		return new MultiCefDataAddTask(addedDataFiles, dataPipeline, alignmentType);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(CefDataImportTask.class))
				finalizeCefImportTask((CefDataImportTask)e.getSource());			
		}
	}
}























