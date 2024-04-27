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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.dbparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class CHEBIParserTask extends AbstractTask {
	
	private File chebiSDFfile;
	private Collection<IAtomContainer>recordSet;

	public CHEBIParserTask(File chebiSDFfile) {
		super();
		this.chebiSDFfile = chebiSDFfile;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			parseFile();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			uploadData();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);	
	}

	private void parseFile() {

		taskDescription = "Reading molecules from SDF file ...";
		total = 300000;
		processed = 0;
		recordSet = new ArrayList<IAtomContainer>();
		IteratingSDFReader reader = null;
		try {
			reader = new IteratingSDFReader(
					new FileInputStream(chebiSDFfile), 
					DefaultChemObjectBuilder.getInstance());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (reader.hasNext()) {
			
			IAtomContainer molecule = null;	
			try {
				molecule = (IAtomContainer)reader.next();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(molecule != null)
				recordSet.add(molecule);
			
			processed++;
		}
	}

	private void uploadData() throws Exception{

		taskDescription = "Uploading CHEBI data to database ...";
		total = recordSet.size();
		processed = 0;
		
		Connection conn = ConnectionManager.getConnection();
		
		//	Compound data
		String compoundDataQuery = 
				"INSERT INTO COMPOUNDDB.CHEBI_COMPOUND_DATA "
				+ "(ACCESSION, NAME, MOL_FORMULA, EXACT_MASS, "
				+ "INCHI, INCHI_KEY, SMILES, CHARGE, STAR, DEFINITION, LAST_MODIFIED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement compoundDataPs = conn.prepareStatement(compoundDataQuery);
		
		//	Synonyms
		String synonymsQuery = 
				"INSERT INTO COMPOUNDDB.CHEBI_SYNONYMS "
				+ "(ACCESSION, NAME, NTYPE) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement synonymsPs = conn.prepareStatement(synonymsQuery);

		//	Crossref
		String dbCrossrefQuery = 
				"INSERT INTO COMPOUNDDB.CHEBI_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement dbCrossrefPs = conn.prepareStatement(dbCrossrefQuery);
		
		//	Citations
		String citationsQuery = 
				"INSERT INTO COMPOUNDDB.CHEBI_CITATIONS "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement citationsPs = conn.prepareStatement(citationsQuery);
		
		for(IAtomContainer record :  recordSet) {
			
			//	Compound data
			try {
				insertCompoundData(record, conn, compoundDataPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//	Synonyms		
			try {
				insertSynonyms(record, conn, synonymsPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
			//	Crossref				
			try {
				insertDatabaseCrossReferences(record, conn, dbCrossrefPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//	Citations				
			try {
				insertCitations(record, conn, citationsPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processed++;
		}		
		compoundDataPs.close();
		synonymsPs.close();
		dbCrossrefPs.close();
		citationsPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	private void insertCompoundData(
			IAtomContainer record, 
			Connection conn, 
			PreparedStatement compoundDataPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertSynonyms(
			IAtomContainer record, 
			Connection conn, 
			PreparedStatement synonymsPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertDatabaseCrossReferences(
			IAtomContainer record, 
			Connection conn, 
			PreparedStatement dbCrossrefPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertCitations(
			IAtomContainer record, 
			Connection conn, 
			PreparedStatement citationsPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task cloneTask() {
		return new CHEBIParserTask(chebiSDFfile);
	}
}
















