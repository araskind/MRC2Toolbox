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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
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
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.ChromatographyDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTParserUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.AgilentAcquisitionMethodParser;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.ChromatographicGradientUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.ThermoAcquisitionMethodParser;

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
			groupAndReassignThermoGradients();
			//	downloadMethodsToExtractGradients();
			//	extractTemporaryGradients();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static void groupAndReassignThermoGradients() throws Exception{
		
		Collection<ChromatographicGradient>tmpGradients = 
				ChromatographyDatabaseUtils.getTempChromatographicGradientList();
		Set<String>tmpGradIds = getTmpThermoGradientIds();
		List<ChromatographicGradient> thermoTmpGrads = tmpGradients.stream().
				filter(g -> tmpGradIds.contains(g.getId())).
				collect(Collectors.toList());		
		Map<Integer,List<ChromatographicGradient>>gradientGroups = 
				new TreeMap<Integer,List<ChromatographicGradient>>();
		ChromatographicGradient[]tmpGradArray = 
				thermoTmpGrads.toArray(new ChromatographicGradient[thermoTmpGrads.size()]);
		int groupCount = 0;
		gradientGroups.put(0, new ArrayList<ChromatographicGradient>());
		gradientGroups.get(0).add(tmpGradArray[0]);
		for(int i=1; i<tmpGradArray.length; i++) {
			
			boolean matched = false;
			for(int j : gradientGroups.keySet()) {
				
				for(ChromatographicGradient grad : gradientGroups.get(j)) {
					
					if(ChromatographicGradientUtils.gradientsEquivalent(tmpGradArray[i], grad)) {						
						matched = true;
						break;
					}
				}
				if(matched) {
					 gradientGroups.get(j).add(tmpGradArray[i]);
					 break;
				}					
			}
			if(!matched) {
				groupCount++;
				gradientGroups.put(groupCount, new ArrayList<ChromatographicGradient>());
				gradientGroups.get(groupCount).add(tmpGradArray[i]);
			}
		}
		System.out.println("Gradients grouped");
		int groupedCount = 0;
		Set<String>tmpIds = new TreeSet<String>();		
		for(List<ChromatographicGradient>gradList : gradientGroups.values()) {
			groupedCount += gradList.size();
			gradList.stream().forEach(g -> tmpIds.add(g.getId()));
		}		
		System.out.println("Count in grouped " + groupedCount);
		System.out.println("Count unique " + tmpIds.size());
		//ArrayList<String>logData = new 	ArrayList<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET GRADIENT_ID = ? WHERE TMP_GRADIENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		for(List<ChromatographicGradient>gradList : gradientGroups.values()) {
			
			ChromatographicGradient newGrad = gradList.get(0);
			String tmpId = newGrad.getId();
			ChromatographyDatabaseUtils.addNewChromatographicGradient(newGrad);	
			
			ps.setString(1, newGrad.getId());
			ps.setString(2, tmpId);
			ps.executeUpdate();				
			if(gradList.size() > 1) {
				
				for(int i=1; i<gradList.size(); i++) {
					
					//logData.add(gradList.get(i).getId() + "" + newGrad.getId());
					ps.setString(1, newGrad.getId());
					ps.setString(2, gradList.get(i).getId());
					ps.executeUpdate();	
				}	
			}	
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
//		Path logPath = 
//				Paths.get("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618", "tmpToRegGradMap.txt");
//		try {
//			Files.write(logPath, 
//					logData, 
//					StandardCharsets.UTF_8, 
//					StandardOpenOption.CREATE,
//					StandardOpenOption.TRUNCATE_EXISTING);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	private static Set<String>getTmpThermoGradientIds() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT TMP_GRADIENT_ID FROM DATA_ACQUISITION_METHOD "
				+ "WHERE GRADIENT_ID IS NULL AND TMP_GRADIENT_ID IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		Set<String>tmpGradIds = new TreeSet<String>();
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			tmpGradIds.add(rs.getString(1));
		
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);	
		
		return tmpGradIds;
	}
	
	private static void checkThermoGradientTimetablesAgainstExisting() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT TMP_GRADIENT_ID FROM DATA_ACQUISITION_METHOD "
				+ "WHERE GRADIENT_ID IS NULL AND TMP_GRADIENT_ID IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		Set<String>tmpGradIds = new TreeSet<String>();
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			tmpGradIds.add(rs.getString(1));
		
		rs.close();
		
		Collection<ChromatographicGradient>tmpGradsAll = 
				ChromatographyDatabaseUtils.getTempChromatographicGradientList();
		List<ChromatographicGradient> thermoTmpGrads = tmpGradsAll.stream().
				filter(g -> tmpGradIds.contains(g.getId())).
				collect(Collectors.toList());
		Collection<ChromatographicGradient>existingGrads = 
				ChromatographyDatabaseUtils.getChromatographicGradientList();
		
		Map<ChromatographicGradient,List<ChromatographicGradient>>gradientGroups = 
				new HashMap<ChromatographicGradient,List<ChromatographicGradient>>();

		for(ChromatographicGradient grad :existingGrads)
			gradientGroups.put(grad, new ArrayList<ChromatographicGradient>());
		
		for(ChromatographicGradient grad :existingGrads) {
			
			for(ChromatographicGradient tmpGrad : thermoTmpGrads) {
				
				if(ChromatographicGradientUtils.timeTableEquivalent(tmpGrad, grad))
					gradientGroups.get(grad).add(tmpGrad);								
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void extractThermoTempGradients() {
		
		File destination = 
				new File("E:\\DataAnalysis\\METHODS\\Acquisition\\"
						+ "Uploaded\\AS_OF_20240618\\Thermo");
		Collection<DataAcquisitionMethod> list = null;
		try {
			list = AcquisitionMethodUtils.getAcquisitionMethodList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<String>badMethods = new ArrayList<String>();
		for(File methodFile : destination.listFiles()) {
			
			if(!methodFile.getName().endsWith(".meth"))
				continue;
			
			ChromatographicGradient grad = 
					ThermoAcquisitionMethodParser.extractGradient(methodFile);
			if(grad == null) 
				badMethods.add(methodFile.getName());
			else {
				DataAcquisitionMethod method = list.stream().
						filter(m -> m.getName().equals(methodFile.getName())).
						findFirst().orElse(null);
				if(method != null) {
					
					try {
						ChromatographyDatabaseUtils.addTmpChromatographicGradientForAcqMethod(
								grad, method.getId(), false);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					System.out.println("Name / file name mismatch for " + methodFile.getName());
				}
			}			
		}	
		if (!badMethods.isEmpty()) {
			Path logPath = Paths.get(
					destination.getAbsolutePath(), "failedGradExtraction.txt");
			try {
				Files.write(
						logPath, 
						badMethods, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void downloadThermoMethodsToExtractGradients() {
		
		File destination = 
				new File("E:\\DataAnalysis\\METHODS\\Acquisition\\"
						+ "Uploaded\\AS_OF_20240618\\Thermo");
		Collection<DataAcquisitionMethod> list = null;
		try {
			list = AcquisitionMethodUtils.getAcquisitionMethodList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<DataAcquisitionMethod> toDownload = list.stream().
				filter(m -> m.getName().endsWith(".meth")).
				collect(Collectors.toList());
			
		for(DataAcquisitionMethod method : toDownload) {			
	
			try {
				AcquisitionMethodUtils.getAcquisitionMethodFile(method, destination);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private static void thermoMethodReadTest() throws Exception {

		File methodFile = new File("C:\\Users\\Sasha\\Downloads\\BEH_Amide_MS2_20min_Neg.meth");
		
		ThermoAcquisitionMethodParser.extractGradient(methodFile);
		
//		byte[] bytes = Files.readAllBytes(methodFile.toPath());
//		String contents = new String(bytes, StandardCharsets.UTF_8);		
//		String contentsCleaned = StringUtils.remove(contents, '\u0000');		
//		int startMethodXML = contentsCleaned.indexOf("<?xml version");
//		int endMethodXML = contentsCleaned.lastIndexOf("</CmData>");		
//		String methodXML = contentsCleaned.substring(startMethodXML, endMethodXML).
//				replaceAll("[^\\x00-\\x7F]", " ").replaceAll("\\p{C}", " ");		
//		
//		Path outputPath = Paths.get(methodFile.getParentFile().getAbsolutePath(), 
//				FileNameUtils.getBaseName(methodFile.getName()) + "-instrumentMethod.xml");
//		try {
//		Files.writeString(outputPath, 
//				methodXML + "</CmData>", 
//				StandardCharsets.UTF_8,
//				StandardOpenOption.CREATE, 
//				StandardOpenOption.TRUNCATE_EXISTING);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
//		Document xmlMethodDoc = XmlUtils.readXmlFromString(methodXML + "</CmData>");
//		if(xmlMethodDoc != null) {
//			
//			File instrumentMethodXml = Paths.get(methodFile.getParentFile().getAbsolutePath(), 
//					FileNameUtils.getBaseName(methodFile.getName()) + "-instrumentMethod.xml").toFile();
//			XmlUtils.writeXMLDocumentToFile(xmlMethodDoc, instrumentMethodXml);
//		}
						
//		int startCalibration = contentsCleaned.indexOf("<TuneFiles>");
//		int endCalibration = contentsCleaned.indexOf("</TuneFiles>");
//		String calibration = contentsCleaned.substring(startCalibration, endCalibration);
//		System.out.println(calibration + "</TuneFiles>");
	
//		int startOverview = contentsCleaned.indexOf("---- Overview");
//		int endOverview = contentsCleaned.indexOf("Stop Run");
//		String overview = contentsCleaned.substring(startOverview, endOverview);		
//		System.out.println(overview + " Stop Run");
		
//		Path outputPath = Paths.get(methodFile.getParentFile().getAbsolutePath(), 
//				FileNameUtils.getBaseName(methodFile.getName()) + "-LC-method-overview.txt");
//		try {
//			Files.writeString(outputPath, 
//					overview + "Stop Run", 
//					StandardCharsets.UTF_8,
//					StandardOpenOption.CREATE, 
//					StandardOpenOption.TRUNCATE_EXISTING);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		int startIDXmethod = contentsCleaned.indexOf("ID-X Method Summary");
//		int endIDXmethod = contentsCleaned.indexOf("AuditData");
//		String idxMethod = contentsCleaned.substring(startIDXmethod, endIDXmethod);		
////		System.out.println(idxMethod);
//		
//		outputPath = Paths.get(methodFile.getParentFile().getAbsolutePath(), 
//				FileNameUtils.getBaseName(methodFile.getName()) + "-IDX-method-overview.txt");
//		try {
//			Files.writeString(outputPath, 
//					idxMethod, 
//					StandardCharsets.UTF_8,
//					StandardOpenOption.CREATE, 
//					StandardOpenOption.TRUNCATE_EXISTING);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}			
	}
	
	public static void dump(DirectoryEntry root) throws IOException {
		
	    System.out.println(root.getName() + " : storage CLSID " + root.getStorageClsid());
	    while(root.iterator().hasNext()) {
	    	
	    	org.apache.poi.poifs.filesystem.Entry entry = root.iterator().next();
	        if (entry instanceof DocumentNode) {
	            DocumentNode node = (DocumentNode) entry;
	            System.out.println("Node name: " + node.getName());
	            System.out.println("Node desc: " + node.getShortDescription());
	            System.out.println("Node size: " + node.getSize());
	            DocumentInputStream is = new DocumentInputStream(node);
	            try {
	                PropertySet ps = new PropertySet(is);
	                if (ps.getSectionCount() != 0) {
	                    for (Property p : ps.getProperties()) {
	                        System.out.println("Prop: " + p.getID() + " " + p.getValue());
	                    }
	                }
	            } catch (NoPropertySetStreamException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } catch (Exception e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        } else if (entry instanceof DirectoryEntry) {
	            DirectoryEntry dir = (DirectoryEntry) entry;
	            dump(dir);
	        } else {
	            System.err.println("Skipping unsupported POIFS entry: " + entry);
	        }
	    }
	}
	
	private static void groupAndReassignGradients() throws Exception{
		
		Collection<ChromatographicGradient>tmpGradients = 
				ChromatographyDatabaseUtils.getTempChromatographicGradientList();
		Map<Integer,List<ChromatographicGradient>>gradientGroups = 
				new TreeMap<Integer,List<ChromatographicGradient>>();
		ChromatographicGradient[]tmpGradArray = 
				tmpGradients.toArray(new ChromatographicGradient[tmpGradients.size()]);
		int groupCount = 0;
		gradientGroups.put(0, new ArrayList<ChromatographicGradient>());
		gradientGroups.get(0).add(tmpGradArray[0]);
		for(int i=1; i<tmpGradArray.length; i++) {
			
			boolean matched = false;
			for(int j : gradientGroups.keySet()) {
				
				for(ChromatographicGradient grad : gradientGroups.get(j)) {
					
					if(ChromatographicGradientUtils.gradientsEquivalent(tmpGradArray[i], grad)) {						
						matched = true;
						break;
					}
				}
				if(matched) {
					 gradientGroups.get(j).add(tmpGradArray[i]);
					 break;
				}					
			}
			if(!matched) {
				groupCount++;
				gradientGroups.put(groupCount, new ArrayList<ChromatographicGradient>());
				gradientGroups.get(groupCount).add(tmpGradArray[i]);
			}
		}
		System.out.println("Gradients grouped");
		int groupedCount = 0;
		Set<String>tmpIds = new TreeSet<String>();		
		for(List<ChromatographicGradient>gradList : gradientGroups.values()) {
			groupedCount += gradList.size();
			gradList.stream().forEach(g -> tmpIds.add(g.getId()));
		}		
		System.out.println("Count in grouped " + groupedCount);
		System.out.println("Count unique " + tmpIds.size());
		//ArrayList<String>logData = new 	ArrayList<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET GRADIENT_ID = ? WHERE TMP_GRADIENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		for(List<ChromatographicGradient>gradList : gradientGroups.values()) {
			
			ChromatographicGradient newGrad = gradList.get(0);
			String tmpId = newGrad.getId();
			ChromatographyDatabaseUtils.addNewChromatographicGradient(newGrad);	
			
			ps.setString(1, newGrad.getId());
			ps.setString(2, tmpId);
			ps.executeUpdate();				
			if(gradList.size() > 1) {
				
				for(int i=1; i<gradList.size(); i++) {
					
					//logData.add(gradList.get(i).getId() + "" + newGrad.getId());
					ps.setString(1, newGrad.getId());
					ps.setString(2, gradList.get(i).getId());
					ps.executeUpdate();	
				}	
			}	
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
//		Path logPath = 
//				Paths.get("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618", "tmpToRegGradMap.txt");
//		try {
//			Files.write(logPath, 
//					logData, 
//					StandardCharsets.UTF_8, 
//					StandardOpenOption.CREATE,
//					StandardOpenOption.TRUNCATE_EXISTING);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	private static void debugGradientExtraction() throws Exception{
		
		File methodFolder = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\"
				+ "AS_OF_20240618\\ProblemMethods\\Tryptophan.m");
		
		//	Extract gradient and save as temporary
		AgilentAcquisitionMethodParser amp = 
				new AgilentAcquisitionMethodParser(methodFolder);
		amp.parseParameterFiles();
		ChromatographicGradient grad = amp.extractGradientData();
		if(grad != null) {					
			if(grad.getGradientSteps().isEmpty())
				System.out.println(methodFolder.getName() + "\tNo time table");
						
			MobilePhase[] gradMobilePhases = new MobilePhase[4];
			for(int i=0; i<4; i++) {
				
				MobilePhase mp = grad.getMobilePhases()[i];
				if(mp != null) {							
					MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(mp.getName());
					if(existing == null) 
						System.out.println(methodFolder.getName() + "\tUnknown mobile phase " + mp.getName());
					else
						gradMobilePhases[i] = existing;
				}
				else
					gradMobilePhases[i] = null;
			}		
			if(!grad.areMobilePhasesDefined()) 
				System.out.println(methodFolder.getName() + "\tNo mobile phases found");
		}
		else 
			System.out.println(methodFolder.getName() + "\tFailed to extract gradient");
	}
	
	private static void extractTemporaryGradients() throws Exception{
		
		Collection<String>logData = new ArrayList<String>();
		File tmpFolder = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\TMP");
		
		Connection conn = ConnectionManager.getConnection();
		String selectQuery = 
				"SELECT ACQ_METHOD_ID FROM DATA_ACQUISITION_METHOD "
				+ "WHERE METHOD_CONTAINER IS NOT NULL AND TMP_GRADIENT_ID IS NULL";
		PreparedStatement ps = conn.prepareStatement(selectQuery);
		Collection<String>validIds = new TreeSet<String>();
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			validIds.add(rs.getString(1));
		
		rs.close();
		
		String query = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET METHOD_NAME = ? WHERE ACQ_METHOD_ID = ?";
		ps = conn.prepareStatement(query);
		
		//	Get all Agilent methodsfrom the database
		Collection<DataAcquisitionMethod> allMethods = 
				IDTDataCache.getAcquisitionMethods().stream().
					filter(m -> validIds.contains(m.getId())).
					filter(m -> m.getSoftware().getPlatform().getId().equals("PL001")).
					filter(m -> (m.getSeparationType().getId().equals("HPLC")
							|| m.getSeparationType().getId().equals("UPLC")
							|| m.getSeparationType().getId().equals("LC"))).
					collect(Collectors.toList());

		for(DataAcquisitionMethod method : allMethods) {
			
			try {
				AcquisitionMethodUtils.getAcquisitionMethodFile(method, tmpFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File[]downloaded = tmpFolder.listFiles(File::isDirectory);
			if(downloaded.length == 0) {
				logData.add(method.getId() + "\t" + method.getName() + "\tNo method file");
				FileUtils.cleanDirectory(tmpFolder);
				continue;
			}
			File methodFolder = downloaded[0];
			if(!methodFolder.isDirectory()) {
				logData.add(method.getId() + "\t" + method.getName() + "\tMethod is not directory");
				FileUtils.cleanDirectory(tmpFolder);
				continue;
			}
			List<Path>realMethodFolderPaths = 
					FIOUtils.findDirectoriesByExtension(methodFolder.toPath(), "m");
			
			if(realMethodFolderPaths != null && !realMethodFolderPaths.isEmpty()) {
				
				File realMethodFolder = null;
				if(realMethodFolderPaths.size() == 1)
					realMethodFolder = realMethodFolderPaths.get(0).toFile();
				
				if(realMethodFolderPaths.size() > 1)
					realMethodFolder = realMethodFolderPaths.get(1).toFile();
				
				//	Replace method file and update the name if method was not saved properly
				if(!realMethodFolder.equals(methodFolder)) {
					
					try {
						AcquisitionMethodUtils.updateAcquisitionMethod(method, realMethodFolder);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}							
					ps.setString(1, realMethodFolder.getName());
					ps.setString(2, method.getId());
					ps.executeUpdate();
					logData.add(method.getId() + "\t" + method.getName() + "\tReplaced method file and updated name");
				}
				//	Extract gradient and save as temporary
				AgilentAcquisitionMethodParser amp = 
						new AgilentAcquisitionMethodParser(methodFolder);
				amp.parseParameterFiles();
				ChromatographicGradient grad = amp.extractGradientData();
				if(grad != null) {					
					if(grad.getGradientSteps().isEmpty()) {
						logData.add(method.getId() + "\t" + method.getName() + "\tNo time table");
						FileUtils.cleanDirectory(tmpFolder);
						continue;
					}				
					MobilePhase[] gradMobilePhases = new MobilePhase[4];
					boolean hasUnknownMobPhase = false;
					for(int i=0; i<4; i++) {
						
						MobilePhase mp = grad.getMobilePhases()[i];
						if(mp != null) {							
							MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(mp.getName());
							if(existing == null) {
								logData.add(method.getId() + "\t" + method.getName() + "\tUnknown mobile phase " + mp.getName());
								hasUnknownMobPhase = true;
							}
							else
								gradMobilePhases[i] = existing;
						}
						else
							gradMobilePhases[i] = null;
					}
					if(hasUnknownMobPhase) {
						FileUtils.cleanDirectory(tmpFolder);
						continue;
					}
					for(int i=0; i<4; i++)
						grad.setMobilePhase(gradMobilePhases[i], i);
										
					if(!grad.areMobilePhasesDefined()) {
						logData.add(method.getId() + "\t" + method.getName() + "\tNo mobile phases found");
						FileUtils.cleanDirectory(tmpFolder);
						continue;
					}
					//	Upload temp gradient for method		
					try {
						ChromatographyDatabaseUtils.addTmpChromatographicGradientForAcqMethod(grad, method.getId(), false);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					logData.add(method.getId() + "\t" + method.getName() + "\tFailed to extract gradient");
					FileUtils.cleanDirectory(tmpFolder);
				}			
			}	
			FileUtils.cleanDirectory(tmpFolder);
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Save Log file
		Path logPath = 
				Paths.get(tmpFolder.getParentFile().getAbsolutePath(), 
				"tmp_gradient_import_log_20240701_1.txt");
		try {
			Files.write(logPath, 
					logData, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void findGradientWithEquivalentTimeTable() {

		Collection<ChromatographicGradient> existingGradients = 
				IDTDataCache.getChromatographicGradientList();
		
		File methodFolder = new File("E:\\DataAnalysis\\METHODS\\Acquisition\\"
				+ "Uploaded\\AS_OF_20240618\\Dups2fix\\2.1 mm Gradient MS2 180 min.m");
		
		AgilentAcquisitionMethodParser amp = 
				new AgilentAcquisitionMethodParser(methodFolder);
		amp.parseParameterFiles();
		ChromatographicGradient grad = amp.extractGradientData();
		if(grad != null) {
			
			for(ChromatographicGradient g : existingGradients) {
				
				if(ChromatographicGradientUtils.timeTableEquivalent(g, grad))
					System.out.println(g.getId());
			}
		}
		System.out.println("***");
	}
	
	private static void mapAdditionalGradients() throws Exception {
		
		File dirToScan = new File("E:\\DataAnalysis\\METHODS\\Acquisition\\"
				+ "Uploaded\\AS_OF_20240618\\GradReextract");
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));
		Collection<File> methodFolders = FileUtils.listFilesAndDirs(
				dirToScan,
				DirectoryFileFilter.DIRECTORY,
				dotMfilter);
		
		Collection<ChromatographicGradient> existingGradients = 
				IDTDataCache.getChromatographicGradientList();
		Map<ChromatographicGradient,Collection<File>>gradientMethodMap = 
				new HashMap<ChromatographicGradient,Collection<File>>();
		for(ChromatographicGradient grad : existingGradients)
			gradientMethodMap.put(grad, new TreeSet<File>());
		
		for(File methodFolder : methodFolders) {
			
			if(!FilenameUtils.getExtension(methodFolder.getName()).equalsIgnoreCase("m"))
				continue;
			
			AgilentAcquisitionMethodParser amp = 
					new AgilentAcquisitionMethodParser(methodFolder);
			amp.parseParameterFiles();
			ChromatographicGradient grad = amp.extractGradientData();
			if(grad != null) {
				
				if(grad.getGradientSteps().isEmpty()) {
					System.err.println("No timetable in \"" + methodFolder.getName() + "\"");
					continue;
				}				
				MobilePhase[] gradMobilePhases = new MobilePhase[4];
				int mpCount = 0;
				for(int i=0; i<4; i++) {
					
					MobilePhase mp = grad.getMobilePhases()[i];
					if(mp != null) {
						
						MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(mp.getName());
						if(existing == null)
							System.err.println("Didn't find existing mobile phase for \"" 
									+ mp.getName() + "\" in \"" + methodFolder.getName() + "\"");
						else
							gradMobilePhases[i] = existing;
					}
					else
						gradMobilePhases[i] = null;
				}
				if(!grad.areMobilePhasesDefined()) {
					System.err.println("No mobile phases found in  \"" 
							+ methodFolder.getName() + "\"");
					continue;
				}
				ChromatographicGradient existingGrad = null;
				for(ChromatographicGradient g : gradientMethodMap.keySet()) {
					
					if(ChromatographicGradientUtils.gradientsEquivalent(g, grad)) {
						existingGrad = g;
						break;
					}
				}
				if(existingGrad == null) {
					gradientMethodMap.put(grad, new TreeSet<File>());
					gradientMethodMap.get(grad).add(methodFolder);
				}
				else {
					gradientMethodMap.get(existingGrad).add(methodFolder);
				}		
			}
			else {
				System.err.println("Failed to extract gradient from  \"" + methodFolder.getName() + "\"");
			}
		}
		for(ChromatographicGradient g : gradientMethodMap.keySet()) {
			
			if(!existingGradients.contains(g)) {
				
				try {
					ChromatographyDatabaseUtils.addNewChromatographicGradient(g);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//	Assign gradients to methods
		Connection conn = ConnectionManager.getConnection();
		String updQuery = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET GRADIENT_ID = ? WHERE ACQ_METHOD_ID = ?";
		PreparedStatement ps = conn.prepareStatement(updQuery);
		
		for(Entry<ChromatographicGradient, Collection<File>> pair : gradientMethodMap.entrySet()) {
			
			for(File f : pair.getValue()) {
				
				DataAcquisitionMethod method = IDTDataCache.getAcquisitionMethodByName(f.getName());
				if(method != null) {
					
					ps.setString(1, pair.getKey().getId());
					ps.setString(2, method.getId());
					ps.executeUpdate();
				}
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);

		System.out.println("***");
	}

	
	private static void downloadMethodsToExtractGradients() {
		
		File destination = 
				new File("E:\\DataAnalysis\\METHODS\\Acquisition\\"
						+ "Uploaded\\AS_OF_20240618\\ProblemMethods");
		Collection<DataAcquisitionMethod> list = null;
		try {
			list = AcquisitionMethodUtils.getAcquisitionMethodList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File methodList = new File("E:\\DataAnalysis\\METHODS\\Acquisition\\"
				+ "Uploaded\\AS_OF_20240618\\tmp_gradient_import_log.txt");
		String[][] methodListData = DelimitedTextParser.parseTextFile(
				methodList, MRC2ToolBoxConfiguration.getTabDelimiter());
		Collection<String>methodIds = new TreeSet<String>();
		for(int i=0; i<methodListData.length; i++)
			methodIds.add(methodListData[i][0]);
		
		List<DataAcquisitionMethod> toDownload = list.stream().
			filter(m -> methodIds.contains(m.getId())).
			collect(Collectors.toList());
		
		for(DataAcquisitionMethod method : toDownload) {			

			try {
				AcquisitionMethodUtils.getAcquisitionMethodFile(method, destination);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
	
	private static void updateNamesForFixedMethods() throws Exception{
		
		//	Read all downloaded method folders
		File dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MSMS");		
		Collection<File> methodFolders = Stream.of(dirToScan.listFiles()).
				filter(file -> file.isDirectory()).collect(Collectors.toList());	      
		dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MS1");
		Collection<File> msOneMethodFolders = Stream.of(dirToScan.listFiles()).
				filter(file -> file.isDirectory()).collect(Collectors.toList());		
		methodFolders.addAll(msOneMethodFolders);
		
		//	Get all methods with un-assigned gradients from the database
		Collection<DataAcquisitionMethod> allMethods = 
				IDTDataCache.getAcquisitionMethods().stream().
					filter(m -> (Objects.isNull(m.getChromatographicGradient()) 
							|| !m.getChromatographicGradient().areMobilePhasesDefined())).
					filter(m -> m.getIonizationType().getId().equals("ESI")).
					filter(m -> (m.getMsType().getId().equals("HRMS")
							|| m.getMsType().getId().equals("HRMSMS"))).
					collect(Collectors.toList());
		
		Collection<String>updatedMethods = new TreeSet<String>();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET METHOD_NAME = ? WHERE ACQ_METHOD_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(DataAcquisitionMethod method : allMethods) {
			
			File methodFolder = methodFolders.stream().
					filter(f -> f.getName().equals(method.getName())).
					findFirst().orElse(null);
			if(methodFolder != null) {
				
				List<Path>realMethodFolderPaths = 
						FIOUtils.findDirectoriesByExtension(methodFolder.toPath(), "m");
				
				if(realMethodFolderPaths != null && !realMethodFolderPaths.isEmpty()) {
					
					File realMethodFolder = realMethodFolderPaths.get(0).toFile();
					if(!realMethodFolder.equals(methodFolder)) {
						
						ps.setString(1, realMethodFolder.getName());
						ps.setString(2, method.getId());
						ps.executeUpdate();
						System.out.println("Updated file for method " + method.getName() );
					}
				}
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void fixMethodFilesInDatabase() throws Exception{
		
		//	Read all downloaded method folders
		File dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MSMS");		
		Collection<File> methodFolders = Stream.of(dirToScan.listFiles()).
				filter(file -> file.isDirectory()).collect(Collectors.toList());	      
		dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MS1");
		Collection<File> msOneMethodFolders = Stream.of(dirToScan.listFiles()).
				filter(file -> file.isDirectory()).collect(Collectors.toList());		
		methodFolders.addAll(msOneMethodFolders);
		
		//	Get all methods with un-assigned gradients from the database
		Collection<DataAcquisitionMethod> allMethods = 
				IDTDataCache.getAcquisitionMethods().stream().
					filter(m -> (Objects.isNull(m.getChromatographicGradient()) 
							|| !m.getChromatographicGradient().areMobilePhasesDefined())).
					filter(m -> m.getIonizationType().getId().equals("ESI")).
					filter(m -> (m.getMsType().getId().equals("HRMS")
							|| m.getMsType().getId().equals("HRMSMS"))).
					collect(Collectors.toList());
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET METHOD_NAME = ? WHERE ACQ_METHOD_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		Collection<String>updatedMethods = new TreeSet<String>();
		for(DataAcquisitionMethod method : allMethods) {
			
			File methodFolder = methodFolders.stream().
					filter(f -> f.getName().equals(method.getName())).
					findFirst().orElse(null);
			if(methodFolder != null) {
				
				List<Path>realMethodFolderPaths = 
						FIOUtils.findDirectoriesByExtension(methodFolder.toPath(), "m");
				
				if(realMethodFolderPaths != null && !realMethodFolderPaths.isEmpty()) {
					
					File realMethodFolder = null;
					if(realMethodFolderPaths.size() == 1)
						realMethodFolder = realMethodFolderPaths.get(0).toFile();
					
					if(realMethodFolderPaths.size() > 1)
						realMethodFolder = realMethodFolderPaths.get(1).toFile();
					
					if(!realMethodFolder.equals(methodFolder)) {
						
						try {
							AcquisitionMethodUtils.updateAcquisitionMethod(method, realMethodFolder);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
						ps.setString(1, realMethodFolder.getName());
						ps.setString(2, method.getId());
						ps.executeUpdate();
						
						updatedMethods.add(method.getId());
						System.out.println("Updated file for method " + method.getName() );
					}
				}
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		Path logPath = 
				Paths.get(dirToScan.getParentFile().getAbsolutePath(), 
				"updated_methods2.txt");
		try {
			Files.write(logPath, 
					updatedMethods, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void extractMultipleGradients() throws Exception{
		
		File dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MSMS");
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));
		Collection<File> methodFolders = FileUtils.listFilesAndDirs(
				dirToScan,
				DirectoryFileFilter.DIRECTORY,
				dotMfilter);
		dirToScan = new File(
				"E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20240618\\MS1");
		Collection<File> msOneMethodFolders = FileUtils.listFilesAndDirs(
				dirToScan,
				DirectoryFileFilter.DIRECTORY,
				dotMfilter);
		methodFolders.addAll(msOneMethodFolders);
		
		Map<ChromatographicGradient,Collection<File>>gradientMethodMap = 
				new HashMap<ChromatographicGradient,Collection<File>>();
		for(File methodFolder : methodFolders) {
			
			if(!FilenameUtils.getExtension(methodFolder.getName()).equalsIgnoreCase("m"))
				continue;
			
			AgilentAcquisitionMethodParser amp = 
					new AgilentAcquisitionMethodParser(methodFolder);
			amp.parseParameterFiles();
			ChromatographicGradient grad = amp.extractGradientData();
			if(grad != null) {
				
				if(grad.getGradientSteps().isEmpty()) {
					System.err.println("No timetable in \"" + methodFolder.getName() + "\"");
					continue;
				}				
				MobilePhase[] gradMobilePhases = new MobilePhase[4];
				int mpCount = 0;
				for(int i=0; i<4; i++) {
					
					MobilePhase mp = grad.getMobilePhases()[i];
					if(mp != null) {
						
						MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(mp.getName());
						if(existing == null)
							System.err.println("Didn't find existing mobile phase for \"" 
									+ mp.getName() + "\" in \"" + methodFolder.getName() + "\"");
						else
							gradMobilePhases[i] = existing;
					}
					else
						gradMobilePhases[i] = null;
				}
				for(int i=0; i<4; i++) {
					grad.getMobilePhases()[i] = gradMobilePhases[i];
					if(grad.getMobilePhases()[i] != null)
						mpCount++;
				}
				if(mpCount == 0) {
					System.err.println("No mobile phases found in  \"" + methodFolder.getName() + "\"");
					continue;
				}
				ChromatographicGradient existingGrad = null;
				for(ChromatographicGradient g : gradientMethodMap.keySet()) {
					
					if(ChromatographicGradientUtils.gradientsEquivalent(g, grad)) {
						existingGrad = g;
						break;
					}
				}
				if(existingGrad == null) {
					gradientMethodMap.put(grad, new TreeSet<File>());
					gradientMethodMap.get(grad).add(methodFolder);
				}
				else {
					gradientMethodMap.get(existingGrad).add(methodFolder);
				}		
			}
			else {
				System.err.println("Failed to extract gradient from  \"" + methodFolder.getName() + "\"");
			}
		}
		for(ChromatographicGradient g : gradientMethodMap.keySet()) {
			
			try {
				ChromatographyDatabaseUtils.addNewChromatographicGradient(g);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//	Assign gradients to methods
		Connection conn = ConnectionManager.getConnection();
		String updQuery = 
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET GRADIENT_ID = ? WHERE ACQ_METHOD_ID = ?";
		PreparedStatement ps = conn.prepareStatement(updQuery);
		
		for(Entry<ChromatographicGradient, Collection<File>> pair : gradientMethodMap.entrySet()) {
			
			for(File f : pair.getValue()) {
				
				DataAcquisitionMethod method = IDTDataCache.getAcquisitionMethodByName(f.getName());
				if(method != null) {
					
					ps.setString(1, pair.getKey().getId());
					ps.setString(2, method.getId());
					ps.executeUpdate();
				}
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);

		System.out.println("***");
	}
	
	private static void gradTest() {

		File methodFolder = new File("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\"
				+ "AS_OF_20240618\\MS1\\QTOF-002-HILIC-27min-1mm-Flux-squishyInsert - A2B2-port1-6.m");
		AgilentAcquisitionMethodParser amp = 
				new AgilentAcquisitionMethodParser(methodFolder);
		amp.parseParameterFiles();
		ChromatographicGradient grad = amp.extractGradientData();
		if(grad != null) {
			
			MobilePhase[] gradMobilePhases = new MobilePhase[4];
			int mpCount = 0;
			for(int i=0; i<4; i++) {
				
				MobilePhase mp = grad.getMobilePhases()[i];
				if(mp != null) {
					
					MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(mp.getName());
					if(existing == null)
						System.err.println("Didn't find existing mobile phase for \"" + mp.getName() + "\"");
					else
						gradMobilePhases[i] = existing;
				}
				else
					gradMobilePhases[i] = null;
			}
			for(int i=0; i<4; i++) {
				grad.getMobilePhases()[i] = gradMobilePhases[i];
				if(grad.getMobilePhases()[i] != null)
					mpCount++;
			}
			if(mpCount == 0) {
				System.err.println("No mobile phases found in  \"" + methodFolder.getName() + "\"");
			}
		}
		else {
			System.err.println("Failed to extract gradient from  \"" + methodFolder.getName() + "\"");
		}
	}
	
	private static void testAgilentMethodParser() {
		
		File methodFolder = 
				new File ("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\"
						+ "AS_OF_20240618\\MSMS\\2.1 mm Gradient MS2 240 min.m");
		AgilentAcquisitionMethodParser amp = 
				new AgilentAcquisitionMethodParser(methodFolder);
		amp.parseParameterFiles();
		ChromatographicGradient grad = amp.extractGradientData();
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
			ChromatographicGradient grad = amp.extractGradientData();
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
