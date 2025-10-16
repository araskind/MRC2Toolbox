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

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;

public class MsFeatureDataSource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6328079932521867081L;
	private String dataSourceId;
	private IDTExperimentalSample sample;
	private LIMSExperiment experiment;
	private DataAcquisitionMethod acquisitionMethod;
	private DataExtractionMethod dataExtractionMethod;

	public MsFeatureDataSource(String dataSourceId) {
		super();
		this.dataSourceId = dataSourceId;
	}

	/**
	 * @return the dataSourceId
	 */
	public String getDataSourceId() {
		return dataSourceId;
	}

	/**
	 * @return the sample
	 */
	public IDTExperimentalSample getSample() {
		return sample;
	}

	/**
	 * @return the experiment
	 */
	public LIMSExperiment getExperiment() {
		return experiment;
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
	 * @param sample the sample to set
	 */
	public void setSample(IDTExperimentalSample sample) {
		this.sample = sample;
	}

	/**
	 * @param experiment the experiment to set
	 */
	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
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



}
