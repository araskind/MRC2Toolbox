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

package edu.umich.med.mrc2.datoolbox.dbparse.load.msdial;

import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class MSDialMSMSRecord {

	private String name;
	private String formula;
	private String smiles;
	private String inchiKey;
	private Polarity polarity;
	private String adduct;
	private String abbreviation;
	private Double exactMass;
	private Double precursorMz;
	private Double rt;
	private Double ccs;
	private String collisionEnergy;
	private String comment;
	private int numPeaks;
	private Collection<MsPoint>spectrum;
	private Integer carbonNumber;

	public MSDialMSMSRecord(String name, String adduct, String abbreviation) {

		super();
		this.name = name;
		this.adduct = adduct;
		this.abbreviation = abbreviation;
		spectrum = new ArrayList<MsPoint>();
	}

	public MSDialMSMSRecord() {

		name = null;
		formula = null;
		adduct = null;
		abbreviation = null;
		exactMass = null;
		precursorMz = null;
		comment = null;;
		numPeaks = 0;
		collisionEnergy = null;
		ccs = null;
		carbonNumber = null;
		spectrum = new ArrayList<MsPoint>();
	}
	
	public MSDialMSMSRecord(MSDialMSMSRecord record) {
		
		this.name = record.getName();
		this.formula = record.getFormula();
		this.smiles = record.getSmiles();
		this.inchiKey = record.getInchiKey();
		this.polarity = record.getPolarity();
		this.adduct = record.getAdduct();
		this.abbreviation = record.getAbbreviation();
		this.exactMass = record.getExactMass();
		this.precursorMz = record.getPrecursorMz();
		this.rt = record.getRt();
		this.ccs = record.getCcs();
		this.collisionEnergy = record.getCollisionEnergy();
		this.comment = record.getComment();
		this.numPeaks = record.getNumPeaks();
		this.spectrum = record.getSpectrum();
		this.carbonNumber = record.getCarbonNumber();
	}

	public void resetRecord() {

		this.name = null;
		this.formula = null;
		this.smiles = null;
		this.inchiKey = null;
		this.polarity = null;
		this.adduct = null;
		this.abbreviation = null;
		this.exactMass = null;
		this.precursorMz = null;
		this.rt = null;
		this.ccs = null;
		this.collisionEnergy = null;
		this.comment = null;
		this.carbonNumber = null;
		numPeaks = 0;
		spectrum = new ArrayList<MsPoint>();
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the adduct
	 */
	public String getAdduct() {
		return adduct;
	}

	/**
	 * @return the abbreviation
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @return the exactMass
	 */
	public Double getExactMass() {
		return exactMass;
	}

	/**
	 * @return the precursorMz
	 */
	public Double getPrecursorMz() {
		return precursorMz;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return the numPeaks
	 */
	public int getNumPeaks() {
		return numPeaks;
	}

	/**
	 * @return the spectrum
	 */
	public Collection<MsPoint> getSpectrum() {
		return spectrum;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param adduct the adduct to set
	 */
	public void setAdduct(String adduct) {
		this.adduct = adduct;
	}

	/**
	 * @param abbreviation the abbreviation to set
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	/**
	 * @param exactMass the exactMass to set
	 */
	public void setExactMass(Double exactMass) {
		this.exactMass = exactMass;
	}

	/**
	 * @param precursorMz the precursorMz to set
	 */
	public void setPrecursorMz(Double precursorMz) {
		this.precursorMz = precursorMz;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @param numPeaks the numPeaks to set
	 */
	public void setNumPeaks(int numPeaks) {
		this.numPeaks = numPeaks;
	}

	/**
	 * @param spectrum the spectrum to set
	 */
	public void setSpectrum(Collection<MsPoint> spectrum) {
		this.spectrum = spectrum;
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public String getInchiKey() {
		return inchiKey;
	}

	public void setInchiKey(String inchiKey) {
		this.inchiKey = inchiKey;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

	public Double getRt() {
		return rt;
	}

	public void setRt(Double rt) {
		this.rt = rt;
	}

	public Double getCcs() {
		return ccs;
	}

	public void setCcs(Double ccs) {
		this.ccs = ccs;
	}

	public String getCollisionEnergy() {
		return collisionEnergy;
	}

	public void setCollisionEnergy(String collisionEnergy) {
		this.collisionEnergy = collisionEnergy;
	}

	public Integer getCarbonNumber() {
		return carbonNumber;
	}

	public void setCarbonNumber(Integer carbonNumber) {
		this.carbonNumber = carbonNumber;
	}
}
