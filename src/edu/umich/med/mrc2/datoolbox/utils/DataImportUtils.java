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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataImportUtils {

	public static ExperimentalSample findSampleById(String sampleId) {
		return findSampleById(sampleId, MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
	}
	
	public static ExperimentalSample findSampleById(String sampleId, DataAnalysisProject experiment) {
		
		if(experiment == null || experiment.getExperimentDesign() == null || 
				sampleId == null || sampleId.isEmpty()) 
			return null;

		return experiment.getExperimentDesign().getSampleById(sampleId);
	}
	
	public static ExperimentalSample findSampleByName(String sampleName) {
		return findSampleByName(sampleName, MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
	}
		
	public static ExperimentalSample findSampleByName(String sampleName, DataAnalysisProject experiment) {
		
		if(experiment == null || experiment.getExperimentDesign() == null || 
				sampleName == null || sampleName.isEmpty()) 
			return null;

		return experiment.getExperimentDesign().getSampleByName(sampleName);
	}
	
	public static ExperimentalSample getSampleFromFileName(String fileName) {		
		return getSampleFromFileName(fileName, MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
	}
	
	public static ExperimentalSample getSampleFromFileName(String fileName, DataAnalysisProject experiment) {
		
		if(experiment == null || experiment.getExperimentDesign() == null || 
				fileName == null || fileName.isEmpty()) 
			return null;

		Pattern sampleIdPattern = Pattern.compile(MRC2ToolBoxConfiguration.getSampleIdMask());
		Pattern sampleNamePattern = Pattern.compile(MRC2ToolBoxConfiguration.getSampleNameMask());
		
		ExperimentalSample matchedSample = null;
		String sampleId = "";
		String sampleName = "";
		
		//	Find by sample ID
		Matcher regexMatcher = sampleIdPattern.matcher(fileName);
		if (regexMatcher.find())
			sampleId = regexMatcher.group();

		if (!sampleId.isEmpty())
			matchedSample = findSampleById(sampleId);
		
		if(matchedSample != null)
			return matchedSample;

		//	Find by sample name
		regexMatcher = sampleNamePattern.matcher(fileName);
		if (regexMatcher.find())
			sampleName = regexMatcher.group();

		if (!sampleName.isEmpty())
			matchedSample = findSampleByName(sampleName);
		
		if(matchedSample != null)
			return matchedSample;
		else 		
			return experiment.getExperimentDesign().getReferenceSamples().stream().
					filter(s -> fileName.contains(s.getId())).findFirst().orElse(null);
	}
	
	public static String[] extractNamedColumn(
			String[][] inputDataArray, 
			String columnName,
			int linesToSkipAfterHeader) {
		
		int columnIndex = -1;
		for(int i=0; i<inputDataArray[0].length; i++) {
			
			if(inputDataArray[0][i].equals(columnName)) {
				
				columnIndex = i;
				break;
			}
		}
		if(columnIndex == -1)
			return new String[0];
		
		int start = 1 + linesToSkipAfterHeader;		
		String[]columnData = new String[inputDataArray.length - start];
		for(int i=start; i<inputDataArray.length; i++) 			
			columnData[i-start] = inputDataArray[i][columnIndex].trim();
		
		return columnData;
	}
}
