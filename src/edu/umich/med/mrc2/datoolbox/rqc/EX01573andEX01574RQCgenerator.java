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

public class EX01573andEX01574RQCgenerator {
	
	private static final String experimentId2B = "EX01573-1574";
	private static final String experimentId = "1574";
	private static final File xlQCfile = new File(
			"Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\EX01573-1574-QC-summary-tables.xlsm");
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateSummaryQcScriptForEX01573and1574rppos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	private static void generateSummaryQcScriptForEX01573and1574rpneg() {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\"
				+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000\\All\\QC");	
	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01573-1574_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01574rpneg() {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\"
				+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000\\All\\QC");	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01574_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01573and1574rppos() {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\"
				+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000\\All\\QC");		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01573-1574_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01574rppos() {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\"
				+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000\\All\\QC");		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01574_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
		
	private static void generateSummaryQcScriptForEX01573and1574ionpneg() {		
		
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\CO300-Pk1000\\All\\QC");		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01573-1574_IONP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01574ionpneg() {		
		
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01574 - Human Tranche 4 Muscle D20001750Q\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\CO300-Pk1000\\All\\QC");	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01574_IONP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
}
