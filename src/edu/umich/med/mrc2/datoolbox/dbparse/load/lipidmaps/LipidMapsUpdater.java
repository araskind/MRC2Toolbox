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

package edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.ChemInfoUtils;
import edu.umich.med.mrc2.datoolbox.utils.IteratingSDFReaderFixed;

public class LipidMapsUpdater {

	private static Collection<LipidMapsRecord> records;
	private static Collection<LipidMapsRecord> newRecords;
	private static Collection<String>recordIdsToReplace;

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			updateLipidMapsFromFile();			
			//	System.out.println(MolFormulaUtils.normalizeFormula("C12H21D3O2"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private static void updateLipidMapsFromFile() {
		
		File sdfInputFile = 
				new File("E:\\DataAnalysis\\Databases\\_LATEST\\LipidMaps-2024-03-04\\structures.sdf");
		System.out.println("Parsing SDF file ... ");
		System.out.println("==============================================\n");
		parseFileToRecords(sdfInputFile);
		
		System.out.println("Verifying records and updating the database ... ");
		System.out.println("==============================================\n");
		try {
			verifyAndAddRecords();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		if(!recordIdsToReplace.isEmpty()) {
			try {
				removeUpdatedRecords(recordIdsToReplace);			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		try {
			updateTaxonomyMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadNewRecordsToDatabase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void updateTaxonomyMap() throws Exception{

		Map<Integer,LipidMapsClassificationObject>lmTaxonomyMap = 
				new HashMap<Integer,LipidMapsClassificationObject>();
		newRecords.stream().flatMap(r -> r.getLmTaxonomy().stream()).
			forEach(t -> lmTaxonomyMap.put(t.hashCode(), t));
		
		Collection<String>availableClasses = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();	
		String query = "SELECT CLASS_ID FROM COMPOUNDDB.LIPIDMAPS_CLASSES";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			availableClasses.add(rs.getString(1));
		
		rs.close();
		
		List<LipidMapsClassificationObject> newClasses = 
				lmTaxonomyMap.values().stream().
					filter(o -> !availableClasses.contains(o.getCode())).
					collect(Collectors.toList());
		
		if(!newClasses.isEmpty()) {
			
			query = 
					"INSERT INTO COMPOUNDDB.LIPIDMAPS_CLASSES "
					+ "(CLASS_LEVEL, CLASS_ID, NAME) VALUES (?, ?, ?)";
			ps = conn.prepareStatement(query);
			
			for(LipidMapsClassificationObject o : newClasses) {
				
				ps.setString(1, o.getGroup().name());
				ps.setString(2, o.getCode());
				ps.setString(3, o.getName());
				ps.executeUpdate();
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void removeUpdatedRecords(Collection<String> idsToReplace) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA WHERE LMID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		for(String id : idsToReplace) {
			
			ps.setString(1, id);
			ps.executeUpdate();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	private static void verifyAndAddRecords() throws Exception {
		
		Collection<String>newRecordIds = new ArrayList<String>();
		Collection<String>mismatches = new ArrayList<String>();
		Collection<String>recordMismatches = new ArrayList<String>();
		recordIdsToReplace = new ArrayList<String>();
		newRecords = new ArrayList<LipidMapsRecord>();
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT MOLECULAR_FORMULA, INCHI_KEY, SMILES, FORMULA_FROM_SMILES "
				+ "FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA WHERE LMID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;

		int count = 0;
		
		String formula = null;
		String formulaFromSmiles = null;
		String inchiKey = null;
		String smiles = null;		
		boolean replace = false;
		for(LipidMapsRecord record : records) {
			
			replace = false;
			if(record.getLmid() == null || record.getLmid().isEmpty())
				continue;
			
			formula = null;
			inchiKey = null;
			smiles = null;
			recordMismatches.clear();
			
			ps.setString(1, record.getLmid());
			rs = ps.executeQuery();
			while(rs.next()) {

				formula = rs.getString(1);
				inchiKey = rs.getString(2);
				smiles = rs.getString(3);
				formulaFromSmiles = rs.getString(4);
			}
			rs.close();
			
			//	New record
			if(inchiKey == null) {
				
				newRecords.add(record);
				newRecordIds.add(record.getLmid());
			}
			else {
				//	Verify existing record
				if(!formula.equals(record.getCompoundIdentity().getFormula()) 
						&& !formulaFromSmiles.equals(record.getCompoundIdentity().getFormula())) {

					String fromSmiles =  
							ChemInfoUtils.generateFormulaStringFromSMILES(record.getCompoundIdentity().getSmiles());
					
					if(!formula.equals(fromSmiles) && !formulaFromSmiles.equals(fromSmiles)) {
						
						recordMismatches.add("Formula mismatch, old: " 
								+ formula + " / " + formulaFromSmiles 
								+ "\tNew: " + record.getCompoundIdentity().getFormula());
						replace = true;
					}
				}
				if(!inchiKey.equals(record.getCompoundIdentity().getInChiKey())) {
					recordMismatches.add("InChiKey mismatch, old: " 
							+ inchiKey
							+ "\tNew: " + record.getCompoundIdentity().getInChiKey());
					replace = true;
				}
				if(!smiles.equals(record.getCompoundIdentity().getSmiles())) {
					recordMismatches.add("SMILES mismatch, old: " 
							+ smiles
							+ "\tNew: " + record.getCompoundIdentity().getSmiles());
				}
				if(!recordMismatches.isEmpty()) {
					
					mismatches.add(record.getLmid());
					mismatches.addAll(recordMismatches);
					mismatches.add("************************\n");					
				}
				if(replace) {
					newRecords.add(record);
					recordIdsToReplace.add(record.getLmid());
				}
			}
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}		
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		if(!newRecordIds.isEmpty()) {
			
			File newRecordsFile = 
					new File("E:\\DataAnalysis\\Databases\\_LATEST\\LipidMaps-2024-03-04\\newRecords.txt");
			try {
				Files.write(newRecordsFile.toPath(), 
							newRecordIds,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!mismatches.isEmpty()) {
			
			File mismatchesFile = 
					new File("E:\\DataAnalysis\\Databases\\_LATEST\\LipidMaps-2024-03-04\\dataMismatches.txt");
			try {
				Files.write(mismatchesFile.toPath(), 
						mismatches,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void parseFileToRecords(File sdfInputFile) {

		IteratingSDFReaderFixed reader = null;
		records = new ArrayList<LipidMapsRecord>();
		try {
			reader = new IteratingSDFReaderFixed(
					new FileInputStream(sdfInputFile), 
					DefaultChemObjectBuilder.getInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int count = 0;
		while (reader.hasNext()) {
			IAtomContainer molecule = (IAtomContainer)reader.next();
			LipidMapsRecord record = null;
			try {
				record = LipidMapsParser.parseMoleculeToLipidMapsRecord(molecule);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if(record != null)
				records.add(record);
				
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}
	}
	
	private static void uploadNewRecordsToDatabase() throws Exception{
	
		Connection conn = ConnectionManager.getConnection();
		
		String dataQuery =
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA " +
				"(LMID, COMMON_NAME, SYSTEMATIC_NAME, MOLECULAR_FORMULA, EXACT_MASS, " +
				"INCHI, INCHI_KEY, SMILES, CATEGORY, MAIN_CLASS, "
				+ "SUB_CLASS, CLASS_LEVEL4, ABBREVIATION) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		String synonymQuery = 
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_SYNONYMS "
				+ "(LMID, NAME, NTYPE) VALUES (?, ?, ?)";
		PreparedStatement synonymPs = conn.prepareStatement(synonymQuery);
		
		String crossrefQuery = "INSERT INTO COMPOUNDDB.LIPIDMAPS_CROSSREF "
				+ "(LMID, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		PreparedStatement crossrefPs = conn.prepareStatement(crossrefQuery);

		for(LipidMapsRecord record : newRecords) {

			//	Insert compound data	
			CompoundIdentity cid = record.getCompoundIdentity();
			ps.setString(1, record.getLmid());
			ps.setString(2, record.getCommonName());
			if(record.getSysName() != null)
				ps.setString(3, record.getSysName());
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			ps.setString(4, cid.getFormula());
			ps.setDouble(5, cid.getExactMass());
			ps.setString(6, cid.getInChi());
			ps.setString(7, cid.getInChiKey());
			ps.setString(8, cid.getSmiles());
			
			String category = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.CATEGORY);
			if(category != null)
				ps.setString(9, category);
			else
				ps.setNull(9, java.sql.Types.NULL);
			
			String mainClass = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.MAIN_CLASS);
			if(mainClass != null)
				ps.setString(10, mainClass);
			else
				ps.setNull(10, java.sql.Types.NULL);
			
			String subClass = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.SUB_CLASS);
			if(subClass != null)
				ps.setString(11, subClass);
			else
				ps.setNull(11, java.sql.Types.NULL);
			
			String classLevel4 = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.CLASS_LEVEL4);
			if(classLevel4 != null)
				ps.setString(12, classLevel4);
			else
				ps.setNull(12, java.sql.Types.NULL);
			
			if(record.getAbbreviation() != null)
				ps.setString(13, record.getAbbreviation());
			else
				ps.setNull(13, java.sql.Types.NULL);
			
			ps.executeUpdate();
		
			//	Insert primary and systematic name(s)
			synonymPs.setString(1, record.getLmid());
			
			String commonName = record.getCommonName();
			String sysName = record.getSysName();
			if(commonName == null || commonName.isEmpty())
				commonName = sysName;

			if(commonName != null && !commonName.isEmpty()) {
				synonymPs.setString(2, commonName);
				synonymPs.setString(3, "PRI");
				synonymPs.addBatch();
			}
			if(sysName != null && !sysName.isEmpty()) {
				synonymPs.setString(2, sysName);
				synonymPs.setString(3, "SYS");
				synonymPs.addBatch();
			}
			//	Insert synonyms
			if(!record.getSynonyms().isEmpty()) {

				for(String syn : record.getSynonyms()) {

					synonymPs.setString(2, syn);
					synonymPs.setString(3, "SYN");
					synonymPs.addBatch();
				}
			}
			synonymPs.executeBatch();
			
			//	Insert database references
			crossrefPs.setString(1, record.getLmid());
			for (Entry<CompoundDatabaseEnum, String> entry : cid.getDbIdMap().entrySet()) {

				if(!entry.getValue().isEmpty()) {

					crossrefPs.setString(2, entry.getKey().name());
					crossrefPs.setString(3, entry.getValue());
					crossrefPs.addBatch();
				}
			}
			crossrefPs.executeBatch();
		}
		ps.close();
		synonymPs.close();
		crossrefPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void fixMissingSmilesAndInchisInCurrentDatabase() throws Exception {
		
		Collection<String>logData = new ArrayList<String>();
		
		Connection conn = ConnectionManager.getConnection();
		
		//	Update where smiles is null
		String query = "SELECT LMID, SMILES FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA "
				+ "WHERE FORMULA_FROM_SMILES IS NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA "
				+ "SET FORMULA_FROM_SMILES = ?, MASS_FROM_SMILES = ? WHERE LMID = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String lmid = rs.getString(1);
			String inchi = rs.getString(2);
			IAtomContainer mol = null;
			try {
				mol = ChemInfoUtils.generateMoleculeFromInchi(inchi);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String smiles = null; 
			if(mol != null) {
				smiles = ChemInfoUtils.generateIsomericSmilesForMolecule(mol);
			}
			else {
				logData.add("Failed to generate molecule for " + lmid);
				logData.add(inchi);
			}
			if(smiles != null) {
				updPs.setString(1, smiles);
				updPs.setString(2, lmid);
				updPs.executeUpdate();
			}
		}
		rs.close();
		
		//	Update where inchi is null
		query = "SELECT LMID, SMILES FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA "
				+ "WHERE INCHI IS NULL";
		ps = conn.prepareStatement(query);
		
		updQuery = "UPDATE COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA "
				+ "SET INCHI = ? WHERE LMID = ?";
		updPs = conn.prepareStatement(updQuery);
		
		rs = ps.executeQuery();
		while(rs.next()) {
			
			String lmid = rs.getString(1);
			String smiles = rs.getString(2);
			IAtomContainer mol = null;
			try {
				mol = ChemInfoUtils.generateMoleculeFromSMILES(smiles);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String inchi = null; 
			if(mol != null) {
				inchi = ChemInfoUtils.generateInchiForMolecule(mol);
			}
			else {
				logData.add("Failed to generate molecule for " + lmid);
				logData.add(smiles);
			}
			if(inchi != null) {
				updPs.setString(1, inchi);
				updPs.setString(2, lmid);
				updPs.executeUpdate();
			}
		}
		rs.close();
		
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
		
		if(!logData.isEmpty()) {
			
			File logFile = 
					new File("E:\\DataAnalysis\\Databases\\_LATEST\\LipidMaps-2024-03-04\\inchiSmilesUpdateLog.txt");
			try {
				Files.write(logFile.toPath(), 
						logData,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
