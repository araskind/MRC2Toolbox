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
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;

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
		record.getSecondaryIds().addAll(secondaryIds);
		
		parseExperimentalProperties(recordElement, record, ns);
		parsePredictedProperties(recordElement, record, ns);
		parseCompoundIdentity(recordElement, record, ns);
		parseDatabaseReferences(recordElement, record, ns);		
		parseTimeStamps(recordElement, record, ns);
		parseDescriptions(recordElement, record, ns);
		parseSynonyms(recordElement, record, ns);
		parseDrugCategories(recordElement, record, ns);
		parseExternalLinks(recordElement, record, ns);
		parseDrugPathways(recordElement, record, ns);
		parseDrugTargets(recordElement, record, ns);
		parseGeneralReferences(recordElement, record, ns);
		return record;
	}

	public static void parseCompoundIdentity(
			Element recordElement, DrugBankRecord record, Namespace ns) {
		
		String name = recordElement.getChildText("name", ns);
		record.setName(name);
		CompoundIdentity cid = record.getCompoundIdentity();
		cid.setCommonName(name);
		cid.setSysName(record.getPropertyValue(
				DrugBankCompoundProperties.TRADITIONAL_IUPAC_NAME.getName()));
		record.setAggregateState(recordElement.getChildText("state", ns));
		cid.setFormula(record.getPropertyValue(
				DrugBankCompoundProperties.MOLECULAR_FORMULA.getName()));
		cid.setSmiles(record.getPropertyValue(
				DrugBankCompoundProperties.SMILES.getName()));
		cid.setInChi(record.getPropertyValue(
				DrugBankCompoundProperties.INCHI.getName()));
		cid.setInChiKey(record.getPropertyValue(
				DrugBankCompoundProperties.INCHIKEY.getName()));
		
		String massString = record.getPropertyValue(
				DrugBankCompoundProperties.MONOISOTOPIC_WEIGHT.getName());
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
		String dateUpdated = recordElement.getAttributeValue("updated");
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
		
		for(DrugBankDescriptiveFields field : DrugBankDescriptiveFields.values()) {
			
			String descriptionString = 
					recordElement.getChildText(field.getName(), ns);
			if(descriptionString != null && !descriptionString.isEmpty())
				record.setDescriptiveField(
						field, 
						StringEscapeUtils.unescapeHtml4(descriptionString));
		}
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
			Element recordElement, 
			DrugBankRecord record, 
			Namespace ns) {

		String unii = recordElement.getChildText(DrugbankCrossrefFields.UNII.getName(), ns);
		if(unii != null && !unii.isEmpty())
			record.getCompoundIdentity().addDbId(CompoundDatabaseEnum.FDA_UNII, unii);
			
		String cas = recordElement.getChildText(DrugbankCrossrefFields.CAS.getName(), ns);
		if(cas != null && !cas.isEmpty())
			record.getCompoundIdentity().addDbId(CompoundDatabaseEnum.CAS, cas);
		
		record.getCompoundIdentity().addDbId(
				CompoundDatabaseEnum.DRUGBANK, record.getPrimaryId());
		
		Element externalIdentifiersElement = 
				recordElement.getChild("external-identifiers", ns);
		if(externalIdentifiersElement == null)
			return;
		
		List<Element>idList = 
				externalIdentifiersElement.getChildren("external-identifier", ns);
		for(Element idElement : idList) {
			
			DrugbankCrossrefFields field = 
					DrugbankCrossrefFields.getFieldByName(
							idElement.getChildText("resource", ns));
			String id = idElement.getChildText("identifier", ns);
			if(field != null && id != null && !id.isEmpty())
				record.getCompoundIdentity().addDbId(field.getDatabase(), id);
		}
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
	
	private static void parseDrugCategories(
			Element recordElement, 
			DrugBankRecord record, 
			Namespace ns) {
		Element categoriesElement = 
				recordElement.getChild("categories", ns);
		if(categoriesElement == null)
			return;
		
		List<Element> categoryList = 
				categoriesElement.getChildren("category", ns);
		
		for(Element categoryElement : categoryList) {
			
			String mesh = categoryElement.getChildText("mesh-id", ns);
			if(mesh != null && mesh.isEmpty())
				mesh = null;
			
			DrugCategory cp = new DrugCategory(
					categoryElement.getChildText("category", ns), mesh);
			record.addCategory(cp);
		}
	}

	private static void parseExternalLinks(
			Element recordElement, 
			DrugBankRecord record, 
			Namespace ns) {
		
		Element elListElement = 
				recordElement.getChild("external-links", ns);
		if(elListElement == null)
			return;
		
		List<Element> elList = 
				elListElement.getChildren("external-link", ns);
		
		for(Element elElement : elList) {
						
			DrugBankExternalLink cp = new DrugBankExternalLink(
					elElement.getChildText("resource", ns), 
					elElement.getChildText("url", ns));
			record.getExternalLinks().add(cp);
		}		
	}
	
	private static void parseDrugPathways(
			Element recordElement, 
			DrugBankRecord 
			record, Namespace ns) {
		
		Element pathwayListElement = 
				recordElement.getChild("pathways", ns);
		if(pathwayListElement == null)
			return;
		
		List<Element> pathwayList = 
				pathwayListElement.getChildren("pathway", ns);
		
		for(Element pathwayElement : pathwayList) {
										
			DrugPathway pw = new DrugPathway(
					pathwayElement.getChildText("name", ns), 
					pathwayElement.getChildText("smpdb-id", ns));
			String category = pathwayElement.getChildText("category", ns);
			if(category != null && !category.isEmpty())
				pw.setCategory(category);
			
			Element drugListElement = 
					pathwayElement.getChild("drugs", ns);
			if(drugListElement != null) {
				
				List<Element> drugList = 
						drugListElement.getChildren("drug", ns);
				for(Element drugElement : drugList)
					pw.getDrugs().add(drugElement.getChildText("drugbank-id", ns));
			}		
			Element enzymeListElement = 
					pathwayElement.getChild("enzymes", ns);
			if(enzymeListElement != null) {
				
				List<Element> enzymeList = 
						enzymeListElement.getChildren("uniprot-id", ns);
				for(Element enzymeElement : enzymeList)
					pw.getEnzymes().add(enzymeElement.getText());
			}		
			record.getPathways().add(pw);
		}
	}
		
	private static void parseGeneralReferences(
			Element recordElement, 
			DrugBankRecord record, 
			Namespace ns) {
		
		Element genrefListElement = 
				recordElement.getChild("general-references", ns);
		if(genrefListElement == null)
			return;
		
		Element articleListElement = 
				genrefListElement.getChild("articles", ns);
		if(articleListElement == null)
			return;
		
		List<Element> refList = 
				articleListElement.getChildren("article", ns);
		
		for(Element refElement : refList) {
						
			HMDBCitation citation = parseCitationElement(refElement, ns);
			record.getReferences().add(citation);
		}	
	}
	
	public static HMDBCitation parseCitationElement(
			Element citationElement, Namespace ns) {
		
		String refText = citationElement.getChildText("citation", ns);
		String pubMedId = citationElement.getChildText("pubmed-id", ns);
		HMDBCitation citation = new HMDBCitation(refText, pubMedId);
		String refId = citationElement.getChildText("ref-id", ns);
		if(refId != null && !refId.isEmpty())
			citation.setRefId(refId);
		
		return citation;
	}
	
	private static void parseDrugTargets(
			Element recordElement, 
			DrugBankRecord record, 
			Namespace ns) {

		Element targetListElement = 
				recordElement.getChild("targets", ns);
		if(targetListElement == null)
			return;
		
		List<Element> targetList = 
				targetListElement.getChildren("target", ns);
		
		for(Element targetElement : targetList) {
					
			DrugTarget dt = new DrugTarget(
					targetElement.getChildText("id", ns), 
					targetElement.getChildText("name", ns));
			String organism = targetElement.getChildText("organism", ns);
			if(organism != null && !organism.isEmpty())
				dt.setOrganizm(organism);
			
			Element actionListElement = 
					targetElement.getChild("actions", ns);
			if(actionListElement != null) {
				
				List<Element> actionList = 
						actionListElement.getChildren("action", ns);
				for(Element actionElement : actionList)
					dt.getActions().add(actionElement.getText());
			}	
			Element refListElement = 
					targetElement.getChild("references", ns);
			if(refListElement != null) {
				
				Element articleListElement = 
						refListElement.getChild("articles", ns);
				if(articleListElement != null) {
					
					List<Element> refList = 
							articleListElement.getChildren("article", ns);
					
					for(Element refElement : refList) {
									
						HMDBCitation citation = parseCitationElement(refElement, ns);
						dt.getReferences().add(citation);
					}
				}					
			}
			record.getDrugTargets().add(dt);
		}
	}
}










