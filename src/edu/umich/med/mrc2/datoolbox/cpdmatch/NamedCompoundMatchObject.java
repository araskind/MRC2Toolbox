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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math4.legacy.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;

public class NamedCompoundMatchObject {

	private int groupId;
	private double mz;
	private double rt;
	private String compoundName;
	private String unknownName;
	private Map<Integer,String>batchFeatureMap;
	private Map<Integer,String>batchAdductMap;
	private boolean autoPick;
	private List<Double>mzValues;
	private List<Double>rtValues;
	private List<Double>medianAreaValues;
	private int missingBatchesCount;
	
	public NamedCompoundMatchObject(int groupId, String compoundName, Set<Integer>batchNumbers ) {
		super();
		this.groupId = groupId;
		this.compoundName = compoundName;
		batchFeatureMap = new TreeMap<>();
		for(Integer batch : batchNumbers)
			batchFeatureMap.put(batch, null);
		
		batchAdductMap = new TreeMap<>();
		for(Integer batch : batchNumbers)
			batchAdductMap.put(batch, null);
		
		mzValues = new ArrayList<>();
		rtValues = new ArrayList<>();
		medianAreaValues = new ArrayList<>();
	}
	
	public NamedCompoundMatchObject(int groupId) {
		super();
		this.groupId = groupId;
		batchFeatureMap = new TreeMap<>();
		batchAdductMap = new TreeMap<>();
		mzValues = new ArrayList<>();
		rtValues = new ArrayList<>();
		medianAreaValues = new ArrayList<>();
	}
	
	public void finalizeObjectParameters() {

		DescriptiveStatistics mzStats = 
				new DescriptiveStatistics(mzValues.stream().mapToDouble(d -> d).toArray());
		mz = mzStats.getPercentile(50.0d);

		DescriptiveStatistics rtStats = 
				new DescriptiveStatistics(rtValues.stream().mapToDouble(d -> d).toArray());
		rt = rtStats.getPercentile(50.0d);
		
		unknownName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() 
				+ DefaultFormatStore.getDefaultMZformat().format(mz) + "_"
				+ DefaultFormatStore.getDefaultRTformat().format(rt);	
	}

	public boolean isAutoPick() {
		return autoPick;
	}

	public void setAutoPick(boolean autoPick) {
		this.autoPick = autoPick;
	}

	public int getGroupId() {
		return groupId;
	}

	public String getCompoundName() {
		return compoundName;
	}

	public Map<Integer, String> getBatchFeatureMap() {
		return batchFeatureMap;
	}

	public Map<Integer, String> getBatchAdductMap() {
		return batchAdductMap;
	}

	public String getUnknownName() {
		return unknownName;
	}

	public void setUnknownName(String unknownName) {
		this.unknownName = unknownName;
	}

	public List<Double> getMzValues() {
		return mzValues;
	}

	public List<Double> getRtValues() {
		return rtValues;
	}

	public List<Double> getMedianAreaValues() {
		return medianAreaValues;
	}

	public void setCompoundName(String compoundName) {
		this.compoundName = compoundName;
	}

	public int getMissingBatchesCount() {
		return missingBatchesCount;
	}

	public void setMissingBatchesCount(int missingBatchesCount) {
		this.missingBatchesCount = missingBatchesCount;
	}
	
	public Set<Integer>getMissingBatches(){
		return batchFeatureMap.entrySet().stream().
				filter(e -> e.getValue() == null).
				mapToInt(Entry::getKey).boxed().
				collect(Collectors.toSet());
	}

	public double getMz() {
		return mz;
	}

	public double getRt() {
		return rt;
	}
	
	public boolean hasDataForBatches(Collection<Integer>batchNumbers) {
		
		for(int i : batchNumbers) {
			
			if(batchFeatureMap.get(i) == null)
				return false;
		}
		return true;
	}
}













