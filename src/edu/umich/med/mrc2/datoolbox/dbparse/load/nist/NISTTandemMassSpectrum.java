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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class NISTTandemMassSpectrum extends TandemMassSpectrum {

	/**
	 *
	 */
	private static final long serialVersionUID = -2895139470741792217L;
	protected Collection<String>synonyms;
	protected int dbnum;
	protected int nistNum;
	protected double exactMass;
	protected Map<MSPField,String>properties;
	protected Collection<MsPoint>precursors;
	protected String peptideModifications;
	protected String peptideSequence;
	protected Collection<String>notes;

	public NISTTandemMassSpectrum(Polarity polarity) {
		this(polarity, true);
	}
	
	public NISTTandemMassSpectrum(Polarity polarity, boolean initAllProperties) {

		super(polarity);
		synonyms = new ArrayList<String>();
		properties = new TreeMap<MSPField,String>();
		
		if(initAllProperties) {
			for(MSPField field : MSPField.values())
				properties.put(field, "");
		}
		precursors = new ArrayList<MsPoint>();
		notes = new ArrayList<String>();
	}

	public Collection<String> getNotes() {
		return notes;
	}

	public void addPrecursor(MsPoint precursor) {
		precursors.add(precursor);
	}

	/**
	 * @return the synonyms
	 */
	public Collection<String> getSynonyms() {
		return synonyms;
	}

	/**
	 * @return the dbnum
	 */
	public int getDbnum() {
		return dbnum;
	}

	/**
	 * @return the nistNum
	 */
	public int getNistNum() {
		return nistNum;
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
	public Map<MSPField, String> getProperties() {
		return properties;
	}
	
	public String getProperty(MSPField property) {
		return properties.get(property);
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void addSynonym(String synonym) {
		synonyms.add(synonym);
	}

	/**
	 * @param dbnum the dbnum to set
	 */
	public void setDbnum(int dbnum) {
		this.dbnum = dbnum;
	}

	/**
	 * @param nistNum the nistNum to set
	 */
	public void setNistNum(int nistNum) {
		this.nistNum = nistNum;
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
	public void addProperty(MSPField field,  String value) {
		properties.put(field, value);
	}

	/**
	 * @return the precursors
	 */
	public Collection<MsPoint> getPrecursors() {
		return precursors;
	}

	/**
	 * @return the peptideModifications
	 */
	public String getPeptideModifications() {
		return peptideModifications;
	}

	/**
	 * @param peptideModifications the peptideModifications to set
	 */
	public void setPeptideModifications(String peptideModifications) {
		this.peptideModifications = peptideModifications;
	}

	/**
	 * @return the peptideSequence
	 */
	public String getPeptideSequence() {
		return peptideSequence;
	}

	/**
	 * @param peptideSequence the peptideSequence to set
	 */
	public void setPeptideSequence(String peptideSequence) {
		this.peptideSequence = peptideSequence;
	}
	
    @Override
    public int hashCode() {
    	return uniqueId.hashCode();
    }
}




















