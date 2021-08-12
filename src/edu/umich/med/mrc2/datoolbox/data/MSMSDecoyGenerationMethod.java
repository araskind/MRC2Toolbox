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

public class MSMSDecoyGenerationMethod implements Serializable, Comparable<MSMSDecoyGenerationMethod>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3110101464989868616L;
	private String methodId;
	private String methodName;
	private String methodNotes;
	
	public MSMSDecoyGenerationMethod(String methodId, String methodName, String methodNotes) {
		super();
		this.methodId = methodId;
		this.methodName = methodName;
		this.methodNotes = methodNotes;
	}

	public String getMethodId() {
		return methodId;
	}

	public void setMethodId(String methodId) {
		this.methodId = methodId;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodNotes() {
		return methodNotes;
	}

	public void setMethodNotes(String methodNotes) {
		this.methodNotes = methodNotes;
	}

	@Override
	public int compareTo(MSMSDecoyGenerationMethod o) {
		return methodName.compareTo(o.getMethodName());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MSMSDecoyGenerationMethod.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSMSDecoyGenerationMethod other = (MSMSDecoyGenerationMethod) obj;

        if ((this.methodId == null) ? (other.getMethodId() != null) : !this.methodId.equals(other.getMethodId()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.methodId != null ? this.methodId.hashCode() : 0);
        return hash;
    }
    
    @Override
	public String toString() {
		return methodName;
	}
}
