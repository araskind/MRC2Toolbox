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

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;

public class LIMSSample extends ExperimentalSample implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3271675261717271276L;
	private String experimentId;
	private String userDescription;
	private String locationId;
	private String userDefinedSampleType;
	private double concentration;
	private String concentrationUnit;
	private double initialVolume;
	private String initialVolumeUnit;
	private double currentVolume;
	private String currentVolumeUnit;
	private String barcodeVerifiedByScan;
	private String statusId;
	private String notes;
	private Date dateCreated;
	private String subjectId;

	public LIMSSample(
			String sampleId2, 
			String sampleName2, 
			String experimentId,
			String userDescription,
			String limsSampleType,
			String locationId,
			String userDefinedSampleType,
			double concentration,
			String concentrationUnit,
			double initialVolume,
			String initialVolumeUnit,
			double currentVolume,
			String currentVolumeUnit,
			String barcodeVerifiedByScan,
			String statusId,
			String notes,
			Date dateCreated,
			String subjectId) {
		super(sampleId2, sampleName2);
		this.experimentId = experimentId;
		this.userDescription = userDescription;
		this.limsSampleType = limsSampleType;
		this.locationId = locationId;
		this.userDefinedSampleType = userDefinedSampleType;
		this.concentration = concentration;
		this.concentrationUnit = concentrationUnit;
		this.initialVolume = initialVolume;
		this.initialVolumeUnit = initialVolumeUnit;
		this.currentVolume = currentVolume;
		this.currentVolumeUnit = currentVolumeUnit;
		this.barcodeVerifiedByScan = barcodeVerifiedByScan;
		this.statusId = statusId;
		this.notes = notes;
		this.dateCreated = dateCreated;
		this.subjectId = subjectId;
	}

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getUserDescription() {
		return userDescription;
	}

	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getUserDefinedSampleType() {
		return userDefinedSampleType;
	}

	public void setUserDefinedSampleType(String userDefinedSampleType) {
		this.userDefinedSampleType = userDefinedSampleType;
	}

	public double getConcentration() {
		return concentration;
	}

	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}

	public String getConcentrationUnit() {
		return concentrationUnit;
	}

	public void setConcentrationUnit(String concentrationUnit) {
		this.concentrationUnit = concentrationUnit;
	}

	public double getInitialVolume() {
		return initialVolume;
	}

	public void setInitialVolume(double initialVolume) {
		this.initialVolume = initialVolume;
	}

	public String getInitialVolumeUnit() {
		return initialVolumeUnit;
	}

	public void setInitialVolumeUnit(String initialVolumeUnit) {
		this.initialVolumeUnit = initialVolumeUnit;
	}

	public double getCurrentVolume() {
		return currentVolume;
	}

	public void setCurrentVolume(double currentVolume) {
		this.currentVolume = currentVolume;
	}

	public String getCurrentVolumeUnit() {
		return currentVolumeUnit;
	}

	public void setCurrentVolumeUnit(String currentVolumeUnit) {
		this.currentVolumeUnit = currentVolumeUnit;
	}

	public String getBarcodeVerifiedByScan() {
		return barcodeVerifiedByScan;
	}

	public void setBarcodeVerifiedByScan(String barcodeVerifiedByScan) {
		this.barcodeVerifiedByScan = barcodeVerifiedByScan;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}
