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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CoreDataBackupUtils {

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
		//	createRoboCopyScript();
			createRMDIRScript();			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
