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

package edu.umich.med.mrc2.datoolbox.gui.integration.mcr;

import java.io.File;

public class MetabCombinerFileInputObject implements Comparable<MetabCombinerFileInputObject>{

	private File dataFile;
	private String experimentId;
	private String batchId;
	
	public MetabCombinerFileInputObject(File dataFile, String experimentId, String batchId) {
		super();
		this.dataFile = dataFile;
		this.experimentId = experimentId;
		this.batchId = batchId;
	}

	public File getDataFile() {
		return dataFile;
	}

	public String getExperimentId() {
		return experimentId;
	}

	public String getBatchId() {
		return batchId;
	}
	
	public boolean isDefined() {
		
		if(dataFile == null || !dataFile.exists())
			return false;
		
		if(experimentId == null || experimentId.isBlank())
			return false;
		
		if(batchId == null || batchId.isBlank())
			return false;
		
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (!MetabCombinerFileInputObject.class.isAssignableFrom(obj.getClass()))
			return false;
		
		final MetabCombinerFileInputObject other = (MetabCombinerFileInputObject) obj;
		
		if ((this.dataFile == null) ? (other.getDataFile() != null) : !this.dataFile.equals(other.getDataFile())) 
			return false; 
		
		if ((this.experimentId == null) ? (other.getExperimentId() != null) : !this.experimentId.equals(other.getExperimentId())) 
			return false; 
		
		if ((this.batchId == null) ? (other.getBatchId() != null) : !this.batchId.equals(other.getBatchId())) 
			return false; 
		
		if (obj == this)
			return true;

		return true;
	}

	@Override
	public int hashCode() {
		
		int hash = 3;
		hash = 53 * hash + (this.dataFile != null ? this.dataFile.hashCode() : 0)
        		+ (this.experimentId != null ? this.experimentId.hashCode() : 0)
        		+ (this.batchId != null ? this.batchId.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(MetabCombinerFileInputObject o) {

		int res = this.experimentId.compareTo(o.getExperimentId());
		if(res == 0)
			res = this.batchId.compareTo(o.getBatchId());
		
		if(res == 0)
			res = this.dataFile.compareTo(o.getDataFile());
		
		return res;
	}
}
