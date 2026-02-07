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

package edu.umich.med.mrc2.datoolbox.msalign.gapfill;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CummulativeAlignmentMetadataObject {

	private String primaryBatchId;
	//	Map<PrimaryFeatureID,Map<BatchID,FetureIDinBatch>>
	private Map<String,Map<String,String>>alignmentMap;
	
	public CummulativeAlignmentMetadataObject(String primaryBatchId) {
		super();
		this.primaryBatchId = primaryBatchId;
		alignmentMap = new TreeMap<>();
	}

	public String getPrimaryBatchId() {
		return primaryBatchId;
	}

	public Map<String, Map<String, String>> getAlignmentMap() {
		return alignmentMap;
	}
	
	public Set<String>getPrimaryFeatureSet(){
		return alignmentMap.keySet();
	}
	
	public Set<String>getMissingBatchesForFeature(String featureId){
		
		Map<String, String> batchFeatureMap = alignmentMap.get(featureId);
		if(batchFeatureMap == null)
			return new TreeSet<>();
		
		return batchFeatureMap.entrySet().stream().
			filter(e -> e.getValue().isBlank()).
			map(e -> e.getKey()).
			collect(Collectors.toCollection(TreeSet::new));
	}
	
	public Map<String, String>getMatchesForFeature(String featureId){
		
		Map<String, String> batchFeatureMap = alignmentMap.get(featureId);
		if(batchFeatureMap == null || batchFeatureMap.isEmpty())
			return new TreeMap<>();
		else
			return batchFeatureMap.entrySet().stream().
				filter(e -> e.getValue().isBlank()).collect(Collectors.toMap(
		                Map.Entry::getKey, 
		                Map.Entry::getValue
		            ));
	}
}
