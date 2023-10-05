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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSRTLibraryUtils {

	/**
	 * Create new library in the database
	 *
	 * @param libraryName
	 * @param libraryDescription
	 * @return
	 */
	public static String createNewLibrary(CompoundLibrary newLibrary) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query =
			"INSERT INTO MS_LIBRARY " +
			"(LIBRARY_ID, LIBRARY_NAME, DESCRIPTION, ENABLED, "
			+ "DATE_CREATED, LAST_EDITED, POLARITY) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query);		
		String libId = SQLUtils.getNextIdFromSequence(conn, 
				"MS_RT_LIBRARY_SEQ",
				DataPrefix.MS_LIBRARY,
				"0",
				5);
		java.sql.Date sqlCreated = new java.sql.Date((new Date()).getTime());
		stmt.setString(1, libId);
		stmt.setString(2, newLibrary.getLibraryName());
		stmt.setString(3, newLibrary.getLibraryDescription());
		stmt.setString(4, "Y");
		stmt.setDate(5, sqlCreated);
		stmt.setDate(6, sqlCreated);
		stmt.setString(7, newLibrary.getPolarity().getCode());
		stmt.executeUpdate();
		stmt.close();

		ConnectionManager.releaseConnection(conn);
		newLibrary.setLibraryId(libId);
		return libId;
	}

	public static boolean libraryNameExists(String libraryName) throws Exception{

		boolean isInDatabase = false;

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT C.LIBRARY_ID FROM MS_LIBRARY C WHERE C.LIBRARY_NAME = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, libraryName);
		ResultSet rs = stmt.executeQuery();
		if (rs.next())
			isInDatabase = true;

		rs.close();
		stmt.close();
		ConnectionManager.releaseConnection(conn);

		return isInDatabase;
	}

	/**
	 * Delete selected library and all associated MS targets and MSMS data from the database
	 *
	 * @param selected
	 * @throws Exception
	 */
	public static void deleteLibrary(CompoundLibrary selected) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM MS_LIBRARY L WHERE L.LIBRARY_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, selected.getLibraryId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void updateLibraryInfo(CompoundLibrary selected) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE MS_LIBRARY L SET L.LIBRARY_NAME = ?, L.DESCRIPTION = ?, " +
			"L.DATE_CREATED = ?, L.LAST_EDITED = ?, ENABLED = ?, POLARITY = ? " +
			"WHERE L.LIBRARY_ID = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, selected.getLibraryName());
		stmt.setString(2, selected.getLibraryDescription());
		stmt.setDate(3, new java.sql.Date(selected.getDateCreated().getTime()));
		stmt.setDate(4, new java.sql.Date(selected.getLastModified().getTime()));
		String enabled = null;
		if(selected.isEnabled())
			enabled = "Y";

		stmt.setString(5, enabled);
		stmt.setString(6, selected.getPolarity().getCode());
		stmt.setString(7, selected.getLibraryId());
		stmt.executeUpdate();
		stmt.close();

		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteLibraryFeature(LibraryMsFeature selected) throws Exception {

		Connection conn = ConnectionManager.getConnection();

		String query = "DELETE FROM MS_LIBRARY_COMPONENT L WHERE L.TARGET_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, selected.getId());
		stmt.executeUpdate();
		stmt.close();

		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteLibraryFeatures(
			Collection<LibraryMsFeature> features) throws Exception {

		Connection conn = ConnectionManager.getConnection();

		String query = "DELETE FROM MS_LIBRARY_COMPONENT L WHERE L.TARGET_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		for(LibraryMsFeature f : features) {
			stmt.setString(1, f.getId());
			stmt.executeUpdate();
		}
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<CompoundLibrary>getAllLibraries() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundLibrary>allLibs = new TreeSet<CompoundLibrary>();
		String query =
			"SELECT LIBRARY_ID, LIBRARY_NAME, DESCRIPTION, " +
			"ENABLED, DATE_CREATED, LAST_EDITED, POLARITY FROM MS_LIBRARY";
		PreparedStatement stmt = conn.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){

			boolean enabled = true;
			if(rs.getString("ENABLED") == null)
				enabled = false;
			
			Polarity pol = null;
			if(rs.getString("POLARITY") != null)
				pol = Polarity.getPolarityByCode(rs.getString("POLARITY"));

			CompoundLibrary newLib = new CompoundLibrary(
					rs.getString("LIBRARY_ID"),
					rs.getString("LIBRARY_NAME"),
					rs.getString("DESCRIPTION"),
					null,
					new Date(rs.getDate("DATE_CREATED").getTime()),
					new Date(rs.getDate("LAST_EDITED").getTime()),
					enabled,
					pol);

			allLibs.add(newLib);
		}
		rs.close();
		stmt.close();
		ConnectionManager.releaseConnection(conn);

		return allLibs;
	}

	public static Map<String, Integer>getLibraryEntryCount() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Map<String, Integer>counts = new HashMap<String, Integer>();
		String query =
			"SELECT LIBRARY_ID, COUNT(TARGET_ID) AS NUM_TARGETS "+
			"FROM MS_LIBRARY_COMPONENT GROUP BY LIBRARY_ID ORDER BY LIBRARY_ID";

		PreparedStatement stmt = conn.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();

		while(rs.next())
			counts.put(rs.getString("LIBRARY_ID"), rs.getInt("NUM_TARGETS"));

		rs.close();
		stmt.close();
		ConnectionManager.releaseConnection(conn);

		return counts;
	}

	public static CompoundLibrary getLibraryForTarget(String targetId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		CompoundLibrary newLib = null;
		String query =
			"SELECT L.LIBRARY_ID, L.LIBRARY_NAME, L.DESCRIPTION, " +
			"L.ENABLED, L.DATE_CREATED, L.LAST_EDITED, L.POLARITY "
			+ "FROM MS_LIBRARY L, MS_LIBRARY_COMPONENT C "
			+ "WHERE C.LIBRARY_ID = L.LIBRARY_ID  AND C.TARGET_ID = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, targetId);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){

			boolean enabled = true;
			if(rs.getString("ENABLED").equals("N"))
				enabled = false;
			
			Polarity pol = null;
			if(rs.getString("POLARITY") != null)
				pol = Polarity.getPolarityByCode(rs.getString("POLARITY"));

			newLib = new CompoundLibrary(
					rs.getString("LIBRARY_ID"),
					rs.getString("LIBRARY_NAME"),
					rs.getString("DESCRIPTION"),
					null,
					new Date(rs.getDate("DATE_CREATED").getTime()),
					new Date(rs.getDate("LAST_EDITED").getTime()),
					enabled,
					pol);
		}
		rs.close();
		stmt.close();
		ConnectionManager.releaseConnection(conn);

		return newLib;
	}

	public  static Collection<CompoundLibrary> getLibrariesForTargets(
			Collection<String> targetIds) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundLibrary>libraries = new HashSet<CompoundLibrary>();
		Collection<String>libIds = new TreeSet<String>();
		String query = "SELECT LIBRARY_ID FROM MS_LIBRARY_COMPONENT WHERE TARGET_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		for(String targetId : targetIds) {
			ps.setString(1, targetId);
			rs = ps.executeQuery();
			while(rs.next()) {
				libIds.add(rs.getString("LIBRARY_ID"));
				break;
			}
			rs.close();
		}
		ps.close();
		for(String libraryId : libIds)
			libraries.add(getLibrary(libraryId, conn));

		ConnectionManager.releaseConnection(conn);

		return libraries;
	}

	public static CompoundLibrary getLibrary(String libraryId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		CompoundLibrary library = getLibrary(libraryId, conn);
		ConnectionManager.releaseConnection(conn);
		return library;
	}

	public static CompoundLibrary getLibrary(String libraryId, Connection conn) throws Exception {

		CompoundLibrary library = null;
		String query =
			"SELECT LIBRARY_ID, LIBRARY_NAME, DESCRIPTION, "
			+ "ENABLED, DATE_CREATED, LAST_EDITED, POLARITY "+
			"FROM MS_LIBRARY WHERE LIBRARY_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, libraryId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			boolean enabled = true;
			if(rs.getString("ENABLED") == null)
				enabled = false;
			
			Polarity pol = null;
			if(rs.getString("POLARITY") != null)
				pol = Polarity.getPolarityByCode(rs.getString("POLARITY"));

			library = new CompoundLibrary(
				rs.getString("LIBRARY_ID"),
				rs.getString("LIBRARY_NAME"),
				rs.getString("DESCRIPTION"),
				null,
				new Date(rs.getDate("DATE_CREATED").getTime()),
				new Date(rs.getDate("LAST_EDITED").getTime()),
				enabled,
				pol);
		}
		rs.close();
		ps.close();
		return library;
	}

	private static boolean insertNewLibraryEntry(
			LibraryMsFeature lt, Connection conn) throws Exception{

		boolean inserted = false;

		String query =
			"INSERT INTO MS_LIBRARY_COMPONENT " +
			"(TARGET_ID, ACCESSION, DATE_LOADED, LAST_MODIFIED, " +
			"RETENTION_TIME, RT_MIN, RT_MAX, NAME, ID_CONFIDENCE, LIBRARY_ID, ENABLED) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query);
		java.sql.Date sqlCreated = new java.sql.Date(lt.getDateCreated().getTime());
		java.sql.Date sqlModified = new java.sql.Date(lt.getLastModified().getTime());
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MS_RT_LIBRARY_TARGET_SEQ",
				DataPrefix.MS_LIBRARY_TARGET,
				"0",
				7);
		lt.setId(newId);
		
		stmt.setString(1, newId);
		stmt.setString(2, lt.getPrimaryIdentity().getCompoundIdentity().getPrimaryDatabaseId());
		stmt.setDate(3, sqlCreated);
		stmt.setDate(4, sqlModified);
		stmt.setDouble(5, lt.getRetentionTime());
		double rtMin = lt.getRetentionTime();
		double rtMax = lt.getRetentionTime();
		if(lt.getRtRange() != null) {

			rtMin = lt.getRtRange().getMin();
			rtMax = lt.getRetentionTime();
		}
		stmt.setDouble(6, rtMin);
		stmt.setDouble(7, rtMax);
		stmt.setString(8, lt.getPrimaryIdentity().getName());
		stmt.setString(9, lt.getPrimaryIdentity().getConfidenceLevel().getLevelId());
		stmt.setString(10, lt.getLibraryId());
		stmt.setString(11, "Y");	//	Enable by default
		stmt.executeUpdate();
		inserted = true;
		stmt.close();

		return inserted;
	}

	public static void updateLibraryEntry(LibraryMsFeature lt) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stmt;

		//	Update spectra
		String query = "DELETE FROM MS_LIBRARY_COMPONENT_ADDUCT WHERE TARGET_ID = ?";
		stmt = conn.prepareStatement(query);
		stmt.setString(1, lt.getId());
		stmt.executeUpdate();
		stmt.close();

		for (Adduct cm : lt.getSpectrum().getAdducts())
			insertAdduct(lt.getId(), cm, conn);

		//	Update MSMS if available
		query = "DELETE FROM MSMS_LIBRARY_COMPONENT WHERE PARENT_TARGET_ID = ?";
		stmt = conn.prepareStatement(query);
		stmt.setString(1, lt.getId());
		stmt.executeUpdate();
		stmt.close();

		for(TandemMassSpectrum msms : lt.getSpectrum().getTandemSpectra())
			insertTandemSpectrum(msms, lt, conn);

		//	Update feature
		query =
			"UPDATE MS_LIBRARY_COMPONENT " +
			"SET ACCESSION = ?, LAST_MODIFIED = ?, "
			+ "RETENTION_TIME = ?, RT_MIN = ?, RT_MAX = ?, " +
			"NAME = ?, ID_CONFIDENCE = ? WHERE TARGET_ID = ?";

		stmt = conn.prepareStatement(query);
		java.sql.Date sqlModified = new java.sql.Date(lt.getLastModified().getTime());
		stmt.setString(1, lt.getPrimaryIdentity().getCompoundIdentity().getPrimaryDatabaseId());
		stmt.setDate(2, sqlModified);
		stmt.setDouble(3, lt.getRetentionTime());

		double rtMin = lt.getRetentionTime();
		double rtMax = lt.getRetentionTime();
		if(lt.getRtRange() != null) {

			rtMin = lt.getRtRange().getMin();
			rtMax = lt.getRetentionTime();
		}
		stmt.setDouble(4, rtMin);
		stmt.setDouble(5, rtMax);
		stmt.setString(6, lt.getName());
		stmt.setString(7, lt.getPrimaryIdentity().getConfidenceLevel().getLevelId());
		stmt.setString(8, lt.getId());
		stmt.executeUpdate();
		stmt.close();

		ConnectionManager.releaseConnection(conn);
	}

	//	TODO use REF_MSMS ... tables?
	private static void insertTandemSpectrum(
			TandemMassSpectrum msms,
			LibraryMsFeature lt,
			Connection conn) throws Exception {

		//	Insert MSMS entry in MSMS components table
		String query =
			"INSERT INTO MSMS_LIBRARY_COMPONENT " +
			"(MSMS_TARGET_ID, PARENT_TARGET_ID, FRAG_VOLTAGE, CID_VALUE, MS_LEVEL) "+
			"VALUES (?, ?, ?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, msms.getId());
		stmt.setString(2, lt.getId());
		stmt.setDouble(3, msms.getFragmenterVoltage());
		stmt.setDouble(4, msms.getCidLevel());
		stmt.setInt(5, msms.getDepth());
		stmt.executeUpdate();
		stmt.close();

		query =
			"INSERT INTO MSMS_LIBRARY_PEAK (MSMS_TARGET_ID, MZ, INTENSITY, IS_PARENT) " +
			"VALUES (?, ?, ?, ?)";
		stmt = conn.prepareStatement(query);
		stmt.setString(1, msms.getId());
		MsPoint parent = msms.getParent();
		for (MsPoint p : msms.getSpectrum()) {

			stmt.setDouble(2, p.getMz());
			stmt.setDouble(3, p.getIntensity());
			if(p.equals(parent))
				stmt.setString(4, "Y");
			else
				stmt.setString(4, null);

			stmt.addBatch();
		}
		stmt.executeBatch();
		stmt.close();
	}

	private static void insertAdduct(
			String targetId, Adduct adduct, Connection conn) throws SQLException {

		String query =
			"INSERT INTO MS_LIBRARY_COMPONENT_ADDUCT "
			+ "(TARGET_ID, ADDUCT_ID, COMPOSITE_ADDUCT_ID) VALUES (?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query);		
		stmt.setString(1, targetId);
		
		String adductId = null;
		String caId = null;
		if(adduct instanceof SimpleAdduct)
			adductId = adduct.getId();
		
		if(adduct instanceof CompositeAdduct)
			caId = adduct.getId();
		
		stmt.setString(2, adductId);
		stmt.setString(3, caId);
		stmt.executeUpdate();
		stmt.close();
	}

	/**
	 * Load new library feature with all associated information into the database
	 * and attach it to the specified library
	 *
	 * @param lt - new library feature to be inserted
	 * @param libId	- library ID for the parent library
	 * @throws Exception
	 */
	public static void loadLibraryFeature(
			LibraryMsFeature lt, String libId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		loadLibraryFeature(lt, libId, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void loadLibraryFeature(
			LibraryMsFeature lt, String libId, Connection conn) throws Exception {

		if (lt.getId() == null || lt.getId().isEmpty())
			lt.setId(DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString());
		else {
			if (isTargetInDatabase(lt.getId(), conn))
				return;
		}
		lt.setLibraryId(libId);

		if (insertNewLibraryEntry(lt, conn)) {

			if(lt.getSpectrum() != null)
				insertSpectrum(lt, conn);

			for(ObjectAnnotation annotation : lt.getAnnotations())
				AnnotationUtils.insertNewAnnotation(annotation, conn);			
		}
	}

	public static boolean isTargetInDatabase(String targetId, Connection conn) throws Exception{

		boolean isInDatabase = false;
		String query =
			"SELECT C.TARGET_ID FROM MS_LIBRARY_COMPONENT C WHERE C.TARGET_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, targetId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next())
			isInDatabase = true;

		rs.close();
		stmt.close();

		return isInDatabase;
	}

	private static void insertSpectrum(LibraryMsFeature lt, Connection conn) throws Exception{

		for (Adduct cm : lt.getSpectrum().getAdducts())
			 insertAdduct(lt.getId(), cm, conn);

		//	Add tandem MS
		for(TandemMassSpectrum msms : lt.getSpectrum().getTandemSpectra())
			insertTandemSpectrum(msms, lt, conn);
	}

	public static void setTargetEnabled(String targetId, boolean isEnabled) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE MS_LIBRARY_COMPONENT K SET ENABLED = ? WHERE TARGET_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);

		String enabled = null;
		if(isEnabled)
			enabled = "Y";

		stmt.setString(1, enabled);
		stmt.setString(2, targetId);
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void setTargetQcStatus(String targetId, boolean isQc) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
				"UPDATE MS_LIBRARY_COMPONENT SET IS_QC = ? WHERE TARGET_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);

		String qc = null;
		if(isQc)
			qc = "Y";

		stmt.setString(1, qc);
		stmt.setString(2, targetId);
		stmt.executeUpdate();
		stmt.close();

		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<LibraryMsFeatureDbBundle> createFeatureBundlesForLibrary(
			String libraryId, Connection conn) throws SQLException {

		Collection<LibraryMsFeatureDbBundle>bundles = new ArrayList<LibraryMsFeatureDbBundle>();
		String query =
			"SELECT TARGET_ID, ACCESSION, NAME, RETENTION_TIME, RT_MIN, RT_MAX, ID_CONFIDENCE, "+
			"DATE_LOADED, LAST_MODIFIED, ENABLED, IS_QC FROM MS_LIBRARY_COMPONENT "
			+ "WHERE LIBRARY_ID = ? ORDER BY NAME";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, libraryId);
		ResultSet idrs = ps.executeQuery();
		while (idrs.next()) {

			double rt = idrs.getDouble("RETENTION_TIME");
			LibraryMsFeature feature = new LibraryMsFeature(
					idrs.getString("NAME"),
					rt,
					idrs.getString("ENABLED"),
					idrs.getString("TARGET_ID"),
					idrs.getDate("DATE_LOADED").getTime(),
					idrs.getDate("LAST_MODIFIED").getTime());

			feature.setLibraryId(libraryId);
			double rtMin = idrs.getDouble("RT_MIN");
			double rtMax = idrs.getDouble("RT_MAX");
			feature.setRtRange(new Range(rtMin, rtMax));

			boolean qcStandard = false;
			if (idrs.getString("IS_QC") != null)
				qcStandard = true;

			CompoundIdentificationConfidence confidenceLevel  =
					CompoundIdentificationConfidence.getLevelById(idrs.getString("ID_CONFIDENCE"));
			bundles.add(new LibraryMsFeatureDbBundle(
					feature, idrs.getString("ACCESSION"), confidenceLevel, qcStandard));
		}
		idrs.close();
		ps.close();
		return bundles;
	}

	public static LibraryMsFeatureDbBundle createFeatureBundleForFeature(
			String targetId, Connection conn) throws SQLException {

		LibraryMsFeatureDbBundle bundle = null;
		String query =
			"SELECT TARGET_ID, LIBRARY_ID, ACCESSION, NAME, "
			+ "RETENTION_TIME, RT_MIN, RT_MAX, ID_CONFIDENCE, "+
			"DATE_LOADED, LAST_MODIFIED, ENABLED, IS_QC "
			+ "FROM MS_LIBRARY_COMPONENT WHERE LIBRARY_ID = ? ORDER BY NAME";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, targetId);
		ResultSet idrs = ps.executeQuery();
		while (idrs.next()) {

			double rt = idrs.getDouble("RETENTION_TIME");
			LibraryMsFeature feature = new LibraryMsFeature(
					idrs.getString("NAME"),
					rt,
					idrs.getString("ENABLED"),
					idrs.getString("TARGET_ID"),
					idrs.getDate("DATE_LOADED").getTime(),
					idrs.getDate("LAST_MODIFIED").getTime());

			feature.setLibraryId(idrs.getString("LIBRARY_ID"));
			double rtMin = idrs.getDouble("RT_MIN");
			double rtMax = idrs.getDouble("RT_MAX");
			feature.setRtRange(new Range(rtMin, rtMax));

			boolean qcStandard = false;
			if (idrs.getString("IS_QC") != null)
				qcStandard = true;

			CompoundIdentificationConfidence confidenceLevel  =
					CompoundIdentificationConfidence.getLevelById(idrs.getString("ID_CONFIDENCE"));
			bundle = new LibraryMsFeatureDbBundle(
					feature, idrs.getString("ACCESSION"), confidenceLevel, qcStandard);
		}
		idrs.close();
		ps.close();
		return bundle;
	}

	public static void attachMassSpectrum(
			LibraryMsFeature newTarget, Connection conn) throws SQLException {

		MassSpectrum spectrum = new MassSpectrum();
		String query =
				"SELECT ADDUCT_ID, COMPOSITE_ADDUCT_ID FROM MS_LIBRARY_COMPONENT_ADDUCT "
				+ "WHERE TARGET_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newTarget.getId());
		ResultSet msrs = ps.executeQuery();
		Collection<Adduct>adducts = 
				new TreeSet<Adduct>(new AdductComparator(SortProperty.Name));
		while(msrs.next()) {

			Adduct adduct = null;
			String adductId = msrs.getString("ADDUCT_ID");
			if(adductId !=  null) {
				adduct = AdductManager.getAdductById(adductId);
			}
			else {
				adductId = msrs.getString("COMPOSITE_ADDUCT_ID");
				if(adductId !=  null)
					adduct = AdductManager.getAdductById(adductId);
			}			
			if(adduct != null)
				adducts.add(adduct);
		}
		msrs.close();
		ps.close();
		Map<Adduct, Collection<MsPoint>> adductMap =
				MsUtils.createIsotopicPatternCollection(
						newTarget.getPrimaryIdentity().getCompoundIdentity(), adducts);

		adductMap.entrySet().stream().
			forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

		newTarget.setSpectrum(spectrum);
	}

	public static void attachTandemMassSpectrum(
			LibraryMsFeature newTarget, Connection conn) throws SQLException {

		// Select tandem spectra
		String query =
			"SELECT MSMS_TARGET_ID, FRAG_VOLTAGE, CID_VALUE, MS_LEVEL " +
			"FROM MSMS_LIBRARY_COMPONENT WHERE PARENT_TARGET_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newTarget.getId());
		ResultSet result = ps.executeQuery();
		while(result.next()) {

			TandemMassSpectrum msms = new TandemMassSpectrum(
					result.getString("MSMS_TARGET_ID"),
					result.getInt("MS_LEVEL"),
					result.getDouble("FRAG_VOLTAGE"),
					result.getDouble("CID_VALUE"),
					newTarget.getPolarity());	//	TODO check if LibMsFeature polarity always not null

			populateTandemMs(msms, conn);
			newTarget.getSpectrum().addTandemMs(msms);
		}
		result.close();
		ps.close();
	}

	public static void populateTandemMs(
			TandemMassSpectrum msms, Connection conn) throws SQLException {

		String query =
			"SELECT MZ, INTENSITY, IS_PARENT FROM MSMS_LIBRARY_PEAK WHERE MSMS_TARGET_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, msms.getId());
		ResultSet result = ps.executeQuery();
		while(result.next()) {

			MsPoint p = new MsPoint(
					result.getDouble("MZ"),
					result.getDouble("INTENSITY"));

			msms.getSpectrum().add(p);
			if(result.getString("IS_PARENT") != null)
				msms.setParent(p);
		}
		result.close();
		ps.close();
	}

	public static void attachIdentity(
			LibraryMsFeature newTarget,
			String cid,
			boolean qcStandard,
			Connection conn) throws SQLException {

		String query =
			"SELECT SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, SMILES, INCHI_KEY "+
			"FROM COMPOUND_DATA D WHERE D.ACCESSION = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, cid);
		ResultSet cidrs = ps.executeQuery();

		while (cidrs.next()){

			CompoundDatabaseEnum dbSource =
					CompoundDatabaseEnum.getCompoundDatabaseByName(cidrs.getString("SOURCE_DB"));

			CompoundIdentity identity = new CompoundIdentity(
					dbSource,
					cid,
					cidrs.getString("PRIMARY_NAME"),
					cidrs.getString("MOL_FORMULA"),
					cidrs.getDouble("EXACT_MASS"),
					cidrs.getString("SMILES"),
					cidrs.getString("INCHI_KEY"));

			if(dbSource != null){

				//	TODO handle MSMS confidence
				MsFeatureIdentity mid = new MsFeatureIdentity(identity,
						CompoundIdentificationConfidence.ACCURATE_MASS_RT);
				mid.setQcStandard(qcStandard);
				newTarget.setPrimaryIdentity(mid);
				newTarget.setNeutralMass(identity.getExactMass());
			}
			else{
				System.out.println(newTarget.getId() + " : " + cid);
			}
		}
		cidrs.close();
		ps.close();
	}

	public static Map<String, MsFeatureIdentity>
		getCompoundIdentitiesByTargetIds(Collection<String> libraryTargetIdList, String libraryId) throws Exception{

		TreeMap<String, MsFeatureIdentity> identityMap = new TreeMap<String, MsFeatureIdentity>();

		Connection conn = ConnectionManager.getConnection();
		ResultSet rs = null;

		String query =
			"SELECT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
			+ "D.MOL_FORMULA, D.EXACT_MASS, D.SMILES, D.INCHI_KEY, C.RETENTION_TIME, C.IS_QC " +
			"FROM COMPOUND_DATA D, MS_LIBRARY_COMPONENT C "
			+ "WHERE D.ACCESSION = C.ACCESSION AND C.TARGET_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		for(String libraryTargetId : libraryTargetIdList) {

			ps.setString(1, libraryTargetId);
			rs = ps.executeQuery();
			while (rs.next()){

				CompoundDatabaseEnum dbSource =
						CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));

				CompoundIdentity identity = new CompoundIdentity(
						dbSource,
						rs.getString("ACCESSION"),
						rs.getString("PRIMARY_NAME"),
						rs.getString("MOL_FORMULA"),
						rs.getDouble("EXACT_MASS"),
						rs.getString("SMILES"),
						rs.getString("INCHI_KEY"));

				MsFeatureIdentity msId = new MsFeatureIdentity(
						identity, CompoundIdentificationConfidence.ACCURATE_MASS_RT);

				MsRtLibraryMatch libMatch = new MsRtLibraryMatch(libraryTargetId);
				libMatch.setExpectedRetention(rs.getDouble("RETENTION_TIME"));
				libMatch.setScore(100.0d);
				msId.setMsRtLibraryMatch(libMatch);
				msId.setQcStandard(rs.getString("IS_QC") != null);
				identityMap.put(libraryTargetId, msId);
			}
			rs.close();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return identityMap;
	}

	public static LibraryMsFeature getLibraryFeatureById(String targetId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		LibraryMsFeatureDbBundle fBundle =  createFeatureBundleForFeature(targetId, conn);
		if(fBundle != null) {

			LibraryMsFeature newTarget = fBundle.getFeature();

			//	Add identity
			if(fBundle.getConmpoundDatabaseAccession() != null)
				attachIdentity(newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);

			// Attach spectrum
			attachMassSpectrum(newTarget, conn);

			//	Attach MSMS
			attachTandemMassSpectrum(newTarget, conn);

			//	Attach annotations
			attachAnnotations(newTarget, conn);

			return newTarget;
		}
		ConnectionManager.releaseConnection(conn);
		return null;
	}

	public static void attachAnnotations(LibraryMsFeature newTarget, Connection conn) throws Exception {

		Collection<ObjectAnnotation> annotations = AnnotationUtils.getObjectAnnotations(
				AnnotatedObjectType.MS_LIB_FEATURE, newTarget.getId(), conn);

		for(ObjectAnnotation annotation : annotations)
			newTarget.addAnnotation(annotation);
	}
	
	public static void addCompoundToLibrary(
			String accession, String libraryId) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		addCompoundToLibrary(accession, libraryId, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addCompoundToLibrary(
			String accession, String libraryId, Connection conn) throws Exception{

		String query =
			"INSERT INTO MS_LIBRARY_COMPONENT " +
			"(TARGET_ID, ACCESSION, DATE_LOADED, LAST_MODIFIED, LIBRARY_ID, ENABLED) " +
			"VALUES (?, ?, ?, ?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query);
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"MS_RT_LIBRARY_TARGET_SEQ",
				DataPrefix.MS_LIBRARY_TARGET,
				"0",
				7);
		java.sql.Date sqlCreated = new java.sql.Date(new Date().getTime());
		java.sql.Date sqlModified = new java.sql.Date(new Date().getTime());
		
		stmt.setString(1, newId);
		stmt.setString(2, accession);
		stmt.setDate(3, sqlCreated);
		stmt.setDate(4, sqlModified);
		stmt.setString(5, libraryId);
		stmt.setString(6, "Y");	//	Enable by default
		stmt.executeUpdate();
		stmt.close();
	}
}































