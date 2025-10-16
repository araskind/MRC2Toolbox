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

package edu.umich.med.mrc2.datoolbox.dbparse.load.massbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class MassBankTandemMassSpectrum extends TandemMassSpectrum {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3024496762958274951L;
	
	private Collection<String>synonyms;
	private double exactMass;
	private Map<MassBankDataField,String>properties;
	private Collection<String>notes;

	public MassBankTandemMassSpectrum(Polarity polarity) {

		super(polarity);
		synonyms = new ArrayList<String>();
		properties = new TreeMap<MassBankDataField,String>();
		for(MassBankDataField field : MassBankDataField.values())
			properties.put(field, "");

		notes = new ArrayList<String>();
	}

	public Collection<String> getNotes() {
		return notes;
	}

	/**
	 * @return the synonyms
	 */
	public Collection<String> getSynonyms() {
		return synonyms;
	}

	/**
	 * @return the exactMass
	 */
	public double getExactMass() {
		return exactMass;
	}

	/**
	 * @return the properties
	 */
	public Map<MassBankDataField, String> getProperties() {
		return properties;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void addSynonym(String synonym) {
		synonyms.add(synonym);
	}

	/**
	 * @param exactMass the exactMass to set
	 */
	public void setExactMass(double exactMass) {
		this.exactMass = exactMass;
	}

	/**
	 * @param properties the properties to set
	 */
	public void addProperty(MassBankDataField field,  String value) {
		properties.put(field, value);
	}
}




















