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

import java.sql.Connection;
import java.util.Collection;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DuplicateLibraryTask extends AbstractTask {

	private CompoundLibrary sourceLibrary;
	private String newLibraryName;
	private boolean clearRt;
	private boolean clearSpectra;
	private boolean clearAnnotations;


	public DuplicateLibraryTask(
			CompoundLibrary sourceLibrary,
			String newLibraryName,
			boolean clearRt,
			boolean clearAdducts,
			boolean clearAnnotations) {

		super();
		this.sourceLibrary = sourceLibrary;
		this.newLibraryName = newLibraryName;
		this.clearRt = clearRt;
		this.clearSpectra = clearAdducts;
		this.clearAnnotations = clearAnnotations;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		String libId = createNewLibrary();

		if(libId != null)
		try {
			duplicateLibrary(libId);
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	// Duplicate the library record
	private String createNewLibrary() {

		String libraryDescription = sourceLibrary.getLibraryDescription() + "\n Copy";

		if(clearRt)
			libraryDescription += "\nRT cleared";

		if(clearSpectra)
			libraryDescription += "\nSpectra cleared";

		if(clearAnnotations)
			libraryDescription += "\nAnnotations cleared";

		String libId = null;
		try {
			libId = MSRTLibraryUtils.createNewLibrary(newLibraryName, libraryDescription);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return libId;
	}

	private void duplicateLibrary(String libId) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Collection<LibraryMsFeatureDbBundle>bundles =
				MSRTLibraryUtils.createFeatureBundlesForLibrary(sourceLibrary.getLibraryId(), conn);

		total = bundles.size();
		processed = 0;
		for(LibraryMsFeatureDbBundle fBundle : bundles) {

			LibraryMsFeature newTarget = fBundle.getFeature();
			if(fBundle.getConmpoundDatabaseAccession() != null) {
				MSRTLibraryUtils.attachIdentity(
						newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);

				if(newTarget.getPrimaryIdentity() != null)
					newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
			}
			if(clearRt) {
				newTarget.setRetentionTime(0.0d);
				newTarget.setRtRange(new Range(0.0d));
			}
			if(!clearSpectra) {

				MSRTLibraryUtils.attachMassSpectrum(newTarget, conn);
				MSRTLibraryUtils.attachTandemMassSpectrum(newTarget, conn);
			}
			if(!clearAnnotations) {

				MSRTLibraryUtils.attachAnnotations(newTarget, conn);

				//	Generate new unique ID for annotations
//				for(ObjectAnnotation annotation : newTarget.getAnnotations())
//					annotation.setUniqueId(DataPrefix.ANNOTATION.getName() + UUID.randomUUID().toString());
			}
			//	Generate new unique ID
			newTarget.setId(DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString());

			//	Generate new unique ID for MSMS data
			if(newTarget.getSpectrum() != null) {

				for(TandemMassSpectrum msms : newTarget.getSpectrum().getTandemSpectra())
					msms.setId(DataPrefix.MSMS_SPECTRUM.getName() + UUID.randomUUID().toString());
			}
			MSRTLibraryUtils.loadLibraryFeature(newTarget, libId);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {

		return new DuplicateLibraryTask(
				sourceLibrary,
				newLibraryName,
				clearRt,
				clearSpectra,
				clearAnnotations);
	}
}





































