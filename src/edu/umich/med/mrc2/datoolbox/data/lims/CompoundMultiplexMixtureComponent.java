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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class CompoundMultiplexMixtureComponent implements Comparable<CompoundMultiplexMixtureComponent> {

	private String id;
	private String mixtureId;
	private CompoundIdentity compound;
	private double concentrationMkMol;
	private Map<String,String>compoundProperties;
	
	public CompoundMultiplexMixtureComponent(String id, String mixtureId, CompoundIdentity compound,
			double concentrationMkMol) {
		super();
		this.id = id;
		this.mixtureId = mixtureId;
		this.compound = compound;
		this.concentrationMkMol = concentrationMkMol;
		compoundProperties = new TreeMap<String,String>();
	}

	public CompoundMultiplexMixtureComponent(CompoundIdentity compound, double concentrationMkMol) {
		super();
		this.compound = compound;
		this.concentrationMkMol = concentrationMkMol;
		compoundProperties = new TreeMap<String,String>();
	}
	
	@Override
	public int compareTo(CompoundMultiplexMixtureComponent o) {
		return this.compound.getName().compareTo(o.getCompound().getName());
	}
	
	@Override
	public String toString() {
		return compound.getName();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMixtureId() {
		return mixtureId;
	}

	public void setMixtureId(String mixtureId) {
		this.mixtureId = mixtureId;
	}

	public CompoundIdentity getCompound() {
		return compound;
	}

	public void setCompound(CompoundIdentity compound) {
		this.compound = compound;
	}

	public double getConcentrationMkMol() {
		return concentrationMkMol;
	}

	public void setConcentrationMkMol(double concentrationMkMol) {
		this.concentrationMkMol = concentrationMkMol;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!CompoundMultiplexMixtureComponent.class.isAssignableFrom(obj.getClass()))
			return false;

		final CompoundMultiplexMixtureComponent other = (CompoundMultiplexMixtureComponent) obj;

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
