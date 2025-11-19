/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.utils.FileNameUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class MFESummarizer {

	public static void main(String[] args) {

//		File mfeDir = new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
//				+ "A003 - Untargeted\\MFE\\POS\\CO300PK1000\\BATCH02");
		
		File rootDir = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A049 - Central carbon metabolism profiling\\MFE\\NEG");
		String cefFolderMask = "CO900-Pk3000";
		File outputFile = new File("Y:\\DataAnalysis\\_Reports\\EX01496 - Human EDTA Tranche 3 plasma X20001463K\\"
				+ "A049 - Central carbon metabolism profiling\\MFE\\NEG\\EX01496-IONP-NEG-MFE-HC-counts.txt");
		File mfeDir = new File("Y:\\DataAnalysis\\_Reports\\EX01526 - Human EDTA Tranche 4 plasma H20001805E\\"
				+ "A003 - Untargeted\\MFE\\POS\\CO300PK1000\\BATCH11");
		try {
			readMFEfeatureCounts(mfeDir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testSatCount() {

		Path cefPath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX01552 - Urine Metabolomics for OAB\\"
				+ "A003 - Untargeted\\MFE\\POS\\V1\\20251002-EX01552-A003-IN0028-S00083348-129c3-P.cef");
		Map<SampleStats,Integer>statMap = getSampleStats(cefPath);
		System.out.println(SampleStats.FEATURE_COUNT.name() + " " + statMap.get(SampleStats.FEATURE_COUNT));
		System.out.println(SampleStats.SATURATED_COUNT.name() + " " + statMap.get(SampleStats.SATURATED_COUNT));
	}
	
	private static void readMFEfeatureCountsFromMultipleDirectories(
			File rootDir,
			String cefFolderMask,
			File outputFile) throws Exception{
		List<Path>cefDirList =  FIOUtils.findDirectoriesByName(rootDir.toPath(), cefFolderMask);
		List<String>featureCountsMap = new ArrayList<>();
		featureCountsMap.add( "DataFile\t#Features\t#Saturated");
		for(Path cefDir : cefDirList) {
			
			List<Path>cefPathList = FIOUtils.findFilesByExtension(cefDir, "cef");
			for(Path cefPath : cefPathList) {
				
				//	int featureCount = getCefFeatureCount(cefPath);
				Map<SampleStats,Integer>statMap = getSampleStats(cefPath);
				featureCountsMap.add(FileNameUtils.getBaseName(cefPath) 
						+ "\t" + statMap.get(SampleStats.FEATURE_COUNT)
						+ "\t" + statMap.get(SampleStats.SATURATED_COUNT));
			}
		}
		try {
		    Files.write(outputFile.toPath(), 
		    		featureCountsMap,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void readMFEfeatureCounts(File mfeDir) throws Exception{
		
		List<Path>cefPathList = FIOUtils.findFilesByExtension(mfeDir.toPath(), "cef");
		List<String>featureCountsMap = new ArrayList<>();
		featureCountsMap.add( "DataFile\t#Features\t#Saturated");
		for(Path cefPath : cefPathList) {
			
			Map<SampleStats,Integer>statMap = getSampleStats(cefPath);
			featureCountsMap.add(FileNameUtils.getBaseName(cefPath) 
					+ "\t" + statMap.get(SampleStats.FEATURE_COUNT)
					+ "\t" + statMap.get(SampleStats.SATURATED_COUNT));
		}
		Path outputPath = Paths.get(mfeDir.toString(), "FeatureCounts.txt");
		try {
		    Files.write(outputPath, 
		    		featureCountsMap,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private static int getCefFeatureCount(Path cefPath) {
		
		Document cefDocument = XmlUtils.readXmlFile(cefPath.toFile());
		List<Element>featureNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");
		return featureNodes.size();
	}
	
	private static Map<SampleStats,Integer>getSampleStats(Path cefPath){
		
		Map<SampleStats,Integer>sampleStats = new TreeMap<>();
		Document cefDocument = XmlUtils.readXmlFile(cefPath.toFile());
		List<Element>featureNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");
		sampleStats.put(SampleStats.FEATURE_COUNT, featureNodes.size());
		int saturatedCount = 0;
		for(Element featureElement : featureNodes) {
			
			Element firstPeak = featureElement.getChild("Spectrum").
					getChild("MSPeaks").getChildren("p").get(0);
			if(firstPeak.getAttribute("sat") != null)
				saturatedCount++;
		}
		sampleStats.put(SampleStats.SATURATED_COUNT, saturatedCount);
		return sampleStats;
	}
	
	
	private enum SampleStats{
		FEATURE_COUNT,
		SATURATED_COUNT
		;
	}
	
	
	
}
