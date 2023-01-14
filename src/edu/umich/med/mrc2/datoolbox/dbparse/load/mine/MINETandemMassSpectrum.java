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

package edu.umich.med.mrc2.datoolbox.dbparse.load.mine;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class MINETandemMassSpectrum extends TandemMassSpectrum {

	/**
	 *
	 */
	private static final long serialVersionUID = -2895139470741792217L;
	private String mineSpectrumId;
	private String mineId;
	private Map<MINEMSPFields,String>properties;

	public MINETandemMassSpectrum(Polarity polarity) {

		super(polarity);
		properties = new TreeMap<MINEMSPFields,String>();
		for(MINEMSPFields field : MINEMSPFields.values())
			properties.put(field, "");
	}

	/**
	 * @return the properties
	 */
	public Map<MINEMSPFields, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void addProperty(MINEMSPFields field,  String value) {
		properties.put(field, value);
	}

	/**
	 * @return the id
	 */
	public String getSpectrumId() {
		return mineSpectrumId;
	}

	/**
	 * @return the mineId
	 */
	public String getMineId() {
		return mineId;
	}

	/**
	 * @param id the id to set
	 */
	public void setSpectrumId(String id) {
		this.mineSpectrumId = id;
	}

	/**
	 * @param mineId the mineId to set
	 */
	public void setMineId(String mineId) {
		this.mineId = mineId;
	}
}




















