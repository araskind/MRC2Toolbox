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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.sql.Connection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibMatchedSimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTMsDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class ReferenceMS1ImportTask extends CEFProcessingTask {

	private DataFile dataFile;
	private DataPipeline dataPipeline;
	private LIMSExperiment experiment;
	private ExperimentalSample selectedSample;
	private DataAcquisitionMethod acquisitionMethod;
	private DataExtractionMethod dataExtractionMethod;

	public ReferenceMS1ImportTask(
			DataFile dataFile,
			DataPipeline dataPipeline,
			LIMSExperiment experiment,
			ExperimentalSample selectedSample) {

		super();
		this.dataFile = dataFile; 
		this.dataPipeline = dataPipeline;
		this.experiment = experiment;
		this.selectedSample = selectedSample;
		this.acquisitionMethod = dataPipeline.getAcquisitionMethod();
		this.dataExtractionMethod = dataPipeline.getDataExtractionMethod();
	}
	
	public ReferenceMS1ImportTask(
			DataFile dataFile,
			DataAcquisitionMethod acquisitionMethod,
			DataExtractionMethod dataExtractionMethod,
			LIMSExperiment experiment,
			ExperimentalSample selectedSample) {

		super();
		this.dataFile = dataFile; 
		this.experiment = experiment;
		this.selectedSample = selectedSample;
		this.acquisitionMethod = acquisitionMethod;
		this.dataExtractionMethod = dataExtractionMethod;
	}

	@Override
	public void run() {

		if(dataExtractionMethod == null) {
			errorMessage = "Data extraction method missing";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = "Importing data from " + dataFile.getName();
		setStatus(TaskStatus.PROCESSING);
		String cefPath = dataFile.getResultForDataExtractionMethod(
				dataExtractionMethod).getFullPath();
		if(cefPath == null) {
			errorMessage = "Path to CEF file not specified";
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
		try {
			uploadParsedData();
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void uploadParsedData() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Uploading data data from " + dataFile.getName() + " to database";
		total = inputFeatureList.size();
		processed = 0;
		String sourceDataBundleId = IDTUtils.addNewReferenceMS1DataBundle(
						experiment,
						selectedSample,
						acquisitionMethod,
						dataExtractionMethod,
						conn);
		
		if(dataPipeline == null)
			dataPipeline = new DataPipeline(
					acquisitionMethod, dataExtractionMethod);
		
		for(MsFeature feature  : inputFeatureList) {
			
			LibMatchedSimpleMsFeature lmFeature = 
					new LibMatchedSimpleMsFeature(feature, dataPipeline);
			IDTMsDataUtils.uploadPoolMs1Feature(lmFeature, sourceDataBundleId, conn);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		
		if(dataPipeline == null) {
			return new ReferenceMS1ImportTask(
					dataFile,
					acquisitionMethod,
					dataExtractionMethod,
					experiment,
					selectedSample);
		}
		else {
			return new ReferenceMS1ImportTask(
					dataFile,
					dataPipeline,
					experiment,
					selectedSample);
		}
	}
}













