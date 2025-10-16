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

package edu.umich.med.mrc2.datoolbox.dbparse.load.t3db;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundBioLocation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundLocationType;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCrossrefFields;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBRecord;

public class T3DBParserJdom2 extends HMDBParserJdom2 {

	public static T3DBRecord parseRecord(Element recordElement) {
		
		Namespace ns = recordElement.getNamespace();
		T3DBRecord record = 
				new T3DBRecord(recordElement.getChildText("accession", ns));
		
		parseCompoundIdentity(recordElement, record, ns);
		parseDatabaseReferences(recordElement, record, ns);
		parseTimeStamps(recordElement, record, ns);
		parseTypes(recordElement, record, ns);
		parseCategories(recordElement, record, ns);
		parseT3DBDescriptions(recordElement, record, ns);
		parseSynonyms(recordElement, record, ns);
		parseT3DBBiologicalProperties(recordElement, record, ns);
		parseExperimentalProperties(recordElement, record, ns);
		parsePredictedProperties(recordElement, record, ns);
		parseGeneralReferences(recordElement, record, ns);
		parseTargets(recordElement, record, ns);
		return record;
	}
	
	public static void parseDatabaseReferences(
			Element recordElement, HMDBRecord record, Namespace ns) {

		for(HMDBCrossrefFields dbRef : HMDBCrossrefFields.values()) {
			
			String dbId = recordElement.getChildText(dbRef.getName(), ns);
			if(dbId != null && !dbId.isEmpty())
				record.getCompoundIdentity().addDbId(dbRef.getDatabase(), dbId);				
		}
	}
	
	public static void parseTargets(
			Element recordElement, T3DBRecord record, Namespace ns) {

		Element targetListElement = recordElement.getChild("targets", ns);
		if(targetListElement == null)
			return;
		
		List<Element> targetList = 
				targetListElement.getChildren("target", ns);
		if(targetList.isEmpty())
			return;

		for(Element te : targetList) 
			parseTargetElement(te, ns, record);	
	}

	private static void parseTargetElement(Element targetElement, Namespace ns, T3DBRecord record) {

		String targetId = targetElement.getChildText("target_id", ns);
		String targetName = targetElement.getChildText("name", ns);
		T3DBProteinTarget tgt = new T3DBProteinTarget(targetId, targetName);
		tgt.setUniprotId(targetElement.getChildText("uniprot_id", ns));
		tgt.setMechanismOfAction(targetElement.getChildText("mechanism_of_action", ns));
		record.addProteinTarget(tgt);
		
		Element targetReferencesElement = targetElement.getChild("references", ns);
		if(targetReferencesElement != null) {
			
			List<Element> referenceList = 
					targetReferencesElement.getChildren("reference", ns);
			if(!referenceList.isEmpty()) {
				
				for(Element ref : referenceList)
					record.addReferenceForProteinTarget(
						tgt, HMDBParserJdom2.parseCitationElement(ref, ns));		
			}
		}		
	}
	
	public static void parseT3DBDescriptions(
			Element recordElement, T3DBRecord record, Namespace ns){
		parseDescriptions(recordElement, record, ns);
		for(T3DBToxProperties prop : T3DBToxProperties.values()) {
			
			String toxProp = recordElement.getChildText(prop.name(), ns);
			if(toxProp != null && !toxProp.isEmpty())
				record.getToxicityProperties().put(prop, toxProp);			
		}
	}
	
	public static void parseTypes(
			Element recordElement, T3DBRecord record, Namespace ns){
		
		Element typeListElement = recordElement.getChild("types", ns);
		if(typeListElement == null)
			return;
		
		List<Element> typeList = 
				typeListElement.getChildren("type", ns);
		if(typeList.isEmpty())
			return;
		
		for(Element se : typeList) 
			record.getTypes().add(se.getText());	
	}
	
	public static void parseCategories(
			Element recordElement, T3DBRecord record, Namespace ns){
		
		Element categoryListElement = recordElement.getChild("categories", ns);
		if(categoryListElement == null)
			return;
		
		List<Element> categoryList = 
				categoryListElement.getChildren("category", ns);
		if(categoryList.isEmpty())
			return;
		
		for(Element se : categoryList) 
			record.getCategories().add(se.getText());	
	}
	
	public static void parseT3DBBiologicalProperties(
			Element recordElement, T3DBRecord record, Namespace ns) {
			
		// Cellular
		Element cellLocationsElement = 
				recordElement.getChild("cellular_locations", ns);

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
				recordElement.getChild("biospecimen_locations", ns);

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
				recordElement.getChild("biofluid_locations", ns);

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
				recordElement.getChild("tissue_locations", ns);

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
				recordElement.getChild("pathways", ns);
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
}
