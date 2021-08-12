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

import edu.umich.med.mrc2.datoolbox.data.MSMSDecoyGenerationMethod;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSMSDecoyUtils {
	
	public static Collection<MSMSDecoyGenerationMethod> getMSMSDecoyGenerationMethods() throws Exception {

		Collection<MSMSDecoyGenerationMethod> methodList = 
				new TreeSet<MSMSDecoyGenerationMethod>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT METHOD_ID, METHOD_NAME, NOTES "
				+ "FROM MSMS_DECOY_GENERATION_METHOD ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			MSMSDecoyGenerationMethod newMethod = new MSMSDecoyGenerationMethod(
					rs.getString("METHOD_ID"), 
					rs.getString("METHOD_NAME"), 
					rs.getString("NOTES"));

			methodList.add(newMethod);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return methodList;
	}
	
	public static void addNewMethod(MSMSDecoyGenerationMethod newMethod) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_DECOY_METHOD_SEQ",
				DataPrefix.MSMS_DECOY_GENERATION_METHOD,
				"0",
				3);
		newMethod.setMethodId(id);
		String query = 
				"INSERT INTO MSMS_DECOY_GENERATION_METHOD "
				+ "(METHOD_ID, METHOD_NAME, NOTES) "
				+ "VALUES (?, ?, ?) " ;
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, id);
		stmt.setString(2, newMethod.getMethodName());
		stmt.setString(3, newMethod.getMethodNotes());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getMethodId(Connection conn) throws Exception {
//		
//		String id = null;
//		String query = "SELECT '" + DataPrefix.MSMS_DECOY_GENERATION_METHOD.getName() + 
//				"' || LPAD(MSMS_DECOY_METHOD_SEQ.NEXTVAL, 3, '0') AS NEXT_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) 
//			id = rs.getString("NEXT_ID");			
//		
//		rs.close();
//		ps.close();
//		return id;
//	}
	
	public static void updateMethod(MSMSDecoyGenerationMethod method) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE MSMS_DECOY_GENERATION_METHOD "
				+ "SET METHOD_NAME = ?, NOTES = ? WHERE METHOD_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, method.getMethodName());
		stmt.setString(2, method.getMethodNotes());
		stmt.setString(3, method.getMethodId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMethod(MSMSDecoyGenerationMethod toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM MSMS_DECOY_GENERATION_METHOD WHERE METHOD_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getMethodId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
}
