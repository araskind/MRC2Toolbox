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
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
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
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileExtensions;
import edu.umich.med.mrc2.datoolbox.project.store.IDTrackerProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;

public class ProjectUtils {
	
	private ProjectUtils() {
		
	}
	
	public static final String dateFormatString = "yyyy-MM-dd HH:mm:ss";
	public static final DateFormat dateTimeFormat = new SimpleDateFormat(dateFormatString);
	public static final String tmpSuffix = "_TMP";
	
	public static DateFormat getDateFormat() {
		return new SimpleDateFormat(dateFormatString);
	}

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
			RawDataAnalysisProject experimentToSave) {
		
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
			RawDataAnalysisProject experimentToSave) {
		
		Document document = new Document();
	    Element experimentRoot = 
	    		new Element(ObjectNames.IDTrackerRawDataProject.name());
		experimentRoot.setAttribute("version", "1.0.0.0");
		experimentRoot.setAttribute(CommonFields.Id.name(), experimentToSave.getId());
		ProjectStoreUtils.addTextElement(
				experimentToSave.getName(), experimentRoot, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(
				experimentToSave.getDescription(), experimentRoot);		
		ProjectStoreUtils.setDateAttribute(
				experimentToSave.getDateCreated(), CommonFields.DateCreated, experimentRoot);
		ProjectStoreUtils.setDateAttribute(
				experimentToSave.getLastModified(), CommonFields.LastModified, experimentRoot);
		
		LIMSUser user = experimentToSave.getCreatedBy();
		if(user == null)
			user = MRC2ToolBoxCore.getIdTrackerUser();
		
		ProjectStoreUtils.setUserIdAttribute(user, experimentRoot);
		
		experimentRoot.setAttribute(IDTrackerProjectFields.ProjectFile.name(), 
				experimentToSave.getExperimentFile().getAbsolutePath());
		experimentRoot.setAttribute(IDTrackerProjectFields.ProjectDir.name(), 
				experimentToSave.getExperimentDirectory().getAbsolutePath());	
		experimentRoot.addContent(       		
				new Element(IDTrackerProjectFields.UniqueCIDList.name()).setText(""));
		experimentRoot.addContent(       		
				new Element(IDTrackerProjectFields.UniqueMSMSLibIdList.name()).setText(""));
		experimentRoot.addContent(       		
				new Element(IDTrackerProjectFields.UniqueMSRTLibIdList.name()).setText(""));
		experimentRoot.addContent(       		
				new Element(IDTrackerProjectFields.UniqueSampleIdList.name()).setText(""));
		
		//	MS2 file list
		Element msTwoFileListElement = 
				new Element(IDTrackerProjectFields.MsTwoFiles.name());		
		for(DataFile ms2dataFile : experimentToSave.getMSMSDataFiles())	        	
			msTwoFileListElement.addContent(ms2dataFile.getXmlElement());
		
		experimentRoot.addContent(msTwoFileListElement);
		
		//	MS1 file list
		Element msOneFileListElement = 
				new Element(IDTrackerProjectFields.MsOneFiles.name());		
		for(DataFile msOnedataFile : experimentToSave.getMSOneDataFiles())         	
			msOneFileListElement.addContent(msOnedataFile.getXmlElement());
		
		
		experimentRoot.addContent(msOneFileListElement);        
		document.setContent(experimentRoot);
		
		//	Save and zip XML document
		File xmlFile = Paths.get(
				experimentToSave.getUncompressedExperimentFilesDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.PROJECT_FILE_NAME).toFile();
		XmlUtils.writeCompactXMLtoFile(document, xmlFile);
		try {
			CompressionUtils.zipFile(xmlFile, experimentToSave.getExperimentFile());
		} catch (IOException | ArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static Collection<String>getIdList(String idListString){
		
		if(idListString == null || idListString.isEmpty())
			return new ArrayList<String>();
		else 
			return Arrays.asList(idListString.split(","));	
	}
	
	//	TODO this will only work after the project is saved in new format
	public static Matrix readFeatureMatrix(			
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline,
			boolean readTemporary) {
		
		File featureMatrixFile = 
				getFeatureMatrixFilePath(currentExperiment,dataPipeline, readTemporary).toFile();

		if (!featureMatrixFile.exists())
			return null;
		
		Matrix featureMatrix = null;
		try {
			featureMatrix = Matrix.Factory.load(featureMatrixFile);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return null;
		}
		if (featureMatrix != null) {

			featureMatrix.setMetaDataDimensionMatrix(0, 
					currentExperiment.getMetaDataMatrixForDataPipeline(dataPipeline, 0));				
			featureMatrix.setMetaDataDimensionMatrix(1, 
					currentExperiment.getMetaDataMatrixForDataPipeline(dataPipeline, 1));
		}				
		return featureMatrix;
	}
		
	public static void createDataDirectoryForProjectIfNotExists(
			DataAnalysisProject currentExperiment) {
		
		Path dataDirectoryPath = Paths.get(
				currentExperiment.getExperimentDirectory().getAbsolutePath(), 
				MRC2ToolBoxConfiguration.DATA_DIRECTORY, 
				MRC2ToolBoxConfiguration.LIBRARY_DIRECTORY);
		if(dataDirectoryPath.toFile().exists())
			return;
		
		try {
			Files.createDirectories(dataDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create data directory");
		}
	}
	
	public static void saveFeatureMatrixToFile(
			Matrix msFeatureMatrix,
			DataAnalysisProject currentExperiment, 
			DataPipeline dataPipeline,
			boolean saveAsTemporary) {
		
		createDataDirectoryForProjectIfNotExists(currentExperiment);

		File featureMatrixFile = 
				getFeatureMatrixFilePath(currentExperiment, dataPipeline, saveAsTemporary).toFile();
		try {
			Matrix featureMatrix = 
					Matrix.Factory.linkToArray(msFeatureMatrix.toObjectArray());
			featureMatrix.save(featureMatrixFile);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void saveDataMatrixForPipeline(
			DataAnalysisProject project,
			DataPipeline pipeline) {
		
		createDataDirectoryForProjectIfNotExists(project);
		
		File dataMatrixFile = getDataMatrixFilePath(project,pipeline,false).toFile();
		try {
			Matrix dataMatrix = Matrix.Factory
					.linkToArray(project.getDataMatrixForDataPipeline(pipeline).
							toDoubleArray());
			dataMatrix.save(dataMatrixFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	//	Will replace existing primary feature matrix file for 
	//	pipeline with temporary if both files are present
	public static void saveTemporaryFeatureMatrixFileAsPrimary(
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline) {
		
		createDataDirectoryForProjectIfNotExists(currentExperiment);
		
		File featureMatrixFile = 
				getFeatureMatrixFilePath(currentExperiment, dataPipeline, false).toFile();
		File tmpFeatureMatrixFile = 
				getFeatureMatrixFilePath(currentExperiment, dataPipeline, true).toFile();
				
		if(featureMatrixFile.exists() && tmpFeatureMatrixFile.exists()) {
			try {
				FIOUtils.replaceFile(tmpFeatureMatrixFile, featureMatrixFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void deleteTemporaryFeatureMatrixFile(
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline) {
		
		createDataDirectoryForProjectIfNotExists(currentExperiment);
		Path tmpFeatureMatrixFilePath = 
				getFeatureMatrixFilePath(currentExperiment, dataPipeline, true);
		
		FIOUtils.safeDeleteFile(tmpFeatureMatrixFilePath);
	}

	public static void moveFeatureMatrixFileToNewDefaultLocation(
			DataAnalysisProject project, DataPipeline pipeline) {
		
		createDataDirectoryForProjectIfNotExists(project);
		
		String featureMatrixFileName = 
				project.getFeatureMatrixFileNameForDataPipeline(pipeline);
		if(featureMatrixFileName == null) 
			return;
		
		Path featureMatrixFileNewLocation = 
				getFeatureMatrixFilePath(project, pipeline, false);
		Path featureMatrixFileOldLocation = 
				Paths.get(project.getExperimentDirectory().getAbsolutePath(), 
				featureMatrixFileName);
		
		if((featureMatrixFileNewLocation.toFile() == null 
				|| !featureMatrixFileNewLocation.toFile().exists())
				&& featureMatrixFileOldLocation.toFile().exists()) {
			try {
				Files.move(
					featureMatrixFileOldLocation, 
					featureMatrixFileNewLocation, 
					StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		if(featureMatrixFileOldLocation.toFile().exists()) {
			try {
				Files.delete(featureMatrixFileOldLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void moveDataMatrixFileToNewDefaultLocation(
			DataAnalysisProject project, DataPipeline pipeline) {
		
		createDataDirectoryForProjectIfNotExists(project);
		
		String dataMatrixFileName = 
				project.getDataMatrixFileNameForDataPipeline(pipeline);
		if(dataMatrixFileName == null || dataMatrixFileName.isEmpty()) 
			return;
		
		Path dataMatrixFileNewLocation = 
				getDataMatrixFilePath(project, pipeline, false);
		Path dataMatrixFileOldLocation = 
				Paths.get(project.getExperimentDirectory().getAbsolutePath(), 
				dataMatrixFileName);
		
		if((dataMatrixFileNewLocation.toFile() == null 
				|| !dataMatrixFileNewLocation.toFile().exists()) 
				&& dataMatrixFileOldLocation.toFile().exists()) {				
			try {
				Files.move(
					dataMatrixFileOldLocation, 
					dataMatrixFileNewLocation, 
					StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}	
		if(dataMatrixFileOldLocation.toFile().exists()) {
			try {
				Files.delete(dataMatrixFileOldLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void moveCEFLibraryFilesToNewDefaultLocation(DataAnalysisProject project) {
		
		createDataDirectoryForProjectIfNotExists(project);
		List<Path>cefPathList = 
				FIOUtils.findFilesByExtension(project.getExperimentDirectory().toPath(), "cef");
		if(!cefPathList.isEmpty()) {
			
			for(Path cefPath : cefPathList) {
				
				Path newPath = Paths.get(
						project.getLibraryDirectory().getAbsolutePath(), 
						cefPath.toFile().getName());
				try {
					Files.move(cefPath, newPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void loadDataMatrixForPipeline(
			DataAnalysisProject project, DataPipeline pipeline) {
		
		File dataMatrixFile = getDataMatrixFilePath(project,pipeline,false).toFile();
		
		//	TODO temp fix for current projects
		if(dataMatrixFile == null || !dataMatrixFile.exists())
			dataMatrixFile = Paths.get(project.getExperimentDirectory().getAbsolutePath(), 
					project.getDataMatrixFileNameForDataPipeline(pipeline)).toFile();

		Matrix dataMatrix = null;
		if (dataMatrixFile.exists()) {
			try {
				dataMatrix = Matrix.Factory.load(dataMatrixFile);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return;
			}
			if (dataMatrix != null) {
			
				dataMatrix.setMetaDataDimensionMatrix(0, 
						project.getMetaDataMatrixForDataPipeline(pipeline, 0));
				dataMatrix.setMetaDataDimensionMatrix(1, 
						project.getMetaDataMatrixForDataPipeline(pipeline, 1));
				project.setDataMatrixForDataPipeline(pipeline, dataMatrix);
			}
		}
	}
	
	public static Matrix loadDataMatrixForPipelineWitoutMetaData(
			DataAnalysisProject project, DataPipeline pipeline) {
		
		File dataMatrixFile = getDataMatrixFilePath(project,pipeline,false).toFile();
		Matrix dataMatrix = null;
		if (dataMatrixFile.exists()) {
			try {
				dataMatrix = Matrix.Factory.load(dataMatrixFile);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dataMatrix;
	}

	public static Path getFeaturesFilePath(
			DataAnalysisProject project, 
			DataPipeline pipeline) {
		Path featureXmlFilePath = 
				Paths.get(project.getDataDirectory().getAbsolutePath(),
				DataPrefix.MS_FEATURE.getName() + pipeline.getSaveSafeName() 
				+ "." + DataFileExtensions.FEATURE_LIST_EXTENSION.getExtension());
		return featureXmlFilePath;
	}
	
	public static Path getAveragedFeaturesFilePath(
			DataAnalysisProject project, 
			DataPipeline pipeline) {
		Path featureXmlFilePath = 
				Paths.get(project.getDataDirectory().getAbsolutePath(),
				DataPrefix.AVERAGED_FEATURE_LIBRARY.getName() + pipeline.getSaveSafeName() 
				+ "." + DataFileExtensions.AVERAGED_FEATURE_LIBRARY_EXTENSION.getExtension());
		return featureXmlFilePath;
	}
	
	public static Path getDataMatrixFilePath(
			DataAnalysisProject project, 
			DataPipeline pipeline,
			boolean isTemporary) {
		
		String temporaryFlag = "";
		if(isTemporary)
			 temporaryFlag = tmpSuffix;
		
		Path dataMatrixFilePath = 
				Paths.get(project.getDataDirectory().getAbsolutePath(),
				DataPrefix.DATA_MATRIX.getName() + pipeline.getSaveSafeName() 
				+ temporaryFlag + "." + DataFileExtensions.DATA_MATRIX_EXTENSION.getExtension());
		return dataMatrixFilePath;
	}
	
	public static Path getFeatureMatrixFilePath(
			DataAnalysisProject project, 
			DataPipeline pipeline,
			boolean isTemporary) {
		
		String temporaryFlag = "";
		if(isTemporary)
			 temporaryFlag = tmpSuffix;
		
		Path featureMatrixFilePath = 
				Paths.get(project.getDataDirectory().getAbsolutePath(), 
				DataPrefix.FEATURE_MATRIX.getName() + pipeline.getSaveSafeName() 
				+ temporaryFlag +  "." + DataFileExtensions.DATA_MATRIX_EXTENSION.getExtension());
		
		return featureMatrixFilePath;
	}
}




