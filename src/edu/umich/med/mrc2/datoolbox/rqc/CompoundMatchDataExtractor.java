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
import java.util.Set;
import java.util.TreeMap;
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
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundMatchGroupObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundMatcherField;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.data.enums.QCANVASInputField;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class CompoundMatchDataExtractor {
	
	private static final Logger logger= LogManager.getLogger(CompoundMatchDataExtractor.class);

	private static final String TARGET_SHEET = "Unambiguous Match Groups";
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
		
		processEX01426RPNeg();
	}
	
	private static void processEX01426RPNeg() {
		
		File inputFile = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426_RP_Neg_Data-Integrator-original-format.xlsx");
		File outputFile = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426_RP_Neg_Data-Integrator-exported.txt");
		File libraryFile = new File("Y:\\DataAnalysis\\CPDMatch\\RP-Neg with 1C IS MIx - Complete Library.txt"); //-CDK-compatible
		
		rtError = 0.05d;
		mzError = 7.00d;
		adductList = new ArrayList<>();
		adductList.add(AdductManager.getAdductByName("[M-H]-"));
		parseCompoundMatchFile(inputFile, outputFile, libraryFile);
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
	
	private static void parseCompoundMatchFile(File inputFile, File outputFile, File libraryFile) {

		List<CompoundMatchGroupObject>matchGroupsList = new ArrayList<>();
		try (Workbook workbook = StreamingReader.builder().rowCacheSize(100) // number of rows to keep in memory
				.bufferSize(4096) // buffer size to use when reading InputStream to file
				.open(new FileInputStream(inputFile))) { // InputStream or File for XLSX file (required)

			for (Sheet sheet : workbook) {

				if (sheet.getSheetName().equalsIgnoreCase(TARGET_SHEET)) {
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
			
			matchUnknowns(matchGroupsList, libraryFile, reportPath);
			exportResults(matchGroupsList, outputFile);
		}
	}

	private static void matchUnknowns(
			List<CompoundMatchGroupObject> matchGroupsList, File libraryFile, Path reportPath) {

		CompoundLibrary pcdlLibrary = parseTextLibrary(libraryFile);
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

	private static CompoundLibrary parseTextLibrary(File libraryFile) {
		
		CompoundLibrary pcdlLibrary = 
				new CompoundLibrary(PathUtils.getBaseName(libraryFile.toPath()));
		String[][] compoundDataArray = DelimitedTextParser.parseTextFile(
				libraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());
				
		Map<PCDLFields, Integer>dataFieldMap = createAndValidateFieldMap(compoundDataArray[0]);
		if(dataFieldMap.isEmpty())
			return pcdlLibrary;
				
		for(int i=1; i<compoundDataArray.length; i++) {
			
			double rt = 0.0d;
			CompoundIdentity identity = null;
			try {
				identity = 
						new CompoundIdentity(
								compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)], 
								compoundDataArray[i][dataFieldMap.get(PCDLFields.FORMULA)]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(identity != null) {
								
				if(dataFieldMap.containsKey(PCDLFields.HMP)) {
					
					String hmdbId = compoundDataArray[i][dataFieldMap.get(PCDLFields.HMP)];
					if(hmdbId != null && !hmdbId.isEmpty())
						identity.addDbId(CompoundDatabaseEnum.HMDB, hmdbId);
				}
				if(dataFieldMap.containsKey(PCDLFields.PUBCHEM)) {
					
					String pubchemId = compoundDataArray[i][dataFieldMap.get(PCDLFields.PUBCHEM)];
					if(pubchemId != null && !pubchemId.isEmpty())
						identity.addDbId(CompoundDatabaseEnum.PUBCHEM, pubchemId);
				}
				if(dataFieldMap.containsKey(PCDLFields.LMP)) {
					
					String lipidMapsId = compoundDataArray[i][dataFieldMap.get(PCDLFields.LMP)];
					if(lipidMapsId != null && !lipidMapsId.isEmpty())
						identity.addDbId(CompoundDatabaseEnum.LIPIDMAPS, lipidMapsId);
				}
				if(dataFieldMap.containsKey(PCDLFields.INCHI_KEY)) {
					
					String inchiKey = compoundDataArray[i][dataFieldMap.get(PCDLFields.INCHI_KEY)];
					if(inchiKey != null && !inchiKey.isEmpty())
						identity.setInChiKey(inchiKey);
				}
				if(dataFieldMap.containsKey(PCDLFields.SMILES)) {
					
					String smiles = compoundDataArray[i][dataFieldMap.get(PCDLFields.SMILES)];
					if(smiles != null && !smiles.isEmpty())
						identity.setSmiles(smiles);
				}
				if(dataFieldMap.containsKey(PCDLFields.RETENTION_TIME)) {
					
					String rtString = compoundDataArray[i][dataFieldMap.get(PCDLFields.RETENTION_TIME)];
					if(rtString != null && !rtString.isEmpty()) {

						try {
							rt = Double.valueOf(rtString);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
				LibraryMsFeature newTarget = 
						new LibraryMsFeature(identity.getName(), null, rt);
				MsFeatureIdentity mid = new MsFeatureIdentity(
						identity, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
				newTarget.setPrimaryIdentity(mid);
				MSRTLibraryUtils.generateMassSpectrumFromAdducts(newTarget, adductList);
				newTarget.setNeutralMass(identity.getExactMass());			
				newTarget.setName(identity.getName());
				if(newTarget.getName().toUpperCase().contains("[ISTD]"))
					mid.setQcStandard(true);
				
				pcdlLibrary.addFeature(newTarget);	
			}
			
		}
		return pcdlLibrary;
	}
	
	private static Map<PCDLFields, Integer> createAndValidateFieldMap(String[]header) {
		
		Map<PCDLFields, Integer>dataFieldMap = new TreeMap<PCDLFields, Integer>();
		ArrayList<String>missingFields = new ArrayList<String>();
		for(int i=0; i<header.length; i++) {
			
			PCDLFields f = PCDLFields.getOptionByUIName(header[i]);
			if(f != null)
				dataFieldMap.put(f, i);
		}
		
		if(!dataFieldMap.containsKey(PCDLFields.NAME))
			missingFields.add(PCDLFields.NAME.getName());

		if(!dataFieldMap.containsKey(PCDLFields.FORMULA))
			missingFields.add(PCDLFields.FORMULA.getName());
		
		if(!missingFields.isEmpty())
			System.err.println("The following obligatory fields are missing form the input data:\n"
					+ StringUtils.join(missingFields, ", "));

		return dataFieldMap;			
	}	

	private static List<CompoundMatchGroupObject> parseMatchGroupsSheet(Sheet sheet) {
		
		Row header = sheet.iterator().next();
		if(hasDuplicateFileNames(header))
			return new ArrayList<>();
		
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
				Integer matchGroup = (int)r.getCell(columnMap.get(CompoundMatcherField.MATCH_GROUP)).getNumericCellValue();
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
