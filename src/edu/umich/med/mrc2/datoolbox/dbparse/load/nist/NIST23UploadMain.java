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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class NIST23UploadMain {
	
	private static Map<File,File>msp2sdfMap;

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			parseAndUploadNIST23();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private static void parseAndUploadNIST23() throws Exception{
		
		File mspDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\MSP");
		File sdfDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\SDF_NORM");
		
		Collection<File> mspFiles = 
				FileUtils.listFiles(mspDirectory, new String[] {"msp", "MSP"}, false);
		Collection<File> sdfFiles = 
				FileUtils.listFiles(sdfDirectory, new String[] {"sdf", "SDF"}, false);
				
		msp2sdfMap = new TreeMap<File,File>();
		for(File mspFile : mspFiles) {
			
			if(mspFile.isDirectory())
				continue;
			
			String fName = FilenameUtils.getBaseName(mspFile.getName());			
			File sdfFile = sdfFiles.stream().
					filter(f -> FilenameUtils.getBaseName(f.getName()).equals(fName)).
					findFirst().orElse(null);
			if(sdfFile != null) {
				msp2sdfMap.put(mspFile, sdfFile);
			}
			else {
				System.out.println("Missing SDF for " + mspFile.getName());				
			}	
		}
		for(Entry<File, File> ff : msp2sdfMap.entrySet()) {
			
			System.out.println("Processing " + ff.getKey().getName());
			Map<NISTTandemMassSpectrum,IAtomContainer>dataForUpload = 
					NISTParserUtils.createMsmsMolMap(ff.getKey(), ff.getValue(), null);
			uploadNistData(dataForUpload);
		}
	}
	
	private static void uploadNistData(
			Map<NISTTandemMassSpectrum,IAtomContainer>dataForUpload) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		int count = 0;
	 	for(Entry<NISTTandemMassSpectrum,IAtomContainer>e : dataForUpload.entrySet()){
	 		
	 		NISTMSPParser.insertSpectrumRecord(e.getKey(), e.getValue(), conn);
	 		
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println(".");
	 	}
		ConnectionManager.releaseConnection(conn);
		System.out.println("MSMS upload for completed");
	}
}


















