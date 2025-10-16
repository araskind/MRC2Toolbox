/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSBioSpecies;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;

public class StockSample implements Serializable, Comparable<StockSample>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5947698148673180844L;
	private String sampleId;
	private String sampleName;
	private String sampleDescription;
	private LIMSSampleType limsSampleType;
	private Date dateCreated;
	private String  limsSampleId;
	private String  externalId;
	private String  externalSource;
	private LIMSBioSpecies  species;
	private LIMSExperiment limsExperiment;

	public StockSample(
			String sampleId,
			String sampleName,
			String sampleDescription,
			LIMSSampleType limsSampleType,
			Date dateCreated,
			String limsSampleId,
			String externalId,
			String externalSource,
			LIMSBioSpecies species) {
		super();
		this.sampleId = sampleId;
		this.sampleName = sampleName;
		this.sampleDescription = sampleDescription;
		this.limsSampleType = limsSampleType;
		this.dateCreated = dateCreated;
		this.limsSampleId = limsSampleId;
		this.externalId = externalId;
		this.externalSource = externalSource;
		this.species = species;
	}

	public StockSample(
			String sampleName,
			String sampleDescription,
			LIMSSampleType limsSampleType,
			LIMSBioSpecies species) {
		super();

		this.sampleName = sampleName;
		this.sampleDescription = sampleDescription;
		this.limsSampleType = limsSampleType;
		this.species = species;
		this.dateCreated = new Date();
	}

	/**
	 * @return the sampleId
	 */
	public String getSampleId() {
		return sampleId;
	}

	/**
	 * @return the sampleName
	 */
	public String getSampleName() {
		return sampleName;
	}

	/**
	 * @return the sampleDescription
	 */
	public String getSampleDescription() {
		return sampleDescription;
	}

	/**
	 * @return the limsSampleType
	 */
	public LIMSSampleType getLimsSampleType() {
		return limsSampleType;
	}

	public String getLimsSampleTypeId() {
		return limsSampleType.getId();
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the limsSampleId
	 */
	public String getLimsSampleId() {
		return limsSampleId;
	}

	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * @return the externalSource
	 */
	public String getExternalSource() {
		return externalSource;
	}

	/**
	 * @return the species
	 */
	public LIMSBioSpecies getSpecies() {
		return species;
	}

	/**
	 * @param sampleId the sampleId to set
	 */
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	/**
	 * @param sampleName the sampleName to set
	 */
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	/**
	 * @param sampleDescription the sampleDescription to set
	 */
	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}

	/**
	 * @param limsSampleType the limsSampleType to set
	 */
	public void setLimsSampleType(LIMSSampleType limsSampleType) {
		this.limsSampleType = limsSampleType;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @param limsSampleId the limsSampleId to set
	 */
	public void setLimsSampleId(String limsSampleId) {
		this.limsSampleId = limsSampleId;
	}

	/**
	 * @param externalId the externalId to set
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * @param externalSource the externalSource to set
	 */
	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}

	/**
	 * @param species the species to set
	 */
	public void setSpecies(LIMSBioSpecies species) {
		this.species = species;
	}

	@Override
	public int compareTo(StockSample o) {
		return this.sampleId.compareTo(o.getSampleId());
	}

	/**
	 * @return the taxonomyId
	 */
	public int getTaxonomyId() {
		return species.getTaxonomyId();
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!StockSample.class.isAssignableFrom(obj.getClass()))
            return false;

        final StockSample other = (StockSample) obj;

        if ((this.sampleId == null) ? (other.getSampleId() != null) : !this.sampleId.equals(other.getSampleId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.sampleId != null ? this.sampleId.hashCode() : 0);
        return hash;
    }

	public LIMSExperiment getLimsExperiment() {
		return limsExperiment;
	}

	public void setLimsExperiment(LIMSExperiment limsExperiment) {
		this.limsExperiment = limsExperiment;
	}
	
	@Override
	public String toString() {
		return sampleName;
	}
}












