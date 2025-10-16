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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundBioLocation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.ReferenceObjectType;

public class T3DBParser {

	public static T3DBRecord parseRecord(Element recordElement) {

		Namespace ns = recordElement.getNamespace();
		String id = recordElement.getChildText("accession", ns);
		T3DBRecord record = new T3DBRecord(id);
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
		HMDBParserJdom2.parseTimeStamps(recordElement, record, ns);
		HMDBParserJdom2.parseDescriptions(recordElement, record, ns);
		HMDBParserJdom2.parseSynonyms(recordElement, record, ns);
		HMDBParserJdom2.parseDatabaseReferences(recordElement, record, ns);
		HMDBParserJdom2.parseBiologicalProperties(recordElement, record, ns);
		HMDBParserJdom2.parseGeneralReferences(recordElement, record, ns);
		
		parseToxicityProperties(recordElement, record, ns);
  		parseCategories(recordElement, record, ns);
  		parseTypes(recordElement, record, ns);
  		parseTargets(recordElement, record, ns);
  		
  		return record;
	}

	private static void parseToxicityProperties(
			Element recordElement, 
			T3DBRecord record, 			
			Namespace ns) {
		
		Map<T3DBToxProperties, String> toxicityProperties = record.getToxicityProperties();
		for(T3DBToxProperties prop : T3DBToxProperties.values()) {
			
			String toxProp = recordElement.getChildText(prop.name(), ns);
			if(toxProp != null)
				toxicityProperties.put(prop, toxProp);			
		}

//		if (recordElement.getElementsByTagName("origin").item(0).getFirstChild() != null)
//			record.setOrigin(recordElement.getElementsByTagName("origin").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("status").item(0).getFirstChild() != null)
//			record.setStatus(recordElement.getElementsByTagName("status").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("route_of_exposure").item(0).getFirstChild() != null)
//			record.setRouteOfExposure(recordElement.getElementsByTagName("route_of_exposure").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("mechanism_of_toxicity").item(0).getFirstChild() != null)
//			record.setMechanismOfToxicity(recordElement.getElementsByTagName("mechanism_of_toxicity").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("metabolism").item(0).getFirstChild() != null)
//			record.setMetabolism(recordElement.getElementsByTagName("metabolism").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("toxicity").item(0).getFirstChild() != null)
//			record.setToxicity(recordElement.getElementsByTagName("toxicity").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("lethaldose").item(0).getFirstChild() != null)
//			record.setLethaldose(recordElement.getElementsByTagName("lethaldose").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("carcinogenicity").item(0).getFirstChild() != null)
//			record.setCarcinogenicity(recordElement.getElementsByTagName("carcinogenicity").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("use_source").item(0).getFirstChild() != null)
//			record.setUsage(recordElement.getElementsByTagName("use_source").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("min_risk_level").item(0).getFirstChild() != null)
//			record.setMinRiskLevel(recordElement.getElementsByTagName("min_risk_level").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("health_effects").item(0).getFirstChild() != null)
//			record.setHealthEffects(recordElement.getElementsByTagName("health_effects").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("symptoms").item(0).getFirstChild() != null)
//			record.setSymptoms(recordElement.getElementsByTagName("symptoms").item(0).getFirstChild().getNodeValue());
//
//		if (recordElement.getElementsByTagName("treatment").item(0).getFirstChild() != null)
//			record.setTreatment(recordElement.getElementsByTagName("treatment").item(0).getFirstChild().getNodeValue());
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
		
		Collection<T3DBTarget> targets = record.getTargets();
		for(Element te : targetList) 
			targets.add(parseTargetElement(te, ns));	
	}

	private static T3DBTarget parseTargetElement(Element targetElement, Namespace ns) {

		String targetId = targetElement.getChildText("target_id", ns);
		String targetName = targetElement.getChildText("name", ns);
		T3DBTarget tgt = new T3DBTarget(targetId, targetName);
		tgt.setUniprotId(targetElement.getChildText("uniprot_id", ns));
		tgt.setMechanismOfAction(targetElement.getChildText("mechanism_of_action", ns));
		
		Element targetReferencesElement = targetElement.getChild("references", ns);
		if(targetReferencesElement != null) {
			
			List<Element> referenceList = 
					targetReferencesElement.getChildren("reference", ns);
			if(!referenceList.isEmpty()) {
				
				for(Element ref : referenceList)
					tgt.getReferences().add(HMDBParserJdom2.parseCitationElement(ref, ns));			
			}
		}

//		String targetId = "";
//		Node idElement = targetElement.getElementsByTagName("target_id").item(0).getFirstChild();
//		if (idElement != null)
//			targetId = idElement.getNodeValue();
//
//		String targetName = "";
//		Node nameElement = targetElement.getElementsByTagName("name").item(0).getFirstChild();
//		if (nameElement != null)
//			targetName = nameElement.getNodeValue();
//
//		tgt = new T3DBTarget(targetId, targetName);
//
//		Node uniprotElement = targetElement.getElementsByTagName("uniprot_id").item(0).getFirstChild();
//		if (uniprotElement != null)
//			tgt.setUniprotId(uniprotElement.getNodeValue());
//
//		Node mechanismOfActionElement = targetElement.getElementsByTagName("mechanism_of_action").item(0).getFirstChild();
//		if (mechanismOfActionElement != null)
//			tgt.setMechanismOfAction(mechanismOfActionElement.getNodeValue());
//
//		// References
//		NodeList refs = targetElement.getElementsByTagName("references");
//		if(refs.getLength() > 0) {
//
//			Element refElement = (Element) refs.item(0);
//			NodeList refList = refElement.getElementsByTagName("reference");
//			for(int i=0; i<refList.getLength(); i++) {
//
//				HMDBCitation ref = HMDBParserJdom2.parseCitationElement((Element) refList.item(i));
//				tgt.getReferences().add(ref);
//			}
//		}
		return tgt;
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
		
		for(Element typeElement : typeList)
			record.getTypes().add(typeElement.getTextTrim());
		
			
//		NodeList types = recordElement.getElementsByTagName("types");
//		if(types.getLength() > 0) {
//
//			Element typeElement = (Element) types.item(0);
//			NodeList typeList = typeElement.getElementsByTagName("type");
//			for(int i=0; i<typeList.getLength(); i++)
//				record.getTypes().add(typeList.item(i).getFirstChild().getNodeValue());
//		}
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
		
		for(Element typeElement : categoryList)
			record.getTypes().add(typeElement.getTextTrim());

//		NodeList categories = recordElement.getElementsByTagName("categories");
//		if(categories.getLength() > 0) {
//
//			Element categoryElement = (Element) categories.item(0);
//			NodeList catList = categoryElement.getElementsByTagName("category");
//			for(int i=0; i<catList.getLength(); i++)
//				record.getCategories().add(catList.item(i).getFirstChild().getNodeValue());
//		}
	}

	/**
	 * Upload record to database
	 */
	public static void insertRecord(T3DBRecord record, Connection conn) {

		try {
			loadMainRecordData(record, conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadSynonyms(record, conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadDatabaseReferences(record, conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadBioLocations(record, conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadPathways(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadCategories(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadTypes(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadDrugTargets(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadGeneralReferences(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void loadCategories(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO T3DB_CATEGORIES (ACCESSION, CATEGORY) VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		for (String cat : record.getCategories()) {
			ps.setString(2, cat);
			ps.executeUpdate();
		}
		ps.close();
	}

	private static void loadTypes(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO T3DB_TYPES (ACCESSION, TYPE) VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		for (String cat : record.getTypes()) {
			ps.setString(2, cat);
			ps.executeUpdate();
		}
		ps.close();
	}

	private static void loadDrugTargets(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO T3DB_DRUG_TARGETS (DT_ID, ACCESSION, TARGET_ID, NAME, UNIPROT_ID, MECHANISM) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(2, record.getPrimaryId());

		for (T3DBTarget target : record.getTargets()) {

			ps.setString(1, target.getUniqueId());

			ps.setString(3, target.getTargetId());
			ps.setString(4, target.getName());

			if(target.getUniprotId() != null)
				ps.setString(5, target.getUniprotId());
			else
				ps.setString(5, "");

			if(target.getMechanismOfAction() != null)
				ps.setString(6, target.getMechanismOfAction());
			else
				ps.setString(6, "");

			ps.executeUpdate();

			for(HMDBCitation reference : target.getReferences())
				insertReference(record.getPrimaryId(), reference, target.getUniqueId(), ReferenceObjectType.DRUG_TARGET, conn);
		}
		ps.close();
	}

	private static void loadGeneralReferences(T3DBRecord record, Connection conn) {

		for(HMDBCitation ref : record.getReferences()) {
			try {
				insertReference(record.getPrimaryId(), ref, record.getPrimaryId(), ReferenceObjectType.COMPOUND, conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void insertReference(String accession,
			HMDBCitation reference, String parentObjectId, ReferenceObjectType type, Connection conn) throws SQLException {

		String dataQuery =
				"INSERT INTO T3DB_CITATIONS " +
				"(ACCESSION, SOURCE_DB, SOURCE_DB_ID, REF_ID, OBJECT_TYPE, OBJECT_ID, CITATION_TEXT) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		String sourceDb = "";
		String sourceDbId = "";
		if(reference.getPubmedId() != null) {
			sourceDb = CompoundDatabaseEnum.PUBMED.name();
			sourceDbId = reference.getPubmedId();
		}
		ps.setString(1, accession);
		ps.setString(2, sourceDb);
		ps.setString(3, sourceDbId);
		ps.setString(4, reference.getUniqueId());
		ps.setString(5, type.name());
		ps.setString(6, parentObjectId);
		ps.setString(7, reference.getCitationText());

		ps.executeUpdate();
		ps.close();
	}

	private static void loadPathways(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery =
				"INSERT INTO T3DB_PATHWAYS (ACCESSION, PATHWAY_NAME, KEGG_MAP_ID, SMPDB_ID) VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(dataQuery);
			ps.setString(1, record.getPrimaryId());

			for (HMDBPathway pathway : record.getPathways()) {

				ps.setString(2, pathway.getName());

				String keggMap = pathway.getKeggMapId();
				if(keggMap == null)
					keggMap = "";

				ps.setString(3, keggMap);

				String smpdb = pathway.getSmpdbId();
				if(smpdb == null)
					smpdb = "";

				ps.setString(4, smpdb);
				ps.executeUpdate();
			}
			ps.close();
	}

	private static void loadBioLocations(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO T3DB_BIOLOCATIONS (ACCESSION, LOCATION_TYPE, LOCATION) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		for (CompoundBioLocation location : record.getBiolocations()) {
			ps.setString(2, location.getLocationType().name());
			ps.setString(3, location.getLocationName());
			ps.executeUpdate();
		}
		ps.close();
	}

	private static void loadDatabaseReferences(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO T3DB_CROSSREF (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		for (Entry<CompoundDatabaseEnum, String> entry : record.getCompoundIdentity().getDbIdMap().entrySet()) {

			ps.setString(2, entry.getKey().name());
			ps.setString(3, entry.getValue());
			ps.executeUpdate();
		}
		ps.close();
	}

	private static void loadSynonyms(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO T3DB_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		//	Primary name
		ps.setString(2, record.getName());
		ps.setString(3, "PRI");
		ps.executeUpdate();

		//	IUPAC
		String iupac = record.getSysName();
		if(iupac != null) {
			ps.setString(2, iupac);
			ps.setString(3, "IUP");
			ps.executeUpdate();
		}
		//	Synonyms
		for(String syn : record.getSynonyms()) {
			ps.setString(2, syn);
			ps.setString(3, "SYN");
			ps.executeUpdate();
		}
		ps.close();
	}

	private static void loadMainRecordData(T3DBRecord record, Connection conn) throws SQLException {

		String dataQuery =
				"INSERT INTO T3DB_COMPOUND_DATA " +
				"(ACCESSION, CREATION_DATE, UPDATE_DATE, COMMON_NAME, IUPAC_NAME, CHEMICAL_FORMULA, " +
				"MONISOTOPIC_MOLECULATE_WEIGHT, SMILES, INCHI, INCHIKEY, STATUS, " +
				"ORIGIN, AGGREGATE_STATE, DESCRIPTION, APPEARANCE, ROUTE_OF_EXPOSURE, MECHANISM_OF_TOXICITY, " +
				"METABOLISM, TOXICITY, LETHALDOSE, CARCINOGENICITY, USE_SOURCE, MIN_RISK_LEVEL, " +
				"HEALTH_EFFECTS, SYMPTOMS, TREATMENT) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		ps.setDate(2, new java.sql.Date(record.getDateCreated().getTime()));
		ps.setDate(3, new java.sql.Date(record.getLastUpdated().getTime()));

		ps.setString(4, record.getName());
		ps.setString(5, record.getSysName());

		String formula = record.getCompoundIdentity().getFormula();
		if(formula == null)
			formula = "";

		ps.setString(6, formula);

		if(record.getCompoundIdentity().getExactMass() > 0.0d)
			ps.setDouble(7, record.getCompoundIdentity().getExactMass());
		else
			ps.setString(7, "");

		String smiles = record.getCompoundIdentity().getSmiles();
		if(smiles == null)
			smiles = "";

		ps.setString(8, smiles);

		String inchi = record.getCompoundIdentity().getInChi();
		if(inchi == null)
			inchi = "";

		ps.setString(9, inchi);

		String inchiKey = record.getCompoundIdentity().getInChiKey();
		if(inchiKey == null)
			inchiKey = "";

		ps.setString(10, inchiKey);

		//****//
//		String status = record.getStatus();
//		if(status == null)
//			status = "";
//
//		ps.setString(11, status);
//
//		String origin = record.getOrigin();
//		if(origin == null)
//			origin = "";

//		ps.setString(12, origin);
//
//		String aggState = record.getAggregateState();
//		if(aggState == null)
//			aggState = "";

//		ps.setString(13, aggState);

//		DESCRIPTION
		String description = record.getDescription();
		if(description == null)
			description = "";

		ps.setString(14, description);

//		APPEARANCE
		String appearance = record.getAggregateState();
		if(appearance == null)
			appearance = "";

		ps.setString(15, appearance);
//
////		ROUTE_OF_EXPOSURE
//		String route_of_exposure = record.getRouteOfExposure();
//		if(route_of_exposure == null)
//			route_of_exposure = "";
//
//		ps.setString(16, route_of_exposure);
//
//
////		MECHANISM_OF_TOXICITY
//		String mechanism_of_toxicity = record.getMechanismOfToxicity();
//		if(mechanism_of_toxicity == null)
//			mechanism_of_toxicity = "";
//
//		ps.setString(17, mechanism_of_toxicity);
//
////		METABOLISM
//		String metabolism = record.getMetabolism();
//		if(metabolism == null)
//			metabolism = "";
//
//		ps.setString(18, metabolism);
//
////		TOXICITY
//		String toxicity = record.getToxicity();
//		if(toxicity == null)
//			toxicity = "";
//
//		ps.setString(19, toxicity);
//
////		LETHALDOSE
//		String lethaldose = record.getLethaldose();
//		if(lethaldose == null)
//			lethaldose = "";
//
//		ps.setString(20, lethaldose);
//
////		CARCINOGENICITY
//		String carcinogenicity = record.getCarcinogenicity();
//		if(carcinogenicity == null)
//			carcinogenicity = "";
//
//		ps.setString(21, carcinogenicity);
//
////		USE_SOURCE
//		String use_source = record.getUsage();
//		if(use_source == null)
//			use_source = "";
//
//		ps.setString(22, use_source);
//
////		MIN_RISK_LEVEL
//		String min_risk_level = record.getMinRiskLevel();
//		if(min_risk_level == null)
//			min_risk_level = "";
//
//		ps.setString(23, min_risk_level);
//
////		HEALTH_EFFECTS
//		String health_effects = record.getHealthEffects();
//		if(health_effects == null)
//			health_effects = "";
//
//		ps.setString(24, health_effects);
//
////		SYMPTOMS
//		String symptoms = record.getSymptoms();
//		if(symptoms == null)
//			symptoms = "";
//
//		ps.setString(25, symptoms);
//
////		TREATMENT
//		String treatment = record.getTreatment();
//		if(treatment == null)
//			treatment = "";

//		ps.setString(26, treatment);

		ps.executeUpdate();
		ps.close();
	}









































}
