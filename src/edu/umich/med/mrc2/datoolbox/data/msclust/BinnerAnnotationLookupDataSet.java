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

package edu.umich.med.mrc2.datoolbox.data.msclust;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.store.BinnerAnnotationLookupDataSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;

public class BinnerAnnotationLookupDataSet implements Comparable<BinnerAnnotationLookupDataSet>{

	private String id;
	private String name;
	private String description;
	private LIMSUser createdBy;
	private Date dateCreated;
	private Date lastModified;
	private Set<BinnerAnnotationCluster>annotationClusters;
	
	public BinnerAnnotationLookupDataSet(
			String name, 
			String description, 
			LIMSUser createdBy) {
		this(name, description, createdBy, 
				new Date(), new Date());
	}
	
	public BinnerAnnotationLookupDataSet(String name) {
		this(name, name, MRC2ToolBoxCore.getIdTrackerUser(), 
				new Date(), new Date());
	}
	
	public BinnerAnnotationLookupDataSet(
			String id, 
			String name, 
			String description, 
			LIMSUser createdBy, 
			Date dateCreated,
			Date lastModified) {
		this(name, description, createdBy, dateCreated, lastModified);
		this.id = id;
	}

	public BinnerAnnotationLookupDataSet(
			String name, 
			String description, 
			LIMSUser createdBy, 
			Date dateCreated,
			Date lastModified) {
		super();
		this.id = DataPrefix.LOOKUP_BINNER_ANNOTATIONS_DATA_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 6);
		this.name = name;
		this.description = description;
		this.createdBy = createdBy;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		annotationClusters = new HashSet<BinnerAnnotationCluster>();
	}
	
	public BinnerAnnotationLookupDataSet(
			String name, 
			String description, 
			Collection<BinnerAnnotationCluster> annotationClusters2) {

		this(name, description, MRC2ToolBoxCore.getIdTrackerUser(), 
				new Date(), new Date());
		this.annotationClusters = new HashSet<BinnerAnnotationCluster>(annotationClusters2);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		
		if (obj == null)
			return false;

		if (!BinnerAnnotationLookupDataSet.class.isAssignableFrom(obj.getClass()))
			return false;

		final BinnerAnnotationLookupDataSet other = (BinnerAnnotationLookupDataSet) obj;

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

	@Override
	public int compareTo(BinnerAnnotationLookupDataSet o) {
		return name.compareTo(o.getName());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return name;
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

	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
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

	public Set<BinnerAnnotationCluster> getBinnerAnnotationClusters() {
		return annotationClusters;
	}

	public void setFeatures(Set<BinnerAnnotationCluster> features) {
		this.annotationClusters = features;
	}

	public Element getXmlElement() {

		Element binnerAnnotationLookupDataSetElement = 
				new Element(ObjectNames.BinnerAnnotationLookupDataSet.name());
		binnerAnnotationLookupDataSetElement.setAttribute(CommonFields.Id.name(), id);	
		ProjectStoreUtils.addTextElement(
				name, binnerAnnotationLookupDataSetElement, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(
				description, binnerAnnotationLookupDataSetElement);
		
		ProjectStoreUtils.setUserIdAttribute(
				createdBy, binnerAnnotationLookupDataSetElement);
		ProjectStoreUtils.setDateAttribute(
				dateCreated, CommonFields.DateCreated, binnerAnnotationLookupDataSetElement);
		ProjectStoreUtils.setDateAttribute(
				lastModified, CommonFields.LastModified, binnerAnnotationLookupDataSetElement);

        Element bacListElement = 
        		new Element(BinnerAnnotationLookupDataSetFields.BAList.name());
        if(!annotationClusters.isEmpty()) {
        	
        	for(BinnerAnnotationCluster bac : annotationClusters)
        		bacListElement.addContent(bac.getXmlElement());      	
        }       
        binnerAnnotationLookupDataSetElement.addContent(bacListElement);
 	
		return binnerAnnotationLookupDataSetElement;
	}
	
	public BinnerAnnotationLookupDataSet(Element xmlElement) {
				
		this.id = xmlElement.getAttributeValue(CommonFields.Id.name());
		if(id == null)
			this.id = DataPrefix.LOOKUP_FEATURE_DATA_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 6);

		name = ProjectStoreUtils.getTextFromElement(xmlElement, CommonFields.Name);
		description = ProjectStoreUtils.getDescriptionFromElement(xmlElement);
		dateCreated = ProjectStoreUtils.getDateFromAttribute(
				xmlElement, CommonFields.DateCreated);
		lastModified = ProjectStoreUtils.getDateFromAttribute(
				xmlElement, CommonFields.LastModified);
	
		createdBy = ProjectStoreUtils.getUserFromAttribute(xmlElement);
		if(createdBy == null)
			createdBy = MRC2ToolBoxCore.getIdTrackerUser();
		
		annotationClusters = new HashSet<BinnerAnnotationCluster>();
		
		List<Element> bacListElements = 
				xmlElement.getChildren(BinnerAnnotationLookupDataSetFields.BAList.name());
		if(!bacListElements.isEmpty()) {
			
			List<Element> bacElementList = 
					bacListElements.get(0).getChildren(ObjectNames.BinnerAnnotationCluster.name());
			for(Element bacElement : bacElementList) {
				
				BinnerAnnotationCluster newCluster = 
						new BinnerAnnotationCluster(bacElement);
				if(newCluster != null)
					annotationClusters.add(newCluster);
			}
		}
	}
	
	public BinnerAnnotationCluster getBinnerAnnotationClusterById(String bacId) {
		
		return annotationClusters.stream().
			filter(c -> c.getId().equals(bacId)).
			findFirst().orElse(null);
	}
}
