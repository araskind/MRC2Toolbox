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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentProFinderSimpleCSVexportColumns;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.BasePCDLutils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.AgilentProfinderDetailedExportParser;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class ProFinderArchivePreprocessingTask extends DataWithLibraryImportAbstractTask {

	private Set<SampleDataResultObject> dataToImport;
	private File proFinderSimpleCsvExportFile;
	private Collection<Adduct>selectedAdducts;
	private boolean isLibraryParsed;
	private Map<AgilentProFinderSimpleCSVexportColumns, Integer>dataFieldMap;
	private CompoundLibrary basePCDLlibrary;
	private Map<String, Double> nameRetentionMap;
	private Map<String, Double> unmatchedProFinderCompounds;
	private File proFinderDetailedCsvExportFile;

	public ProFinderArchivePreprocessingTask(
			DataPipeline dataPipeline,
			File pfaTempDir,
			Set<SampleDataResultObject> dataToImport, 			
			File proFinderSimpleCsvExportFile, 
			File proFinderDetailedCsvExportFile, 
			Collection<Adduct> selectedAdducts) {
		super();
		this.dataPipeline = dataPipeline;
		this.dataToImport = dataToImport;
		this.dataFiles = dataToImport.stream().
				map(s -> s.getDataFile()).
				toArray(size -> new DataFile[size]);
		this.tmpCefDirectory = pfaTempDir;
		this.proFinderSimpleCsvExportFile = proFinderSimpleCsvExportFile;
		this.selectedAdducts = selectedAdducts;
		removeAbnormalIsoPatterns = false;
		this.proFinderDetailedCsvExportFile = proFinderDetailedCsvExportFile;
		isLibraryParsed = false;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Preparing ProFinder data set for import ...";
		total = 100;
		processed = 2;
		try {
			isLibraryParsed = parseLibraryDataFromSimpleCsvExportFile();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(isLibraryParsed) {
			//	TODO
			if(proFinderDetailedCsvExportFile != null)
				parseDetailedProFinderExportFile();
			
			if(generateCompoundLibrary()) {
				initDataMatrixes();
				initDataLoad();
			}
			else {
				errorMessage = listUnmatchedCompoundsAsError(unmatchedProFinderCompounds);
				setStatus(TaskStatus.ERROR);
			}
		}
	}

	private void parseDetailedProFinderExportFile() {
		
		taskDescription = "Extracting RT data from detailed export ...";
		total = 100;
		processed = 40;
		AgilentProfinderDetailedExportParser dataParser = 
				new AgilentProfinderDetailedExportParser(proFinderDetailedCsvExportFile);
		rtMatrix = dataParser.extractRTdata();
	}

	private boolean generateCompoundLibrary() {

		taskDescription = "Parsing library data ...";
		total = nameRetentionMap.size();
		processed = 0;
		basePCDLlibrary = BasePCDLutils.getPCDLbaseLibrary();
		library = new CompoundLibrary("ProFinder library for " + dataPipeline.getName()); 
		unmatchedProFinderCompounds = new HashMap<String, Double>();
		libFeatureNameIdMap = new HashMap<String, String>();
		for(Entry<String,Double>mapEntry : nameRetentionMap.entrySet()) {
			
			LibraryMsFeature baseEntry = basePCDLlibrary.getFeatureByName(mapEntry.getKey());
			if(baseEntry == null)
				unmatchedProFinderCompounds.put(mapEntry.getKey(), mapEntry.getValue());
			else {
				LibraryMsFeature newLibFeature = new LibraryMsFeature(baseEntry);
				newLibFeature.setRetentionTime(mapEntry.getValue());
				MSRTLibraryUtils.generateMassSpectrumFromAdducts(newLibFeature, selectedAdducts);

				newLibFeature.getPrimaryIdentity().setConfidenceLevel(
						CompoundIdentificationConfidence.ACCURATE_MASS_RT);
				library.addFeature(newLibFeature);
				libFeatureNameIdMap.put(newLibFeature.getName(), newLibFeature.getId());
			}
			processed++;
		}
		return unmatchedProFinderCompounds.isEmpty();
	}	

	private String listUnmatchedCompoundsAsError(Map<String, Double> unmatchedProFinderCompounds2) {

		String details = "The following entries were not found in PCDL base library:\n";
		List<String>missingEntries = unmatchedProFinderCompounds2.entrySet().stream().
				map(f -> f.getKey() + "\t" + Double.toString(f.getValue())).sorted().
				collect(Collectors.toList());
		details += StringUtils.join(missingEntries, "\n");
		return details;
	}

	private boolean parseLibraryDataFromSimpleCsvExportFile() {
		
		taskDescription = "Parsing library data ...";
		total = 100;
		processed = 2;
		
		nameRetentionMap = new HashMap<String,Double>();
		String[][] compoundDataArray = 
				DelimitedTextParser.parseTextFile(proFinderSimpleCsvExportFile, ',');
		
		boolean dataValid = createAndValidateFieldMap(compoundDataArray[0]);
		if(!dataValid)
			return false;
		
		int nameColumnIndex = dataFieldMap.get(AgilentProFinderSimpleCSVexportColumns.COMPOUND_NAME);
		int rtColumnIndex = dataFieldMap.get(AgilentProFinderSimpleCSVexportColumns.RT);
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String rtString = compoundDataArray[i][rtColumnIndex];
			if(rtString.isEmpty() || !NumberUtils.isParsable(rtString)) {
				errorMessage = "Some values in the " 
						+ AgilentProFinderSimpleCSVexportColumns.RT.getName()
						+ " column are not valid numbers";
				return false;
			}
			double rt = NumberUtils.createDouble(rtString);
			nameRetentionMap.put(compoundDataArray[i][nameColumnIndex], rt);
		}
		return true;
	}

	private boolean createAndValidateFieldMap(String[]header) {
		
		dataFieldMap = new TreeMap<AgilentProFinderSimpleCSVexportColumns, Integer>();
		for(int i=0; i<header.length; i++) {
			
			AgilentProFinderSimpleCSVexportColumns f = 
					AgilentProFinderSimpleCSVexportColumns.getOptionByUIName(header[i]);
			if(f != null)
				dataFieldMap.put(f, i);
		}
		ArrayList<String>missingFields = new ArrayList<String>();
		if(!dataFieldMap.containsKey(AgilentProFinderSimpleCSVexportColumns.COMPOUND_NAME))
			missingFields.add(AgilentProFinderSimpleCSVexportColumns.COMPOUND_NAME.getName());

		if(!dataFieldMap.containsKey(AgilentProFinderSimpleCSVexportColumns.RT))
			missingFields.add(AgilentProFinderSimpleCSVexportColumns.RT.getName());
		
		if(missingFields.isEmpty())
			return true;
		else {
			errorMessage = 
					"The following obligatory fields are missing form the input data:\n"
					+ StringUtils.join(missingFields, ", ");
			return false;
		}		
	}	

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(CefDataImportTask.class))
				finalizeCefImportTask((CefDataImportTask)e.getSource());	
					
			if (e.getSource().getClass().equals(CefImportFinalizationTask.class))
				setStatus(TaskStatus.FINISHED);
		}
	}

	@Override
	public Task cloneTask() {

		return new ProFinderArchivePreprocessingTask(
				dataPipeline,
				tmpCefDirectory,
				dataToImport, 			
				proFinderSimpleCsvExportFile, 
				proFinderDetailedCsvExportFile,
				selectedAdducts);
	}
}



