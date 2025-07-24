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

package edu.umich.med.mrc2.datoolbox.dmutils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class CoreDataBackupUtils {
	
	private static final Pattern limsFileNamePattern = 
			Pattern.compile("^\\d{8}-EX\\d{5}-A\\d{3}-IN\\d{4}-");
	
	private static final Pattern expIdPattern = Pattern.compile("EX0+(\\d+)");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {			
			File sourceDir = new File("K:\\DataAnalysis");
			File destinationDir = new File("J:\\Metabolomics-BRCF\\Shared\\_Reports");
			File batchFile = new File("E:\\DataAnalysis\\_BACKUP\\server2_to_corefs2_update_20250723.bat");
			File unmatchedDirsList = new File("E:\\DataAnalysis\\_BACKUP\\server2_to_corefs2_update_unmatched_dirs_20250723.txt");
			File copyLogDir = new File("F:\\DataAnalysis\\_COPY_LOGS");
			createRoboCopyUpdateScript(
					sourceDir, destinationDir, batchFile, unmatchedDirsList, copyLogDir);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	File reportsDir = new File("Y:\\DataAnalysis\\_Reports");
//	File logsDir = new File("E:\\DataAnalysis\\_BACKUP");			
//	compressAndDeleteCEFfiles(reportsDir, logsDir, 1392);	
	
	private static void compressAndDeleteCEFfiles(File reportsDir, File logDir, int startExpId) {
		
		Path zipLogPath = Paths.get(logDir.toPath().toString(), "cefCompressionFailedLog.txt");
		File[] expFiles = reportsDir.listFiles();
		Map<Path,Collection<Path>>cefDirMap = new TreeMap<Path,Collection<Path>>();
		for(File expFile : expFiles) {
			
			if(!expFile.getName().startsWith("EX0"))
				continue;
			
			int expId = getExperimentNumber(expFile.getName());
			if(expId < startExpId)
				continue;
			
			System.out.println("Processing " + expFile.toPath().toString());
			cefDirMap.clear();
			List<Path>cefFilesList = FIOUtils.findFilesByExtension(expFile.toPath(), "cef");
			if(!cefFilesList.isEmpty()) {
				
				for(Path cefFilePath : cefFilesList) {
					
					if(!cefDirMap.containsKey(cefFilePath.getParent())) 
						cefDirMap.put(cefFilePath.getParent(), new TreeSet<Path>());
					
					cefDirMap.get(cefFilePath.getParent()).add(cefFilePath);
				}
				for(Entry<Path, Collection<Path>> dirEntry : cefDirMap.entrySet()) {
					
					Path zipFilePath = Paths.get(dirEntry.getKey().toString(), "CEFFiles.zip");
					CompressionUtils.createZipFileFromMultipleFiles(zipFilePath, dirEntry.getValue());
					if(zipFilePath.toFile().exists()) {
						
						System.out.println("Saved  CEFFiles.zip to " + dirEntry.getKey().toString());
						dirEntry.getValue().stream().forEach(p -> p.toFile().delete());
						System.out.println("Deleted CEF files from " + dirEntry.getKey().toString());
					}
					else {
						String logLine = "CEF file compression failed in\t" + dirEntry.getKey().toString();
						try {
						    Files.write(zipLogPath, 
						            Collections.singleton(logLine),
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
	}
	
	private static void removeZippedAndConvertedFiles(File reportsDir, File logDir) {
		
		Path zipLogPath = Paths.get(logDir.toPath().toString(), "nonRawDataZips.txt");	
		File[] expFiles = reportsDir.listFiles();
		for(File expFile : expFiles) {
			
			if(!expFile.getName().startsWith("EX0"))
				continue;
			
			System.out.println("Processing " + expFile.toPath().toString());
			
			//	Remove zipped raw data files
			List<Path>zipFilesList = FIOUtils.findFilesByExtension(expFile.toPath(), "zip");
			if(!zipFilesList.isEmpty()) {
				
				for(Path zipFilePath : zipFilesList) {
					
					if(isLIMSFile(zipFilePath.getFileName().toString()))
						zipFilePath.toFile().delete();
					else {
						String logLine = zipFilePath.toString() +  "\t" + 
								Double.toString((double)zipFilePath.toFile().length() / 1024.0d / 1024.0d) + "MB";
						try {
						    Files.write(zipLogPath, 
						            Collections.singleton(logLine),
						            StandardCharsets.UTF_8,
						            StandardOpenOption.CREATE, 
						            StandardOpenOption.APPEND);
						} catch (IOException e) {
						    e.printStackTrace();
						}
					}
				}
			}
			// Remove MZML/MZXML files
			List<Path>convertedFilesList = FIOUtils.findFilesByExtension(expFile.toPath(), "mzml");
			convertedFilesList.addAll(FIOUtils.findFilesByExtension(expFile.toPath(), "mzxml"));
			if(!convertedFilesList.isEmpty()) {
				
				for(Path convertedFilePath : convertedFilesList) {
					
					if(isLIMSFile(convertedFilePath.getFileName().toString()))
						convertedFilePath.toFile().delete();
				}
			}
		}
	}
	
	public static boolean isLIMSFile(String fileName) {
		return limsFileNamePattern.matcher(fileName.trim()).find();
	}
		
	private static int getExperimentNumber(String exId) {
		
		int exNum = -1;
		Matcher regexMatcher = expIdPattern.matcher(exId);
		if(regexMatcher.find() && regexMatcher.groupCount() > 0)
			exNum = Integer.parseInt(regexMatcher.group(1));
		
		return exNum;
	}
	
	private static void createRoboCopyScript() {
		
		List<File>expDirs = new ArrayList<File>();
		try {
			expDirs = Files.list(Paths.get("Y:\\DataAnalysis\\_Reports"))
			        .map(Path::toFile).filter(f -> f.getName().startsWith("EX"))
			        .collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String>commands = new ArrayList<String>();
		for(File expDir : expDirs) {
			
			String command = "robocopy \"" + expDir.getAbsolutePath() + "\\ \" " + 
			"\"R:\\Metabolomics-BRCF\\Shared\\_Reports\\" + expDir.getName() + " \" /mir /mt:16 /tbd /r:1 /w:3 /fft /np";
			commands.add(command);
		}
		Path mspOutputPath = Paths.get("E:\\DataAnalysis\\Automation scripts & soft\\robocopy_20231007.bat");
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			Files.write(mspOutputPath, 
					commands, 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private static void createRoboCopyUpdateScript(
			File sourceDir, 
			File destinationDir, 
			File batchFile,
			File unmatchedDirsList,
			File copyLogDir) {
		
		List<File>expDirs = new ArrayList<File>();
		try {
			expDirs = Files.list(Paths.get(sourceDir.getAbsolutePath()))
			        .map(Path::toFile).filter(f -> f.getName().startsWith("EX"))
			        .collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<File>dirsToUpdate = new ArrayList<File>();
		try {
			dirsToUpdate = Files.list(Paths.get(destinationDir.getAbsolutePath()))
			        .map(Path::toFile).filter(f -> f.getName().startsWith("EX"))
			        .collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<File,File>matchingDirMap = new TreeMap<File,File>();
		List<File>unmatchedDirs = new ArrayList<File>();		
		for(File expDir : expDirs) {
			
			File dirMatch = dirsToUpdate.stream().
					filter(d -> d.getName().equals(expDir.getName())).
					findFirst().orElse(null);
			if(dirMatch == null)
				unmatchedDirs.add(expDir);
			else
				matchingDirMap.put(expDir, dirMatch);
		}
		List<String>commands = new ArrayList<String>();
		for(Entry<File,File>fe : matchingDirMap.entrySet()) {
			
			Path copyLogPath = Paths.get(copyLogDir.getAbsolutePath(), 
					FileNameUtils.getBaseName(fe.getKey().toPath()) + "-" + FIOUtils.getTimestamp() + ".log");
			String command = "call RoboCopy.exe \"" + fe.getKey().getAbsolutePath() 
					+ " \" \"" + fe.getValue().getAbsolutePath() + " \" /mir /fft /r:5 /w:0  /mt:20 /xx /xc /xn /xo  "
					+ "/log+:\"" + copyLogPath.toString() + "\" "
					+ "/nc /ns /np /ndl /nfl /tee";
			commands.add(command);
		}
	    try {
			Files.write(batchFile.toPath(), 
					commands, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    if(!unmatchedDirs.isEmpty()) {
	    	
	    	 List<String>unmatchedNames = 
	    			 unmatchedDirs.stream().map(f -> f.getAbsolutePath()).
	    			 collect(Collectors.toList());
	 	    try {
				Files.write(unmatchedDirsList.toPath(), 
						unmatchedNames, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			} 
	    }   
	}
	
	private static void createRMDIRScript() {
		
		List<File>expDirs = new ArrayList<File>();
		try {
			expDirs = Files.list(Paths.get("Y:\\DataAnalysis\\_Reports"))
			        .map(Path::toFile).filter(f -> f.getName().startsWith("EX"))
			        .collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String>commands = new ArrayList<String>();
		for(File expDir : expDirs) {
			
			String command = "rm -rf \"" + expDir.getAbsolutePath() +"\"";
			commands.add(command);
		}
		Path mspOutputPath = Paths.get("E:\\DataAnalysis\\Automation scripts & soft\\rmdir_20231007.bat");
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			Files.write(mspOutputPath, 
					commands, 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
