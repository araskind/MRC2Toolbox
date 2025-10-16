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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.Record;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;

public class DrugBankRecord implements Record {

	private String primaryId;
	private String name;
	private String aggregateState;
	private Date dateCreated;
	private Date lastUpdated;
	
	private CompoundIdentity compoundIdentity;
	
	private Collection<String>synonyms;
	private Collection<String>secondaryIds;
	private Collection<DrugCategory>categories;
	private Collection<CompoundProperty>compoundProperties;	
	private Collection<DrugTarget>drugTargets;
	private Map<DrugBankDescriptiveFields,String>descriptiveFields;
	private Collection<DrugBankExternalLink>externalLinks;
	private Collection<DrugPathway>pathways;
	private Collection<HMDBCitation>references;

	public DrugBankRecord(String id) {

		this.primaryId = id;
		categories = new ArrayList<DrugCategory>();
		synonyms = new TreeSet<String>();
		compoundProperties = new ArrayList<CompoundProperty>();
		compoundIdentity = new CompoundIdentity();		
		drugTargets = new ArrayList<DrugTarget>();
		externalLinks = new ArrayList<DrugBankExternalLink>();
		descriptiveFields = new TreeMap<DrugBankDescriptiveFields,String>();		
		secondaryIds = new TreeSet<String>();
		pathways = new ArrayList<DrugPathway>();
		references = new ArrayList<HMDBCitation>();
	}

	public void addCategory(DrugCategory category){
		categories.add(category);
	}

	public void addExternalIdentifier(
			CompoundDatabaseEnum database, String identifier){
		compoundIdentity.addDbId(database, identifier);
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

	public CompoundIdentity getCompoundIdentity() {
		return compoundIdentity;
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

	public Collection<CompoundProperty> getCompoundProperties() {
		return compoundProperties;
	}
	
	public String getPropertyValue(DrugBankCompoundProperties property) {
		
		String pn = property.getName();
		CompoundProperty prop =  compoundProperties.stream().
				filter(p -> p.getPropertyName().equals(pn)).
				findFirst().orElse(null);
		if(prop == null)
			return null;
		else
			return prop.getPropertyValue();
	}

	public Collection<String> getSynonyms() {
		return synonyms;
	}

	public String getAggregateState() {
		return aggregateState;
	}

	public void setAggregateState(String aggregateState) {
		this.aggregateState = aggregateState;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Collection<DrugCategory> getCategories() {
		return categories;
	}

	public Collection<DrugTarget> getDrugTargets() {
		return drugTargets;
	}
	
	public String getPropertyValue(String propertyName) {
		
		CompoundProperty prop = compoundProperties.stream().
			filter(p -> p.getPropertyName().equals(propertyName)).
			findFirst().orElse(null);
		if(prop == null)
			return null;
		else
			return prop.getPropertyValue();		
	}

	public Collection<String> getSecondaryIds() {
		return secondaryIds;
	}

	public Collection<DrugBankExternalLink> getExternalLinks() {
		return externalLinks;
	}

	public Collection<DrugPathway> getPathways() {
		return pathways;
	}

	public Collection<HMDBCitation> getReferences() {
		return references;
	}
}

































