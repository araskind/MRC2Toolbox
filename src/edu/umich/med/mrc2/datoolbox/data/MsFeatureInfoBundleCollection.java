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
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

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
	private Collection<MsFeatureInfoBundle>features;
	private TreeSet<ObjectAnnotation> annotations;
	private boolean offLine;
	
	public MsFeatureInfoBundleCollection(String name) {
		super();
		this.name = name;
		this.id = DataPrefix.MSMS_FEATURE_COLLECTION.getName() + UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = new Date();
		features = new TreeSet<MsFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		annotations = new TreeSet<ObjectAnnotation>();
		owner = MRC2ToolBoxCore.getIdTrackerUser();
		offLine = false;
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
		this.name = name;	
		this.description = description;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.owner = owner;
		features = new TreeSet<MsFeatureInfoBundle>(
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		annotations = new TreeSet<ObjectAnnotation>();
		offLine = false;
	}
	
	public void clearCollection(){
		features.clear();		
	}
	
	public int getCollectionSize() {
		return features.size();
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

	public Collection<MsFeatureInfoBundle> getFeatures() {
		return features;
	}
	
	public Collection<MsFeatureInfoBundle> getMSMSFeatures() {
		return features.stream().
				filter(f -> f.getMSMSFeatureId() != null).
				collect(Collectors.toList());
	}
	
	public Collection<MsFeatureInfoBundle> getM1Features() {
		return features.stream().
				filter(f -> f.getMSMSFeatureId() == null).
				collect(Collectors.toList());
	}
	
	public void addFeature(MsFeatureInfoBundle featureToAdd) {
		features.add(featureToAdd);
	}
	
	public void removeFeature(MsFeatureInfoBundle toRemove) {
		features.remove(toRemove);
	}

	public void addFeatures(Collection<MsFeatureInfoBundle> featuresToAdd) {
		features.addAll(featuresToAdd);
	}
	
	public void removeFeatures(Collection<MsFeatureInfoBundle> featuresToremove) {
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
				filter(f -> f.getMSMSFeatureId() != null).
				map(f -> f.getMSMSFeatureId()).
				collect(Collectors.toSet());
	}

	public boolean isOffLine() {
		return offLine;
	}

	public void setOffLine(boolean offLine) {
		this.offLine = offLine;
	}
}
