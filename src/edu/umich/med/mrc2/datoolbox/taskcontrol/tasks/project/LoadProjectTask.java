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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.ujmp.core.Matrix;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LoadProjectTask extends AbstractTask {

	private DataAnalysisProject newProject;
	private File projectDirectory, projectFile;

	public LoadProjectTask(File projectFile) {

		this.projectFile = projectFile;
		projectDirectory = projectFile.getParentFile();
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Loading project " + 
				FilenameUtils.getBaseName(projectFile.getName());

		total = 100;
		processed = 30;
		newProject = null;
		try {
			loadProjectFile();
		} catch (Throwable e) {

			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		if (newProject != null) {

			newProject.updateProjectLocation(projectFile);
			verifyExperimentDesign();
			newProject.recreateMatrixMaps();

			for (DataPipeline dataPipeline : newProject.getDataPipelines()) {

				taskDescription = "Reading data matrix for " + dataPipeline.getName();
				//	Data matrix
				File dataMatrixFile = 
						Paths.get(projectDirectory.getAbsolutePath(), 
							newProject.getDataMatrixFileNameForDataPipeline(dataPipeline)).toFile();

				Matrix dataMatrix = null;
				if (dataMatrixFile.exists()) {
					try {
						dataMatrix = Matrix.Factory.load(dataMatrixFile);
					} catch (ClassNotFoundException | IOException e) {
						setStatus(TaskStatus.ERROR);
						e.printStackTrace();
					}
					if (dataMatrix != null) {

						dataMatrix.setMetaDataDimensionMatrix(0, 
								newProject.getMetaDataMatrixForDataPipeline(dataPipeline, 0));
						dataMatrix.setMetaDataDimensionMatrix(1, 
								newProject.getMetaDataMatrixForDataPipeline(dataPipeline, 1));
						newProject.setDataMatrixForDataPipeline(dataPipeline, dataMatrix);
					}
				}
				//	Feature matrix			
//				if(newProject.getFeatureMatrixFileNameForDataPipeline(dataPipeline) != null) {
//					
//					taskDescription = "Reading feature matrix for " + dataPipeline.getName();
//					
//					File featureMatrixFile = 
//							Paths.get(projectDirectory.getAbsolutePath(), 
//								newProject.getFeatureMatrixFileNameForDataPipeline(dataPipeline)).toFile();
//
//					Matrix featureMatrix = null;
//					if (featureMatrixFile.exists()) {
//						try {
//							featureMatrix = Matrix.Factory.load(featureMatrixFile);
//						} catch (ClassNotFoundException | IOException e) {
//							setStatus(TaskStatus.ERROR);
//							e.printStackTrace();
//						}
//						if (featureMatrix != null) {
//
//							featureMatrix.setMetaDataDimensionMatrix(0, 
//									newProject.getMetaDataMatrixForDataPipeline(dataPipeline, 0));
//							featureMatrix.setMetaDataDimensionMatrix(1, 
//									newProject.getMetaDataMatrixForDataPipeline(dataPipeline, 1));
//							newProject.setFeatureMatrixForDataPipeline(dataPipeline, featureMatrix);
//						}
//					}
//				}
			}
			newProject.restoreData();
		}
		processed = 100;
		this.setStatus(TaskStatus.FINISHED);
	}

	private void loadProjectFile() throws ZipException, IOException {

		taskDescription = "Unzipping and reading project file ...";
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

			newProject = (DataAnalysisProject) projectImport.fromXML(br);

			br.close();
			input.close();
			zipFile.close();
		}
		processed = 50;
	}

	//	This is a workaround to handle reference samples
	private void verifyExperimentDesign() {

		ExperimentDesign design = newProject.getExperimentDesign();
		ExperimentDesignFactor	sampleTypeFactor = 
				new ExperimentDesignFactor(StandardFactors.SAMPLE_CONTROL_TYPE.getName());
		Collection<ExperimentDesignLevel>refLevels =
				design.getSamples().stream().map(s -> s.getDesignCell().get(sampleTypeFactor)).
				distinct().collect(Collectors.toList());

		refLevels.stream().forEach(l -> sampleTypeFactor.addLevel(l));
		design.replaceSampleTypeFactor(sampleTypeFactor);
		
		//	Check samples and replace IDs/Names for reference samples where necessary
		Collection<ExperimentalSample> dbRefSamples = ReferenceSamplesManager.getReferenceSamples();
		for(ExperimentalSample sample : design.getReferenceSamples()) {
			
			ExperimentalSample match = dbRefSamples.stream().
					filter(s -> s.getSampleIdDeprecated() != null).
					filter(s -> s.getSampleIdDeprecated().equals(sample.getId())).
					findFirst().orElse(null);
			
			if(match != null) {
				
				if(!sample.getId().equals(match.getId())) {
					sample.setId(match.getId());
					sample.setName(match.getName());
				}
			}
		}
	}	

	@Override
	public Task cloneTask() {
		return new LoadProjectTask(projectFile);
	}
	
	public DataAnalysisProject getNewProject() {
		return newProject;
	}
}

















