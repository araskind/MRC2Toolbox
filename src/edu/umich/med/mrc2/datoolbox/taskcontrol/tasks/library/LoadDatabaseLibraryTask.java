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

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LoadDatabaseLibraryTask extends AbstractTask {

	private CompoundLibrary library;
	private String libraryId;

	public LoadDatabaseLibraryTask(String libraryId) {

		this.libraryId = libraryId;
	}

	@Override
	public Task cloneTask() {

		return new LoadDatabaseLibraryTask(library.getLibraryId());
	}

	public CompoundLibrary getLibrary() {
		return library;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		library = null;
		try {
			library = MSRTLibraryUtils.getLibrary(libraryId);
		} catch (Exception e1) {

			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		total = 100;
		processed = 20;

		if (library != null) {

			try {
				Connection conn = ConnectionManager.getConnection();
				Collection<LibraryMsFeatureDbBundle>bundles =
						MSRTLibraryUtils.createFeatureBundlesForLibrary(library.getLibraryId(), conn);

				total = bundles.size();
				processed = 0;

				for(LibraryMsFeatureDbBundle fBundle : bundles) {

					if(fBundle.getConmpoundDatabaseAccession() != null) {

						LibraryMsFeature newTarget = fBundle.getFeature();
						MSRTLibraryUtils.attachIdentity(
								newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);

						if(newTarget.getPrimaryIdentity() != null) {

							newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
							MSRTLibraryUtils.attachMassSpectrum(newTarget, conn);
							MSRTLibraryUtils.attachTandemMassSpectrum(newTarget, conn);
							MSRTLibraryUtils.attachAnnotations(newTarget, conn);
							library.addFeature(newTarget);
						}
					}
					processed++;
				}
				ConnectionManager.releaseConnection(conn);
				setStatus(TaskStatus.FINISHED);
			}
			catch (Exception e) {

				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
}


























