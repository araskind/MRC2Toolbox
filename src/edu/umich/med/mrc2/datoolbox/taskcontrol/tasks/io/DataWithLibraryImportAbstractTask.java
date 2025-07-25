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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.CefImportFinalizationObjest;
import edu.umich.med.mrc2.datoolbox.data.CefImportSettingsObject;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public abstract class DataWithLibraryImportAbstractTask extends AbstractTask implements TaskListener{

	protected File libraryFile;
	protected DataFile[] dataFiles;
	protected DataPipeline dataPipeline;
	protected CompoundLibrary library;
	protected Matrix featureMatrix;
	protected Matrix dataMatrix;
	protected Matrix rtMatrix;
	protected Map<String,Integer>featureCoordinateMap;
	protected Map<String, List<Double>>retentionMap;
	protected Map<String, List<Double>>mzMap;
	protected Map<String, List<Double>> peakWidthMap;
	protected File tmpCefDirectory;
	protected boolean removeAbnormalIsoPatterns;
	protected int fileCounter;
	protected SortedSet<String> unmatchedAdducts;
	protected Map<String, String> libFeatureNameIdMap;
	
	protected void initDataMatrixes() {

		featureCoordinateMap = new ConcurrentHashMap<String,Integer>();
		retentionMap = new ConcurrentHashMap<String, List<Double>>();
		mzMap = new ConcurrentHashMap<String, List<Double>>();
		peakWidthMap = new ConcurrentHashMap<String, List<Double>>();
		AtomicInteger counter = new AtomicInteger(0);
		LibraryMsFeature[] features =
			library.getFeatures().stream().
			sorted(new MsFeatureComparator(SortProperty.ID)).	//	Better sort by ID?
			map(f -> {
				f.setStatsSummary(new MsFeatureStatisticalSummary(f));
				featureCoordinateMap.put(f.getId(), counter.getAndIncrement());
				retentionMap.put(f.getId(), new CopyOnWriteArrayList<Double>());
				peakWidthMap.put(f.getId(), new CopyOnWriteArrayList<Double>());
				mzMap.put(f.getId(), new CopyOnWriteArrayList<Double>());
				return f;
			}).
			toArray(size -> new LibraryMsFeature[size]);

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
	
	protected void initDataLoad() {

		taskDescription = "Loading individual CEF data files ...";
		total = dataFiles.length;
		processed = 0;
		DataAcquisitionMethod acqMethod = dataPipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = dataPipeline.getDataExtractionMethod();
		for(int i=0; i<dataFiles.length; i++) {

			if(dataFiles[i].getDataAcquisitionMethod() == null)
				dataFiles[i].setDataAcquisitionMethod(acqMethod);
			
			CefImportSettingsObject ciso = new CefImportSettingsObject();
			ciso.setDataFile(dataFiles[i]);
			ciso.setResultsFile(dataFiles[i].getResultForDataExtractionMethod(daMethod));
			ciso.setFileIndex(i);
			ciso.setFeatureMatrix(featureMatrix);
			ciso.setDataMatrix(dataMatrix);
			ciso.setFeatureCoordinateMap(featureCoordinateMap);
			ciso.setRetentionMap(retentionMap);
			ciso.setMzMap(mzMap);
			ciso.setPeakWidthMap(peakWidthMap);
			ciso.setLibFeatureNameIdMap(libFeatureNameIdMap);
			
			CefDataImportTask cdit = new CefDataImportTask(ciso);
			cdit.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(cdit);
		}
	}
	
	protected synchronized void finalizeCefImportTask(CefDataImportTask cdit) {
		
		fileCounter++;
		System.out.println("Imported file " + fileCounter + " out of " 
				+ dataFiles.length + " -> " + cdit.getInputCefFile().getName());
		MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(cdit);
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
		if(fileCounter == dataFiles.length) {
			
			CefImportFinalizationObjest ciFinObj = new CefImportFinalizationObjest();
			ciFinObj.setDataFiles(dataFiles);
			ciFinObj.setDataPipeline(dataPipeline);
			ciFinObj.setLibrary(library);
			ciFinObj.setFeatureMatrix(featureMatrix);
			ciFinObj.setDataMatrix(dataMatrix);
			ciFinObj.setRtMatrix(rtMatrix);
			ciFinObj.setRetentionMap(retentionMap);
			ciFinObj.setMzMap(mzMap);
			ciFinObj.setPeakWidthMap(peakWidthMap);
			ciFinObj.setRemoveAbnormalIsoPatterns(removeAbnormalIsoPatterns);
			ciFinObj.setTmpCefDirectory(tmpCefDirectory);			

			CefImportFinalizationTask finalizationTask = 
					new CefImportFinalizationTask(ciFinObj);
			finalizationTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(finalizationTask);
		}
	}
	
	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Matrix getFeatureMatrix() {
		return featureMatrix;
	}

	public Matrix getDataMatrix() {
		return dataMatrix;
	}

	public CompoundLibrary getLibrary() {
		return library;
	}

	public Collection<DataFile> getDataFiles() {		
		return Arrays.asList(dataFiles);
	}

	public SortedSet<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}
}
