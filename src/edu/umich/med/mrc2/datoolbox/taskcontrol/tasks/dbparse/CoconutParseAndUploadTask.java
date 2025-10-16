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
import edu.umich.med.mrc2.datoolbox.dbparse.load.coconut.CoconutParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.coconut.CoconutParser.CoconutRecordFields;
import edu.umich.med.mrc2.datoolbox.dbparse.load.coconut.CoconutRecord;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.IteratingSDFReaderFixed;

public class CoconutParseAndUploadTask extends AbstractTask {

	private File sdfInputFile;
	private Collection<CoconutRecord>records;

	public CoconutParseAndUploadTask(File sdfInputFile) {
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
				"Parsing Coconut SDF file " + 
						sdfInputFile.getName() + " ...";		
		total = 500000;
		processed = 0;
		IteratingSDFReaderFixed reader = null;
		records = new ArrayList<CoconutRecord>();
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
			CoconutRecord record = null;
			try {
				record = CoconutParser.parseMoleculeToCoconutRecord(molecule);
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
				"INSERT INTO COMPOUNDDB.COCONUT_COMPOUND_DATA " +
				"(ACCESSION, NAME, MOLECULAR_FORMULA, AVG_MOLECULAR_WEIGHT,  " +
				"SMILES, SUGAR_FREE_SMILES, INCHI, INCHI_KEY, SYNONYMS,  " +
				"TOTAL_ATOM_NUMBER, NUMBER_OF_CARBONS, NUMBER_OF_NITROGENS,  " +
				"NUMBER_OF_OXYGENS, NUMBER_OF_RINGS, BOND_COUNT, MURKO_FRAMEWORK,  " +
				"TEXTTAXA, CITATIONDOI, FOUND_IN_DATABASES, NPL_SCORE,  " +
				"ALOGP, APOL, TOPOPSA) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		for(CoconutRecord record : records) {

			//	Insert compound data	
			CompoundIdentity cid = record.getCompoundIdentity();
			ps.setString(1, record.getId());
			ps.setString(2, record.getName());		
			ps.setString(3, cid.getFormula());
			ps.setDouble(4, cid.getExactMass());
			ps.setString(5, cid.getSmiles());
			String sugar_free_smiles = 
					(String)record.getProperty(CoconutRecordFields.sugar_free_smiles);
			if(sugar_free_smiles != null)
				ps.setString(6, sugar_free_smiles);
			else
				ps.setNull(6, java.sql.Types.NULL);
			
			ps.setString(7, cid.getInChi());
			ps.setString(8, cid.getInChiKey());
			
			Object synonyms = 
					record.getProperty(CoconutRecordFields.synonyms);
			if(synonyms != null)
				ps.setString(9, (String)synonyms);
			else
				ps.setNull(9, java.sql.Types.NULL);
			
			Object total_atom_number = 
					record.getProperty(CoconutRecordFields.total_atom_number);
			if(total_atom_number != null)
				ps.setInt(10, (Integer)total_atom_number);
			else
				ps.setNull(10, java.sql.Types.NULL);
			
			Object number_of_carbons = 
					record.getProperty(CoconutRecordFields.number_of_carbons);
			if(number_of_carbons != null)
				ps.setInt(11, (Integer)number_of_carbons);
			else
				ps.setNull(11, java.sql.Types.NULL);
			
			Object number_of_nitrogens = 
					record.getProperty(CoconutRecordFields.number_of_nitrogens);
			if(number_of_nitrogens != null)
				ps.setInt(12, (Integer)number_of_nitrogens);
			else
				ps.setNull(12, java.sql.Types.NULL);
			
			Object number_of_oxygens = 
					record.getProperty(CoconutRecordFields.number_of_oxygens);
			if(number_of_oxygens != null)
				ps.setInt(13, (Integer)number_of_oxygens);
			else
				ps.setNull(13, java.sql.Types.NULL);
			
			Object number_of_rings = 
					record.getProperty(CoconutRecordFields.number_of_rings);
			if(number_of_rings != null)
				ps.setInt(14, (Integer)number_of_rings);
			else
				ps.setNull(14, java.sql.Types.NULL);
			
			Object bond_count = 
					record.getProperty(CoconutRecordFields.bond_count);
			if(bond_count != null)
				ps.setInt(15, (Integer)bond_count);
			else
				ps.setNull(15, java.sql.Types.NULL);
			
			Object murko_framework = 
					record.getProperty(CoconutRecordFields.murko_framework);
			if(murko_framework != null)
				ps.setString(16, (String)murko_framework);
			else
				ps.setNull(16, java.sql.Types.NULL);
			
			ps.setNull(17, java.sql.Types.NULL);
//			Object textTaxa = 
//					record.getProperty(CoconutRecordFields.textTaxa);
//			if(textTaxa != null)
//				ps.setString(17, (String)textTaxa);
//			else
//				ps.setNull(17, java.sql.Types.NULL);
			
			Object citationDOI = 
					record.getProperty(CoconutRecordFields.citationDOI);
			if(citationDOI != null)
				ps.setString(18, (String)citationDOI);
			else
				ps.setNull(18, java.sql.Types.NULL);
			
			Object found_in_databases = 
					record.getProperty(CoconutRecordFields.found_in_databases);
			if(found_in_databases != null)
				ps.setString(19, (String)found_in_databases);
			else
				ps.setNull(19, java.sql.Types.NULL);
			
			Object NPL_score = 
					record.getProperty(CoconutRecordFields.NPL_score);
			if(NPL_score != null && !((Double)NPL_score).equals(Double.NaN))
				ps.setDouble(20, (Double)NPL_score);
			else
				ps.setNull(20, java.sql.Types.NULL);
			
			Object alogp = 
					record.getProperty(CoconutRecordFields.alogp);
			if(alogp != null && !((Double)alogp).equals(Double.NaN))
				ps.setDouble(21, (Double)alogp);
			else
				ps.setNull(21, java.sql.Types.NULL);
			
			Object apol = 
					record.getProperty(CoconutRecordFields.apol);
			if(apol != null && !((Double)apol).equals(Double.NaN))
				ps.setDouble(22, (Double)apol);
			else
				ps.setNull(22, java.sql.Types.NULL);
			
			Object topoPSA = 
					record.getProperty(CoconutRecordFields.topoPSA);
			if(topoPSA != null && !((Double)topoPSA).equals(Double.NaN))
				ps.setDouble(23, (Double)topoPSA);
			else
				ps.setNull(23, java.sql.Types.NULL);
			
			ps.executeUpdate();

			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new CoconutParseAndUploadTask(sdfInputFile);
	}
}











