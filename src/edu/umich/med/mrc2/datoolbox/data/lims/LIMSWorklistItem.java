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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;

public class LIMSWorklistItem extends WorklistItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3966153123806953344L;
	private DataAcquisitionMethod acquisitionMethod;
	private ExperimentalSample sample;
	private LIMSSamplePreparation samplePrep;
	private String prepItemId;
	private double injectionVolume;

	public LIMSWorklistItem(DataFile dataFile) {
		super(dataFile);
		// TODO Auto-generated constructor stub
	}

	public LIMSWorklistItem(
			DataFile dataFile,
			ExperimentalSample sample,
			DataAcquisitionMethod acquisitionMethod,
			LIMSSamplePreparation samplePrep,
			String prepItemId,
			Date timestamp,
			double injectionVolume) {

		super(dataFile);
		this.sample = sample;
		this.acquisitionMethod = acquisitionMethod;
		this.samplePrep = samplePrep;
		this.prepItemId = prepItemId;
		this.timeStamp = timestamp;
		this.injectionVolume = injectionVolume;
	}

	/**
	 * @return the acquisitionMethod
	 */
	public DataAcquisitionMethod getAcquisitionMethod() {
		return acquisitionMethod;
	}

	/**
	 * @return the sample
	 */
	public ExperimentalSample getSample() {
		return sample;
	}

	/**
	 * @return the samplePrep
	 */
	public LIMSSamplePreparation getSamplePrep() {
		return samplePrep;
	}

	/**
	 * @return the prepItemId
	 */
	public String getPrepItemId() {
		return prepItemId;
	}

	/**
	 * @param acquisitionMethod the acquisitionMethod to set
	 */
	public void setAcquisitionMethod(DataAcquisitionMethod acquisitionMethod) {
		this.acquisitionMethod = acquisitionMethod;
	}

	/**
	 * @param sample the sample to set
	 */
	public void setSample(ExperimentalSample sample) {
		this.sample = sample;
	}

	/**
	 * @param samplePrep the samplePrep to set
	 */
	public void setSamplePrep(LIMSSamplePreparation samplePrep) {
		this.samplePrep = samplePrep;
	}

	/**
	 * @param prepItemId the prepItemId to set
	 */
	public void setPrepItemId(String prepItemId) {
		this.prepItemId = prepItemId;
	}

	/**
	 * @return the injectionVolume
	 */
	public double getInjectionVolume() {
		return injectionVolume;
	}

	/**
	 * @param injectionVolume the injectionVolume to set
	 */
	public void setInjectionVolume(double injectionVolume) {
		this.injectionVolume = injectionVolume;
	}

}
