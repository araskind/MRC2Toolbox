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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundConcentration;
import edu.umich.med.mrc2.datoolbox.data.CompoundDatabase;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameCategory;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameScope;
import edu.umich.med.mrc2.datoolbox.data.enums.InChiKeyPortion;
import edu.umich.med.mrc2.datoolbox.data.enums.StringMatchFidelity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.DiskCacheUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class CompoundDatabaseUtils {

	public static CompoundIdentity mapLibraryCompoundIdentity(CompoundIdentity cid) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		CompoundIdentity mapped = mapLibraryCompoundIdentity(cid,conn);
		ConnectionManager.releaseConnection(conn);
		return mapped;
	}

	public static CompoundIdentity mapLibraryCompoundIdentity(
			CompoundIdentity cid, Connection conn) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		String accession = null;
		CompoundIdentity mappedId = null;
		if(!cid.getDbIdMap().isEmpty()) {

			ps = conn.prepareStatement(
				"SELECT ACCESSION FROM COMPOUND_DATA " +
				" WHERE ACCESSION = ? and SOURCE_DB = ?");
			for (Entry<CompoundDatabaseEnum, String> entry : cid.getDbIdMap().entrySet()) {

				ps.setString(1, entry.getValue());
				ps.setString(2, entry.getKey().name());
				rs = ps.executeQuery();
				while (rs.next()) {
					accession = rs.getString("ACCESSION");
					break;
				}
				if(accession != null) {
					rs.close();
					ps.close();
					mappedId = getCompoundById(accession, conn);
					mappedId.addDatabaseIds(cid.getDbIdMap(), false);
					return mappedId;
				}
				rs.close();
			}
			//	Check cross-reference table
			ps = conn.prepareStatement(
				"SELECT ACCESSION FROM COMPOUND_CROSSREF " +
				"WHERE SOURCE_DB_ID = ? AND SOURCE_DB = ?");
			for (Entry<CompoundDatabaseEnum, String> entry : cid.getDbIdMap().entrySet()) {

				//System.out.println( entry.getKey().name() + " ~ " + entry.getValue());
				ps.setString(1, entry.getValue());
				ps.setString(2, entry.getKey().name());
				rs = ps.executeQuery();
				while (rs.next()) {
					accession = rs.getString("ACCESSION");
					break;
				}
				if(accession != null) {
					rs.close();
					ps.close();
					mappedId = getCompoundById(accession, conn);
					mappedId.addDatabaseIds(cid.getDbIdMap(), false);
					return mappedId;
				}
				rs.close();
			}
		}
		//	Check InChi key
		if(cid.getInChiKey() != null && !cid.getInChiKey().isEmpty()) {
			mappedId = getCompoundByInChiKey(cid.getInChiKey(), conn);
			if(mappedId != null) 
				return mappedId;
		}		
		//	Check synonyms
		if(cid.getFormula() != null) {

			if(!cid.getFormula().isEmpty()) {

				ps = conn.prepareStatement(
					"SELECT D.ACCESSION FROM COMPOUND_SYNONYMS S, COMPOUND_DATA D "
					+ "WHERE UPPER(S.NAME) = ? AND D.MOL_FORMULA = ? "
					+ "AND D.ACCESSION = S.ACCESSION");
				
				String mf = cid.getFormula();
				if(mf.contains("[") || mf.contains("D"))
					mf = mf.replaceAll("\\[13C\\]", "C").replaceAll("\\[15N\\]", "N").replaceAll("D", "H");
					
				String cleanName = 
						cid.getName().toUpperCase().replace("[ISTD]", "").replaceAll("_.+_.+$", "").trim();
				ps.setString(1, cleanName);
				ps.setString(2, mf);
				rs = ps.executeQuery();
				while (rs.next()) {
					accession = rs.getString("ACCESSION");
					break;
				}
				if(accession != null) {
					rs.close();
					ps.close();
					mappedId =  getCompoundById(accession, conn);
					mappedId.addDatabaseIds(cid.getDbIdMap(), false);
					return mappedId;
				}
			}
		}
		if(rs != null)
			rs.close();

		ps.close();
		return null;
	}

	public static CompoundIdentity getCompoundBySmiles(String smiles) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		CompoundIdentity identity = getCompoundBySmiles(smiles, conn);
		ConnectionManager.releaseConnection(conn);

		return identity;
	}

	public static CompoundIdentity getCompoundBySmiles(String smiles, Connection conn) throws Exception{

		CompoundIdentity identity = null;
		String query =
			"SELECT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
			+ "D.MOL_FORMULA, D.EXACT_MASS, D.SMILES, D.INCHI_KEY "+
			"FROM COMPOUND_DATA D WHERE D.SMILES = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, smiles);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundDatabaseEnum dbSource =
					CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
			String accession = rs.getString("ACCESSION");
			String commonName = rs.getString("PRIMARY_NAME");
			String formula = rs.getString("MOL_FORMULA");
			double exactMass = rs.getDouble("EXACT_MASS");
			identity = new CompoundIdentity(
					dbSource, accession, commonName,
					commonName, formula, exactMass, smiles);
			identity.setInChiKey(rs.getString("INCHI_KEY"));
		}
		rs.close();
		ps.close();
		return identity;
	}
	
	public static Collection<CompoundIdentity> getCompoundsByInChiKey(
			String inchiKey, 
			InChiKeyPortion portion, 
			Range massRange) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundIdentity>identities = getCompoundsByInChiKey(inchiKey, portion, massRange, conn);
		ConnectionManager.releaseConnection(conn);

		return identities;
	}
	
	public static Collection<CompoundIdentity> getCompoundsByInChiKey(
			String inchiKey, 
			InChiKeyPortion portion, 
			Range massRange,
			Connection conn) throws Exception {
		
		Collection<CompoundIdentity>identities = new ArrayList<CompoundIdentity>();
		String query =
				"SELECT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
				+ "D.MOL_FORMULA, D.EXACT_MASS, D.SMILES, D.INCHI_KEY "+
				"FROM COMPOUND_DATA D WHERE ";
		
		String queryInChiKey = inchiKey;
		if(portion.equals(InChiKeyPortion.COMPLETE))
			query += "INCHI_KEY = ? ";
		
		if(portion.equals(InChiKeyPortion.STRUCTURE_ONLY)) {
			query += "INCHI_KEY_CONNECT = ? ";
			queryInChiKey = inchiKey.substring(0, 14);
		}
		if(massRange != null)
			query += "AND EXACT_MASS BETWEEN ? AND ? ";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, queryInChiKey);
		if(massRange != null) {
			ps.setDouble(2, massRange.getMin());
			ps.setDouble(3, massRange.getMax());
		}
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundDatabaseEnum dbSource =
					CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
			String accession = rs.getString("ACCESSION");
			String commonName = rs.getString("PRIMARY_NAME");
			String formula = rs.getString("MOL_FORMULA");
			String smiles = rs.getString("SMILES");
			double exactMass = rs.getDouble("EXACT_MASS");
			CompoundIdentity identity = new CompoundIdentity(
					dbSource, accession, commonName,
					commonName, formula, exactMass, smiles);
			identity.setInChiKey(rs.getString("INCHI_KEY"));
			identities.add(identity);
		}
		rs.close();
		ps.close();					
		return identities;
	}
	
	public static CompoundIdentity getCompoundByInChiKey(String inchiKey) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		CompoundIdentity identity = getCompoundByInChiKey(inchiKey, conn);
		ConnectionManager.releaseConnection(conn);

		return identity;
	}

	public static CompoundIdentity getCompoundByInChiKey(String inchiKey, Connection conn) throws Exception{

		CompoundIdentity identity = null;
		String query =
			"SELECT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
			+ "D.MOL_FORMULA, D.EXACT_MASS, D.SMILES, D.INCHI_KEY "+
			"FROM COMPOUND_DATA D WHERE D.INCHI_KEY = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, inchiKey);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundDatabaseEnum dbSource =
					CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
			String accession = rs.getString("ACCESSION");
			String commonName = rs.getString("PRIMARY_NAME");
			String formula = rs.getString("MOL_FORMULA");
			String smiles = rs.getString("SMILES");
			double exactMass = rs.getDouble("EXACT_MASS");
			identity = new CompoundIdentity(
					dbSource, accession, commonName,
					commonName, formula, exactMass, smiles);
			identity.setInChiKey(rs.getString("INCHI_KEY"));
		}
		rs.close();
		ps.close();
		return identity;
	}

	public static CompoundIdentity getCompoundById(String accession) throws Exception {
		
		CompoundIdentity identity = DiskCacheUtils.retrieveCompoundIdentityFromCache(accession);
		if(identity != null)
			return identity;
		
		Connection conn = ConnectionManager.getConnection();
		identity = getCompoundById(accession, conn);
		ConnectionManager.releaseConnection(conn);

		return identity;
	}

	public static CompoundIdentity getCompoundById(
			String accession, Connection conn) throws Exception{

		CompoundIdentity identity = DiskCacheUtils.retrieveCompoundIdentityFromCache(accession);
		if(identity != null)
			return identity;
		
		String query =
			"SELECT D.SOURCE_DB, D.PRIMARY_NAME, D.MOL_FORMULA, "
			+ "D.EXACT_MASS, D.SMILES, D.INCHI_KEY "+
			"FROM COMPOUND_DATA D WHERE D.ACCESSION = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, accession);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundDatabaseEnum dbSource =
					CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
			String commonName = rs.getString("PRIMARY_NAME");
			String formula = rs.getString("MOL_FORMULA");
			double exactMass = rs.getDouble("EXACT_MASS");
			String smiles = rs.getString("SMILES");
			identity = new CompoundIdentity(
					dbSource, accession, commonName,
					commonName, formula, exactMass, smiles);
			identity.setInChiKey(rs.getString("INCHI_KEY"));
		}
		rs.close();
		ps.close();
		return identity;
	}

	public static CompoundIdentity getCompoundIdentityByName(
			String featureName) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		CompoundIdentity identity = getCompoundIdentityByName(featureName, conn);
		ConnectionManager.releaseConnection(conn);
		return identity;
	}

	//	TODO remove after checking. This is obsolete method for compatibility with old libraries
	public static CompoundIdentity getCompoundIdentityByName(
			String featureName, Connection conn) throws Exception {

		CompoundIdentity pcId = null;
		String query =
			"SELECT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
			+ "D.MOL_FORMULA, D.EXACT_MASS, D.INCHI_KEY, D.SMILES "+
			"FROM CID_LOOKUP L, COMPOUND_DATA D WHERE UPPER(L.CPD_NAME) = ? "
			+ "AND L.ACCESSION = D.ACCESSION" ;

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureName.toUpperCase());
		ResultSet rs = ps.executeQuery();

		while (rs.next()){

			pcId = new CompoundIdentity();
			pcId.setCommonName(rs.getString("PRIMARY_NAME"));
			pcId.setFormula(rs.getString("MOL_FORMULA"));
			pcId.setExactMass(rs.getDouble("EXACT_MASS"));
			pcId.setSmiles(rs.getString("SMILES"));
			pcId.setInChiKey(rs.getString("INCHI_KEY"));

			String accession = rs.getString("ACCESSION");
			String sourceDb = rs.getString("SOURCE_DB");

			for( CompoundDatabaseEnum db : CompoundDatabaseEnum.values()){

				if(db.getName().equals(sourceDb)){

					pcId.addDbId(db, accession);
					break;
				}
			}
		}
		rs.close();
		ps.close();
		return pcId;
	}

	public static List<CompoundIdentity> findCompounds(
			String cpdName,
			boolean exactMatch,
			boolean searchSynonyms,
			boolean allowSpellingErrors,
			String molFormula,
			String cpdId,
			String inchi,
			Range massRange) throws Exception {

		List<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query = "";
		String synonymQuery = "";
		int nameId = 0;
		int synonymId = 0;
		int formulaId = 0;
		int accId = 0;
		int inchiId = 0;
		int minMassId = 0;
		int maxMassId = 0;
		ArrayList<String>partQueries = new ArrayList<String>();
		char escapeChar = '!';
		int fieldCount = 1;
		if(!cpdName.isEmpty()) {

			if(searchSynonyms) {
				if(exactMatch && allowSpellingErrors)
					synonymQuery = "UPPER(S.NAME) = ?";
				else
					synonymQuery = "UPPER(S.NAME) LIKE ? {ESCAPE '" + escapeChar + "'}";

				synonymId = fieldCount;
				fieldCount++;
			}
			else {
				if(exactMatch)
					partQueries.add("UPPER(D.PRIMARY_NAME) = ?");
				else
					partQueries.add("UPPER(D.PRIMARY_NAME) LIKE ? {ESCAPE '" + escapeChar + "'}");

				nameId = fieldCount;
				fieldCount++;
			}
		}
		if(!molFormula.isEmpty()) {
			partQueries.add("D.MOL_FORMULA = ?");
			formulaId = fieldCount;
			fieldCount++;
		}
		if(!cpdId.isEmpty()) {
			partQueries.add("D.ACCESSION = ?");
			accId = fieldCount;
			fieldCount++;
		}
		if(!inchi.isEmpty()) {
			partQueries.add("D.INCHI_KEY = ?");
			inchiId = fieldCount;
			fieldCount++;
		}
		if(massRange != null) {
			partQueries.add("D.EXACT_MASS >= ? AND D.EXACT_MASS <= ?");
			minMassId = fieldCount;
			fieldCount++;
			maxMassId = fieldCount;
			fieldCount++;
		}
		if(!cpdName.isEmpty() && searchSynonyms) {

			String partq = "";
			if(!partQueries.isEmpty())
				partq = "AND " + StringUtils.join(partQueries, " AND ");

			query =
				"SELECT DISTINCT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
				+ "D.MOL_FORMULA, D.EXACT_MASS, D.INCHI_KEY, D.SMILES " +
				"FROM COMPOUND_DATA D, COMPOUND_SYNONYMS S " +
				"WHERE " + synonymQuery +
				" AND D.ACCESSION = S.ACCESSION " + partq + " ORDER BY D.PRIMARY_NAME ASC";
		}
		else {
			query =
				"SELECT DISTINCT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
				+ "D.MOL_FORMULA, D.EXACT_MASS, D.INCHI_KEY, D.SMILES " +
				"FROM COMPOUND_DATA D WHERE " + StringUtils.join(partQueries, " AND ") +
				" ORDER BY D.PRIMARY_NAME ASC";
		}
		PreparedStatement ps = conn.prepareStatement(query);

		if(synonymId > 0) {

			if(exactMatch)
				ps.setString(synonymId,
						TextUtils.escapeForSqlLike(cpdName, escapeChar).toUpperCase());
			else
				ps.setString(synonymId, "%" +
						TextUtils.escapeForSqlLike(cpdName, escapeChar).toUpperCase() + "%");
		}
		if(nameId > 0) {
			if(exactMatch)
				ps.setString(nameId,
						TextUtils.escapeForSqlLike(cpdName, escapeChar).toUpperCase());
			else
				ps.setString(nameId, "%" +
						TextUtils.escapeForSqlLike(cpdName, escapeChar).toUpperCase() + "%");
		}
		if(formulaId > 0)
			ps.setString(formulaId, molFormula);

		if(accId > 0)
			ps.setString(accId, cpdId);

		if(inchiId > 0)
			ps.setString(inchiId, inchi);

		if(minMassId > 0) {
			ps.setDouble(minMassId, massRange.getMin());
			ps.setDouble(maxMassId, massRange.getMax());
		}
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity pcId = 
					DiskCacheUtils.retrieveCompoundIdentityFromCache(rs.getString("ACCESSION"));
			if(pcId != null) {
				idList.add(pcId);
				continue;
			}
			else {
				pcId = new CompoundIdentity();
				pcId.setCommonName(rs.getString("PRIMARY_NAME"));
				pcId.setFormula(rs.getString("MOL_FORMULA"));
				pcId.setExactMass(rs.getDouble("EXACT_MASS"));
				pcId.setSmiles(rs.getString("SMILES"));
				pcId.setInChiKey(rs.getString("INCHI_KEY"));
				CompoundDatabaseEnum db =
						CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
				if(db != null)
					pcId.addDbId(db, rs.getString("ACCESSION"));

				idList.add(pcId);
			}
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}

	public static String getCompoundNarrative(String accession) throws Exception {

		String narrative = "";
		Connection conn = ConnectionManager.getConnection();
		String query =
				"SELECT C.DESCRIPTION, CS_DESCRIPTION FROM "
				+ "COMPOUND_DESCRIPTION C WHERE C.ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, accession);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			if(rs.getString("DESCRIPTION") != null)
				narrative += rs.getString("DESCRIPTION") + "\n\n";

			if(rs.getString("CS_DESCRIPTION") != null)
				narrative += rs.getString("CS_DESCRIPTION");
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return narrative;
	}

	public static List<CompoundIdentity>getDbIdList(String accession) throws Exception {

		List<CompoundIdentity>dbIdMap = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
				"SELECT C.SOURCE_DB, C.SOURCE_DB_ID "
				+ "FROM COMPOUND_CROSSREF C WHERE C.ACCESSION = ? ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, accession);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			CompoundDatabaseEnum db = CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
			if(db != null)
				dbIdMap.add(new CompoundIdentity(db, rs.getString("SOURCE_DB_ID")));
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);

		return dbIdMap;
	}

	public static CompoundNameSet getSynonyms(String accession) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		CompoundNameSet nameSet = getSynonyms(accession, conn);
		ConnectionManager.releaseConnection(conn);
		return nameSet;
	}
	
	public static CompoundNameSet getSynonyms(String accession, Connection conn) throws Exception {

		CompoundNameSet nameSet = new CompoundNameSet(accession);
		String query = "SELECT S.NAME, S.NTYPE FROM COMPOUND_SYNONYMS S WHERE S.ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, accession);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			nameSet.addName(rs.getString("NAME"), rs.getString("NTYPE"));

		rs.close();
		ps.close();
		return nameSet;
	}

	public static void updateSynonyms(CompoundNameSet nameSet) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		updateSynonyms(nameSet, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateSynonyms(CompoundNameSet nameSet, Connection conn) throws Exception {

		String query = "DELETE FROM COMPOUND_SYNONYMS S WHERE S.ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, nameSet.getCompoundAccession());
		ps.executeUpdate();
		ps.close();
		query = "INSERT INTO COMPOUND_SYNONYMS(ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(1, nameSet.getCompoundAccession());
		for (Entry<String, CompoundNameCategory> entry : nameSet.getSynonyms().entrySet()) {

			ps.setString(2, entry.getKey());
			ps.setString(3, entry.getValue().name());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();

		query = "UPDATE COMPOUND_DATA SET PRIMARY_NAME = ? WHERE ACCESSION = ?";
		ps = conn.prepareStatement(query);
		ps.setString(1, nameSet.getPrimaryName());
		ps.setString(2, nameSet.getCompoundAccession());
		ps.executeUpdate();
		ps.close();
	}

	public static CompoundIdentity insertNewCompound(
			CompoundIdentity newCompound, 
			CompoundNameSet nameSet, 
			String description) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String dataQuery =
				"INSERT INTO COMPOUND_DATA " +
				"(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, "
				+ "EXACT_MASS, SMILES, INCHI, INCHI_KEY) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, newCompound.getPrimaryDatabaseId());
		ps.setString(2, newCompound.getPrimaryDatabase().name());
		ps.setString(3, newCompound.getCommonName());
		ps.setString(4, newCompound.getFormula());
		ps.setDouble(5, newCompound.getExactMass());
		ps.setString(6, newCompound.getSmiles());
		ps.setString(7, newCompound.getInChi());
		ps.setString(8, newCompound.getInChiKey());
		ps.executeUpdate();
		ps.close();
		
		//	Description
		dataQuery = "INSERT INTO COMPOUND_DESCRIPTION (ACCESSION, DESCRIPTION) VALUES (?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, newCompound.getPrimaryDatabaseId());
		ps.setString(2, description);
		ps.executeUpdate();
		ps.close();
		
		//	Insert synonyms
		dataQuery = "INSERT INTO COMPOUND_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, newCompound.getPrimaryDatabaseId());
		for(Entry<String, CompoundNameCategory> synonym : nameSet.getSynonyms().entrySet()) {

			ps.setString(2, synonym.getKey());
			ps.setString(3, synonym.getValue().name());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		CompoundIdentity inserted = 
				CompoundDatabaseUtils.getCompoundById(newCompound.getPrimaryDatabaseId(), conn);
		DiskCacheUtils.putCompoundIdentityInCache(inserted);
		ConnectionManager.releaseConnection(conn);
		return inserted;
	}

	public static Collection<String> findCompoundAccessions(
			String cpdName,
			CompoundNameScope nameScope,
			StringMatchFidelity nameMatchFidelity,
			String molFormula,
			String cpdId,
			String inchi,
			Range massRange)  throws Exception {

		Collection<String>idList = new ArrayList<String>();		
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DISTINCT S.ACCESSION  " +
			"FROM COMPOUND_SYNONYMS S, " +
			"COMPOUND_DATA D " +
			"WHERE S.ACCESSION = D.ACCESSION ";

		Map<Integer,SQLParameter>parameterMap = new TreeMap<Integer,SQLParameter>();
		int paramCount = 1;
		if(!molFormula.isEmpty()) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, molFormula));
			query += "AND D.MOL_FORMULA = ? ";
		}
		if(!cpdId.isEmpty()) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, cpdId));
			query += "AND D.ACCESSION = ? ";
		}
		if(!inchi.isEmpty()) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, inchi));
			query += "AND D.INCHI_KEY = ? ";
		}
		if(massRange != null) {

			parameterMap.put(paramCount++, new SQLParameter(Double.class, massRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, massRange.getMax()));
			query += "AND D.EXACT_MASS >= ? AND D.EXACT_MASS <= ? ";
		}
		if(!cpdName.isEmpty()) {

			if(nameScope.equals(CompoundNameScope.PRIMARY_ONLY)) {

				if(nameMatchFidelity.equals(StringMatchFidelity.EXACT_MATCH)) {
					parameterMap.put(paramCount++, new SQLParameter(String.class, cpdName.toUpperCase()));
					query += "AND UPPER(D.PRIMARY_NAME) = ? ";
				}
				if(nameMatchFidelity.equals(StringMatchFidelity.UTL_MATCH)) {

					parameterMap.put(paramCount++, new SQLParameter(String.class, cpdName.toUpperCase()));
					query += "AND UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(D.PRIMARY_NAME), ?) > 80 ";
				}
				if(nameMatchFidelity.equals(StringMatchFidelity.LIKE_MATCH)) {
					parameterMap.put(paramCount++, new SQLParameter(String.class, "%" + cpdName.toUpperCase() + "%"));
					query += "AND UPPER(D.PRIMARY_NAME) LIKE ? ";
				}
			}
			if(nameScope.equals(CompoundNameScope.ALL_SYNONYMS)) {

				if(nameMatchFidelity.equals(StringMatchFidelity.EXACT_MATCH)) {
					parameterMap.put(paramCount++, new SQLParameter(String.class, cpdName.toUpperCase()));
					query += "AND UPPER(S.NAME) = ? ";
				}
				if(nameMatchFidelity.equals(StringMatchFidelity.UTL_MATCH)) {
					parameterMap.put(paramCount++, new SQLParameter(String.class, cpdName.toUpperCase()));
					query += "AND UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(S.NAME), ?) > 80 ";
				}
				if(nameMatchFidelity.equals(StringMatchFidelity.LIKE_MATCH)) {
					parameterMap.put(paramCount++, new SQLParameter(String.class, "%" + cpdName.toUpperCase() + "%"));
					query += "AND UPPER(S.NAME) LIKE ? ";
				}
			}
		}
		PreparedStatement ps = conn.prepareStatement(query);
		for(Entry<Integer, SQLParameter> entry : parameterMap.entrySet()) {

			if(entry.getValue().getClazz().equals(String.class))
				ps.setString(entry.getKey(), (String)entry.getValue().getValue());

			if(entry.getValue().getClazz().equals(Double.class))
				ps.setDouble(entry.getKey(), (Double)entry.getValue().getValue());
		}
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			idList.add(rs.getString("ACCESSION"));

		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;
	}
	
	public static Collection<CompoundConcentration>getConcentrationsForCompound(CompoundIdentity id) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundConcentration>concentrations = getConcentrationsForCompound(id, conn);
		ConnectionManager.releaseConnection(conn);
		return concentrations;
	}
	
	public static Collection<CompoundConcentration>getConcentrationsForCompound(
			CompoundIdentity id, Connection conn) throws Exception {
		
		Collection<CompoundConcentration>concentrations = new ArrayList<CompoundConcentration>();
		String sql = 
				"SELECT BIOFLUID, UNITS, VALUE, AGE, SEX, SUBJECT_CONDITION, "
				+ "COMMENTS, FLAG, CONC_ID, TYPE FROM COMPOUND_CONCENTRATIONS WHERE ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, id.getPrimaryDatabaseId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			CompoundConcentration conc = new CompoundConcentration(
					rs.getString("CONC_ID"),
					id.getPrimaryDatabaseId(), 
					rs.getString("BIOFLUID"), 
					rs.getString("UNITS"), 
					rs.getString("VALUE"),
					rs.getString("AGE"), 
					rs.getString("SEX"), 
					rs.getString("SUBJECT_CONDITION"), 
					rs.getString("COMMENTS"), 
					rs.getString("FLAG"), 
					rs.getString("TYPE"));
			concentrations.add(conc);
		}
		rs.close();
		ps.close();		
		return concentrations;
	}
	
	public static Collection<String>getClassyFireNodesForCompound(String accession) throws Exception {
		
		TreeSet<String>nodes = new TreeSet<>();
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT KINGDOM, SUPERCLASS, CLASS, SUBCLASS, DIRECT_PARENT " +
						"FROM CLASSYFIRE_CLASSIFICATION WHERE ACCESSION = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, accession);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			if(rs.getString("KINGDOM") != null)
				nodes.add(rs.getString("KINGDOM"));

			if(rs.getString("SUPERCLASS") != null)
				nodes.add(rs.getString("SUPERCLASS"));
			
			if(rs.getString("CLASS") != null)
				nodes.add(rs.getString("CLASS"));
			
			if(rs.getString("SUBCLASS") != null)
				nodes.add(rs.getString("SUBCLASS"));
			
			if(rs.getString("DIRECT_PARENT") != null)
				nodes.add(rs.getString("DIRECT_PARENT"));
		}
		rs.close();
		ps.close();
		
		sql = "SELECT TAX_ID FROM CLASSYFIRE_INTERMEDIATE_NODES WHERE ACCESSION = ? ";
		ps = conn.prepareStatement(sql);
		ps.setString(1, accession);
		rs = ps.executeQuery();
		while(rs.next())
			nodes.add(rs.getString("TAX_ID"));
		
		rs.close();
		ps.close();	
		
		ConnectionManager.releaseConnection(conn);
		return nodes;
	}
	
	public static Collection<CompoundDatabase>getCompoundDatabaseList() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundDatabase>databases = getCompoundDatabaseList(conn);
		ConnectionManager.releaseConnection(conn);
		return databases;
	}
	
	public static Collection<CompoundDatabase>getCompoundDatabaseList(Connection conn) throws Exception {
		
		Collection<CompoundDatabase>databases = new TreeSet<>();
		
		
		
		return databases;
	}
	
	public static CompoundIdentity getRefMetCompoundById(
			String refMetId, Connection conn) throws Exception{

		CompoundIdentity identity = DiskCacheUtils.retrieveCompoundIdentityFromCache(refMetId);
		if(identity != null)
			return identity;
		
		String query =
			"SELECT NAME, FORMULA, EXACTMASS, "
			+ "PUBCHEM_CID, CHEBI_ID, HMDB_ID, LIPIDMAPS_ID, KEGG_ID, INCHI_KEY "
			+ "FROM REFMET_DATA_NEW WHERE REFMET_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, refMetId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			identity = new CompoundIdentity(
					CompoundDatabaseEnum.REFMET, 
					refMetId, 
					rs.getString("NAME"),
					rs.getString("FORMULA"), 
					rs.getDouble("EXACTMASS"), 
					null,
					rs.getString("INCHI_KEY"));
			
			identity.addDbId(CompoundDatabaseEnum.PUBCHEM, rs.getString("PUBCHEM_CID"));
			identity.addDbId(CompoundDatabaseEnum.CHEBI, rs.getString("CHEBI_ID"));
			identity.addDbId(CompoundDatabaseEnum.HMDB, rs.getString("HMDB_ID"));
			identity.addDbId(CompoundDatabaseEnum.LIPIDMAPS, rs.getString("LIPIDMAPS_ID"));
			identity.addDbId(CompoundDatabaseEnum.KEGG, rs.getString("KEGG_ID"));
		}
		rs.close();
		ps.close();
		return identity;
	}
	
	
//	public static String getNextMrc2CompoundId() {
//
//		String nextId = null;		
//		try {
//			Connection conn = ConnectionManager.getConnection();		
//			String query  =
//					"SELECT '" + DataPrefix.MRC2_COMPOUND.getName() + 
//					"' || LPAD(MRC2_COMPOUND_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//			
//			PreparedStatement ps = conn.prepareStatement(query);
//			ResultSet rs = ps.executeQuery();
//			while(rs.next()) {
//				nextId = rs.getString("NEXT_ID");
//			}
//			rs.close();
//			ps.close();	
//			ConnectionManager.releaseConnection(conn);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		return nextId;		
//		try {
//			nextId = SQLUtils.getNextIdFromSequence(
//						"MRC2_COMPOUND_SEQ",
//						DataPrefix.MRC2_COMPOUND,
//						"0",
//						4);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		return nextId;
//	}
}

























