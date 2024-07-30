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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class ChromatographyDatabaseUtils {

	public static String addNewChromatographicGradient(
			ChromatographicGradient gradient) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"CHROMATOGRAPHIC_GRADIENT_SEQ",
				DataPrefix.CROMATOGRAPHIC_GRADIENT,
				"0",
				4);
		gradient.setId(nextId);
		if(gradient.getName() == null || gradient.getName().isEmpty())
			gradient.setName("Gradient " + nextId);
		
		String query  = 
			"INSERT INTO CHROMATOGRAPHIC_GRADIENT("
			+ "GRADIENT_ID, GRADIENT_NAME, GRADIENT_DESCRIPTION, "
			+ "MOBILE_PHASE_A, MOBILE_PHASE_B, MOBILE_PHASE_C, "
			+ "MOBILE_PHASE_D, COLUMN_COMPARTMENT_TEMPERATURE, STOP_TIME) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, gradient.getId());
		ps.setString(2, gradient.getName());
		ps.setString(3, gradient.getDescription());
		
		if(gradient.getMobilePhases()[0] != null)
			ps.setString(4, gradient.getMobilePhases()[0].getId());
		else
			ps.setNull(4, java.sql.Types.NULL);
		
		if(gradient.getMobilePhases()[1] != null)
			ps.setString(5, gradient.getMobilePhases()[1].getId());
		else
			ps.setNull(5, java.sql.Types.NULL);
		
		if(gradient.getMobilePhases()[2] != null)
			ps.setString(6, gradient.getMobilePhases()[2].getId());
		else
			ps.setNull(6, java.sql.Types.NULL);
		
		if(gradient.getMobilePhases()[3] != null)
			ps.setString(7, gradient.getMobilePhases()[3].getId());
		else
			ps.setNull(7, java.sql.Types.NULL);

		ps.setDouble(8, gradient.getColumnCompartmentTemperature());
		ps.setDouble(9, gradient.getStopTime());

		ps.executeUpdate();
		
		//	Insert gradient steps		
		query = 
			"INSERT INTO CHROMATOGRAPHIC_GRADIENT_STEP("
			+ "GRADIENT_ID, START_TIME, MOBILE_PHASE_A_START_VALUE, "
			+ "MOBILE_PHASE_B_START_VALUE, MOBILE_PHASE_C_START_VALUE, "
			+ "MOBILE_PHASE_D_START_VALUE, FLOW_RATE) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(1, gradient.getId());
		for(ChromatographicGradientStep step : gradient.getGradientSteps()) {
			
			ps.setDouble(2, step.getStartTime());			
			ps.setDouble(3, step.getMobilePhaseStartingPercent()[0]);
			ps.setDouble(4, step.getMobilePhaseStartingPercent()[1]);
			ps.setDouble(5, step.getMobilePhaseStartingPercent()[2]);
			ps.setDouble(6, step.getMobilePhaseStartingPercent()[3]);
			ps.setDouble(7, step.getFlowRate());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}
	
	public static String addTmpChromatographicGradientForAcqMethod(
			ChromatographicGradient gradient, 
			String acqMethodId, 
			boolean isActual) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"TMP_CHROMATOGRAPHIC_GRADIENT_SEQ",
				DataPrefix.CROMATOGRAPHIC_GRADIENT_TMP,
				"0",
				4);
		gradient.setId(nextId);
		if(gradient.getName() == null || gradient.getName().isEmpty())
			gradient.setName("Gradient " + nextId);
		
		String query  = 
			"INSERT INTO TMP_CHROMATOGRAPHIC_GRADIENT("
			+ "GRADIENT_ID, GRADIENT_NAME, GRADIENT_DESCRIPTION, "
			+ "MOBILE_PHASE_A, MOBILE_PHASE_B, MOBILE_PHASE_C, "
			+ "MOBILE_PHASE_D, COLUMN_COMPARTMENT_TEMPERATURE, STOP_TIME) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, gradient.getId());
		ps.setString(2, gradient.getName());
		ps.setString(3, gradient.getDescription());
		
		if(gradient.getMobilePhases()[0] != null)
			ps.setString(4, gradient.getMobilePhases()[0].getId());
		else
			ps.setNull(4, java.sql.Types.NULL);
		
		if(gradient.getMobilePhases()[1] != null)
			ps.setString(5, gradient.getMobilePhases()[1].getId());
		else
			ps.setNull(5, java.sql.Types.NULL);
		
		if(gradient.getMobilePhases()[2] != null)
			ps.setString(6, gradient.getMobilePhases()[2].getId());
		else
			ps.setNull(6, java.sql.Types.NULL);
		
		if(gradient.getMobilePhases()[3] != null)
			ps.setString(7, gradient.getMobilePhases()[3].getId());
		else
			ps.setNull(7, java.sql.Types.NULL);

		ps.setDouble(8, gradient.getColumnCompartmentTemperature());
		ps.setDouble(9, gradient.getStopTime());

		ps.executeUpdate();
		
		//	Insert gradient steps		
		query = 
			"INSERT INTO TMP_CHROMATOGRAPHIC_GRADIENT_STEP("
			+ "GRADIENT_ID, START_TIME, MOBILE_PHASE_A_START_VALUE, "
			+ "MOBILE_PHASE_B_START_VALUE, MOBILE_PHASE_C_START_VALUE, "
			+ "MOBILE_PHASE_D_START_VALUE, FLOW_RATE) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(1, gradient.getId());
		for(ChromatographicGradientStep step : gradient.getGradientSteps()) {
			
			ps.setDouble(2, step.getStartTime());			
			ps.setDouble(3, step.getMobilePhaseStartingPercent()[0]);
			ps.setDouble(4, step.getMobilePhaseStartingPercent()[1]);
			ps.setDouble(5, step.getMobilePhaseStartingPercent()[2]);
			ps.setDouble(6, step.getMobilePhaseStartingPercent()[3]);
			ps.setDouble(7, step.getFlowRate());
			ps.addBatch();
		}
		ps.executeBatch();
		
		String fieldToUpdate = "TMP_GRADIENT_ID";
		if(isActual)
			fieldToUpdate = "TMP_ACTUAL_GRADIENT_ID";
		
		query = 
			"UPDATE DATA_ACQUISITION_METHOD "
			+ "SET " + fieldToUpdate + " = ? WHERE ACQ_METHOD_ID = ?";				
		ps = conn.prepareStatement(query);
		ps.setString(1, gradient.getId());
		ps.setString(2, acqMethodId);
		ps.executeUpdate();
		
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}
	
	public static void editChromatographicGradient(
			ChromatographicGradient gradient) throws Exception{
		
		//	TODO
	}
	
	public static void deleteChromatographicGradient(
			ChromatographicGradient gradient) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = 
			"DELETE FROM CHROMATOGRAPHIC_GRADIENT WHERE GRADIENT_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, gradient.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<ChromatographicGradient>getChromatographicGradientList() throws Exception{
		
		Collection<ChromatographicGradient>chromatographicGradientList = 
				new HashSet<ChromatographicGradient>();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT GRADIENT_ID, GRADIENT_NAME, GRADIENT_DESCRIPTION,  " +
				"MOBILE_PHASE_A, MOBILE_PHASE_B, MOBILE_PHASE_C,  " +
				"MOBILE_PHASE_D, COLUMN_COMPARTMENT_TEMPERATURE, STOP_TIME " +
				"FROM CHROMATOGRAPHIC_GRADIENT ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String stepQuery = 
				"SELECT START_TIME, MOBILE_PHASE_A_START_VALUE,  " +
				"MOBILE_PHASE_B_START_VALUE, MOBILE_PHASE_C_START_VALUE,  " +
				"MOBILE_PHASE_D_START_VALUE, FLOW_RATE " +
				"FROM CHROMATOGRAPHIC_GRADIENT_STEP " +
				"WHERE GRADIENT_ID = ? ";
		PreparedStatement stepPs = conn.prepareStatement(stepQuery);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			ChromatographicGradient grad = new ChromatographicGradient(
					rs.getString("GRADIENT_ID"), 
					rs.getString("GRADIENT_NAME"),
					rs.getString("GRADIENT_DESCRIPTION"),
					rs.getDouble("COLUMN_COMPARTMENT_TEMPERATURE"),
					rs.getDouble("STOP_TIME"));
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_A")), 0);
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_B")), 1);
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_C")), 2);
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_D")), 3);
			
			stepPs.setString(1, grad.getId());
			ResultSet stepRs = stepPs.executeQuery();
			while(stepRs.next()) {
				
				ChromatographicGradientStep step = new ChromatographicGradientStep(
						stepRs.getDouble("START_TIME"), 
						stepRs.getDouble("FLOW_RATE"), 
						stepRs.getDouble("MOBILE_PHASE_A_START_VALUE"),
						stepRs.getDouble("MOBILE_PHASE_B_START_VALUE"), 
						stepRs.getDouble("MOBILE_PHASE_C_START_VALUE"), 
						stepRs.getDouble("MOBILE_PHASE_D_START_VALUE"));
				grad.addChromatographicGradientStep(step);				
			}
			stepRs.close();
			chromatographicGradientList.add(grad);
		}
		rs.close();
		ps.close();
		stepPs.close();
		ConnectionManager.releaseConnection(conn);
		
		return chromatographicGradientList;
	}
	
	public static Collection<ChromatographicGradient>getTempChromatographicGradientList() throws Exception{
		
		Collection<ChromatographicGradient>chromatographicGradientList = 
				new HashSet<ChromatographicGradient>();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT GRADIENT_ID, GRADIENT_NAME, GRADIENT_DESCRIPTION,  " +
				"MOBILE_PHASE_A, MOBILE_PHASE_B, MOBILE_PHASE_C,  " +
				"MOBILE_PHASE_D, COLUMN_COMPARTMENT_TEMPERATURE, STOP_TIME " +
				"FROM TMP_CHROMATOGRAPHIC_GRADIENT ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String stepQuery = 
				"SELECT START_TIME, MOBILE_PHASE_A_START_VALUE,  " +
				"MOBILE_PHASE_B_START_VALUE, MOBILE_PHASE_C_START_VALUE,  " +
				"MOBILE_PHASE_D_START_VALUE, FLOW_RATE " +
				"FROM TMP_CHROMATOGRAPHIC_GRADIENT_STEP " +
				"WHERE GRADIENT_ID = ? ";
		PreparedStatement stepPs = conn.prepareStatement(stepQuery);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			ChromatographicGradient grad = new ChromatographicGradient(
					rs.getString("GRADIENT_ID"), 
					rs.getString("GRADIENT_NAME"),
					rs.getString("GRADIENT_DESCRIPTION"),
					rs.getDouble("COLUMN_COMPARTMENT_TEMPERATURE"),
					rs.getDouble("STOP_TIME"));
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_A")), 0);
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_B")), 1);
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_C")), 2);
			grad.setMobilePhase(
					IDTDataCache.getMobilePhaseById(rs.getString("MOBILE_PHASE_D")), 3);
			
			stepPs.setString(1, grad.getId());
			ResultSet stepRs = stepPs.executeQuery();
			while(stepRs.next()) {
				
				ChromatographicGradientStep step = new ChromatographicGradientStep(
						stepRs.getDouble("START_TIME"), 
						stepRs.getDouble("FLOW_RATE"), 
						stepRs.getDouble("MOBILE_PHASE_A_START_VALUE"),
						stepRs.getDouble("MOBILE_PHASE_B_START_VALUE"), 
						stepRs.getDouble("MOBILE_PHASE_C_START_VALUE"), 
						stepRs.getDouble("MOBILE_PHASE_D_START_VALUE"));
				grad.addChromatographicGradientStep(step);				
			}
			stepRs.close();
			chromatographicGradientList.add(grad);
		}
		rs.close();
		ps.close();
		stepPs.close();
		ConnectionManager.releaseConnection(conn);
		
		return chromatographicGradientList;
	}
	
	public static Set<String> getUnassignedTemporaryGradientIds() throws Exception{
		
		Set<String>idSet = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = "";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);	
		return idSet;
	}
	
	/*
	 * MOBILE_PHASE
	 * */
	public static String addNewMobilePhase(MobilePhase phase) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"MOBILE_PHASE_SEQ",
				DataPrefix.MOBILE_PHASE,
				"0",
				4);
		phase.setId(nextId);
		String query  = 
				"INSERT INTO MOBILE_PHASE ("
				+ "MOBILE_PHASE_ID, MOBILE_PHASE_NAME) "
				+ "VALUES(?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, phase.getId());
		ps.setString(2, phase.getName());
		ps.executeUpdate();
		
		if(!phase.getSynonyms().isEmpty()) {
			
			query  = 
				"INSERT INTO MOBILE_PHASE_SYNONYMS ("
				+ "MOBILE_PHASE_ID, MP_SYNONYM) "
				+ "VALUES(?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, phase.getId());
			for(String syn : phase.getSynonyms()) {
				ps.setString(2, syn);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}
	
	public static void editMobilePhase(MobilePhase phase) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
			"UPDATE MOBILE_PHASE SET MOBILE_PHASE_NAME = ? "
			+ "WHERE MOBILE_PHASE_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, phase.getName());
		ps.setString(2, phase.getId());
		ps.executeUpdate();
		
		//	Update synonyms
		query  = 
			"DELETE FROM MOBILE_PHASE_SYNONYMS MOBILE_PHASE "
			+ "WHERE MOBILE_PHASE_ID = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, phase.getId());
		ps.executeUpdate();
		if(!phase.getSynonyms().isEmpty()) {
			
			query  = 
				"INSERT INTO MOBILE_PHASE_SYNONYMS ("
				+ "MOBILE_PHASE_ID, MP_SYNONYM) "
				+ "VALUES(?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, phase.getId());
			for(String syn : phase.getSynonyms()) {
				ps.setString(2, syn);
				ps.addBatch();
			}
			ps.executeBatch();
		}	
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMobilePhase(MobilePhase phase) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = 
			"DELETE FROM MOBILE_PHASE WHERE MOBILE_PHASE_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, phase.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<MobilePhase> getMobilePhaseList() throws Exception{

		Connection conn = ConnectionManager.getConnection();
		Collection<MobilePhase>mobilePhaseList = getMobilePhaseList(conn);
		ConnectionManager.releaseConnection(conn);
		return mobilePhaseList;
	}
	
	public static Collection<MobilePhase> getMobilePhaseList(Connection conn) throws Exception{
		
		Collection<MobilePhase>mobilePhaseList = new TreeSet<MobilePhase>();
		String query = 
				"SELECT MOBILE_PHASE_ID, MOBILE_PHASE_NAME "
				+ "FROM MOBILE_PHASE ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		String synonymQquery = 
				"SELECT MP_SYNONYM FROM MOBILE_PHASE_SYNONYMS "
				+ "WHERE  MOBILE_PHASE_ID = ?";
		PreparedStatement synonymPs = conn.prepareStatement(synonymQquery);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			MobilePhase p = new MobilePhase(
					rs.getString("MOBILE_PHASE_ID"), 
					rs.getString("MOBILE_PHASE_NAME"));
			
			synonymPs.setString(1, p.getId());
			ResultSet synRs = synonymPs.executeQuery();
			while(synRs.next())
				p.getSynonyms().add(synRs.getString(1));
				
			synRs.close();
			mobilePhaseList.add(p);
		}
		rs.close();
		ps.close();	
		return mobilePhaseList;
	}
	
	public static String getMobilePhaseId(MobilePhase phaseToCheck) throws Exception{
		
		String id = null;
		Connection conn = ConnectionManager.getConnection();
		String query = 
			"SELECT MOBILE_PHASE_ID FROM MOBILE_PHASE "
			+ "WHERE UPPER(MOBILE_PHASE_NAME) = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, phaseToCheck.getName().toUpperCase());
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			id = rs.getString("MOBILE_PHASE_ID");
		
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);		
		return id;
	}
	
	public static boolean hasNameConflict(MobilePhase phaseToCheck) throws Exception{
		
		boolean hasConflict = false;
		Connection conn = ConnectionManager.getConnection();
		String query = 
			"SELECT MOBILE_PHASE_ID FROM MOBILE_PHASE "
			+ "WHERE UPPER(MOBILE_PHASE_NAME) = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, phaseToCheck.getName().toUpperCase());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String id = rs.getString("MOBILE_PHASE_ID");
			if(!id.equals(phaseToCheck.getId()))
				hasConflict = true;
		}	
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);		
		return hasConflict;
	}
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
