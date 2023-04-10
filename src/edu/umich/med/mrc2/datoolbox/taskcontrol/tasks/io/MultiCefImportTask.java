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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureAlignmentType;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.CefLibraryImportTask;

public class MultiCefImportTask extends AbstractTask implements TaskListener{

	private File libraryFile;
	private DataFile[] dataFiles;
	private DataPipeline dataPipeline;
	private FeatureAlignmentType alignmentType;
	private boolean fileLoadInitiated;
	private HashMap<DataFile, HashSet<SimpleMsFeature>>featureData;
	private CompoundLibrary library;
	private TreeSet<String> unmatchedAdducts;
	private Matrix featureMatrix;
	private Matrix dataMatrix;
	private boolean libraryParsed;
	private boolean dataParsed;
	private DescriptiveStatistics descStats;
	private Map<String,Integer>featureCoordinateMap;
	private int fileCounter;
	private Map<String, List<Double>>retentionMap;
	private Map<String, List<Double>>mzMap;
	private HashMap<DataFile, HashSet<SimpleMsFeature>> featureDataPers;
	private File cacheFile;
	private File tmpCefDirectory;

	public MultiCefImportTask(
			File libraryFile, 
			DataFile[] dataFiles, 
			DataPipeline dataPipeline, 
			FeatureAlignmentType alignmentType) {

		super();
		this.libraryFile = libraryFile;
		this.dataFiles = dataFiles;
		this.dataPipeline = dataPipeline;
		this.alignmentType = alignmentType;
		
		fileLoadInitiated = false;
		featureData = new HashMap<DataFile, HashSet<SimpleMsFeature>>();
		unmatchedAdducts = new TreeSet<String>();
		libraryParsed = false;
		dataParsed = false;
		fileCounter = 0;
	}

	public MultiCefImportTask(
			File libraryFile, 
			Set<SampleDataResultObject> dataToImport, 
			DataPipeline dataPipeline,
			FeatureAlignmentType alignmentType,
			File tmpCefDirectory) {
		super();
		this.libraryFile = libraryFile;
		this.dataPipeline = dataPipeline;
		this.alignmentType = alignmentType;
		this.dataFiles = dataToImport.stream().
				map(s -> s.getDataFile()).
				toArray(size -> new DataFile[size]);
		this.tmpCefDirectory = tmpCefDirectory;
		
		fileLoadInitiated = false;
		featureData = new HashMap<DataFile, HashSet<SimpleMsFeature>>();
		unmatchedAdducts = new TreeSet<String>();
		libraryParsed = false;
		dataParsed = false;
		fileCounter = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		//	Read library
		taskDescription = "Loading library file ...";
		total = 100;
		processed = 20;
		CefLibraryImportTask lit = 
				new CefLibraryImportTask(dataPipeline, libraryFile, false, false);
		lit.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(lit);
	}

	@Override
	public Task cloneTask() {
		return new MultiCefImportTask(
				libraryFile, dataFiles, dataPipeline, alignmentType);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(CefLibraryImportTask.class) && !libraryParsed)
				finalizeLibraryImportTask((CefLibraryImportTask)e.getSource());
			
			if (e.getSource().getClass().equals(CefDataImportTask.class))
				finalizeCefImportTask((CefDataImportTask)e.getSource());			
		}
	}
	
	private void finalizeLibraryImportTask(CefLibraryImportTask lit) {
		
		libraryParsed = libraryCorrectlyParsed(lit);
		if(libraryParsed) {

			//	Create array to align features
			initDataMatrixes();

			//	Spin off tasks to read individual CEF files
			if(!fileLoadInitiated) {
				initDataLoad();
				fileLoadInitiated = true;
			}
		}
		else {
			setStatus(TaskStatus.ERROR);
			return;
		}
	}
	
	private void finalizeCefImportTask(CefDataImportTask cdit) {
		
		fileCounter++;
		if(!cdit.getUnmatchedAdducts().isEmpty()) {

			@SuppressWarnings("unused")
			InformationDialog id = new InformationDialog(
				"Unmatched adducts",
				"Not all adducts were matched to the database.\n"
				+ "Below is the list of unmatched adducts.",
				StringUtils.join(unmatchedAdducts, "\n"),
				null);

			setStatus(TaskStatus.ERROR);
			MRC2ToolBoxCore.getTaskController().cancelAllTasks();
			MainWindow.hideProgressDialog();
			return;
		}
		//	Process
		if(fileCounter == dataFiles.length) {

			dataParsed = true;
			try {
				finalizeDataParsing();
				addDataToExperiment();
				saveDataMatrixes();
				setStatus(TaskStatus.FINISHED);
			} catch (Exception e1) {
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
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
		currentExperiment.setDataFilesForAcquisitionMethod(
				dataPipeline.getAcquisitionMethod(), getDataFiles());		
		currentExperiment.addFeatureMatrixForDataPipeline(dataPipeline, featureMatrix);		

		MsFeatureSet allFeatures = 
				new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),	
						currentExperiment.getMsFeaturesForDataPipeline(dataPipeline));
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		currentExperiment.addFeatureSetForDataPipeline(allFeatures, dataPipeline);
	}
	
	private void saveDataMatrixes() {
		
		DataAnalysisProject experimentToSave = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if (experimentToSave.getDataMatrixForDataPipeline(dataPipeline) != null) {

			taskDescription = "Saving data matrix for  " + experimentToSave.getName() +
					"(" + dataPipeline.getName() + ")";
			processed = 50;
			File dataMatrixFile = Paths.get(experimentToSave.getExperimentDirectory().getAbsolutePath(), 
					experimentToSave.getDataMatrixFileNameForDataPipeline(dataPipeline)).toFile();
			try {
				Matrix dataMatrix = Matrix.Factory
						.linkToArray(experimentToSave.getDataMatrixForDataPipeline(dataPipeline).
								toDoubleArray());
				dataMatrix.save(dataMatrixFile);
				processed = 80;
			} catch (IOException e) {
				e.printStackTrace();
//					setStatus(TaskStatus.ERROR);
			}
			String featureMatrixFileName = experimentToSave.getFeatureMatrixFileNameForDataPipeline(dataPipeline);
			if(featureMatrixFileName != null) {
				
				taskDescription = "Saving feature matrix for  " + experimentToSave.getName() +
						"(" + dataPipeline.getName() + ")";
				processed = 90;
				File featureMatrixFile = 
						Paths.get(experimentToSave.getExperimentDirectory().getAbsolutePath(), 
						featureMatrixFileName).toFile();
				try {
					Matrix featureMatrix = Matrix.Factory
							.linkToArray(experimentToSave.getFeatureMatrixForDataPipeline(dataPipeline).toObjectArray());
					featureMatrix.save(featureMatrixFile);
					processed = 100;
				} catch (IOException e) {
					e.printStackTrace();
//					setStatus(TaskStatus.ERROR);
				}
				experimentToSave.setFeatureMatrixForDataPipeline(dataPipeline, null);
				featureMatrix = null;
				System.gc();
			}
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

	private void finalizeDataParsing() {

		taskDescription = "Finalizing data";
		total = library.getFeatures().size();
		processed = 0;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//	Calculate RT stats 
		Map<String, DescriptiveStatistics>rtStatsMap = new TreeMap<String, DescriptiveStatistics>();
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
		Map<String, DescriptiveStatistics>mzStatsMap = new TreeMap<String, DescriptiveStatistics>();
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
		for(MsFeature feature : library.getFeatures()) {

			MsFeatureStatisticalSummary ss = feature.getStatsSummary();			
			ss.setRtStatistics(rtStatsMap.get(feature.getId()));
			ss.setMzStatistics(mzStatsMap.get(feature.getId()));
			processed++;
		}
		//	TODO - deal with standard samples
		//	Move "remove empty features" to a separate task to allow multipart imports
		//	removeEmptyFeatures();
		if(tmpCefDirectory != null && tmpCefDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(tmpCefDirectory);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initDataMatrixes() {

		featureCoordinateMap = new ConcurrentHashMap<String,Integer>();
		retentionMap = new ConcurrentHashMap<String, List<Double>>();
		mzMap = new ConcurrentHashMap<String, List<Double>>();
		AtomicInteger counter = new AtomicInteger(0);
		MsFeature[] features =
			library.getFeatures().stream().
			sorted(new MsFeatureComparator(SortProperty.ID)).	//	Better sort by ID?
			map(f -> {
				f.setStatsSummary(new MsFeatureStatisticalSummary(f));
				featureCoordinateMap.put(f.getId(), counter.getAndIncrement());
				retentionMap.put(f.getId(), new CopyOnWriteArrayList<Double>());
				mzMap.put(f.getId(), new CopyOnWriteArrayList<Double>());
				return f;
			}).
			toArray(size -> new MsFeature[size]);

		// Create feature matrix
		Object[][] featureArray = new Object[dataFiles.length][features.length];
		featureMatrix = Matrix.Factory.linkToArray(featureArray);
		featureMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])features));
		featureMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])dataFiles).transpose(Ret.NEW));

		double[][] quantitativeMatrix = 
				new double[dataFiles.length][library.getFeatures().size()];
		dataMatrix = Matrix.Factory.linkToArray(quantitativeMatrix);
		dataMatrix.setMetaDataDimensionMatrix(
				0, Matrix.Factory.linkToArray((Object[])features));
		dataMatrix.setMetaDataDimensionMatrix(
				1, Matrix.Factory.linkToArray((Object[])dataFiles).transpose(Ret.NEW));
	}

	private boolean libraryCorrectlyParsed(CefLibraryImportTask lit) {

		boolean libraryParsed = true;
		library = lit.getParsedLibrary();

		// Show unassigned features
		if (!lit.getUnassignedFeatures().isEmpty()) {

			ArrayList<String> flist = new ArrayList<String>();

			for (MsFeature msf : lit.getUnassignedFeatures())
				flist.add(msf.getName());

			@SuppressWarnings("unused")
			InformationDialog id = new InformationDialog(
					"Unmatched features",
					"Not all features were matched to the library.\nBelow is the list of unmatched features.",
					StringUtils.join(flist, "\n"),
					null);
		}
		// Show unassigned adducts
		if (!lit.getUnmatchedAdducts().isEmpty()) {

			@SuppressWarnings("unused")
			InformationDialog id = new InformationDialog(
					"Unmatched adducts",
					"Not all adducts were matched to the database.\nBelow is the list of unmatched adducts.",
					StringUtils.join(lit.getUnmatchedAdducts(), "\n"),
					null);
		}
		if (!lit.getUnassignedFeatures().isEmpty() || !lit.getUnmatchedAdducts().isEmpty()) {

			setStatus(TaskStatus.ERROR);
			return false;
		}
		return libraryParsed;
	}

	private void initDataLoad() {

		taskDescription = "Loading individual data files ...";
		total = dataFiles.length;
		processed = 0;
		DataAcquisitionMethod acqMethod = dataPipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = dataPipeline.getDataExtractionMethod();
		for(int i=0; i<dataFiles.length; i++) {

			dataFiles[i].setDataAcquisitionMethod(acqMethod);
			CefDataImportTask cdit = new CefDataImportTask(
					dataFiles[i],
					dataFiles[i].getResultForDataExtractionMethod(daMethod),
					i, 
					featureMatrix, 
					dataMatrix, 
					featureCoordinateMap, 
					retentionMap, 
					mzMap);

			cdit.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(cdit);
		}
	}

	/**
	 * @return the method
	 */
	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	/**
	 * @return the unmatchedAdducts
	 */
	public TreeSet<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

	/**
	 * @return the featureMatrix
	 */
	public Matrix getFeatureMatrix() {
		return featureMatrix;
	}

	/**
	 * @return the dataMatrix
	 */
	public Matrix getDataMatrix() {
		return dataMatrix;
	}

	/**
	 * @return the library
	 */
	public CompoundLibrary getLibrary() {
		return library;
	}

	/**
	 * @return the dataFiles
	 */
	public Collection<DataFile> getDataFiles() {		
		return Arrays.asList(dataFiles);
	}
}























