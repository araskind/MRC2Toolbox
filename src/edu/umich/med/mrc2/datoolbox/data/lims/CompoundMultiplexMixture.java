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

import java.util.HashSet;
import java.util.Set;

public class CompoundMultiplexMixture implements Comparable<CompoundMultiplexMixture> {

	private String id;
	private String name;
	private MobilePhase solvent;
	private double aliquoteVolume;
	private Set<CompoundMultiplexMixtureComponent>mixComponents;
	
	public CompoundMultiplexMixture(String id, String name, MobilePhase solvent, double aliquoteVolume) {
		super();
		this.id = id;
		this.name = name;
		this.solvent = solvent;
		this.aliquoteVolume = aliquoteVolume;
		mixComponents = new HashSet<CompoundMultiplexMixtureComponent>();
	}

	public CompoundMultiplexMixture(String name, MobilePhase solvent) {
		super();
		this.name = name;
		this.solvent = solvent;
		mixComponents = new HashSet<CompoundMultiplexMixtureComponent>();
	}

	@Override
	public int compareTo(CompoundMultiplexMixture o) {
		return this.name.compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return name;
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

	public MobilePhase getSolvent() {
		return solvent;
	}

	public void setSolvent(MobilePhase solvent) {
		this.solvent = solvent;
	}

	public double getAliquoteVolume() {
		return aliquoteVolume;
	}

	public void setAliquoteVolume(double aliquoteVolume) {
		this.aliquoteVolume = aliquoteVolume;
	}
		
	public void addMixtureComponent(CompoundMultiplexMixtureComponent newComponent) {
		mixComponents.add(newComponent);
	}
	
	public void removeMixtureComponent(CompoundMultiplexMixtureComponent toRemove) {
		mixComponents.remove(toRemove);
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!CompoundMultiplexMixture.class.isAssignableFrom(obj.getClass()))
			return false;

		final CompoundMultiplexMixture other = (CompoundMultiplexMixture) obj;

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

	public Set<CompoundMultiplexMixtureComponent> getMixComponents() {
		return mixComponents;
	}
}
