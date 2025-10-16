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

package edu.umich.med.mrc2.datoolbox.database.cpdcol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollection;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataFieldCategory;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.PCDLImportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class CompoundMultiplexUtils {
	
	/*
	 * Compound collections
	 * */

	public static Collection<CompoundCollection>getCompoundCollections() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<CompoundCollection>cpdColls = getCompoundCollections(conn) ;
		ConnectionManager.releaseConnection(conn);
		return cpdColls;
	}

	public static Collection<CompoundCollection>getCompoundCollections(Connection conn) throws Exception {
	
		Collection<CompoundCollection>compoundCollections = new HashSet<CompoundCollection>();
		String query  = 
				"SELECT CC_ID, CC_NAME, CC_DESCRIPTION, CC_URL FROM COMPOUND_COLLECTIONS";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			CompoundCollection cc = new CompoundCollection(
					rs.getString("CC_ID"), 
					rs.getString("CC_NAME"), 
					rs.getString("CC_DESCRIPTION"),
					rs.getString("CC_URL"));
			compoundCollections.add(cc);
		}
		rs.close();
		ps.close();
		return compoundCollections;
	}
	
	public static void addCompoundCollection(CompoundCollection newCollection) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		String query  = 
				"INSERT INTO COMPOUND_COLLECTIONS "
				+ "(CC_ID, CC_NAME, CC_DESCRIPTION, CC_URL) VALUES (?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(query);
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"CPD_COLL_SEQ",
				DataPrefix.COMPOUND_COLLECTION,
				"0",
				4);
		ps.setString(1, nextId);
		ps.setString(2, newCollection.getName());
		String desc = newCollection.getDescription();
		if(desc != null)
			ps.setString(3, desc);
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		String url = newCollection.getUrl();
		if(url != null)
			ps.setString(4, url);
		else
			ps.setNull(4, java.sql.Types.NULL);
		
		ps.executeUpdate();
		ps.close();
		newCollection.setId(nextId);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteCompoundCollection(CompoundCollection toDelete) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		String query  = 
				"DELETE FROM COMPOUND_COLLECTIONS WHERE CC_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, toDelete.getId());		
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateCompoundCollection(CompoundCollection toUpdate) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		String query  = 
				"UPDATE COMPOUND_COLLECTIONS SET CC_NAME = ?, "
				+ "CC_DESCRIPTION = ?, CC_URL = ? WHERE CC_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, toUpdate.getName());
		String desc = toUpdate.getDescription();
		if(desc != null)
			ps.setString(2, desc);
		else
			ps.setNull(2, java.sql.Types.NULL);
		
		String url = toUpdate.getUrl();
		if(url != null)
			ps.setString(3, url);
		else
			ps.setNull(3, java.sql.Types.NULL);
		
		ps.setString(4, toUpdate.getId());
		
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	/*
	 * Temporary components, only to process legacy data
	 * */
	
	public static void insertTemporaryCCComponent(
			CompoundCollectionComponent component, Connection conn) throws Exception {
		
		String query  = 
				"INSERT INTO COMPOUND_COLLECTION_COMPONENTS "
				+ "(CC_ID, CC_COMPONENT_ID, CAS) VALUES (?,?,?)";
		PreparedStatement ps = conn.prepareStatement(query);
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"CPD_COLL_COMP_SEQ",
				DataPrefix.COMPOUND_COLLECTION_COMPONENT,
				"0",
				7);
		ps.setString(1, component.getCollectionId());
		ps.setString(2, nextId);
		ps.setString(3, component.getCas());		
		ps.executeUpdate();
		ps.close();
		component.setId(nextId);
		
		Map<CpdMetadataField, String> metadata = component.getMetadata();
		if(!metadata.isEmpty()) {
			
			query  = 
					"INSERT INTO COMPOUND_COLLECTION_COMPONENT_METADATA "
					+ "(CC_COMPONENT_ID, FIELD_ID, FIELD_VALUE) VALUES (?,?,?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, component.getId());
			for(Entry<CpdMetadataField, String> md : metadata.entrySet()) {

				if(md.getValue() != null) {
					ps.setString(2, md.getKey().getId());
					ps.setString(3, md.getValue());
					ps.addBatch();
				}
			}
			ps.executeBatch();			
			ps.close();
		}
	}	

	/*
	 * Solvent
	 * */
	public static String addNewSolvent(MobilePhase solvent) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"MULTIPLEX_SOLVENT_SEQ",
				DataPrefix.MOBILE_PHASE,
				"0",
				3);
		solvent.setId(nextId);
		String query  = 
				"INSERT INTO COMPOUND_MULTIPLEX_SOLVENTS (SOLVENT_ID, SOLVENT_NAME) VALUES(?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, solvent.getId());
		ps.setString(2, solvent.getName());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return nextId;
	}

	public static void editSolvent(MobilePhase solvent) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
			"UPDATE COMPOUND_MULTIPLEX_SOLVENTS SET SOLVENT_NAME = ? WHERE SOLVENT_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, solvent.getName());
		ps.setString(2, solvent.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteSolvent(MobilePhase solvent) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = 
			"DELETE FROM COMPOUND_MULTIPLEX_SOLVENTS WHERE SOLVENT_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, solvent.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<MobilePhase> getSolventList() throws Exception{

		Connection conn = ConnectionManager.getConnection();
		Collection<MobilePhase>solventList = getSolventList(conn);
		ConnectionManager.releaseConnection(conn);
		return solventList;
	}
	
	public static Collection<MobilePhase> getSolventList(Connection conn) throws Exception{
		
		Collection<MobilePhase>solventList = new TreeSet<MobilePhase>();
		String query = 
				"SELECT SOLVENT_ID, SOLVENT_NAME FROM "
				+ "COMPOUND_MULTIPLEX_SOLVENTS ORDER BY 1";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			MobilePhase p = new MobilePhase(
					rs.getString("SOLVENT_ID"), 
					rs.getString("SOLVENT_NAME"));
			solventList.add(p);
		}
		rs.close();
		ps.close();
		return solventList;
	}
	
	
	
	public static Collection<CompoundMultiplexMixture>getCompoundMultiplexMixtureList() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		Collection<CompoundMultiplexMixture>mixtureSet = 
				getCompoundMultiplexMixtureList(conn);
		ConnectionManager.releaseConnection(conn);
		return mixtureSet;
	}
	
	public static Collection<CompoundMultiplexMixture>getCompoundMultiplexMixtureList(
			Connection conn) throws Exception {
		
		Collection<CompoundMultiplexMixture>mixtureSet = 
				new TreeSet<CompoundMultiplexMixture>();
		String query  = 
				"SELECT MIX_ID, MIX_NAME FROM COMPOUND_MULTIPLEX_MIXTURE "
				+ "ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			CompoundMultiplexMixture p = new CompoundMultiplexMixture(
					rs.getString("MIX_ID"), 
					rs.getString("MIX_NAME"));
			mixtureSet.add(p);
		}
		rs.close();		
		ps.close();
		return mixtureSet;
	}
	
	public static void addCompoundMultiplexMixture(
			CompoundMultiplexMixture newMixture) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		addCompoundMultiplexMixture(newMixture, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addCompoundMultiplexMixture(
			CompoundMultiplexMixture newMixture, Connection conn) throws Exception {
		String query  = 
				"INSERT INTO COMPOUND_MULTIPLEX_MIXTURE "
				+ "(MIX_ID, MIX_NAME) VALUES (?,?)";
		PreparedStatement ps = conn.prepareStatement(query);
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"COMPOUND_MULTIPLEX_MIXTURE_SEQ",
				DataPrefix.COMPOUND_MULTIPLEX_MIXTURE,
				"0",
				5);
		ps.setString(1, nextId);
		ps.setString(2, newMixture.getName());
		ps.executeUpdate();
		ps.close();
		newMixture.setId(nextId);
	}
	
	public static Collection<CpdMetadataField>getCpdMetadataFields() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<CpdMetadataField>metadataFields = getCpdMetadataFields(conn);
		ConnectionManager.releaseConnection(conn);
		return metadataFields;
	}

	public static Collection<CpdMetadataField> getCpdMetadataFields(Connection conn) throws Exception{
		
		Collection<CpdMetadataFieldCategory>categories = 
				getCpdMetadataFieldCategories(conn);
		Collection<CpdMetadataField>metadataFields = 
				new HashSet<CpdMetadataField>();
		String query = 
				"SELECT FIELD_ID, FIELD_NAME, FIELD_CATEGORY FROM "
				+ "COMPOUND_COLLECTION_METADATA_FIELDS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String categoryId = rs.getString("FIELD_CATEGORY");
			CpdMetadataFieldCategory category = categories.stream().
					filter(c -> c.getId().equals(categoryId)).
					findFirst().orElse(null);
			
			CpdMetadataField p = new CpdMetadataField(
					rs.getString("FIELD_ID"), 
					rs.getString("FIELD_NAME"),
					category);
			metadataFields.add(p);
		}
		rs.close();
		ps.close();
		return metadataFields;
	}
	
	public static Collection<CpdMetadataFieldCategory> getCpdMetadataFieldCategories(Connection conn) throws Exception{
		
		Collection<CpdMetadataFieldCategory>categories = new HashSet<CpdMetadataFieldCategory>();
		String query = 
				"SELECT CATEGORY_ID, CATEGORY_NAME FROM "
				+ "COMPOUND_COLLECTION_METADATA_FIELD_CATEGORY ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()) {
			
			CpdMetadataFieldCategory p = new CpdMetadataFieldCategory(
					rs.getString("CATEGORY_ID"), 
					rs.getString("CATEGORY_NAME"));
			categories.add(p);
		}
		rs.close();
		ps.close();
		return categories;
	}
	
	public static void updateMetadataFieldForComponent(
			CompoundCollectionComponent component, 
			CpdMetadataField field, 
			String value) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
			"UPDATE COMPOUND_COLLECTION_COMPONENT_METADATA "
			+ "SET FIELD_VALUE = ? WHERE FIELD_ID = ? AND CC_COMPONENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, value);
		ps.setString(2, field.getId());
		ps.setString(3, component.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);	
		component.getMetadata().put(field, value);
	}
	
	public static String createFindByFormulaInputForMultiplex(CompoundMultiplexMixture plex) {
		
		ArrayList<String>output = new ArrayList<String>();
		output.add("# Agilent TOF Formula data store");
		output.add("# Version: 1");
		output.add("# Formula, Mass, Cpd");
		ArrayList<String>line = new ArrayList<String>();
		for(CompoundMultiplexMixtureComponent component : plex.getComponents()) {
					
			line.clear();
			CompoundCollectionComponent ccComponent = component.getCCComponent();			
			line.add(ccComponent.getMsReadyFormula().replaceAll("[\\[,\\],\\+,\\-]", ""));
			double mass = MolFormulaUtils.calculateExactMonoisotopicMass(ccComponent.getMsReadyFormula());
			line.add(MsUtils.spectrumMzExportFormat.format(mass));
			String name = "\"" + ccComponent.getCid().getCommonName() + 
					" (" + ccComponent.getCid().getPrimaryDatabaseId() + ")\"";
			line.add(name);
			output.add(StringUtils.join(line, ","));
		}
		return StringUtils.join(output, "\n");
	}
	
	public static String createPCDLImportInputForMultiplex(CompoundMultiplexMixture plex) {
		
		ArrayList<String>output = new ArrayList<String>();
		ArrayList<String>line = new ArrayList<String>();
		
		//	Header
		line.add(PCDLImportFields.NAME.getName());
		line.add(PCDLImportFields.FORMULA.getName());
		line.add(PCDLImportFields.MASS.getName());
		line.add(PCDLImportFields.ANION.getName());
		line.add(PCDLImportFields.CATION.getName());
		line.add(PCDLImportFields.CAS.getName());
		line.add(PCDLImportFields.HMDB.getName());
		line.add(PCDLImportFields.PUBCHEM.getName());
		//line.add(PCDLImportFields.SMILES.getName());
		output.add(StringUtils.join(line, ","));
		
		SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
		for(CompoundMultiplexMixtureComponent component : plex.getComponents()) {					

			line.clear();
			CompoundCollectionComponent ccComponent = component.getCCComponent();
			if(ccComponent.getMsReadySmiles() == null || ccComponent.getMsReadySmiles().isEmpty()) {
				System.err.println(
						"SMILES string absent for ccComponent ID " + ccComponent.getId() +
						"; omitting from output");
				continue;
			}			
			int charge = 0;
			double smilesMass = 0.0d;
			String mfFromFStringFromSmiles = "";
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(ccComponent.getMsReadySmiles());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				smilesMass = MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic);					
				charge = molFormula.getCharge();
			}
			else {
				System.err.println("Error parsing SMILES for ccComponent ID " + ccComponent.getId());
				System.err.println(ccComponent.getMsReadySmiles());
				return null;
			}
			line.add("\"" + ccComponent.getCid().getCommonName() + "\"");
			line.add(mfFromFStringFromSmiles.replaceAll("[\\[,\\],\\+,\\-]", ""));
			line.add(MsUtils.spectrumMzExportFormat.format(smilesMass));
			String anionString = "";
			if(charge < 0)
				anionString = "1";
			
			line.add(anionString);
			
			String cationString = "";
			if(charge > 0)
				cationString = "1";
			
			line.add(cationString);
			
			String casString = "";
			if(ccComponent.getCas() != null && !ccComponent.getCas().isEmpty())
				casString = ccComponent.getCas();
			
			line.add(casString);
			
			String hmdbString = 
					ccComponent.getCid().getDbId(CompoundDatabaseEnum.HMDB);
			
			if(hmdbString != null && !hmdbString.isEmpty())
				line.add(hmdbString.replace("HMDB00", "HMDB"));
			else
				line.add("");
			
			String pubchemString = 
					ccComponent.getCid().getDbId(CompoundDatabaseEnum.PUBCHEM);
			
			if(pubchemString != null && !pubchemString.isEmpty())
				line.add(pubchemString);
			else
				line.add("");
			
//			line.add(ccComponent.getMsReadySmiles());
			output.add(StringUtils.join(line, ","));
		}
		return StringUtils.join(output, "\n");
	}
	
	public static void updateMsReadyData(
			CompoundMultiplexMixtureComponent component, 
			String smiles, 
			String formula) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOUND_COLLECTION_COMPONENTS  " +
				"SET MS_READY_SMILES = ?, MS_READY_FORMULA = ?  " +
				"WHERE CC_COMPONENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);		
		ps.setString(1, smiles);
		ps.setString(2, formula);
		ps.setString(3, component.getCCComponent().getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static Collection<CpdMetadataField> addCpdMetadataFields(
//			Collection<String>fields, Connection conn) throws Exception {
//		
//		Collection<CpdMetadataField>metadataFields = new HashSet<CpdMetadataField>();
//		String query = 
//				"INSERT INTO COMPOUND_COLLECTION_METADATA_FIELDS "
//				+ "(FIELD_ID, FIELD_NAME) VALUES(?,?)";
//		PreparedStatement ps = conn.prepareStatement(query);
//		for(String field : fields){
//			
//			String nextId = SQLUtils.getNextIdFromSequence(conn, 
//					"CCC_METADATA_FIELD_SEQ",
//					DataPrefix.CCC_METADATA_FIELD,
//					"0",
//					5);
//			ps.setString(1, nextId);
//			ps.setString(2, field);
//			ps.executeUpdate();
//			CpdMetadataField f = new CpdMetadataField(nextId, field);
//			metadataFields.add(f);
//		}
//		ps.close();
//		return metadataFields;
//	}
	
	
}








//




