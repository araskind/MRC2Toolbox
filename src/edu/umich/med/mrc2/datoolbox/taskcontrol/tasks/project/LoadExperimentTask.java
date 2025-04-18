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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LoadDatabaseLibraryTask;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class LoadExperimentTask extends AbstractTask implements TaskListener{

	private DataAnalysisProject newExperiment;
	private File experimentFile;
	private boolean waitingForlibrariesToLoad;
	private Set<String>libraryIds;
	private int loadedLibsCount;

	public LoadExperimentTask(File newExperimentFile) {

		this.experimentFile = newExperimentFile;
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Loading experiment " + 
				FilenameUtils.getBaseName(experimentFile.getName());

		total = 100;
		processed = 30;
		newExperiment = null;
		try {
			loadExperimentFile();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if (newExperiment != null) {

			newExperiment.updateExperimentLocation(experimentFile);
			verifyExperimentDesign();
			newExperiment.recreateMatrixMaps();

			for (DataPipeline dataPipeline : newExperiment.getDataPipelines()) {
				
				if(dataPipeline.equals(newExperiment.getActiveDataPipeline())) {
					taskDescription = "Reading data matrix for " + dataPipeline.getName();
					ProjectUtils.loadDataMatrixForPipeline(newExperiment, dataPipeline);
				}
//
//				
//				//	Data matrix
//				File dataMatrixFile = 
//						Paths.get(newExperiment.getDataDirectory().getAbsolutePath(), 
//							newExperiment.getDataMatrixFileNameForDataPipeline(dataPipeline)).toFile();
//				
//				//	TODO temp fix for current projects
//				if(dataMatrixFile == null || !dataMatrixFile.exists())
//					dataMatrixFile = Paths.get(newExperiment.getExperimentDirectory().getAbsolutePath(), 
//						newExperiment.getDataMatrixFileNameForDataPipeline(dataPipeline)).toFile();
//
//				Matrix dataMatrix = null;
//				if (dataMatrixFile.exists()) {
//					try {
//						dataMatrix = Matrix.Factory.load(dataMatrixFile);
//					} catch (ClassNotFoundException | IOException e) {
//						e.printStackTrace();
//						setStatus(TaskStatus.ERROR);
//						return;
//					}
//					if (dataMatrix != null) {
//					
//						dataMatrix.setMetaDataDimensionMatrix(0, 
//								newExperiment.getMetaDataMatrixForDataPipeline(dataPipeline, 0));
//						dataMatrix.setMetaDataDimensionMatrix(1, 
//								newExperiment.getMetaDataMatrixForDataPipeline(dataPipeline, 1));
//						newExperiment.setDataMatrixForDataPipeline(dataPipeline, dataMatrix);
//					}
//				}
			}
			newExperiment.restoreData();
			updateFeatureIdentifications();
		}
		loadLibraries();
		
		if(!waitingForlibrariesToLoad) {
			processed = 100;
			this.setStatus(TaskStatus.FINISHED);
		}
	}

	private void loadLibraries() {
		
		libraryIds = new TreeSet<String>();		
		for (DataPipeline dataPipeline : newExperiment.getDataPipelines()) {
			
			Set<String> plLibIds = newExperiment.getMsFeaturesForDataPipeline(dataPipeline).stream().
				filter(f -> f.isIdentified()).flatMap(f -> f.getMSRTIdentifications().stream()).				
				filter(i -> Objects.nonNull(i.getMsRtLibraryMatch().getLibraryId())).
				map(i -> i.getMsRtLibraryMatch().getLibraryId()).distinct().collect(Collectors.toSet());
			if(!plLibIds.isEmpty())
				libraryIds.addAll(plLibIds);
		}
		if(!libraryIds.isEmpty()) {
			
			waitingForlibrariesToLoad = true;
			loadedLibsCount = 0;
			for(String libId : libraryIds) {
				
				LoadDatabaseLibraryTask task = new LoadDatabaseLibraryTask(libId);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
	}

	private void updateFeatureIdentifications() {

		taskDescription = "Processing identifications ... ";
		Collection<MsFeatureIdentity>toRemove = new ArrayList<MsFeatureIdentity>();
		for (DataPipeline dataPipeline : newExperiment.getDataPipelines()) {
			
			for(MsFeature feature : newExperiment.getMsFeaturesForDataPipeline(dataPipeline)) {
				
				toRemove.clear();
				for(MsFeatureIdentity id : feature.getIdentifications()) {
					
					MsFeatureIdentity fbfId = null;
					if(id.getCompoundIdentity() == null 
							|| id.getCompoundIdentity().getPrimaryDatabaseId() == null)
						continue;
						
					String dbId = id.getCompoundIdentity().getPrimaryDatabaseId();
					if(dbId.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
							|| dbId.startsWith(DataPrefix.MS_LIBRARY_TARGET_OLD.getName())
							|| dbId.startsWith(DataPrefix.MS_FEATURE.getName())) {
						fbfId = id;
					}
					if(fbfId != null)
						toRemove.add(fbfId);
						
				}
				if(!toRemove.isEmpty()) {
					
					for(MsFeatureIdentity fbfId : toRemove)
						feature.removeIdentity(fbfId);
				}
				if(feature.getPrimaryIdentity() == null && feature.getIdentifications().isEmpty())
					feature.createDefaultPrimaryIdentity();
			}
		}
	}

	private void loadExperimentFile() {

		taskDescription = "Unzipping and reading experiment file ...";
		XStream experimentImport = new XStream(new StaxDriver());
		/**
		 * From
		 * https://stackoverflow.com/questions/44698296/security-framework-of-xstream-not-initialized-xstream-is-probably-vulnerable
		 * */
		//clear out existing permissions and set own ones
		experimentImport.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		experimentImport.addPermission(NoTypePermission.NONE);
		experimentImport.addPermission(NullPermission.NULL);
		experimentImport.addPermission(PrimitiveTypePermission.PRIMITIVES);

		//	TODO limit to actually used stuff
		experimentImport.allowTypesByRegExp(new String[] { ".*" });
		experimentImport.ignoreUnknownElements();
        
		try(ZipFile zipFile = new ZipFile(experimentFile)){

			if (zipFile.entries().hasMoreElements()) {
	
				ZipEntry zippedExperiment = zipFile.entries().nextElement();

				try(BufferedReader br = new BufferedReader(
						new InputStreamReader(zipFile.getInputStream(zippedExperiment), StandardCharsets.UTF_8))){
	
					newExperiment = (DataAnalysisProject) experimentImport.fromXML(br);
					if(newExperiment.getProjectType() == null)	
						newExperiment.setProjectType(ProjectType.DATA_ANALYSIS);
					
					newExperiment.setProjectFile(experimentFile);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		processed = 50;
	}

	//	This is a workaround to handle reference samples
	private void verifyExperimentDesign() {

		ExperimentDesign design = newExperiment.getExperimentDesign();
		ExperimentDesignFactor	sampleTypeFactor = 
				new ExperimentDesignFactor(StandardFactors.SAMPLE_CONTROL_TYPE.getName());
		Collection<ExperimentDesignLevel>refLevels =
				design.getSamples().stream().map(s -> s.getDesignCell().get(sampleTypeFactor)).
				distinct().collect(Collectors.toList());

		refLevels.stream().forEach(l -> sampleTypeFactor.addLevel(l));
		design.replaceSampleTypeFactor(sampleTypeFactor);
		
		//	Check samples and replace IDs/Names for reference samples where necessary
		Collection<ExperimentalSample> dbRefSamples = 
				ReferenceSamplesManager.getReferenceSamples();
		for(ExperimentalSample sample : design.getReferenceSamples()) {
			
			ExperimentalSample match = dbRefSamples.stream().
					filter(s -> Objects.nonNull(s.getSampleIdDeprecated())).
					filter(s -> s.getSampleIdDeprecated().equals(sample.getId())).
					findFirst().orElse(null);
			
			if(match != null && !sample.getId().equals(match.getId())) {
				sample.setId(match.getId());
				sample.setName(match.getName());				
			}
		}
	}	

	@Override
	public Task cloneTask() {
		return new LoadExperimentTask(experimentFile);
	}
	
	public DataAnalysisProject getNewExperiment() {
		return newExperiment;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(LoadDatabaseLibraryTask.class)){
				
				CompoundLibrary library = 
						((LoadDatabaseLibraryTask)e.getSource()).getLibrary();
				
				if(library != null)
					MRC2ToolBoxCore.getActiveMsLibraries().add(library);
				
				loadedLibsCount = loadedLibsCount + 1;

				if (loadedLibsCount == libraryIds.size()) {
					
					((MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().
								getPanel(PanelList.MS_LIBRARY)).updateLibraryMenuAndLabel();
					setStatus(TaskStatus.FINISHED);
				}
			}
		}
	}
}

















