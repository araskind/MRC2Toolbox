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

import edu.umich.med.mrc2.datoolbox.data.ExternalDatabase;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class ExternalDatabaseUtils {

	public static Collection<ExternalDatabase>getExternalDatabaseList() throws Exception {

		Collection<ExternalDatabase>dbList = new ArrayList<ExternalDatabase>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DATABASE_ID,  DATABASE_NAME, LINK_PREFIX, LINK_SUFFIX " +
			"FROM COMPOUND_DATABASES ORDER BY DATABASE_ID";
		PreparedStatement stmt = conn.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {

			ExternalDatabase newDbSource = new ExternalDatabase(
								rs.getString("DATABASE_ID"),
								rs.getString("DATABASE_NAME"),
								rs.getString("LINK_PREFIX"),
								rs.getString("LINK_SUFFIX")
							);
			dbList.add(newDbSource);
		}
		rs.close();
		ConnectionManager.releaseConnection(conn);
		return dbList;
	}

	public static void addDatabaseDefinition(ExternalDatabase database) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"INSERT INTO COMPOUND_DATABASES (DATABASE_ID, DATABASE_NAME, LINK_PREFIX, LINK_SUFFIX) VALUES (?,?,?,?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, database.getId());
		stmt.setString(2, database.getName());
		stmt.setString(3, database.getWebLinkPrefix());
		stmt.setString(4, database.getWebLinkSuffix());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteDatabaseDefinition(ExternalDatabase database) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM COMPOUND_DATABASES WHERE DATABASE_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, database.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void updateDatabaseDefinition(ExternalDatabase database) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE COMPOUND_DATABASES SET DATABASE_NAME = ?, LINK_PREFIX = ?, LINK_SUFFIX = ? WHERE DATABASE_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, database.getName());
		stmt.setString(2, database.getWebLinkPrefix());
		stmt.setString(3, database.getWebLinkSuffix());
		stmt.setString(4, database.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static boolean idExists(ExternalDatabase database) throws Exception {

		boolean exists = false;
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DATABASE_NAME FROM COMPOUND_DATABASES WHERE DATABASE_ID = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, database.getId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next())
			exists = true;

		rs.close();
		ConnectionManager.releaseConnection(conn);
		return exists;
	}

	public static boolean nameExists(ExternalDatabase database) throws Exception {

		boolean exists = false;
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DATABASE_NAME FROM COMPOUND_DATABASES WHERE DATABASE_NAME = ? AND DATABASE_ID != ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, database.getName());
		stmt.setString(2, database.getId());

		ResultSet rs = stmt.executeQuery();
		while (rs.next())
			exists = true;

		rs.close();
		ConnectionManager.releaseConnection(conn);
		return exists;
	}
}
