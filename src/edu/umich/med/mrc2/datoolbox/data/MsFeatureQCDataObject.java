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

import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeatureQCDataObject {

	protected String libraryTargetId;
	protected double qualityScore;
	protected Range rtRange;
	
	public MsFeatureQCDataObject(
			String libraryTargetId, 
			double qualityScore, 
			Range rtRange) {
		super();
		this.libraryTargetId = libraryTargetId;
		this.qualityScore = qualityScore;
		this.rtRange = rtRange;
	}

	public String getLibraryTargetId() {
		return libraryTargetId;
	}

	public double getQualityScore() {
		return qualityScore;
	}

	public Range getRtRange() {
		return rtRange;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureQCDataObject.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureQCDataObject other = (MsFeatureQCDataObject) obj;

        if ((this.libraryTargetId == null) ? (other.getLibraryTargetId() != null) : 
        		!this.libraryTargetId.equals(other.getLibraryTargetId()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.libraryTargetId != null ? this.libraryTargetId.hashCode() : 0);
        return hash;
    }
}
