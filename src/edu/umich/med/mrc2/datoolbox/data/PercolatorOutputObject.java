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

public class PercolatorOutputObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7951985435936836862L;
	
	private String msmsFeatureId;
	private String mrc2libid;
	private String libraryName;
	private double score;
	private double qValue;
	private double posteriorErrorProbablility;
	
	public PercolatorOutputObject(
			String msmsFeatureId, 
			String mrc2libid, 
			String libraryName, 
			double score,
			double qValue, 
			double posteriorErrorProbablility) {
		super();
		this.msmsFeatureId = msmsFeatureId;
		this.mrc2libid = mrc2libid;
		this.libraryName = libraryName;
		this.score = score;
		this.qValue = qValue;
		this.posteriorErrorProbablility = posteriorErrorProbablility;
	}

	@Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!PercolatorOutputObject.class.isAssignableFrom(obj.getClass()))
            return false;

        final PercolatorOutputObject other = (PercolatorOutputObject) obj;
            
        if ((this.msmsFeatureId == null) ? (other.getMsmsFeatureId() != null) : !this.msmsFeatureId.equals(other.getMsmsFeatureId()))
            return false;
        
        if ((this.mrc2libid == null) ? (other.getMrc2libid() != null) : !this.mrc2libid.equals(other.getMrc2libid()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash 
        		+ (this.msmsFeatureId != null ? this.msmsFeatureId.hashCode() : 0)
        		+ (this.mrc2libid != null ? this.mrc2libid.hashCode() : 0);
        return hash;
    }

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getMsmsFeatureId() {
		return msmsFeatureId;
	}

	public String getMrc2libid() {
		return mrc2libid;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public double getScore() {
		return score;
	}

	public double getqValue() {
		return qValue;
	}

	public double getPosteriorErrorProbablility() {
		return posteriorErrorProbablility;
	}

}















