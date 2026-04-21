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

public class EX01355andEX01572RQCgenerator2 {
	
	private static final String experimentId2B = "EX01355-1572";
	private static final String experimentId = "1572";

	private static final File xlQCfile = new File(
			"Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\EX01355-1572-QC-summary-tables.xlsm");
	private static final File xlHighCutoffQCfile = new File(
			"Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\EX01355-1572-QC-HC-summary-tables.xlsm");
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateSummaryQcScriptForEX01572ionpneg(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			generateSummaryQcScriptForEX01572rpneg(true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private static void generateSummaryQcScriptForEX01355and1572ionpneg(boolean highCutoffData) {		
		
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\NEG\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
					+ "A049 - Central carbon metabolism profiling\\Documents\\NEG\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01355-1572_IONP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01572ionpneg(boolean highCutoffData) {		
		
		String assayType = "IONP-NEG";
		String assayType4R = "ionpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\NEG\\CO300-Pk1000\\All\\QC");
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
					+ "A049 - Central carbon metabolism profiling\\Documents\\NEG\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01572_IONP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
			
	private static void generateSummaryQcScriptForEX01355and1572rpneg(boolean highCutoffData) {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
				+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
					+ "A003 - Untargeted\\Documents\\NEG\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01355-1572_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01572rpneg(boolean highCutoffData) {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
				+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
					+ "A003 - Untargeted\\Documents\\NEG\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01572_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01355and1572rppos(boolean highCutoffData) {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
				+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
					+ "A003 - Untargeted\\Documents\\POS\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01355-1572_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId2B,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01572rppos(boolean highCutoffData) {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
				+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01572 - Human Tranche 2 Muscle G20001069S\\"
					+ "A003 - Untargeted\\Documents\\POS\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01572_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
}
