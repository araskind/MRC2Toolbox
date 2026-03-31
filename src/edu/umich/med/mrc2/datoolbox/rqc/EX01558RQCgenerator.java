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

public class EX01558RQCgenerator {

	private static final String experimentId = "1558";

	private static final File xlQCfile = new File(
			"Y:\\DataAnalysis\\_Reports\\EX01558 - PLAN metabolomics\\EX01558-QC-summary-tables.xlsm");
	private static final File xlHighCutoffQCfile = new File(
			"Y:\\DataAnalysis\\_Reports\\EX01558 - PLAN metabolomics\\EX01558-QC-HC-summary-tables.xlsm");
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			generateSummaryQcScriptForEX01558rppos(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			generateSummaryQcScriptForEX01558rpneg(true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private static void generateSummaryQcScriptForEX01558rpneg(boolean highCutoffData) {

		String assayType = "RP-NEG";
		String assayType4R = "rpneg";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01558 - PLAN metabolomics\\"
				+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01558 - PLAN metabolomics\\"
					+ "A003 - Untargeted\\Documents\\NEG\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}	
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01558_RP-NEG-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
	
	private static void generateSummaryQcScriptForEX01558rppos(boolean highCutoffData) {

		String assayType = "RP-POS";
		String assayType4R = "rppos";
		File xlQc = xlQCfile;
		File rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01558 - PLAN metabolomics\\"
				+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000\\All\\QC");	
		if(highCutoffData) {
			rWorkingDir = new File("Y:\\DataAnalysis\\_Reports\\EX01558 - PLAN metabolomics\\"
					+ "A003 - Untargeted\\Documents\\POS\\CO900-Pk3000\\All\\QC");
			xlQc = xlHighCutoffQCfile;
		}		
		File inputMap = Paths.get(rWorkingDir.getAbsolutePath(), "EX01558_RP-POS-SummaryQC-inputMap.txt").toFile();
		RQCScriptGenerator.generateSummaryQcScript(
				experimentId,
				rWorkingDir,
				xlQc,
				inputMap,
				assayType,
				assayType4R);
	}
}
