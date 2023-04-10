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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.store.FeatureLookupDataSetFields;
import edu.umich.med.mrc2.datoolbox.project.store.MinimalMSOneFeatureFields;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class FeatureLookupDataSet implements Comparable<FeatureLookupDataSet>{

	private String id;
	private String name;
	private String description;
	private LIMSUser createdBy;
	private Date dateCreated;
	private Date lastModified;
	private Set<MinimalMSOneFeature>features;
	
	public FeatureLookupDataSet(
			String name, 
			String description, 
			LIMSUser createdBy) {
		this(name, description, createdBy, 
				new Date(), new Date());
	}
	
	public FeatureLookupDataSet(String name) {
		this(name, name, MRC2ToolBoxCore.getIdTrackerUser(), 
				new Date(), new Date());
	}
	
	public FeatureLookupDataSet(
			String id, 
			String name, 
			String description, 
			LIMSUser createdBy, 
			Date dateCreated,
			Date lastModified) {
		this(name, description, createdBy, dateCreated, lastModified);
		this.id = id;
	}

	public FeatureLookupDataSet(
			String name, 
			String description, 
			LIMSUser createdBy, 
			Date dateCreated,
			Date lastModified) {
		super();
		this.id = DataPrefix.LOOKUP_FEATURE_DATA_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 6);
		this.name = name;
		this.description = description;
		this.createdBy = createdBy;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		features = new HashSet<MinimalMSOneFeature>();
	}
	
	public FeatureLookupDataSet(
			String name, 
			String description, 
			Collection<MinimalMSOneFeature> features2) {

		this(name, description, MRC2ToolBoxCore.getIdTrackerUser(), 
				new Date(), new Date());
		this.features = new HashSet<MinimalMSOneFeature>(features2);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (!FeatureLookupDataSet.class.isAssignableFrom(obj.getClass()))
			return false;

		final FeatureLookupDataSet other = (FeatureLookupDataSet) obj;

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

	@Override
	public int compareTo(FeatureLookupDataSet o) {
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

	public Set<MinimalMSOneFeature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<MinimalMSOneFeature> features) {
		this.features = features;
	}

	public Element getXmlElement() {

		Element featureLookupDataSetElement = 
				new Element(FeatureLookupDataSetFields.FeatureLookupDataSet.name());
		featureLookupDataSetElement.setAttribute(
				FeatureLookupDataSetFields.Id.name(), id);	
		featureLookupDataSetElement.setAttribute(
				FeatureLookupDataSetFields.Name.name(), name);
		String descString = description;
		if(descString == null)
			descString = "";
		
		featureLookupDataSetElement.setAttribute(
				FeatureLookupDataSetFields.Description.name(), descString);
		featureLookupDataSetElement.setAttribute(
				FeatureLookupDataSetFields.CreatedBy.name(), createdBy.getId());	
		featureLookupDataSetElement.setAttribute(
				FeatureLookupDataSetFields.DateCreated.name(), 
				ExperimentUtils.dateTimeFormat.format(dateCreated));
		featureLookupDataSetElement.setAttribute(
				FeatureLookupDataSetFields.LastModified.name(), 
				ExperimentUtils.dateTimeFormat.format(lastModified));		

        Element featureListElement = 
        		new Element(FeatureLookupDataSetFields.FeatureList.name());
        if(!features.isEmpty()) {
        	
        	for(MinimalMSOneFeature fbc : features)
        		featureListElement.addContent(fbc.getXmlElement());      	
        }       
        featureLookupDataSetElement.addContent(featureListElement);
 	
		return featureLookupDataSetElement;
	}
	
	public FeatureLookupDataSet(Element xmlElement) {
				
		this.id = xmlElement.getAttributeValue(FeatureLookupDataSetFields.Id.name());
		if(id == null)
			this.id = DataPrefix.LOOKUP_FEATURE_DATA_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 6);
		
		this.name = xmlElement.getAttributeValue(FeatureLookupDataSetFields.Name.name());
		this.description = xmlElement.getAttributeValue(FeatureLookupDataSetFields.Description.name());
		try {
			this.dateCreated = ExperimentUtils.dateTimeFormat.parse(
					xmlElement.getAttributeValue(FeatureLookupDataSetFields.DateCreated.name()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.lastModified = ExperimentUtils.dateTimeFormat.parse(
					xmlElement.getAttributeValue(FeatureLookupDataSetFields.LastModified.name()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		String userId =  xmlElement.getAttributeValue(FeatureLookupDataSetFields.CreatedBy.name());
		if(userId != null)
			this.createdBy = IDTDataCache.getUserById(userId);
		else
			this.createdBy = MRC2ToolBoxCore.getIdTrackerUser();
		
		features = new HashSet<MinimalMSOneFeature>();
		
		List<Element> featureListElements = 
				xmlElement.getChildren(FeatureLookupDataSetFields.FeatureList.name());
		if(featureListElements.size() > 0) {
			
			List<Element> featureElementList = 
					featureListElements.get(0).getChildren(MinimalMSOneFeatureFields.MinimalMSOneFeature.name());
			for(Element featureElement : featureElementList) {
				
				MinimalMSOneFeature newFeature = 
						new MinimalMSOneFeature(featureElement);
				if(newFeature != null)
					features.add(newFeature);
			}
		}
	}
}
