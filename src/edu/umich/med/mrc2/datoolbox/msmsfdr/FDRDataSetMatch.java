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

package edu.umich.med.mrc2.datoolbox.msmsfdr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.PepSearchOutputObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputFields;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;

public class FDRDataSetMatch {
	
	private static final PepSearchOutputObjectComparator pooSorter = 
			new PepSearchOutputObjectComparator(SortProperty.msmsScore, SortDirection.DESC);
	protected static final DecimalFormat scoreFormat = new DecimalFormat("##.##");
	
	private static File libraryMatchFile;
	private static File decoyMatchFile;
	private static File outputFile;

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		//	NEG hybrid
		String libFileDir = "I:\\Sasha\\LibAndDecoySearch\\NEG\\Results\\Library\\Hybrid";
		String decoyFileDir = "I:\\Sasha\\LibAndDecoySearch\\NEG\\Results\\Decoy\\Hybrid";
		String outFileDir = "I:\\Sasha\\LibAndDecoySearch\\NEG\\Results\\Merged\\Overall\\Hybrid";
		
		String[]libFileList = new String[] {
			"EX00663-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00884-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-liver-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-muscle-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-plasma-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-white-adipose-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Hippocampus-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Kidney-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Lung-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-brown-adipose-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-heart-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Hippocampus-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Kidney-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Liver-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Lung-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Muscle-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-brown-adipose-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-heart-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-white-adipose-NEG-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
		};
		String[] decoyFileList = new String[] {
			"EX00663-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00884-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-liver-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-muscle-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-plasma-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-white-adipose-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Hippocampus-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Kidney-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Lung-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-brown-adipose-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-heart-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Hippocampus-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Kidney-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Liver-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Lung-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Muscle-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-brown-adipose-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-heart-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-white-adipose-NEG-test-20210111_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
		};		
		String[] outFileList = new String[] {
			"EX00663-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00884-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-liver-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-muscle-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-plasma-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-white-adipose-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-Hippocampus-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-Kidney-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-Lung-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-brown-adipose-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-heart-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Hippocampus-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Kidney-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Liver-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Lung-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Muscle-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-brown-adipose-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-heart-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-white-adipose-NEG-BEST-OVERALL-HYBRID-RESULTS.TXT",
		};
	
		for(int i=0; i<libFileList.length; i++) {
			
			libraryMatchFile = Paths.get(libFileDir, libFileList[i]).toFile();
			decoyMatchFile = Paths.get(decoyFileDir, decoyFileList[i]).toFile();
			outputFile = Paths.get(outFileDir, outFileList[i]).toFile();
			System.out.println("Creating file " + outputFile.getAbsolutePath());
			
			Collection<PepSearchOutputObject>libraryHits = parsePepSearchResults(libraryMatchFile);
			Collection<PepSearchOutputObject>decoyHits = parsePepSearchResults(decoyMatchFile);
			decoyHits.stream().forEach(p -> p.setDecoy(true));
			
			Collection<PepSearchOutputObject>mergedData = mergeLibraryAndDecoyHits(
					libraryHits, decoyHits, MergeType.BEST_OVERALL);		
			try {
				writeMergedDataToFile(mergedData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//	POS hybrid
		libFileDir = "I:\\Sasha\\LibAndDecoySearch\\POS\\Results\\Library\\Hybrid";
		decoyFileDir = "I:\\Sasha\\LibAndDecoySearch\\POS\\Results\\Decoy\\Hybrid";
		outFileDir = "I:\\Sasha\\LibAndDecoySearch\\POS\\Results\\Merged\\Overall\\Hybrid";
		
		libFileList = new String[] {
			"EX00663-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00884-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-liver-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-muscle-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-plasma-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-white-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Hippocampus-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Kidney-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Lung-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-brown-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-heart-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Hippocampus-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Kidney-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Liver-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Lung-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Muscle-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-brown-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-heart-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-white-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_PEPSEARCH_RESULTS.TXT",
		};
		decoyFileList = new String[] {
			"EX00663-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00884-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-liver-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-muscle-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-plasma-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00930-MOTRPAC-white-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Hippocampus-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Kidney-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-Lung-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-brown-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00953-MOTRPAC-heart-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Hippocampus-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Kidney-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Liver-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Lung-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-Muscle-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-brown-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-heart-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
			"EX00979-MOTRPAC-white-adipose-POS-test-20201218-090840_NIST_MSMS_HYBRID_DECOY_PEPSEARCH_RESULTS.TXT",
		};		
		outFileList = new String[] {
			"EX00663-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00884-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-liver-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-muscle-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-plasma-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00930-MOTRPAC-white-adipose-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-Hippocampus-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-Kidney-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-Lung-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-brown-adipose-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00953-MOTRPAC-heart-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Hippocampus-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Kidney-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Liver-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Lung-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-Muscle-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-brown-adipose-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-heart-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
			"EX00979-MOTRPAC-white-adipose-POS-BEST-OVERALL-HYBRID-RESULTS.TXT",
		};
	
		for(int i=0; i<libFileList.length; i++) {
			
			libraryMatchFile = Paths.get(libFileDir, libFileList[i]).toFile();
			decoyMatchFile = Paths.get(decoyFileDir, decoyFileList[i]).toFile();
			outputFile = Paths.get(outFileDir, outFileList[i]).toFile();
			System.out.println("Creating file " + outputFile.getAbsolutePath());
			
			Collection<PepSearchOutputObject>libraryHits = parsePepSearchResults(libraryMatchFile);
			Collection<PepSearchOutputObject>decoyHits = parsePepSearchResults(decoyMatchFile);
			decoyHits.stream().forEach(p -> p.setDecoy(true));
			
			Collection<PepSearchOutputObject>mergedData = mergeLibraryAndDecoyHits(
					libraryHits, decoyHits, MergeType.BEST_OVERALL);		
			try {
				writeMergedDataToFile(mergedData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private static NISTPepSearchParameterObject getNISTPepSearchParameterObject(File pepSearchResults) {
		
	      String command = "";
	      try (Stream<String> lines = Files.lines(Paths.get(pepSearchResults.getAbsolutePath()))) {
	        command = lines.skip(1).findFirst().get();
	      }
	      catch(IOException e){
	        e.printStackTrace();
	      }
	      if(command == null || command.isEmpty())
	    	  return null;
	      else
	    	  return NISTPepSearchUtils.parsePepSearchCommandLine(command);
	}
	
	private static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHits(
			Collection<PepSearchOutputObject>libaryHits, 
			Collection<PepSearchOutputObject>decoyHits, 
			MergeType mergeType){
		
		if(mergeType.equals(MergeType.BEST_OVERALL))
			return mergeLibraryAndDecoyHitsByBestOverall(libaryHits, decoyHits);
		
		if(mergeType.equals(MergeType.BEST_IN_TYPE))
			return mergeLibraryAndDecoyHitsByBestInType(libaryHits, decoyHits);
				
		return null;
	}
	
	private static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHitsByBestOverall(
			Collection<PepSearchOutputObject>libaryHits, 
			Collection<PepSearchOutputObject>decoyHits) {
		
		Collection<PepSearchOutputObject>merged = new ArrayList<PepSearchOutputObject>();
		Collection<PepSearchOutputObject>allHits = new ArrayList<PepSearchOutputObject>();
		allHits.addAll(libaryHits);
		allHits.addAll(decoyHits);
		
		Map<String, List<PepSearchOutputObject>> hitMap = 
				allHits.stream().collect(Collectors.groupingBy(PepSearchOutputObject::getMsmsFeatureId));
		
		for(Entry<String, List<PepSearchOutputObject>> pooListEntry : hitMap.entrySet()) {
			
			List<PepSearchOutputObject> featureHits = pooListEntry.getValue().stream().
					sorted(pooSorter).collect(Collectors.toList());
			
			PepSearchOutputObject topHit = featureHits.get(0);
			if(featureHits.size() > 1) {				
				PepSearchOutputObject nextHit = featureHits.get(1);
				topHit.setDeltaScoreWithNextBestMatch(topHit.getScore() - nextHit.getScore());
				topHit.setNextBestMatchDecoy(nextHit.isDecoy());
			}
			else
				topHit.setDeltaScoreWithNextBestMatch(null);
			
			merged.add(topHit);
		}	
		try {
			assignMrc2LibIds(merged);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return merged;
	}
	
	private static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHitsByBestInType(
			Collection<PepSearchOutputObject>libaryHits, 
			Collection<PepSearchOutputObject>decoyHits) {
		
		Collection<PepSearchOutputObject>merged = new ArrayList<PepSearchOutputObject>();
		
		return merged;
	}
	
	private static Collection<PepSearchOutputObject> parsePepSearchResults(File resultFile) {
		
		NISTPepSearchParameterObject parObject = getNISTPepSearchParameterObject(resultFile);
		String[][] searchData = null;
		try {
			searchData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						resultFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(searchData == null) {
			return null;
		}
		return parsePepSearchOutputToObjects(searchData, parObject);
	}
	
	private static void writeMergedDataToFile(Collection<PepSearchOutputObject>merged) throws Exception {
		
		ArrayList<String>headerChunks = new ArrayList<String>();
		headerChunks.add("MSMS_FEATURE_ID");
		headerChunks.add("MRC2_LIB_ID");
		headerChunks.add("LIBRARY_NAME");
		headerChunks.add("DECOY");
		headerChunks.add("SCORE");
		headerChunks.add("DELTA_NEXT_BEST_SCORE");
		headerChunks.add("IS_NEXT_BEST_MATCH_DECOY");
		headerChunks.add("DOT_PRODUCT");
		headerChunks.add("REVERSE_DOT_PRODUCT");
		headerChunks.add("PROBABILITY");
		headerChunks.add("MATCH_TYPE");
		headerChunks.add("DELTA_MZ");
		
		final Writer writer = new BufferedWriter(new FileWriter(outputFile));
		writer.append(StringUtils.join(headerChunks, "\t") + "\n");
		
		for(PepSearchOutputObject poo : merged) {
			
			ArrayList<String>line = new ArrayList<String>();
			line.add(poo.getMsmsFeatureId());
			line.add(poo.getMrc2libid());
			line.add(poo.getLibraryName());
			line.add(Boolean.toString(poo.isDecoy()));
			line.add(scoreFormat.format(poo.getScore()));
			
			Double deltaScore = poo.getDeltaScoreWithNextBestMatch();
			if(deltaScore == null) {
				line.add("");
				line.add("");
			}
			else {
				line.add(scoreFormat.format(deltaScore));
				line.add(Boolean.toString(poo.isNextBestMatchDecoy()));
			}			
			line.add(scoreFormat.format(poo.getDotProduct()));
			line.add(scoreFormat.format(poo.getReverseDotProduct()));
			line.add(scoreFormat.format(poo.getProbablility()));
			line.add(poo.getMatchType().name());
			line.add(MRC2ToolBoxConfiguration.getMzFormat().format(poo.getDeltaMz()));
			
			writer.append(StringUtils.join(line, "\t") + "\n");
		}
		writer.flush();
		writer.close();
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchOutputToObjects(
			String[][] searchData,
			NISTPepSearchParameterObject pepSearchParameterObject){
		
		Collection<PepSearchOutputObject>pooList = new ArrayList<PepSearchOutputObject>();
		Map<NISTPepSearchOutputFields,Integer>columnMap = 
				NISTPepSearchUtils.createPepSearchOutputColumnMap(searchData[0]);
		if(columnMap.isEmpty())
			return pooList;

		Integer unkIdColumn = columnMap.get(NISTPepSearchOutputFields.UNKNOWN);
		Integer libNameColumn = columnMap.get(NISTPepSearchOutputFields.LIBRARY);
		Integer dbNumColumn = columnMap.get(NISTPepSearchOutputFields.ID);
		Integer nistNumColumn = columnMap.get(NISTPepSearchOutputFields.NIST_RN);
		Integer peptideColumn = columnMap.get(NISTPepSearchOutputFields.PEPTIDE);
		Integer rankColumn = columnMap.get(NISTPepSearchOutputFields.RANK);
		Integer deltaMzColumn = columnMap.get(NISTPepSearchOutputFields.DELTA_MZ);
		Integer scoreColumn = columnMap.get(NISTPepSearchOutputFields.SCORE);
		Integer dotProductColumn = columnMap.get(NISTPepSearchOutputFields.DOT_PRODUCT);
		Integer probablilityColumn = columnMap.get(NISTPepSearchOutputFields.PROB);		
		Integer revDotProductColumn = columnMap.get(NISTPepSearchOutputFields.REVERSE_DOT_PRODUCT);
		Integer hybridDotProductColumn = columnMap.get(NISTPepSearchOutputFields.HYBRID_DOT_PRODUCT);
		Integer hybridScoreColumn = columnMap.get(NISTPepSearchOutputFields.HYBRID_SCORE);
		Integer hybridDeltaMzColumn = columnMap.get(NISTPepSearchOutputFields.HYBRID_DELTA_MZ);
		
		MSMSMatchType matchType = MSMSMatchType.Regular;
		if(pepSearchParameterObject.getHiResSearchOption().equals(HiResSearchOption.y))
			matchType = MSMSMatchType.Hybrid;

		if(pepSearchParameterObject.getHiResSearchOption().equals(HiResSearchOption.u))
			matchType = MSMSMatchType.InSource;
		
		for(int i=1; i<searchData.length; i++) {
			
			String[] dataLine = searchData[i];
			
			//	Check if line contains the match at all, quick and dirty
			if(dataLine.length < libNameColumn)
				continue;
			
			//	Check that correctly formatted MSMS feature ID is present
			String msmsFeatureId = dataLine[unkIdColumn];
			if(!msmsFeatureId.startsWith(DataPrefix.MSMS_SPECTRUM.getName()) && !msmsFeatureId.startsWith(DataPrefix.MS_FEATURE.getName()))
				continue;
							
			PepSearchOutputObject poo = new PepSearchOutputObject(msmsFeatureId);

			if(libNameColumn != null)
				poo.setLibraryName(dataLine[libNameColumn]);

			if(dbNumColumn != null)
				poo.setDatabaseNumber(dataLine[dbNumColumn]);

			if(nistNumColumn != null)
				poo.setNistRegId(dataLine[nistNumColumn]);

			if(peptideColumn != null)
				poo.setPeptide(dataLine[peptideColumn]);

			if(rankColumn != null)
				poo.setMatchRank(Integer.parseInt(dataLine[rankColumn]));

			if(deltaMzColumn != null)
				poo.setDeltaMz(Double.parseDouble(dataLine[deltaMzColumn]));

			if(scoreColumn != null)
				poo.setScore(Double.parseDouble(dataLine[scoreColumn]));

			if(dotProductColumn != null)
				poo.setDotProduct(Double.parseDouble(dataLine[dotProductColumn]));

			if(probablilityColumn != null)
				poo.setProbablility(Double.parseDouble(dataLine[probablilityColumn]));
			
			//
			if(revDotProductColumn != null)
				poo.setReverseDotProduct(Double.parseDouble(dataLine[revDotProductColumn]));
			
			if(hybridDotProductColumn != null)
				poo.setHybridDotProduct(Double.parseDouble(dataLine[hybridDotProductColumn]));
			
			if(hybridScoreColumn != null)
				poo.setHybridScore(Double.parseDouble(dataLine[hybridScoreColumn]));
			
			if(hybridDeltaMzColumn != null)
				poo.setHybridDeltaMz(Double.parseDouble(dataLine[hybridDeltaMzColumn]));

			poo.setMatchType(matchType);		
			pooList.add(poo);			
		}
		return pooList;
	}
	
	private static void setOrigLibIdForPepSearchOutputObject(PepSearchOutputObject poo, String refLibraryId, boolean isDecoy) {
		
		if(refLibraryId.equals(NISTReferenceLibraries.nist_msms.name()) || 
				refLibraryId.equals(NISTReferenceLibraries.hr_msms_nist.name()))
			poo.setOriginalLibid(poo.getNistRegId());
		else if(refLibraryId.equals(NISTReferenceLibraries.nist_msms2.name()))
			poo.setOriginalLibid(poo.getDatabaseNumber());
		else
			poo.setOriginalLibid(poo.getPeptide());
		
		poo.setDecoy(isDecoy);
	}
	
	private static void assignMrc2LibIds(Collection<PepSearchOutputObject> pooList) throws Exception {
		
		Map<String, ReferenceMsMsLibrary> refLibMap = 
				NISTPepSearchUtils.getMSMSLibraryNameMap(pooList);
		Map<String, String>refLibNameMap = new TreeMap<String, String>();		
		refLibMap.entrySet().stream().
			forEach(l -> refLibNameMap.put(l.getKey(), 
					l.getValue().getPrimaryLibraryId()));
		
		Map<String, Boolean>decoyNameMap = new TreeMap<String, Boolean>();
		refLibMap.entrySet().stream().
			forEach(l -> decoyNameMap.put(l.getKey(), 
					l.getValue().isDecoy()));
		
		pooList.stream().filter(p -> Objects.nonNull(p.getLibraryName())).
			forEach(p -> setOrigLibIdForPepSearchOutputObject(
					p, refLibNameMap.get(p.getLibraryName()), 
					decoyNameMap.get(p.getLibraryName())));
		
		//	Filter out hits with MRC2 lib IDs present
		Pattern libIdPattern = Pattern.compile("^MSL\\d{9}$");
		Collection<PepSearchOutputObject> unssignedLibPooList =
				pooList.stream().filter(p -> Objects.nonNull(p.getOriginalLibid())).
				filter(p -> !libIdPattern.matcher(p.getOriginalLibid()).matches()).
				collect(Collectors.toList());
		
		pooList.stream().filter(p -> Objects.nonNull(p.getOriginalLibid())).
			filter(p -> libIdPattern.matcher(p.getOriginalLibid()).matches()).
			forEach(p -> p.setMrc2libid(p.getOriginalLibid()));
		
		Map<String, Map<String,String>>refLibIdMap = 
				new HashMap<String, Map<String,String>>();
		refLibNameMap.entrySet().stream().
			forEach(m -> refLibIdMap.put(m.getValue(),new HashMap<String,String>()));
		unssignedLibPooList.stream().
			filter(p -> Objects.nonNull(p.getLibraryName())).
			filter(p -> Objects.nonNull(p.getOriginalLibid())).
			forEach(p -> refLibIdMap.get(
					refLibNameMap.get(p.getLibraryName())).put(p.getOriginalLibid(), null));
		
		Connection conn = ConnectionManager.getConnection();		
		String query =
			"SELECT C.MRC2_LIB_ID  " +
			"FROM REF_MSMS_LIBRARY_COMPONENT C " +
			"WHERE C.LIBRARY_NAME = ? " +
			"AND C.ORIGINAL_LIBRARY_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);	
		
		for(Entry<String, Map<String, String>> rle : refLibIdMap.entrySet()) {
		
			String mrc2id = null;
			ps.setString(1, rle.getKey());			
			for(String originalLibraryId : rle.getValue().keySet()) {
				
				ps.setString(2, originalLibraryId);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
					mrc2id = rs.getString("MRC2_LIB_ID");
				
				rs.close();
				if(mrc2id == null && rle.getKey().equals(NISTReferenceLibraries.hr_msms_nist.name())) {
					
					ps.setString(1, NISTReferenceLibraries.nist_msms.name());
					ps.setString(2, originalLibraryId);
					rs = ps.executeQuery();
					while(rs.next())
						mrc2id = rs.getString("MRC2_LIB_ID");
					
					rs.close();
				}
				rle.getValue().put(originalLibraryId, mrc2id);
			}
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		unssignedLibPooList.stream().
			filter(p -> Objects.nonNull(p.getLibraryName())).
			filter(p -> Objects.nonNull(p.getOriginalLibid())).
			forEach(p -> p.setMrc2libid(
				refLibIdMap.get(
					refLibNameMap.get(p.getLibraryName())).get(p.getOriginalLibid())));
	}
}
