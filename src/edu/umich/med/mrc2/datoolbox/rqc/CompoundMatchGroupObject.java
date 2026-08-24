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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.utils.StringProcessingUtils;


public class CompoundMatchGroupObject {

	private int groupId;
	private String feature;
	private double mz;
	private double rt;
	private List<String>featureNames;
	private List<Double>mzValues;
	private List<Double>rtValues;
	private Map<String,Double>peakAreas;
	
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
		
		double[] mzArr = mzValues.stream().mapToDouble(d -> d).toArray();
		mz = StatUtils.percentile(mzArr, 50.0d);
		
		double[] rtArr = rtValues.stream().mapToDouble(d -> d).toArray();
		rt = StatUtils.percentile(rtArr, 50.0d);
		
		if(featureNames.get(0).startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
			feature = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() 
					+ DefaultFormatStore.getDefaultMZformat().format(mz) + "_"
					+ DefaultFormatStore.getDefaultRTformat().format(rt);
		}
		else {
			feature = StringProcessingUtils.findLongestOverlap(featureNames).
				replaceAll(nameSuffixPattern, "").trim();
		}
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
	
	
}
