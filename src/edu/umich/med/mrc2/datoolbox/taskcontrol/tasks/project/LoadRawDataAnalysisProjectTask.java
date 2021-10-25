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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;

public class LoadRawDataAnalysisProjectTask extends AbstractTask implements TaskListener{

	private RawDataAnalysisProject newProject;
	private File projectFile;
	private ArrayList<String>errors;
	private int ticCount;

	public LoadRawDataAnalysisProjectTask(File projectFile) {

		this.projectFile = projectFile;
		errors = new ArrayList<String>();
		ticCount = 0;
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Loading project " + 
				FilenameUtils.getBaseName(projectFile.getName());

		total = 100;
		processed = 0;
		newProject = null;
		try {
			loadProjectFile();
		} catch (Throwable e) {

			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		if (newProject != null) {

			newProject.updateProjectLocation(projectFile);
			loadRawData();			
			if(getStatus().equals(TaskStatus.PROCESSING)) {
				
				//	Already have TICs extracted?
				//	initTicExtraction();
				
				//	TODO any additional data load?
			}
			setStatus(TaskStatus.FINISHED);
		}
		processed = 100;
		setStatus(TaskStatus.FINISHED);
	}
	
	private void loadRawData() {
		
		taskDescription = "Loading raw data files for project ...";
		total = newProject.getRawDataFiles().size();
		processed = 0;	
		
		for(DataFile df : newProject.getRawDataFiles()) {
			
			File rdf = new File(df.getFullPath());
			if(!rdf.exists())
				errors.add("Data file " + df.getFullPath() + "not found");
		}
		if(!errors.isEmpty()) {
			this.setStatus(TaskStatus.ERROR);
			return;
		}
		for(DataFile df : newProject.getRawDataFiles()) {
			
			RawDataManager.getRawData(df);
			processed++;
		}
	}
		
	private void initTicExtraction() {
		
		taskDescription = "Extracting TICs ...";
		total = newProject.getRawDataFiles().size();
		processed = 1;	
		Collection<Double> mzList = new ArrayList<Double>();
		
		for(DataFile df : newProject.getRawDataFiles()) {
			
			ChromatogramExtractionTask xicTask = new ChromatogramExtractionTask(
					Collections.singleton(df), 
					ChromatogramPlotMode.TIC, 
					null, 
					1, 
					mzList,
					false, 
					Double.NaN, 
					null, 
					null,
					true,
					SavitzkyGolayWidth.FIVE);
			xicTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(xicTask);
		}
	}

	private void loadProjectFile() throws ZipException, IOException {

		ZipFile zipFile;
		ZipEntry zippedProject;
		InputStream input;
		BufferedReader br;

		XStream projectImport = new XStream(new StaxDriver());
		/**
		 * From
		 * https://stackoverflow.com/questions/44698296/security-framework-of-xstream-not-initialized-xstream-is-probably-vulnerable
		 * */
		//clear out existing permissions and set own ones
		projectImport.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		projectImport.addPermission(NoTypePermission.NONE);
		projectImport.addPermission(NullPermission.NULL);
		projectImport.addPermission(PrimitiveTypePermission.PRIMITIVES);

		//	TODO limit to actually used stuff
		projectImport.allowTypesByRegExp(new String[] { ".*" });
		projectImport.ignoreUnknownElements();
        
		zipFile = new ZipFile(projectFile);

		if (zipFile.entries().hasMoreElements()) {

			zippedProject = zipFile.entries().nextElement();
			input = zipFile.getInputStream(zippedProject);
			br = new BufferedReader(new InputStreamReader(input, "UTF-8"));

			newProject = (RawDataAnalysisProject) projectImport.fromXML(br);

			br.close();
			input.close();
			zipFile.close();
		}
		processed = 50;
	}

	@Override
	public Task cloneTask() {
		return new LoadRawDataAnalysisProjectTask(projectFile);
	}
	
	public RawDataAnalysisProject getNewProject() {
		return newProject;
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class)) {
				
				ticCount++;
				if(ticCount == newProject.getRawDataFiles().size())
					setStatus(TaskStatus.FINISHED);
			}
		}		
	}

	public ArrayList<String> getErrors() {
		return errors;
	}
}

















