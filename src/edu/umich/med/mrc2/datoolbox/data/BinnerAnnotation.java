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
import java.util.Objects;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.project.store.BinnerAnnotationFields;

public class BinnerAnnotation implements Serializable, Comparable<BinnerAnnotation>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8415317491808995172L;
	
	private String id;
	private String featureName;
	private String annotation;
	
	private String chargeCarrier;
	private String additionalGroupAnnotations;
	private String furtherAnnotations;
	private String derivations;
	private String isotopes;
	private String additionalIsotopes;	
	private String additionalAdducts;
	
	private int molIonNumber;
	private int binNumber;
	private int corrClusterNumber;
	private int rebinSubclusterNumber;
	private int rtSubclusterNumber;
	
	private boolean isPrimary;
	
	private double massError;
	private double rmd;
	private double binnerMz;
	private double binnerRt;

	public BinnerAnnotation(String id, String featureName, String annotation) {
		super();
		this.id = id;
		this.featureName = featureName;
		this.annotation = annotation;
	}

	public BinnerAnnotation(String featureName, String annotation) {
		super();
		this.id = DataPrefix.BINNER_ANNOTATIONS_CLUSTER_COMPONENT.getName() + 
				UUID.randomUUID().toString().substring(0, 11);
		this.featureName = featureName;
		this.annotation = annotation;
	}

	public String toString() {
		return getCleanAnnotation();
	}

	public String getAnnotation() {
		return annotation;
	}
	
	public String getCleanAnnotation() {
		return replaceMolIonNumber(annotation).
					replace("(duplicate)", "").trim();
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
	
	public String getCleanIsotopes() {
		return replaceMolIonNumber(isotopes);
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

	public String getAdditionalIsotopes() {
		return additionalIsotopes;
	}
	
	public String getCleanAdditionalIsotopes() {
		return replaceMolIonNumber(additionalIsotopes);
	}

	public void setFurtherAnnotations(String extraAnnotations) {
		this.furtherAnnotations = extraAnnotations;
	}

	public void setAdditionalIsotopes(String extraIsotopes) {
		this.additionalIsotopes = extraIsotopes;
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
		
		if(input == null)
			return null;
		
		String moinum = Integer.toString(molIonNumber);
		return input.replaceAll(moinum, "");
	}
	
	public String getNameWithAnnotation() {
		return featureName + " " + getCleanAnnotation();
	}
	
	@Override
	public int compareTo(BinnerAnnotation o) {
		return getNameWithAnnotation().compareTo(o.getNameWithAnnotation());
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

        if(!this.id.equals(other.getId()))
        	return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
	public Element getXmlElement() {

		Element binnerAnnotationElement = 
				new Element(BinnerAnnotationFields.BinnerAnnotation.name());
		
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.BaId.name(), id);
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.FeatureName.name(), Objects.toString(featureName, ""));	
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.Annotation.name(), Objects.toString(annotation, ""));		
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.AdditionalGroupAnnotations.name(), Objects.toString(additionalGroupAnnotations, ""));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.FurtherAnnotations.name(), Objects.toString(furtherAnnotations, ""));	
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.Derivations.name(),  Objects.toString(derivations, ""));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.Isotopes.name(),  Objects.toString(isotopes, ""));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.AdditionalIsotopes.name(),  Objects.toString(additionalIsotopes, ""));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.MassError.name(), Double.toString(massError));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.Rmd.name(), Double.toString(rmd));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.MolIonNumber.name(), Integer.toString(molIonNumber));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.ChargeCarrier.name(),  Objects.toString(chargeCarrier, ""));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.AdditionalAdducts.name(),  Objects.toString(additionalAdducts, ""));		
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.BinNumber.name(), Integer.toString(binNumber));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.CorrClusterNumber.name(), Integer.toString(corrClusterNumber));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.RebinSubclusterNumber.name(), Integer.toString(rebinSubclusterNumber));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.RtSubclusterNumber.name(), Integer.toString(rtSubclusterNumber));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.RtSubclusterNumber.name(), Boolean.toString(isPrimary));		
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.BinnerMz.name(), Double.toString(binnerMz));
		binnerAnnotationElement.setAttribute(
				BinnerAnnotationFields.BinnerRt.name(), Double.toString(binnerRt));
		
		return binnerAnnotationElement;
	}
	
	public BinnerAnnotation(Element xmlElement) {
			
		this.id = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.BaId.name());
		this.featureName = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.FeatureName.name());
		this.annotation = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.Annotation.name());
		this.additionalGroupAnnotations = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.AdditionalGroupAnnotations.name());
		this.furtherAnnotations = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.FurtherAnnotations.name());
		this.derivations = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.Derivations.name());
		this.isotopes = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.Isotopes.name());
		this.additionalIsotopes = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.AdditionalIsotopes.name());
		this.massError = Double.parseDouble(
				xmlElement.getAttributeValue(BinnerAnnotationFields.MassError.name()));
		this.rmd = Double.parseDouble(
				xmlElement.getAttributeValue(BinnerAnnotationFields.Rmd.name()));
		this.molIonNumber = Integer.parseInt(
				xmlElement.getAttributeValue(BinnerAnnotationFields.MolIonNumber.name()));
		this.chargeCarrier = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.ChargeCarrier.name());
		this.additionalAdducts = 
				xmlElement.getAttributeValue(BinnerAnnotationFields.AdditionalAdducts.name());
		
		this.binNumber = Integer.parseInt(
				xmlElement.getAttributeValue(BinnerAnnotationFields.BinNumber.name()));	
		this.corrClusterNumber = Integer.parseInt(
				xmlElement.getAttributeValue(BinnerAnnotationFields.CorrClusterNumber.name()));
		this.rebinSubclusterNumber = Integer.parseInt(
				xmlElement.getAttributeValue(BinnerAnnotationFields.RebinSubclusterNumber.name()));
		this.rtSubclusterNumber = Integer.parseInt(
				xmlElement.getAttributeValue(BinnerAnnotationFields.RtSubclusterNumber.name()));
		this.isPrimary =  Boolean.parseBoolean(
				xmlElement.getAttributeValue(BinnerAnnotationFields.IsPrimary.name()));
		this.binnerMz = Double.parseDouble(
				xmlElement.getAttributeValue(BinnerAnnotationFields.BinnerMz.name()));
		this.binnerRt = Double.parseDouble(
				xmlElement.getAttributeValue(BinnerAnnotationFields.BinnerRt.name()));
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}








