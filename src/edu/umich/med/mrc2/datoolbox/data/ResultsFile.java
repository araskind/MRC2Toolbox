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

import java.io.Serializable;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;

public class ResultsFile implements Serializable, Comparable<ResultsFile> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8271053705030591552L;
	private String name, fullPath;
	private DataExtractionMethod method;
	private Date dateCreated;
	private DataFile rawDataFile;
	
	public ResultsFile(
			String name, 
			DataExtractionMethod method, 
			Date dateCreated, 
			DataFile rawDataFile) {
		super();
		this.name = name;
		this.method = method;
		this.dateCreated = dateCreated;
		this.rawDataFile = rawDataFile;
	}
	
	public ResultsFile(String name, DataExtractionMethod method) {
		this(name, method, new Date(), null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataExtractionMethod getMethod() {
		return method;
	}

	public void setMethod(DataExtractionMethod method) {
		this.method = method;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public DataFile getRawDataFile() {
		return rawDataFile;
	}

	public void setRawDataFile(DataFile rawDataFile) {
		this.rawDataFile = rawDataFile;
	}

	@Override
	public int compareTo(ResultsFile o) {

		int res = 0;
		res = this.rawDataFile.compareTo(o.getRawDataFile());
		if(res == 0)
			res = this.method.compareTo(o.getMethod());
		
		if(res == 0)
			res = this.name.compareTo(o.getName());
		
		if(res == 0)
			res = this.dateCreated.compareTo(o.getDateCreated());
		
		return res;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ResultsFile.class.isAssignableFrom(obj.getClass()))
            return false;

        final ResultsFile other = (ResultsFile) obj;

        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
            return false;
        
        if ((this.method == null) ? (other.getMethod() != null) : !this.method.equals(other.getMethod()))
            return false;
        
        if ((this.dateCreated == null) ? (other.getDateCreated() != null) : !this.dateCreated.equals(other.getDateCreated()))
            return false;

        if ((this.rawDataFile == null) ? (other.getRawDataFile() != null) : !this.rawDataFile.equals(other.getRawDataFile()))
            return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash += (this.method != null ? this.method.hashCode() : 0);
        hash += (this.dateCreated != null ? this.dateCreated.hashCode() : 0);
        hash += (this.rawDataFile != null ? this.rawDataFile.hashCode() : 0);
        return hash;
    }

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
}





