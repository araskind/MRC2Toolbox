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
import java.util.Set;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

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
}
