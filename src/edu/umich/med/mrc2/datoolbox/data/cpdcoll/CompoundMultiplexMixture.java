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

package edu.umich.med.mrc2.datoolbox.data.cpdcoll;

import java.util.ArrayList;
import java.util.Collection;

public class CompoundMultiplexMixture implements Comparable<CompoundMultiplexMixture> {

	private String id;
	private String name;
	private Collection<CompoundMultiplexMixtureComponent>components;

	public CompoundMultiplexMixture(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		components = new ArrayList<CompoundMultiplexMixtureComponent>();
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

	@Override
	public String toString() {
		return name;
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

		if (!this.id.equals(other.getId()))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public int compareTo(CompoundMultiplexMixture o) {
		return this.id.compareTo(o.getId());
	}

	public Collection<CompoundMultiplexMixtureComponent> getComponents() {
		return components;
	}
	
	public void addComponent(CompoundMultiplexMixtureComponent component) {
		components.add(component);
	}
	
	public void removeComponent(CompoundMultiplexMixtureComponent component) {
		components.remove(component);
	}
}




