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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.ResultsFile;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class CefDataImportTask extends CEFProcessingTask {
	
	private DataFile dataFile;
	private ResultsFile resultsFile;
	private DataPipeline dataPipeline;
	private HashSet<SimpleMsFeature>features;

	private int fileIndex;
	private Matrix featureMatrix;
	private Matrix dataMatrix;
	private Map<String, Integer> featureCoordinateMap;
	private Map<String, List<Double>> retentionMap;
	private Map<String, List<Double>> mzMap;
	private Map<String, List<Double>> peakWidthMap;
	
	public CefDataImportTask(
			DataFile dataFile,
			ResultsFile resultsFile,
			int fileIndex,
			Matrix featureMatrix,
			Matrix dataMatrix,
			Map<String, Integer> featureCoordinateMap,
			Map<String, List<Double>> retentionMap,
			Map<String, List<Double>> mzMap,
			Map<String, List<Double>> peakWidthMap) {

		this.dataFile = dataFile;
		this.resultsFile = resultsFile;
		this.fileIndex = fileIndex;
		this.featureMatrix = featureMatrix;
		this.dataMatrix = dataMatrix;
		this.featureCoordinateMap = featureCoordinateMap;
		this.retentionMap = retentionMap;
		this.mzMap = mzMap;
		this.peakWidthMap = peakWidthMap;

		total = 100;
		processed = 2;
		taskDescription = "Importing MS data from " + dataFile.getName();
		features = new HashSet<SimpleMsFeature>();
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public Task cloneTask() {
		return new CefDataImportTask(
				 dataFile,
				 resultsFile,
				 fileIndex,
				 featureMatrix,
				 dataMatrix,
				 featureCoordinateMap,
				 retentionMap,
				 mzMap,
				 peakWidthMap);
	}

	@Override
	public void run() {
		
		if(featureCoordinateMap == null) {
			errorMessage = "Fature coordinates map missing";
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.PROCESSING);
		// Read CEF file
		String cefPath = null;
		if(resultsFile != null && resultsFile.getFullPath() != null)
			cefPath = resultsFile.getFullPath();	
		else if(dataPipeline != null && dataPipeline.getDataExtractionMethod() != null) 
			cefPath = dataFile.getResultForDataExtractionMethod(
					dataPipeline.getDataExtractionMethod()).getFullPath();
		else {
			System.err.println("Path to CEF file not specified");
			setStatus(TaskStatus.ERROR);
			return;
		}
		inputCefFile = new File(cefPath);		
		try {
			parseInputCefFile(inputCefFile);
		} catch (Exception e) {
			errorMessage = "Failed to parse " + inputCefFile.getName();
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		recordDataIntoFeatureMatrix();
		setStatus(TaskStatus.FINISHED);
	}

	private void recordDataIntoFeatureMatrix() {

		taskDescription = "Parsing CEF data file...";
		features = new HashSet<SimpleMsFeature>();
		total = inputFeatureList.size();
		processed = 0;
		
		long[] coordinates = new long[2];
		coordinates[0] = fileIndex;
		
		for(MsFeature feature : inputFeatureList) {

			SimpleMsFeature msf = new SimpleMsFeature(feature, dataPipeline);
			msf.setRtRange(feature.getRtRange());
			
				if(featureCoordinateMap.get(msf.getLibraryTargetId()) != null) {

					retentionMap.get(msf.getLibraryTargetId()).add(msf.getRetentionTime());
					mzMap.get(msf.getLibraryTargetId()).add(msf.getObservedSpectrum().getMonoisotopicMz());
					if(feature.getRtRange() != null)
						peakWidthMap.get(msf.getLibraryTargetId()).add(feature.getRtRange().getSize());
						
					coordinates[1] = featureCoordinateMap.get(msf.getLibraryTargetId());
					featureMatrix.setAsObject(msf, coordinates);
					dataMatrix.setAsDouble(msf.getArea(), coordinates);
				}
				else {
					//	TODO handle library mismatches
					System.out.println(msf.getName() + "(" + msf.getLibraryTargetId() + ") not in the library.");
				}
			
			features.add(msf);
			processed++;
		}
	}

	public DataFile getInputCefFile() {
		return dataFile;
	}

	public HashSet<SimpleMsFeature> getFeatures() {
		return features;
	}

	/**
	 * @return the unmatchedAdducts
	 */
	public TreeSet<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

}
