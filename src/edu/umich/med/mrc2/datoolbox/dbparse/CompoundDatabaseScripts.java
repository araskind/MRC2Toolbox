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

package edu.umich.med.mrc2.datoolbox.dbparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.LoggerFactory;

import ambit2.tautomers.TautomerManager;
import ambit2.tautomers.ranking.EnergyRanking;
import ambit2.tautomers.zwitterion.ZwitterionManager;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.ChemInfoUtils;
import edu.umich.med.mrc2.datoolbox.utils.CompoundStructureUtils;
import edu.umich.med.mrc2.datoolbox.utils.PubChemUtils;
import io.github.dan2097.jnainchi.InchiStatus;

public class CompoundDatabaseScripts {
	
	public static String dataDir = "." + File.separator + "data" + File.separator;
	public static String configDir = dataDir + "config" + File.separator;
	public static org.slf4j.Logger logger;
	
	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	public static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	public static final SmilesParser smipar = new SmilesParser(builder);
	public static final MDLV2000Reader molReader  = new MDLV2000Reader();
	public static InChIGeneratorFactory igfactory;
	public static InChIGenerator inChIGenerator;
	
	public static final String encoding = StandardCharsets.UTF_8.toString();
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		
		//	Configure logging
		File file = new File(configDir + "log4j2.xml");
		((LoggerContext) LogManager.getContext(false)).setConfigLocation(file.toURI());			
		logger = LoggerFactory.getLogger(CompoundDatabaseScripts.class);
		logger.info("Statring the program");
		MRC2ToolBoxConfiguration.initConfiguration();		
		try {
			//	standardizeNoConflictLipidMapsData();
			//	createLipidMapsSMILESBasedData(true);
			//	generateTautomersAndZwitterIonsForCompoundDatabase("LIPIDMAPS_COMPOUND_DATA", "LIPIDMAPS", true);
			getPubChemIdsForLipidMapsEntriesByInchiOrName();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void retTest() {
		
		String inchi = "InChI=1S/C47H87O13P/c1-3-5-7-9-11-13-15-17-19-20-22-24-26-28-30-32-34-36-41"
				+ "(49)59-39(38-58-61(55,56)60-47-45(53)43(51)42(50)44(52)46(47)54)37-57-40(48)35-33-"
				+ "31-29-27-25-23-21-18-16-14-12-10-8-6-4-2/h11,13,17,19,39,42-47,50-54H,3-10,12,14-16,"
				+ "18,20-38H2,1-2H3,(H,55,56)/b13-11-,19-17-/t39-,42?,43-,44?,45?,46?,47-/m1/s1";
		PubChemCompoundDescriptionBundle pds = null;
		try {
			pds = PubChemUtils.getCompoundDescriptionByInchi(inchi);
			System.out.println(pds.getCid());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String name = "PI(18:0/20:2(11Z,14Z))";
		PubChemCompoundDescriptionBundle pdsn = null;
		try {
			pdsn = PubChemUtils.getCompoundDescriptionByName(name);
			System.out.println(pdsn.getCid());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testUrlEncode() {
		
		String input = "PC(22:0/22:6(4Z,7Z,10Z,13Z,16Z,19Z))";
		String correct = "PC(22%3A0%2F22%3A6(4Z%2C7Z%2C10Z%2C13Z%2C16Z%2C19Z))";
		String encoded = "";
		try {
			encoded  = URLEncoder.encode(input, encoding).
				replaceAll("\\+", "%20").
				replaceAll("%28", "(").
				replaceAll("%29", ")");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!encoded.equals(correct)) {
			System.out.println(encoded);
		}		
	}
	
	public static void standardizeNoConflictLipidMapsData() {
		
		String sourceTable = "LIPIDMAPS_COMPOUND_DATA";
		String sourceDb = "LipidMaps";
		File logFile = 
				new File("E:\\DataAnalysi;s\\Databases\\_LATEST\\LipidMaps-2024-03-04\\LipidMapsNoConflictStandardizeLog_20240306.txt");
		try {
			standardizeNoConflictData(sourceTable, sourceDb, logFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void standardizeNoConflictData(
			String sourceTable, String sourceDb, File logFile) throws Exception {
		
		Collection<String>stdLog = new ArrayList<String>();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT ACCESSION, SMILES, INCHI FROM "
				+ "COMPOUNDDB." + sourceTable + " WHERE CHARGE = 0 "
				+ "AND FORMULA_CONFLICT IS NULL AND MS_READY_SMILES IS NULL "
				+ "ORDER BY ACCESSION";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = 
				"UPDATE COMPOUNDDB." + sourceTable
				+ " SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?, "
				+ "MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?, "
				+ "MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? "
				+ "WHERE ACCESSION = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		int counter = 0;
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String accession = rs.getString(1);
			String smiles = rs.getString(2);
			String inchi = rs.getString(3);
			StandardizedStructure stdMol = 
					StructureStandardizationUtils.standardizeStructure(smiles, inchi);
			
			if(stdMol == null) {
				stdLog.add("Could not process " + sourceDb + ": " + accession);
				stdLog.add(smiles);
				stdLog.add(inchi);
				stdLog.add("\n************************\n");
			}
			else {
				
				updPs.setString(1, stdMol.getFormulaStringFromSmiles());
				updPs.setDouble(2, stdMol.getSmilesMass());
				updPs.setString(3, stdMol.getStdSmiles());
				updPs.setString(4, stdMol.getStdInchiKey());
				updPs.setString(5, stdMol.getStdInchiKey().split("-")[0]);
				updPs.setInt(6, 0);
				updPs.setString(7, accession);
				updPs.executeUpdate();
			}
			counter++;
			if(counter % 100 == 0)
				System.out.print(".");
			if(counter % 10000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
		
		if(!stdLog.isEmpty()) {
			
			try {
				Files.write(logFile.toPath(), 
						stdLog, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void generateTautomersAndZwitterIonsForCompoundDatabase(
			String sourceTable, String sourceDb, boolean newEntriesOnly) throws Exception {
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT ACCESSION, MS_READY_SMILES FROM "
				+ "COMPOUNDDB." + sourceTable + " WHERE MS_READY_SMILES IS NOT NULL "
				+ "ORDER BY ACCESSION";
		if(newEntriesOnly) {
			
			query = "SELECT D.ACCESSION, D.MS_READY_SMILES  " +
					"FROM  COMPOUNDDB." + sourceTable + " D  " +
					"WHERE  D.ACCESSION NOT IN( " +
					"SELECT  T.ACCESSION  " +
					"FROM COMPOUNDDB.COMPOUND_TAUTOMERS T  " +
					"WHERE T.SOURCE_DB = '" + sourceDb + "') ";
		}
		PreparedStatement ps = conn.prepareStatement(query);
		
		String insertQuery = 
				"INSERT INTO COMPOUNDDB.COMPOUND_TAUTOMERS "
				+ "(ACCESSION, TAUTOMER_SMILES, TAUTOMER_INCHI_KEY, SOURCE_DB) VALUES (?, ?, ?, ?)";
		PreparedStatement insertPs = conn.prepareStatement(insertQuery);
		
		ArrayList<String>errorLog = new ArrayList<String>();
		Map<String,String>tautomerDataMap = new TreeMap<String,String>();
		int counter = 0;
		ResultSet rs = ps.executeQuery();
		
		TautomerManager tautomerManager = new TautomerManager();		
		tautomerManager.maxNumOfBackTracks = 20;
		tautomerManager.maxNumOfTautomerRegistrations = 100;
		tautomerManager.maxNumOfSubCombinations = 1000;
		tautomerManager.FlagCalculateCACTVSEnergyRank = false;
		tautomerManager.getKnowledgeBase().activateChlorineRules(false);
		tautomerManager.getKnowledgeBase().activateRingChainRules(false);
		tautomerManager.getKnowledgeBase().use15ShiftRules(true);
		tautomerManager.getKnowledgeBase().use17ShiftRules(true);
		tautomerManager.getKnowledgeBase().use19ShiftRules(false);
		tautomerManager.getKnowledgeBase().use13ShiftRulesOnly(false);		
		try {
			tautomerManager.setEnergyRanking(new EnergyRanking());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		ZwitterionManager zwitterionManager = new ZwitterionManager();
		zwitterionManager.FlagUseCarboxylicGroups = true;
		zwitterionManager.FlagUseSulfonicAndSulfinicGroups = true;
		zwitterionManager.FlagUsePhosphoricGroups = true;
		zwitterionManager.FlagUsePrimaryAmines = true;
		zwitterionManager.FlagUseSecondaryAmines = true;
		zwitterionManager.FlagUseTertiaryAmines = true;
		zwitterionManager.FlagFilterDuplicates = true;		
		zwitterionManager.MaxNumberOfZwitterionicPairs = 10;
		zwitterionManager.MaxNumberOfRegisteredZwitterions = 100;
		
		while(rs.next()) {

			tautomerDataMap.clear();
			String accession = rs.getString("ACCESSION");
			String smiles = rs.getString("MS_READY_SMILES");
			IAtomContainer mol = null;
			String inchiKey = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			if(mol != null) {
				
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						errorLog.add(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						errorLog.add(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if(inchiKey != null) {
				tautomerDataMap.put(smiles, inchiKey);
			}
			else {
				errorLog.add("Failed to convert SMILES to INCHI KEY for " + accession + "\t" + smiles);
			}
			//	Generate tautomers
			List<IAtomContainer> res = new ArrayList<IAtomContainer>();
			try {
				tautomerManager.setStructure(mol);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				res = tautomerManager.generateTautomersIncrementaly();				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(res.size() > 1) {
				
				for(IAtomContainer taut : res) {
					
					CompoundStructureUtils.finalizeHydrogens(taut);
					String tautSmiles = null;
					String tautInChiKey = null;
					try {
						tautSmiles = smilesGenerator.create(taut);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(tautSmiles != null) {
						try {
							inChIGenerator = igfactory.getInChIGenerator(taut);
							InchiStatus inchiStatus = inChIGenerator.getStatus();
							if (inchiStatus.equals(InchiStatus.WARNING)) {
								errorLog.add(accession + "\tInChI warning: " + inChIGenerator.getMessage());
							} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
								errorLog.add(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
							}
							tautInChiKey = inChIGenerator.getInchiKey();
						} catch (CDKException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					if(tautSmiles != null && tautInChiKey != null)
						tautomerDataMap.put(tautSmiles, tautInChiKey);					
				}
			}
			//	Generate zwitterions
			res = new ArrayList<IAtomContainer>();
			try {
				zwitterionManager.setStructure(mol);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				res = zwitterionManager.generateZwitterions();		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(res.size() > 0) {
				
				for(IAtomContainer taut : res) {
					
					CompoundStructureUtils.finalizeHydrogens(taut);
					String zwiSmiles = null;
					String zwiInChiKey = null;
					try {
						zwiSmiles = smilesGenerator.create(taut);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(zwiSmiles != null) {
						
						try {
							inChIGenerator = igfactory.getInChIGenerator(taut);
							InchiStatus inchiStatus = inChIGenerator.getStatus();
							if (inchiStatus.equals(InchiStatus.WARNING)) {
								errorLog.add(accession + "\tInChI warning: " + inChIGenerator.getMessage());
							} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
								errorLog.add(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
							}
							zwiInChiKey = inChIGenerator.getInchiKey();
						} catch (CDKException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					if(zwiSmiles != null && zwiInChiKey != null)
						tautomerDataMap.put(zwiSmiles, zwiInChiKey);					
				}
			}
			//	Insert tautomer/zwitterion data
			insertPs.setString(1, accession);
			insertPs.setString(4, sourceDb);
			for(Entry<String,String>td : tautomerDataMap.entrySet()) {
				
				insertPs.setString(2, td.getKey());
				insertPs.setString(3, td.getValue());
				insertPs.addBatch();
			}
			insertPs.executeBatch();
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		ps.close();
		insertPs.close();
		ConnectionManager.releaseConnection(conn);
		
		Path outputPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\_LATEST\\" + sourceDb + "_tautomersLog.txt");
		try {
			Files.write(outputPath, 
					errorLog, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void runScript() {
		// TODO Auto-generated method stub

	}
	
	public static void scanCHEBIsdfForFields() {

		File chebiSDFfile = new File(
				"E:\\DataAnalysis\\Databases\\_LATEST\\CHEBI-2023-01-01\\ChEBI_complete_3star.sdf");
		IteratingSDFReader reader = null;
		try {
			reader = new IteratingSDFReader(
					new FileInputStream(chebiSDFfile), 
					DefaultChemObjectBuilder.getInstance());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 1;
		Collection<String>properties = new TreeSet<String>();
		while (reader.hasNext()) {
			IAtomContainer molecule = (IAtomContainer)reader.next();			
			molecule.getProperties().keySet().stream().
				forEach(k -> properties.add(k.toString()));
		}
		Path outputPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\_LATEST\\CHEBI-2023-01-01\\CHEBI_fields.txt");
		try {
			Files.write(outputPath, 
					properties, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void generateInchiKeysFromSMILESforHMDBcompounds() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES FROM COMPOUNDDB.HMDB_COMPOUND_DATA";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA "
				+ "SET INCHI_KEY_FROM_SMILES = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		ArrayList<String>errorLog = new ArrayList<String>();
		int counter = 0;
		while(rs.next()) {
			
			String accession = rs.getString("ACCESSION");
			String smiles = rs.getString("SMILES");
			IAtomContainer mol = null;
			String inchiKey = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						errorLog.add(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						errorLog.add(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if(inchiKey != null) {
				updps.setString(1, inchiKey);
				updps.setString(2, accession);
				updps.executeUpdate();
			}
			else {
				errorLog.add("Failed to convert SMILES to INCHI KEY for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		Path outputPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\_LATEST\\HMDB-5-2022-11-17\\hmdb_inchiKeyFromSmilesLog.txt");
		try {
			Files.write(outputPath, 
					errorLog, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void calculateHMDBFormulasAndChargesFromSmiles() throws Exception{
		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES FROM COMPOUNDDB.HMDB_COMPOUND_DATA";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String accession = rs.getString("ACCESSION");
			String smiles = rs.getString("SMILES");
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				updps.setString(4, accession);
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void calculateMetaSciFormulasAndChargesFromSmiles() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT CC_COMPONENT_ID, PRIMARY_SMILES "
				+ "FROM COMPOUND_COLLECTION_COMPONENTS";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUND_COLLECTION_COMPONENTS "
				+ "SET CHARGE_FROM_PRIMARY_SMILES = ?, FORMULA_FROM_PRIMARY_SMILES = ?, "
				+ "INCHI_KEY_FROM_PRIMARY_SMILES = ?, MASS_FROM_PRIMARY_SMILES = ? "
				+ "WHERE CC_COMPONENT_ID = ?";
		
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String componentId = rs.getString("CC_COMPONENT_ID");
			String smiles = rs.getString("PRIMARY_SMILES");
			String inchiKey = null;
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(componentId + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(componentId + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				if(inchiKey != null)
					updps.setString(3, inchiKey);
				else
					updps.setNull(3, java.sql.Types.NULL);
				
				updps.setDouble(4, smilesMass);
				updps.setString(5, componentId);
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + componentId + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void createDrugBankSMILESBasedData() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES, MOL_FORMULA"
				+ " FROM COMPOUNDDB.DRUGBANK_COMPOUND_DATA"
				+ " WHERE SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.DRUGBANK_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ?, INCHI_KEY_FROM_SMILES = ?, "
				+ "INCHI_KEY_FS2D = ?, FORMULA_CONFLICT = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String smiles = rs.getString("SMILES");
			String accession = rs.getString("ACCESSION");						
			String dbFormula = rs.getString("MOL_FORMULA");
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				String inchiKey = null;
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				if(inchiKey != null) {
					updps.setString(4, inchiKey);
					updps.setString(5, inchiKey.substring(0, 14));
				}
				else {
					updps.setNull(4, java.sql.Types.NULL);
					updps.setNull(5, java.sql.Types.NULL);
				}
				if(!dbFormula.equals(mfFromFStringFromSmiles)) {
					updps.setString(6, "Y");
				}
				else {
					updps.setNull(6, java.sql.Types.NULL);
				}
				updps.setString(7, accession);				
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void createFooDbSMILESBasedData() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT PUBLIC_ID, MOLDB_SMILES"
				+ " FROM COMPOUNDDB.FOODB_COMPOUND_DATA"
				+ " WHERE MOLDB_SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.FOODB_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ?, INCHI_KEY_FROM_SMILES = ?, "
				+ "INCHI_KEY_FS2D = ? WHERE PUBLIC_ID = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String smiles = rs.getString("MOLDB_SMILES");
			String accession = rs.getString("PUBLIC_ID");						
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				String inchiKey = null;
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				if(inchiKey != null) {
					updps.setString(4, inchiKey);
					updps.setString(5, inchiKey.substring(0, 14));
				}
				else {
					updps.setNull(4, java.sql.Types.NULL);
					updps.setNull(5, java.sql.Types.NULL);
				}
				updps.setString(6, accession);				
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void createLipidMapsSMILESBasedData(boolean newOnly) throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT LMID, SMILES, INCHI, MOLECULAR_FORMULA"
				+ " FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA"
				+ " WHERE SMILES IS NOT NULL";
		if(newOnly)
			query += " AND FORMULA_FROM_SMILES IS NULL";
			
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ?, INCHI_KEY_FROM_SMILES = ?, "
				+ "INCHI_KEY_FS2D = ?, FORMULA_CONFLICT = ? WHERE LMID = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String smiles = rs.getString("SMILES");
			String accession = rs.getString("LMID");						
			String dbFormula = rs.getString("MOLECULAR_FORMULA");
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol == null) {
				String inchi = rs.getString("INCHI");
				mol = ChemInfoUtils.generateMoleculeFromInchi(inchi);
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				String inchiKey = null;
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				if(inchiKey != null) {
					updps.setString(4, inchiKey);
					updps.setString(5, inchiKey.substring(0, 14));
				}
				else {
					updps.setNull(4, java.sql.Types.NULL);
					updps.setNull(5, java.sql.Types.NULL);
				}
				if(!dbFormula.equals(mfFromFStringFromSmiles)) {
					updps.setString(6, "Y");
				}
				else {
					updps.setNull(6, java.sql.Types.NULL);
				}
				updps.setString(7, accession);				
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void createNPASMILESBasedData() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES, FORMULA"
				+ " FROM COMPOUNDDB.NPA_COMPOUND_DATA"
				+ " WHERE SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.NPA_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ?, INCHI_KEY_FROM_SMILES = ?, "
				+ "INCHI_KEY_FS2D = ?, FORMULA_CONFLICT = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String smiles = rs.getString("SMILES");
			String accession = rs.getString("ACCESSION");						
			String dbFormula = rs.getString("FORMULA");
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				String inchiKey = null;
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				if(inchiKey != null) {
					updps.setString(4, inchiKey);
					updps.setString(5, inchiKey.substring(0, 14));
				}
				else {
					updps.setNull(4, java.sql.Types.NULL);
					updps.setNull(5, java.sql.Types.NULL);
				}
				if(!dbFormula.equals(mfFromFStringFromSmiles)) {
					updps.setString(6, "Y");
				}
				else {
					updps.setNull(6, java.sql.Types.NULL);
				}
				updps.setString(7, accession);				
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void createT3DBSMILESBasedData() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES, CHEMICAL_FORMULA"
				+ " FROM COMPOUNDDB.T3DB_COMPOUND_DATA"
				+ " WHERE SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.T3DB_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ?, INCHI_KEY_FROM_SMILES = ?, "
				+ "INCHI_KEY_FS2D = ?, FORMULA_CONFLICT = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String smiles = rs.getString("SMILES");
			String accession = rs.getString("ACCESSION");						
			String dbFormula = rs.getString("CHEMICAL_FORMULA");
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				String inchiKey = null;
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				if(inchiKey != null) {
					updps.setString(4, inchiKey);
					updps.setString(5, inchiKey.substring(0, 14));
				}
				else {
					updps.setNull(4, java.sql.Types.NULL);
					updps.setNull(5, java.sql.Types.NULL);
				}
				if(dbFormula == null || !dbFormula.equals(mfFromFStringFromSmiles)) {
					updps.setString(6, "Y");
				}
				else {
					updps.setNull(6, java.sql.Types.NULL);
				}
				updps.setString(7, accession);				
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void createCoconutSMILESBasedData() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES, MOLECULAR_FORMULA"
				+ " FROM COMPOUNDDB.COCONUT_COMPOUND_DATA"
				+ " WHERE SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FROM_SMILES = ?, INCHI_KEY_FROM_SMILES = ?, "
				+ "INCHI_KEY_FS2D = ?, FORMULA_CONFLICT = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);		
		ResultSet rs = ps.executeQuery();

		int counter = 0;
		while(rs.next()) {
			
			String smiles = rs.getString("SMILES");
			String accession = rs.getString("ACCESSION");						
			String dbFormula = rs.getString("MOLECULAR_FORMULA");
			IAtomContainer mol = null;
			try {
				mol = smipar.parseSmiles(smiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mol != null) {
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = 
						Math.round(MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic) * toRound)/toRound;	
				String inchiKey = null;
				try {
					inChIGenerator = igfactory.getInChIGenerator(mol);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
					if (inchiStatus.equals(InchiStatus.WARNING)) {
						System.out.println(accession + "\tInChI warning: " + inChIGenerator.getMessage());
					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
						System.out.println(accession + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
					}
					inchiKey = inChIGenerator.getInchiKey();
				} catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				updps.setInt(1, molFormula.getCharge());
				updps.setString(2, mfFromFStringFromSmiles);
				updps.setDouble(3, smilesMass);
				if(inchiKey != null) {
					updps.setString(4, inchiKey);
					updps.setString(5, inchiKey.substring(0, 14));
				}
				else {
					updps.setNull(4, java.sql.Types.NULL);
					updps.setNull(5, java.sql.Types.NULL);
				}
				if(dbFormula == null || !dbFormula.equals(mfFromFStringFromSmiles)) {
					updps.setString(6, "Y");
				}
				else {
					updps.setNull(6, java.sql.Types.NULL);
				}
				updps.setString(7, accession);				
				updps.executeUpdate();
			}
			else {
				System.out.println("Failed to convert SMILES for " + accession + "\t" + smiles);
			}
			counter++;
			if(counter % 1000 == 0)
				System.out.print(".");
			if(counter % 30000 == 0)
				System.out.print(".\n");
		}
		rs.close();
		updps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
		
	private static void getPubChemIdsForDrugBankEntriesByInchiKey() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT D.ACCESSION, D.INCHI_KEY "
				+ "FROM COMPOUNDDB.DRUGBANK_COMPOUND_DATA D "
				+ "WHERE D.PUBCHEM_ID IS NULL AND D.INCHI_KEY IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.DRUGBANK_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchiKey = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByInchiKey(inchiKey);
			
			if(db == null) {
				notFound.add(accession + "\t" + inchiKey);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\DrugBank-5.1.10-2023-01-04\\NotFoundInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForT3DBEntries() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT D.ACCESSION, D.INCHIKEY "
				+ "FROM COMPOUNDDB.T3DB_COMPOUND_DATA D "
				+ "WHERE D.PUBCHEM_ID IS NULL AND D.INCHIKEY IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.T3DB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchiKey = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByInchiKey(inchiKey);
			
			if(db == null) {
				notFound.add(accession + "\t" + inchiKey);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\T3DB\\NotFoundInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForFooDBEntries() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = 
				"SELECT D.ACCESSION, D.INCHI_KEY_FROM_SMILES  " +
				"FROM COMPOUNDDB.FOODB_COMPOUND_DATA D  " +
				"WHERE D.PUBCHEM_ID IS NULL  " +
				"AND D.INCHI_KEY_FROM_SMILES IS NOT NULL " +
				"ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.FOODB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchiKey = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByInchiKey(inchiKey);
			
			if(db == null) {
				notFound.add(accession + "\t" + inchiKey);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\T3DB\\NotFoundInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForFooDBEntriesByName() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = 
				"SELECT D.ACCESSION, D.NAME " +
				"FROM COMPOUNDDB.FOODB_COMPOUND_DATA D  " +
				"WHERE D.PUBCHEM_ID IS NULL ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.FOODB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String compoundName = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByName(compoundName);
			
			if(db == null) {
				notFound.add(accession + "\t" + compoundName);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\T3DB\\NotFoundInPubChem_byName.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForLipidMapsEntries() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
//		String query = 
//				"SELECT D.ACCESSION, D.INCHI_KEY " +
//				"FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D  " +
//				"WHERE D.PUBCHEM_ID IS NULL  " +
//				"AND D.INCHI_KEY IS NOT NULL " +

		String query = 
				"SELECT D.ACCESSION, D.MS_READY_INCHI_KEY " +
				"FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D  " +
				"WHERE D.PUBCHEM_ID IS NULL  " +
				"AND D.MS_READY_INCHI_KEY IS NOT NULL " + 
				"AND D.MS_READY_INCHI_KEY != D.INCHI_KEY " +
				"ORDER BY 1 ";		
		PreparedStatement ps = conn.prepareStatement(query);		
		
		String query2 = "UPDATE COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchiKey = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByInchiKey(inchiKey);
			
			if(db == null) {
				notFound.add(accession + "\t" + inchiKey);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\T3DB\\LipidMapsNotFoundInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForHMDBEntries() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT D.ACCESSION, D.INCHI_KEY "
				+ "FROM COMPOUNDDB.HMDB_COMPOUND_DATA D "
				+ "WHERE D.PUBCHEM_ID IS NULL AND D.INCHI_KEY IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchiKey = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByInchiKey(inchiKey);
			
			if(db == null) {
				notFound.add(accession + "\t" + inchiKey);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\HMDB-5-2022-11-17\\HMDB_NotFoundByNameInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForHMDBEntriesByInchi() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = 
				"SELECT D.ACCESSION, D.INCHI " +
				"FROM COMPOUNDDB.HMDB_COMPOUND_DATA D  " +
				"WHERE D.PUBCHEM_ID IS NULL ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchi = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByInchi(inchi);
			
			if(db == null) {
				notFound.add(accession + "\t" + inchi);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\HMDB-5-2022-11-17\\HMDB_NotFoundByInchiInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForHMDBEntriesByName() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = 
				"SELECT D.ACCESSION, D.NAME " +
				"FROM COMPOUNDDB.HMDB_COMPOUND_DATA D  " +
				"WHERE D.PUBCHEM_ID IS NULL ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String compoundName = rs.getString(2);			
			
			PubChemCompoundDescriptionBundle db = 
					PubChemUtils.getCompoundDescriptionByName(compoundName);
			
			if(db == null) {
				notFound.add(accession + "\t" + compoundName);
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\_LATEST\\HMDB-5-2022-11-17\\HMDB_NotFoundByNameInPubChem.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForMultipleDatabases() {
		
		 try {
			getPubChemIdsForDrugBankEntriesByInchiOrName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			getPubChemIdsForFooDBEntriesByInchiOrName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			getPubChemIdsForLipidMapsEntriesByInchiOrName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void getPubChemIdsForDrugBankEntriesByInchiOrName() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT D.ACCESSION, D.INCHI, D.COMMON_NAME "
				+ "FROM COMPOUNDDB.DRUGBANK_COMPOUND_DATA D "
				+ "WHERE D.PUBCHEM_ID IS NULL AND "
				+ "(D.INCHI IS NOT NULL OR D.COMMON_NAME IS NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.DRUGBANK_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchi = rs.getString(2);	
			String name = rs.getString(3);	
			PubChemCompoundDescriptionBundle db = null;
			
			if(inchi != null && !inchi.isEmpty())
				db = PubChemUtils.getCompoundDescriptionByInchi(inchi);
			
			if(db == null) {
				
				Thread.sleep(300);
				if(name != null && !name.isEmpty())
					db = PubChemUtils.getCompoundDescriptionByName(name);
			}		
			if(db == null) {
				notFound.add(accession + "\t" + name + "\t" + inchi );
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = 
					new File("E:\\DataAnalysis\\Databases\\_LATEST\\"
							+ "DrugBank-5.1.10-2023-01-04\\NotFoundInPubChemByInchiOrName.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForFooDBEntriesByInchiOrName() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT D.ACCESSION, D.MOLDB_INCHI, D.NAME "
				+ "FROM COMPOUNDDB.FOODB_COMPOUND_DATA D "
				+ "WHERE D.PUBCHEM_ID IS NULL AND "
				+ "(D.INCHI IS NOT NULL OR D.COMMON_NAME IS NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.FOODB_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchi = rs.getString(2);	
			String name = rs.getString(3);	
			PubChemCompoundDescriptionBundle db = null;
			
			if(inchi != null && !inchi.isEmpty())
				db = PubChemUtils.getCompoundDescriptionByInchi(inchi);
			
			if(db == null) {
				
				Thread.sleep(300);
				if(name != null && !name.isEmpty())
					db = PubChemUtils.getCompoundDescriptionByName(name);
			}		
			if(db == null) {
				notFound.add(accession + "\t" + name + "\t" + inchi );
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = 
					new File("E:\\DataAnalysis\\Databases\\_LATEST\\"
							+ "FooDB-2020-04-07\\NotFoundInPubChemByInchiOrName.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void getPubChemIdsForLipidMapsEntriesByInchiOrName() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT D.ACCESSION, D.INCHI, D.COMMON_NAME, D.SYSTEMATIC_NAME "
				+ "FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D "
				+ "WHERE D.PUBCHEM_ID IS NULL AND "
				+ "(D.INCHI IS NOT NULL OR D.COMMON_NAME IS NOT NULL OR D.SYSTEMATIC_NAME IS NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA D "
				+ "SET D.PUBCHEM_ID = ? WHERE D.ACCESSION = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ArrayList<String>notFound = new ArrayList<String>();
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			Thread.sleep(300);
			
			String accession = rs.getString(1);
			String inchi = rs.getString(2);	
			String name = rs.getString(3);	
			String sysName = rs.getString(4);	
			PubChemCompoundDescriptionBundle db = null;
			
			if(inchi != null && !inchi.isEmpty())
				db = PubChemUtils.getCompoundDescriptionByInchi(inchi);
			
			if(db == null) {
				
				Thread.sleep(300);
				if(name != null && !name.isEmpty())
					db = PubChemUtils.getCompoundDescriptionByName(name);
			}	
			if(db == null) {
				
				Thread.sleep(300);
				if(sysName != null && !sysName.isEmpty())
					db = PubChemUtils.getCompoundDescriptionByName(sysName);
			}
			if(db == null) {
				notFound.add(accession + "\t" + name + "\t" + sysName + "\t"+ inchi );
			}
			else {
				ps2.setString(1, db.getCid());
				ps2.setString(2, accession);
				ps2.executeUpdate();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();

		ConnectionManager.releaseConnection(conn);
		
		if(!notFound.isEmpty()) {
			
			File mismatchLog = 
					new File("E:\\DataAnalysis\\Databases\\_LATEST\\"
							+ "LipidMaps-2024-03-04\\NotFoundInPubChemByInchiOrName.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						notFound, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}























