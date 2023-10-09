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
import java.lang.ProcessBuilder.Redirect;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.PercolatorOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.compare.PepSearchOutputObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputColumnCode;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputFields;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchThreshold;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HitRejectionOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.OutputInclusionOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PeptideScoreOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PreSearchType;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class NISTPepSearchResultManipulator {
	
	private static final PepSearchOutputObjectComparator pooSorter = 
			new PepSearchOutputObjectComparator(SortProperty.msmsScore, SortDirection.DESC);
	private static final DecimalFormat scoreFormat = new DecimalFormat("##.##");
	private static final DecimalFormat pvalFdrFormat = new DecimalFormat("##.#####");
	
	public static void mergeTargetDecoySearchResultsForPercolator(
			File libraryMatchFile, 
			File decoyMatchFile,
			MergeType mergeType,
			File outputFile) {
		
		System.out.println("Creating file " + outputFile.getAbsolutePath());		
		Collection<PepSearchOutputObject>libraryHits = null;
		try {
			libraryHits = parsePepSearchResults(libraryMatchFile);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Collection<PepSearchOutputObject>decoyHits = null;
		try {
			decoyHits = parsePepSearchResults(decoyMatchFile);
			decoyHits.stream().forEach(p -> p.setDecoy(true));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(libraryHits == null || decoyHits == null)
			return;
				
		Collection<PepSearchOutputObject>mergedData = mergeLibraryAndDecoyHits(
				libraryHits, decoyHits, mergeType);		
		try {
			writeMergedDataToFile(mergedData, outputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchResults(File resultFile) throws Exception {
		return parsePepSearchResults(resultFile, true);
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchResults(
			File resultFile, boolean requireMsMsFeatureId) throws Exception {
		
		NISTPepSearchParameterObject parObject = getNISTPepSearchParameterObject(resultFile);
		if(parObject == null) {			
			throw new Exception("Pepsearch command not found or can not be parsed!");
		}
		String[][] searchData = null;
		try {
			searchData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						resultFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(searchData == null) {
			throw new Exception("Unable to read PepSearch results file!");
		}
		return parsePepSearchOutputToObjects(searchData, parObject, requireMsMsFeatureId);
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
	
	public static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHits(
			Collection<PepSearchOutputObject>libraryHits, 
			Collection<PepSearchOutputObject>decoyHits, 
			MergeType mergeType){
		
		if(mergeType.equals(MergeType.BEST_OVERALL))
			return mergeLibraryAndDecoyHitsByBestOverall(libraryHits, decoyHits);
		
		if(mergeType.equals(MergeType.BEST_IN_TYPE))
			return mergeLibraryAndDecoyHitsByBestInType(libraryHits, decoyHits);
		
		if(mergeType.equals(MergeType.MERGE_ALL))
			return mergeAllLibraryAndDecoyHits(libraryHits, decoyHits);
				
		return null;
	}
	
	public static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHitsByBestOverall(
			Collection<PepSearchOutputObject>libraryHits, 
			Collection<PepSearchOutputObject>decoyHits) {
		
		Collection<PepSearchOutputObject>merged = new ArrayList<PepSearchOutputObject>();
		Collection<PepSearchOutputObject>allHits = new ArrayList<PepSearchOutputObject>();
		allHits.addAll(libraryHits);
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
	
	
	public static Collection<PepSearchOutputObject>mergeAllLibraryAndDecoyHits(
			Collection<PepSearchOutputObject>libraryHits, 
			Collection<PepSearchOutputObject>decoyHits) {
		Collection<PepSearchOutputObject>allHits = new ArrayList<PepSearchOutputObject>();
		allHits.addAll(libraryHits);
		allHits.addAll(decoyHits);		
		try {
			assignMrc2LibIds(allHits);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allHits;
	}
	
	public static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHitsOnUnknownInChiKeyByBestOverall(
			Collection<PepSearchOutputObject>libraryHits, 
			Collection<PepSearchOutputObject>decoyHits) {
		
		Collection<PepSearchOutputObject>merged = new ArrayList<PepSearchOutputObject>();
		Collection<PepSearchOutputObject>allHits = new ArrayList<PepSearchOutputObject>();
		allHits.addAll(libraryHits);
		allHits.addAll(decoyHits);
		
		Map<String, List<PepSearchOutputObject>> hitMap = 
				allHits.stream().collect(Collectors.groupingBy(PepSearchOutputObject::getUnknownInchiKey));
		
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
	
	//	TODO
	private static Collection<PepSearchOutputObject>mergeLibraryAndDecoyHitsByBestInType(
			Collection<PepSearchOutputObject>libraryHits, 
			Collection<PepSearchOutputObject>decoyHits) {
		
		Collection<PepSearchOutputObject>merged = new ArrayList<PepSearchOutputObject>();
		
		return merged;
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchOutputToObjects(
			String[][] searchData,
			NISTPepSearchParameterObject pepSearchParameterObject){
		return parsePepSearchOutputToObjects(searchData, pepSearchParameterObject, true);
	}
	
	public static Collection<PepSearchOutputObject> parsePepSearchOutputToObjects(
			String[][] searchData,
			NISTPepSearchParameterObject pepSearchParameterObject,
			boolean requireMsMsFeatureId){
		
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
		Integer unknownInChiKeyColumn = columnMap.get(NISTPepSearchOutputFields.UNKNOWN_INCHIKEY);
		Integer libraryInChiKeyColumn = columnMap.get(NISTPepSearchOutputFields.INCHI_KEY);
				
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
			if(requireMsMsFeatureId && !msmsFeatureId.startsWith(DataPrefix.MSMS_SPECTRUM.getName()) && 
					!msmsFeatureId.startsWith(DataPrefix.MS_FEATURE.getName()))
				continue;
							
			PepSearchOutputObject poo = new PepSearchOutputObject(msmsFeatureId);

			if(libNameColumn != null)
				poo.setLibraryName(dataLine[libNameColumn]);

			if(dbNumColumn != null)
				poo.setDatabaseNumber(dataLine[dbNumColumn]);

			if(nistNumColumn != null)
				poo.setNistRegId(dataLine[nistNumColumn]);

			if(peptideColumn != null) {
				String peptide = dataLine[peptideColumn];
				poo.setPeptide(peptide);
				if(peptide.startsWith(DataPrefix.MSMS_LIBRARY_ENTRY.getName()) && peptide.length() == 12) 
					poo.setMrc2libid(peptide);
			}
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

			if(unknownInChiKeyColumn != null && !dataLine[unknownInChiKeyColumn].isEmpty())
				poo.setUnknownInchiKey(dataLine[unknownInChiKeyColumn]);
			
			if(libraryInChiKeyColumn != null && !dataLine[libraryInChiKeyColumn].isEmpty())
				poo.setLibInchiKey(dataLine[libraryInChiKeyColumn]);
			
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
	
	private static void assignMrc2LibIds(Collection<PepSearchOutputObject> inputPooList) throws Exception {
		
		List<PepSearchOutputObject> pooList = inputPooList.stream().
				filter(p -> Objects.isNull(p.getMrc2libid())).
				collect(Collectors.toList());
		
		if(pooList.isEmpty())
			return;
		
		Map<String, ReferenceMsMsLibrary> refLibMap = 			
				NISTPepSearchUtils.getMSMSLibraryNameMap(pooList);
		Map<String, String>refLibNameMap = new TreeMap<String, String>();		
		refLibMap.entrySet().stream().
			forEach(l -> refLibNameMap.put(l.getKey(), 
					l.getValue().getPrimaryLibraryId()));
		
		Map<String, Boolean>decoyNameMap = new TreeMap<String, Boolean>();
		refLibMap.entrySet().stream().
			forEach(l -> decoyNameMap.put(l.getKey(), l.getValue().isDecoy()));
		
		pooList.stream().filter(p -> Objects.nonNull(p.getLibraryName())).
			forEach(p -> setOrigLibIdForPepSearchOutputObject(
					p, refLibNameMap.get(
							p.getLibraryName()), decoyNameMap.get(p.getLibraryName())));
		
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
			forEach(m -> refLibIdMap.put(m.getValue(), new HashMap<String,String>()));
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
	
	public static void writeMergedDataToFile(Collection<PepSearchOutputObject>merged, File outputFile) throws Exception {
		
		ArrayList<String>headerChunks = new ArrayList<String>();
		headerChunks.add(LibDecoyMergedResultsFields.MSMS_FEATURE_ID.name());
		headerChunks.add(LibDecoyMergedResultsFields.MRC2_LIB_ID.name());
		headerChunks.add(LibDecoyMergedResultsFields.LIBRARY_NAME.name());
		headerChunks.add(LibDecoyMergedResultsFields.DECOY.name());
		headerChunks.add(LibDecoyMergedResultsFields.SCORE.name());
		headerChunks.add(LibDecoyMergedResultsFields.DELTA_NEXT_BEST_SCORE.name());
		headerChunks.add(LibDecoyMergedResultsFields.IS_NEXT_BEST_MATCH_DECOY.name());
		headerChunks.add(LibDecoyMergedResultsFields.DOT_PRODUCT.name());
		headerChunks.add(LibDecoyMergedResultsFields.REVERSE_DOT_PRODUCT.name());
		headerChunks.add(LibDecoyMergedResultsFields.PROBABILITY.name());
		headerChunks.add(LibDecoyMergedResultsFields.MATCH_TYPE.name());
		headerChunks.add(LibDecoyMergedResultsFields.DELTA_MZ.name());
		headerChunks.add(LibDecoyMergedResultsFields.IS_TRUE_MATCH.name());
		headerChunks.add(LibDecoyMergedResultsFields.PVALUE.name());
		headerChunks.add(LibDecoyMergedResultsFields.PVALUE_BASE_ALL.name());
		headerChunks.add(LibDecoyMergedResultsFields.FDR.name());
		headerChunks.add(LibDecoyMergedResultsFields.QVALUE.name());
		
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
			line.add(MRC2ToolBoxConfiguration.getMzFormat().format(Math.abs(poo.getDeltaMz())));
			
			line.add(Boolean.toString(poo.isTrueHit()));
			
			if(poo.getpValue() > 0.0d)
				line.add(pvalFdrFormat.format(poo.getpValue()));			
			else 
				line.add("");
			
			if(poo.getpValueBaseAll() > 0.0d)
				line.add(pvalFdrFormat.format(poo.getpValueBaseAll()));			
			else 
				line.add("");
			
			if(poo.getFdr() > 0.0d)
				line.add(pvalFdrFormat.format(poo.getFdr()));			
			else 
				line.add("");
			
			if(poo.getqValue() > 0.0d)
				line.add(pvalFdrFormat.format(poo.getqValue()));			
			else 
				line.add("");
			
			writer.append(StringUtils.join(line, "\t") + "\n");
		}
		writer.flush();
		writer.close();
	}
	
	public static void convertMergedResultFileToPinFormat(File pepSearchMergedResultFile, File outputFile) throws IOException {
		
		String[][] mergedData = null;
		try {
			mergedData =
				DelimitedTextParser.parseTextFileWithEncoding(
						pepSearchMergedResultFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(mergedData == null) {
			return;
		}
		Map<LibDecoyMergedResultsFields,Integer>columnMap = 
				createMergedResultsColumnMap(mergedData[0]);
		Integer fidColumn = columnMap.get(LibDecoyMergedResultsFields.MSMS_FEATURE_ID);
		Integer libIdColumn = columnMap.get(LibDecoyMergedResultsFields.MRC2_LIB_ID);
		Integer libNameColumn = columnMap.get(LibDecoyMergedResultsFields.LIBRARY_NAME);
		Integer isDecoyColumn = columnMap.get(LibDecoyMergedResultsFields.DECOY);
		Integer scoreColumn = columnMap.get(LibDecoyMergedResultsFields.SCORE);
//		Integer deltaNextBestScoreColumn = columnMap.get(LibDecoyMergedResultsFields.DELTA_NEXT_BEST_SCORE);
//		Integer isNextBestMatchDecoyColumn = columnMap.get(LibDecoyMergedResultsFields.IS_NEXT_BEST_MATCH_DECOY);
		Integer dotProductColumn = columnMap.get(LibDecoyMergedResultsFields.DOT_PRODUCT);
		Integer revDotProductColumn = columnMap.get(LibDecoyMergedResultsFields.REVERSE_DOT_PRODUCT);
		Integer probabilityColumn = columnMap.get(LibDecoyMergedResultsFields.PROBABILITY);
		Integer matchTypeColumn = columnMap.get(LibDecoyMergedResultsFields.MATCH_TYPE);
		Integer absDeltaMzColumn = columnMap.get(LibDecoyMergedResultsFields.DELTA_MZ);	
		
		ArrayList<String>headerChunks = new ArrayList<String>();
		headerChunks.add(PercolatorIputFields.SpecId.name());
		headerChunks.add(PercolatorIputFields.Label.name());
		headerChunks.add(PercolatorIputFields.ScanNr.name());
		headerChunks.add(PercolatorIputFields.score.name());
//		headerChunks.add(PercolatorIputFields.delta_next_best_score.name());
		headerChunks.add(PercolatorIputFields.dot_product.name());
		headerChunks.add(PercolatorIputFields.reverse_dot_product.name());
		headerChunks.add(PercolatorIputFields.probability.name());
		headerChunks.add(PercolatorIputFields.abs_delta_mz.name());
		headerChunks.add(PercolatorIputFields.Peptide.name());
		headerChunks.add(PercolatorIputFields.Proteins.name());
		
//		SpecId
//		Label
//		ScanNr
//		ExpMass
//		retentiontime
//		rank
//		score
//		delta_score
//		probability
//		abs_mass_error
//		msms_entropy_score
//		dot_product
//		abs_delta_rt
//		Peptide
//		Proteins

		
		final Writer writer = new BufferedWriter(new FileWriter(outputFile));
		writer.append(StringUtils.join(headerChunks, "\t") + "\n");
		
		for(int i=1; i<mergedData.length; i++) {
			
			String[] dataLine = mergedData[i];
			String[]outLine = new String[headerChunks.size()];
			
			int j = 0;
			outLine[j] = dataLine[fidColumn];
			j++;
			
			String label = "1";
			if(Boolean.valueOf(dataLine[isDecoyColumn]))
				label = "-1";
			
			outLine[j] = label;
			j++;
			
			String scanNum = Integer.toString(i);
			if(dataLine[fidColumn].startsWith("MSN"))
				scanNum = dataLine[fidColumn].replaceFirst("MSN_0+", "");
			
			if(dataLine[fidColumn].startsWith("MSL"))
				scanNum = dataLine[fidColumn].replaceFirst("MSL0+", "");
			
			outLine[j] = scanNum;
			j++;
			
			String score = dataLine[scoreColumn];
			if(score == null || score.isEmpty())
				score = "0";
			
			outLine[j] = score;
			j++;
			
//			String deltaNextBestScore = dataLine[deltaNextBestScoreColumn];
//			if(deltaNextBestScore == null || deltaNextBestScore.isEmpty())
//				deltaNextBestScore = "0";
//			
//			outLine[4] = deltaNextBestScore;
			
			String dotProduct = dataLine[dotProductColumn];
			if(dotProduct == null || dotProduct.isEmpty())
				dotProduct = "0";
			
			outLine[j] = dotProduct;
			j++;
			
			String revDotProduct = dataLine[revDotProductColumn];
			if(revDotProduct == null || revDotProduct.isEmpty())
				revDotProduct = "0";
			
			outLine[j] = revDotProduct;
			j++;
			
			String probability = dataLine[probabilityColumn];
			if(probability == null || probability.isEmpty())
				probability = "0";
			
			outLine[j] = probability;
			j++;
				
			String absDeltaMz = dataLine[absDeltaMzColumn];
			if(absDeltaMz == null || absDeltaMz.isEmpty())
				absDeltaMz = "0";
			
			outLine[j] = absDeltaMz;
			j++;
			
			outLine[j] = dataLine[libIdColumn];
			j++;
			outLine[j] = dataLine[libNameColumn];
			
			writer.append(StringUtils.join(outLine, "\t") + "\n");
		}
		writer.flush();
		writer.close();
	}
	
	public static Map<LibDecoyMergedResultsFields,Integer>createMergedResultsColumnMap(String[]pepsearchResultsHeader){
		
		Map<LibDecoyMergedResultsFields,Integer>columnMap = new HashMap<LibDecoyMergedResultsFields,Integer>();
		for(int i=0; i<pepsearchResultsHeader.length; i++) {

			LibDecoyMergedResultsFields col = LibDecoyMergedResultsFields.getFieldByName(pepsearchResultsHeader[i]);
			if(col != null)
				columnMap.put(col, i);
		}
		return columnMap;
	}
	
	public static Collection<PercolatorOutputObject>parsePercolatorOutputFile(File percolatorOutputFile){
		
		Collection<PercolatorOutputObject>percolatorOutput = new ArrayList<PercolatorOutputObject>();
		String[][] percolatorData = null;
		try {
			percolatorData =
				DelimitedTextParser.parseTextFileWithEncoding(
						percolatorOutputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(percolatorData == null) {
			return null;
		}
		Map<PercolatorOutputFields,Integer>columnMap  = 
				createPercolatorResultsColumnMap(percolatorData[0]);
		
		Integer fidColumn = columnMap.get(PercolatorOutputFields.PSMID);
		Integer scoreColumn = columnMap.get(PercolatorOutputFields.SCORE);
		Integer qValueColumn = columnMap.get(PercolatorOutputFields.Q_VALUE);
		Integer posteriorErrorProbColumn = columnMap.get(PercolatorOutputFields.POSTERIOR_ERROR_PROB);
		Integer libIdColumn = columnMap.get(PercolatorOutputFields.PEPTIDE);
		Integer libNameColumn = columnMap.get(PercolatorOutputFields.PROTEINIDS);

		for(int i=1; i<percolatorData.length; i++) {
			
			PercolatorOutputObject poo = new PercolatorOutputObject(
					percolatorData[i][fidColumn], 
					percolatorData[i][libIdColumn],
					percolatorData[i][libNameColumn],
					Double.parseDouble(percolatorData[i][scoreColumn]),
					Double.parseDouble(percolatorData[i][qValueColumn]),
					Double.parseDouble(percolatorData[i][posteriorErrorProbColumn]));
			percolatorOutput.add(poo);
		}		
		return percolatorOutput;
	}
	
	public static Map<PercolatorOutputFields,Integer>createPercolatorResultsColumnMap(String[]percolatorResultsHeader){
		
		Map<PercolatorOutputFields,Integer>columnMap = new HashMap<PercolatorOutputFields,Integer>();
		for(int i=0; i<percolatorResultsHeader.length; i++) {

			PercolatorOutputFields col = PercolatorOutputFields.getFieldByName(percolatorResultsHeader[i]);
			if(col != null)
				columnMap.put(col, i);
		}
		return columnMap;
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
		
		commandParts.add("\"" + execFile.getAbsolutePath() +"\"");
		
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
		commandParts.add("\"" + inputFile.getAbsolutePath() + "\"");		
		
		//	Output file
		commandParts.add("/OUTTAB"); 
		commandParts.add("\"" + resultFile.getAbsolutePath() + "\"");

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
	public static NISTPepSearchParameterObject parsePepSearchCommandLine(String commandLine) {

		NISTPepSearchParameterObject pepSearchParameterObject = new NISTPepSearchParameterObject();
		
		String[] commands = StringUtils.substringsBetween(commandLine, "\"", ".exe\"");
		commandLine = commandLine.replace( "> \"" + commands[0] + ".exe\"", "");
		
		String[] inputs = StringUtils.substringsBetween(commandLine, "/INP \"", "\"");
		if(inputs != null)
			commandLine = commandLine.replace( "/INP \"" + inputs[0] + "\"", "");
		else {
			inputs = StringUtils.substringsBetween(commandLine, "/INP", "/OUTTAB");
			if(inputs != null)
				commandLine = commandLine.replace( "/INP" + inputs[0] + "/OUTTAB", "/OUTTAB");
		}		
		String[] outputs = StringUtils.substringsBetween(commandLine, "/OUTTAB \"", "\"");
		if(outputs != null)
			commandLine = commandLine.replace( "/OUTTAB \"" + outputs[0] + "\"", "");
		else {
			outputs = StringUtils.substringsBetween(commandLine, "/OUTTAB", "/");
			if(outputs != null)
				commandLine = commandLine.replace( "/OUTTAB" + outputs[0] + "/", "/");
		}	
		//commandLine = commandLine.replace( "/OUTTAB \"" + outputs[0] + "\"", "");

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
	
	public static void createPassatuttoEBAinputFile(
			File libraryPepSearchOutputFile, 
			File passatuttoInputFile,
			boolean bestHitsOnly) {
		
		Collection<PepSearchOutputObject>libraryObjects = null;
		try {
			libraryObjects = 
					NISTPepSearchResultManipulator.parsePepSearchResults(libraryPepSearchOutputFile, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collection<PepSearchOutputObject>filtered = new ArrayList<PepSearchOutputObject>();
		if(bestHitsOnly) {
			Map<String, List<PepSearchOutputObject>> hitMap = 
					libraryObjects.stream().collect(Collectors.groupingBy(PepSearchOutputObject::getMsmsFeatureId));
			
			for(Entry<String, List<PepSearchOutputObject>> pooListEntry : hitMap.entrySet()) {
				
				List<PepSearchOutputObject> featureHits = pooListEntry.getValue().stream().
						sorted(pooSorter).collect(Collectors.toList());
				filtered.add(featureHits.get(0));
			}
		}
		else
			filtered = libraryObjects;
	
		try {
			assignMrc2LibIds(filtered);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<PepSearchOutputObject> sortedHits = 
				filtered.stream().
				sorted(pooSorter).collect(Collectors.toList());
		
		ArrayList<String>lines = new ArrayList<String>();
		lines.add("query\ttarget\tscore");
		
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		
		for(PepSearchOutputObject o : sortedHits) {
			
			String line = o.getMsmsFeatureId() 
					+ "\t" + o.getMrc2libid() + "\t" 
					+ nf.format(o.getScore());
			lines.add(line);
		}
		Path outputPath = Paths.get(passatuttoInputFile.getAbsolutePath());
	    try {
			Files.write(outputPath, 
					lines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void evaluateDecoyLibrary(
			File libraryPepSearchOutputFile, 
			File decoyPepSearchOutputFile,
			File percolatorOutputDirectory) {
	
		Collection<PepSearchOutputObject>libraryObjects = null;
		try {
			libraryObjects = 
					NISTPepSearchResultManipulator.parsePepSearchResults(libraryPepSearchOutputFile, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Collection<PepSearchOutputObject>decoyObjects = null;
		try {
			decoyObjects = 
					NISTPepSearchResultManipulator.parsePepSearchResults(decoyPepSearchOutputFile, false);
			decoyObjects.stream().forEach(p -> p.setDecoy(true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(libraryObjects == null || decoyObjects == null) {
			System.out.println("Couldn't parse input files.");
			return;
		}
		Collection<PepSearchOutputObject>mergedData = 
				NISTPepSearchResultManipulator.mergeLibraryAndDecoyHitsByBestOverall(libraryObjects, decoyObjects);	
		int totalHits = mergedData.size();
		
		DoubleSummaryStatistics sumStats = 
				mergedData.stream().mapToDouble(o -> o.getScore()).summaryStatistics();
		Collection<PepSearchOutputObject>libHits = 
				mergedData.stream().filter(h -> !h.isDecoy()).collect(Collectors.toList());
		Collection<PepSearchOutputObject>decoyHits = 
				mergedData.stream().filter(h -> h.isDecoy()).collect(Collectors.toList());

		for(PepSearchOutputObject poo : libHits) {
			
			if(poo.getLibInchiKey() != null && poo.getUnknownInchiKey() != null) {
				boolean sameStructure = 
						poo.getLibInchiKey().substring(0, 14).equals(poo.getUnknownInchiKey().substring(0, 14));
				poo.setTrueHit(sameStructure);
			}
		}
		long wrongLibHitCount = libHits.stream().filter(p -> !p.isTrueHit()).count();
		double percentIncorrectHits = (double)wrongLibHitCount/libHits.size();
		System.out.println("% incorrect top hits = " + MRC2ToolBoxConfiguration.getPpmFormat().format(percentIncorrectHits));
		
		TreeMap<Double,Double>scoreFdrMap = new TreeMap<Double,Double>();
		TreeMap<Double,Double>scorePvalueMap = new TreeMap<Double,Double>();
		TreeMap<Double,Double>scorePvalueBaseAllMap = new TreeMap<Double,Double>();
		for(double score = sumStats.getMin(); score < sumStats.getMax(); score = score + 1.0d) {
			
			final double cutoff = score;
			long numDecoyHitsAboveCutoff = decoyHits.stream().filter(d -> d.getScore() > cutoff).count();
			long numLibHitsAboveCutoff = libHits.stream().filter(d -> d.getScore() > cutoff).count();
			double pValue = (double)numDecoyHitsAboveCutoff/(numDecoyHitsAboveCutoff + numLibHitsAboveCutoff);
			scorePvalueMap.put(cutoff, pValue);
			double pValueBaseAll = (double)numDecoyHitsAboveCutoff/totalHits;
			scorePvalueBaseAllMap.put(cutoff, pValueBaseAll);			
			double fdr = percentIncorrectHits * numDecoyHitsAboveCutoff / numLibHitsAboveCutoff;
			scoreFdrMap.put(cutoff, fdr);
		}
		for(PepSearchOutputObject poo : mergedData) {

			Double fdr = scoreFdrMap.get(poo.getScore());
			if(fdr != null)
				poo.setFdr(fdr);
			
			Double pVal = scorePvalueMap.get(poo.getScore());
			if(pVal != null)
				poo.setpValue(pVal);
			
			Double pValBaseAll = scorePvalueBaseAllMap.get(poo.getScore());
			if(pValBaseAll != null)
				poo.setpValueBaseAll(pValBaseAll);
		}
//		//	Calculate q-values based on pValues
//		double[] pValueList = mergedData.stream().mapToDouble(p -> p.getpValue()).sorted().toArray();
		
//		Calculate q-values based on pValues BaseAll
		double[] pValueList = mergedData.stream().mapToDouble(p -> p.getpValueBaseAll()).sorted().toArray();
		TreeMap<Double,Double>pValueQvalueMap = new TreeMap<Double,Double>();
		DescriptiveStatistics ds = new DescriptiveStatistics();
		
		//	Wrong, map will screw up, have to use Integer/ Double?
		for(int i=pValueList.length-1; i>=0; i--) {
			
			double qVal = (pValueList[i]) * totalHits / (i+1);
			pValueQvalueMap.put(pValueList[i], qVal);
			if(qVal > 0.0d) {
				
				ds.addValue(qVal);
				if(qVal > ds.getMin())
					pValueQvalueMap.put(pValueList[i], ds.getMin());
			}
		}
		for(PepSearchOutputObject poo : mergedData) {
			
//			Double qVal = pValueQvalueMap.get(poo.getpValue());
			Double qVal = pValueQvalueMap.get(poo.getpValueBaseAll());
			if(qVal != null)
				poo.setqValue(qVal);
		}		
		File mergedFile = Paths.get(percolatorOutputDirectory.getAbsolutePath(), 
				FilenameUtils.getBaseName(decoyPepSearchOutputFile.getName()) + "_merged.txt").toFile();
		try {
			NISTPepSearchResultManipulator.writeMergedDataToFile(mergedData, mergedFile);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		File percolatorInputFile = Paths.get(percolatorOutputDirectory.getAbsolutePath(), 
				FilenameUtils.getBaseName(decoyPepSearchOutputFile.getName()) + ".pin").toFile();
		try {
			NISTPepSearchResultManipulator.convertMergedResultFileToPinFormat(mergedFile, percolatorInputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			runPercolator(percolatorInputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void runPercolator(File percolatorInputFile) throws Exception {
				
		File percolatorResultFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_psms.tsv").toFile();
		File percolatorDecoyResultFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_decoy_psms.tsv").toFile();
		File percolatorLogFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_log.txt").toFile();			

		if(percolatorInputFile != null && percolatorInputFile.exists()) {
			
			 ProcessBuilder pb =
					   new ProcessBuilder(MRC2ToolBoxConfiguration.getPercolatorBinaryPath(), 
							   "--results-psms", percolatorResultFile.getAbsolutePath(),
							   "--decoy-results-psms", percolatorDecoyResultFile.getAbsolutePath(),
							   "--only-psms",
							   "--post-processing-tdc",
							   "--num-threads", "6",
							   percolatorInputFile.getAbsolutePath());
			try {
				pb.redirectErrorStream(true);
				pb.redirectOutput(Redirect.appendTo(percolatorLogFile));
				Process p = pb.start();
				assert pb.redirectInput() == Redirect.PIPE;
				assert pb.redirectOutput().file() == percolatorLogFile;
				assert p.getInputStream().read() == -1;
				int exitCode = p.waitFor();
				if (exitCode == 0) {

					p.destroy();
					if (!percolatorResultFile.exists() || !percolatorDecoyResultFile.exists()) {
						System.out.println("Percolator failed to create result files.");
					}
				} else {
					System.out.println("Percolator run failed.");
				}
			} catch (IOException e) {
				System.out.println("Percolator run failed.");
				e.printStackTrace();
			}		 
		}
	}
}









