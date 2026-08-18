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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IdFollowupUtils {
	
	private static final Logger logger = LogManager.getLogger(IdFollowupUtils.class);

	private IdFollowupUtils() {
		/* This utility class should not be instantiated */
	}	
	
	/*
	 * Followup steps
	 * */
	
	public static Collection<MSFeatureIdentificationFollowupStep> getMSFeatureIdentificationFollowupStepList() 
			throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		Collection<MSFeatureIdentificationFollowupStep> stepList
			= getMSFeatureIdentificationFollowupStepList(conn);
		ConnectionManager.releaseConnection(conn);
		return stepList;
	}
	
	private static Collection<MSFeatureIdentificationFollowupStep> getMSFeatureIdentificationFollowupStepList(
			Connection conn) throws SQLException {

		Collection<MSFeatureIdentificationFollowupStep>stepList = 
				new TreeSet<MSFeatureIdentificationFollowupStep>();
		String query =
				"SELECT FOLLOWUP_STEP_ID, NAME FROM IDENTIFICATION_FOLLOWUP_STEP ORDER BY NAME";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				MSFeatureIdentificationFollowupStep status = new MSFeatureIdentificationFollowupStep(
						rs.getString("FOLLOWUP_STEP_ID"),
						rs.getString("NAME"));
				stepList.add(status);
			}
			rs.close();
		}
		return stepList;
	}

	public static void addNewMSFeatureIdentificationFollowupStep(
			MSFeatureIdentificationFollowupStep step) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_FOLLOWUP_STEP_SEQ",
				DataPrefix.IDENTIFICATION_FOLLOWUP_STEP,
				"0",
				3);
		step.setId(nextId);		
		String query =
			"INSERT INTO IDENTIFICATION_FOLLOWUP_STEP " + 
			"(FOLLOWUP_STEP_ID, NAME) VALUES(?, ?)";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, step.getId());
			ps.setString(2, step.getName());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void editMSFeatureIdentificationFollowupStep(
			MSFeatureIdentificationFollowupStep step) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query =
				"UPDATE IDENTIFICATION_FOLLOWUP_STEP SET NAME = ? "
				+ "WHERE FOLLOWUP_STEP_ID = ?";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, step.getName());
			ps.setString(2, step.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMSFeatureIdentificationFollowupStep(
			MSFeatureIdentificationFollowupStep step) throws SQLException {

		//	References will cascade, so no need to clear them first
		Connection conn = ConnectionManager.getConnection();
		String query =
				"DELETE FROM IDENTIFICATION_FOLLOWUP_STEP WHERE FOLLOWUP_STEP_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, step.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static void attachIdFollowupStepsToMSMSFeature(MSFeatureInfoBundle fib) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		attachIdFollowupStepsToMSMSFeature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void attachIdFollowupStepsToMSMSFeature(
			MSFeatureInfoBundle fib, Connection conn) throws SQLException {

		String query =
				"SELECT FOLLOWUP_STEP_ID FROM MSMS_FEATURE_FOLLOWUP_STEPS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, fib.getMsFeature().getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				 MSFeatureIdentificationFollowupStep newStep = 
						 IDTDataCache.getMSFeatureIdentificationFollowupStepById(rs.getString("FOLLOWUP_STEP_ID"));
				 if(newStep != null)
					 fib.addIdFollowupStep(newStep);
			}
			rs.close();
		}
	}
	
	public static void setIdFollowupStepsForMSMSFeature(MSFeatureInfoBundle fib) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		setIdFollowupStepsForMSMSFeature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setIdFollowupStepsForMSMSFeature(MSFeatureInfoBundle fib, Connection conn) throws SQLException {

		String query =
				"DELETE FROM MSMS_FEATURE_FOLLOWUP_STEPS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, fib.getMsFeature().getId());
			ps.executeUpdate();
		}
		if(!fib.getIdFollowupSteps().isEmpty()) {
			
			query = "INSERT INTO MSMS_FEATURE_FOLLOWUP_STEPS(MSMS_PARENT_FEATURE_ID, FOLLOWUP_STEP_ID) " +
					"VALUES(?, ?)";
			try(PreparedStatement ps = conn.prepareStatement(query)){
				ps.setString(1, fib.getMsFeature().getId());
				for(MSFeatureIdentificationFollowupStep step : fib.getIdFollowupSteps()) {
					ps.setString(2, step.getId());
					ps.addBatch();
				}
				ps.executeBatch();
			}
		}	
	}	
	
	public static void attachIdFollowupStepsToMS1Feature(MSFeatureInfoBundle fib) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		attachIdFollowupStepsToMS1Feature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void attachIdFollowupStepsToMS1Feature(MSFeatureInfoBundle fib, Connection conn) throws SQLException {

		String query =
			"SELECT FOLLOWUP_STEP_ID FROM POOLED_MS1_FEATURE_FOLLOWUP_STEPS " +
			"WHERE POOLED_MS_FEATURE_ID = ? ";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, fib.getMsFeature().getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				 MSFeatureIdentificationFollowupStep newStep = 
						 IDTDataCache.getMSFeatureIdentificationFollowupStepById(rs.getString("FOLLOWUP_STEP_ID"));
				 if(newStep != null)
					 fib.addIdFollowupStep(newStep);
			}
			rs.close();
		}
	}
	
	public static void setIdFollowupStepsForMS1Feature(MSFeatureInfoBundle fib) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		setIdFollowupStepsForMS1Feature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}	

	public static void setIdFollowupStepsForMS1Feature(MSFeatureInfoBundle fib, Connection conn) throws SQLException {

		String query =
				"DELETE FROM POOLED_MS1_FEATURE_FOLLOWUP_STEPS " +
				"WHERE POOLED_MS_FEATURE_ID = ? ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, fib.getMsFeature().getId());
			ps.executeUpdate();
		}
		if(!fib.getIdFollowupSteps().isEmpty()) {
			
			query =
					"INSERT INTO POOLED_MS1_FEATURE_FOLLOWUP_STEPS (POOLED_MS_FEATURE_ID, FOLLOWUP_STEP_ID) " +
					"VALUES(?, ?)";
			try(PreparedStatement ps = conn.prepareStatement(query)){
				ps.setString(1, fib.getMsFeature().getId());
				for(MSFeatureIdentificationFollowupStep step : fib.getIdFollowupSteps()) {
					ps.setString(2, step.getId());
					ps.addBatch();
				}
				ps.executeBatch();
			}
		}
	}
}











