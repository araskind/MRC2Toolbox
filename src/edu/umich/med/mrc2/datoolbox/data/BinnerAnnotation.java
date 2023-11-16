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
	private double rmd;
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

	public String getAnnotation() {
		return annotation;
	}
	
	public String getCleanAnnotation() {
		return replaceMolIonNumber(annotation);
	}

	public String getDerivations() {
		return derivations;
	}
	
	public String getCleanDerivations() {
		return replaceMolIonNumber(derivations);
	}

	public String getIsotopes() {
		return isotopes;
	}

	public double getMassError() {
		return massError;
	}

	public int getMolIonNumber() {
		return molIonNumber;
	}

	public String getChargeCarrier() {
		return chargeCarrier;
	}

	public String getAdditionalAdducts() {
		return additionalAdducts;
	}
	
	public String getCleanAdditionalAdducts() {
		return replaceMolIonNumber(additionalAdducts);
	}

	public int getBinNumber() {
		return binNumber;
	}

	public int getCorrClusterNumber() {
		return corrClusterNumber;
	}

	public int getRebinSubclusterNumber() {
		return rebinSubclusterNumber;
	}

	public int getRtSubclusterNumber() {
		return rtSubclusterNumber;
	}

	public void setDerivations(String derivations) {
		this.derivations = derivations;
	}

	public void setIsotopes(String isotopes) {
		this.isotopes = isotopes;
	}

	public void setMassError(double massError) {
		this.massError = massError;
	}


	public void setMolIonNumber(int molIonNumber) {
		this.molIonNumber = molIonNumber;
	}

	public void setChargeCarrier(String chargeCarrier) {
		this.chargeCarrier = chargeCarrier;
	}

	public void setAdditionalAdducts(String additionalAdducts) {
		this.additionalAdducts = additionalAdducts;
	}

	public void setBinNumber(int binNumber) {
		this.binNumber = binNumber;
	}

	public void setCorrClusterNumber(int corrClusterNumber) {
		this.corrClusterNumber = corrClusterNumber;
	}

	public void setRebinSubclusterNumber(int rebinSubclusterNumber) {
		this.rebinSubclusterNumber = rebinSubclusterNumber;
	}

	public void setRtSubclusterNumber(int rtSubclusterNumber) {
		this.rtSubclusterNumber = rtSubclusterNumber;
	}

	public double getKmd() {
		return kmd;
	}

	public void setKmd(double kmd) {
		this.kmd = kmd;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public double getBinnerMz() {
		return binnerMz;
	}

	public double getBinnerRt() {
		return binnerRt;
	}

	public void setBinnerMz(double binnerMz) {
		this.binnerMz = binnerMz;
	}

	public void setBinnerRt(double binnerRt) {
		this.binnerRt = binnerRt;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	
	public String getFurtherAnnotations() {
		return furtherAnnotations;
	}
	
	public String getCleanFurtherAnnotations() {
		return replaceMolIonNumber(furtherAnnotations);
	}

	public String getAditionalIsotopes() {
		return aditionalIsotopes;
	}

	public void setFurtherAnnotations(String extraAnnotations) {
		this.furtherAnnotations = extraAnnotations;
	}

	public void setAditionalIsotopes(String extraIsotopes) {
		this.aditionalIsotopes = extraIsotopes;
	}

	public String getAdditionalGroupAnnotations() {
		return additionalGroupAnnotations;
	}
	
	public String getCleanAdditionalGroupAnnotations() {
		return replaceMolIonNumber(additionalGroupAnnotations);
	}

	public void setAdditionalGroupAnnotations(String additionalGroupAnnotations) {
		this.additionalGroupAnnotations = additionalGroupAnnotations;
	}

	public double getRmd() {
		return rmd;
	}

	public void setRmd(double rmd) {
		this.rmd = rmd;
	}
	
	private String replaceMolIonNumber(String input) {
		
		String moinum = Integer.toString(molIonNumber);
		return input.replaceAll(moinum, "");
	}
	
	public String getNameWithAnnotation() {
		return featureName + " " + getCleanAnnotation();
	}
	
	@Override
	public int compareTo(BinnerAnnotation o) {

		String thisQualifiedName = featureName + annotation;
		String otherQualifiedName = o.getFeatureName() + o.getAnnotation();
		return thisQualifiedName.compareTo(otherQualifiedName);
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
}








