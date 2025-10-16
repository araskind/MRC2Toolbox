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

import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;

public class IDTMsSummary implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7541385357499297744L;
	private String dataBundleId;
	private LIMSExperiment experiment;
	private IDTExperimentalSample sample;
	private DataAcquisitionMethod acquisitionMethod;
	private DataExtractionMethod dataExtractionMethod;
	private int featureCount;
	private double collisionEnergy;

	public IDTMsSummary(String dataBundleId, int featureCount) {
		super();
		this.dataBundleId = dataBundleId;
		this.featureCount = featureCount;
	}

	public IDTMsSummary(int featureCount) {
		super();
		this.featureCount = featureCount;
	}

	/**
	 * @return the dataBundleId
	 */
	public String getDataBundleId() {
		return dataBundleId;
	}

	/**
	 * @return the experiment
	 */
	public LIMSExperiment getExperiment() {
		return experiment;
	}

	/**
	 * @return the sample
	 */
	public IDTExperimentalSample getSample() {
		return sample;
	}

	/**
	 * @return the acquisitionMethod
	 */
	public DataAcquisitionMethod getAcquisitionMethod() {
		return acquisitionMethod;
	}

	/**
	 * @return the dataExtractionMethod
	 */
	public DataExtractionMethod getDataExtractionMethod() {
		return dataExtractionMethod;
	}

	/**
	 * @return the featureCount
	 */
	public int getFeatureCount() {
		return featureCount;
	}

	/**
	 * @param dataBundleId the dataBundleId to set
	 */
	public void setDataBundleId(String dataBundleId) {
		this.dataBundleId = dataBundleId;
	}

	/**
	 * @param experiment the experiment to set
	 */
	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}

	/**
	 * @param sample the sample to set
	 */
	public void setSample(IDTExperimentalSample sample) {
		this.sample = sample;
	}

	/**
	 * @param acquisitionMethod the acquisitionMethod to set
	 */
	public void setAcquisitionMethod(DataAcquisitionMethod acquisitionMethod) {
		this.acquisitionMethod = acquisitionMethod;
	}

	/**
	 * @param dataExtractionMethod the dataExtractionMethod to set
	 */
	public void setDataExtractionMethod(DataExtractionMethod dataExtractionMethod) {
		this.dataExtractionMethod = dataExtractionMethod;
	}

	/**
	 * @param featureCount the featureCount to set
	 */
	public void setFeatureCount(int featureCount) {
		this.featureCount = featureCount;
	}

	/**
	 * @return the collisionEnergy
	 */
	public double getCollisionEnergy() {
		return collisionEnergy;
	}

	/**
	 * @param collisionEnergy the collisionEnergy to set
	 */
	public void setCollisionEnergy(double collisionEnergy) {
		this.collisionEnergy = collisionEnergy;
	}



}
