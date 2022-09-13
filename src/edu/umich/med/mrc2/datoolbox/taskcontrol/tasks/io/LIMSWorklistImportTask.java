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
import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LIMSWorklistImportTask extends WorklistTask {
	
	private LIMSExperiment limsExperiment;
	private LIMSSamplePreparation samplePrep;
	private Collection<String>missingMethods;

	//	TODO - handle different instrument vendors
	public LIMSWorklistImportTask(
			File inFile,
			WorklistImportType importType,
			LIMSExperiment limsExperiment,
			LIMSSamplePreparation samplePrep) {
		super(inFile, importType);
		this.limsExperiment = limsExperiment;
		this.samplePrep = samplePrep;
		taskDescription = "Importing worklist data from " + sourceFileOrDirectory.getName();
		worklist = null;
		processed = 0;
		total = 100;
		missingMethods = new TreeSet<String>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		if(importType.equals(WorklistImportType.VENDOR_WORKLIST)) {

			try {
				readWorklistFile();
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		if(importType.equals(WorklistImportType.RAW_DATA_DIRECTORY_SCAN)) {

			Collection<File> dataDiles = 
					getAgiletDFileList(sourceFileOrDirectory);
			if (!dataDiles.isEmpty()) {

				try {
					scanDirectoryForSampleInfo(dataDiles);
				} catch (Exception e) {

					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		}
		if(importType.equals(WorklistImportType.PLAIN_TEXT_FILE)) {
			//	TODO
		}
		if(importType.equals(WorklistImportType.EXCEL_FILE)) {
			//	TODO
		}
		setSampleAndMethodInformation();
		setStatus(TaskStatus.FINISHED);
	}
	
	private void setSampleAndMethodInformation() {
		
		Collection<DataAcquisitionMethod> methods = 
				IDTDataCash.getAcquisitionMethods();
		TreeSet<ExperimentalSample> samples = 
				limsExperiment.getExperimentDesign().getSamples();
		
		Collection<LIMSWorklistItem>lwItems = new ArrayList<LIMSWorklistItem>();
		for(WorklistItem item : worklist.getWorklistItems()) {
			
			//	Acquisition method
			File acqMethodFile = new File(item.getProperty(AgilentSampleInfoFields.METHOD.getName()));
			String nameNoExtension = FilenameUtils.getBaseName(acqMethodFile.getName());
			DataAcquisitionMethod limsMethod = methods.stream().
					filter(m -> (m.getName().equals(nameNoExtension)||
					m.getName().equals(acqMethodFile.getName()))).findFirst().orElse(null);			
			if(limsMethod == null)
				missingMethods.add(acqMethodFile.getName());

			//	Find matching sample
			String sampleId = item.getProperty(AgilentSampleInfoFields.SAMPLE_ID.getName());
			String sampleName = item.getProperty(AgilentSampleInfoFields.SAMPLE_NAME.getName());
			ExperimentalSample sample = samples.stream().
				filter(s -> (s.getId().equals(sampleId) || s.getName().equals(sampleName))).
				findFirst().orElse(null);
						
			LIMSWorklistItem lwItem = new LIMSWorklistItem(item);
			lwItem.setSample(sample);
			if(sample != null)
				lwItem.setPrepItemId(samplePrep.getPrepItemsForSample(sample.getId()).iterator().next());

			lwItem.setAcquisitionMethod(limsMethod);
			lwItem.setSamplePrep(samplePrep);
			double injectionVolume = Double.parseDouble(item.getProperty(AgilentSampleInfoFields.INJ_VOL.getName()));
			lwItem.setInjectionVolume(injectionVolume);
			lwItem.getDataFile().setInjectionTime(lwItem.getTimeStamp());
			lwItems.add(lwItem);
		}
		worklist.getWorklistItems().clear();
		lwItems.stream().forEach(i -> worklist.addItem(i));
	}

	@Override
	public Task cloneTask() {
		return new LIMSWorklistImportTask(
			sourceFileOrDirectory, importType, limsExperiment, samplePrep);
	}

	/**
	 * @return the missingMethods
	 */
	public Collection<String> getMissingMethods() {
		return missingMethods;
	}

}
