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

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibMatchedSimpleMsFeature;
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
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ReferenceMS1ImportTask extends CEFProcessingTask {

	private LIMSExperiment experiment;
	private ExperimentalSample selectedSample;
	private DataAcquisitionMethod acquisitionMethod;

	public ReferenceMS1ImportTask(
			DataFile dataFile,
			DataPipeline dataPipeline,
			LIMSExperiment experiment,
			ExperimentalSample selectedSample) {

		super(dataFile, dataPipeline);
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

		super(dataFile, dataExtractionMethod);
		this.experiment = experiment;
		this.selectedSample = selectedSample;
		this.acquisitionMethod = acquisitionMethod;
	}

	@Override
	public void run() {

		taskDescription = "Importing data from " + dataFile.getName();
		setStatus(TaskStatus.PROCESSING);
		try {
			dataDocument = null;
			// Read CEF file
			try {
				dataDocument = XmlUtils.readXmlFile(inputCefFile);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			//	Parse CEF data
			if(dataDocument != null) {
				try {
					parseCefData();
				}
				catch (Exception e) {
					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
				try {
					uploadParsedData();
				}
				catch (Exception e) {
					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		} catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	protected void uploadParsedData() throws Exception {

		conn = ConnectionManager.getConnection();
		taskDescription = "Uploading data data from " + dataFile.getName() + " to database";
		total = features.size();
		processed = 0;
		String sourceDataBundleId = IDTUtils.addNewReferenceMS1DataBundle(
						experiment,
						selectedSample,
						acquisitionMethod,
						dataExtractionMethod,
						conn);
		for(LibMatchedSimpleMsFeature feature  : features) {
			IDTMsDataUtils.uploadPoolMs1Feature(feature, sourceDataBundleId, conn);
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













