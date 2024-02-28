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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.misctest.IteratingSDFReaderFixed;
import edu.umich.med.mrc2.datoolbox.utils.MsImportUtils;
import io.github.dan2097.jnainchi.InchiStatus;

public class NISTParserUtils {
	
	private static final SmilesGenerator smilesGenerator = 
			new SmilesGenerator(SmiFlavor.Isomeric);
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	
	public static final String[] nistGreekInEnlish = new String[]{
			".alpha.",".beta.",".gamma.",".delta.",".epsilon.",".zeta.",
			".eta.",".theta.",".iota.",".kappa.",".lambda.",".mu.",
			".nu.",".xi.",".omicron.",".pi.",".rho.",".sigma.",
			".tau.",".upsilon.",".phi.",".chi.",".psi.",".omega."
			};
	public static final String[] nistGreekInEnlishEscaped = new String[]{
			"\\.alpha\\.","\\.beta\\.","\\.gamma\\.","\\.delta\\.","\\.epsilon\\.","\\.zeta\\.",
			"\\.eta\\.","\\.theta\\.","\\.iota\\.","\\.kappa\\.","\\.lambda\\.","\\.mu\\.",
			"\\.nu\\.","\\.xi\\.","\\.omicron\\.","\\.pi\\.","\\.rho\\.","\\.sigma\\.",
			"\\.tau\\.","\\.upsilon\\.","\\.phi\\.","\\.chi\\.","\\.psi\\.","\\.omega\\."
			};
	public static final String[] greek = new String[]{
			"α","β","γ","δ","ε","ζ","η","θ",
			"ι","κ","λ","μ","ν","ξ","ο","π",
			"ρ","σ","τ","υ","φ","χ","ψ","ω"
			};	
	
	public static String restoreGreekLetters(String inputName) {
		
		String output = inputName;
		for(int i=0; i<24; i++) {
			
			if(output.contains(nistGreekInEnlish[i])) {
				
				//	output = output.replaceAll(nistGreekInEnlish[i], greek[i]);
				output = output.replaceAll(nistGreekInEnlishEscaped[i], greek[i]);
			}
		}
		return output;
	}
	
	public static void countEntriesAndEnumerateNistDataFields(
			File mspDirectory, 
			File fieldsOutputFile,
			File countsOutputFile) {
		
		TreeSet<String>fields = new TreeSet<String>();
		TreeMap<String,Integer>entryCount = new TreeMap<String,Integer>();
		Pattern searchPattern = 
				Pattern.compile("^([^:\\d]+): ");
		Pattern numPeaksPattern = 
				Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+(\\d+)");
		
		Collection<File> mspFiles = 
				FileUtils.listFiles(mspDirectory, new String[] {"msp", "MSP"}, false);
		
		try {
			mspFiles.stream().forEach(f -> {
						try {
							List<List<String>> mspChunks = MsImportUtils.parseMspInputFile(f);
							entryCount.put(f.getName(), mspChunks.size());
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
		
		TreeSet<String>countList = new TreeSet<String>();
		for(Entry<String, Integer> ce : entryCount.entrySet())
			countList.add(ce.getKey() + "\t" + Integer.toString(ce.getValue()));
			
		try {
			Files.write(countsOutputFile.toPath(),
					countList, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Files.write(fieldsOutputFile.toPath(),
					fields, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void normalizeSdfData(File sdfDirectory, File normSdfFolder) throws IOException {
		
//		File sdfDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\SDF");
//		File normSdfFolder = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\SDF_NORM");
		
		List<Path> pathList = Files.find(sdfDirectory.toPath(), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".sdf") 
					&& fileAttr.isRegularFile()).collect(Collectors.toList());
		
		for(Path sdfPath : pathList) {
			
			System.out.println("Processing " + sdfPath.toFile().getName());
			List<String> sdfText = new ArrayList<String>();
			try {
				sdfText = Files.readAllLines(sdfPath, StandardCharsets.ISO_8859_1);
			} catch (IOException e) {
				e.printStackTrace();
			}				
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
		    Path path = Paths.get(normSdfFolder.getAbsolutePath(), sdfPath.toFile().getName());		 
		    Files.write(path, newSdfText);
		}
	}
	
	public static void checkForMissingSDFFiles(File mspDirectory, File sdfDirectory) throws Exception{
		
//		File mspDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\MSP");
//		File sdfDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\SDF_NORM");
		
		Collection<File> mspFiles = 
				FileUtils.listFiles(mspDirectory, new String[] {"msp", "MSP"}, false);
		Collection<File> sdfFiles = 
				FileUtils.listFiles(sdfDirectory, new String[] {"sdf", "SDF"}, false);
		
		for(File mspFile : mspFiles) {
			
			if(mspFile.isDirectory())
				continue;
			
			String fName = FilenameUtils.getBaseName(mspFile.getName());			
			File sdfFile = sdfFiles.stream().
					filter(f -> FilenameUtils.getBaseName(f.getName()).equals(fName)).
					findFirst().orElse(null);
			if(sdfFile == null)
				System.out.println("Missing SDF for " + mspFile.getName());	
		}		
	}
	
	public static void findSizeMismatchedMSPandSDFFiles(
			File mspDirectory, 
			File sdfDirectory,
			File mspDestDirectory,
			File sdfDestDirectory,
			File logFile) throws Exception{
		
//		File mspDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\MSP");
//		File mspDestDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\_MISMATCHED\\MSP");		
//		File sdfDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\SDF_NORM");
//		File sdfDestDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\_MISMATCHED\\SDF_NORM");
		
		Collection<File> mspFiles = 
				FileUtils.listFiles(mspDirectory, new String[] {"msp", "MSP"}, false);
		Collection<File> sdfFiles = 
				FileUtils.listFiles(sdfDirectory, new String[] {"sdf", "SDF"}, false);
		
		Map<File,File>msp2sdfMap = new TreeMap<File,File>();
		for(File mspFile : mspFiles) {
			
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
		System.out.println("Found " + msp2sdfMap.size() + " MSP/SDF file pairs");
		TreeSet<String>mismatches = new TreeSet<String>();
		for(Entry<File, File> ff : msp2sdfMap.entrySet()) {
			
			System.out.println("Processing " + ff.getKey().getName());
			List<List<String>> mspChunks = MsImportUtils.parseMspInputFile(ff.getKey());
			List<IAtomContainer>mols = parseSDFFile(ff.getValue());
			if(mspChunks.size() != mols.size()) {
				
				mismatches.add(FilenameUtils.getBaseName(ff.getKey().getName()));
				
				File src = Paths.get(mspDirectory.getAbsolutePath(), ff.getKey().getName()).toFile();
				File dest = Paths.get(mspDestDirectory.getAbsolutePath(), ff.getKey().getName()).toFile();
				try {
					FileUtils.moveFile(src, dest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				src = Paths.get(sdfDirectory.getAbsolutePath(), ff.getValue().getName()).toFile();
				dest = Paths.get(sdfDestDirectory.getAbsolutePath(), ff.getValue().getName()).toFile();
				try {
					FileUtils.moveFile(src, dest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			Files.write(
					logFile.toPath(), 
					mismatches, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static List<IAtomContainer>parseSDFFile(File sdfFile){
		
		IteratingSDFReaderFixed reader = null;
		List<IAtomContainer>molecules = new ArrayList<IAtomContainer>();
		try {
			reader = new IteratingSDFReaderFixed(
					new FileInputStream(sdfFile), 
					DefaultChemObjectBuilder.getInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		while (reader.hasNext()) {
			
			IAtomContainer molecule = null;					
			try {
				molecule = (IAtomContainer)reader.next();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(molecule != null)
				molecules.add(molecule);
			else {
				System.err.print("SDF error in file " + sdfFile.getName());
			}
		}
		return molecules;
	}
	
	private static List<IAtomContainer>parseSDFFileCreatingSMILES(File sdfFile){
		
		IteratingSDFReaderFixed reader = null;
		List<IAtomContainer>molecules = new ArrayList<IAtomContainer>();
		try {
			reader = new IteratingSDFReaderFixed(
					new FileInputStream(sdfFile), 
					DefaultChemObjectBuilder.getInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		while (reader.hasNext()) {
			
			IAtomContainer molecule = null;					
			try {
				molecule = (IAtomContainer)reader.next();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(molecule != null) {
				
				String smiles = null;
				try {
					smiles = smilesGenerator.create(molecule);
				} catch (NullPointerException | CDKException e) {
					System.out.println("Unable to generate SMILES for " + molecule.getProperty(CDKConstants.TITLE));
				}
				if(smiles != null)					
					molecules.add(molecule);
			}
			else {
				System.err.print("SDF error in file " + sdfFile.getName());
			}
		}
		return molecules;
	}
	
	public static void findSDFFilesWithBadMolEntries(
			File mspDirectory, 
			File sdfDirectory,
			File mspDestDirectory,
			File sdfDestDirectory,
			File logFile) throws Exception{
		
//		File mspDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\MSP");
//		File mspDestDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\_BAD_MOL\\MSP");		
//		File sdfDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\SDF_NORM");
//		File sdfDestDirectory = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST2023-export\\_BAD_MOL\\SDF_NORM");
		
		Collection<File> mspFiles = 
				FileUtils.listFiles(mspDirectory, new String[] {"msp", "MSP"}, false);
		Collection<File> sdfFiles = 
				FileUtils.listFiles(sdfDirectory, new String[] {"sdf", "SDF"}, false);
		
		Map<File,File>msp2sdfMap = new TreeMap<File,File>();
		for(File mspFile : mspFiles) {
			
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
		System.out.println("Found " + msp2sdfMap.size() + " MSP/SDF file pairs");
		TreeSet<String>mismatches = new TreeSet<String>();
		for(Entry<File, File> ff : msp2sdfMap.entrySet()) {
			
			System.out.println("Processing " + ff.getKey().getName());
			List<List<String>> mspChunks = MsImportUtils.parseMspInputFile(ff.getKey());
			List<IAtomContainer>mols = parseSDFFileCreatingSMILES(ff.getValue());
			if(mspChunks.size() != mols.size()) {
				
				mismatches.add(FilenameUtils.getBaseName(ff.getKey().getName()));
				
				File src = Paths.get(mspDirectory.getAbsolutePath(), ff.getKey().getName()).toFile();
				File dest = Paths.get(mspDestDirectory.getAbsolutePath(), ff.getKey().getName()).toFile();
				try {
					FileUtils.moveFile(src, dest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				src = Paths.get(sdfDirectory.getAbsolutePath(), ff.getValue().getName()).toFile();
				dest = Paths.get(sdfDestDirectory.getAbsolutePath(), ff.getValue().getName()).toFile();
				try {
					FileUtils.moveFile(src, dest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			Files.write(
					logFile.toPath(), 
					mismatches, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void veryfyMSP2SDFsructureMatch(
			List<List<String>> mspChunks, 
			List<IAtomContainer>mols,
			String logName,
			File logDir) throws Exception {
		
		if(mspChunks.size() != mols.size()) {
			throw new IllegalArgumentException(
					"List of spectra is not the same size as list of molecules");
		}
		Map<NISTTandemMassSpectrum,IAtomContainer>msmsMolMap = 
				new HashMap<NISTTandemMassSpectrum,IAtomContainer>();	
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String>molErrorLog = new ArrayList<String>();
		for(int i=0; i<mspChunks.size(); i++) {
			
			IAtomContainer molecule = mols.get(i);		
			String smiles = null;
			try {
				smiles = smilesGenerator.create(molecule);
			} catch (NullPointerException | CDKException e) {
				molErrorLog.add("Unable to generate SMILES for " + molecule.getProperty(CDKConstants.TITLE));
				molErrorLog.add(e.getMessage());
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
			
			NISTTandemMassSpectrum msms = 
					NISTMSPParser.parseNistMspDataSource(mspChunks.get(i));

			msmsMolMap.put(msms, molecule);			
			if(msms.getProperties().get(MSPField.INCHI_KEY) == null 
					|| molecule.getProperty(CompoundIdentityField.INCHIKEY.name()) == null) {
				molErrorLog.add("No InChiKey in MSP and/or SDF for NIST# " + msms.getNistNum());
			}
			else {
				String mspInChiKey2D = msms.getProperties().get(MSPField.INCHI_KEY).split("-")[0];
				String molInChiKey2D = ((String)molecule.getProperty(CompoundIdentityField.INCHIKEY.name())).split("-")[0];
				if(molInChiKey2D != null && !molInChiKey2D.equals(mspInChiKey2D))				
					molErrorLog.add("InChiKey mismatch for NIST# " +
							msms.getNistNum() + " MSP: " + mspInChiKey2D + " | MOL: " + molInChiKey2D);	
			}
		}
		if(!molErrorLog.isEmpty()) {
			
			String logFileName = FilenameUtils.getBaseName(logName) + ".log";
			try {
				Files.write(Paths.get(logDir.getAbsolutePath(), logFileName), 
						molErrorLog, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("MSMS upload completed");
	}
	
	public static Map<NISTTandemMassSpectrum,IAtomContainer> createMsmsMolMap(
			File mspFile, File sdfFile, File logDir) throws Exception {
		
		List<String>molErrorLog = new ArrayList<String>();
		List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(mspFile);
		List<IAtomContainer>molChunks = new ArrayList<IAtomContainer>();
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				if(smiles != null)
					molecule.setProperty(CompoundIdentityField.SMILES.name(), smiles);
				
				inChIGenerator = igfactory.getInChIGenerator(molecule);
				InchiStatus ret = inChIGenerator.getStatus();
				if (ret == InchiStatus.SUCCESS || ret == InchiStatus.WARNING)
					molecule.setProperty(CompoundIdentityField.INCHIKEY.name(), inChIGenerator.getInchiKey());
				else
					molErrorLog.add("Unable to generate InChi for " + molecule.getProperty(CDKConstants.TITLE));
			}
			StringWriter writer = new StringWriter();
			SDFWriter sdfWriter = new SDFWriter(writer);
	        sdfWriter.write(molecule);
	        sdfWriter.close();
	        molecule.setProperty(CompoundIdentityField.MOL_TEXT.name(), writer.toString());
			molChunks.add(molecule);
		}			
		Map<NISTTandemMassSpectrum,IAtomContainer>msmsMolMap = 
				new HashMap<NISTTandemMassSpectrum,IAtomContainer>();	
		if(mspChunks.size() != molChunks.size()) {	
			
			String message = FilenameUtils.getBaseName(mspFile.getName()) + 
					"\n# of MSMS = " + Integer.toString(mspChunks.size()) + 
					" | " + "# of MOL = " + Integer.toString(molChunks.size()) + "\n";
			System.out.print(message);
			molErrorLog.add(message);
		}
		for(int i=0; i<mspChunks.size(); i++) {
			
			NISTTandemMassSpectrum msms = 
					NISTMSPParser.parseNistMspDataSource(mspChunks.get(i));
			IAtomContainer molecule = molChunks.get(i);
			msmsMolMap.put(msms, molChunks.get(i));
			
			String mspInChiKey2D = msms.getProperties().get(MSPField.INCHI_KEY).split("-")[0];
			String molInChiKey2D = ((String)molecule.getProperty(CompoundIdentityField.INCHIKEY.name())).split("-")[0];
			if(molInChiKey2D != null && !molInChiKey2D.equals(mspInChiKey2D))				
				molErrorLog.add("InChiKey mismatch for NIST# " +
						msms.getNistNum() + " MSP: " + mspInChiKey2D + " | MOL: " + molInChiKey2D);			
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
		return msmsMolMap;
	}
}
