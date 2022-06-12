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

package edu.umich.med.mrc2.datoolbox.data.msclust;

import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;

public class MSMSClusteringParameterSet {

	private String id;
	private String name;
	private double mzErrorValue;
	private MassErrorType massErrorType;
	private double rtErrorValue;
	private double msmsSimilarityCutoff;
	private String md5;
	
	public MSMSClusteringParameterSet(
			String id, 
			String name, 
			double mzErrorValue, 
			MassErrorType massErrorType,
			double rtErrorValue, 
			double msmsSimilarityCutoff,
			String md5) {
		this(name, mzErrorValue, massErrorType, rtErrorValue, msmsSimilarityCutoff);
		this.id = id;
		this.md5 = md5;
	}

	public MSMSClusteringParameterSet(
			String name, 
			double mzErrorValue, 
			MassErrorType massErrorType,
			double rtErrorValue, 
			double msmsSimilarityCutoff) {
		
		this.id = DataPrefix.MSMS_CLUSTERING_PARAM_SET.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		this.name = name;
		this.mzErrorValue = mzErrorValue;
		this.massErrorType = massErrorType;
		this.rtErrorValue = rtErrorValue;
		this.msmsSimilarityCutoff = msmsSimilarityCutoff;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMzErrorValue() {
		return mzErrorValue;
	}

	public void setMzErrorValue(double mzErrorValue) {
		this.mzErrorValue = mzErrorValue;
	}

	public MassErrorType getMassErrorType() {
		return massErrorType;
	}

	public void setMassErrorType(MassErrorType massErrorType) {
		this.massErrorType = massErrorType;
	}

	public double getRtErrorValue() {
		return rtErrorValue;
	}

	public void setRtErrorValue(double rtErrorValue) {
		this.rtErrorValue = rtErrorValue;
	}

	public double getMsmsSimilarityCutoff() {
		return msmsSimilarityCutoff;
	}

	public void setMsmsSimilarityCutoff(double msmsSimilarityCutoff) {
		this.msmsSimilarityCutoff = msmsSimilarityCutoff;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MSMSClusteringParameterSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSMSClusteringParameterSet other = (MSMSClusteringParameterSet) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}

