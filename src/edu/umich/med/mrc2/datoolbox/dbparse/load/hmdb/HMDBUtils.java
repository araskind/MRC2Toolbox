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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundPropertyType;

public class HMDBUtils {

	public static Collection<CompoundBioLocation>getCompoundBioLocations() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundBioLocation>compoundBioLocations = 
				getCompoundBioLocations(conn);
		ConnectionManager.releaseConnection(conn);
		return compoundBioLocations;
	}
	
	public static Collection<CompoundBioLocation>getCompoundBioLocations(
			Connection conn) throws Exception{
		
		Collection<CompoundBioLocation>compoundBioLocations = 
				new ArrayList<CompoundBioLocation>();
		String query = 
				"SELECT LOCATION_ID, LOCATION_TYPE, LOCATION "
				+ "FROM COMPOUNDDB.HMDB_BIOLOCATION ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			CompoundBioLocation bl = new CompoundBioLocation(
					rs.getString("LOCATION"), 
					Enum.valueOf(CompoundLocationType.class, 
							rs.getString("LOCATION_TYPE")), 
					rs.getString("LOCATION_ID"));
			compoundBioLocations.add(bl);
		}
		rs.close();
		ps.close();
		return compoundBioLocations;
	}
	
	public static Collection<HMDBPathway>getHMDBPathways() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<HMDBPathway>hmdbPathways = 
				getHMDBPathways(conn);
		ConnectionManager.releaseConnection(conn);
		return hmdbPathways;
	}
	
	public static Collection<HMDBPathway>getHMDBPathways(
			Connection conn) throws Exception{
		
		Collection<HMDBPathway>hmdbPathways = 
				new ArrayList<HMDBPathway>();
		String query = "SELECT PATHWAY_ID, PATHWAY_NAME, KEGG_MAP_ID, SMPDB_ID "
				+ "FROM COMPOUNDDB.HMDB_PATHWAY ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			HMDBPathway pw = new HMDBPathway(
					rs.getString("PATHWAY_ID"),
					rs.getString("PATHWAY_NAME"), 
					rs.getString("SMPDB_ID"), 
					rs.getString("KEGG_MAP_ID"));
			hmdbPathways.add(pw);
		}
		rs.close();
		ps.close();
		return hmdbPathways;
	}
	
	public static Collection<CompoundProperty>getCompoundProperties() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundProperty>compoundProperties = 
				getCompoundProperties(conn);
		ConnectionManager.releaseConnection(conn);
		return compoundProperties;
	}
	
	public static Collection<CompoundProperty>getCompoundProperties(
			Connection conn) throws Exception{
		
		Collection<CompoundProperty>compoundProperties = 
				new ArrayList<CompoundProperty>();
		String query = "SELECT PROPERTY_ID, PROPERTY_TYPE, PROPERTY_NAME "
				+ "FROM COMPOUNDDB.HMDB_COMPOUND_PROPERTY ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			CompoundProperty cp = new CompoundProperty(
					rs.getString("PROPERTY_NAME"), 
					Enum.valueOf(CompoundPropertyType.class, 
							rs.getString("PROPERTY_TYPE")), 
					rs.getString("PROPERTY_ID"));			
			compoundProperties.add(cp);
		}
		rs.close();
		ps.close();						
		return compoundProperties;		
	}
	
	public static Map<Integer,String>getHMDBReferencesMap() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Map<Integer,String>hmdbReferencesMap = 
				getHMDBReferencesMap(conn);
		ConnectionManager.releaseConnection(conn);
		return hmdbReferencesMap;
	}
	
	public static Map<Integer,String>getHMDBReferencesMap(Connection conn) throws Exception{
		
		Map<Integer,String>hmdbReferencesMap = new HashMap<Integer,String>();
		String query = 
				"SELECT HASH_CODE, LIT_REF_ID "
				+ "FROM COMPOUNDDB.HMDB_LITERATURE_REFERENCES";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			hmdbReferencesMap.put(
					rs.getInt("HASH_CODE"), 
					rs.getString("LIT_REF_ID"));
		}
		rs.close();
		ps.close();
		return hmdbReferencesMap;
	}
}











