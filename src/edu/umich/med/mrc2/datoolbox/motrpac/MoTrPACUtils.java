/*******************************************************************************
 *
duplicateDataUploadDirectoryForResultsCorrection4PreCovidPlasma * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.motrpac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACmetaboliteMetaDataFields;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.LIMSReportingUtils;
import edu.umich.med.mrc2.datoolbox.utils.filefilter.DirectoryFileFilterIE;

public class MoTrPACUtils {
	
	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final MDLV2000Reader molReader  = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		
		// File parentDir = new File("Y:\\DataAnalysis\\_Reports\\EX01094 - MoTrPAC Muscle PreCOVID-20210219\\4Upload\\_4BIC\\HUMAN");
		//	File parentDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1A-06\\_FINALS");
		try {
			//	createMoTrPACFileManifests4PreCovidAdipose();
			extractRefmetDisrepanciesForAllExperiments();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createMultiTissueFixScriptGenInputMapForEX01263(){
		
		File partialInput = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\fixInputInterm.txt");
		File bicDataDir = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\20241023FIX\\PASS1A-18");
		File outputFile = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\fixInputComplete.txt");
		
		createMultiTissueFixScriptGenInputMap(partialInput, bicDataDir, outputFile);
	}
	
	private static void createMultiTissueFixScriptGenInputMap(File partialInput, File bicDataDir, File outputFile) {
		
		String[][] inputData = DelimitedTextParser.parseTextFile(
				partialInput, MRC2ToolBoxConfiguration.getTabDelimiter());
		ArrayList<String>outputData = new ArrayList<String>();
		List<Path> manifestPathList = new ArrayList<Path>();
		List<Path> resultPathList = new ArrayList<Path>();
		
		for(int i=0; i<inputData.length; i++) {
			
			manifestPathList.clear();
			resultPathList.clear();
			
			String manifestPath = null;
			String resultPath = null;
			
			Path searchPath = Paths.get(bicDataDir.toPath().toString(), inputData[i][1], inputData[i][2]);
			manifestPathList = FIOUtils.findFilesByNameStartingWith(searchPath, "metadata_sample_");
			for(Path mp : manifestPathList) {
				
				for(Path part : mp) {
					 if(part.toString().equalsIgnoreCase(inputData[i][3])) {
						 manifestPath = mp.toString();
						 break;
					 }
				}
			}	
			resultPathList = FIOUtils.findFilesByNameStartingWith(searchPath, "results_metabolites_");
			for(Path rp : resultPathList) {
				
				for(Path part : rp) {
					 if(part.toString().equalsIgnoreCase(inputData[i][3])) {
						 resultPath = rp.toString();
						 break;
					 }
				}
			}
			String line = StringUtils.join(inputData[i], "\t");
			if(manifestPath == null || resultPath == null) {
				System.err.println(line + " not found");
			}
			else{
				outputData.add(line + "\t" + manifestPath + "\t" + resultPath);
			}
		}
		try {
		    Files.write(outputFile.toPath(), 
		    		outputData,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void getFulPathsToQcanvasDirsForEX01263() {
		
		File dirNamesFile = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\QCANVASdirList.txt");
		File lookupDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN");
		File outputFile = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\QCANVASdirMap.txt");
		
		getFulPathsToQcanvasDirs(dirNamesFile, lookupDir, outputFile);
	}
	
	private static void getFulPathsToQcanvasDirs(File dirNamesFile, File lookupDir, File outputFile) {
		
		String[][] inputData = DelimitedTextParser.parseTextFile(
				dirNamesFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		ArrayList<String>outputData = new ArrayList<String>();
		List<Path> pathList = new ArrayList<Path>();
		for(int i=0; i<inputData.length; i++) {
			pathList.clear();
			pathList = FIOUtils.findDirectoriesByName(lookupDir.toPath(), inputData[i][0]);
			if(pathList.isEmpty()) {
				System.err.println(inputData[i][0] + " not found");
			}
			else {
				outputData.add(inputData[i][0] + "\t" + pathList.get(0).toString());
			}
		}
		try {
		    Files.write(outputFile.toPath(), 
		    		outputData,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PASS1A18() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20241023FIX\\PASS1A-18");
		String processingDateIdentifier = "20241023";
		
		duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}	
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PASS1A06() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS06-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS06-20241017");
		String processingDateIdentifier = "20241017";
		
		duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PreCovidAdipose() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\HUMAN\\T11-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\HUMAN\\T11-20241017");
		String processingDateIdentifier = "20241017";
		
		duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}	
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PreCovidMuscle() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01094-tmp\\T06-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01094-tmp\\T06-20241017");
		String processingDateIdentifier = "20241017";
		
		duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PreCovidPlasma() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\HUMAN\\T02-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\HUMAN\\T02-20241017");
		String processingDateIdentifier = "20241017";
		
		duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}
	
	//
	
	private static void duplicateDataUploadDirectoryForResultsCorrection(
			File sourceDir,
			File destinationDir,
			String processingDateIdentifier) {
		
		//	Copy excluding RAW directories
		DirectoryFileFilterIE filter = new DirectoryFileFilterIE("RAW", true);
		try {
			FileUtils.copyDirectory(sourceDir, destinationDir, filter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(destinationDir.exists()) {
			
			//	Rename PROCESSED_ directories 
			Path newDirPath = Paths.get(destinationDir.getAbsolutePath());
			List<Path>processedDirs = FIOUtils.findDirectoriesByNameStartingWith(newDirPath, "PROCESSED_");
			String processedFolderId  = "PROCESSED_" + processingDateIdentifier;
			for(Path pd : processedDirs) {
				
				Path newProcessedPath = Paths.get(pd.getParent().toString(), processedFolderId);
				try {					
					Files.move(pd, newProcessedPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//	Truncate "results" files
				if(newProcessedPath.toFile().exists()) {
					
					List<Path>results = FIOUtils.findFilesByNameStartingWith(newProcessedPath, "results_metabolites_");
					for(Path res : results) {
						try {
							Files.write(res, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}			
		}
	}
		
	private static void createMoTrPACFileManifests1263Heart() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T31"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18");
		String batchDateIdentifier = "BATCH1_20230710";
		String processingDateIdentifier = "PROCESSED_20230710";
		createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230817",
				false);
	}
	
	private static void createMoTrPACFileManifests4Pass1A18() {
		
		List<String>tissueTypes = Arrays.asList(
				"T31",
				"T55",
				//"T58",
				"T59",
				"T66",
				"T68",
				"T69",
				"T70");
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18");
		String batchDateIdentifier = "BATCH1_20230710";
		String processingDateIdentifier = "PROCESSED_20230710";
		createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230725",
				false);
	}
		
	private static void createMotrpacDataUploadDirectoryStructure(
			List<String>tissueTypes, 
			File parentDirectory,
			String batchDateIdentifier,
			String processingDateIdentifier,
			String studyPhase) {
		List<String>assayTypes = 
				new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		try {
			LIMSReportingUtils.createMotrpacDataUploadDirectoryStructure(
					tissueTypes, 
					assayTypes, 
					parentDirectory,
					1,
					batchDateIdentifier,
					processingDateIdentifier,
					studyPhase);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createMoTrPACFileManifest(
			List<String>tissueTypes, 
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate,
			boolean processRawFiles) {
		List<String>assayTypes = 
				new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					createMoTrPACTissueAssayManifestFile(
							tissue,
							assay,
							parentDirectory,
							batchId,
							processedFolderId,
							manifestDate,
							processRawFiles);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void createMoTrPACTissueAssayManifestFile(
			String tissue,
			String assay,
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate,
			boolean processRawFiles) throws IOException {
		
		StringBuffer checkSumData = new StringBuffer();
		checkSumData.append("file_name,md5\n");
		Path batchPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId);
		Path processedPath = Paths.get(batchPath.toString(), processedFolderId);
		Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
		List<Path> pathList = Files.find(namedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "NAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
		pathList = Files.find(unnamedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "UNNAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(processedPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(batchPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_phase"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		if(processRawFiles) {
			
			Path rawDirPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW");
			pathList = Files.find(rawDirPath,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".zip"))).
				collect(Collectors.toList());
			for(Path filePath : pathList) {
				String zipHash = DigestUtils.sha256Hex(
						new FileInputStream(filePath.toString()));
				String localPath = filePath.toFile().getName().replace(".zip", "");
				checkSumData.append(localPath + "," + zipHash + "\n");
			}
		}
		String withCorrectedSeparator = checkSumData.toString().replaceAll("\\\\", "/");
		File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), 
				tissue, assay, batchId, "file_manifest_" + manifestDate + ".csv").toFile();
		FileUtils.writeStringToFile(manifestFile, withCorrectedSeparator, Charset.defaultCharset());
	}
	
	public static void createMoTrPACTissueAssayNoRawManifestFile(
			String tissue,
			String assay,
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate) throws IOException {
		
		StringBuffer checkSumData = new StringBuffer();
		checkSumData.append("file_name,md5\n");
		Path batchPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId);
		Path processedPath = Paths.get(batchPath.toString(), processedFolderId);
		Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
		List<Path> pathList = Files.find(namedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "NAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
		pathList = Files.find(unnamedDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + "UNNAMED" + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(processedPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		pathList = Files.find(batchPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_phase"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = processedFolderId + File.separator + filePath.toFile().getName();
			checkSumData.append(localPath + "," + zipHash + "\n");
		}
		File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "file_manifest_noRaw" + manifestDate + ".csv").toFile();
		FileUtils.writeStringToFile(manifestFile, checkSumData.toString(), Charset.defaultCharset());
	}
	
	private static void compressRawDataFromMutibatchRun(
			String includeFileListFile, 
			List<String>sourceFolders, 
			String destinationFolder) {
		
		List<String> includeFileList = new ArrayList<String>();
		Path includeFileListPath = Paths.get(includeFileListFile);
		try {
			includeFileList = Files.readAllLines(includeFileListPath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Path destinationPath = Paths.get(destinationFolder);
		for(String rawDataDirectory : sourceFolders) {
			
			Path sourcePath = Paths.get(rawDataDirectory);
			List<Path> pathList = new ArrayList<Path>();
			try {
				pathList = Files.find(sourcePath,
						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
					collect(Collectors.toList());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			for(Path rdp : pathList) {
				
				String rawFileName = FilenameUtils.getBaseName(rdp.toString());
				if(includeFileList.contains(rawFileName)) {
					
					try {
						FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					File destination = Paths.get(destinationPath.toString(),
							FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
					try {
						CompressionUtils.zipFolder(rdp.toFile(), destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ArchiveException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}								
				}
			}
		}		
	}
	
	private static void writeMetaboliteMetadataFile(
			Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata,
			Path metaboliteMetadataFilePath) {
		
		ArrayList<String>lines = new ArrayList<String>();
		ArrayList<String>header = new ArrayList<String>();
		for(MoTrPACmetaboliteMetaDataFields field : MoTrPACmetaboliteMetaDataFields.values())
			header.add(field.getName());
		
		lines.add(StringUtils.join(header, MRC2ToolBoxConfiguration.getTabDelimiter()));
		for(Map<MoTrPACmetaboliteMetaDataFields, String> mmd : metaboliteMetadata) {
			
			ArrayList<String>line = new ArrayList<String>();
			for(MoTrPACmetaboliteMetaDataFields field : MoTrPACmetaboliteMetaDataFields.values()) {
				
				String value = mmd.get(field);
				if(value != null)
					line.add(value);
				else
					line.add("");				
			}
			lines.add(StringUtils.join(line, MRC2ToolBoxConfiguration.getTabDelimiter()));
		}
		try {
			Files.write(metaboliteMetadataFilePath, 
					lines, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>
			parseMetaboliteMetadataFile(File metaboliteMetadataFile) throws Exception{
		
		Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
				new ArrayList<Map<MoTrPACmetaboliteMetaDataFields,String>>();
		String[][] metaboliteData = null;
		try {
			metaboliteData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						metaboliteMetadataFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(metaboliteData == null) {
			throw new Exception("Unable to read metabolite data file!");
		}
		Map<MoTrPACmetaboliteMetaDataFields,Integer>columnMap = 
				new TreeMap<MoTrPACmetaboliteMetaDataFields,Integer>();
		String[]header = metaboliteData[0];
		for(int i=0; i<header.length; i++) {
			if(header[i].trim().isEmpty())
				continue;
			
			MoTrPACmetaboliteMetaDataFields field = 
					MoTrPACmetaboliteMetaDataFields.getOptionByUIName(header[i].trim());
			
			if(field != null)
				columnMap.put(field, i);
		}
		for(int i=1; i<metaboliteData.length; i++) {
			Map<MoTrPACmetaboliteMetaDataFields,String>mDataMap = new TreeMap<MoTrPACmetaboliteMetaDataFields,String>();
			for(Entry<MoTrPACmetaboliteMetaDataFields, Integer> col : columnMap.entrySet())				
				mDataMap.put(col.getKey(), metaboliteData[i][col.getValue()]);

			metaboliteMetadata.add(mDataMap);
		}
		return metaboliteMetadata;
	}
	
	private static void createMotrpacDataUploadDirectoryStructure4PreCovidAdipose() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T11"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\Interm");
		String batchDateIdentifier = "20230202";
		String processingDateIdentifier = "20230202";
		String studyPhase = "HUMAN-PRECOVID";
		createMotrpacDataUploadDirectoryStructure(
				tissueTypes, 
				 parentDirectory,
				 batchDateIdentifier,
				 processingDateIdentifier,
				 studyPhase);
	}
	
	private static void createMoTrPACFileManifests4PreCovidAdipose() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T11"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\HUMAN");
		String batchDateIdentifier = "BATCH1_20230202";
		String processingDateIdentifier = "PROCESSED_20230202";
		createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230617",
				false);
	}
	
	private static void createMotrpacDataUploadDirectoryStructure4PreCovidPlasma() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T02"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\Interm2");
		String batchDateIdentifier = "20230124";
		String processingDateIdentifier = "20241016";
		String studyPhase = "HUMAN-PRECOVID";
		createMotrpacDataUploadDirectoryStructure(
				tissueTypes, 
				 parentDirectory,
				 batchDateIdentifier,
				 processingDateIdentifier,
				 studyPhase);
	}
	
	private static void createMotrpacDataUploadDirectoryStructure4PreCovidMuscle() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T06"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01094 - MoTrPAC Muscle "
				+ "PreCOVID-20210219\\4Upload\\_4BIC\\_BACKUP_20230617\\");
		String batchDateIdentifier = "20220404";
		String processingDateIdentifier = "20220404";
		String studyPhase = "HUMAN-PRECOVID";
		createMotrpacDataUploadDirectoryStructure(
			 tissueTypes, 
			 parentDirectory,
			 batchDateIdentifier,
			 processingDateIdentifier,
			 studyPhase);
	}
	
	private static void createMoTrPACFileManifests4PreCovidMuscle() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T06"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01094 - MoTrPAC Muscle PreCOVID-20210219\\4Upload\\_4BIC\\HUMAN");
		String batchDateIdentifier = "BATCH1_20221014";
		String processingDateIdentifier = "PROCESSED_20221014";
		createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230617",
				false);
	}
		
	private static void createMoTrPACFileManifests4PreCovidPlasma() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T02"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\HUMAN");
		String batchDateIdentifier = "BATCH1_20230124";
		String processingDateIdentifier = "PROCESSED_20230616";
		createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230616",
				false);
	}
	
	private static void compressMoTrPACPreCovidMuscleRawDataFiles() throws IOException {
		
		//	Excluded list
		Path excludeFileListPath = Paths.get(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 "
				+ "Clinical Skeletal Muscle Samples Pre COVID 2 19 2021"
				+ "\\4Upload\\_4BIC\\RAW\\exclude_file_list.txt");
		List<String> excludeFileList = new ArrayList<String>();
		try {
			excludeFileList = Files.readAllLines(excludeFileListPath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Map<String,String>sourceDestinationMap = new TreeMap<String,String>();
		sourceDestinationMap.put(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\RAW\\IONPNEG", 
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\T06\\IONPNEG\\BATCH1_20220404\\RAW");
		sourceDestinationMap.put(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\RAW\\RPNEG", 
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\T06\\RPNEG\\BATCH1_20220404\\RAW");
		sourceDestinationMap.put(
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\RAW\\RPPOS", 
				"Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC\\T06\\RPPOS\\BATCH1_20220404\\RAW");
				
		for(Entry<String, String> entry : sourceDestinationMap.entrySet()) {
			
			Path rawDataDirectory = Paths.get(entry.getKey());
			Path zippedRawFilesDirPath = Paths.get(entry.getValue());
			File checkSumFile = Paths.get(zippedRawFilesDirPath.toString(), " checksum.txt").toFile();			
			
			Collection<String>processed = new TreeSet<String>();
			TreeMap<File,Long> fileSizeMap = new TreeMap<File,Long>();
			TreeMap<File,String> fileHashMap = new TreeMap<File,String>();
					
			List<Path> pathList = Files.find(rawDataDirectory,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
				collect(Collectors.toList());
			for(Path rdp : pathList) {
				
				FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
				String rawFileName = FilenameUtils.getBaseName(rdp.toString());
				if(!excludeFileList.contains(rawFileName)) {
					
					File destination = Paths.get(zippedRawFilesDirPath.toString(),
							FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
					try {
						CompressionUtils.zipFolder(rdp.toFile(), destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ArchiveException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(destination.exists()) {
						try {
							String zipHash = DigestUtils.sha256Hex(
									new FileInputStream(destination.getAbsolutePath()));

							fileHashMap.put(destination, zipHash);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						fileSizeMap.put(destination, destination.length());
						processed.add(rawFileName);
					}								
				}
			}					
			//	Write checksum file
			StringBuffer checkSumData = new StringBuffer();
			checkSumData.append("File name\t");
			checkSumData.append("SHA256\t");
			checkSumData.append("Size\n");
			for(Entry<File, String> csentry : fileHashMap.entrySet()) {
				
				if(fileSizeMap.get(csentry.getKey()) != null) {
					
					checkSumData.append(csentry.getKey().getName() +"\t");
					checkSumData.append(csentry.getValue() +"\t");
					checkSumData.append(Long.toString(fileSizeMap.get(csentry.getKey())) + "\n");
				}
			}
			try {
				FileUtils.writeStringToFile(checkSumFile, checkSumData.toString(), Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void createPass1ACMotrpacDataUploadDirectoryStructure() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T31 - Plasma",
						"T55 - Muscle",
						"T58 - Heart",
						"T59 - Kidney",
						"T66 - Lung",
						"T68 - Liver",
						"T69 - Adipose brown",
						"T70 - Adipose white"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));		
		File parentDirectory = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1AC\\_FINALS");
		String studyPhase = "PASS1A-06";
		try {
			LIMSReportingUtils.createMotrpacDataUploadDirectoryStructure(
					tissueTypes, 
					assayTypes, 
					parentDirectory,
					1,
					"20210603",
					"20220926",
					studyPhase);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createManifestsDirectoryStructure() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T31 - Plasma",
						"T55 - Muscle",
						"T58 - Heart",
						"T59 - Kidney",
						"T66 - Lung",
						"T68 - Liver",
						"T69 - Adipose brown",
						"T70 - Adipose white"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C Shipment ANI870 10082\\4BIC\\PASS1AC\\Manifests");
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					Files.createDirectories(
							Paths.get(parentDirectory.getAbsolutePath(), tissue, assay));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void createRawDataVaultDirectoryStructure() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T31 - Plasma",
						"T55 - Muscle",
						"T58 - Heart",
						"T59 - Kidney",
						"T66 - Lung",
						"T68 - Liver",
						"T69 - Adipose brown",
						"T70 - Adipose white"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C Shipment ANI870 10082\\4BIC\\PASS1AC\\RAW_AC");
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					Files.createDirectories(
							Paths.get(parentDirectory.getAbsolutePath(), tissue, assay));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}	

	private static void createMoTrPACFileManifest() throws IOException {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T31 - Plasma",
						"T55 - Muscle",
						"T58 - Heart",
						"T59 - Kidney",
						"T66 - Lung",
						"T68 - Liver",
						"T69 - Adipose brown",
						"T70 - Adipose white"));
		List<String>assayTypes = 
				new ArrayList<String>(Arrays.asList("IONPNEG" ,"RPNEG", "RPPOS"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1A-06");	
		String batchId = "BATCH1_20210603";	
		String processedFolderId  = "PROCESSED_20210806";
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				StringBuffer checkSumData = new StringBuffer();
				checkSumData.append("file_name,md5\n");
				Path processedPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, processedFolderId);
				Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
				List<Path> pathList = Files.find(namedDirPath,
						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
					collect(Collectors.toList());
				for(Path filePath : pathList) {
					String zipHash = DigestUtils.sha256Hex(
							new FileInputStream(filePath.toString()));
					String localPath = processedFolderId + File.separator + "NAMED" + File.separator + filePath.toFile().getName();
					checkSumData.append(localPath + "," + zipHash + "\n");
				}
				Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
				pathList = Files.find(unnamedDirPath,
						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
					collect(Collectors.toList());
				for(Path filePath : pathList) {
					String zipHash = DigestUtils.sha256Hex(
							new FileInputStream(filePath.toString()));
					String localPath = processedFolderId + File.separator + "UNNAMED" + File.separator + filePath.toFile().getName();
					checkSumData.append(localPath + "," + zipHash + "\n");
				}
				pathList = Files.find(processedPath,
						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
					collect(Collectors.toList());
				for(Path filePath : pathList) {
					String zipHash = DigestUtils.sha256Hex(
							new FileInputStream(filePath.toString()));
					String localPath = processedFolderId + File.separator + filePath.toFile().getName();
					checkSumData.append(localPath + "," + zipHash + "\n");
				}
				Path rawChecksumPathPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW", " checksum.txt");
				List<String> zipCs = Files.readAllLines(rawChecksumPathPath);
				for(int i=1; i<zipCs.size(); i++) {
					String[]parts = zipCs.get(i).split("\t");
					checkSumData.append(parts[0].replace(".zip", "") + "," + parts[1] + "\n");
				}
				File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "file_manifest_20220208.csv").toFile();
				FileUtils.writeStringToFile(manifestFile, checkSumData.toString(), Charset.defaultCharset());
			}
		}
	}
	
	private static void compressMoTrPACRawDataFiles() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T69 - Adipose brown",
						"T70 - Adipose white",
						"T58 - Heart",
						"T59 - Kidney",
						"T68 - Liver",
						"T66 - Lung",
						"T55 - Muscle",
						"T31 - Plasma"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1AC");	
		File rawBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1AC\\RAW_AC");	
		try {
			String batchId = "BATCH1_20210603";	
			String processedFolderId  = "PROCESSED_20210806";
			for(String tissue : tissueTypes) {
							
				for(String assay : assayTypes) {
					
					Path zippedRawFilesDirPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW");
					Path namedDirPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, processedFolderId, "NAMED");
					Path sampleInfo = Files.find(namedDirPath, Integer.MAX_VALUE, (p, basicFileAttributes) ->
			                        p.getFileName().toString().startsWith("metadata_sample_")).findFirst().orElse(null);
					if(sampleInfo != null) {
						
						Collection<String>toCompress = new TreeSet<String>();
						Collection<String>processed = new TreeSet<String>();
						TreeMap<File,Long> fileSizeMap = new TreeMap<File,Long>();
						TreeMap<File,String> fileHashMap = new TreeMap<File,String>();
						File manifest = Paths.get(zippedRawFilesDirPath.toString(), "manifest_" + assay + ".txt").toFile();
						File checkSumFile = Paths.get(zippedRawFilesDirPath.toString(), " checksum.txt").toFile();
						FileUtils.copyFile(sampleInfo.toFile(), manifest);						
						String[][] manifestData = null;
						try {
							manifestData = DelimitedTextParser.parseTextFileWithEncoding(manifest, MRC2ToolBoxConfiguration.getTabDelimiter());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (manifestData != null) {
							String[] header = manifestData[0];
							int fileNameColumn = -1;
							for(int i=0; i<header.length; i++) {
								if(header[i].equals("raw_file")) {
									fileNameColumn = i;
									break;
								}
							}							
							for(int i=1; i<manifestData.length; i++) 
								toCompress.add(manifestData[i][fileNameColumn]);
							
							System.out.println(sampleInfo.getFileName().toString());
						}
						Path rawDataDirectory = Paths.get(rawBaseDir.getAbsolutePath(), tissue, assay);
						List<Path> pathList = Files.find(rawDataDirectory,
								1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
							collect(Collectors.toList());
						for(Path rdp : pathList) {
							
							FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
							String rawFileName = FilenameUtils.getBaseName(rdp.toString());
							if(toCompress.contains(rawFileName)) {
								
								File destination = Paths.get(zippedRawFilesDirPath.toString(),
										FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
								CompressionUtils.zipFolder(rdp.toFile(), destination);
								if(destination.exists()) {
									try {
										String zipHash = DigestUtils.sha256Hex(
												new FileInputStream(destination.getAbsolutePath()));

										fileHashMap.put(destination, zipHash);
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									fileSizeMap.put(destination, destination.length());
									processed.add(rawFileName);
								}								
							}
						}
						@SuppressWarnings({ "unchecked" })
						Collection<String> missing = CollectionUtils.removeAll(toCompress, processed);
						if(!missing.isEmpty()) {
							System.out.println("Missing files in " + zippedRawFilesDirPath.toString());
							for(String m : missing)
								System.out.println(m);
						}					
						//	Write checksum file
						StringBuffer checkSumData = new StringBuffer();
						checkSumData.append("File name\t");
						checkSumData.append("SHA256\t");
						checkSumData.append("Size\n");
						for(Entry<File, String> entry : fileHashMap.entrySet()) {
							
							if(fileSizeMap.get(entry.getKey()) != null) {
								
								checkSumData.append(entry.getKey().getName() +"\t");
								checkSumData.append(entry.getValue() +"\t");
								checkSumData.append(Long.toString(fileSizeMap.get(entry.getKey())) + "\n");
							}
						}
						FileUtils.writeStringToFile(checkSumFile, checkSumData.toString(), Charset.defaultCharset());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void fixMetaboliteNames() throws Exception {
		
		File sourceDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix");
		String destinationDirName = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed";
		IOFileFilter metNameFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter("^metadata_metabolites_named_.+\\.(txt)|(TXT)$"));
		Collection<File> metNamefiles = FileUtils.listFiles(
				sourceDirectory,
				metNameFilter,
				null);
		
		Path bucketFileListing = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metadata_metabolites_named_listing.txt");
		List<String>bucketAddresses = Files.readAllLines(bucketFileListing);
		List<String>copyCommands = new ArrayList<String>();		
		String[]lipidsWithNoIsomers = new String[] {"LPC(15:0)", "MG(14:0)", "LPC(18:1)", "LPC(17:0)",};
		if (!metNamefiles.isEmpty()) {

			for(File mnf : metNamefiles) {
				
				boolean hasChanged = false;
				//	Read file and replace names if necessary
				Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
						parseMetaboliteMetadataFile(mnf);
				
				for(Map<MoTrPACmetaboliteMetaDataFields, String> mData : metaboliteMetadata) {
					
					String mName = mData.get(MoTrPACmetaboliteMetaDataFields.METABOLITE_NAME);
					for(String lipid : lipidsWithNoIsomers) {
						
						if(mName.contains(lipid) && !mName.equals(lipid)) {
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, lipid);
							hasChanged = true;
						}
					}
					if(mName.endsWith("_a")) {
						
						if(mName.endsWith("_rp_a"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName);
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_a", "_rp_a"));
						
						hasChanged = true;
					}
					if(mName.endsWith("_b")) {
						
						if(mName.endsWith("_rp_b"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName);
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_b", "_rp_b"));
						
						hasChanged = true;
					}
					if(mName.endsWith("_a_b")) {
						if(mName.endsWith("_rp_a_b"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_rp_a_b", ""));
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_a_b", ""));
						hasChanged = true;
					}
					if(mName.equals("Car(5:0) isomers")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_a_b");
						hasChanged = true;
					}
				}			
				if(hasChanged) {
					Path newFilePath = Paths.get(destinationDirName, mnf.getName());
					try {
						writeMetaboliteMetadataFile(metaboliteMetadata, newFilePath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String fName = mnf.getName();
					String bucketAddres = bucketAddresses.stream().
							filter(l -> l.contains(fName)).
							findFirst().orElse(null);
					
					if(bucketAddres != null) {
						copyCommands.add("gsutil cp \"" + newFilePath.toString() + "\" " + bucketAddres.replace("PROCESSED_20191008", "PROCESSED_20210629"));
					}
				}
			}
		}
		Path copyCommandsFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed\\copyCommands.txt");
		try {
			Files.write(copyCommandsFilePath, 
					copyCommands, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void fixMetaboliteNamesStepTwo() throws Exception {
		
		File sourceDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix");
		String destinationDirName = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed";
		IOFileFilter metNameFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter("^metadata_metabolites_named_.+\\.(txt)|(TXT)$"));
		Collection<File> metNamefiles = FileUtils.listFiles(
				sourceDirectory,
				metNameFilter,
				null);
		
		Path bucketFileListing = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metadata_metabolites_named_listing.txt");
		List<String>bucketAddresses = Files.readAllLines(bucketFileListing);
		List<String>copyCommands = new ArrayList<String>();		
		if (!metNamefiles.isEmpty()) {

			for(File mnf : metNamefiles) {
				
				boolean hasChanged = false;
				//	Read file and replace names if necessary
				Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
						parseMetaboliteMetadataFile(mnf);
				
				for(Map<MoTrPACmetaboliteMetaDataFields, String> mData : metaboliteMetadata) {
					
					String refmetOld = mData.get(MoTrPACmetaboliteMetaDataFields.REFMET_NAME);
					if(refmetOld.equalsIgnoreCase("CAR(3:0(2Me))")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)_rp_a");
						hasChanged = true;
					}				
					if(refmetOld.equalsIgnoreCase("Car(5:0)")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_b");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("Car(5:0) isomers") || refmetOld.equals("CAR(5:0)_rp_a_b")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)");
						hasChanged = true;
					}
					
					if(refmetOld.equalsIgnoreCase("Car(4:0)")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)_rp_b");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("Car(4:0) isomers") || refmetOld.equals("CAR(4:0)_rp_a_b")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("CAR(4:0(3Me))")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_a");
						hasChanged = true;
					}
				}			
				if(hasChanged) {
					Path newFilePath = Paths.get(destinationDirName, mnf.getName());
					try {
						writeMetaboliteMetadataFile(metaboliteMetadata, newFilePath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String fName = mnf.getName();
					String bucketAddres = bucketAddresses.stream().
							filter(l -> l.contains(fName)).
							findFirst().orElse(null);
					
					if(bucketAddres != null) {
						copyCommands.add("gsutil cp \"" + newFilePath.toString() + "\" " + bucketAddres.replace("PROCESSED_20191008", "PROCESSED_20210629"));
					}
				}
			}
		}
		Path copyCommandsFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed\\copyCommands.txt");
		try {
			Files.write(copyCommandsFilePath, 
					copyCommands, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void updateMotrPacRefSampleAssignment() throws Exception {
		
		//	Read list of files
		Path listPath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RefSampleCorrection\\PASS1A-06_sample_metadata_file_list.txt");
		String tmpDir = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RefSampleCorrection\\TMP";
		String gsUtilBinary = "C:\\Users\\Sasha\\AppData\\Local\\Google\\Cloud SDK\\google-cloud-sdk\\bin\\gsutil.cmd";
		List<String>sampleMetaDataList = new ArrayList<String>();
		try {
			sampleMetaDataList = Files.readAllLines(listPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		for (String bucketPath : sampleMetaDataList) {

			Path tmpFilePath = Paths.get(tmpDir, "sample_metadata.txt").toAbsolutePath();
			Path correctedTmpFilePath = Paths.get(tmpDir, "sample_metadata_corrected.txt");
			ProcessBuilder pb = new ProcessBuilder(
					gsUtilBinary, "cp", bucketPath, tmpFilePath.toString());
			try {
				Process p = pb.start();
				int exitCode = p.waitFor();
				if (exitCode == 0) {
					p.destroy();
					List<String> sampleDataLines = new ArrayList<String>();
					if(tmpFilePath.toFile() != null && tmpFilePath.toFile().exists())
						sampleDataLines  = Files.readAllLines(tmpFilePath, Charset.forName("ISO-8859-1"));
					
					boolean corrected = false;
					List<String> correctedSampleDataLines = new ArrayList<String>();
					for(String sdl : sampleDataLines) {
						
						if(sdl.startsWith("CS0UM")) {
							correctedSampleDataLines.add(sdl.replace("QC-Reference", "QC-ReCAS"));
							corrected = true;
						}
						else {
							correctedSampleDataLines.add(sdl);
						}
					}
					if(corrected) {						
					    try {
							Files.write(correctedTmpFilePath, 
									correctedSampleDataLines, 
									StandardCharsets.UTF_8,
									StandardOpenOption.CREATE, 
									StandardOpenOption.TRUNCATE_EXISTING);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					    
					    //	Upload corrected file 
					    if(correctedTmpFilePath.toFile() != null && correctedTmpFilePath.toFile().exists()) {
							pb = new ProcessBuilder(
									gsUtilBinary, "cp", correctedTmpFilePath.toString(), bucketPath);
							p = pb.start();
							exitCode = p.waitFor();
							if (exitCode == 0) {
								System.out.println("Updated " + bucketPath);
							}
					    }
					    //	Delete corrected file					    
					    Files.delete(correctedTmpFilePath);
					}
					Files.delete(tmpFilePath);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private static void compressRawDataFromEX01190() {
		
		String includeFileListFile = 
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\Interm\\T02\\IONPNEG\\BATCH1_20230124\\RAW\\EX01190-IONPNEG-file-list.txt"; 
		
		List<String>sourceFolders = new ArrayList<String>();
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-01");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-02");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-03");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-04");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-05");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-06");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-07");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-08");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-09");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-10");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-11");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-12");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Raw data\\NEG\\BATCH-13");
		
		String destinationFolder = 
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\T02\\IONPNEG\\BATCH1_20230124\\RAW";
		
		compressRawDataFromMutibatchRun(
				includeFileListFile, 
				sourceFolders, 
				destinationFolder);
	}
	
	public static void extractRefmetDisrepanciesForAllExperiments() {
		
		File logLocationList = new File("E:\\DataAnalysis\\_MOTRPAC_DEVEL\\QC-log-locations.txt");
		File output = new File("E:\\DataAnalysis\\_MOTRPAC_DEVEL\\RefmetDisrepancies.txt");
		extractRefmetDisrepancies(logLocationList, output);
	}
	
	public static void extractRefmetDisrepancies(File logLocationList, File output) {
		
		List<String> logLocations = new ArrayList<String>();
		List<File>qcLogFiles = new ArrayList<File>();
		Set<String>refmetDiscrepancies = new TreeSet<String>();
		try {
			logLocations = Files.readAllLines(logLocationList.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String ll : logLocations) {
			
			File logDir = new File(ll);
			if(logDir.exists()) 				
				qcLogFiles.addAll(Arrays.asList(logDir.listFiles()));			
		}
		List<String> loglines = new ArrayList<String>(); 
		List<String> refmetErrors = new ArrayList<String>(); 
		for(File qcLog : qcLogFiles) {
			
			loglines.clear();
			refmetErrors.clear();
			try {
				loglines = Files.readAllLines(qcLog.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refmetErrors = loglines.stream().
					filter(l -> l.contains("(-) `refmet_name`")).
					collect(Collectors.toList());
			if(!refmetErrors.isEmpty()) {
				
				for(String refmetError : refmetErrors) {
					
					String line = refmetError.replace("(-) `refmet_name`", "").
						replaceAll("\\[`", "").replaceAll("`\\]\\s+", "\t").replaceAll("\\(Error.+\\)", "").trim();
					refmetDiscrepancies.add(line);
				}				
			}
		}
		try {
		    Files.write(output.toPath(), 
		    		refmetDiscrepancies,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}

















