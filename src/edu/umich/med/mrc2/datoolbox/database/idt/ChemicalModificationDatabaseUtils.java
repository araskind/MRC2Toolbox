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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
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
}
