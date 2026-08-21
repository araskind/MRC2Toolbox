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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.github.pjfanning.xlsx.StreamingReader;

import edu.umich.med.mrc2.datoolbox.data.compare.CompoundMatchGroupObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundMatcherField;
import edu.umich.med.mrc2.datoolbox.data.enums.QCANVASInputField;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class CompoundMatchDataExtractor {

	private static final String TARGET_SHEET = "Unambiguous Match Groups";
	private static final Pattern fileNamePattern = 
			Pattern.compile(MRC2ToolBoxConfiguration.CORE_DATA_FILE_MASK_DEFAULT);
	
	public static void main(String[] args) {

		File inputFile = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426_RP_Neg_Data-Integrator-original-format.xlsx");
		File outputFile = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426_RP_Neg_Data-Integrator-exported.txt");
		parseCompoundMatchFile(inputFile, outputFile);
	}
	
	private static void parseCompoundMatchFile(File inputFile, File outputFile) {

		try (Workbook workbook = StreamingReader.builder().rowCacheSize(100) // number of rows to keep in memory
				.bufferSize(4096) // buffer size to use when reading InputStream to file
				.open(new FileInputStream(inputFile))) { // InputStream or File for XLSX file (required)

			for (Sheet sheet : workbook) {

				if (sheet.getSheetName().equalsIgnoreCase(TARGET_SHEET)) {
					List<CompoundMatchGroupObject>matchGroupsList = parseMatchGroupsSheet(sheet);
					if(matchGroupsList != null && !matchGroupsList.isEmpty())
						exportResults(matchGroupsList, outputFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<CompoundMatchGroupObject> parseMatchGroupsSheet(Sheet sheet) {
		
		Row header = sheet.iterator().next();
		int headerLength = header.getPhysicalNumberOfCells();
		if(hasDuplicateFileNames(header))
			return null;
		
		Map<CompoundMatcherField, Integer> columnMap = getColumnMap(header);
		Map<String,Integer>rawFileMap = extractDataFileMap(header);	
		Integer currentMatchGroup = null;
		List<CompoundMatchGroupObject>matchGroupsList = new ArrayList<>();
		CompoundMatchGroupObject cmgo = null;
		for (Row r : sheet) {			
			
			if(r.getRowNum() > 0 
					&& !r.getCell(columnMap.get(CompoundMatcherField.FEATURE)).getStringCellValue().trim().isEmpty()) {
				
				if(!r.getCell(columnMap.get(CompoundMatcherField.MATCH_GROUP)).getCellType().equals(CellType.NUMERIC)) {
					System.out.println("Non-numeric MATCH_GROUP in row " + r.getRowNum());
					if(cmgo != null) {
						cmgo.finalizeObjectParameters();
						matchGroupsList.add(cmgo);						
					}
					break;
				}			
				Integer matchGroup =  
						Double.valueOf(r.getCell(columnMap.get(CompoundMatcherField.MATCH_GROUP)).getNumericCellValue()).intValue();
				if(matchGroup != null && !matchGroup.equals(currentMatchGroup)) {
					
					if(cmgo != null) {
						cmgo.finalizeObjectParameters();
						matchGroupsList.add(cmgo);						
					}
					
					currentMatchGroup = matchGroup;
					cmgo = new CompoundMatchGroupObject(currentMatchGroup, rawFileMap.keySet());
				}
				if(cmgo != null) {
					
					String featureName = r.getCell(columnMap.get(CompoundMatcherField.FEATURE)).getStringCellValue().trim();
					double mz = r.getCell(columnMap.get(CompoundMatcherField.MONOISOTOPIC_MZ)).getNumericCellValue();
					double rt = r.getCell(columnMap.get(CompoundMatcherField.RT)).getNumericCellValue();
					cmgo.getFeatureNames().add(featureName);
					cmgo.getMzValues().add(mz);
					cmgo.getRtValues().add(rt);
					for(Entry<String,Integer>rfe : rawFileMap.entrySet()) {
						
						Cell paCell = r.getCell(rfe.getValue());
						if(paCell.getCellType().equals(CellType.NUMERIC)) {
							double peakArea = r.getCell(rfe.getValue()).getNumericCellValue();
							cmgo.getPeakAreas().put(rfe.getKey(), peakArea);
						}
					}
				}
			}
		}
		return matchGroupsList;
	}
	
	private static void exportResults(List<CompoundMatchGroupObject> matchGroupsList, File outputFile) {
		
		List<CompoundMatchGroupObject>sortedList = matchGroupsList.stream().
				sorted(new CompoundMatchGroupObjectComparator(SortProperty.RT)).
				collect(Collectors.toList());		
		
		NumberFormat intensityFormatter = DefaultFormatStore.getDefaultSpectrumIntensityFormat();
		List<String> lines = new ArrayList<>();
		String header = createExportHeader(sortedList.get(0));
		lines.add(header);
		List<String> line = new ArrayList<>();
		for(CompoundMatchGroupObject cmgo : sortedList) {
			
			line.clear();
			for(QCANVASInputField f : QCANVASInputField.values()) {
				
				if(f.equals(QCANVASInputField.FEATURE)) {
					line.add(cmgo.getFeature());
				}
				else if(f.equals(QCANVASInputField.AVERAGE_MONOISOTOPIC_MZ)) {
					line.add(Double.toString(cmgo.getMz()));
				}
				else if(f.equals(QCANVASInputField.AVERAGE_RT)) {
					line.add(Double.toString(cmgo.getRt()));
				}
				else
					line.add("");
			}
			line.add("");
			for(Entry<String,Double>peakAreaEntry : cmgo.getPeakAreas().entrySet()) {
				
				if(peakAreaEntry.getValue() == null)
					line.add("");
				else
					line.add(intensityFormatter.format(peakAreaEntry.getValue()));
			}			
			lines.add(StringUtils.join(line, "\t"));
		}
		try {
		    Files.write(outputFile.toPath(), 
		    		lines,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private static String createExportHeader(CompoundMatchGroupObject compoundMatchGroupObject) {
		
		List<String>parts = new ArrayList<>();
		for(QCANVASInputField f : QCANVASInputField.values())
			parts.add(f.getName());
		
		parts.add("");
		compoundMatchGroupObject.getPeakAreas().keySet().forEach(e -> parts.add(e));	
		return StringUtils.join(parts, "\t");
	}

	private static Map<CompoundMatcherField,Integer>getColumnMap(Row header){

		Map<CompoundMatcherField,Integer>columnMap = new TreeMap<CompoundMatcherField,Integer>();
		int headerLength = header.getPhysicalNumberOfCells();
		for (int i=0; i<headerLength; i++) {

			Cell c = header.getCell(i);
			for(CompoundMatcherField field : CompoundMatcherField.values()) {

				if(c.getStringCellValue().equals(field.getName()))
					columnMap.put(field, i);
			}
		}
		return columnMap;
	}
	
	private static Map<String,Integer>extractDataFileMap(Row header){
		
		int headerLength = header.getPhysicalNumberOfCells();
		Map<String,Integer>rawFileMap = new TreeMap<>();
		for (int i=0; i<headerLength; i++) {

			String colName = header.getCell(i).getStringCellValue();
			if(fileNamePattern.matcher(colName).matches())
				rawFileMap.put(colName, i);
		}
		return rawFileMap;
	}
	
	private static boolean hasDuplicateFileNames(Row header) {
		
		int headerLength = header.getPhysicalNumberOfCells();
		List<String>rawFileNames = new ArrayList<>();
		for (int i=0; i<headerLength; i++) {

			String colName = header.getCell(i).getStringCellValue();
			if(fileNamePattern.matcher(colName).matches())
				rawFileNames.add(colName);
		}		
		Set<String> duplicates = TextUtils.findDuplicateNames(rawFileNames);
		if(!duplicates.isEmpty()) {
			System.err.println("Duplicate file names found");
			duplicates.forEach(d -> System.out.println(d));
			return true;
		}
		else
			return false;
	}

}
