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

package edu.umich.med.mrc2.datoolbox.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ReleasePreferencesCleanup {

	public static void main(String[] args) {

		resetUserSpecificPreferences();
		System.out.println("\n*****************************************************");
		System.out.println("*	Cleanup completed.");
		System.out.println("*****************************************************");
	}
	
	private static void resetUserSpecificPreferences() {
		
		System.out.println("Cleaning user-specific preferences.");
		Properties config = new Properties();
		InputStream inputStream = null;
		String configFileName = "MRC2ToolBoxPrefs.txt";		
		try {
			inputStream = MRC2ToolBoxConfiguration.class.getClassLoader().getResourceAsStream(configFileName);
			if (inputStream != null) {
				config.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + configFileName + "' not found in the classpath");
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			if(inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		//	Reset database configuration	
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databaseConnectionString", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databaseHost", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databaseNameSid", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databasePassword", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databasePort", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databaseSchema", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databaseType", "");
		config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.databaseUser", "");
		
		//	Reset folders/directories
	    @SuppressWarnings("unchecked")
	    Enumeration<String> enums = (Enumeration<String>) config.propertyNames();
	    while (enums.hasMoreElements()) {
	      String key = enums.nextElement();
	      if(key.contains("BASE_DIR") || key.contains("Directory") || key.contains("CURRENT_DIRECTORY")
	    		  || key.contains("INPUT_FILE") || key.contains("INPUT_FILE_DIR"))
	    	  config.setProperty(key, "");
	    }
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.automator.recentDataFolders", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.automator.recentMethods", "");    
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.idworks.CefMsMsPrescanSetupDialog.CEF_DIR_PARENT", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.idworks.CefMsMsPrescanSetupDialog.CPD_FILE_PARENT", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.OUTPUT_DIR", "");	    	    
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.LIBRARY_DIR", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.LIB_LIST", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.io.MultiFileDataImportDialog.BASE_DATA_FILES_DIRECTORY", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.io.MultiFileDataImportDialog.BASE_DESIGN_DIRECTORY", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.io.MultiFileDataImportDialog.BASE_LIBRARY_DIRECTORY", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.rawdata.msc.RawDataConversionSetupDialog.OUTPUT_DIRECTORY", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.gui.rawdata.msc.RawDataConversionSetupDialog.SOURCE_FILE_LOCATION", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.LIB2NIST_BINARY_PATH", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.PERCOLATOR_BINARY_PATH", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.RAW_DATA_REPOSITORY_DIR_PATH", "");
	    config.setProperty("edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration.SIRIUS_BINARY_PATH", "");
	}
}











