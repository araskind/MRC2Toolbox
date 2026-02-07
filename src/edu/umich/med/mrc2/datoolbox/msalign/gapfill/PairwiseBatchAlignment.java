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

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class PairwiseBatchAlignment implements Comparable<PairwiseBatchAlignment>{

	private File dataSource;
	private String firstExperimentId;
	private String firstBatchId;
	private String secondExperimentId;
	private String secondBatchId;
	private Map<String,String>featureAlignmentMap;
	private Map<String,String>reverseFeatureAlignmentMap;
	
	public PairwiseBatchAlignment(
			File dataSource, 
			String firstExperimentId, 
			String firstBatchId,
			String secondExperimentId, 
			String secondBatchId) {
		super();
		this.dataSource = dataSource;
		this.firstExperimentId = firstExperimentId;
		this.firstBatchId = firstBatchId;
		this.secondExperimentId = secondExperimentId;
		this.secondBatchId = secondBatchId;
		featureAlignmentMap = new TreeMap<>();
		reverseFeatureAlignmentMap = new TreeMap<>();
	}

	public File getDataSource() {
		return dataSource;
	}

	public String getFirstExperimentId() {
		return firstExperimentId;
	}

	public String getFirstBatchId() {
		return firstBatchId;
	}

	public String getSecondExperimentId() {
		return secondExperimentId;
	}

	public String getSecondBatchId() {
		return secondBatchId;
	}
	
	public String getFirstCompositeId() {
		return firstExperimentId + "." + firstBatchId;
	}

	public String getSecondCompositeId() {
		return secondExperimentId + "." + secondBatchId;
	}

	public Map<String, String> getFeatureAlignmentMap() {
		return featureAlignmentMap;
	}
	
	public String getForwardMatch(String featureId) {
		return featureAlignmentMap.get(featureId);
	}
	
	public String getReverseMatch(String featureId) {
		return reverseFeatureAlignmentMap.get(featureId);
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!PairwiseBatchAlignment.class.isAssignableFrom(obj.getClass()))
            return false;

        final PairwiseBatchAlignment other = (PairwiseBatchAlignment) obj;
        if(!dataSource.equals(other.getDataSource()))
        	return false;

        return true;
    }

    @Override
    public int hashCode() {
        return dataSource.hashCode();
    }

	@Override
	public int compareTo(PairwiseBatchAlignment o) {
		return this.dataSource.compareTo(o.getDataSource());
	}

	public Map<String, String> getReverseFeatureAlignmentMap() {
		return reverseFeatureAlignmentMap;
	}
}
