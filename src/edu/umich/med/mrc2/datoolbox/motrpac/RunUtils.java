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

package edu.umich.med.mrc2.datoolbox.motrpac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.motrpac.MoTrPACUtils.AssayModes;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.LIMSReportingUtils;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class RunUtils {


	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			addExtractionDatesToManifestForEX01496IONPNeg();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	private static void addExtractionDatesToManifestForEX01496IONPNeg() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01496-IONP-NEG-Manifest-complete-filtered-20261020_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01496-IONP-NEG-raw-file-list-with-blanks.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\Manifests");            
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01496-IONP-NEG-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01496-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.IONPNEG;
        int nextColumnId = 38;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void createUniqueSampleIdsForControlsAndOrderSamplesEX01496IONPNeg() {
		File runManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01496-IONP-NEG-Manifest-complete-filtered-20261020.txt");
		MoTrPACUtils.createUniqueSampleIdsForControlsAndOrderSamples(runManifestFile);
	}
	
	private static void correctEX01496ionpnegBatch7fileNames() {
		
		File proFinderDataFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A049 - Central carbon metabolism profiling\\CompoundMatch\\Profinder exports\\EX01496-IP Neg-B07-Custom PCDL results.csv");
		File renameMapFile = new File ("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01496-IONP-NEG-B7-file-rename_map.txt");
		File proFinderRenamedDataFile = new File("S:\\DataAnalysis\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A049 - Central carbon metabolism profiling\\CompoundMatch\\Profinder exports\\EX01496-IP Neg-B07-Custom PCDL results-batch7renamed.csv");
		
		String[][]proFinderData = DelimitedTextParser.parseCSVFile(proFinderDataFile);
		String[][] renameMapData = DelimitedTextParser.parseTabSeparatedFile(renameMapFile);
		Map<String,String>renameMap = new TreeMap<>();
		for(int i=0; i<renameMapData.length; i++)
			renameMap.put(renameMapData[i][0], renameMapData[i][1]);

		for(int i=0; i<proFinderData[0].length; i++) {
			
			for(Entry<String,String>me : renameMap.entrySet()) {
				
				if(proFinderData[0][i].contains(me.getKey()))					
					proFinderData[0][i] = proFinderData[0][i].replace(me.getKey(), me.getValue());
				
			}
		}
		TextUtils.write2DstringArrayToCSV(proFinderData, proFinderRenamedDataFile);
	}
	
	private static void addExtractionDatesToManifestForEX01496RpNeg() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01496-RP-NEG-Manifest-complete_uniqueControls_filtered4named.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01496-RP-NEG-raw-file-list-with-blanks.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\Manifests");        
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01496-RP-NEG-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01496-RP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPNEG;
        int nextColumnId = 43;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void createUniqueSampleIdsForControlsAndOrderSamplesEX01496RpNeg() {
		
		File runManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A003 - Untargeted\\Documents\\NEG\\Alignment\\NormalCutoff\\All\\MetabCombinerMultiBatchAlignment-20261016_153944\\MergedManifest.txt");
		MoTrPACUtils.createUniqueSampleIdsForControlsAndOrderSamples(runManifestFile);
	}
	
	private static void addExtractionDatesToManifestForEX01496RpPos() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01496-RP-POS-Manifest-complete_uniqueControls_filtered4named.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01496-RP-POS-raw-file-list-with-blanks.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\Documents\\POS\\Alignment\\NormalCutoff\\All\\Manifests");        
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01496-RP-POS-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01496-RP-POS-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPPOS;
        int nextColumnId = 43;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void createUniqueSampleIdsForControlsAndOrderSamplesEX01387RpPos() {
		
		File runManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
				+ "A003 - Untargeted\\Documents\\POS\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_130924\\MergedCleanManifest.txt");
		MoTrPACUtils.createUniqueSampleIdsForControlsAndOrderSamples(runManifestFile);
	}
	
	private static void addExtractionDatesToManifestForEX01387RpPos() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\POS\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_130924\\MergedCleanManifest_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\POS\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_130924\\EX01387_RP_POS_raw_file_list.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\POS\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_130924\\Manifests");        
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\POS\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_130924\\Manifests\\EX01387-RP-POS-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\POS\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_130924\\Manifests\\EX01387-RP-POS-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPPOS;
        int nextColumnId = 36;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void createUniqueSampleIdsForControlsAndOrderSamplesEX01387RpNeg() {
		
		File runManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
				+ "A003 - Untargeted\\Documents\\NEG\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_121447\\MergedCleanManifest.txt");
		MoTrPACUtils.createUniqueSampleIdsForControlsAndOrderSamples(runManifestFile);
	}

	private static void addExtractionDatesToManifestForEX01387RpNeg() {
        
        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\NEG\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_121447\\MergedCleanManifest_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\NEG\\EX01387_RP_NEG_raw_file_list.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\NEG\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_121447\\Manifests");        
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\NEG\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_121447\\Manifests\\EX01387-RP-NEG-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A003 - Untargeted\\Documents\\NEG\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_121447\\Manifests\\EX01387-RP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPNEG;
        int nextColumnId = 36;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void compressRawDataFromEX01387RpNeg() {

		String includeFileListFile = 
				"Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
				+ "A003 - Untargeted\\Documents\\NEG\\4Alignment\\MetabCombinerMultiBatchAlignment-20261012_121447\\EX01387_RP_NEG_raw_file_list.txt"; 
		
		List<String>sourceFolders = new ArrayList<String>();
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\A003 - Untargeted\\Raw data\\NEG\\BATCH01-LC8");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\A003 - Untargeted\\Raw data\\NEG\\BATCH02\\V2");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\A003 - Untargeted\\Raw data\\NEG\\BATCH03");
		sourceFolders.add(
				"Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\A003 - Untargeted\\Raw data\\NEG\\BATCH04");				
		String destinationFolder = 
				"Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\4BIC_UPLOAD\\HUMAN\\T11\\RPNEG\\BATCH1_20260306\\RAW";
		
		MoTrPACUtils.compressRawDataFromMutibatchRun(
				includeFileListFile, 
				sourceFolders, 
				destinationFolder);
	}
	
	private static void createMoTrPACFileManifests1387() {
		
		String tissue ="T11";
//		List<String>assayTypes = new ArrayList<>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		String assay = "IONPNEG";
		String batchId = "BATCH1_20260306";
		String processedFolderId = "PROCESSED_20260306";
		String manifestDate = "20260513";
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\4BIC_UPLOAD\\HUMAN");		
		try {
			MoTrPACUtils.createMoTrPACTissueAssayManifestFile(
					tissue,
					assay,
					parentDirectory,
					batchId,
					processedFolderId,
					manifestDate,
					false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createMoTrPACFileManifests1426() {
		
		String tissue ="T02";
//		List<String>assayTypes = new ArrayList<>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		String assay = "IONPNEG";
		String batchId = "BATCH3_20260306";
		String processedFolderId = "PROCESSED_20260306";
		String manifestDate = "20260519";
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\4BIC_UPLOAD\\HUMAN");		
		try {
			MoTrPACUtils.createMoTrPACTissueAssayManifestFile(
					tissue,
					assay,
					parentDirectory,
					batchId,
					processedFolderId,
					manifestDate,
					false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void addExtractionDatesToManifestForEX01426IonpNeg() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01426-IONP-NEG-Manifest-complete_uniqueControls.txt");        
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01426-IONP-NEG-raw-file-list-with-blanks.txt");      
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\Manifests");
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01426-IONP-NEG-extraction-dates-list.txt");        
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01426-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.IONPNEG;
        int nextColumnId = 36;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01426RNeg() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01426-RP-NEG-Manifest-complete_uniqueControls.txt");             
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01426-RP-NEG-raw-file-list.txt");             
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\Manifests");       
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01426-RP-NEG-extraction-date-list.txt");        
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01426-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPNEG;
        int nextColumnId = 40;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01426RPOS() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01426-RP-POS-Manifest-complete_uniqueControls.txt");             
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01426-RP-POS-raw-file-list.txt");             
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\Manifests");       
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01426-RP-POS-extraction-date-list.txt");        
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01426-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPNEG;
        int nextColumnId = 40;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01387IonpNeg() {

        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "4QCANVAS\\IONP-NEG\\EX01387-IONP-NEG-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\EX01387-IONP-NEG-raw-file-list-wblanks.txt");      
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "4QCANVAS\\IONP-NEG\\Manifests");        
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\EX01387-IONP-NEG-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\EX01387-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.IONPNEG;
        int nextColumnId = 36;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }

	private static void addExtractionDatesToManifestForEX01355RpNeg() {
        
        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-NEG\\Unnamed\\EX01355-RP-NEG-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-NEG\\EX01355-RP-NEG-raw-file-list.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-NEG\\Manifests");        
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-NEG\\EX01355-RP-NEG-extraction-dates-list.txt");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-NEG\\EX01355-RP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPNEG;
        int nextColumnId = 35;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01355RpPos() {
        
        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-POS\\Unnamed\\EX01355-RP-POS-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-POS\\EX01355-RP-POS-raw-file-list.txt");
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-POS\\EX01355-RP-POS-extraction-dates-list.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-POS\\Manifests");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A003 - Untargeted\\QCaNVaS_v0.8.0\\RP-POS\\EX01355-RP-POS-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPPOS;
        int nextColumnId = 35;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01355IonPNeg() {
		
        File completeManifestFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\Unnamed\\EX01355-IONP-NEG-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\EX01355-IONP-NEG-raw-file-list.txt");
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\EX01355-IONP-NEG-extraction-dates-list.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\Manifests");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01355 - Human Muscle Tranche 1 H20000820Y\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS_v0.8.0\\EX01355-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.IONPNEG;
        int nextColumnId = 35;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01409RpNeg() {
        
        File completeManifestFile = 
        		new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01409-RP-NEG-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01409-RP-NEG-raw-file-list.txt");
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        + "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01409-RP-NEG-extractionDatesMap.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\Manifests");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-NEG\\EX01409-RP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPNEG;
        int nextColumnId = 38;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01409RpPos() {
        
        File completeManifestFile = 
        		new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        				+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01409-RP-POS-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01409-RP-POS-raw-file-list.txt");
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01409-RP-POS-extractionDatesMap.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\Manifests");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A003 - Untargeted\\QCaNVaS\\RP-POS\\EX01409-RP-POS-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.RPPOS;
        int nextColumnId = 38;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void addExtractionDatesToManifestForEX01409IonPNeg() {
        
        File completeManifestFile = 
        		new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        				+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01409-IONP-NEG-Manifest-complete_uniqueControls.txt");
        File commonFileListFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01409-IONP-NEG-raw-file-list.txt");
        File extrtactionDatesMapFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01409-IONP-NEG-extractionDatesMap.txt");
        File batchManifestsBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\Manifests");
        File columnChangeDatesFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
        		+ "A049 - Central carbon metabolism profiling\\QCaNVaS\\EX01409-IONP-NEG-columnChangeTimes.txt");
        AssayModes assayMode = AssayModes.IONPNEG;
        int nextColumnId = 38;
        
        MoTrPACUtils.addExtractionDatesAndColumnIdsToManifest(
        		completeManifestFile, 
        		commonFileListFile,
        		extrtactionDatesMapFile,
        		batchManifestsBaseDir,
        		columnChangeDatesFile,
        		assayMode,
        		nextColumnId);
    }
	
	private static void renameProcessedDirs4EX01263() {
		
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20241023FIX\\PASS1A-18");
		String oldName = "PROCESSED_20241101";
		String newName = "PROCESSED_20241101";
		MoTrPACUtils.renameProcessedDirs(destinationDir, oldName, newName);
	}
	
	private static void renameProcessedDirs4EX01242() {
		
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\20241017FIX\\HUMAN\\T11");
		String oldName = "PROCESSED_20220926";
		String newName = "PROCESSED_20241031";
		MoTrPACUtils.renameProcessedDirs(destinationDir, oldName, newName);
	}
	
	private static void renameProcessedDirs4EX01117() {
		
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01117 - MoTrPAC-1C\\4BIC\\20241023FIX\\PASS1A-06");
		String oldName = "PROCESSED_20220926";
		String newName = "PROCESSED_20241031";
		MoTrPACUtils.renameProcessedDirs(destinationDir, oldName, newName);
	}	
	
	private static void createMultiTissueFixScriptGenInputMapForEX01263(){
		
		File partialInput = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\fixInputInterm.txt");
		File bicDataDir = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\20241023FIX\\PASS1A-18");
		File outputFile = new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\fixInputComplete.txt");
		
		MoTrPACUtils.createMultiTissueFixScriptGenInputMap(partialInput, bicDataDir, outputFile);
	}

	
	private static void getFulPathsToQcanvasDirsForEX01263() {
		
		File dirNamesFile = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\QCANVASdirList.txt");
		File lookupDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN");
		File outputFile = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-interm\\QCANVAS\\MAIN\\QCANVASdirMap.txt");
		
		MoTrPACUtils.getFulPathsToQcanvasDirs(dirNamesFile, lookupDir, outputFile);
	}
	
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PASS1A18() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\PASS1A-18-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20241023FIX\\PASS1A-18");
		String processingDateIdentifier = "20241101";
		
		MoTrPACUtils.duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}	
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PASS1A06() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS06-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS06-20241017");
		String processingDateIdentifier = "20241017";
		
		MoTrPACUtils.duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PreCovidAdipose() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\HUMAN\\T11-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\HUMAN\\T11-20241031");
		String processingDateIdentifier = "20241031";
		
		MoTrPACUtils.duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}	
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PreCovidMuscle() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01094-tmp\\T06-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01094-tmp\\T06-20241017");
		String processingDateIdentifier = "20241017";
		
		MoTrPACUtils.duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}
	
	private static void duplicateDataUploadDirectoryForResultsCorrection4PreCovidPlasma() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\HUMAN\\T02-FromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\HUMAN\\T02-20241031");
		String processingDateIdentifier = "20241031";
		
		MoTrPACUtils.duplicateDataUploadDirectoryForResultsCorrection(
				sourceDir,
				destinationDir,
				processingDateIdentifier);
	}
	
	private static void duplicateDataUploadDirectoryForResultsCorrectionNoTruncExcludeOldBatch4Pass1A06() {
		
		File sourceDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1A-06-fromBIC");
		File destinationDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\20241023FIX\\PASS1A-06");
		String processingDateIdentifier = "20241030";
		String excludeBatchIdentifier = "BATCH1_20190909";
		
		MoTrPACUtils.duplicateDataUploadDirectoryForResultsCorrectionNoTruncExcludeOldBatch(
				sourceDir,
				destinationDir,
				processingDateIdentifier,
				excludeBatchIdentifier);
	}
	
	private static void createMoTrPACFileManifests1263Heart() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T31"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20251016FIX\\PASS1A-18");
		String batchDateIdentifier = "BATCH1_20230710";
		String processingDateIdentifier = "PROCESSED_20230710";
		MoTrPACUtils.createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20251112",
				false);
	}
	
	private static void createMoTrPACFileManifests4Pass1A18() {
		
		List<String>tissueTypes = Arrays.asList(
				"T31",
				"T55",
				"T58",
				"T59",
				"T66",
				"T68",
				"T69",
				"T70");
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\20251016FIX\\PASS1A-18");
		String batchDateIdentifier = "BATCH1_20230710";
		String processingDateIdentifier = "PROCESSED_20230710";
		MoTrPACUtils.createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20251112",
				false);
	}
	
	
	private static void createMotrpacDataUploadDirectoryStructure4PreCovidAdipose() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T11"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\4BIC\\Interm");
		String batchDateIdentifier = "20230202";
		String processingDateIdentifier = "20230202";
		String studyPhase = "HUMAN-PRECOVID";
		MoTrPACUtils.createMotrpacDataUploadDirectoryStructure(
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
		MoTrPACUtils.createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230617",
				false);
	}
	
	private static void createMoTrPACFileManifestsEX01387Adipose() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T11"));
		File parentDirectory = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01387 - Human Post Suspension Adipose powder H20000902J\\4BIC_UPLOAD\\HUMAN");
		String batchDateIdentifier = "BATCH1_20260306";
		String processingDateIdentifier = "PROCESSED_20260306";
		MoTrPACUtils.createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20260511",
				false);
	}
	
	private static void createMotrpacDataUploadDirectoryStructure4PreCovidPlasma() {
		
		List<String>tissueTypes = new ArrayList<String>(Arrays.asList("T02"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\Interm2");
		String batchDateIdentifier = "20230124";
		String processingDateIdentifier = "20241016";
		String studyPhase = "HUMAN-PRECOVID";
		MoTrPACUtils.createMotrpacDataUploadDirectoryStructure(
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
		MoTrPACUtils.createMotrpacDataUploadDirectoryStructure(
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
		MoTrPACUtils.createMoTrPACFileManifest(
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
		MoTrPACUtils.createMoTrPACFileManifest(
				tissueTypes, 
				parentDirectory,
				batchDateIdentifier,
				processingDateIdentifier,
				"20230616",
				false);
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
		
		List<String>tissueTypes = new ArrayList<>(
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
				new ArrayList<>(Arrays.asList("IONPNEG" ,"RPNEG", "RPPOS"));
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
		
		MoTrPACUtils.compressRawDataFromMutibatchRun(
				includeFileListFile, 
				sourceFolders, 
				destinationFolder);
	}
	
	public static void extractRefmetDisrepanciesForAllExperiments() {
		
		File logLocationList = new File("E:\\DataAnalysis\\_MOTRPAC_DEVEL\\QC-log-locations.txt");
		File output = new File("E:\\DataAnalysis\\_MOTRPAC_DEVEL\\RefmetDisrepancies_20241031_2.txt");
		MoTrPACUtils.extractRefmetDisrepancies(logLocationList, output);
	}
}
