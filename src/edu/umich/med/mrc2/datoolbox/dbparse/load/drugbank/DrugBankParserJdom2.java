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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundPropertyType;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCrossrefFields;

public class DrugBankParserJdom2 {
	
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static DrugBankRecord parseRecord(Element recordElement) {
		
		//	Only process small molecules
		if(recordElement.getAttributeValue("type") != null
				&& !recordElement.getAttributeValue("type").equals("small molecule")) {
			return null;
		}		
	    Namespace ns = recordElement.getNamespace();
	    List<Element> idList = 
	    		recordElement.getChildren("drugbank-id", ns);
	    String drugId = null;
	    Collection<String>secondaryIds = new ArrayList<String>();
	    for(Element idElement : idList) {
	    	
	    	String isPrimary = idElement.getAttributeValue("primary");
	    	if(isPrimary != null) 
	    		drugId = idElement.getText();
	    	else
	    		secondaryIds.add(idElement.getText());
	    }
		DrugBankRecord record = new DrugBankRecord(drugId);	
		parseExperimentalProperties(recordElement, record, ns);
		parsePredictedProperties(recordElement, record, ns);
		parseCompoundIdentity(recordElement, record, ns);
		parseDatabaseReferences(recordElement, record, ns);		
		parseTimeStamps(recordElement, record, ns);
		parseDescriptions(recordElement, record, ns);
		parseSynonyms(recordElement, record, ns);	

//		parseGeneralReferences(recordElement, record, ns);
		return record;
	}
	
	public static void parseCompoundIdentity(
			Element recordElement, DrugBankRecord record, Namespace ns) {
		
		String name = recordElement.getChildText("name", ns);
		record.setName(name);
		CompoundIdentity cid = record.getCompoundIdentity();
		cid.setCommonName(name);

		record.setAggregateState(recordElement.getChildText("state", ns));
		cid.setFormula(record.getPropertyValue(
				DrugBankCompoundProperties.MOLECULAR_FORMULA.name()));
		cid.setSmiles(record.getPropertyValue(
				DrugBankCompoundProperties.SMILES.name()));
		cid.setInChi(record.getPropertyValue(
				DrugBankCompoundProperties.INCHI.name()));
		cid.setInChiKey(record.getPropertyValue(
				DrugBankCompoundProperties.INCHIKEY.name()));
		
		String massString = record.getPropertyValue(
				DrugBankCompoundProperties.MONOISOTOPIC_WEIGHT.name());
		if(massString != null && !massString.isEmpty()) {
			double mass = Double.parseDouble(massString);
			cid.setExactMass(mass);
		}
	}
	
	public static void parseTimeStamps(
			Element recordElement, DrugBankRecord record, Namespace ns) {

		String dateCreated = recordElement.getAttributeValue("created");
		if(dateCreated != null) {
			
			try {
				Date dc = dateFormat.parse(dateCreated);
				record.setDateCreated(dc);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		String dateUpdated = recordElement.getAttributeValue("created");
		if(dateUpdated != null) {
			
			try {
				Date du = dateFormat.parse(dateUpdated);
				record.setLastUpdated(du);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void parseDescriptions(
			Element recordElement, DrugBankRecord record, Namespace ns) {

		String description = recordElement.getChildText("description", ns);
		if(description != null && !description.isEmpty())
			record.setDescription(StringEscapeUtils.unescapeHtml4(description));
		

	}
	
	public static void parseSynonyms(
			Element recordElement, DrugBankRecord record, Namespace ns){
		
		Element synonymListElement = recordElement.getChild("synonyms", ns);
		if(synonymListElement == null)
			return;
		
		List<Element> synonymsList = 
				synonymListElement.getChildren("synonym", ns);
		if(synonymsList.isEmpty())
			return;
		
		for(Element se : synonymsList) 
			record.getSynonyms().add(se.getText());	
	}
	
	public static void parseDatabaseReferences(
			Element recordElement, DrugBankRecord record, Namespace ns) {

		for(HMDBCrossrefFields dbRef : HMDBCrossrefFields.values()) {
			
			if(dbRef.getDatabase().equals(CompoundDatabaseEnum.HMDB))
				continue;
			
			String dbId = recordElement.getChildText(dbRef.getName(), ns);
			if(dbId != null && !dbId.isEmpty())
				record.getCompoundIdentity().addDbId(dbRef.getDatabase(), dbId);				
		}
		//	Parse secondary HMDB accessions
		Element secondaryAccessionsListElement = 
				recordElement.getChild("secondary_accessions", ns);

//		if(secondaryAccessionsListElement != null) {
//			
//			List<Element> secondaryAccessionsList = 
//					secondaryAccessionsListElement.getChildren("accession", ns);
//			for(Element secondaryAccessionElement : secondaryAccessionsList) 		
//				record.getSecondaryHmdbAccesions().add(secondaryAccessionElement.getText());			
//		}
	}
	
	public static void parseExperimentalProperties(
			Element recordElement, DrugBankRecord record, Namespace ns) {
		
		Element experimentalPropertiesElement = 
				recordElement.getChild("experimental-properties", ns);
		if(experimentalPropertiesElement == null)
			return;
		
		List<Element> propertyList = 
				experimentalPropertiesElement.getChildren("property", ns);
		
		for(Element propertyElement : propertyList) {
			
			CompoundProperty cp = new CompoundProperty(
					propertyElement.getChildText("kind", ns), 
					propertyElement.getChildText("value", ns), 
					propertyElement.getChildText("source", ns),
					CompoundPropertyType.EXPERIMENTAL);
			record.getCompoundProperties().add(cp);
		}
	}

	public static void parsePredictedProperties(
			Element recordElement, DrugBankRecord record, Namespace ns) {
		
		Element predictedPropertiesElement = 
				recordElement.getChild("calculated-properties", ns);
		if(predictedPropertiesElement == null)
			return;
		
		List<Element> propertyList = 
				predictedPropertiesElement.getChildren("property", ns);
		
		for(Element propertyElement : propertyList) {
			
			CompoundProperty cp = new CompoundProperty(
					propertyElement.getChildText("kind", ns), 
					propertyElement.getChildText("value", ns), 
					propertyElement.getChildText("source", ns),
					CompoundPropertyType.PREDICTED);
			record.getCompoundProperties().add(cp);
		}
	}
}
