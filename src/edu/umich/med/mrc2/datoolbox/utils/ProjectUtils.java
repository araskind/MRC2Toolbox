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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectFields;

public class ProjectUtils {
	
	public static final DateFormat dateTimeFormat = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
	
	public static void saveStorableRawDataAnalysisProject(RawDataAnalysisProject projectToSave) {
		
		Document document = new Document();
	    Element projectRoot = 
	    		new Element(ProjectFields.IDTrackerRawDataProject.name());
		projectRoot.setAttribute("version", "1.0.0.0");
		projectRoot.setAttribute(ProjectFields.Id.name(), 
				projectToSave.getId());
		projectRoot.setAttribute(ProjectFields.Name.name(), 
				projectToSave.getName());
		projectRoot.setAttribute(ProjectFields.Description.name(), 
				projectToSave.getDescription());
		projectRoot.setAttribute(ProjectFields.ProjectFile.name(), 
				projectToSave.getProjectFile().getAbsolutePath());
		projectRoot.setAttribute(ProjectFields.ProjectDir.name(), 
				projectToSave.getProjectDirectory().getAbsolutePath());	
		projectRoot.setAttribute(ProjectFields.DateCreated.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getDateCreated()));
		projectRoot.setAttribute(ProjectFields.DateModified.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getLastModified()));
		
		projectRoot.addContent(       		
				new Element(ProjectFields.UniqueCIDList.name()).setText(""));
		projectRoot.addContent(       		
				new Element(ProjectFields.UniqueMSMSLibIdList.name()).setText(""));
		projectRoot.addContent(       		
				new Element(ProjectFields.UniqueMSRTLibIdList.name()).setText(""));
		projectRoot.addContent(       		
				new Element(ProjectFields.UniqueSampleIdList.name()).setText(""));
		
		//	MS2 file list
		Element msTwoFileListElement = 
				new Element(ProjectFields.MsTwoFiles.name());		
		for(DataFile ms2dataFile : projectToSave.getMSMSDataFiles())	        	
			msTwoFileListElement.addContent(ms2dataFile.getXmlElement());
		
		projectRoot.addContent(msTwoFileListElement);
		
		//	MS1 file list
		Element msOneFileListElement = 
				new Element(ProjectFields.MsOneFiles.name());		
		for(DataFile msOnedataFile : projectToSave.getMSOneDataFiles())         	
			msOneFileListElement.addContent(msOnedataFile.getXmlElement());
		
		
		projectRoot.addContent(msOneFileListElement);        
		document.setContent(projectRoot);
		
		//	Save XML document
		File xmlFile = Paths.get(
				projectToSave.getUncompressedProjectFilesDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.PROJECT_FILE_NAME).toFile();
		try {
		    FileWriter writer = new FileWriter(xmlFile, false);
		    XMLOutputter outputter = new XMLOutputter();
		    outputter.setFormat(Format.getCompactFormat());
		    outputter.output(document, writer);
		    outputter.output(document, System.out);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		try {
			CompressionUtils.zipFile(xmlFile, projectToSave.getProjectFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Collection<String>getIdList(String idListString){
		
		if(idListString == null || idListString.isEmpty())
			return Arrays.asList(new String[0]);
		else {
			return Arrays.asList(idListString.split(","));
		}
	}
}




