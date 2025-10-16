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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibMatchedSimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IDTCefMSMSPrescanOrImportTask extends CEFProcessingTask {


	private Collection<CompoundIdentity>missingIdentities;
	private Map<CompoundIdentity, Collection<TandemMassSpectrum>>idSpectrumMap;
	private DataExtractionMethod dataExtractionMethod;
	private HashSet<LibMatchedSimpleMsFeature>features;
	private Collection<String> importLog;
	private boolean uploadData;
	private DataPipeline dataPipeline;
	private String injectionId;
	
	private static final int BATCH_SIZE = 100;

	public IDTCefMSMSPrescanOrImportTask(
			File inputCefFile, 
			String injectionId,
			DataExtractionMethod dataExtractionMethod,
			boolean uploadData) {
		super();
		this.inputCefFile = inputCefFile;
		this.injectionId = injectionId;
		this.dataExtractionMethod = dataExtractionMethod;
		this.uploadData = uploadData;
		dataPipeline = new DataPipeline(null, dataExtractionMethod);
		missingIdentities = new HashSet<CompoundIdentity>();
		idSpectrumMap = new HashMap<CompoundIdentity, Collection<TandemMassSpectrum>>();
		features = new HashSet<LibMatchedSimpleMsFeature>();
		importLog = new ArrayList<String>();
	}

	public IDTCefMSMSPrescanOrImportTask(File inputCefFile) {
		this(inputCefFile, null, null, false);
	}
	
	public IDTCefMSMSPrescanOrImportTask(
			DataFile dataFile, 
			DataExtractionMethod dataExtractionMethod, 
			boolean uploadData) {

		this(new File(dataFile.getFullPath()),
				dataFile.getInjectionId(),
				dataExtractionMethod,
				uploadData);
	}

	@Override
	public void run() {

		if(uploadData)
			taskDescription = "Importing data from " + inputCefFile.getName();
		else
			taskDescription = "Pre-scanning data from " + inputCefFile.getName();
		
		setStatus(TaskStatus.PROCESSING);
		try {
			parseInputCefFile(inputCefFile);
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(inputFeatureList == null || inputFeatureList.isEmpty()) {
			errorMessage = "No features found in input file";
			setStatus(TaskStatus.ERROR);
			return;
		}
//		inputFeatureList.stream().
//			forEach(f -> features.add(new LibMatchedSimpleMsFeature(f, dataPipeline)));
		try {
			mapCompoundIdsToDatabase();
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			mapMSMSLibraryMatchesToDatabase();
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(uploadData) {
			
			if(dataExtractionMethod == null || injectionId == null) {
				setStatus(TaskStatus.FINISHED);
				return;
			}				
			if(!missingIdentities.isEmpty() || !idSpectrumMap.isEmpty()) {
				importLog.add("Data can not be imported from " + 
						inputCefFile.getName() + 
						" before the compound IDs and/or MSMS library entries are  resolved.");				
				setStatus(TaskStatus.FINISHED);
				return;
			}
			try {
				uploadParsedData();
			}
			catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}		
		setStatus(TaskStatus.FINISHED);
	}
	
	private void mapCompoundIdsToDatabase() throws Exception {

		taskDescription = "Mapping identifications to compound database for " + inputCefFile.getName();
		total = features.size();
		processed = 0;		
		Connection conn = ConnectionManager.getConnection();
		
		//	Map compound identity queries
		String accessionQuery = 
				"SELECT ACCESSION FROM COMPOUND_DATA "
				+ "WHERE ACCESSION = ? and SOURCE_DB = ?";
		PreparedStatement accessionPs = conn.prepareStatement(accessionQuery);
		ResultSet accessionRs = null;
		
		String crossrefQuery = 
				"SELECT ACCESSION FROM COMPOUND_CROSSREF "
				+ "WHERE SOURCE_DB_ID = ? AND SOURCE_DB = ?";
		PreparedStatement crossrefPs = conn.prepareStatement(crossrefQuery);
		ResultSet crossrefRs = null;
		
		String synonymAndFormulaQuery = 
				"SELECT D.ACCESSION FROM COMPOUND_SYNONYMS S, COMPOUND_DATA D "
				+ "WHERE UPPER(S.NAME) = ? AND D.MOL_FORMULA = ? "
				+ "AND D.ACCESSION = S.ACCESSION";
		PreparedStatement synonymAndFormulaPs = conn.prepareStatement(synonymAndFormulaQuery);
		ResultSet synonymAndFormulaRs = null;
		
		String cidQuery =
				"SELECT D.SOURCE_DB, D.PRIMARY_NAME, D.MOL_FORMULA, "
				+ "D.EXACT_MASS, D.SMILES, D.INCHI_KEY "+
				"FROM COMPOUND_DATA D WHERE D.ACCESSION = ?";
		PreparedStatement cidPs = conn.prepareStatement(cidQuery);
		ResultSet cidRs = null;
		
		//	Map MSMS library matches queries		
		for(MsFeature feature : inputFeatureList) {
			
			MsFeatureIdentity msid = feature.getPrimaryIdentity();			
			if(msid == null 
					|| msid.getIdSource().equals(CompoundIdSource.FORMULA_GENERATOR)
					|| msid.getIdSource().equals(CompoundIdSource.UNKNOWN)) {
				processed++;
				continue;
			}
			CompoundIdentity cid = msid.getCompoundIdentity();
			CompoundIdentity mappedId = null;
			String accession = null;			
			if(!cid.getDbIdMap().isEmpty()) {

				for (Entry<CompoundDatabaseEnum, String> entry : cid.getDbIdMap().entrySet()) {

					accessionPs.setString(1, entry.getValue());
					accessionPs.setString(2, entry.getKey().name());
					accessionRs = accessionPs.executeQuery();
					while (accessionRs.next())
						accession = accessionRs.getString("ACCESSION");

					accessionRs.close();
				}
				if(accession == null) {
					
					for (Entry<CompoundDatabaseEnum, String> entry : cid.getDbIdMap().entrySet()) {
						
						crossrefPs.setString(1, entry.getValue());
						crossrefPs.setString(2, entry.getKey().name());
						crossrefRs = crossrefPs.executeQuery();
						while (crossrefRs.next())
							accession = crossrefRs.getString("ACCESSION");

						crossrefRs.close();
					}
				}
			}
			if(accession == null && cid.getFormula() != null && cid.getName() != null) {
				
				synonymAndFormulaPs.setString(1, cid.getName().trim().toUpperCase());
				synonymAndFormulaPs.setString(2, cid.getFormula());
				synonymAndFormulaRs = synonymAndFormulaPs.executeQuery();
				while (synonymAndFormulaRs.next())
					accession = synonymAndFormulaRs.getString("ACCESSION");
					
				synonymAndFormulaRs.close();
			}
			if(accession != null) {				
				
				cidPs.setString(1, accession);
				cidRs = cidPs.executeQuery();
					while (cidRs.next()){

						CompoundDatabaseEnum dbSource =
								CompoundDatabaseEnum.getCompoundDatabaseByName(cidRs.getString("SOURCE_DB"));
						String commonName = cidRs.getString("PRIMARY_NAME");
						String formula = cidRs.getString("MOL_FORMULA");
						double exactMass = cidRs.getDouble("EXACT_MASS");
						String smiles = cidRs.getString("SMILES");
						mappedId = new CompoundIdentity(
								dbSource, accession, commonName,
								commonName, formula, exactMass, smiles);
						mappedId.setInChiKey(cidRs.getString("INCHI_KEY"));
					}
					cidRs.close();
			}
			if(mappedId == null) {
				mappedId = cid;
				missingIdentities.add(cid);
			}
			else {
				mappedId.addDatabaseIds(cid.getDbIdMap(), false);
				msid.setCompoundIdentity(mappedId);
			}
			processed++;
		}
		accessionPs.close();
		crossrefPs.close();
		synonymAndFormulaPs.close();
		cidPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void mapMSMSLibraryMatchesToDatabase() throws Exception {

		taskDescription = 
				"Mapping identifications to compound database for " + 
						inputCefFile.getName();
		total = features.size();
		processed = 0;	
		DecimalFormat ceFormat = new DecimalFormat("##.#");
		
		//	TODO maybe drop collision energy? it is not from METLIN, but from the experimantal MSMS
		Connection conn = ConnectionManager.getConnection();
		String idQuery =
				"SELECT C.MRC2_LIB_ID, SPECTRUM_HASH " +
				"FROM REF_MSMS_LIBRARY_COMPONENT C " +
				"WHERE C.ORIGINAL_LIBRARY_ID = ? " +
				"AND C.LIBRARY_NAME = 'METLIN' " +
				"AND C.COLLISION_ENERGY = ? " +
				"AND C.POLARITY = ?";
		PreparedStatement idPs = conn.prepareStatement(idQuery);
		ResultSet rs = null;
		for(MsFeature feature : inputFeatureList) {

			TandemMassSpectrum refMsms = 
					feature.getSpectrum().getReferenceTandemSpectrum();
			if(refMsms == null) {
				processed++;
				continue;
			}
			CompoundIdentity cid = feature.getPrimaryIdentity().getCompoundIdentity();
			if(cid == null) {
				processed++;
				continue;
			}
			String mrcMsMsId = null;
			String metlinId = null;
			if(cid.getDbId(CompoundDatabaseEnum.METLIN) != null)
				metlinId = cid.getDbId(CompoundDatabaseEnum.METLIN).replace("METLIN:", "");
			
			if(metlinId == null) {
				String importError =
						"Could not match library spectrum for " +
						cid.getName() + " in " + inputCefFile.getName() + ", no METLIN ID\n";
				importLog.add(importError);
				processed++;
				continue;
			}
			String spectrumHash = MsUtils.calculateSpectrumHash(refMsms.getSpectrum());
			String colEnLevel = Integer.toString((int)Math.round(refMsms.getCidLevel()));
//			System.out.println(colEnLevel);
			idPs.setString(1, metlinId);
			idPs.setString(2, colEnLevel);
			idPs.setString(3, refMsms.getPolarity().getCode());
			rs = idPs.executeQuery();
			while(rs.next()) {
				if(spectrumHash.equals(rs.getString("SPECTRUM_HASH")))
					mrcMsMsId = rs.getString("MRC2_LIB_ID");
			}
			rs.close();
			if(mrcMsMsId != null) {
				ReferenceMsMsLibraryMatch rmsMatch = 
						new ReferenceMsMsLibraryMatch(mrcMsMsId, feature.getPrimaryIdentity().getScoreCarryOver());
				rmsMatch.setMatchType(MSMSMatchType.Regular);
				feature.getPrimaryIdentity().setReferenceMsMsLibraryMatch(rmsMatch);
			}
			else {
//				String importError =
//						"Could not match library spectrum for " 
//						+ cid.getName() + ", METLIN ID " + metlinId + ", CE = " + ceFormat.format(refMsms.getCidLevel()) 
//						+ " in " + inputCefFile.getName() + ",  wrong HASH " + spectrumHash;
//				importLog.add(importError);
				
				if(!idSpectrumMap.containsKey(cid))
					idSpectrumMap.put(cid, new HashSet<TandemMassSpectrum>());

				TandemMassSpectrum existingMsms = idSpectrumMap.get(cid).stream().
					filter(s -> s.getPolarity().equals(refMsms.getPolarity())).
					filter(s -> s.getCidLevel() == refMsms.getCidLevel()).findFirst().orElse(null);

				if(existingMsms == null)
					idSpectrumMap.get(cid).add(refMsms);
			}
			processed++;
		}
		idPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	private void uploadParsedData() throws Exception {
		
		taskDescription = "Uploading data data from " + inputCefFile.getName() + " to database";
		total = features.size();
		processed = 0;
		
		Connection conn = ConnectionManager.getConnection();
		String dataAnalysisId = IDTUtils.addNewDataAnalysis(
				dataExtractionMethod, injectionId, conn);
		
//		String nextParentFeatureIdQuery = "SELECT '" + DataPrefix.MS_FEATURE.getName() +
//				"' || LPAD(MSMS_PARENT_FEATURE_SEQ.NEXTVAL, 12, '0') AS FEATURE_ID FROM DUAL";
//		PreparedStatement nextParentFeatureIdPs = conn.prepareStatement(nextParentFeatureIdQuery);
//		ResultSet nextParentFeatureIdRs = null;
			
		String parentFeatureQuery =
				"INSERT INTO MSMS_PARENT_FEATURE "
				+ "(FEATURE_ID, DATA_ANALYSIS_ID, RETENTION_TIME, HEIGHT, "
				+ "AREA, DETECTION_ALGORITHM, BASE_PEAK, POLARITY) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement parentFeaturePs = conn.prepareStatement(parentFeatureQuery);
		parentFeaturePs.setString(2, dataAnalysisId);
		
		String msOneQuery = "INSERT INTO MSMS_PARENT_FEATURE_PEAK "
				+ "(FEATURE_ID, MZ, HEIGHT, ADDUCT_ID, COMPOSITE_ADDUCT_ID) "
				+ "VALUES (?, ?, ?, ?, ?)";
		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);
		
//		String nextMsmsFeatureIdQuery = "SELECT '" + DataPrefix.MSMS_SPECTRUM.getName() +
//				"' || LPAD(MSMS_FEATURE_SEQ.NEXTVAL, 12, '0') AS MSMS_FEATURE_ID FROM DUAL";
//		PreparedStatement nextMsmsFeatureIdPs = conn.prepareStatement(nextMsmsFeatureIdQuery);
//		ResultSet nextMsmsFeatureIdRs = null;
		
		String msmsFeatureQuery =
				"INSERT INTO MSMS_FEATURE (PARENT_FEATURE_ID, MSMS_FEATURE_ID, DATA_ANALYSIS_ID, "
				+ "RETENTION_TIME, PARENT_MZ, FRAGMENTATION_ENERGY, COLLISION_ENERGY, POLARITY, "
				+ "ISOLATION_WINDOW_MIN, ISOLATION_WINDOW_MAX) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement msmsFeaturePs = conn.prepareStatement(msmsFeatureQuery);
		
		String msTwoQuery = "INSERT INTO MSMS_FEATURE_PEAK (MSMS_FEATURE_ID, MZ, HEIGHT) VALUES (?, ?, ?)";
		PreparedStatement msTwoPs = conn.prepareStatement(msTwoQuery);
		
		String precursorQuery = "UPDATE MSMS_PARENT_FEATURE SET MZ_OF_INTEREST = ? WHERE FEATURE_ID = ?";
		PreparedStatement precursorPs = conn.prepareStatement(precursorQuery);
		
//		String nextLibMatchIdQuery = "SELECT '" + DataPrefix.MSMS_LIBRARY_MATCH.getName() +
//				"' || LPAD(MSMS_LIB_MATCH_SEQ.NEXTVAL, 15, '0') AS MATCH_ID FROM DUAL";
//		PreparedStatement nextLibMatchIdPs = conn.prepareStatement(nextLibMatchIdQuery);
//		ResultSet nextLibMatchIdRs = null;
		
		String libMatchQuery =
				"INSERT INTO MSMS_FEATURE_LIBRARY_MATCH ( " +
				"MATCH_ID, MSMS_FEATURE_ID, MRC2_LIB_ID, MATCH_SCORE, IS_PRIMARY, MATCH_TYPE) " +
				"VALUES (?,?,?,?,?,?) ";
		PreparedStatement libMatchPs = conn.prepareStatement(libMatchQuery);
		
		for(LibMatchedSimpleMsFeature feature  : features) {
			
			//	Parent feature ID
//			nextParentFeatureIdRs = nextParentFeatureIdPs.executeQuery();
//			while(nextParentFeatureIdRs.next())
//				parentFeatureId = nextParentFeatureIdRs.getString("FEATURE_ID");
//
//			nextParentFeatureIdRs.close();
			
			//	Parent feature
			String parentFeatureId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_PARENT_FEATURE_SEQ",
					DataPrefix.MS_FEATURE,
					"0",
					12);			
			MassSpectrum msOne = feature.getObservedSpectrum();
			parentFeaturePs.setString(1, parentFeatureId);
			parentFeaturePs.setString(2, dataAnalysisId);
			parentFeaturePs.setDouble(3, feature.getRetentionTime());
			parentFeaturePs.setDouble(4, feature.getHeight());
			parentFeaturePs.setDouble(5, feature.getArea());
			parentFeaturePs.setString(6, feature.getObservedSpectrum().getDetectionAlgorithm());
			if(msOne == null)
				parentFeaturePs.setString(7, null);
			else
				parentFeaturePs.setDouble(7, msOne.getBasePeakMz());

			parentFeaturePs.setString(8, feature.getPolarity().getCode());
			parentFeaturePs.addBatch();
			
			//	MS1
			if(!msOne.getAdducts().isEmpty()) {
				
				msOnePs.setString(1, parentFeatureId);
				for(Adduct adduct : msOne.getAdducts()) {

					for(MsPoint point : msOne.getMsForAdduct(adduct)) {
						
						String adductId = null;
						String compositeAdductId = null;
						if(adduct instanceof SimpleAdduct)
							adductId = adduct.getId();
						
						if(adduct instanceof CompositeAdduct)
							compositeAdductId = adduct.getId();
						
						msOnePs.setDouble(2, point.getMz());
						msOnePs.setDouble(3, point.getIntensity());
						msOnePs.setString(4, adductId);
						msOnePs.setString(5, compositeAdductId);
						msOnePs.addBatch();
					}
				}
			}			
			TandemMassSpectrum instrumentMsms = 
					msOne.getTandemSpectrum(SpectrumSource.EXPERIMENTAL);

			if(instrumentMsms != null) {
				
				//	MSMS feature ID				
//				nextMsmsFeatureIdRs = nextMsmsFeatureIdPs.executeQuery();
//				while(nextMsmsFeatureIdRs.next())
//					msmsId = nextMsmsFeatureIdRs.getString("MSMS_FEATURE_ID");
//
//				nextMsmsFeatureIdRs.close();
				
				//	MSMS feature
				String msmsId = SQLUtils.getNextIdFromSequence(conn, 
						"MSMS_FEATURE_SEQ",
						DataPrefix.MSMS_SPECTRUM,
						"0",
						12);			
				msmsFeaturePs.setString(1, parentFeatureId);
				msmsFeaturePs.setString(2, msmsId);
				msmsFeaturePs.setString(3, dataAnalysisId);
				msmsFeaturePs.setDouble(4, feature.getRetentionTime());
				msmsFeaturePs.setDouble(5, instrumentMsms.getParent().getMz());
				msmsFeaturePs.setDouble(6, instrumentMsms.getFragmenterVoltage());
				msmsFeaturePs.setDouble(7, instrumentMsms.getCidLevel());
				msmsFeaturePs.setString(8, feature.getPolarity().getCode());
				
				Range isolationWindow = instrumentMsms.getIsolationWindow();
				if(isolationWindow != null && isolationWindow.getAverage() > 0.0) {
					msmsFeaturePs.setDouble(9, isolationWindow.getMin());
					msmsFeaturePs.setDouble(10, isolationWindow.getMax());
				}
				else {
					msmsFeaturePs.setNull(9, java.sql.Types.NULL);
					msmsFeaturePs.setNull(10, java.sql.Types.NULL);
				}
				msmsFeaturePs.addBatch();
				
				//	MS2
				msTwoPs.setString(1, msmsId);
				for(MsPoint point : instrumentMsms.getSpectrum()) {

					msTwoPs.setDouble(2, point.getMz());
					msTwoPs.setDouble(3, point.getIntensity());
					msTwoPs.addBatch();
				}				
				//	Precursor
				precursorPs.setDouble(1, instrumentMsms.getParent().getMz());
				precursorPs.setString(2, parentFeatureId);
				precursorPs.addBatch();
				
				//	MSMS library match
				ReferenceMsMsLibraryMatch refMatch = 
						feature.getIdentity().getReferenceMsMsLibraryMatch();
				if(refMatch != null) {
										
//					nextLibMatchIdRs = nextLibMatchIdPs.executeQuery();
//					while(nextLibMatchIdRs.next())
//						msmsMatchId = nextLibMatchIdRs.getString("MATCH_ID");
//
//					nextLibMatchIdRs.close();
					
					String msmsMatchId = SQLUtils.getNextIdFromSequence(conn, 
							"MSMS_LIB_MATCH_SEQ",
							DataPrefix.MSMS_LIBRARY_MATCH,
							"0",
							15);				
					libMatchPs.setString(1, msmsMatchId);
					libMatchPs.setString(2, msmsId);
					libMatchPs.setString(3, refMatch.getMatchedLibraryFeature().getUniqueId());
					libMatchPs.setDouble(4, refMatch.getScore());
					libMatchPs.setString(5,"Y");
					
					String matchType = null;
					if(refMatch.getMatchType() != null)
						matchType = refMatch.getMatchType().name();
						
					libMatchPs.setString(6, matchType);
					libMatchPs.addBatch();
				}
			}
			//	IDTMsDataUtils.uploadExperimentalMsMsFeature(feature, dataAnalysisId, conn);
			processed++;
			if(processed % BATCH_SIZE == 0) {

				parentFeaturePs.executeBatch();
				msOnePs.executeBatch();
				msmsFeaturePs.executeBatch();
				msTwoPs.executeBatch();
				precursorPs.executeBatch();
				libMatchPs.executeBatch();
			}
		}
		//	Execute last batch
		parentFeaturePs.executeBatch();
		msOnePs.executeBatch();
		msmsFeaturePs.executeBatch();
		msTwoPs.executeBatch();
		precursorPs.executeBatch();
		libMatchPs.executeBatch();
		
		//	Close all statements
//		nextParentFeatureIdPs.close();
		parentFeaturePs.close();
		msOnePs.close();
//		nextMsmsFeatureIdPs.close();
		msmsFeaturePs.close();
		msTwoPs.close();
		precursorPs.close();
//		nextLibMatchIdPs.close();
		libMatchPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {

		IDTCefMSMSPrescanOrImportTask task = new IDTCefMSMSPrescanOrImportTask(
				inputCefFile,
				injectionId,
				dataExtractionMethod,
				uploadData);
		task.setDataPipeline(dataPipeline);
		return task;
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public void setDataPipeline(DataPipeline dataPipeline) {
		this.dataPipeline = dataPipeline;
		this.dataExtractionMethod = dataPipeline.getDataExtractionMethod();
	}

	public Collection<CompoundIdentity> getMissingIdentities() {
		return missingIdentities;
	}
	
	public Map<CompoundIdentity, Collection<TandemMassSpectrum>> getIdSpectrumMap() {
		return idSpectrumMap;
	}
	
	public Collection<String> getImportLog() {
		return importLog;
	}

	public File getInputCefFile() {
		return inputCefFile;
	}
}
