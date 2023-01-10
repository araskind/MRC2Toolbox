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

package edu.umich.med.mrc2.datoolbox.misctest;

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
import edu.umich.med.mrc2.datoolbox.utils.LIMSReportingUtils;

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
		File parentDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1A-06\\_FINALS");
		try {
			createMoTrPACTissueAssayNoRawManifestFile(
					"T70 - Adipose white",
					"IONPNEG",
					parentDir,
					"BATCH1_20210603",
					"PROCESSED_20220926",
					"20221019");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createMotrpacDataUploadDirectoryStructure4PreCovidMuscle() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T06"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01094 - ADU870 10063 Clinical "
				+ "Skeletal Muscle Samples Pre COVID 2 19 2021\\4Upload\\_4BIC");
		String batchDateIdentifier = "20220404";
		String processingDateIdentifier = "20220404";
		createMotrpacDataUploadDirectoryStructure(
				tissueTypes, 
				 parentDirectory,
				 batchDateIdentifier,
				 processingDateIdentifier);
	}
	
	private static void createMoTrPACFileManifests4PreCovidMuscle() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T06"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01094 - MoTrPAC Muscle "
						+ "PreCOVID-20210219\\4Upload\\_4BIC\\HUMAN");
		String batchDateIdentifier = "BATCH1_20220404";
		String processingDateIdentifier = "PROCESSED_20220404";
		createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20220208");
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
	
	private static void createMotrpacDataUploadDirectoryStructure(
			List<String>tissueTypes, 
			File parentDirectory,
			String batchDateIdentifier,
			String processingDateIdentifier) {
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		try {
			LIMSReportingUtils.createMotrpacDataUploadDirectoryStructure(
					tissueTypes, 
					assayTypes, 
					parentDirectory,
					1,
					batchDateIdentifier,
					processingDateIdentifier);
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
			LIMSReportingUtils.createMotrpacDataUploadDirectoryStructure(
					tissueTypes, 
					assayTypes, 
					parentDirectory,
					1,
					"20210603",
					"20220926");
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
	
	private static void createMoTrPACFileManifest(
			List<String>tissueTypes, 
			File parentDirectory,
			String batchId,
			String processedFolderId,
			String manifestDate) {
		List<String>assayTypes = 
				new ArrayList<String>(Arrays.asList("IONPNEG" ,"RPNEG", "RPPOS"));
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					createMoTrPACTissueAssayManifestFile(
							tissue,
							assay,
							parentDirectory,
							batchId,
							processedFolderId,
							manifestDate);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				StringBuffer checkSumData = new StringBuffer();
//				checkSumData.append("file_name,md5\n");
//				Path processedPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, processedFolderId);
//				Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
//				List<Path> pathList = Files.find(namedDirPath,
//						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
//					collect(Collectors.toList());
//				for(Path filePath : pathList) {
//					String zipHash = DigestUtils.sha256Hex(
//							new FileInputStream(filePath.toString()));
//					String localPath = processedFolderId + File.separator + "NAMED" + File.separator + filePath.toFile().getName();
//					checkSumData.append(localPath + "," + zipHash + "\n");
//				}
//				Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
//				pathList = Files.find(unnamedDirPath,
//						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
//					collect(Collectors.toList());
//				for(Path filePath : pathList) {
//					String zipHash = DigestUtils.sha256Hex(
//							new FileInputStream(filePath.toString()));
//					String localPath = processedFolderId + File.separator + "UNNAMED" + File.separator + filePath.toFile().getName();
//					checkSumData.append(localPath + "," + zipHash + "\n");
//				}
//				pathList = Files.find(processedPath,
//						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
//					collect(Collectors.toList());
//				for(Path filePath : pathList) {
//					String zipHash = DigestUtils.sha256Hex(
//							new FileInputStream(filePath.toString()));
//					String localPath = processedFolderId + File.separator + filePath.toFile().getName();
//					checkSumData.append(localPath + "," + zipHash + "\n");
//				}
//				Path rawChecksumPathPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW", " checksum.txt");
//				List<String> zipCs = Files.readAllLines(rawChecksumPathPath);
//				for(int i=1; i<zipCs.size(); i++) {
//					String[]parts = zipCs.get(i).split("\t");
//					checkSumData.append(parts[0].replace(".zip", "") + "," + parts[1] + "\n");
//				}
//				File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "file_manifest_20220208.csv").toFile();
//				FileUtils.writeStringToFile(manifestFile, checkSumData.toString(), Charset.defaultCharset());
			}
		}
	}
	
	public static void createMoTrPACTissueAssayManifestFile(
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
		File manifestFile = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "file_manifest_" + manifestDate + ".csv").toFile();
		FileUtils.writeStringToFile(manifestFile, checkSumData.toString(), Charset.defaultCharset());
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
					MoTrPACmetaboliteMetaDataFields.getMoTrPACmetaboliteMetadataFieldByName(header[i].trim());
			
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
}
