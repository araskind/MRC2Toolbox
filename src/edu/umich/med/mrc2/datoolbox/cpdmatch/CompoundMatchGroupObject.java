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

package edu.umich.med.mrc2.datoolbox.cpdmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math4.legacy.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.utils.StringProcessingUtils;


public class CompoundMatchGroupObject {

	private int groupId;
	private String feature;
	private String unknownName;
	private double mz;
	private double rt;
	private List<String>featureNames;
	private List<Double>mzValues;
	private List<Double>rtValues;
	private Map<String,Double>peakAreas;
	private boolean hasMzOutliers;	
	private boolean hasRTOutliers;
	
	private static final String nameSuffixPattern = "-[PN]-$";
	
	public CompoundMatchGroupObject(int groupId, Set<String>rawFileNames) {
		super();
		this.groupId = groupId;
		peakAreas = new TreeMap<>();
		featureNames = new ArrayList<>();
		mzValues = new ArrayList<>();
		rtValues = new ArrayList<>();
		rawFileNames.forEach(key -> peakAreas.put(key, null));
	}
	
	public void finalizeObjectParameters() {

		DescriptiveStatistics mzStats = new DescriptiveStatistics(mzValues.stream().mapToDouble(d -> d).toArray());
		mz = mzStats.getPercentile(50.0d);
		if(mzStats.getMax() - mzStats.getMin() > 0.05d)
			hasMzOutliers = true;
		else
			hasMzOutliers = false;

		DescriptiveStatistics rtStats = new DescriptiveStatistics(rtValues.stream().mapToDouble(d -> d).toArray());
		rt = rtStats.getPercentile(50.0d);
		if(rtStats.getMax() - rtStats.getMin() > 0.15d)
			hasRTOutliers = true;
		else
			hasRTOutliers = false;
		
		if(featureNames.get(0).startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
			feature = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() 
					+ DefaultFormatStore.getDefaultMZformat().format(mz) + "_"
					+ DefaultFormatStore.getDefaultRTformat().format(rt);
			unknownName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() 
					+ DefaultFormatStore.getDefaultMZformat().format(mz) + "_"
					+ DefaultFormatStore.getDefaultRTformat().format(rt);
		}
		else {
			feature = StringProcessingUtils.findLongestOverlap(featureNames).
				replaceAll(nameSuffixPattern, "").trim();
		}
	}
	
	//	Find outliers using interquartile range (IQR) method
	private boolean findRToutliersByIQR(DescriptiveStatistics rtStats) {
		
        double q1 = rtStats.getPercentile(25.0d); 
        double q3 = rtStats.getPercentile(75.0d);        
        double iqr = q3 - q1;
        double lowerBound = q1 - (1.5 * iqr);
        double upperBound = q3 + (1.5 * iqr);
        
        for (double val : rtStats.getValues()) {
            if (val < lowerBound || val > upperBound)
            	 return true;
        }		
		return false;
	}
	
	private boolean findRToutliersByZscore(DescriptiveStatistics rtStats, double threshold) {
		
		double mean = rtStats.getMean();
		double stdDev = rtStats.getStandardDeviation();
        for (double val : rtStats.getValues()) {
			double zScore = (val - mean) / stdDev;
			if (Math.abs(zScore) > threshold)
				return true;	
        }		
		return false;
	}

	public int getGroupId() {
		return groupId;
	}

	public String getFeature() {
		return feature;
	}

	public double getMz() {
		return mz;
	}

	public double getRt() {
		return rt;
	}

	public List<String> getFeatureNames() {
		return featureNames;
	}

	public List<Double> getMzValues() {
		return mzValues;
	}

	public List<Double> getRtValues() {
		return rtValues;
	}

	public Map<String, Double> getPeakAreas() {
		return peakAreas;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public boolean mzOutliersPresent() {
		return hasMzOutliers;
	}

	public boolean rtOutliersPresent() {
		return hasRTOutliers;
	}

	public String getUnknownName() {
		return unknownName;
	}

	public void setUnknownName(String unknownName) {
		this.unknownName = unknownName;
	}
}
