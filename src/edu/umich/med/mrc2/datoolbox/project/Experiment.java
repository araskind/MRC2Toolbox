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

package edu.umich.med.mrc2.datoolbox.project;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public abstract class Experiment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ProjectType projectType;
	protected String id;
	protected String name;
	protected String description;
	protected File experimentFile;	
	protected File experimentDirectory;
	protected File exportsDirectory;
	protected Date dateCreated, lastModified;
	protected ExperimentDesign experimentDesign;
	
	public Experiment(ProjectType projectType,
					String name, 
					String description, 
					File parentDirectory) {
		super();
		this.projectType = projectType;
		this.name = name;
		this.description = description;		
		this.id = UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = new Date();
	}

	public Experiment(
			String id, 
			String name,
			String description, 
			File experimentFile, 
			Date dateCreated, 
			Date lastModified) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.experimentFile = experimentFile;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		
		experimentDirectory = experimentFile.getParentFile();
	}
	
//	public Experiment(Experiment other) {
//		super();
//		this.projectType = other.getProjectType();
//		this.id = other.getId();
//		this.name = other.getName();
//		this.description = other.getDescription();
//		this.experimentFile = other.getExperimentFile();
//		this.dateCreated = other.getDateCreated();
//		this.lastModified = other.getLastModified();		
//		this.experimentDirectory = other.getExperimentDirectory();
//		this.exportsDirectory = other.getExportsDirectory();
//	}
	
	protected void createDirectoryStructureForNewExperiment(File parentDirectory) {
		
		experimentDirectory = 
				Paths.get(parentDirectory.getAbsolutePath(), name.replaceAll("\\W+", "-")).toFile();				
		exportsDirectory = Paths.get(experimentDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();
		try {
			Files.createDirectories(Paths.get(experimentDirectory.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create project directory");
			return;
		}
		try {
			Files.createDirectories(Paths.get(exportsDirectory.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create exports directory");
			return;
		}
	}
	
	protected void setExperimentDirectories() {
		
		exportsDirectory = Paths.get(experimentDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public File getExperimentFile() {
		return experimentFile;
	}

	public void setExperimentFile(File experimentFile) {
		this.experimentFile = experimentFile;
	}

	public File getExperimentDirectory() {
		return experimentDirectory;
	}

	public void setExperimentDirectory(File experimentDirectory) {
		this.experimentDirectory = experimentDirectory;
	}

	public File getExportsDirectory() {
		return exportsDirectory;
	}

	public void setExportsDirectory(File exportsDirectory) {
		this.exportsDirectory = exportsDirectory;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public ProjectType getProjectType() {
		return projectType;
	}

	public void setProjectType(ProjectType projectType) {
		this.projectType = projectType;
	}

	public ExperimentDesign getExperimentDesign() {
		return experimentDesign;
	}

	public void setExperimentDesign(ExperimentDesign experimentDesign) {
		this.experimentDesign = experimentDesign;
	}
}


