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

package edu.umich.med.mrc2.datoolbox.database.cpdcol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class CompoundMultiplexUtils {
	
	/*
	 * Solvent
	 * */
	public static String addNewSolvent(MobilePhase solvent) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"MULTIPLEX_SOLVENT_SEQ",
				DataPrefix.MOBILE_PHASE,
				"0",
				3);
		solvent.setId(nextId);
		String query  = 
				"INSERT INTO COMPOUND_MULTIPLEX_SOLVENTS (SOLVENT_ID, SOLVENT_NAME) VALUES(?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, solvent.getId());
		ps.setString(2, solvent.getName());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}

	public static void editSolvent(MobilePhase solvent) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
			"UPDATE COMPOUND_MULTIPLEX_SOLVENTS SET SOLVENT_NAME = ? WHERE SOLVENT_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, solvent.getName());
		ps.setString(2, solvent.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteSolvent(MobilePhase solvent) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = 
			"DELETE FROM COMPOUND_MULTIPLEX_SOLVENTS WHERE SOLVENT_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, solvent.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<MobilePhase> getSolventList() throws Exception{

		Connection conn = ConnectionManager.getConnection();
		Collection<MobilePhase>solventList = getSolventList(conn);
		ConnectionManager.releaseConnection(conn);
		return solventList;
	}
	
	public static Collection<MobilePhase> getSolventList(Connection conn) throws Exception{
		
		Collection<MobilePhase>solventList = new TreeSet<MobilePhase>();
		String query = 
				"SELECT SOLVENT_ID, SOLVENT_NAME FROM COMPOUND_MULTIPLEX_SOLVENTS ORDER BY 1";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			MobilePhase p = new MobilePhase(
					rs.getString("SOLVENT_ID"), 
					rs.getString("SOLVENT_NAME"));
			solventList.add(p);
		}
		rs.close();
		ps.close();
		return solventList;
	}
}
