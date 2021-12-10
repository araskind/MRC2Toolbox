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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;

public class NISTMsPepSearchRoundTripTask extends NISTMsPepSearchTask {

	protected Collection<MsFeatureInfoBundle>featuresToSearch;	
	protected File inputFile;	
	private Semaphore outputSem;
	private Semaphore errorSem;
	private String output;	
	private String error;
	private Process p;
	private boolean skipResultsUpload;
	
	public NISTMsPepSearchRoundTripTask(
			String searchCommand,
			Collection<MsFeatureInfoBundle> featuresToSearch,
			File inputFile,
			File resultFile) {
		super();
		this.searchCommand = searchCommand;
		this.featuresToSearch = featuresToSearch;
		this.inputFile = inputFile;
		this.resultFile = resultFile;
		skipResultsUpload = false;
		total = 100;
		processed = 20;
		taskDescription = "Running NIST MS/MS search";
	}
	
	public NISTMsPepSearchRoundTripTask(
			List<String>commandParts,
			Collection<MsFeatureInfoBundle> featuresToSearch,
			File inputFile,
			File resultFile) {
		super();
		this.commandParts = commandParts;
		this.featuresToSearch = featuresToSearch;
		this.inputFile = inputFile;
		this.resultFile = resultFile;
		skipResultsUpload = false;
		total = 100;
		processed = 20;
		taskDescription = "Running NIST MS/MS search";
	}

	@Override
	public void run() {

		pooList = new ArrayList<PepSearchOutputObject>();
		setStatus(TaskStatus.PROCESSING);
		try {
			createInputMspFile();
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			runPepSearch();
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			parseAndFilterSearchResults(skipResultsUpload);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}		
		if(pooList.size() > 0) {
			
			IDTDataCash.refreshNISTPepSearchParameters();
			if(skipResultsUpload) {
				try {
					updateOfflineFeatureIdentifications();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();	
				}
				assignTopHits();
			}
			else {
				try {
					uploadSearchResults();
				} catch (Exception e) {
					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
				try {
					updateFeatureIdentifications();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// Cleanup working directory
		cleanupWorkingDirectory();
		setStatus(TaskStatus.FINISHED);
	}

	private void assignTopHits() {

		taskDescription = "Assigning default ids ...";
		total = featuresToSearch.size();
		processed = 0;	
		
		Map<String,HiResSearchOption>searchTypeMap = 
				NISTPepSearchUtils.getSearchTypeMap(featuresToSearch);			
		MSFeatureIdentificationLevel tentativeLevel = 
				IDTDataCash.getMSFeatureIdentificationLevelById("IDS002");
				
		for(MsFeatureInfoBundle bundle : featuresToSearch) {
			
			if(bundle.getMsFeature().getIdentifications().isEmpty()) {
				processed++;
				continue;
			}		
			Map<HiResSearchOption,Collection<MsFeatureIdentity>>hitTypeMap = 
					NISTPepSearchUtils.getSearchTypeIdentityMap(bundle.getMsFeature(), searchTypeMap);
			
			MsFeatureIdentity topNormalHit = null;
			MsFeatureIdentity topInSourceHit = null;
			MsFeatureIdentity topHybridHit = null;
			
			if(!hitTypeMap.get(HiResSearchOption.z).isEmpty())
				topNormalHit = hitTypeMap.get(HiResSearchOption.z).iterator().next();
			
			if(!hitTypeMap.get(HiResSearchOption.u).isEmpty()) 
				topInSourceHit = hitTypeMap.get(HiResSearchOption.u).iterator().next();
			
			if(!hitTypeMap.get(HiResSearchOption.y).isEmpty())
				topHybridHit = hitTypeMap.get(HiResSearchOption.y).iterator().next();
			
			if(topNormalHit != null) {
				bundle.getMsFeature().setPrimaryIdentity(topNormalHit);
			}
			else {
				if(topInSourceHit != null) {
					bundle.getMsFeature().setPrimaryIdentity(topInSourceHit);
				}
				else {
					if(topHybridHit != null)
						bundle.getMsFeature().setPrimaryIdentity(topHybridHit);
				}
			}
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
			if(primaryId.getIdentificationLevel() == null)
				primaryId.setIdentificationLevel(tentativeLevel);
			
			processed++;
		}
//		MsFeatureIdentityMSMSScoreComparator sorter = 
//				new MsFeatureIdentityMSMSScoreComparator();
//		for(PepSearchOutputObject poo : pooList) {
//			
//			MsFeature msf = msmsIdMap.get(poo.getMsmsFeatureId());
//			if(msf.getIdentifications().size() == 1 && msf.getPrimaryIdentity() == null)
//				msf.setPrimaryIdentity(msf.getIdentifications().iterator().next());
//			
//			if(msf.getIdentifications().size() > 1) {
//				MsFeatureIdentity primaryId = msf.getIdentifications().stream().
//						sorted(sorter).findFirst().orElse(null);
//				if(primaryId != null)
//					msf.setPrimaryIdentity(primaryId);
//			}					
//			processed++;
//		}
	}

	private void updateOfflineFeatureIdentifications() throws Exception {

		taskDescription = "Updating identification data ...";
		total = pooList.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();	
		for(PepSearchOutputObject poo : pooList) {
			
			MsFeature msf = msmsIdMap.get(poo.getMsmsFeatureId());
			MsMsLibraryFeature msmsId =
					MSMSLibraryUtils.getMsMsLibraryFeatureById(
							poo.getMrc2libid(), conn);
			if(msmsId == null)
				continue;

			String msmsMatchId = DataPrefix.MSMS_LIBRARY_MATCH.getName() + 
					UUID.randomUUID().toString().substring(0, 15);
			MsFeatureIdentity id = new MsFeatureIdentity(msmsId.getCompoundIdentity(),
					CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
			id.setIdSource(CompoundIdSource.LIBRARY_MS2);
			id.setUniqueId(msmsMatchId);		
			MSMSMatchType matchType = 
					MSMSMatchType.getMSMSMatchTypeByName(poo.getMatchType().name());		
			ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
					msmsId,
					poo.getScore(), 
					0.0d,
					0.0d, 
					poo.getProbablility(), 
					poo.getDotProduct(), 
					poo.getReverseDotProduct(),
					poo.getHybridDotProduct(),
					poo.getHybridScore(),
					poo.getHybridDeltaMz(),
					matchType,
					poo.isDecoy(),
					pepSearchParameterObject.getId());			
			id.setReferenceMsMsLibraryMatch(match);
			match.setEntropyBasedScore(
					MSMSScoreCalculator.calculateEntropyMatchScore(
							msf.getSpectrum().getExperimentalTandemSpectrum(), match));
			msf.addIdentity(id);						
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	protected void createInputMspFile() throws IOException {

		taskDescription = "Wtiting MSP output";
		total = featuresToSearch.size();
		processed = 0;
		
		List<MsFeatureInfoBundle> msmsFeatures = featuresToSearch.stream().
			filter(f -> f.getMsFeature().hasInstrumentMsMs()).
			collect(Collectors.toList());
		featuresToSearch.clear();
		featuresToSearch.addAll(msmsFeatures);		

		final Writer writer = new BufferedWriter(new FileWriter(inputFile));
		for(MsFeatureInfoBundle bundle : featuresToSearch) {

			MsFeature msf = bundle.getMsFeature();
			TandemMassSpectrum tandemMs = 
					msf.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);

			//	TODO check why this happens??
			if(tandemMs.getSpectrum().isEmpty())
				continue;

			msmsIdMap.put(tandemMs.getId(), msf);
			defaultIdUpdateMap.put(tandemMs.getId(), allowDefaultIdUpdate(bundle));
			
			writer.append(MSPField.NAME.getName() + ": " + tandemMs.getId() + "\n");
			writer.append("Feature name: " + msf.getName() + "\n");
			if(msf.isIdentified()) {
				CompoundIdentity cid = msf.getPrimaryIdentity().getCompoundIdentity();
				writer.append(MSPField.SYNONYM.getName() + ": " + cid.getName() + "\n");
				if(cid.getFormula() != null)
					writer.append(MSPField.FORMULA.getName() + ": " + cid.getFormula() + "\n");
				if(cid.getInChiKey() != null)
					writer.append(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey() + "\n");
			}
			String polarity = "P";
			if(msf.getPolarity().equals(Polarity.Negative))
				polarity = "N";
			writer.append(MSPField.ION_MODE.getName() + ": " + polarity + "\n");

			if(tandemMs.getCidLevel() >0)
				writer.append(MSPField.COLLISION_ENERGY.getName() + ": " + Double.toString(tandemMs.getCidLevel()) + "\n");

			//	RT
			writer.append(MSPField.RETENTION_INDEX.getName() + ": " +
					MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRetentionTime()) + " min.\n");

			//	Comments
			String comment = MSPField.COMMENT.getName() + ": ";
			if(bundle.getAcquisitionMethod() != null)
				comment += "Acq. method: " + bundle.getAcquisitionMethod().getName() + "; ";
			
			if(bundle.getDataExtractionMethod() != null)
				comment += "DA method: " + bundle.getDataExtractionMethod().getName();
			
			comment += "\n";
			writer.append(comment);
			
			writer.append(MSPField.PRECURSORMZ.getName() + ": " +
				MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()) + "\n");
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()) + "\n");

			MsPoint[] msms = MsUtils.normalizeAndSortMsPatternForMsp(tandemMs.getSpectrum());
			int pointCount = 0;
			for(MsPoint point : msms) {

				writer.append(
					MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
					+ " " + intensityFormat.format(point.getIntensity()) + "; ") ;
				pointCount++;
				if(pointCount % 5 == 0)
					writer.append("\n");
			}
			writer.append("\n\n");			
			processed++;
		}
		writer.flush();
		writer.close();
	}
	
	protected void runPepSearch() {
		
		taskDescription = "Running NIST MS/MS pep-search";
		if(searchCommand != null) {
			initLogFile();
			try {
				Runtime runtime = Runtime.getRuntime();	
				p = runtime.exec(searchCommand);
				new OutputReader().start();
				new ErrorReader().start();
				int exitCode = p.waitFor();
				if(exitCode == 0) {
	        		p.destroy();
	        		addLogLine(getOutput());
				}
				else {
					errorMessage = getError();
					System.out.println("PepSearch error");
					System.out.println(errorMessage);
					System.out.println(searchCommand);
					addLogLine(getOutput());
	        		addLogLine(errorMessage);
					setStatus(TaskStatus.ERROR);
				}
			}
			catch (IOException e1) {
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
			} catch (InterruptedException e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		if(commandParts != null) {
			
			ProcessBuilder pb = new ProcessBuilder(commandParts);
			logPath = Paths.get(inputFile.getParentFile().getAbsolutePath(), 
							FilenameUtils.getBaseName(inputFile.getName()) + "_PEPSEARCH.LOG");
			File logFile = logPath.toFile();
			try {
				pb.redirectErrorStream(true);
				pb.redirectOutput(Redirect.appendTo(logFile));
				Process p = pb.start();
				assert pb.redirectInput() == Redirect.PIPE;
				assert pb.redirectOutput().file() == logFile;
				assert p.getInputStream().read() == -1;
				int exitCode = p.waitFor();
				if (getStatus().equals(TaskStatus.CANCELED)) {
					try {
						p.destroyForcibly();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (exitCode == 0)
					p.destroy();
				else 
					setStatus(TaskStatus.ERROR);				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private class OutputReader extends Thread {
		public OutputReader() {
			try {
				outputSem = new Semaphore(1);
				outputSem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				StringBuffer readBuffer = new StringBuffer();
				BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String buff = new String();
				while ((buff = isr.readLine()) != null) {
					readBuffer.append(buff);
					System.out.println(buff);
				}
				output = readBuffer.toString();
				outputSem.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ErrorReader extends Thread {
		public ErrorReader() {
			try {
				errorSem = new Semaphore(1);
				errorSem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				StringBuffer readBuffer = new StringBuffer();
				BufferedReader isr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String buff = new String();
				while ((buff = isr.readLine()) != null) {
					readBuffer.append(buff);
				}
				error = readBuffer.toString();
				errorSem.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (error.length() > 0)
				System.out.println(error);
		}
	}
		
	public String getOutput() {
		try {
			outputSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String value = output;
		outputSem.release();
		return value;
	}

	public String getError() {
		try {
			errorSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String value = error;
		errorSem.release();
		return value;
	}	

	private void updateFeatureIdentifications() throws Exception {

		taskDescription = "Updating identification data ...";
		total = msmsIdMap.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();	
		String query =
			"SELECT MATCH_ID, MRC2_LIB_ID, MATCH_SCORE, FWD_SCORE, REVERSE_SCORE, " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, IS_PRIMARY, IDENTIFICATION_LEVEL_ID, " +
			"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH " +
			"FROM MSMS_FEATURE_LIBRARY_MATCH M " +
			"WHERE MSMS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);	
		Set<String> fidsToUpdate = 
				pooList.stream().map(p -> p.getMsmsFeatureId()).
				distinct().collect(Collectors.toSet());
		
		for (Entry<String, MsFeature> entry : msmsIdMap.entrySet()) {
			
			if(!fidsToUpdate.contains(entry.getKey())) {
				processed++;
				continue;
			}
			MsFeature msf = entry.getValue();
			msf.clearIdentification();
			
			ps.setString(1, entry.getKey());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {

				MsMsLibraryFeature msmsId =
						MSMSLibraryUtils.getMsMsLibraryFeatureById(
								rs.getString("MRC2_LIB_ID"), conn);
				if(msmsId == null)
					continue;

				MsFeatureIdentity id = new MsFeatureIdentity(msmsId.getCompoundIdentity(),
						CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
				id.setIdSource(CompoundIdSource.LIBRARY_MS2);
				id.setUniqueId(rs.getString("MATCH_ID"));
				
				MSMSMatchType matchType = 
						MSMSMatchType.getMSMSMatchTypeByName(rs.getString("MATCH_TYPE"));
				
				ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
						msmsId,
						rs.getDouble("MATCH_SCORE"), 
						rs.getDouble("FWD_SCORE"),
						rs.getDouble("REVERSE_SCORE"), 
						rs.getDouble("PROBABILITY"), 
						rs.getDouble("DOT_PRODUCT"), 
						rs.getDouble("REVERSE_DOT_PRODUCT"),
						rs.getDouble("HYBRID_DOT_PRODUCT"),
						rs.getDouble("HYBRID_SCORE"),
						rs.getDouble("HYBRID_DELTA_MZ"),
						matchType,
						rs.getString("DECOY_MATCH") != null,
						rs.getString("SEARCH_PARAMETER_SET_ID"));
				
				id.setReferenceMsMsLibraryMatch(match);
				if(rs.getString("IS_PRIMARY") != null)
					id.setPrimary(true);

				String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
				if(statusId != null) 
					id.setIdentificationLevel(IDTDataCash.getMSFeatureIdentificationLevelById(statusId));
				
				msf.addIdentity(id);
				if(id.isPrimary())
					msf.setPrimaryIdentity(id);
			}
			rs.close();
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	private void cleanupWorkingDirectory() {
		// TODO remove tmp files?

	}

	@Override
	public Task cloneTask() {
		
		if(searchCommand != null)
			return new NISTMsPepSearchRoundTripTask(
					searchCommand, featuresToSearch, inputFile, resultFile);
		else if(commandParts != null)
			return new NISTMsPepSearchRoundTripTask(
					commandParts, featuresToSearch, inputFile, resultFile);
		else
			return null;
	}

	public void setSkipResultsUpload(boolean skipResultsUpload) {
		this.skipResultsUpload = skipResultsUpload;
	}
}