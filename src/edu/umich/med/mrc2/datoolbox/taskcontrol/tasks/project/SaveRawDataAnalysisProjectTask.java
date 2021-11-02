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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class SaveRawDataAnalysisProjectTask extends AbstractTask {

	private RawDataAnalysisProject projectToSave;
	
	public SaveRawDataAnalysisProjectTask(RawDataAnalysisProject rawDataAnalyzerProject) {

		this.projectToSave = rawDataAnalyzerProject;
		total = 100;
		processed = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		long startTime = System.currentTimeMillis();
		projectToSave.setLastModified(new Date());

		backUpListeners();
		
		//	Create copy without features
		RawDataAnalysisProject copyToSave = new RawDataAnalysisProject(projectToSave);

		// Save project file
		try {
			processed = 10;
			taskDescription = "Parsing project code ... ";
			XStream projectXstream = initXstream();
			File xmlFile = Paths.get(copyToSave.getProjectDirectory().getAbsolutePath(), 
					copyToSave.getName() + ".xml").toFile();
	        RandomAccessFile raf = new RandomAccessFile(xmlFile.getAbsolutePath(), "rw");
	        FileOutputStream fout = new FileOutputStream(raf.getFD());	        
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			projectXstream.toXML(copyToSave, bout);
			bout.close();
			fout.close();
			raf.close();

			processed = 50;		
			if(xmlFile.exists()) {
				
				taskDescription = "Compressing XML file";
				processed = 60;
		        OutputStream archiveStream = new FileOutputStream(copyToSave.getProjectFile());
		        ZipArchiveOutputStream archive =
		        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
		        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
		        archive.setUseZip64(Zip64Mode.Always);
		        ZipArchiveEntry entry = new ZipArchiveEntry(copyToSave.getName());
		        archive.putArchiveEntry(entry);

		        BufferedInputStream input = new BufferedInputStream(new FileInputStream(xmlFile));
		        org.apache.commons.io.IOUtils.copy(input, archive);
		        input.close();
		        archive.closeArchiveEntry();
		        archive.finish();
		        archive.close();
		        archiveStream.close();	        
		        xmlFile.delete();
		        processed = 99;
		        
				long endTime = System.currentTimeMillis();
				System.out.println("Project save time : " 
						+ Math.round((endTime - startTime) / 1000) + " seconds");
			}
			processed = 100;
		} catch (Exception ex) {

			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		restoreListeners();
		saveDataMatrixes();
		setStatus(TaskStatus.FINISHED);
	}
	
	private void saveDataMatrixes() {
		//	TODO maybe?
	}

	private void backUpListeners() {
		//	TODO maybe?
	}

	private XStream initXstream() {

		XStream xstream = new XStream(new StaxDriver());

		xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		xstream.addPermission(NoTypePermission.NONE);
		xstream.addPermission(NullPermission.NULL);
		xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
		
//		xstream.omitField(DataAnalysisProject.class, "dataMatrixMap");
//		xstream.omitField(DataAnalysisProject.class, "corrMatrixMap");
//		xstream.omitField(MsFeature.class, "eventListeners");
//		xstream.omitField(MsFeatureSet.class, "eventListeners");
//		xstream.omitField(ExperimentDesignSubset.class, "eventListeners");
//		xstream.omitField(ExperimentDesign.class, "eventListeners");

		return xstream;
	}

	private void restoreListeners() {
		//	TODO maybe?
	}

	@Override
	public Task cloneTask() {
		return new SaveRawDataAnalysisProjectTask(projectToSave);
	}
}
