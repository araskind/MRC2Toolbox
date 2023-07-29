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

package edu.umich.med.mrc2.datoolbox.database.cpd;

import java.sql.Connection;
import java.sql.PreparedStatement;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class CompoundCurationUtils {
	
	public static void setCompoundTautomerGroupCuratedFlag(
			String primaryCompoundAccession, boolean isCurated) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOUNDDB.COMPOUND_GROUP "
				+ "SET CURATED = ? WHERE PRIMARY_ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		if(isCurated)
			ps.setString(1, "Y");
		else
			ps.setNull(1, java.sql.Types.NULL);

		ps.setString(2, primaryCompoundAccession);
		ps.executeQuery();	 
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void removeTautomerFromCompoundGroup(
			String primaryCompoundAccession, String tautomerAccession) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM COMPOUNDDB.COMPOUND_GROUP "
				+ "WHERE PRIMARY_ACCESSION = ? AND SECONDARY_ACCESSION = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, primaryCompoundAccession);
		ps.setString(2, tautomerAccession);
		ps.executeQuery();	 
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
}
