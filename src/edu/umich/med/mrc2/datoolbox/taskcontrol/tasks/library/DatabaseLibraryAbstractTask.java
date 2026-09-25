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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openscience.cdk.exception.CDKException;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public abstract class DatabaseLibraryAbstractTask extends AbstractTask {

	private static final Logger logger = LogManager.getLogger(DatabaseLibraryAbstractTask.class);
	
	protected Collection<LibraryMsFeature>fetchFeaturesForLibrary(String libraryId) {
		
		Collection<LibraryMsFeature>features = new ArrayList<>();
		Connection conn = ConnectionManager.getConnection();
		Collection<LibraryMsFeatureDbBundle>bundles = createFeatureBundlesForLibrary(libraryId, conn);

		total = bundles.size();
		processed = 0;

		for(LibraryMsFeatureDbBundle fBundle : bundles) {

			if(fBundle.getConmpoundDatabaseAccession() != null) {

				LibraryMsFeature newTarget = fBundle.getFeature();
				try {
					MSRTLibraryUtils.attachIdentity(
							newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(newTarget.getPrimaryIdentity() != null) {

					newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
					try {
						MSRTLibraryUtils.attachMassSpectrum(newTarget, conn);
						MSRTLibraryUtils.attachTandemMassSpectrum(newTarget, conn);
						MSRTLibraryUtils.attachAnnotations(newTarget, conn);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					features.add(newTarget);
				}
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
		return features;
	}
	
	protected Collection<LibraryMsFeatureDbBundle> createFeatureBundlesForLibrary(String libraryId, Connection conn){

		taskDescription = "Fetching MSRT library feature bundles";
		Collection<LibraryMsFeatureDbBundle>bundles = new ArrayList<>();
		total = IDTDataCache.getMsRtLibraryEntryCount().get(libraryId);
		if(total == 0)
			total = 500;
		
		String query =
			"SELECT TARGET_ID, ACCESSION, NAME, RETENTION_TIME, RT_MIN, RT_MAX, ID_CONFIDENCE, "+
			"DATE_LOADED, LAST_MODIFIED, ENABLED, IS_QC FROM MS_LIBRARY_COMPONENT "
			+ "WHERE LIBRARY_ID = ? ORDER BY NAME";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, libraryId);
			try(ResultSet idrs = ps.executeQuery()){
				while (idrs.next()) {		
					double rt = idrs.getDouble("RETENTION_TIME");
					LibraryMsFeature feature = new LibraryMsFeature(
							idrs.getString("NAME"),
							rt,
							idrs.getString("ENABLED"),
							idrs.getString("TARGET_ID"),
							idrs.getDate("DATE_LOADED").getTime(),
							idrs.getDate("LAST_MODIFIED").getTime());		
					feature.setLibraryId(libraryId);
					double rtMin = idrs.getDouble("RT_MIN");
					double rtMax = idrs.getDouble("RT_MAX");
					feature.setRtRange(new Range(rtMin, rtMax));	
					boolean qcStandard = (idrs.getString("IS_QC") != null);
					CompoundIdentificationConfidence confidenceLevel  =
							CompoundIdentificationConfidence.getLevelById(idrs.getString("ID_CONFIDENCE"));
					bundles.add(new LibraryMsFeatureDbBundle(
							feature, idrs.getString("ACCESSION"), confidenceLevel, qcStandard));
					
					processed++;
				}
			}
		} catch (SQLException e) {
			logger.error(String.format("Failed to fetch feature bundles for library with ID %s", libraryId));
		}
		return bundles;
	}
	
}
