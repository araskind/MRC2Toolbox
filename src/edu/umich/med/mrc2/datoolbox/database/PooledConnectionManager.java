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

package edu.umich.med.mrc2.datoolbox.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class PooledConnectionManager {

	private static HikariDataSource ds;
	private static HikariConfig config = new HikariConfig();
    
    static {
		config.setJdbcUrl(MRC2ToolBoxConfiguration.getDatabaseConnectionString());
		config.setUsername(MRC2ToolBoxConfiguration.getDatabaseUserName());
		config.setPassword(MRC2ToolBoxConfiguration.getDatabasePassword());
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }
    
	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
	
	public static void closeDataSource() {
		ds.close();
	}
	
    private PooledConnectionManager(){}
}





