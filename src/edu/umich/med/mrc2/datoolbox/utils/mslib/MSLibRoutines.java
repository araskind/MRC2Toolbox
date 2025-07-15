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

package edu.umich.med.mrc2.datoolbox.utils.mslib;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSLibRoutines {

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			uploadBasePCDLCompoundList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void uploadBasePCDLCompoundList() {
		
		File compoundListFile = new File(
				"E:\\Development\\MRC2Toolbox\\ProFinderImport\\PCDL\\TSV\\"
				+ "All\\PCDL_compoundList4import_20250704.txt");		
		String targetLibraryId = "MSRTLIB00121";
		try {
			uploadCompoundList(compoundListFile, targetLibraryId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void uploadCompoundList(
			File compoundListFile, String targetLibraryId) throws Exception{
		
		String[][] compoundData = DelimitedTextParser.parseTextFile(
				compoundListFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		String query =
				"INSERT INTO MS_LIBRARY_COMPONENT " +
				"(TARGET_ID, ACCESSION, DATE_LOADED, LAST_MODIFIED, " +
				"NAME, ID_CONFIDENCE, LIBRARY_ID, ENABLED) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stmt = conn.prepareStatement(query);
		
		for(int i=1; i<compoundData.length; i++) {

			String libEntryName = compoundData[i][0];
			String accession = compoundData[i][1];
			String newId = SQLUtils.getNextIdFromSequence(conn, 
					"MS_RT_LIBRARY_TARGET_SEQ",
					DataPrefix.MS_LIBRARY_TARGET,
					"0",
					7);
				
			stmt.setString(1, newId);
			stmt.setString(2, accession);
			stmt.setDate(3, new java.sql.Date(new Date().getTime()));
			stmt.setDate(4, new java.sql.Date(new Date().getTime()));
			stmt.setString(5, libEntryName);
			stmt.setString(6, CompoundIdentificationConfidence.ACCURATE_MASS.getLevelId());
			stmt.setString(7, targetLibraryId);
			stmt.setString(8, "Y");
			
			stmt.executeUpdate();
		}
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
}
