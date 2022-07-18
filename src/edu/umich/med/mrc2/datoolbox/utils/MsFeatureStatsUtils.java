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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;

public class MsFeatureStatsUtils {
	
	private static final DescriptiveStatistics descStats = new DescriptiveStatistics();

	public static double getMedianRtForFeatureCollection(Collection<MsFeatureInfoBundle>features) {
		
		if(features.isEmpty())
			return 0.0d;
		
		if(features.size() == 1)
			return features.iterator().next().getRetentionTime();
		
		descStats.clear();
		for(MsFeatureInfoBundle b : features)
			descStats.addValue(b.getRetentionTime());
		
		return descStats.getPercentile(50.0d);
	}
	
	public static double getMedianParentIonMzForFeatureCollection(Collection<MsFeatureInfoBundle>features) {
		
		if(features.isEmpty())
			return 0.0d;
		
		if(features.size() == 1) {
			TandemMassSpectrum msms = features.iterator().next().getMsFeature().
					getSpectrum().getExperimentalTandemSpectrum();
			if(msms == null || msms.getParent() == null)
				return 0.0d;
			else
				return msms.getParent().getMz();
		}		
		descStats.clear();
		features.stream().map(b -> b.getMsFeature()).
			filter(f -> f.getSpectrum() != null).
			filter(f -> f.getSpectrum().getExperimentalTandemSpectrum() != null).
			filter(f -> f.getSpectrum().getExperimentalTandemSpectrum().getParent() != null).
			forEach(f -> descStats.addValue(f.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz()));		
		return descStats.getPercentile(50.0d);
	}
	
	public static double getMedianMSMSAreaForFeatureCollection(Collection<MsFeatureInfoBundle>features) {
		
		if(features.isEmpty())
			return 0.0d;
		
		if(features.size() == 1) {
			TandemMassSpectrum msms = features.iterator().next().getMsFeature().
					getSpectrum().getExperimentalTandemSpectrum();
			if(msms == null || msms.getParent() == null)
				return 0.0d;
			else
				return msms.getParent().getMz();
		}		
		descStats.clear();
		features.stream().map(b -> b.getMsFeature()).
			filter(f -> f.getSpectrum() != null).
			filter(f -> f.getSpectrum().getExperimentalTandemSpectrum() != null).
			forEach(f -> descStats.addValue(f.getSpectrum().getExperimentalTandemSpectrum().getTotalIntensity()));		
		return descStats.getPercentile(50.0d);
	}
}
