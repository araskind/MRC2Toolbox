/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class RScriptTestClass {

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			createDataSummariesForEX01526rpPosExperiment();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void runEX01242RPNEGMultialignment() {
		
		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\4MetabCombinner");
		
		File inputMap = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\4MetabCombinner\\EX01242-IONP-NEG-withAdducts-MC-inputMap.txt");
		
		RQCScriptGenerator.generateMultiBatchMetabCombinerAlignmentScriptScript(rWorkingDir, inputMap, 2);
	}
	
	private static void testMetabCombinerAlignmentScript() {

		File rWorkingDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\"
				+ "Documents\\_4MetabCombiner\\CLEANED");
		
		File inputMap = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\"
				+ "Documents\\_4MetabCombiner\\CLEANED\\EX01190-IONP-NEG-CLEANED-withAdducts-MC-inputMap.txt");
		
		RQCScriptGenerator.generateMultiBatchMetabCombinerAlignmentScriptScript(rWorkingDir, inputMap);
	}
			
	private static void createDataSummariesForEX01526rpPosExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
						+ "QC\\RP-POS\\EX01526-RP-POS-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\QC\\RP-POS");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}

	private static void createDataSummariesForEX01526rpNegExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
						+ "QC\\RP-NEG\\EX01526-RP-NEG-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\QC\\RP-NEG");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
	
	private static void createDataSummariesForEX01526ionpNegExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
						+ "QC\\IONP-NEG\\EX01526-IONP-NEG-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\QC\\IONP-NEG");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
	
	private static void createDataSummariesForEX01526ionpNegHighCutoffExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
						+ "QC\\HighCutoff\\IONP-NEG\\EX01526-IONP-NEG-HC-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\"
						+ "EX01526 - Human EDTA Tranche 4 plasma H20001805E\\QC\\HighCutoff\\IONP-NEG");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
	
	private static void createDataSummariesForEX01526rpNegHighCutoffExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
						+ "QC\\HighCutoff\\RP-NEG\\EX01526-RP-NEG-HC-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\"
						+ "EX01526 - Human EDTA Tranche 4 plasma H20001805E\\QC\\HighCutoff\\RP-NEG");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
		
	private static void createDataSummariesForEX01496ionpNegExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
						+ "A049 - Central carbon metabolism profiling\\Documents\\CO300-Pk1000\\_QC\\"
						+ "EX01496_IONP-NEG-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
						+ "A049 - Central carbon metabolism profiling\\Documents\\CO300-Pk1000");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
	
	private static void createDataSummariesForEX01496rpNegExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
						+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000\\_QC\\"
						+ "EX01496_RP-NEG-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
						+ "A003 - Untargeted\\Documents\\NEG\\CO300-Pk1000");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
	
	private static void createDataSummariesForEX01496rpPosExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
						+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000\\"
						+ "_QC\\EX01496_RP-POS-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
						+ "A003 - Untargeted\\Documents\\POS\\CO300-Pk1000");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
	
	private static void createDataSummariesForEX01426B6rpPosEMvoltageExperiment() {
		
		File inputMapFile = 
				new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents\\EX01426_RP-POS-EM-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\_QUALTMP\\EX01426\\RP-POS\\B6\\Documents");
		RQCScriptGenerator.createMultyBatchDataSummarizationScript(inputMapFile, dataDir);
	}
}
