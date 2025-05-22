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
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerAdductList;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
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
				3);
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

		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"BINNER_ANNOTATION_DATA_SET_SEQ",
				DataPrefix.BINNER_ANNOTATIONS_DATA_SET,
				"0",
				6);
		newDataSet.setId(newId);

		String query = 
			"INSERT INTO BINNER_ANNOTATION_LOOKUP_DATA_SET " +
			"(BALDS_ID, NAME, DESCRIPTION, CREATED_BY,  " +
			"DATE_CREATED, LAST_MODIFIED) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, newDataSet.getId());
		ps.setString(2, newDataSet.getName());
		if(newDataSet.getDescription() != null)
			ps.setString(3, newDataSet.getDescription());
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		ps.setString(4, newDataSet.getCreatedBy().getId());
		ps.setTimestamp(5, new java.sql.Timestamp(
				newDataSet.getDateCreated().getTime()));
		ps.setTimestamp(6, new java.sql.Timestamp(
				newDataSet.getLastModified().getTime()));	
		ps.executeUpdate();
		
		query = 
			"INSERT INTO BINNER_ANNOTATION_CLUSTER "
			+ "(BA_CLUSTER_ID, BALDS_ID, MOL_ION_NUMBER) "
			+ "VALUES (?, ?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(2, newDataSet.getId());
		
		String bccQuery = 
				"INSERT INTO BINNER_ANNOTATION_CLUSTER_COMPONENT " +
				"(BCC_ID, BA_CLUSTER_ID, MOL_ION_NUMBER, FEATURE_NAME, BINNER_MZ,  " +
				"BINNER_RT, ANNOTATION, IS_PRIMARY, ADDITIONAL_GROUP_ANNOTATIONS,  " +
				"FURTHER_ANNOTATIONS, DERIVATIONS, ISOTOPES, ADDITIONAL_ISOTOPES,  " +
				"CHARGE_CARRIER, ADDITIONAL_ADDUCTS, BIN_NUMBER, CORR_CLUSTER_NUMBER,  " +
				"REBIN_SUBCLUSTER_NUMBER, RT_SUBCLUSTER_NUMBER, MASS_ERROR, RMD) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement bccPs = conn.prepareStatement(bccQuery);
		
		int counter = 0;
		for(BinnerAnnotationCluster bac : newDataSet.getBinnerAnnotationClusters()) {
			
			String cId = SQLUtils.getNextIdFromSequence(conn, 
					"BINNER_ANNOTATION_CLUSTER_SEQ",
					DataPrefix.BINNER_ANNOTATIONS_CLUSTER,
					"0",
					9);
			bac.setId(cId);
			ps.setString(1, cId);
			ps.setInt(3, bac.getMolIonNumber());
			ps.addBatch();
			
			bccPs.setString(2, cId);
			for(BinnerAnnotation ba : bac.getAnnotations()) {
				
				String bccId = SQLUtils.getNextIdFromSequence(conn, 
						"BA_CLUSTER_COMPONENT_SEQ",
						DataPrefix.BINNER_ANNOTATIONS_CLUSTER_COMPONENT,
						"0",
						11);
				ba.setId(bccId);
				bccPs.setString(1, bccId);
				bccPs.setInt(3, ba.getMolIonNumber());	//MOL_ION_NUMBER
				bccPs.setString(4, ba.getFeatureName());	//FEATURE_NAME
				bccPs.setDouble(5, ba.getBinnerMz());	//BINNER_MZ
				bccPs.setDouble(6, ba.getBinnerRt());	//BINNER_RT
				bccPs.setString(7, ba.getAnnotation());	//ANNOTATION
				if(ba.isPrimary())
					bccPs.setString(8, "Y");	//IS_PRIMARY
				else
					bccPs.setNull(8, java.sql.Types.NULL);
					
				bccPs.setString(9, ba.getAdditionalGroupAnnotations()); //ADDITIONAL_GROUP_ANNOTATIONS
				bccPs.setString(10, ba.getFurtherAnnotations()); //FURTHER_ANNOTATIONS
				bccPs.setString(11, ba.getDerivations()); //DERIVATIONS
				bccPs.setString(12, ba.getIsotopes()); //ISOTOPES
				bccPs.setString(13, ba.getAdditionalIsotopes()); //ADDITIONAL_ISOTOPES
				bccPs.setString(14, ba.getChargeCarrier());	//CHARGE_CARRIER
				bccPs.setString(15, ba.getAdditionalAdducts());	//ADDITIONAL_ADDUCTS
				bccPs.setInt(16, ba.getBinNumber()); //BIN_NUMBER
				bccPs.setInt(17, ba.getCorrClusterNumber()); //CORR_CLUSTER_NUMBER
				bccPs.setInt(18, ba.getRebinSubclusterNumber()); //REBIN_SUBCLUSTER_NUMBER
				bccPs.setInt(19, ba.getRtSubclusterNumber()); //RT_SUBCLUSTER_NUMBER
				bccPs.setDouble(20, ba.getMassError()); //MASS_ERROR
				bccPs.setDouble(21, ba.getRmd()); //RMD
				
				bccPs.addBatch();
			}
			counter++;			
			if(counter % 100 == 0) {
				ps.executeBatch();
				bccPs.executeBatch();
			}
		}
		ps.executeBatch();
		bccPs.executeBatch();
		bccPs.close();
		ps.close();
	}
	
	public static void editBinnerAnnotationLookupDataSetMetadata(
			BinnerAnnotationLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE BINNER_ANNOTATION_LOOKUP_DATA_SET " +
				"SET NAME = ?, DESCRIPTION = ?, LAST_MODIFIED = ? "
				+ "WHERE BALDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, dataSet.getName());
		ps.setString(2, dataSet.getDescription());
		ps.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
		ps.setString(4, dataSet.getId());	
		ps.executeUpdate();
		ConnectionManager.releaseConnection(conn);	
	}
	
	public static void deleteBinnerAnnotationLookupDataSet(
			BinnerAnnotationLookupDataSet dataSet) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"DELETE FROM BINNER_ANNOTATION_LOOKUP_DATA_SET "
				+ "WHERE BALDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, dataSet.getId());	
		ps.executeUpdate();
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
		String query = 
				"SELECT BALDS_ID, NAME, DESCRIPTION, CREATED_BY, "
				+ "DATE_CREATED, LAST_MODIFIED "
				+ "FROM BINNER_ANNOTATION_LOOKUP_DATA_SET ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			BinnerAnnotationLookupDataSet ds = 
					new BinnerAnnotationLookupDataSet(
					rs.getString("BALDS_ID"), 
					rs.getString("NAME"), 
					rs.getString("DESCRIPTION"), 
					IDTDataCache.getUserById(rs.getString("CREATED_BY")), 
					new Date(rs.getTimestamp("DATE_CREATED").getTime()),
					new Date(rs.getTimestamp("LAST_MODIFIED").getTime()));			
			dataSets.add(ds);
		}
		rs.close();
		ps.close();
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
		
		String query = 
				"SELECT BA_CLUSTER_ID, MOL_ION_NUMBER "
				+ "FROM BINNER_ANNOTATION_CLUSTER WHERE BALDS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String baQuery = 
				"SELECT BCC_ID, MOL_ION_NUMBER, FEATURE_NAME, BINNER_MZ, " +
				"BINNER_RT, ANNOTATION, IS_PRIMARY, ADDITIONAL_GROUP_ANNOTATIONS, " +
				"FURTHER_ANNOTATIONS, DERIVATIONS, ISOTOPES, ADDITIONAL_ISOTOPES, " +
				"CHARGE_CARRIER, ADDITIONAL_ADDUCTS, BIN_NUMBER, CORR_CLUSTER_NUMBER, " +
				"REBIN_SUBCLUSTER_NUMBER, RT_SUBCLUSTER_NUMBER, MASS_ERROR, RMD " +
				"FROM BINNER_ANNOTATION_CLUSTER_COMPONENT " +
				"WHERE BA_CLUSTER_ID = ? ";
		PreparedStatement baPs = conn.prepareStatement(baQuery);
		ResultSet baRs;
		
		ps.setString(1, dataSet.getId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			BinnerAnnotationCluster bac =
					new BinnerAnnotationCluster(
							rs.getString("BA_CLUSTER_ID"), 
							rs.getInt("MOL_ION_NUMBER"));
			baPs.setString(1, bac.getId());
			baRs = baPs.executeQuery();
			while(baRs.next()) {
				
				BinnerAnnotation ba = new BinnerAnnotation(
						baRs.getString("BCC_ID"), 
						baRs.getString("FEATURE_NAME"), 
						baRs.getString("ANNOTATION"));
				ba.setMolIonNumber(baRs.getInt("MOL_ION_NUMBER"));
				ba.setBinnerMz(baRs.getDouble("BINNER_MZ"));
				ba.setBinnerRt(baRs.getDouble("BINNER_RT"));
				if(baRs.getString("IS_PRIMARY") != null) 
					ba.setPrimary(true);

				ba.setAdditionalGroupAnnotations(baRs.getString("ADDITIONAL_GROUP_ANNOTATIONS"));
				ba.setFurtherAnnotations(baRs.getString("FURTHER_ANNOTATIONS"));
				ba.setDerivations(baRs.getString("DERIVATIONS"));
				ba.setIsotopes(baRs.getString("ISOTOPES"));
				ba.setAdditionalIsotopes(baRs.getString("ADDITIONAL_ISOTOPES"));
				ba.setChargeCarrier(baRs.getString("CHARGE_CARRIER"));
				ba.setAdditionalAdducts(baRs.getString("ADDITIONAL_ADDUCTS"));
				ba.setBinNumber(baRs.getInt("BIN_NUMBER"));
				ba.setRebinSubclusterNumber(baRs.getInt("REBIN_SUBCLUSTER_NUMBER"));
				ba.setRtSubclusterNumber(baRs.getInt("RT_SUBCLUSTER_NUMBER"));
				ba.setMassError(baRs.getDouble("MASS_ERROR"));
				ba.setRmd(baRs.getDouble("RMD"));
				
				bac.getAnnotations().add(ba);
			}
			baRs.close();			
			dataSet.getBinnerAnnotationClusters().add(bac);
		}
		rs.close();
		ps.close();
	}

	public static void addNewBinnerNeutralMassDifferenceAsAnnotation(BinnerNeutralMassDifference massDiff) {
		
		BinnerAdduct newAdduct = new BinnerAdduct(
				null, 
				massDiff.getBinnerName(),
				0, 
				1, 
				null,
				null, 
				massDiff);
		
		try {
			addNewBinnerAdduct(newAdduct);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//	BinnerAdductList
	public static void addNewBinnerAdductList(BinnerAdductList newList) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String id = SQLUtils.getNextIdFromSequence(conn, 
				"BINNER_ANNOTATION_LIST_SEQ",
				DataPrefix.BINNER_ANNOTATION_LIST,
				"0",
				5);
		newList.setId(id);
		
		String query  =
				"INSERT INTO BINNER_ANNOTATION_LIST  " +
				"(LIST_ID, LIST_NAME, DESCRIPTION, OWNER, DATE_CREATED, DATE_MODIFIED) " +
				"VALUES(?, ?, ?, ?, ?, ?) ";
		PreparedStatement ps = conn.prepareStatement(query);		

		ps.setString(1, newList.getId());
		ps.setString(2, newList.getName());
		if(newList.getDescription() != null)
			ps.setString(3, newList.getDescription());
		else
			ps.setNull(3, java.sql.Types.NULL);
			
		ps.setString(4, newList.getOwner().getId());
		ps.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
		ps.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
		
		ps.executeUpdate();
		ps.close();
		insertBinnerAdductListComponents(newList, conn);		
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void insertBinnerAdductListComponents(BinnerAdductList baList, Connection conn) {
		
		String query  =
				"INSERT INTO BINNER_ANNOTATION_LIST_COMPONENT  " +
				"(LIST_ID, ANNOTATION_ID, TIER) " +
				"VALUES(?, ?, ?) ";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, baList.getId());
			for(Entry<BinnerAdduct,Integer> ba : baList.getComponents().entrySet()) {
				
				ps.setString(2, ba.getKey().getId());
				ps.setInt(3, ba.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void editBinnerAdductList(BinnerAdductList listToEdit) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		String query  =
				"UPDATE BINNER_ANNOTATION_LIST " +
				"SET LIST_NAME = ?, DESCRIPTION = ?, OWNER = ?, " +
				"DATE_CREATED = ?, DATE_MODIFIED = ? " +
				"WHERE LIST_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);		

		ps.setString(1, listToEdit.getName());
		if(listToEdit.getDescription() != null)
			ps.setString(2, listToEdit.getDescription());
		else
			ps.setNull(2, java.sql.Types.NULL);
			
		ps.setString(3, listToEdit.getOwner().getId());
		ps.setTimestamp(4, new java.sql.Timestamp(listToEdit.getDateCreated().getTime()));
		ps.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
		ps.setString(6, listToEdit.getId());
		
		ps.executeUpdate();
		ps.close();
		
		clearBinnerAdductListComponents(listToEdit, conn);
		insertBinnerAdductListComponents(listToEdit, conn);		
		ConnectionManager.releaseConnection(conn);
	}
		
	private static void clearBinnerAdductListComponents(
			BinnerAdductList baList, Connection conn)  {
		
		String query  =
				"DELETE FROM BINNER_ANNOTATION_LIST_COMPONENT WHERE LIST_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, baList.getId());
			ps.executeUpdate();
			ps.close();	
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteBinnerAdductList(BinnerAdductList listToDelete) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();	
		String query  = "DELETE FROM BINNER_ANNOTATION_LIST WHERE LIST_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){		
			ps.setString(1, listToDelete.getId());	
			ps.executeUpdate();
		}		
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<BinnerAdductList>
			getBinnerAdductListCollection() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<BinnerAdductList>dataSets = 
				getBinnerAdductListCollection(conn);
		ConnectionManager.releaseConnection(conn);	
		return dataSets;
	}
		
	public static Collection<BinnerAdductList>
		getBinnerAdductListCollection(Connection conn) throws SQLException {
		
		Collection<BinnerAdductList>dataSets = new TreeSet<BinnerAdductList>();
		String query = 
				"SELECT LIST_ID, LIST_NAME, DESCRIPTION, OWNER, DATE_CREATED, DATE_MODIFIED "
				+ "FROM BINNER_ANNOTATION_LIST ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			BinnerAdductList ds = 
					new BinnerAdductList(
					rs.getString("LIST_ID"), 
					rs.getString("LIST_NAME"), 
					rs.getString("DESCRIPTION"), 
					IDTDataCache.getUserById(rs.getString("OWNER")), 
					new Date(rs.getTimestamp("DATE_CREATED").getTime()),
					new Date(rs.getTimestamp("DATE_MODIFIED").getTime()));
			populateBinnerAdductList(ds, conn);
			dataSets.add(ds);
		}
		rs.close();
		ps.close();
		return dataSets;
	}
	
	private static void populateBinnerAdductList(
			BinnerAdductList baList, Connection conn) throws SQLException {
		
		String query = "SELECT ANNOTATION_ID, TIER "
				+ "FROM BINNER_ANNOTATION_LIST_COMPONENT WHERE LIST_ID = ?";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setString(1, baList.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				BinnerAdduct bad = AdductManager.getBinnerAdductById(rs.getString("ANNOTATION_ID"));
				int tier = rs.getInt("TIER");
				if(bad != null)
					baList.addComponent(bad, tier);
			}
			rs.close();
		}

	}	
}





























