/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.nio.file.Paths;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class EX01526andEX01617RQCgenerator {
	
	private static final String experimentId2B = "EX01526-1617";
	private static final String experimentId = "1617";
	
//	private static final File xlQCfile = new File("");
//	private static final File rpNegQcDir = new File("");
//	private static final File rpPosQcDir = new File("");
//	private static final File ionpNegQcDir = new File("");

	private static final File xlQCfile = new File("Y:\\DataAnalysis\\_Reports\\"
			+ "EX01617 - Human EDTA Tranche 5 plasma W20001945P and K20001964Q\\EX01526-1617-QC-summary-tables.xlsm");
	private static final File rpNegQcDir = new File("Y:\\DataAnalysis\\_Reports\\"
			+ "EX01617 - Human EDTA Tranche 5 plasma W20001945P and K20001964Q\\A003 - Untargeted\\Documents\\NEG\\All\\QC");
	private static final File rpPosQcDir = new File("Y:\\DataAnalysis\\_Reports\\"
			+ "EX01617 - Human EDTA Tranche 5 plasma W20001945P and K20001964Q\\A003 - Untargeted\\Documents\\POS\\All\\QC");
	private static final File ionpNegQcDir = new File("Y:\\DataAnalysis\\_Reports\\"
			+ "EX01617 - Human EDTA Tranche 5 plasma W20001945P and K20001964Q\\A049 - Central carbon metabolism profiling\\Documents\\All\\QC");
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateSummaryQcScriptForEX01526and1617rppos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	private static void generateSummaryQcScriptForEX01526and1617rpneg() {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = rpNegQcDir;	
	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01617_EX01526_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01617rpneg() {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = rpNegQcDir;	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01617_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01526and1617rppos() {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = rpPosQcDir;		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01617_EX01526_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01617rppos() {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = rpPosQcDir;		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01617_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
		
	private static void generateSummaryQcScriptForEX01526and1617ionpneg() {		
		
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = ionpNegQcDir;		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01617_EX01526_IONP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01617ionpneg() {		
		
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = ionpNegQcDir;	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01617_IONP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
}
