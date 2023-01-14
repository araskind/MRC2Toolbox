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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.Record;

public class DrugBankRecord implements Record {

	private String primaryId;
	private String name;
	private String unii;

	private Collection<String>synonyms;
	private Collection<DrugCategory>categories;
	private Collection<CompoundProperty>compoundProperties;
	private CompoundIdentity drugIdentity;
	private Map<DrugBankDescriptiveFields,String>descriptiveFields;

	public DrugBankRecord() {

		categories = new ArrayList<DrugCategory>();
		synonyms = new TreeSet<String>();
		compoundProperties = new ArrayList<CompoundProperty>();
		drugIdentity = new CompoundIdentity();

		descriptiveFields = new HashMap<DrugBankDescriptiveFields,String>();
		for(DrugBankDescriptiveFields f : DrugBankDescriptiveFields.values())
			descriptiveFields.put(f, "");
	}

	public void addCategory(DrugCategory category){
		categories.add(category);
	}

	public void addExternalIdentifier(CompoundDatabaseEnum database, String identifier){
		drugIdentity.addDbId(database, identifier);
	}

	public void addProperty(CompoundProperty property){
		compoundProperties.add(property);
	}

	public void addSynonym(String synonym) {
		synonyms.add(synonym);
	}

	@Override
	public String getDescription() {
		return descriptiveFields.get(DrugBankDescriptiveFields.DESCRIPTION);
	}

	public CompoundIdentity getDrugIdentity() {
		return drugIdentity;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPrimaryId() {
		return primaryId;
	}

	public void setCategories(ArrayList<DrugCategory> categories) {
		this.categories = categories;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setPrimaryId(String primaryId) {
		this.primaryId = primaryId;
	}

	public void setUnii(String unii) {
		this.unii = unii;
	}

	@Override
	public void setDescription(String description) {
		descriptiveFields.put(DrugBankDescriptiveFields.DESCRIPTION, description);
	}

	public void setDescriptiveField(DrugBankDescriptiveFields field, String contents) {
		descriptiveFields.put(field, contents);
	}

	public String getDescriptiveField(DrugBankDescriptiveFields field) {
		return descriptiveFields.get(field);
	}

	/**
	 * @return the unii
	 */
	public String getUnii() {
		return unii;
	}

	/**
	 * @return the compoundProperties
	 */
	public Collection<CompoundProperty> getCompoundProperties() {
		return compoundProperties;
	}

	/**
	 * @return the synonyms
	 */
	public Collection<String> getSynonyms() {
		return synonyms;
	}
}

































