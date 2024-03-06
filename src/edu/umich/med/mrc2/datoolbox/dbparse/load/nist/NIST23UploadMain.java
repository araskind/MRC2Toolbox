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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import edu.umich.med.mrc2.datoolbox.data.MinimalNISTTandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.misctest.IteratingSDFReaderFixed;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.PubChemUtils;
import io.github.dan2097.jnainchi.InchiStatus;

public class NIST23UploadMain {
	
	private static Map<File,File>msp2sdfMap;
	private static final SmilesGenerator smilesGenerator = 
			new SmilesGenerator(SmiFlavor.Isomeric);
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			compateHashesAndAssignMRC2libIdsToNIST23entries();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private static void compateHashesAndAssignMRC2libIdsToNIST23entries() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		
		String query = "SELECT NIST_ID, MZ_HASH "
				+ "FROM COMPOUNDDB.NIST_LIBRARY_COMPONENT";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "SELECT MRC2_LIB_ID, MZ_HASH "
				+ "FROM REF_MSMS_LIBRARY_COMPONENT "
				+ "WHERE ORIGINAL_LIBRARY_ID = ? "
				+ "AND LIBRARY_NAME IN('hr_msms_nist','nist_msms','nist_msms2')";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		String query3 = "UPDATE COMPOUNDDB.NIST_LIBRARY_COMPONENT "
				+ "SET MRC2_LIB_ID = ? WHERE NIST_ID = ?";
		PreparedStatement ps3 = conn.prepareStatement(query3);
			
		ArrayList<String>hashMismatches = new ArrayList<String>();

		ResultSet rs = ps.executeQuery();
		ResultSet rs2 = null;
		int count = 0;
		while(rs.next()) {
			
			int nistId = rs.getInt(1);
			String newHash = rs.getString(2);
			ps2.setString(1, Integer.toString(nistId));
			rs2 = ps2.executeQuery();
			while(rs2.next()) {
				
				String mrcId = rs2.getString(1);
				String oldHash = rs2.getString(2);
				if(oldHash.equals(newHash)) {
					
					ps3.setString(1, mrcId);
					ps3.setInt(2, nistId);
					ps3.executeUpdate();
				}
				else {
					hashMismatches.add("MZ Hash mismatch for NIST ID " + nistId + " MRC2_LIB_ID " + mrcId);
				}
			}
			rs2.close();
			
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}
		rs.close();
		ps.close();
		ps2.close();
		ps3.close();
		ConnectionManager.releaseConnection(conn);
		
		if(!hashMismatches.isEmpty()) {
			
			File mismatchLog = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\SpecrumHashMismatches.txt");
			try {
				Files.write(mismatchLog.toPath(), 
						hashMismatches, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void updateNISTCompoundNamesWithGreekLetters() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT NIST_ID, NAME FROM COMPOUNDDB.NIST_COMPOUND_DATA";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.NIST_COMPOUND_DATA "
				+ "SET NAME = ? WHERE NIST_ID = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			String oldName = rs.getString(2);
			String newName = NISTParserUtils.restoreGreekLetters(oldName);
			if(!newName.equals(oldName)) {
				
				ps2.setString(1, newName);
				ps2.setInt(2, rs.getInt(1));
				ps2.executeUpdate();
			}			
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();
		ps2.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateNISTSynonymsNamesWithGreekLetters() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT NIST_ID, CPD_NAME, STYPE "
				+ "FROM COMPOUNDDB.NIST_SYNONYM";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.NIST_SYNONYM "
				+ "SET STYPE = ? WHERE NIST_ID = ? AND CPD_NAME = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		String query3 = "INSERT INTO COMPOUNDDB.NIST_SYNONYM "
				+ "(NIST_ID, CPD_NAME, STYPE) VALUES(?, ?, ?)";
		PreparedStatement ps3 = conn.prepareStatement(query3);
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			String oldName = rs.getString(2);
			String newName = NISTParserUtils.restoreGreekLetters(oldName);			
			if(!newName.equals(oldName)) {
				
				int nistId = rs.getInt(1);
				String sType = rs.getString(3);
				
				if(sType.equals("PRI")) {
					
					ps2.setString(1, "SYN");
					ps2.setInt(2, nistId);
					ps2.setString(3, oldName);
					ps2.executeUpdate();
										
					ps3.setInt(1, nistId);
					ps3.setString(2, newName);
					ps3.setString(3, "PRI");										
					ps3.executeUpdate();
				}
				else {
					ps3.setInt(1, nistId);
					ps3.setString(2, newName);
					ps3.setString(3, sType);										
					ps3.executeUpdate();
				}
			}			
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();

		ps3.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateNISTComponentNamesWithGreekLetters() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT NIST_ID, NAME FROM COMPOUNDDB.NIST_LIBRARY_COMPONENT";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "UPDATE COMPOUNDDB.NIST_LIBRARY_COMPONENT "
				+ "SET NAME = ? WHERE NIST_ID = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			String oldName = rs.getString(2);
			String newName = NISTParserUtils.restoreGreekLetters(oldName);
			if(!newName.equals(oldName)) {
				
				ps2.setString(1, newName);
				ps2.setInt(2, rs.getInt(1));
				ps2.executeUpdate();
			}			
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();
		ps2.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateCurrentSpectralLibs() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "SELECT MZ, INTENSITY, IS_PARENT "
				+ "FROM REF_MSMS_LIBRARY_PEAK WHERE MRC2_LIB_ID = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		String query3 = "UPDATE REF_MSMS_LIBRARY_COMPONENT "
				+ "SET SPECTRUM_HASH = ?, ENTROPY = ? WHERE MRC2_LIB_ID = ?";
		PreparedStatement ps3 = conn.prepareStatement(query3);
		
		String query4 = "UPDATE REF_MSMS_LIBRARY_PEAK SET INTENSITY = ? "
				+ "WHERE MRC2_LIB_ID = ? AND IS_PARENT IS NOT NULL";
		PreparedStatement ps4 = conn.prepareStatement(query4);
		
		ResultSet rs = ps.executeQuery();
		ResultSet rs2 = null;
		int count = 0;
		while(rs.next()) {
			
			 String mrc2id = rs.getString(1);
			 ps2.setString(1, mrc2id);
			 rs2 = ps2.executeQuery();
			 MsPoint parent = null;
			 Collection<MsPoint>points = new ArrayList<MsPoint>();
			 while(rs2.next()) {
				 
				 if(rs2.getString(3) != null)
					 parent = new MsPoint(rs2.getDouble(1), rs2.getDouble(2));
				 else
					 points.add(new MsPoint(rs2.getDouble(1), rs2.getDouble(2)));			 
			 }
			 rs2.close();
			 
			 //	Insert hash and entropy
			 MinimalNISTTandemMassSpectrum msms = 
					 new MinimalNISTTandemMassSpectrum(mrc2id, parent, points);
			 ps3.setString(1, msms.getSpectrumHash());
			 ps3.setDouble(2, msms.getEntropy());
			 ps3.setString(3, mrc2id);
			 ps3.executeUpdate();
			 
			 //	Correct parent
			 MsPoint parentFromSpectrum = msms.getParentFromSpectrum(10.0d);
			 double minIntensity = msms.getMinimalIntensity() / 2.0d;
			 if(parentFromSpectrum != null) 
				 minIntensity = parentFromSpectrum.getIntensity();
				 
			 ps4.setDouble(1, minIntensity);
			 ps4.setString(2, mrc2id);
			 ps4.executeUpdate();
			 
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();
		ps2.close();
		ps3.close();		
		ps4.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	
	private static void updateNISTSpectra() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT NIST_ID FROM COMPOUNDDB.NIST_LIBRARY_COMPONENT";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "SELECT MZ, INTENSITY, IS_PARENT "
				+ "FROM COMPOUNDDB.NIST_LIBRARY_PEAK WHERE NIST_ID = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		String query3 = "UPDATE COMPOUNDDB.NIST_LIBRARY_COMPONENT "
				+ "SET SPECTRUM_HASH = ?, ENTROPY = ? WHERE NIST_ID = ?";
		PreparedStatement ps3 = conn.prepareStatement(query3);
		
		String query4 = "UPDATE COMPOUNDDB.NIST_LIBRARY_PEAK SET INTENSITY = ? "
				+ "WHERE NIST_ID = ? AND IS_PARENT IS NOT NULL";
		PreparedStatement ps4 = conn.prepareStatement(query4);
		
		ResultSet rs = ps.executeQuery();
		ResultSet rs2 = null;
		int count = 0;
		while(rs.next()) {
			
			 int nistId = rs.getInt(1);
			 ps2.setInt(1, nistId);
			 rs2 = ps2.executeQuery();
			 MsPoint parent = null;
			 Collection<MsPoint>points = new ArrayList<MsPoint>();
			 while(rs2.next()) {
				 
				 if(rs2.getString(3) != null)
					 parent = new MsPoint(rs2.getDouble(1), rs2.getDouble(2));
				 else
					 points.add(new MsPoint(rs2.getDouble(1), rs2.getDouble(2)));			 
			 }
			 rs2.close();
			 
			 //	Insert hash and entropy
			 MinimalNISTTandemMassSpectrum msms = 
					 new MinimalNISTTandemMassSpectrum( nistId, parent, points);
			 ps3.setString(1, msms.getSpectrumHash());
			 ps3.setDouble(2, msms.getEntropy());
			 ps3.setInt(3, nistId);
			 ps3.executeUpdate();
			 
			 //	Correct parent
			 MsPoint parentFromSpectrum = msms.getParentFromSpectrum(10.0d);
			 double minIntensity = msms.getMinimalIntensity() / 2.0d;
			 if(parentFromSpectrum != null) 
				 minIntensity = parentFromSpectrum.getIntensity();
				 
			 ps4.setDouble(1, minIntensity);
			 ps4.setInt(2, nistId);
			 ps4.executeUpdate();
			 
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();
		ps2.close();
		ps3.close();		
		ps4.close();
		ConnectionManager.releaseConnection(conn);		
	}
		
	private static void clearDataForMSPFile(){
		
		File mspFile = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\MSP\\P1-1015001-1020000.MSP");
		List<NISTTandemMassSpectrum>msmsList = NISTParserUtils.parseNISTmspFile(mspFile);
		
		List<Integer> idList = msmsList.stream().map(f -> f.getNistNum()).collect(Collectors.toList());
		try {
			NISTParserUtils.clearDataForNISTIDs(idList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void uploadDataForMismatchedMSPtoSDFfiles() throws Exception{
		
		File mspDirectory = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\MSP");
		List<File>mspFiles = Files.find(mspDirectory.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		File sdfDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\SDF_PUBCHEM");
		List<File>sdfFiles = Files.find(sdfDir.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".sdf") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		
		List<String>molErrorLog = new ArrayList<String>();
		File logDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED");
		
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(File mspFile : mspFiles) {
			
			System.out.println("Processing " + mspFile.getName());
			List<NISTTandemMassSpectrum>msmsList = NISTParserUtils.parseNISTmspFile(mspFile);
			
			
			Map<NISTTandemMassSpectrum,IAtomContainer>msmsMolMap = 
					new HashMap<NISTTandemMassSpectrum,IAtomContainer>();
			for(NISTTandemMassSpectrum msms : msmsList) {
				
				String inchiKey = msms.getProperty(MSPField.INCHI_KEY);
				if(inchiKey == null || inchiKey.isEmpty()) {
					msmsMolMap.put(msms, null);
				}
				else {
					File sdfFile = sdfFiles.stream().
							filter(f -> f.getName().startsWith(inchiKey)).
							findFirst().orElse(null);
					if(sdfFile == null) {
						msmsMolMap.put(msms, null);
					}
					else {
						IteratingSDFReaderFixed reader = 
								new IteratingSDFReaderFixed(new FileInputStream(sdfFile), 
										DefaultChemObjectBuilder.getInstance());
						while (reader.hasNext()) {

							IAtomContainer molecule = new AtomContainer();
							try {
								molecule = (IAtomContainer)reader.next();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(molecule != null){

								String smiles = null;
								try {
									smiles = smilesGenerator.create(molecule);
								} catch (NullPointerException | CDKException e) {
									molErrorLog.add("Unable to generate SMILES for " + molecule.getProperty(CDKConstants.TITLE));
									molErrorLog.add(e.getMessage());
									e.printStackTrace();
								}
								if(smiles != null) {
									molecule.setProperty(CompoundIdentityField.SMILES.name(), smiles);
									
									inChIGenerator = igfactory.getInChIGenerator(molecule);
									InchiStatus ret = inChIGenerator.getStatus();
									if (ret == InchiStatus.SUCCESS || ret == InchiStatus.WARNING)
										molecule.setProperty(CompoundIdentityField.INCHIKEY.name(), inChIGenerator.getInchiKey());
									else
										molErrorLog.add("Unable to generate InChi for " + molecule.getProperty(CDKConstants.TITLE));

									StringWriter writer = new StringWriter();
									SDFWriter sdfWriter = new SDFWriter(writer);
							        sdfWriter.write(molecule);
							        sdfWriter.close();
							        molecule.setProperty(CompoundIdentityField.MOL_TEXT.name(), writer.toString());
							        msmsMolMap.put(msms, molecule);
								}
								else {
									msmsMolMap.put(msms, null);
								}
							}
							else {
								msmsMolMap.put(msms, null);
							}
							break;
						}
					}
				}
			}
			if(!molErrorLog.isEmpty() && logDir != null) {
				
				try {
					Files.write(
							Paths.get(logDir.getAbsolutePath(), 
									FilenameUtils.getBaseName(mspFile.getName()) + ".log"), 
							molErrorLog,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			uploadNistData(msmsMolMap);
		}
	}
	
	private static void parseAndUploadNIST23noSDF() throws Exception{
		
		File mspDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\MSP");
		File mismatchedLogDir = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED");
		List<File>mspFiles = Files.find(mspDirectory.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		for(File mspFile : mspFiles) {
			
			System.out.println("Processing " + mspFile.getName());

			Map<NISTTandemMassSpectrum,IAtomContainer>dataForUpload = 
					NISTParserUtils.createMsmsMolMapWithPubChemLookupOnly(mspFile, mismatchedLogDir);
			//	uploadNistData(dataForUpload);
		}		
	}

	private static void parseAndUploadNIST23() throws Exception{
		
		File mspDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_BAD_MOL\\MSP");
		File sdfDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_BAD_MOL\\SDF_NORM");		
		File badMolLogDir = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_BAD_MOL");
		
		Collection<File> mspFiles = 
				FileUtils.listFiles(mspDirectory, new String[] {"msp", "MSP"}, false);
		Collection<File> sdfFiles = 
				FileUtils.listFiles(sdfDirectory, new String[] {"sdf", "SDF"}, false);
				
		msp2sdfMap = new TreeMap<File,File>();
		for(File mspFile : mspFiles) {
			
			if(mspFile.isDirectory())
				continue;
			
			String fName = FilenameUtils.getBaseName(mspFile.getName());			
			File sdfFile = sdfFiles.stream().
					filter(f -> FilenameUtils.getBaseName(f.getName()).equals(fName)).
					findFirst().orElse(null);
			if(sdfFile != null) {
				msp2sdfMap.put(mspFile, sdfFile);
			}
			else {
				System.out.println("Missing SDF for " + mspFile.getName());				
			}	
		}
		for(Entry<File, File> ff : msp2sdfMap.entrySet()) {
			
			System.out.println("Processing " + ff.getKey().getName());
			Map<NISTTandemMassSpectrum,IAtomContainer>dataForUpload = 
					NISTParserUtils.createMsmsMolMapWithPubChemLookup(
							ff.getKey(), ff.getValue(), badMolLogDir);
			uploadNistData(dataForUpload);
		}
	}
	
	private static void uploadNistData(
			Map<NISTTandemMassSpectrum,IAtomContainer>dataForUpload) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		int count = 0;
	 	for(Entry<NISTTandemMassSpectrum,IAtomContainer>e : dataForUpload.entrySet()){
	 		
	 		NISTMSPParser.insertSpectrumRecord(e.getKey(), e.getValue(), conn);
	 		
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println(".");
	 	}
		ConnectionManager.releaseConnection(conn);
		System.out.println("MSMS upload for completed");
	}
	

	private static void getSdfForMismatchedByINCHIKEY() throws Exception{
		
		File mspDirectory = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\MSP");
		File sdfDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\SDF_PUBCHEM");
		File logDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED");
		
		List<File>mspFiles = Files.find(mspDirectory.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		List<File>sdfFiles = Files.find(sdfDir.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		
		TreeSet<String>inchiKeys = new TreeSet<String>();
		TreeSet<String>noInchiKeysInPubChem = new TreeSet<String>();
		TreeSet<String>noInchi = new TreeSet<String>();
		for(File mspFile : mspFiles) {
			
			System.out.println("Processing " + mspFile.getName());
			List<NISTTandemMassSpectrum>msmsList = NISTParserUtils.parseNISTmspFile(mspFile);
			for(NISTTandemMassSpectrum msms : msmsList) {
				
				String inchiKey = msms.getProperty(MSPField.INCHI_KEY);
				if(inchiKey == null || inchiKey.isEmpty()) {
					
					noInchi.add("No InChiKey for NIST ID " 			
							+ msms.getNistNum() 
							+ " " + msms.getProperty(MSPField.NAME));
					continue;
				}
				else {
					inchiKeys.add(inchiKey);
				}
			}			
		}
		for(String ik : inchiKeys) {
			
			Thread.sleep(300);
			
			File sdfFile = Paths.get(sdfDir.getAbsolutePath(), ik + ".SDF").toFile();
			File existing = sdfFiles.stream().
					filter(f -> f.getName().equals(sdfFile.getName())).
					findFirst().orElse(null);
			
			if(existing == null) {
				
				boolean sdfSaved = PubChemUtils.saveMoleculeFromPubChemByInChiKeyToSDFFile(ik, sdfFile);
				if(!sdfSaved)
					noInchiKeysInPubChem.add(ik);
			}
		}
		if(!noInchi.isEmpty()) {
			  try {
					Files.write(
							Paths.get(logDir.getAbsolutePath(), "missingInchiKeys.txt"), 
							noInchi,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		if(!noInchiKeysInPubChem.isEmpty()) {
			  try {
					Files.write(
							Paths.get(logDir.getAbsolutePath(), "notInPubChemInchiKeys.txt"), 
							noInchiKeysInPubChem,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private static void getSDFbyInChiKey() throws IOException {
		
		File logDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED");
		File sdfDir = 
				new File("E:\\DataAnalysis\\Databases\\NIST\\NIST23\\ExportUpload\\_MISMATCHED\\SDF_PUBCHEM");
		List<File>sdfFiles = Files.find(sdfDir.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		
		String[]keys = new String[] {
				"CFHPTZFRFWGDPD-UHFFFAOYSA-N",
				"CVTLXFXYWADARJ-OZRJHGEASA-N",
				"DDTIIBBQDYQDRC-NFTDJXRFSA-N",
				"FNNOEKVUXXVPAI-DUVOQLPESA-N",
				"FTLSPWXEYPAXNK-XCDQGSAISA-N",
				"GOMGANBHXVPZLC-WZXLXTKGSA-N",
				"HPXOVUNHWGMZOP-AYNQCERRSA-N",
				"ICUFDQIAXVTJSW-MHNYLRBJSA-N",
				"ILLLCSAUGQAXSM-CNYBTUBUSA-N",
				"JYWRCSAWBUAIFO-KRRNMHNDSA-N",
				"KMEHAZABHUEHFL-RXZRQKMISA-N",
				"LAUQPEVOYBPTPF-OWMKZRBUSA-N",
				"LISLGICORSJAKO-UHFFFAOYSA-N",
				"LTDDHUQIMJCFPX-UHFFFAOYSA-N",
				"NSFSLUUZQIAOOX-QEWKCGBTSA-N",
				"NYOTWECLMHGIDA-JDCHHWNGSA-N",
				"PKBPXZGKOKUHAX-NFTDJXRFSA-N",
				"SMJJSMCZQVHHTN-UHFFFAOYSA-N",
				"TUOUDHUYLGFLLN-CVLVPUKFSA-N",
				"VWZLXOVPPNZMTL-QZSHFQNPSA-N",
				"WYRJYQDEHNYEDO-OLWNVYNHSA-N",
				"YAWTZTDMXHYGQM-UHFFFAOYSA-O",
				"YGFDMXRTEQHAIV-OKGGTHDMSA-N",
		};
		TreeSet<String>noInchiKeysInPubChem = new TreeSet<String>();
		
		for(String key : keys) {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			File sdfFile = Paths.get(sdfDir.getAbsolutePath(), key + ".SDF").toFile();
			File existing = sdfFiles.stream().
					filter(f -> f.getName().equals(sdfFile.getName())).
					findFirst().orElse(null);
			
			if(existing == null) {
				
				boolean sdfSaved = PubChemUtils.saveMoleculeFromPubChemByInChiKeyToSDFFile(key, sdfFile);
				if(!sdfSaved)
					noInchiKeysInPubChem.add(key);
			}
		}
		if(!noInchiKeysInPubChem.isEmpty()) {
			  try {
					Files.write(
							Paths.get(logDir.getAbsolutePath(), "notInPubChemInchiKeys2.txt"), 
							noInchiKeysInPubChem,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private static void calculateMassOnlyHashes() throws Exception {
		calculateCurrentMassOnlyHashes();
		calculateNIST23MassOnlyHashes();
	}
	
	private static void calculateCurrentMassOnlyHashes() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "SELECT MZ FROM REF_MSMS_LIBRARY_PEAK "
				+ "WHERE MRC2_LIB_ID = ? AND IS_PARENT IS NULL";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		String query3 = "UPDATE REF_MSMS_LIBRARY_COMPONENT "
				+ "SET MZ_HASH = ? WHERE MRC2_LIB_ID = ?";
		PreparedStatement ps3 = conn.prepareStatement(query3);
		
		ResultSet rs = ps.executeQuery();
		ResultSet rs2 = null;
		int count = 0;
		TreeSet<Double>mzValues = new TreeSet<Double>();
		while(rs.next()) {
			
			 String mrc2id = rs.getString(1);
			 ps2.setString(1, mrc2id);
			 rs2 = ps2.executeQuery();
			 mzValues.clear();
			 while(rs2.next()) 
				 mzValues.add(rs2.getDouble(1));			 
			 
			 rs2.close();

			 String mzHash = MsUtils.calculateMzHash(mzValues);
			 ps3.setString(1, mzHash);
			 ps3.setString(2, mrc2id);
			 ps3.executeUpdate();		
			 
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();
		ps2.close();
		ps3.close();		
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void calculateNIST23MassOnlyHashes() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();

		String query = "SELECT NIST_ID FROM COMPOUNDDB.NIST_LIBRARY_COMPONENT";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String query2 = "SELECT MZ FROM COMPOUNDDB.NIST_LIBRARY_PEAK "
				+ "WHERE NIST_ID = ? AND IS_PARENT IS NULL";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		
		String query3 = "UPDATE COMPOUNDDB.NIST_LIBRARY_COMPONENT "
				+ "SET MZ_HASH = ? WHERE NIST_ID = ?";
		PreparedStatement ps3 = conn.prepareStatement(query3);
		
		ResultSet rs = ps.executeQuery();
		ResultSet rs2 = null;
		int count = 0;
		TreeSet<Double>mzValues = new TreeSet<Double>();
		while(rs.next()) {
			
			 int nistId = rs.getInt(1);
			 ps2.setInt(1, nistId);
			 rs2 = ps2.executeQuery();
			 mzValues.clear();
			 while(rs2.next()) 
				 mzValues.add(rs2.getDouble(1));			 
			 
			 rs2.close();

			 String mzHash = MsUtils.calculateMzHash(mzValues);
			 ps3.setString(1, mzHash);
			 ps3.setInt(2, nistId);
			 ps3.executeUpdate();		
			 
			count++;
			if(count % 100 == 0)
				System.out.print(".");
			
			if(count % 10000 == 0)
				System.out.println("\n" + count + " records processed");
		}	
		rs.close();
		ps.close();
		ps2.close();
		ps3.close();		
		ConnectionManager.releaseConnection(conn);		
	}
}


















