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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundMatcherMappingComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.AgilentProfinderDetailedExportParser;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.PCDLUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class CompoundMatchMergeOutputProcessor {
	
	private static final Logger logger= LogManager.getLogger(CompoundMatchMergeOutputProcessor.class);
	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		initDatabaseConnection();		
		//	createCustomPCDLforEX01496RPPOS();
		
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

		File originalPCDL = new File("S:\\DataAnalysis\\CPDMatch\\RP-Pos with 1C IS MIx - Complete Library.txt");
		File referenceBatchProFinderResults = 
				new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\A003 - Untargeted\\"
				+ "CompoundMatch\\Profinder exports\\Pos\\EX01496-RP Pos-B01-Complete Library Profinder export.csv");
		File compoundMatchOutput = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\merge_output_20260909_named.xlsx");
		File customPCDL = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\EX01496-RP-POS_customPCDL_40_60_missing_20260916.txt");
		
		CompoundMatchMergeOutputParametersObject params = new CompoundMatchMergeOutputParametersObject(
				originalPCDL, 
				referenceBatchProFinderResults, 
				compoundMatchOutput, 
				customPCDL);
		params.setReferenceBatchNumber(1);
		params.getAdductSet().add(AdductManager.getAdductByName("[M+H]+"));
		params.getAdductSet().add(AdductManager.getAdductByName("[M+Na]+"));
		params.setMaxPercentMissingPooled(40.0d);
		params.setMaxPercentMissingSample(60.0d);
		params.setMaxMissingBatches(2);
		params.setMzErrorForMissingLookup(30.0d);
		params.setRtErrorForMissingLookup(0.2d);
		
		//	createCustomPCDL(params);
		createCustomPCDLwithBackfill(params);		
	}
	
	private static void testIncomletePicksExtraction() {
		
		File compoundMatchOutput = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\merge_output_20260909_named.xlsx");
		Map<Integer,String>incompletePicksMap = new TreeMap<>();
		try (Workbook workbook = WorkbookFactory.create(compoundMatchOutput)){

			Sheet compoundMatchesSheet = 
					workbook.getSheet(CompoundMatchNamedOutputSheets.COMPOUND_MATCHES.getName());
			incompletePicksMap = 
					extractIncompletePicksMapFromCompoundMatchesSheet(workbook);
	
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(Entry<Integer,String> e : incompletePicksMap.entrySet()) {
			System.out.println(e.getValue() + " => " + e.getKey());
		}
	}
	
	
	 
	
	private static void createCustomPCDLwithBackfill(CompoundMatchMergeOutputParametersObject params) {
		
		CompoundLibrary pcdlLibrary = PCDLUtils.parsePCDLTextLibrary(
				params.getOriginalPCDL(), params.getAdductSet());
		
//		AgilentProfinderDetailedExportParser parser = 
//				new AgilentProfinderDetailedExportParser(params.getReferenceBatchProFinderResults());
//		Set<String>proFinderUndetected = parser.extractUndetectedCompounds(
//				params.getMaxPercentMissingPooled(), 
//				params.getMaxPercentMissingSample());
	
		Map<Integer,String>fullPicksMap = new TreeMap<Integer,String>();
		List<NamedCompoundMatchObject>namedGroupsList = new ArrayList<NamedCompoundMatchObject>();
		Map<Integer,String>incompletePicksMap = new TreeMap<>();
		
		try (Workbook workbook = WorkbookFactory.create(params.getCompoundMatchOutput())){

			fullPicksMap = extractFullPicksMapFromCompoundMatchesSheet(workbook);
			namedGroupsList = parseNamedMatchGroupsSheet(workbook);
			//	matcherGroups = extractMatcherGroups(workbook, pcdlLibrary);
			if(params.getMaxMissingBatches() > 0) {				

				incompletePicksMap = 
						extractIncompletePicksMapFromCompoundMatchesSheet(workbook);
				calculateDataMissingness(namedGroupsList, fullPicksMap);
				fillInIncompletePicks(incompletePicksMap, namedGroupsList, workbook, params);
			}
			File filteredFullPicksOutputFile = 
					Paths.get(params.getCompoundMatchOutput().getParent(), 
							PathUtils.getBaseName(params.getCompoundMatchOutput().toPath()) +
							"_filteredFullPicks.csv").toFile();
			//	saveFilteredFullPicksCsv(workbook, filteredFullPicksOutputFile);
	
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//	Check for duplicate names

		
		
	}
		
	private static void fillInIncompletePicks(
			Map<Integer, String> incompletePicksMap,
			List<NamedCompoundMatchObject> namedGroupsList, 
			Workbook workbook,
			CompoundMatchMergeOutputParametersObject params) {
		for(Entry<Integer, String> e : incompletePicksMap.entrySet()) {
			
			NamedCompoundMatchObject toFill = 
					namedGroupsList.stream().filter(o -> o.getGroupId() == e.getKey()).
					findFirst().orElse(null);
			if(toFill == null)
				continue;
					
			Set<Integer>missingBatches = toFill.getMissingBatches();
			if(missingBatches.isEmpty()) {
				System.out.println(toFill.getGroupId() + " has no missing data");
			}
			if(missingBatches.isEmpty() || missingBatches.size() > params.getMaxMissingBatches())
				continue;
			
			Range mzRange = MsUtils.createPpmMassRange(toFill.getMz(), params.getMzErrorForMissingLookup());
			Range rtRange = new Range(
					toFill.getRt() - params.getRtErrorForMissingLookup(),
					toFill.getRt() + params.getRtErrorForMissingLookup());
			
			List<NamedCompoundMatchObject>candidates = namedGroupsList.stream().
					filter(g -> g.getGroupId() != toFill.getGroupId()).
					filter(g -> mzRange.contains(g.getMz())).
					filter(g -> rtRange.contains(g.getRt())).
					filter(g -> g.hasDataForBatches(missingBatches)).
					collect(Collectors.toList());
//			if(candidates.isEmpty())
//				System.out.println(toFill.getGroupId() + " => Nothing found");
//			else if(candidates.size() == 1) {
//				System.out.println(toFill.getGroupId() + " => " + candidates.iterator().next().getGroupId());
//			}
//			else {
//				System.out.println("Multiple hits");
//			}
		}		
	}

	private static void fillInMissingData(Workbook workbook,
			Map<NamedCompoundMatchObject, NamedCompoundMatchObject> fillInsForMissingData) {
		// TODO Auto-generated method stub
		for(Entry<NamedCompoundMatchObject, NamedCompoundMatchObject>e : fillInsForMissingData.entrySet()) {
			String message =String.format("Original group: %s\nReplacement group: %s\nMissing batches: %s\n***\n", 
					e.getKey().getGroupId(), 
					e.getValue().getGroupId(), 
					StringUtils.join(e.getKey().getMissingBatches(), ", "));
			System.out.println(message);
		}
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
	
	private static List<CompoundMatcherMappingGroupObject> extractMatcherGroups(
			Workbook workbook, 
			CompoundLibrary pcdlLibrary) {

		Sheet sheet = workbook.getSheet(CompoundMatchNamedOutputSheets.MAPPING.getName());
		Row header = sheet.getRow(0);
		Map<CompoundMatcherMappingFields, Integer> columnMap = 
				CompoundMatcherUtils.getColumnMap4MappingSheet(header);
		
		int namedFeatureColumn = columnMap.get(CompoundMatcherMappingFields.NAMED_FEATURE);
		int unnamedFeatureColumn = columnMap.get(CompoundMatcherMappingFields.UNNAMED_FEATURE);	
		int correlationColumn = columnMap.get(CompoundMatcherMappingFields.CORRELATION);

		String namedSuffixPattern = "-\\d+_(?:\\d+\\.\\d+|NaN)$";
		String unnamedSuffixPattern = "_\\d+\\.\\d+-\\d$";
		List<CompoundMatcherMappingGroupObject>matchGroupsList = new ArrayList<>();
		for (Row r : sheet) {			
			Cell nameFeatureCell = r.getCell(namedFeatureColumn);
			if(r.getRowNum() > 0 && nameFeatureCell != null
					&& nameFeatureCell.getCellType().equals(CellType.STRING)) {
				
				String compoundName = nameFeatureCell.getStringCellValue().
						trim().replaceAll(namedSuffixPattern, "");
								
				LibraryMsFeature libFeature = null;
				if(!compoundName.isBlank()) 
					libFeature = pcdlLibrary.getFeatureByName(compoundName);
				
				if(libFeature == null) {
					
					if(!compoundName.isBlank())
						System.err.println("***\nCouldn'f find " + compoundName + " in the library\n***");
				}
				else {
					CompoundMatcherMappingGroupObject cmgo = 
							matchGroupsList.stream().filter(o -> o.getLibraryMatch().getName().equals(compoundName)).
							findFirst().orElse(null);					
					if(cmgo == null ) {
						cmgo = new CompoundMatcherMappingGroupObject(libFeature);
						matchGroupsList.add(cmgo);
					}
					double correlation = 0.0d;
					Cell corrCell = r.getCell(correlationColumn);
					if(corrCell != null && corrCell.getCellType().equals(CellType.NUMERIC))
						correlation = corrCell.getNumericCellValue();
						
					String unnamedFeature = r.getCell(unnamedFeatureColumn).getStringCellValue().
							trim().replaceAll(unnamedSuffixPattern, "");
					if(unnamedFeature.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
						String[]nameParts = unnamedFeature.replace(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName(), "").split("_");
						double mz = Double.parseDouble(nameParts[0]);
						double rt = Double.parseDouble(nameParts[1]);
						Adduct a = getAdductForMz(libFeature, mz);
						if(a != null) {
							CompoundMatcherMapping mapping = new CompoundMatcherMapping(unnamedFeature, mz, rt, correlation);					
							cmgo.addMapping(mapping, a);
						}
						else {
							System.err.println("***\nCouldn'f find adduct in " + 
									libFeature.getName() + " spectrum for M/Z "+ 
									Double.toString(mz) + "\n***");
						}
					}
				}
			}
		}
		return matchGroupsList;
	}
	
	private static Adduct getAdductForMz(LibraryMsFeature libFeature, double queryMz) {
		
		Range mzRange = new Range(queryMz - 0.1, queryMz + 0.1);
		for(Adduct a : libFeature.getSpectrum().getAdducts()) {
			
			if(mzRange.contains(libFeature.getSpectrum().getMonoisotopicMzForAdduct(a)))
				return a;
		}
		return null;
	}

	private static Map<NamedCompoundMatchObject,NamedCompoundMatchObject> findFillInsForMissingData(
			List<NamedCompoundMatchObject>namedGroupsList, 
			List<CompoundMatcherMappingGroupObject> matcherGroups, 
			CompoundMatchMergeOutputParametersObject params) {
		
		Map<NamedCompoundMatchObject,NamedCompoundMatchObject>fillInMap = new HashMap<>();
		int maxMissing = params.getMaxMissingBatches();
		List<NamedCompoundMatchObject> groupsToFillIn = namedGroupsList.stream().
				filter(g -> Objects.nonNull(g.getCompoundName())).
				filter(g -> g.getMissingBatchesCount() > 0).
				filter(g -> g.getMissingBatchesCount() <= maxMissing).
				collect(Collectors.toList());
		for(NamedCompoundMatchObject groupToFill : groupsToFillIn) {
			
			String compoundName = groupToFill.getCompoundName();
			CompoundMatcherMappingGroupObject cmgo = 
					matcherGroups.stream().filter(m -> m.getLibraryMatch().getName().equals(compoundName)).
					findFirst().orElse(null);
			if(cmgo != null) {
				
				NamedCompoundMatchObject bestCandidateForFillIn = findBestCandidateForFillIn(groupToFill,
												namedGroupsList,
												cmgo,
												params);
				if(bestCandidateForFillIn != null) 
					fillInMap.put(groupToFill, bestCandidateForFillIn);
			}
		}
		return fillInMap;
	}
	
	private static NamedCompoundMatchObject getBestMatch(NamedCompoundMatchObject groupToFill,
			Map<CompoundMatcherMapping, List<NamedCompoundMatchObject>> candidatesForFillIn) {

		double correlation = 0.0d;
		CompoundMatcherMapping bestCorrMapping = null;
		for (Entry<CompoundMatcherMapping, List<NamedCompoundMatchObject>> cmmEntry : candidatesForFillIn.entrySet()) {

			if(cmmEntry.getKey().getCorrelation() > correlation) {
				bestCorrMapping = cmmEntry.getKey();
				correlation = cmmEntry.getKey().getCorrelation();
			}
		}
		if(bestCorrMapping == null)
			bestCorrMapping = candidatesForFillIn.keySet().iterator().next();
		
		double relDiff = 1000000.0d;
		NamedCompoundMatchObject bestMatch = null;
		for(NamedCompoundMatchObject candidate : candidatesForFillIn.get(bestCorrMapping)) {
			
			double newRelDiff = groupToFill.getAbsRelativeMZRTdifference(candidate);
			if(newRelDiff < relDiff) {
				bestMatch = candidate;
				relDiff = newRelDiff;
			}
		}
		
		return bestMatch;
	}
	
	private static NamedCompoundMatchObject findBestCandidateForFillIn(
			NamedCompoundMatchObject groupToFill,
			List<NamedCompoundMatchObject>namedGroupsList,
			CompoundMatcherMappingGroupObject cmgo,
			CompoundMatchMergeOutputParametersObject params){
		if(groupToFill.getGroupId() == 5465)
			System.out.println("***");
		
		Map<CompoundMatcherMapping, List<NamedCompoundMatchObject>>candidates = 
				new TreeMap<>(new CompoundMatcherMappingComparator(SortProperty.Name));
		Set<Integer>missingBatches = groupToFill.getMissingBatches();
		String unkToExclude = groupToFill.getUnknownName();
		Adduct adduct = cmgo.getAdductForMz(groupToFill.getMz());
		double mzError = params.getMzErrorForMissingLookup();
		double rtError = params.getRtErrorForMissingLookup();
		if(adduct != null) {
			
			List<NamedCompoundMatchObject>namedGroupsListClean = 
					namedGroupsList.stream().filter(o -> !o.equals(groupToFill)).
					collect(Collectors.toList());			
			List<CompoundMatcherMapping> candidateRefBatchMatches = 
					cmgo.getMappings().get(adduct).stream().
					filter(m -> !m.getUnknownFeature().equals(unkToExclude)).
					collect(Collectors.toList());
						
			for(CompoundMatcherMapping cmm : candidateRefBatchMatches) {
				
				Range mzRange = MsUtils.createPpmMassRange(cmm.getMz(), mzError);
				Range rtRange = new Range(cmm.getRt() - rtError, cmm.getRt() + rtError);
				List<NamedCompoundMatchObject>mappedObjects = namedGroupsListClean.stream().
						filter(g -> mzRange.contains(g.getMz())).
						filter(g -> rtRange.contains(g.getRt())).
						filter(g -> g.hasDataForBatches(missingBatches)).
						collect(Collectors.toList());
				if(!mappedObjects.isEmpty())
					candidates.put(cmm, mappedObjects);
			}
		}
		if(candidates.isEmpty())
			return null;
		else
			return getBestMatch(groupToFill,candidates);
	}
		
	private static void calculateDataMissingness(
			List<NamedCompoundMatchObject>namedGroupsList,
			Map<Integer,String>fullPicksMap) {
		
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
		
		String namedSuffixPattern = "-\\d+_\\d+\\.\\d+$";
		String unnamedSuffixPattern = "_\\d+\\.\\d+-\\d+$";
		
		int matchGroupColumn = columnMap.get(CompoundMatchFullPicksFields.MATCH_GROUP);
		int featureColumn = columnMap.get(CompoundMatchFullPicksFields.FEATURE_NAME);	
		int batchColumn = columnMap.get(CompoundMatchFullPicksFields.BATCH);
		int adductColumn = columnMap.get(CompoundMatchFullPicksFields.BINNER_ANNOTATION);
		int mzColumn = columnMap.get(CompoundMatchFullPicksFields.MONOISOTOPIC_MZ);
		int rtColumn = columnMap.get(CompoundMatchFullPicksFields.UNNAMED_RT);
		int mappedCompoundColumn = columnMap.get(CompoundMatchFullPicksFields.MAPPED_COMPOUND);
		
		Map<Integer,Set<Integer>>matchGroupRowMap = 
				getMatchGroupRowMap(sheet, matchGroupColumn);
		
		List<NamedCompoundMatchObject>matchGroupsList = new ArrayList<>();
		for(Entry<Integer,Set<Integer>>entry : matchGroupRowMap.entrySet()) {
			
			NamedCompoundMatchObject cmgo = new NamedCompoundMatchObject(entry.getKey());
			for(Integer rowNum : entry.getValue()) {
				Row r = sheet.getRow(rowNum);
				Integer batchNumber = (int)r.getCell(batchColumn).getNumericCellValue();
				
				String featureName = r.getCell(featureColumn).getStringCellValue().
						trim().replaceAll(unnamedSuffixPattern, "");
				if(featureName.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) 				
					cmgo.getBatchFeatureMap().put(batchNumber, featureName);				

				String adductName = r.getCell(adductColumn).getStringCellValue().trim();
				cmgo.getBatchAdductMap().put(batchNumber, adductName);
				
				double mz = r.getCell(mzColumn).getNumericCellValue();
				double rt = r.getCell(rtColumn).getNumericCellValue();
				cmgo.getMzValues().add(mz);
				cmgo.getRtValues().add(rt);
				
				Cell mappedCompoundCell = r.getCell(mappedCompoundColumn);
				if(mappedCompoundCell != null 
						&& mappedCompoundCell.getCellType().equals(CellType.STRING)
						&& mappedCompoundCell.getStringCellValue() != null
						&& !mappedCompoundCell.getStringCellValue().isBlank()) {
					String compoundNameRaw = mappedCompoundCell.getStringCellValue().trim();
					if(!compoundNameRaw.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
						
						String compoundName = compoundNameRaw.replaceAll(namedSuffixPattern, "");
						cmgo.setCompoundName(compoundName);
					}					
				}
					
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

	private static Set<String>markAndExtractFullPicksFromCompoundMatchOutput(File compoundMatchOutput) {
		
		Map<Integer,String>fullPicks = new TreeMap<Integer,String>();
		try (Workbook workbook = WorkbookFactory.create(compoundMatchOutput)){

			fullPicks = extractFullPicksMapFromCompoundMatchesSheet(workbook);			
			Sheet namedMassGroupsSheet = 
					workbook.getSheet(CompoundMatchNamedOutputSheets.NAMED_MASS_GROUPS.getName());
			markFullPicks(namedMassGroupsSheet, fullPicks);
			saveFilteredFullPicksCsv(workbook, compoundMatchOutput);
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return fullPicks.values().stream().collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
	}
	
	private static void saveFilteredFullPicksCsv(Workbook workbook, File compoundMatchOutput) {

		Sheet source = 
				workbook.getSheet(CompoundMatchNamedOutputSheets.NAMED_MASS_GROUPS.getName());

    	List<Row>toCopy = new ArrayList<>();
    	for (Row r : source) {
    		
    		if(r.getRowNum() == 1)
    			r.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue("Pick");
    		
    		if(r.getRowNum() < 2)
    			toCopy.add(r);
    		else {
    			Cell pick = r.getCell(0);
    			if(pick != null && pick.getCellType().equals(CellType.STRING) && !pick.getStringCellValue().isBlank())
    				toCopy.add(r);
    		}      		
    	}
		Path shortPicksPath = Paths.get(compoundMatchOutput.getParent(), 
				PathUtils.getBaseName(compoundMatchOutput.toPath()) + "_filtered.csv");
		
    	FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(shortPicksPath.toFile()))) {
			DataFormatter formatter = new DataFormatter(true);
			for (Row row : toCopy) {
				
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
	
	private static void saveFullPickRowssAsCsv(
			Collection<Row>rows, 
			File outputFile, 
			FormulaEvaluator evaluator) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			DataFormatter formatter = new DataFormatter(true);
			for (Row row : rows) {
				
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

    private static Map<Integer,String> extractIncompletePicksMapFromCompoundMatchesSheet(Workbook workbook) {
    	
		Sheet compoundMatchesSheet = 
				workbook.getSheet(CompoundMatchNamedOutputSheets.COMPOUND_MATCHES.getName());
		
    	Map<Integer,String>incompletePicks = new TreeMap<Integer,String>();
    	Map<Integer,String>fullPicks = extractFullPicksMapFromCompoundMatchesSheet(workbook);
    	Set<String>matchedCompounds = new TreeSet<>(fullPicks.values());
    	Map<String,Double>correlationMap = new TreeMap<>();
    	Map<String,Integer>bestGroupMap = new TreeMap<>();
		for (Row r : compoundMatchesSheet) {			
			
			Cell pickCell = r.getCell(0);
			if(r.getRowNum() > 1 
					&& pickCell != null && pickCell.getCellType().equals(CellType.STRING)
					&& pickCell.getStringCellValue().isBlank()
					&& r.getCell(1) != null
					&& r.getCell(1).getCellType().equals(CellType.STRING)) {
				
				String compoundName = r.getCell(1).getStringCellValue().trim();
				if(!matchedCompounds.contains(compoundName) && r.getCell(2).getCellType().equals(CellType.NUMERIC)) {
					int matchGroupId = (int)r.getCell(2).getNumericCellValue();
					double correlation = r.getCell(4).getNumericCellValue();
					bestGroupMap.computeIfAbsent(compoundName, v -> matchGroupId);
					correlationMap.computeIfAbsent(compoundName, v -> correlation);
					if(correlationMap.get(compoundName) < correlation) {
						correlationMap.put(compoundName, correlation);
						bestGroupMap.put(compoundName, matchGroupId);
					}				
				}			
			}
		}
		bestGroupMap.forEach((k,v) -> incompletePicks.put(v, k));
    	return incompletePicks;
    }

	private static Map<Integer,String> extractFullPicksMapFromCompoundMatchesSheet(Workbook workbook) {

		Sheet compoundMatchesSheet = 
				workbook.getSheet(CompoundMatchNamedOutputSheets.COMPOUND_MATCHES.getName());
		
		Map<Integer,String>fullPicks = new TreeMap<Integer,String>();
		for (Row r : compoundMatchesSheet) {			
			
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
	
	
	private static void evaluateMzErrors() {
		
		File originalPCDL = new File("S:\\DataAnalysis\\CPDMatch\\RP-Pos with 1C IS MIx - Complete Library.txt");
		Set<Adduct>adductSet = new TreeSet<>();
		adductSet.add(AdductManager.getAdductByName("[M+H]+"));
		adductSet.add(AdductManager.getAdductByName("[M+Na]+"));
		CompoundLibrary pcdlLibrary = PCDLUtils.parsePCDLTextLibrary(originalPCDL, adductSet);
		
		File inputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\picked_4error_evaluation.txt");
		String[][]tranchData = new String[0][0];
		try {
			tranchData = DelimitedTextParser.parseTextFileWithEncoding(
					inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NumberFormat ppmFormat = DefaultFormatStore.getDefaultPpmFormat();
		NumberFormat rtFormat = DefaultFormatStore.getDefaultRTformat();
		List<String>lines = new ArrayList<>();
		List<String>rtLines = new ArrayList<>();
		Set<String>detectedAdducts = new TreeSet<>();
		for(int i=1; i<tranchData.length; i++) {
			
			LibraryMsFeature libFeature = pcdlLibrary.getFeatureByName(tranchData[i][0]);
			double libRt = libFeature.getRetentionTime();
			List<String>line = new ArrayList<>();
			List<String>rtLine = new ArrayList<>();
			line.add(tranchData[i][0]);
			rtLine.add(tranchData[i][0]);
			detectedAdducts.clear();
			for(int j=1; j<tranchData[i].length; j++) {

				String[] parts = tranchData[i][j].split("_");
				double mz = Double.parseDouble(parts[1]);
				double rt = Double.parseDouble(parts[2]);
				double minMzErrorPpm = 1000000000000.0d;			
				Adduct matchedAdduct = null;			
				for(Adduct a : libFeature.getSpectrum().getAdducts()) {
					
					double libMz = libFeature.getSpectrum().getMonoisotopicMzForAdduct(a);
					double mzError = Math.abs((mz - libMz)/mz * 1000000.0d);
					if(mzError < minMzErrorPpm) {
						minMzErrorPpm = mzError;
						matchedAdduct = a;
					}				
				}
				double rtError = rt - libRt;
				rtLine.add(rtFormat.format(rtError));
				detectedAdducts.add(matchedAdduct.getName());
				line.add(ppmFormat.format(minMzErrorPpm));
			}
			line.add(StringUtils.join(detectedAdducts, "\t"));
			lines.add(StringUtils.join(line, "\t"));
			rtLines.add(StringUtils.join(rtLine, "\t"));
		}
		File outputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\picked_mz_error_evaluation_results.txt");
		try {
		    Files.write(outputFile.toPath(), 
		    		lines,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		File rtOutputFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\CompoundMatch\\20260909\\POS\\picked_RT_error_evaluation_results.txt");
		try {
		    Files.write(rtOutputFile.toPath(), 
		    		rtLines,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}













