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

package edu.umich.med.mrc2.datoolbox.database.thermo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThermoSqliteConnectionManager {

	public static Connection getConnection(File sqliteDatabase) throws Exception {

		Class.forName("org.sqlite.JDBC");		
		String url = "jdbc:sqlite:" + sqliteDatabase.getAbsolutePath().replaceAll("\\\\", "/");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url);
			System.out.println("Connection to SQLite has been established.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return connection;		
	}

	/**
	 * Helper wrapper around boilerplat JDBC code. Execute a statement that
	 * returns results using a PreparedStatement that takes no parameters
	 * (you're on your own if you're binding parameters).
	 *
	 * @return the results from the query
	 */
	public static ResultSet executeQueryNoParams(Connection conn, String statement) throws Exception {
		PreparedStatement ps = conn.prepareStatement(statement);
		return ps.executeQuery();
	}

	/**
	 * Helper wrapper around boilerplate JDBC code. Execute a statement that
	 * doesn't return results using a PreparedStatment, and returns the number
	 * of rows affected
	 */
	public static int executeUpdate(String statement, Connection conn) {
		try {
			PreparedStatement ps = conn.prepareStatement(statement);
			return ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return 0;
	}

	public static void releaseConnection(Connection connection) {
        try {  
            if (connection != null) {  
            	connection.close();  
            }  
        } catch (SQLException ex) {  
            System.out.println(ex.getMessage());  
        } 
	}
}
