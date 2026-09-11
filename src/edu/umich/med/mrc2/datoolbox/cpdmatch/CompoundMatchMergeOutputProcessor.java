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

import edu.umich.med.mrc2.datoolbox.data.Adduct;
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
	
	private static Collection<Adduct> adductList;

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
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\EX01496-RP-POS_customPCDL.txt");
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getAdductByName("[M+H]+"));
		adductList.add(AdductManager.getAdductByName("[M+Na]+"));
		
		createCustomPCDL(
				originalPCDL, 
				referenceBatchProFinderResults, 
				compoundMatchOutput, 
				customPCDL);
	}
	
	private static void createCustomPCDL(
			File originalPCDL, 
			File referenceBatchProFinderResults, 
			File compoundMatchOutput,
			File customPCDL) {
		CompoundLibrary pcdlLibrary = PCDLUtils.parsePCDLTextLibrary(originalPCDL, adductList);
		AgilentProfinderDetailedExportParser parser = 
				new AgilentProfinderDetailedExportParser(referenceBatchProFinderResults);
		Set<String>proFinderUndetected = parser.extractUndetectedCompounds();
		Set<String>fullPicksFromCompoundMatch = markAndExtractFullPicksFromCompoundMatchOutput(compoundMatchOutput);		
		List<LibraryMsFeature> customFeatureList = pcdlLibrary.getFeatures().stream().
			filter(f -> !fullPicksFromCompoundMatch.contains(f.getName())).
			filter(f -> !proFinderUndetected.contains(f.getName())).
			collect(Collectors.toList());
		
		PCDLUtils.writePCDLLibrary(customFeatureList, customPCDL);
	}
	
	private static Set<String>markAndExtractFullPicksFromCompoundMatchOutput(File compoundMatchOutput) {
		
		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(compoundMatchOutput);
		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		//	Extract full picks from compound match output
		Map<Integer,String>fullPicks = new TreeMap<Integer,String>();
		for (Sheet sheet : workbook) {

			if (sheet.getSheetName().equalsIgnoreCase(CompoundMatchNamedOutputSheets.COMPOUND_MATCHES.getName())) {
				fullPicks = parseCompoundMatchesSheet(sheet);	
				break;
			}
		}
		//	Mark full picks in compound match output Named Mass Groups sheet
		for (Sheet sheet : workbook) {

			if (sheet.getSheetName().equalsIgnoreCase(CompoundMatchNamedOutputSheets.NAMED_MASS_GROUPS.getName())) {
				markFullPicks(sheet, fullPicks);
				File outputFile = new File(compoundMatchOutput.getParentFile(), 
						PathUtils.getBaseName(compoundMatchOutput.toPath()) + "_fullPicks.csv");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				saveFullPicksAsCsv(sheet, outputFile, evaluator);
				break;
			}
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



	private static Map<Integer,String> parseCompoundMatchesSheet(Sheet sheet) {

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













