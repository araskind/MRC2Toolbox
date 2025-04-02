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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.project.store.BinnerAnnotationClusterFields;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class BinnerAnnotationCluster {
	
	private String id;
	private int molIonNumber;
	private Set<BinnerAnnotation>annotations;
	private BinnerAnnotation primaryFeatureAnnotation;
		
	public BinnerAnnotationCluster(String id, int molIonNumber) {
		super();
		this.id = id;
		this.molIonNumber = molIonNumber;
		annotations = new TreeSet<BinnerAnnotation>();
	}

	public BinnerAnnotationCluster(BinnerAnnotation firstAnnotation) {
		super();
		this.id = DataPrefix.BINNER_ANNOTATIONS_CLUSTER.getName() + 
				UUID.randomUUID().toString().substring(0, 7);
		annotations = new TreeSet<BinnerAnnotation>();
		annotations.add(firstAnnotation);
		molIonNumber = firstAnnotation.getMolIonNumber();
		if(firstAnnotation.isPrimary())
			primaryFeatureAnnotation = firstAnnotation;
	}

	public boolean addAnnotation(BinnerAnnotation newAnnotation) {
		
		if(newAnnotation.getMolIonNumber() != molIonNumber)
			return false;
		
		annotations.add(newAnnotation);
		if(newAnnotation.isPrimary())
			primaryFeatureAnnotation = newAnnotation;
		
		return true;
	}
	
	public boolean addUniqueAnnotation(
			BinnerAnnotation newAnnotation,
			double mergeMzWindow,
			MassErrorType masErrorType, 
			double mergeRtWindow) {
		
		if(newAnnotation.getMolIonNumber() != molIonNumber)
			return false;
		
		Range mzRange = MsUtils.createMassRange(
				newAnnotation.getBinnerMz(), mergeMzWindow, masErrorType);
		Range rtRange = new Range(
				newAnnotation.getBinnerRt() - mergeRtWindow, 
				newAnnotation.getBinnerRt() + mergeRtWindow);
		List<BinnerAnnotation> matches = annotations.stream().
			filter(a -> a.getCleanAnnotation().equals(newAnnotation.getCleanAnnotation())).
			filter(a -> mzRange.contains(a.getBinnerMz())).
			filter(a -> rtRange.contains(a.getBinnerRt())).
			collect(Collectors.toList());
		if(matches.isEmpty()) {
			
			annotations.add(newAnnotation);
			if(newAnnotation.isPrimary())
				primaryFeatureAnnotation = newAnnotation;
			
			return true;
		}
		else {
			matches.add(newAnnotation);
			BinnerAnnotation merged = mergeAnnotations(matches);
			annotations.removeAll(matches);			
			annotations.add(merged);
			if(merged.isPrimary())
				primaryFeatureAnnotation = merged;
			
			return true;
		}
	}
	
	private BinnerAnnotation mergeAnnotations(List<BinnerAnnotation> matches) {
		
		BinnerAnnotation first = matches.get(0);
		
		BinnerAnnotation merged = 
				new BinnerAnnotation(first.getFeatureName(), first.getAnnotation());
		merged.setMolIonNumber(first.getMolIonNumber());
		merged.setBinNumber(first.getBinNumber());		
		merged.setCorrClusterNumber(first.getCorrClusterNumber());
		merged.setRebinSubclusterNumber(first.getRebinSubclusterNumber());
		merged.setRtSubclusterNumber(first.getRtSubclusterNumber());
		
		double mzSum = 0.0d;
		double rtSum = 0.0d;
		double rmdSum = 0.0d;
		double massErrSum = 0.0d;
		
		TreeSet<String>additionalGroupAnnotationsSet = new TreeSet<String>();
		TreeSet<String>furtherAnnotationsSet = new TreeSet<String>();
		TreeSet<String>derivationsSet = new TreeSet<String>();
		TreeSet<String>isotopesSet = new TreeSet<String>();
		TreeSet<String>additionalIsotopesSet = new TreeSet<String>();
		TreeSet<String>chargeCarrierSet = new TreeSet<String>();
		TreeSet<String>additionalAdductsSet = new TreeSet<String>();
		
		for(BinnerAnnotation ba : matches) {
						
			CollectionUtils.addIgnoreNull(
					additionalGroupAnnotationsSet, ba.getAdditionalGroupAnnotations());			
			CollectionUtils.addIgnoreNull(
					furtherAnnotationsSet, ba.getFurtherAnnotations());			
			CollectionUtils.addIgnoreNull(derivationsSet, ba.getDerivations());			
			CollectionUtils.addIgnoreNull(isotopesSet, ba.getIsotopes());		
			CollectionUtils.addIgnoreNull(
					additionalIsotopesSet, ba.getAdditionalIsotopes());			
			CollectionUtils.addIgnoreNull(chargeCarrierSet, ba.getChargeCarrier());
			CollectionUtils.addIgnoreNull(
					additionalAdductsSet, ba.getAdditionalAdducts());
			
			mzSum += ba.getBinnerMz();
			rtSum += ba.getBinnerRt();
			rmdSum += ba.getRmd();
			massErrSum += ba.getMassError();
			
			if(ba.isPrimary())
				merged.setPrimary(true);
		}
		double div = (double)matches.size();
		merged.setBinnerMz(mzSum / div);
		merged.setBinnerRt(rtSum / div);
		merged.setRmd(rmdSum / div);
		merged.setMassError(massErrSum / div);

		if(!additionalGroupAnnotationsSet.isEmpty())
			merged.setAdditionalGroupAnnotations(
					StringUtils.join(additionalGroupAnnotationsSet, " "));
		
		if(!furtherAnnotationsSet.isEmpty())
			merged.setFurtherAnnotations(
					StringUtils.join(furtherAnnotationsSet, " "));
		
		if(!derivationsSet.isEmpty())
			merged.setDerivations(
					StringUtils.join(derivationsSet, " "));
				
		if(!isotopesSet.isEmpty())
			merged.setIsotopes(
					StringUtils.join(isotopesSet, " "));
		
		if(!additionalIsotopesSet.isEmpty())
			merged.setAdditionalIsotopes(
					StringUtils.join(additionalIsotopesSet, " "));
		
		if(!chargeCarrierSet.isEmpty())
			merged.setChargeCarrier(
					StringUtils.join(chargeCarrierSet, " "));
				
		if(!additionalAdductsSet.isEmpty())
			merged.setAdditionalAdducts(
					StringUtils.join(additionalAdductsSet, " "));
				
		return merged;
	}

	public int getMolIonNumber() {
		return molIonNumber;
	}

	public Set<BinnerAnnotation> getAnnotations() {
		return annotations;
	}

	public BinnerAnnotation getPrimaryFeatureAnnotation() {
		
		if(primaryFeatureAnnotation == null) {
			primaryFeatureAnnotation = 
				annotations.stream().
				filter(a -> a.isPrimary()).
				findFirst().orElse(null);
		}
		return primaryFeatureAnnotation;
	}
	
	public Map<Double,Double>getMZvalues(){
		
		Map<Double,Double>mzrtMap = new TreeMap<Double,Double>();
		annotations.stream().forEach(
				a -> mzrtMap.put(a.getBinnerMz(), a.getBinnerMz()));
		return mzrtMap;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!BinnerAnnotationCluster.class.isAssignableFrom(obj.getClass()))
            return false;

        final BinnerAnnotationCluster other = (BinnerAnnotationCluster) obj;


        if(this.id != other.getId())
        	return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
	public Element getXmlElement() {

		Element binnerAnnotationClusterElement = 
				new Element(ObjectNames.BinnerAnnotationCluster.name());
		binnerAnnotationClusterElement.setAttribute(
				CommonFields.Id.name(), id);	
		binnerAnnotationClusterElement.setAttribute(
				BinnerAnnotationClusterFields.MolIonNumber.name(), Integer.toString(molIonNumber));	
		Element primaryAnnotationElement = 
				new Element(BinnerAnnotationClusterFields.PrimaryAnnotation.name());		
		if(primaryFeatureAnnotation != null)
			primaryAnnotationElement.addContent(primaryFeatureAnnotation.getXmlElement());
		
		binnerAnnotationClusterElement.addContent(primaryAnnotationElement);
		
		Element binnerAnnotationsElement = 
				new Element(BinnerAnnotationClusterFields.Annotations.name());
		for(BinnerAnnotation ba : annotations) 			
			binnerAnnotationsElement.addContent(ba.getXmlElement());

		binnerAnnotationClusterElement.addContent(binnerAnnotationsElement);
		
		return binnerAnnotationClusterElement;
	}
	
	public BinnerAnnotationCluster(Element clusterElement) {
		
		
		id = clusterElement.getAttributeValue(CommonFields.Id.name());
		molIonNumber = Integer.parseInt(
				clusterElement.getAttributeValue(BinnerAnnotationClusterFields.MolIonNumber.name()));
		annotations = new TreeSet<BinnerAnnotation>();
		
		List<Element> annotationListElements = 
				clusterElement.getChild(
						BinnerAnnotationClusterFields.Annotations.name()).getChildren();
		if(annotationListElements.size() > 0) {
			
			for(Element annotationElement : annotationListElements) {
				
				BinnerAnnotation annotation = 
						new BinnerAnnotation(annotationElement);
				annotations.add(annotation);
			}
		}
		List<Element> primaryListElements = 
				clusterElement.getChild(
						BinnerAnnotationClusterFields.PrimaryAnnotation.name()).getChildren();
		if(primaryListElements.size() > 0) {
			
			for(Element annotationElement : primaryListElements) {
				
				BinnerAnnotation annotation = 
						new BinnerAnnotation(annotationElement);
				primaryFeatureAnnotation = annotation;
			}
		}
	}
	
	public String toString() {
		return getPrimaryFeatureAnnotation().getNameWithAnnotation();
	}
	
	public String getAllAnnotationsAsString() {
		
		Collection<String>allAnnotations = new ArrayList<String>();
		annotations.stream().
			forEach(a -> allAnnotations.add(a.getCleanAnnotation()));
		
		return StringUtils.join(allAnnotations, "; ");
	}
	
	public BinnerAnnotation getBinnerAnnotationById(String bacId) {
		
		return annotations.stream().
				filter(a -> a.getId().equals(bacId)).
				findFirst().orElse(null);
	}
}









