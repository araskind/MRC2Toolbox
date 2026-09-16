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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.github.pjfanning.xlsx.StreamingReader;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundMatchGroupObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.QCANVASInputField;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.PCDLUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class CompoundMatchDataExtractor {
	
	private static final Logger logger= LogManager.getLogger(CompoundMatchDataExtractor.class);

	//private static final String TARGET_SHEET = "Unambiguous Match Groups";
	private static final Pattern fileNamePattern = 
			Pattern.compile(MRC2ToolBoxConfiguration.CORE_DATA_FILE_MASK_DEFAULT);
	
	private static double rtError;
	private static double mzError;
	private static Collection<Adduct> adductList;
	private static NumberFormat intensityFormatter = DefaultFormatStore.getDefaultSpectrumIntensityFormat();
	private static NumberFormat mzFormatter = DefaultFormatStore.getDefaultMZformat();
	private static NumberFormat rtFormatter = DefaultFormatStore.getDefaultRTformat();
		
	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		initDatabaseConnection();
		
		processEX01426RPPos();
	}
	
	private static void processEX01496RPNeg() {

		File inputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01496_RP_NEG_merge_output_20260902.xlsx");
		File fullPicksFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\1496 RP Neg-full picks.xlsx");
		File outputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01496_RP_Neg_Data-Integrator-exported-20260910.txt");
		File libraryFile = new File("Y:\\DataAnalysis\\CPDMatch\\RP-Neg with 1C IS MIx - Complete Library.txt");
		rtError = 0.05d;
		mzError = 7.00d;
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getDefaultAdductForCharge(-1));
		parseCompoundMatchFile(inputFile, outputFile, libraryFile, fullPicksFile);		
	}
	
	private static void processEX01496RPPos() {

		File inputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01496_RP_POS_merge_output_20260902.xlsx");
		File fullPicksFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\1496 RP Pos-full picks.xlsx");
		File outputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01496_RP_Pos_Data-Integrator-exported-20260910.txt");
		File libraryFile = new File("Y:\\DataAnalysis\\CPDMatch\\RP-Pos with 1C IS MIx - Complete Library.txt");
		rtError = 0.05d;
		mzError = 7.00d;
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getDefaultAdductForCharge(1));
		parseCompoundMatchFile(inputFile, outputFile, libraryFile, fullPicksFile);		
	}
	
	private static void processEX01426IONPNeg() {

		File inputFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\CompoundMatch\\EX01426_IONP_NEG_merge_output_20260902.xlsx");
		File fullPicksFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\CompoundMatch\\1426 IP FeaturePicker_named-full picks.xlsx");
		File outputFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\CompoundMatch\\EX01426_IONP_Neg_Data-Integrator-exported-20260910.txt");
		File libraryFile = new File("Y:\\DataAnalysis\\CPDMatch\\IP-Neg with 1C IS MIx - Complete Library.txt");
		rtError = 0.05d;
		mzError = 7.00d;
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getDefaultAdductForCharge(-1));
		parseCompoundMatchFile(inputFile, outputFile, libraryFile, fullPicksFile);		
	}
	
	private static void processEX01426RPNeg() {

		File inputFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01426_RP_NEG_merge_output_20260902.xlsx");
		File fullPicksFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\CompoundMatch\\1426 RP Neg named full picks.xlsx");
		File outputFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01426_RP_Neg_Data-Integrator-exported-20260910.txt");
		File libraryFile = new File("Y:\\DataAnalysis\\CPDMatch\\RP-Neg with 1C IS MIx - Complete Library.txt");
		rtError = 0.05d;
		mzError = 7.00d;
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getDefaultAdductForCharge(-1));
		parseCompoundMatchFile(inputFile, outputFile, libraryFile, fullPicksFile);		
	}
	
	private static void processEX01426RPPos() {

		File inputFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01426_RP_POS_merge_output_20260902.xlsx");
		File fullPicksFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\CompoundMatch\\1426 RP Pos named full picks.xlsx");
		File outputFile = new File("S:\\DataAnalysis\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\CompoundMatch\\EX01426_RP_Pos_Data-Integrator-exported-20260914.txt");
		File libraryFile = new File("Y:\\DataAnalysis\\CPDMatch\\RP-Pos with 1C IS MIx - Complete Library.txt");
		rtError = 0.05d;
		mzError = 7.00d;
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getDefaultAdductForCharge(1));
		parseCompoundMatchFile(inputFile, outputFile, libraryFile, fullPicksFile);		
	}
	
	private static void parseCompoundMatchFile(
			File inputFile, File outputFile, File libraryFile, File fullPicksFile) {

		List<CompoundMatchGroupObject>matchGroupsList = new ArrayList<>();
		try (Workbook workbook = StreamingReader.builder().rowCacheSize(100) // number of rows to keep in memory
				.bufferSize(4096) // buffer size to use when reading InputStream to file
				.open(new FileInputStream(inputFile))) { // InputStream or File for XLSX file (required)

			for (Sheet sheet : workbook) {

				if (sheet.getSheetName().equalsIgnoreCase(DataIntegratorOutputSheets.UNAMBIGUOUS_MATCH_GROUPS.getName())) {
					matchGroupsList = parseMatchGroupsSheet(sheet);	
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!matchGroupsList.isEmpty() && libraryFile != null) {
			Path reportPath = 
					Paths.get(inputFile.toPath().getParent().toString(), 
							PathUtils.getBaseName(inputFile.toPath()) + "_unk_match_report.txt");
			
			//	matchUnknowns(matchGroupsList, libraryFile, reportPath);
			matchByFullPicks(matchGroupsList, fullPicksFile, reportPath);
			exportResults(matchGroupsList, outputFile);
		}
	}

	private static void matchByFullPicks(
			List<CompoundMatchGroupObject> matchGroupsList, File fullPicksFile, Path reportPath) {

		Map<Integer,String>matchGroupCompoundMap = 
				extractMatchGroupCompoundMap(fullPicksFile);
		List<String>unambiguousMatches = new ArrayList<>();
		List<String>umatched = new ArrayList<>();
		for(CompoundMatchGroupObject cmgo : matchGroupsList) {
			
			if(cmgo.getFeature().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
				
				String originalName = cmgo.getFeature();
				String fullPicksMatch = matchGroupCompoundMap.get(cmgo.getGroupId());
				if(fullPicksMatch == null)
					umatched.add(cmgo.getFeature());
				else {
					cmgo.setFeature(fullPicksMatch);
					unambiguousMatches.add(originalName + "\t" + fullPicksMatch);
				}
			}
		}
		List<String>reportParts = new ArrayList<>();
		reportParts.add("UNAMBIGUOUS MATCHES\n");
		reportParts.addAll(unambiguousMatches);
		reportParts.add("\n********************\n");
		reportParts.add("UNMATCHED UNKNOWNS\n");
		reportParts.addAll(umatched);
		reportParts.add("\n********************\n");
		
		//	Add MZ and RT outliers
		reportParts.add("FEATURES WITH M/Z OUTLIERS\n");
		for(CompoundMatchGroupObject cmo : matchGroupsList) {
			if(cmo.mzOutliersPresent())
				reportParts.add(cmo.getFeature() + "\t" + cmo.getGroupId());
		}	
		reportParts.add("\n********************\n");
		
		reportParts.add("FEATURES WITH RT OUTLIERS\n");
		for(CompoundMatchGroupObject cmo : matchGroupsList) {
			if(cmo.rtOutliersPresent())
				reportParts.add(cmo.getFeature() + "\t" + cmo.getGroupId());
		}	
		reportParts.add("\n********************\n");	
		//	Add duplicate names
		reportParts.add("DUPLICATE FEATURE NAMES\n");
		Map<String, Long> countsByFeature = matchGroupsList.stream().map(o -> o.getFeature()).
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		for(Entry<String, Long>cbf : countsByFeature.entrySet()) {
			if(cbf.getValue() > 1)
				reportParts.add("Duplicate feature name: " + cbf.getKey());
		}		
		try {
		    Files.write(reportPath, 
		    		reportParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private static void matchUnknowns(
			List<CompoundMatchGroupObject> matchGroupsList, File libraryFile, Path reportPath) {

		CompoundLibrary pcdlLibrary = PCDLUtils.parsePCDLTextLibrary(libraryFile, adductList);
		if(pcdlLibrary.getFeatures().isEmpty())
			return;
		
		List<String>unambiguousMatches = new ArrayList<>();
		List<String>ambiguousMatches = new ArrayList<>();
		List<String>umatched = new ArrayList<>();
		for(CompoundMatchGroupObject cmgo : matchGroupsList) {
			
			String originalName = cmgo.getFeature();
			
			if(cmgo.getFeature().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
				List<LibraryMsFeature>matches = matchToLibrary(cmgo, pcdlLibrary);
				if(matches.isEmpty())
					umatched.add(cmgo.getFeature());
				
				if(matches.size() == 1)
					unambiguousMatches.add(originalName + "\t" + cmgo.getFeature());
					
				if(matches.size() > 1) {				
					ambiguousMatches.add(originalName + " matched to " + cmgo.getFeature());
					for(LibraryMsFeature m : matches) {
						
						for(Adduct ad : m.getSpectrum().getAdducts()) {
							ambiguousMatches.add(m.getName() + "\t" + ad.getName() + 
								"\tMZ: " + mzFormatter.format(m.getSpectrum().getMonoisotopicMzForAdduct(ad)) +
								"\tRT: " + rtFormatter.format(m.getRetentionTime()));
						}
					}
				}
			}
		}
		List<String>reportParts = new ArrayList<>();
		reportParts.add("Matching parameters:");
		reportParts.add("Mass error " + DefaultFormatStore.getDefaultPpmFormat().format(mzError) + "ppm");
		reportParts.add("Mass error " + DefaultFormatStore.getDefaultRTformat().format(rtError) + "min");
		reportParts.add("\n********************\n");
		reportParts.add("UNAMBIGUOUS MATCHES\n");
		reportParts.addAll(unambiguousMatches);
		reportParts.add("\n********************\n");
		if(!ambiguousMatches.isEmpty()) {
			reportParts.add("AMBIGUOUS MATCHES\n");
			reportParts.addAll(ambiguousMatches);
			reportParts.add("\n********************\n");
		}
		reportParts.add("UNMATCHED UNKNOWNS\n");
		reportParts.addAll(umatched);
		try {
		    Files.write(reportPath, 
		    		reportParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}		
	}
	
	private static List<LibraryMsFeature> matchToLibrary(
			CompoundMatchGroupObject cmgo, 
			CompoundLibrary pcdlLibrary) {
		List<LibraryMsFeature>matches = new ArrayList<>();
		Range rtRange = new Range(cmgo.getRt() - rtError, cmgo.getRt() + rtError);
		List<LibraryMsFeature> matchedByRt = pcdlLibrary.getFeatures().stream().
			filter(f -> rtRange.contains(f.getRetentionTime())).
			collect(Collectors.toList());
		if(matchedByRt.isEmpty()) {
			return matches;
		}
		else {
			for(LibraryMsFeature lf : matchedByRt) {
				
				for(Adduct ad : lf.getSpectrum().getAdducts()) {				
					
					Range mzRange = MsUtils.createMassRange(
							lf.getSpectrum().getMonoisotopicMzForAdduct(ad), mzError, MassErrorType.mDa);
					if(mzRange.contains(cmgo.getMz()))
						matches.add(lf);					
				}
			}
		}
		if(matches.isEmpty()) {
			return matches;
		}
		else if(matches.size() == 1) {
			cmgo.setFeature(matches.get(0).getName());
			return matches;
		}
		else {
			LibraryMsFeature bestMatch = null;
			double deltaRt = 100000.0d;
			for(LibraryMsFeature m : matches) {
				double delta = Math.abs(cmgo.getRt() - m.getRetentionTime());
				if(delta < deltaRt) {
					deltaRt = delta;
					bestMatch = m;
				}
			}
			cmgo.setFeature(bestMatch.getName());
		}	
		return matches;
	}

	

	private static List<CompoundMatchGroupObject> parseMatchGroupsSheet(Sheet sheet) {
		
		Row header = sheet.iterator().next();
		if(hasDuplicateFileNames(header))
			return new ArrayList<>();
		
		Map<DataIntegratorMatchGroupFields, Integer> columnMap = CompoundMatcherUtils.getColumnMap4MatchGroupsSheet(header);
		Map<String,Integer>rawFileMap = extractDataFileMap(header);	
		Integer currentMatchGroup = null;
		List<CompoundMatchGroupObject>matchGroupsList = new ArrayList<>();
		CompoundMatchGroupObject cmgo = null;
		int featureColumn = columnMap.get(DataIntegratorMatchGroupFields.FEATURE);
		int matchGroupColumn = columnMap.get(DataIntegratorMatchGroupFields.MATCH_GROUP);
		int mzColumn = columnMap.get(DataIntegratorMatchGroupFields.MONOISOTOPIC_MZ);
		int rtColumn = columnMap.get(DataIntegratorMatchGroupFields.RT);
		
		for (Row r : sheet) {			
			
			if(r.getRowNum() > 0 
					&& !r.getCell(featureColumn).getStringCellValue().trim().isEmpty()) {
				
				if(!r.getCell(matchGroupColumn).getCellType().equals(CellType.NUMERIC)) {
					System.out.println("Non-numeric MATCH_GROUP in row " + r.getRowNum());
					if(cmgo != null) {
						cmgo.finalizeObjectParameters();
						matchGroupsList.add(cmgo);						
					}
					break;
				}			
				Integer matchGroup = (int)r.getCell(matchGroupColumn).getNumericCellValue();
				if(matchGroup != null && !matchGroup.equals(currentMatchGroup)) {
					
					if(cmgo != null) {
						cmgo.finalizeObjectParameters();
						matchGroupsList.add(cmgo);						
					}					
					currentMatchGroup = matchGroup;
					cmgo = new CompoundMatchGroupObject(currentMatchGroup, rawFileMap.keySet());
				}
				if(cmgo != null) {
					
					String featureName = r.getCell(featureColumn).getStringCellValue().trim();
					double mz = r.getCell(mzColumn).getNumericCellValue();
					double rt = r.getCell(rtColumn).getNumericCellValue();
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
		
		List<CompoundMatchGroupObject>cleanList = 
				removeUnknownsOverlappingWithManuallyPicked(matchGroupsList);
		
		List<CompoundMatchGroupObject>sortedList = cleanList.stream().
				sorted(new CompoundMatchGroupObjectComparator(SortProperty.RT)).
				collect(Collectors.toList());		
		
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
	
	private static List<CompoundMatchGroupObject>removeUnknownsOverlappingWithManuallyPicked(
			List<CompoundMatchGroupObject> matchGroupsList){
		
		List<CompoundMatchGroupObject>filteredList = new ArrayList<>();
		List<CompoundMatchGroupObject> manualPicks = matchGroupsList.stream().
				filter(f -> Objects.isNull(f.getUnknownName())).collect(Collectors.toList());
		filteredList.addAll(manualPicks);
		
		Set<String> manualPickNames = manualPicks.stream().
				map(f -> f.getFeature()).collect(Collectors.toSet());
		List<CompoundMatchGroupObject> filteredUnknowns = matchGroupsList.stream().
				filter(f -> !Objects.isNull(f.getUnknownName())).
				filter(f -> !manualPickNames.contains(f.getFeature())).
				collect(Collectors.toList());
		filteredList.addAll(filteredUnknowns);

		return filteredList;
	}

	private static String createExportHeader(CompoundMatchGroupObject compoundMatchGroupObject) {
		
		List<String>parts = new ArrayList<>();
		for(QCANVASInputField f : QCANVASInputField.values())
			parts.add(f.getName());
		
		parts.add("");
		compoundMatchGroupObject.getPeakAreas().keySet().forEach(e -> parts.add(e));	
		return StringUtils.join(parts, "\t");
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
	
	private static Map<Integer,String>extractMatchGroupCompoundMap(File fullPicksFile){
		
		Map<Integer,String>matchGroupCompoundMap = new TreeMap<>();
		String nameTrimPattern = "-\\d+_\\d+.\\d+$";
		try (Workbook workbook = StreamingReader.builder().rowCacheSize(100) // number of rows to keep in memory
				.bufferSize(4096) // buffer size to use when reading InputStream to file
				.open(new FileInputStream(fullPicksFile))) { // InputStream or File for XLSX file (required)

			Sheet sheet = workbook.getSheetAt(0);
			Map<CompoundMatchFullPicksFields,Integer>columnMap = new TreeMap<>();
			for (Row r : sheet) {			
				if(r.getRowNum() == 1) {
					columnMap = createFullPicksColumnMap(r);
					break;
				}
			}
			int matchGroupColumn = columnMap.get(CompoundMatchFullPicksFields.MATCH_GROUP);
			int compoundNameColumn = columnMap.get(CompoundMatchFullPicksFields.MAPPED_COMPOUND);
			
			for (Row r : sheet) {			
				if(r.getRowNum() > 1 && r.getCell(compoundNameColumn) != null && r.getCell(matchGroupColumn) != null) {
					String cpdName = r.getCell(compoundNameColumn).getStringCellValue().trim().replaceFirst(nameTrimPattern, "");
					if(!cpdName.isEmpty()) {
						int matchGroup = (int)r.getCell(matchGroupColumn).getNumericCellValue();
						matchGroupCompoundMap.put(matchGroup, cpdName);
					}
				}
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return matchGroupCompoundMap;
	}
	
	private static Map<CompoundMatchFullPicksFields, Integer> createFullPicksColumnMap(Row row) {

		Map<CompoundMatchFullPicksFields, Integer>columnMap = new TreeMap<>();
		for (int i=0; i<row.getPhysicalNumberOfCells(); i++) {

			if(row.getCell(i) != null) {
				
				String colName = row.getCell(i).getStringCellValue();
				if(!colName.isBlank()) {
					CompoundMatchFullPicksFields field = 
							CompoundMatchFullPicksFields.getOptionByUIName(colName);
					if(field != null)
						columnMap.put(field, i);
				}
			}
		}
		return columnMap;
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

}
