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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.InChiKeyPortion;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class RetrieveCompoundsForCurationTask extends AbstractTask {
	
	private Range massRange;
	private InChiKeyPortion inChiKeyPortionForMatching;
	private int maxRecords;
	private Collection<CompoundIdentityCluster>compoundClustersForCurration;
	private Connection conn;

	public RetrieveCompoundsForCurationTask(Range massRange, InChiKeyPortion inChiKeyPortionForMatching,
			int maxRecords) {
		super();
		this.massRange = massRange;
		this.inChiKeyPortionForMatching = inChiKeyPortionForMatching;
		this.maxRecords = maxRecords;
		compoundClustersForCurration= new ArrayList<CompoundIdentityCluster>();
	}

	@Override
	public void run() {
		
		taskDescription = "Pulling redundant compounds for curration ...";
		total = 100;
		processed = 10;		
		conn = null;
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		Collection<String>inchiList = null;
		try {
			inchiList = getInChiList(conn);
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(inchiList.isEmpty()) {
			finishTask();
			return;
		}
		try {
			createCompoundClustersForCurration(inchiList, conn);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			attachSynonyms(conn);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			attachDbCrossref(conn);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		finishTask();
	}
	
	private void attachSynonyms(Connection conn) throws Exception {
		
	}
	
	private void attachDbCrossref(Connection conn) throws Exception {
		
	}
	
	private void createCompoundClustersForCurration(Collection<String>inchiList, Connection conn) throws Exception {
		
		String inChiField = "INCHI_KEY";
		if(inChiKeyPortionForMatching.equals(InChiKeyPortion.STRUCTURE_ONLY))
			inChiField = "INCHI_KEY_CONNECT";
		
		String query =
				"SELECT D.ACCESSION, D.SOURCE_DB, D.PRIMARY_NAME, "
				+ "D.MOL_FORMULA, D.EXACT_MASS, D.SMILES, D.INCHI_KEY "+
				"FROM COMPOUND_DATA D WHERE " + inChiField + " = ?";
	
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(String inChi : inchiList) {
			
			CompoundIdentityCluster idCluster = new CompoundIdentityCluster();
			ps.setString(1, inChi);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){

				CompoundDatabaseEnum dbSource =
						CompoundDatabaseEnum.getCompoundDatabaseByName(rs.getString("SOURCE_DB"));
				String accession = rs.getString("ACCESSION");
				String commonName = rs.getString("PRIMARY_NAME");
				String formula = rs.getString("MOL_FORMULA");
				String smiles = rs.getString("SMILES");
				double exactMass = rs.getDouble("EXACT_MASS");
				CompoundIdentity identity = new CompoundIdentity(
						dbSource, accession, commonName,
						commonName, formula, exactMass, smiles);
				identity.setInChiKey(rs.getString("INCHI_KEY"));
				CompoundIdentityInfoBundle bundle = new CompoundIdentityInfoBundle(identity);
				idCluster.addIdentity(bundle);
			}
			rs.close();
			compoundClustersForCurration.add(idCluster);
		}
		ps.close();		
	}
	
	private void finishTask() {
		
		if(conn != null) {
			try {
				ConnectionManager.releaseConnection(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private Collection<String>getInChiList(Connection conn) throws Exception {
		
		taskDescription = "Getting InChi/InChiKey list ...";
		total = 100;
		processed = 10;	
		Collection<String>inChiList = new TreeSet<String>();
		if(massRange == null)
			return inChiList;
		
		String inChiField = "INCHI_KEY";
		if(inChiKeyPortionForMatching.equals(InChiKeyPortion.STRUCTURE_ONLY))
			inChiField = "INCHI_KEY_CONNECT";
		
		String query = 
			"SELECT DISTINCT " + inChiField + ", COUNT(ACCESSION) AS REPS " +
			"FROM COMPOUND_DATA  " +
			"WHERE " + inChiField +" IS NOT NULL " +
			"AND EXACT_MASS BETWEEN ? AND ? " +
			"HAVING COUNT(ACCESSION) > 1 " +
			"GROUP BY INCHI_KEY ORDER BY REPS DESC ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setDouble(1, massRange.getMin());
		ps.setDouble(2, massRange.getMax());	
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			inChiList.add(rs.getString(inChiField));
		
		rs.close();
		ps.close();	
		return inChiList;
	}
	
	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<CompoundIdentityCluster> getCompoundClustersForCurration() {
		return compoundClustersForCurration;
	}
}
