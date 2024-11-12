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
import java.util.List;

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
			generateSummaryQcScriptForEX01426rpneg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void generateSummaryQcScriptForEX01426ionpneg() {
		
		String experimentId = "EX01426";
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\EX01426-QC-summary-tables.xlsx");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\EX01426_IONP-NEG-SummaryQC-inputMap.txt");
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
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\A003 - Untargeted\\Documents\\NEG");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\EX01426-QC-summary-tables.xlsx");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\Documents\\NEG\\EX01426_RP-NEG-SummaryQC-inputMap.txt");
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
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\A003 - Untargeted\\Documents\\POS");		
		File xlQCfile  = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01426 - Human EDTA Tranche 2 plasma W20001176L\\EX01426-QC-summary-tables.xlsx");		
		File inputMap =  new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\Documents\\POS\\EX01426_RP-POS-SummaryQC-inputMap.txt");
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
				+ "A003 - Untargeted\\Documents\\EX01414-QC.xlsx");
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
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\"
				+ "EX01409 - Human EDTA Tranche 1 plasma W20000960M\\A003 - Untargeted\\Documents\\NEG");
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























