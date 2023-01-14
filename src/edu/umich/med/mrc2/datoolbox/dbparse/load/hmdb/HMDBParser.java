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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class HMDBParser {

	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static HMDBRecord parseRecord(Node recordDocument) throws ParserConfigurationException {

		Element recordElement = (Element)recordDocument;
		String id = recordElement.getElementsByTagName("accession").item(0).getFirstChild().getNodeValue();
		HMDBRecord record = new HMDBRecord(id);
		//	Timestamps
		try {
			parseTimeStamps(recordElement, record);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		//	Descriptions
		try {
			parseDescriptions(recordElement, record);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//	Synonyms
  		try {
			parseSynonyms(recordElement, record);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//	Name & IUPAC name
		String name = recordElement.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		record.setName(name);
		record.getCompoundIdentity().setCommonName(name);

		if (recordElement.getElementsByTagName("iupac_name").item(0).getFirstChild() != null)
			record.setSysName(recordElement.getElementsByTagName("iupac_name").item(0).getFirstChild().getNodeValue());

		if (recordElement.getElementsByTagName("state").item(0).getFirstChild() != null)
			record.setAggregateState(recordElement.getElementsByTagName("state").item(0).getFirstChild().getNodeValue());

		//	Basic properties and structure
		if (recordElement.getElementsByTagName("chemical_formula").item(0).getFirstChild() != null)
			record.getCompoundIdentity().setFormula(
					recordElement.getElementsByTagName("chemical_formula").item(0).getFirstChild().getNodeValue());

		if (recordElement.getElementsByTagName("monisotopic_molecular_weight").item(0).getFirstChild() != null) {
			double mz = Double.parseDouble(recordElement.getElementsByTagName("monisotopic_molecular_weight").item(0)
					.getFirstChild().getNodeValue());
			record.getCompoundIdentity().setExactMass(mz);
		}
		if (recordElement.getElementsByTagName("smiles").item(0).getFirstChild() != null)
			record.getCompoundIdentity().setSmiles(
					recordElement.getElementsByTagName("smiles").item(0).getFirstChild().getNodeValue());

		if (recordElement.getElementsByTagName("inchi").item(0).getFirstChild() != null)
			record.getCompoundIdentity().setInChi(
					recordElement.getElementsByTagName("inchi").item(0).getFirstChild().getNodeValue());

		if (recordElement.getElementsByTagName("inchikey").item(0).getFirstChild() != null)
			record.getCompoundIdentity().setInChiKey(
					recordElement.getElementsByTagName("inchikey").item(0).getFirstChild().getNodeValue());

  		// Database references
  		try {
			parseDatabaseReferences(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		// Biolocations
  		try {
			parseBioLocations(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		// Pathways
  		try {
			parsePathways(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		// Concentrations
  		try {
			parseConcentrations(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		// Deseases
  		try {
			parseDeseases(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		// References
  		try {
			parseReferences(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		// Protein associations
  		try {
			parseProteinAssociations(recordElement, record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return record;
	}

	public static void parseProteinAssociations(Element recordElement, HMDBRecord record) {

		NodeList proteinAssociations = recordElement.getElementsByTagName("protein_associations");
		if(proteinAssociations.getLength() > 0) {

			Element paElement = (Element) proteinAssociations.item(0);
			NodeList proteinList = paElement.getElementsByTagName("protein");
			for(int i=0; i<proteinList.getLength(); i++) {

				Element proteinNode = (Element) proteinList.item(i);

				String proteinAccession = "";
				NodeList idElement = proteinNode.getElementsByTagName("protein_accession");
				if(idElement.getLength() > 0) {

					if(idElement.item(0).getFirstChild() != null)
						proteinAccession = idElement.item(0).getFirstChild().getNodeValue();
				}
				String name = "";
				NodeList nameElement = proteinNode.getElementsByTagName("name");
				if(nameElement.getLength() > 0) {

					if(nameElement.item(0).getFirstChild() != null)
						name = nameElement.item(0).getFirstChild().getNodeValue();
				}
				String uniprotId = "";
				NodeList uniprotIdElement = proteinNode.getElementsByTagName("uniprot_id");
				if(uniprotIdElement.getLength() > 0) {

					if(uniprotIdElement.item(0).getFirstChild() != null)
						uniprotId = uniprotIdElement.item(0).getFirstChild().getNodeValue();
				}
				String geneName = "";
				NodeList geneNameElement = proteinNode.getElementsByTagName("gene_name");
				if(geneNameElement.getLength() > 0) {

					if(geneNameElement.item(0).getFirstChild() != null)
						geneName = geneNameElement.item(0).getFirstChild().getNodeValue();
				}

				String proteinType = "";
				NodeList proteinTypeElement = proteinNode.getElementsByTagName("protein_type");
				if(proteinTypeElement.getLength() > 0) {

					if(proteinTypeElement.item(0).getFirstChild() != null)
						proteinType = proteinTypeElement.item(0).getFirstChild().getNodeValue();
				}
				HMDBProteinAssociation pa = new HMDBProteinAssociation(proteinAccession, name, uniprotId, geneName, proteinType);
				record.getProteinAssociation().add(pa);
			}
		}
	}

	public static void parseReferences(Element recordElement, HMDBRecord record) {

		NodeList refs = recordElement.getElementsByTagName("general_references");
		if(refs.getLength() > 0) {

			Element refElement = (Element) refs.item(0);
			NodeList refList = refElement.getElementsByTagName("reference");
			for(int j=0; j<refList.getLength(); j++) {

				HMDBCitation ref = parseCitationElement((Element) refList.item(j));
				record.getReferences().add(ref);
			}
		}
	}

	public static void parseDeseases(Element recordElement, HMDBRecord record) {

		NodeList deseases = recordElement.getElementsByTagName("diseases");
		if(deseases.getLength() > 0) {

			Element dzElement = (Element) deseases.item(0);
			NodeList dzList = dzElement.getElementsByTagName("disease");
			for(int i=0; i<dzList.getLength(); i++) {

				Element deseaseNode = (Element) dzList.item(i);

				String deseaseName = deseaseNode.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
				String omimId = "";
				NodeList idElement = deseaseNode.getElementsByTagName("omim_id");
				if(idElement.getLength() > 0) {

					if(idElement.item(0).getFirstChild() != null)
						omimId = idElement.item(0).getFirstChild().getNodeValue();
				}
				HMDBDesease des = new HMDBDesease(deseaseName, omimId);
				// References
				NodeList refs = deseaseNode.getElementsByTagName("references");
				if(refs.getLength() > 0) {

					Element refElement = (Element) refs.item(0);
					NodeList refList = refElement.getElementsByTagName("reference");
					for(int j=0; j<refList.getLength(); j++) {

						HMDBCitation ref = parseCitationElement((Element) refList.item(j));
						des.getReferences().add(ref);
					}
				}
				record.getDeseases().add(des);
			}
		}
	}

	public static void parsePathways(Element recordElement, HMDBRecord record) {

		NodeList pathways = recordElement.getElementsByTagName("pathways");
		if(pathways.getLength() > 0) {

			Element pwElement = (Element) pathways.item(0);
			NodeList pwList = pwElement.getElementsByTagName("pathway");
			for(int i=0; i<pwList.getLength(); i++) {

				Element pathwayNode = (Element) pwList.item(i);

				String pathwayName = pathwayNode.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
				String smpdbId = "";
				NodeList idElement = pathwayNode.getElementsByTagName("smpdb_id");
				if(idElement.getLength() > 0) {

					if(idElement.item(0).getFirstChild() != null)
						smpdbId = idElement.item(0).getFirstChild().getNodeValue();
				}
				String keggMapId = "";
				NodeList keggMapIdElement = pathwayNode.getElementsByTagName("kegg_map_id");
				if(keggMapIdElement.getLength() > 0) {

					if(keggMapIdElement.item(0).getFirstChild() != null)
						keggMapId = keggMapIdElement.item(0).getFirstChild().getNodeValue();
				}
				HMDBPathway patwayEntry = new HMDBPathway(pathwayName, smpdbId, keggMapId);
				record.getPathways().add(patwayEntry);
			}
		}
	}

	public static void parseConcentrations(Element recordElement, HMDBRecord record) {

		// Normal
		NodeList normalConcentrations = recordElement.getElementsByTagName("normal_concentrations");
		if(normalConcentrations.getLength() > 0) {

			Element ncElement = (Element) normalConcentrations.item(0);
			NodeList ncList = ncElement.getElementsByTagName("concentration");
			for(int i=0; i<ncList.getLength(); i++) {
				CompoundConcentration nc = parseConcentrationElement((Element) ncList.item(i), ConcentrationType.NORMAL);
				record.getConcentrations().add(nc);
			}
		}
		// Abnormal
		NodeList abnormalConcentrations = recordElement.getElementsByTagName("abnormal_concentrations");
		if(abnormalConcentrations.getLength() > 0) {

			Element acElement = (Element) abnormalConcentrations.item(0);
			NodeList acList = acElement.getElementsByTagName("concentration");
			for(int i=0; i<acList.getLength(); i++) {
				CompoundConcentration nc = parseConcentrationElement((Element) acList.item(i), ConcentrationType.ABNORMAL);
				record.getConcentrations().add(nc);
			}
		}
	}

	public static CompoundConcentration parseConcentrationElement(Element concElement, ConcentrationType type) {

		CompoundConcentration cc = null;

		String biospecimen = "";
		Node bsElement = concElement.getElementsByTagName("biospecimen").item(0).getFirstChild();
		if (bsElement != null)
			biospecimen = bsElement.getNodeValue();

		String value = "";
		Node valueElement = concElement.getElementsByTagName("concentration_value").item(0).getFirstChild();
		if (valueElement != null)
			value = valueElement.getNodeValue();

		String units = "";
		Node unitsElement = concElement.getElementsByTagName("concentration_units").item(0).getFirstChild();
		if (unitsElement != null)
			units = unitsElement.getNodeValue();

		cc = new CompoundConcentration(biospecimen, value, units, type);

		if(type.equals(ConcentrationType.NORMAL)) {

			Node ageElement = concElement.getElementsByTagName("subject_age").item(0).getFirstChild();
			if (ageElement != null)
				cc.setAge(ageElement.getNodeValue());

			Node sexElement = concElement.getElementsByTagName("subject_sex").item(0).getFirstChild();
			if (sexElement != null)
				cc.setSex(sexElement.getNodeValue());

			Node conditionElement = concElement.getElementsByTagName("subject_condition").item(0).getFirstChild();
			if (conditionElement != null)
				cc.setCondition(conditionElement.getNodeValue());
		}
		if(type.equals(ConcentrationType.ABNORMAL)) {

			Node ageElement = concElement.getElementsByTagName("patient_age").item(0).getFirstChild();
			if (ageElement != null)
				cc.setAge(ageElement.getNodeValue());

			Node sexElement = concElement.getElementsByTagName("patient_sex").item(0).getFirstChild();
			if (sexElement != null)
				cc.setSex(sexElement.getNodeValue());

			Node conditionElement = concElement.getElementsByTagName("patient_information").item(0).getFirstChild();
			if (conditionElement != null)
				cc.setCondition(conditionElement.getNodeValue());
		}
		NodeList comment = concElement.getElementsByTagName("comment");
		if(comment.getLength() > 0) {

			if(comment.item(0).getFirstChild() != null)
				cc.setComment(comment.item(0).getFirstChild().getNodeValue());
		}
		// References
		NodeList refs = concElement.getElementsByTagName("references");
		if(refs.getLength() > 0) {

			Element refElement = (Element) refs.item(0);
			NodeList refList = refElement.getElementsByTagName("reference");
			for(int i=0; i<refList.getLength(); i++) {

				HMDBCitation ref = parseCitationElement((Element) refList.item(i));
				cc.getReferences().add(ref);
			}
		}
		return cc;
	}

	public static HMDBCitation parseCitationElement(Element citationElement) {

		String refText = citationElement.getElementsByTagName("reference_text").item(0).getFirstChild().getNodeValue();
		String id = "";
		NodeList idElement = citationElement.getElementsByTagName("pubmed_id");
		if(idElement.getLength() > 0) {

			if(idElement.item(0).getFirstChild() != null)
				id = idElement.item(0).getFirstChild().getNodeValue();
		}
		return new HMDBCitation(refText, id);
	}

	public static void parseBioLocations(Element recordElement, HMDBRecord record) {

		// Cellular
		NodeList cellLocations = recordElement.getElementsByTagName("cellular_locations");
		if(cellLocations.getLength() > 0) {

			Element clElement = (Element) cellLocations.item(0);
			NodeList clList = clElement.getElementsByTagName("cellular");
			for(int i=0; i<clList.getLength(); i++) {

				record.getBiolocations().add(new CompoundBioLocation(clList.item(i).getFirstChild().getNodeValue(),
						CompoundLocationType.CELLULAR));
			}
		}
		// Biospecimen
		NodeList biospecimenLocations = recordElement.getElementsByTagName("biospecimen_locations");
		if(biospecimenLocations.getLength() > 0) {

			Element bsElement = (Element) biospecimenLocations.item(0);
			NodeList bsList = bsElement.getElementsByTagName("biospecimen");
			for(int i=0; i<bsList.getLength(); i++) {

				record.getBiolocations().add(new CompoundBioLocation(bsList.item(i).getFirstChild().getNodeValue(),
						CompoundLocationType.BIOSPECIMEN));
			}
		}
		// Biofluid
		NodeList biofluidLocations = recordElement.getElementsByTagName("biofluid_locations");
		if(biofluidLocations.getLength() > 0) {

			Element bfElement = (Element) biofluidLocations.item(0);
			NodeList bfList = bfElement.getElementsByTagName("biofluid");
			for(int i=0; i<bfList.getLength(); i++) {

				record.getBiolocations().add(new CompoundBioLocation(bfList.item(i).getFirstChild().getNodeValue(),
						CompoundLocationType.BIOFLUID));
			}
		}
		// Tissue
		NodeList tissueLocations = recordElement.getElementsByTagName("tissue_locations");
		if(tissueLocations.getLength() > 0) {

			Element tisElement = (Element) tissueLocations.item(0);
			NodeList tisList = tisElement.getElementsByTagName("tissue");
			for(int i=0; i<tisList.getLength(); i++) {

				record.getBiolocations().add(new CompoundBioLocation(tisList.item(i).getFirstChild().getNodeValue(),
						CompoundLocationType.TISSUE));
			}
		}
	}

	public static void parseDatabaseReferences(Element recordElement, HMDBRecord record) {

		for(HMDBCrossrefFields dbRef : HMDBCrossrefFields.values()) {

			NodeList refNode = recordElement.getElementsByTagName(dbRef.getName());
			if(refNode.getLength() > 0) {

				if(refNode.item(0).getFirstChild() != null) {
					String dbId = refNode.item(0).getFirstChild().getNodeValue();
					record.getCompoundIdentity().addDbId(dbRef.getDatabase(), dbId);
				}
			}
		}
	}

	public static void parseSynonyms(Element recordElement, HMDBRecord record){

		NodeList synonyms = recordElement.getElementsByTagName("synonyms");
		if(synonyms.getLength() > 0) {

			Element synonymElement = (Element) synonyms.item(0);
			NodeList synList = synonymElement.getElementsByTagName("synonym");
			for(int i=0; i<synList.getLength(); i++)
				record.getSynonyms().add(synList.item(i).getFirstChild().getNodeValue());
		}
	}

	public static void parseDescriptions(Element recordElement, HMDBRecord record) {

		NodeList desc = recordElement.getElementsByTagName("description");
		if(desc.getLength() > 0) {

			if(desc.item(0).getFirstChild() != null)
				record.setDescription(StringEscapeUtils.unescapeHtml4(desc.item(0).getFirstChild().getNodeValue()));;
		}
		NodeList csdesc = recordElement.getElementsByTagName("cs_description");
		if(csdesc.getLength() > 0) {

			if(csdesc.item(0).getFirstChild() != null)
				record.setCsDescription(StringEscapeUtils.unescapeHtml4(csdesc.item(0).getFirstChild().getNodeValue()));
		}
	}

	public static void parseTimeStamps(Element recordElement, HMDBRecord record) {

		String dateCreated = recordElement.getElementsByTagName("creation_date").item(0).getFirstChild().getNodeValue();
		try {
			Date dc = dateFormat.parse(dateCreated.replace(" UTC", ""));
			record.setDateCreated(dc);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String dateUpdated = recordElement.getElementsByTagName("update_date").item(0).getFirstChild().getNodeValue();
		try {
			Date du = dateFormat.parse(dateUpdated.replace(" UTC", ""));
			record.setLastUpdated(du);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Upload record to database
	 */
	public static void insertRecord(HMDBRecord record, Connection conn) {

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
			loadConcentrations(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadDeseases(record, conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			loadProteinAssociations(record, conn);
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

	private static void loadMainRecordData(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery =
				"INSERT INTO HMDB_COMPOUND_DATA " +
				"(ACCESSION, NAME, FORMULA, EXACT_MASS, SMILES, INCHI, INCHI_KEY, AGGREGATE_STATE, DESCRIPTION, CS_DESCRIPTION, DATE_CREATED, LAST_UPDATED) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		ps.setString(2, record.getName());

		String formula = record.getCompoundIdentity().getFormula();
		if(formula == null)
			formula = "";

		ps.setString(3, formula);

		if(record.getCompoundIdentity().getExactMass() > 0.0d)
			ps.setDouble(4, record.getCompoundIdentity().getExactMass());
		else
			ps.setString(4, "");

		String smiles = record.getCompoundIdentity().getSmiles();
		if(smiles == null)
			smiles = "";

		ps.setString(5, smiles);

		String inchi = record.getCompoundIdentity().getInChi();
		if(inchi == null)
			inchi = "";

		ps.setString(6, inchi);

		String inchiKey = record.getCompoundIdentity().getInChiKey();
		if(inchiKey == null)
			inchiKey = "";

		ps.setString(7, inchiKey);

		String aggState = record.getAggregateState();
		if(aggState == null)
			aggState = "";

		ps.setString(8, aggState);

		String description = record.getDescription();
		if(description == null)
			description = "";

		ps.setString(9, description);

		String csdescription = record.getCsDescription();
		if(csdescription == null)
			csdescription = "";

		ps.setString(10, csdescription);

		ps.setDate(11, new java.sql.Date(record.getDateCreated().getTime()));
		ps.setDate(12, new java.sql.Date(record.getLastUpdated().getTime()));

		ps.executeUpdate();
		ps.close();
//
	}

	public static void loadSynonyms(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO HMDB_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
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

	public static void loadDatabaseReferences(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO HMDB_CROSSREF (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		for (Entry<CompoundDatabaseEnum, String> entry : record.getCompoundIdentity().getDbIdMap().entrySet()) {

			ps.setString(2, entry.getKey().name());
			ps.setString(3, entry.getValue());
			ps.executeUpdate();
		}
		ps.close();
	}

	public static void loadBioLocations(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery = "INSERT INTO HMDB_BIOLOCATIONS (ACCESSION, LOCATION_TYPE, LOCATION) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		for (CompoundBioLocation location : record.getBiolocations()) {
			ps.setString(2, location.getLocationType().name());
			ps.setString(3, location.getLocationName());
			ps.executeUpdate();
		}
		ps.close();
	}

	public static void loadPathways(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery =
			"INSERT INTO HMDB_PATHWAYS (ACCESSION, PATHWAY_NAME, KEGG_MAP_ID, SMPDB_ID) VALUES (?, ?, ?, ?)";
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

	private static void loadConcentrations(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery =
			"INSERT INTO HMDB_CONCENTRATIONS (ACCESSION, CONC_ID, BIOFLUID, UNITS, VALUE, AGE, SEX, SUBJECT_CONDITION, COMMENTS, TYPE) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		for (CompoundConcentration concentration : record.getConcentrations()) {

			ps.setString(2, concentration.getUniqueId());

			String biospecimen = concentration.getBiospecimen();
			if(biospecimen == null)
				biospecimen = "";

			ps.setString(3, biospecimen);

			String units = concentration.getUnits();
			if(units == null)
				units = "";

			ps.setString(4, units);

			String value = concentration.getValue();
			if(value == null)
				value = "";

			ps.setString(5, value);

			String age = concentration.getAge();
			if(age == null)
				age = "";

			ps.setString(6, age);

			String sex = concentration.getSex();
			if(sex == null)
				sex = "";

			ps.setString(7, sex);

			String condition = concentration.getCondition();
			if(condition == null)
				condition = "";

			ps.setString(8, condition);

			String comment = concentration.getComment();
			if(comment == null)
				comment = "";

			ps.setString(9, comment);

			ps.setString(10, concentration.getType().name());

			ps.executeUpdate();

			for(HMDBCitation reference : concentration.getReferences())
				insertReference(record.getPrimaryId(), reference, concentration.getUniqueId(), ReferenceObjectType.CONCENTRATION, conn);
		}
		ps.close();
	}

	private static void loadDeseases(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery =
			"INSERT INTO HMDB_DESEASES (ACCESSION, DESEASE_ID, DESEASE_NAME, OMIM_ID) VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		for (HMDBDesease desease : record.getDeseases()) {

			ps.setString(2, desease.getUniqueId());
			ps.setString(3, desease.getName());

			String omim = desease.getOmimId();
			if(omim == null)
				omim = "";

			ps.setString(4, omim);
			ps.executeUpdate();

			for(HMDBCitation reference : desease.getReferences())
				insertReference(record.getPrimaryId(), reference, desease.getUniqueId(), ReferenceObjectType.DESEASE, conn);
		}
		ps.close();
	}

	private static void loadProteinAssociations(HMDBRecord record, Connection conn) throws SQLException {

		String dataQuery =
			"INSERT INTO HMDB_PROTEIN_ASSOC (ACCESSION, PROTEIN_ACCESSION, PROTEIN_NAME, UNIPROT_ID, GENE_NAME, PROTEIN_TYPE) "+
			"VALUES (?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		for (HMDBProteinAssociation assoc : record.getProteinAssociation()) {

			String proteinAccession = assoc.getProteinAccession();
			if(proteinAccession == null)
				proteinAccession = "";

			ps.setString(2, proteinAccession);

			String name = assoc.getName();
			if(name == null)
				name = "";

			ps.setString(3, name);

			String uniprot = assoc.getUniprot();
			if(uniprot == null)
				uniprot = "";

			ps.setString(4, uniprot);

			String gene = assoc.getGeneName();
			if(gene == null)
				gene = "";

			ps.setString(5, gene);

			String type = assoc.getProteinType();
			if(type == null)
				type = "";

			ps.setString(6, type);

			ps.executeUpdate();
		}
		ps.close();
	}

	public static void loadGeneralReferences(HMDBRecord record, Connection conn) {

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
				"INSERT INTO HMDB_CITATIONS " +
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
}













































