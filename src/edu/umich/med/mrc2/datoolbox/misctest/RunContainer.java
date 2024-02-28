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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.json.JSONObject;
import org.openscience.cdk.Atom;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.isomorphism.IsomorphismTester;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.w3c.dom.Node;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.LipidMapsClassifier;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireObject;
import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireOntologyEntry;
import edu.umich.med.mrc2.datoolbox.data.classyfire.ClassyFireOntologyLevel;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.data.thermo.raw.ThermoRawMetadata;
import edu.umich.med.mrc2.datoolbox.data.thermo.raw.ThermoRawMetadataComparator;
import edu.umich.med.mrc2.datoolbox.data.thermo.raw.ThermoUtils;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.database.lipid.LipidOntologyUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassification;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassificationObject;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsFields;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTMSPParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTPeptideMSPParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTPeptideTandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTTandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex.MultiplexLoadTempObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.met.MoleculeEquivalence;
import edu.umich.med.mrc2.datoolbox.utils.ClassyFireUtils;
import edu.umich.med.mrc2.datoolbox.utils.CompoundStructureUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.JSONUtils;
import edu.umich.med.mrc2.datoolbox.utils.MSReadyUtils;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;
import edu.umich.med.mrc2.datoolbox.utils.PubChemUtils;
import edu.umich.med.mrc2.datoolbox.utils.WebUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;
import io.github.dan2097.jnainchi.InchiStatus;

public class RunContainer {

	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	private static final InChITautomerGenerator tautgen = new InChITautomerGenerator(InChITautomerGenerator.KETO_ENOL);
	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static Aromaticity aromaticity;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			readThermoWorklistFromJson();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void copyRawData() {
		
		Path destinationPath = Paths.get("Y:\\_QUALTMP\\2022_0421_Rat_FM_RPLCSemiPrep_HSST3_Fractions_Pos");			
		Path sourcePath = Paths.get("R:\\Metabolomics-BRCF\\Shared\\UserBackup\\Brady\\Data\\2022_0421_Rat_FM_RPLCSemiPrep_HSST3_Fractions_Pos");
		List<Path> pathList = new ArrayList<Path>();
		try {
			pathList = Files.find(sourcePath,
					2, (filePath, fileAttr) -> (filePath.toString().matches(".+HSST3_Pos_Fraction_.._ID_.+"))).
				collect(Collectors.toList());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for(Path rdp : pathList) {
			
			String rawFileName = FilenameUtils.getName(rdp.toString());
			Path newRdp = Paths.get(destinationPath.toString(), rawFileName);
			try {
				Files.copy(rdp, newRdp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}				
	}
	
	private static void testHmdbXmlFetch(String accession) {
		
		InputStream hmdbXmlStream = null;
		try {
			hmdbXmlStream = WebUtils.getInputStreamFromURL("https://hmdb.ca/metabolites/" + accession + ".xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(hmdbXmlStream == null)
			return;
		
		Document xmlDocument = XmlUtils.readXmlStream(hmdbXmlStream);
		Element errorElement = xmlDocument.getRootElement().getChild("error");
		System.out.println("***");
	}
	
	private static void copyQcanvasResults() {
		
		Path sourceFolderPath = 
				Paths.get("O:\\_QCANVAS\\EX01263\\Kidney-POS-named-rep");		
		String destinationFolder = "Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\QCeval";
		Set<File> results = new TreeSet<File>();
		FIOUtils.findFilesByNameRecursively(sourceFolderPath.toFile(), "normalized_by_SERRF_with_blanks_and_NAs.csv", results);			
		
//		List<Path> dataFilePathList = FIOUtils.findFilesByName(sourceFolderPath,
//				"normalized_by_SERRF_with_blanks_and_NAs.csv");
		List<String> readInString = new ArrayList<String>();
		List<String> dfString = new ArrayList<String>();
		int counter = 1;
		for (File dataFile : results) {
			
			String newFileName = 
					FileNameUtils.getBaseName(dataFile.getName()) + "-" + counter + ".csv";			
			try {
				Files.copy(Paths.get(dataFile.getAbsolutePath()), 
						Paths.get(destinationFolder, newFileName),
					      StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String datName = "data" + counter;
			dfString.add("\"" + datName + "\" = " + datName);
			String readFileString = datName + " <- read.csv('" + newFileName + "', check.names=FALSE, row.names = 1)";
			readInString.add(readFileString);
			counter++;
		}
		readInString.add("ld <- list(" + StringUtils.join(dfString, ",") + ")");
		Path readInFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\4BIC\\QCeval\\readIn.R");
		try {
			Files.write(
					readInFilePath, 
					readInString, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void parseLibraryFile() {

		File libraryFile = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST peptide library"
						+ "\\Mouse\\Mouse HCD Library 2014-11-24\\cptac2_mouse_hcd_selected.msp");
		List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(libraryFile);
		Collection<NISTPeptideTandemMassSpectrum>msmsDataSet = 
				new ArrayList<NISTPeptideTandemMassSpectrum>();
		
		for(List<String> chunk : mspChunks) {

			NISTPeptideTandemMassSpectrum msms = null;
			try {
				msms = NISTPeptideMSPParser.parseNistMspDataSource(chunk);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(msms != null) {	
				
				System.out.println("************");
				msmsDataSet.add(msms);				
			}
		}
	}
	
	private static void testSMilesInchiMatcher() {
		
		String smiles = "O=C1C=CC(O)(C=C1Br)CC(=O)N";
		String inchi = "InChI=1S/C8H8BrNO3/c9-5-3-8(13,4-7(10)12)2-1-6(5)11/h1-3,13H,4H2,(H2,10,12)";
		
		boolean match = CompoundStructureUtils.doSmilesMatchInchi(smiles, inchi);
		System.out.println(Boolean.toString(match));
	}
	
	private static void markCoconutSmilesInchiMismatches() throws Exception {
		
		initCDKFunctions();
		IsomorphismTester imTester = new IsomorphismTester();
		Collection<CompoundIdentity>compounds = fetchCoconutDataForCuration();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET SMILES_INCHI_MISMATCH = 'Y' WHERE ACCESSION = ? ";	
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(CompoundIdentity cid : compounds) {
			
			//	From InChi
			IAtomContainer inchiAtomContainer = null;
			try {
				inchiAtomContainer = convertInchi2Mol(cid.getInChi());
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(inchiAtomContainer == null) {
				System.out.println("\nFailed to parse InChi for " + cid.getPrimaryDatabaseId());
				continue;
			}
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(inchiAtomContainer);
			} catch (CDKException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				CDKHydrogenAdder.getInstance(inchiAtomContainer.getBuilder()).addImplicitHydrogens(inchiAtomContainer);
			} catch (CDKException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//	From SMILES
			IAtomContainer smilesAtomContainer = null;
			try {
				smilesAtomContainer = smipar.parseSmiles(cid.getSmiles());
			} catch (InvalidSmilesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(smilesAtomContainer == null) {				
				System.out.println("\nFailed to parse SMILES for " + cid.getPrimaryDatabaseId());
				continue;
			}			
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(smilesAtomContainer);
			} catch (CDKException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				CDKHydrogenAdder.getInstance(smilesAtomContainer.getBuilder()).addImplicitHydrogens(smilesAtomContainer);
			} catch (CDKException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	        MoleculeEquivalence eq = 
	        		new MoleculeEquivalence(smilesAtomContainer, inchiAtomContainer);	        
			boolean isEquivalent = eq.areEquivalent();
			if(isEquivalent) {
				continue;
			}
			else {
				List<IAtomContainer>tautomers = new ArrayList<IAtomContainer>();
				try {
					tautomers = tautgen.getTautomers(smilesAtomContainer);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!tautomers.isEmpty()) {
					
					for(IAtomContainer tautomer : tautomers) {
						
				        MoleculeEquivalence teq = 
				        		new MoleculeEquivalence(tautomer, inchiAtomContainer);
				        if(teq.areEquivalent()) {
				        	isEquivalent = true;
				        	break;
				        }
					}					
				}
			}
	        if (!isEquivalent) {
				System.out.println("SMILES: " + cid.getSmiles() +"\tInChi: " + cid.getInChi());
				ps.setString(1, cid.getPrimaryDatabaseId());
				ps.executeUpdate();
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}	
	
	private static void insetCoconutAnnotationLevels() throws Exception {
		
		File mappingFile = 
				new File("E:\\DataAnalysis\\Databases\\_LATEST\\COCONUT-2022-01\\CSV\\coconut_annotation_levels.txt");
		String[][] mappingData = null;
		try {
			mappingData = DelimitedTextParser.parseTextFileWithEncoding(
							mappingFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET ANNOT_LEVEL = ? WHERE ACCESSION = ? ";		
		PreparedStatement ps = conn.prepareStatement(query);
		int count = 0;
		for(String[]md : mappingData) {
			ps.setInt(1, Integer.valueOf(md[1]));
			ps.setString(2, md[0]);
			ps.addBatch();
			count++;
			if (count % 1000 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		ps.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void calculateCoconutCompoundDataFromInChi() throws Exception {
			
			initCDKFunctions();
			int count = 0;
			Connection conn = ConnectionManager.getConnection();
			String smilesQuery = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET CDK_SMILES = ?, CDK_FORMULA = ?, CDK_MASS = ?, "
				+ "CDK_INCHI_KEY = ?, CDK_INCHI_KEY2D = ?"
				+ " WHERE ACCESSION = ?";	
		
			PreparedStatement smilesPs = conn.prepareStatement(smilesQuery);
			
			Collection<CompoundIdentity>compounds = fetchCoconutDataForCuration();
			for(CompoundIdentity cid : compounds) {
				
				IAtomContainer atomContainer = null;
				try {
					atomContainer = convertInchi2Mol(cid.getInChi());
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(atomContainer == null)
					continue;

				try {
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
				} catch (CDKException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				try {
					CDKHydrogenAdder.getInstance(atomContainer.getBuilder()).addImplicitHydrogens(atomContainer);
				} catch (CDKException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				String smiles = "";		
				try {
					smiles = smilesGenerator.create(atomContainer);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(atomContainer);			
				String mfFromFStringFromSmiles = 
						MolecularFormulaManipulator.getString(molFormula);	
				double smilesMass = MolecularFormulaManipulator.getMass(
								molFormula, MolecularFormulaManipulator.MonoIsotopic);
				
				String inchiKey = "";
				try {
					inChIGenerator = igfactory.getInChIGenerator(atomContainer);
					InchiStatus inchiStatus = inChIGenerator.getStatus();
//					if (inchiStatus.equals(InchiStatus.WARNING)) {
//						System.out.println("InChI warning: " + inChIGenerator.getMessage());
//					} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
//						System.out.println("InChI failed: [" + inChIGenerator.getMessage() + "]");
//					}
					inchiKey = inChIGenerator.getInchiKey();
				} 
				catch (CDKException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
				if(smiles != null && !smiles.isEmpty() && inchiKey != null && !inchiKey.isEmpty()) {
					
					smilesPs.setString(1, smiles);
					smilesPs.setString(2, mfFromFStringFromSmiles);
					smilesPs.setDouble(3, smilesMass);
					smilesPs.setString(4, inchiKey);
					smilesPs.setString(5, inchiKey.substring(0, 14));
					smilesPs.setString(6, cid.getPrimaryDatabaseId());
					smilesPs.executeUpdate();
				}
				else {
					System.out.println("Conversion failed: " + cid.getPrimaryDatabaseId());
				}			
				count++;
				if(count % 50 == 0)
					System.out.print(".");
				
				if(count % 5000 == 0)
					System.out.println(".");
			}	
			smilesPs.close();
	}
	
	private static void cleanupCoconutCompoundData() throws Exception {
		
		initCDKFunctions();
		int count = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET MS_READY_MOL_FORMULA = ?, MS_READY_EXACT_MASS = ?,  " +
				"MS_READY_SMILES = ?, MS_READY_INCHI_KEY = ?,  " +
				"MS_READY_INCHI_KEY2D = ?, MS_READY_CHARGE = ? " +
				"WHERE ACCESSION = ? ";		
		PreparedStatement ps = conn.prepareStatement(query);
		
		String smilesQuery = 
				"UPDATE COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"SET CDK_SMILES = ?, CDK_FORMULA = ?, CDK_MASS = ?, "
				+ "CDK_INCHI_KEY = ?, CDK_INCHI_KEY2D = ?"
				+ " WHERE ACCESSION = ?";	
		PreparedStatement smilesPs = conn.prepareStatement(smilesQuery);
		
		Collection<CompoundIdentity>compounds = fetchCoconutDataForCuration();
		for(CompoundIdentity cid : compounds) {
			
			CompoundIdentity fixed = neutralizeSmiles(cid);
			if(fixed.getInChiKey() == null) {
				count++;
				continue;
			}
			if(fixed.getCharge() == 0) {
				
				ps.setString(1, fixed.getFormula());	//	MS_READY_MOL_FORMULA
				ps.setDouble(2, fixed.getExactMass());		//	MS_READY_EXACT_MASS
				ps.setString(3, fixed.getSmiles());		//	MS_READY_SMILES
				ps.setString(4, fixed.getInChiKey());	//	MS_READY_INCHI_KEY
				ps.setString(5, fixed.getInChiKey().substring(0, 14));	//	MS_READY_INCHI_KEY2D
				ps.setInt(6, fixed.getCharge());		//	MS_READY_CHARGE
				ps.setString(7, fixed.getPrimaryDatabaseId());			//	ACCESSION		
				ps.executeUpdate();
			}
			smilesPs.setString(1, fixed.getSmiles());
			smilesPs.setString(2, fixed.getFormula());
			smilesPs.setDouble(3, fixed.getExactMass());
			smilesPs.setString(4, fixed.getInChiKey());
			smilesPs.setString(5, fixed.getInChiKey().substring(0, 14));
			smilesPs.setString(6, fixed.getPrimaryDatabaseId());
			smilesPs.executeUpdate();
			
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println(".");
		}
		ps.close();	
		smilesPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private  static CompoundIdentity neutralizeSmiles(CompoundIdentity charged) {
		
		CompoundIdentity fixed = MSReadyUtils.neutralizePhosphoCholine(charged);
		if(fixed != null)
			return fixed;
		
		fixed = MSReadyUtils.neutralizeSmiles(charged);
		if(fixed != null)
			return fixed;
		else
			return charged;
	}
	
	private static void initCDKFunctions() {
		
		aromaticity = new Aromaticity(
				ElectronDonation.cdk(),
                Cycles.or(Cycles.all(), Cycles.all(6)));
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static IAtomContainer convertInchi2Mol(String inchi) throws CDKException {

		if (inchi == null)
			throw new NullPointerException("Given InChI is null");
		if (inchi.isEmpty())
			throw new IllegalArgumentException("Empty string given as InChI");

		final InChIToStructure converter = igfactory.getInChIToStructure(inchi, builder);
		if (converter.getStatus() == InchiStatus.SUCCESS || converter.getStatus() == InchiStatus.WARNING) {
			return converter.getAtomContainer();
		} else {
			System.out.println("Error while parsing InChI:\n'" + inchi + "'\n-> " + converter.getMessage());
			final IAtomContainer a = converter.getAtomContainer();
			if (a != null)
				return a;
			else
				throw new CDKException(converter.getMessage());
		}
	}
	
	private static Collection<CompoundIdentity> fetchCoconutDataForCuration() throws Exception{

		Collection<CompoundIdentity>idList = new ArrayList<CompoundIdentity>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ACCESSION, NAME, MOLECULAR_FORMULA, CDK_MASS, SMILES, INCHI " +
			"FROM COMPOUNDDB.COCONUT_COMPOUND_DATA D "
			+ "WHERE SMILES IS NOT NULL AND INCHI IS NOT NULL ORDER BY 1";
 		
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()){

			CompoundIdentity identity = new CompoundIdentity(
					CompoundDatabaseEnum.COCONUT, 
					rs.getString("ACCESSION"),
					rs.getString("NAME"), 					
					rs.getString("NAME"), 
					rs.getString("MOLECULAR_FORMULA"), 
					rs.getDouble("CDK_MASS"), 
					rs.getString("SMILES"));
			identity.setInChi(rs.getString("INCHI"));
			idList.add(identity);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return idList;		
	}
	
	private static void fixChargedPyrrole() {
		
		CompoundIdentity charged = new CompoundIdentity();
		charged.setSmiles("O=C(CC(=O)C1C(=CC=2N=C[CH-]C2C1CCCCC=3C=CC=C(O)C3)CNC4=NC=C[CH-]4)CCC5=CC(OC6CCCC6)=C(O)C(=C5)CNC");
		CompoundIdentity fixed = MSReadyUtils.neutralizeSmiles(charged);
		System.out.println(fixed.getSmiles());
	}
	
	private static void fixChargedLipid() throws InvalidSmilesException {

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IAtomContainer atomContainer = sp.parseSmiles(
				"[H]C1=C(OC(CCCCCCCCCCC(OCC(OC(CCCCCCCCCCC2=C(C(C)=C(O2)CCCCC)C)=O)COP(O)(OCC[N+](C)(C)C)=O)=O)=C1C)CCCCC");
		
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
		} catch (CDKException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			CDKHydrogenAdder.getInstance(atomContainer.getBuilder()).addImplicitHydrogens(atomContainer);
		} catch (CDKException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		SmartsPattern phosCholPattern = SmartsPattern.create("OP(OCC[N+](C)(C)C)(OC)=O");
		SmartsPattern poPattern = SmartsPattern.create("[P]O");

		int[] phosCholMapping = phosCholPattern.match(atomContainer);
		Mappings poMappings = poPattern.matchAll(atomContainer);
		if (phosCholMapping.length > 0 && poMappings.count() > 0) {
			
			for(int[] poMapping : poMappings) {
				
				int[] intersect = Arrays.stream(phosCholMapping).distinct()
						.filter(x -> Arrays.stream(poMapping).anyMatch(y -> y == x)).toArray();
				for (int i : intersect) {
					
					IAtom atom = atomContainer.getAtom(i);
					
					if (atom.getSymbol().equals("O") && atom.getImplicitHydrogenCount() == 1) {

						IAtom hydroxyl = new Atom("O");
						  hydroxyl.setImplicitHydrogenCount(0);
						  hydroxyl.setFormalCharge(-1);
						AtomContainerManipulator.replaceAtomByAtom(atomContainer, atom, hydroxyl);
						String smiles = "";		
						try {
							smiles = smilesGenerator.create(atomContainer);
						} catch (CDKException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
						System.out.println(smiles);
						return;
					}
				}
			}
		}
	}
	
	private static void readThermoWorklistFromJson() {
		
		String outputName = 
				"Y:\\F2rats\\2023-03-15 F2 plasma RPLC new\\RPLCpos\\2023-03-15-F2-plasma-RPLC-new-POS.txt";
		File jsonFolder = new File("Y:\\F2rats\\2023-03-15 F2 plasma RPLC new\\RPLCpos\\JSON");
		File[] jsonFileList = JSONUtils.getJsonFileList(jsonFolder);
		Collection<ThermoRawMetadata>metadataList = 
				new ArrayList<ThermoRawMetadata>();

		for(File jsonFile : jsonFileList) {
			
			JSONObject jso = JSONUtils.readJsonFromFile(jsonFile);	
			
			ThermoRawMetadata md = 
					ThermoUtils.parseMetadataObjectFromJson(
							FilenameUtils.getBaseName(jsonFile.getName().replace("-metadata", "")), jso);
			if(md != null)
				metadataList.add(md);
		}
		metadataList = metadataList.stream().
				sorted(new ThermoRawMetadataComparator(SortProperty.injectionTime)).
				collect(Collectors.toList());		
		
		//	Create output 
		Collection<String>dataToExport = new ArrayList<String>();
		String[] header = new String[] {
			"MRC2 sample ID",	
			"sample_id",	
			"raw_file",	
			"Injection time",	
			"Sample Position",	
			"Sample Name",	
			"sample_type",	
			"sample_order",	
			"batch_override",	
		};
		dataToExport.add(StringUtils.join(header, "\t"));
		Collection<String>line = new ArrayList<String>();
		int counter = 1;
		for(ThermoRawMetadata md : metadataList) {
			
			line.clear();
			line.add(""); //"MRC2 sample ID",	
			line.add(md.getSampleName()); //"sample_id",	
			line.add(md.getFileName()); //"raw_file",	
			line.add(MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(md.getInjectionTime())); //"Injection time",	
			line.add(md.getSamplePosition()); //"Sample Position",	
			line.add(md.getSampleName()); //"Sample Name",	
			line.add(""); //"sample_type",	
			line.add(Integer.toString(counter)); //"sample_order",	
			line.add(""); //"batch_override",
			dataToExport.add(StringUtils.join(line, "\t"));
			counter++;
		}	
		Path outputPath = Paths.get(outputName);
		try {
			Files.write(outputPath, 
					dataToExport, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setNIST20asEntrySource() throws Exception{
		
		String mspDirectoryPath = "E:\\DataAnalysis\\Databases\\NIST\\NIST20-export\\EXPORT\\MSP";
		List<File>mspFiles = null;
		try {
			mspFiles = Files.find(Paths.get(mspDirectoryPath), 1,
					(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
					map(p -> p.toFile()).collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE REF_MSMS_LIBRARY_COMPONENT SET LIBRARY_NAME = ? "
				+ "WHERE LIBRARY_NAME = ? AND ORIGINAL_LIBRARY_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, "hr_msms_nist");
		ps.setString(2, "nist_msms");
		for(File msp : mspFiles) {
			
			System.out.println("Starting to process " + FilenameUtils.getBaseName(msp.getName()));
			try {
				List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(msp);
				int count = 1;
				for(List<String> msmsChunk : mspChunks) {
					
					NISTTandemMassSpectrum msms = NISTMSPParser.parseNistMspDataSource(msmsChunk);
					if(msms.getNistNum() > 0) {
						ps.setString(3, Integer.toString(msms.getNistNum()));
						ps.executeUpdate();
					}
					count++;
					if(count % 50 == 0)
						System.out.print(".");
					
					if(count % 5000 == 0)
						System.out.println(".");
				}
				System.out.println(FilenameUtils.getBaseName(msp.getName()) + " was processed.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void classyfyWithLipidMaps() throws Exception{
		
		String outputName = 
				"E:\\_Downloads\\ClassyFireJSON\\CLASSIFICATION_WITH_LIPID_MAPS_ONLY.TXT";
		File inchiKeyMappingFile = 
				new File("E:\\_Downloads\\ClassyFireJSON\\4ClassyFire\\KeyRemap.txt");
		String[][] inchiKeyMappingData = null;
		try {
			inchiKeyMappingData = DelimitedTextParser.parseTextFileWithEncoding(
							inchiKeyMappingFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Collection<LipidMapsClassificationObject> lmClasses = 
				new ArrayList<LipidMapsClassificationObject>();
		try {
			lmClasses = LipidMapsParser.getLipidMapsClasses();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<String,LipidMapsClassificationObject> lmClassesMap = lmClasses.stream()
			      .collect(Collectors.toMap(LipidMapsClassificationObject::getCode, Function.identity()));
		
		ArrayList<String>output = new ArrayList<String>();
		String[] headerPieces = new String[] {
				"InChIKey",
				"InChIKey-original",
				LipidMapsClassification.CATEGORY.name(),
				LipidMapsClassification.MAIN_CLASS.name(),
				LipidMapsClassification.SUB_CLASS.name(),
				LipidMapsClassification.CLASS_LEVEL4.name(),
		};		
		char tabDelimiter = MRC2ToolBoxConfiguration.getTabDelimiter();
		output.add(StringUtils.join(headerPieces, tabDelimiter));
		ArrayList<String>line = new ArrayList<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT CATEGORY, MAIN_CLASS, SUB_CLASS, CLASS_LEVEL4 " +
				"FROM COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA " +
				"WHERE INCHI_KEY = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		for(int i=1; i<inchiKeyMappingData.length; i++) {
			
			line.clear();		
			line.add(inchiKeyMappingData[i][1]);
			line.add(inchiKeyMappingData[i][0]);
			String category = "";
			String main_class = "";
			String sub_class = "";
			String class_level4 = "";
			
			ps.setString(1, inchiKeyMappingData[i][1]);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				if(rs.getString("CATEGORY") != null)
					category = lmClassesMap.get(rs.getString("CATEGORY")).getName();
				
				if(rs.getString("MAIN_CLASS") != null)
					main_class = lmClassesMap.get(rs.getString("MAIN_CLASS")).getName();
				
				if(rs.getString("SUB_CLASS") != null)
					sub_class = lmClassesMap.get(rs.getString("SUB_CLASS")).getName();
				
				if(rs.getString("CLASS_LEVEL4") != null)
					class_level4 = lmClassesMap.get(rs.getString("CLASS_LEVEL4")).getName();
			}
			rs.close();
			line.add(category);
			line.add(main_class);
			line.add(sub_class);
			line.add(class_level4);
			output.add(StringUtils.join(line, tabDelimiter));
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		Path logPath = Paths.get(outputName);
		try {
			Files.write(logPath, 
					output, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void parseJsonsFromClassyFireWithLipidMapsToTable() {
		
		String outputName = 
				"E:\\_Downloads\\ClassyFireJSON\\CF_CLASSIFICATION_WITH_LIPID_MAPS.TXT";
		File jsonFolder = new File("E:\\_Downloads\\ClassyFireJSON");
		File[] jsonFileList = JSONUtils.getJsonFileList(jsonFolder);
		File inchiKeyMappingFile = 
				new File("E:\\_Downloads\\ClassyFireJSON\\4ClassyFire\\KeyRemap.txt");
		Collection<ClassyFireObject>cfObjetList = 
				new ArrayList<ClassyFireObject>();
		for(File jsonFile : jsonFileList) {
			
			JSONObject jso = JSONUtils.readJsonFromFile(jsonFile);			
			ClassyFireObject cfObj = 
					ClassyFireUtils.parseJsonToClassyFireObject(jso);
			if(cfObj.getInchiKey() == null) {
				cfObj.setName(jsonFile.getName());
			}
			cfObjetList.add(cfObj);
		}
		Collection<LipidMapsClassificationObject> lmClasses = 
				new ArrayList<LipidMapsClassificationObject>();
		try {
			lmClasses = LipidMapsParser.getLipidMapsClasses();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<String,LipidMapsClassificationObject> lmClassesMap = lmClasses.stream()
			      .collect(Collectors.toMap(LipidMapsClassificationObject::getCode, Function.identity()));
		
		Map<String,String> inchiKeyMap = new TreeMap<String,String>();
		
		String[][] inchiKeyMappingData = null;
		try {
			inchiKeyMappingData = DelimitedTextParser.parseTextFileWithEncoding(
							inchiKeyMappingFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=1; i<inchiKeyMappingData.length; i++)
			inchiKeyMap.put(inchiKeyMappingData[i][1], inchiKeyMappingData[i][0]);
				
		ArrayList<String>output = new ArrayList<String>();
		String[] headerPieces = new String[] {
				"InChIKey",
				"InChIKey-original",
				"SMILES",
				"Kingdom",
				"Superclass",
				"Class",
				"Subclass",
				"Direct parent",
				"Intermediate node 1",
				"Intermediate node 2",
				"Intermediate node 3",
				LipidMapsClassification.CATEGORY.name(),
				LipidMapsClassification.MAIN_CLASS.name(),
				LipidMapsClassification.SUB_CLASS.name(),
				LipidMapsClassification.CLASS_LEVEL4.name(),
		};
		Map<String,Collection<String>>lipidMapsClassification = 
				new TreeMap<String,Collection<String>>();
		char tabDelimiter = MRC2ToolBoxConfiguration.getTabDelimiter();
		output.add(StringUtils.join(headerPieces, tabDelimiter));
		ArrayList<String>line = new ArrayList<String>();
		for(ClassyFireObject co : cfObjetList) {
			
			if(co.getInchiKey() == null) {
				System.out.println(co.getName());
				continue;
			}			
			line.clear();
			lipidMapsClassification.clear();			
			line.add(co.getInchiKey());
			String origInChiKey = inchiKeyMap.get(co.getInchiKey());
			if(origInChiKey != null)
				line.add(origInChiKey);
			else
				line.add("");
			
			if(co.getSmiles() != null)
				line.add(co.getSmiles());
			else
				line.add("");
			
			Map<ClassyFireOntologyLevel, ClassyFireOntologyEntry> classifier = co.getPrimaryClassification();
			
			if(classifier.get(ClassyFireOntologyLevel.KINGDOM) != null)
				line.add(classifier.get(ClassyFireOntologyLevel.KINGDOM).getName());
			else
				line.add("");
			
			if(classifier.get(ClassyFireOntologyLevel.SUPERCLASS) != null)
				line.add(classifier.get(ClassyFireOntologyLevel.SUPERCLASS).getName());
			else
				line.add("");
			
			if(classifier.get(ClassyFireOntologyLevel.CLASS) != null)
				line.add(classifier.get(ClassyFireOntologyLevel.CLASS).getName());
			else
				line.add("");
		
			if(classifier.get(ClassyFireOntologyLevel.SUBCLASS) != null)
				line.add(classifier.get(ClassyFireOntologyLevel.SUBCLASS).getName());
			else
				line.add("");
			
			if(classifier.get(ClassyFireOntologyLevel.DIRECT_PARENT) != null)
				line.add(classifier.get(ClassyFireOntologyLevel.DIRECT_PARENT).getName());
			else
				line.add("");
			
			if(co.getIntermediateNodes().size() > 0 )
				line.add(co.getIntermediateNodes().get(0).getName());
			else
				line.add("");
			
			if(co.getIntermediateNodes().size() > 1 )
				line.add(co.getIntermediateNodes().get(1).getName());
			else
				line.add("");
			
			if(co.getIntermediateNodes().size() > 2 )
				line.add(co.getIntermediateNodes().get(2).getName());
			else
				line.add("");
						
			//	LipidMaps
			if(co.getPredictedLipidMapsTerms().isEmpty()) {
				line.add("");
				line.add("");
				line.add("");
				line.add("");
			}
			else {
				for(String plt : co.getPredictedLipidMapsTerms().keySet()) {
					
					String family = plt.substring(0, 2);
					if(!lipidMapsClassification.containsKey(family))
						lipidMapsClassification.put(family, new TreeSet<String>());	
					
					lipidMapsClassification.get(family).add(plt);
				}
				for(Entry<String, Collection<String>> es : lipidMapsClassification.entrySet()) {
										 
					String category = "";
					String main_class = "";
					String sub_class = "";
					String class_level4 = "";
					for(String cid : es.getValue()) {
						
						LipidMapsClassificationObject lmco = lmClassesMap.get(cid);
						if(lmco == null) {
							System.out.println(cid);
							continue;
						}
						if(lmco.getGroup().equals(LipidMapsClassification.CATEGORY))
							category = lmco.getName();
						
						if(lmco.getGroup().equals(LipidMapsClassification.MAIN_CLASS))
							main_class = lmco.getName();
						
						if(lmco.getGroup().equals(LipidMapsClassification.SUB_CLASS))
							sub_class = lmco.getName();
						
						if(lmco.getGroup().equals(LipidMapsClassification.CLASS_LEVEL4))
							class_level4 = lmco.getName();
					}
					line.add(category);
					line.add(main_class);
					line.add(sub_class);
					line.add(class_level4);
				}			
			}
			output.add(StringUtils.join(line, tabDelimiter));
		}
		Path logPath = Paths.get(outputName);
		try {
			Files.write(logPath, 
					output, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void dowloadJsonsFromClassyFire() {
				
		File inchiKeyFile = 
				new File("E:\\_Downloads\\ClassyFireJSON\\4ClassyFire\\2023_0404_All_Unique_InChIKeyNotFoundCorrected.txt");
		String outputBase = "E:\\_Downloads\\ClassyFireJSON";
		File outputDir = new File(outputBase);
		FileFilter jsonFilter = new RegexFileFilter(".+\\.json$");
		File[] jsonFileList = outputDir.listFiles(jsonFilter);
		ArrayList<String>processed = new ArrayList<String>();
		for(File jsf : jsonFileList)
			processed.add(FilenameUtils.getBaseName(jsf.getName()));
		
		
		String[][] inchiKeyData = null;
		try {
			inchiKeyData = DelimitedTextParser.parseTextFileWithEncoding(
							inchiKeyFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String>missing = new ArrayList<String>();
		String baseUrl = "http://classyfire.wishartlab.com/entities/";
		for(int i=0; i<inchiKeyData.length; i++) {
			
			String inchiKey = inchiKeyData[i][0];
			if(processed.contains(inchiKey))
				continue;
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject jso = JSONUtils.readJsonFromUrl(baseUrl + inchiKey+ ".json");
			if(jso == null) {
				missing.add(inchiKey + "\tNot found");
				System.out.println(inchiKey + "\tNot found");
			}
			else if(jso != null && jso.has("error")) {
				missing.add(inchiKey + "\t" + jso.get("error").toString());
				System.out.println(inchiKey + "\t" + jso.get("error").toString());
			}
			else {
				File outFile = Paths.get(outputBase, inchiKey + ".json").toFile();
				JSONUtils.writeJSON2File(jso, outFile);
			}
		}
		if (!missing.isEmpty()) {
			
			Path logPath = Paths.get("E:\\_Downloads\\ClassyFireJSON\\NotFound_InChiKeys_FullList.txt");
			try {
				Files.write(logPath, 
						missing, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//	CompoundDatabaseScripts.calculateMetaSciFormulasAndChargesFromSmiles();
	
	private static void classyFireTest() {

		String url = "http://classyfire.wishartlab.com/entities/QNAYBMKLOCPYGJ-REOHCLBHSA-N.json";
		JSONObject jso = JSONUtils.readJsonFromUrl(url);
		System.out.println("***");
	}
	
	private static void uploadMultiplexMixtures() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		for(int i=1; i<23; i++) {
			CompoundMultiplexMixture newMixture = 
					new CompoundMultiplexMixture(null, "MetaSci-" +
							StringUtils.leftPad(Integer.toString(i), 2, "0"));
			CompoundMultiplexUtils.addCompoundMultiplexMixture(newMixture, conn);			
		}
		ConnectionManager.releaseConnection(conn);
	}

	
	private static void uploadMultiplexMixtureComponents() throws Exception {
		
		File mixtureFile = 
				new File("E:\\Development\\MRC2Toolbox\\Database\\"
						+ "MetaSci\\MetaSci_multiplexes4upload.txt");
		String[][] mixtureData = 
				DelimitedTextParser.parseTextFileWithEncoding(
						mixtureFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Collection<MultiplexLoadTempObject>mloList = 
				new ArrayList<MultiplexLoadTempObject>();
		Map<String,Collection<String>>processedComponentsSolventMap = 
				new TreeMap<String,Collection<String>>();
		for(int i=0; i<mixtureData.length; i++) {
			
			int mixNum = Integer.valueOf(mixtureData[i][0]);
			String inchiKey = mixtureData[i][1];
			String solventId = mixtureData[i][2];
			double conc = Double.valueOf(mixtureData[i][3]);
			Double xlogP = null;
			if(mixtureData[i][4] != null && !mixtureData[i][4].isEmpty() && !mixtureData[i][4].equals("--"))
				xlogP = Double.valueOf(mixtureData[i][4]);
			
			double volume = Double.valueOf(mixtureData[i][5]);
			MultiplexLoadTempObject mlo = new MultiplexLoadTempObject(			
					mixNum, 
					inchiKey, 
					solventId, 
					conc, 
					xlogP,
					volume);
			mloList.add(mlo);
		}
		Collection<CompoundMultiplexMixture>mixtureSet = 
				CompoundMultiplexUtils.getCompoundMultiplexMixtureList();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT CC_COMPONENT_ID FROM COMPOUND_COLLECTION_COMPONENT_METADATA "
				+ "WHERE (FIELD_ID = ? OR FIELD_ID = ?) AND FIELD_VALUE = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, "CCCMF00001");
		ps.setString(2, "CCCMF00091");				
		String insertQuery = 
				"INSERT INTO COMPOUND_MULTIPLEX_MIXTURE_COMPONENTS "
				+ "(MIX_ID, CC_COMPONENT_ID, CPD_CONC_MKM, SOLVENT_ID, XLOGP, ALIQUOTE_VOLUME) "
				+ "VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement insertPs = conn.prepareStatement(insertQuery);
		ResultSet rs = null;
		for(MultiplexLoadTempObject mlo : mloList) {
			
			ps.setString(3, mlo.getName());
			rs = ps.executeQuery();
			String componentId = null;
			while(rs.next()) {
				componentId = rs.getString("CC_COMPONENT_ID");
//				if(!processedComponentsSolventMap.containsKey(nextComponentId)) {
//					processedComponentsSolventMap.put(nextComponentId, new TreeSet<String>());
//					componentId = nextComponentId;
//				}
			}
			rs.close();
			if(componentId != null) {
				
				String mixName = "MetaSci-" +
						StringUtils.leftPad(Integer.toString(mlo.getMixNum()), 2, "0");
				CompoundMultiplexMixture mix = mixtureSet.stream().
						filter(m -> m.getName().equals(mixName)).findFirst().orElse(null);
				if(mix != null 
						//	&& !processedComponentsSolventMap.get(componentId).contains(mlo.getSolventId())
						) {					
					
					insertPs.setString(1, mix.getId());
					insertPs.setString(2, componentId);
					insertPs.setDouble(3, mlo.getConc());
					insertPs.setString(4, mlo.getSolventId());
					
					if(mlo.getXlogp() != null && !mlo.getXlogp().equals(Double.NaN))
						insertPs.setDouble(5, mlo.getXlogp());
					else
						insertPs.setNull(5, java.sql.Types.NULL);
					
					insertPs.setDouble(6, mlo.getVolume());
					insertPs.executeUpdate();
				}
				//processedComponentsSolventMap.get(componentId).add(mlo.getSolventId());
			}
			else {
				System.out.println(mlo.getName() + " not found");
			}
		}		
		ps.close();
		insertPs.close();
		ConnectionManager.releaseConnection(conn);	
		
		//QRMZSPFSDQBLIX-UHFFFAOYSA-N

		System.out.println("***");
	}	
	
	private static void uploadMultiplexSolvents() {
		
		String[] solventNames = new String[] {
				"0.1M NH4OH",
				"0.1N NaOH",
				"1:1 Water:Acetonitrile",
				"1:13 Water:DMSO",
				"1M Formic acid",
				"1M HCl",
				"1M NaOH",
				"1M NH4OH",
				"4M NH4OH in MeOH",
				"50:50 Water/EtOH",
				"80% EtOH",
				"85% EtOH",
				"87% EtOH",
				"89% EtOH",
				"91% EtOH",
				"Acetic acid",
				"Acetone",
				"Acetonitrile",
				"Chloroform",
				"DMF",
				"DMSO",
				"EtOH",
				"EtOH (90%)",
				"EtOH (MeOH pref)",
				"MeOH",
				"Water"
		};
		for(String sn : solventNames) {
			
			
			try {
				CompoundMultiplexUtils.addNewSolvent(new MobilePhase(null, sn));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void parseCoconutCrossref() throws Exception{

		Map<String,String>dbUrlMap = 
				new TreeMap<String,String>();
		Map<String,Map<String,String>>crossrefMap = 
				new TreeMap<String,Map<String,String>>();
		Path inputPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\_LATEST\\COCONUT-2022-01\\CSV\\uniqueNaturalProduct-xrefs.csv");
		List<String> lines = Files.readAllLines(inputPath);
		Pattern idPattern = Pattern.compile("^CNP\\d{7}");
		Matcher regexMatcher = null;
		for(int i=1; i<lines.size(); i++) {
			
			String line = lines.get(i);
			regexMatcher = idPattern.matcher(line);
			
			if(regexMatcher.find()){
				
				
				String id = regexMatcher.group();
				String[] data = line.replace(id+",", "").replaceAll("\"", "").trim().split("\\],\\[");
				if(data.length > 0) {
					
					crossrefMap.put(id, new TreeMap<String,String>());
					for(int j=0; j<data.length; j++) {
						String[] refData = data[j].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
						if(refData.length == 3) {
							dbUrlMap.put(refData[0], refData[2]);
							crossrefMap.get(id).put(refData[1], refData[0]);
						}						
					}
				}
			}
		}
//		List<String>dbUrlList = new ArrayList<String>();
//		dbUrlMap.entrySet().stream().forEach(e -> dbUrlList.add(e.getKey() + "\t" + e.getValue()));
//		Path outputPath = Paths.get("E:\\DataAnalysis\\Databases\\_LATEST\\COCONUT-2022-01\\CSV\\dbList.txt");
//		try {
//			Files.write(
//					outputPath, 
//					dbUrlList, 
//					StandardCharsets.UTF_8, 
//					StandardOpenOption.CREATE,
//					StandardOpenOption.APPEND);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		Connection conn = ConnectionManager.getConnection();
		String crossrefQuery = "INSERT INTO COMPOUNDDB.COCONUT_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		PreparedStatement crossrefPs = conn.prepareStatement(crossrefQuery);
		for(Entry<String, Map<String, String>> re : crossrefMap.entrySet()) {
			
			crossrefPs.setString(1, re.getKey());
			for(Entry<String, String> kv : re.getValue().entrySet()) {
				
				crossrefPs.setString(2, kv.getValue());
				crossrefPs.setString(3, kv.getKey());
				crossrefPs.addBatch();
			}
			crossrefPs.executeBatch();
		}		
		crossrefPs.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void getMissingNPAData() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT ACCESSION, INCHI_KEY FROM COMPOUNDDB.NPA_COMPOUND_DATA "
				+ "WHERE NAME = 'Not named' ORDER BY ACCESSION";	
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String updSql = 
				"INSERT INTO COMPOUNDDB.NPA_SYNONYMS "
				+ "(ACCESSION, NAME, NTYPE) VALUES(?,?,?)";	
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		String updSql2 = 
				"UPDATE COMPOUNDDB.NPA_COMPOUND_DATA SET NAME = ? WHERE ACCESSION = ?";	
		PreparedStatement updPs2 = conn.prepareStatement(updSql2);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			String accession = rs.getString("ACCESSION");
			PubChemCompoundDescriptionBundle ib = 
					PubChemUtils.getCompoundDescriptionByInchiKey(rs.getString("INCHI_KEY"));
			if(ib != null && ib.getTitle() != null && !ib.getTitle().isEmpty()) {
				
				updPs.setString(1, accession);
				updPs.setString(2, ib.getTitle());
				updPs.setString(3, "PRI");
				updPs.executeUpdate();
				
				updPs2.setString(2, accession);
				updPs2.setString(1, ib.getTitle());
				updPs2.executeUpdate();
				
				System.err.println(accession + " - " + ib.getTitle());
			}
			else {
				System.err.println(accession + " - not found");
			}
		}
		ps.close();
		updPs.close();
		updPs2.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void extractNatProdAtlasFields() {
		
		Collection<String>fields = new TreeSet<String>();
		File sdfFile = new File("E:\\DataAnalysis\\Databases\\_LATEST\\Natural Products Atlas-2022-09\\NPAtlas_download.sdf");
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				IAtomContainer molecule = (IAtomContainer)reader.next();
				molecule.getProperties().forEach((k,v)->fields.add(k.toString()));
			}
		}		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Path outputPath = Paths.get("E:\\DataAnalysis\\Databases\\_LATEST\\Natural Products Atlas-2022-09\\NPA_fields.txt");
		try {
			Files.write(
					outputPath, 
					fields, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void extractCoconutFields() {
		
		Collection<String>fields = new TreeSet<String>();
		File sdfFile = new File("E:\\DataAnalysis\\Databases\\_LATEST\\COCONUT-2022-01\\COCONUT_2022_01_2D.SDF");
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				IAtomContainer molecule = (IAtomContainer)reader.next();
				molecule.getProperties().forEach((k,v)->fields.add(k.toString()));
			}
		}		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Path outputPath = Paths.get("E:\\DataAnalysis\\Databases\\_LATEST\\COCONUT-2022-01\\coconut_fields.txt");
		try {
			Files.write(
					outputPath, 
					fields, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void splitDrugBankIntoIndividualEntries() throws Exception{
		
		File xmlInputFile = 
				new File("E:\\DataAnalysis\\Databases\\_LATEST"
						+ "\\DrugBank-5.1.10-2023-01-04\\full database.xml");
		String outDir = "E:\\DataAnalysis\\Databases\\_LATEST"
				+ "\\DrugBank-5.1.10-2023-01-04\\DRUGS";
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		
		XMLInputFactory xif = null;
		try {
			xif = XMLInputFactory.newInstance();
		} catch (FactoryConfigurationError e1) {

			e1.printStackTrace();
			return;
		}
		TransformerFactory tf = null;
		try {
			tf = TransformerFactory.newInstance();
		} catch (TransformerFactoryConfigurationError e2) {

			e2.printStackTrace();
			return;
		}		
		Transformer t = null;
		try {
			t = tf.newTransformer();
		} catch (TransformerConfigurationException e2) {
			
			e2.printStackTrace();
			return;
		}
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		DOMBuilder domBuider = new DOMBuilder();
		
		XMLStreamReader xsr = null;
		try {
			xsr = xif.createXMLStreamReader(new FileReader(xmlInputFile));
			xsr.nextTag();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (XMLStreamException e1) {

			e1.printStackTrace();
			return;
		}
        try {						
			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {

			    DOMResult result = new DOMResult();
			    t.transform(new StAXSource(xsr), result);
			    Node domNode = result.getNode();
			    if(domNode.getFirstChild().getNodeName().equals("drug")){
			    	
			    	String drugId = null;
				    org.jdom2.Element recordElement = 
				    		domBuider.build((org.w3c.dom.Element)domNode.getFirstChild());
				    Namespace ns = recordElement.getNamespace();
				    List<Element> idList = 
				    		recordElement.getChildren("drugbank-id", ns);
				    
				    for(Element idElement : idList) {
				    	
				    	String isPrimary = idElement.getAttributeValue("primary");
				    	if(isPrimary != null) {
				    		drugId = idElement.getText();
				    		File outputFile = Paths.get(outDir, drugId + ".xml").toFile();
				    		Document document = new Document();
				    		
				    		Element elemCopy = (Element)recordElement.clone();
				    		elemCopy.detach();				    		
				    		document.addContent(elemCopy);
				    		XmlUtils.writeXMLDocumentToFile(document, outputFile);
				    	}
				    }
			    }
			}
		}
        catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getPepSearchCommand() {
		
		File inputFile = new File(
				"E:\\Eclipse\\git2\\MRC2Toolbox\\data\\mssearch\\20200903-150116_NIST_MSMS_PEPSEARCH_RESULTS.TXT");
		String line = "";
		try (Stream<String> lines = Files.lines(Paths.get(inputFile.getAbsolutePath()))) {
			line = lines.skip(1).findFirst().get();
			System.out.println(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		NISTPepSearchUtils.parsePepSearchCommandLine(line);
		return line;
	}
	
	private static void normalizeFormula(String inputFormula) {
		
		IMolecularFormula formula = MolecularFormulaManipulator.
				getMolecularFormula(inputFormula, builder);
		
		System.out.println(MolecularFormulaManipulator.getString(formula));
	}
	
	private static void createLmAbbreviationsForLipidBlast() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT RLB_ID, NAME, ABBREVIATION FROM LIPIDBLAST_RIKEN_COMPONENTS WHERE LM_ABBREVIATION IS NULL";	
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String updSql = "UPDATE LIPIDBLAST_RIKEN_COMPONENTS SET LM_ABBREVIATION = ? WHERE RLB_ID = ?";	
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		Pattern fa = Pattern.compile("\\d+:\\d+");
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			String[] matches = fa.matcher(rs.getString("NAME"))
	                .results()
	                .map(MatchResult::group)
	                .toArray(String[]::new);
			if(matches.length > 0) {
				int carbons = 0;
				int doubleBonds = 0;
				for(String match : matches) {
					
					String[] code = match.split(":");
					if(code.length == 2) {
						carbons += Integer.valueOf(code[0]);
						doubleBonds += Integer.valueOf(code[1]);
					}
				}
				if(carbons > 0) {
					String lmAbbr = rs.getString("ABBREVIATION") + 
							"(" + Integer.toString(carbons) + ":" + Integer.toString(doubleBonds) + ")";
					updPs.setString(1, lmAbbr);
					updPs.setString(2, rs.getString("RLB_ID"));
					updPs.addBatch();
					count++;
				}				
			}
			if(count % 100 == 0) {
				updPs.executeBatch();
				System.out.print(".");
			}
			if(count % 5000 == 0) {
				updPs.executeBatch();
				System.out.println(".");
			}
		}	
		updPs.executeBatch();
		updPs.close();
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	//;
	
	private static void classifyLipidBlastByLipidMapsFingerprints() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String> subFingerprints = LipidOntologyUtils.getUniqueLipidMapsSubFingerprints(conn);		
		Map<String, Collection<LipidMapsClassifier>> fpMap = 
				LipidOntologyUtils.mapLipidMapsSubFingerprintsToClassifiers(subFingerprints, conn);
		Map<String, LipidMapsClassifier>bestClassifiers = LipidOntologyUtils.findBestLipidMapsClassifiers(fpMap);
			
		ConnectionManager.releaseConnection(conn);
		System.out.println("###");
	}
	
	private static void insertLipidMapsData() throws Exception {

		File sdfFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\LMSD_20191002.sdf");
		Connection conn = ConnectionManager.getConnection();
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				IAtomContainer molecule = (IAtomContainer)reader.next();
				LipidMapsParser.insertLipidMapsRecord(molecule, conn);
			
				count++;
				if((count % 50) == 0) 
					System.out.print(".");
				if((count % 2000) == 0) 
					System.out.println(".");				
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void extractLipidMapsFields() {
		
		Collection<String>fields = new TreeSet<String>();
		File sdfFile = new File("E:\\DataAnalysis\\Databases\\_LATEST\\LipidMaps-2023-03-03\\structures.sdf");
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				IAtomContainer molecule = (IAtomContainer)reader.next();
				molecule.getProperties().forEach((k,v)->fields.add(k.toString()));
			}
		}		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Path outputPath = Paths.get("E:\\DataAnalysis\\Databases\\_LATEST\\LipidMaps-2023-03-03\\fields.txt");
		try {
			Files.write(
					outputPath, 
					fields, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void extractLipidMapsClasses() {

		File sdfFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\LMSD_20191002.sdf");
	//	File sdfFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\lmtest.sdf");

		Map<LipidMapsFields, Collection<String>>classMap = new TreeMap<LipidMapsFields, Collection<String>>();
		
		classMap.put(LipidMapsFields.CATEGORY, new TreeSet<String>());
		classMap.put(LipidMapsFields.MAIN_CLASS, new TreeSet<String>());
		classMap.put(LipidMapsFields.SUB_CLASS, new TreeSet<String>());
		classMap.put(LipidMapsFields.CLASS_LEVEL4, new TreeSet<String>());
		
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				Map<String, String>lipidMapsDataMap = LipidMapsParser.getCompoundDataMap();
				IAtomContainer molecule = (IAtomContainer)reader.next();
				molecule.getProperties().forEach((k,v)->{

					if(lipidMapsDataMap.containsKey(k.toString()))
						lipidMapsDataMap.put(k.toString(), v.toString());
				});
				String category = lipidMapsDataMap.get(LipidMapsFields.CATEGORY.name());
				if(category != null)
					classMap.get(LipidMapsFields.CATEGORY).add(category);
				
				String mainClass = lipidMapsDataMap.get(LipidMapsFields.MAIN_CLASS.name());
				if(mainClass != null)
					classMap.get(LipidMapsFields.MAIN_CLASS).add(mainClass);
				
				String subClass = lipidMapsDataMap.get(LipidMapsFields.SUB_CLASS.name());
				if(subClass != null)
					classMap.get(LipidMapsFields.SUB_CLASS).add(subClass);
				
				String classLevel4 = lipidMapsDataMap.get(LipidMapsFields.CLASS_LEVEL4.name());
				if(classLevel4 != null)
					classMap.get(LipidMapsFields.CLASS_LEVEL4).add(classLevel4);
				
				count++;
				if((count % 50) == 0) 
					System.out.print(".");
				if((count % 2000) == 0) 
					System.out.println(".");				
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String>output = new ArrayList<String>();
		for(Entry<LipidMapsFields, Collection<String>> e : classMap.entrySet()) {
			
			for(String lmClass : e.getValue())				
				output.add(e.getKey().name() + "\t" + lmClass);			
		}
		File outFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\LMSD_20191002_ClASSES.TXT");
		try {
			FileUtils.writeLines(outFile, output, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static void exportLipidBlastForNist(Polarity pol, File exportFile) throws Exception {
		
		String libName = "LIPIDBLAST_RIKEN";
		String ionMode = pol.getCode();

		exportFile =
				FIOUtils.changeExtension(
					exportFile, MsLibraryFormat.MSP.getFileExtension());
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));

		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT C.MRC2_LIB_ID, C.PRECURSOR_MZ, C.ADDUCT,"
			+ "R.NAME, R.INCHI_KEY, R.FORMULA, R.COMMENTS, R.COMPOUNDCLASS "
			+ "FROM REF_MSMS_LIBRARY_COMPONENT C, "
			+ "LIPIDBLAST_RIKEN_COMPONENTS R "
			+ "WHERE C.ORIGINAL_LIBRARY_ID = R.RLB_ID "
			+ "AND  C.LIBRARY_NAME = ? AND C.POLARITY = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		String msQuery =
			"SELECT MZ, INTENSITY FROM REF_MSMS_LIBRARY_PEAK WHERE MRC2_LIB_ID = ? ORDER BY MZ";
		PreparedStatement msps = conn.prepareStatement(msQuery);
		
		ps.setString(1, libName);
		ps.setString(2, ionMode);
		ResultSet rs = ps.executeQuery();
		
		ResultSet msrs = null;
		StringBuffer sb  = null;
		int counter = 0;
		while(rs.next()) {

			writer.append(MSPField.NAME.getName() + ": " + rs.getString("MRC2_LIB_ID") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("COMPOUNDCLASS") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("NAME") + "\n");
			writer.append(MSPField.FORMULA.getName() + ": " + rs.getString("FORMULA") + "\n");
			writer.append(MSPField.ION_MODE.getName() + ": " + ionMode + "\n");

			if(rs.getString("ADDUCT") != null)
				writer.append(MSPField.PRECURSOR_TYPE.getName() + ": " + rs.getString("ADDUCT") + "\n");

			if(rs.getString("PRECURSOR_MZ") != null)
				writer.append(MSPField.PRECURSORMZ.getName() + ": " + rs.getString("PRECURSOR_MZ") + "\n");
			
			writer.append(MSPField.INCHI_KEY.getName() + ": " + rs.getString("INCHI_KEY") + "\n");
			writer.append(MSPField.COMMENTS.getName() + ": " + rs.getString("COMMENTS") + "\n");
			
			msps.setString(1, rs.getString("MRC2_LIB_ID"));
			msrs = msps.executeQuery();
			int pointCount = 0;
			sb = new StringBuffer();
			while(msrs.next()) {

				double mz = msrs.getDouble("MZ");
				if(mz > 10.0d) {
					
					sb.append(
							MRC2ToolBoxConfiguration.getMzFormat().format(msrs.getDouble("MZ"))
							+ " " + intensityFormat.format(msrs.getDouble("INTENSITY")) +"\n") ;
						pointCount++;
				}
			}
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(pointCount) + "\n");
			writer.append(sb.toString());
			writer.append("\n\n");
			msrs.close();
			counter++;
			
			if(counter % 50 == 0)
				System.out.print(".");
			if(counter % 2000 == 0)
				System.out.println(".");
		}
		rs.close();
		ps.close();
		msps.close();
		ConnectionManager.releaseConnection(conn);

		writer.flush();
		writer.close();
	}
	

}
