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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.LibMatchedSimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class IDTMsDataUtils {

	public static void uploadPoolMs1Feature(
			LibMatchedSimpleMsFeature feature,
			String referenceMS1DataBundleId,
			Connection conn) throws Exception{
		
		String featureId = SQLUtils.getNextIdFromSequence(conn, 
				"MS_POOL_FEATURE_SEQ",
				DataPrefix.MS_FEATURE_POOLED,
				"0",
				12);
		feature.setUniqueId(featureId);
		String query =
				"INSERT INTO POOLED_MS1_FEATURE "
				+ "(POOLED_MS_FEATURE_ID, SOURCE_DATA_BUNDLE_ID, RETENTION_TIME, HEIGHT, "
				+ "AREA, DETECTION_ALGORITHM, ACCESSION, BASE_PEAK)"
				+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ps.setString(2, referenceMS1DataBundleId);
		ps.setDouble(3, feature.getRetentionTime());
		ps.setDouble(4, feature.getHeight());
		ps.setDouble(5, Math.round(feature.getArea()));
		ps.setString(6, feature.getObservedSpectrum().getDetectionAlgorithm());
		if(feature.getIdentity().getCompoundIdentity().getPrimaryDatabase() != null)
			ps.setString(7, feature.getIdentity().getCompoundIdentity().getPrimaryDatabaseId());
		else
			ps.setString(7, null);

		ps.setDouble(8, feature.getObservedSpectrum().getBasePeakMz());
		ps.executeUpdate();
		ps.close();

		//	Add MS1
		MassSpectrum msOne = feature.getObservedSpectrum();
		if(!msOne.getAdducts().isEmpty()) {

			query = "INSERT INTO POOLED_MS1_FEATURE_PEAK (POOLED_MS_FEATURE_ID, MZ, HEIGHT, "
					+ "ADDUCT_ID, COMPOSITE_ADDUCT_ID) VALUES (?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, featureId);
			for(Adduct adduct : msOne.getAdducts()) {
				
				String adductId = null;
				String compositeAdductId = null;
				if(adduct instanceof SimpleAdduct)
					adductId = adduct.getId();
				
				if(adduct instanceof CompositeAdduct)
					compositeAdductId = adduct.getId();

				for(MsPoint point : msOne.getMsForAdduct(adduct)) {

					ps.setDouble(2, point.getMz());
					ps.setDouble(3, Math.round(point.getIntensity()));
					ps.setString(4, adductId);
					ps.setString(5, compositeAdductId);
					ps.addBatch();
				}
			}
			ps.executeBatch();
			ps.close();
		}
		ps.close();
	}

	public static Collection<MsFeature>getReferenceMS1FeaturesForSample(
			String sampleId,
			String acquisitionMethodId,
			String daMethodId,
			Connection conn) throws Exception{

		Collection<MsFeature>features = new ArrayList<MsFeature>();
		String query =
			"SELECT F.POOLED_MS_FEATURE_ID, F.BASE_PEAK, F.RETENTION_TIME, F.AREA " +
			"FROM POOLED_MS1_DATA_SOURCE R, " +
			"POOLED_MS1_FEATURE F " +
			"WHERE R.SAMPLE_ID = ? " +
			"AND R.ACQ_METHOD_ID = ? " +
			"AND R.EXTRACTION_METHOD_ID = ? " +
			"AND R.SOURCE_DATA_BUNDLE_ID = F.SOURCE_DATA_BUNDLE_ID " +
			"ORDER BY F.RETENTION_TIME, F.BASE_PEAK ";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, sampleId);
		ps.setString(2, acquisitionMethodId);
		ps.setString(3, daMethodId);

		String msQuery = "SELECT MZ, HEIGHT, ADDUCT_ID, COMPOSITE_ADDUCT_ID "
				+ "FROM POOLED_MS1_FEATURE_PEAK WHERE FEATURE_ID = ? ";
		PreparedStatement psms = conn.prepareStatement(msQuery);
		Map<String,Collection<MsPoint>>adductMap = new TreeMap<String,Collection<MsPoint>>();
		ResultSet rs = ps.executeQuery();
		ResultSet msrs = null;
		while (rs.next()) {

			double bpMz = rs.getDouble("BASE_PEAK");
			double rt = rs.getDouble("RETENTION_TIME");
			String id = rs.getString("MS_FEATURE_ID");
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(bpMz) + "_" +
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);
			MsFeature newFeature = new MsFeature(name, bpMz, rt);
			newFeature.setId(id);

			psms.setString(1, id);
			msrs = psms.executeQuery();
			MassSpectrum observedSpectrum = new MassSpectrum();
			adductMap.clear();
			while (msrs.next()) {

				MsPoint p = new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT"));
				String adductId = msrs.getString("ADDUCT_ID");
				String compositeAdductId = msrs.getString("COMPOSITE_ADDUCT_ID");
				if(adductId != null) {
					
					if(!adductMap.containsKey(adductId))
						adductMap.put(adductId, new ArrayList<MsPoint>());

					adductMap.get(adductId).add(p);
				}
				if(compositeAdductId != null) {
					
					if(!adductMap.containsKey(compositeAdductId))
						adductMap.put(compositeAdductId, new ArrayList<MsPoint>());

					adductMap.get(compositeAdductId).add(p);
				}
			}
			msrs.close();
			for (Entry<String, Collection<MsPoint>> entry : adductMap.entrySet()) {

				Adduct cm = AdductManager.getAdductById(entry.getKey());
				if(cm != null)
					observedSpectrum.addSpectrumForAdduct(cm, entry.getValue());
			}
			newFeature.setSpectrum(observedSpectrum);
			features.add(newFeature);
		}
		rs.close();
		ps.close();
		psms.close();

		return features;
	}

	public static void attachMS1SpectrumForMsMs(
			MsFeature newTarget, Connection conn) throws SQLException {

		MassSpectrum spectrum = new MassSpectrum();
		Map<Adduct, Collection<MsPoint>> adductMap =
				new TreeMap<Adduct,Collection<MsPoint>>();

		String query =
			"SELECT ADDUCT_ID, COMPOSITE_ADDUCT_ID, MZ, HEIGHT "
			+ "FROM MSMS_PARENT_FEATURE_PEAK WHERE FEATURE_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newTarget.getId());
		ResultSet msrs = ps.executeQuery();
		while(msrs.next()) {
			
			String adductId = msrs.getString("ADDUCT_ID");
			if(adductId == null)
				adductId = msrs.getString("COMPOSITE_ADDUCT_ID");

			Adduct adduct =
					AdductManager.getAdductById(adductId);

			if(adduct == null)
				continue;

			if(!adductMap.containsKey(adduct))
				adductMap.put(adduct, new ArrayList<MsPoint>());

			adductMap.get(adduct).add(new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT")));
		}
		msrs.close();
		ps.close();
		adductMap.entrySet().stream().
			forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

		newTarget.setSpectrum(spectrum);
	}

	public static void attachPooledMS1Spectrum(
			MsFeature newTarget, Connection conn) throws SQLException {

		MassSpectrum spectrum = new MassSpectrum();
		Map<Adduct, Collection<MsPoint>> adductMap =
				new TreeMap<Adduct,Collection<MsPoint>>();

		String query =
			"SELECT MZ, RT, HEIGHT, ADDUCT_ID, COMPOSITE_ADDUCT_ID "
			+ "FROM POOLED_MS1_FEATURE_PEAK WHERE POOLED_MS_FEATURE_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newTarget.getId());
		ResultSet msrs = ps.executeQuery();
		while(msrs.next()) {
			
			String adductId = msrs.getString("ADDUCT_ID");
			if(adductId == null)
				adductId = msrs.getString("COMPOSITE_ADDUCT_ID");

			Adduct adduct =
					AdductManager.getAdductById(adductId);

			if(adduct == null)
				continue;

			if(!adductMap.containsKey(adduct))
				adductMap.put(adduct, new ArrayList<MsPoint>());

			adductMap.get(adduct).add(new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT")));
		}
		msrs.close();
		ps.close();
		adductMap.entrySet().stream().
			forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));

		newTarget.setSpectrum(spectrum);
	}

	public static void attachMsMsLibraryIdentifications(
			MsFeature newTarget,
			Connection conn) throws Exception {

		TandemMassSpectrum msms = newTarget.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(msms == null)
			return;
		
		Collection<MsFeatureIdentity> msmsIds =
				DatabaseIdentificationUtils.getMSMSFeatureLibraryMatches(msms.getId(), conn);

		for(MsFeatureIdentity cid : msmsIds) {
			
			if(cid.isPrimary())
				newTarget.setPrimaryIdentity(cid);
			else
				newTarget.addIdentity(cid);
		}		
	}
	
	public static void attachMsMsManualIdentifications(
			MsFeature newTarget,
			Connection conn) throws Exception {

		TandemMassSpectrum msms = newTarget.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(msms == null)
			return;

		Collection<MsFeatureIdentity>altIds = 
				DatabaseIdentificationUtils.getMSMSFeatureManualIds(msms.getId(), conn);
		
		for(MsFeatureIdentity cid : altIds) {
			
			if(cid.isPrimary())
				newTarget.setPrimaryIdentity(cid);
			else
				newTarget.addIdentity(cid);
		}
	}

	@Deprecated
	public static void attachExperimentalTandemSpectra(
			MsFeature newTarget, Connection conn) throws SQLException {

		String query =
			"SELECT MSMS_FEATURE_ID, PARENT_MZ, FRAGMENTATION_ENERGY, "
			+ "COLLISION_ENERGY, POLARITY, ISOLATION_WINDOW_MIN, ISOLATION_WINDOW_MAX " +
			"FROM MSMS_FEATURE WHERE PARENT_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newTarget.getId());	
		ResultSet rs = ps.executeQuery();
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
			msmsList.add(msms);

			Range isolationWindow = new Range(
					rs.getDouble("ISOLATION_WINDOW_MIN"), 
					rs.getDouble("ISOLATION_WINDOW_MAX"));
			if(isolationWindow.getAverage() == 0.0d)	//	TODO this is a temporary fix based on Agilent narrow window
				isolationWindow = new Range(parent.getMz() - 0.65, parent.getMz() + 0.65);
			
			msms.setIsolationWindow(isolationWindow);
		}
		rs.close();
		ps.close();		
		String msquery =
				"SELECT MZ, HEIGHT FROM MSMS_FEATURE_PEAK WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msquery);
		ResultSet msrs = null;
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
								
			newTarget.getSpectrum().addTandemMs(msms);
		}
		msps.close();
	}

	public static void addNewPrimaryIdentity(
			MsFeature newTarget,
			String cid,
			CompoundIdSource idSource,
			CompoundIdentificationConfidence idConfidence,
			Connection conn) throws SQLException {

		String query =
			"SELECT SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, SMILES, INCHI_KEY "+
			"FROM COMPOUND_DATA D WHERE D.ACCESSION = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, cid);
		ResultSet cidrs = ps.executeQuery();

		while (cidrs.next()){

			CompoundDatabaseEnum dbSource =
					CompoundDatabaseEnum.getCompoundDatabaseByName(cidrs.getString("SOURCE_DB"));

			CompoundIdentity identity = new CompoundIdentity(
					dbSource,
					cid,
					cidrs.getString("PRIMARY_NAME"),
					cidrs.getString("MOL_FORMULA"),
					cidrs.getDouble("EXACT_MASS"),
					cidrs.getString("SMILES"),
					cidrs.getString("INCHI_KEY"));

			if(dbSource != null){

				MsFeatureIdentity mid = new MsFeatureIdentity(identity,idConfidence);
				mid.setIdSource(idSource);
				newTarget.setPrimaryIdentity(mid);
				newTarget.setNeutralMass(identity.getExactMass());
			}
			else{
				System.out.println(newTarget.getId() + " : " + cid);
			}
		}
		cidrs.close();
		ps.close();
	}
	
	public static void attachMS1LibraryIdentifications(
			MsFeature newTarget,
			Connection conn) throws Exception {
		
		Collection<MsFeatureIdentity> msmsIds =
				DatabaseIdentificationUtils.getReferenceMS1FeatureLibraryMatches(
						newTarget.getId(), conn);

		for(MsFeatureIdentity cid : msmsIds) {
			
			if(cid.isPrimary())
				newTarget.setPrimaryIdentity(cid);
			else
				newTarget.addIdentity(cid);
		}		
	}
	
	public static void attachMS1ManualIdentifications(
			MsFeature newTarget,
			Connection conn) throws Exception {

		Collection<MsFeatureIdentity>altIds = 
				DatabaseIdentificationUtils.getReferenceMS1FeatureManualIds(
						newTarget.getId(), conn);
		
		for(MsFeatureIdentity cid : altIds) {
			
			if(cid.isPrimary())
				newTarget.setPrimaryIdentity(cid);
			else
				newTarget.addIdentity(cid);
		}
	}
}
