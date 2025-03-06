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
			createDataSummariesForEX01283rpPosExperiment();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createDataSummariesForEX01283rpPosExperiment() {
		
		File inputMapFile = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01283 - Starr County Metabolomics IV\\"
						+ "A003 - Untargeted\\Documents\\POS\\PW\\EX01283_RP-POS-dataSummarization-inputMap.txt");
		File dataDir = 
				new File("Y:\\DataAnalysis\\_Reports\\EX01283 - Starr County Metabolomics IV\\"
						+ "A003 - Untargeted\\Documents\\POS\\PW");
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
