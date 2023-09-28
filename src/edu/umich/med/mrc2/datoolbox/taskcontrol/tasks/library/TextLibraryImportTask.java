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

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class TextLibraryImportTask extends AbstractTask {

	private File inputLibraryFile;
	private CompoundLibrary library;
	private Collection<CompoundIdentity>unmatchedIdentities;
		
	public TextLibraryImportTask(File inputLibraryFile, CompoundLibrary library) {
		super();
		this.inputLibraryFile = inputLibraryFile;
		this.library = library;
		unmatchedIdentities = new ArrayList<CompoundIdentity>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if (inputLibraryFile == null || !inputLibraryFile.exists()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		try {
			parseTextLibrary();
		}
		catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(unmatchedIdentities.isEmpty()) {
			try {
				writeFeaturesToDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void parseTextLibrary() {
		
//		CompoundIdentity identity  = new CompoundIdentity(name, formula);
//		identity.addDbId(CompoundDatabaseEnum.CAS, casId);
//
//		double neutralMass = 0.0d;
//		IMolecularFormula mf = null;
//		try {
//			mf = MolecularFormulaManipulator.getMolecularFormula(formula, DefaultChemObjectBuilder.getInstance());;
//		} catch (Exception e) {
//			//e.printStackTrace();
//		}
//		if(mf != null){
//
//			neutralMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
//			LibraryMsFeature newTarget = new LibraryMsFeature(name, spectrumMap.get(cid), rt);
//			newTarget.setDateCreated(library.getDateCreated());
//			newTarget.setLastModified(lastModified);
//			newTarget.setNeutralMass(neutralMass);
//
//			//	TODO handle MSMS confidence level
//			MsFeatureIdentity mid = new MsFeatureIdentity(identity, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
//			newTarget.setPrimaryIdentity(mid);
//			library.addFeature(newTarget);
//		}
	}
	
	private void writeFeaturesToDatabase() throws Exception {

		taskDescription = "Writing library to database ...";

		total = library.getFeatures().size();
		processed = 0;
		String libId = library.getLibraryId();
		
		Connection conn = ConnectionManager.getConnection();
		for(MsFeature lt : library.getFeatures()){

			try {
				MSRTLibraryUtils.loadLibraryFeature(
						(LibraryMsFeature) lt, libId, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	public File getInputLibraryFile() {
		return inputLibraryFile;
	}

	public CompoundLibrary getLibrary() {
		return library;
	}

	@Override
	public Task cloneTask() {
		return new TextLibraryImportTask(inputLibraryFile, library);
	}
}
