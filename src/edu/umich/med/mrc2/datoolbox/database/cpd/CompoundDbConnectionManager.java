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

package edu.umich.med.mrc2.datoolbox.database.cpd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CompoundDbConnectionManager {

	private static ThreadLocal<Connection> tranConnection = new ThreadLocal<Connection>();

	/** get a connection */
	public static Connection getConnection() throws Exception {

		if (tranConnection.get() != null) {
			return tranConnection.get();
		} else {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection connection = null;
			connection = DriverManager.getConnection(
					MRC2ToolBoxConfiguration.getDatabaseConnectionString(),
					MRC2ToolBoxConfiguration.getDatabaseUserName(), 
					MRC2ToolBoxConfiguration.getDatabasePassword());
			return connection;
		}
	}

	public static synchronized void beginTransaction() throws Exception {

		if (tranConnection.get() != null)
			throw new Exception("This thread is already in a transaction");

		Connection conn = getConnection();
		conn.setAutoCommit(false);
		tranConnection.set(conn);
	}

	public static void commitTransaction() throws Exception {

		if (tranConnection.get() == null)
			throw new Exception("Can't commit: this thread isn't currently in a " + "transaction");

		tranConnection.get().commit();
		tranConnection.set(null);
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
	public static int executeUpdate(String statement) throws Exception {
		Connection conn = getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(statement);
			return ps.executeUpdate();
		} finally {
			releaseConnection(conn);
		}
	}

	public static void releaseConnection(Connection conn) throws Exception {

		if (tranConnection.get() == null)
			conn.close();
	}

	public static void rollbackTransaction() throws Exception {

		if (tranConnection.get() == null)
			throw new Exception("Can't rollback: this thread isn't currently in a " + "transaction");

		tranConnection.get().rollback();
		tranConnection.set(null);
	}
}
