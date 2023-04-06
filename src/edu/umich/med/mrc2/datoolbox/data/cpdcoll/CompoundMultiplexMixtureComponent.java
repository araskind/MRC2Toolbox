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

package edu.umich.med.mrc2.datoolbox.data.cpdcoll;

import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;

public class CompoundMultiplexMixtureComponent {

	private String id;
	private CompoundCollectionComponent component;
	private Double concentrationMkm;
	private MobilePhase solvent;
	private Double xlogp;
	private Double aliquoteVolume;
	private String cccid;
	
	public CompoundMultiplexMixtureComponent(
			String cccid, 
			Double concentrationMkm, 
			MobilePhase solvent,
			Double xlogp, 
			Double aliquoteVolume) {
		super();
		id = DataPrefix.COMPOUND_MULTIPLEX_MIXTURE_COMPONENT.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		this.cccid = cccid;
		this.concentrationMkm = concentrationMkm;
		this.solvent = solvent;
		this.xlogp = xlogp;
		this.aliquoteVolume = aliquoteVolume;
	}
	
	public CompoundMultiplexMixtureComponent(
			CompoundCollectionComponent component, 
			Double concentrationMkm, 
			MobilePhase solvent,
			Double xlogp, 
			Double aliquoteVolume) {
		super();
		id = DataPrefix.COMPOUND_MULTIPLEX_MIXTURE_COMPONENT.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		this.component = component;
		this.concentrationMkm = concentrationMkm;
		this.solvent = solvent;
		this.xlogp = xlogp;
		this.aliquoteVolume = aliquoteVolume;
	}

	public Double getConcentrationMkm() {
		return concentrationMkm;
	}

	public MobilePhase getSolvent() {
		return solvent;
	}

	public Double getXlogp() {
		return xlogp;
	}

	public Double getAliquoteVolume() {
		return aliquoteVolume;
	}

	public String getCccid() {
		return cccid;
	}

	public CompoundCollectionComponent getCCComponent() {
		return component;
	}
	
	public void setCCComponent(CompoundCollectionComponent component) {
		this.component = component;
	}
	
	@Override
	public String toString() {
		
		if(component == null)
			return cccid;
		
		if(component.getCid() == null)
			return component.getCas();
		
		return component.getCid().getCommonName();
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

	public String getId() {
		return id;
	}
}





