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
import org.ujmp.core.Matrix;

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
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentFields;

public class ExperimentUtils {
	
	public static final DateFormat dateTimeFormat = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static boolean designValidForStats(
			DataAnalysisProject currentExperiment,
			DataPipeline activePipeline,
			boolean requirePooled) {

		int pooledCount = 0;
		int sampleCount = 0;

		//	TODO deal with by-batch stats
		for (DataFile df : currentExperiment.getDataFilesForAcquisitionMethod(
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
	
	public static void saveExperimentFile(
			RawDataAnalysisExperiment experimentToSave) {
		
		experimentToSave.setLastModified(new Date());
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
			
			File xmlFile = Paths.get(experimentToSave.getExperimentDirectory().getAbsolutePath(), 
					FilenameUtils.getBaseName(experimentToSave.getExperimentFile().getName()) + ".xml").toFile();
	        RandomAccessFile raf = new RandomAccessFile(xmlFile.getAbsolutePath(), "rw");
	        FileOutputStream fout = new FileOutputStream(raf.getFD());	        
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			xstream.toXML(experimentToSave, bout);
			bout.close();
			fout.close();
			raf.close();
			
			if(xmlFile.exists()) {
				
		        OutputStream archiveStream = new FileOutputStream(experimentToSave.getExperimentFile());
		        ZipArchiveOutputStream archive =
		        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
		        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
		        archive.setUseZip64(Zip64Mode.Always);
		        ZipArchiveEntry entry = new ZipArchiveEntry(experimentToSave.getName());
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
	
	public static void saveStorableRawDataAnalysisExperiment(
			RawDataAnalysisExperiment experimentToSave) {
		
		Document document = new Document();
	    Element experimentRoot = 
	    		new Element(ExperimentFields.IDTrackerRawDataProject.name());
		experimentRoot.setAttribute("version", "1.0.0.0");
		experimentRoot.setAttribute(ExperimentFields.Id.name(), 
				experimentToSave.getId());
		experimentRoot.setAttribute(ExperimentFields.Name.name(), 
				experimentToSave.getName());
		experimentRoot.setAttribute(ExperimentFields.Description.name(), 
				experimentToSave.getDescription());
		experimentRoot.setAttribute(ExperimentFields.ProjectFile.name(), 
				experimentToSave.getExperimentFile().getAbsolutePath());
		experimentRoot.setAttribute(ExperimentFields.ProjectDir.name(), 
				experimentToSave.getExperimentDirectory().getAbsolutePath());	
		experimentRoot.setAttribute(ExperimentFields.DateCreated.name(), 
				ExperimentUtils.dateTimeFormat.format(experimentToSave.getDateCreated()));
		experimentRoot.setAttribute(ExperimentFields.DateModified.name(), 
				ExperimentUtils.dateTimeFormat.format(experimentToSave.getLastModified()));
		
		experimentRoot.addContent(       		
				new Element(ExperimentFields.UniqueCIDList.name()).setText(""));
		experimentRoot.addContent(       		
				new Element(ExperimentFields.UniqueMSMSLibIdList.name()).setText(""));
		experimentRoot.addContent(       		
				new Element(ExperimentFields.UniqueMSRTLibIdList.name()).setText(""));
		experimentRoot.addContent(       		
				new Element(ExperimentFields.UniqueSampleIdList.name()).setText(""));
		
		//	MS2 file list
		Element msTwoFileListElement = 
				new Element(ExperimentFields.MsTwoFiles.name());		
		for(DataFile ms2dataFile : experimentToSave.getMSMSDataFiles())	        	
			msTwoFileListElement.addContent(ms2dataFile.getXmlElement());
		
		experimentRoot.addContent(msTwoFileListElement);
		
		//	MS1 file list
		Element msOneFileListElement = 
				new Element(ExperimentFields.MsOneFiles.name());		
		for(DataFile msOnedataFile : experimentToSave.getMSOneDataFiles())         	
			msOneFileListElement.addContent(msOnedataFile.getXmlElement());
		
		
		experimentRoot.addContent(msOneFileListElement);        
		document.setContent(experimentRoot);
		
		//	Save XML document
		File xmlFile = Paths.get(
				experimentToSave.getUncompressedExperimentFilesDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.PROJECT_FILE_NAME).toFile();
		try {
		    FileWriter writer = new FileWriter(xmlFile, false);
		    XMLOutputter outputter = new XMLOutputter();
		    outputter.setFormat(Format.getCompactFormat());
		    outputter.output(document, writer);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		try {
			CompressionUtils.zipFile(xmlFile, experimentToSave.getExperimentFile());
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
	
	public static Matrix readFeatureMatrix(			
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline) {
		
		File featureMatrixFile = Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(), 
				currentExperiment.getFeatureMatrixFileNameForDataPipeline(dataPipeline)).toFile();
		if (!featureMatrixFile.exists())
			return null;
		
		Matrix featureMatrix = null;
		if (featureMatrixFile.exists()) {
			try {
				featureMatrix = Matrix.Factory.load(featureMatrixFile);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return null;
			}
			if (featureMatrix != null) {

				if(featureMatrix.getMetaDataDimensionMatrix(0).getMetaData() == null)
					featureMatrix.setMetaDataDimensionMatrix(0, 
							currentExperiment.getMetaDataMatrixForDataPipeline(dataPipeline, 0));
				
				if(featureMatrix.getMetaDataDimensionMatrix(1).getMetaData() == null)
					featureMatrix.setMetaDataDimensionMatrix(1, 
							currentExperiment.getMetaDataMatrixForDataPipeline(dataPipeline, 1));
			}
		}		
		return featureMatrix;
	}
	
	public static void saveFeatureMatrixToFile(
			Matrix msFeatureMatrix,
			DataAnalysisProject currentExperiment, 
			DataPipeline dataPipeline) {
		
		String featureMatrixFileName = 
				currentExperiment.getFeatureMatrixFileNameForDataPipeline(dataPipeline);
		if(featureMatrixFileName == null || featureMatrixFileName.isEmpty()) 
			return;
			
		File featureMatrixFile = 
				Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(), 
				featureMatrixFileName).toFile();
		try {
			Matrix featureMatrix = 
					Matrix.Factory.linkToArray(msFeatureMatrix.toObjectArray());
			featureMatrix.save(featureMatrixFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}		
	}
	
	public static void saveDataMatrixForPipeline(
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline) {
		
		File dataMatrixFile = Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(), 
				currentExperiment.getDataMatrixFileNameForDataPipeline(dataPipeline)).toFile();
		try {
			Matrix dataMatrix = Matrix.Factory
					.linkToArray(currentExperiment.getDataMatrixForDataPipeline(dataPipeline).
							toDoubleArray());
			dataMatrix.save(dataMatrixFile);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}




