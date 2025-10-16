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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemModel;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.StoredExtractedIonData;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityIDLevelComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityMSMSScoreComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IdentifierSearchOptions;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerAnnotationCache;
import edu.umich.med.mrc2.datoolbox.database.idt.DatabaseIdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureChromatogramUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DiskCacheUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import rtf.AdvancedRTFDocument;
import rtf.AdvancedRTFEditorKit;

public class IDTMSMSFeatureSearchTask extends AbstractTask {
	
	protected Polarity polarity;
	protected Double precursorMz;
	protected Collection<Double> fragments;
	protected Double massError;
	protected MassErrorType massErrorType;
	protected boolean ignoreMz;
	protected Double collisionEnergy;
	protected Range rtRange;
	protected FeatureSubsetByIdentification featureSubsetById;
	protected String compoundNameOrId;
	protected IdentifierSearchOptions idOpt;
	protected String formula;
	protected String inchiKey;
	protected boolean annotatedOnly;
	protected boolean searchAllIds;
	protected Collection<MSFeatureIdentificationLevel>idLevels;
	protected Collection<MSFeatureIdentificationFollowupStep>follwUpSteps;
	protected Collection<LIMSSampleType> sampleTypes;
	protected Collection<LIMSExperiment> experiments;
	protected Collection<ChromatographicSeparationType>separationTypes; 
	protected Collection<LIMSChromatographicColumn> chromatographicColumns;
	protected Collection<MsType> msType;	
	protected Collection<DataAcquisitionMethod> acquisitionMethods;
	protected Collection<DataExtractionMethod> dataExtractionMethods;
	protected String originalLibraryId;
	protected String mrc2libraryId;
	protected boolean searchAllLibIds;
	protected Collection<ReferenceMsMsLibrary>msmsLibs;
	protected boolean lookupSecondaryIds;
	protected boolean lookupSecondaryLibMatches;
	protected Collection<MSFeatureInfoBundle>features;
	protected Collection<MSFeatureInfoBundle>cachedFeatures;
	
	protected Collection<String>compoundIds;
	protected Collection<String>msmsLibraryIds;
	protected Map<String, CompoundIdentity>compoundMap;
	protected Map<String, MsMsLibraryFeature>msmsLibraryFeatureMap;
	protected Map<String,DataFile>injectionFileMap;
	
	public IDTMSMSFeatureSearchTask(
			Polarity polarity, 
			Double basePeakMz, 
			Collection<Double> fragments,
			Double massError, 
			MassErrorType massErrorType, 
			boolean ignoreMz, 
			Double collisionEnergy, 
			Range rtRange,
			FeatureSubsetByIdentification featureSubsetById, 
			String compoundNameOrId, 
			IdentifierSearchOptions idOpt,
			String formula, 
			String inchiKey, 
			boolean annotatedOnly, 
			boolean searchAllIds,
			Collection<MSFeatureIdentificationLevel> idLevels,
			Collection<MSFeatureIdentificationFollowupStep> follwUpSteps, 
			Collection<LIMSSampleType> sampleTypes,
			Collection<LIMSExperiment> experiments, 
			Collection<ChromatographicSeparationType> separationTypes,
			Collection<LIMSChromatographicColumn> chromatographicColumns, 
			Collection<MsType> msType,
			Collection<DataAcquisitionMethod> acquisitionMethods,
			Collection<DataExtractionMethod> dataExtractionMethods,
			String originalLibraryId, 
			String mrc2libraryId, 
			boolean searchAllLibIds,
			Collection<ReferenceMsMsLibrary> msmsLibs) {
		this();
		this.polarity = polarity;
		this.precursorMz = basePeakMz;
		this.fragments = fragments;
		this.massError = massError;
		this.massErrorType = massErrorType;
		this.ignoreMz = ignoreMz;
		this.collisionEnergy = collisionEnergy;
		this.rtRange = rtRange;
		this.featureSubsetById = featureSubsetById;
		this.compoundNameOrId = compoundNameOrId;
		this.idOpt = idOpt;
		this.formula = formula;
		this.inchiKey = inchiKey;
		this.annotatedOnly = annotatedOnly;
		this.searchAllIds = searchAllIds;
		this.idLevels = idLevels;
		this.follwUpSteps = follwUpSteps;
		this.sampleTypes = sampleTypes;
		this.experiments = experiments;
		this.separationTypes = separationTypes;
		this.chromatographicColumns = chromatographicColumns;
		this.msType = msType;
		this.acquisitionMethods = acquisitionMethods;
		this.dataExtractionMethods = dataExtractionMethods;
		this.originalLibraryId = originalLibraryId;
		this.mrc2libraryId = mrc2libraryId;
		this.searchAllLibIds = searchAllLibIds;
		this.msmsLibs = msmsLibs;

		lookupSecondaryIds = false;
		if(searchAllIds && (!formula.isEmpty() || !inchiKey.isEmpty() || (!compoundNameOrId.isEmpty() && idOpt != null)))
			lookupSecondaryIds = true;
		
		lookupSecondaryLibMatches = false;
		if(searchAllLibIds && (!originalLibraryId.isEmpty() || !mrc2libraryId.isEmpty()))
			lookupSecondaryLibMatches = true;		
	}

	public IDTMSMSFeatureSearchTask() {
		features = new ArrayList<MSFeatureInfoBundle>();
		cachedFeatures = new HashSet<MSFeatureInfoBundle>();
	}

	@Override
	public void run() {
		taskDescription = "Looking up features in IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			selectMsMsFeatures();
			if(!features.isEmpty()) {
				
				attachExperimentalTandemSpectra();
				attachMsMsLibraryIdentifications();
				attachMsMsManualIdentities();
				retievePepSearchParameters();
				attachAnnotations();
				attachFollowupSteps();
				putDataInCache();
				attachChromatograms();
				fetchBinnerAnnotations();
			}
			finalizeFeatureList();
			applyAdditionalFilters();
			
			//	updateAutomaticDefaultIdsBasedOnScores();
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	protected void selectMsMsFeatures() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		updateCompoundIdFromLibraryId(conn);	
		taskDescription = "Fetching MS2 features ...";
		total = 100;
		processed = 20;
		StringBuilder builder = null;
		Map<Integer,SQLParameter>parameterMap = new TreeMap<Integer,SQLParameter>();
		int paramCount = 1;
		
		String query = 
			"SELECT DISTINCT FEATURE_ID, MSMS_FEATURE_ID, "
			+ "IDENTIFICATION_LEVEL_ID, RETENTION_TIME, "
			+ "MZ_OF_INTEREST, ACQUISITION_METHOD_ID, " + 
			"EXTRACTION_METHOD_ID, EXPERIMENT_ID, STOCK_SAMPLE_ID, SAMPLE_ID, "
			+ "INJECTION_ID, POLARITY, HAS_CHROMATOGRAM FROM (" + 
			"SELECT F.FEATURE_ID, F.POLARITY, F.MZ_OF_INTEREST, F.RETENTION_TIME, F.HAS_CHROMATOGRAM, " +
			"I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, S.EXPERIMENT_ID, S.SAMPLE_ID, " +
			"T.STOCK_SAMPLE_ID, I.INJECTION_ID, R.ACCESSION, F2.MSMS_FEATURE_ID, " + 
			"F2.COLLISION_ENERGY, F2.IDENTIFICATION_LEVEL_ID " + 
			"FROM MSMS_PARENT_FEATURE F, " +
			"DATA_ANALYSIS_MAP M, " +
			"DATA_ACQUISITION_METHOD A, " +
			"INJECTION I, " +
			"PREPARED_SAMPLE P, " +
			"SAMPLE S, " +
			"STOCK_SAMPLE T, " +
			"MSMS_FEATURE F2 ";

		//	Library matches
		query += 
			"LEFT JOIN (SELECT H.MSMS_FEATURE_ID, RC.ACCESSION " + 
			"FROM MSMS_FEATURE_LIBRARY_MATCH H, " + 
			"REF_MSMS_LIBRARY_COMPONENT RC " +
			"WHERE H.MRC2_LIB_ID = RC.MRC2_LIB_ID "; 
		
		//	Library filtering
		if(!originalLibraryId.isEmpty()) {
			
			parameterMap.put(paramCount++, new SQLParameter(String.class, originalLibraryId));
			query += "AND RC.ORIGINAL_LIBRARY_ID = ? ";
		}
		if(!mrc2libraryId.isEmpty()) {
			
			parameterMap.put(paramCount++, new SQLParameter(String.class, mrc2libraryId));
			query += "AND RC.MRC2_LIB_ID = ? ";
		}
		if(!msmsLibs.isEmpty()) {
			
			builder = new StringBuilder();
			for(ReferenceMsMsLibrary lib : msmsLibs) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, lib.getPrimaryLibraryId()));
				builder.append("?,");
			}		
			query += "AND RC.LIBRARY_NAME IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}		
		//	Account for non-primary identifications
		if(!lookupSecondaryIds && !lookupSecondaryLibMatches)
			query += "AND H.IS_PRIMARY IS NOT NULL ";

		query += ") R ON (F2.MSMS_FEATURE_ID = R.MSMS_FEATURE_ID) ";
		
		//	Manual matches
		query += 
			"LEFT JOIN (SELECT AI.MSMS_FEATURE_ID, AI.ACCESSION " + 
			"FROM MSMS_FEATURE_ALTERNATIVE_ID AI "; 
		
		//	Account for non-primary identifications
		if(!lookupSecondaryIds)
			query += "WHERE AI.IS_PRIMARY IS NOT NULL ";

		query += ") AR ON (F2.MSMS_FEATURE_ID = AR.MSMS_FEATURE_ID) " +							
			"WHERE F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID " +
			"AND  F2.PARENT_FEATURE_ID = F.FEATURE_ID " +
			"AND M.INJECTION_ID = I.INJECTION_ID " +
			"AND A.ACQ_METHOD_ID = I.ACQUISITION_METHOD_ID " +			
			"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
			"AND P.SAMPLE_ID = S.SAMPLE_ID " +
			"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID " +
			"AND F.BASE_PEAK IS NOT NULL ";
		
		if(polarity != null && !polarity.equals(Polarity.Neutral)) {
			
			query += "AND F.POLARITY = ? ";
			parameterMap.put(paramCount++, new SQLParameter(String.class, polarity.getCode()));
		}
		if(!ignoreMz && !fragments.isEmpty()) {
			query += "AND F2.MSMS_FEATURE_ID IN ("
					+ "SELECT FP.MSMS_FEATURE_ID FROM MSMS_FEATURE_PEAK FP WHERE (";
			ArrayList<String>fragQueries = new ArrayList<String>();
			for(double mz : fragments) {
				
				Range mzRange = MsUtils.createMassRange(mz, massError, massErrorType);
				parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMin()));
				parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMax()));
				fragQueries.add(" FP.MZ BETWEEN ? AND ? ");
			}
			parameterMap.put(paramCount++, new SQLParameter(Integer.class, fragments.size()));
			query += StringUtils.join(fragQueries, " OR ") + ") HAVING COUNT(DISTINCT FP.MZ) >= ? GROUP BY FP.MSMS_FEATURE_ID)";
		}
		if(!ignoreMz && precursorMz != null) {

			Range mzRange = MsUtils.createMassRange(precursorMz, massError, massErrorType);
			parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMax()));
			query += "AND F.MZ_OF_INTEREST BETWEEN ? AND  ? ";
		}			
		if(rtRange != null) {

			parameterMap.put(paramCount++, new SQLParameter(Double.class, rtRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, rtRange.getMax()));
			query += "AND F.RETENTION_TIME BETWEEN ? AND  ? ";
		}
		if(!msType.isEmpty()) {
			
			builder = new StringBuilder();
			for(MsType type : msType) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, type.getId()));
				builder.append("?,");
			}			
			query += "AND A.MS_TYPE IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(!acquisitionMethods.isEmpty()) {
			
			builder = new StringBuilder();
			for(DataAcquisitionMethod acqMethod : acquisitionMethods) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, acqMethod.getId()));
				builder.append("?,");
			}			
			query += "AND I.ACQUISITION_METHOD_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(!dataExtractionMethods.isEmpty()) {
			
			builder = new StringBuilder();
			for(DataExtractionMethod daMethod : dataExtractionMethods) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, daMethod.getId()));
				builder.append("?,");
			}			
			query += "AND M.EXTRACTION_METHOD_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(!separationTypes.isEmpty()) {
			
			builder = new StringBuilder();
			for(ChromatographicSeparationType type : separationTypes) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, type.getId()));
				builder.append("?,");
			}			
			query += "AND A.SEPARATION_TYPE IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(!chromatographicColumns.isEmpty()) {
			builder = new StringBuilder();
			for(LIMSChromatographicColumn column : chromatographicColumns) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, column.getColumnId()));
				builder.append("?,");
			}			
			query += "AND A.COLUMN_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(collisionEnergy != null) {
			parameterMap.put(paramCount++, new SQLParameter(Double.class, collisionEnergy));
			query += "AND F2.COLLISION_ENERGY = ? ";
		}
		if(!sampleTypes.isEmpty()) {
			builder = new StringBuilder();
			for(LIMSSampleType sType : sampleTypes) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, sType.getId()));
				builder.append("?,");
			}			
			query += "AND T.SAMPLE_TYPE_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(!experiments.isEmpty()) {
			
			builder = new StringBuilder();
			for(LIMSExperiment experiment : experiments) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, experiment.getId()));
				builder.append("?,");
			}		
			query += "AND S.EXPERIMENT_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		//	ID search block
		if(!compoundNameOrId.isEmpty() && idOpt != null) {

			if(idOpt.equals(IdentifierSearchOptions.COMPOUND_ID)) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId));
				query += "AND R.ACCESSION = ? ";
			}
			if(idOpt.equals(IdentifierSearchOptions.NAME_EQUALS)) {

				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase()));
				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase()));
				query +=
					"AND (R.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) = ?) "
					+ "OR AR.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) = ?))";
			}
			if(idOpt.equals(IdentifierSearchOptions.NAME_STARTS_WITH)) {

				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase() + "%"));
				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase() + "%"));
				query +=
					"AND (R.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?)"
					+ " OR AR.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?))";
			}
			if(idOpt.equals(IdentifierSearchOptions.NAME_CONTAINS)) {

				parameterMap.put(paramCount++, new SQLParameter(String.class, "%" + compoundNameOrId.toUpperCase() + "%"));
				parameterMap.put(paramCount++, new SQLParameter(String.class, "%" + compoundNameOrId.toUpperCase() + "%"));
				query +=
					"AND (R.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?) "
					+ "OR AR.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?))";
			}
		}
		//	Formula
		if(!formula.isEmpty()) {

			parameterMap.put(paramCount++, new SQLParameter(String.class, formula));
			parameterMap.put(paramCount++, new SQLParameter(String.class, formula));
			query +=
				"AND (R.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C.MOL_FORMULA = ?)"
				+ " OR AR.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C.MOL_FORMULA = ?))";
		}
		//	INCHI key 
		if(!inchiKey.isEmpty()) {

			parameterMap.put(paramCount++, new SQLParameter(String.class, inchiKey));
			String column = "INCHI_KEY";
			if(inchiKey.length() == 14)
				column = "INCHI_KEY_CONNECT";
			
			query +=
				"AND (R.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C." + column + " = ?) "
						+ "OR AR.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C." + column + " = ?))";
		}		
		query += ") AD ORDER BY MZ_OF_INTEREST, RETENTION_TIME";
		
//		System.out.println(query);
		
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);

		for(Entry<Integer, SQLParameter> entry : parameterMap.entrySet()) {

			if(entry.getValue().getClazz().equals(String.class))
				ps.setString(entry.getKey(), (String)entry.getValue().getValue());

			if(entry.getValue().getClazz().equals(Double.class))
				ps.setDouble(entry.getKey(), (Double)entry.getValue().getValue());
			
			if(entry.getValue().getClazz().equals(Integer.class))
				ps.setInt(entry.getKey(), (Integer)entry.getValue().getValue());
		}		
		//	MS1 query 
		String msOneQuery =
				"SELECT ADDUCT_ID, COMPOSITE_ADDUCT_ID, MZ, HEIGHT "
				+ "FROM MSMS_PARENT_FEATURE_PEAK WHERE FEATURE_ID = ?";

		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);
		ResultSet msOneRs = null;
			
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		while (rs.next()) {

			MSFeatureInfoBundle fInCache = 
					DiskCacheUtils.retrieveMSFeatureInfoBundleFromCache(
							rs.getString("FEATURE_ID"));
			if(fInCache != null) {
				cachedFeatures.add(fInCache);
				processed++;
				continue;				
			}
			String id = rs.getString("FEATURE_ID");
			double rt = rs.getDouble("RETENTION_TIME");
			double mz = rs.getDouble("MZ_OF_INTEREST");
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);

			MsFeature f = new MsFeature(id, name, rt);
			f.setPolarity(Polarity.getPolarityByCode(rs.getString("POLARITY")));
			f.setAnnotatedObjectType(AnnotatedObjectType.MSMS_FEATURE);
			
			//	TODO
			MassSpectrum spectrum = new MassSpectrum();
			Map<Adduct, Collection<MsPoint>> adductMap =
					new TreeMap<Adduct,Collection<MsPoint>>();
			
			msOnePs.setString(1, id);
			msOneRs = msOnePs.executeQuery();
			while(msOneRs.next()) {
				
				Adduct adduct = 
						AdductManager.getDefaultAdductForPolarity(f.getPolarity());;
				String adductId = msOneRs.getString("ADDUCT_ID");
				if(adductId == null)
					adductId = msOneRs.getString("COMPOSITE_ADDUCT_ID");

				if(adductId != null)
					adduct = AdductManager.getAdductById(adductId);

				if(!adductMap.containsKey(adduct))
					adductMap.put(adduct, new ArrayList<MsPoint>());

				adductMap.get(adduct).add(
						new MsPoint(msOneRs.getDouble("MZ"), msOneRs.getDouble("HEIGHT")));
			}
			msOneRs.close();
			adductMap.entrySet().stream().
				forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

			f.setSpectrum(spectrum);
			if(rs.getString("IDENTIFICATION_LEVEL_ID") != null) {
				MSFeatureIdentificationLevel level = 
						IDTDataCache.getMSFeatureIdentificationLevelById(
								rs.getString("IDENTIFICATION_LEVEL_ID"));
				if(f.getPrimaryIdentity() != null)
					f.getPrimaryIdentity().setIdentificationLevel(level);
			}			
			MSFeatureInfoBundle bundle = new MSFeatureInfoBundle(f);
			bundle.setAcquisitionMethod(
				IDTDataCache.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID")));
			bundle.setDataExtractionMethod(
				IDTDataCache.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID")));
			bundle.setExperiment(
				IDTDataCache.getExperimentById(rs.getString("EXPERIMENT_ID")));
			StockSample stockSample =
				IDTDataCache.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
			bundle.setStockSample(stockSample);
			IDTExperimentalSample sample =
				IDTUtils.getExperimentalSampleById(rs.getString("SAMPLE_ID"), conn);
			bundle.setSample(sample);
			bundle.setInjectionId(rs.getString("INJECTION_ID"));
			bundle.setHasChromatogram(rs.getString("HAS_CHROMATOGRAM") != null);
			features.add(bundle);
			processed++;
		}
		rs.close();
		ps.close();
		msOnePs.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Remove redundant
		features = features.stream().distinct().collect(Collectors.toSet());
		attachDataFiles();
	}
	
	protected void attachDataFiles() {
		
		List<String> injectionIds = features.stream().
				map(c -> c.getInjectionId()).distinct().
				collect(Collectors.toList());
		Collection<Injection> injections = new ArrayList<Injection>();
		try {
			injections = IDTUtils.getInjectionsByIds(injectionIds);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(injections.isEmpty())
			return;		
		
		injectionFileMap = new TreeMap<String,DataFile>();
		injections.stream().forEach(i -> injectionFileMap.put(i.getId(), new DataFile(i)));		
		int fCount = 0;
		for(DataFile df : injectionFileMap.values()) {			
			df.setColor(ColorUtils.getColor(fCount));
			fCount++;
		}
		for(MSFeatureInfoBundle fb : features) {
			
			if(fb.getInjectionId() != null)
				fb.setDataFile(injectionFileMap.get(fb.getInjectionId()));
		}
	}

	protected void updateCompoundIdFromLibraryId(Connection conn) throws Exception {

		if(mrc2libraryId.isEmpty() && originalLibraryId.isEmpty())
			return;
		
		if(!compoundNameOrId.isEmpty() || !formula.isEmpty() || !inchiKey.isEmpty())
			return;
		
		idOpt = IdentifierSearchOptions.COMPOUND_ID;
		String column = null;
		String id = null;
		if(!mrc2libraryId.isEmpty()) {
			 column = "MRC2_LIB_ID";
			 id = mrc2libraryId;
		}
		if(!originalLibraryId.isEmpty()) {
			 column = "ORIGINAL_LIBRARY_ID";
			 id = originalLibraryId;
		}	
		String sql = "SELECT ACCESSION FROM REF_MSMS_LIBRARY_COMPONENT WHERE " + column + " = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			compoundNameOrId = rs.getString("ACCESSION");
		
		rs.close();
		ps.close();
	}

	protected void attachExperimentalTandemSpectra() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MSMS data ...";
		total = features.size();
		processed = 0;
		String query =
				"SELECT MSMS_FEATURE_ID, PARENT_MZ, FRAGMENTATION_ENERGY, "
				+ "COLLISION_ENERGY, POLARITY, ID_DISABLED, "
				+ "ISOLATION_WINDOW_MIN, ISOLATION_WINDOW_MAX, HAS_SCANS " +
				"FROM MSMS_FEATURE WHERE PARENT_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		
		String msquery =
				"SELECT MZ, HEIGHT FROM MSMS_FEATURE_PEAK WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msquery);
		ResultSet msrs = null;
		
		//	Scan map
		String scanMapQuery =
				"SELECT SCAN, PARENT_SCAN, MS_LEVEL, RT,  " +
				"PARENT_MS_LEVEL, PARENT_RT  " +
				"FROM MSMS_FEATURE_SCAN_MAP  " +
				"WHERE MSMS_FEATURE_ID = ? ";
		PreparedStatement scanMapPs = conn.prepareStatement(scanMapQuery);
		ResultSet scanMapRs = null;
		
		for(MSFeatureInfoBundle fb : features) {

			try {
				ps.setString(1, fb.getMsFeature().getId());	
				rs = ps.executeQuery();
				ArrayList<TandemMassSpectrum>msmsList = new ArrayList<TandemMassSpectrum>();
				while(rs.next()) {
					
					Polarity polarity = Polarity.getPolarityByCode(rs.getString("POLARITY"));
					TandemMassSpectrum msms = new TandemMassSpectrum(
							rs.getString("MSMS_FEATURE_ID"),
							2,
							rs.getDouble("FRAGMENTATION_ENERGY"),
							rs.getDouble("COLLISION_ENERGY"),
							polarity);
					msms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);
					MsPoint parent = new MsPoint(rs.getDouble("PARENT_MZ"), 200.0d);
					msms.setParent(parent);	
					msms.setHasScans(rs.getString("HAS_SCANS") != null);
					
					Range isolationWindow = new Range(
							rs.getDouble("ISOLATION_WINDOW_MIN"), 
							rs.getDouble("ISOLATION_WINDOW_MAX"));
					if(isolationWindow.getAverage() == 0.0d)	//	TODO this is a temporary fix based on Agilent narrow window
						isolationWindow = new Range(parent.getMz() - 0.65, parent.getMz() + 0.65);
					
					msms.setIsolationWindow(isolationWindow);					
					msmsList.add(msms);
					
					//	Set flag if ID is disabled
					fb.getMsFeature().setIdDisabled(rs.getString("ID_DISABLED") != null);
				}
				rs.close();
				for(TandemMassSpectrum msms : msmsList) {

					Collection<MsPoint> spectrum = msms.getSpectrum();
					msps.setString(1, msms.getId());
					msrs = msps.executeQuery();
					while(msrs.next())
						spectrum.add(new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT")));

					msrs.close();
					msms.setEntropy(MsUtils.calculateCleanedSpectrumEntropyNatLog(spectrum));
					
					//	Adjust parent intensity
					Range parentMzRange = MsUtils.createMassRange(
							msms.getParent().getMz(), 10, MassErrorType.mDa);
					MsPoint observedParent = spectrum.stream().
							filter(p -> parentMzRange.contains(p.getMz())).
							sorted(MsUtils.reverseIntensitySorter).
							findFirst().orElse(null);
					if(observedParent != null)
						msms.setParent(new MsPoint(observedParent));
					
					MsPoint msOneParent = fb.getMsFeature().getSpectrum().getMsPoints().stream().
							filter(p -> parentMzRange.contains(p.getMz())).
							sorted(MsUtils.reverseIntensitySorter).findFirst().orElse(null);
					if(msOneParent == null)
						msOneParent = msms.getParent();
						
					// Find isotopes within isolation window
					final MsPoint m1p = new MsPoint(msOneParent);
					Range iw = msms.getIsolationWindow();
					Collection<MsPoint>minorParentIons = 
							fb.getMsFeature().getSpectrum().getMsPoints().stream().
							filter(p -> !p.equals(m1p)).
							filter(p -> iw.contains(p.getMz())).
							sorted(MsUtils.mzSorter).collect(Collectors.toList());					
					msms.setMinorParentIons(minorParentIons, msOneParent);
					
					//	Get scan map
					if(msms.getHasScans()) {
						
						scanMapPs.setString(1, msms.getId());
						scanMapRs = scanMapPs.executeQuery();
						while(scanMapRs.next()) {
							
							msms.getAveragedScanNumbers().put(
									scanMapRs.getInt("SCAN"), scanMapRs.getInt("PARENT_SCAN"));
							msms.getScanRtMap().put(
									scanMapRs.getInt("SCAN"), scanMapRs.getDouble("RT"));
							msms.getScanRtMap().put(
									scanMapRs.getInt("PARENT_SCAN"), scanMapRs.getDouble("PARENT_RT"));
						}
						scanMapRs.close();
					}
					fb.getMsFeature().getSpectrum().addTandemMs(msms);
					fb.getMsFeature().updateUnknownPrimaryIdentityBasedOnMSMS();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			processed++;
		}
		ps.close();
		msps.close();
		scanMapPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	protected void attachMsMsLibraryIdentificationsOld() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MSMS library identifications ...";
		total = features.size();
		processed = 0;
		
		String query =
			"SELECT MATCH_ID, MRC2_LIB_ID, MATCH_SCORE, FWD_SCORE, REVERSE_SCORE, " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, IS_PRIMARY, IDENTIFICATION_LEVEL_ID, " +
			"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH " +
			"FROM MSMS_FEATURE_LIBRARY_MATCH M " +
			"WHERE MSMS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		
		String lfQuery =
				"SELECT POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ, ADDUCT, "
				+ "COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE, "
				+ "MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION, "
				+ "SPECTRUM_TYPE, CHROMATOGRAPHY_TYPE, CONTRIBUTOR, SPLASH, "
				+ "RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, "
				+ "ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, ENTROPY "
				+ "FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?";
		PreparedStatement lfps = conn.prepareStatement(lfQuery);
		ResultSet lfrs = null;
		
		String msmsQuery =
				"SELECT MZ, INTENSITY, FRAGMENT_COMMENT FROM REF_MSMS_LIBRARY_PEAK "
				+ "WHERE MRC2_LIB_ID = ? ORDER BY 1";
		PreparedStatement msmsps = conn.prepareStatement(msmsQuery);
		ResultSet msmsrs = null;
		
		for(MSFeatureInfoBundle fb : features) {
			try {
				TandemMassSpectrum msms = 
						fb.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
				if(msms == null) {
					processed++;
					continue;
				}
				ps.setString(1, msms.getId());
				rs = ps.executeQuery();
				while(rs.next()) {

					String mrc2msmsId = rs.getString("MRC2_LIB_ID");
					MsMsLibraryFeature feature = null;
					lfps.setString(1, mrc2msmsId);
					lfrs = lfps.executeQuery();
					while(lfrs.next()) {

						feature = new MsMsLibraryFeature(
								mrc2msmsId,
								Polarity.getPolarityByCode(
										lfrs.getString(MSMSComponentTableFields.POLARITY.name())));
						feature.setSpectrumSource(
								SpectrumSource.getOptionByName(
										lfrs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
						feature.setIonizationType(
								IDTDataCache.getIonizationTypeById(
										lfrs.getString(MSMSComponentTableFields.IONIZATION_TYPE.name())));
						feature.setCollisionEnergyValue(
								lfrs.getString(MSMSComponentTableFields.COLLISION_ENERGY.name()));
						feature.setSpectrumEntropy(
								lfrs.getDouble(MSMSComponentTableFields.ENTROPY.name()));
						
						Map<String, String> properties = feature.getProperties();
						for(MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

							if(!field.equals(MSMSComponentTableFields.PRECURSOR_MZ) 
									&& !field.equals(MSMSComponentTableFields.MRC2_LIB_ID)) {
								
								String value = lfrs.getString(field.name());
								if(value != null && !value.trim().isEmpty())
									properties.put(field.getName(), value);
							}
						}
						ReferenceMsMsLibrary refLib =
								IDTDataCache.getReferenceMsMsLibraryByPrimaryLibraryId(
										lfrs.getString(MSMSComponentTableFields.LIBRARY_NAME.name()));
						feature.setMsmsLibraryIdentifier(refLib.getUniqueId());

						//	Add spectrum
						double precursorMz = lfrs.getDouble(MSMSComponentTableFields.PRECURSOR_MZ.name());
						msmsps.setString(1, mrc2msmsId);
						msmsrs = msmsps.executeQuery();
						while(msmsrs.next()) {

							MsPoint p = new MsPoint(msmsrs.getDouble("MZ"), msmsrs.getDouble("INTENSITY"));
							feature.getSpectrum().add(p);
							if(p.getMz() == precursorMz)
								feature.setParent(p);

							if(msmsrs.getString("FRAGMENT_COMMENT") != null)
								feature.getMassAnnotations().put(p, msmsrs.getString("FRAGMENT_COMMENT"));
						}
						msmsrs.close();
						CompoundIdentity compoundIdentity =
								CompoundDatabaseUtils.getCompoundById(lfrs.getString("ACCESSION"), conn);

						feature.setCompoundIdentity(compoundIdentity);
					}
					lfrs.close();
					if(feature == null)
						continue;

					MsFeatureIdentity id = new MsFeatureIdentity(feature.getCompoundIdentity(),
							CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
					id.setIdSource(CompoundIdSource.LIBRARY_MS2);
					id.setUniqueId(rs.getString("MATCH_ID"));
					MSMSMatchType matchType = 
							MSMSMatchType.getOptionByName(rs.getString("MATCH_TYPE"));
					
					ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
							feature,
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
						id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
					
					if(id.isPrimary() && !fb.getMsFeature().isIdDisabled())
						fb.getMsFeature().setPrimaryIdentity(id);
					else
						fb.getMsFeature().addIdentity(id);
					
					match.setEntropyBasedScore(
							MSMSScoreCalculator.calculateDefaultEntropyMatchScore(msms, match));
				}
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processed++;
		}
		ps.close();
		lfps.close();
		msmsps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void attachMsMsLibraryIdentifications() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		getCompoundAndMSMSLibraryIds(conn);
		getCompounds(conn);
		getMSMSLibraryEntries(conn);
		
		taskDescription = "Adding MSMS library identifications ...";
		List<MSFeatureInfoBundle> featuresToProcess = features.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
				collect(Collectors.toList());
		
		total = featuresToProcess.size();
		processed = 0;
		
		String query =
			"SELECT MATCH_ID, MRC2_LIB_ID, MATCH_SCORE, FWD_SCORE, REVERSE_SCORE, " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, IS_PRIMARY, IDENTIFICATION_LEVEL_ID, " +
			"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH " +
			"FROM MSMS_FEATURE_LIBRARY_MATCH M " +
			"WHERE MSMS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		for(MSFeatureInfoBundle fb : featuresToProcess) {
					
			TandemMassSpectrum msms = 
					fb.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
			ps.setString(1, msms.getId());
			rs = ps.executeQuery();
			while(rs.next()) {

				MsMsLibraryFeature feature = msmsLibraryFeatureMap.get(rs.getString("MRC2_LIB_ID"));
				MsFeatureIdentity id = new MsFeatureIdentity(feature.getCompoundIdentity(),
						CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);
				id.setIdSource(CompoundIdSource.LIBRARY_MS2);
				id.setUniqueId(rs.getString("MATCH_ID"));
				MSMSMatchType matchType = 
						MSMSMatchType.getOptionByName(rs.getString("MATCH_TYPE"));
				
				ReferenceMsMsLibraryMatch match = new ReferenceMsMsLibraryMatch(
						feature,
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
					id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
				
				if(id.isPrimary() && !fb.getMsFeature().isIdDisabled())
					fb.getMsFeature().setPrimaryIdentity(id);
				else
					fb.getMsFeature().addIdentity(id);
				
				match.setEntropyBasedScore(
						MSMSScoreCalculator.calculateDefaultEntropyMatchScore(msms, match));
			}
			rs.close();
			processed++;
		}		
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void getCompoundAndMSMSLibraryIds(Connection conn) throws Exception {
				
		taskDescription = "Gettting compound and library IDs ...";
		List<MSFeatureInfoBundle> featuresToProcess = features.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
				filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
				collect(Collectors.toList());
		
		total = featuresToProcess.size();
		processed = 0;
		compoundIds = new TreeSet<String>();
		msmsLibraryIds = new TreeSet<String>();
		
		String query = "SELECT DISTINCT C.MRC2_LIB_ID, C.ACCESSION " +
						"FROM MSMS_FEATURE_LIBRARY_MATCH M, " +
						"REF_MSMS_LIBRARY_COMPONENT C " +
						"WHERE C.MRC2_LIB_ID = M.MRC2_LIB_ID " +
						"AND M.MSMS_FEATURE_ID = ? ";			
		PreparedStatement ps = conn.prepareStatement(query);		
		ResultSet rs = null;			
		for(MSFeatureInfoBundle fb : featuresToProcess) {
			
			TandemMassSpectrum msms = 
					fb.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
			ps.setString(1, msms.getId());
			rs = ps.executeQuery();
			while(rs.next()) {	
				
				if(rs.getString("ACCESSION") != null)
					compoundIds.add(rs.getString("ACCESSION"));
				
				msmsLibraryIds.add(rs.getString("MRC2_LIB_ID"));				
			}
			rs.close();
			processed++;
		}
		ps.close();	
	}
	
	protected void getCompounds(Connection conn) throws Exception {
		
		if(compoundIds.isEmpty())
			return;
		
		taskDescription = "Gettting compounds ...";
		total = compoundIds.size();
		processed = 0;
		compoundMap = new TreeMap<String, CompoundIdentity>();		
		String query =
				"SELECT SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, "
				+ "EXACT_MASS, SMILES, INCHI_KEY "+
				"FROM COMPOUND_DATA WHERE ACCESSION IN (?)";
		PreparedStatement ps = conn.prepareStatement(query);	
				
		for(String cid : compoundIds) {
			
			CompoundIdentity identity = DiskCacheUtils.retrieveCompoundIdentityFromCache(cid);
			if(identity != null) {
				compoundMap.put(cid, identity);
				processed++;
				continue;
			}			
            ps.setString(1, cid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){

				CompoundDatabaseEnum dbSource =
						CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
				String commonName = rs.getString("PRIMARY_NAME");
				identity = new CompoundIdentity(
						dbSource, 
						cid, 
						commonName,
						commonName, 
						rs.getString("MOL_FORMULA"), 
						rs.getDouble("EXACT_MASS"), 
						rs.getString("SMILES"));
				identity.setInChiKey(rs.getString("INCHI_KEY"));
				compoundMap.put(cid, identity);
				DiskCacheUtils.putCompoundIdentityInCache(identity);
			}
			rs.close();
			processed++;
		}
		ps.close();
	}
	
	protected void getMSMSLibraryEntries(Connection conn) throws Exception {
		
		if(msmsLibraryIds.isEmpty())
			return;
		
		taskDescription = "Getting MSMS library features ...";
		total = msmsLibraryIds.size();
		processed = 0;
		msmsLibraryFeatureMap = new TreeMap<String, MsMsLibraryFeature>();
		
		String lfQuery =
				"SELECT POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ, ADDUCT, "
				+ "COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE, "
				+ "MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION, "
				+ "SPECTRUM_TYPE, CHROMATOGRAPHY_TYPE, CONTRIBUTOR, SPLASH, "
				+ "RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, "
				+ "ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, ENTROPY "
				+ "FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?";
		PreparedStatement lfps = conn.prepareStatement(lfQuery);
		ResultSet lfrs = null;
		
		String msmsQuery =
				"SELECT MZ, INTENSITY, FRAGMENT_COMMENT FROM REF_MSMS_LIBRARY_PEAK "
				+ "WHERE MRC2_LIB_ID = ? ORDER BY 1";
		PreparedStatement msmsps = conn.prepareStatement(msmsQuery);
		ResultSet msmsrs = null;
		
		for(String mrc2msmsId : msmsLibraryIds) {
			
			MsMsLibraryFeature feature = DiskCacheUtils.retrieveMsMsLibraryFeatureFromCache(mrc2msmsId);
			if(feature != null) {
				msmsLibraryFeatureMap.put(mrc2msmsId, feature);
				processed++;
				continue;
			}
			lfps.setString(1, mrc2msmsId);
			lfrs = lfps.executeQuery();
			while(lfrs.next()) {

				feature = new MsMsLibraryFeature(
						mrc2msmsId,
						Polarity.getPolarityByCode(
								lfrs.getString(MSMSComponentTableFields.POLARITY.name())));
				feature.setSpectrumSource(
						SpectrumSource.getOptionByName(
								lfrs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
				feature.setIonizationType(
						IDTDataCache.getIonizationTypeById(
								lfrs.getString(MSMSComponentTableFields.IONIZATION_TYPE.name())));
				feature.setCollisionEnergyValue(
						lfrs.getString(MSMSComponentTableFields.COLLISION_ENERGY.name()));
				feature.setSpectrumEntropy(
						lfrs.getDouble(MSMSComponentTableFields.ENTROPY.name()));
				
				Map<String, String> properties = feature.getProperties();
				for(MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

					if(!field.equals(MSMSComponentTableFields.PRECURSOR_MZ) 
							&& !field.equals(MSMSComponentTableFields.MRC2_LIB_ID)) {
						
						String value = lfrs.getString(field.name());
						if(value != null && !value.trim().isEmpty())
							properties.put(field.getName(), value);
					}
				}
				ReferenceMsMsLibrary refLib =
						IDTDataCache.getReferenceMsMsLibraryByPrimaryLibraryId(
								lfrs.getString(MSMSComponentTableFields.LIBRARY_NAME.name()));
				feature.setMsmsLibraryIdentifier(refLib.getUniqueId());

				//	Add spectrum
				double precursorMz = lfrs.getDouble(MSMSComponentTableFields.PRECURSOR_MZ.name());
				msmsps.setString(1, mrc2msmsId);
				msmsrs = msmsps.executeQuery();
				while(msmsrs.next()) {

					MsPoint p = new MsPoint(msmsrs.getDouble("MZ"), msmsrs.getDouble("INTENSITY"));
					feature.getSpectrum().add(p);
					if(p.getMz() == precursorMz)
						feature.setParent(p);

					if(msmsrs.getString("FRAGMENT_COMMENT") != null)
						feature.getMassAnnotations().put(p, msmsrs.getString("FRAGMENT_COMMENT"));
				}
				msmsrs.close();
				if(lfrs.getString("ACCESSION") != null)
					feature.setCompoundIdentity(compoundMap.get(lfrs.getString("ACCESSION")));
				
				msmsLibraryFeatureMap.put(mrc2msmsId, feature);
				DiskCacheUtils.putMsMsLibraryFeatureInCache(feature);
			}
			lfrs.close();
			processed++;
		}
		lfps.close();
		msmsps.close();
	}

	protected void retievePepSearchParameters() {		
		
		Set<String> pepSearchParIds = features.stream().
			flatMap(f -> f.getMsFeature().getIdentifications().stream().
					filter(i -> i.getReferenceMsMsLibraryMatch() != null)).
			map(m -> m.getReferenceMsMsLibraryMatch().getSearchParameterSetId()).
			distinct().collect(Collectors.toSet());
		
		if(pepSearchParIds.isEmpty())
			return;
		
		for(String id : pepSearchParIds)
			IDTDataCache.getNISTPepSearchParameterObjectById(id);		
	}
	
	protected void attachMsMsManualIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding manual identifications ...";
		total = features.size();
		processed = 0;
		
		String query =
				"SELECT IDENTIFICATION_ID, ACCESSION, ID_CONFIDENCE, "
				+ "IS_PRIMARY, ID_SOURCE, ASSIGNED_BY, ASSIGNED_ON, "
				+ "IDENTIFICATION_LEVEL_ID, ADDUCT_ID, COMPOSITE_ADDUCT_ID " +
				"FROM MSMS_FEATURE_ALTERNATIVE_ID WHERE MSMS_FEATURE_ID = ?";			
		PreparedStatement ps = conn.prepareStatement(query);		
		ResultSet rs = null;			
		for(MSFeatureInfoBundle fb : features) {

			try {
				TandemMassSpectrum msms =
						fb.getMsFeature().getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msms == null)
					continue;
				
				ps.setString(1, msms.getId());
				rs = ps.executeQuery();
				while(rs.next()) {

					CompoundIdentity compoundIdentity =
							CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
					CompoundIdentificationConfidence confidenceLevel =
							CompoundIdentificationConfidence.getLevelById(rs.getString("ID_CONFIDENCE"));
					MsFeatureIdentity id = new MsFeatureIdentity(compoundIdentity, confidenceLevel);
					id.setIdSource(CompoundIdSource.getOptionByName(rs.getString("ID_SOURCE")));
					if(rs.getString("IS_PRIMARY") != null)
						id.setPrimary(true);

					id.setUniqueId(rs.getString("IDENTIFICATION_ID"));
					LIMSUser assignedBy = IDTDataCache.getUserById(rs.getString("ASSIGNED_BY"));
					id.setAssignedBy(assignedBy);
					id.setAssignedOn(new Date(rs.getDate("ASSIGNED_ON").getTime()));
					String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
					if(statusId != null) 
						id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));
					
					String adductId = rs.getString("ADDUCT_ID");
					if(adductId == null)
						adductId = rs.getString("COMPOSITE_ADDUCT_ID");
					
					if(adductId != null)
						id.setPrimaryAdduct(AdductManager.getAdductById(adductId));
					
					if(id.isPrimary() && !fb.getMsFeature().isIdDisabled())
						fb.getMsFeature().setPrimaryIdentity(id);
					else
						fb.getMsFeature().addIdentity(id);
				}
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void attachChromatograms() throws Exception {
			
		List<MSFeatureInfoBundle> featuresWithChromatograms = 
				features.stream().
				filter(f -> f.getHasChromatogram()).
				collect(Collectors.toList());
		if(featuresWithChromatograms.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Reading chromatograms ...";
		total = featuresWithChromatograms.size();
		processed = 0;
		Collection<StoredExtractedIonData>storedChroms = 
				new ArrayList<StoredExtractedIonData>();		
		String query = 
				"SELECT INJECTION_ID, MS_LEVEL, EXTRACTED_MASS,  " +
				"MASS_ERROR_VALUE, MASS_ERROR_TYPE, START_RT, END_RT,  " +
				"TITLE, TIME_VALUES, INTENSITY_VALUES " +
				"FROM MSMS_PARENT_FEATURE_CHROMATOGRAM  " +
				"WHERE FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		for(MSFeatureInfoBundle fb : featuresWithChromatograms) {
			
			ps.setString(1, fb.getMSFeatureId());
			rs = ps.executeQuery();
			while (rs.next()) {

				double[] timeValues = new double[0];
				double[] intensityValues = new double[0];
				InputStream its = rs.getBinaryStream("TIME_VALUES");
				if (its != null) {
					BufferedInputStream itbis = new BufferedInputStream(its);
					String encodedTime = new String(itbis.readAllBytes(), StandardCharsets.US_ASCII);
					timeValues = NumberArrayUtils.decodeNumberArray(encodedTime);
					its.close();
				}
				its = rs.getBinaryStream("INTENSITY_VALUES");
				if (its != null) {
					BufferedInputStream itbis = new BufferedInputStream(its);
					String encodedIntensity = new String(itbis.readAllBytes(), StandardCharsets.US_ASCII);
					intensityValues = NumberArrayUtils.decodeNumberArray(encodedIntensity);
				}
				StoredExtractedIonData seid = new StoredExtractedIonData(
						rs.getString("TITLE"),
						rs.getDouble("EXTRACTED_MASS"), 
						timeValues, 
						intensityValues, 
						fb.getMSFeatureId(),
						rs.getString("INJECTION_ID"), 
						rs.getInt("MS_LEVEL"), 
						rs.getDouble("MASS_ERROR_VALUE"),
						MassErrorType.getTypeByName(rs.getString("MASS_ERROR_TYPE")), 
						rs.getDouble("START_RT"),
						rs.getDouble("END_RT"));
				storedChroms.add(seid);				
			}
			rs.close();
			processed++;
		}
		ps.close();
		if(storedChroms.isEmpty()) {
			ConnectionManager.releaseConnection(conn);
			return;
		}	
		addMissingInjectionsForChromatograms(storedChroms);
		
		Map<String, List<StoredExtractedIonData>> featureChromMap = 
				storedChroms.stream().collect(Collectors.groupingBy(StoredExtractedIonData::getFeatureId));
		taskDescription = "Creating chromatogram bundles ...";
		total = featureChromMap.size();
		processed = 0;
		for(Entry<String, List<StoredExtractedIonData>>chrEntry : featureChromMap.entrySet()) {
			
			ChromatogramDefinition chromatogramDefinition = 
					createChromatogramDefinition(chrEntry.getValue());	//TODO
			
			MsFeatureChromatogramBundle chromatogramBundle = 
					new MsFeatureChromatogramBundle(chrEntry.getKey(), chromatogramDefinition);
			for(StoredExtractedIonData scd : chrEntry.getValue()) {

				DataFile df =injectionFileMap.get(scd.getInjectionId());
				if(df != null) {
					if(chromatogramBundle.getChromatogramDefinition().getPolarity() == null)
						chromatogramBundle.getChromatogramDefinition().setPolarity(df.getDataAcquisitionMethod().getPolarity());
						
					chromatogramBundle.addChromatogramForDataFile(df, scd);		
				}
			}			
			FeatureChromatogramUtils.putFeatureChromatogramBundleInCache(chromatogramBundle);
			processed++;
		}
	}
	
	//	Add missing data files to injection/file map to account for the cases
	//	when chromatograms were extracted from MS1 files
	protected void addMissingInjectionsForChromatograms(
			Collection<StoredExtractedIonData>storedChroms) {

		Set<String> missingInjectionIds = 
				storedChroms.stream().map(c -> c.getInjectionId()).
				filter(i -> !injectionFileMap.keySet().contains(i)).
				collect(Collectors.toSet());
		if(!missingInjectionIds.isEmpty()) {
			
			Collection<Injection> injections = new ArrayList<Injection>();
			try {
				injections = IDTUtils.getInjectionsByIds(missingInjectionIds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(injections.isEmpty())
				return;	
			
			int fCount = 0;
			if(injectionFileMap == null)
				injectionFileMap = new TreeMap<String,DataFile>();			
			else
				fCount = injectionFileMap.size();
				
			injections.stream().forEach(i -> injectionFileMap.put(i.getId(), new DataFile(i)));	
			for(DataFile df : injectionFileMap.values()) {			
				df.setColor(ColorUtils.getColor(fCount));
				fCount++;
			}
		}
	}
	
	private ChromatogramDefinition createChromatogramDefinition(List<StoredExtractedIonData>extractedIonDataList) {
		
		Collection<Double> mzList = extractedIonDataList.stream().
				map(c -> c.getExtractedMass()).
				collect(Collectors.toCollection(TreeSet::new));
		Double mzWindowValue = extractedIonDataList.stream().mapToDouble(c -> c.getMassErrorValue()).max().getAsDouble();
		Range rtRange = new Range(extractedIonDataList.get(0).getRtRange());
		for(int i=1; i<extractedIonDataList.size(); i++)
			rtRange.extendRange(extractedIonDataList.get(i).getRtRange());
		
		ChromatogramDefinition chromDef = new ChromatogramDefinition(
				null, 
				extractedIonDataList.get(0).getMsLevel(), 
				mzList,
				mzWindowValue, 
				extractedIonDataList.get(0).getMassErrorType(), 
				rtRange);

		return chromDef;
	}

	protected void attachAnnotations() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding annotations ...";
		total = features.size();
		processed = 0;
		
		String stAnQuery =
				"SELECT STANDARD_ANNOTATION_ID FROM MSMS_FEATURE_STANDARD_ANNOTATIONS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";		
		PreparedStatement stAnPs = conn.prepareStatement(stAnQuery);
		ResultSet stAnRs = null;
		
		AdvancedRTFEditorKit editor = new  AdvancedRTFEditorKit();
		String query =
			"SELECT ANNOTATION_ID, ANNOTATION_RTF_DOCUMENT, CREATED_BY, CREATED_ON, "
			+ "LAST_EDITED_BY, LAST_EDITED_ON, LINKED_DOCUMENT_ID, CML, CML_NOTE " +
			"FROM OBJECT_ANNOTATIONS WHERE OBJECT_TYPE = ? AND OBJECT_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, AnnotatedObjectType.MSMS_FEATURE.name());
		ResultSet rs = null;
		
		String mdQuery = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement mdps = conn.prepareStatement(mdQuery);
		ResultSet mdrs = null;
		
		for(MSFeatureInfoBundle fb : features) {
			
			stAnPs.setString(1, fb.getMSFeatureId());
			stAnRs = stAnPs.executeQuery();
			while(stAnRs.next()) {
				StandardFeatureAnnotation newAnnotation = 
						 IDTDataCache.getStandardFeatureAnnotationById(stAnRs.getString("STANDARD_ANNOTATION_ID"));
				 if(newAnnotation != null)
					 fb.addStandardFeatureAnnotation(newAnnotation);
			}
			stAnRs.close();
			try {
				ps.setString(2, fb.getMsFeature().getId());
				rs = ps.executeQuery();
				while (rs.next()) {

					ObjectAnnotation annotation = new ObjectAnnotation(
							rs.getString("ANNOTATION_ID"), 
							AnnotatedObjectType.MSMS_FEATURE, 
							fb.getMsFeature().getId(),
							rs.getDate("CREATED_ON"), 
							rs.getDate("LAST_EDITED_ON"),
							IDTDataCache.getUserById(rs.getString("CREATED_BY")),
							IDTDataCache.getUserById(rs.getString("LAST_EDITED_BY")),
							null);

//					Blob blob = rs.getBlob("ANNOTATION_RTF_DOCUMENT");
					InputStream ads = rs.getBinaryStream("ANNOTATION_RTF_DOCUMENT");
					if (ads != null) {
						BufferedInputStream is = new BufferedInputStream(ads);
						AdvancedRTFDocument doc = (AdvancedRTFDocument) editor.createDefaultDocument();
						editor.read(is, doc, 0);
						is.close();
//						blob.free();
						annotation.setRtfDocument(doc);
					}
//					Blob cml = rs.getBlob("CML");
					InputStream cmls = rs.getBinaryStream("CML");
					if (cmls != null) {
						
						BufferedInputStream is = new BufferedInputStream(cmls);
						IChemModel chemModel = AnnotationUtils.getChemModelFromStream(is);
						is.close();
//						cml.free();				
						annotation.setChemModel(chemModel );				
						annotation.setChemModelNotes(rs.getString("CML_NOTE"));
					}
					annotation.setLinkedDocumentId(rs.getString("LINKED_DOCUMENT_ID"));
					if(annotation.getLinkedDocumentId() != null) {
						
						mdps.setString(1, annotation.getLinkedDocumentId());
						mdrs = mdps.executeQuery();
						while(mdrs.next()) {

							annotation.setLinkedDocumentFormat(
									DocumentFormat.getFormatByFileExtension(mdrs.getString("DOCUMENT_FORMAT")));
							annotation.setLinkedDocumentName(mdrs.getString("DOCUMENT_NAME"));
						}
						mdrs.close();
					}
					fb.getMsFeature().addAnnotation(annotation);
				}
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processed++;
		}
		ps.close();
		mdps.close();
		stAnPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	protected void attachFollowupSteps() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding follow-up steps ...";
		total = features.size();
		processed = 0;		
		String query =
				"SELECT FOLLOWUP_STEP_ID FROM MSMS_FEATURE_FOLLOWUP_STEPS " +
				"WHERE MSMS_PARENT_FEATURE_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;		
		for(MSFeatureInfoBundle fb : features) {

			try {
				ps.setString(1, fb.getMsFeature().getId());
				rs = ps.executeQuery();
				while(rs.next()) {
					 MSFeatureIdentificationFollowupStep newStep = 
							 IDTDataCache.getMSFeatureIdentificationFollowupStepById(rs.getString("FOLLOWUP_STEP_ID"));
					 if(newStep != null)
						 fb.addIdFollowupStep(newStep);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void putDataInCache() {		
		features.stream().
			forEach(f -> DiskCacheUtils.putMSFeatureInfoBundleInCache(f));
	}
	
	protected void applyAdditionalFilters() {
		
		taskDescription = "Applying additional filters ...";
		total = 100;
		processed = 80;
		//	ID state
		Collection<MSFeatureInfoBundle>filteredByIdStatus = 
				MsFeatureStatsUtils.filterFeaturesByIdSubset(features, featureSubsetById);
		features.clear();
		features.addAll(filteredByIdStatus);
		
		// Annotations
		if(annotatedOnly) {
			List<MSFeatureInfoBundle> annotated = features.stream().
					filter(f -> !f.getMsFeature().getAnnotations().isEmpty()).
					collect(Collectors.toList());
			features.clear();
			features.addAll(annotated);
		}
		if(!idLevels.isEmpty()) {
			
			List<MSFeatureInfoBundle> byIdLevel = features.stream().
					filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
					filter(f -> idLevels.contains(
							f.getMsFeature().getPrimaryIdentity().getIdentificationLevel())).
					collect(Collectors.toList());
			features.clear();
			features.addAll(byIdLevel);
		}
		if(!follwUpSteps.isEmpty()) {
			
			List<MSFeatureInfoBundle> byFollowup = 
					features.stream().
					filter(f -> !CollectionUtils.intersection(
							follwUpSteps, f.getIdFollowupSteps()).isEmpty()).
					collect(Collectors.toList());
			features.clear();
			features.addAll(byFollowup);
		}
	}
	
	protected void finalizeFeatureList() {
		
		if(!cachedFeatures.isEmpty()) {
			features.addAll(cachedFeatures);
			features = features.stream().distinct().collect(Collectors.toSet());
		}
	}
	
	//	Most likely skip this alltogether
	protected void updateAutomaticDefaultIdsBasedOnScores() {
		
		taskDescription = "Checking automatic default IDs ...";
		total = features.size();
		processed = 0;
		
		MsFeatureIdentityMSMSScoreComparator idSorter = 
				new MsFeatureIdentityMSMSScoreComparator();	
		MsFeatureIdentityIDLevelComparator levelSorter = 
				new MsFeatureIdentityIDLevelComparator();
		for(MSFeatureInfoBundle bundle : features) {
			
			
			MsFeature feature = bundle.getMsFeature();
			if(feature.isIdDisabled()) {
				feature.setPrimaryIdentity(null);
				processed++;
				continue;
			}			
			if(
//				feature.getIdentifications().size() <= 1 || 
				feature.getIdentifications().isEmpty() || !allowDefaultIdUpdate(bundle)) {
				processed++;
				continue;
			}
			MsFeatureIdentity bestId = 
					feature.getIdentifications().stream().
					sorted(idSorter).
					sorted(levelSorter).
					findFirst().orElse(null);
			
			if(bestId != null && !bestId.equals(bundle.getMsFeature().getPrimaryIdentity())) {
				
				TandemMassSpectrum msmsFeature = 
						feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msmsFeature != null) {
					try {
						DatabaseIdentificationUtils.setMSMSFeaturePrimaryIdentity(msmsFeature.getId(), bestId);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					feature.setPrimaryIdentity(bestId);
				}
			}
			processed++;
		}
	}

	protected boolean allowDefaultIdUpdate(MSFeatureInfoBundle bundle) {
		
		MsFeature feature = bundle.getMsFeature();
		if(feature.getIdentifications().isEmpty())
			return true;
		
		if(feature.isIdDisabled() 
				|| !bundle.getIdFollowupSteps().isEmpty() 
				|| !feature.getAnnotations().isEmpty() 
				|| !bundle.getStandadAnnotations().isEmpty())
			return false;
				
		MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
		if(primaryId != null) {	
			
			//	Exclude manually assigned
			if(primaryId.getAssignedBy() != null)
				return false;
			
			if(primaryId.getIdentificationLevel() != null)
				return primaryId.getIdentificationLevel().isAllowToReplaceAsDefault();
		}		
//		if(primaryId == null || primaryId.getIdentificationLevel() == null) {
//			
//			if(feature.getIdentifications().isEmpty()) //	Keep features with manually disabled primary ID as is
//				return true;
//			else
//				return false;
//		}
		return true;
	}
	
	protected void fetchBinnerAnnotations() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding Binner annotations ...";
		total = 100;
		processed = 20;
		Map<String,Collection<String>>featureAnnotationMap = 
				new TreeMap<String,Collection<String>>();
		Collection<String>annotationIds = new TreeSet<String>();
		
		String query = "SELECT BCC_ID FROM MSMS_CLUSTER_COMPONENT "
				+ "WHERE BCC_ID IS NOT NULL AND MS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		for(MSFeatureInfoBundle fb : features) {
			
			annotationIds.clear();
			ps.setString(1, fb.getMSFeatureId());	
			rs = ps.executeQuery();
			while(rs.next())
				annotationIds.add(rs.getString("BCC_ID"));			
			
			rs.close();
			if(!annotationIds.isEmpty())
				featureAnnotationMap.put(fb.getMSFeatureId(), new TreeSet<String>(annotationIds));
		}
		if(featureAnnotationMap.isEmpty()) {
			
			ps.close();
			ConnectionManager.releaseConnection(conn);
			return;
		}
		total = featureAnnotationMap.size();
		processed = 0;
		query = "SELECT MOL_ION_NUMBER, FEATURE_NAME, BINNER_MZ, " +
			"BINNER_RT, ANNOTATION, IS_PRIMARY, ADDITIONAL_GROUP_ANNOTATIONS, " +
			"FURTHER_ANNOTATIONS, DERIVATIONS, ISOTOPES, ADDITIONAL_ISOTOPES, " +
			"CHARGE_CARRIER, ADDITIONAL_ADDUCTS, BIN_NUMBER, CORR_CLUSTER_NUMBER, " +
			"REBIN_SUBCLUSTER_NUMBER, RT_SUBCLUSTER_NUMBER, MASS_ERROR, RMD " +
			"FROM BINNER_ANNOTATION_CLUSTER_COMPONENT " +
			"WHERE BCC_ID = ? ";
		ps = conn.prepareStatement(query);
		ResultSet baRs = null;
		for(Entry<String, Collection<String>> me : featureAnnotationMap.entrySet()) {
			
			Collection<BinnerAnnotation>annotations = new ArrayList<BinnerAnnotation>();
			for(String bccId : me.getValue()) {
				
				ps.setString(1, bccId);
				baRs = ps.executeQuery();
				while(baRs.next()) {
					
					BinnerAnnotation ba = new BinnerAnnotation(
							bccId, 
							baRs.getString("FEATURE_NAME"), 
							baRs.getString("ANNOTATION"));
					ba.setMolIonNumber(baRs.getInt("MOL_ION_NUMBER"));
					ba.setBinnerMz(baRs.getDouble("BINNER_MZ"));
					ba.setBinnerRt(baRs.getDouble("BINNER_RT"));
					if(baRs.getString("IS_PRIMARY") != null) 
						ba.setPrimary(true);

					ba.setAdditionalGroupAnnotations(baRs.getString("ADDITIONAL_GROUP_ANNOTATIONS"));
					ba.setFurtherAnnotations(baRs.getString("FURTHER_ANNOTATIONS"));
					ba.setDerivations(baRs.getString("DERIVATIONS"));
					ba.setIsotopes(baRs.getString("ISOTOPES"));
					ba.setAdditionalIsotopes(baRs.getString("ADDITIONAL_ISOTOPES"));
					ba.setChargeCarrier(baRs.getString("CHARGE_CARRIER"));
					ba.setAdditionalAdducts(baRs.getString("ADDITIONAL_ADDUCTS"));
					ba.setBinNumber(baRs.getInt("BIN_NUMBER"));
					ba.setRebinSubclusterNumber(baRs.getInt("REBIN_SUBCLUSTER_NUMBER"));
					ba.setRtSubclusterNumber(baRs.getInt("RT_SUBCLUSTER_NUMBER"));
					ba.setMassError(baRs.getDouble("MASS_ERROR"));
					ba.setRmd(baRs.getDouble("RMD"));
					annotations.add(ba);
				}
				baRs.close();			
			}
			BinnerAnnotationCache.setAnnotationdForMsFeature(me.getKey(), annotations);
		}		
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public Task cloneTask() {

		return new IDTMSMSFeatureSearchTask(
				 polarity, 
				 precursorMz, 
				 fragments,
				 massError, 
				 massErrorType, 
				 ignoreMz, 
				 collisionEnergy, 
				 rtRange,
				 featureSubsetById, 
				 compoundNameOrId, 
				 idOpt,
				 formula, 
				 inchiKey, 
				 annotatedOnly, 
				 searchAllIds,
				 idLevels,
				 follwUpSteps, 
				 sampleTypes,
				 experiments, 
				 separationTypes,
				 chromatographicColumns, 
				 msType,
				 acquisitionMethods,
				 dataExtractionMethods,
				 originalLibraryId, 
				 mrc2libraryId,
				 searchAllLibIds,
				 msmsLibs);
	}
	
	public Collection<MSFeatureInfoBundle> getSelectedFeatures() {
		return features;
	}
}
