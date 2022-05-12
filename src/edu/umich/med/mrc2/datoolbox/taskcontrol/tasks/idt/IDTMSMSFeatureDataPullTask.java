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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTMSMSFeatureDataPullTask extends IDTMSMSFeatureSearchTask {

	private Collection<String>featureIds;

	public IDTMSMSFeatureDataPullTask(Collection<String> featureIds) {
		super();
		this.featureIds = featureIds;
		features = new ArrayList<MsFeatureInfoBundle>();
	}

	@Override
	public void run() {
		taskDescription = "Getting feature data from IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			getMsMsFeatures();
			if(!features.isEmpty()) {
				
				attachExperimentalTandemSpectra();
				attachMsMsLibraryIdentifications();
				attachMsMsManualIdentities();
				retievePepSearchParameters();
				attachAnnotations();
				attachFollowupSteps();
				putDataInCache();
			}
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
	}
	
	private void getMsMsFeatures() throws Exception {
		
		taskDescription = "Fetching MS2 features ...";
		total = featureIds.size();
		processed = 0;	
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT F.FEATURE_ID, F.POLARITY, F.MZ_OF_INTEREST, F.RETENTION_TIME, " +
				"I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, S.EXPERIMENT_ID, S.SAMPLE_ID, " +
				"T.STOCK_SAMPLE_ID, I.INJECTION_ID, F2.COLLISION_ENERGY  " +
				"FROM MSMS_PARENT_FEATURE F, " +
				"DATA_ANALYSIS_MAP M, " +
				"DATA_ACQUISITION_METHOD A, " +
				"INJECTION I, " +
				"PREPARED_SAMPLE P, " +
				"SAMPLE S, " +
				"STOCK_SAMPLE T, " +
				"MSMS_FEATURE F2 " +
				"WHERE F2.MSMS_FEATURE_ID = ? " +
				"AND F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID " +
				"AND  F2.PARENT_FEATURE_ID = F.FEATURE_ID " +
				"AND M.INJECTION_ID = I.INJECTION_ID " +
				"AND A.ACQ_METHOD_ID = I.ACQUISITION_METHOD_ID " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID " +
				"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID " +
				"AND F.BASE_PEAK IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		String msOneQuery =
				"SELECT ADDUCT_ID, COMPOSITE_ADDUCT_ID, MZ, HEIGHT "
				+ "FROM MSMS_PARENT_FEATURE_PEAK WHERE FEATURE_ID = ?";
		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);
		
		for(String msmsId : featureIds) {
			
			ps.setString(1, msmsId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				String id = rs.getString("FEATURE_ID");
				double rt = rs.getDouble("RETENTION_TIME");
				double mz = rs.getDouble("MZ_OF_INTEREST");
				String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
						MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" + 
						MRC2ToolBoxConfiguration.getRtFormat().format(rt);

				MsFeature f = new MsFeature(id, name, rt);
				polarity = Polarity.getPolarityByCode(rs.getString("POLARITY"));
				f.setPolarity(polarity);
				f.setAnnotatedObjectType(AnnotatedObjectType.MSMS_FEATURE);
				
				MassSpectrum spectrum = new MassSpectrum();
				Map<Adduct, Collection<MsPoint>> adductMap =
						new TreeMap<Adduct,Collection<MsPoint>>();
				
				msOnePs.setString(1, id);
				ResultSet msOneRs = msOnePs.executeQuery();
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
					IDTUtils.getExperimentalSampleById(rs.getString("SAMPLE_ID"), conn);
				bundle.setSample(sample);
				bundle.setInjectionId(rs.getString("INJECTION_ID"));
				features.add(bundle);
			}			
			rs.close();
		}		
		ps.close();
		msOnePs.close();
		ConnectionManager.releaseConnection(conn);
		
		//	Remove redundant
		features = features.stream().distinct().collect(Collectors.toSet());
	}
		
	@Override
	public Task cloneTask() {
		return new IDTMSMSFeatureDataPullTask(featureIds);
	}
}
