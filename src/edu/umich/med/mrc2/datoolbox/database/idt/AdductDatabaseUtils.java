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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductNotationType;
import edu.umich.med.mrc2.datoolbox.data.enums.CompositeAdductComponentType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class AdductDatabaseUtils {
	
	private static final Logger logger = LogManager.getLogger(AdductDatabaseUtils.class);

	private AdductDatabaseUtils() {
		/* This utility class should not be instantiated */
	}
	
	/**
	 * Adduct (charge carrier) management
	 * */
	public static Collection<Adduct> getAdductList() throws SQLException {

		Collection<Adduct> adductList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ADDUCT_ID, ADDUCT_NAME, DESCRIPTION, CHARGE, "
				+ "NMER, ADDED_GROUP, REMOVED_GROUP, CEF_NOTATION, "
				+ "BINNER_NOTATION, SIRIUS_NOTATION FROM ADDUCTS "
				+ "ORDER BY ADDUCT_NAME";
				
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				Adduct newAdduct = new SimpleAdduct(
						rs.getString("ADDUCT_ID"), 
						rs.getString("ADDUCT_NAME"), 
						rs.getString("DESCRIPTION"), 
						rs.getString("ADDED_GROUP"), 
						rs.getString("REMOVED_GROUP"), 
						null, 
						rs.getInt("CHARGE"), 
						rs.getInt("NMER"), 
						0.0d,
						ModificationType.ADDUCT, 
						true);
				
				newAdduct.setMassCorrection(
						MsUtils.calculateMassCorrectionFromAddedRemovedGroups(newAdduct));
				
				String cefNotation = rs.getString("CEF_NOTATION");
				if(cefNotation != null && !cefNotation.isEmpty())
					newAdduct.setNotationForType(AdductNotationType.CEF, cefNotation);
				
				String binnerNotation = rs.getString("BINNER_NOTATION");
				if(binnerNotation != null && !binnerNotation.isEmpty())
					newAdduct.setNotationForType(AdductNotationType.BINNER, binnerNotation);
				
				String siriusNotation = rs.getString("SIRIUS_NOTATION");
				if(siriusNotation != null && !siriusNotation.isEmpty())
					newAdduct.setNotationForType(AdductNotationType.SIRIUS, siriusNotation);
				
				adductList.add(newAdduct);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return adductList;
	}
	
	public static void addNewAdduct(SimpleAdduct newAdduct) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ADDUCT_SEQ",
				DataPrefix.ADDUCT,
				"0",
				4);
		newAdduct.setId(id);
		
		String query = 
				"INSERT INTO ADDUCTS (ADDUCT_ID, ADDUCT_NAME, DESCRIPTION, "
				+ "CHARGE, NMER, ADDED_GROUP, REMOVED_GROUP, "
				+ "CEF_NOTATION, BINNER_NOTATION, SIRIUS_NOTATION) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " ;
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, id);
			stmt.setString(2, newAdduct.getName());
			stmt.setString(3, newAdduct.getDescription());
			stmt.setInt(4, newAdduct.getCharge());
			stmt.setInt(5, newAdduct.getOligomericState());
			stmt.setString(6, newAdduct.getAddedGroup());
			stmt.setString(7, newAdduct.getRemovedGroup());
			stmt.setString(8, newAdduct.getNotationForType(AdductNotationType.CEF));
			stmt.setString(9, newAdduct.getNotationForType(AdductNotationType.BINNER));
			stmt.setString(10, newAdduct.getNotationForType(AdductNotationType.SIRIUS));
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateAdduct(SimpleAdduct adduct) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE ADDUCTS SET ADDUCT_NAME = ?, DESCRIPTION = ?, CHARGE = ?, NMER = ?, "
				+ "ADDED_GROUP = ?, REMOVED_GROUP = ?, CEF_NOTATION = ?, "
				+ "BINNER_NOTATION = ?, SIRIUS_NOTATION = ? WHERE ADDUCT_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, adduct.getName());
			stmt.setString(2, adduct.getDescription());
			stmt.setInt(3, adduct.getCharge());
			stmt.setInt(4, adduct.getOligomericState());
			stmt.setString(5, adduct.getAddedGroup());
			stmt.setString(6, adduct.getRemovedGroup());
			stmt.setString(7, adduct.getNotationForType(AdductNotationType.CEF));
			stmt.setString(8, adduct.getNotationForType(AdductNotationType.BINNER));
			stmt.setString(9, adduct.getNotationForType(AdductNotationType.SIRIUS));		
			stmt.setString(10, adduct.getId());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteAdduct(SimpleAdduct toDelete) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM ADDUCTS WHERE ADDUCT_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, toDelete.getId());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static SimpleAdduct getAdductById(String id, Connection conn) throws SQLException {

		SimpleAdduct newAdduct = null;
		String query = "SELECT ADDUCT_NAME, DESCRIPTION, CHARGE, "
				+ "NMER, ADDED_GROUP, REMOVED_GROUP, CEF_NOTATION, "
				+ "BINNER_NOTATION, SIRIUS_NOTATION FROM ADDUCTS "
				+ "WHERE  ADDUCT_ID = ?";
				
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				newAdduct = new SimpleAdduct(
						id, 
						rs.getString("ADDUCT_NAME"), 
						rs.getString("DESCRIPTION"), 
						rs.getString("ADDED_GROUP"), 
						rs.getString("REMOVED_GROUP"), 
						null, 
						rs.getInt("CHARGE"), 
						rs.getInt("NMER"), 
						0.0d,
						ModificationType.ADDUCT, 
						true);
				
				newAdduct.setMassCorrection(
						MsUtils.calculateMassCorrectionFromAddedRemovedGroups(newAdduct));
				
				String cefNotation = rs.getString("CEF_NOTATION");
				if(cefNotation != null && !cefNotation.isEmpty())
					newAdduct.setNotationForType(AdductNotationType.CEF, cefNotation);
				
				String binnerNotation = rs.getString("BINNER_NOTATION");
				if(binnerNotation != null && !binnerNotation.isEmpty())
					newAdduct.setNotationForType(AdductNotationType.BINNER, binnerNotation);
				
				String siriusNotation = rs.getString("SIRIUS_NOTATION");
				if(siriusNotation != null && !siriusNotation.isEmpty())
					newAdduct.setNotationForType(AdductNotationType.SIRIUS, siriusNotation);
			}
			rs.close();
		}
		return newAdduct;
	}
		
	/**
	 * Neutral loss management
	 * */
	public static Collection<Adduct> getNeutralLossList() throws SQLException {

		Collection<Adduct> neutralLossList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT LOSS_ID, LOSS_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "REMOVED_GROUP, LOST_MASS, SMILES FROM NEUTRAL_LOSSES "
				+ "ORDER BY LOSS_NAME";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				Adduct newAdduct = new SimpleAdduct(
						rs.getString("LOSS_ID"), 
						rs.getString("LOSS_NAME"), 
						rs.getString("DESCRIPTION"), 
						rs.getString("ADDED_GROUP"), 
						rs.getString("REMOVED_GROUP"), 
						rs.getString("SMILES"), 
						0, 
						1, 
						rs.getDouble("LOST_MASS") * -1.0d,
						ModificationType.LOSS, 
						true);
	
				neutralLossList.add(newAdduct);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return neutralLossList;
	}	
	
	public static void addNewNeutralLoss(SimpleAdduct newLoss) throws SQLException {

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
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, id);
			stmt.setString(2, newLoss.getName());
			stmt.setString(3, newLoss.getDescription());
			stmt.setString(4, newLoss.getAddedGroup());
			stmt.setString(5, newLoss.getRemovedGroup());
			stmt.setDouble(6, Math.abs(newLoss.getMassCorrection()));
			stmt.setString(7, newLoss.getSmiles());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateNeutralLoss(SimpleAdduct loss) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE NEUTRAL_LOSSES SET LOSS_NAME = ?, DESCRIPTION = ?, "
				+ "ADDED_GROUP = ?, REMOVED_GROUP = ?, LOST_MASS = ?, SMILES = ? "
				+ "WHERE LOSS_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, loss.getName());
			stmt.setString(2, loss.getDescription());
			stmt.setString(3, loss.getAddedGroup());
			stmt.setString(4, loss.getRemovedGroup());
			stmt.setDouble(5, Math.abs(loss.getMassCorrection()));
			stmt.setString(6, loss.getSmiles());	
			stmt.setString(7, loss.getId());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteNeutralLoss(SimpleAdduct toDelete) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM NEUTRAL_LOSSES WHERE LOSS_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, toDelete.getId());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Adduct getNeutralLossLById(String id, Connection conn) throws SQLException {

		Adduct loss = null;
		String query = "SELECT LOSS_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "REMOVED_GROUP, LOST_MASS, SMILES FROM NEUTRAL_LOSSES "
				+ "WHERE LOSS_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				loss = new SimpleAdduct(
						id, 
						rs.getString("LOSS_NAME"), 
						rs.getString("DESCRIPTION"), 
						rs.getString("ADDED_GROUP"), 
						rs.getString("REMOVED_GROUP"),
						rs.getString("SMILES"), 
						0, 
						1, 
						rs.getDouble("LOST_MASS"),
						ModificationType.LOSS, 
						true);
			}
			rs.close();
		}
		return loss;
	}	
	
	/**
	 * Neutral adduct management
	 * */
	public static Collection<Adduct> getNeutralAdductList() throws SQLException {

		Collection<Adduct> adductList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT REPEAT_ID, ADDUCT_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "ADDED_MASS, SMILES FROM NEUTRAL_ADDUCTS "
				+ "ORDER BY ADDUCT_NAME";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				Adduct newAdduct = new SimpleAdduct(
						rs.getString("REPEAT_ID"), 
						rs.getString("ADDUCT_NAME"), 
						rs.getString("DESCRIPTION"), 
						rs.getString("ADDED_GROUP"), 
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
		}
		ConnectionManager.releaseConnection(conn);
		return adductList;
	}
	
	public static void addNewNeutralAdduct(SimpleAdduct newAdduct) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"NEUTRAL_ADDUCT_SEQ",
				DataPrefix.REPEAT,
				"0",
				4);
		newAdduct.setId(id);
		
		String query = "INSERT INTO NEUTRAL_ADDUCTS (REPEAT_ID, ADDUCT_NAME, DESCRIPTION, "
				+ "ADDED_GROUP, ADDED_MASS, SMILES) VALUES (?, ?, ?, ?, ?, ?)";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, newAdduct.getId());	
			stmt.setString(2, newAdduct.getName());		
			stmt.setString(3, newAdduct.getDescription());
			stmt.setString(4, newAdduct.getAddedGroup());		
			stmt.setDouble(5, Math.abs(newAdduct.getMassCorrection()));
			stmt.setString(6, newAdduct.getSmiles());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateNeutralAdduct(SimpleAdduct neutralAdduct) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "UPDATE NEUTRAL_ADDUCTS SET ADDUCT_NAME = ?, DESCRIPTION = ?, "
				+ "ADDED_GROUP = ?, ADDED_MASS = ?, SMILES = ? WHERE REPEAT_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, neutralAdduct.getName());		
			stmt.setString(2, neutralAdduct.getDescription());
			stmt.setString(3, neutralAdduct.getAddedGroup());		
			stmt.setDouble(4, Math.abs(neutralAdduct.getMassCorrection()));
			stmt.setString(5, neutralAdduct.getSmiles());		
			stmt.setString(6, neutralAdduct.getId());	
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteNeutralAdduct(SimpleAdduct toDelete) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM NEUTRAL_ADDUCTS WHERE REPEAT_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, toDelete.getId());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Adduct getNeutralAdductById(String id, Connection conn) throws SQLException {

		Adduct repeat = null;
		String query = "SELECT ADDUCT_NAME, DESCRIPTION, ADDED_GROUP, "
				+ "ADDED_MASS, SMILES FROM NEUTRAL_ADDUCTS "
				+ "WHERE REPEAT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				repeat = new SimpleAdduct(
						id, 
						rs.getString("ADDUCT_NAME"), 
						rs.getString("DESCRIPTION"), 
						rs.getString("ADDED_GROUP"), 
						null, 
						rs.getString("SMILES"), 
						0, 
						1, 
						rs.getDouble("ADDED_MASS"),
						ModificationType.REPEAT, 
						true);
			}
			rs.close();
		}
		return repeat;
	}
	
	/**
	 * Composite adduct management
	 * */	
	public static Collection<Adduct> getCompositeAdductList() throws SQLException {

		Collection<Adduct> adductList = new ArrayList<Adduct>();
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT COMPOSITE_ADDUCT_ID, CHARGE_CARRIER, "
				+ "DESCRIPTION, CEF_NOTATION, BINNER_NOTATION, "
				+ "SIRIUS_NOTATION FROM COMPOSITE_ADDUCTS";		
		String cmpQuery = "SELECT COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT "
				+ "FROM COMPOSITE_ADDUCT_COMPONENTS WHERE COMPOSITE_ADDUCT_ID = ?";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){	

			try(PreparedStatement cmpPs = conn.prepareStatement(cmpQuery)){
		
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
		
							Adduct loss = AdductManager.getAdductById(cmpRs.getString("COMPONENT_ID"));
							if(loss == null) {
								throw new SQLException("Loss ID " + cmpRs.getString("COMPONENT_ID") + " can not be resolved.");
							}
							int count = cmpRs.getInt("COMPONENT_COUNT");
							for(int i=0; i<count; i++)
								newCompositeAdduct.addNeutralLoss((SimpleAdduct) loss);
						}
						if(cmpRs.getString("COMPONENT_TYPE").equals(CompositeAdductComponentType.REPEAT.name())) {
		
							Adduct repeat = AdductManager.getAdductById(cmpRs.getString("COMPONENT_ID"));
							if(repeat == null) {
								throw new SQLException("Repeat ID " + cmpRs.getString("COMPONENT_ID") + " can not be resolved.");
							}
							int count = cmpRs.getInt("COMPONENT_COUNT");
							for(int i=0; i<count; i++)
								newCompositeAdduct.addNeutralAdduct((SimpleAdduct) repeat);
						}
					}	
					cmpRs.close();
					
					String cefNotation = rs.getString("CEF_NOTATION");
					if(cefNotation != null && !cefNotation.isEmpty())
						newCompositeAdduct.setNotationForType(AdductNotationType.CEF, cefNotation);
					
					String binnerNotation = rs.getString("BINNER_NOTATION");
					if(binnerNotation != null && !binnerNotation.isEmpty())
						newCompositeAdduct.setNotationForType(AdductNotationType.BINNER, binnerNotation);
					
					String siriusNotation = rs.getString("SIRIUS_NOTATION");
					if(siriusNotation != null && !siriusNotation.isEmpty())
						newCompositeAdduct.setNotationForType(AdductNotationType.SIRIUS, siriusNotation);
					
					adductList.add(newCompositeAdduct);
				}
				rs.close();
			}
		}
		ConnectionManager.releaseConnection(conn);
		return adductList;
	}
	
	public static void addNewCompositeAdduct(CompositeAdduct newAdduct) throws SQLException {

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
				"COMPOSITE_ADDUCT_ID, DESCRIPTION, CHARGE_CARRIER, "
				+ "CEF_NOTATION, BINNER_NOTATION, SIRIUS_NOTATION)  " +
				"VALUES (?, ?, ?, ?, ?, ?) " ;
		try(PreparedStatement stmt = conn.prepareStatement(query)){	
			stmt.setString(1, id);
			stmt.setString(2, newAdduct.getDescription());
			stmt.setString(3, newAdduct.getChargeCarrier().getId());		
			stmt.setString(4, newAdduct.getNotationForType(AdductNotationType.CEF));
			stmt.setString(5, newAdduct.getNotationForType(AdductNotationType.BINNER));
			stmt.setString(6, newAdduct.getNotationForType(AdductNotationType.SIRIUS));
			
			stmt.executeUpdate();
		}		
		//	Losses and repeats
		String partsQuery = 
				"INSERT INTO COMPOSITE_ADDUCT_COMPONENTS ( " +
				"COMPOSITE_ADDUCT_ID, COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT)  " +
				"VALUES (?, ?, ?, ?) " ;
		try(PreparedStatement partsStmt = conn.prepareStatement(partsQuery)){
			
			partsStmt.setString(1, id);		
			if(!newAdduct.getNeutralLosses().isEmpty()) {
				
				Map<SimpleAdduct, Long> countedLosses = newAdduct.getNeutralLossCounts();
				for(Entry<SimpleAdduct, Long> entry : countedLosses.entrySet()) {
					partsStmt.setString(2, entry.getKey().getId());
					partsStmt.setString(3, CompositeAdductComponentType.LOSS.name());
					partsStmt.setLong(4, entry.getValue());
					partsStmt.addBatch();
				}
				partsStmt.executeBatch();
			}
			if(!newAdduct.getNeutralAdducts().isEmpty()) {
				
				Map<SimpleAdduct, Long> countedRepeats = newAdduct.getNeutralAdductCounts();
				for(Entry<SimpleAdduct, Long> entry : countedRepeats.entrySet()) {
					partsStmt.setString(2, entry.getKey().getId());
					partsStmt.setString(3, CompositeAdductComponentType.REPEAT.name());
					partsStmt.setLong(4, entry.getValue());
					partsStmt.addBatch();
				}
				partsStmt.executeBatch();
			}
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateCompositeAdduct(CompositeAdduct adduct) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOSITE_ADDUCTS SET CHARGE_CARRIER = ?, DESCRIPTION = ?, "
				+ "CEF_NOTATION = ?, BINNER_NOTATION = ?, SIRIUS_NOTATION = ? "
				+ "WHERE COMPOSITE_ADDUCT_ID =?" ;
		try(PreparedStatement stmt = conn.prepareStatement(query)){		
			stmt.setString(1, adduct.getChargeCarrier().getId());
			stmt.setString(2, adduct.getDescription());		
			stmt.setString(3, adduct.getNotationForType(AdductNotationType.CEF));
			stmt.setString(4, adduct.getNotationForType(AdductNotationType.BINNER));
			stmt.setString(5, adduct.getNotationForType(AdductNotationType.SIRIUS));
			stmt.setString(6, adduct.getId());		
			stmt.executeUpdate();
		}		
		//	Remove and re-insert losses and repeats
		query = "DELETE FROM COMPOSITE_ADDUCT_COMPONENTS WHERE COMPOSITE_ADDUCT_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, adduct.getId());
			stmt.executeUpdate();
		}		
		String partsQuery = 
				"INSERT INTO COMPOSITE_ADDUCT_COMPONENTS ( " +
				"COMPOSITE_ADDUCT_ID, COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT)  " +
				"VALUES (?, ?, ?, ?) " ;
		try(PreparedStatement partsStmt = conn.prepareStatement(partsQuery)){
			
			partsStmt.setString(1, adduct.getId());		
			if(!adduct.getNeutralLosses().isEmpty()) {
				
				Map<SimpleAdduct, Long> countedLosses = adduct.getNeutralLossCounts();
				for(Entry<SimpleAdduct, Long> entry : countedLosses.entrySet()) {
					partsStmt.setString(2, entry.getKey().getId());
					partsStmt.setString(3, CompositeAdductComponentType.LOSS.name());
					partsStmt.setLong(4, entry.getValue());
					partsStmt.addBatch();
				}
				partsStmt.executeBatch();
			}
			if(!adduct.getNeutralAdducts().isEmpty()) {
				
				Map<SimpleAdduct, Long> countedRepeats = adduct.getNeutralAdductCounts();
				for(Entry<SimpleAdduct, Long> entry : countedRepeats.entrySet()) {
					partsStmt.setString(2, entry.getKey().getId());
					partsStmt.setString(3, CompositeAdductComponentType.REPEAT.name());
					partsStmt.setLong(4, entry.getValue());
					partsStmt.addBatch();
				}
				partsStmt.executeBatch();
			}
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteCompositeAdduct(CompositeAdduct toDelete) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM COMPOSITE_ADDUCTS WHERE COMPOSITE_ADDUCT_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, toDelete.getId());
			stmt.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Adduct getCompositeAdductById(String id, Connection conn) throws SQLException {
				
		CompositeAdduct newCompositeAdduct = null;	
		String query = "SELECT CHARGE_CARRIER, DESCRIPTION, "
				+ "CEF_NOTATION, BINNER_NOTATION, SIRIUS_NOTATION "
				+ "FROM COMPOSITE_ADDUCTS WHERE COMPOSITE_ADDUCT_ID = ?";
		String cmpQuery = "SELECT COMPONENT_ID, COMPONENT_TYPE, COMPONENT_COUNT "
				+ "FROM COMPOSITE_ADDUCT_COMPONENTS WHERE COMPOSITE_ADDUCT_ID = ?";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){

			try(PreparedStatement cmpPs = conn.prepareStatement(cmpQuery)){
		
				ps.setString(1,  id); 
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
						String cefNotation = rs.getString("CEF_NOTATION");
						if(cefNotation != null && !cefNotation.isEmpty())
							newCompositeAdduct.setNotationForType(AdductNotationType.CEF, cefNotation);
						
						String binnerNotation = rs.getString("BINNER_NOTATION");
						if(binnerNotation != null && !binnerNotation.isEmpty())
							newCompositeAdduct.setNotationForType(AdductNotationType.BINNER, binnerNotation);
						
						String siriusNotation = rs.getString("SIRIUS_NOTATION");
						if(siriusNotation != null && !siriusNotation.isEmpty())
							newCompositeAdduct.setNotationForType(AdductNotationType.SIRIUS, siriusNotation);
					}	
					cmpRs.close();
				}
				rs.close();
			}
		}
		return newCompositeAdduct;
	}
	
	/**
	 * Adduct exchange management
	 * */

	public static Collection<AdductExchange> getAdductExchangeList() throws SQLException {

		Collection<AdductExchange> adductExchangeList = new TreeSet<AdductExchange>();
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ADDUCT_EXCHANGE_ID, COMING_ADDUCT_ID, LEAVING_ADDUCT_ID, "
				+ "COMING_COMPOSITE_ADDUCT_ID, LEAVING_COMPOSITE_ADDUCT_ID FROM ADDUCT_EXCHANGE";
		try(PreparedStatement ps = conn.prepareStatement(query)){
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
		}
		ConnectionManager.releaseConnection(conn);
		return adductExchangeList;
	}
	
	public static void addNewAdductExchange(AdductExchange newExchange) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"ADDUCT_EXCHANGE_SEQ",
				DataPrefix.ADDUCT_EXCHANGE,
				"0",
				4);
		String query = "INSERT INTO ADDUCT_EXCHANGE ("
				+ "ADDUCT_EXCHANGE_ID, COMING_ADDUCT_ID, LEAVING_ADDUCT_ID, "
				+ "COMING_COMPOSITE_ADDUCT_ID, LEAVING_COMPOSITE_ADDUCT_ID) "
				+ "VALUES (?, ?, ?, ?, ?)";
		try(PreparedStatement stmt = conn.prepareStatement(query)){

			newExchange.setId(id);				
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
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateAdductExchange(
			AdductExchange originalExchange, 
			AdductExchange modifiedExchange) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE ADDUCT_EXCHANGE " +
			"SET COMING_ADDUCT_ID = ?, LEAVING_ADDUCT_ID = ?, "
			+ "COMING_COMPOSITE_ADDUCT_ID = ?, LEAVING_COMPOSITE_ADDUCT_ID = ?, "
			+ "WHERE ADDUCT_EXCHANGE_ID = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(query)){
		
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
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteAdductExchange(AdductExchange toDelete) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM ADDUCT_EXCHANGE WHERE ADDUCT_EXCHANGE_ID = ?";
		try(PreparedStatement stmt = conn.prepareStatement(query)){
			stmt.setString(1, toDelete.getId());
			stmt.execute();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static boolean adductExchangeExists(Adduct comingAdduct, Adduct leavingAdduct) {

		return AdductManager.getAdductExchangeList().stream().anyMatch(e -> 
				(e.getComingAdduct().equals(comingAdduct) && e.getLeavingAdduct().equals(leavingAdduct)) 
					|| (e.getComingAdduct().equals(leavingAdduct) && e.getLeavingAdduct().equals(comingAdduct)));
	}
}
