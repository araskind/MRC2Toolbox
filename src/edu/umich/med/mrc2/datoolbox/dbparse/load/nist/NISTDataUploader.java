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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.utils.MsImportUtils;
import io.github.dan2097.jnainchi.InchiStatus;
import net.sf.jniinchi.INCHI_RET;

public class NISTDataUploader {
	
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;

	private String mspDirectoryPath;
	private String sdfDirectoryPath;
	private NISTReferenceLibraries stdLibrary;
	private Map<File,File>msp2sdfMap;
	private List<File> mspFiles, sdfFiles;
	
	public NISTDataUploader(
			String mspDirectoryPath, 
			String sdfDirectoryPath,
			NISTReferenceLibraries stdLibrary) {
		super();
		this.mspDirectoryPath = mspDirectoryPath;
		this.sdfDirectoryPath = sdfDirectoryPath;
		this.stdLibrary = stdLibrary;		
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void normalizeSdfData(File normSdfFolder) throws IOException {
		
		List<Path> pathList = Files.find(Paths.get(sdfDirectoryPath), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".sdf") 
					&& fileAttr.isRegularFile()).collect(Collectors.toList());
		
		for(Path sdf : pathList) {
			
			//"utf-8" 
			//CharsetDecoder cs
			
			System.out.println("Processing " + sdf.toString());
			List<String> sdfText = readAllLines(sdf);
			List<String> newSdfText = new ArrayList<String>();
			List<String> chunk = new ArrayList<String>();
			for(int i=0; i<sdfText.size(); i++) {
				
				if(sdfText.get(i).trim().equals("$$$$")) {
					
					if(sdfText.get(i-1).trim().equals("M  END")) {
						chunk.add("$$$$");
					}
					else {
						chunk.add("M  END");
						chunk.add("$$$$");
					}
					newSdfText.addAll(chunk);
					chunk = new ArrayList<String>();
				}
				else {
					chunk.add(sdfText.get(i));
				}
			}
		    Path path = Paths.get(normSdfFolder.getAbsolutePath(), sdf.toFile().getName());		 
		    Files.write(path, newSdfText);
		}
	}
	
    public static List<String> readAllLines(Path path) {
    	
    	List<String> result = new ArrayList<>();
        CharsetDecoder decoder = StandardCharsets.US_ASCII.newDecoder().
        		onMalformedInput(CodingErrorAction.REPLACE).
        		onUnmappableCharacter(CodingErrorAction.REPLACE);
        try {
			Reader isreader = new InputStreamReader(new FileInputStream(path.toString()), decoder);
			BufferedReader reader = new BufferedReader(isreader);       
			for (;;) {
			    String line = reader.readLine();
			    if (line == null)
			        break;
			    result.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return result;       
    }
    
	
	public void mapDataFiles() {
		
		mspFiles = null;
		try {
			mspFiles = Files.find(Paths.get(mspDirectoryPath), 1,
					(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".msp") && fileAttr.isRegularFile()).
					map(p -> p.toFile()).collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sdfFiles = null;
		try {
			sdfFiles = Files.find(Paths.get(sdfDirectoryPath), 1,
					(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".sdf") && fileAttr.isRegularFile()).
					map(p -> p.toFile()).collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msp2sdfMap = new TreeMap<File,File>();
		for(File msp : mspFiles) {
			
			String baseName = FilenameUtils.getBaseName(msp.getName());
			File sdf = sdfFiles.stream().
					filter(f -> FilenameUtils.getBaseName(f.getName()).equals(baseName)).
					findFirst().orElse(null);
			if(sdf != null) {
				msp2sdfMap.put(msp, sdf);
			}
			else {
				System.out.println("Missing SDF for " + msp.getName());
			}			
		}
		System.out.println("MSP to SDF map created");
	}
	
	public void uploadNistDataNoStructure() throws Exception {
		 
		Connection conn = ConnectionManager.getConnection();
		for(File msp : mspFiles) {
			
			System.out.println("Starting to process " + FilenameUtils.getBaseName(msp.getName()));
			try {
				List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(msp);
				int count = 1;
				for(List<String> msmsChunk : mspChunks) {
					
					NISTTandemMassSpectrum msms = NISTMSPParser.parseNistMspDataSource(msmsChunk);
					
					if(!NISTMSPParser.isNISTSpectrumInDatabase(msms.getNistNum(), conn))
						NISTMSPParser.insertSpectrumRecord(msms, null, conn);
					
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
		ConnectionManager.releaseConnection(conn);
		System.out.println("MSMS upload completed");
	}
	
	public void uploadNistData() throws Exception {
		 
		Connection conn = ConnectionManager.getConnection();
		for(Entry<File, File> pair : msp2sdfMap.entrySet()) {
			
			System.out.println("Starting to process " + FilenameUtils.getBaseName(pair.getKey().getName()));
			try {
				Map<List<String>,IAtomContainer>msmsMolMap = createMsmsMolMap(pair.getKey(), pair.getValue());
				int count = 1;
				for(Entry<List<String>, IAtomContainer> msmsMol : msmsMolMap.entrySet()) {
					
					NISTTandemMassSpectrum msms = NISTMSPParser.parseNistMspDataSource(msmsMol.getKey());
					
//					String molName = msmsMol.getValue().getProperty(CDKConstants.TITLE);
//					String nistName = msms.getProperties().get(NISTmspField.NAME);
//					if(!molName.equals(nistName)) {
//						
//						System.out.println("Name mismatch between SDF name \"" + molName + 
//								"\" and MSMS name \"" + nistName + "\" for entry NIST# " 
//								+ Integer.toString(msms.getNistNum()));
//					}
//					else {
//						
//					}
					if(!NISTMSPParser.isNISTSpectrumInDatabase(msms.getNistNum(), conn))
						NISTMSPParser.insertSpectrumRecord(msms, msmsMol.getValue(), conn);
					
					count++;
					if(count % 50 == 0)
						System.out.print(".");
					
					if(count % 5000 == 0)
						System.out.println(".");
				}
				System.out.println(FilenameUtils.getBaseName(pair.getKey().getName()) + " was processed.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}	
		ConnectionManager.releaseConnection(conn);
		System.out.println("MSMS upload completed");
	}
	
	public static Map<List<String>,IAtomContainer> createMsmsMolMap(File mspFile, File sdfFile) throws Exception {
		
		List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(mspFile);
		List<IAtomContainer>molChunks = new ArrayList<IAtomContainer>();

		IteratingSDFReader reader = 
				new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
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
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(smiles != null)
					molecule.setProperty(CompoundIdentityField.SMILES.name(), smiles);
			}
			StringWriter writer = new StringWriter();
			SDFWriter sdfWriter = new SDFWriter(writer);
	        sdfWriter.write(molecule);
	        sdfWriter.close();
	        molecule.setProperty(CompoundIdentityField.MOL_TEXT.name(), writer.toString());
			molChunks.add(molecule);
		}			
		Map<List<String>,IAtomContainer>msmsMolMap = new HashMap<List<String>,IAtomContainer>();	
		if(mspChunks.size() != molChunks.size()) {			
			System.out.println(FilenameUtils.getBaseName(mspFile.getName()));
			System.out.println("# of MSMS = " + Integer.toString(mspChunks.size()) + 
					" | " + "# of MOL = " + Integer.toString(molChunks.size()));			
		}
		for(int i=0; i<mspChunks.size(); i++)
				msmsMolMap.put(mspChunks.get(i), molChunks.get(i));
				
		return msmsMolMap;
	}
	
	public static Map<String,IAtomContainer> createInChiKeyMolMap(
			File mspFile, 
			File sdfFile, 
			List<String>mismatchLog,
			List<String>molErrorLog ) throws Exception {
		
		List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(mspFile);
		List<IAtomContainer>molChunks = new ArrayList<IAtomContainer>();
		IteratingSDFReader reader = 
				new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
	
		while (reader.hasNext()) {

			IAtomContainer molecule = null;
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
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(smiles != null)
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
				molChunks.add(molecule);
			}
			else {
				molChunks.add(null);
			}
		}		
		Map<String,IAtomContainer>inchiKeyMolMap = new HashMap<String,IAtomContainer>();	
		for(int i=0; i<mspChunks.size(); i++) {
			
			NISTTandemMassSpectrum msms = NISTMSPParser.parseNistMspDataSource(mspChunks.get(i));
			IAtomContainer molecule = molChunks.get(i);
			molecule.setProperty(MSPField.NAME.getName(), msms.getProperties().get(MSPField.NAME));			
			if(molChunks.get(i) != null) {
				
				String mspInChiKey = msms.getProperties().get(MSPField.INCHI_KEY);
				String molInChiKey = molecule.getProperty(CompoundIdentityField.INCHIKEY.name());
				if(molInChiKey != null && molInChiKey.equals(mspInChiKey))				
					inchiKeyMolMap.put(mspInChiKey, molecule);
				else {
					mismatchLog.add("InChiKey mismatch for NIST# " +
							msms.getNistNum() + " MSP: " + mspInChiKey + " | MOL: " + molInChiKey);
				}
			}
		}		
		return inchiKeyMolMap;
	}
	
	public static Map<String,IAtomContainer> createInChiKeyMolMapFromSdfOnly( 
			File sdfFile, 
			List<String>molErrorLog ) throws Exception {
		
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String,IAtomContainer>inchiKeyMolMap = new HashMap<String,IAtomContainer>();
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		IteratingSDFReader reader = 
				new IteratingSDFReader(new FileInputStream(sdfFile), builder);
	
		while (reader.hasNext()) {

			IAtomContainer molecule = null;
			try {
				molecule = (IAtomContainer)reader.next();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(molecule != null){
				
		        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
		        CDKHydrogenAdder.getInstance(builder).addImplicitHydrogens(molecule);				
				String smiles = null;
				try {
					smiles = smilesGenerator.create(molecule);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(smiles != null)
					molecule.setProperty(CompoundIdentityField.SMILES.name(), smiles);
				
				inChIGenerator = igfactory.getInChIGenerator(molecule);
				INCHI_RET ret = inChIGenerator.getReturnStatus();
				if (ret == INCHI_RET.OKAY || ret == INCHI_RET.WARNING) {
					molecule.setProperty(CompoundIdentityField.INCHIKEY.name(), inChIGenerator.getInchiKey());
					StringWriter writer = new StringWriter();
					SDFWriter sdfWriter = new SDFWriter(writer);
			        sdfWriter.write(molecule);
			        sdfWriter.close();
			        molecule.setProperty(CompoundIdentityField.MOL_TEXT.name(), writer.toString());
			        inchiKeyMolMap.put(inChIGenerator.getInchiKey(), molecule);
				}
				else
					molErrorLog.add("Unable to generate InChi for " + molecule.getProperty(CDKConstants.TITLE));
			}
		}		
		return inchiKeyMolMap;
	}

	public void enumerateNistDataFields() {
		
		TreeSet<String>fields = new TreeSet<String>();
		Pattern searchPattern = Pattern.compile("^([^:\\d]+): ");
		Pattern numPeaksPattern = Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+(\\d+)");
		try {
			mspFiles.stream().forEach(f -> {
						try {
							List<List<String>> mspChunks = MsImportUtils.parseMspInputFile(f);
							for(List<String> chunk : mspChunks) {

								for(String line : chunk) {

									Matcher regexMatcher = numPeaksPattern.matcher(line.trim());
									if (regexMatcher.find())
										break;
									else {
										regexMatcher = searchPattern.matcher(line.trim());
										if (regexMatcher.find())
											fields.add(regexMatcher.group(1));
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(StringUtils.join(fields, "\n"));
	}

	public Map<File, File> getMsp2sdfMap() {
		return msp2sdfMap;
	}
	
}



















