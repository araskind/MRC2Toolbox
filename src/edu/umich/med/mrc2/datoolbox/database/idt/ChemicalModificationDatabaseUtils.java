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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.ChemicalModificationsManager;

public class ChemicalModificationDatabaseUtils {

	public static void addNewAdductExchange(AdductExchange newExchange) throws Exception {

//		Connection conn = ConnectionManager.getConnection();
//		String query = "INSERT INTO ADDUCT_EXCHANGE (MOD_ONE_NAME, MOD_TWO_NAME, ENABLED) VALUES (?, ?, ?)";
//		PreparedStatement stmt = conn.prepareStatement(query);
//		stmt.setString(1, newExchange.getLeavingAdduct().getName());
//		stmt.setString(2, newExchange.getComingAdduct().getName());
//		String enabled = "N";
//		if (newExchange.isEnabled())
//			enabled = "Y";
//
//		stmt.setString(3, enabled);
//		stmt.executeUpdate();
//		stmt.close();
//		ConnectionManager.releaseConnection(conn);
	}

	public static void addNewChemicalModification(Adduct newModification) throws Exception {

		Connection conn = ConnectionManager.getConnection();

		String query =
			"INSERT INTO CHEM_MOD (MOD_NAME, MOD_DESCRIPTION, CHARGE, XM, ADDED_GROUP, " +
			"REMOVED_GROUP, MOD_TYPE, MASS_CORRECTION, POLARITY, ENABLED, CEF_NOTATION, SMILES) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query);

		stmt.setString(1, newModification.getName());
		stmt.setString(2, newModification.getDescription());
		stmt.setInt(3, newModification.getCharge());
		stmt.setInt(4, newModification.getOligomericState());
		stmt.setString(5, newModification.getAddedGroup());
		stmt.setString(6, newModification.getRemovedGroup());
		stmt.setString(7, newModification.getModificationType().name());
		stmt.setDouble(8, newModification.getMassCorrection());

		String polarity = "ALL";
		if (newModification.getPolarity().equals(Polarity.Negative))
			polarity = "NEG";

		if (newModification.getPolarity().equals(Polarity.Positive))
			polarity = "POS";

		stmt.setString(9, polarity);

		String enabled = "N";
		if (newModification.isEnabled())
			enabled = "Y";

		stmt.setString(10, enabled);
		stmt.setString(11, newModification.getCefNotation());
		stmt.setString(12, newModification.getSmiles());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static boolean adductExchangeExists(Adduct modOne, Adduct modTwo) throws Exception {

		boolean exists = false;
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT MOD_ONE_NAME FROM ADDUCT_EXCHANGE "
			+ "WHERE (MOD_ONE_NAME = ? AND MOD_TWO_NAME = ?) OR (MOD_ONE_NAME = ? AND MOD_TWO_NAME = ?)";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, modOne.getName());
		stmt.setString(2, modTwo.getName());
		stmt.setString(3, modTwo.getName());
		stmt.setString(4, modOne.getName());
		ResultSet rs = stmt.executeQuery();
		while (rs.next())
			exists = true;

		rs.close();
		ConnectionManager.releaseConnection(conn);
		return exists;
	}

	public static TreeMap<String, Adduct> createAdductNameMap() {

		TreeMap<String, Adduct> adductNameMap = new TreeMap<String, Adduct>();

		for (Adduct mod : ChemicalModificationsManager.getChargedModifications(Polarity.Positive))
			adductNameMap.put(mod.getName(), mod);

		for (Adduct mod : ChemicalModificationsManager.getChargedModifications(Polarity.Negative))
			adductNameMap.put(mod.getName(), mod);

		TreeMap<String, String> adductRemap = null;
		try {
			adductRemap = ChemicalModificationDatabaseUtils.getAdductRemapping();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (adductRemap != null) {

			TreeMap<String, Adduct> adductRenameMap = new TreeMap<String, Adduct>();

			for (Entry<String, String> entry : adductRemap.entrySet()) {

				for (Entry<String, Adduct> mapping : adductNameMap.entrySet()) {

					if (entry.getValue().equals(mapping.getKey()))
						adductRenameMap.put(entry.getKey(), mapping.getValue());
				}
			}
			for (Entry<String, Adduct> entry : adductRenameMap.entrySet())
				adductNameMap.put(entry.getKey(), entry.getValue());
		}
		return adductNameMap;
	}

	public static void deleteAdductExchange(AdductExchange toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM ADDUCT_EXCHANGE A WHERE A.MOD_ONE_NAME = ? AND A.MOD_TWO_NAME = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getLeavingAdduct().getName());
		stmt.setString(2, toDelete.getComingAdduct().getName());
		stmt.execute();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteChemicalModification(Adduct toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM CHEM_MOD A WHERE A.MOD_NAME = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getName());
		stmt.execute();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static TreeMap<String, String> getAdductRemapping() throws Exception {

		TreeMap<String, String> adductRemap = new TreeMap<String, String>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT INTERNAL_NAME, EXTERNAL_NAME FROM ADDUCT_CROSSREF";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			adductRemap.put(rs.getString(2), rs.getString(1));

		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return adductRemap;
	}

	public static HashSet<AdductExchange> getAdductExchangeList() throws Exception {

		HashSet<AdductExchange> adductExchangeList = new HashSet<AdductExchange>();
//		Connection conn = ConnectionManager.getConnection();
//		String query = "SELECT MOD_ONE_NAME, MOD_TWO_NAME, ENABLED FROM ADDUCT_EXCHANGE";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) {
//
//			AdductExchange newExchange = new AdductExchange(
//					rs.getString("MOD_ONE_NAME"),
//					rs.getString("MOD_TWO_NAME"),
//					rs.getString("ENABLED"));
//
//			adductExchangeList.add(newExchange);
//		}
//		rs.close();
//		ps.close();
//		ConnectionManager.releaseConnection(conn);
		return adductExchangeList;
		
	}

	public static Collection<Adduct> getChemicalModificationList() throws Exception {

		Set<Adduct> adducts = new HashSet<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT MOD_NAME, CEF_NOTATION, MOD_DESCRIPTION, CHARGE, XM, ADDED_GROUP, " +
			"REMOVED_GROUP, MOD_TYPE, MASS_CORRECTION, POLARITY, SMILES, ENABLED " +
			"FROM CHEM_MOD ORDER BY MOD_TYPE, MOD_NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			Adduct newAdduct = new SimpleAdduct(
					null,
					rs.getString("MOD_NAME"),
					rs.getString("MOD_DESCRIPTION"),
					rs.getInt("CHARGE"),
					rs.getInt("XM"),
					0.0,
					ModificationType.getModificationTypeByName(rs.getString("MOD_TYPE")),
					rs.getString("SMILES"));
	
			newAdduct.setAddedGroup(rs.getString("ADDED_GROUP"));
			newAdduct.setRemovedGroup(rs.getString("REMOVED_GROUP"));
			newAdduct.setMassCorrection(rs.getDouble("MASS_CORRECTION"));
			newAdduct.setCefNotation(rs.getString("CEF_NOTATION"));
			newAdduct.setEnabled(rs.getString("ENABLED").equals("Y"));
			newAdduct.finalizeModification();
			adducts.add(newAdduct);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return adducts.stream().sorted().collect(Collectors.toList());
	}

	public static void updateAdductExchange(AdductExchange originalExchange, AdductExchange modifiedExchange)
			throws Exception {

//		Connection conn = ConnectionManager.getConnection();
//		String query =
//			"UPDATE ADDUCT_EXCHANGE " +
//			"SET MOD_ONE_NAME = ?, MOD_TWO_NAME = ?, ENABLED = ? " +
//			"WHERE MOD_ONE_NAME = ? AND MOD_TWO_NAME = ?";
//
//		PreparedStatement stmt = conn.prepareStatement(query);
//		stmt.setString(1, modifiedExchange.getLeavingAdduct().getName());
//		stmt.setString(2, modifiedExchange.getComingAdduct().getName());
//		stmt.setString(4, originalExchange.getLeavingAdduct().getName());
//		stmt.setString(5, originalExchange.getComingAdduct().getName());
//		String enabled = "N";
//		if (modifiedExchange.isEnabled())
//			enabled = "Y";
//
//		stmt.setString(3, enabled);
//		stmt.executeUpdate();
//		stmt.close();
//		ConnectionManager.releaseConnection(conn);
	}

	public static void updateChemicalModification(String originalName, Adduct modified) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE CHEM_MOD SET MOD_NAME = ?, MOD_DESCRIPTION = ?, CHARGE = ?, XM = ?, ADDED_GROUP = ?, " +
			"REMOVED_GROUP = ?, MOD_TYPE = ?, MASS_CORRECTION = ?, POLARITY = ?, ENABLED = ?, CEF_NOTATION = ? " +
			"SMILES = ? WHERE MOD_NAME = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, modified.getName());
		stmt.setString(2, modified.getDescription());
		stmt.setInt(3, modified.getCharge());
		stmt.setInt(4, modified.getOligomericState());
		stmt.setString(5, modified.getAddedGroup());
		stmt.setString(6, modified.getRemovedGroup());
		stmt.setString(7, modified.getModificationType().name());
		stmt.setDouble(8, modified.getMassCorrection());
		String polarity = "ALL";
		if (modified.getPolarity().equals(Polarity.Negative))
			polarity = "NEG";

		if (modified.getPolarity().equals(Polarity.Positive))
			polarity = "POS";

		stmt.setString(9, polarity);

		String enabled = "N";
		if (modified.isEnabled())
			enabled = "Y";

		stmt.setString(10, enabled);
		stmt.setString(11, modified.getCefNotation());
		stmt.setString(12, modified.getSmiles());
		
		stmt.setString(13, originalName);

		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
}
