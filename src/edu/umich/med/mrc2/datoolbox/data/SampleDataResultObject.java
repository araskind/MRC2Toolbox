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

public class SampleDataResultObject {

	private ExperimentalSample sample;
	private DataFile dataFile;
	private ResultsFile resultFile;
	
	public SampleDataResultObject() {
		this(null, null, null);
	}
	
	public SampleDataResultObject(
			ExperimentalSample sample, 
			DataFile dataFile) {
		this(sample, dataFile, null);
	}
	
	public SampleDataResultObject(
			ExperimentalSample sample, 
			DataFile dataFile, 
			ResultsFile resultFile) {
		super();
		this.sample = sample;
		this.dataFile = dataFile;
		this.resultFile = resultFile;
	}

	public ExperimentalSample getSample() {
		return sample;
	}

	public void setSample(ExperimentalSample sample) {
		this.sample = sample;
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public void setDataFile(DataFile dataFile) {
		this.dataFile = dataFile;
	}

	public ResultsFile getResultFile() {
		return resultFile;
	}

	public void setResultFile(ResultsFile resultFile) {
		this.resultFile = resultFile;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!SampleDataResultObject.class.isAssignableFrom(obj.getClass()))
            return false;

        final SampleDataResultObject other = (SampleDataResultObject) obj;

        if ((this.sample == null) ? (other.getSample() != null) : !this.sample.equals(other.getSample()))
            return false;
        
        if ((this.dataFile == null) ? (other.getDataFile() != null) : !this.dataFile.equals(other.getDataFile()))
            return false;
        
        if ((this.resultFile == null) ? (other.getResultFile() != null) : !this.resultFile.equals(other.getResultFile()))
            return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.sample != null ? this.sample.hashCode() : 0);
        hash += (this.dataFile != null ? this.dataFile.hashCode() : 0);
        hash += (this.resultFile != null ? this.resultFile.hashCode() : 0);
        return hash;
    }
}
