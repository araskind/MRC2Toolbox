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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class PeakParameterAnalysisGenerator {
	
	private String experimentId;
	private String separationMode;
	private File inputMapFile;
	private File workingDirectory;
	
	private List<String>scriptBlock;
	private Map<PeakQualityParameter,Set<Path>>inputFilesMap;


	public PeakParameterAnalysisGenerator(
			String experimentId, 
			String separationMode, 
			File inputMapFile,
			File workingDirectory) {
		super();
		this.experimentId = experimentId;
		this.separationMode = separationMode;
		this.inputMapFile = inputMapFile;
		this.workingDirectory = workingDirectory;
	}

	public void generatePeakParameterAnalysisScript() {
		
		createInputMap(inputMapFile);
		
		scriptBlock = new ArrayList<>();		
		scriptBlock.add("# Multi-batch peak parameters analysis ####");
		scriptBlock.add("library(ggplot2)");
		scriptBlock.add("library(reshape2)");
		scriptBlock.add("library(dplyr)");
		scriptBlock.add("library(readxl)");
		scriptBlock.add("library(psych)");
		scriptBlock.add("library(tibble)");

		writeScript();
	}
	
	private void createInputMap(File inputMapFile) {
		
		String[][] inputData = DelimitedTextParser.parseTextFile(
				inputMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		inputFilesMap = new TreeMap<>();
		Map<PeakQualityParameter,Integer>columnMap = new TreeMap<>();
		for(int i= 0; i<inputData[0].length; i++) {
			
			PeakQualityParameter par = PeakQualityParameter.getParameterByName(inputData[0][i]);
			if(par != null) {
				columnMap.put(par, i);
				inputFilesMap.put(par, new TreeSet<>());
			}
		}
		for(int i=1; i<inputData.length; i++) {
			
			for(Entry<PeakQualityParameter, Integer> me : columnMap.entrySet()) {
				
			}
		}		
	}
	
	private void writeScript() {
		
		String rScriptFileName = experimentId + "-" + separationMode 
				+ "-MultiBatchPeakQualityAnalysis-" + FIOUtils.getTimestamp() + ".R";
		Path outputPath = Paths.get(
				workingDirectory.getAbsolutePath(), rScriptFileName);
		try {
		    Files.write(outputPath, 
		    		scriptBlock,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}
