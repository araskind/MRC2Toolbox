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
import java.util.Collection;
import java.util.HashSet;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;

public class MsFeatureSet extends FeatureSet implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7634310759074576227L;
	private Collection<MsFeature>features;


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
}
