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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemModel;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityIDLevelComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityMSMSScoreComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
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
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
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
	protected Collection<MsFeatureInfoBundle>features;
	
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
		super();
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
		
		features = new ArrayList<MsFeatureInfoBundle>();
		lookupSecondaryIds = false;
		if(searchAllIds && (!formula.isEmpty() || !inchiKey.isEmpty() || (!compoundNameOrId.isEmpty() && idOpt != null)))
			lookupSecondaryIds = true;
		
		lookupSecondaryLibMatches = false;
		if(searchAllLibIds && (!originalLibraryId.isEmpty() || !mrc2libraryId.isEmpty()))
			lookupSecondaryLibMatches = true;		
	}

	public IDTMSMSFeatureSearchTask() {
		// TODO Auto-generated constructor stub
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
			}
			applyAdditionalFilters();
			//	updateAutomaticDefaultIdsBasedOnScores();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
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
			"SELECT DISTINCT FEATURE_ID, RETENTION_TIME, MZ_OF_INTEREST, ACQUISITION_METHOD_ID, " + 
			"EXTRACTION_METHOD_ID, EXPERIMENT_ID, STOCK_SAMPLE_ID, SAMPLE_ID, INJECTION_ID, POLARITY FROM (" + 
			"SELECT F.FEATURE_ID, F.POLARITY, F.MZ_OF_INTEREST, F.RETENTION_TIME, " +
			"I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, S.EXPERIMENT_ID, S.SAMPLE_ID, " +
			"T.STOCK_SAMPLE_ID, I.INJECTION_ID, R.ACCESSION, F2.COLLISION_ENERGY " + 
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
			"AND F.POLARITY = ? " +
			"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
			"AND P.SAMPLE_ID = S.SAMPLE_ID " +
			"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID " +
			"AND F.BASE_PEAK IS NOT NULL ";
		
		parameterMap.put(paramCount++, new SQLParameter(String.class, polarity.getCode()));
		
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

			String id = rs.getString("FEATURE_ID");
			double rt = rs.getDouble("RETENTION_TIME");
			double mz = rs.getDouble("MZ_OF_INTEREST");
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);

			MsFeature f = new MsFeature(id, name, rt);
			f.setPolarity(polarity);
			f.setAnnotatedObjectType(AnnotatedObjectType.MSMS_FEATURE);
			
			//	TODO
//			IDTMsDataUtils.attachMS1SpectrumForMsMs(f, conn);
			MassSpectrum spectrum = new MassSpectrum();
			Map<Adduct, Collection<MsPoint>> adductMap =
					new TreeMap<Adduct,Collection<MsPoint>>();
			
			msOnePs.setString(1, id);
			msOneRs = msOnePs.executeQuery();
			while(msOneRs.next()) {
				
				String adductId = msOneRs.getString("ADDUCT_ID");
				if(adductId == null)
					adductId = msOneRs.getString("COMPOSITE_ADDUCT_ID");

				Adduct adduct =
						AdductManager.getAdductById(adductId);

				if(adduct == null)
					continue;

				if(!adductMap.containsKey(adduct))
					adductMap.put(adduct, new ArrayList<MsPoint>());

				adductMap.get(adduct).add(new MsPoint(msOneRs.getDouble("MZ"), msOneRs.getDouble("HEIGHT")));
			}
			msOneRs.close();
			adductMap.entrySet().stream().
				forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

			f.setSpectrum(spectrum);
			
			MsFeatureInfoBundle bundle = new MsFeatureInfoBundle(f);
			bundle.setAcquisitionMethod(
				IDTDataCash.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID")));
			bundle.setDataExtractionMethod(
				IDTDataCash.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID")));
			bundle.setExperiment(
				IDTDataCash.getExperimentById(rs.getString("EXPERIMENT_ID")));
			StockSample stockSample =
				IDTDataCash.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
			bundle.setStockSample(stockSample);
			IDTExperimentalSample sample =
				IDTUtils.getExperimentalSample(rs.getString("SAMPLE_ID"), conn);
			bundle.setSample(sample);
			bundle.setInjectionId(rs.getString("INJECTION_ID"));
			features.add(bundle);
			processed++;
		}
		rs.close();
		ps.close();
		msOnePs.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Remove redundant
		features = features.stream().distinct().collect(Collectors.toSet());
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
				+ "COLLISION_ENERGY, TOTAL_INTENSITY, ENTROPY, POLARITY, ID_DISABLED " +
				"FROM MSMS_FEATURE WHERE PARENT_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		
		String msquery =
				"SELECT MZ, HEIGHT FROM MSMS_FEATURE_PEAK WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msquery);
		ResultSet msrs = null;
		
		for(MsFeatureInfoBundle fb : features) {
//			try {
//				IDTMsDataUtils.attachExperimentalTandemSpectra(fb.getMsFeature(), conn);
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
					msms.setParent(new MsPoint(rs.getDouble("PARENT_MZ"), 999.0d));	
//					msms.setTotalIntensity(rs.getDouble("TOTAL_INTENSITY"));
					msms.setEntropy(rs.getDouble("ENTROPY"));		
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
					
//					if(msms.getTotalIntensity() == 0.0d)				
//						msms.setTotalIntensity(spectrum.stream().mapToDouble(p -> p.getIntensity()).sum());
					
					if(msms.getEntropy() == 0.0d)
						msms.setEntropy(MsUtils.calculateSpectrumEntropy(spectrum));
					
					fb.getMsFeature().getSpectrum().addTandemMs(msms);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			processed++;
		}
		ps.close();
		msps.close();
		ConnectionManager.releaseConnection(conn);
	}

	protected void attachMsMsLibraryIdentifications() throws Exception {

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
		
		for(MsFeatureInfoBundle fb : features) {
			try {
				TandemMassSpectrum msms = 
						fb.getMsFeature().getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msms == null)
					continue;

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
								SpectrumSource.getSpectrumSourceByName(
										lfrs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
						feature.setIonizationType(
								IDTDataCash.getIonizationTypeById(
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
								IDTDataCash.getReferenceMsMsLibraryByPrimaryLibraryId(
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
							MSMSMatchType.getMSMSMatchTypeByName(rs.getString("MATCH_TYPE"));
					
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
						id.setIdentificationLevel(IDTDataCash.getMSFeatureIdentificationLevelById(statusId));
					
					fb.getMsFeature().addIdentity(id);
					if(id.isPrimary() && !fb.getMsFeature().isIdDisabled())
						fb.getMsFeature().setPrimaryIdentity(id);
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
	
	protected void retievePepSearchParameters() {		
		
		Set<String> pepSearchParIds = features.stream().
			flatMap(f -> f.getMsFeature().getIdentifications().stream().
					filter(i -> i.getReferenceMsMsLibraryMatch() != null)).
			map(m -> m.getReferenceMsMsLibraryMatch().getSearchParameterSetId()).
			distinct().collect(Collectors.toSet());
		
		if(pepSearchParIds.isEmpty())
			return;
		
		for(String id : pepSearchParIds)
			IDTDataCash.getNISTPepSearchParameterObjectById(id);		
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
		for(MsFeatureInfoBundle fb : features) {
//			try {
//				IDTMsDataUtils.attachMsMsManualIdentifications(fb.getMsFeature(), conn);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
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
					id.setIdSource(CompoundIdSource.getIdSourceByName(rs.getString("ID_SOURCE")));
					if(rs.getString("IS_PRIMARY") != null)
						id.setPrimary(true);

					id.setUniqueId(rs.getString("IDENTIFICATION_ID"));
					LIMSUser assignedBy = IDTDataCash.getUserById(rs.getString("ASSIGNED_BY"));
					id.setAssignedBy(assignedBy);
					id.setAssignedOn(new Date(rs.getDate("ASSIGNED_ON").getTime()));
					String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
					if(statusId != null) 
						id.setIdentificationLevel(IDTDataCash.getMSFeatureIdentificationLevelById(statusId));
					
					String adductId = rs.getString("ADDUCT_ID");
					if(adductId == null)
						adductId = rs.getString("COMPOSITE_ADDUCT_ID");
					
					if(adductId != null)
						id.setPrimaryAdduct(AdductManager.getAdductById(adductId));
					
					fb.getMsFeature().addIdentity(id);
					if(id.isPrimary())
						fb.getMsFeature().setPrimaryIdentity(id);
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
		
		for(MsFeatureInfoBundle fb : features) {
			
			stAnPs.setString(1, fb.getMsFeature().getId());
			stAnRs = stAnPs.executeQuery();
			while(stAnRs.next()) {
				StandardFeatureAnnotation newAnnotation = 
						 IDTDataCash.getStandardFeatureAnnotationById(stAnRs.getString("STANDARD_ANNOTATION_ID"));
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
							IDTDataCash.getUserById(rs.getString("CREATED_BY")),
							IDTDataCash.getUserById(rs.getString("LAST_EDITED_BY")),
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
		for(MsFeatureInfoBundle fb : features) {

			try {
				ps.setString(1, fb.getMsFeature().getId());
				rs = ps.executeQuery();
				while(rs.next()) {
					 MSFeatureIdentificationFollowupStep newStep = 
							 IDTDataCash.getMSFeatureIdentificationFollowupStepById(rs.getString("FOLLOWUP_STEP_ID"));
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
			forEach(f -> FeatureCollectionUtils.putMSMSFetureInfoBundleInCache(f));
	}
	
	protected void applyAdditionalFilters() {
		
		taskDescription = "Applying additional filters ...";
		total = 100;
		processed = 80;
		//	ID state
		if(featureSubsetById.equals(FeatureSubsetByIdentification.IDENTIFIED_ONLY)) {
			List<MsFeatureInfoBundle> identified = 
					features.stream().filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
					collect(Collectors.toList());
			features.clear();
			features.addAll(identified);
		}
		if(featureSubsetById.equals(FeatureSubsetByIdentification.UNKNOWN_ONLY)) {
			List<MsFeatureInfoBundle> unidentified = 
					features.stream().filter(f -> f.getMsFeature().getPrimaryIdentity() == null).
					collect(Collectors.toList());
			features.clear();
			features.addAll(unidentified);
		}
		// Annotations
		if(annotatedOnly) {
			List<MsFeatureInfoBundle> annotated = 
					features.stream().filter(f -> !f.getMsFeature().getAnnotations().isEmpty()).
					collect(Collectors.toList());
			features.clear();
			features.addAll(annotated);
		}
		if(!idLevels.isEmpty()) {
			
			List<MsFeatureInfoBundle> byIdLevel = 
					features.stream().
					filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
					filter(f -> idLevels.contains(f.getMsFeature().getPrimaryIdentity().getIdentificationLevel())).
					collect(Collectors.toList());
			features.clear();
			features.addAll(byIdLevel);
		}
		if(!follwUpSteps.isEmpty()) {
			
			List<MsFeatureInfoBundle> byFollowup = 
					features.stream().
					filter(f -> !CollectionUtils.intersection(follwUpSteps, f.getIdFollowupSteps()).isEmpty()).
					collect(Collectors.toList());
			features.clear();
			features.addAll(byFollowup);
		}
	}
	
	//	Most likely skip this alltogether
	protected void updateAutomaticDefaultIdsBasedOnScores() {
		
		taskDescription = "Checking automatic default IDs ...";
		total = features.size();
		processed = 0;
		
		MsFeatureIdentityMSMSScoreComparator idSorter = new MsFeatureIdentityMSMSScoreComparator();	
		MsFeatureIdentityIDLevelComparator levelSorter = new MsFeatureIdentityIDLevelComparator();
		for(MsFeatureInfoBundle bundle : features) {
			
			
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
						IdentificationUtils.setMSMSFeaturePrimaryIdentity(msmsFeature.getId(), bestId);
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

	protected boolean allowDefaultIdUpdate(MsFeatureInfoBundle bundle) {
		
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
	
	public Collection<MsFeatureInfoBundle> getSelectedFeatures() {
		return features;
	}
}
