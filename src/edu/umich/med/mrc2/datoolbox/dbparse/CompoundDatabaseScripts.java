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
	
	private static String dataDir = "." + File.separator + "data" + File.separator;
	private static String configDir = dataDir + "config" + File.separator;
	private static org.slf4j.Logger logger;
	
	private static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final MDLV2000Reader molReader  = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	
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
			scanCHEBIsdfForFields();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runScript() {
		// TODO Auto-generated method stub

	}
	
	private static void scanCHEBIsdfForFields() {

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
	
	private static void generateInchiKeysFromSMILESforHMDBcompounds() throws Exception{
		
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
	
	private static void calculateHMDBFormulasAndChargesFromSmiles() throws Exception{
		
		double toRound = 1000000.0d;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES FROM COMPOUNDDB.HMDB_COMPOUND_DATA";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.HMDB_COMPOUND_DATA "
				+ "SET CHARGE = ?, FORMULA_FROM_SMILES = ?, "
				+ "MASS_FR0M_SMILES = ? WHERE ACCESSION = ?";
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

}























