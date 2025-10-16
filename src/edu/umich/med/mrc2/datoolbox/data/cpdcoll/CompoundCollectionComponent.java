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

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class CompoundCollectionComponent {

	private String id;
	private String collectionId;
	private String cas;
	private Map<CpdMetadataField,String>metadata;
	private CompoundIdentity cid;
	
	private String primary_smiles;
	private String primary_formula;
	private double primary_mass;
	private String formula_from_primary_smiles;
	private int charge_from_primary_smiles;
	private double mass_from_primary_smiles;
	private String primary_inchi_key_smiles_conflict;
	private String primary_smiles_formula_conflict;
	private double primary_formula_mass_conflict;
	
	private String msReadySmiles;
	private String msReadyFormula;
	
	public CompoundCollectionComponent(String id, String collectionId, String cas) {
		super();
		this.id = id;
		this.collectionId = collectionId;
		this.cas = cas;
		metadata = 
				new TreeMap<CpdMetadataField,String>(new CpdMetadataFieldComparator());
	}

	public CompoundCollectionComponent(String collectionId, String cas) {
		this(null, collectionId, cas);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public String getCas() {
		return cas;
	}

	public Map<CpdMetadataField, String> getMetadata() {
		return metadata;
	}
	
	public void addMetadata(CpdMetadataField field, String value) {
		metadata.put(field, value);
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!CompoundCollectionComponent.class.isAssignableFrom(obj.getClass()))
			return false;

		final CompoundCollectionComponent other = (CompoundCollectionComponent) obj;

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

	public CompoundIdentity getCid() {
		return cid;
	}

	public void setCid(CompoundIdentity cid) {
		this.cid = cid;
	}

	public String getPrimary_smiles() {
		return primary_smiles;
	}

	public void setPrimary_smiles(String primary_smiles) {
		this.primary_smiles = primary_smiles;
	}

	public String getPrimary_formula() {
		return primary_formula;
	}

	public void setPrimary_formula(String primary_formula) {
		this.primary_formula = primary_formula;
	}

	public double getPrimary_mass() {
		return primary_mass;
	}

	public void setPrimary_mass(double primary_mass) {
		this.primary_mass = primary_mass;
	}

	public String getFormula_from_primary_smiles() {
		return formula_from_primary_smiles;
	}

	public void setFormula_from_primary_smiles(String formula_from_primary_smiles) {
		this.formula_from_primary_smiles = formula_from_primary_smiles;
	}

	public int getCharge_from_primary_smiles() {
		return charge_from_primary_smiles;
	}

	public void setCharge_from_primary_smiles(int charge_from_primary_smiles) {
		this.charge_from_primary_smiles = charge_from_primary_smiles;
	}

	public double getMass_from_primary_smiles() {
		return mass_from_primary_smiles;
	}

	public void setMass_from_primary_smiles(double mass_from_primary_smiles) {
		this.mass_from_primary_smiles = mass_from_primary_smiles;
	}

	public String getPrimary_inchi_key_smiles_conflict() {
		return primary_inchi_key_smiles_conflict;
	}

	public void setPrimary_inchi_key_smiles_conflict(String primary_inchi_key_smiles_conflict) {
		this.primary_inchi_key_smiles_conflict = primary_inchi_key_smiles_conflict;
	}

	public String getPrimary_smiles_formula_conflict() {
		return primary_smiles_formula_conflict;
	}

	public void setPrimary_smiles_formula_conflict(String primary_smiles_formula_conflict) {
		this.primary_smiles_formula_conflict = primary_smiles_formula_conflict;
	}

	public double getPrimary_formula_mass_conflict() {
		return primary_formula_mass_conflict;
	}

	public void setPrimary_formula_mass_conflict(double primary_formula_mass_conflict) {
		this.primary_formula_mass_conflict = primary_formula_mass_conflict;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public void setCas(String cas) {
		this.cas = cas;
	}

	public void setMetadata(Map<CpdMetadataField, String> metadata) {
		this.metadata = metadata;
	}
	
	public boolean hasConflict() {
		
		if(primary_inchi_key_smiles_conflict != null
				|| primary_smiles_formula_conflict != null)
			return true;
		else
			return false;
	}

	public String getMsReadySmiles() {
		return msReadySmiles;
	}

	public void setMsReadySmiles(String msReadySmiles) {
		this.msReadySmiles = msReadySmiles;
	}

	public String getMsReadyFormula() {
		return msReadyFormula;
	}

	public void setMsReadyFormula(String msReadyFormula) {
		this.msReadyFormula = msReadyFormula;
	}
	
	public boolean isMsReady() {
		
		if(msReadySmiles == null || msReadyFormula == null)
			return false;		
		else
			return true;
	}
}
