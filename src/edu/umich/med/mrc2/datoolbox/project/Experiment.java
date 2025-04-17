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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public abstract class Experiment implements Serializable, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ProjectType projectType;
	protected String id;
	protected String name;
	protected String description;
	protected File experimentFile;
	protected Date dateCreated;
	protected Date lastModified;
	protected ExperimentDesign experimentDesign;
	protected LIMSUser createdBy;
	protected LIMSExperiment limsExperiment;
	
	protected Experiment(
			ProjectType projectType,
			String name, 
			String description) {
		super();
		this.projectType = projectType;
		this.name = name;
		this.description = description;		
		this.id = UUID.randomUUID().toString();
		this.dateCreated = new Date();
		this.lastModified = new Date();
	}

	protected Experiment(
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
	}

	protected void createDirectoryStructureForNewExperiment(File parentDirectory) {
		
		Path experimentDirectoryPath = 
				Paths.get(parentDirectory.getAbsolutePath(), name.replaceAll("\\W+", "-"));				
		try {
			Files.createDirectories(experimentDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create project directory");
			return;
		}
		Path exportsDirectoryPath = Paths.get(experimentDirectoryPath.toString(), 
				MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY);
		try {
			Files.createDirectories(exportsDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create exports directory");
			return;
		}
		// Also creates libraries directory inside
		Path dataDirectoryPath = Paths.get(experimentDirectoryPath.toString(), 
				MRC2ToolBoxConfiguration.DATA_DIRECTORY, MRC2ToolBoxConfiguration.LIBRARY_DIRECTORY);
		try {
			Files.createDirectories(dataDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.showWarningMsg("Failed to create data directory");
		}
	}
		
	public void updateExperimentLocation(File newExperimentFile) {		
		experimentFile = newExperimentFile;
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
		return experimentFile.getParentFile();
	}
	
	public File getExportsDirectory() {
		return Paths.get(experimentFile.getParentFile().getAbsolutePath(), 
					MRC2ToolBoxConfiguration.DATA_EXPORT_DIRECTORY).toFile();
	}
	
	public File getDataDirectory() {
		return Paths.get(experimentFile.getParentFile().getAbsolutePath(), 
					MRC2ToolBoxConfiguration.DATA_DIRECTORY).toFile();
	}

	public File getLibraryDirectory() {
		return Paths.get(experimentFile.getParentFile().getAbsolutePath(), 
					MRC2ToolBoxConfiguration.DATA_DIRECTORY,
					MRC2ToolBoxConfiguration.LIBRARY_DIRECTORY).toFile();
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

	public abstract Set<ExperimentalSample> getPooledSamples();

	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	public abstract LIMSExperiment getLimsExperiment();

	public void setLimsExperiment(LIMSExperiment limsExperiment) {
		this.limsExperiment = limsExperiment;
	}
		
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!Experiment.class.isAssignableFrom(obj.getClass()))
            return false;

        final Experiment other = (Experiment) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    } 
    
	protected Experiment(Element experimentElement) {
		
		super();
		experimentElement.setAttribute(CommonFields.Id.name(), id);

		name = ProjectStoreUtils.getTextFromElement(experimentElement, CommonFields.Name);
		description = ProjectStoreUtils.getDescriptionFromElement(experimentElement);
		
		dateCreated = ProjectStoreUtils.getDateFromAttribute(
				experimentElement, CommonFields.DateCreated);
		lastModified = ProjectStoreUtils.getDateFromAttribute(
				experimentElement, CommonFields.LastModified);		
		createdBy = ProjectStoreUtils.getUserFromAttribute(experimentElement);
		
		Element designElement = experimentElement.getChild(
				ObjectNames.ExperimentDesign.name());
		if(designElement != null)
			experimentDesign = new ExperimentDesign(designElement, null);
		
		Element limsExperimentElement = experimentElement.getChild(
				ObjectNames.limsExperiment.name());
		if(limsExperimentElement != null)
			limsExperiment = new LIMSExperiment(limsExperimentElement, null);
	}
	
	@Override
    public Element getXmlElement() {
    	
    	Element experimentElement = new Element(ObjectNames.Experiment.name());
    	experimentElement.setAttribute(CommonFields.Id.name(), id);
    	
    	ProjectStoreUtils.addTextElement(name, experimentElement, CommonFields.Name);  	
    	ProjectStoreUtils.addDescriptionElement(description, experimentElement);
    	
    	ProjectStoreUtils.setDateAttribute(
    			dateCreated, CommonFields.DateCreated, experimentElement);
    	ProjectStoreUtils.setDateAttribute(
    			lastModified, CommonFields.LastModified, experimentElement);  	
    	ProjectStoreUtils.setUserIdAttribute(createdBy, experimentElement);
    	
    	if(experimentDesign != null)
    		experimentElement.addContent(experimentDesign.getXmlElement());
    	
    	if(limsExperiment != null)
    		experimentElement.addContent(limsExperiment.getXmlElement());
    	
    	return experimentElement;
    }
}

















