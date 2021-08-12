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
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class IDTRawDataUtils {

	public static Injection getInjectionForId(String injectionId) throws Exception {

		Connection conn = ConnectionManager.getConnection();		
		Injection inj = getInjectionForId(injectionId, conn);
		ConnectionManager.releaseConnection(conn);
		return inj;
	}

	public static Injection getInjectionForId(String injectionId, Connection conn) throws Exception {
		
		Injection inj = null;
		String query =
			"SELECT DATA_FILE_NAME, PREP_ITEM_ID, INJECTION_TIMESTAMP, "
			+ "ACQUISITION_METHOD_ID, INJECTION_VOLUME FROM INJECTION "
			+ "WHERE INJECTION_ID = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, injectionId);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			 inj = new Injection(
					 injectionId,
					 rs.getString("DATA_FILE_NAME"),
					 new Date(rs.getDate("INJECTION_TIMESTAMP").getTime()),
					 rs.getString("PREP_ITEM_ID"),
					 rs.getString("ACQUISITION_METHOD_ID"),
					 rs.getDouble("INJECTION_VOLUME"));
		rs.close();
		ps.close();
		return inj;
	}
}
