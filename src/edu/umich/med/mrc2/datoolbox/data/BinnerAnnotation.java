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

public class BinnerAnnotation implements Serializable, Comparable<BinnerAnnotation>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8415317491808995172L;
	private String featureName;
	private String annotation;
	private String additionalGroupAnnotations;
	private String furtherAnnotations;
	private String derivations;
	private String isotopes;
	private String aditionalIsotopes;
	private double massError;
	private double kmd;
	private int molIonNumber;
	private String chargeCarrier;
	private String additionalAdducts;
	private int binNumber;
	private int corrClusterNumber;
	private int rebinSubclusterNumber;
	private int rtSubclusterNumber;
	private boolean isPrimary;
	private double binnerMz;
	private double binnerRt;

	public BinnerAnnotation(String featureName, String annotation) {
		super();
		this.featureName = featureName;
		this.annotation = annotation;
	}

	public String toString() {
		return annotation;
	}

	/**
	 * @return the annotation
	 */
	public String getAnnotation() {
		return annotation;
	}

	/**
	 * @return the derivations
	 */
	public String getDerivations() {
		return derivations;
	}

	/**
	 * @return the isotopes
	 */
	public String getIsotopes() {
		return isotopes;
	}

	/**
	 * @return the massError
	 */
	public double getMassError() {
		return massError;
	}

	/**
	 * @return the molIonNumber
	 */
	public int getMolIonNumber() {
		return molIonNumber;
	}

	/**
	 * @return the chargeCarrier
	 */
	public String getChargeCarrier() {
		return chargeCarrier;
	}

	/**
	 * @return the additionalAdducts
	 */
	public String getAdditionalAdducts() {
		return additionalAdducts;
	}

	/**
	 * @return the binNumber
	 */
	public int getBinNumber() {
		return binNumber;
	}

	/**
	 * @return the corrClusterNumber
	 */
	public int getCorrClusterNumber() {
		return corrClusterNumber;
	}

	/**
	 * @return the rebinSubclusterNumber
	 */
	public int getRebinSubclusterNumber() {
		return rebinSubclusterNumber;
	}

	/**
	 * @return the rtSubclusterNumber
	 */
	public int getRtSubclusterNumber() {
		return rtSubclusterNumber;
	}

	/**
	 * @param derivations the derivations to set
	 */
	public void setDerivations(String derivations) {
		this.derivations = derivations;
	}

	/**
	 * @param isotopes the isotopes to set
	 */
	public void setIsotopes(String isotopes) {
		this.isotopes = isotopes;
	}

	/**
	 * @param massError the massError to set
	 */
	public void setMassError(double massError) {
		this.massError = massError;
	}

	/**
	 * @param molIonNumber the molIonNumber to set
	 */
	public void setMolIonNumber(int molIonNumber) {
		this.molIonNumber = molIonNumber;
	}

	/**
	 * @param chargeCarrier the chargeCarrier to set
	 */
	public void setChargeCarrier(String chargeCarrier) {
		this.chargeCarrier = chargeCarrier;
	}

	/**
	 * @param additionalAdducts the additionalAdducts to set
	 */
	public void setAdditionalAdducts(String additionalAdducts) {
		this.additionalAdducts = additionalAdducts;
	}

	/**
	 * @param binNumber the binNumber to set
	 */
	public void setBinNumber(int binNumber) {
		this.binNumber = binNumber;
	}

	/**
	 * @param corrClusterNumber the corrClusterNumber to set
	 */
	public void setCorrClusterNumber(int corrClusterNumber) {
		this.corrClusterNumber = corrClusterNumber;
	}

	/**
	 * @param rebinSubclusterNumber the rebinSubclusterNumber to set
	 */
	public void setRebinSubclusterNumber(int rebinSubclusterNumber) {
		this.rebinSubclusterNumber = rebinSubclusterNumber;
	}

	/**
	 * @param rtSubclusterNumber the rtSubclusterNumber to set
	 */
	public void setRtSubclusterNumber(int rtSubclusterNumber) {
		this.rtSubclusterNumber = rtSubclusterNumber;
	}

	/**
	 * @return the kmd
	 */
	public double getKmd() {
		return kmd;
	}

	/**
	 * @param kmd the kmd to set
	 */
	public void setKmd(double kmd) {
		this.kmd = kmd;
	}

	/**
	 * @return the isPrimary
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * @param isPrimary the isPrimary to set
	 */
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	/**
	 * @return the binnerMz
	 */
	public double getBinnerMz() {
		return binnerMz;
	}

	/**
	 * @return the binnerRt
	 */
	public double getBinnerRt() {
		return binnerRt;
	}

	/**
	 * @param binnerMz the binnerMz to set
	 */
	public void setBinnerMz(double binnerMz) {
		this.binnerMz = binnerMz;
	}

	/**
	 * @param binnerRt the binnerRt to set
	 */
	public void setBinnerRt(double binnerRt) {
		this.binnerRt = binnerRt;
	}

	@Override
	public int compareTo(BinnerAnnotation o) {

		String thisQualifiedName = featureName + annotation;
		String otherQualifiedName = o.getFeatureName() + o.getAnnotation();
		return thisQualifiedName.compareTo(otherQualifiedName);
	}

	/**
	 * @return the featureName
	 */
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * @param featureName the featureName to set
	 */
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!BinnerAnnotation.class.isAssignableFrom(obj.getClass()))
            return false;

        final BinnerAnnotation other = (BinnerAnnotation) obj;

        if ((this.featureName == null) ? (other.getFeatureName() != null) : !this.featureName.equals(other.getFeatureName()))
            return false;

        if ((this.annotation == null) ? (other.getAnnotation() != null) : !this.annotation.equals(other.getAnnotation()))
            return false;

        if(this.binnerMz != other.getBinnerMz())
        	return false;

        if(this.binnerRt != other.getBinnerRt())
        	return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        String mz = Double.toString(this.binnerMz);
        String rt = Double.toString(this.binnerRt);
        hash = 53 * hash
        		+ (this.featureName != null ? this.featureName.hashCode() : 0)
        		+ (this.annotation != null ? this.annotation.hashCode() : 0)
        		+ (mz != null ? mz.hashCode() : 0)
        		+ (rt != null ? rt.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the extraAnnotations
	 */
	public String getFurtherAnnotations() {
		return furtherAnnotations;
	}

	/**
	 * @return the extraIsotopes
	 */
	public String getAditionalIsotopes() {
		return aditionalIsotopes;
	}

	/**
	 * @param extraAnnotations the extraAnnotations to set
	 */
	public void setFurtherAnnotations(String extraAnnotations) {
		this.furtherAnnotations = extraAnnotations;
	}

	/**
	 * @param extraIsotopes the extraIsotopes to set
	 */
	public void setAditionalIsotopes(String extraIsotopes) {
		this.aditionalIsotopes = extraIsotopes;
	}

	/**
	 * @return the additionalGroupAnnotations
	 */
	public String getAdditionalGroupAnnotations() {
		return additionalGroupAnnotations;
	}

	/**
	 * @param additionalGroupAnnotations the additionalGroupAnnotations to set
	 */
	public void setAdditionalGroupAnnotations(String additionalGroupAnnotations) {
		this.additionalGroupAnnotations = additionalGroupAnnotations;
	}
}








