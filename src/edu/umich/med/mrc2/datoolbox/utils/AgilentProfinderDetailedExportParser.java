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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.enums.AgilentProFinderDetailedCSVexportColumns;


public class AgilentProfinderDetailedExportParser {

	private File proFinderDetailedCsvExportFile;
	private AgilentProFinderDetailedCSVexportColumns[]quantitativeFiedldPrefixes;
	private Map<AgilentProFinderDetailedCSVexportColumns, Integer>dataFieldMap;
	private Map<AgilentProFinderDetailedCSVexportColumns,Map<String,Integer>>quantDataMap;
	private String[]dataFiles;
	private String[]compoundNames;
	private String errorMessage;

	public AgilentProfinderDetailedExportParser(File proFinderDetailedCsvExportFile) {
		super();
		this.proFinderDetailedCsvExportFile = proFinderDetailedCsvExportFile;
		quantitativeFiedldPrefixes = 
				AgilentProFinderDetailedCSVexportColumns.getQuantitativeFiedldPrefixes();
	}
	
	public Matrix extractDataOfType(AgilentProFinderDetailedCSVexportColumns prefixField) {
		
		if(!Arrays.asList(quantitativeFiedldPrefixes).contains(prefixField))
			return null;
		
		String[][] compoundDataArray = 
				DelimitedTextParser.parseTextFile(proFinderDetailedCsvExportFile, ',');
		if(!createAndValidateDetailedFieldMap(compoundDataArray[0], prefixField))
			return null;
		
		Map<String,Integer>dataFileMap = quantDataMap.get(prefixField);
		double[][]rtArray = new double[dataFileMap.size()][compoundDataArray.length-1];
		compoundNames = new String[compoundDataArray.length-1];
		int nameColIndex = dataFieldMap.get(AgilentProFinderDetailedCSVexportColumns.NAME);
		int fileCount = 0;
		for(int i=1; i<compoundDataArray.length; i++) {
			
			compoundNames[i-1] = compoundDataArray[i][nameColIndex];
			fileCount = 0;
			for(int dfColIndex : dataFileMap.values()) {
				
				double rt = 0.0d;
				String rtValueString = compoundDataArray[i][dfColIndex];
				if(rtValueString != null && !rtValueString.isBlank())
					rt = Double.parseDouble(rtValueString);
				
				rtArray[fileCount][i-1] = rt;
				fileCount++;
			}
		}
		Matrix dataMatrix = Matrix.Factory.linkToArray(rtArray);
		dataMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray(dataFiles).transpose(Ret.NEW));
		dataMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray(compoundNames));
		return dataMatrix;
	}
	
	private boolean createAndValidateDetailedFieldMap(
			String[]header,
			AgilentProFinderDetailedCSVexportColumns prefixField) {
		
		quantDataMap = 
				new TreeMap<AgilentProFinderDetailedCSVexportColumns,Map<String,Integer>>();

		for(AgilentProFinderDetailedCSVexportColumns pf : quantitativeFiedldPrefixes)
			quantDataMap.put(pf, new TreeMap<String,Integer>());
			
		dataFieldMap = new TreeMap<AgilentProFinderDetailedCSVexportColumns, Integer>();		
		for(int i=0; i<header.length; i++) {
			
			AgilentProFinderDetailedCSVexportColumns f = 
					AgilentProFinderDetailedCSVexportColumns.getOptionByUIName(header[i]);
			if(f != null)
				dataFieldMap.put(f, i);
			
			for(AgilentProFinderDetailedCSVexportColumns pf : quantitativeFiedldPrefixes) {
				
				if(header[i].startsWith(pf.getName())) {
					String fileName = header[i].replace(pf.getName(), "").trim();
					quantDataMap.get(pf).put(fileName, i);
				}
			}
		}
		dataFiles = 
				quantDataMap.get(AgilentProFinderDetailedCSVexportColumns.RT_PREFIX).
					keySet().stream().toArray(String[]::new);
		ArrayList<String>missingFields = new ArrayList<String>();
		if(!dataFieldMap.containsKey(AgilentProFinderDetailedCSVexportColumns.NAME))
			missingFields.add(AgilentProFinderDetailedCSVexportColumns.NAME.getName());
		if(quantDataMap.get(prefixField).isEmpty())
			missingFields.add(prefixField.getName());
		
		if(missingFields.isEmpty())
			return true;
		else {
			errorMessage = 
					"The following obligatory fields are missing form the input data:\n"
					+ StringUtils.join(missingFields, ", ");
			System.out.println(errorMessage);
			return false;
		}		
	}
	
	public Matrix extractRTdata() {		
		return extractDataOfType(AgilentProFinderDetailedCSVexportColumns.RT_PREFIX);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
