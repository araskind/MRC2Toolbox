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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class CompoundConcentration {

	private String uniqueId;
	private String biospecimen;
	private String value;
	private String units;
	private ConcentrationType type;
	private String age;
	private String sex;
	private String condition;
	private String patientInfo;
	private String comment;
	private Collection<HMDBCitation>references;

	public CompoundConcentration(
			String biospecimen, String value, String units, ConcentrationType type) {
		super();

		uniqueId = DataPrefix.HMDB_CONCENTRATION.getName() + 
				UUID.randomUUID().toString();
		this.biospecimen = biospecimen;
		this.value = value;
		this.units = units;
		this.type = type;
		references = new ArrayList<HMDBCitation>();
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the biospecimen
	 */
	public String getBiospecimen() {
		return biospecimen;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @return the age
	 */
	public String getAge() {
		return age;
	}

	/**
	 * @return the sex
	 */
	public String getSex() {
		return sex;
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(String age) {
		this.age = age;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the references
	 */
	public Collection<HMDBCitation> getReferences() {
		return references;
	}

	/**
	 * @return the type
	 */
	public ConcentrationType getType() {
		return type;
	}

	/**
	 * @return the patientInfo
	 */
	public String getPatientInfo() {
		return patientInfo;
	}

	/**
	 * @param patientInfo the patientInfo to set
	 */
	public void setPatientInfo(String patientInfo) {
		this.patientInfo = patientInfo;
	}



}
