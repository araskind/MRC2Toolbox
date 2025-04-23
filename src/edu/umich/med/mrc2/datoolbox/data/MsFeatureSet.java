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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

public class MsFeatureSet extends FeatureSet implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7634310759074576227L;
	private Collection<MsFeature>features;
	private Set<String>featureIdSet;

	public MsFeatureSet(String name) {

		super(name);
		features = new HashSet<MsFeature>();
	}

	public MsFeatureSet(String name, Collection<MsFeature> features2) {

		super(name);
		features = new HashSet<MsFeature>();
		features.addAll(features2);
	}

	public void addFeature(MsFeature newFeature) {

		features.add(newFeature);	
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	public void addFeatures(Collection<MsFeature> newFeatures) {
		
		features.addAll(newFeatures);
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	public boolean containsFeature(MsFeature feature) {
		return features.contains(feature);
	}

	public Collection<MsFeature> getFeatures() {
		return features;
	}
	
	public void setFeatures(Collection<MsFeature>newFeatures) {
		features.clear();
		features.addAll(newFeatures);
	}

	public void removeFeature(MsFeature featureToRemove) {
		
		features.remove(featureToRemove);
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	public void removeFeatures(Collection<MsFeature> featuresToRemove) {

		features.removeAll(featuresToRemove);
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}	

	public MsFeatureSet(Element featureSetElement) {
		
		super(featureSetElement);
		features = new HashSet<MsFeature>();
		featureIdSet = new HashSet<String>();
		Element featureListElement = 
				featureSetElement.getChild(CommonFields.FeatureList.name());
		if(featureListElement != null && !featureListElement.getText().isBlank()) {
			
			String[]idArray = featureListElement.getText().split(",");
			if(idArray.length > 0)
				featureIdSet.addAll(Arrays.asList(idArray));
		}
	}
	
	@Override
	public Element getXmlElement() {
		
		Element msFeatureSetElement = super.getXmlElement();
		msFeatureSetElement.setName(ObjectNames.MsFeatureSet.name());
		Element featureListElement = 
				new Element(CommonFields.FeatureList.name());
		
		Set<String>idSet = features.stream().
				map(f -> f.getId()).collect(Collectors.toSet());
		featureListElement.setText(StringUtils.join(idSet, ","));		
		msFeatureSetElement.addContent(featureListElement);
		return msFeatureSetElement;
	}

	public Set<String> getFeatureIdSet() {
		return featureIdSet;
	}
}
