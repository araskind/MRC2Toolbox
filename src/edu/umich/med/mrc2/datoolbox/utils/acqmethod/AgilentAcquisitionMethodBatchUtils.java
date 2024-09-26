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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.IonizationType;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.ChromatographyDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class AgilentAcquisitionMethodBatchUtils {
	
	/**
	 * Recursively find AcqMethod.xml files and copy them to the destination folder
	 * with renaming to actual method name
	 * 
	 * AcqMethod.xml file contains the actual acquisition method parameters. 
	 * It is stored inside the AcqData folder of Agilent .D raw data folder
	 * together with the data acquisition method folder with .m extension.
	 * AcqMethod.xml is copied to destination folder and renamed to the base name 
	 * of the method folder (name without extension) with .xml extension.
	 * The list of existing method names is maintained, so only new methods are copied.
	 * Methods are compared by name (case-insensitive), so if different methods have the same name
	 * they are considered identical.
	 * Locations of the actual method files in the file system are saved to log
	 * for future reference
	 * 
	 * @param reportFolder - folder containing Agilent data files (.D), may be nested
	 * @param destination - destination to copy acquisition method reports in XML format
	 * @param methodLocationsLog - method locations log file
	 */
	public static void collectUniqueAcquisitionMethodReports(
			File reportFolder, 
			File destination,
			File methodLocationsLog) {
		
		Set<String>foundMethods = new TreeSet<String>();
		for(String methodFileName : destination.list())
			foundMethods.add(FilenameUtils.getBaseName(methodFileName).toLowerCase() +".m");
		
		for(File f : reportFolder.listFiles()) {
			
			if(!f.isDirectory())
				continue;
			
			List<Path> amdirList = FIOUtils.findDirectoriesByName(f.toPath(), "AcqData");
			if(amdirList.isEmpty())
				continue;
			
			System.out.println("Processing " + f.getName());				
			
			for(Path p : amdirList) {
				
				File methodXMLreport = null;
				File methodFile = null;
				
				for(File fl : p.toFile().listFiles()) {
					
					if(fl.getName().equalsIgnoreCase("AcqMethod.xml"))
						methodXMLreport = fl;
					
					if(fl.isDirectory() && FilenameUtils.getExtension(fl.getName()).equalsIgnoreCase("m"))
						methodFile = fl;
				}
				if(methodFile != null 
						&& methodXMLreport != null 
						&& !foundMethods.contains(methodFile.getName().toLowerCase())) {
					
					foundMethods.add(methodFile.getName().toLowerCase());
					Path xmlReportPath = Paths.get(destination.getAbsolutePath(), 
							FilenameUtils.getBaseName(methodFile.getName()) + ".xml");
					
					if(!xmlReportPath.toFile().exists()) {
						
						try {
							Files.copy(methodXMLreport.toPath(), xmlReportPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						Files.writeString(methodLocationsLog.toPath(), 
								methodFile.getName()+ "\t" + methodFile.getAbsolutePath() + "\n", 
								StandardCharsets.UTF_8,
								StandardOpenOption.CREATE, 
								StandardOpenOption.APPEND);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}			
		}
	}
	
	/**
	 * Extract RCDevicesXml and SCICDevicesXml as new XML documents 
	 * from previously collected AcqMethod.xml files
	 * The function assumes that specific folder structure is in place
	 * inside the specified methodsFolder:
	 * <UL>
	 * <LI>Original method reports copied from AcqMethod.xml are in MethodReports folder
	 * <LI>RCDevicesXml and SCICDevicesXml folders should be present
	 * </UL>
	 * If RCDevicesXml or SCICDevicesXml are not found in method files
	 * the log file nonConformingMethods.txt will be created in methodsFolder 
	 * listing these methods
	 * 
	 * @param methodsFolder
	 */
	public static void extractEmbededXmlFromAcqMethodReport(File methodsFolder) {
		
		Path methodReportsFolder = Paths.get(methodsFolder.getAbsolutePath(), "MethodReports");
		String rcDevicesDir = "RCDevicesXml";
		String scicDevicesDir = "SCICDevicesXml";
		ArrayList<String>log = new ArrayList<String>();
		List<Path> methodReportsList = FIOUtils.findFilesByExtension(methodReportsFolder, "xml");
		for(Path mrPath : methodReportsList) {
			
			File reportFile = mrPath.toFile();
			Document methodDocument = XmlUtils.readXmlFile(reportFile);
			Namespace ns = methodDocument.getRootElement().getNamespace();
			Element mr = methodDocument.getRootElement().getChild("MethodReport", ns);
			if(mr == null) {
				log.add(reportFile.getName());
				continue;
			}
			Element rcDevicesXmlElement = mr.getChild("RCDevicesXml", ns);
			if(rcDevicesXmlElement == null 
					|| rcDevicesXmlElement.getText() == null
					|| rcDevicesXmlElement.getText().isEmpty()) {
				log.add(reportFile.getName());
				continue;
			}			
			String rcDevicesXml = 
					StringEscapeUtils.unescapeXml(rcDevicesXmlElement.getText());
			Path outputPath = Paths.get(
					methodsFolder.getAbsolutePath(), rcDevicesDir, reportFile.getName());
			try {
				Files.writeString(outputPath, 
						rcDevicesXml, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Element scicDevicesXmlElement = mr.getChild("RCDevicesXml", ns);
			if(scicDevicesXmlElement == null 
					|| scicDevicesXmlElement.getText() == null
					|| scicDevicesXmlElement.getText().isEmpty()) {
				log.add(reportFile.getName() + "\t" + "No SCICDevicesXml Element");
				continue;
			}			
			String scicDevicesXml = 
					StringEscapeUtils.unescapeXml(rcDevicesXmlElement.getText());
			outputPath = Paths.get(
					methodsFolder.getAbsolutePath(), scicDevicesDir, reportFile.getName());
			try {
				Files.writeString(outputPath, 
						scicDevicesXml, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!log.isEmpty()){
			
			Path logPath = 
					Paths.get(methodsFolder.getAbsolutePath(), "nonConformingMethods.txt");
			try {
				Files.write(logPath, 
						log,
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	/**
	 * Create method to mobile phase mapping using RCDevicesXml documents.<BR>
	 * Output is a file listing the method and all found mobile phases 
	 * in tab-separated format.<BR>
	 * Only methods already in database are considered (matched by name)<BR>
	 * Found mobile phases are NOT matched to the database, this is just 
	 * a first-path exploratory list when batch-processing the methods
	 * 
	 * @param rcDevicesXmlFolder - folder containing RCDevicesXml documents to scan
	 * @param methodMobilePhaseMap - output mapping file
	 */
	public static void createMobilePhaseNameAndSynonymListByMethod(
			File rcDevicesXmlFolder,
			File methodMobilePhaseMap) {

		ArrayList<String>log = new ArrayList<String>();
		List<Path> chromMethodList = 
				FIOUtils.findFilesByExtension(rcDevicesXmlFolder.toPath(), "xml");
		ArrayList<String>line = new ArrayList<String>();
		for(Path mrPath : chromMethodList) {

			String acqName = FileNameUtils.getBaseName(mrPath.toString()) + ".m";
			DataAcquisitionMethod acqMethod = 
					IDTDataCache.getAcquisitionMethodByName(acqName);
			if(acqMethod == null)
				continue;
					
			File methodFile = mrPath.toFile();
			ExtractedAgilentAcquisitionMethodParser parser = 
					new ExtractedAgilentAcquisitionMethodParser(methodFile);
			parser.setDoNotMatchMobilePhases(true);
			ChromatographicGradient grad = 
					parser.extractChromatographicGradientFromFile();
			if(grad != null) {
				
				line.add(acqName);
				for(MobilePhase mp : grad.getMobilePhases()) {
					
					line.clear();
					if(mp != null) {
						line.add(acqName);
						line.add(mp.getName());
						if(!mp.getSynonyms().isEmpty())
							line.addAll(mp.getSynonyms());
						
						log.add(StringUtils.join(line, "\t"));
					}
				}
			}
		}
		if (!log.isEmpty()) {

			try {
				Files.write(methodMobilePhaseMap.toPath(), 
						log, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create the list of mobile phases not mapping to those already in database 
	 * using RCDevicesXml documents.
	 * 
	 * @param rcDevicesXmlFolder - folder containing RCDevicesXml documents to scan
	 * @param unknownListFile - output file
	 */
	public static void extractUnknownMobilePhaseList(
			File rcDevicesXmlFolder,
			File unknownListFile) {

		ArrayList<String>log = new ArrayList<String>();
		List<Path> chromMethodList = 
				FIOUtils.findFilesByExtension(rcDevicesXmlFolder.toPath(), "xml");
		String unkLine = "Unknown mobile phase";
		for(Path mrPath : chromMethodList) {

			String acqName = FileNameUtils.getBaseName(mrPath.toString()) + ".m";
			DataAcquisitionMethod acqMethod = 
					IDTDataCache.getAcquisitionMethodByName(acqName);
			if(acqMethod == null)
				continue;
					
			File methodFile = mrPath.toFile();
			ExtractedAgilentAcquisitionMethodParser parser = 
					new ExtractedAgilentAcquisitionMethodParser(methodFile);
			ChromatographicGradient grad = 
					parser.extractChromatographicGradientFromFile();
			if(!parser.getErrorLog().isEmpty()) {
				
				for(String logLine : parser.getErrorLog()) {
					
					if(logLine.contains(unkLine))
						log.add(unkLine);					
				}
			}
		}
		if (!log.isEmpty()) {

			try {
				Files.write(unknownListFile.toPath(), 
						log, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Copy method files not present in the database to a single directory
	 * Methods are compared by name (case-insensitive)
	 * 
	 * @param methodLocationLog - method location list, tab separated file with 2 columns: 
	 * method file name and method path in the local file system
	 * @param destination - folder where to copy new method files
	 */
	public static void collectMissingMethods(
			File methodLocationLog, 
			File destination) {
		
		Map<String,String> methodLocationMap = 
				getMethodPropertyMap(methodLocationLog);
		
		for(String mName : methodLocationMap.keySet()) {
			
			DataAcquisitionMethod existing = 
					IDTDataCache.getAcquisitionMethodByName(mName);
			
			if(existing == null) {
				//	System.err.println(mName + "\t" + methodLocationMap.get(mName));
				Path methodPath = Paths.get(destination.getAbsolutePath(), mName);
				if(!methodPath.toFile().exists()) {
					
					try {
						FileUtils.copyDirectory(
								Paths.get(methodLocationMap.get(mName)).toFile(), 
								methodPath.toFile());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}		
	}
	
	/**
	 * Create method name/method property map from file.<BR>
	 * Method name is converted to lower case to avoid ambiguity. 
	 * Mapping file is a tab-separated plain text document with two columns:
	 * <UL>
	 * 	<LI>Method name
	 * 	<LI>Property value
	 * </UL>
	 * 
	 * @param methodPropertyFile
	 * @return methodPropertyMap - key is method name, value - method property
	 */
	public static Map<String,String> getMethodPropertyMap(File methodPropertyFile) {
		
		Set<String> methodProperties = new TreeSet<String>();		
		try{
			methodProperties = Files.lines(methodPropertyFile.toPath()).
					collect(Collectors.toCollection(TreeSet::new));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
		Map<String,String> methodPropertyMap = new TreeMap<String,String>();
		for(String ml : methodProperties) {
			
			String[]mla = ml.split("\t");
			if(mla.length >= 2) 
				methodPropertyMap.put(mla[0].toLowerCase(), mla[1]);			
		}
		return methodPropertyMap;
	}
	
	/**
	 * Map methods to mass analyzer types extracting information from RCDevicesXml
	 * RCDevicesXml is an XML element in AcqMethod.xml file that contains 
	 * an XML document with the actual method data with RC standing for RapidControl
	 * 
	 * @param rcDevicesDir - directory containing RCDevicesXml documents for methods
	 * @param instrumentTypesFile - mapping file, tab-separated; column 1 - method name, column 2 - instrument type
	 */
	public static void extractMassAnalyzerTypesFromRCDevicesXmlFiles(
			File rcDevicesDir,
			File instrumentTypesFile) {

		ArrayList<String>log = new ArrayList<String>();
		List<Path> chromMethodList = 
				FIOUtils.findFilesByExtension(rcDevicesDir.toPath(), "xml");
		
		for(Path mrPath : chromMethodList) {
			
			String acqName = FileNameUtils.getBaseName(mrPath.toString()) + ".m";
			File methodFile = mrPath.toFile();
			ExtractedAgilentAcquisitionMethodParser parser = 
					new ExtractedAgilentAcquisitionMethodParser(methodFile);
			String instrumentType = parser.extractInstrumentTypeFromFile();
			if(instrumentType != null && !instrumentType.trim().isEmpty())
				log.add(acqName + "\t" + instrumentType);
		}
		if (!log.isEmpty()) {
			try {
				Files.write(instrumentTypesFile.toPath(), 
						log, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Batch upload new acquisition methods to the database
	 * The function assumes that all methods are in the same folder
	 * and requires method locations mapping file and method
	 * to mass analyzer mapping file. It is using generic AUTO user ID (U00077)
	 * and sets a few default parameters:
	 * <UL>
	 * 	<LI>Ionization Type - ESI
	 * 	<LI>Chromatographic Separation Type - UPLC
	 *  <LI>Control software - MassHunter Acquisition
	 * </UL>
	 * Method names are checked against existing to avoid uploading duplicates
	 * 
	 * @param methodsDir - directory containing methods to upload
	 * @param methodMassAnalyzerMapFile - method name to mass analyzer type mapping file
	 * @param methodLocationsMapFile - method name to method location mapping file
	 */
	public static void uploadMissingMethods(
			File methodsDir,
			File methodMassAnalyzerMapFile,
			File methodLocationsMapFile) {
		
		Collection<File> methodFolders = 
				getListOfMethodFilesInFolder(methodsDir);			
		Map<String,MassAnalyzerType>methodMassAnalyzerMap = 
				getMethodMassAnalyzerMap(methodMassAnalyzerMapFile);
		Map<String,String> methodLocationMap = 
				getMethodPropertyMap(methodLocationsMapFile);

		LIMSUser user = LIMSDataCache.getUserById("U00077");
		IonizationType ionizationType = 
				IDTDataCache.getIonizationTypeById("ESI");
		ChromatographicSeparationType chSepType = 
				IDTDataCache.getChromatographicSeparationTypeById("UPLC");
		DataProcessingSoftware software = IDTDataCache.getSoftwareById("SW0010");
		
		for(File methodFolder : methodFolders) {
			
			if(!FilenameUtils.getExtension(methodFolder.getName()).equalsIgnoreCase("m"))
				continue;
			
			DataAcquisitionMethod existingMethod = 
					IDTDataCache.getAcquisitionMethodByName(methodFolder.getName());
			if(existingMethod != null) {
				
				System.err.println("Method \"" + methodFolder.getName() + "\" alredy in database");
				continue;
			}			
			DataAcquisitionMethod newMethod = new DataAcquisitionMethod(
					null,
					methodFolder.getName(),
					null,
					user,
					new Date());
			
			Polarity polarity = guessPolarityFromFilePath(
					methodLocationMap.get(methodFolder.getName().toLowerCase()));

			newMethod.setPolarity(polarity);
			newMethod.setIonizationType(ionizationType);
			newMethod.setSeparationType(chSepType);
			newMethod.setSoftware(software);
			newMethod.setMassAnalyzerType(
					methodMassAnalyzerMap.get(methodFolder.getName().toLowerCase()));	
			
			MsType msType = guessMsType(
					newMethod.getMassAnalyzerType(), methodFolder.getName());
			newMethod.setMsType(msType);
					
			try {
				AcquisitionMethodUtils.addNewAcquisitionMethod(newMethod, methodFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(methodFolder.getName() + " uploaded");
		}
	}
	
	/**
	 * Guess method polarity from path to the method inside 
	 * raw data folder (.D)
	 * It assumes convention to include polarity 
	 * when naming data files and experiment folders
	 * Agilent XML method files for Q-TOF are not available
	 * and for QQQ have polarity in individual segments
	 * 
	 * @param path - path to method folder inside the .D file
	 * @return Polarity - method polarity
	 */
	public static Polarity guessPolarityFromFilePath(String path) {
		
		if(path == null || path.isEmpty())
			return null;
		
		Polarity pol = null;
		String[]locationParts = path.split("\\\\");
		for(String part : locationParts) {
			
			if(part.equalsIgnoreCase("NEG"))
				pol = Polarity.Negative;
			
			if(part.equalsIgnoreCase("POS"))
				pol = Polarity.Positive;
			
			if(pol == null) {
				
				if(part.endsWith("-N.d") || part.endsWith("-N.D"))
					pol = Polarity.Negative;
				
				if(part.endsWith("-P.d") || part.endsWith("-P.D"))
					pol = Polarity.Positive;
			}
		}		
		return pol;
	}
	
	/**
	 * Crude function to guess MS type from mass analyzer type and name of the method
	 * @param massAnalyzer
	 * @param methodName
	 * @return MsType
	 */
	public static MsType guessMsType(
			MassAnalyzerType massAnalyzer, 
			String methodName) {
		
		if(massAnalyzer == null)
			return null;
		
		if(massAnalyzer.getId().equals("QQQ"))
			return IDTDataCache.getMsTypeById("MRM");
		
		if(massAnalyzer.getId().equals("QTOF")) {
			
			if(methodName.toUpperCase().contains("MSMS"))
				return IDTDataCache.getMsTypeById("HRMSMS");
			else
				return IDTDataCache.getMsTypeById("HRMS");
		}		
		return null;
	}
	
	/**
	 * List all Agilent method (.m) files inside the specified directory 
	 * and its subdirectories
	 * 
	 * @param dirToScan
	 * @return
	 */
	public static Collection<File>getListOfMethodFilesInFolder(File dirToScan) {
		
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));
		Collection<File> methodFolders = FileUtils.listFilesAndDirs(
				dirToScan,
				DirectoryFileFilter.DIRECTORY,
				dotMfilter);
		
		return methodFolders;
	}
	
	/**
	 * Create method name to mass analyzer type map from the mapping file
	 * Mapping file is a tab-separated plain text document with two columns:
	 * <UL>
	 * 	<LI>Method name
	 * 	<LI>Mass analyzer abbreviation
	 * </UL>
	 * The following abbreviations are supported:
	 * <UL>
	 * 	<LI>Q - Sigle quadrupol
	 * 	<LI>QQQ - Tripple quadrupol
	 * 	<LI>QTOF - Quadrupol-time of flight
	 * 	<LI>IT - Quadrupole ion trap
	 * 	<LI>TOF - Time of flight
	 * 	<LI>ORBI - Orbitrap
	 * 	<LI>FTICR - Fourier transform ion cyclotron resonance
	 * 	<LI>TRIBRID - Tribrid
	 * </UL>
	 * @param massAnalyzerMapFile
	 * @return
	 */
	public static Map<String,MassAnalyzerType> getMethodMassAnalyzerMap(File massAnalyzerMapFile) {
		
		Map<String,String>mmaMap = getMethodPropertyMap(massAnalyzerMapFile);
		Map<String,MassAnalyzerType> methodMassAnalyzerMap = 
				new TreeMap<String,MassAnalyzerType>();
		for(Entry<String, String> pair : mmaMap.entrySet()) {

			MassAnalyzerType massAnalyzer = 
					IDTDataCache.getMassAnalyzerTypeById(pair.getValue());
			if(massAnalyzer != null)
				methodMassAnalyzerMap.put(pair.getKey().toLowerCase(), massAnalyzer);
		}
		return methodMassAnalyzerMap;
	}	
	
	/**
	 * Batch extract and upload into the database actual gradients
	 * as recorded in AcqMethod.xml files. Gradients are uploaded 
	 * only for methods already in database (matched by name) and
	 * only if data from RCDevicesXml document are successfully parsed
	 * into the gradient including matching mobile phases. 
	 * All gradient parsing errors are saved to log file.
	 * 
	 * @param rcDevicesXmlFolder
	 * @param logFile
	 */
	public static void extractAndUploadActualGradients(
			File rcDevicesXmlFolder,
			File logFile) {
		
		ArrayList<String>log = new ArrayList<String>();
		List<Path> chromMethodList = FIOUtils.findFilesByExtension(
				rcDevicesXmlFolder.toPath(), "xml");
		
		for(Path mrPath : chromMethodList) {
			
			File methodFile = mrPath.toFile();
			
//			if(methodFile.getName().equalsIgnoreCase("pos-lc-tof-2018-460ul-min.xml"))
//				System.out.println("***");
				
			String acqName = FileNameUtils.getBaseName(mrPath.toString()) + ".m";
			DataAcquisitionMethod acqMethod = 
					IDTDataCache.getAcquisitionMethodByName(acqName);
			if(acqMethod == null) {
				log.add(acqName + "\tNot in database");
				continue;
			}	
			if(acqMethod.getChromatographicGradient() != null) {
				
				ChromatographicGradient existingGrad = IDTDataCache.getChromatographicGradientById(
						acqMethod.getChromatographicGradient().getId());
				
				if(existingGrad != null) {
					log.add(acqName + "\thas assigned gradient " 
							+ acqMethod.getChromatographicGradient().getId());
					continue;
				}
			}
			ExtractedAgilentAcquisitionMethodParser parser = 
					new ExtractedAgilentAcquisitionMethodParser(methodFile);
			ChromatographicGradient grad = 
					parser.extractChromatographicGradientFromFile();
			if(grad == null) {
				log.add(methodFile.getName() + "\tFailed to extract gradient");
				continue;
			}
			else if(!parser.getErrorLog().isEmpty()) { 
				log.addAll(parser.getErrorLog());
				continue;
			}
			else {
				String gradId = null;
				try {
					gradId = ChromatographyDatabaseUtils.addNewChromatographicGradient(grad);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(gradId != null) {
					
					try {
						ChromatographyDatabaseUtils.setGradientForMethod(gradId, acqMethod.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (!log.isEmpty()) {

			try {
				Files.write(logFile.toPath(), 
						log, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Extract method descriptions from RCDevicesXml
	 * RCDevicesXml is an XML element in AcqMethod.xml file that contains 
	 * an XML document with the actual method data with RC standing for RapidControl
	 * 
	 * @param rcDevicesDir - directory containing RCDevicesXml documents for methods
	 * @param descriptionsFile - mapping file, tab-separated; column 1 - method name, column 2 - method description
	 */
	public static void extractMethodDescriptions(
			File rcDevicesDir,
			File descriptionsFile) {

		ArrayList<String>log = new ArrayList<String>();
		List<Path> chromMethodList = FIOUtils.findFilesByExtension(
				rcDevicesDir.toPath(), "xml");
		
		for(Path mrPath : chromMethodList) {
			
			String acqName = FileNameUtils.getBaseName(mrPath.toString()) + ".m";
			File methodFile = mrPath.toFile();
			ExtractedAgilentAcquisitionMethodParser parser = 
					new ExtractedAgilentAcquisitionMethodParser(methodFile);
			String description = parser.extractMethodDescriptionFromFile();
			if(description != null && !description.trim().isEmpty())
				log.add(acqName + "\t" + description);
		}
		if (!log.isEmpty()) {

			try {
				Files.write(descriptionsFile.toPath(), 
						log, 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Update method descriptions in the database using external mapping file.<BR>
	 * Methods are matched by name, descriptions are updated if mapping contains
	 * description and there is no existing description or it equals method name.
	 * If there is existing description other than metod name, new description is appended to it.
	 * 
	 * @param methodDescriptionsFile
	 * @throws Exception
	 */
	public static void updateMethodDescriptions(File methodDescriptionsFile) throws Exception {
		
		Map<String,String> methodDescriptionsMap = 
				getMethodPropertyMap(methodDescriptionsFile);
		Collection<DataAcquisitionMethod> methodList = 
				IDTDataCache.getAcquisitionMethods();
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
				"UPDATE DATA_ACQUISITION_METHOD "
				+ "SET METHOD_DESCRIPTION = ? WHERE ACQ_METHOD_ID = ?";		
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(DataAcquisitionMethod method : methodList) {
			
			String forUpdate = null;
			String newDescription = methodDescriptionsMap.get(method.getName().toLowerCase());
			if(newDescription != null && !newDescription.trim().isEmpty()) {

				if(method.getDescription() == null || method.getDescription().isEmpty() 
						|| (method.getDescription() != null 
								&& method.getDescription().equalsIgnoreCase(method.getName()))) {
					forUpdate = newDescription;
				}
				else {
					forUpdate = method.getDescription() + "; " + newDescription;
				}
				ps.setString(1, forUpdate);
				ps.setString(2, method.getId());
				ps.executeUpdate();
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void reassignTemporaryGradientsAsPermanent() throws Exception {
		
		Map<String,String> methodIdTmpGradIdMap = new TreeMap<String,String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT ACQ_METHOD_ID, TMP_GRADIENT_ID " +
				"FROM DATA_ACQUISITION_METHOD " +
				"WHERE TMP_ACTUAL_GRADIENT_ID IS NULL " +
				"AND TMP_GRADIENT_ID IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			methodIdTmpGradIdMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();
		
		query = "SELECT ACQ_METHOD_ID, TMP_ACTUAL_GRADIENT_ID " +
				"FROM DATA_ACQUISITION_METHOD  " +
				"WHERE TMP_ACTUAL_GRADIENT_ID IS NOT NULL " +
				"AND TMP_GRADIENT_ID IS NULL";
		ps = conn.prepareStatement(query);

		rs = ps.executeQuery();
		while(rs.next()) 
			methodIdTmpGradIdMap.put(rs.getString(1), rs.getString(2));
		
		rs.close();	
		
		query = "UPDATE DATA_ACQUISITION_METHOD SET GRADIENT_ID = ? "
				+ "WHERE ACQ_METHOD_ID = ?";
		ps = conn.prepareStatement(query);	
		
		Collection<ChromatographicGradient> tmpGradList = 
				ChromatographyDatabaseUtils.getTempChromatographicGradientList();
		
		for(Entry<String, String> pair : methodIdTmpGradIdMap.entrySet()) {
			
			ChromatographicGradient tmpGrad = 
					tmpGradList.stream().
					filter(g -> g.getId().equals(pair.getValue())).
					findFirst().orElse(null);
			if(tmpGrad != null) {
				
				String gradId = 
						ChromatographyDatabaseUtils.addNewChromatographicGradient(tmpGrad);
				ps.setString(1, gradId);
				ps.setString(2, pair.getKey());
				ps.executeUpdate();
			}
		}
		ps.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addMissingGradientsUsingExternalMobilePhaseAndColumnKey(
			File rcDevicesXmlFolder,
			File externalKeyFile) {
		
		Collection<DataAcquisitionMethod>methodsToUpdate = 
				getMethodsWithMissingGradientData();
		
		List<Path> chromMethodList = 
				FIOUtils.findFilesByExtension(rcDevicesXmlFolder.toPath(), "xml");
		Set<String>availableXml = new TreeSet<String>();
		for(Path xmlPath : chromMethodList) {
			
			String methodName = FileNameUtils.getBaseName(xmlPath.toString()).toLowerCase() + ".m";
			availableXml.add(methodName);
		}
		List<String>missingXml = methodsToUpdate.stream().
			map(m -> m.getName().toLowerCase()).
			filter(n -> !availableXml.contains(n)).
			collect(Collectors.toList());
		
		if(!missingXml.isEmpty()) {
			
			for(String m : missingXml)
				System.err.println(m);
		}
	}
	
	public static Collection<DataAcquisitionMethod>getMethodsWithMissingGradientData(){
		
		Collection<ChromatographicGradient> existingGradients = 
				IDTDataCache.getChromatographicGradientList();
		Set<String> gradIds = existingGradients.stream().
				map(g -> g.getId()).collect(Collectors.toSet());
		Collection<DataAcquisitionMethod> methods = IDTDataCache.getAcquisitionMethods();
		Collection<DataAcquisitionMethod>methodsWithMissingGradientData = methods.stream().
			filter(m-> Objects.nonNull(m.getSoftware())).
			filter(m-> m.getSoftware().getId().equals("SW0010")).
			filter(m-> Objects.nonNull(m.getChromatographicGradient())).
			filter(m -> !gradIds.contains(m.getChromatographicGradient().getId())).
			collect(Collectors.toList());
		
		return methodsWithMissingGradientData;
	}
	
	public static void copyNewMethodReports(File reportDir, File destination) {
		
		List<Path> chromMethodList = FIOUtils.findFilesByExtension(
				reportDir.toPath(), "xml");
		
		for(Path xmlReportPath : chromMethodList) {
			
			String methodName = FileNameUtils.getBaseName(
					xmlReportPath.toString()).toLowerCase() + ".m";
			DataAcquisitionMethod existingMethod = 
					IDTDataCache.getAcquisitionMethodByName(methodName);
			if(existingMethod == null) {

				Path newXmlReportPath = 
						Paths.get(destination.getAbsolutePath(), xmlReportPath.toFile().getName());
				try {
					Files.copy(xmlReportPath, newXmlReportPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
	
	public static void extractGradientsFromMethodFiles(File tmpFolder, File logFile){
		
		Collection<String>logData = new ArrayList<String>();
		Collection<DataAcquisitionMethod>methodsToUpdate = 
				getMethodsWithMissingGradientData();

		for(DataAcquisitionMethod method : methodsToUpdate) {
			
			try {
				AcquisitionMethodUtils.getAcquisitionMethodFile(method, tmpFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File[]downloaded = tmpFolder.listFiles(File::isDirectory);
			if(downloaded.length == 0) {
				logData.add(method.getId() + "\t" + method.getName() + "\tNo method file");
				try {
					FileUtils.cleanDirectory(tmpFolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			File methodFolder = downloaded[0];
			if(!methodFolder.isDirectory()) {
				logData.add(method.getId() + "\t" + method.getName() + "\tMethod is not directory");
				try {
					FileUtils.cleanDirectory(tmpFolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			List<Path>realMethodFolderPaths = 
					FIOUtils.findDirectoriesByExtension(methodFolder.toPath(), "m");
			
			if(realMethodFolderPaths != null && !realMethodFolderPaths.isEmpty()) {
				
				//	Extract gradient and save as temporary
				AgilentAcquisitionMethodParser amp = 
						new AgilentAcquisitionMethodParser(methodFolder);
				amp.parseParameterFiles();
				ChromatographicGradient grad = amp.extractGradientData();
				if(grad != null) {					
					if(grad.getGradientSteps().isEmpty()) {
						logData.add(method.getId() + "\t" + method.getName() + "\tNo time table");
						try {
							FileUtils.cleanDirectory(tmpFolder);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
						try {
							FileUtils.cleanDirectory(tmpFolder);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
					for(int i=0; i<4; i++)
						grad.setMobilePhase(gradMobilePhases[i], i);
										
					if(!grad.areMobilePhasesDefined()) {
						logData.add(method.getId() + "\t" + method.getName() + "\tNo mobile phases found");
						try {
							FileUtils.cleanDirectory(tmpFolder);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
					//	Upload temp gradient for method		
//					try {
//						ChromatographyDatabaseUtils.addTmpChromatographicGradientForAcqMethod(grad, method.getId(), false);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
				else {
					logData.add(method.getId() + "\t" + method.getName() + "\tFailed to extract gradient");
					try {
						FileUtils.cleanDirectory(tmpFolder);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}			
			}	
			try {
				FileUtils.cleanDirectory(tmpFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//	Save Log file
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
	
	public static void saveGradientToTextFile(
			ChromatographicGradient gradient, File destination) {
		
		String[]mpNames = new String[] {"A","B","C","D"};
		
		ArrayList<String>gradientData = new ArrayList<String>();
		if(gradient.getName() != null && !gradient.getName().isEmpty())
			gradientData.add(gradient.getName());
		
		if(gradient.getDescription() != null && !gradient.getDescription().isEmpty())
			gradientData.add(gradient.getDescription());
		
		if(gradient.getColumnCompartmentTemperature() > 0.0d)
			gradientData.add("Column compartment temperature: " +
					MRC2ToolBoxConfiguration.getPpmFormat().format(
							gradient.getColumnCompartmentTemperature()) + "C");
		
		for(int i=0; i<4; i++) {
			
			if(gradient.getMobilePhases()[i] != null) {
				gradientData.add("Channel " + mpNames[i] + ": " 
						+ gradient.getMobilePhases()[i].getName());
			}
		}
		ArrayList<String>header = new ArrayList<String>();
		header.add("Start time");
		header.add("Flow rate");
		header.add("%A");
		header.add("%B");
		header.add("%C");
		header.add("%D");
		gradientData.add(StringUtils.join(header, "\t"));
		ArrayList<String>line = new ArrayList<String>();
		for(ChromatographicGradientStep step : gradient.getGradientSteps()) {
			
			line.clear();
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(step.getStartTime()));
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(step.getFlowRate()));
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(step.getMobilePhaseStartingPercent()[0]));
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(step.getMobilePhaseStartingPercent()[1]));
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(step.getMobilePhaseStartingPercent()[2]));
			line.add(MRC2ToolBoxConfiguration.getPpmFormat().format(step.getMobilePhaseStartingPercent()[3]));
			gradientData.add(StringUtils.join(line, "\t"));
		}		
		try {
			Files.write(destination.toPath(),
					gradientData, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}























	
