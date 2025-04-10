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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.LIMSProjectFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class LIMSProject implements Serializable, Comparable<LIMSProject>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1566589729503535035L;

	private String id;
	private String name;
	private String description;
	private String notes;
	private Date startDate;
	private LIMSClient client;
	private Set<LIMSExperiment>experiments;

	public LIMSProject(String id, String name, String description) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		experiments = new TreeSet<LIMSExperiment>();
	}
	
	public LIMSProject(
			String name, 
			String description, 
			String notes, 
			LIMSClient client) {
		super();

		this.name = name;
		this.description = description;
		this.notes = notes;
		this.client = client;
		experiments = new TreeSet<LIMSExperiment>();
	}
	
	public LIMSProject(
			String id,
			String name, 
			String description, 
			String notes, 
			LIMSClient client) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.notes = notes;
		this.client = client;
		experiments = new TreeSet<LIMSExperiment>();
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSProject.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSProject other = (LIMSProject) obj;

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
	 * @return the id
	 */
	public String getId() {
		return id;
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

	@Override
	public int compareTo(LIMSProject o) {
		return id.compareTo(o.getId());
	}

	/**
	 * @return the contactPerson
	 */
	public LIMSUser getContactPerson() {
		return client.getContactPerson();
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
	 * @return the organization
	 */
	public LIMSOrganization getOrganization() {
		return client.getOrganization();
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return the experiments
	 */
	public Set<LIMSExperiment> getExperiments() {
		return experiments;
	}	

	public void setId(String id) {
		this.id = id;
	}


	public LIMSClient getClient() {
		return client;
	}


	public void setClient(LIMSClient client) {
		this.client = client;
	}
	
	public LIMSProject(Element limsProjectElement) {
		
		id = limsProjectElement.getAttributeValue(CommonFields.Id.name());
		name = ProjectStoreUtils.getTextFromElement(
				limsProjectElement, CommonFields.Name);
		description = ProjectStoreUtils.getTextFromElement(
				limsProjectElement, CommonFields.Description);
		notes = ProjectStoreUtils.getTextFromElement(
				limsProjectElement, LIMSProjectFields.Notes);
		startDate = ProjectStoreUtils.getDateFromAttribute(
				limsProjectElement, CommonFields.DateCreated);
		
		Element clientElement = limsProjectElement.getChild(ObjectNames.LIMSClient.name());
		if(clientElement != null)
			client = new LIMSClient(clientElement);

		experiments = new TreeSet<LIMSExperiment>();
		List<Element>experimentList = 
				limsProjectElement.getChild(LIMSProjectFields.ExperimentList.name()).
				getChildren(ObjectNames.limsExperiment.name());
		if(experimentList != null && !experimentList.isEmpty()) {
			
			for(Element experimentElement : experimentList)
				experiments.add(new LIMSExperiment(experimentElement, null));
		}		
	}		

	@Override
	public Element getXmlElement() {
		
		Element limsProjectElement = new Element(ObjectNames.LIMSProject.name());
		limsProjectElement.setAttribute(CommonFields.Id.name(), id);
		ProjectStoreUtils.addTextElement(name, limsProjectElement, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(description, limsProjectElement);
		ProjectStoreUtils.addTextElement(notes, limsProjectElement, LIMSProjectFields.Notes);
		ProjectStoreUtils.setDateAttribute(
				startDate, CommonFields.DateCreated, limsProjectElement);
		
		if(client != null)
			limsProjectElement.addContent(client.getXmlElement());
		
		Element experimentListElement = 
				new Element(LIMSProjectFields.ExperimentList.name());
		if(experiments != null && !experiments.isEmpty()) {
			
			for(LIMSExperiment experiment : experiments)
				experimentListElement.addContent(experiment.getXmlElement());
		}
		limsProjectElement.addContent(experimentListElement);	
			
		return limsProjectElement;
	}
}











