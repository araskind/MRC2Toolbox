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

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.StoredProjectFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class SaveStoredRawDataAnalysisProjectTask extends AbstractTask implements TaskListener {
	
	private RawDataAnalysisProject projectToSave;
	private File xmlFile;
	private int fileFeatureCount;
	private int processedFiles;
	
	private Set<String>uniqueCompoundIds;
	private Set<String>uniqueMSMSLibraryIds;
	private Set<String>uniqueMSRTLibraryIds;
	private Set<String>uniqueSampleIds;

	public SaveStoredRawDataAnalysisProjectTask(RawDataAnalysisProject projectToSave) {
		super();
		this.projectToSave = projectToSave;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		projectToSave.setLastModified(new Date());
		fileFeatureCount = getFeatureFileCount();
		processedFiles = 0;
		extractDatabaseReferences();
		try {
			createProjectXml();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(fileFeatureCount == 0) {
			//compressAndCleanup();
			try {
				compressProjectFile();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void createProjectXml() {
		taskDescription = "Creating XML file";
		processed = 10;
		
        Document document = new Document();
        Element projectRoot = 
        		new Element(StoredProjectFields.IDTrackerRawDataProject.name());
		projectRoot.setAttribute("version", "1.0.0.0");
		projectRoot.setAttribute(StoredProjectFields.Id.name(), 
				projectToSave.getId());
		projectRoot.setAttribute(StoredProjectFields.Name.name(), 
				projectToSave.getName());
		projectRoot.setAttribute(StoredProjectFields.Description.name(), 
				projectToSave.getDescription());
		projectRoot.setAttribute(StoredProjectFields.ProjectFile.name(), 
				projectToSave.getProjectFile().getAbsolutePath());
		projectRoot.setAttribute(StoredProjectFields.ProjectDir.name(), 
				projectToSave.getProjectDirectory().getAbsolutePath());	
		projectRoot.setAttribute(StoredProjectFields.DateCreated.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getDateCreated()));
		projectRoot.setAttribute(StoredProjectFields.DateModified.name(), 
				ProjectUtils.dateTimeFormat.format(projectToSave.getLastModified()));
        
		projectRoot.addContent(       		
        		new Element(StoredProjectFields.UniqueCIDList.name()).
        		setText(StringUtils.join(uniqueCompoundIds, ",")));
		projectRoot.addContent(       		
        		new Element(StoredProjectFields.UniqueMSMSLibIdList.name()).
        		setText(StringUtils.join(uniqueMSMSLibraryIds, ",")));
		projectRoot.addContent(       		
        		new Element(StoredProjectFields.UniqueMSRTLibIdList.name()).
        		setText(StringUtils.join(uniqueMSRTLibraryIds, ",")));
		projectRoot.addContent(       		
        		new Element(StoredProjectFields.UniqueSampleIdList.name()).
        		setText(StringUtils.join(uniqueSampleIds, ",")));
		
		//	MS2 file list
        Element msTwoFileListElement = 
        		new Element(StoredProjectFields.MsTwoFiles.name());		
        for(DataFile ms2dataFile : projectToSave.getMSMSDataFiles()) {
        	
        	msTwoFileListElement.addContent(ms2dataFile.getXmlElement());
			Collection<MsFeatureInfoBundle>fileFeatures = 
					projectToSave.getMsFeaturesForDataFile(ms2dataFile);
			if(fileFeatures.size() > 0) {
				SaveFileMsFeaturesTask task = 
						new SaveFileMsFeaturesTask(
								ms2dataFile, 
								projectToSave.getUncompressedProjectFilesDirectory(), 
								fileFeatures);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
        }
        projectRoot.addContent(msTwoFileListElement);
		//	MS1 file list
        Element msOneFileListElement = 
        		new Element(StoredProjectFields.MsOneFiles.name());		
        for(DataFile msOnedataFile : projectToSave.getMSOneDataFiles()) {
        	
        	msOneFileListElement.addContent(msOnedataFile.getXmlElement());
			Collection<MsFeatureInfoBundle>fileFeatures = 
					projectToSave.getMsFeaturesForDataFile(msOnedataFile);
			if(fileFeatures.size() > 0) {
				SaveFileMsFeaturesTask task = 
						new SaveFileMsFeaturesTask(
								msOnedataFile, 
								projectToSave.getUncompressedProjectFilesDirectory(), 
								fileFeatures);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
        }
        projectRoot.addContent(msOneFileListElement);        
        document.setContent(projectRoot);

		//	Save XML document
		xmlFile = Paths.get(
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
	}

	private int getFeatureFileCount() {
		
		long msmsCount = projectToSave.getMSMSDataFiles().stream().				
				filter(f -> !projectToSave.getMsFeaturesForDataFile(f).isEmpty()).count();
		long msOneCount = projectToSave.getMSOneDataFiles().stream().				
				filter(f -> !projectToSave.getMsFeaturesForDataFile(f).isEmpty()).count();
				
		return Long.valueOf(msmsCount + msOneCount).intValue();
	}
	
	private void extractDatabaseReferences() {
		taskDescription = "Collecting common database references ...";
		processed = 10;		
		List<MsFeatureIdentity> idList = projectToSave.getMsMsFeatureBundles().stream().
			filter(p -> p.getMsFeature().getPrimaryIdentity() != null).
			flatMap(p -> p.getMsFeature().getIdentifications().stream()).
			collect(Collectors.toList());
			
		uniqueCompoundIds = idList.stream().
				map(i -> i.getCompoundIdentity().getPrimaryDatabaseId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSMSLibraryIds = idList.stream().
				filter(i -> i.getReferenceMsMsLibraryMatch() != null).
				map(i -> i.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getUniqueId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSRTLibraryIds = idList.stream().
				filter(i -> i.getMsRtLibraryMatch() != null).
				map(i -> i.getMsRtLibraryMatch().getLibraryTargetId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueSampleIds = projectToSave.getExperimentalSamples().
				stream().map(s -> s.getId()).
				collect(Collectors.toCollection(TreeSet::new));
	}
	
	private void compressProjectFile() throws Exception {
		
		taskDescription = "Compressing XML file";
		processed = 50;		
		if(xmlFile != null && xmlFile.exists()) {		
			
			processed = 60;
//	        OutputStream archiveStream = new FileOutputStream(projectToSave.getProjectFile());
//	        ZipArchiveOutputStream archive =
//	        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
//	        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
//	        archive.setUseZip64(Zip64Mode.Always);
//	        ZipArchiveEntry entry = new ZipArchiveEntry(projectToSave.getName());
//	        archive.putArchiveEntry(entry);
//
//	        BufferedInputStream input = new BufferedInputStream(new FileInputStream(xmlFile));
//	        org.apache.commons.io.IOUtils.copy(input, archive);
//	        input.close();
//	        archive.closeArchiveEntry();
//	        archive.finish();
//	        archive.close();
//	        archiveStream.close();	
			CompressionUtils.zipFile(
					xmlFile, projectToSave.getProjectFile());
	        //	xmlFile.delete();
	        processed = 100;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(SaveFileMsFeaturesTask.class)) {	
				processedFiles++;
				if(processedFiles == fileFeatureCount) {
					//	compressAndCleanup();
					try {
						compressProjectFile();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}				
			}
		}		
	}

	@Override
	public Task cloneTask() {
		return new SaveStoredRawDataAnalysisProjectTask(projectToSave);
	}
}
