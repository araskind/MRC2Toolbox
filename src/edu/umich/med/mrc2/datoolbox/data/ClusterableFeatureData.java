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

import org.apache.commons.math3.ml.clustering.Clusterable;

public class ClusterableFeatureData implements Clusterable {
	
	private MsFeature feature;
	private double[] values;

	public ClusterableFeatureData(MsFeature feature, double[] values) {
		super();
		this.feature = feature;
		this.values = values;
	}

	@Override
	public double[] getPoint() {
		return values;
	}

	public MsFeature getFeature() {
		return feature;
	}

}
