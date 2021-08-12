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

package edu.umich.med.mrc2.datoolbox.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DatabseDialect;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class SQLUtils {
	
	public static String getNextIdFromSequence(
			String sequenceName,
			DataPrefix prefix,
			String padChar,
			int padLength) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String nextId = getNextIdFromSequence(
				conn, 
				sequenceName,
				prefix,
				padChar,
				padLength);		
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}
	
	public static String getNextIdFromSequence(
			Connection conn, 
			String sequenceName,
			DataPrefix prefix,
			String padChar,
			int padLength) throws Exception {
		
		String nextId = getNextIdFromSequence(
					conn, 
					MRC2ToolBoxConfiguration.getDatabaseType(),
					sequenceName,
					prefix,
					padChar,
					padLength);
		return nextId;
	}
	
	public static String getNextIdFromSequence(
			Connection conn, 
			DatabseDialect dialect,
			String sequenceName,
			DataPrefix prefix,
			String padChar,
			int padLength) throws Exception {
		
		String nexId = null;
		if(dialect.equals(DatabseDialect.Oracle) ) {
			
			String query  =
				"SELECT '" + prefix.getName() +
				"' || LPAD(" + sequenceName + ".NEXTVAL, " + Integer.toString(padLength) +
				", '" + padChar + "') AS NEXT_ID FROM DUAL";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				nexId = rs.getString("NEXT_ID");
			
			rs.close();
			ps.close();	
			return nexId;
		}
		if(dialect.equals(DatabseDialect.PostgreSQL) ) {
			
			String query  =
				"SELECT '" + prefix.getName() +
				"' || LPAD( NEXTVAL('"+ sequenceName +"')::text, " + Integer.toString(padLength) +
				", '" + padChar + "') AS NEXT_ID";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				nexId = rs.getString("NEXT_ID");
			
			rs.close();
			ps.close();	
			return nexId;
		}
		return nexId;
	}
}
