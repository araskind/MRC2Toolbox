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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum CompoundIdentificationConfidence {

	ACCURATE_MASS_RT_MSMS(1, "1",
			"Confirmed structure (AM-RT-MSMS)",
			"Accurate mass, retention time, and MS/MS based on standard in every sample"),
	ACCURATE_MASS_RT_MSMS_POOLED(2, "1p",
			"Confirmed structure in pool (AM-RT-MSMS-POOL)",
			"Accurate mass, retention time, and MS/MS based on standard (MSMS in pooled samples only)"),
	ACCURATE_MASS_RT(2, "2a",
			"Highly confident match (AM-RT)",
			"Accurate mass and retention time based on standard"),
	ACCURATE_MASS_MSMS(3, "3",
			"Confident match (AM-MSMS)",
			"Accurate mass and MS/MS, as found in library"),
	ACCURATE_MASS(4, "4a",
			"Formula match (AM)",
			"Accurate mass only, as found in database"),
	UNKNOWN_MSMS_RT(5, "5",
			"Unknown with MSMS (UNK-AM-RT-MSMS)",
			"Unknown, retention time and MS/MS available"),
	UNKNOWN_ACCURATE_MASS_RT(6, "6",
			"Unknown (UNK-AM-RT)",
			"Unknown, retention time and MS available");

	private final int level;
	private final String levelId;
	private final String name;
	private final String description;

	CompoundIdentificationConfidence(int level, String levelId, String name, String description) {

		this.level = level;
		this.levelId = levelId;
		this.name = name;
		this.description = description;
	}

	public int getLevel() {
		return level;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return levelId;
	}

	/**
	 * @return the levelId
	 */
	public String getLevelId() {
		return levelId;
	}

	public static CompoundIdentificationConfidence getLevelByNumber(int number) {

		for(CompoundIdentificationConfidence cl : CompoundIdentificationConfidence.values()) {
			if(cl.getLevel() == number)
				return cl;
		}
		return null;
	}

	public static CompoundIdentificationConfidence getLevelById(String id) {

		for(CompoundIdentificationConfidence cl : CompoundIdentificationConfidence.values()) {
			if(cl.getLevelId().equals(id))
				return cl;
		}
		return null;
	}
	
	public static CompoundIdentificationConfidence getLevelByName(String name) {

		for(CompoundIdentificationConfidence cl : CompoundIdentificationConfidence.values()) {
			if(cl.name().equals(name))
				return cl;
		}
		return null;
	}
}
















