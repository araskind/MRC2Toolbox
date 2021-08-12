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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.CompositeAdductComponentType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class AdductDatabaseUtils {
	
	/**
	 * Adduct (charge carrier) management
	 * */
	public static Collection<Adduct> getAdductList() throws Exception {

		Collection<Adduct> adductList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ADDUCT_ID, ADDUCT_NAME, DESCRIPTION, CHARGE, "
				+ "NMER, ADDED_GROUP, REMOVED_GROUP, CEF_NOTATION FROM ADDUCTS "
				+ "ORDER BY ADDUCT_NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			Adduct newAdduct = new SimpleAdduct(
					rs.getString("ADDUCT_ID"), 
					rs.getString("ADDUCT_NAME"), 
					rs.getString("DESCRIPTION"), 
					rs.getString("ADDED_GROUP"), 
					rs.getString("REMOVED_GROUP"), 
					rs.getString("CEF_NOTATION"), 
					null, 
					rs.getInt("CHARGE"), 
					rs.getInt("NMER"), 
					0.0d,
					ModificationType.ADDUCT, 
					true);
			newAdduct.setMassCorrection(
					MsUtils.calculateMassCorrectionFromAddedRemovedGroups(newAdduct));
			adductList.add(newAdduct);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return adductList;
	}
	
	public static void addNewAdduct(SimpleAdduct newAdduct) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ADDUCT_SEQ",
				DataPrefix.ADDUCT,
				"0",
				4);
		newAdduct.setId(id);
		String query = 
				"INSERT INTO ADDUCTS (ADDUCT_ID, ADDUCT_NAME, DESCRIPTION, "
				+ "CHARGE, NMER, ADDED_GROUP, REMOVED_GROUP, CEF_NOTATION) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " ;
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, id);
		stmt.setString(2, newAdduct.getName());
		stmt.setString(3, newAdduct.getDescription());
		stmt.setInt(4, newAdduct.getCharge());
		stmt.setInt(5, newAdduct.getOligomericState());
		stmt.setString(6, newAdduct.getAddedGroup());
		stmt.setString(7, newAdduct.getRemovedGroup());
		stmt.setString(8, newAdduct.getCefNotation());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextAdductId(Connection conn) throws Exception {
//		
//		String id = null;
//		String query = "SELECT '" + DataPrefix.ADDUCT.getName() + 
//				"' || LPAD(ADDUCT_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) 
//			id = rs.getString("NEXT_ID");			
//		
//		rs.close();
//		ps.close();
//		return id;
//	}
	
	public static void updateAdduct(SimpleAdduct adduct) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE ADDUCTS SET ADDUCT_NAME = ?, DESCRIPTION = ?, CHARGE = ?, NMER = ?, "
				+ "ADDED_GROUP = ?, REMOVED_GROUP = ?, CEF_NOTATION = ? WHERE ADDUCT_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, adduct.getName());
		stmt.setString(2, adduct.getDescription());
		stmt.setInt(3, adduct.getCharge());
		stmt.setInt(4, adduct.getOligomericState());
		stmt.setString(5, adduct.getAddedGroup());
		stmt.setString(6, adduct.getRemovedGroup());
		stmt.setString(7, adduct.getCefNotation());		
		stmt.setString(8, adduct.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteAdduct(SimpleAdduct toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM ADDUCTS WHERE ADDUCT_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static SimpleAdduct getAdductById(String id, Connection conn) throws Exception {

		SimpleAdduct newAdduct = null;
		String query = "SELECT ADDUCT_NAME, DESCRIPTION, CHARGE, "
				+ "NMER, ADDED_GROUP, REMOVED_GROUP, CEF_NOTATION FROM ADDUCTS "
				+ "WHERE  ADDUCT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			newAdduct = new SimpleAdduct(
					id, 
					rs.getString("ADDUCT_NAME"), 
					rs.getString("DESCRIPTION"), 
					rs.getString("ADDED_GROUP"), 
					rs.getString("REMOVED_GROUP"), 
					rs.getString("CEF_NOTATION"), 
					null, 
					rs.getInt("CHARGE"), 
					rs.getInt("NMER"), 
					0.0d,
					ModificationType.ADDUCT, 
					true);
			newAdduct.setMassCorrection(
					MsUtils.calculateMassCorrectionFromAddedRemovedGroups(newAdduct));
		}
		rs.close();
		ps.close();
		return newAdduct;
	}
		
	/**
	 * Neutral loss management
	 * */
	public static Collection<Adduct> getNeutralLossList() throws Exception {

		Collection<Adduct> neutralLossList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT LOSS_ID, LOSS_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "REMOVED_GROUP, LOST_MASS, SMILES FROM NEUTRAL_LOSSES "
				+ "ORDER BY LOSS_NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			Adduct newAdduct = new SimpleAdduct(
					rs.getString("LOSS_ID"), 
					rs.getString("LOSS_NAME"), 
					rs.getString("DESCRIPTION"), 
					rs.getString("ADDED_GROUP"), 
					rs.getString("REMOVED_GROUP"), 
					null, 
					rs.getString("SMILES"), 
					0, 
					1, 
					rs.getDouble("LOST_MASS") * -1.0d,
					ModificationType.LOSS, 
					true);

			neutralLossList.add(newAdduct);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return neutralLossList;
	}	
	
	public static void addNewNeutralLoss(SimpleAdduct newLoss) throws Exception {

		Connection conn = ConnectionManager.getConnection();		
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"NEUTRAL_LOSS_SEQ",
				DataPrefix.NEUTRAL_LOSS,
				"0",
				4);
		newLoss.setId(id);
		String query = "INSERT INTO NEUTRAL_LOSSES (LOSS_ID, LOSS_NAME, DESCRIPTION, "
				+ "ADDED_GROUP, REMOVED_GROUP, LOST_MASS, SMILES) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, id);
		stmt.setString(2, newLoss.getName());
		stmt.setString(3, newLoss.getDescription());
		stmt.setString(4, newLoss.getAddedGroup());
		stmt.setString(5, newLoss.getRemovedGroup());
		stmt.setDouble(6, Math.abs(newLoss.getMassCorrection()));
		stmt.setString(7, newLoss.getSmiles());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
		
//	public static String getNextNeutralLossId(Connection conn) throws Exception {
//		
//		String id = null;
//		String query = "SELECT '" + DataPrefix.NEUTRAL_LOSS.getName() + 
//				"' || LPAD(NEUTRAL_LOSS_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) 
//			id = rs.getString("NEXT_ID");			
//		
//		rs.close();
//		ps.close();
//		return id;
//	}
	
	public static void updateNeutralLoss(SimpleAdduct loss) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE NEUTRAL_LOSSES SET LOSS_NAME = ?, DESCRIPTION = ?, "
				+ "ADDED_GROUP = ?, REMOVED_GROUP = ?, LOST_MASS = ?, SMILES = ? "
				+ "WHERE LOSS_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, loss.getName());
		stmt.setString(2, loss.getDescription());
		stmt.setString(3, loss.getAddedGroup());
		stmt.setString(4, loss.getRemovedGroup());
		stmt.setDouble(5, Math.abs(loss.getMassCorrection()));
		stmt.setString(6, loss.getSmiles());	
		stmt.setString(7, loss.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteNeutralLoss(SimpleAdduct toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM NEUTRAL_LOSSES WHERE LOSS_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Adduct getNeutralLossLById(String id, Connection conn) throws Exception {

		Adduct loss = null;
		String query = "SELECT LOSS_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "REMOVED_GROUP, LOST_MASS, SMILES FROM NEUTRAL_LOSSES "
				+ "WHERE LOSS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			loss = new SimpleAdduct(
					id, 
					rs.getString("LOSS_NAME"), 
					rs.getString("DESCRIPTION"), 
					rs.getString("ADDED_GROUP"), 
					rs.getString("REMOVED_GROUP"), 
					null, 
					rs.getString("SMILES"), 
					0, 
					1, 
					rs.getDouble("LOST_MASS"),
					ModificationType.LOSS, 
					true);
		}
		rs.close();
		ps.close();
		return loss;
	}	
	
	/**
	 * Neutral adduct management
	 * */
	public static Collection<Adduct> getNeutralAdductList() throws Exception {

		Collection<Adduct> adductList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT REPEAT_ID, ADDUCT_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "ADDED_MASS, SMILES FROM NEUTRAL_ADDUCTS "
				+ "ORDER BY ADDUCT_NAME";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			Adduct newAdduct = new SimpleAdduct(
					rs.getString("REPEAT_ID"), 
					rs.getString("ADDUCT_NAME"), 
					rs.getString("DESCRIPTION"), 
					rs.getString("ADDED_GROUP"), 
					null, 
					null, 
					rs.getString("SMILES"), 
					0, 
					1, 
					rs.getDouble("ADDED_MASS"),
					ModificationType.REPEAT, 
					true);

			adductList.add(newAdduct);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return adductList;
	}
	
	public static void addNewNeutralAdduct(SimpleAdduct newAdduct) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"NEUTRAL_ADDUCT_SEQ",
				DataPrefix.REPEAT,
				"0",
				4);
		newAdduct.setId(id);
		String query = "INSERT INTO NEUTRAL_ADDUCTS (REPEAT_ID, ADDUCT_NAME, DESCRIPTION, "
				+ "ADDED_GROUP, ADDED_MASS, SMILES) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, newAdduct.getId());	
		stmt.setString(2, newAdduct.getName());		
		stmt.setString(3, newAdduct.getDescription());
		stmt.setString(4, newAdduct.getAddedGroup());		
		stmt.setDouble(5, Math.abs(newAdduct.getMassCorrection()));
		stmt.setString(6, newAdduct.getSmiles());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}	
	
//	public static String getNextNeutralAdductId(Connection conn) throws Exception {
//		
//		String id = null;
//		String query = "SELECT '" + DataPrefix.REPEAT.getName() + 
//				"' || LPAD(NEUTRAL_ADDUCT_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) 
//			id = rs.getString("NEXT_ID");			
//		
//		rs.close();
//		ps.close();
//		return id;
//	}
	
	public static void updateNeutralAdduct(SimpleAdduct neutralAdduct) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE NEUTRAL_ADDUCTS SET ADDUCT_NAME = ?, DESCRIPTION = ?, "
				+ "ADDED_GROUP = ?, ADDED_MASS = ?, SMILES = ? WHERE REPEAT_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, neutralAdduct.getName());		
		stmt.setString(2, neutralAdduct.getDescription());
		stmt.setString(3, neutralAdduct.getAddedGroup());		
		stmt.setDouble(4, Math.abs(neutralAdduct.getMassCorrection()));
		stmt.setString(5, neutralAdduct.getSmiles());		
		stmt.setString(6, neutralAdduct.getId());	
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteNeutralAdduct(SimpleAdduct toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM NEUTRAL_ADDUCTS WHERE REPEAT_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Adduct getNeutralAdductById(String id, Connection conn) throws Exception {

		Adduct repeat = null;
		String query = "SELECT ADDUCT_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "ADDED_MASS, SMILES FROM NEUTRAL_ADDUCTS "
				+ "WHERE REPEAT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			repeat = new SimpleAdduct(
					id, 
					rs.getString("ADDUCT_NAME"), 
					rs.getString("DESCRIPTION"), 
					rs.getString("ADDED_GROUP"), 
					null, 
					null, 
					rs.getString("SMILES"), 
					0, 
					1, 
					rs.getDouble("ADDED_MASS"),
					ModificationType.REPEAT, 
					true);
		}
		rs.close();
		ps.close();
		return repeat;
	}
	
	/**
	 * Composite adduct management
	 * */	
	public static Collection<Adduct> getCompositeAdductList() throws Exception {

		Collection<Adduct> adductList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT COMPOSITE_ADDUCT_ID, CHARGE_CARRIER, DESCRIPTION FROM COMPOSITE_ADDUCTS";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String cmpQuery = "SELECT COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT "
				+ "FROM COMPOSITE_ADDUCT_COMPONENTS WHERE COMPOSITE_ADDUCT_ID = ?";
		PreparedStatement cmpPs = conn.prepareStatement(cmpQuery);
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			SimpleAdduct chargeCarrier = getAdductById(rs.getString("CHARGE_CARRIER"), conn);
			CompositeAdduct newCompositeAdduct = new CompositeAdduct(
					rs.getString("COMPOSITE_ADDUCT_ID"),
					chargeCarrier, 
					rs.getString("DESCRIPTION"));
			
			cmpPs.setString(1, newCompositeAdduct.getId());
			ResultSet cmpRs = cmpPs.executeQuery();
			while (cmpRs.next()) {
				
				if(cmpRs.getString("COMPONENT_TYPE").equals(CompositeAdductComponentType.LOSS.name())) {
					//	Adduct loss = getNeutralLossLById(cmpRs.getString("COMPONENT_ID"), conn);
					Adduct loss = AdductManager.getAdductById(cmpRs.getString("COMPONENT_ID"));
					if(loss == null) {
						throw new Exception("Loss ID " + cmpRs.getString("COMPONENT_ID") + " can not be resolved.");
					}
					int count = cmpRs.getInt("COMPONENT_COUNT");
					for(int i=0; i<count; i++)
						newCompositeAdduct.addNeutralLoss((SimpleAdduct) loss);
				}
				if(cmpRs.getString("COMPONENT_TYPE").equals(CompositeAdductComponentType.REPEAT.name())) {
					//	Adduct repeat = getNeutralLossLById(cmpRs.getString("COMPONENT_ID"), conn);
					Adduct repeat = AdductManager.getAdductById(cmpRs.getString("COMPONENT_ID"));
					if(repeat == null) {
						throw new Exception("Repeat ID " + cmpRs.getString("COMPONENT_ID") + " can not be resolved.");
					}
					int count = cmpRs.getInt("COMPONENT_COUNT");
					for(int i=0; i<count; i++)
						newCompositeAdduct.addNeutralAdduct((SimpleAdduct) repeat);
				}
			}	
			cmpRs.close();
			adductList.add(newCompositeAdduct);
		}
		rs.close();
		ps.close();
		cmpPs.close();
		ConnectionManager.releaseConnection(conn);
		return adductList;
	}
	
	public static void addNewCompositeAdduct(CompositeAdduct newAdduct) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"COMPOSITE_ADDUCT_SEQ",
				DataPrefix.COMPOSITE_ADDUCT,
				"0",
				5);
		newAdduct.setId(id);
		
		//	Adduct
		String query = 
				"INSERT INTO COMPOSITE_ADDUCTS ( " +
				"COMPOSITE_ADDUCT_ID, DESCRIPTION, CHARGE_CARRIER)  " +
				"VALUES (?, ?, ?) " ;
		PreparedStatement stmt = conn.prepareStatement(query);				
		stmt.setString(1, id);
		stmt.setString(2, newAdduct.getDescription());
		stmt.setString(3, newAdduct.getChargeCarrier().getId());
		stmt.executeUpdate();
		stmt.close();
		
		//	Losses and repeats
		String partsQuery = 
				"INSERT INTO COMPOSITE_ADDUCT_COMPONENTS ( " +
				"COMPOSITE_ADDUCT_ID, COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT)  " +
				"VALUES (?, ?, ?, ?) " ;
		PreparedStatement partsStmt = conn.prepareStatement(partsQuery);
		if(!newAdduct.getNeutralLosses().isEmpty()) {
			
			Map<SimpleAdduct, Long> countedLosses = newAdduct.getNeutralLossCounts();
			for(Entry<SimpleAdduct, Long> entry : countedLosses.entrySet()) {
				
				partsStmt.setString(1, id);
				partsStmt.setString(2, entry.getKey().getId());
				partsStmt.setString(3, CompositeAdductComponentType.LOSS.name());
				partsStmt.setLong(4, entry.getValue());
				partsStmt.executeUpdate();
			}			
		}
		if(!newAdduct.getNeutralAdducts().isEmpty()) {
			
			Map<SimpleAdduct, Long> countedRepeats = newAdduct.getNeutralAdductCounts();
			for(Entry<SimpleAdduct, Long> entry : countedRepeats.entrySet()) {
				
				partsStmt.setString(1, id);
				partsStmt.setString(2, entry.getKey().getId());
				partsStmt.setString(3, CompositeAdductComponentType.REPEAT.name());
				partsStmt.setLong(4, entry.getValue());
				partsStmt.executeUpdate();
			}	
		}
		partsStmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextCompositeAdductId(Connection conn) throws Exception {
//		
//		String id = null;
//		String query = "SELECT '" + DataPrefix.COMPOSITE_ADDUCT.getName() + 
//				"' || LPAD(COMPOSITE_ADDUCT_SEQ.NEXTVAL, 5, '0') AS NEXT_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) 
//			id = rs.getString("NEXT_ID");			
//		
//		rs.close();
//		ps.close();
//		return id;
//	}
	
	public static void updateCompositeAdduct(CompositeAdduct adduct) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOSITE_ADDUCTS SET CHARGE_CARRIER = ?, DESCRIPTION = ? WHERE COMPOSITE_ADDUCT_ID =?" ;
		PreparedStatement stmt = conn.prepareStatement(query);				
		stmt.setString(1, adduct.getChargeCarrier().getId());
		stmt.setString(2, adduct.getDescription());
		stmt.setString(3, adduct.getId());
		stmt.executeUpdate();
		stmt.close();
		
		//	Remove and re-insert losses and repeats
		query = "DELETE FROM COMPOSITE_ADDUCT_COMPONENTS WHERE COMPOSITE_ADDUCT_ID = ?";
		stmt = conn.prepareStatement(query);
		stmt.setString(1, adduct.getId());
		stmt.executeUpdate();
		stmt.close();
		
		String partsQuery = 
				"INSERT INTO COMPOSITE_ADDUCT_COMPONENTS ( " +
				"COMPOSITE_ADDUCT_ID, COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT)  " +
				"VALUES (?, ?, ?, ?) " ;
		PreparedStatement partsStmt = conn.prepareStatement(partsQuery);
		if(!adduct.getNeutralLosses().isEmpty()) {
			
			Map<SimpleAdduct, Long> countedLosses = adduct.getNeutralLossCounts();
			for(Entry<SimpleAdduct, Long> entry : countedLosses.entrySet()) {
				
				partsStmt.setString(1, adduct.getId());
				partsStmt.setString(2, entry.getKey().getId());
				partsStmt.setString(3, CompositeAdductComponentType.LOSS.name());
				partsStmt.setLong(4, entry.getValue());
				partsStmt.executeUpdate();
			}			
		}
		if(!adduct.getNeutralAdducts().isEmpty()) {
			
			Map<SimpleAdduct, Long> countedRepeats = adduct.getNeutralAdductCounts();
			for(Entry<SimpleAdduct, Long> entry : countedRepeats.entrySet()) {
				
				partsStmt.setString(1, adduct.getId());
				partsStmt.setString(2, entry.getKey().getId());
				partsStmt.setString(3, CompositeAdductComponentType.REPEAT.name());
				partsStmt.setLong(4, entry.getValue());
				partsStmt.executeUpdate();
			}	
		}
		partsStmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteCompositeAdduct(CompositeAdduct toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM COMPOSITE_ADDUCTS WHERE COMPOSITE_ADDUCT_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Adduct getCompositeAdductById(String id, Connection conn) throws Exception {
				
		CompositeAdduct newCompositeAdduct = null;
		
		String query = "SELECT CHARGE_CARRIER, DESCRIPTION "
				+ "FROM COMPOSITE_ADDUCTS WHERE COMPOSITE_ADDUCT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1,  id); 
		
		String cmpQuery = "SELECT COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT "
				+ "FROM COMPOSITE_ADDUCT_COMPONENTS WHERE COMPOSITE_ADDUCT_ID = ?";
		PreparedStatement cmpPs = conn.prepareStatement(cmpQuery);
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			SimpleAdduct chargeCarrier = getAdductById(rs.getString("CHARGE_CARRIER"), conn);
			newCompositeAdduct = new CompositeAdduct(
					id,
					chargeCarrier, 
					rs.getString("DESCRIPTION"));
			
			cmpPs.setString(1, newCompositeAdduct.getId());
			ResultSet cmpRs = cmpPs.executeQuery();
			while (cmpRs.next()) {
				
				if(cmpRs.getString("COMPONENT_TYPE").equals(CompositeAdductComponentType.LOSS.name())) {
					Adduct loss = getNeutralLossLById(rs.getString("COMPONENT_ID"), conn);
					int count = cmpRs.getInt("COMPONENT_COUNT");
					for(int i=0; i<count; i++)
						newCompositeAdduct.addNeutralLoss((SimpleAdduct) loss);
				}
				if(cmpRs.getString("COMPONENT_TYPE").equals(CompositeAdductComponentType.REPEAT.name())) {
					Adduct repeat = getNeutralLossLById(rs.getString("COMPONENT_ID"), conn);
					int count = cmpRs.getInt("COMPONENT_COUNT");
					for(int i=0; i<count; i++)
						newCompositeAdduct.addNeutralAdduct((SimpleAdduct) repeat);
				}
			}	
			cmpRs.close();
		}
		rs.close();
		ps.close();
		cmpPs.close();
		return newCompositeAdduct;
	}
	
	/**
	 * Adduct exchange management
	 * */

	public static Collection<AdductExchange> getAdductExchangeList() throws Exception {

		Collection<AdductExchange> adductExchangeList = new TreeSet<AdductExchange>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ADDUCT_EXCHANGE_ID, COMING_ADDUCT_ID, LEAVING_ADDUCT_ID, "
				+ "COMING_COMPOSITE_ADDUCT_ID, LEAVING_COMPOSITE_ADDUCT_ID FROM ADDUCT_EXCHANGE";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			
			Adduct coming = null;
			Adduct leaving = null;
			if(rs.getString("COMING_ADDUCT_ID") != null)
				coming = AdductManager.getAdductById(rs.getString("COMING_ADDUCT_ID"));
			
			if(rs.getString("COMING_COMPOSITE_ADDUCT_ID") != null)
				coming = AdductManager.getAdductById(rs.getString("COMING_COMPOSITE_ADDUCT_ID"));
			
			if(rs.getString("LEAVING_ADDUCT_ID") != null)
				leaving = AdductManager.getAdductById(rs.getString("LEAVING_ADDUCT_ID"));
			
			if(rs.getString("LEAVING_COMPOSITE_ADDUCT_ID") != null)
				leaving = AdductManager.getAdductById(rs.getString("LEAVING_COMPOSITE_ADDUCT_ID"));
			
			if(coming != null && leaving != null) {
				
				AdductExchange newExchange = new AdductExchange(
						rs.getString("ADDUCT_EXCHANGE_ID"),
						coming,
						leaving);

				adductExchangeList.add(newExchange);
			}
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return adductExchangeList;
	}
	
	public static void addNewAdductExchange(AdductExchange newExchange) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "INSERT INTO ADDUCT_EXCHANGE ("
				+ "ADDUCT_EXCHANGE_ID, COMING_ADDUCT_ID, LEAVING_ADDUCT_ID, "
				+ "COMING_COMPOSITE_ADDUCT_ID, LEAVING_COMPOSITE_ADDUCT_ID) "
				+ "VALUES (?, ?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ADDUCT_EXCHANGE_SEQ",
				DataPrefix.ADDUCT_EXCHANGE,
				"0",
				4);
		
		String comingAdductId = null;
		String leavingAdductId = null;
		String comingCompositeAdductId = null;		
		String leavingCompositeAdductId = null;
		
		if(newExchange.getComingAdduct() instanceof SimpleAdduct)
			comingAdductId = newExchange.getComingAdduct().getId();
		
		if(newExchange.getLeavingAdduct() instanceof SimpleAdduct)
			leavingAdductId = newExchange.getLeavingAdduct().getId();
		
		if(newExchange.getComingAdduct() instanceof CompositeAdduct)
			comingCompositeAdductId = newExchange.getComingAdduct().getId();
		
		if(newExchange.getLeavingAdduct() instanceof CompositeAdduct)
			leavingCompositeAdductId = newExchange.getLeavingAdduct().getId();
		
		stmt.setString(1, id);
		stmt.setString(2, comingAdductId);
		stmt.setString(3, leavingAdductId);
		stmt.setString(4, comingCompositeAdductId);		
		stmt.setString(5, leavingCompositeAdductId);

		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextAdductExchangeId(Connection conn) throws Exception {
//		
//		String id = null;
//		String query = "SELECT '" + DataPrefix.ADDUCT_EXCHANGE.getName() + 
//				"' || LPAD(ADDUCT_EXCHANGE_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) 
//			id = rs.getString("NEXT_ID");			
//		
//		rs.close();
//		ps.close();
//		return id;
//	}
	
	public static void updateAdductExchange(AdductExchange originalExchange, AdductExchange modifiedExchange)
			throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE ADDUCT_EXCHANGE " +
			"SET COMING_ADDUCT_ID = ?, LEAVING_ADDUCT_ID = ?, "
			+ "COMING_COMPOSITE_ADDUCT_ID = ?, LEAVING_COMPOSITE_ADDUCT_ID = ?, "
			+ "WHERE ADDUCT_EXCHANGE_ID = ?";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		
		String comingAdductId = null;
		String leavingAdductId = null;
		String comingCompositeAdductId = null;		
		String leavingCompositeAdductId = null;
		
		if(modifiedExchange.getComingAdduct() instanceof SimpleAdduct)
			comingAdductId = modifiedExchange.getComingAdduct().getId();
		
		if(modifiedExchange.getLeavingAdduct() instanceof SimpleAdduct)
			leavingAdductId = modifiedExchange.getLeavingAdduct().getId();
		
		if(modifiedExchange.getComingAdduct() instanceof CompositeAdduct)
			comingCompositeAdductId = modifiedExchange.getComingAdduct().getId();
		
		if(modifiedExchange.getLeavingAdduct() instanceof CompositeAdduct)
			leavingCompositeAdductId = modifiedExchange.getLeavingAdduct().getId();
		
		stmt.setString(1, comingAdductId);
		stmt.setString(2, leavingAdductId);
		stmt.setString(3, comingCompositeAdductId);		
		stmt.setString(4, leavingCompositeAdductId);		
		stmt.setString(5, originalExchange.getId());

		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteAdductExchange(AdductExchange toDelete) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM ADDUCT_EXCHANGE WHERE ADDUCT_EXCHANGE_ID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, toDelete.getId());
		stmt.execute();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static boolean adductExchangeExists(Adduct comingAdduct, Adduct leavingAdduct) throws Exception {

		AdductExchange existing = AdductManager.getAdductExchangeList().stream().filter(e -> 
				(e.getComingAdduct().equals(comingAdduct) && e.getLeavingAdduct().equals(leavingAdduct)) 
					|| (e.getComingAdduct().equals(leavingAdduct) && e.getLeavingAdduct().equals(comingAdduct))
			).findFirst().orElse(null);

		return existing != null;
	}
}
