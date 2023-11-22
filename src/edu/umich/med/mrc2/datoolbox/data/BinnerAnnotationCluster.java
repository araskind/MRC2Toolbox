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

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.project.store.BinnerAnnotationClusterFields;

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
				new Element(BinnerAnnotationClusterFields.BinnerAnnotationCluster.name());
		binnerAnnotationClusterElement.setAttribute(
				BinnerAnnotationClusterFields.BacId.name(), id);	
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
		
		
		id = clusterElement.getAttributeValue(BinnerAnnotationClusterFields.BacId.name());
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
		return primaryFeatureAnnotation.getNameWithAnnotation();
	}
	
	public String getAllAnnotationsAsString() {
		
		Collection<String>allAnnotations = new ArrayList<String>();
		annotations.stream().
			forEach(a -> allAnnotations.add(a.getCleanAnnotation()));
		
		return StringUtils.join(allAnnotations, "; ");
	}
}









