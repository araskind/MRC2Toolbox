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

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;

public class LibMatchedSimpleMsFeature extends SimpleMsFeature implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1624686224156749028L;
	private MsFeatureIdentity identity;

	public LibMatchedSimpleMsFeature(
			String targetId,
			MassSpectrum observedSpectrum,
			double retentionTime,
			DataPipeline dataPipeline) {

		super(targetId, observedSpectrum, retentionTime, dataPipeline);
	}
	
	public LibMatchedSimpleMsFeature(
			MsFeature feature, 
			DataPipeline dataPipeline) {
		super(feature, dataPipeline);
		identity = feature.getPrimaryIdentity();
	}

	/**
	 * @return the identity
	 */
	public MsFeatureIdentity getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	public void setIdentity(MsFeatureIdentity identity) {
		this.identity = identity;
	}

}
