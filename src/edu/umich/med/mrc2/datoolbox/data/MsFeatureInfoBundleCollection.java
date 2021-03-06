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
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.store.FeatureCollectionFields;
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
	private Collection<String>featureIds;
	private TreeSet<ObjectAnnotation> annotations;
	
	public MsFeatureInfoBundleCollection(String name) {
		super();
		this.name = name;
		this.id = DataPrefix.MSMS_FEATURE_COLLECTION.getName() + UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = new Date();
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
		features = new TreeSet<MSFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		annotations = new TreeSet<ObjectAnnotation>();
	}
	
	public void clearCollection(){
		features.clear();		
	}
	
	public int getCollectionSize() {
		
		if(!features.isEmpty())
			return features.size();
		else
			return featureIds.size();
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
	
	public Collection<MSFeatureInfoBundle> getM1Features() {
		return features.stream().
				filter(f -> Objects.isNull(f.getMSMSFeatureId())).
				collect(Collectors.toList());
	}
	
	public void addFeature(MSFeatureInfoBundle featureToAdd) {
		features.add(featureToAdd);
	}
	
	public void removeFeature(MSFeatureInfoBundle toRemove) {
		features.remove(toRemove);
	}

	public void addFeatures(Collection<MSFeatureInfoBundle> featuresToAdd) {
		features.addAll(featuresToAdd);
	}
	
	public void removeFeatures(Collection<MSFeatureInfoBundle> featuresToremove) {
		features.removeAll(featuresToremove);
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
	
	public Collection<String> getFeatureIds() {
		return featureIds;
	}
	
	public Element getXmlElement() {
		
		Element msFeatureCollectionElement = 
				new Element(FeatureCollectionFields.FeatureCollection.name());
		msFeatureCollectionElement.setAttribute(
				FeatureCollectionFields.Id.name(), id);
		msFeatureCollectionElement.setAttribute(
				FeatureCollectionFields.Name.name(), name);
		msFeatureCollectionElement.setAttribute(
				FeatureCollectionFields.Description.name(), description);
		msFeatureCollectionElement.setAttribute(
				FeatureCollectionFields.DateCreataed.name(), 
				ProjectUtils.dateTimeFormat.format(dateCreated));
		msFeatureCollectionElement.setAttribute(
				FeatureCollectionFields.DateModified.name(), 
				ProjectUtils.dateTimeFormat.format(lastModified));	
		msFeatureCollectionElement.setAttribute(
				FeatureCollectionFields.UserId.name(), owner.getId());
		
		Set<String>featureIds = features.stream().
				map(f -> f.getMsFeature().getId()).
				collect(Collectors.toSet());
		msFeatureCollectionElement.addContent(       		
        		new Element(FeatureCollectionFields.FeatureList.name()).
        		setText(StringUtils.join(featureIds, ",")));
		
		return msFeatureCollectionElement;
	}

	public MsFeatureInfoBundleCollection(Element xmlElement) {
		
		super();
		this.id = xmlElement.getAttributeValue(FeatureCollectionFields.Id.name());
		if(id == null)
			this.id = DataPrefix.MSMS_FEATURE_COLLECTION.getName() + UUID.randomUUID().toString();
		
		this.name = xmlElement.getAttributeValue(FeatureCollectionFields.Name.name());
		this.description = xmlElement.getAttributeValue(FeatureCollectionFields.Description.name());
		try {
			this.dateCreated = ProjectUtils.dateTimeFormat.parse(
					xmlElement.getAttributeValue(FeatureCollectionFields.DateCreataed.name()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.lastModified = ProjectUtils.dateTimeFormat.parse(
					xmlElement.getAttributeValue(FeatureCollectionFields.DateModified.name()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		String userId =  xmlElement.getAttributeValue(FeatureCollectionFields.UserId.name());
		if(userId != null)
			this.owner = IDTDataCash.getUserById(userId);
		
		features = new TreeSet<MSFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		
		String featureIdIdList = 
				xmlElement.getChild(FeatureCollectionFields.FeatureList.name()).getText();
		featureIds = new TreeSet<String>(Arrays.asList(featureIdIdList.split(",")));
		annotations = new TreeSet<ObjectAnnotation>();
	}
}










