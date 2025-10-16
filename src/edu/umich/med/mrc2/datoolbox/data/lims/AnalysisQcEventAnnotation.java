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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.QcEventType;

public class AnalysisQcEventAnnotation implements Comparable<AnalysisQcEventAnnotation>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2504173546662713324L;

	private String id;
	private String text;
	private Date dateCreated;
	private Date lastModified;
	private LIMSUser createBy;
	private LIMSUser lastModifiedBy;
	private LIMSInstrument instrument;
	private LIMSExperiment experiment;
	private ExperimentalSample sample;
	private Assay assay;
	private QcEventType qcEventType;

	public AnalysisQcEventAnnotation(
			String uniqueId,
			String text,
			Date dateCreated,
			Date lastModified,
			LIMSUser createBy,
			LIMSUser lastModifiedBy,
			LIMSInstrument instrument,
			LIMSExperiment experimet,
			ExperimentalSample sample,
			Assay assay,
			QcEventType qcEventType) {
		super();
		this.id = uniqueId;
		this.text = text;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.createBy = createBy;
		this.lastModifiedBy = lastModifiedBy;
		this.instrument = instrument;
		this.experiment = experimet;
		this.sample = sample;
		this.assay = assay;
		this.qcEventType = qcEventType;
	}

	public AnalysisQcEventAnnotation(String text) {
		super();
		this.text = text;
		dateCreated = new Date();
		lastModified = new Date();
		id = DataPrefix.ANNOTATION.getName() + UUID.randomUUID().toString();
	}

	public AnalysisQcEventAnnotation(String uid, String contents, long created, long modified) {

		id = uid;
		dateCreated = new Date(created);
		lastModified = new Date(modified);
	}

	public AnalysisQcEventAnnotation(String uid, String contents) {

		id = uid;
		dateCreated = new Date();
		lastModified = new Date();
	}

	public AnalysisQcEventAnnotation(LIMSUser idTrackerUser, LIMSInstrument activeInstrument) {

		createBy = idTrackerUser;
		lastModifiedBy = idTrackerUser;
		instrument = activeInstrument;
		dateCreated = new Date();
		lastModified = new Date();
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @return the createBy
	 */
	public LIMSUser getCreateBy() {
		return createBy;
	}

	/**
	 * @return the lastModifiedBy
	 */
	public LIMSUser getLastModifiedBy() {
		return lastModifiedBy;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @param createBy the createBy to set
	 */
	public void setCreateBy(LIMSUser createBy) {
		this.createBy = createBy;
	}

	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(LIMSUser lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public int compareTo(AnalysisQcEventAnnotation o) {
		return id.compareTo(o.getId());
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!AnalysisQcEventAnnotation.class.isAssignableFrom(obj.getClass()))
            return false;

        final AnalysisQcEventAnnotation other = (AnalysisQcEventAnnotation) obj;

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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the uniqueId
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the uniqueId to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the instrument
	 */
	public LIMSInstrument getInstrument() {
		return instrument;
	}

	/**
	 * @param instrument the instrument to set
	 */
	public void setInstrument(LIMSInstrument instrument) {
		this.instrument = instrument;
	}

	/**
	 * @return the experimet
	 */
	public LIMSExperiment getExperiment() {
		return experiment;
	}

	/**
	 * @return the sample
	 */
	public ExperimentalSample getSample() {
		return sample;
	}

	/**
	 * @return the assay
	 */
	public Assay getAssay() {
		return assay;
	}

	/**
	 * @return the qcEventType
	 */
	public QcEventType getQcEventType() {
		return qcEventType;
	}

	/**
	 * @param experimet the experimet to set
	 */
	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}

	/**
	 * @param sample the sample to set
	 */
	public void setSample(ExperimentalSample sample) {
		this.sample = sample;
	}

	/**
	 * @param assay the assay to set
	 */
	public void setAssay(Assay assay) {
		this.assay = assay;
	}

	/**
	 * @param qcEventType the qcEventType to set
	 */
	public void setQcEventType(QcEventType qcEventType) {
		this.qcEventType = qcEventType;
	}

	public String getInstrumentId() {

		if(instrument == null)
			return null;
		else
			return instrument.getInstrumentId();
	}

	public String getExperimentId() {

		if(experiment == null)
			return null;
		else
			return experiment.getId();
	}

	public String getSampleId() {

		if(sample == null)
			return null;
		else
			return sample.getId();
	}

	public String getAssayId() {

		if(assay == null)
			return null;
		else
			return assay.getId();
	}

}















