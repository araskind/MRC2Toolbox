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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.codehaus.plexus.util.CollectionUtils;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class QCANVASUtils {
	
	public static final String regularNormFileName = "normalized_by_SERRF_with_NAs.csv";
	public static final String normFileWithBlanksName = "normalized_by_SERRF_with_blanks_and_NAs.csv";
	
	private static File logDir;
	private static Path globalLogPath;
	private static File qcanvasDataDir;
	private static String tissueMethodPrefix;	

	public static void main(String[] args) {
		
		batchCompareFiles();
	}
	
//	private static void batchCompareFiles() {
//		
//		logDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\QC-20241010");		
//		File inputMap = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\QC-20241010\\InputMap.txt");
//		String[][] inputMapData = DelimitedTextParser.parseTextFile(
//				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
//		
//		globalLogPath = Paths.get(logDir.getAbsolutePath(), "EX01263-QCANVAS-compare-global-log.txt");
//
//		for(int i=1; i<inputMapData.length; i++) {
//			
//			tissueMethodPrefix = inputMapData[i][0];
//			qcanvasDataDir = new File(inputMapData[i][1]);
//			
//			System.out.println("\n*********************************\n");
//			System.out.println("Processing data for " + tissueMethodPrefix);
//			System.out.println("QCANVAS folder  " + qcanvasDataDir.getAbsolutePath());
//			
//			compareRegNormFileToFileWithBlanks(
//					qcanvasDataDir,
//					logDir,
//					tissueMethodPrefix);
//		}
//	}
	
	private static void batchCompareFiles() {
		
		logDir = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\QC-2024-10-14\\QCANVAS-compare-logs");
		
		File inputMap = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\QC-2024-10-14\\QCANVAS-compare-logs\\InputMap.txt");
		String[][] inputMapData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		globalLogPath = Paths.get(logDir.getAbsolutePath(), "EX01190-QCANVAS-compare-global-log.txt");

		for(int i=1; i<inputMapData.length; i++) {
			
			tissueMethodPrefix = inputMapData[i][0];
			qcanvasDataDir = new File(inputMapData[i][1]);
			
			System.out.println("\n*********************************\n");
			System.out.println("Processing data for " + tissueMethodPrefix);
			System.out.println("QCANVAS folder  " + qcanvasDataDir.getAbsolutePath());
			
			compareRegNormFileToFileWithBlanks(
					qcanvasDataDir,
					logDir,
					tissueMethodPrefix);
		}
	}
	
	private static void appendToGlobalLog(Collection<String> toAppend) {
		
		if(globalLogPath == null)
			return;
		
		try {
		    Files.write(globalLogPath, 
		    		toAppend,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.APPEND);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void compareRegNormFileToFileWithBlanks(
			File qcanvasDataDir,
			File logDir,
			String tissueMethodPrefix) {
		
		if(qcanvasDataDir == null)
			return;
		
		ArrayList<String>log = new ArrayList<String>();
		
		if(!qcanvasDataDir.exists()) {
			log.add("\n***************************\n");
			log.add(qcanvasDataDir.getAbsolutePath() + " not found");
			appendToGlobalLog(log);
			return;
		}		
		File regularNormFile = 
				Paths.get(qcanvasDataDir.getAbsolutePath(), "Data", regularNormFileName).toFile();
		File normFileWithBlanks = 
				Paths.get(qcanvasDataDir.getAbsolutePath(), "Data", normFileWithBlanksName).toFile();
		
		if(!regularNormFile.exists() || !normFileWithBlanks.exists()) {
			
			log.add("\n***************************\n");
			
			if(!regularNormFile.exists())
				log.add(regularNormFile.getAbsolutePath() + " not found");
			
			if(!normFileWithBlanks.exists())
				log.add(normFileWithBlanks.getAbsolutePath() + " not found");
			
			appendToGlobalLog(log);
			return;
		}
		String[][] normData = DelimitedTextParser.parseTextFile(regularNormFile, ',');
		String[][] normDataWithBlanks = DelimitedTextParser.parseTextFile(normFileWithBlanks, ',');
		if(normData.length != normDataWithBlanks.length)
			log.add("Different number of lines in the regular and \"with blanks\" data files.\n");
		
		//	Map and check columns
		Map<Integer,Integer>columnMap = compareAndMapColumns(normData, normDataWithBlanks, log);

		//	Check rows
		Map<Integer,Integer>rowMap = compareAndMapRows(normData, normDataWithBlanks, log);
		
		//	Compare numerical data
		compareNumericalData(normData, normDataWithBlanks, columnMap, rowMap, log);
		
		Path logPath = Paths.get(logDir.getAbsolutePath(), tissueMethodPrefix +
				qcanvasDataDir.getParentFile().getName() + "_norm2withBlanksCompareLog.txt");
		try {
		    Files.write(logPath, 
		            log,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void compareNumericalData(			
			String[][] normData,
			String[][] normDataWithBlanks,
			Map<Integer,Integer>columnMap,
			Map<Integer,Integer>rowMap,
			ArrayList<String>log) {
		
		ArrayList<String>forGlobalLog = new ArrayList<String>();
		String[]normHeader = normData[0];	
		boolean columnMatches;
		for(int normColIndex : columnMap.keySet()) {
			
			int wbColIndex = columnMap.get(normColIndex);
			columnMatches = true;
			for(int normRowIndex : rowMap.keySet()) {
				
				if(!normData[normRowIndex][normColIndex].equals(normDataWithBlanks[rowMap.get(normRowIndex)][wbColIndex])) {
//					System.out.println(normData[normRowIndex][0] + 
//							"\t" + normData[normRowIndex][normColIndex] +
//							"\t" + normDataWithBlanks[rowMap.get(normRowIndex)][wbColIndex]);
					columnMatches = false;
					break;
				}
			}
			if(!columnMatches) {
				
				String colMismatch = "Data mismatch between column \"" + normHeader[normColIndex] + 
						"\" the regular data file and \"with blanks\" data file";
				log.add(colMismatch);
				forGlobalLog.add(colMismatch);
				findMatchingColumn(normData,
									normDataWithBlanks,
									columnMap,
									rowMap,
									normColIndex,
									log,
									forGlobalLog);
			}
		}	
		if(!forGlobalLog.isEmpty()) {
			
			ArrayList<String>forGlobalLogBlock = new ArrayList<String>();
			forGlobalLogBlock.add("\n***************************\n");

			forGlobalLogBlock.add(tissueMethodPrefix);
			forGlobalLogBlock.add(qcanvasDataDir.getAbsolutePath() + "\n");
			forGlobalLogBlock.addAll(forGlobalLog);
			appendToGlobalLog(forGlobalLogBlock);
		}
	}
	
	private static void findMatchingColumn(			
			String[][] normData,
			String[][] normDataWithBlanks,
			Map<Integer,Integer>columnMap,
			Map<Integer,Integer>rowMap,
			int normMismatchIndex,
			ArrayList<String>log,
			ArrayList<String>forGlobalLog) {
		
		String[]normHeader = normData[0];
		String[]normHeaderWithBlanks = normDataWithBlanks[0];
		int excludedIndex = columnMap.get(normMismatchIndex);
		
		boolean columnMatches;
		for(int wbColIndex=1; wbColIndex < normHeaderWithBlanks.length; wbColIndex++) {
			
			if(wbColIndex == excludedIndex)
				continue;
			
			columnMatches = true;
			for(int normRowIndex : rowMap.keySet()) {
				
				if(!normData[normRowIndex][normMismatchIndex].equals(normDataWithBlanks[rowMap.get(normRowIndex)][wbColIndex])){
					columnMatches = false;
					break;
				}
			}	
			if(columnMatches) {
				
				String colMatch = "Column \"" + normHeader[normMismatchIndex ] + 
						"\"  in the regular data file matches column \"" + 
						normHeaderWithBlanks[wbColIndex] + 
						"\" in the \"with blanks\" data file\n";
				log.add(colMatch);
				forGlobalLog.add(colMatch);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Map<Integer,Integer> compareAndMapRows(
			String[][] normData,
			String[][] normDataWithBlanks,
			ArrayList<String>log) {
			
		Map<Integer,String>normRowMap = new TreeMap<Integer,String>();
		for(int i=1; i<normData.length; i++)
			normRowMap.put(i,  normData[i][0]);
		
		Map<Integer,String>withBlanksRowMap = new TreeMap<Integer,String>();
		for(int i=1; i<normDataWithBlanks.length; i++)
			withBlanksRowMap.put(i,  normDataWithBlanks[i][0]);
		
		if(!normRowMap.values().containsAll(withBlanksRowMap.values())) {
			
			log.add("The following feature(s) present in the \"with blanks\" data file but not in the regular data file:\n");			
			Collection<String>diff = CollectionUtils.subtract(withBlanksRowMap.values(), normRowMap.values());
			log.addAll(diff);
		}
		if(!withBlanksRowMap.values().containsAll(normRowMap.values())) {
			
			log.add("The following feature(s) present in the regular data file but not in the \"with blanks\" data file:\n");
			Collection<String>diff = CollectionUtils.subtract(normRowMap.values(), withBlanksRowMap.values());
			log.addAll(diff);
		}		
		Map<Integer,Integer>rowMatchMap = new TreeMap<Integer,Integer>();
		for(Entry<Integer,String>nr : normRowMap.entrySet()) {
						
			for(Entry<Integer,String>wb : withBlanksRowMap.entrySet()) {
				
				if(nr.getValue().equals(wb.getValue()))
					rowMatchMap.put(nr.getKey(), wb.getKey());
			}
		}
		return rowMatchMap;
	}	
	
	private static Map<Integer,Integer> compareAndMapColumns(
			String[][] normData,
			String[][] normDataWithBlanks,
			ArrayList<String>log) {
		
		String[]normHeader = normData[0];
		String[]normHeaderWithBlanks = normDataWithBlanks[0];
		Map<Integer,Integer>columnMap = new TreeMap<Integer,Integer>();
		for(int i=0; i<normHeader.length; i++) {
			
			for(int j=0; j<normHeaderWithBlanks.length; j++) {
				
				if(normHeader[i].equals(normHeaderWithBlanks[j]))
					columnMap.put(i, j);
			}
		}
		//	Check columns
		Set<String>notInNorm = new TreeSet<String>();
		Set<String>notInBlanks = new TreeSet<String>();
		for(int i=0; i<normHeader.length; i++) {
			
			if(!columnMap.keySet().contains(i))
				notInBlanks.add(normHeader[i]);				
		}
		for(int j=0; j<normHeaderWithBlanks.length; j++) {
			
			if(!columnMap.values().contains(j))
				notInNorm.add(normHeaderWithBlanks[j]);	
		}
		if(!notInNorm.isEmpty()) {
			
			log.add("The following column(s) present in the \"with blanks\" data file but not in the regular data file:\n");
			log.addAll(notInNorm);
			
		}
		if(!notInBlanks.isEmpty()) {
			
			log.add("The following column(s) present in the regular data file but not in the \"with blanks\" data file:\n");
			log.addAll(notInBlanks);
		}
		if(!log.isEmpty())
			log.add(" ");
		
		return columnMap;
	}	
}













