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

package edu.umich.med.mrc2.datoolbox.cpdmatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.AgilentProfinderDetailedExportParser;
import edu.umich.med.mrc2.datoolbox.utils.PCDLUtils;

public class CompoundMatchMergeOutputProcessor {
	
	private static final Logger logger= LogManager.getLogger(CompoundMatchMergeOutputProcessor.class);
	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		initDatabaseConnection();
		
		createCustomPCDLforEX01496RPPOS();
	}
	
	private static void initDatabaseConnection() {
		
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e1) {
			logger.error("Failed to establish database connection!", e1);
		}
		if(conn == null) {
			System.exit(1);
		} else {
			try {
				conn.close();
			} catch (SQLException e1) {
				logger.error("Failed to close database connection properly!", e1);
			}
		}
	}
	
	private static void createCustomPCDLforEX01496RPPOS() {

		File originalPCDL = new File("Y:\\DataAnalysis\\CPDMatch\\RP-Pos with 1C IS MIx - Complete Library.txt");
		File referenceBatchProFinderResults = 
				new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\A003 - Untargeted\\"
				+ "CompoundMatch\\Profinder exports\\Pos\\EX01496-RP Pos-B01-Complete Library Profinder export.csv");
		File compoundMatchOutput = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\merge_output_20260909_named.xlsx");
		File customPCDL = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\EX01496-RP-POS_customPCDL_40_60_missing.txt");
		
		CompoundMatchMergeOutputParametersObject params = new CompoundMatchMergeOutputParametersObject(
				originalPCDL, 
				referenceBatchProFinderResults, 
				compoundMatchOutput, 
				customPCDL);
		params.getAdductSet().add(AdductManager.getAdductByName("[M+H]+"));
		params.getAdductSet().add(AdductManager.getAdductByName("[M+Na]+"));
		params.setMaxPercentMissingPooled(40.0d);
		params.setMaxPercentMissingSample(60.0d);
		
		createCustomPCDLwithBackfill(params);
	}
	
	private static void createCustomPCDLwithBackfill(CompoundMatchMergeOutputParametersObject params) {
		
//		CompoundLibrary pcdlLibrary = PCDLUtils.parsePCDLTextLibrary(
//				params.getOriginalPCDL(), params.getAdductSet());
//		AgilentProfinderDetailedExportParser parser = 
//				new AgilentProfinderDetailedExportParser(params.getReferenceBatchProFinderResults());
//		Set<String>proFinderUndetected = parser.extractUndetectedCompounds(
//				params.getMaxPercentMissingPooled(), 
//				params.getMaxPercentMissingSample());
		Map<Integer,String>fullPicksMap = new TreeMap<Integer,String>();
		List<NamedCompoundMatchObject>namedGroupsList = new ArrayList<NamedCompoundMatchObject>();
		try (Workbook workbook = WorkbookFactory.create(params.getCompoundMatchOutput())){

			fullPicksMap = extractFullPicksFromCompoundMatchOutput(workbook);
			namedGroupsList = parseNamedMatchGroupsSheet(workbook);
	
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collection<Integer> batchSet = namedGroupsList.stream().
				flatMap(f -> f.getBatchFeatureMap().keySet().stream()).
				collect(Collectors.toCollection(TreeSet::new));
		int totalBatchCount = batchSet.size();
		for(NamedCompoundMatchObject cmgo : namedGroupsList) {
			
			//	Add compound names
			if(fullPicksMap.containsKey(cmgo.getGroupId()))
				cmgo.setCompoundName(fullPicksMap.get(cmgo.getGroupId()));
			
			cmgo.setMissingBatchesCount(totalBatchCount - cmgo.getBatchFeatureMap().size());
			
			//	Fill in missing data for batches
			for(int i : batchSet) {
				
				if(!cmgo.getBatchFeatureMap().containsKey(i))
					cmgo.getBatchFeatureMap().put(i, null);
				
				if(!cmgo.getBatchAdductMap().containsKey(i))
					cmgo.getBatchAdductMap().put(i, null);
			}
		}
	}
	
	private static List<NamedCompoundMatchObject> parseNamedMatchGroupsSheet(Workbook workbook) {
		
		Sheet sheet = workbook.getSheet(CompoundMatchNamedOutputSheets.NAMED_MASS_GROUPS.getName());
		Row header = sheet.getRow(1);
		Map<CompoundMatchFullPicksFields, Integer> columnMap = 
				CompoundMatcherUtils.getColumnMap4NamedMatchGroupsSheet(header);
		
		int matchGroupColumn = columnMap.get(CompoundMatchFullPicksFields.MATCH_GROUP);
		int featureColumn = columnMap.get(CompoundMatchFullPicksFields.FEATURE_NAME);	
		int batchColumn = columnMap.get(CompoundMatchFullPicksFields.BATCH);
		int adductColumn = columnMap.get(CompoundMatchFullPicksFields.BINNER_ANNOTATION);
		int mzColumn = columnMap.get(CompoundMatchFullPicksFields.MONOISOTOPIC_MZ);
		int rtColumn = columnMap.get(CompoundMatchFullPicksFields.UNNAMED_RT);
		
		Map<Integer,Set<Integer>>matchGroupRowMap = 
				getMatchGroupRowMap(sheet, matchGroupColumn);
		
		List<NamedCompoundMatchObject>matchGroupsList = new ArrayList<>();
		for(Entry<Integer,Set<Integer>>entry : matchGroupRowMap.entrySet()) {
			
			NamedCompoundMatchObject cmgo = new NamedCompoundMatchObject(entry.getKey());
			for(Integer rowNum : entry.getValue()) {
				Row r = sheet.getRow(rowNum);
				Integer batchNumber = (int)r.getCell(batchColumn).getNumericCellValue();
				
				String featureNameCM = r.getCell(featureColumn).getStringCellValue().trim();
				String[]nameParts = featureNameCM.split("_");
				if(nameParts.length>1) {
					String featureName = nameParts[0] + "_" + nameParts[1];
					cmgo.getBatchFeatureMap().put(batchNumber, featureName);
				}
				else {
					cmgo.getBatchFeatureMap().put(batchNumber, featureNameCM);
				}
				String adductName = r.getCell(adductColumn).getStringCellValue().trim();
				cmgo.getBatchAdductMap().put(batchNumber, adductName);
				
				double mz = r.getCell(mzColumn).getNumericCellValue();
				double rt = r.getCell(rtColumn).getNumericCellValue();
				cmgo.getMzValues().add(mz);
				cmgo.getRtValues().add(rt);
			}
			cmgo.finalizeObjectParameters();
			matchGroupsList.add(cmgo);
		}
		return matchGroupsList;
	}
	
	private static Map<Integer,Set<Integer>>getMatchGroupRowMap(Sheet sheet, int matchGroupColumn) {
		
		Map<Integer,Set<Integer>>matchGroupRowMap = new TreeMap<>();
			
		for (Row r : sheet) {			
			 Cell matchGroupCell = r.getCell(matchGroupColumn);
			if(r.getRowNum() > 0 && matchGroupCell != null 
					&& matchGroupCell.getCellType().equals(CellType.NUMERIC)) {
							
				Integer matchGroup = (int)matchGroupCell.getNumericCellValue();
				if(matchGroup != null) {
					
					matchGroupRowMap.computeIfAbsent(matchGroup, v -> new TreeSet<>());
					matchGroupRowMap.get(matchGroup).add(r.getRowNum());
				}
			}
		}
		return matchGroupRowMap;
	}
	
	private static void createCustomPCDL(CompoundMatchMergeOutputParametersObject params) {
		
		CompoundLibrary pcdlLibrary = PCDLUtils.parsePCDLTextLibrary(
				params.getOriginalPCDL(), params.getAdductSet());
		AgilentProfinderDetailedExportParser parser = 
				new AgilentProfinderDetailedExportParser(params.getReferenceBatchProFinderResults());
		Set<String>proFinderUndetected = parser.extractUndetectedCompounds(
				params.getMaxPercentMissingPooled(), 
				params.getMaxPercentMissingSample());
		Set<String>fullPicksFromCompoundMatch = 
				markAndExtractFullPicksFromCompoundMatchOutput(params.getCompoundMatchOutput());		
		List<LibraryMsFeature> customFeatureList = pcdlLibrary.getFeatures().stream().
			filter(f -> !fullPicksFromCompoundMatch.contains(f.getName())).
			filter(f -> !proFinderUndetected.contains(f.getName())).
			collect(Collectors.toList());
		
		PCDLUtils.writePCDLLibrary(customFeatureList, params.getCustomPCDL());
	}
	
	private static Map<Integer,String>extractFullPicksFromCompoundMatchOutput(Workbook workbook) {
		
		Map<Integer,String>fullPicks = new TreeMap<Integer,String>();
		
		Sheet compoundMatchesSheet = 
				workbook.getSheet(CompoundMatchNamedOutputSheets.COMPOUND_MATCHES.getName());
		fullPicks = extractFullPicksMapFromCompoundMatchesSheet(compoundMatchesSheet);				
		return fullPicks;
	}
		
	private static Set<String>markAndExtractFullPicksFromCompoundMatchOutput(File compoundMatchOutput) {
		
		Map<Integer,String>fullPicks = new TreeMap<Integer,String>();
		try (Workbook workbook = WorkbookFactory.create(compoundMatchOutput)){

			Sheet compoundMatchesSheet = 
					workbook.getSheet(CompoundMatchNamedOutputSheets.COMPOUND_MATCHES.getName());
			fullPicks = extractFullPicksMapFromCompoundMatchesSheet(compoundMatchesSheet);
			
			Sheet namedMassGroupsSheet = 
					workbook.getSheet(CompoundMatchNamedOutputSheets.NAMED_MASS_GROUPS.getName());
			markFullPicks(namedMassGroupsSheet, fullPicks);
			
			File outputFile = new File(compoundMatchOutput.getParentFile(), 
					PathUtils.getBaseName(compoundMatchOutput.toPath()) + "_fullPicks.csv");
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			saveFullPicksAsCsv(namedMassGroupsSheet, outputFile, evaluator);
			
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return fullPicks.values().stream().collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
	}
	
	private static void markFullPicks(Sheet sheet, Map<Integer, String> fullPicks) {

		for (Row r : sheet) {			
			
			if(r.getRowNum() > 1 && r.getCell(1) != null
					&& r.getCell(2).getCellType().equals(CellType.NUMERIC)) {
				int matchGroup = (int)r.getCell(2).getNumericCellValue();
				if(matchGroup != 0 && fullPicks.containsKey(matchGroup))
					r.getCell(0).setCellValue("1A");				
			}
		}
	}

	private static void saveFullPicksAsCsv(
			Sheet sheet, 
			File outputFile, 
			FormulaEvaluator evaluator) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			DataFormatter formatter = new DataFormatter(true);
			int maxRowIndex = sheet.getLastRowNum();
			for (Row row : sheet) {
				
				if(row != null && row.getRowNum() > maxRowIndex)
					break;

				if (row == null) {
					writer.newLine();
					continue;
				}
				int maxCellIndex = row.getLastCellNum();
				StringBuilder rowString = new StringBuilder();
				for (int j = 0; j < maxCellIndex; j++) {
					Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String cellValue = formatter.formatCellValue(cell,evaluator);
					rowString.append(escapeCsvValue(cellValue));
					if (j < maxCellIndex - 1)
						rowString.append(",");
				}
				writer.write(rowString.toString());
				writer.newLine();
			}
			System.out.println("Worksheet converted to CSV successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private static String escapeCsvValue(String value) {
    	
        if (value == null) 
            return "";
        
        String escapedValue = value;
        // If the value contains commas, quotes, or newlines, wrap it in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            escapedValue = value.replace("\"", "\"\""); // Double up existing quotes
            escapedValue = "\"" + escapedValue + "\"";
        }
        return escapedValue;
    }



	private static Map<Integer,String> extractFullPicksMapFromCompoundMatchesSheet(Sheet sheet) {

		Map<Integer,String>fullPicks = new TreeMap<Integer,String>();
		for (Row r : sheet) {			
			
			if(r.getRowNum() > 1 
					&& r.getCell(0).getCellType().equals(CellType.NUMERIC)
					&& ((int)r.getCell(0).getNumericCellValue() != 0)) {
				
				String compoundName = r.getCell(1).getStringCellValue().trim();
				int matchGroupId = (int)r.getCell(2).getNumericCellValue();
				fullPicks.put(matchGroupId, compoundName);
			}
		}
		return fullPicks;
	}
}













