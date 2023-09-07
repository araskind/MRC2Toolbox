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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MzFrequencyObject {
	
	private MsFeatureCluster featureCluster;
	private Range mzRange;
	private double frequency;
	
	public MzFrequencyObject(MsFeatureCluster featureCluster) {
		super();
		this.featureCluster = featureCluster;
	}

	public Range getMzRange() {
		return mzRange;
	}

	public void setMzRange(Range mzRange) {
		this.mzRange = mzRange;
	}

	public double getFrequency() {
		return frequency;
	}
	
	public double getRtRSD() {
		
		if(featureCluster.getFeatures().size() < 2)
			return 0.0;
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		featureCluster.getFeatures().stream().
			forEach(f -> ds.addValue(f.getRetentionTime()));

				
		return ds.getStandardDeviation() / ds.getMean();
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public MsFeatureCluster getFeatureCluster() {
		return featureCluster;
	}
	
	public int getFeatureCount() {
		return featureCluster.getFeatures().size();
	}
	
	public Range getRTRange() {
		return featureCluster.getRtRange();
	}
}
