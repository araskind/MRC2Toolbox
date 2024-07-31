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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import edu.umich.med.mrc2.datoolbox.data.IonizationType;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

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
		Collection<DataAcquisitionMethod> existingMethods = 
				IDTDataCache.getAcquisitionMethods();
		for(String mName : methodLocationMap.keySet()) {
			
			DataAcquisitionMethod existing = existingMethods.stream().
					filter(m -> m.getName().equalsIgnoreCase(mName)).
					findFirst().orElse(null);
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
	 * Create method name/method property map from existing list
	 * Method name is converted to lower case to avoid ambiguity
	 * 
	 * @param methodPropertyFile - method property list, tab separated file with 2 columns: 
	 * method file name and method property
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
		List<String> chromMethodList = 
				FIOUtils.findFilesByExtension(rcDevicesDir.toPath(), "xml");
		
		for(String mrName : chromMethodList) {
			
			String acqName = FileNameUtils.getBaseName(mrName) + ".m";
			File methodFile = new File(mrName);
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
		
		//	TODO check if method already in database
		for(File methodFolder : methodFolders) {
			
			if(!FilenameUtils.getExtension(methodFolder.getName()).equalsIgnoreCase("m"))
				continue;
			
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
	
	public static Collection<File>getListOfMethodFilesInFolder(File dirToScan) {
		
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));
		Collection<File> methodFolders = FileUtils.listFilesAndDirs(
				dirToScan,
				DirectoryFileFilter.DIRECTORY,
				dotMfilter);
		
		return methodFolders;
	}
	
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
}
