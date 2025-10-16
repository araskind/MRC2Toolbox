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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.CefImportSettingsObject;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.ResultsFile;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class CefDataImportTask extends CEFProcessingTask {
	
	private CefImportSettingsObject ciso;
	
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
	private Map<String, String> libFeatureNameIdMap;
	
	public CefDataImportTask(CefImportSettingsObject ciso) {
		
		this.ciso = ciso;
		this.dataFile = ciso.getDataFile();
		this.resultsFile = ciso.getResultsFile();
		this.fileIndex = ciso.getFileIndex();
		this.featureMatrix = ciso.getFeatureMatrix();
		this.dataMatrix = ciso.getDataMatrix();
		this.featureCoordinateMap = ciso.getFeatureCoordinateMap();
		this.retentionMap = ciso.getRetentionMap();
		this.mzMap = ciso.getMzMap();
		this.peakWidthMap = ciso.getPeakWidthMap();
		this.libFeatureNameIdMap = ciso.getLibFeatureNameIdMap();
	}

	@Override
	public Task cloneTask() {
		return new CefDataImportTask(ciso);
	}

	@Override
	public void run() {
		
		if(featureCoordinateMap == null) {
			errorMessage = "Fature coordinates map missing";
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 2;
		taskDescription = "Importing MS data from " + dataFile.getName();
		features = new HashSet<SimpleMsFeature>();
		unmatchedAdducts = new TreeSet<String>();
		
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
		assignMissingTargetIds();
		recordDataIntoFeatureMatrix();
		setStatus(TaskStatus.FINISHED);
	}

	private void assignMissingTargetIds(){
		
		if(libFeatureNameIdMap == null || libFeatureNameIdMap.isEmpty())
			return;
		
		List<MsFeature> featuresWithMissingTargetIds = 
				inputFeatureList.stream().
				filter(f -> Objects.isNull(f.getTargetId())).
				collect(Collectors.toList());
		if(featuresWithMissingTargetIds.isEmpty())
			return;
		
		total = featuresWithMissingTargetIds.size();
		processed = 0;
		taskDescription = "Assigning target IDs for " + dataFile.getName();
		for(MsFeature msf : featuresWithMissingTargetIds) {
			
			String idName = null;
			if(msf.getPrimaryIdentity() != null && msf.getPrimaryIdentity().getCompoundName() != null)
				idName = msf.getPrimaryIdentity().getCompoundName();
			
			msf.setTargetId(libFeatureNameIdMap.get(idName));			
		}
	}
	
	private void recordDataIntoFeatureMatrix() {

		taskDescription = "Parsing CEF data file...";
		features = new HashSet<SimpleMsFeature>();
		total = inputFeatureList.size();
		processed = 0;
		
		long[] coordinates = new long[2];
		coordinates[0] = fileIndex;
		
		for(MsFeature feature : inputFeatureList) {
			
			if(feature.getTargetId() == null)
				System.out.println(feature.getName() + " not in the library?");

			SimpleMsFeature msf = new SimpleMsFeature(feature, dataPipeline);
			msf.setRtRange(feature.getRtRange());
			String targetId = msf.getLibraryTargetId();
			if(featureCoordinateMap.get(targetId) != null) {

				retentionMap.get(targetId).add(msf.getRetentionTime());
				mzMap.get(targetId).add(msf.getObservedSpectrum().getMonoisotopicMz());
				if(feature.getRtRange() != null)
					peakWidthMap.get(targetId).add(feature.getRtRange().getSize());
					
				coordinates[1] = featureCoordinateMap.get(targetId);
				featureMatrix.setAsObject(msf, coordinates);
				dataMatrix.setAsDouble(msf.getArea(), coordinates);
			}
			else {
				//	TODO handle library mismatches
				System.out.println(msf.getName() + "(" + targetId + ") not in the library.");
			}		
			features.add(msf);
			processed++;
		}
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public Set<SimpleMsFeature> getFeatures() {
		return features;
	}

	@Override
	public Set<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

}
