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

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MinimalMSOneFeature implements Serializable, Comparable<MinimalMSOneFeature> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3016172144844330033L;
	
	private String name;
	private double mz;
	private double rt;

	public MinimalMSOneFeature(double mz, double rt) {
		super();
		this.mz = mz;
		this.rt = rt;
		this.name = MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_"
				+ MRC2ToolBoxConfiguration.getRtFormat().format(rt);
	}

	public MinimalMSOneFeature(String name, double mz, double rt) {
		super();
		this.name = name;
		this.mz = mz;
		this.rt = rt;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public double getMz() {
		return mz;
	}

	public double getRt() {
		return rt;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (!MinimalMSOneFeature.class.isAssignableFrom(obj.getClass()))
			return false;

		final MinimalMSOneFeature other = (MinimalMSOneFeature) obj;

		if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
			return false;

		if (obj == this)
			return true;

		return true;
	}

	@Override
	public int hashCode() {

		int hash = 3;
		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(MinimalMSOneFeature o) {
		 
		int result = Double.compare(mz, o.getMz());
		if(result == 0)
			result = Double.compare(rt, o.getRt());
		
		return result;
	}

	public void setName(String name) {
		this.name = name;
	}
}
