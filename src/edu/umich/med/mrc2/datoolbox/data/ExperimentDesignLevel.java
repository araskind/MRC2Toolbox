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
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class ExperimentDesignLevel implements Comparable<ExperimentDesignLevel>, Serializable, Renamable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6652398772144696241L;
	private String levelName;
	private String levelDescription;
	private String levelId;
	private ExperimentDesignFactor parentFactor;
	private boolean enabled;
	private boolean nameIsValid;

	public ExperimentDesignLevel(String levelName) {
		super();
		this.levelName = levelName;
		parentFactor = null;
		this.levelId = DataPrefix.EXPERIMENTAL_FACTOR_LEVEL.getName() + UUID.randomUUID().toString();
		enabled = true;
	}

	public ExperimentDesignLevel(String levelName, String levelId) {
		super();
		this.levelName = levelName;
		parentFactor = null;
		this.levelId = levelId;
		enabled = true;
	}

	public ExperimentDesignLevel(String levelName, ExperimentDesignFactor parent) {
		super();
		this.levelName = levelName;
		parentFactor = parent;
		this.levelId = DataPrefix.EXPERIMENTAL_FACTOR_LEVEL.getName() + UUID.randomUUID().toString();
		enabled = true;
	}

	
	public ExperimentDesignLevel(
			String levelId,
			String levelName, 
			String levelDescription) {
		super();
		this.levelId = levelId;
		this.levelName = levelName;
		this.levelDescription = levelDescription;
	
		enabled = true;
	}

	@Override
	public int compareTo(ExperimentDesignLevel o) {

		String thisLevelComparableName = "";
		if(this.parentFactor != null)
			thisLevelComparableName = this.parentFactor.getName() + levelName;
		else
			thisLevelComparableName = levelName;

		String otherComparableName = "";
		if(o.getParentFactor() != null)
			otherComparableName = o.getParentFactor().getName() + o.getName();
		else
			otherComparableName = o.getName();

		return thisLevelComparableName.compareTo(otherComparableName);
	}

	public String getLevelDescription() {
		return levelDescription;
	}

	public String getLevelId() {
		return levelId;
	}

	public String getName() {
		return levelName;
	}

	public ExperimentDesignFactor getParentFactor() {
		return parentFactor;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setLevelDescription(String levelDescription) {
		this.levelDescription = levelDescription;
	}

	public void setName(String levelName) {
		this.levelName = levelName;
	}

	public void setParentFactor(ExperimentDesignFactor parentFactor) {
		this.parentFactor = parentFactor;
	}

	@Override
	public String toString() {
		return this.levelName;
	}

	public boolean nameIsValid() {
		return nameIsValid;
	}

	public void setNameValid(boolean valid) {
		this.nameIsValid = valid;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ExperimentDesignLevel.class.isAssignableFrom(obj.getClass()))
            return false;

        final ExperimentDesignLevel other = (ExperimentDesignLevel) obj;

        //	If belong to different factors
        if ((this.parentFactor == null) ? (other.getParentFactor() != null) : !this.parentFactor.equals(other.getParentFactor()))
            return false;

        //	If belong to same factor
        if(this.parentFactor != null && other.getParentFactor() != null) {

        	if(this.parentFactor.equals(other.getParentFactor())) {

                if ((this.levelName == null) ? (other.getName() != null) : !this.levelName.equals(other.getName()))
                    return false;
        	}
        }
        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash
        		+ (this.levelName != null ? this.levelName.hashCode() : 0)
        		+ (this.parentFactor != null ? this.parentFactor.hashCode() : 0);
        return hash;
    }
}






