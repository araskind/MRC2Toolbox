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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureDataSource;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.IdentifierSearchOptions;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTMsDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdFollowupUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.StandardAnnotationUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class IDTMsOneFeatureSearchTask extends IDTFeatureSearchTask {


	public IDTMsOneFeatureSearchTask(
			Double basePeakMz,
			Double massError,
			MassErrorType massErrorType,
			boolean ignoreMz,
			Range rtRange,
			String featureName,
			IdentifierSearchOptions idOpt,
			MsType msType,
			LIMSChromatographicColumn chromatographicColumn,
			LIMSSampleType sampleType,
			Double collisionEnergy,
			Polarity polarity,
			LIMSExperiment experiment) {

		super(basePeakMz,
				massError,
				massErrorType,
				ignoreMz,
				rtRange,
				featureName,
				idOpt,
				msType,
				chromatographicColumn,
				sampleType,
				collisionEnergy,
				polarity,
				experiment);
	}

	@Override
	public void run() {
		taskDescription = "Looking up MS1 features in IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			selectMsFeatures();
			if(!features.isEmpty()) {
				
				attachAnnotations();
				attachMS1LibraryIdentities();
				attachMS1ManualIdentities();
				attachFollowupSteps();
			}
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
	}

	private void selectMsFeatures() throws Exception {
		
		//	TODO handle MS1 identifications., for now if now MZ or RT limits do not search
		if(basePeakMz == null && rtRange == null)
			return;
		
		Connection conn = ConnectionManager.getConnection();
		Collection<MsFeatureDataSource>sources = getMsOneSources(conn);
		if(sources.isEmpty())
			return;

		taskDescription = "Fetching MS1 features ...";
		List<String> sourceIds = sources.stream().map(s -> s.getDataSourceId()).collect(Collectors.toList());
		String sourceListString = "'" + StringUtils.join(sourceIds, "','") + "'";

		String query =
				"SELECT DISTINCT SOURCE_DATA_BUNDLE_ID, F.POOLED_MS_FEATURE_ID, "
				+ "F.RETENTION_TIME, F.AREA, F.BASE_PEAK " +
				"FROM POOLED_MS1_FEATURE F " +
				"WHERE F.SOURCE_DATA_BUNDLE_ID IN (" + sourceListString + ") ";

		Map<Integer,SQLParameter>parameterMap = new TreeMap<Integer,SQLParameter>();
		int paramCount = 1;
		if(!ignoreMz && basePeakMz != null) {

			Range mzRange = MsUtils.createMassRange(basePeakMz, massError, massErrorType);
			parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, mzRange.getMax()));
			query += "AND F.BASE_PEAK >= ? AND F.BASE_PEAK <= ? ";
		}
		if(rtRange != null) {

			parameterMap.put(paramCount++, new SQLParameter(Double.class, rtRange.getMin()));
			parameterMap.put(paramCount++, new SQLParameter(Double.class, rtRange.getMax()));
			query += "AND F.RETENTION_TIME >= ? AND F.RETENTION_TIME <= ? ";
		}
		query += " ORDER BY F.BASE_PEAK, F.RETENTION_TIME";

		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);

		for(Entry<Integer, SQLParameter> entry : parameterMap.entrySet()) {

			if(entry.getValue().getClazz().equals(String.class))
				ps.setString(entry.getKey(), (String)entry.getValue().getValue());

			if(entry.getValue().getClazz().equals(Double.class))
				ps.setDouble(entry.getKey(), (Double)entry.getValue().getValue());
		}
		ResultSet rs = ps.executeQuery();
		total = 0;
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
			f.setPolarity(polarity);
			f.setAnnotatedObjectType(AnnotatedObjectType.MS_FEATURE_POOLED);
			IDTMsDataUtils.attachPooledMS1Spectrum(f, conn);
			MsFeatureInfoBundle bundle = new MsFeatureInfoBundle(f);
			String soId = rs.getString("SOURCE_DATA_BUNDLE_ID");
			MsFeatureDataSource so = sources.stream().
					filter(s -> s.getDataSourceId().equals(soId)).findFirst().get();
			if(so != null) {
				bundle.setAcquisitionMethod(so.getAcquisitionMethod());
				bundle.setDataExtractionMethod(so.getDataExtractionMethod());
				bundle.setExperiment(so.getExperiment());
				bundle.setSample(so.getSample());
				features.add(bundle);
			}
			processed++;
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachMS1LibraryIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MS/RT library identifications ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			try {
				IDTMsDataUtils.attachMS1LibraryIdentifications(fb.getMsFeature(), conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachMS1ManualIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding manual identifications ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			try {
				IDTMsDataUtils.attachMS1ManualIdentifications(fb.getMsFeature(), conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachAnnotations() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding annotations ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			Collection<ObjectAnnotation>featureAnnotations = new ArrayList<ObjectAnnotation>();
			try {
				 featureAnnotations = AnnotationUtils.getObjectAnnotations(
						 AnnotatedObjectType.MS_FEATURE_POOLED, 
						 fb.getMsFeature().getId(), conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!featureAnnotations.isEmpty())
				featureAnnotations.stream().forEach(a -> fb.getMsFeature().addAnnotation(a));
			
			StandardAnnotationUtils.attachStandardFeatureAnnotationsToMS1Feature(fb, conn);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachFollowupSteps() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding follow-up steps ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			
			IdFollowupUtils.attachIdFollowupStepsToMSMSFeature(fb, conn);			
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private Collection<MsFeatureDataSource> getMsOneSources(Connection conn) throws SQLException {

		taskDescription = "Getting MS1 data sources ...";
		total = 0;
		processed = 20;
		Collection<MsFeatureDataSource>sources = new ArrayList<MsFeatureDataSource>();
		String query =
				"SELECT DISTINCT D.SOURCE_DATA_BUNDLE_ID, D.SAMPLE_ID, D.EXPERIMENT_ID, " +
				"D.ACQ_METHOD_ID, D.EXTRACTION_METHOD_ID " +
				"FROM POOLED_MS1_DATA_SOURCE D, " +
				"SAMPLE S, " +
				"STOCK_SAMPLE T, " +
				"DATA_ACQUISITION_METHOD A " +
				"WHERE D.ACQ_METHOD_ID = A.ACQ_METHOD_ID " +
				"AND A.POLARITY = ? " +
				"AND D.SAMPLE_ID = S.SAMPLE_ID " +
				"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID ";

		Map<Integer, SQLParameter> parameterMap = new TreeMap<Integer, SQLParameter>();
		int paramCount = 1;
		parameterMap.put(paramCount++, new SQLParameter(String.class, polarity.getCode()));
		if (msType != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, msType.getId()));
			query += "AND A.MS_TYPE = ? ";
		}
		if (chromatographicColumn != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, chromatographicColumn.getColumnId()));
			query += "AND A.COLUMN_ID = ? ";
		}
		if (sampleType != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, sampleType.getId()));
			query += "AND T.SAMPLE_TYPE_ID = ? ";
		}
		if (experiment != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, experiment.getId()));
			query += "AND D.EXPERIMENT_ID = ? ";
		}
		query += " ORDER BY 1";

		PreparedStatement ps = conn.prepareStatement(query);
		for (Entry<Integer, SQLParameter> entry : parameterMap.entrySet()) {

			if (entry.getValue().getClazz().equals(String.class))
				ps.setString(entry.getKey(), (String) entry.getValue().getValue());

			if (entry.getValue().getClazz().equals(Double.class))
				ps.setDouble(entry.getKey(), (Double) entry.getValue().getValue());
		}
		//	System.out.println(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			MsFeatureDataSource source = new MsFeatureDataSource(rs.getString("SOURCE_DATA_BUNDLE_ID"));
			IDTExperimentalSample sample = IDTUtils.getExperimentalSample(rs.getString("SAMPLE_ID"),  conn);
			source.setSample(sample);
			LIMSExperiment experiment = IDTDataCash.getExperimentById(rs.getString("EXPERIMENT_ID"));
			source.setExperiment(experiment);
			DataAcquisitionMethod acquisitionMethod = IDTDataCash.getAcquisitionMethodById(rs.getString("ACQ_METHOD_ID"));
			source.setAcquisitionMethod(acquisitionMethod);
			DataExtractionMethod dataExtractionMethod = IDTDataCash.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID"));
			source.setDataExtractionMethod(dataExtractionMethod);
			sources.add(source);
		}
		rs.close();
		ps.close();
		return sources;
	}

	@Override
	public Task cloneTask() {

		return new IDTMsOneFeatureSearchTask(
				basePeakMz,
				massError,
				massErrorType,
				ignoreMz,
				rtRange,
				featureName,
				idOpt,
				msType,
				chromatographicColumn,
				sampleType,
				collisionEnergy,
				polarity,
				experiment);
	}
}
