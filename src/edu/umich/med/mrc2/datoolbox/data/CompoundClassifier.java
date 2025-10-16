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

import java.io.Serializable;

public class CompoundClassifier implements Serializable, Comparable<CompoundClassifier>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8670415396232898741L;
	private String superClass;
	private String mainClass;
	private String subClass;

	public CompoundClassifier() {
		super();
		superClass = "";
		mainClass = "";
		subClass = "";
	}

	/**
	 * @return the superClass
	 */
	public String getSuperClass() {
		return superClass;
	}

	/**
	 * @return the mainClass
	 */
	public String getMainClass() {
		return mainClass;
	}

	/**
	 * @return the subClass
	 */
	public String getSubClass() {
		return subClass;
	}

	/**
	 * @param superClass the superClass to set
	 */
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

	/**
	 * @param mainClass the mainClass to set
	 */
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	/**
	 * @param subClass the subClass to set
	 */
	public void setSubClass(String subClass) {
		this.subClass = subClass;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CompoundClassifier.class.isAssignableFrom(obj.getClass()))
            return false;

        final CompoundClassifier other = (CompoundClassifier) obj;

        if(!this.superClass.equals(other.getSuperClass()))
        	 return false;

        if(!this.mainClass.equals(other.getMainClass()))
       	 	return false;

        if(!this.subClass.equals(other.getSubClass()))
       	 	return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.superClass != null ? this.superClass.hashCode() : 0)
        		+ (this.mainClass != null ? this.mainClass.hashCode() : 0)
        		+ (this.subClass != null ? this.subClass.hashCode() : 0);
        return hash;
    }

	@Override
	public int compareTo(CompoundClassifier o) {

		String name = superClass + mainClass + subClass;
		String otherName = o.getSuperClass() + o.getMainClass() + o.getSubClass();
		return name.compareTo(otherName);
	}

	@Override
	public String toString() {
		return superClass + "\n" + mainClass + "\n" + subClass;
	}
}


















