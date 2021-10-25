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

package edu.umich.med.mrc2.datoolbox.utils;

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
import org.apache.commons.io.FilenameUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;

public class ProjectUtils {

	public static boolean designValidForStats(
			DataAnalysisProject currentProject,
			DataPipeline activePipeline,
			boolean requirePooled) {

		int pooledCount = 0;
		int sampleCount = 0;

		//	TODO deal with by-batch stats
		for (DataFile df : currentProject.getDataFilesForAcquisitionMethod(
				activePipeline.getAcquisitionMethod())) {

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.masterPoolLevel))
				pooledCount++;

			if(df.getParentSample().hasLevel(ReferenceSamplesManager.sampleLevel))
				sampleCount++;
		}
		if (requirePooled && pooledCount == 0)
			return false;

		if (sampleCount > 0)
			return true;

		return false;
	}
	
	public static void saveProjectFile(RawDataAnalysisProject projectToSave) {
		
		projectToSave.setLastModified(new Date());
		try {
			XStream xstream = new XStream(new StaxDriver());

			xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
			xstream.addPermission(NoTypePermission.NONE);
			xstream.addPermission(NullPermission.NULL);
			xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
			
			xstream.omitField(DataAnalysisProject.class, "dataMatrixMap");
			xstream.omitField(DataAnalysisProject.class, "corrMatrixMap");
			xstream.omitField(MsFeature.class, "eventListeners");
			xstream.omitField(MsFeatureSet.class, "eventListeners");
			xstream.omitField(ExperimentDesignSubset.class, "eventListeners");
			xstream.omitField(ExperimentDesign.class, "eventListeners");
			
			File xmlFile = Paths.get(projectToSave.getProjectDirectory().getAbsolutePath(), 
					FilenameUtils.getBaseName(projectToSave.getProjectFile().getName()) + ".xml").toFile();
	        RandomAccessFile raf = new RandomAccessFile(xmlFile.getAbsolutePath(), "rw");
	        FileOutputStream fout = new FileOutputStream(raf.getFD());	        
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			xstream.toXML(projectToSave, bout);
			bout.close();
			fout.close();
			raf.close();
			
			if(xmlFile.exists()) {
				
		        OutputStream archiveStream = new FileOutputStream(projectToSave.getProjectFile());
		        ZipArchiveOutputStream archive =
		        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
		        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
		        archive.setUseZip64(Zip64Mode.Always);
		        ZipArchiveEntry entry = new ZipArchiveEntry(projectToSave.getName());
		        archive.putArchiveEntry(entry);

		        BufferedInputStream input = new BufferedInputStream(new FileInputStream(xmlFile));
		        org.apache.commons.io.IOUtils.copy(input, archive);
		        input.close();
		        archive.closeArchiveEntry();
		        archive.finish();
		        archive.close();
		        archiveStream.close();	        
		        xmlFile.delete();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
