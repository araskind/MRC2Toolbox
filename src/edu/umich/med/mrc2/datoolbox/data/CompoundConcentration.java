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

public class CompoundConcentration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6472092277322287285L;
	private String concentrationId;
	private String accession;
	private String biofluid;
	private String units;
	private String value;
	private String age;
	private String sex;
	private String subjectCondition;
	private String comments;
	private String flag;
	private String type;
	
	public CompoundConcentration(
			String concentrationId, 
			String accession, 
			String biofluid, 
			String units, 
			String value,
			String age, 
			String sex, 
			String subjectCondition, 
			String comments, 
			String flag, 
			String type) {
		super();
		this.concentrationId = concentrationId;
		this.accession = accession;
		this.biofluid = biofluid;
		this.units = units;
		this.value = value;
		this.age = age;
		this.sex = sex;
		this.subjectCondition = subjectCondition;
		this.comments = comments;
		this.flag = flag;
		this.type = type;
	}

	public String getConcentrationId() {
		return concentrationId;
	}

	public void setConcentrationId(String concentrationId) {
		this.concentrationId = concentrationId;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getBiofluid() {
		return biofluid;
	}

	public void setBiofluid(String biofluid) {
		this.biofluid = biofluid;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getSubjectCondition() {
		return subjectCondition;
	}

	public void setSubjectCondition(String subjectCondition) {
		this.subjectCondition = subjectCondition;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CompoundConcentration.class.isAssignableFrom(obj.getClass()))
            return false;

        final CompoundConcentration other = (CompoundConcentration) obj;

        if ((this.concentrationId == null) ? (other.getConcentrationId() != null) : !this.concentrationId.equals(other.getConcentrationId()))
            return false;

        return true;
    }
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.concentrationId != null ? this.concentrationId.hashCode() : 0);
        return hash;
    }
}
