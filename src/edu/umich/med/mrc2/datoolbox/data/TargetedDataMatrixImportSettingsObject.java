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

import java.io.File;
import java.util.Map;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;

public class TargetedDataMatrixImportSettingsObject {
	
	private Set<SampleDataResultObject> dataToImport;
	private DataPipeline importPipeline;
	private File inputDataFile;
	private int linesToSkipAfterHeader;
	private String featureColumn;
	private String retentionColumn;
	private CompoundLibrary referenceLibrary;
	private Map<String,LibraryMsFeature>nameFeatureMap;
	
	public TargetedDataMatrixImportSettingsObject() {
		super();
	}

	public TargetedDataMatrixImportSettingsObject(
			Set<SampleDataResultObject> dataToImport, 
			DataPipeline importPipeline,
			File inputDataFile, 
			int linesToSkipAfterHeader, 
			String featureColumn,
			String retentionColumn,
			CompoundLibrary referenceLibrary,
			Map<String, 
			LibraryMsFeature> nameFeatureMap) {
		super();
		this.dataToImport = dataToImport;
		this.importPipeline = importPipeline;
		this.inputDataFile = inputDataFile;
		this.linesToSkipAfterHeader = linesToSkipAfterHeader;
		this.featureColumn = featureColumn;
		this.retentionColumn = retentionColumn;
		this.referenceLibrary = referenceLibrary;
		this.nameFeatureMap = nameFeatureMap;
	}

	public Set<SampleDataResultObject> getDataToImport() {
		return dataToImport;
	}

	public void setDataToImport(Set<SampleDataResultObject> dataToImport) {
		this.dataToImport = dataToImport;
	}

	public DataPipeline getImportPipeline() {
		return importPipeline;
	}

	public void setImportPipeline(DataPipeline importPipeline) {
		this.importPipeline = importPipeline;
	}

	public File getInputDataFile() {
		return inputDataFile;
	}

	public void setInputDataFile(File libraryFile) {
		this.inputDataFile = libraryFile;
	}

	public int getLinesToSkipAfterHeader() {
		return linesToSkipAfterHeader;
	}

	public void setLinesToSkipAfterHeader(int linesToSkipAfterHeader) {
		this.linesToSkipAfterHeader = linesToSkipAfterHeader;
	}

	public String getFeatureColumn() {
		return featureColumn;
	}

	public void setFeatureColumn(String featureColumn) {
		this.featureColumn = featureColumn;
	}

	public CompoundLibrary getReferenceLibrary() {
		return referenceLibrary;
	}

	public void setReferenceLibrary(CompoundLibrary referenceLibrary) {
		this.referenceLibrary = referenceLibrary;
	}

	public Map<String, LibraryMsFeature> getNameFeatureMap() {
		return nameFeatureMap;
	}

	public void setNameFeatureMap(Map<String, LibraryMsFeature> nameFeatureMap) {
		this.nameFeatureMap = nameFeatureMap;
	}

	public String getRetentionColumn() {
		return retentionColumn;
	}

	public void setRetentionColumn(String retentionColumn) {
		this.retentionColumn = retentionColumn;
	}
}
