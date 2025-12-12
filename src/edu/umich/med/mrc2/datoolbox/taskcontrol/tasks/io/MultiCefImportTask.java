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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureAlignmentType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.CefLibraryImportTask;

public class MultiCefImportTask extends DataWithLibraryImportAbstractTask{

	private FeatureAlignmentType alignmentType;
	private boolean fileLoadInitiated;
	private boolean libraryParsed;
	private boolean skipCompoundMatching;

	public MultiCefImportTask(
			File libraryFile, 
			DataFile[] dataFiles, 
			DataPipeline dataPipeline, 
			FeatureAlignmentType alignmentType) {

		super();
		this.libraryFile = libraryFile;
		this.dataFiles = dataFiles;
		this.dataPipeline = dataPipeline;
		this.alignmentType = alignmentType;
		this.skipCompoundMatching = false;
		
		fileLoadInitiated = false;
		unmatchedAdducts = new TreeSet<String>();
		libraryParsed = false;
		fileCounter = 0;
	}

	public MultiCefImportTask(
			File libraryFile, 
			Set<SampleDataResultObject> dataToImport, 
			DataPipeline dataPipeline,
			FeatureAlignmentType alignmentType,
			File tmpCefDirectory,
			boolean skipCompoundMatching) {
		super();
		this.libraryFile = libraryFile;
		this.dataPipeline = dataPipeline;
		this.alignmentType = alignmentType;
		this.dataFiles = dataToImport.stream().
				map(s -> s.getDataFile()).
				toArray(size -> new DataFile[size]);
		this.tmpCefDirectory = tmpCefDirectory;
		this.skipCompoundMatching = skipCompoundMatching;
		
		fileLoadInitiated = false;
		unmatchedAdducts = new TreeSet<String>();
		libraryParsed = false;
		fileCounter = 0;
	}

	@Override
	public void run() {

		if(libraryFile == null) {
			libraryParsed = true;
			initDataLoad();
			fileLoadInitiated = true;
		}
		setStatus(TaskStatus.PROCESSING);

		//	Read library
		taskDescription = "Loading library file ...";
		total = 100;
		processed = 20;
		CefLibraryImportTask lit = 
				new CefLibraryImportTask(dataPipeline, libraryFile, false, false);
		lit.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(lit);
	}

	@Override
	public Task cloneTask() {
		return new MultiCefImportTask(
				libraryFile, dataFiles, dataPipeline, alignmentType);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(CefLibraryImportTask.class) && !libraryParsed)
				finalizeLibraryImportTask((CefLibraryImportTask)e.getSource());
			
			if (e.getSource().getClass().equals(CefDataImportTask.class))
				finalizeCefImportTask((CefDataImportTask)e.getSource());	
					
			if (e.getSource().getClass().equals(CefImportFinalizationTask.class))
				setStatus(TaskStatus.FINISHED);
		}
	}
	
	private synchronized void finalizeLibraryImportTask(CefLibraryImportTask lit) {
		
		libraryParsed = libraryCorrectlyParsed(lit);
		if(libraryParsed) {

			MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(lit);
			
			//	Create array to align features
			initDataMatrixes();

			//	Spin off tasks to read individual CEF files
			if(!fileLoadInitiated) {
				initDataLoad();
				fileLoadInitiated = true;
			}
		}
		else {
			setStatus(TaskStatus.ERROR);
		}
	}

	private boolean libraryCorrectlyParsed(CefLibraryImportTask lit) {

		library = lit.getParsedLibrary();

		// Show unassigned features
		if (!lit.getUnassignedFeatures().isEmpty()) {

			ArrayList<String> flist = new ArrayList<String>();

			for (MsFeature msf : lit.getUnassignedFeatures())
				flist.add(msf.getName());

			//	TODO this needs change, handle in the calling panel
			InformationDialog id = new InformationDialog(
					"Unmatched features",
					"Not all features were matched to the library.\n"
					+ "Below is the list of unmatched features.",
					StringUtils.join(flist, "\n"),
					null);
			id.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			id.setVisible(true);
		}
		// Show unassigned adducts
		if (!lit.getUnmatchedAdducts().isEmpty()) {

			@SuppressWarnings("unused")
			InformationDialog id = new InformationDialog(
					"Unmatched adducts",
					"Not all adducts were matched to the database.\n"
					+ "Below is the list of unmatched adducts.",
					StringUtils.join(lit.getUnmatchedAdducts(), "\n"),
					null);
		}
		if (!lit.getUnassignedFeatures().isEmpty() || !lit.getUnmatchedAdducts().isEmpty()) {

			setStatus(TaskStatus.ERROR);
			return false;
		}
		return true;
	}

	public boolean isRemoveAbnormalIsoPatterns() {
		return removeAbnormalIsoPatterns;
	}

	public void setRemoveAbnormalIsoPatterns(boolean removeAbnormalIsoPatterns) {
		this.removeAbnormalIsoPatterns = removeAbnormalIsoPatterns;
	}
}























