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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MoTrPACCompoundRenamer {

	public static void main(String[] args) {
		
		File rootDir = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\20251016FIX\\PASS1A-18");
		Set<Path>filesForProcessing = getFilesForProcessing(rootDir);
		
		File renameMapFile = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01263 - PASS 1A 1C 18mo P20000245T C20000316E\\"
				+ "4BIC\\20251016FIX\\EX01263-BIC-compound-rename-map.txt");
		Map<String,String>compoundRenameMap = getCompoundRenameMap(renameMapFile);
		
		renameCompounds(filesForProcessing, compoundRenameMap);
	}
	
	private static void renameCompounds(Set<Path>filesForProcessing, Map<String,String>compoundRenameMap) {
		
		for(Path filePath : filesForProcessing) {
			
			String[][]fileData = new String[0][0];
			List<String> fixedData = new ArrayList<>();
			try {
				fileData = DelimitedTextParser.parseTextFileWithEncoding(
						filePath.toFile(), MRC2ToolBoxConfiguration.getTabDelimiter());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(String[] line : fileData) 
				fixedData.add(replaceInLine(line, compoundRenameMap));
			
			try {
			    Files.write(filePath, 
			    		fixedData,
			            StandardCharsets.UTF_8,
			            StandardOpenOption.CREATE, 
			            StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}		
	}
	
	private static String replaceInLine(String[] line, Map<String, String> compoundRenameMap) {

		String newRefMet = compoundRenameMap.get(line[1]);
		if(newRefMet != null && !newRefMet.isBlank())
			line[1] = newRefMet;
		
		return StringUtils.join(line, MRC2ToolBoxConfiguration.getTabDelimiter());
	}

	private static Set<Path>getFilesForProcessing(File rootDir){
		
		Set<Path>pathSet = new TreeSet<>();
		List<Path> metaDataFiles = 
				FIOUtils.findFilesByNameStartingWith(rootDir.toPath(), "metadata_metabolites_named");
		pathSet.addAll(metaDataFiles);
		
		return pathSet;
	}
	
	private static Map<String,String>getCompoundRenameMap(File renameMapFile){
		
		Map<String,String>compoundRenameMap = new TreeMap<String,String>();
		String[][]renameInput = new String[0][0];
		try {
			renameInput = DelimitedTextParser.parseTextFileWithEncoding(
					renameMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<renameInput.length; i++)
			compoundRenameMap.put(renameInput[i][0], renameInput[i][1]);
		
		
		return compoundRenameMap;
	}

}
