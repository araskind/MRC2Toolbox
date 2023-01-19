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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundPropertyType;

public class HMDBParserJdom2 {

	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static HMDBRecord parseRecord(Element recordElement) {
		
		Namespace ns = recordElement.getNamespace();
		String id = recordElement.getChildText("accession", ns);
		HMDBRecord record = new HMDBRecord(id);
		String name = recordElement.getChildText("name", ns);
		record.setName(name);
		record.getCompoundIdentity().setCommonName(name);

		String sysName = recordElement.getChildText("iupac_name", ns);
		record.setSysName(sysName);
		record.getCompoundIdentity().setSysName(sysName);
		record.setTraditionalIupacName(recordElement.getChildText("traditional_iupac", ns));
		record.setAggregateState(recordElement.getChildText("state", ns));
		record.getCompoundIdentity().setFormula(recordElement.getChildText("chemical_formula", ns));
		record.getCompoundIdentity().setSmiles(recordElement.getChildText("smiles", ns));
		record.getCompoundIdentity().setInChi(recordElement.getChildText("inchi", ns));
		record.getCompoundIdentity().setInChiKey(recordElement.getChildText("inchikey", ns));
		
		String mzString = recordElement.getChildText("monisotopic_molecular_weight", ns);
		if(mzString != null && !mzString.isEmpty()) {
			double mz = Double.parseDouble(mzString);
			record.getCompoundIdentity().setExactMass(mz);
		}		
		parseDatabaseReferences(recordElement, record, ns);
		parseTimeStamps(recordElement, record, ns);
		parseDescriptions(recordElement, record, ns);
		parseSynonyms(recordElement, record, ns);
		parseBiologicalProperties(recordElement, record, ns);
		parseExperimentalProperties(recordElement, record, ns);
		parsePredictedProperties(recordElement, record, ns);
		parseConcentrations(recordElement, record, ns);
		parseDeseases(recordElement, record, ns);
		parseProteinAssociations(recordElement, record, ns);
		parseGeneralReferences(recordElement, record, ns);
		return record;
	}
	
	public static void parseTimeStamps(
			Element recordElement, HMDBRecord record, Namespace ns) {

		String dateCreated = recordElement.getChildText("creation_date", ns).replace(" UTC", "");
		try {
			Date dc = dateFormat.parse(dateCreated);
			record.setDateCreated(dc);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String dateUpdated = recordElement.getChildText("update_date", ns).replace(" UTC", "");
		try {
			Date du = dateFormat.parse(dateUpdated);
			record.setLastUpdated(du);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static void parseDescriptions(
			Element recordElement, HMDBRecord record, Namespace ns) {

		String description = recordElement.getChildText("description", ns);
		if(description != null && !description.isEmpty())
			record.setDescription(StringEscapeUtils.unescapeHtml4(description));
		
		String scDescription = recordElement.getChildText("cs_description", ns);
		if(scDescription != null && !scDescription.isEmpty())
			record.setCsDescription(StringEscapeUtils.unescapeHtml4(scDescription));
	}
	
	public static void parseSynonyms(
			Element recordElement, HMDBRecord record, Namespace ns){
		
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
			Element recordElement, HMDBRecord record, Namespace ns) {

		for(HMDBCrossrefFields dbRef : HMDBCrossrefFields.values()) {
			
			String dbId = recordElement.getChildText(dbRef.getName(), ns);
			if(dbId != null && !dbId.isEmpty())
				record.getCompoundIdentity().addDbId(dbRef.getDatabase(), dbId);				
		}
		//	Parse secondary HMDB accessions
		Element secondaryAccessionsListElement = 
				recordElement.getChild("secondary_accessions", ns);

		if(secondaryAccessionsListElement != null) {
			
			List<Element> secondaryAccessionsList = 
					secondaryAccessionsListElement.getChildren("accession", ns);
			for(Element secondaryAccessionElement : secondaryAccessionsList) 		
				record.getSecondaryHmdbAccesions().add(secondaryAccessionElement.getText());			
		}
	}
	
	public static void parseBiologicalProperties(
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		Element biologicalPropertiesElement = 
				recordElement.getChild("biological_properties", ns);
		if(biologicalPropertiesElement == null)
			return;
		
		// Cellular
		Element cellLocationsElement = 
				biologicalPropertiesElement.getChild("cellular_locations", ns);

		if(cellLocationsElement != null) {
			
			List<Element> cellLocations = 
					cellLocationsElement.getChildren("cellular", ns);
			for(Element cellLocation : cellLocations) {
				
				record.getBiolocations().add(
						new CompoundBioLocation(cellLocation.getText(),
									CompoundLocationType.CELLULAR));
			}
		}
		// Biospecimen
		Element biospecimenLocationsElement = 
				biologicalPropertiesElement.getChild("biospecimen_locations", ns);

		if(biospecimenLocationsElement != null) {
			
			List<Element> biospecimenLocations = 
					biospecimenLocationsElement.getChildren("biospecimen", ns);
			for(Element biospecimenLocation : biospecimenLocations) {
				
				record.getBiolocations().add(
						new CompoundBioLocation(biospecimenLocation.getText(),
									CompoundLocationType.BIOSPECIMEN));
			}
		}
		// Biofluid
		Element biofluidLocationsElement = 
				biologicalPropertiesElement.getChild("biofluid_locations", ns);

		if(biofluidLocationsElement != null) {
			
			List<Element> biofluidLocations = 
					biofluidLocationsElement.getChildren("biofluid", ns);
			for(Element biofluidLocation : biofluidLocations) {
				
				record.getBiolocations().add(
						new CompoundBioLocation(biofluidLocation.getText(),
									CompoundLocationType.BIOFLUID));
			}
		}
		// Tissue
		Element tissueLocationsElement = 
				biologicalPropertiesElement.getChild("tissue_locations", ns);

		if(tissueLocationsElement != null) {
			
			List<Element> tissueLocations = 
					tissueLocationsElement.getChildren("tissue", ns);
			for(Element tissueLocation : tissueLocations) {
				
				record.getBiolocations().add(
						new CompoundBioLocation(tissueLocation.getText(),
									CompoundLocationType.TISSUE));
			}
		}
		//	Pathways
		Element pathwayListElement = 
				biologicalPropertiesElement.getChild("pathways", ns);
		if(pathwayListElement != null) {
			
			List<Element> pathwayList = 
					pathwayListElement.getChildren("pathway", ns);
			
			for(Element pathwayElement : pathwayList) {
				
				HMDBPathway patwayEntry = 
						new HMDBPathway(
								pathwayElement.getChildText("name", ns), 
								pathwayElement.getChildText("smpdb_id", ns), 
								pathwayElement.getChildText("kegg_map_id", ns));
				record.getPathways().add(patwayEntry);
			}
		}
	}
	
	public static void parseExperimentalProperties(
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		Element experimentalPropertiesElement = 
				recordElement.getChild("experimental_properties", ns);
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
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		Element predictedPropertiesElement = 
				recordElement.getChild("predicted_properties", ns);
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
	
	public static void parseConcentrations(
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		//	Normal
		Element normalConcentrationsElement = 
				recordElement.getChild("normal_concentrations", ns);
		if(normalConcentrationsElement != null) {
			
			List<Element> normalConcentrationsList = 
					normalConcentrationsElement.getChildren("concentration", ns);
			for(Element ncElement : normalConcentrationsList) {
				
				CompoundConcentration nc = parseConcentrationElement(
						ncElement, ConcentrationType.NORMAL, ns);
				record.getConcentrations().add(nc);
			}
		}
		//	Abnormal
		Element abnormalConcentrationsElement = 
				recordElement.getChild("abnormal_concentrations", ns);
		if(abnormalConcentrationsElement != null) {
			
			List<Element> abnormalConcentrationsList = 
					abnormalConcentrationsElement.getChildren("concentration", ns);
			for(Element acElement : abnormalConcentrationsList) {
				
				CompoundConcentration ac = parseConcentrationElement(
						acElement, ConcentrationType.ABNORMAL, ns);
				record.getConcentrations().add(ac);
			}
		}
	}
	
	public static CompoundConcentration parseConcentrationElement(
			Element concElement, ConcentrationType type, Namespace ns) {
		CompoundConcentration cc = null;
		
		String biospecimen = concElement.getChildText("biospecimen", ns);
		String value = concElement.getChildText("concentration_value", ns);
		String units = concElement.getChildText("concentration_units", ns);
		cc = new CompoundConcentration(biospecimen, value, units, type);
		
		if(type.equals(ConcentrationType.NORMAL)) {
			cc.setAge(concElement.getChildText("subject_age", ns));
			cc.setSex(concElement.getChildText("subject_sex", ns));
			cc.setCondition(concElement.getChildText("subject_condition", ns));
		}
		if(type.equals(ConcentrationType.ABNORMAL)) {
			cc.setAge(concElement.getChildText("patient_age", ns));
			cc.setSex(concElement.getChildText("patient_sex", ns));
			cc.setCondition(concElement.getChildText("patient_information", ns));
		}
		cc.setComment(concElement.getChildText("comment", ns));
		Element citationListElement = 
				concElement.getChild("references", ns);
		if(citationListElement != null) {
			
			List<Element> citationList = 
					citationListElement.getChildren("reference", ns);
			
			for(Element citationElement : citationList) {
				
				HMDBCitation ref = parseCitationElement(citationElement, ns);
				cc.getReferences().add(ref);
			}
		}		
		return cc;
	}	
	
	public static HMDBCitation parseCitationElement(
			Element citationElement, Namespace ns) {
		
		String refText = citationElement.getChildText("reference_text", ns);
		String pubMedId = citationElement.getChildText("pubmed_id", ns);;
		return new HMDBCitation(refText, pubMedId);
	}
	
	public static void parseDeseases(
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		Element diseasesElement = 
				recordElement.getChild("diseases", ns);
		if(diseasesElement != null) {
			
			List<Element> diseasesList = 
					diseasesElement.getChildren("disease", ns);
			for(Element desElement : diseasesList) {
				
				String deseaseName = desElement.getChildText("name", ns);
				String omimId = desElement.getChildText("omim_id", ns);
				HMDBDesease des = new HMDBDesease(deseaseName, omimId);
				
				Element citationListElement = 
						desElement.getChild("references", ns);
				if(citationListElement != null) {
					
					List<Element> citationList = 
							citationListElement.getChildren("reference", ns);
					
					for(Element citationElement : citationList) {
						
						HMDBCitation ref = parseCitationElement(citationElement, ns);
						des.getReferences().add(ref);
					}
				}
				record.getDeseases().add(des);
			}
		}
	}
	
	public static void parseProteinAssociations(
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		Element proteinAssociationListElement = 
				recordElement.getChild("protein_associations", ns);
		if(proteinAssociationListElement != null) {
			
			List<Element> proteinAssociationList = 
					proteinAssociationListElement.getChildren("protein", ns);
			
			for(Element proteinAssociationElement : proteinAssociationList) {
				
				HMDBProteinAssociation pa = new HMDBProteinAssociation(
						proteinAssociationElement.getChildText("protein_accession", ns), 
						proteinAssociationElement.getChildText("name", ns),
						proteinAssociationElement.getChildText("uniprot_id", ns),
						proteinAssociationElement.getChildText("gene_name", ns), 
						proteinAssociationElement.getChildText("protein_type", ns));
				record.getProteinAssociations().add(pa);;
			}
		}		
	}
	
	public static void parseGeneralReferences(
			Element recordElement, HMDBRecord record, Namespace ns) {
		
		Element citationListElement = 
				recordElement.getChild("general_references", ns);
		if(citationListElement != null) {
			
			List<Element> citationList = 
					citationListElement.getChildren("reference", ns);
			
			for(Element citationElement : citationList) {
				
				HMDBCitation ref = parseCitationElement(citationElement, ns);
				record.getReferences().add(ref);
			}
		}
	}
}





















