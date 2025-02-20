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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class RQCScriptGenerator {

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			createDataSummariesForEX01426B6rpPosEMvoltageExperiment();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createDataSummariesForEX01426B6rpPosEMvoltageExperiment() {
		
		File inputMapFile = 
				new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents\\EX01426_RP-POS-EM-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents");
		createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}

			
	private static void createMultyBatchDataSummarizationScript(
			File inputMapFile, 
			File dataDir) {
		
		List<String>qcSummaryNames = new ArrayList<String>();
		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = dataDir.getAbsolutePath().replaceAll("\\\\", "/");
		String[][] inputData = DelimitedTextParser.parseTextFile(
				inputMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		List<String>header = Arrays.asList(inputData[0]);
		for(SummaryInputColumns field : SummaryInputColumns.values()) {
			
			if(!header.contains(field.name())) {
				
				System.err.println(field.name() + " missing in data input map file!");
				return;
			}
		}
		List<SummarizationDataInputObject>dataInputList = 
				new ArrayList<SummarizationDataInputObject>();
		for(int i = 1; i<inputData.length; i++) {
			
			SummarizationDataInputObject io = new SummarizationDataInputObject();
			for(int j = 0; j<inputData[0].length; j++) {
			
				SummaryInputColumns field = SummaryInputColumns.getOptionByName(inputData[0][j]);
				String value = inputData[i][j];
				if(value == null || value.isEmpty()) {
					
					System.err.println(field.name() + 
							" missing in data input map file on line " + Integer.toString(i+ 1) + "!");
					return;
				}
					
				io.setField(field, value);			
			}
			dataInputList.add(io);			
		}
		Map<SummaryInputColumns,Set<String>>feildVariationMap = 
				new TreeMap<SummaryInputColumns,Set<String>>();
		for(SummaryInputColumns field : SummaryInputColumns.values()) {
			
			if(field.isFactor()) {
				
				Set<String>fieldValues = new TreeSet<String>();
				for(SummarizationDataInputObject io : dataInputList) {
					
					String value = io.getField(field);
					if(io != null)
						fieldValues.add(value);
				}
				feildVariationMap.put(field, fieldValues);
			}
		}
		List<SummaryInputColumns>nonRedundantFields = new ArrayList<SummaryInputColumns>();
		feildVariationMap.entrySet().stream().
			filter(e -> e.getValue().size() > 1).
			forEach(e -> nonRedundantFields.add(e.getKey()));
		List<SummaryInputColumns>commonFields = new ArrayList<SummaryInputColumns>();
		feildVariationMap.entrySet().stream().
			filter(e -> e.getValue().size() == 1).
			forEach(e -> commonFields.add(e.getKey()));
		
		String prefix = "";
		ArrayList<String>prefixParts = new ArrayList<String>();
		ArrayList<String>nonRedundantParts = new ArrayList<String>();
		
		if(!commonFields.isEmpty()) {
			
			for(SummaryInputColumns cf : commonFields)
				prefixParts.add(dataInputList.get(0).getField(cf));
			
			prefix = StringUtils.join(prefixParts, "_");
		}
//		System.out.println("Prefix: " + prefix);
//		for(SummarizationDataInputObject io : dataInputList) {
//			
//			for(SummaryInputColumns f : nonRedundantFields) {
//				
//				System.out.println(f.name() + " -> " + io.getField(f));
//			}
//			System.out.println("****");
//		}		
		rscriptParts.add("# Untargeted data summarization script " + FIOUtils.getTimestamp() + "####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		
		Map<SummaryInputColumns,List<String>>mergeComponentsMap = 
				new TreeMap<SummaryInputColumns,List<String>>();
		mergeComponentsMap.put(SummaryInputColumns.PEAK_AREAS, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.PEAK_QUALITY, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.MZ_VALUES, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.RT_VALUES, new ArrayList<String>());
		mergeComponentsMap.put(SummaryInputColumns.PEAK_WIDTH, new ArrayList<String>());
		
		for(SummarizationDataInputObject io : dataInputList) {
			
			nonRedundantParts.clear();
			nonRedundantParts.addAll(prefixParts);
			for(SummaryInputColumns nrs : nonRedundantFields)
				nonRedundantParts.add(io.getField(nrs));
			
			rscriptParts.add("\n## Analysis for" + StringUtils.join(nonRedundantParts, ", ") +" ####\n");
			String dataLinePrefix = StringUtils.join(nonRedundantParts, ".");
			String designObject = dataLinePrefix + ".design";
			
			//	Read in design
			rscriptParts.add("### Read common manifest file ####\n");
			rscriptParts.add(designObject + ".in <- read.delim(\"" + 
					io.getField(SummaryInputColumns.MANIFEST) + "\", check.names=FALSE)");
			rscriptParts.add(designObject + " <- as.data.frame(" + designObject + ".in[,2])");
			rscriptParts.add("rownames(" + designObject + ") <- "+ designObject + ".in$`raw_file`");
			rscriptParts.add("colnames(" + designObject + ")[1] <- \"sample_type\"");
			rscriptParts.add(designObject + "$sample_type <- as.factor("+ designObject + "$sample_type)");
			rscriptParts.add("rm(" + designObject + ".in)");
			
			String summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_AREAS, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_AREAS).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_QUALITY, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_QUALITY).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.MZ_VALUES, true);
			mergeComponentsMap.get(SummaryInputColumns.MZ_VALUES).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.RT_VALUES, true);
			mergeComponentsMap.get(SummaryInputColumns.RT_VALUES).add(summaryObject);
			
			summaryObject = createParameterSummaryBlock(
					rscriptParts, dataLinePrefix, designObject,
					nonRedundantFields, io, SummaryInputColumns.PEAK_WIDTH, true);
			mergeComponentsMap.get(SummaryInputColumns.PEAK_WIDTH).add(summaryObject);
		}
		rscriptParts.add("\n# Combine and export summaries ####\n");
		String ts = FIOUtils.getTimestamp();
		for(Entry<SummaryInputColumns,List<String>> me : mergeComponentsMap.entrySet()) {
						
			rscriptParts.add(me.getKey().getRName() + 
					".data.summary <- bind_rows(" + StringUtils.join(me.getValue(), ", ") + ")");
			String summaryFileName = prefix + "_" + me.getKey().getRName() + "_summary_" + ts + ".txt";
			rscriptParts.add("write.table(" + me.getKey().getRName() + ".data.summary, file = \"" + 
					summaryFileName + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
		}
		String rScriptFileName = "UntargetedDataSummarization-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(dataDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static String createParameterSummaryBlock(
			List<String>rscriptParts,
			String dataLinePrefix,
			String designObject,
			List<SummaryInputColumns>nonRedundantFields,
			SummarizationDataInputObject io,
			SummaryInputColumns fieldToSummarize,
			boolean removeExtraControls) {
		
		rscriptParts.add("\n### Summarize " + fieldToSummarize.getName() + " ####\n");
				
		String dataFieldObject = dataLinePrefix + "." + fieldToSummarize.getRName();
		rscriptParts.add(dataFieldObject + ".in <- read.delim(\"" + 
				io.getField(fieldToSummarize) + "\", check.names=FALSE)");
		rscriptParts.add(dataFieldObject + " <- " + dataFieldObject + ".in[,-c(1:9)]");
		rscriptParts.add("rownames(" + dataFieldObject + ") <- " + dataFieldObject + ".in$`Feature name`");
		rscriptParts.add(dataFieldObject + ".t <- as.data.frame(t(" + dataFieldObject + "))");
		rscriptParts.add(dataFieldObject + ".with.design <- merge(" + designObject + ", " + dataFieldObject + ".t, by = 0, all = T)");
		rscriptParts.add(dataFieldObject + ".melt <- melt(" + dataFieldObject + ".with.design[,-1], id = c(\"sample_type\"))");
		
		String fieldSummaryObject = dataLinePrefix + "." + fieldToSummarize.getRName() + ".summary";
		rscriptParts.add(fieldSummaryObject + " <- " + dataFieldObject + ".melt %>% group_by(sample_type, variable) "
				+ "%>% summarise(medianVal = median(value, na.rm = T), meanVal = mean(value, na.rm = T), "
				+ "stDev = sd(value, na.rm = T), pcmissing = 100 * mean(is.na(value)))");			
		rscriptParts.add(fieldSummaryObject + "$RSD <- " + fieldSummaryObject + "$stDev / " + 
				fieldSummaryObject + "$meanVal * 100");
		rscriptParts.add(fieldSummaryObject + "$sample_type <- as.factor(" + fieldSummaryObject + "$sample_type)");
		
		if(removeExtraControls) {
			
			rscriptParts.add(fieldSummaryObject + " <- " + fieldSummaryObject + 
					"[!(" + fieldSummaryObject + "$sample_type %in% c(\"QC-Blank\",\"QC-InternalStandard\",\"QC-Reference\")),]");
		}	
		for(SummaryInputColumns nrf : nonRedundantFields) {
			
			rscriptParts.add(fieldSummaryObject + "$" + nrf.getRName() + " <- \"" + io.getField(nrf) + "\"");
			rscriptParts.add(fieldSummaryObject + "$" + nrf.getRName() + 
					" <- as.factor(" + fieldSummaryObject + "$" + nrf.getRName() + ")");
		}	
		rscriptParts.add("rm(" + dataFieldObject + ", " + dataFieldObject + 
				".in, " + dataFieldObject + ".melt, " + dataFieldObject + ".t, " + dataFieldObject + ".with.design)"); 
		
		return fieldSummaryObject;
	}
	
	private static void generateSummaryQcScriptForEMvoltageExperiment() {
		
		String experimentId = "EX01426-EM-LH";
		File rWorkingDir = new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents");		
		File xlQCfile  = new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents\\EX01426-RP-POS-B6-EM-low-reg.xlsx");		
		File inputMap =  new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents\\EX01426_RP-POS-EM-SummaryQC-inputMap.txt");
		String assayType = "Untargeted";
		String assayType4R = "untarg";

		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}	
	
	private static void generateSummaryQcScriptForEX01426ionpnegMFEparams() {
		
		String experimentId = "EX01426";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\MFEParams");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\MFEParams"
				+ "\\EX01426-IONP-NEG-MFE_CUTOFFS-QC.xlsm");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\"
				+ "MFEParams\\EX01426_IONP-NEG-SummaryQC-inputMap.txt");
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";

		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01426ionpneg() {
		
		String experimentId = "EX01426";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\HighCutoff");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\EX01426-QC-summary-tables - HighCutoff.xlsm");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\HighCutoff\\EX01426_IONP-NEG-SummaryQC-inputMap.txt");
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";

		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01426rpneg() {
		
		String experimentId = "EX01426";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\A003 - Untargeted\\Documents\\HighCutoff\\NEG");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\EX01426-QC-summary-tables - HighCutoff.xlsm");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\Documents\\HighCutoff\\NEG\\EX01426_RP-NEG-SummaryQC-inputMap.txt");
		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01426rppos() {
		
		String experimentId = "EX01426";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\Documents\\HighCutoff\\POS");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\EX01426-QC-summary-tables - HighCutoff.xlsm");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\Documents\\HighCutoff\\POS\\EX01426_RP-POS-SummaryQC-inputMap.txt");
		String assayType = "RP-POS";
		String assayType4R = "rppos";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01414rppos() {
		
		String experimentId = "EX01414";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\POS");
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\EX01414-QC.xlsm");
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\POS\\EX01414-RP-POS-SummaryQC-inputMap.txt");
		String assayType = "RP-POS";
		String assayType4R = "rppos";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01414rpneg() {
		
		String experimentId = "EX01414";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\NEG");
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\EX01414-QC.xlsx");
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\NEG\\EX01414-RP-NEG-SummaryQC-inputMap.txt");
		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01409rppos() {
		
		String experimentId = "EX01409";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01409 - Human EDTA Tranche 1 plasma W20000960M\\A003 - Untargeted\\Documents\\POS");
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\POS\\EX01409_RP-POS-SummaryQC-inputMap.txt");
		String assayType = "RP-POS";
		String assayType4R = "rppos";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01409rpneg() {
		
		String experimentId = "EX01409";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\A003 - Untargeted\\Documents\\NEG");
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\NEG\\EX01409_RP-NEG-SummaryQC-inputMap.txt");
		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01409ionpneg() {
		
		String experimentId = "EX01409";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A049 - Central carbon metabolism profiling\\Documents");
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\EX01409_IONP-NEG-SummaryQC-inputMap.txt");
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		
		generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQCfile,
				inputMap,
				assayType,
				assayType4R);
	}

	private static void generateSummaryQcScript(
			String experimentId,
			File rWorkingDir,
			File xlQCfile,
			File inputMap,
			String assayType,
			String assayType4R) {
		
		List<String>qcSummaryNames = new ArrayList<String>();
		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = rWorkingDir.getAbsolutePath().replaceAll("\\\\", "/");
		String xlFilePath4R = xlQCfile.getAbsolutePath().replaceAll("\\\\", "/");
		String[][] methodListData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		rscriptParts.add("# " + experimentId + " " + assayType + " summary QC ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(readxl)\n");	
		rscriptParts.add("## Feature count and total area summaries ####\n");
		
		for(int i=1; i<=methodListData.length; i++) {
			
			String baseName = assayType4R + ".qcSummary.B" + i;
			rscriptParts.add("\n## " + methodListData[i-1][2] + " ####\n");
			rscriptParts.add(baseName + " <- read_excel(\"" + 
					xlFilePath4R + " \", sheet = \"" + methodListData[i-1][1] + "\")");
			rscriptParts.add(baseName + ".design.in <- read.delim(\"" + 
					methodListData[i-1][0] + "\", check.names=FALSE)");
			rscriptParts.add(baseName + ".design <- as.data.frame(" + 
					baseName + ".design.in[,c(5,2)])");
			rscriptParts.add("colnames(" + baseName + ".design) <- c(\"Data file\",\"sample_type\")");
			rscriptParts.add(baseName + ".design$batch <- \"" + methodListData[i-1][2] + "\"");
			rscriptParts.add(baseName + ".des <- merge(" + 
					baseName + ".design, " + baseName + ", by = \"Data file\", all = T)");
			
			qcSummaryNames.add(baseName + ".des");
		}
		rscriptParts.add("\n## Summary plots ####\n");
		
		String masterName = assayType4R + ".QC.summary";		
		rscriptParts.add(masterName + " <- rbind(" + StringUtils.join(qcSummaryNames, ",") + ")");
		rscriptParts.add(masterName + "$sample_type <- as.factor(" + masterName + "$sample_type)");
		rscriptParts.add(masterName + "$batch <- as.factor(" + masterName + "$batch)");
		rscriptParts.add(masterName +".melt <- melt(" + masterName + 
				", id.vars = c(\"sample_type\",\"batch\"), measure.vars = c(\"Observations\",\"Total area\"))");		
		String pivotName = assayType4R + ".QC.pivot";
		rscriptParts.add(pivotName + " <- " + masterName + ".melt %>% group_by(sample_type, batch, variable) "
				+ "%>% summarise(medianVal = median(value, na.rm = T), "
				+ "meanVal = mean(value, na.rm = T), stDev = sd(value, na.rm = T))");
		rscriptParts.add(pivotName + "$RSD <- " + pivotName + "$stDev / " + pivotName + "$meanVal * 100");
		rscriptParts.add(pivotName + "$sample_type <- as.factor(" + pivotName + "$sample_type)");
		rscriptParts.add(pivotName + "$batch <- as.factor(" + pivotName + "$batch)\n");
		rscriptParts.add(pivotName + ".counts <- " + pivotName + "[(" + pivotName + "$variable %in% c(\"Observations\")),]\n");
		
		rscriptParts.add("featureCountsPlot <- ggplot(" + pivotName + ".counts, aes(x = sample_type, y = medianVal, fill = batch))+");
		rscriptParts.add("\tgeom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1)+");
		rscriptParts.add("\t geom_errorbar(aes(ymin = medianVal-stDev, ymax = medianVal+stDev), "
				+ "position =  position_dodge(width = 0.5), width = 0.2)+");
		rscriptParts.add("ggtitle(\"" + experimentId + " " + assayType + " median feature counts by sample type / batch\")");
		rscriptParts.add("ggsave(\"" + experimentId + "-" + assayType + "-featureCounts.png\","
				+ " plot = featureCountsPlot,  width = 14, height = 8.5)\n");

		rscriptParts.add(pivotName + ".areas <- " + pivotName + "[(" + pivotName + "$variable %in% c(\"Total area\")),]");
		rscriptParts.add("areasPlot <- ggplot(" + pivotName + ".areas, aes(x = sample_type, y = medianVal, fill = batch))+");
		rscriptParts.add("geom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1)+");
		rscriptParts.add("geom_errorbar(aes(ymin = medianVal-stDev, ymax = medianVal+stDev), "
				+ "position =  position_dodge(width = 0.5), width = 0.2)+");
		rscriptParts.add("ggtitle(\"" + experimentId + " " + assayType + " median total areas by sample type / batch\")");
		rscriptParts.add("ggsave(\"" + experimentId + "-" + assayType + "-totalAreas.png\","
				+ " plot = areasPlot,  width = 14, height = 8.5)\n");
		
		String rScriptFileName = experimentId + "-" + assayType + 
				"-SummaryQC-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				rWorkingDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void generateParameterDistributionScriptForEX01409rpneg() {
		
		String experimentId = "EX01409";
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";	
		File inputMap = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\NEG\\EX01409-RP-NEG-QC-input-map.txt");
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A049 - Central carbon metabolism profiling\\Documents");	
		generateParameterDistributionScript(
				experimentId,
				assayType,
				assayType4R,
				inputMap,
				rWorkingDir);
	}
	
	private static void generateParameterDistributionScript(
			String experimentId,
			String assayType,
			String assayType4R,
			File inputMap,
			File rWorkingDir) {
		String[][] methodListData = DelimitedTextParser.parseTextFile(
				inputMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		String workDirForR = rWorkingDir.getAbsolutePath().replaceAll("\\\\", "/");
		
		List<String>rscriptParts = new ArrayList<String>();
		rscriptParts.add("# " + experimentId + " " + assayType + " running QC, parameter distributions ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		
		for(int i=1; i<=methodListData.length; i++) {
			
		}		
		String rScriptFileName = experimentId + "-" + assayType + 
				"-RunningQC-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				rWorkingDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void generateSummaryQcScript() {
		
		String experimentId = "EX01409";
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A049 - Central carbon metabolism profiling\\Documents");
		File xlQCfile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		
		String[]batchNames = new String[] {
				"BATCH01-10ul",
				"BATCH01-7ul",	
				"BATCH02",
				"BATCH03",
				"BATCH04",	
		};
		String[]xlSheetNames = new String[] {
				"IONP-NEG-B01-10ul",
				"IONP-NEG-B01-7ul",
				"IONP-NEG-B02",
				"IONP-NEG-B03",
				"IONP-NEG-B04",				
		};
		String[]manifestFileNames = new String[] {
				"EX01409-IONP-NEG-BATCH01_4BINNER_20240729_203405_MANIFEST.txt",	
				"EX01409-IONP-NEG-BATCH01-V2-7ul_4BINNER_20240809_113358_MANIFEST.txt",
				"EX01409-IONP-NEG-BATCH02_4BINNER_20240805_121015_MANIFEST.txt",
				"EX01409-IONP-NEG-BATCH03_4BINNER_20240812_191326_MANIFEST.txt",				
				"EX01409-IONP-NEG-BATCH04_4BINNER_20240902_102156_MANIFEST.txt",
		};
		if(xlSheetNames.length != manifestFileNames.length 
				|| xlSheetNames.length != batchNames.length) {
			System.err.println("Input length mismatch!");
			return;
		}	
		List<String>qcSummaryNames = new ArrayList<String>();
		List<String>rscriptParts = new ArrayList<String>();
		String workDirForR = rWorkingDir.getAbsolutePath().replaceAll("\\\\", "/");
		String xlFilePath4R = xlQCfile.getAbsolutePath().replaceAll("\\\\", "/");
		
		rscriptParts.add("# " + experimentId + " " + assayType + " summary QC ####\n");
		rscriptParts.add("setwd(\"" + workDirForR + "\")\n");
		rscriptParts.add("library(ggplot2)");
		rscriptParts.add("library(reshape2)");
		rscriptParts.add("library(dplyr)");
		rscriptParts.add("library(readxl)\n");	
		rscriptParts.add("## Feature count and total area summaries ####\n");
		
		for(int i=1; i<=batchNames.length; i++) {
			
			String baseName = assayType4R + ".qcSummary.B" + i;
			rscriptParts.add("\n## " + batchNames[i-1] + " ####\n");
			rscriptParts.add(baseName + " <- read_excel(\"" + 
					xlFilePath4R + " \", sheet = \"" + xlSheetNames[i-1] + "\")");
			rscriptParts.add(baseName + ".design.in <- read.delim(\"" + 
					manifestFileNames[i-1] + "\", check.names=FALSE)");
			rscriptParts.add(baseName + ".design <- as.data.frame(" + 
					baseName + ".design.in[,c(5,2)])");
			rscriptParts.add("colnames(" + baseName + ".design) <- c(\"Data file\",\"sample_type\")");
			rscriptParts.add(baseName + ".design$batch <- \"" + batchNames[i-1] + "\"");
			rscriptParts.add(baseName + ".des <- merge(" + 
					baseName + ".design, " + baseName + ", by = \"Data file\", all = T)");
			
			qcSummaryNames.add(baseName + ".des");
		}
		rscriptParts.add("\n## Summary plots ####\n");
		
		String masterName = assayType4R + ".QC.summary";		
		rscriptParts.add(masterName + " <- rbind(" + StringUtils.join(qcSummaryNames, ",") + ")");
		rscriptParts.add(masterName + "$sample_type <- as.factor(" + masterName + "$sample_type)");
		rscriptParts.add(masterName + "$batch <- as.factor(" + masterName + "$batch)");
		rscriptParts.add(masterName +".melt <- melt(" + masterName + 
				", id.vars = c(\"sample_type\",\"batch\"), measure.vars = c(\"Observations\",\"Total area\"))");		
		String pivotName = assayType4R + ".QC.pivot";
		rscriptParts.add(pivotName + " <- " + masterName + ".melt %>% group_by(sample_type, batch, variable) "
				+ "%>% summarise(medianVal = median(value, na.rm = T), "
				+ "meanVal = mean(value, na.rm = T), stDev = sd(value, na.rm = T))");
		rscriptParts.add(pivotName + "$RSD <- " + pivotName + "$stDev / " + pivotName + "$meanVal * 100");
		rscriptParts.add(pivotName + "$sample_type <- as.factor(" + pivotName + "$sample_type)");
		rscriptParts.add(pivotName + "$batch <- as.factor(" + pivotName + "$batch)\n");
		rscriptParts.add(pivotName + ".counts <- " + pivotName + "[(" + pivotName + "$variable %in% c(\"Observations\")),]\n");
		
		rscriptParts.add("featureCountsPlot <- ggplot(" + pivotName + ".counts, aes(x = sample_type, y = medianVal, fill = batch))+");
		rscriptParts.add("\tgeom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1)+");
		rscriptParts.add("\t geom_errorbar(aes(ymin = medianVal-stDev, ymax = medianVal+stDev), "
				+ "position =  position_dodge(width = 0.5), width = 0.2)+");
		rscriptParts.add("ggtitle(\"" + experimentId + " " + assayType + " median feature counts by sample type / batch\")");
		rscriptParts.add("ggsave(\"" + experimentId + "-" + assayType + "-featureCounts.png\","
				+ " plot = featureCountsPlot,  width = 14, height = 8.5)\n");

		rscriptParts.add(pivotName + ".areas <- " + pivotName + "[(" + pivotName + "$variable %in% c(\"Total area\")),]");
		rscriptParts.add("areasPlot <- ggplot(" + pivotName + ".areas, aes(x = sample_type, y = medianVal, fill = batch))+");
		rscriptParts.add("geom_col( position = \"dodge\", width = 0.5, alpha = 0.7, color = \"black\", linewidth = 0.1)+");
		rscriptParts.add("geom_errorbar(aes(ymin = medianVal-stDev, ymax = medianVal+stDev), "
				+ "position =  position_dodge(width = 0.5), width = 0.2)+");
		rscriptParts.add("ggtitle(\"" + experimentId + " " + assayType + " median total areas by sample type / batch\")");
		rscriptParts.add("ggsave(\"" + experimentId + "-" + assayType + "-totalAreas.png\","
				+ " plot = areasPlot,  width = 14, height = 8.5)\n");
		
		String rScriptFileName = experimentId + "-" + assayType + 
				"-SummaryQC-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				rWorkingDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		rscriptParts,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void ex01409rppos(){
		
		String experimentId = "EX01409";
		String assayType = "RP-POS";
		String assayType4R = "rppos";
		
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\POS");
		File xlQCfile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		
		String[]batchNames = new String[] {
				"BATCH01",	
				"BATCH02",
				"BATCH03",
				"BATCH04",	
				"BATCH05",
		};
		String[]xlSheetNames = new String[] {
				"RP-POS-B01",
				"RP-POS-B02",
				"RP-POS-B03",
				"RP-POS-B04",
				"RP-POS-B05",
		};
		String[]manifestFileNames = new String[] {
				"EX01409-RP-POS-BATCH01_4BINNER_20240729_132811_MANIFEST.txt",	
				"EX01409-RP-POS-BATCH02_4BINNER_20240729_111057_MANIFEST.txt",
				"EX01409-RP-POS-BATCH03_4BINNER_20240805_111421_MANIFEST.txt",
				"EX01409-RP-POS-BATCH04_4BINNER_20240807_213529_MANIFEST.txt",
				"EX01409-RP-POS-BATCH05_4BINNER_20240902_153727_MANIFEST.txt",
		};
	}
	
	private static void ex01409neg() {
		
		String experimentId = "EX01409";
		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\NEG");
		File xlQCfile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		
		String[]batchNames = new String[] {
				"BATCH01",	
				"BATCH02",
				"BATCH03",
				"BATCH04",	
				"BATCH05",
		};
		String[]xlSheetNames = new String[] {
				"RP-NEG-B01",
				"RP-NEG-B02",
				"RP-NEG-B03",
				"RP-NEG-B04",
				"RP-NEG-B05",
		};
		String[]manifestFileNames = new String[] {
				"EX01409-RP-NEG-BATCH01_4BINNER_20240729_130551_MANIFEST.txt",	
				"EX01409-RP-NEG-BATCH02_4BINNER_20240729_120743_MANIFEST.txt",
				"EX01409-RP-NEG-BATCH03_4BINNER_20240805_113637_MANIFEST.txt",
				"EX01409-RP-NEG-BATCH04_4BINNER_20240830_195058_MANIFEST.txt",
				"EX01409-RP-NEG-BATCH05_4BINNER_20240902_205630_MANIFEST.txt",
		};
	}
	
	private static void ex01409ionpNeg() {
		
		String experimentId = "EX01409";
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A049 - Central carbon metabolism profiling\\Documents");
		File xlQCfile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A003 - Untargeted\\Documents\\EX01409-QC-from-toolbox.xlsx");
		
		String[]batchNames = new String[] {
				"BATCH01-10ul",
				"BATCH01-7ul",	
				"BATCH02",
				"BATCH03",
				"BATCH04",	
		};
		String[]xlSheetNames = new String[] {
				"IONP-NEG-B01-10ul",
				"IONP-NEG-B01-7ul",
				"IONP-NEG-B02",
				"IONP-NEG-B03",
				"IONP-NEG-B04",				
		};
		String[]manifestFileNames = new String[] {
				"EX01409-IONP-NEG-BATCH01_4BINNER_20240729_203405_MANIFEST.txt",	
				"EX01409-IONP-NEG-BATCH01-V2-7ul_4BINNER_20240809_113358_MANIFEST.txt",
				"EX01409-IONP-NEG-BATCH02_4BINNER_20240805_121015_MANIFEST.txt",
				"EX01409-IONP-NEG-BATCH03_4BINNER_20240812_191326_MANIFEST.txt",				
				"EX01409-IONP-NEG-BATCH04_4BINNER_20240902_102156_MANIFEST.txt",
		};
	}
	
	private static void ex01414neg() {
		
		String experimentId = "EX01414";
		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01414 - META Metabolomics Repeat\\"
				+ "A003 - Untargeted\\Documents\\NEG");
		File xlQCfile = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01414 - META Metabolomics Repeat\\A003 - Untargeted\\Documents\\EX01414-QC.xlsx");
		
		String[]batchNames = new String[] {
				"BATCH01",	
				"BATCH02",
				"BATCH03",
				//"BATCH04",	
		};
		String[]xlSheetNames = new String[] {
			"RP-NEG-B01",	
			"RP-NEG-B02",
			"RP-NEG-B03",
			//"RP-NEG-B04",
		};
		String[]manifestFileNames = new String[] {
				"EX01414_RP-NEG-BATCH01_4BINNER_20240821_154640_MANIFEST.txt",	
				"EX01414_RP-NEG-BATCH02_4BINNER_20240825_134750_MANIFEST.txt",
				"EX01414_RP-NEG-BATCH03_4BINNER_20240901_131050_MANIFEST.txt",
				//"BATCH04",
		};
	}
	

}























