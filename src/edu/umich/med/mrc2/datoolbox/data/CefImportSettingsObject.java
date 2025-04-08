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

package edu.umich.med.mrc2.datoolbox.data;

import java.util.List;
import java.util.Map;

import org.ujmp.core.Matrix;

public class CefImportSettingsObject {

	private DataFile dataFile;
	private ResultsFile resultsFile;
	private int fileIndex;
	private Matrix featureMatrix;
	private Matrix dataMatrix;
	private Map<String, Integer> featureCoordinateMap;
	private Map<String, List<Double>> retentionMap;
	private Map<String, List<Double>> mzMap;
	private Map<String, List<Double>> peakWidthMap;
	
	public CefImportSettingsObject() {
		super();
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public void setDataFile(DataFile dataFile) {
		this.dataFile = dataFile;
	}

	public ResultsFile getResultsFile() {
		return resultsFile;
	}

	public void setResultsFile(ResultsFile resultsFile) {
		this.resultsFile = resultsFile;
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}

	public Matrix getFeatureMatrix() {
		return featureMatrix;
	}

	public void setFeatureMatrix(Matrix featureMatrix) {
		this.featureMatrix = featureMatrix;
	}

	public Matrix getDataMatrix() {
		return dataMatrix;
	}

	public void setDataMatrix(Matrix dataMatrix) {
		this.dataMatrix = dataMatrix;
	}

	public Map<String, Integer> getFeatureCoordinateMap() {
		return featureCoordinateMap;
	}

	public void setFeatureCoordinateMap(Map<String, Integer> featureCoordinateMap) {
		this.featureCoordinateMap = featureCoordinateMap;
	}

	public Map<String, List<Double>> getRetentionMap() {
		return retentionMap;
	}

	public void setRetentionMap(Map<String, List<Double>> retentionMap) {
		this.retentionMap = retentionMap;
	}

	public Map<String, List<Double>> getMzMap() {
		return mzMap;
	}

	public void setMzMap(Map<String, List<Double>> mzMap) {
		this.mzMap = mzMap;
	}

	public Map<String, List<Double>> getPeakWidthMap() {
		return peakWidthMap;
	}

	public void setPeakWidthMap(Map<String, List<Double>> peakWidthMap) {
		this.peakWidthMap = peakWidthMap;
	}
	
	
}
