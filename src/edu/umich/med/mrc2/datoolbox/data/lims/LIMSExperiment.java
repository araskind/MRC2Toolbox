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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentDesignFields;
import edu.umich.med.mrc2.datoolbox.project.store.LIMSExperimentFields;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class LIMSExperiment implements Serializable, Comparable<LIMSExperiment>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -213980463044107366L;
	private String id;
	private LIMSProject project;
	private String name;
	private String description;
	private String notes;
	private String serviceRequestId;
	private Date startDate;
	private String nihGrant;
	private ExperimentDesign design;
	private Set<LIMSSamplePreparation>samplePreps;
	private boolean isChear;
	private boolean isMotrpac;
	private LIMSUser creator;
	
	//	TODO add DataPipelines collection

	public LIMSExperiment(
			String experimentId, 
			String name, 
			String description, 
			String notes, 
			String serviceRequestId, 
			Date startDate) {

		super();
		this.id = experimentId;
		this.name = name;
		this.description = description;
		this.notes = notes;
		this.serviceRequestId = serviceRequestId;
		this.startDate = startDate;
		samplePreps = new TreeSet<LIMSSamplePreparation>();
	}

	public LIMSExperiment(
			String name,
			String description, 
			String notes, 
			LIMSProject parentProject) {

		super();
		this.name = name;
		this.description = description;
		this.notes = notes;
		this.project = parentProject;
		samplePreps = new TreeSet<LIMSSamplePreparation>();
	}
	
	public LIMSExperiment(LIMSExperiment experimentToCopy) {

		super();
		this.id = experimentToCopy.getId();
		this.name = experimentToCopy.getName();
		this.description = experimentToCopy.getDescription();
		this.notes = experimentToCopy.getNotes();
		this.serviceRequestId = experimentToCopy.getServiceRequestId();
		this.startDate = experimentToCopy.getStartDate();
		this.creator = experimentToCopy.getCreator();		
		this.project = experimentToCopy.getProject();
		samplePreps = new TreeSet<LIMSSamplePreparation>();
		for(LIMSSamplePreparation prep : experimentToCopy.getSamplePreps()) {
			
			LIMSSamplePreparation prepCopy = new LIMSSamplePreparation (prep);
			samplePreps.add(prepCopy);
		}
		this.nihGrant = experimentToCopy.getNihGrant();
		this.isChear = experimentToCopy.isChear();
		this.isMotrpac = experimentToCopy.isMotrpac();
		
		if(experimentToCopy.getExperimentDesign() != null)
			this.design = new ExperimentDesign(experimentToCopy.getExperimentDesign());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSExperiment.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSExperiment other = (LIMSExperiment) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

		if (obj == this)
			return true;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @return the serviceRequestId
	 */
	public String getServiceRequestId() {
		return serviceRequestId;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @param serviceRequestId the serviceRequestId to set
	 */
	public void setServiceRequestId(String serviceRequestId) {
		this.serviceRequestId = serviceRequestId;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the project
	 */
	public LIMSProject getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(LIMSProject project) {
		this.project = project;
	}

	@Override
	public int compareTo(LIMSExperiment o) {
		return id.compareTo(o.getId());
	}

	/**
	 * @return the design
	 */
	public ExperimentDesign getExperimentDesign() {
		return design;
	}

	/**
	 * @param design the design to set
	 */
	public void setDesign(ExperimentDesign design) {
		this.design = design;
	}

	/**
	 * @return the nihGrant
	 */
	public String getNihGrant() {
		return nihGrant;
	}

	/**
	 * @param nihGrant the nihGrant to set
	 */
	public void setNihGrant(String nihGrant) {
		this.nihGrant = nihGrant;
	}

	@Override
	public String toString() {
		return id + " (" + name +")";
	}

	/**
	 * @return the samplePreps
	 */
	public Set<LIMSSamplePreparation> getSamplePreps() {
		return samplePreps;
	}

	public boolean isChear() {
		return isChear;
	}

	public void setChear(boolean isChear) {
		this.isChear = isChear;
	}

	public boolean isMotrpac() {
		return isMotrpac;
	}

	public void setMotrpac(boolean isMotrpac) {
		this.isMotrpac = isMotrpac;
	}

	public LIMSUser getCreator() {
		return creator;
	}

	public void setCreator(LIMSUser creator) {
		this.creator = creator;
	}
	
	public Element getXmlElement() {
		
		Element experimentElement = 
				new Element(LIMSExperimentFields.limsExperiment.name());

		if(id != null)
			experimentElement.setAttribute(
					LIMSExperimentFields.Id.name(), id);	
		
		if(name != null)
			experimentElement.setAttribute(
					LIMSExperimentFields.Name.name(), name);
		
		if(description != null)
			experimentElement.setAttribute(
					LIMSExperimentFields.Description.name(), description);
		
		if(notes != null)
			experimentElement.setAttribute(
					LIMSExperimentFields.Notes.name(), notes);
		
		if(project != null)
			experimentElement.setAttribute(
					LIMSExperimentFields.ProjectId.name(), project.getId());
					
		if(startDate == null)
			startDate = new Date();
		
		experimentElement.setAttribute(LIMSExperimentFields.DateCreated.name(), 
				ExperimentUtils.dateTimeFormat.format(startDate));
		
		if(creator != null)
			experimentElement.setAttribute(
					LIMSExperimentFields.UserId.name(), creator.getId());
		
		//	ExperimentDesign
		if(design != null) 
			experimentElement.addContent(design.getXmlElement());
			
		//	LIMSSamplePreparation list
		Element samplePrepListElement = 
				new Element(LIMSExperimentFields.SamplePrepList.name());
		
		if(samplePreps != null && !samplePreps.isEmpty()) {
			
			for(LIMSSamplePreparation prep : samplePreps)
				samplePrepListElement.addContent(prep.getXmlElement());
		}	
		experimentElement.addContent(samplePrepListElement);
		
		return experimentElement;
	}
	
	public LIMSExperiment(
			Element experimentElement, 
			RawDataAnalysisExperiment parentProject) {
		
		id = experimentElement.getAttributeValue(
				LIMSExperimentFields.Id.name());
		name = experimentElement.getAttributeValue(
				LIMSExperimentFields.Name.name());
		description = experimentElement.getAttributeValue(
				LIMSExperimentFields.Description.name());
		notes = experimentElement.getAttributeValue(
				LIMSExperimentFields.Notes.name());
		String projectId = 
				experimentElement.getAttributeValue(
						LIMSExperimentFields.ProjectId.name());
		if(projectId != null)
			project = IDTDataCache.getProjectById(projectId);
		
		startDate = new Date();
		String startDateString = 
				experimentElement.getAttributeValue(LIMSExperimentFields.DateCreated.name());
		if(startDateString != null) {
			try {
				startDate = ExperimentUtils.dateTimeFormat.parse(startDateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		String userId = 
				experimentElement.getAttributeValue(LIMSExperimentFields.UserId.name());
		if(userId != null)
			creator = IDTDataCache.getUserById(userId);
		
		//	ExperimentDesign
		Element experimentDesignElement =
				experimentElement.getChild(ExperimentDesignFields.ExperimentDesign.name());
		if(experimentDesignElement != null)
			design = new ExperimentDesign(
					experimentDesignElement, parentProject);
				
		//	LIMSSamplePreparation list
		samplePreps = new TreeSet<LIMSSamplePreparation>();
		Element prepListElement = 
				experimentElement.getChild(LIMSExperimentFields.SamplePrepList.name());
		if(prepListElement != null) {
			
			for(Element prepElement : prepListElement.getChildren()) {
				LIMSSamplePreparation prep = new LIMSSamplePreparation(prepElement);
				if(prep != null)
					samplePreps.add(prep);
			}
		}
	}
}










