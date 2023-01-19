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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.Record;

public class HMDBRecord implements Record, Comparable<HMDBRecord>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6636647368395053931L;
	
	private String primaryId;
	private String name;
	private String sysName;
	private String traditionalIupacName;
	private String description;
	private String csDescription;
	private String aggregateState;
	private Date dateCreated;
	private Date lastUpdated;

	private Collection<String>synonyms;
	private Collection<String>secondaryHmdbAccesions;
	private CompoundIdentity compoundIdentity;
	private Collection<CompoundProperty>compoundProperties;
	private Collection<CompoundBioLocation>biolocations;
	private Collection<HMDBCitation>references;
	private Collection<CompoundConcentration>concentrations;
	private Collection<HMDBDesease>deseases;
	private Collection<HMDBPathway>pathways;
	private Collection<HMDBProteinAssociation>proteinAssociations;

	public HMDBRecord(String primaryId) {

		super();
		this.primaryId = primaryId;
		synonyms = new TreeSet<String>();
		secondaryHmdbAccesions = new TreeSet<String>();
		compoundIdentity = new CompoundIdentity();
		compoundProperties = new ArrayList<CompoundProperty>();
		biolocations = new ArrayList<CompoundBioLocation>();
		references = new ArrayList<HMDBCitation>();
		concentrations = new ArrayList<CompoundConcentration>();
		deseases = new ArrayList<HMDBDesease>();
		pathways =  new ArrayList<HMDBPathway>();
		proteinAssociations = new ArrayList<HMDBProteinAssociation>();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPrimaryId() {
		return primaryId;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the synonyms
	 */
	public Collection<String> getSynonyms() {
		return synonyms;
	}

	/**
	 * @return the compoundIdentity
	 */
	public CompoundIdentity getCompoundIdentity() {
		return compoundIdentity;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @return the compoundProperties
	 */
	public Collection<CompoundProperty> getCompoundProperties() {
		return compoundProperties;
	}

	/**
	 * @return the biolocations
	 */
	public Collection<CompoundBioLocation> getBiolocations() {
		return biolocations;
	}

	/**
	 * @return the references
	 */
	public Collection<HMDBCitation> getReferences() {
		return references;
	}

	/**
	 * @return the concentrations
	 */
	public Collection<CompoundConcentration> getConcentrations() {
		return concentrations;
	}

	/**
	 * @return the deseases
	 */
	public Collection<HMDBDesease> getDeseases() {
		return deseases;
	}

	/**
	 * @return the pathways
	 */
	public Collection<HMDBPathway> getPathways() {
		return pathways;
	}

	/**
	 * @return the proteinAssociation
	 */
	public Collection<HMDBProteinAssociation> getProteinAssociations() {
		return proteinAssociations;
	}

	public String getCsDescription() {
		return csDescription;
	}

	public void setCsDescription(String csDescription) {
		this.csDescription = csDescription;
	}

	/**
	 * @return the sysName
	 */
	public String getSysName() {
		return sysName;
	}

	/**
	 * @param sysName the sysName to set
	 */
	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	/**
	 * @return the aggregateState
	 */
	public String getAggregateState() {
		return aggregateState;
	}

	/**
	 * @param aggregateState the aggregateState to set
	 */
	public void setAggregateState(String aggregateState) {
		this.aggregateState = aggregateState;
	}

	@Override
	public int compareTo(HMDBRecord o) {
		return this.primaryId.compareTo(o.getPrimaryId()) ;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!HMDBRecord.class.isAssignableFrom(obj.getClass()))
            return false;

        final HMDBRecord other = (HMDBRecord) obj;

        if ((this.primaryId == null) ? (other.getPrimaryId() != null) : !this.primaryId.equals(other.getPrimaryId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.primaryId != null ? this.primaryId.hashCode() : 0);
        return hash;
    }

	public String getTraditionalIupacName() {
		return traditionalIupacName;
	}

	public void setTraditionalIupacName(String traditionalIupacName) {
		this.traditionalIupacName = traditionalIupacName;
	}

	public Collection<String> getSecondaryHmdbAccesions() {
		return secondaryHmdbAccesions;
	}
}




















