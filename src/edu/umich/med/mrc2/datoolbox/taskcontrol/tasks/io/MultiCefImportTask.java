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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
	private CompoundLibrary library;
	private SortedSet<String> unmatchedAdducts;
	private Matrix featureMatrix;
	private Matrix dataMatrix;
	private boolean libraryParsed;
	private Map<String,Integer>featureCoordinateMap;
	private int fileCounter;
	private Map<String, List<Double>>retentionMap;
	private Map<String, List<Double>>mzMap;
	private Map<String, List<Double>> peakWidthMap;
	private File tmpCefDirectory;
	private boolean removeAbnormalIsoPatterns;

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
		unmatchedAdducts = new TreeSet<String>();
		libraryParsed = false;
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
		unmatchedAdducts = new TreeSet<String>();
		libraryParsed = false;
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
			
			if (e.getSource().getClass().equals(CefDataImportTask.class)) {
				finalizeCefImportTask((CefDataImportTask)e.getSource());	
			}
			
			if (e.getSource().getClass().equals(CefImportFinalizationTask.class))
				setStatus(TaskStatus.FINISHED);
		}
	}
	
	private synchronized void finalizeLibraryImportTask(CefLibraryImportTask lit) {
		
		libraryParsed = libraryCorrectlyParsed(lit);
		if(libraryParsed) {

			MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(lit);
			
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
		}
	}
	
	private synchronized void finalizeCefImportTask(CefDataImportTask cdit) {
		
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
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(fileCounter == dataFiles.length) {
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("CEF impotr complete, starting data set assembly ...");
			
			CefImportFinalizationObjest ciFinObj = new CefImportFinalizationObjest();
			ciFinObj.setDataFiles(dataFiles);
			ciFinObj.setDataPipeline(dataPipeline);
			ciFinObj.setLibrary(library);
			ciFinObj.setFeatureMatrix(featureMatrix);
			ciFinObj.setDataMatrix(dataMatrix);
			ciFinObj.setRetentionMap(retentionMap);
			ciFinObj.setMzMap(mzMap);
			ciFinObj.setPeakWidthMap(peakWidthMap);
			ciFinObj.setRemoveAbnormalIsoPatterns(removeAbnormalIsoPatterns);
			ciFinObj.setTmpCefDirectory(tmpCefDirectory);

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CefImportFinalizationTask finalizationTask = 
					new CefImportFinalizationTask(ciFinObj);
			finalizationTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(finalizationTask);
		}
	}

	private void initDataMatrixes() {

		featureCoordinateMap = new ConcurrentHashMap<String,Integer>();
		retentionMap = new ConcurrentHashMap<String, List<Double>>();
		mzMap = new ConcurrentHashMap<String, List<Double>>();
		peakWidthMap = new ConcurrentHashMap<String, List<Double>>();
		AtomicInteger counter = new AtomicInteger(0);
		MsFeature[] features =
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

		library = lit.getParsedLibrary();

		// Show unassigned features
		if (!lit.getUnassignedFeatures().isEmpty()) {

			ArrayList<String> flist = new ArrayList<String>();

			for (MsFeature msf : lit.getUnassignedFeatures())
				flist.add(msf.getName());

			//	TODO this needs change, handle in the calling panel
			InformationDialog id = new InformationDialog(
					"Unmatched features",
					"Not all features were matched to the library.\n"
					+ "Below is the list of unmatched features.",
					StringUtils.join(flist, "\n"),
					null);
			id.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			id.setVisible(true);
		}
		// Show unassigned adducts
		if (!lit.getUnmatchedAdducts().isEmpty()) {

			@SuppressWarnings("unused")
			InformationDialog id = new InformationDialog(
					"Unmatched adducts",
					"Not all adducts were matched to the database.\n"
					+ "Below is the list of unmatched adducts.",
					StringUtils.join(lit.getUnmatchedAdducts(), "\n"),
					null);
		}
		if (!lit.getUnassignedFeatures().isEmpty() || !lit.getUnmatchedAdducts().isEmpty()) {

			setStatus(TaskStatus.ERROR);
			return false;
		}
		return true;
	}

	private void initDataLoad() {

		taskDescription = "Loading individual data files ...";
		total = dataFiles.length;
		processed = 0;
		DataAcquisitionMethod acqMethod = dataPipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = dataPipeline.getDataExtractionMethod();
		for(int i=0; i<dataFiles.length; i++) {

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
			
			CefDataImportTask cdit = new CefDataImportTask(ciso);
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
	public SortedSet<String> getUnmatchedAdducts() {
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

	public boolean isRemoveAbnormalIsoPatterns() {
		return removeAbnormalIsoPatterns;
	}

	public void setRemoveAbnormalIsoPatterns(boolean removeAbnormalIsoPatterns) {
		this.removeAbnormalIsoPatterns = removeAbnormalIsoPatterns;
	}
}























