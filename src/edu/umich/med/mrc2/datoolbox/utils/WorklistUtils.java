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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.WorklistItemComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACRawDataManifestFields;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class WorklistUtils {

	public static String createManifest(
			DataAnalysisProject currentExperiment,
			DataPipeline activeDataPipeline) {

		Worklist worklist = currentExperiment.getWorklistForDataAcquisitionMethod(
				activeDataPipeline.getAcquisitionMethod());
		ExperimentDesign design = currentExperiment.getExperimentDesign();
		TreeSet<ExperimentalSample> activeSamples = 
				design.getActiveSamplesForDesignSubset(design.getActiveDesignSubset());
		DataAcquisitionMethod acquisitionMethod  = activeDataPipeline.getAcquisitionMethod();
		Set<DataFile> activeFiles = activeSamples.stream().
				flatMap(s -> s.getDataFilesForMethod(acquisitionMethod).stream()).
				filter(f -> f.isEnabled()).collect(Collectors.toSet());
		
		List<? extends WorklistItem> items = worklist.getTimeSortedWorklistItems().stream().
				filter(i -> activeFiles.contains(i.getDataFile())).
				sorted(new WorklistItemComparator(SortProperty.injectionTime)).
				collect(Collectors.toList());

		//	Create header
		List<String>columnNames = createManifestColumns(worklist);
		if(columnNames == null)
			return null;

		StringBuffer manifestData = new StringBuffer();
		for(int i=0; i<columnNames.size(); i++) {

			manifestData.append(columnNames.get(i));
			if(i<columnNames.size()-1)
				manifestData.append(MRC2ToolBoxConfiguration.getTabDelimiter());
		}
		manifestData.append("\n");
		
		Map<DataFile,String>fileMotrPacSampleTypeMap = 
				createDataFileMotrPacSampleTypeMap(worklist, design);
		Map<DataFile,String>fileMotrPacSampleIdMap = 
				createDataFileMotrPacSampleIdMap(worklist, design);
		
		//Get MoTrPAC MS mode
		String msMode = "";	
		if(activeDataPipeline.getMotrpacAssay() != null)
			msMode = activeDataPipeline.getMotrpacAssay().getBucketCode();
			
//		try {
//			msMode = AssayDatabaseUtils.getMotrPacMsModeForDataPipeline(activeDataPipeline);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
		int injectionOrder = 1;

		//	Append data
		for(WorklistItem item : items) {

			DataFile df = item.getDataFile();
			ExperimentalSample sample = design.getSampleByDataFile(df);
			if(sample == null)
				continue;

			for(int i=0; i<columnNames.size(); i++) {

				String colName = columnNames.get(i);
				
				//	Obligatory MoTrPAC columns
				if(colName.equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ID.getName()))					
					manifestData.append(fileMotrPacSampleIdMap.get(df));
				
				else if(colName.equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName()))
					manifestData.append(fileMotrPacSampleTypeMap.get(df));
					
				else if(colName.equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName()))			
					manifestData.append(Integer.toString(injectionOrder));
					
				else if(colName.equals(MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName()))				
					manifestData.append(df.getName());
				
				else if(colName.equals(MoTrPACRawDataManifestFields.MOTRPAC_MS_MODE.getName()))				
					manifestData.append(msMode);

				else if(colName.equals(DataExportFields.MRC2_SAMPLE_ID.getName()))
					manifestData.append(sample.getId());

				else if(colName.equals(DataExportFields.INJECTION_TIME.getName()))
					manifestData.append(MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(item.getTimeStamp()));
				else {
					String value = item.getProperty(colName);
					if(value == null)
						value = "";

					manifestData.append(value);
				}
				if(i<columnNames.size()-1)
					manifestData.append(MRC2ToolBoxConfiguration.getTabDelimiter());
			}
			manifestData.append("\n");
			injectionOrder++;
		}
		return manifestData.toString();
	}
	
	
	public static List<String> createManifestColumns(Worklist worklist) {

		Set<String> allColumnNames = new TreeSet<String>();
		worklist.getTimeSortedWorklistItems().stream().
			forEach(i -> allColumnNames.addAll(i.getProperties().keySet()));

		HashMap<String, Integer> valueCount = new HashMap<String, Integer>();

		for (String field : allColumnNames) {

			valueCount.put(field, 0);
			for (WorklistItem item : worklist.getTimeSortedWorklistItems()) {

				if (item.getProperty(field) == null)
					continue;

				if (!item.getProperty(field).isEmpty()) {
					Integer current = valueCount.get(field) + 1;
					valueCount.replace(field, current);
				}
			}
		}
		ArrayList<String>columnNames = new ArrayList<String>();
		
		//	Add MoTrPAC obligatory column names
		for(MoTrPACRawDataManifestFields field : MoTrPACRawDataManifestFields.values())
			columnNames.add(field.getName());
			
		columnNames.add(DataExportFields.MRC2_SAMPLE_ID.getName());
		
		//	This will go to MoTrPAC sample id
		//	columnNames.add(DataExportFields.CLIENT_SAMPLE_ID.getName());
		
		//	Data file goes to MotrPAC column
//		if(valueCount.containsKey(AgilentSampleInfoFields.DATA_FILE.getName())) {
//			columnNames.add(AgilentSampleInfoFields.DATA_FILE.getName());
//		}
//		else {
//			MessageDialogue.showErrorMsg("Data file name field missing.", this.getContentPane());
//			return null;
//		}
		if(!valueCount.containsKey(AgilentSampleInfoFields.DATA_FILE.getName())) {			
			MessageDialog.showErrorMsg("Data file name field missing.", MRC2ToolBoxCore.getMainWindow());
			return null;
		}
		if(valueCount.containsKey(AgilentSampleInfoFields.ACQUISITION_TIME.getName())
				|| valueCount.containsKey(AgilentSampleInfoFields.ACQTIME.getName())) {
			columnNames.add(DataExportFields.INJECTION_TIME.getName());
		}
		else {
			MessageDialog.showErrorMsg("Injection time field missing.", MRC2ToolBoxCore.getMainWindow());
			return null;
		}
		for (Entry<String, Integer> entry : valueCount.entrySet()) {

			if (entry.getValue() > 0
					&& !entry.getKey().equals(AgilentSampleInfoFields.DATA_FILE.getName())
					&& !entry.getKey().equals(AgilentSampleInfoFields.ACQTIME.getName())
					&& !entry.getKey().equals(AgilentSampleInfoFields.ACQUISITION_TIME.getName()))
				columnNames.add(entry.getKey());
		}
		return columnNames;
	}
	

	public static Map<DataFile, String> createDataFileMotrPacSampleIdMap(Worklist worklist, ExperimentDesign design) {
		
		Map<DataFile,String>fileMotrPacSampleIdMap = new TreeMap<DataFile,String>();
		List<? extends WorklistItem> items = worklist.getTimeSortedWorklistItems().stream().
				sorted(new WorklistItemComparator(SortProperty.injectionTime)).
				collect(Collectors.toList());
		
		Map<ExperimentalSample,List<DataFile>>sampleFileMap = new TreeMap<ExperimentalSample,List<DataFile>>();
		
		for(WorklistItem item : items) {
			
			DataFile df = item.getDataFile();		
			ExperimentalSample sample = design.getSampleByDataFile(df);			
			if(!sampleFileMap.containsKey(sample))
				sampleFileMap.put(sample, new ArrayList<>());
			
			sampleFileMap.get(sample).add(df);
		}
		for (Entry<ExperimentalSample, List<DataFile>> entry : sampleFileMap.entrySet()) {
			
			ExperimentalSample sample = entry.getKey();
			if(entry.getValue().size() == 1) {
				
				if(sample.getSampleType().equals(ReferenceSamplesManager.sampleLevel))
					fileMotrPacSampleIdMap.put(entry.getValue().get(0), sample.getName());
				else
					fileMotrPacSampleIdMap.put(entry.getValue().get(0), sample.getId());
			}
			else {
				int counter = 1;
				for(DataFile df : entry.getValue()) {
					
					String increment = StringUtils.leftPad(Integer.toString(counter), 2, '0');
					
					if(sample.getSampleType().equals(ReferenceSamplesManager.sampleLevel))
						fileMotrPacSampleIdMap.put(df, sample.getName() + "-" + increment);
					else
						fileMotrPacSampleIdMap.put(df, sample.getId() + "-" + increment);
					counter++;
				}
			}
		}
		return fileMotrPacSampleIdMap;
	}

	public static Map<DataFile, String> createDataFileMotrPacSampleTypeMap(Worklist worklist, ExperimentDesign design) {
		
		Map<DataFile,String>fileMotrPacSampleTypeMap = new TreeMap<DataFile,String>();
		for(WorklistItem item : worklist.getTimeSortedWorklistItems()) {
			
			DataFile df = item.getDataFile();		
			ExperimentalSample sample = design.getSampleByDataFile(df);		
			if(sample.getSampleType().equals(ReferenceSamplesManager.sampleLevel))
				fileMotrPacSampleTypeMap.put(df,"Sample");
			else {				
				if(sample.getMoTrPACQCSampleType() == null) {
					
					ExperimentalSample refSample = ReferenceSamplesManager.getReferenceSampleById(sample.getId());
					if(refSample != null)
						sample.setMoTrPACQCSampleType(refSample.getMoTrPACQCSampleType());
				}
				if(sample.getMoTrPACQCSampleType() != null)
					fileMotrPacSampleTypeMap.put(df, sample.getMoTrPACQCSampleType().getName());
				else
					fileMotrPacSampleTypeMap.put(df, "Sample");
			}			
		}
		return fileMotrPacSampleTypeMap;
	}
}
