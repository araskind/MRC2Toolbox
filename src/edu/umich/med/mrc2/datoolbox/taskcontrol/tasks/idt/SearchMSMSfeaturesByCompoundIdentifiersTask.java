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
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdFilter;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdFilterType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class SearchMSMSfeaturesByCompoundIdentifiersTask extends IDTMSMSFeatureDataPullTask {

	private CompoundIdFilter compoundIdFilter;
	private Polarity polarity;
	
	public SearchMSMSfeaturesByCompoundIdentifiersTask(
			CompoundIdFilter compoundIdFilter, 
			Polarity polarity) {
		super();
		this.compoundIdFilter = compoundIdFilter;
		this.polarity = polarity;
	}
	
	@Override
	public void run() {
		taskDescription = "Looking up features in IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			selectMsMsFeaturesUsingCompoundIdentifiers();
			getCachedFeatures();
			getMsMsFeatures();
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
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void selectMsMsFeaturesUsingCompoundIdentifiers() throws Exception {
		
		String searchTerm = null;
		CompoundIdFilterType filterType = compoundIdFilter.getFilterType();
		if(filterType.equals(CompoundIdFilterType.COMPOUND_DATABASE_ID))
			searchTerm = "ACCESSION";
		
		if(filterType.equals(CompoundIdFilterType.COMPOUND_NAME))
			searchTerm = "UPPER(PRIMARY_NAME)";
		
		if(filterType.equals(CompoundIdFilterType.SMILES))
			searchTerm = "SMILES";
		
		if(filterType.equals(CompoundIdFilterType.INCHI_KEY))
			searchTerm = "INCHI_KEY";
		
		if(filterType.equals(CompoundIdFilterType.INCHI_KEY2D))
			searchTerm = "INCHI_KEY_CONNECT";

		if(searchTerm == null)
			return;
		
		featureIds = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DISTINCT F.PARENT_FEATURE_ID  " +
				"FROM MSMS_FEATURE F, MSMS_FEATURE_LIBRARY_MATCH M, " +
				"REF_MSMS_LIBRARY_COMPONENT C " +
				"WHERE F.MSMS_FEATURE_ID = M.MSMS_FEATURE_ID "
				+ "AND M.MRC2_LIB_ID = C.MRC2_LIB_ID " +
				"AND C.ACCESSION IN( " +
				"SELECT DISTINCT ACCESSION  " +
				"FROM COMPOUND_DATA  " +
				"WHERE " + searchTerm + " = ?) ";
		if(polarity != null)
			query += "AND C.POLARITY = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);
		if(polarity != null)
			ps.setString(2, polarity.getCode());
				
		for(String lookupString : compoundIdFilter.getFilterComponents()) {
			
			if(filterType.equals(CompoundIdFilterType.COMPOUND_NAME))
				ps.setString(1, lookupString.toUpperCase());
			else
				ps.setString(1, lookupString);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				featureIds.add(rs.getString(1));
			
			rs.close();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new SearchMSMSfeaturesByCompoundIdentifiersTask(
				compoundIdFilter, polarity);
	}
}
