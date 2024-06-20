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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTParserUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.AgilentAcquisitionMethodParser;

public class RunContainer2 {

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
			//	extractSolventsFromAgilentMethods();
			Collection<MobilePhase> mpList = IDTDataCache.getMobilePhaseList();
			System.out.println("***");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testAgilentMethodParser() {
		
		File methodFolder = 
				new File ("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\"
						+ "AS_OF_20240618\\MSMS\\2.1 mm Gradient MS2 240 min.m");
		AgilentAcquisitionMethodParser amp = 
				new AgilentAcquisitionMethodParser(methodFolder);
		amp.parseParameterFiles();
		ChromatographicGradient grad = null;		
		try {
			grad = amp.extractGradientData();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void extractSolventsFromAgilentMethods() {
		
		File dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MSMS");
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));
		Collection<File> methodFolders = FileUtils.listFilesAndDirs(
				dirToScan,
				DirectoryFileFilter.DIRECTORY,
				dotMfilter);
		Set<String>solventSet  = new TreeSet<String>();
		Set<String>problemMethodSet  = new TreeSet<String>();
		int solvCount = 0;
		for(File methodFolder : methodFolders) {
			
			AgilentAcquisitionMethodParser amp = 
					new AgilentAcquisitionMethodParser(methodFolder);
			amp.parseParameterFiles();
			ChromatographicGradient grad = null;		
			try {
				grad = amp.extractGradientData();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(grad != null) {
				
				solvCount = 0;
				for(MobilePhase mp : grad.getMobilePhases()) {
					
					if(mp != null) {
						solventSet.add(mp.getName());
						solvCount++;
					}
				}
				if(solvCount == 0)
					problemMethodSet.add(methodFolder.getName());
				else
					System.out.println("Added solvents from " + methodFolder.getName());
			}
		}
		Path outputPath = 
				Paths.get(dirToScan.getParentFile().getAbsolutePath(), 
						"MSMSSolvents.txt");
		try {
			Files.write(outputPath, 
					solventSet, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Path errorPath = 
				Paths.get(dirToScan.getParentFile().getAbsolutePath(), 
						"MSMSMethodsWithoutSolvents.txt");
		try {
			Files.write(errorPath, 
					problemMethodSet, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void getMSMSClusteringParameterSets() throws Exception{
		
		Collection<MSMSClusteringParameterSet>existingSets = 
				MSMSClusteringDBUtils.getMSMSClusteringParameterSets();
		
		for(MSMSClusteringParameterSet params : existingSets) {
			System.out.println(params.getName());
		}
	}
	
	private static void copyClusteringParamsToXMLTable() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		
		Collection<MSMSClusteringParameterSet>existingSets = 
				MSMSClusteringDBUtils.getMSMSClusteringParameterSetsOld(conn);
		
		String query = 
				"INSERT INTO MSMS_CLUSTERING_PARAMETERS_XML ( " +
				"PAR_SET_ID, PAR_SET_NAME, PAR_SET_XML, PAR_SET_MD5)  " +
				"VALUES(?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(MSMSClusteringParameterSet params : existingSets) {
		
			if(params.getMd5() == null) {
				String md5 = MSMSClusteringUtils.calculateClusteringParametersMd5(params);
				params.setMd5(md5);
			}
			String paramsXml = MSMSClusteringDBUtils.getXMLStringForMSMSClusteringParameterSet(params);
			if(paramsXml == null) {
				throw new InvalidArgumentException(
						"Unable to create XML string for MSMS clustering parameters object");
			}				
			ps.setString(1, params.getId());
			ps.setString(2,params.getName());		
			ps.setString(3, paramsXml);
			ps.setString(4, params.getMd5());
			ps.executeUpdate();			
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void extractNISTFields() {

		File mspDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\MSP");
		File fieldList = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\NIST23_MSP_FIELD_LIST.TXT");
		File countsList = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\NIST23_MSP_ENTRY_COUNTS.TXT");
		
		NISTParserUtils.countEntriesAndEnumerateNistDataFields(mspDir, fieldList, countsList);
	}

	private static void calculateDistinctIsotopicPatterns() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String>formulas = new ArrayList<String>();
		String query = "SELECT MOL_FORMULA, FSIZE FROM FORMULA_ISOTOPIC_PATTERNS ORDER BY 2";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			formulas.add(rs.getString(1));
		}
		rs.close();
		
		query = "UPDATE FORMULA_ISOTOPIC_PATTERNS "
				+ "SET MONOISOTOPE = ?, ISOTOPE_2 = ?,  ISOTOPE_3 = ?, "
				+ "INTENSITY_2 = ?,  INTENSITY_3 = ? "
				+ "WHERE MOL_FORMULA = ?";			
		
		ps = conn.prepareStatement(query);
		for(String formulaString : formulas) {
			
			IMolecularFormula queryFormula = null;
			try {
				queryFormula = 
						MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
			} catch (Exception e) {
				//	e.printStackTrace();
				System.out.println("Invalid formula: " + formulaString);
			}
			if (queryFormula != null) {

				queryFormula.setCharge(0);
				Collection<MsPoint> msPoints = null;
				try {
					msPoints = MsUtils.calculateIsotopeDistribution(queryFormula, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Can't generate isotopes for: " + formulaString);
				}
				if(msPoints != null) {
					
					MsPoint[] msa = msPoints.toArray(new MsPoint[msPoints.size()]);
					
					ps.setDouble(1, msa[0].getMz());
					
					if(msa.length> 1) {
						ps.setDouble(2, msa[1].getMz());
						ps.setDouble(4, msa[1].getIntensity());
					}
					else {
						ps.setNull(2, java.sql.Types.NULL);
						ps.setNull(4, java.sql.Types.NULL);
					}
									
					if(msa.length> 2) {
						ps.setDouble(3, msa[2].getMz());
						ps.setDouble(5, msa[2].getIntensity());
					}
					else {
						ps.setNull(3, java.sql.Types.NULL);
						ps.setNull(5, java.sql.Types.NULL);
					}				
					ps.setString(6,  formulaString);
					
					ps.executeUpdate();
				}
			}
		}
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
}
