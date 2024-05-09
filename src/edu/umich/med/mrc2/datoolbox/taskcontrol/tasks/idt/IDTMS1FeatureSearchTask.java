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
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;

import org.apache.commons.collections4.CollectionUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemModel;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IdentifierSearchOptions;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
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
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import rtf.AdvancedRTFDocument;
import rtf.AdvancedRTFEditorKit;

public class IDTMS1FeatureSearchTask extends AbstractTask {
	
	private Polarity polarity;
	private Double monoisotopicPeakMz;
	private Double massError;
	private MassErrorType massErrorType;
	private boolean ignoreMz;
	private Range rtRange;
	private FeatureSubsetByIdentification featureSubsetById;
	private String compoundNameOrId;
	private IdentifierSearchOptions idOpt;
	private String formula;
	private String inchiKey;
	private boolean annotatedOnly;
	private boolean searchAllIds;
	private Collection<MSFeatureIdentificationLevel>idLevels;
	private Collection<MSFeatureIdentificationFollowupStep>follwUpSteps;
	private Collection<LIMSSampleType> sampleTypes;
	private Collection<LIMSExperiment> experiments;
	private Collection<ChromatographicSeparationType>separationTypes; 
	private Collection<LIMSChromatographicColumn> chromatographicColumns;
	private Collection<DataAcquisitionMethod> acquisitionMethods;
	private Collection<DataExtractionMethod> dataExtractionMethods;
	
	protected Collection<MSFeatureInfoBundle>features;
	private boolean lookupSecondaryIds;
	private boolean lookupSecondaryLibMatches;
	private boolean lookupIds;
	
	public IDTMS1FeatureSearchTask(
			Polarity polarity, 
			Double monoisotopicPeakMz, 
			Double massError, 
			MassErrorType massErrorType, 
			boolean ignoreMz, 
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
			Collection<DataAcquisitionMethod> acquisitionMethods, 
			Collection<DataExtractionMethod> dataExtractionMethods) {
		super();
		this.polarity = polarity;
		this.monoisotopicPeakMz = monoisotopicPeakMz;
		this.massError = massError;
		this.massErrorType = massErrorType;
		this.ignoreMz = ignoreMz;
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
		this.acquisitionMethods = acquisitionMethods;
		this.dataExtractionMethods = dataExtractionMethods;
		
		features = new ArrayList<MSFeatureInfoBundle>();
		
		lookupIds =  false;
		if(!formula.isEmpty() || !inchiKey.isEmpty() || (!compoundNameOrId.isEmpty() && idOpt != null))
			lookupIds = true;
		
		//	TODO add MS/RT library lookup
		lookupSecondaryIds = false;
		if(searchAllIds && lookupIds)
			lookupSecondaryIds = true;
//		
//		lookupSecondaryLibMatches = false;
//		if(searchAllLibIds && (!originalLibraryId.isEmpty() || !mrc2libraryId.isEmpty()))
//			lookupSecondaryLibMatches = true;		
	}

	@Override
	public void run() {
		taskDescription = "Looking up MS1 features in IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			selectMsFeatures();
			if(!features.isEmpty()) {
				
				attachPooledMS1Spectrums();
				attachAnnotations();
				attachMS1LibraryIdentities();
				attachMS1ManualIdentities();
				attachFollowupSteps();				
			}
			applyAdditionalFilters();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}
	
	private void selectMsFeatures() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Fetching MS1 features ...";
		total = 100;
		processed = 20;
		StringBuilder builder = null;
		Map<Integer,SQLParameter>parameterMap = new TreeMap<Integer,SQLParameter>();
		int paramCount = 1;
		
		String query = 
				"SELECT DISTINCT F.POOLED_MS_FEATURE_ID, F.RETENTION_TIME, " +
				"F.AREA, F.BASE_PEAK, D.SAMPLE_ID, D.EXPERIMENT_ID,  " +
				"D.ACQ_METHOD_ID, D.EXTRACTION_METHOD_ID, "
				+ "T.STOCK_SAMPLE_ID, F.ID_DISABLED, A.POLARITY " +
				"FROM POOLED_MS1_DATA_SOURCE D, " +
				"SAMPLE S, " +
				"STOCK_SAMPLE T, " +
				"DATA_ACQUISITION_METHOD A, " +
				"POOLED_MS1_FEATURE F " +
				"LEFT JOIN POOLED_MS1_FEATURE_LIBRARY_MATCH H " +
				"ON F.POOLED_MS_FEATURE_ID = H.POOLED_MS_FEATURE_ID " +
				"LEFT JOIN POOLED_MS1_FEATURE_ALTERNATIVE_ID AI  " +
				"ON F.POOLED_MS_FEATURE_ID = AI.POOLED_MS_FEATURE_ID " +
				"WHERE F.SOURCE_DATA_BUNDLE_ID = D.SOURCE_DATA_BUNDLE_ID " +
				"AND D.ACQ_METHOD_ID = A.ACQ_METHOD_ID  " +				
				"AND D.SAMPLE_ID = S.SAMPLE_ID  " +
				"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID ";
			
		if(polarity != null && !polarity.equals(Polarity.Neutral)) {
			
			query += "AND A.POLARITY = ? ";
			parameterMap.put(paramCount++, new SQLParameter(String.class, polarity.getCode()));
		}
		//	Account for non-primary identifications
		if(!lookupSecondaryIds && lookupIds
				//	&& !lookupSecondaryLibMatches //	Hanldle MS/RT libraries here
				)
			query += "AND (H.IS_PRIMARY IS NOT NULL OR AI.IS_PRIMARY IS NOT NULL) ";
		
//		parameterMap.put(paramCount++, new SQLParameter(String.class, polarity.getCode()));

		if(!ignoreMz && monoisotopicPeakMz != null) {

			Range mzRange = MsUtils.createMassRange(monoisotopicPeakMz, massError, massErrorType);
			parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMax()));
			query += "AND F.BASE_PEAK BETWEEN ? AND  ? ";
		}			
		if(rtRange != null) {

			parameterMap.put(paramCount++, new SQLParameter(Double.class, rtRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, rtRange.getMax()));
			query += "AND F.RETENTION_TIME BETWEEN ? AND  ? ";
		}
		if(!chromatographicColumns.isEmpty()) {
			builder = new StringBuilder();
			for(LIMSChromatographicColumn column : chromatographicColumns) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, column.getColumnId()));
				builder.append("?,");
			}			
			query += "AND A.COLUMN_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
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
		if(!acquisitionMethods.isEmpty()) {
			
			builder = new StringBuilder();
			for(DataAcquisitionMethod acqMethod : acquisitionMethods) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, acqMethod.getId()));
				builder.append("?,");
			}			
			query += "AND D.ACQ_METHOD_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		if(!dataExtractionMethods.isEmpty()) {
			
			builder = new StringBuilder();
			for(DataExtractionMethod daMethod : dataExtractionMethods) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, daMethod.getId()));
				builder.append("?,");
			}			
			query += "AND D.EXTRACTION_METHOD_ID IN (" + builder.deleteCharAt( builder.length() -1 ).toString() + ") ";
		}
		//	ID search block
		if(!compoundNameOrId.isEmpty() && idOpt != null) {

			if(idOpt.equals(IdentifierSearchOptions.COMPOUND_ID)) {
				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId));
				query += "AND (H.ACCESSION = ? OR AI.ACCESSION = ?) ";
			}
			if(idOpt.equals(IdentifierSearchOptions.NAME_EQUALS)) {

				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase()));
				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase()));
				query +=
					"AND (H.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) = ?) "
					+ "OR AI.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) = ?))";
			}
			if(idOpt.equals(IdentifierSearchOptions.NAME_STARTS_WITH)) {

				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase() + "%"));
				parameterMap.put(paramCount++, new SQLParameter(String.class, compoundNameOrId.toUpperCase() + "%"));
				query +=
					"AND (H.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?)"
					+ " OR AI.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?))";
			}
			if(idOpt.equals(IdentifierSearchOptions.NAME_CONTAINS)) {

				parameterMap.put(paramCount++, new SQLParameter(String.class, "%" + compoundNameOrId.toUpperCase() + "%"));
				parameterMap.put(paramCount++, new SQLParameter(String.class, "%" + compoundNameOrId.toUpperCase() + "%"));
				query +=
					"AND (H.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?) "
					+ "OR AI.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE UPPER(C.PRIMARY_NAME) LIKE ?))";
			}
		}
		//	Formula
		if(!formula.isEmpty()) {

			parameterMap.put(paramCount++, new SQLParameter(String.class, formula));
			parameterMap.put(paramCount++, new SQLParameter(String.class, formula));
			query +=
				"AND (H.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C.MOL_FORMULA = ?)"
				+ " OR AI.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C.MOL_FORMULA = ?))";
		}
		//	INCHI key 
		if(!inchiKey.isEmpty()) {

			parameterMap.put(paramCount++, new SQLParameter(String.class, inchiKey));
			String column = "INCHI_KEY";
			if(inchiKey.length() == 14)
				column = "INCHI_KEY_CONNECT";
			
			query +=
				"AND (H.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C." + column + " = ?) "
						+ "OR AI.ACCESSION IN (SELECT DISTINCT C.ACCESSION FROM COMPOUND_DATA C WHERE C." + column + " = ?))";
		}		
		query += " ORDER BY RETENTION_TIME, BASE_PEAK";
		
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
		System.out.println(query);
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		while (rs.next()) {
			
			String id = rs.getString("POOLED_MS_FEATURE_ID");
			double rt = rs.getDouble("RETENTION_TIME");
			double mz = rs.getDouble("BASE_PEAK");
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);

			MsFeature f = new MsFeature(id, name, rt);
			f.setPolarity(Polarity.getPolarityByCode(rs.getString("POLARITY")));
			
//			f.setPolarity(polarity);
			f.setAnnotatedObjectType(AnnotatedObjectType.MS_FEATURE_POOLED);
			f.setIdDisabled(rs.getString("ID_DISABLED") != null);
			
			MSFeatureInfoBundle bundle = new MSFeatureInfoBundle(f);
			bundle.setAcquisitionMethod(
				IDTDataCache.getAcquisitionMethodById(rs.getString("ACQ_METHOD_ID")));
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

			features.add(bundle);
			processed++;
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Remove redundant 
		features = features.stream().distinct().collect(Collectors.toSet());
	}
	
	private void attachPooledMS1Spectrums() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MS1 spectra to features ...";
		total = features.size();
		processed = 0;
		String query =
				"SELECT MZ, RT, HEIGHT, ADDUCT_ID, COMPOSITE_ADDUCT_ID "
				+ "FROM POOLED_MS1_FEATURE_PEAK WHERE POOLED_MS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);		
		for(MSFeatureInfoBundle fb : features) {
			
			MassSpectrum spectrum = new MassSpectrum();
			Map<Adduct, Collection<MsPoint>> adductMap =
					new TreeMap<Adduct,Collection<MsPoint>>();

			ps.setString(1, fb.getMsFeature().getId());
			ResultSet msrs = ps.executeQuery();
			while(msrs.next()) {
				
				String adductId = msrs.getString("ADDUCT_ID");
				if(adductId == null)
					adductId = msrs.getString("COMPOSITE_ADDUCT_ID");

				Adduct adduct =
						AdductManager.getAdductById(adductId);

				if(adduct == null)
					adduct = AdductManager.getDefaultAdductForPolarity(fb.getMsFeature().getPolarity());

				if(!adductMap.containsKey(adduct))
					adductMap.put(adduct, new ArrayList<MsPoint>());

				adductMap.get(adduct).add(new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT")));
			}
//			while(msrs.next()) {
//				
//				Adduct adduct = defaultAdduct;
//				String adductId = msrs.getString("ADDUCT_ID");
//				if(adductId == null)
//					adductId = msrs.getString("COMPOSITE_ADDUCT_ID");
//
//				if(adductId != null)
//					adduct = AdductManager.getAdductById(adductId);
//
//				if(!adductMap.containsKey(adduct))
//					adductMap.put(adduct, new ArrayList<MsPoint>());
//
//				adductMap.get(adduct).add(new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT")));
//			}
			msrs.close();
			adductMap.entrySet().stream().
				forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));
			fb.getMsFeature().setSpectrum(spectrum);
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachMS1LibraryIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MS/RT library identifications ...";
		total = features.size();
		processed = 0;
		String query =
				"SELECT I.ACCESSION, I.LIBRARY_ENTRY_ID, I.ADDUCT_ID, I.COMPOSITE_ADDUCT_ID,  " +
				"I.IDENTIFICATION_CONFIDENCE, I.ID_SOURCE, I.MATCH_SCORE,  " +
				"I.IS_PRIMARY, I.IDENTIFICATION_LEVEL_ID, I.MATCH_ID, C.RETENTION_TIME, C.LIBRARY_ID, C.NAME " +
				"FROM POOLED_MS1_FEATURE_LIBRARY_MATCH I " +
				"LEFT JOIN MS_LIBRARY_COMPONENT C ON I.LIBRARY_ENTRY_ID = C.TARGET_ID " +
				"WHERE I.POOLED_MS_FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;	
		for(MSFeatureInfoBundle fb : features) {
			
			ps.setString(1, fb.getMsFeature().getId());
			rs = ps.executeQuery();
			while(rs.next()) {

				CompoundIdentity compoundIdentity =
						CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);
				CompoundIdentificationConfidence confidenceLevel =
						CompoundIdentificationConfidence.getLevelByNumber(rs.getInt("IDENTIFICATION_CONFIDENCE"));
				MsFeatureIdentity id = new MsFeatureIdentity(compoundIdentity, confidenceLevel);
				id.setIdSource(CompoundIdSource.getOptionByName(rs.getString("ID_SOURCE")));
				
				MsRtLibraryMatch match = new MsRtLibraryMatch(
						rs.getString("LIBRARY_ID"), 			
						rs.getString("LIBRARY_ENTRY_ID"), 
						rs.getString("NAME"), 
						rs.getDouble("MATCH_SCORE"),
						rs.getDouble("RETENTION_TIME"), 
						null,
						null);
				match.setObservedRetention(fb.getRetentionTime());
				
				String adductName = rs.getString("ADDUCT_NAME");
				if(adductName != null) {
					Adduct adduct = AdductManager.getAdductByName(adductName);
					if(adduct != null)
						match.getAdductScoreMap().add(new AdductMatch(adduct, adduct, match.getScore()));
				}
				if(rs.getString("IS_PRIMARY") != null)
					id.setPrimary(true);
				
				String statusId = rs.getString("IDENTIFICATION_LEVEL_ID");
				if(statusId != null) 
					id.setIdentificationLevel(IDTDataCache.getMSFeatureIdentificationLevelById(statusId));			

				id.setUniqueId(rs.getString("MATCH_ID"));
				id.setMsRtLibraryMatch(match);
				
				String adductId = rs.getString("ADDUCT_ID");
				if(adductId == null)
					adductId = rs.getString("COMPOSITE_ADDUCT_ID");

				if(adductId != null)
					id.setPrimaryAdduct(AdductManager.getAdductById(adductId));
				
				if(id.isPrimary())
					fb.getMsFeature().setPrimaryIdentity(id);
				else
					fb.getMsFeature().addIdentity(id);

			}
			rs.close();
			
//			Collection<MsFeatureIdentity> msmsIds =
//					IdentificationUtils.getReferenceMS1FeatureLibraryMatches(
//							fb.getMsFeature().getId(), conn);
//
//			for(MsFeatureIdentity cid : msmsIds) {
//				fb.getMsFeature().addIdentity(cid);
//				if(cid.isPrimary())
//					fb.getMsFeature().setPrimaryIdentity(cid);
//			}
			
//			try {
//				IDTMsDataUtils.attachMS1LibraryIdentifications(fb.getMsFeature(), conn);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachMS1ManualIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding manual identifications ...";
		total = features.size();
		processed = 0;
		String query =
				"SELECT IDENTIFICATION_ID, ACCESSION, ID_CONFIDENCE, IS_PRIMARY, ID_SOURCE, "
				+ "IDENTIFICATION_LEVEL_ID, ASSIGNED_BY, ASSIGNED_ON, ADDUCT_ID, COMPOSITE_ADDUCT_ID " +
				"FROM POOLED_MS1_FEATURE_ALTERNATIVE_ID WHERE POOLED_MS_FEATURE_ID = ?";		
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null;
		
		for(MSFeatureInfoBundle fb : features) {
			
			ps.setString(1, fb.getMsFeature().getId());
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
				LIMSUser assignedBy = IDTDataCache.getUserById(rs.getString("ID_SOURCE"));
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
				
				if(id.isPrimary())
					fb.getMsFeature().setPrimaryIdentity(id);
				else
					fb.getMsFeature().addIdentity(id);
			}
			rs.close();			
			
//			Collection<MsFeatureIdentity> msmsIds =
//					IdentificationUtils.getReferenceMS1FeatureManualIds(
//							newTarget.getId(), conn);
//
//			for(MsFeatureIdentity cid : msmsIds) {
//				newTarget.addIdentity(cid);
//				if(cid.isPrimary())
//					newTarget.setPrimaryIdentity(cid);
//			}
			
//			try {
//				IDTMsDataUtils.attachMS1ManualIdentifications(fb.getMsFeature(), conn);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachAnnotations() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding annotations ...";
		total = features.size();
		processed = 0;
		
		String stAnQuery =
				"SELECT STANDARD_ANNOTATION_ID FROM POOLED_MS1_FEATURE_STANDARD_ANNOTATIONS " +
				"WHERE POOLED_MS_FEATURE_ID = ? ";		
		PreparedStatement stAnPs = conn.prepareStatement(stAnQuery);
		ResultSet stAnRs = null;
		
		AdvancedRTFEditorKit editor = new  AdvancedRTFEditorKit();
		String query =
			"SELECT ANNOTATION_ID, ANNOTATION_RTF_DOCUMENT, CREATED_BY, CREATED_ON, "
			+ "LAST_EDITED_BY, LAST_EDITED_ON, LINKED_DOCUMENT_ID, CML, CML_NOTE " +
			"FROM OBJECT_ANNOTATIONS WHERE OBJECT_TYPE = ? AND OBJECT_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, AnnotatedObjectType.MS_FEATURE_POOLED.name());
		ResultSet rs = null;
		
		String mdQuery = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement mdps = conn.prepareStatement(mdQuery);
		ResultSet mdrs = null;
		
		for(MSFeatureInfoBundle fb : features) {
			
			stAnPs.setString(1, fb.getMsFeature().getId());
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
	
	private void attachFollowupSteps() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding follow-up steps ...";
		total = features.size();
		processed = 0;
		
		String query =
				"SELECT FOLLOWUP_STEP_ID FROM POOLED_MS1_FEATURE_FOLLOWUP_STEPS " +
				"WHERE POOLED_MS_FEATURE_ID = ? ";
		
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
	
	private void applyAdditionalFilters() {
		
		taskDescription = "Applying additional filters ...";
		total = 100;
		processed = 80;
		
		//	ID state		
		Collection<MSFeatureInfoBundle> filteredByIdStatus = 
				MsFeatureStatsUtils.filterFeaturesByIdSubset(features,featureSubsetById);
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
					filter(f -> idLevels.contains(f.getMsFeature().
							getPrimaryIdentity().getIdentificationLevel())).
					collect(Collectors.toList());
			features.clear();
			features.addAll(byIdLevel);
		}
		if(!follwUpSteps.isEmpty()) {
			
			List<MSFeatureInfoBundle> byFollowup = features.stream().
					filter(f -> !CollectionUtils.intersection(
							follwUpSteps, f.getIdFollowupSteps()).isEmpty()).
					collect(Collectors.toList());
			features.clear();
			features.addAll(byFollowup);
		}
		//	Make sure disabled ids are honored
		features.stream().
			filter(f -> f.getMsFeature().isIdDisabled()).
			forEach(f -> f.getMsFeature().setPrimaryIdentity(null));
	}
	
	@Override
	public Task cloneTask() {

		return new IDTMS1FeatureSearchTask(
				polarity,
				monoisotopicPeakMz,
				massError,
				massErrorType,
				ignoreMz,
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
				acquisitionMethods,
				dataExtractionMethods);
	}
	
	public Collection<MSFeatureInfoBundle> getSelectedFeatures() {
		return features;
	}
}
