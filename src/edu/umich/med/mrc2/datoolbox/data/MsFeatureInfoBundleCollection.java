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

import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;

public class MsFeatureInfoBundleCollection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7040790270506625733L;
	private String id;
	private String name;	
	private String description;
	private Date dateCreated;
	private Date lastModified;
	private LIMSUser owner;
	private Collection<MSFeatureInfoBundle>features;
	private Set<String>featureIds;
	private TreeSet<ObjectAnnotation> annotations;
	private int collectionSize;
	
	public MsFeatureInfoBundleCollection(String name) {
		super();
		this.name = name;
		this.id = DataPrefix.MSMS_FEATURE_COLLECTION.getName() + UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = new Date();
		featureIds = new TreeSet<String>();
		features = new TreeSet<MSFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		annotations = new TreeSet<ObjectAnnotation>();
		owner = MRC2ToolBoxCore.getIdTrackerUser();
	}
	
	public MsFeatureInfoBundleCollection(
			String id, 
			String name) {
		this.id = id;
		this.name = name;
		dateCreated = new Date();
		lastModified = new Date();
		featureIds = new TreeSet<String>();
		features = new TreeSet<MSFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		annotations = new TreeSet<ObjectAnnotation>();
		owner = MRC2ToolBoxCore.getIdTrackerUser();
	}			

	public MsFeatureInfoBundleCollection(
			String id, 
			String name,
			String description,
			Date dateCreated, 
			Date lastModified,
			LIMSUser owner) {
		super();
		this.id = id;
		if(id == null)
			this.id = DataPrefix.MSMS_FEATURE_COLLECTION.getName() + UUID.randomUUID().toString();
		
		this.name = name;	
		this.description = description;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.owner = owner;
		featureIds = new TreeSet<String>();
		features = new TreeSet<MSFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		annotations = new TreeSet<ObjectAnnotation>();
	}
	
	public void clearCollection(){
		features.clear();		
	}
	
	public int getCollectionSize() {		
		return collectionSize;
	}
	
	public int getLoadedMSMSFeatureCount() {
		return features.size();
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public LIMSUser getOwner() {
		return owner;
	}

	public void setOwner(LIMSUser owner) {
		this.owner = owner;
	}

	public Collection<MSFeatureInfoBundle> getFeatures() {
		return features;
	}
	
	public Collection<MSFeatureInfoBundle> getMSMSFeatures() {
		return features.stream().
				filter(f -> Objects.nonNull(f.getMSMSFeatureId())).
				collect(Collectors.toList());
	}
	
	public Collection<MSFeatureInfoBundle> getMSOneFeatures() {
		return features.stream().
				filter(f -> Objects.isNull(f.getMSMSFeatureId())).
				collect(Collectors.toList());
	}
	
	public void addFeature(MSFeatureInfoBundle featureToAdd) {
		
		featureIds.add(featureToAdd.getMSFeatureId());		
		features.add(featureToAdd);
		collectionSize = featureIds.size();
	}
	
	public void removeFeature(MSFeatureInfoBundle toRemove) {
		
		featureIds.remove(toRemove.getMSFeatureId());
		features.remove(toRemove);
		collectionSize = featureIds.size();
	}

	public void addFeatures(Collection<MSFeatureInfoBundle> featuresToAdd) {
		
		Set<String> newIds = featuresToAdd.stream().
				map(f -> f.getMSFeatureId()).collect(Collectors.toSet());
		featureIds.addAll(newIds);
		features.addAll(featuresToAdd);
		collectionSize = featureIds.size();
	}
	
	public void removeFeatures(Collection<MSFeatureInfoBundle> featuresToremove) {
		
		Set<String> idsToRemove = featuresToremove.stream().
				map(f -> f.getMSFeatureId()).collect(Collectors.toSet());
		featureIds.removeAll(idsToRemove);
		features.removeAll(featuresToremove);
		collectionSize = featureIds.size();
	}

	public AnnotatedObjectType getAnnotatedObjectType() {
		return AnnotatedObjectType.MSMS_FEATURE_COLLECTION;
	}

	public TreeSet<ObjectAnnotation> getAnnotations() {
		return annotations;
	}

	public void addAnnotation(ObjectAnnotation annotation) {
		annotations.add(annotation);
	}
	
	public void removeAnnotation(ObjectAnnotation annotation) {
		annotations.remove(annotation);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureInfoBundleCollection.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureInfoBundleCollection other = (MsFeatureInfoBundleCollection) obj;

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
	
	public Collection<String> getMSMSFeatureIds() {		
		return features.stream().
				filter(f -> Objects.nonNull(f.getMSMSFeatureId())).
				map(f -> f.getMSMSFeatureId()).
				collect(Collectors.toSet());
	}
	
	public Collection<String> getMsFeatureIds() {		
		return features.stream().
				filter(f -> Objects.nonNull(f.getMSFeatureId())).
				map(f -> f.getMSFeatureId()).
				collect(Collectors.toSet());
	}
	
	public Set<String> getFeatureIds() {
		
		if(featureIds == null)
			featureIds = new TreeSet<String>();
		
		return featureIds;
	}
	
	public Element getXmlElement() {
		
		Element msFeatureCollectionElement = 
				new Element(ObjectNames.FeatureCollection.name());
		msFeatureCollectionElement.setAttribute(CommonFields.Id.name(), id);		
		ProjectStoreUtils.addTextElement(
				name, msFeatureCollectionElement, CommonFields.Name);
		ProjectStoreUtils.addDescriptionElement(
				description, msFeatureCollectionElement);
		
		ProjectStoreUtils.setDateAttribute(
				dateCreated, CommonFields.DateCreated, msFeatureCollectionElement);
		ProjectStoreUtils.setDateAttribute(
				lastModified, CommonFields.LastModified, msFeatureCollectionElement);
		ProjectStoreUtils.setUserIdAttribute(owner, msFeatureCollectionElement);
		
		//	TODO update feature ID list from database if necessary
		
		Set<String>featureIdSet = features.stream().
				map(f -> f.getMsFeature().getId()).
				collect(Collectors.toSet());
		msFeatureCollectionElement.addContent(       		
        		new Element(CommonFields.FeatureList.name()).
        		setText(StringUtils.join(featureIdSet, ",")));
		
		return msFeatureCollectionElement;
	}
	
	//	This is a temp fix for typo
	private void fixDateCreated(Element xmlElement) {
		
		String dateCreatedString = xmlElement.getAttributeValue("DateCreataed");
		if(dateCreatedString != null && !dateCreatedString.isBlank()) {
			try {	
				this.dateCreated = ProjectUtils.dateTimeFormat.parse(dateCreatedString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(this.dateCreated == null)
			this.dateCreated = new Date();
	}

	public MsFeatureInfoBundleCollection(Element xmlElement) {
		
		super();
		id = xmlElement.getAttributeValue(CommonFields.Id.name());
		if(id == null)
			this.id = DataPrefix.MSMS_FEATURE_COLLECTION.getName() + UUID.randomUUID().toString();
		
		//	TODO remove
		name = xmlElement.getAttributeValue(CommonFields.Name.name());
		if(name == null)
			name = ProjectStoreUtils.getTextFromElement(xmlElement, CommonFields.Name);
		
		//	TODO remove
		description = xmlElement.getAttributeValue(CommonFields.Description.name());
		if(description == null)
			description = ProjectStoreUtils.getDescriptionFromElement(xmlElement);
		
		dateCreated = ProjectStoreUtils.getDateFromAttribute(
				xmlElement, CommonFields.DateCreated);
		lastModified = ProjectStoreUtils.getDateFromAttribute(
				xmlElement, CommonFields.LastModified);
		
		if(dateCreated == null)
			fixDateCreated(xmlElement);
		
		if(lastModified == null)
			lastModified = dateCreated;
		
		owner = ProjectStoreUtils.getUserFromAttribute(xmlElement);
		
		features = new TreeSet<MSFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		
		String featureIdIdList = 
				xmlElement.getChild(CommonFields.FeatureList.name()).getText();
		featureIds = new TreeSet<String>(Arrays.asList(featureIdIdList.split(",")));
		annotations = new TreeSet<ObjectAnnotation>();
	}
	
	public boolean isEmpty() {
		return features.isEmpty();
	}

	public void setCollectionSize(int collectionSize) {
		this.collectionSize = collectionSize;
	}
	
	public String getFormattedMetadata() {
				
		String data = "<html><b>" + name + "</b><br>";
		if(description != null && !description.isEmpty())
			data += WordUtils.wrap(description, 80, "<br>", true) + "<br>";
		
		if(owner != null)
			data += "<b>Created by: </b>" + owner.getFullName() + "<br>";
		
		data += "<b># of features: </b>" + Integer.toString(collectionSize) + "<br>";
		data += "<b>Created on: </b>" + ProjectUtils.dateTimeFormat.format(dateCreated) + "<br>";
		data += "<b>Last modified on: </b>" + ProjectUtils.dateTimeFormat.format(lastModified);		
		return data;
	}
}










