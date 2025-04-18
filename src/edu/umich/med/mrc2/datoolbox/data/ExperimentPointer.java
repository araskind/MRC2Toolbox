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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.project.Project;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class ExperimentPointer {

	public static final String ELEMENT_NAME = "experimentPointer";
	public static final String EXPERIMENT_TYPE = "projectType";
	public static final String EXPERIMENT_ID = "id";
	public static final String EXPERIMENT_NAME = "name";
	public static final String EXPERIMENT_DESCRIPTION = "description";
	public static final String EXPERIMENT_FILE = "experimentFile";
	public static final String DATE_CREATED = "dateCreated";
	public static final String LAST_MODIFIED = "lastModified";
	public static final String LIMS_EXPERIMENT = "limsExperiment";
	public static final String CREATED_BY = "createdBy";
	
	private ProjectType projectType;
	private String id;
	private String name;
	private String description;
	private File experimentFile;
	private Date dateCreated;
	private Date lastModified;	
	protected LIMSUser createdBy;
	protected LIMSExperiment limsExperiment;
	
	public ExperimentPointer(Project parent) {
		super();
		this.projectType = parent.getProjectType();
		this.id = parent.getId();
		this.name = parent.getName();
		this.description = parent.getDescription();
		this.experimentFile = parent.getExperimentFile();
		this.dateCreated = parent.getDateCreated();
		this.lastModified = parent.getLastModified();
		this.createdBy = parent.getCreatedBy();
		this.limsExperiment = parent.getLimsExperiment();
	}

	public ExperimentPointer(LIMSExperiment limsExperiment) {
		super();
		this.limsExperiment = limsExperiment;
		
		this.projectType = ProjectType.ID_TRACKER_DATA_ANALYSIS;
		this.id = limsExperiment.getId();
		this.name = limsExperiment.getName();
		this.description = limsExperiment.getDescription();
		this.dateCreated = limsExperiment.getStartDate();
		this.lastModified = limsExperiment.getStartDate();
		this.createdBy = limsExperiment.getCreator();
	}

	public ProjectType getProjectType() {
		return projectType;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public File getExperimentFile() {
		return experimentFile;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Element getXmlElement() {
		
		Element experimentPointerElement = 
				new Element(ELEMENT_NAME);
		experimentPointerElement.setAttribute(EXPERIMENT_TYPE, projectType.name());	
		experimentPointerElement.setAttribute(EXPERIMENT_ID, id);	
		experimentPointerElement.setAttribute(EXPERIMENT_NAME, name.trim());
		experimentPointerElement.setAttribute(EXPERIMENT_DESCRIPTION, description.trim());
		
		if(experimentFile != null)
			experimentPointerElement.setAttribute(
					EXPERIMENT_FILE, experimentFile.getAbsolutePath());
		
		experimentPointerElement.setAttribute(DATE_CREATED,
				ProjectUtils.dateTimeFormat.format(dateCreated));
		experimentPointerElement.setAttribute(LAST_MODIFIED,
				ProjectUtils.dateTimeFormat.format(lastModified));
		
		if(limsExperiment != null && limsExperiment.getId() != null)
			experimentPointerElement.setAttribute(LIMS_EXPERIMENT, limsExperiment.getId());
		
		if(createdBy != null)
			experimentPointerElement.setAttribute(CREATED_BY, createdBy.getId());
		
		return experimentPointerElement;
	}
	
	public ExperimentPointer(Element pointerElement) {
		
		super();
		this.id = pointerElement.getAttributeValue(EXPERIMENT_ID);
		this.projectType = 
				ProjectType.valueOf(pointerElement.getAttributeValue(EXPERIMENT_TYPE));
		this.name = pointerElement.getAttributeValue(EXPERIMENT_NAME);
		this.description = pointerElement.getAttributeValue(EXPERIMENT_DESCRIPTION);
		
		String expFilePath = pointerElement.getAttributeValue(EXPERIMENT_FILE);
		if(expFilePath != null && !expFilePath.isEmpty())
			this.experimentFile = new File(expFilePath);
		
		try {
			this.dateCreated = ProjectUtils.dateTimeFormat.parse(
					pointerElement.getAttributeValue(DATE_CREATED));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			this.lastModified = ProjectUtils.dateTimeFormat.parse(
					pointerElement.getAttributeValue(LAST_MODIFIED));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		String userId = pointerElement.getAttributeValue(CREATED_BY); 
		if(userId != null)
			createdBy = IDTDataCache.getUserById(userId);
		
		String experimentId = pointerElement.getAttributeValue(LIMS_EXPERIMENT); 
		if(experimentId != null) {
			this.limsExperiment = IDTDataCache.getExperimentById(experimentId);
			if(this.limsExperiment == null)
				this.limsExperiment = LIMSDataCache.getExperimentById(experimentId);
		}				
	}

	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	public LIMSExperiment getLimsExperiment() {
		return limsExperiment;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ExperimentPointer.class.isAssignableFrom(obj.getClass()))
            return false;

        final ExperimentPointer other = (ExperimentPointer) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;
              
		if ((this.projectType == null) ? (other.getProjectType() != null)
				: !this.projectType.equals(other.getProjectType()))
			return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0) + 
        		(this.projectType != null ? this.projectType.hashCode() : 0);

        return hash;
    }	
}














