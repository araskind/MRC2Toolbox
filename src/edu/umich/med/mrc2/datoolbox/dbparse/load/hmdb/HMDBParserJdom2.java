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
		if(mzString != null) {
			double mz = Double.parseDouble(mzString);
			record.getCompoundIdentity().setExactMass(mz);
		}		
		parseDatabaseReferences(recordElement, record, ns);
		parseTimeStamps(recordElement, record, ns);
		parseDescriptions(recordElement, record, ns);
		parseSynonyms(recordElement, record, ns);
		
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
		
		List<Element> synonymsList = 
				recordElement.getChild("synonyms", ns).
				getChildren("synonym", ns);
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
	}
}





















