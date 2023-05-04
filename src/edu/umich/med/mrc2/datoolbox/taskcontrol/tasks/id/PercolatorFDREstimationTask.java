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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.PercolatorOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsfdr.MergeType;
import edu.umich.med.mrc2.datoolbox.msmsfdr.NISTPepSearchResultManipulator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;

public class PercolatorFDREstimationTask extends NISTMsPepSearchTask implements TaskListener {
	
	private Collection<MSFeatureInfoBundle>featureList;
	private NISTPepSearchParameterObject searchParameters;
	private File decoySearchInputFile;
	private File decoySearchResultFile;
	private File percolatorInputFile;
	private File percolatorResultFile;
	private File percolatorDecoyResultFile;
	private File percolatorLogFile;
	private Collection<String>messageLog;
	private Map<String,MsFeatureIdentity>decoyHitMap;

	public PercolatorFDREstimationTask(
			Collection<MSFeatureInfoBundle> featureList,
			NISTPepSearchParameterObject searchParameters) {
		super();
		this.featureList = featureList;
		this.searchParameters = searchParameters;
		messageLog = new ArrayList<String>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Staring decoy library search ...";
		total = 100;
		processed = 30;
		initDecoySearchTask();
	}
	
	private void initDecoySearchTask() {
		
		Collection<MSFeatureInfoBundle> featuresToSearch = featureList.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
				collect(Collectors.toList());
		
		//	Results file
		String timestamp = 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		decoySearchResultFile = 
				Paths.get(System.getProperty("user.dir"), "data", "mssearch",
					"NIST_MSMS_PEPSEARCH_RESULTS_" + timestamp + ".TXT").toFile();
		
		decoySearchInputFile = 
				Paths.get(System.getProperty("user.dir"), "data", "mssearch",
					"NIST_MSMS_PEPSEARCH_INPUT_" + timestamp + ".MSP").toFile();
		List<String> commandParts =  
				NISTPepSearchUtils.createPepsearchcommandFromParametersObject(
					searchParameters,
					decoySearchInputFile,
					decoySearchResultFile);
		NISTMspepSearchOfflineTask task = new NISTMspepSearchOfflineTask(
				commandParts, 
				featuresToSearch,
				decoySearchInputFile, 
				decoySearchResultFile);	
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private Collection<PepSearchOutputObject>createPsoObjectsFromLibraryHits(
			Collection<MSFeatureInfoBundle> featureList){
		
		taskDescription = "Processing library hits ...";
		Collection<MSFeatureInfoBundle> featuresToSearch = 
				MsFeatureStatsUtils.getFeaturesWithMSMSLibMatch(featureList);		
		total = featuresToSearch.size();
		processed = 0;
		
		Collection<PepSearchOutputObject>psoCollection = 
				new ArrayList<PepSearchOutputObject>();
		MSMSMatchType matchType = MSMSMatchType.Regular;
		if(searchParameters.getHiResSearchOption().equals(HiResSearchOption.y))
			matchType = MSMSMatchType.Hybrid;

		if(searchParameters.getHiResSearchOption().equals(HiResSearchOption.u))
			matchType = MSMSMatchType.InSource;		

		Map<String,String>libraryIdNameMap = createLibraryIdNameMap(featuresToSearch);
		
		for(MSFeatureInfoBundle bundle : featuresToSearch) {
				
			MsFeature feature = bundle.getMsFeature();
			PepSearchOutputObject poo = new PepSearchOutputObject(
					feature.getSpectrum().getExperimentalTandemSpectrum().getId());
			
			ReferenceMsMsLibraryMatch libMatch = 
					feature.getPrimaryIdentity().getReferenceMsMsLibraryMatch();
			String libName = libraryIdNameMap.get(
					libMatch.getMatchedLibraryFeature().getMsmsLibraryIdentifier());

			poo.setLibraryName(libName);
			poo.setMrc2libid(libMatch.getMatchedLibraryFeature().getUniqueId());
			double deltaMz = 
					MsUtils.getMassErrorForIdentity(
							feature, feature.getPrimaryIdentity(), MassErrorType.Da);			
			poo.setDeltaMz(deltaMz);		
			poo.setScore(libMatch.getScore());
			poo.setDotProduct(libMatch.getDotProduct());
			poo.setProbablility(libMatch.getProbability());
			poo.setReverseDotProduct(libMatch.getReverseDotProduct());	
			poo.setHybridDotProduct(libMatch.getHybridDotProduct());
			poo.setHybridScore(libMatch.getHybridScore());
			poo.setHybridDeltaMz(libMatch.getHybridDeltaMz());
			poo.setMatchType(matchType);	
		
			psoCollection.add(poo);
			processed++;
		}	
		return psoCollection;
	}
	
	private Map<String,String>createLibraryIdNameMap(Collection<MSFeatureInfoBundle> featuresToSearch){
		
		Set<String> libIds = featuresToSearch.stream().
				map(b -> b.getMsFeature()).
				map(f -> f.getPrimaryIdentity().getReferenceMsMsLibraryMatch().
						getMatchedLibraryFeature().getMsmsLibraryIdentifier()).
				collect(Collectors.toSet());
		
		Map<String,String>libraryIdNameMap = new TreeMap<String,String>();
		for(String id : libIds) {
			ReferenceMsMsLibrary lib = IDTDataCache.getReferenceMsMsLibraryById(id);
			if(lib != null)
				libraryIdNameMap.put(id, lib.getPrimaryLibraryId());
		}		
		return libraryIdNameMap;
	}
	
	private void runFDRAnalysis() {
		try {
			writePercolatorInputFile();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(getStatus().equals(TaskStatus.PROCESSING)) {
			try {
				runPercolator();
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		if(getStatus().equals(TaskStatus.PROCESSING)) {
			try {
				parsePercolatorOutput();
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void writePercolatorInputFile() throws Exception {
		
		Collection<PepSearchOutputObject>libraryHits = 
				createPsoObjectsFromLibraryHits(featureList);
		
		Collection<PepSearchOutputObject>decoyHits = 
				parseDecoySearchResults();
					
		if(decoyHits == null) {
			setStatus(TaskStatus.ERROR);
			return;
		}
		decoyHits.stream().forEach(p -> p.setDecoy(true));
		Collection<PepSearchOutputObject>mergedData = 
				NISTPepSearchResultManipulator.mergeLibraryAndDecoyHits(
						libraryHits, 
						decoyHits, 
						MergeType.BEST_OVERALL);
		//	Check if any decoy hits included
		List<PepSearchOutputObject> decoysInMerged = 
				mergedData.stream().filter(p -> p.isDecoy()).
				collect(Collectors.toList());
		if(decoysInMerged.isEmpty()) {
			errorMessage = "No decoy hits added to the merged library/decoy data\n"
					+ "All decoy hit scores lower than library hit scores\n"
					+ "for corresponding features.";
			messageLog.add(errorMessage);
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			createMsFeatureIdentitiesFromDecoyHits(mergedData);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		File mergedFile = Paths.get(decoySearchResultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoySearchResultFile.getName()) + "_merged.txt").toFile();
		try {
			NISTPepSearchResultManipulator.writeMergedDataToFile(mergedData, mergedFile);
		} catch (Exception e) {			
			errorMessage = "Failed to create Percolator input file";
			messageLog.add(errorMessage);
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		percolatorInputFile = Paths.get(decoySearchResultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoySearchResultFile.getName()) + ".pin").toFile();
		try {
			NISTPepSearchResultManipulator.convertMergedResultFileToPinFormat(mergedFile, percolatorInputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createMsFeatureIdentitiesFromDecoyHits(
			Collection<PepSearchOutputObject> mergedData) throws Exception {

		List<PepSearchOutputObject> decoyHits = mergedData.stream().
				filter(p -> p.isDecoy()).collect(Collectors.toList());
		taskDescription = "Mapping decoy hits ...";
		total = decoyHits.size();
		processed = 0;		
		decoyHitMap = new TreeMap<String,MsFeatureIdentity>();

		Connection conn = ConnectionManager.getConnection();		
		for(PepSearchOutputObject poo : decoyHits) {
			
			MsMsLibraryFeature matchedLibraryFeature = 
					MSMSLibraryUtils.getMsMsLibraryFeatureById(poo.getMrc2libid(), conn);			
			ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
					matchedLibraryFeature,
					poo,
					searchParameters.getId());
			
			MsFeatureIdentity id = new MsFeatureIdentity(
					matchedLibraryFeature.getCompoundIdentity(),
					CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
			id.setIdSource(CompoundIdSource.LIBRARY_MS2);
			id.setReferenceMsMsLibraryMatch(match);			
			decoyHitMap.put(poo.getMsmsFeatureId(), id);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private Collection<PepSearchOutputObject> parseDecoySearchResults() throws Exception {

		if(!decoySearchResultFile.exists()) {
			errorMessage = "No decoy search resuls file found";
			messageLog.add(errorMessage);
			setStatus(TaskStatus.FINISHED);
			return null;
		}
		String[][] searchData = null;
		try {
			searchData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						decoySearchResultFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(searchData == null) {
			errorMessage = "No decoy matches found";
			messageLog.add(errorMessage);
			setStatus(TaskStatus.FINISHED);
			return null;
		}
		return NISTPepSearchUtils.parsePepSearchOutputToObjects(
				searchData, searchParameters);
	}
	
	private void runPercolator() throws Exception {
		
		taskDescription = "Running Percolator ...";
		total = 100;
		processed = 30;	
		
		percolatorResultFile = Paths.get(decoySearchResultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoySearchResultFile.getName()) + "_percolator_psms.tsv").toFile();
		percolatorDecoyResultFile = Paths.get(decoySearchResultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoySearchResultFile.getName()) + "_percolator_decoy_psms.tsv").toFile();
		percolatorLogFile = Paths.get(decoySearchResultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoySearchResultFile.getName()) + "_percolator_log.txt").toFile();			

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
					if (getStatus().equals(TaskStatus.CANCELED)) {
						try {
							p.destroyForcibly();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (exitCode == 0 && getStatus().equals(TaskStatus.PROCESSING)) {

						p.destroy();
						if (!percolatorResultFile.exists() || !percolatorDecoyResultFile.exists()) {
							errorMessage = "Percolator failed to create result files.";
							messageLog.add(errorMessage);
							addPercollatorLogToErrors();
							setStatus(TaskStatus.ERROR);
						}
					} else {
						errorMessage = "Percolator run failed.";
						messageLog.add(errorMessage);
						addPercollatorLogToErrors();
						setStatus(TaskStatus.ERROR);
					}
				} catch (IOException e) {
					errorMessage = "Percolator run failed.";
					messageLog.add(errorMessage);
					addPercollatorLogToErrors();
					setStatus(TaskStatus.ERROR);
					e.printStackTrace();
				}
			 processed = 100;		 
		}
	}
	
	private void addPercollatorLogToErrors() {
		
		if(percolatorLogFile.exists()) {
			List<String> pLog = new ArrayList<String>();
			try {
				pLog = Files.readAllLines(Paths.get(percolatorLogFile.getAbsolutePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			messageLog.addAll(pLog);
		}
	}
	
	private void parsePercolatorOutput() throws Exception {
		
		if(!percolatorResultFile.exists() || !percolatorDecoyResultFile.exists())
			return;
		
		taskDescription = "Parsing Percolator output ...";
		total = 100;
		processed = 30;	
		
		Collection<PercolatorOutputObject>libResults =  
				NISTPepSearchResultManipulator.parsePercolatorOutputFile(percolatorResultFile);
		
		Collection<PercolatorOutputObject>decoyResults =  
				NISTPepSearchResultManipulator.parsePercolatorOutputFile(percolatorDecoyResultFile);

		if(libResults == null || decoyResults == null) {
			
			errorMessage = "Failed to parse Percolator result files.";
			messageLog.add(errorMessage);
			setStatus(TaskStatus.ERROR);
		}
		Collection<MSFeatureInfoBundle> featuresToUpdate = 
				MsFeatureStatsUtils.getFeaturesWithMSMSLibMatch(featureList);		
		updateLibraryMatchesWithPercolatorResults(libResults, featuresToUpdate);
		updateDecoyMatchesWithPercolatorResults(decoyResults, featuresToUpdate);		
	}
	
	private void updateDecoyMatchesWithPercolatorResults(
			Collection<PercolatorOutputObject>decoyResults,
			Collection<MSFeatureInfoBundle> featuresToUpdate) {
		
		taskDescription = "Updating decoy matches with Percolator results ...";
		total = decoyResults.size();
		processed = 0;
		
		for(PercolatorOutputObject percRes : decoyResults) {
			
			MsFeatureIdentity id = decoyHitMap.get(percRes.getMsmsFeatureId());
			if(id != null) {				
				ReferenceMsMsLibraryMatch libMatch = id.getReferenceMsMsLibraryMatch();
				libMatch.setPercolatorScore(percRes.getScore());
				libMatch.setqValue(percRes.getqValue());
				libMatch.setPosteriorErrorProbability(percRes.getPosteriorErrorProbablility());
			}
			processed++;
		}
		for(MSFeatureInfoBundle b : featuresToUpdate) {
						
			MsFeatureIdentity id = 
					decoyHitMap.get(b.getMsFeature().getSpectrum().
							getExperimentalTandemSpectrum().getId());
			if(id != null)
				b.getMsFeature().addIdentity(id);
		}
	}
	
	private void updateLibraryMatchesWithPercolatorResults(
			Collection<PercolatorOutputObject>libResults,
			Collection<MSFeatureInfoBundle> featuresToUpdate) {
		
		taskDescription = "Updating library matches with Percolator results ...";
		total = featuresToUpdate.size();
		processed = 0;
		for(MSFeatureInfoBundle b : featuresToUpdate) {
			
			String msmsId = b.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getId();
			PercolatorOutputObject percRes = libResults.stream().
					filter(p -> p.getMsmsFeatureId().equals(msmsId)).
					findFirst().orElse(null);
			if(percRes != null) {
				
			 ReferenceMsMsLibraryMatch libMatch = 
					 b.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch();
				//	Just to check for now
				if(!libMatch.getMatchedLibraryFeature().getUniqueId().equals(percRes.getMrc2libid()))
					System.out.println("LibId mismatch");
				
				libMatch.setPercolatorScore(percRes.getScore());
				libMatch.setqValue(percRes.getqValue());
				libMatch.setPosteriorErrorProbability(percRes.getPosteriorErrorProbablility());
			}
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		return new PercolatorFDREstimationTask(
				featureList, searchParameters);
	}

	public Collection<MSFeatureInfoBundle> getFeatureList() {
		return featureList;
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.ERROR) {
			if (e.getSource().getClass().equals(NISTMspepSearchOfflineTask.class)) {
				messageLog.add(e.getSource().getErrorMessage());
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(NISTMspepSearchOfflineTask.class)) {
				try {
					insertPepSearchParametersInDatabase();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				runFDRAnalysis();
			}
		}
	}

	private void insertPepSearchParametersInDatabase() {
	
		if(searchParameters != null) {
			try {
				IdentificationUtils.addNewPepSearchParameterSet(searchParameters);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Collection<String> getMessageLog() {
		return messageLog;
	}
}
