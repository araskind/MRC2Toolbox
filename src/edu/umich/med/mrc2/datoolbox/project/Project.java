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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public abstract class Project {

	protected String id;
	protected String name;
	protected String description;
	protected File projectFile;	
	protected File projectDirectory;
	protected File exportsDirectory;
	protected Date dateCreated, lastModified;
	
	public Project(String projectName, 
			String projectDescription, 
			File parentDirectory) {
		super();
		this.name = projectName;
		this.description = projectDescription;		
		this.id = UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = new Date();
	}

	public Project(
			String id, 
			String name,
			String description, 
			File projectFile, 
			Date dateCreated, 
			Date lastModified) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.projectFile = projectFile;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		
		projectDirectory = projectFile.getParentFile();
	}
	
	public Project(Project other) {
		super();
		this.id = other.getId();
		this.name = other.getName();
		this.description = other.getDescription();
		this.projectFile = other.getProjectFile();
		this.dateCreated = other.getDateCreated();
		this.lastModified = other.getLastModified();		
		this.projectDirectory = other.getProjectDirectory();
		this.exportsDirectory = other.getExportsDirectory();
	}
	
	protected void initNewProject(File parentDirectory) {
		
		projectDirectory = 
				Paths.get(parentDirectory.getAbsolutePath(), name.replaceAll("\\W+", "-")).toFile();				
		projectFile = 
				Paths.get(projectDirectory.getAbsolutePath(), name.replaceAll("\\W+", "-") + "."
				+ MRC2ToolBoxConfiguration.RAW_DATA_PROJECT_FILE_EXTENSION).toFile();	
		exportsDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();
		try {
			Files.createDirectories(Paths.get(projectDirectory.getAbsolutePath()));
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
	
	protected void setProjectDirectories() {
		
		exportsDirectory = Paths.get(projectDirectory.getAbsolutePath(), 
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

	public File getProjectFile() {
		return projectFile;
	}

	public void setProjectFile(File projectFile) {
		this.projectFile = projectFile;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(File projectDirectory) {
		this.projectDirectory = projectDirectory;
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
	
	
	
	
	
	
}
