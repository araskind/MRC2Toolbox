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

package edu.umich.med.mrc2.datoolbox.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACQCSampleType;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;

public class ReferenceSamplesManager {

	private static Collection<ExperimentalSample>referenceSamples;
	private static ExperimentDesignFactor scFactor;
	public static final String REGULAR_SAMPLE = "Sample";
	public static final ExperimentDesignLevel sampleLevel = new ExperimentDesignLevel(REGULAR_SAMPLE);
	public static final String MASTER_POOL = "Master pool";
	public static final ExperimentDesignLevel masterPoolLevel = new ExperimentDesignLevel(MASTER_POOL);
	public static final String REFERENCE_SAMPLE = "REFERENCE_SAMPLE";

	public static void getReferenceSamplesFromDatabase() throws Exception {

		referenceSamples = new ArrayList<ExperimentalSample>();
		scFactor = new ExperimentDesignFactor(StandardFactors.SAMPLE_CONTROL_TYPE.getName());
		scFactor.addLevel(sampleLevel);
		scFactor.addLevel(masterPoolLevel);
		
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT SAMPLE_ID, SAMPLE_NAME, SAMPLE_TYPE, IS_LOCKED, MOTRPAC_REF_TYPE, "
			+ "LIMS_ID, LIMS_NAME, INCLUDE_IN_POOL_STATS FROM REFERENCE_SAMPLE ORDER BY LIMS_ID";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			ExperimentalSample newSample = new ExperimentalSample(
					rs.getString("LIMS_ID"), 
					rs.getString("LIMS_NAME"));
			newSample.setSampleIdDeprecated(rs.getString("SAMPLE_ID"));
			newSample.setSampleNameDeprecated(rs.getString("SAMPLE_NAME"));			
			newSample.setLockedReference(rs.getString("IS_LOCKED") != null);
			newSample.setIncloodeInPoolStats(rs.getString("INCLUDE_IN_POOL_STATS") != null);			
			String mpType = rs.getString("MOTRPAC_REF_TYPE");
			if(mpType != null)
				newSample.setMoTrPACQCSampleType(MoTrPACQCSampleType.getOptionByUIName(mpType));

			ExperimentDesignLevel typeLevel = new ExperimentDesignLevel(rs.getString("LIMS_NAME"));
			scFactor.addLevel(typeLevel);
			newSample.addDesignLevel(typeLevel);
			referenceSamples.add(newSample);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void refreshReferenceSampleList() {
		
		try {
			getReferenceSamplesFromDatabase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	TODO handle "Include in pool stats"
	public static int addReferenceSample(ExperimentalSample newRefSample) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"INSERT INTO REFERENCE_SAMPLE(LIMS_ID, LIMS_NAME, SAMPLE_TYPE, IS_LOCKED, "
			+ "MOTRPAC_REF_TYPE, SAMPLE_ID, SAMPLE_NAME) VALUES(?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newRefSample.getId());
		ps.setString(2, newRefSample.getName());
		ps.setString(3, REFERENCE_SAMPLE);
		ps.setString(4, "N");
		ps.setString(5, newRefSample.getMoTrPACQCSampleType().getName());
		ps.setString(6, newRefSample.getId());
		ps.setString(7, newRefSample.getName());
		int inserted = ps.executeUpdate();
		if(inserted == 1) {
			ExperimentDesignLevel typeLevel = new ExperimentDesignLevel(newRefSample.getName());
			scFactor.addLevel(typeLevel);
			newRefSample.addDesignLevel(typeLevel);
			referenceSamples.add(newRefSample);
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return inserted;
	}
	
	//	TODO handle "Include in pool stats"
	public static int updateReferenceSample(
			ExperimentalSample sample, 
			String newId, 
			String newName, 
			MoTrPACQCSampleType moTrPACQCSampleType) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE REFERENCE_SAMPLE SET LIMS_ID = ?, LIMS_NAME = ?, MOTRPAC_REF_TYPE = ? WHERE SAMPLE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, newId);
		ps.setString(2, newName);
		ps.setString(3, moTrPACQCSampleType.name());
		ps.setString(4, sample.getId());

		int updated = ps.executeUpdate();
		if(updated == 1) {

			scFactor.getLevelByName(sample.getName()).setName(newName);
			sample.setId(newId);
			sample.setName(newName);
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);

		return updated;
	}

	public static void deleteReferenceSample(ExperimentalSample refSample) throws Exception {

		referenceSamples.remove(refSample);
		ExperimentDesignLevel refLevel = scFactor.getLevelByName(refSample.getName());
		if(refLevel != null)
			scFactor.removeLevel(refLevel,false);

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM REFERENCE_SAMPLE WHERE LIMS_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, refSample.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static ExperimentDesignFactor getSampleControlTypeFactor() {

		if(scFactor == null) {
			 try {
				getReferenceSamplesFromDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return scFactor;
	}

	public static Collection<ExperimentalSample>getReferenceSamples(){

		if(referenceSamples == null) {
			 try {
				getReferenceSamplesFromDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return referenceSamples;
	}
	
	public static Collection<ExperimentalSample>getReferencePoolSamples(){
		
		return getReferenceSamples().stream().
				filter(s -> s.isIncloodeInPoolStats()).collect(Collectors.toList());
	}

	public static boolean isReferenceSample(ExperimentalSample sample) {

		return getReferenceSamples().stream().
				filter(s -> s.getId().equals(sample.getId())).findFirst().isPresent();
	}

	public static ExperimentalSample getReferenceSampleByName(String name) {

		return getReferenceSamples().stream().
				filter(s -> s.getName().equals(name)).findFirst().orElse(null);
	}

	public static ExperimentalSample getReferenceSampleById(String id) {

		Optional<ExperimentalSample> match = getReferenceSamples().stream().
				filter(s -> s.getId().equals(id)).findFirst();
		if(match.isPresent())
			return match.get();
		else
			return null;
	}
	
	public static ExperimentalSample getGenericRegularSample() {
		return new ExperimentalSample(REGULAR_SAMPLE, REGULAR_SAMPLE);
	}
}	























