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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSResolution;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.utils.DiskCacheUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSMSLibraryUtils {
	
	private static final Logger logger = LogManager.getLogger(MSMSLibraryUtils.class);
	public static final String ANNOTATION_FIELD_NAME = "Annotation";
	
	private MSMSLibraryUtils() {
		/* This utility class should not be instantiated */
	}
	
	public static Collection<ReferenceMsMsLibrary>getReferenceMsMsLibraries() throws SQLException{

		Collection<ReferenceMsMsLibrary>libraries = new ArrayList<ReferenceMsMsLibrary>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT MSMS_LIBRARY_ID, LIB_NAME, DESCRIPTION, DATE_CREATED, "
			+ "LAST_EDITED, SEARCH_OUTPUT_CODE, PRIMARY_LIBRARY_ID, IS_SUBSET, IS_DECOY " +
			"FROM REF_MSMS_LIBRARY_LIST ORDER BY 1 ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				ReferenceMsMsLibrary lib = new ReferenceMsMsLibrary(
						rs.getString("MSMS_LIBRARY_ID"),
						rs.getString("LIB_NAME"),
						rs.getString("DESCRIPTION"),
						rs.getString("SEARCH_OUTPUT_CODE"),
						rs.getString("PRIMARY_LIBRARY_ID"),
						new Date(rs.getDate("DATE_CREATED").getTime()),
						new Date(rs.getDate("LAST_EDITED").getTime()),
						rs.getString("IS_SUBSET") != null,
						rs.getString("IS_DECOY") != null);
				libraries.add(lib);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return libraries;
	}
	
	public static String insertNewReferenceMsMsLibrary(ReferenceMsMsLibrary newLibrary) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String libId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_LIB_LIST_SEQ",
				DataPrefix.MSMS_LIBRARY,
				"0",
				4);
		newLibrary.setUniqueId(libId);
		String query =
			"INSERT INTO REF_MSMS_LIBRARY_LIST (MSMS_LIBRARY_ID, LIB_NAME, DESCRIPTION, DATE_CREATED, "
			+ "LAST_EDITED, SEARCH_OUTPUT_CODE, PRIMARY_LIBRARY_ID, IS_SUBSET, IS_DECOY) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, libId);
			ps.setString(2, newLibrary.getName());
			ps.setString(3, newLibrary.getDescription());
			ps.setDate(4, new java.sql.Date(new Date().getTime()));
			ps.setDate(5, new java.sql.Date(new Date().getTime()));
			ps.setString(6, newLibrary.getSearchOutputCode());
			ps.setString(7, newLibrary.getPrimaryLibraryId());
			if(newLibrary.isSubset())
				ps.setString(8, "Y");
			else
				ps.setString(8, null);
	
			if(newLibrary.isDecoy())
				ps.setString(9, "Y");
			else
				ps.setString(9, null);
			
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
		return libId;
	}
	
	public static MsMsLibraryFeature getMsMsLibraryFeatureById(String mrc2msmsId) throws SQLException {

		MsMsLibraryFeature f = DiskCacheUtils.retrieveMsMsLibraryFeatureFromCache(mrc2msmsId);
		if(f != null)
			return f;
		
		Connection conn = ConnectionManager.getConnection();
		f = getMsMsLibraryFeatureById(mrc2msmsId, conn);
		ConnectionManager.releaseConnection(conn);
		return f;
	}

	public static MsMsLibraryFeature getMsMsLibraryFeatureById(
			String mrc2msmsId,
			Connection conn) throws SQLException {

		MsMsLibraryFeature feature = 
				DiskCacheUtils.retrieveMsMsLibraryFeatureFromCache(mrc2msmsId);
		if(feature != null)
			return feature;
		
		String query =
			"SELECT POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ, ADDUCT, "
			+ "COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE, "
			+ "MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION, "
			+ "SPECTRUM_TYPE, CHROMATOGRAPHY_TYPE, CONTRIBUTOR, SPLASH, "
			+ "RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, "
			+ "ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, ENTROPY "
			+ "FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?";

		try(PreparedStatement ps = conn.prepareStatement(query)){			
			ps.setString(1, mrc2msmsId);			
			String msmsQuery =
					"SELECT MZ, INTENSITY, FRAGMENT_COMMENT FROM REF_MSMS_LIBRARY_PEAK "
					+ "WHERE MRC2_LIB_ID = ? ORDER BY 1";
			try(PreparedStatement msmsps = conn.prepareStatement(msmsQuery)){	
				msmsps.setString(1, mrc2msmsId);
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
		
					feature = new MsMsLibraryFeature(
							mrc2msmsId,
							Polarity.getPolarityByCode(
									rs.getString(MSMSComponentTableFields.POLARITY.name())));
					feature.setSpectrumSource(
							SpectrumSource.getOptionByName(
									rs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
					feature.setIonizationType(
							IDTDataCache.getIonizationTypeById(
							rs.getString(MSMSComponentTableFields.IONIZATION_TYPE.name())));
					feature.setCollisionEnergyValue(
							rs.getString(MSMSComponentTableFields.COLLISION_ENERGY.name()));
					feature.setSpectrumEntropy(
							rs.getDouble(MSMSComponentTableFields.ENTROPY.name()));
		
					for(MSMSComponentTableFields field : MSMSComponentTableFields.values()) {
		
						if(!field.equals(MSMSComponentTableFields.PRECURSOR_MZ) 
								&& !field.equals(MSMSComponentTableFields.MRC2_LIB_ID)) {
							
							String value = rs.getString(field.name());
							if(value != null && !value.trim().isEmpty())
								feature.addProperty(field.getName(), value);
						}
					}
					ReferenceMsMsLibrary refLib =
							IDTDataCache.getReferenceMsMsLibraryByPrimaryLibraryId(
									rs.getString(MSMSComponentTableFields.LIBRARY_NAME.name()));
					feature.setMsmsLibraryIdentifier(refLib.getUniqueId());
		
					//	Add spectrum
					double precursorMz = rs.getDouble(MSMSComponentTableFields.PRECURSOR_MZ.name());
					
					ResultSet msmsrs = msmsps.executeQuery();
					while(msmsrs.next()) {
		
						MsPoint p = new MsPoint(msmsrs.getDouble("MZ"), msmsrs.getDouble("INTENSITY"));
						feature.getSpectrum().add(p);
						if(p.getMz() == precursorMz)
							feature.setParent(p);
		
						if(msmsrs.getString("FRAGMENT_COMMENT") != null)
							feature.getMassAnnotations().put(p, msmsrs.getString("FRAGMENT_COMMENT"));
					}
					msmsrs.close();
					CompoundIdentity compoundIdentity =
							CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
		
					feature.setCompoundIdentity(compoundIdentity);
				}
				rs.close();
			}
		}
		return feature;
	}
	
	public static Map<String,String>getMsMsFeatureProperties(String featureId) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		Map<String,String>properties = getMsMsFeatureProperties(featureId, conn);
		ConnectionManager.releaseConnection(conn);
		return properties;
	}
	
	public static Map<String,String>getMsMsFeatureProperties(String featureId, Connection conn ) throws SQLException {
		
		Map<String,String>properties = new TreeMap<String,String>();
		String query = "SELECT PROPERTY_NAME, PROPERTY_VALUE FROM REF_MSMS_PROPERTIES WHERE MRC2_LIB_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, featureId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String value = rs.getString("PROPERTY_VALUE");
				if(value != null && !value.trim().isEmpty())
					properties.put(rs.getString("PROPERTY_NAME"), value);
			}		
			rs.close();
		}
		return properties;
	}
	
	public static Collection<String>getMsMsFeatureLibraryAnnotations(String featureId) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String>annotations = getMsMsFeatureLibraryAnnotations(featureId, conn);
		ConnectionManager.releaseConnection(conn);
		return annotations;
	}
	
	public static Collection<String>getMsMsFeatureLibraryAnnotations(String featureId, Connection conn ) throws SQLException {
		
		Collection<String>annotations = new TreeSet<String>();
		String query = "SELECT PROPERTY_VALUE FROM REF_MSMS_PROPERTIES WHERE PROPERTY_NAME = ? AND MRC2_LIB_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, ANNOTATION_FIELD_NAME);
			ps.setString(2, featureId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String value = rs.getString("PROPERTY_VALUE");
				if(value != null && !value.trim().isEmpty())
					annotations.add(value);
			}		
			rs.close();																
		}
		return annotations;
	}

	public static MsMsLibraryFeature getMsMsLibraryFeatureByOriginalLibraryId(
			String libraryName, String spectrumId) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		MsMsLibraryFeature f = getMsMsLibraryFeatureByOriginalLibraryId(libraryName, spectrumId, conn);
		ConnectionManager.releaseConnection(conn);
		return f;
	}

	public static MsMsLibraryFeature getMsMsLibraryFeatureByOriginalLibraryId(
			String libraryName, 
			String spectrumId,
			Connection conn) throws SQLException {

		MsMsLibraryFeature feature = null;
		String query = 
				"SELECT MRC2_LIB_ID, C.POLARITY, C.IONIZATION_TYPE, C.COLLISION_ENERGY, C.PRECURSOR_MZ, "
				+ "C.ADDUCT, C.COLLISION_GAS, C.INSTRUMENT, C.INSTRUMENT_TYPE, C.IN_SOURCE_VOLTAGE, "
				+ "C.MSN_PATHWAY, C.PRESSURE, C.SAMPLE_INLET, C.SPECIAL_FRAGMENTATION, C.SPECTRUM_TYPE, "
				+ "C.CHROMATOGRAPHY_TYPE, C.CONTRIBUTOR, C.SPLASH, RESOLUTION, "
				+ "C.SPECTRUM_SOURCE, C.LIBRARY_NAME, C.ORIGINAL_LIBRARY_ID, C.ENTROPY "
				+ "FROM REF_MSMS_LIBRARY_COMPONENT C, " 
				+ "REF_MSMS_LIBRARY_LIST L " + "WHERE L.SEARCH_OUTPUT_CODE = ? "
				+ "AND ORIGINAL_LIBRARY_ID = ? " 
				+ "AND C.LIBRARY_NAME = L.PRIMARY_LIBRARY_ID ";
		String msmsQuery = "SELECT MZ, INTENSITY, FRAGMENT_COMMENT FROM REF_MSMS_LIBRARY_PEAK "
				+ "WHERE MRC2_LIB_ID = ? ORDER BY 1";

		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, libraryName);
			ps.setString(2, spectrumId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String mrc2msmsId = rs.getString("MRC2_LIB_ID");
				feature = DiskCacheUtils.retrieveMsMsLibraryFeatureFromCache(mrc2msmsId);
				if (feature != null)
					break;

				feature = new MsMsLibraryFeature(mrc2msmsId,
						Polarity.getPolarityByCode(MSMSComponentTableFields.POLARITY.name()));
				feature.setSpectrumSource(
						SpectrumSource.getOptionByName(rs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
				feature.setIonizationType(IDTDataCache
						.getIonizationTypeById(rs.getString(MSMSComponentTableFields.IONIZATION_TYPE.name())));
				feature.setCollisionEnergyValue(rs.getString(MSMSComponentTableFields.COLLISION_ENERGY.name()));
				feature.setSpectrumEntropy(rs.getDouble(MSMSComponentTableFields.ENTROPY.name()));

				Map<String, String> properties = feature.getProperties();
				for (MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

					if (!field.equals(MSMSComponentTableFields.PRECURSOR_MZ)
							&& !field.equals(MSMSComponentTableFields.MRC2_LIB_ID)) {

						String value = rs.getString(field.name());
						if (value != null && !value.trim().isEmpty())
							properties.put(field.getName(), value);
					}
				}
				// Add spectrum
				double precursorMz = rs.getDouble(MSMSComponentTableFields.PRECURSOR_MZ.name());

				try (PreparedStatement msmsps = conn.prepareStatement(msmsQuery)) {
					ResultSet msmsrs = msmsps.executeQuery();
					while (msmsrs.next()) {

						MsPoint p = new MsPoint(msmsrs.getDouble("MZ"), msmsrs.getDouble("INTENSITY"));
						feature.getSpectrum().add(p);
						if (p.getMz() == precursorMz)
							feature.setParent(p);

						if (msmsrs.getString("FRAGMENT_COMMENT") != null)
							feature.getMassAnnotations().put(p, msmsrs.getString("FRAGMENT_COMMENT"));
					}
					msmsrs.close();
				}
				CompoundIdentity compoundIdentity = 
						CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
				feature.setCompoundIdentity(compoundIdentity);
				break;
			}
			rs.close();
		}
		return feature;
	}

	public static String insertNewReferenceMsMsLibraryFeature(
			TandemMassSpectrum msms,
			CompoundIdentity cid,
			String libraryName,
			String libraryId) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String newFeatureId = insertNewReferenceMsMsLibraryFeature(
				msms, 
				cid.getPrimaryDatabaseId(), 
				libraryName, 
				libraryId, 
				conn);
		ConnectionManager.releaseConnection(conn);
		return newFeatureId;
	}
	
	public static String insertNewReferenceMsMsLibraryFeature(
			TandemMassSpectrum msms,
			String accession,
			String libraryName,
			String libraryId) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String newFeatureId = insertNewReferenceMsMsLibraryFeature(
				msms, accession, libraryName, libraryId, conn);
		ConnectionManager.releaseConnection(conn);
		return newFeatureId;
	}

	public static String insertNewReferenceMsMsLibraryFeature(
			TandemMassSpectrum msms,
			String accession,
			String libraryName,
			String libraryId,
			Connection conn) throws SQLException {

		//	Get next ID
		String libId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_LIB_ENTRY_SEQ",
				DataPrefix.MSMS_LIBRARY_ENTRY,
				"0",
				9);
		//	Insert component data
		MsPoint parent = null;
		String query =
			"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( " +
			"MRC2_LIB_ID, POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ, SPECTRUM_TYPE,  " +
			"SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, ENTROPY) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, libId);
			ps.setString(2, msms.getPolarity().getCode());
			ps.setString(3, msms.getIonisationType());
			ps.setDouble(4, msms.getCidLevel());
	
			Double precursorMz = null;
			parent = msms.getParent();
			if(parent != null)
				precursorMz = parent.getMz();
	
			ps.setDouble(5, precursorMz);
			ps.setString(6, "MS" + Integer.toString(msms.getDepth()));
			ps.setString(7, msms.getSpectrumSource().name());
			ps.setString(8, msms.getIonisationType());
			ps.setString(9, libraryName);
			ps.setString(10, libraryId);
			ps.setString(11, accession);
			ps.setString(12, MsUtils.calculateSpectrumHash(msms.getSpectrum()));		
			ps.setDouble(13, MsUtils.calculateSpectrumEntropy(msms.getSpectrum()));
			ps.executeUpdate();
		}

		// 	Insert spectrum peaks
		query =
			"INSERT INTO REF_MSMS_LIBRARY_PEAK (MRC2_LIB_ID, MZ, INTENSITY, IS_PARENT) " +
			"VALUES(?, ?, ?, ?) ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, libId);
			boolean parentInSpectrum = false;
			for(MsPoint p : msms.getSpectrum()) {
	
				ps.setDouble(2, p.getMz());
				ps.setDouble(3, p.getIntensity());
				ps.setString(4, null);
				if(parent != null && p.getMz() == parent.getMz()) {
					ps.setString(4, "Y");
					parentInSpectrum = true;					
				}
				ps.addBatch();
			}
			if(!parentInSpectrum && parent != null) {
	
				ps.setDouble(2, parent.getMz());
				ps.setDouble(3, parent.getIntensity());
				ps.setString(4, "Y");
				ps.addBatch();
			}
			ps.executeBatch();
		}
		return libId;
	}
	
	public static Collection<MsMsLibraryFeature>getMsMsLibraryFeaturesForCompound(String accession) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<MsMsLibraryFeature>msmsLibFeatures = getMsMsLibraryFeaturesForCompound(accession, conn);
		ConnectionManager.releaseConnection(conn);
		return msmsLibFeatures;
	}
	
	public static Collection<MsMsLibraryFeature>getMsMsLibraryFeaturesForCompound(String accession, Connection conn) throws SQLException {
		
		Collection<MsMsLibraryFeature>msmsLibFeatures = new ArrayList<MsMsLibraryFeature>();
		String sql = "SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT WHERE ACCESSION = ? ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, accession);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				MsMsLibraryFeature lf = getMsMsLibraryFeatureById(rs.getString("MRC2_LIB_ID"), conn);
				msmsLibFeatures.add(lf);
			}
			rs.close();
		}	
		return msmsLibFeatures;
	}
	
	public static Collection<String>getFilteredLibraryIds(
			String libraryId, 
			Polarity polarity, 
			MSMSResolution resolution, 
			SpectrumSource spectrumSource) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String>ids = getFilteredLibraryIds(libraryId, polarity, resolution, spectrumSource, conn);
		ConnectionManager.releaseConnection(conn);		
		return ids;
	}

	public static Collection<String>getFilteredLibraryIds(
			String libraryId, 
			Polarity polarity, 
			MSMSResolution resolution, 
			SpectrumSource spectrumSource,
			Connection conn) throws SQLException {
		
		Collection<String> ids = new ArrayList<String>();
		String sql = 
				"SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT  " +
				"WHERE ACCESSION IS NOT NULL AND LIBRARY_NAME = ? " +
				"AND POLARITY = ? " + 
				"AND SPECTRUM_SOURCE = ? ";
		
		if(!resolution.equals(MSMSResolution.ANY))
			sql += "AND RESOLUTION = ? ";
		
		sql += "ORDER BY 1 ";

		try(PreparedStatement ps = conn.prepareStatement(sql)){	
			ps.setString(1, libraryId);
			ps.setString(2, polarity.getCode());
			ps.setString(3, spectrumSource.name());
			
			if(!resolution.equals(MSMSResolution.ANY))
				ps.setString(4, resolution.name());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				ids.add(rs.getString("MRC2_LIB_ID"));
	
			rs.close();
		}	
		return ids;
	}
	
	public static String getAccessionByLibraryFeatureId(
			String featureId,
			Connection conn) throws SQLException {

		String accession = null;
		String query =
			"SELECT ACCESSION FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setString(1, featureId);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				accession = rs.getString("ACCESSION");
			
			rs.close();
		}
		return accession;
	}
}


















