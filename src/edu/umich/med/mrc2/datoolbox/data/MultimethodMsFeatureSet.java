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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;

public class MultimethodMsFeatureSet extends FeatureSet implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7634310759074576227L;
	private HashMap<DataPipeline, Collection<MsFeature>> features;

	public MultimethodMsFeatureSet(String name) {

		super(name);
		features = new HashMap<DataPipeline, Collection<MsFeature>>();
	}

	public MultimethodMsFeatureSet(String name, 
			DataPipeline pipeline,
			Collection<MsFeature> features2) {

		super(name);
		features = new HashMap<DataPipeline, Collection<MsFeature>>();
		features.put(pipeline, new HashSet<MsFeature>(features2));
	}

	public void addFeature(MsFeature newFeature, DataPipeline pipeline) {
		
		if(!features.containsKey(pipeline))
			features.put(pipeline, new HashSet<MsFeature>());

		features.get(pipeline).add(newFeature);
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	public void addFeatures(Collection<MsFeature> newFeatures, DataPipeline pipeline) {

		if(!features.containsKey(pipeline))
			features.put(pipeline, new HashSet<MsFeature>());
		
		features.get(pipeline).addAll(newFeatures);
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	public void addFeatures(MsFeature[] newFeatures, DataPipeline pipeline) {		
		addFeatures(Arrays.asList(newFeatures), pipeline);
	}

	public boolean containsFeature(MsFeature feature) {
		return features.values().stream().flatMap(c -> c.stream()).
				filter(f -> f.equals(feature)).findFirst().isPresent();
	}

	public Map<DataPipeline, Collection<MsFeature>> getFeatures() {
		return features;
	}
	
	public Collection<MsFeature> getFeaturesForDataPipeline(DataPipeline pipeline) {
		return features.get(pipeline);
	}

	public void removeFeature(MsFeature featureToRemove) {
		
		features.values().stream().
			forEach(c -> c.remove(featureToRemove));
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	public void removeFeatures(Collection<MsFeature> featuresToRemove) {

		features.values().stream().
			forEach(c -> c.removeAll(featuresToRemove));
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}
}


