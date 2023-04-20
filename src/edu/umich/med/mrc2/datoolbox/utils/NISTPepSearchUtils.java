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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputColumnCode;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputFields;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchThreshold;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HitRejectionOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.OutputInclusionOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PeptideScoreOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PreSearchType;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class NISTPepSearchUtils {
	
	public static final MsFeatureIdentityComparator idScoreComparator = 
			new MsFeatureIdentityComparator(SortProperty.msmsScore, SortDirection.DESC);

	public static Map<NISTPepSearchOutputFields,Integer>createPepSearchOutputColumnMap(String[]pepsearchResultsHeader){
		
		Map<NISTPepSearchOutputFields,Integer>columnMap = new HashMap<NISTPepSearchOutputFields,Integer>();
		for(int i=0; i<pepsearchResultsHeader.length; i++) {

			NISTPepSearchOutputFields col = NISTPepSearchOutputFields.getFieldByColumnName(pepsearchResultsHeader[i]);
			if(col != null)
				columnMap.put(col, i);
		}
		return columnMap;
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchOutputToObjects(
			File searchResults,
			NISTPepSearchParameterObject pepSearchParameterObject){
			
		String[][] searchData = null;
		try {
			searchData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						searchResults, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(searchData == null)
			return null;
		
		if(pepSearchParameterObject == null) {
			
			String commandLine = "";
			try (Stream<String> lines = Files.lines(Paths.get(searchResults.getAbsolutePath()))) {
				commandLine = lines.skip(1).findFirst().get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			pepSearchParameterObject = parsePepSearchCommandLine(commandLine);
		}	
		Collection<PepSearchOutputObject>pooList = 
				parsePepSearchOutputToObjects(searchData, pepSearchParameterObject);
		return pooList;
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchOutputToObjects(
			String[][] searchData,
			NISTPepSearchParameterObject pepSearchParameterObject){
		
		Collection<PepSearchOutputObject>pooList = new ArrayList<PepSearchOutputObject>();
		Map<NISTPepSearchOutputFields,Integer>columnMap = createPepSearchOutputColumnMap(searchData[0]);
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
		Integer unknownInChiKeyColumn = columnMap.get(NISTPepSearchOutputFields.UNKNOWN_INCHIKEY);		
		
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
			if(!msmsFeatureId.startsWith(DataPrefix.MSMS_SPECTRUM.getName()) || msmsFeatureId.length() != 16)
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
			
			if(unknownInChiKeyColumn != null)
				poo.setUnknownInchiKey(dataLine[unknownInChiKeyColumn]);

			poo.setMatchType(matchType);		
			pooList.add(poo);			
		}
		return pooList;
	}
	
	public static Collection<String> createResultsSummary(Collection<PepSearchOutputObject>pooList) {
		
		Collection<String>resultSummary = new ArrayList<String>();
		
		long totalDistinctFeatures = pooList.stream().map(o -> o.getMsmsFeatureId()).distinct().count();
		resultSummary.add("Found total " + Integer.toString(pooList.size()) + " output lines for " + 
				Long.toString(totalDistinctFeatures) + " distinct MSMS features");
				
		List<PepSearchOutputObject> libHits = pooList.stream().
				filter(poo -> (poo.getLibraryName() != null  && (poo.getNistRegId() != null || poo.getPeptide() != null))).
				collect(Collectors.toList());
		
		long identifiedDistinctFeatures = libHits.stream().map(o -> o.getMsmsFeatureId()).distinct().count();
		resultSummary.add("Found total " + Integer.toString(libHits.size()) + " identifications for " + 
				Long.toString(identifiedDistinctFeatures) + " distinct MSMS features");
		
		Map<String, Long> countsByLibrary = libHits.stream().map(o -> o.getLibraryName()).
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		
		resultSummary.add("Hits count by library:");
		countsByLibrary.entrySet().stream().forEach(e -> resultSummary.add(e.getKey() + "\t" + Long.toString(e.getValue())));
		
		//	Check for unknown libraries
		for(Entry<String, Long> e : countsByLibrary.entrySet()) {
			
			ReferenceMsMsLibrary refLib = 
					IDTDataCache.getReferenceMsMsLibraryByCode(e.getKey());
			if(refLib == null) 
				resultSummary.add("Unknown library " + e.getKey());
		}
		return resultSummary;
	}
	
	public static Map<String, ReferenceMsMsLibrary>getMSMSLibraryNameMap(Collection<PepSearchOutputObject>pooList){
		
		Map<String, ReferenceMsMsLibrary>refLibMap = new TreeMap<String, ReferenceMsMsLibrary>();
		Collection<String> libNames = pooList.stream().
				filter(poo -> (poo.getLibraryName() != null  && (poo.getNistRegId() != null || poo.getPeptide() != null))).
				map(poo -> poo.getLibraryName()).distinct().
				collect(Collectors.toSet());
		
		for(String name : libNames) {
			
			ReferenceMsMsLibrary refLib = IDTDataCache.getReferenceMsMsLibraryByCode(name);
			if(refLib != null) 
				refLibMap.put(name, refLib);				
		}
		return refLibMap;
	}
	
	public static NISTPepSearchParameterObject parsePepSearchCommandLine(String commandLine) {

		NISTPepSearchParameterObject pepSearchParameterObject = new NISTPepSearchParameterObject();
		
		String[] commands = StringUtils.substringsBetween(commandLine, "\"", ".exe\"");
		commandLine = commandLine.replace( "> \"" + commands[0] + ".exe\"", "");
		
		String[] inputs = StringUtils.substringsBetween(commandLine, "/INP \"", "\"");
		commandLine = commandLine.replace( "/INP \"" + inputs[0] + "\"", "");
		
		String[] outputs = StringUtils.substringsBetween(commandLine, "/OUTTAB \"", "\"");
		commandLine = commandLine.replace( "/OUTTAB \"" + outputs[0] + "\"", "");

		String[] libraries = StringUtils.substringsBetween(commandLine, "/LIB \"", "\"");
		for (String v : libraries) {
			pepSearchParameterObject.getLibraryFiles().add(new File(v));
			commandLine = commandLine.replace( "/LIB \"" + v + "\"", "");
		}	
		commandLine = commandLine.replaceAll("\\s+", " ").trim();
		pepSearchParameterObject.setUseInputFile(true);		

		//	MZ range
		Pattern regexPattern = Pattern.compile("/MzLimits\\s+(\\d+\\.?\\d*)\\s+(\\d+\\.?\\d*)");
		Matcher regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {
			
			double mzRangeMin = Double.parseDouble(regexMatcher.group(1));
			double mzRangeMax = Double.parseDouble(regexMatcher.group(2));
			if(mzRangeMax == -1.0d)
				mzRangeMax = NISTPepSearchParameterObject.INFINITE_MZ_UPPER_LIMIT;
			
			Range mzRange = new Range(mzRangeMin, mzRangeMax);
			pepSearchParameterObject.setMzRange(mzRange);
			String mzLimits = regexMatcher.group(0);
			commandLine = commandLine.replace(regexMatcher.group(0), "").trim();
		}
		else {
			pepSearchParameterObject.setMzRange(
				new Range(0.0d, NISTPepSearchParameterObject.INFINITE_MZ_UPPER_LIMIT));
		}
		//	List output columns
		regexPattern = Pattern.compile("/COL\\s+(\\w{2})(,\\w{2})*");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {
			String colString = regexMatcher.group(0).replaceFirst("/COL\\s+", "").trim();
			List<String>columnCodes = Arrays.asList(colString.split(","));
			Map<String, Boolean> outputColumnMap = pepSearchParameterObject.getOutputColumns();
			for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values())
				outputColumnMap.put(code.name(), columnCodes.contains(code.getCode()));
			
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		regexPattern = Pattern.compile("/MwForLoss\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			double mwForLoss = Double.parseDouble(regexMatcher.group(1));
			pepSearchParameterObject.setHybridSearchMassLoss(mwForLoss);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		if(commandLine.contains("/MatchPolarity")) {
			pepSearchParameterObject.setMatchPolarity(true);
			commandLine = commandLine.replaceAll( "/MatchPolarity\\s?", "");
		}
		else {
			pepSearchParameterObject.setMatchPolarity(false);
		}
		if(commandLine.contains("/MatchCharge")) {
			pepSearchParameterObject.setMatchCharge(true);
			commandLine = commandLine.replaceAll( "/MatchCharge\\s?", "");
		}
		else {
			pepSearchParameterObject.setMatchCharge(false);
		}
		if(commandLine.contains("/HiPri")) {
			pepSearchParameterObject.setHighExecutionPriority(true);
			commandLine = commandLine.replaceAll( "/HiPri\\s?", "");
		}
		else {
			pepSearchParameterObject.setHighExecutionPriority(false);
		}
		if(commandLine.contains("/LibInMem")) {
			pepSearchParameterObject.setLoadLibrariesInMemory(true);
			commandLine = commandLine.replaceAll( "/LibInMem\\s?", "");
		}
		else {
			pepSearchParameterObject.setLoadLibrariesInMemory(false);
		}
		regexPattern = Pattern.compile("/HITS\\s+(\\d+)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			int numHits = Integer.parseInt(regexMatcher.group(1));
			pepSearchParameterObject.setMaxNumberOfHits(numHits);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		regexPattern = Pattern.compile("/MinMF\\s+(\\d+)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			int minMatchFactor = Integer.parseInt(regexMatcher.group(1));
			pepSearchParameterObject.setMinMatchFactor(minMatchFactor);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		regexPattern = Pattern.compile("/MinInt\\s+(\\d+)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			int minIntensity = Integer.parseInt(regexMatcher.group(1));
			pepSearchParameterObject.setMinimumIntensityCutoff(minIntensity);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		else {
			pepSearchParameterObject.setMinimumIntensityCutoff(1);
		}
		for(OutputInclusionOption outOpt : OutputInclusionOption.values()) {
			
			if(commandLine.contains("/" + outOpt.name())) {
				pepSearchParameterObject.setOutputInclusionOption(outOpt);
				commandLine = commandLine.replaceAll( "/" + outOpt.name() + "\\s?", "");
				break;
			}
		}
		//	Precursor
		regexPattern = Pattern.compile("/Z\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {
			//	This is iffy, probably need a better way
			double massErrorValue = Double.parseDouble(regexMatcher.group(1));
			if(massErrorValue < 1.0) {
				pepSearchParameterObject.setPrecursorMzErrorValue(massErrorValue * 1000.0d);
				pepSearchParameterObject.setPrecursorMzErrorType(MassErrorType.mDa);
			}
			else {
				pepSearchParameterObject.setPrecursorMzErrorValue(massErrorValue);
				pepSearchParameterObject.setPrecursorMzErrorType(MassErrorType.Da);
			}
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		regexPattern = Pattern.compile("/ZPPM\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			double massErrorValue = Double.parseDouble(regexMatcher.group(1));
			pepSearchParameterObject.setPrecursorMzErrorValue(massErrorValue);
			pepSearchParameterObject.setPrecursorMzErrorType(MassErrorType.ppm);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		//	Fragment
		regexPattern = Pattern.compile("/M\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {
			//	This is iffy, probably need a better way
			double massErrorValue = Double.parseDouble(regexMatcher.group(1));
			if(massErrorValue < 1.0) {
				pepSearchParameterObject.setFragmentMzErrorValue(massErrorValue * 1000.0d);
				pepSearchParameterObject.setFragmentMzErrorType(MassErrorType.mDa);
			}
			else {
				pepSearchParameterObject.setFragmentMzErrorValue(massErrorValue);
				pepSearchParameterObject.setFragmentMzErrorType(MassErrorType.Da);
			}
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		regexPattern = Pattern.compile("/MPPM\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			double massErrorValue = Double.parseDouble(regexMatcher.group(1));
			pepSearchParameterObject.setFragmentMzErrorValue(massErrorValue);
			pepSearchParameterObject.setFragmentMzErrorType(MassErrorType.ppm);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		//	Mass around precursor
		regexPattern = Pattern.compile("/ZI\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {
			pepSearchParameterObject.setIgnorePeaksAroundPrecursor(true);
			//	This is iffy, probably need a better way
			double massErrorValue = Double.parseDouble(regexMatcher.group(1));
			if(massErrorValue < 1.0) {
				pepSearchParameterObject.setIgnorePeaksAroundPrecursorWindow(massErrorValue * 1000.0d);
				pepSearchParameterObject.setIgnorePeaksAroundPrecursorAccuracyUnits(MassErrorType.mDa);
			}
			else {
				pepSearchParameterObject.setIgnorePeaksAroundPrecursorWindow(massErrorValue);
				pepSearchParameterObject.setIgnorePeaksAroundPrecursorAccuracyUnits(MassErrorType.Da);
			}
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		regexPattern = Pattern.compile("/ZIPPM\\s+(\\d+\\.?\\d*)");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) {

			pepSearchParameterObject.setIgnorePeaksAroundPrecursor(true);			
			double massErrorValue = Double.parseDouble(regexMatcher.group(1));
			pepSearchParameterObject.setIgnorePeaksAroundPrecursorWindow(massErrorValue);
			pepSearchParameterObject.setIgnorePeaksAroundPrecursorAccuracyUnits(MassErrorType.ppm);
			commandLine = commandLine.replaceAll(regexMatcher.group(0), "").trim();
		}
		//	Presearch type
		for(PreSearchType pt : PreSearchType.values()) {
			
			regexPattern = Pattern.compile("\\s*" + pt.name() + "\\s*");
			regexMatcher = regexPattern.matcher(commandLine);
			if(regexMatcher.find()) {
				pepSearchParameterObject.setPreSearchType(pt);
				break;
			}
		}
		//	HiRes search type
		for(HiResSearchType hrst : HiResSearchType.values()) {
			
			regexPattern = Pattern.compile("\\s*" + hrst.name() + "\\s*+");
			regexMatcher = regexPattern.matcher(commandLine);
			if(regexMatcher.find()) {
				pepSearchParameterObject.setHiResSearchType(hrst);
				break;
			}
		}
		//	HiRes search option
		for(HiResSearchOption hrso : HiResSearchOption.values()) {
			
			regexPattern = Pattern.compile("\\s*" + hrso.name() + "\\s*");
			regexMatcher = regexPattern.matcher(commandLine);
			if(regexMatcher.find()) {
				pepSearchParameterObject.setHiResSearchOption(hrso);
				break;
			}
		}
		//	HiRes search threshold
		for(HiResSearchThreshold hrsth : HiResSearchThreshold.values()) {
			
			regexPattern = Pattern.compile("\\s*" + hrsth.name() + "\\s*");
			regexMatcher = regexPattern.matcher(commandLine);
			if(regexMatcher.find()) 
				pepSearchParameterObject.setHiResSearchThreshold(hrsth);			
		}
		//	HiRes hit rejection option
		for(HitRejectionOption hrhro : HitRejectionOption.values()) {
			
			regexPattern = Pattern.compile("\\s*" + hrhro.name() + "\\s*");
			regexMatcher = regexPattern.matcher(commandLine);
			if(regexMatcher.find()) {
				pepSearchParameterObject.setHitRejectionOption(hrhro);	
				break;
			}
		}
		//	Peptide scoring option
		for(PeptideScoreOption pso : PeptideScoreOption.values()) {
			
			regexPattern = Pattern.compile("\\s*" + pso.name() + "\\s*+");
			regexMatcher = regexPattern.matcher(commandLine);
			if(regexMatcher.find()) { 
				pepSearchParameterObject.setPeptideScoreOption(pso);
				break;
			}
		}
		//	Alternative peak matching
		regexPattern = Pattern.compile("\\s*a\\s*");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find())
			pepSearchParameterObject.setEnableAlternativePeakMatching(true);
	
		//	Ignore peaks around precursor
		regexPattern = Pattern.compile("\\s*i\\s*");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) 
			pepSearchParameterObject.setIgnorePeaksAroundPrecursor(true);
		
		regexPattern = Pattern.compile("\\s*r\\s*");
		regexMatcher = regexPattern.matcher(commandLine);
		if(regexMatcher.find()) 
			pepSearchParameterObject.setEnableReverseSearch(true);
		
		//	TODO 
//		if(outRevMatchCheckBox.isSelected())
//			commandParts.add("v");
//		if(noHitProbabsCheckBox.isSelected())
//			commandParts.add("x");
		
		return pepSearchParameterObject;
	}
	
	public static List<String>createPepsearchcommandFromParametersObject(
			NISTPepSearchParameterObject parametersObject,
			File inputFile,
			File resultFile){
		
		List<String>commandParts = new ArrayList<String>();
		
		//	Add binary
		File execFile = new File(MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile());
		if(!execFile.exists())
			return null;
		
		commandParts.add(execFile.getAbsolutePath());
		
		//	Pre-search type [{sdfmk[n]}]
		commandParts.add(parametersObject.getPreSearchType().name());
		//	Search options	[aijnopqrvx]
		if(parametersObject.isEnableAlternativePeakMatching())
			commandParts.add("a");
		if(parametersObject.isIgnorePeaksAroundPrecursor())
			commandParts.add("i");
		if(parametersObject.getHitRejectionOption() != null)
			commandParts.add(parametersObject.getHitRejectionOption().name());
		if(parametersObject.isEnableReverseSearch())
			commandParts.add("r");
		
		Map<String, Boolean> outputColumnMap = parametersObject.getOutputColumns();

		if(outputColumnMap.get(PepSearchSetupDialog.OUTPUT_REVERSE_MATCH_FACTOR) != null 
				&& outputColumnMap.get(PepSearchSetupDialog.OUTPUT_REVERSE_MATCH_FACTOR))
			commandParts.add("v");
		
		if(outputColumnMap.get(PepSearchSetupDialog.NO_HIT_PROBABILITIES) != null 
				&& outputColumnMap.get(PepSearchSetupDialog.NO_HIT_PROBABILITIES))	//	TODO	Not saved in params object
			commandParts.add("x");
		//	Hi-res search option	[{uyz}]
		commandParts.add(parametersObject.getHiResSearchOption().name());
		//	Hi res threshold:	l e h[n]
		commandParts.add(parametersObject.getHiResSearchThreshold().name());
		//	HiRes search type	[{PGD}]
		commandParts.add(parametersObject.getHiResSearchType().name());
		
		//	Peptide scoring option
		if(parametersObject.getPeptideScoreOption() != null)
			commandParts.add(parametersObject.getPeptideScoreOption().name());
		
		//	Precursor mass uncertainty
		String precMassWindow = "/Z";
		if(parametersObject.getPrecursorMzErrorType().equals(MassErrorType.ppm))
			precMassWindow = "/ZPPM";

		commandParts.add(precMassWindow);
		
		String precMassWindowValue = getMassErrorValueString(
				Double.toString(parametersObject.getPrecursorMzErrorValue()),
				parametersObject.getPrecursorMzErrorType());
		commandParts.add(precMassWindowValue);
		
		//	Peak mass uncertainty
		String peakMassWindow = "/M";
		if(parametersObject.getFragmentMzErrorType().equals(MassErrorType.ppm))
			peakMassWindow = "/MPPM";

		commandParts.add(peakMassWindow);
		
		String peakMassWindowValue = getMassErrorValueString(
				Double.toString(parametersObject.getFragmentMzErrorValue()),
				parametersObject.getFragmentMzErrorType());
		commandParts.add(peakMassWindowValue);
		
		//	Exclude masses around precursor
		if(parametersObject.isIgnorePeaksAroundPrecursor()) {
			
			String ignoreMassWindow = "/ZI";
			if(parametersObject.getIgnorePeaksAroundPrecursorAccuracyUnits().equals(MassErrorType.ppm))
				ignoreMassWindow = "/ZIPPM";
			
			commandParts.add(ignoreMassWindow);

			String ignoreMassWindowValue = getMassErrorValueString(
					Double.toString(parametersObject.getIgnorePeaksAroundPrecursorWindow()),
					parametersObject.getIgnorePeaksAroundPrecursorAccuracyUnits());
			commandParts.add(ignoreMassWindowValue);
		}
		//	Search MZ range
		if(parametersObject.getMzRange() != null) {
			
			String mzMinString = Integer.toString((int)parametersObject.getMzRange().getMin());
			double mzMax = parametersObject.getMzRange().getMax();
			if(mzMax == NISTPepSearchParameterObject.INFINITE_MZ_UPPER_LIMIT)
				mzMax = -1;	
			
			String mzMaxString = Integer.toString((int)mzMax);
			
			commandParts.add("/MzLimits");
			commandParts.add(mzMinString);
			commandParts.add(mzMaxString);
		}
		//	Minimal peak intensity
		int minIntensity = parametersObject.getMinimumIntensityCutoff();
		if(minIntensity > 1 && minIntensity <= 999) {
			commandParts.add("/MinInt");
			commandParts.add(Integer.toString(minIntensity)); 
		}
		//	Add loss MW if hybrid search
		if(parametersObject.getHiResSearchOption().equals(HiResSearchOption.y)
				&& parametersObject.getHybridSearchMassLoss() > 0) {

				commandParts.add("/MwForLoss");
				commandParts.add(Double.toString(parametersObject.getHybridSearchMassLoss()));
		}
		if(parametersObject.isMatchCharge())
			commandParts.add("/MatchCharge");

		if(parametersObject.isMatchPolarity())
			commandParts.add("/MatchPolarity");

		if(parametersObject.isHighExecutionPriority())
			commandParts.add("/HiPri");

		if(parametersObject.isLoadLibrariesInMemory())
			commandParts.add("/LibInMem");

		//	Add libraries
		for(File libFile : parametersObject.getLibraryFiles()) {
			commandParts.add("/LIB");
			commandParts.add(libFile.getAbsolutePath());
		}
		//	Input file
		commandParts.add("/INP");
		commandParts.add(inputFile.getAbsolutePath());		
		
		//	Output file
		commandParts.add("/OUTTAB"); 
		commandParts.add(resultFile.getAbsolutePath());

		//	Add output options
		//	Minimal match factor
		commandParts.add("/MinMF");
		commandParts.add(Integer.toString(parametersObject.getMinMatchFactor()));
				
		//	Hits per compound
		commandParts.add("/HITS");
		commandParts.add(Integer.toString(parametersObject.getMaxNumberOfHits()));
		
		//	Add diagnostics to stderr
//		commandParts.add("/TIME");
//		commandParts.add("/PROGRESS");
		
		//	Inclusion list
		commandParts.add("/" + parametersObject.getOutputInclusionOption().name());

		
		ArrayList<String>columns = new ArrayList<String>();
		for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values()) {
			Boolean col = outputColumnMap.get(code.name());
			if(col != null && col)
				columns.add(code.getCode());		
		}
		if(!columns.isEmpty()) {
			commandParts.add("/COL");
			commandParts.add(StringUtils.join(columns, ","));
		}
			String commandString = StringUtils.join(commandParts, " ");
			System.out.println(commandString);
		return commandParts;
	}
	
	private static String getMassErrorValueString(String stringValue, MassErrorType errorType) {

		String converted = stringValue;
		if(errorType.equals(MassErrorType.mDa)) {

			double precMassWindowValue = Double.parseDouble(stringValue);
			return Double.toString(precMassWindowValue / 1000.0d);
		}
		if(errorType.equals(MassErrorType.ppm)) {
			double precMassWindowValue = Double.parseDouble(stringValue);
			return Integer.toString((int)precMassWindowValue);
		}
		return converted;
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchOutputFileToObjects(File pepSearchOutputFile) throws Exception{
		
		String command = "";
		try (Stream<String> lines = Files.lines(Paths.get(pepSearchOutputFile.getAbsolutePath()))) {
			command = lines.skip(1).findFirst().get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(command.isEmpty()) {
			throw new Exception("Pepsearch command not found!");
		}
		NISTPepSearchParameterObject pepSearchParameterObject = parsePepSearchCommandLine(command);
		String[][] searchData = null;
		try {
			searchData = DelimitedTextParser.parseTextFileWithEncodingSkippingComments(pepSearchOutputFile,
					MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return parsePepSearchOutputToObjects(searchData, pepSearchParameterObject);
	}
	
	public static Map<String,HiResSearchOption>getSearchTypeMap(Collection<MSFeatureInfoBundle>bundles){
		
		Set<String> searchParamSet = bundles.stream().
				flatMap(f -> f.getMsFeature().getIdentifications().stream()).
				filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch())).
				filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch().
						getSearchParameterSetId())).
				map(i -> i.getReferenceMsMsLibraryMatch().getSearchParameterSetId()).
				collect(Collectors.toSet());
			
		Map<String,HiResSearchOption>searchTypeMap = 
					new TreeMap<String,HiResSearchOption>();
		for(String spId : searchParamSet) {
			NISTPepSearchParameterObject pepSearchParams = 
					IDTDataCache.getNISTPepSearchParameterObjectById(spId);
			searchTypeMap.put(spId, pepSearchParams.getHiResSearchOption());
		}
		return searchTypeMap;
	}
	
	public static Map<HiResSearchOption,Collection<MsFeatureIdentity>>getSearchTypeIdentityMap(
			MsFeature feature, 
			Map<String,HiResSearchOption>searchTypeMap,
			boolean ignoreDecoys) {
		
		Map<HiResSearchOption,Collection<MsFeatureIdentity>>typeMap = 
				new TreeMap<HiResSearchOption,Collection<MsFeatureIdentity>>();
		for(HiResSearchOption o : HiResSearchOption.values())
			typeMap.put(o, new TreeSet<MsFeatureIdentity>(idScoreComparator));
		
		List<MsFeatureIdentity> nistSearchHits = feature.getIdentifications().stream().
			filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch())).
			filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch().getSearchParameterSetId())).
			collect(Collectors.toList());
		
		if(ignoreDecoys)
			nistSearchHits =  nistSearchHits.stream().
			filter(i -> !i.getReferenceMsMsLibraryMatch().isDecoyMatch()).
			collect(Collectors.toList());
		
		for(MsFeatureIdentity hit : nistSearchHits) {
			
			String parSetId = hit.getReferenceMsMsLibraryMatch().getSearchParameterSetId();
			typeMap.get(searchTypeMap.get(parSetId)).add(hit);	
		}		
		return typeMap;
	}
	
	public static Collection<MSFeatureInfoBundle> removeLockedFeatures(
			Collection<MSFeatureInfoBundle>featuresToFilter) {
		
		Collection<MSFeatureInfoBundle> filteredFeatures = featuresToFilter.stream().
			filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
			filter(f -> Objects.nonNull(f.getMsFeature().
					getPrimaryIdentity().getReferenceMsMsLibraryMatch())).			
			filter(f -> f.getMsFeature().getMSMSLibraryMatchCount() > 1).
			filter(f -> f.getIdFollowupSteps().isEmpty()).
			filter(f -> f.getStandadAnnotations().isEmpty()).
			filter(f -> f.getMsFeature().getAnnotations().isEmpty()).
			filter(f -> Objects.isNull(f.getMsFeature().getPrimaryIdentity().getAssignedBy())).
			filter(f -> (Objects.isNull(f.getMsFeature().getPrimaryIdentity().getIdentificationLevel()) 
				|| f.getMsFeature().getPrimaryIdentity().getIdentificationLevel().getId().equals("IDS002"))).
			collect(Collectors.toList());
		
		return filteredFeatures;
	}
		
	public static Map<NISTPepSearchParameterObject, Long> getPepSearchParameterSetCountsForDataSet(
			Collection<MSFeatureInfoBundle> msmsFeatures) {
		
		Map<NISTPepSearchParameterObject, Long> paramCounts = msmsFeatures.stream().
			filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
			map(f -> f.getMsFeature().getPrimaryIdentity()).
			filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
			map(id -> IDTDataCache.getNISTPepSearchParameterObjectById(
					id.getReferenceMsMsLibraryMatch().getSearchParameterSetId())).
						filter(o -> Objects.nonNull(o)).
//			filter(o -> o.getHiResSearchOption().equals(HiResSearchOption.z)).
			collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
				
		return paramCounts;
	}
	
	public static Collection<MSFeatureInfoBundle>fiterMSMSFeaturesByPepSearchParameterSet(
			Collection<MSFeatureInfoBundle> featuresToFilter,
			String paramSetId) {
		
		 return featuresToFilter.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().getSearchParameterSetId())).
				filter(f -> f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().getSearchParameterSetId().equals(paramSetId)).
				collect(Collectors.toList());
	}
}






























