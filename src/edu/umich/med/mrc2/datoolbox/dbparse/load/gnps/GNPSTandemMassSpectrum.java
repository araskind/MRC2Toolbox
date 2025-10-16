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

package edu.umich.med.mrc2.datoolbox.dbparse.load.gnps;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class GNPSTandemMassSpectrum extends TandemMassSpectrum {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1220453647354797007L;
	private Map<GNPSMGFFields,String>properties;

	public GNPSTandemMassSpectrum(String gnpsId, Polarity polarity) {

		super(polarity);
		this.uniqueId = gnpsId;
		properties = new TreeMap<GNPSMGFFields,String>();
		for(GNPSMGFFields field : GNPSMGFFields.values())
			properties.put(field, "");
	}
	
	public Map<GNPSMGFFields, String> getProperties() {
		return properties;
	}
	
	public String getProperty(GNPSMGFFields field) {
		return properties.get(field);
	}

	public void addProperty(GNPSMGFFields field,  String value) {
		properties.put(field, value);
	}
}




















