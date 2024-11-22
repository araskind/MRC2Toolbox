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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.load.npa.NPALiteratureReference;
import edu.umich.med.mrc2.datoolbox.dbparse.load.npa.NPAParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.npa.NPARecord;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.IteratingSDFReaderFixed;

public class NPAParseAndUploadTask extends AbstractTask {

	private File sdfInputFile;
	private Collection<NPARecord>records;
 
	public NPAParseAndUploadTask(File sdfInputFile) {
		super();
		this.sdfInputFile = sdfInputFile;
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		try {
			parseFileToRecords();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			uploadRecordsToDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);	
	}

	private void parseFileToRecords() {

		taskDescription = 
				"Parsing Natural Products Atlas (NPA) SDF file " + 
						sdfInputFile.getName() + " ...";		
		total = 200000;
		processed = 0;
		IteratingSDFReaderFixed reader = null;
		records = new ArrayList<NPARecord>();
		try {
			reader = new IteratingSDFReaderFixed(
					new FileInputStream(sdfInputFile), 
					DefaultChemObjectBuilder.getInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		while (reader.hasNext()) {
			IAtomContainer molecule = (IAtomContainer)reader.next();
			NPARecord record = null;
			try {
				record = NPAParser.parseMoleculeToLipidNPARecord(molecule);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
			if(record != null)
				records.add(record);
				
			processed++;
		}
	}

	private void uploadRecordsToDatabase() throws Exception{

		taskDescription = "Uploading records ...";		
		total = records.size();
		processed = 0;		
		Connection conn = ConnectionManager.getConnection();
		
		String dataQuery =
				"INSERT INTO COMPOUNDDB.NPA_COMPOUND_DATA " +
				"(ACCESSION, NAME, FORMULA, EXACT_MASS, SMILES, INCHI, "
				+ "INCHI_KEY, ORIGIN_TYPE, ORIGIN_GENUS, ORIGIN_SPECIES) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		String litRefQuery = 
				"INSERT INTO COMPOUNDDB.NPA_LIT_REFERENCES "
				+ "(ACCESSION, TITLE, AUTHOR_LIST, JOURNAL_TITLE, "
				+ "ISSUE, VOLUME, YEAR, PAGES, DOI, PMID, TYPE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement litRefPs = conn.prepareStatement(litRefQuery);
		
		String mibigQuery = "INSERT INTO COMPOUNDDB.NPA_MIBIG_CROSSREF "
				+ "(ACCESSION, MIBIG_ID) VALUES (?, ?)";
		PreparedStatement mibigPs = conn.prepareStatement(mibigQuery);
		
		String gnpsQuery = "INSERT INTO COMPOUNDDB.NPA_GNPS_MSMS_CROSSREF "
				+ "(ACCESSION, GNPS_MSMS_ID) VALUES (?, ?)";
		PreparedStatement gnpsPs = conn.prepareStatement(gnpsQuery);

		for(NPARecord record : records) {

			//	Insert compound data	
			CompoundIdentity cid = record.getCompoundIdentity();
			ps.setString(1, record.getId());
			ps.setString(2, record.getName());
			ps.setString(3, cid.getFormula());
			ps.setDouble(4, cid.getExactMass());
			ps.setString(5, cid.getSmiles());
			ps.setString(6, cid.getInChi());
			ps.setString(7, cid.getInChiKey());
			
			String orType = record.getOriginType();
			if(orType != null)
				ps.setString(8, orType);
			else
				ps.setNull(8, java.sql.Types.NULL);
			
			String genus = record.getGenus();
			if(genus != null)
				ps.setString(9, genus);
			else
				ps.setNull(9, java.sql.Types.NULL);
			
			String species = record.getSpecies();
			if(species != null)
				ps.setString(10, species);
			else
				ps.setNull(10, java.sql.Types.NULL);
			
			ps.executeUpdate();
		
			//	Insert literature reference
			NPALiteratureReference ref = record.getReference();
			if(ref != null) {
				litRefPs.setString(1, record.getId());
				litRefPs.setString(2, ref.getTitle());
				litRefPs.setString(3, ref.getAuthorList());
				
				if(ref.getJournalTitle() != null)
					litRefPs.setString(4, ref.getJournalTitle());
				else
					litRefPs.setNull(4, java.sql.Types.NULL);
				
				if(ref.getIssue() != null)
					litRefPs.setString(5, ref.getIssue());
				else
					litRefPs.setNull(5, java.sql.Types.NULL);
				
				if(ref.getVolume() != null)
					litRefPs.setString(6, ref.getVolume());
				else
					litRefPs.setNull(6, java.sql.Types.NULL);
				
				if(ref.getYear() != null)
					litRefPs.setString(7, ref.getYear());
				else
					litRefPs.setNull(7, java.sql.Types.NULL);
				
				if(ref.getPages() != null)
					litRefPs.setString(8, ref.getPages());
				else
					litRefPs.setNull(8, java.sql.Types.NULL);
				
				if(ref.getDoi() != null)
					litRefPs.setString(9, ref.getDoi());
				else
					litRefPs.setNull(9, java.sql.Types.NULL);
				
				if(ref.getPmid() != null)
					litRefPs.setString(10, ref.getPmid());
				else
					litRefPs.setNull(10, java.sql.Types.NULL);
				
				if(ref.getType() != null)
					litRefPs.setString(11, ref.getType());
				else
					litRefPs.setNull(11, java.sql.Types.NULL);

				litRefPs.executeUpdate();
			}
			//	Insert mibig references
			if(!record.getMibigIds().isEmpty()) {
				
				mibigPs.setString(1, record.getId());
				for(String mibigId : record.getMibigIds()) {
					mibigPs.setString(2, mibigId);
					mibigPs.addBatch();
				}
				mibigPs.executeBatch();
			}
			if(!record.getGnpsIds().isEmpty()) {
				
				gnpsPs.setString(1, record.getId());
				for(String gnpsId : record.getGnpsIds()) {
					gnpsPs.setString(2, gnpsId);
					gnpsPs.addBatch();
				}
				gnpsPs.executeBatch();
			}
			processed++;
		}
		ps.close();
		litRefPs.close();
		mibigPs.close();
		gnpsPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new NPAParseAndUploadTask(sdfInputFile);
	}
}
