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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class CefLibraryImportTask extends CEFProcessingTask {

	private DataAnalysisProject currentExperiment;
	private DataPipeline dataPipeline;
	private String plusRtMask;
	private ArrayList<MsFeature> unassigned;
	private CompoundLibrary newLibrary;
	private boolean matchToFeatures;
	private boolean generateLibraryFeatures;

	public CefLibraryImportTask(
			DataPipeline dataPipeline,
			File inputFile,
			boolean matchToFeatures,
			boolean generateLibraryFeatures) {

		inputCefFile = inputFile;
		this.dataPipeline = dataPipeline;
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();


		this.matchToFeatures = matchToFeatures;
		this.generateLibraryFeatures = generateLibraryFeatures;

		plusRtMask = "(\\d+.\\d+)@(\\d+.\\d+) [\\+\\-] (\\d+.\\d+)\\s*:*\\d*";

		unmatchedAdducts = new TreeSet<>();
		unassigned = new ArrayList<>();
		unassigned = new ArrayList<>();
	}

	@Override
	public void run() {
		
		taskDescription = "Importing library from  " + inputCefFile.getName();
		total = 100;
		processed = 0;
		
		if(inputCefFile == null || !inputCefFile.exists()) {
			errorMessage = "Library file not found";
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.PROCESSING);
		newLibrary = 
				new CompoundLibrary(FilenameUtils.getBaseName(inputCefFile.getPath()));
		newLibrary.setDataPipeline(dataPipeline);
				try {
			parseInputCefFile(inputCefFile);
		} catch (Exception e1) {
			errorMessage = "Failed to parse library file";
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		inputFeatureList.stream().
			forEach(f -> newLibrary.addFeature(new LibraryMsFeature(f)));
		
		copyLibraryFileToExperiment();
		
		if(matchToFeatures) {

			matchToFeatures();
			collectUnassignedFeatures();
		}
		setStatus(TaskStatus.FINISHED);
	}

//	private void clearCurrentLibrary() {
//
//		taskDescription = "Clearing old library data...";
//		total = 100;
//		processed = 10;
//		
//		if(dataPipeline == null)
//			return;
//		
//		if(currentExperiment.getMsFeaturesForDataPipeline(dataPipeline) == null ||
//				currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).isEmpty())
//			return;
//
//		for (MsFeature cf : currentExperiment.getMsFeaturesForDataPipeline(dataPipeline)) {
//
//			cf.setSpectrum(null);
//			cf.setNeutralMass(0.0d);
//			cf.clearIdentification();
//			processed++;
//		}
//	}
	
	private void matchToFeatures() {

		taskDescription = "Matching library entries to features ...";
		total = currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).size();
		processed = 0;

		for (MsFeature cf : currentExperiment.getMsFeaturesForDataPipeline(dataPipeline)) {

			for(MsFeature lf : newLibrary.getFeatures()) {

				if (cf.getName().equals(lf.getName())) {

					cf.setSpectrum(lf.getSpectrum());
					cf.getSpectrum().finalizeCefImportSpectrum();
					cf.setNeutralMass(lf.getNeutralMass());

					for (MsFeatureIdentity cid : lf.getIdentifications())
						cf.addIdentity(cid);

					cf.setPrimaryIdentity(lf.getPrimaryIdentity());

					//	Set retention from library if not present
					if(cf.getRetentionTime() == 0.0d)
						cf.setRetentionTime(lf.getRetentionTime());
				}
			}
			processed++;
		}
	}

	private void copyLibraryFileToExperiment() {
		
		if(inputCefFile == null || !inputCefFile.exists())
			return;

		try {
			FileUtils.copyFileToDirectory(
					inputCefFile, currentExperiment.getLibraryDirectory());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private void collectUnassignedFeatures() {

		taskDescription = "Checking for unassigned features ...";
		total = currentExperiment.getMsFeaturesForDataPipeline(dataPipeline).size();
		processed = 0;
		for (MsFeature cf : currentExperiment.getMsFeaturesForDataPipeline(dataPipeline)) {

			if (cf.getSpectrum() == null)
				unassigned.add(cf);

			processed++;
		}
	}

	@Override
	public Task cloneTask() {

		return new CefLibraryImportTask(
				dataPipeline, inputCefFile, matchToFeatures, generateLibraryFeatures);
	}

	public Collection<MsFeature> getUnassignedFeatures() {
		return unassigned;
	}

	public CompoundLibrary getParsedLibrary() {
		return newLibrary;
	}
}
