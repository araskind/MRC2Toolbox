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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;

public class CefImportFinalizationObjest {

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
	
	public CefImportFinalizationObjest() {
		super();
	}

	public DataFile[] getDataFiles() {
		return dataFiles;
	}

	public void setDataFiles(DataFile[] dataFiles) {
		this.dataFiles = dataFiles;
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public void setDataPipeline(DataPipeline dataPipeline) {
		this.dataPipeline = dataPipeline;
	}

	public CompoundLibrary getLibrary() {
		return library;
	}

	public void setLibrary(CompoundLibrary library) {
		this.library = library;
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

	public boolean isRemoveAbnormalIsoPatterns() {
		return removeAbnormalIsoPatterns;
	}

	public void setRemoveAbnormalIsoPatterns(boolean removeAbnormalIsoPatterns) {
		this.removeAbnormalIsoPatterns = removeAbnormalIsoPatterns;
	}

	public File getTmpCefDirectory() {
		return tmpCefDirectory;
	}

	public void setTmpCefDirectory(File tmpCefDirectory) {
		this.tmpCefDirectory = tmpCefDirectory;
	}

	public Matrix getRtMatrix() {
		return rtMatrix;
	}

	public void setRtMatrix(Matrix rtMatrix) {
		this.rtMatrix = rtMatrix;
	}
}
