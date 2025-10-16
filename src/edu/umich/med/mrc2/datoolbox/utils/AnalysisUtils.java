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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;

public class AnalysisUtils {

	public static MsFeature findTopScoringFeature(
			Collection<MsFeature> inputFeatures, 
			double areaWeight,
			double frequencyWeight) {

		double maxFrequency = 0;
		double maxArea = 0;
		boolean usePooled = true;
		double score = 0;

		// Normalize weights
		double perCent = (areaWeight + frequencyWeight) / 100;
		double areaWeightNorm = areaWeight / perCent;
		double frequencyWeightNorm = frequencyWeight / perCent;

		TreeMap<Double, MsFeature> scoreMap = new TreeMap<Double, MsFeature>();

		// Find max values
		for (MsFeature cf : inputFeatures) {

			if (cf.getStatsSummary().getPooledFrequency() > maxFrequency)
				maxFrequency = cf.getStatsSummary().getPooledFrequency();

			if (cf.getStatsSummary().getPooledMedian() > maxArea)
				maxArea = cf.getStatsSummary().getPooledMean();
		}
		// If no pooled samples
		if (maxArea == 0) {

			usePooled = false;

			for (MsFeature cf : inputFeatures) {

				if (cf.getStatsSummary().getSampleFrequency() > maxFrequency)
					maxFrequency = cf.getStatsSummary().getSampleFrequency();

				if (cf.getStatsSummary().getSampleMedian() > maxArea)
					maxArea = cf.getStatsSummary().getSampleMean();
			}
		}
		if (maxFrequency == 0) {

			maxFrequency = 0.01;
			maxArea = 0.01;
		}
		// Calculate scores
		for (MsFeature cf : inputFeatures) {

			if (usePooled) {

				score = cf.getStatsSummary().getPooledMean() / maxArea * areaWeightNorm
						+ cf.getStatsSummary().getPooledFrequency() / maxFrequency * frequencyWeightNorm;
			} else {
				score = cf.getStatsSummary().getSampleMean() / maxArea * areaWeightNorm
						+ cf.getStatsSummary().getSampleFrequency() / maxFrequency * frequencyWeightNorm;
			}
			scoreMap.put(score, cf);
			cf.setQualityScore(score);
		}
		return scoreMap.get(scoreMap.lastKey());
	}

	public static MsFeature findTopScoringFeature(Set<MsFeature> inputFeatures, double areaWeight,
			double frequencyWeight) {

		return AnalysisUtils.findTopScoringFeature(inputFeatures, areaWeight, frequencyWeight);
	}
}
