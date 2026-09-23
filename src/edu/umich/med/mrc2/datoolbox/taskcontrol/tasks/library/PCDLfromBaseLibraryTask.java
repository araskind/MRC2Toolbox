/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.sql.SQLException;
import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class PCDLfromBaseLibraryTask extends PCDLAbstractTask {

	private static final Logger logger = LogManager.getLogger(PCDLfromBaseLibraryTask.class);
	
	private Collection<Adduct>selectedAdducts;
	
	public PCDLfromBaseLibraryTask(
			CompoundLibrary basePCDLlibrary, 
			CompoundLibrary newLlibrary, 
			File inputLibraryFile,
			Collection<Adduct> selectedAdducts) {
		super();
		this.masterPCDLlibrary = basePCDLlibrary;
		this.newLlibrary = newLlibrary;
		this.inputLibraryFile = inputLibraryFile;
		this.selectedAdducts = selectedAdducts;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if (inputLibraryFile == null || !inputLibraryFile.exists()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!parseTextLibrary()) {			
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(unmatchedFeatures.isEmpty()) {
			
			String newLibId = null;
			try {
				newLibId = MSRTLibraryUtils.createNewLibrary(newLlibrary);
			} catch (Exception e) {
				logger.error("Failed to create new library in the database", e);
			}
			if(newLibId == null) {
				errorMessage = "Failed to crete new library!";
				setStatus(TaskStatus.ERROR);
				return;
			}			
			try {
				writeFeaturesToDatabase();
			} catch (Exception e) {
				logger.error("Failed to write new library into the database", e);
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void writeFeaturesToDatabase() throws SQLException {

		taskDescription = "Writing library to database ...";
		total = newLlibrary.getFeatures().size();
		processed = 0;
		
		String libId = newLlibrary.getLibraryId();		
		Connection conn = ConnectionManager.getConnection();
		for(LibraryMsFeature lt : newLlibrary.getFeatures()){

			try {
				MSRTLibraryUtils.loadLibraryFeature(lt, libId, conn);
			} catch (Exception e) {
				logger.error(String.format("Failed to insert in the database new library feature  %s", lt.getName()), e);
			}
			if(lt.getName().toUpperCase().contains("[ISTD]")) {
				
				try {
					MSRTLibraryUtils.setTargetQcStatus(lt.getId(), true, conn);
				} catch (Exception e) {
					logger.error(String.format("Failed to set target QC status in the database  %s", lt.getName()), e);
				}
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public Task cloneTask() {

		return new PCDLfromBaseLibraryTask(
				masterPCDLlibrary, newLlibrary, inputLibraryFile, selectedAdducts);
	}
}
