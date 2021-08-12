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

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IdFollowupUtils {
	
	
	/*
	 * Followup steps
	 * */
	
	public static Collection<MSFeatureIdentificationFollowupStep> getMSFeatureIdentificationFollowupStepList() 
			throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<MSFeatureIdentificationFollowupStep> stepList
			= getMSFeatureIdentificationFollowupStepList(conn);
		ConnectionManager.releaseConnection(conn);
		return stepList;
	}
	
	private static Collection<MSFeatureIdentificationFollowupStep> getMSFeatureIdentificationFollowupStepList(
			Connection conn) throws Exception {

		Collection<MSFeatureIdentificationFollowupStep>stepList = 
				new TreeSet<MSFeatureIdentificationFollowupStep>();
		String query =
				"SELECT FOLLOWUP_STEP_ID, NAME FROM IDENTIFICATION_FOLLOWUP_STEP ORDER BY NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			MSFeatureIdentificationFollowupStep status = new MSFeatureIdentificationFollowupStep(
					rs.getString("FOLLOWUP_STEP_ID"),
					rs.getString("NAME"));
			stepList.add(status);
		}
		rs.close();
		ps.close();
		return stepList;
	}

	public static void addNewMSFeatureIdentificationFollowupStep(
			MSFeatureIdentificationFollowupStep step) throws Exception {

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

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, step.getId());
		ps.setString(2, step.getName());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	private static String getNextMSFeatureIdentificationFollowupStepId(
//			Connection conn) throws SQLException {
//
//		String stepId = null;
//		String query = "SELECT '" + DataPrefix.IDENTIFICATION_FOLLOWUP_STEP.getName() +
//				"' || LPAD(ID_FOLLOWUP_STEP_SEQ.NEXTVAL, 3, '0') AS STEP_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			stepId = rs.getString("STEP_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		return stepId;
//	}

	public static void editMSFeatureIdentificationFollowupStep(
			MSFeatureIdentificationFollowupStep step) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
				"UPDATE IDENTIFICATION_FOLLOWUP_STEP SET NAME = ? "
				+ "WHERE FOLLOWUP_STEP_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, step.getName());
		ps.setString(2, step.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMSFeatureIdentificationFollowupStep(
			MSFeatureIdentificationFollowupStep step) throws Exception {

		//	References will cascade, so no need to clear them first
		Connection conn = ConnectionManager.getConnection();
		String query =
				"DELETE FROM IDENTIFICATION_FOLLOWUP_STEP WHERE FOLLOWUP_STEP_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, step.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void attachIdFollowupStepsToMSMSFeature(MsFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		attachIdFollowupStepsToMSMSFeature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void attachIdFollowupStepsToMSMSFeature(
			MsFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
				"SELECT FOLLOWUP_STEP_ID FROM MSMS_FEATURE_FOLLOWUP_STEPS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			 MSFeatureIdentificationFollowupStep newStep = 
					 IDTDataCash.getMSFeatureIdentificationFollowupStepById(rs.getString("FOLLOWUP_STEP_ID"));
			 if(newStep != null)
				 fib.addIdFollowupStep(newStep);
		}
		rs.close();
		ps.close();
	}
	
	public static void setIdFollowupStepsForMSMSFeature(MsFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		setIdFollowupStepsForMSMSFeature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setIdFollowupStepsForMSMSFeature(MsFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
				"DELETE FROM MSMS_FEATURE_FOLLOWUP_STEPS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ps.executeUpdate();
		
		if(!fib.getIdFollowupSteps().isEmpty()) {
			
			query =
					"INSERT INTO MSMS_FEATURE_FOLLOWUP_STEPS(MSMS_PARENT_FEATURE_ID, FOLLOWUP_STEP_ID) " +
					"VALUES(?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, fib.getMsFeature().getId());
			for(MSFeatureIdentificationFollowupStep step : fib.getIdFollowupSteps()) {
				ps.setString(2, step.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ps.close();
	}
	
	
	public static void attachIdFollowupStepsToMS1Feature(MsFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		attachIdFollowupStepsToMS1Feature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void attachIdFollowupStepsToMS1Feature(MsFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
			"SELECT FOLLOWUP_STEP_ID FROM POOLED_MS1_FEATURE_FOLLOWUP_STEPS " +
			"WHERE POOLED_MS_FEATURE_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			 MSFeatureIdentificationFollowupStep newStep = 
					 IDTDataCash.getMSFeatureIdentificationFollowupStepById(rs.getString("FOLLOWUP_STEP_ID"));
			 if(newStep != null)
				 fib.addIdFollowupStep(newStep);
		}
		rs.close();
		ps.close();
	}
	
	public static void setIdFollowupStepsForMS1Feature(MsFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		setIdFollowupStepsForMS1Feature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}	

	public static void setIdFollowupStepsForMS1Feature(MsFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
				"DELETE FROM POOLED_MS1_FEATURE_FOLLOWUP_STEPS " +
				"WHERE POOLED_MS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ps.executeUpdate();
		
		if(!fib.getIdFollowupSteps().isEmpty()) {
			
			query =
					"INSERT INTO POOLED_MS1_FEATURE_FOLLOWUP_STEPS (POOLED_MS_FEATURE_ID, FOLLOWUP_STEP_ID) " +
					"VALUES(?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, fib.getMsFeature().getId());
			for(MSFeatureIdentificationFollowupStep step : fib.getIdFollowupSteps()) {
				ps.setString(2, step.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ps.close();
	}
}
