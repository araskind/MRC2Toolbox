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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class IDTRawDataUtils {

	public static Injection getInjectionForId(String injectionId) throws Exception {

		Connection conn = ConnectionManager.getConnection();		
		Injection inj = getInjectionForId(injectionId, conn);
		ConnectionManager.releaseConnection(conn);
		return inj;
	}

	public static Injection getInjectionForId(
			String injectionId, Connection conn) throws Exception {
		
		Injection inj = null;
		String query =
			"SELECT DATA_FILE_NAME, PREP_ITEM_ID, INJECTION_TIMESTAMP, "
			+ "ACQUISITION_METHOD_ID, INJECTION_VOLUME FROM INJECTION "
			+ "WHERE INJECTION_ID = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, injectionId);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			 inj = new Injection(
					 injectionId,
					 rs.getString("DATA_FILE_NAME"),
					 new Date(rs.getDate("INJECTION_TIMESTAMP").getTime()),
					 rs.getString("PREP_ITEM_ID"),
					 rs.getString("ACQUISITION_METHOD_ID"),
					 rs.getDouble("INJECTION_VOLUME"));
		rs.close();
		ps.close();
		return inj;
	}
	
	public static Map<LIMSExperiment,Collection<DataFile>>getExistingDataFiles(
			Collection<DataFile>filesToCheck) throws Exception {

		Connection conn = ConnectionManager.getConnection();		
		Map<LIMSExperiment,Collection<DataFile>>existingDataFiles = 
				getExistingDataFiles(filesToCheck, conn);
		ConnectionManager.releaseConnection(conn);
		return existingDataFiles;
	}
	
	public static Map<LIMSExperiment,Collection<DataFile>>getExistingDataFiles(
			Collection<DataFile>filesToCheck, Connection conn) throws Exception {
		
		Map<LIMSExperiment,Collection<DataFile>>existingDataFiles = 
				new TreeMap<LIMSExperiment,Collection<DataFile>>();
		String query = 
				"SELECT I.DATA_FILE_NAME, I.INJECTION_TIMESTAMP,  " +
				"I.ACQUISITION_METHOD_ID, S.SAMPLE_ID, S.EXPERIMENT_ID " +
				"FROM INJECTION I, " +
				"PREPARED_SAMPLE P, " +
				"SAMPLE S " +
				"WHERE I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"AND P.SAMPLE_ID= S.SAMPLE_ID " +
				"AND I.DATA_FILE_NAME LIKE ?";
		PreparedStatement ps = conn.prepareStatement(query);
		for(DataFile df : filesToCheck) {
			
			ps.setString(1, df.getBaseName() + "%");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				LIMSExperiment experiment = 
						IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));
				if(experiment != null) {
					
					if(!existingDataFiles.containsKey(experiment))
						existingDataFiles.put(experiment, new TreeSet<DataFile>());
					
					DataAcquisitionMethod acqMethod = IDTDataCache.getAcquisitionMethodById(
							rs.getString("ACQUISITION_METHOD_ID"));
					
					DataFile existingDataFile = 
							new DataFile(rs.getString("DATA_FILE_NAME"), acqMethod);
					existingDataFile.setInjectionTime(
							new Date(rs.getDate("INJECTION_TIMESTAMP").getTime()));
					
					IDTExperimentalSample sample = IDTUtils.getExperimentalSampleById(
							rs.getString("SAMPLE_ID"), conn);
					existingDataFile.setParentSample(sample);
					existingDataFiles.get(experiment).add(existingDataFile);
				}				
			}		
		}
		return existingDataFiles;
	}
}








