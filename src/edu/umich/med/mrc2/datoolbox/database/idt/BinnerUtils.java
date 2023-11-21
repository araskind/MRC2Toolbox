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
import java.util.Map.Entry;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.CompositeAdductComponentType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class BinnerUtils {
	
	/*
	 * Binner annotations
	 * */
	public static Collection<BinnerAdduct> getBinnerAdducts() throws Exception {

		Collection<BinnerAdduct>binAdductList = new TreeSet<BinnerAdduct>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT ANNOTATION_ID, ANNOTATION_NAME, CHARGE, TIER, CHARGE_CARRIER_ID, "
			+ "ADDUCT_EXCHANGE_ID, MASS_DIFF_ID FROM BINNER_ANNOTATIONS";
		PreparedStatement ps = conn.prepareStatement(query);		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			
			Adduct chargeCarrier = null;
			String chargeCarrierId = rs.getString("CHARGE_CARRIER_ID");
			if(chargeCarrierId != null) {
				
				chargeCarrier = AdductManager.getAdductById(chargeCarrierId);				
				if(chargeCarrier == null)
					System.out.println("Could not find adduct for ID " + chargeCarrierId);			
			}
			
			AdductExchange exchange = null;
			String exchangeId = rs.getString("ADDUCT_EXCHANGE_ID");
			if(exchangeId != null) {
				exchange = AdductManager.getAdductExchangeById(exchangeId);
				if(exchange == null)
						System.out.println("Could not find exchange for ID " + exchangeId);
			}
			BinnerNeutralMassDifference bmd = null;
			String bmdId  = rs.getString("MASS_DIFF_ID");
			if(bmdId != null) {
				bmd = AdductManager.getBinnerNeutralMassDifferenceById(bmdId);
				if(bmd == null)
					System.out.println("Could not find MassDiff for ID " + bmdId);
			}
			BinnerAdduct newAdduct = new BinnerAdduct(
						rs.getString("ANNOTATION_ID"),
						rs.getString("ANNOTATION_NAME"),
						rs.getInt("CHARGE"),
						rs.getInt("TIER"),
						chargeCarrier,
						exchange,
						bmd);
			
			binAdductList.add(newAdduct);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return binAdductList;
	}
	
	public static void addNewBinnerAdduct(BinnerAdduct newAdduct) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"BINNER_ANNOTATION_SEQ",
				DataPrefix.BINNER_ANNOTATION,
				"0",
				4);
		newAdduct.setId(id);
		
		String query  =
			"INSERT INTO BINNER_ANNOTATIONS "
			+ "(ANNOTATION_ID, ANNOTATION_NAME, MASS, POLARITY, CHARGE, TIER, "
			+ "CHARGE_CARRIER_ID, ADDUCT_EXCHANGE_ID, MASS_DIFF_ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);		

		String chargeCarrierId = null;
		if(newAdduct.getChargeCarrier() != null)
			chargeCarrierId = newAdduct.getChargeCarrier().getId();

		String exchangeId = null;
		if(newAdduct.getAdductExchange() != null)
			exchangeId = newAdduct.getAdductExchange().getId();
		
		String bmdId  = null;
		if(newAdduct.getBinnerNeutralMassDifference() != null)
			bmdId = newAdduct.getBinnerNeutralMassDifference().getId();
		
		String polarityCode = newAdduct.getPolarity().getCode();
		if(newAdduct.getPolarity().equals(Polarity.Neutral))
			polarityCode = null;
			
		ps.setString(1, newAdduct.getId());
		ps.setString(2, newAdduct.getBinnerName());	
		ps.setDouble(3, newAdduct.getMass());
		ps.setString(4, polarityCode);
		ps.setInt(5, newAdduct.getCharge());
		ps.setInt(6, newAdduct.getTier());
		ps.setString(7, chargeCarrierId);
		ps.setString(8, exchangeId);
		ps.setString(9, bmdId);
		
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextBinnerAdductId(Connection conn) throws Exception{
//		
//		String nextId = null;
//		String query  =
//				"SELECT '" + DataPrefix.BINNER_ANNOTATION.getName() + 
//				"' || LPAD(BINNER_ANNOTATION_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			nextId = rs.getString("NEXT_ID");
//		}
//		rs.close();
//		ps.close();
//		return nextId;
//	}
	
	public static void editBinnerAdduct(BinnerAdduct toEdit) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE BINNER_ANNOTATIONS SET "
			+ "ANNOTATION_NAME = ?, MASS = ?, POLARITY = ?, CHARGE = ?, TIER = ?, "
			+ "CHARGE_CARRIER_ID = ?, ADDUCT_EXCHANGE_ID = ?, MASS_DIFF_ID = ? "
			+ "WHERE ANNOTATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);		

		String chargeCarrierId = null;
		if(toEdit.getChargeCarrier() != null)
			chargeCarrierId = toEdit.getChargeCarrier().getId();

		String exchangeId = null;
		if(toEdit.getAdductExchange() != null)
			exchangeId = toEdit.getAdductExchange().getId();
		
		String bmdId  = null;
		if(toEdit.getBinnerNeutralMassDifference() != null)
			bmdId = toEdit.getBinnerNeutralMassDifference().getId();
		
		String polarityCode = toEdit.getPolarity().getCode();
		if(toEdit.getPolarity().equals(Polarity.Neutral))
			polarityCode = null;
					
		ps.setString(1, toEdit.getBinnerName());	
		ps.setDouble(2, toEdit.getMass());
		ps.setString(3, polarityCode);
		ps.setInt(4, toEdit.getCharge());
		ps.setInt(5, toEdit.getTier());
		ps.setString(6, chargeCarrierId);
		ps.setString(7, exchangeId);
		ps.setString(8, bmdId);
		ps.setString(9, toEdit.getId());
		
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteBinnerAdduct(BinnerAdduct toDelete) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query  = "DELETE FROM BINNER_ANNOTATIONS WHERE ANNOTATION_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, toDelete.getId());
		ps.executeUpdate();
		ps.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	/*
	 * Binner mass differences - for now essentially salt clusters and similar
	 * */
	
	public static Collection<BinnerNeutralMassDifference>getBinnerNeutralMassDifferences() throws Exception {
		
		Collection<BinnerNeutralMassDifference>binMdList = new TreeSet<BinnerNeutralMassDifference>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT MASS_DIFF_ID, BINNER_MD_NAME FROM BINNER_MASS_DIFFERENCE ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String compQuery  =
				"SELECT COMPONENT_ID, COMPONENT_COUNT, COMPONENT_TYPE FROM "
				+ "BINNER_MASS_DIFFERENCE_COMPONENT WHERE MASS_DIFF_ID = ?";
		PreparedStatement compPs = conn.prepareStatement(compQuery);
			
		ResultSet rs = ps.executeQuery();
		ResultSet compRs = null;
		while (rs.next()) {
			
			BinnerNeutralMassDifference newDiff 
				= new BinnerNeutralMassDifference(
						rs.getString("MASS_DIFF_ID"),
						rs.getString("BINNER_MD_NAME"));
			
			compPs.setString(1, newDiff.getId());
			compRs = compPs.executeQuery();
			while(compRs.next()) {
				
				String componentId = compRs.getString("COMPONENT_ID");
				String componentType = compRs.getString("COMPONENT_TYPE");
				int componentCount = compRs.getInt("COMPONENT_COUNT");
				Adduct adduct = AdductManager.getAdductById(componentId);
				if(adduct == null) {
					System.out.println("Could not find adduct for ID " + componentId);
					continue;
				}
				if(componentType.equals(CompositeAdductComponentType.REPEAT.name())) {
						
					for(int i=0; i<componentCount; i++)
						newDiff.addNeutralAdduct((SimpleAdduct) adduct);				
				}
				if(componentType.equals(CompositeAdductComponentType.LOSS.name())) {
					
					for(int i=0; i<componentCount; i++)
						newDiff.addNeutralLoss((SimpleAdduct) adduct);
				}
			}	
			compRs.close();
			binMdList.add(newDiff);
		}
		rs.close();
		ps.close();
		compPs.close();
		ConnectionManager.releaseConnection(conn);
		
		return binMdList;
	}
	
	public static void addNewBinnerNeutralMassDifference(
			BinnerNeutralMassDifference newDiff) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"BINNER_MASS_DIFFERENCE_SEQ",
				DataPrefix.BINNER_MASS_DIFFERENCE,
				"0",
				4);
		newDiff.setId(id);
		
		String query  =
			"INSERT INTO BINNER_MASS_DIFFERENCE "
			+ "(MASS_DIFF_ID, BINNER_MD_NAME) VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String compQuery  =
				"INSERT INTO BINNER_MASS_DIFFERENCE_COMPONENT "
				+ "(MASS_DIFF_ID, COMPONENT_ID, COMPONENT_COUNT, "
				+ "COMPONENT_TYPE) VALUES (?, ?, ?, ?)";
		PreparedStatement compPs = conn.prepareStatement(compQuery);
		
		ps.setString(1, newDiff.getId());
		ps.setString(2, newDiff.getBinnerName());
		ps.executeUpdate();
		ps.close();
		
		compPs.setString(1, newDiff.getId());
		for(Entry<SimpleAdduct, Long> counts : newDiff.getNeutralAdductCounts().entrySet()) {
			
			compPs.setString(2, counts.getKey().getId());
			compPs.setLong(3, counts.getValue());
			compPs.setString(4, CompositeAdductComponentType.REPEAT.name());
			compPs.addBatch();
		}
		for(Entry<SimpleAdduct, Long> nlcounts : newDiff.getNeutralLossCounts().entrySet()) {
			
			compPs.setString(2, nlcounts.getKey().getId());
			compPs.setLong(3, nlcounts.getValue());
			compPs.setString(4, CompositeAdductComponentType.LOSS.name());
			compPs.addBatch();
		}		
		compPs.executeBatch();
		compPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextBinnerNeutralMassDifferenceId(Connection conn) throws Exception{
//		
//		String nextId = null;
//		String query  =
//				"SELECT '" + DataPrefix.BINNER_MASS_DIFFERENCE.getName() + 
//				"' || LPAD(BINNER_MASS_DIFFERENCE_SEQ.NEXTVAL, 3, '0') AS NEXT_ID FROM DUAL";
//		
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			nextId = rs.getString("NEXT_ID");
//		}
//		rs.close();
//		ps.close();	
//		return nextId;
//	}
	
	public static void editBinnerNeutralMassDifference(
			BinnerNeutralMassDifference toEdit) throws Exception {
				
		Connection conn = ConnectionManager.getConnection();		
		String compDelQuery  =
				"DELETE FROM BINNER_MASS_DIFFERENCE_COMPONENT WHERE MASS_DIFF_ID = ?";
		PreparedStatement compPs = conn.prepareStatement(compDelQuery);
		compPs.setString(1, toEdit.getId());
		compPs.executeUpdate();
		compPs.close();
		
		String query  =
			"UPDATE BINNER_MASS_DIFFERENCE SET BINNER_MD_NAME = ? WHERE MASS_DIFF_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, toEdit.getBinnerName());
		ps.setString(2, toEdit.getId());		
		ps.executeUpdate();
		ps.close();
		
		String compQuery  =
				"INSERT INTO BINNER_MASS_DIFFERENCE_COMPONENT "
				+ "(MASS_DIFF_ID, COMPONENT_ID, COMPONENT_COUNT, COMPONENT_TYPE) VALUES (?, ?, ?, ?)";
		compPs = conn.prepareStatement(compQuery);

		compPs.setString(1, toEdit.getId());
		for(Entry<SimpleAdduct, Long> counts : toEdit.getNeutralAdductCounts().entrySet()) {
			
			compPs.setString(2, counts.getKey().getId());
			compPs.setLong(3, counts.getValue());
			compPs.setString(4, CompositeAdductComponentType.REPEAT.name());
			compPs.addBatch();
		}
		for(Entry<SimpleAdduct, Long> nlcounts : toEdit.getNeutralLossCounts().entrySet()) {
			
			compPs.setString(2, nlcounts.getKey().getId());
			compPs.setLong(3, nlcounts.getValue());
			compPs.setString(4, CompositeAdductComponentType.LOSS.name());
			compPs.addBatch();
		}		
		compPs.executeBatch();
		compPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteBinnerNeutralMassDifference(BinnerNeutralMassDifference toDelete) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query  = "DELETE FROM BINNER_MASS_DIFFERENCE WHERE MASS_DIFF_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, toDelete.getId());
		ps.executeUpdate();
		ps.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	/*	
	 * BinnerAnnotations lookup 
	 * 
	 * */
	
	public static void addBinnerAnnotationLookupDataSet(
			BinnerAnnotationLookupDataSet newDataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		addBinnerAnnotationLookupDataSet(newDataSet, conn);
		ConnectionManager.releaseConnection(conn);	
	}

	public static void addBinnerAnnotationLookupDataSet(
			BinnerAnnotationLookupDataSet newDataSet, Connection conn) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public static void editBinnerAnnotationLookupDataSetMetadata(
			BinnerAnnotationLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
//		String query = 
//				"UPDATE FEATURE_LOOKUP_DATA_SET " +
//				"SET NAME = ?, DESCRIPTION = ?, LAST_MODIFIED = ? "
//				+ "WHERE FLDS_ID = ?";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ps.setString(1, dataSet.getName());
//		ps.setString(2, dataSet.getDescription());
//		ps.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
//		ps.setString(4, dataSet.getId());	
//		ps.executeUpdate();
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void deleteBinnerAnnotationLookupDataSet(
			BinnerAnnotationLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
//		String query = 
//				"DELETE FROM FEATURE_LOOKUP_DATA_SET WHERE FLDS_ID = ?";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ps.setString(1, dataSet.getId());	
//		ps.executeUpdate();
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static Collection<BinnerAnnotationLookupDataSet>
			getBinnerAnnotationLookupDataSetList() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<BinnerAnnotationLookupDataSet>dataSets = 
				getBinnerAnnotationLookupDataSetList(conn);
		ConnectionManager.releaseConnection(conn);	
		return dataSets;
	}
	
	public static Collection<BinnerAnnotationLookupDataSet>
			getBinnerAnnotationLookupDataSetList(Connection conn) throws Exception {
	
		Collection<BinnerAnnotationLookupDataSet>dataSets = 
				new TreeSet<BinnerAnnotationLookupDataSet>();
//		String query = 
//				"SELECT FLDS_ID, NAME, DESCRIPTION, CREATED_BY, "
//				+ "DATE_CREATED, LAST_MODIFIED "
//				+ "FROM FEATURE_LOOKUP_DATA_SET ORDER BY 1";
//		PreparedStatement ps = conn.prepareStatement(query);		
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			FeatureLookupDataSet ds = new FeatureLookupDataSet(
//					rs.getString("FLDS_ID"), 
//					rs.getString("NAME"), 
//					rs.getString("DESCRIPTION"), 
//					IDTDataCache.getUserById(rs.getString("CREATED_BY")), 
//					new Date(rs.getTimestamp("DATE_CREATED").getTime()),
//					new Date(rs.getTimestamp("LAST_MODIFIED").getTime()));
//			
////			getFeaturesForFeatureLookupDataSet(ds, conn);			
//			dataSets.add(ds);
//		}
//		rs.close();
//		ps.close();
		return dataSets;
	}
	
	public static void getClustersForBinnerAnnotationLookupDataSet(
			BinnerAnnotationLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		getClustersForBinnerAnnotationLookupDataSet(dataSet, conn);
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void getClustersForBinnerAnnotationLookupDataSet(
			BinnerAnnotationLookupDataSet dataSet, Connection conn) throws Exception {
		
//		String query = 
//				"SELECT COMPONENT_ID, NAME, MZ, RT, RANK, "
//				+ "SMILES, INCHI_KEY, FOLD_CHANGE, P_VALUE "
//				+ "FROM FEATURE_LOOKUP_DATA_SET_COMPONENT "
//				+ "WHERE FLDS_ID = ?";
//		PreparedStatement ps = conn.prepareStatement(query);	
//		ps.setString(1, dataSet.getId());
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			MinimalMSOneFeature feature = 
//					new MinimalMSOneFeature(
//							rs.getString("COMPONENT_ID"), 
//							rs.getString("NAME"), 
//							rs.getDouble("MZ"), 
//							rs.getDouble("RT"), 
//							rs.getDouble("RANK"),
//							rs.getString("SMILES"),
//							rs.getString("INCHI_KEY"));	
//			
//			feature.setFoldChange(rs.getDouble("FOLD_CHANGE"));
//			feature.setpValue(rs.getDouble("P_VALUE"));
//			dataSet.getFeatures().add(feature);
//		}
//		rs.close();
//		ps.close();
	}
}


