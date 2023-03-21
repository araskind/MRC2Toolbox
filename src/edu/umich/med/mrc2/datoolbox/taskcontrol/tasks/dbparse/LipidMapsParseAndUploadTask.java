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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassification;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassificationObject;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsRecord;
import edu.umich.med.mrc2.datoolbox.misctest.IteratingSDFReaderFixed;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LipidMapsParseAndUploadTask extends AbstractTask {

	private File sdfInputFile;
	private Collection<LipidMapsRecord>records;
	private Map<Integer,LipidMapsClassificationObject>lmTaxonomyMap;

	public LipidMapsParseAndUploadTask(File sdfInputFile) {
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
		}
		try {
			insertRedundantData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			uploadRecordsToDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);	
	}

	private void parseFileToRecords() {

		taskDescription = 
				"Parsing LipidMaps SDF file " + 
						sdfInputFile.getName() + " ...";		
		total = 200000;
		processed = 0;
		IteratingSDFReaderFixed reader = null;
		records = new ArrayList<LipidMapsRecord>();
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
			LipidMapsRecord record = null;
			try {
				record = LipidMapsParser.parseMoleculeToLipidMapsRecord(molecule);
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

	private void insertRedundantData() throws Exception{

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Uploading redundant data ...";		

		lmTaxonomyMap = 
				new HashMap<Integer,LipidMapsClassificationObject>();
		records.stream().flatMap(r -> r.getLmTaxonomy().stream()).
			forEach(t -> lmTaxonomyMap.put(t.hashCode(), t));
		total = lmTaxonomyMap.size();
		processed = 0;
		
		String query = 
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_CLASSES "
				+ "(CLASS_LEVEL, CLASS_ID, NAME) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		for(LipidMapsClassificationObject o : lmTaxonomyMap.values()) {
			
			ps.setString(1, o.getGroup().name());
			ps.setString(2, o.getCode());
			ps.setString(3, o.getName());
			ps.executeUpdate();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	private void uploadRecordsToDatabase() throws Exception{

		taskDescription = "Uploading records ...";		
		total = records.size();
		processed = 0;		
		Connection conn = ConnectionManager.getConnection();
		
		String dataQuery =
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_COMPOUND_DATA " +
				"(LMID, SYSTEMATIC_NAME, COMMON_NAME, MOLECULAR_FORMULA, EXACT_MASS, " +
				"INCHI, INCHI_KEY, SMILES, CATEGORY, MAIN_CLASS, "
				+ "SUB_CLASS, CLASS_LEVEL4, ABBREVIATION) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		String synonymQuery = 
				"INSERT INTO COMPOUNDDB.LIPIDMAPS_SYNONYMS "
				+ "(LMID, NAME, NTYPE) VALUES (?, ?, ?)";
		PreparedStatement synonymPs = conn.prepareStatement(synonymQuery);
		
		String crossrefQuery = "INSERT INTO COMPOUNDDB.LIPIDMAPS_CROSSREF "
				+ "(LMID, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		PreparedStatement crossrefPs = conn.prepareStatement(crossrefQuery);

		for(LipidMapsRecord record : records) {

			//	Insert compound data	
			CompoundIdentity cid = record.getCompoundIdentity();
			ps.setString(1, record.getLmid());
			ps.setString(2, record.getCommonName());
			if(record.getSysName() != null)
				ps.setString(3, record.getSysName());
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			ps.setString(4, cid.getFormula());
			ps.setDouble(5, cid.getExactMass());
			ps.setString(6, cid.getInChi());
			ps.setString(7, cid.getInChiKey());
			ps.setString(8, cid.getSmiles());
			
			String category = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.CATEGORY);
			if(category != null)
				ps.setString(9, category);
			else
				ps.setNull(9, java.sql.Types.NULL);
			
			String mainClass = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.MAIN_CLASS);
			if(mainClass != null)
				ps.setString(10, mainClass);
			else
				ps.setNull(10, java.sql.Types.NULL);
			
			String subClass = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.SUB_CLASS);
			if(subClass != null)
				ps.setString(11, subClass);
			else
				ps.setNull(11, java.sql.Types.NULL);
			
			String classLevel4 = 
					record.getTaxonomyCodeForLevel(LipidMapsClassification.CLASS_LEVEL4);
			if(classLevel4 != null)
				ps.setString(12, classLevel4);
			else
				ps.setNull(12, java.sql.Types.NULL);
			
			if(record.getAbbreviation() != null)
				ps.setString(13, record.getAbbreviation());
			else
				ps.setNull(13, java.sql.Types.NULL);
			
			ps.executeUpdate();
		
			//	Insert primary and systematic name(s)
			synonymPs.setString(1, record.getLmid());
			
			String commonName = record.getCommonName();
			String sysName = record.getSysName();
			if(commonName == null || commonName.isEmpty())
				commonName = sysName;

			if(commonName != null && !commonName.isEmpty()) {
				synonymPs.setString(2, commonName);
				synonymPs.setString(3, "PRI");
				synonymPs.addBatch();
			}
			if(sysName != null && !sysName.isEmpty()) {
				synonymPs.setString(2, sysName);
				synonymPs.setString(3, "SYS");
				synonymPs.addBatch();
			}
			//	Insert synonyms
			if(!record.getSynonyms().isEmpty()) {

				for(String syn : record.getSynonyms()) {

					synonymPs.setString(2, syn);
					synonymPs.setString(3, "SYN");
					synonymPs.addBatch();
				}
			}
			synonymPs.executeBatch();
			
			//	Insert database references
			crossrefPs.setString(1, record.getLmid());
			for (Entry<CompoundDatabaseEnum, String> entry : cid.getDbIdMap().entrySet()) {

				if(!entry.getValue().isEmpty()) {

					crossrefPs.setString(2, entry.getKey().name());
					crossrefPs.setString(3, entry.getValue());
					crossrefPs.addBatch();
				}
			}
			crossrefPs.executeBatch();
			processed++;
		}
		ps.close();
		synonymPs.close();
		crossrefPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new LipidMapsParseAndUploadTask(sdfInputFile);
	}
}
