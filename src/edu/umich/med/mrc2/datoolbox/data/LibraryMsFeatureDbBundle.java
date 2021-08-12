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

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;

public class LibraryMsFeatureDbBundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5739353231032635167L;
	private LibraryMsFeature feature;
	private String conmpoundDatabaseAccession;
	private boolean isQcStandard;
	private CompoundIdentificationConfidence idConfidence;

	public LibraryMsFeatureDbBundle(
			LibraryMsFeature feature,
			String conmpoundDatabaseAccession,
			CompoundIdentificationConfidence idConfidence,
			boolean isQcStandard) {
		super();
		this.feature = feature;
		this.conmpoundDatabaseAccession = conmpoundDatabaseAccession;
		this.idConfidence = idConfidence;
		this.isQcStandard = isQcStandard;
	}

	/**
	 * @return the feature
	 */
	public LibraryMsFeature getFeature() {
		return feature;
	}

	/**
	 * @return the conmpoundDatabaseAccession
	 */
	public String getConmpoundDatabaseAccession() {
		return conmpoundDatabaseAccession;
	}

	/**
	 * @return the isQcStandard
	 */
	public boolean isQcStandard() {
		return isQcStandard;
	}

	/**
	 * @return the idConfidence
	 */
	public CompoundIdentificationConfidence getIdConfidence() {
		return idConfidence;
	}


}
