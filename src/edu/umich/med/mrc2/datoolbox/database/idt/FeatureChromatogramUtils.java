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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;

import org.apache.commons.jcs3.access.exception.CacheException;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.StoredExtractedIonData;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;

public class FeatureChromatogramUtils {

	public static void putFeatureChromatogramBundleInCache(MsFeatureChromatogramBundle bundle) {

		String key = bundle.getFeatureId();
		try {
			MRC2ToolBoxCore.featureChromatogramCache.put(key, bundle);
		} catch (CacheException e) {
			System.err.println(String.format(
					"Problem putting feature chromatogram bundle in the cache, for key %s%n%s",
					key, e.getMessage()));
		}
	}

	public static MsFeatureChromatogramBundle retrieveFeatureChromatogramBundleFromCache(String msId) {
		return (MsFeatureChromatogramBundle) 
				MRC2ToolBoxCore.featureChromatogramCache.get(msId);
	}
	
	public static MsFeatureChromatogramBundle getMsFeatureChromatogramBundleForFeature(
			String featureId, DataFile dataFile) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		MsFeatureChromatogramBundle fcb = 
				getMsFeatureChromatogramBundleForFeature(featureId, dataFile, conn);
		ConnectionManager.releaseConnection(conn);
		return fcb;
	}
	
	public static MsFeatureChromatogramBundle getMsFeatureChromatogramBundleForFeature(
			String featureId, DataFile dataFile, Connection conn) throws Exception {
		
		MsFeatureChromatogramBundle chromatogramBundle = null;
		String query = 
				"SELECT INJECTION_ID, MS_LEVEL, EXTRACTED_MASS,  " +
				"MASS_ERROR_VALUE, MASS_ERROR_TYPE, START_RT, END_RT,  " +
				"TITLE, TIME_VALUES, INTENSITY_VALUES " +
				"FROM MSMS_PARENT_FEATURE_CHROMATOGRAM  " +
				"WHERE FEATURE_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, featureId);
		ResultSet rs = ps.executeQuery();
		ps.setString(1, featureId);
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
					featureId,
					rs.getString("INJECTION_ID"), 
					rs.getInt("MS_LEVEL"), 
					rs.getDouble("MASS_ERROR_VALUE"),
					MassErrorType.getTypeByName(rs.getString("MASS_ERROR_TYPE")), 
					rs.getDouble("START_RT"),
					rs.getDouble("END_RT"));

			ChromatogramDefinition chromDef = new ChromatogramDefinition(
						null, 
						seid.getMsLevel(), 
						Collections.singleton(seid.getExtractedMass()),
						seid.getMassErrorValue(), 
						seid.getMassErrorType(), 
						seid.getRtRange());
			chromatogramBundle = 
					new MsFeatureChromatogramBundle(featureId, chromDef);
			
			chromatogramBundle.addChromatogramForDataFile(dataFile, seid);	
		}
		rs.close();
		ps.close();
		return chromatogramBundle;
	}
}


















