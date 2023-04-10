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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.InstrumentPlatform;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class AssayDatabaseUtils {

	public static Collection<Assay> getLimsAssayList() throws Exception {

		Collection<Assay> assays = new TreeSet<Assay>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ASSAY_ID, ASSAY_NAME, PLATFORM_ID, "
				+ "ALTERNATE_NAME FROM LIMS_ASSAY ORDER BY ASSAY_NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			Assay assay = new Assay(
					rs.getString("ASSAY_ID"),
					rs.getString("ASSAY_NAME"),	
					rs.getString("ALTERNATE_NAME"));
			InstrumentPlatform ip = 
					LIMSDataCache.getInstrumentPlatformById(rs.getString("PLATFORM_ID"));
			assay.setInstrumentPlatform(ip);
			assays.add(assay);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return assays;
	}

	public static Collection<Assay> getAssaysForExperiment(LIMSExperiment experiment) throws Exception {

		Collection<Assay> assays = new TreeSet<Assay>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT ASSAY_ID FROM LIMS_SAMPLE_ASSAY_MAP M, "
				+ "LIMS_SAMPLE S WHERE S.SAMPLE_ID = M.SAMPLE_ID "
				+ "AND S.EXPERIMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, experiment.getId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
						
			Assay assay = LIMSDataCache.getAssayById(rs.getString("ASSAY_ID"));
			if(assay != null)
				assays.add(assay);
		}
		ps.close();
		rs.close();
		ConnectionManager.releaseConnection(conn);
		return assays;
	}
	
	//	TODO 
	public static String getMotrPacMsModeForDataPipeline(DataPipeline dataPipeline) throws Exception {
		
		
		return null;		
	}

	public static void updateAssay(Assay method) throws Exception {

		String query =
			"UPDATE LIMS_ASSAY SET ASSAY_NAME = ?  WHERE ASSAY_ID = ?";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, method.getName());
		ps.setString(2, method.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addNewAssay(Assay method) throws Exception {

		Connection conn = ConnectionManager.getConnection();		
		String newAssayId = addNewAssay(method, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static String addNewAssay(Assay method, Connection conn) throws Exception {

		String query =
			"INSERT INTO LIMS_ASSAY (ASSAY_NAME, ASSAY_ID, "
			+ "PLATFORM_ID, ALTERNATE_NAME) VALUES (?, ?, ?, ?)";	
		String newAssayId = SQLUtils.getNextIdFromSequence(conn, 
				"ASSAY_METHOD_SEQ",
				DataPrefix.ASSAY_METHOD,
				"0",
				3);
		method.setId(newAssayId);
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, method.getName());
		ps.setString(2, method.getId());
		String platformId = null;
		if(method.getInstrumentPlatform() != null)
			platformId = method.getInstrumentPlatform().getId();
		
		ps.setString(3, platformId);
		ps.setString(4, method.getAlternativeName());
		ps.executeUpdate();
		ps.close();
		return newAssayId;
	}
	
//	public static String getNextAssayId(Connection conn) throws Exception {
//		
//		String query  =
//				"SELECT '" + DataPrefix.ASSAY_METHOD.getName() + 
//				"' || LPAD(ASSAY_METHOD_SEQ.NEXTVAL, 3, '0') AS NEXT_ID FROM DUAL";
//		
//		String nextId = null;
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next())
//			nextId = rs.getString("NEXT_ID");
//
//		rs.close();
//		ps.close();
//		return nextId;		
//	}

	public static void deleteAssay(Assay method) throws Exception {

		String query = "DELETE FROM LIMS_ASSAY WHERE ASSAY_ID = ?";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, method.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
}
