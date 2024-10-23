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

public class QCANVASMoTrPACreportFixer {

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateMoTrPACQCScript4EX01242();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void generateMoTrPACQCScript4EX01242() {
		
		String experimentId = "EX01242";
		File baseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01242 - "
				+ "preCovid adipose Shipment W20000044X\\4BIC\\QCANVAS");	
		File ioMap = new File("Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\"
				+ "4BIC\\QCANVAS\\EX01242_resultFixInputMap.txt");
		
		generateMoTrPACQCScript(experimentId, baseDir,ioMap);
	}
	
	private static void generateMoTrPACQCScript4EX01190() {
		
		String experimentId = "EX01190";
		File baseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\4BIC\\QCANVAS");	
		File ioMap = new File("Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\"
				+ "4BIC\\QCANVAS\\EX01190_resultFixInputMap.txt");
		
		generateMoTrPACQCScript(experimentId, baseDir,ioMap);
	}
				
	private static void generateMoTrPACQCScript(
			String experimentId,
			File baseDir,			
			File ioMap) {
		
		String[][] ioData = DelimitedTextParser.parseTextFile(
				ioMap, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		List<String>scriptBlock = new ArrayList<String>();
		String workDirForR = Paths.get(baseDir.getAbsolutePath()).toString().replaceAll("\\\\", "/");
		scriptBlock.add("setwd(\"" + workDirForR + "\")");
		scriptBlock.add("\t");
		scriptBlock.add("# " + experimentId + " Fixing blank samples addition for QCANVAS ####");		
		scriptBlock.add("library(reshape2)");
		scriptBlock.add("library(dplyr)");
		
		String methodCode = null;
		String subGroup = null;
		String qcanvasDir = null;
		String manifestString = null;
		String normWithoutBlanksFilePath = null;
		String rawWithBlanksFilePath = null;		
		String[] manifestFilePaths;
		String[] manifests;
		
		for(int i=0; i<ioData.length; i++) {
			
			methodCode = ioData[i][0];
			subGroup =  ioData[i][1];
			qcanvasDir =  ioData[i][2];
			manifestString = ioData[i][3];
			
			scriptBlock.add("## " + methodCode + ", " + subGroup + " ####\n");
			
			normWithoutBlanksFilePath = 
					Paths.get(qcanvasDir, "Data", "normalized_by_SERRF_with_NAs.csv").
					toString().replaceAll("\\\\", "/");
			rawWithBlanksFilePath = 
					Paths.get(qcanvasDir, "Data", "raw_data_with_manifest_info_QCaNVaS.csv").
					toString().replaceAll("\\\\", "/");
			
			scriptBlock.add("# Read normalized data w/out blanks");
			scriptBlock.add(methodCode + "." + subGroup + ".norm.wout.blanks.in <- read.csv(\"" 
					+ normWithoutBlanksFilePath + "\", check.names=FALSE)");
			
			//	Remove  UNK prefix
			scriptBlock.add(methodCode + "." + subGroup + ".norm.wout.blanks.in$Feature <- gsub(\"\\\\s*UNK_\", \"\", " + 
					methodCode + "." + subGroup + ".norm.wout.blanks.in$Feature)");
			
			scriptBlock.add(methodCode + "." + subGroup + ".norm.wout.blanks <- " 
					+ methodCode + "." + subGroup + ".norm.wout.blanks.in[,-1]");
			scriptBlock.add("rownames(" + methodCode + "." + subGroup + ".norm.wout.blanks) <- " 
					+ methodCode + "." + subGroup + ".norm.wout.blanks.in[,1]");
			scriptBlock.add("\t");
			scriptBlock.add("# Read raw data with blanks");
			scriptBlock.add(methodCode + "." + subGroup + ".raw.with.blanks <- read.csv(\"" 
					+ rawWithBlanksFilePath + "\", check.names=FALSE)");
			scriptBlock.add(methodCode + "." + subGroup + ".raw.blanks.only <- " 
					+ methodCode + "." + subGroup + ".raw.with.blanks[" + methodCode + "." + subGroup 
					+ ".raw.with.blanks$SampleType %in% "
					+ "c(\"QC-Blank\"),] %>% select(!c(\"Index\",\"Batch\",\"SampleType\",\"SampleOrder\",\"InjOrder\",\"RunOrder\"))");
			scriptBlock.add("\t");
			scriptBlock.add("# Transpose raw data and convert to numeric");
			scriptBlock.add(methodCode + "." + subGroup + ".raw.blanks.only.t <- as.data.frame(t(" 
					+ methodCode + "." + subGroup + ".raw.blanks.only))[-1,]");
			scriptBlock.add("colnames(" + methodCode + "." + subGroup + ".raw.blanks.only.t) <- " 
					+ methodCode + "." + subGroup + ".raw.blanks.only[,1]");
			scriptBlock.add(methodCode + "." + subGroup + ".raw.blanks.only.t[] <- lapply(" 
					+ methodCode + "." + subGroup + ".raw.blanks.only.t, function(x) as.numeric(as.character(x)))");
			//	Remove  UNK prefix
			scriptBlock.add("rownames(" + methodCode + "." + subGroup + ".raw.blanks.only.t) <- "
					+ "gsub(\"\\\\s*UNK_\", \"\", rownames(" + methodCode + "." + subGroup + ".raw.blanks.only.t))");
			
			scriptBlock.add("\t");
			scriptBlock.add("# Merge normalized data with blanks");
			scriptBlock.add(methodCode + "." + subGroup + ".raw.all <- merge(" 
					+ methodCode + "." + subGroup + ".norm.wout.blanks, " 
					+ methodCode + "." + subGroup + ".raw.blanks.only.t, by = 0, all.x = T)");
			scriptBlock.add("\t");
			scriptBlock.add("# Read manifest file ");
			manifestFilePaths = manifestString.split(";");
			if(manifestFilePaths.length == 1) {
				scriptBlock.add("# Single manifest file ");
				String rPath = manifestFilePaths[0].replaceAll("\\\\", "/");
				scriptBlock.add(methodCode + "." + subGroup + ".manifest <- read.delim(\"" + 
						rPath + "\", check.names=FALSE)"
						+ " %>% select(c(\"raw_file\",\"sample_id\"))");
			}
			else {
				manifests = new String[manifestFilePaths.length];
				for(int j=0; j<manifestFilePaths.length; j++) {
					
					String mfRname = manifestFilePaths[j].replaceAll("\\\\", "/");
					String varName = methodCode + "." + subGroup + ".manifest" + Integer.toString(j);
					manifests[j] = varName;
					scriptBlock.add(varName + " <- read.delim(\"" + mfRname + "\", check.names=FALSE)"
							+ " %>% select(c(\"raw_file\",\"sample_id\"))");				
				}
				String manifestBindBlock = StringUtils.join(manifests, ",");
				scriptBlock.add(methodCode + "." + subGroup + ".manifest <- bind_rows(" + manifestBindBlock + ")"
						+ " %>% distinct(.keep_all = T)");
			}
			scriptBlock.add("\t");
			scriptBlock.add("# Rename columns to sample IDs");
			scriptBlock.add(methodCode + "." + subGroup + ".sidNames <- " + methodCode + "." + subGroup + 
					".manifest$sample_id[match(names(" + methodCode + "." + subGroup + ".raw.all), " +
					methodCode + "." + subGroup + ".manifest$raw_file)]");
			scriptBlock.add(methodCode + "." + subGroup + ".sidNames[1] <- \"metabolite_name\"");
			scriptBlock.add("names(" + methodCode + "." + subGroup + ".raw.all) <- " + methodCode + "." + subGroup + ".sidNames");
			scriptBlock.add("\t");
			scriptBlock.add("# Write out the results");
			String outFilePath = Paths.get(baseDir.getAbsolutePath(), 
					experimentId + "_" + methodCode + "_" + subGroup + "_metabolite_results.txt").
					toString().replaceAll("\\\\", "/");
			scriptBlock.add("write.table(" + methodCode + "." + subGroup + ".raw.all, file = \"" + 
					outFilePath + "\", quote = F, sep = \"\t\", na = \"\", row.names = F)");
			scriptBlock.add("\t");
		}
		String rScriptFileName = experimentId + "-QCANVASMoTrPACreportFix" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				baseDir.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		scriptBlock,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}
