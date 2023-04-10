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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.IDTSearchQuery;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IDTSearchQueryUtils {

	public static Collection<IDTSearchQuery> getSearchQueryList() throws Exception{

		Collection<IDTSearchQuery> queries = new ArrayList<IDTSearchQuery>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT QUERY_ID, CREATED_BY, CREATED_ON, DESCRIPTION, QUERY_PARAMETERS  " +
				"FROM IDTRACKER_SEARCH_QUERIES  " +
				"ORDER BY CREATED_ON DESC ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();			
		while (rs.next()) {
			
			LIMSUser user = LIMSDataCache.getUserById(rs.getString("CREATED_BY"));
			IDTSearchQuery idtquery = new IDTSearchQuery(
					rs.getString("QUERY_ID"),
					rs.getString("DESCRIPTION"),
					user,
					new Date(rs.getDate("CREATED_ON").getTime()),					
					rs.getString("QUERY_PARAMETERS"));

			queries.add(idtquery);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return queries;
	}
	
	public static void insertNewQuery(IDTSearchQuery newQuery) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String sql  =
				"INSERT INTO IDTRACKER_SEARCH_QUERIES "
				+ "(QUERY_ID, CREATED_BY, CREATED_ON, DESCRIPTION, QUERY_PARAMETERS)  " +
				"VALUES (?, ?, ?, ?, ?) ";
		PreparedStatement ps = conn.prepareStatement(sql);
		String queryId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_TRACKER_SEARCH_QUERY_SEQ",
				DataPrefix.ID_TRACKER_SEARCH_QUERY,
				"0",
				9);
		newQuery.setId(queryId);
		ps.setString(1, queryId);
		ps.setString(2, newQuery.getAuthor().getId());
		ps.setDate(3, new java.sql.Date(newQuery.getCreatedOn().getTime()));
		ps.setString(4, newQuery.getDescription());
		ps.setString(5, newQuery.getQueryParameters());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteQuery(IDTSearchQuery queryToDelete) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String sql  =
				"DELETE FROM IDTRACKER_SEARCH_QUERIES WHERE QUERY_ID = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, queryToDelete.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextQueryId(Connection conn) throws Exception{
//		
//		String annotationId = null;
//		String query = "SELECT '" + DataPrefix.ID_TRACKER_SEARCH_QUERY.getName()
//				+ "' || LPAD(ID_TRACKER_SEARCH_QUERY_SEQ.NEXTVAL, 9, '0') AS QUERY_ID FROM DUAL";
//
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) {
//			annotationId = rs.getString("QUERY_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		return annotationId;
//	}
}
