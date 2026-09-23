/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.sql.SQLException;
import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class BasePCDLutils {
	
	private static final Logger logger = LogManager.getLogger(BasePCDLutils.class);
	
	private BasePCDLutils() {
		/* This utility class should not be instantiated */
	}
	
	public static String getMasterLibraryID() throws SQLException {
		
		String masterLibraryID = null;
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT LIBRARY_ID FROM MS_LIBRARY WHERE IS_MASTER IS NOT NULL";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			
			try(ResultSet rs = ps.executeQuery()){
				
				while(rs.next())
					masterLibraryID = rs.getString(1);
			}
		}
		ConnectionManager.releaseConnection(conn);
		return masterLibraryID;
	}
	
	public static CompoundLibrary getDefaultMasterLibrary() {
		
		String masterLibraryID = null;
		try {
			masterLibraryID = getMasterLibraryID();
		} catch (SQLException e) {
			logger.error("Failed to get master library ID from the database", e);
		}
		if(masterLibraryID == null)
			return null;
		
		return getMasterLibrary(masterLibraryID);
	}

	public static CompoundLibrary getMasterLibrary(String libId) {

		CompoundLibrary basePCDLlibrary = MRC2ToolBoxCore.getActiveMsLibraries().stream().
				filter(l -> l.getLibraryId().equals(libId)).
				findFirst().orElse(null);
		if(basePCDLlibrary != null)
			return basePCDLlibrary;

		basePCDLlibrary = MSRTLibraryUtils.getLibrary(libId);
		if (basePCDLlibrary == null) 
			return null;
		try {
			populateBasePCDLlibrary(basePCDLlibrary);
		}
		catch (Exception e) {
			logger.error("Failed to populate base PCDL library from the database", e);
			return null;
		}	
		if(!basePCDLlibrary.getFeatures().isEmpty())
			MRC2ToolBoxCore.getActiveMsLibraries().add(basePCDLlibrary);
		
		return basePCDLlibrary;
	}
	
	public static void populateBasePCDLlibrary(CompoundLibrary basePCDLlibrary) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<LibraryMsFeatureDbBundle>bundles =
				MSRTLibraryUtils.createFeatureBundlesForLibrary(basePCDLlibrary.getLibraryId(), conn);

		for(LibraryMsFeatureDbBundle fBundle : bundles) {

			if(fBundle.getConmpoundDatabaseAccession() != null) {

				LibraryMsFeature newTarget = fBundle.getFeature();
				MSRTLibraryUtils.attachIdentity(
						newTarget, fBundle.getConmpoundDatabaseAccession(), false, conn);

				if(newTarget.getPrimaryIdentity() != null) {

					newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
					basePCDLlibrary.addFeature(newTarget);
				}
			}
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void setMasterLibrary(String newMasterLibraryId) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE MS_LIBRARY SET IS_MASTER = NULL";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.executeUpdate();
		}
		query = "UPDATE MS_LIBRARY SET IS_MASTER = 'Y' WHERE LIBRARY_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, newMasterLibraryId);
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
}













