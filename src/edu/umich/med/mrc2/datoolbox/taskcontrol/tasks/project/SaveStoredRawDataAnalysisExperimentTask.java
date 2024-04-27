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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class SaveStoredRawDataAnalysisExperimentTask extends AbstractTask implements TaskListener {
	
	private RawDataAnalysisExperiment experimentToSave;
	private File xmlFile;
	private int fileFeatureCount;
	private int processedFiles;
	private boolean featureSaveCompleted;
	private boolean chromatogramSaveCompleted;
	
	private Set<String>uniqueCompoundIds;
	private Set<String>uniqueMSMSLibraryIds;
	private Set<String>uniqueMSRTLibraryIds;
	private Set<String>uniqueSampleIds;

	public SaveStoredRawDataAnalysisExperimentTask(
			RawDataAnalysisExperiment experiment) {
		super();
		this.experimentToSave = experiment;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		experimentToSave.setLastModified(new Date());
		fileFeatureCount = getFeatureFileCount();
		processedFiles = 0;
		extractDatabaseReferences();
		initChromatogramSave();
//		chromatogramSaveCompleted = true;
		try {
			createExperimentXml();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		if(fileFeatureCount == 0) {
			//compressAndCleanup();
			try {
				compressExperimentFile();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void initChromatogramSave() {

        if(experimentToSave.getChromatogramMap() != null 
        		&& !experimentToSave.getChromatogramMap().isEmpty()) {
        	SaveFeatureChromatogramsTask task = 
        			new SaveFeatureChromatogramsTask(experimentToSave);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
        }
        else {
        	chromatogramSaveCompleted = true;
        }
	}

	private void createExperimentXml() {
		taskDescription = "Creating XML file";
		processed = 10;
		
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
		
		if(experimentToSave.getCreatedBy() != null)
			experimentRoot.setAttribute(ExperimentFields.UserId.name(), 
					experimentToSave.getCreatedBy().getId());
				
		if(experimentToSave.getInstrument() != null)
			experimentRoot.setAttribute(ExperimentFields.Instrument.name(), 
					experimentToSave.getInstrument().getInstrumentId());
		
		LIMSExperiment experiment = experimentToSave.getLimsExperiment();
		if(experiment != null)
			experimentRoot.addContent(experiment.getXmlElement());
			
		experimentRoot.setAttribute(ExperimentFields.ProjectFile.name(), 
				experimentToSave.getExperimentFile().getAbsolutePath());
		experimentRoot.setAttribute(ExperimentFields.ProjectDir.name(), 
				experimentToSave.getExperimentDirectory().getAbsolutePath());	
		experimentRoot.setAttribute(ExperimentFields.DateCreated.name(), 
				ExperimentUtils.dateTimeFormat.format(experimentToSave.getDateCreated()));
		experimentRoot.setAttribute(ExperimentFields.DateModified.name(), 
				ExperimentUtils.dateTimeFormat.format(experimentToSave.getLastModified()));
        
		experimentRoot.addContent(       		
        		new Element(ExperimentFields.UniqueCIDList.name()).
        		setText(StringUtils.join(uniqueCompoundIds, ",")));
		experimentRoot.addContent(       		
        		new Element(ExperimentFields.UniqueMSMSLibIdList.name()).
        		setText(StringUtils.join(uniqueMSMSLibraryIds, ",")));
		experimentRoot.addContent(       		
        		new Element(ExperimentFields.UniqueMSRTLibIdList.name()).
        		setText(StringUtils.join(uniqueMSRTLibraryIds, ",")));
		experimentRoot.addContent(       		
        		new Element(ExperimentFields.UniqueSampleIdList.name()).
        		setText(StringUtils.join(uniqueSampleIds, ",")));
		
		//	MSMS method
		if(experimentToSave.getMsmsExtractionParameterSet() != null)
			experimentRoot.addContent(experimentToSave.getMsmsExtractionParameterSet().getXmlElement());
		
		//	Feature Collections
        Element msFeatureCollectionListElement = 
        		new Element(ExperimentFields.FeatureCollectionList.name());
        if(!experimentToSave.getEditableMsFeatureInfoBundleCollections().isEmpty()) {
        	
        	for(MsFeatureInfoBundleCollection fbc : 
        				experimentToSave.getEditableMsFeatureInfoBundleCollections())
        		msFeatureCollectionListElement.addContent(fbc.getXmlElement());      	
        }       
        experimentRoot.addContent(msFeatureCollectionListElement);
		//	MS2 file list
        Element msTwoFileListElement = 
        		new Element(ExperimentFields.MsTwoFiles.name());		
        for(DataFile ms2dataFile : experimentToSave.getMSMSDataFiles()) {
        	
        	msTwoFileListElement.addContent(ms2dataFile.getXmlElement());
			Collection<MSFeatureInfoBundle>fileFeatures = 
					experimentToSave.getMsFeaturesForDataFile(ms2dataFile);
			if(fileFeatures.size() > 0) {
				SaveFileMsFeaturesTask task = 
						new SaveFileMsFeaturesTask(
								ms2dataFile, 
								experimentToSave.getUncompressedExperimentFilesDirectory(), 
								fileFeatures);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
        }
        experimentRoot.addContent(msTwoFileListElement);
		//	MS1 file list
        Element msOneFileListElement = 
        		new Element(ExperimentFields.MsOneFiles.name());		
        for(DataFile msOnedataFile : experimentToSave.getMSOneDataFiles()) {
        	
        	msOneFileListElement.addContent(msOnedataFile.getXmlElement());
			Collection<MSFeatureInfoBundle>fileFeatures = 
					experimentToSave.getMsFeaturesForDataFile(msOnedataFile);
			if(fileFeatures.size() > 0) {
				SaveFileMsFeaturesTask task = 
						new SaveFileMsFeaturesTask(
								msOnedataFile, 
								experimentToSave.getUncompressedExperimentFilesDirectory(), 
								fileFeatures);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
        }
        experimentRoot.addContent(msOneFileListElement);        
        document.setContent(experimentRoot);
        
        //	Injections
        Element injectionListElement = 
        		new Element(ExperimentFields.Injections.name());
        for(Injection injection : experimentToSave.getInjections())
        	injectionListElement.addContent(injection.getXmlElement());
             
        experimentRoot.addContent(injectionListElement); 
               
        //	MSMS cluster collections
        Element msmsClusterListElement = 
        		new Element(ExperimentFields.MSMSClusterDataSetList.name());
        if(!experimentToSave.getMsmsClusterDataSets().isEmpty()) {
        	
        	for(IMSMSClusterDataSet fbc : experimentToSave.getMsmsClusterDataSets())
        		msmsClusterListElement.addContent(fbc.getXmlElement());       	
        }
        experimentRoot.addContent(msmsClusterListElement);
        
		//	Save XML document
		xmlFile = Paths.get(
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
	}

	private int getFeatureFileCount() {
		
		long msmsCount = experimentToSave.getMSMSDataFiles().stream().				
				filter(f -> !experimentToSave.getMsFeaturesForDataFile(f).isEmpty()).count();
		long msOneCount = experimentToSave.getMSOneDataFiles().stream().				
				filter(f -> !experimentToSave.getMsFeaturesForDataFile(f).isEmpty()).count();
				
		return Long.valueOf(msmsCount + msOneCount).intValue();
	}
	
	private void extractDatabaseReferences() {
		taskDescription = "Collecting common database references ...";
		processed = 10;		
		List<MsFeatureIdentity> idList = experimentToSave.getMsMsFeatureBundles().stream().
			filter(p -> Objects.nonNull(p.getMsFeature().getPrimaryIdentity())).
			flatMap(p -> p.getMsFeature().getIdentifications().stream()).
			collect(Collectors.toList());
			
		uniqueCompoundIds = idList.stream().
				filter(i -> Objects.nonNull(i.getCompoundIdentity())).
				filter(i -> Objects.nonNull(i.getCompoundIdentity().getPrimaryDatabaseId())).
				map(i -> i.getCompoundIdentity().getPrimaryDatabaseId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSMSLibraryIds = idList.stream().
				filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch())).
				map(i -> i.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getUniqueId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueMSRTLibraryIds = idList.stream().
				filter(i -> Objects.nonNull(i.getMsRtLibraryMatch())).
				map(i -> i.getMsRtLibraryMatch().getLibraryTargetId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		uniqueSampleIds = experimentToSave.getExperimentalSamples().
				stream().map(s -> s.getId()).
				collect(Collectors.toCollection(TreeSet::new));
	}
	
	private void compressExperimentFile() throws Exception {
		
		taskDescription = "Compressing XML file";
		processed = 50;		
		if(xmlFile != null && xmlFile.exists()) {		
			
			processed = 60;	
			CompressionUtils.zipFile(
					xmlFile, experimentToSave.getExperimentFile());
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
				if(processedFiles == fileFeatureCount)
					featureSaveCompleted = true;
			}
			if (e.getSource().getClass().equals(SaveFeatureChromatogramsTask.class))
				chromatogramSaveCompleted = true;
			
			if(featureSaveCompleted && chromatogramSaveCompleted) {
				//	compressAndCleanup();
				try {
					compressExperimentFile();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}		
	}

	@Override
	public Task cloneTask() {
		return new SaveStoredRawDataAnalysisExperimentTask(experimentToSave);
	}

	public RawDataAnalysisExperiment getExperimentToSave() {
		return experimentToSave;
	}
}
