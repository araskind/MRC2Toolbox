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
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.LoggerFactory;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
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
			createCoconutSMILESBasedData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	
	public static void createLipidMapsSMILESBasedData() throws Exception{
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT LMID, SMILES, MOLECULAR_FORMULA"
				+ " FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA"
				+ " WHERE SMILES IS NOT NULL";
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
	//
}























