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

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class StandardAnnotationUtils {
	
	public static Collection<StandardFeatureAnnotation> getStandardFeatureAnnotationList() 
			throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<StandardFeatureAnnotation> stepList
			= getStandardFeatureAnnotationList(conn);
		ConnectionManager.releaseConnection(conn);
		return stepList;
	}
	
	public static Collection<StandardFeatureAnnotation> getStandardFeatureAnnotationList(
			Connection conn) throws Exception {

		Collection<StandardFeatureAnnotation>annotationList = 
				new TreeSet<StandardFeatureAnnotation>();
		String query =
				"SELECT STANDARD_ANNOTATION_ID, CODE, TEXT FROM FEATURE_STANDARD_ANNOTATION";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			StandardFeatureAnnotation annotation = new StandardFeatureAnnotation(
					rs.getString("STANDARD_ANNOTATION_ID"),
					rs.getString("CODE"),
					rs.getString("TEXT"));
			annotationList.add(annotation);
		}
		rs.close();
		ps.close();
		return annotationList;
	}

	public static void addNewStandardFeatureAnnotation(
			StandardFeatureAnnotation annotation) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"STANDARD_FEATURE_ANNOTATION_SEQ",
				DataPrefix.STANDARD_FEATURE_ANNOTATION,
				"0",
				4);
		annotation.setId(newId);		
		String query =
			"INSERT INTO FEATURE_STANDARD_ANNOTATION " + 
			"(STANDARD_ANNOTATION_ID, CODE, TEXT) VALUES(?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getId());
		ps.setString(2, annotation.getCode());
		ps.setString(3, annotation.getText());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	private static String getNextStandardFeatureAnnotationId(
//			Connection conn) throws SQLException {
//
//		String stepId = null;
//		String query = "SELECT '" + DataPrefix.STANDARD_FEATURE_ANNOTATION.getName() +
//				"' || LPAD(STANDARD_FEATURE_ANNOTATION_SEQ.NEXTVAL, 4, '0') AS STAN_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			stepId = rs.getString("STAN_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		return stepId;
//	}

	public static void editStandardFeatureAnnotation(
			StandardFeatureAnnotation annotation) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
				"UPDATE FEATURE_STANDARD_ANNOTATION SET CODE = ?, TEXT = ? "
				+ "WHERE STANDARD_ANNOTATION_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getCode());
		ps.setString(2, annotation.getText());
		ps.setString(3, annotation.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteStandardFeatureAnnotation(
			StandardFeatureAnnotation annotation) throws Exception {

		//	References will cascade, so no need to clear them first
		Connection conn = ConnectionManager.getConnection();
		String query =
				"DELETE FROM FEATURE_STANDARD_ANNOTATION WHERE STANDARD_ANNOTATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void attachStandardFeatureAnnotationToMSMSFeature(
				MSFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		attachStandardFeatureAnnotationToMSMSFeature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void attachStandardFeatureAnnotationToMSMSFeature(
			MSFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
				"SELECT STANDARD_ANNOTATION_ID FROM MSMS_FEATURE_STANDARD_ANNOTATIONS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			StandardFeatureAnnotation newAnnotation = 
					 IDTDataCash.getStandardFeatureAnnotationById(rs.getString("STANDARD_ANNOTATION_ID"));
			 if(newAnnotation != null)
				 fib.addStandardFeatureAnnotation(newAnnotation);
		}
		rs.close();
		ps.close();
	}
	
	public static void setStandardFeatureAnnotationsForMSMSFeature(
			MSFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		setStandardFeatureAnnotationsForMSMSFeature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setStandardFeatureAnnotationsForMSMSFeature(
			MSFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
				"DELETE FROM MSMS_FEATURE_STANDARD_ANNOTATIONS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ps.executeUpdate();
		
		if(!fib.getStandadAnnotations().isEmpty()) {
			
			query =
					"INSERT INTO MSMS_FEATURE_STANDARD_ANNOTATIONS ("
					+ "MSMS_PARENT_FEATURE_ID, STANDARD_ANNOTATION_ID) VALUES(?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, fib.getMsFeature().getId());
			for(StandardFeatureAnnotation annotation : fib.getStandadAnnotations()) {
				ps.setString(2, annotation.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ps.close();
	}
		
	public static void attachStandardFeatureAnnotationsToMS1Feature(
			MSFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		attachStandardFeatureAnnotationsToMS1Feature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void attachStandardFeatureAnnotationsToMS1Feature(
			MSFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
			"SELECT STANDARD_ANNOTATION_ID FROM POOLED_MS1_FEATURE_STANDARD_ANNOTATIONS " +
			"WHERE POOLED_MS_FEATURE_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			StandardFeatureAnnotation newAnnotation = 
					 IDTDataCash.getStandardFeatureAnnotationById(rs.getString("STANDARD_ANNOTATION_ID"));
			 if(newAnnotation != null)
				 fib.addStandardFeatureAnnotation(newAnnotation);
		}
		rs.close();
		ps.close();
	}
	
	public static void setStandardFeatureAnnotationsForMS1Feature(
			MSFeatureInfoBundle fib) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		setStandardFeatureAnnotationsForMS1Feature(fib, conn);
		ConnectionManager.releaseConnection(conn);
	}	

	public static void setStandardFeatureAnnotationsForMS1Feature(
			MSFeatureInfoBundle fib, Connection conn) throws Exception {

		String query =
				"DELETE FROM POOLED_MS1_FEATURE_STANDARD_ANNOTATIONS " +
				"WHERE POOLED_MS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, fib.getMsFeature().getId());
		ps.executeUpdate();
		
		if(!fib.getStandadAnnotations().isEmpty()) {
			
			query =
					"INSERT INTO POOLED_MS1_FEATURE_STANDARD_ANNOTATIONS "
					+ "(POOLED_MS_FEATURE_ID, STANDARD_ANNOTATION_ID) " +
					"VALUES(?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, fib.getMsFeature().getId());
			for(StandardFeatureAnnotation newAnnotation : fib.getStandadAnnotations()) {
				ps.setString(2, newAnnotation.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ps.close();
	}
}
