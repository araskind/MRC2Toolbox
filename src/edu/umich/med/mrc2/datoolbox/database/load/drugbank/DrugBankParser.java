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

package edu.umich.med.mrc2.datoolbox.database.load.drugbank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.database.load.CompoundPropertyType;

public class DrugBankParser {

	private static final NumberFormat mzFormat = new DecimalFormat("###.####");

	public static DrugBankRecord parseRecord(Node recordDocument) throws XPathExpressionException {

		DrugBankRecord record = new DrugBankRecord();
		Element drugElement = (Element)recordDocument;
		record.setPrimaryId(getDrugBankId(drugElement));

		//	Name
		String name = drugElement.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		record.setName(name);
		record.getDrugIdentity().setCommonName(name);

		//	UNII
		Node uniiNode = drugElement.getElementsByTagName("unii").item(0).getFirstChild();
		if(uniiNode != null)
			record.getDrugIdentity().addDbId(CompoundDatabaseEnum.UNII, uniiNode.getNodeValue());

		//	CAS ID
		Node casNode = drugElement.getElementsByTagName("cas-number").item(0).getFirstChild();
		if(casNode != null)
			record.getDrugIdentity().addDbId(CompoundDatabaseEnum.CAS, casNode.getNodeValue());

		for(DrugBankDescriptiveFields field : DrugBankDescriptiveFields.values()) {

			Node fieldNode = drugElement.getElementsByTagName(field.getName()).item(0).getFirstChild();
			if(fieldNode != null)
				record.setDescriptiveField(field, StringEscapeUtils.unescapeHtml4(fieldNode.getNodeValue()));
			else
				record.setDescriptiveField(field, "");
		}
		//	Properties
		NodeList expropNodes = drugElement.getElementsByTagName("property");
		parseProperties(expropNodes, record);

		//	External IDs
		NodeList externIdNodes = drugElement.getElementsByTagName("external-identifier");
		parseExternalIds(externIdNodes, record);

		//	Categories
		NodeList categoriesNodes = drugElement.getElementsByTagName("category");
		parseCategories(categoriesNodes, record);

		//	Synonyms
		NodeList synonymsNodes = drugElement.getElementsByTagName("synonym");
		for (int i = 0; i < synonymsNodes.getLength(); i++){

			if (synonymsNodes.item(i).getParentNode().getNodeName().equals("synonyms"))
				record.addSynonym(synonymsNodes.item(i).getFirstChild().getNodeValue());
		}
		return record;
	}

	private static void parseExternalIds(NodeList externIdNodes, DrugBankRecord record) {

		for (int i = 0; i < externIdNodes.getLength(); i++) {

			String eresource = ((Element)externIdNodes.item(i)).getElementsByTagName("resource").item(0).getFirstChild().getNodeValue();
			String identifier = ((Element)externIdNodes.item(i)).getElementsByTagName("identifier").item(0).getFirstChild().getNodeValue();
			DrugbankCrossrefFields cf = DrugbankCrossrefFields.getByName(eresource);
			if(cf != null)
				record.getDrugIdentity().addDbId(cf.getDatabase(), identifier);
		}
	}

	private static void parseCategories(NodeList categoriesNodes, DrugBankRecord record) {

		for (int i = 0; i < categoriesNodes.getLength(); i++) {

			if (categoriesNodes.item(i).getParentNode().getNodeName().equals("categories")) {

				String ccategory = ((Element)categoriesNodes.item(i)).getElementsByTagName("category").item(0).getFirstChild().getNodeValue();

				String meshid = "";
				Node mesh = ((Element)categoriesNodes.item(i)).getElementsByTagName("mesh-id").item(0).getFirstChild();

				if(mesh != null)
					meshid = mesh.getNodeValue();

				DrugCategory dc = new DrugCategory(ccategory, meshid);
				record.addCategory(dc);
			}
		}
	}

	private static void parseProperties(NodeList expropNodes, DrugBankRecord record) {

		for (int i = 0; i < expropNodes.getLength(); i++) {

			String pkind = null;
			String pvalue = null;
			if (expropNodes.item(i).getParentNode().getNodeName().equals("experimental-properties")) {

				pkind = ((Element)expropNodes.item(i)).getElementsByTagName("kind").item(0).getFirstChild().getNodeValue();
				pvalue = ((Element)expropNodes.item(i)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
				CompoundProperty ep = new CompoundProperty(pkind, pvalue, CompoundPropertyType.EXPERIMENTAL);
				record.addProperty(ep);
			}
			if (expropNodes.item(i).getParentNode().getNodeName().equals("calculated-properties")) {

				pkind = ((Element)expropNodes.item(i)).getElementsByTagName("kind").item(0).getFirstChild().getNodeValue();
				pvalue = ((Element)expropNodes.item(i)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
				CompoundProperty ep = new CompoundProperty(pkind, pvalue, CompoundPropertyType.CALCULATED);
				record.addProperty(ep);
			}
		}
		for(CompoundProperty property : record.getCompoundProperties()){

			String propertyName = property.getPropertyName();
			String propertyValue = property.getPropertyValue();

			if(propertyName.equals(DrugBankCompoundProperties.MOLECULAR_FORMULA.getName()))
				record.getDrugIdentity().setFormula(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.MONOISOTOPIC_WEIGHT.getName()))
				record.getDrugIdentity().setExactMass(Double.valueOf(propertyValue));

			if(propertyName.equals(DrugBankCompoundProperties.SMILES.getName()))
				record.getDrugIdentity().setSmiles(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.INCHI.getName()))
				record.getDrugIdentity().setInChi(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.INCHIKEY.getName()))
				record.getDrugIdentity().setInChiKey(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.IUPAC_NAME.getName()))
				record.getDrugIdentity().setSysName(propertyValue);
		}
	}

	private static String getDrugBankId(Element drugElement) {

		NodeList idNodes = drugElement.getElementsByTagName("drugbank-id");
		for (int i = 0; i < idNodes.getLength(); i++) {

			if (idNodes.item(i).getParentNode().getNodeName().equals("drug")
					&& idNodes.item(i).getAttributes().getNamedItem("primary") != null) {

				return idNodes.item(i).getFirstChild().getNodeValue();
			}
		}
		return null;
	}

	public static void insertRecord(DrugBankRecord record, Connection conn) throws SQLException {

		PreparedStatement ps = null;

		//	Insert main record data
		String dataQuery =
			"INSERT INTO DRUGBANK_COMPOUND_DATA "+
			"(ACCESSION, COMMON_NAME, DESCRIPTION, INDICATION, PHARMACODYNAMICS, " +
			"MECHANISM_OF_ACTION, TOXICITY, METABOLISM, ABSORPTION, HALF_LIFE, " +
			"PROTEIN_BINDING, ROUTE_OF_ELIMINATION, VOLUME_OF_DISTRIBUTION, " +
			"CLEARANCE, MOL_FORMULA, EXACT_MASS, INCHI, INCHI_KEY, SMILES) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());
		ps.setString(2, record.getName());
		ps.setString(3, record.getDescription());
		ps.setString(4, record.getDescriptiveField(DrugBankDescriptiveFields.INDICATION));
		ps.setString(5, record.getDescriptiveField(DrugBankDescriptiveFields.PHARMACODYNAMICS));
		ps.setString(6, record.getDescriptiveField(DrugBankDescriptiveFields.MECHANISMOFACTION));
		ps.setString(7, record.getDescriptiveField(DrugBankDescriptiveFields.TOXICITY));
		ps.setString(8, record.getDescriptiveField(DrugBankDescriptiveFields.METABOLISM));
		ps.setString(9, record.getDescriptiveField(DrugBankDescriptiveFields.ABSORPTION));
		ps.setString(10, record.getDescriptiveField(DrugBankDescriptiveFields.HALFLIFE));
		ps.setString(11, record.getDescriptiveField(DrugBankDescriptiveFields.PROTEINBINDING));
		ps.setString(12, record.getDescriptiveField(DrugBankDescriptiveFields.ROUTEOFELIMINATION));
		ps.setString(13, record.getDescriptiveField(DrugBankDescriptiveFields.VOLUMEOFDISTRIBUTION));
		ps.setString(14, record.getDescriptiveField(DrugBankDescriptiveFields.CLEARANCE));

		//	Mol formula
		String formula = "";
		if(record.getDrugIdentity().getFormula() != null)
			formula = record.getDrugIdentity().getFormula();

		ps.setString(15, formula);

		//	Mass
		String mass = "";
		double em = record.getDrugIdentity().getExactMass();
		if(em > 0.0d)
			mass = mzFormat.format(em);

		ps.setString(16, mass);

		//	InChi
		String inchi = "";
		if(record.getDrugIdentity().getInChi() != null)
			inchi = record.getDrugIdentity().getInChi();

		ps.setString(17, inchi);

		//	InChi key
		String inchikey = "";
		if(record.getDrugIdentity().getInChiKey() != null)
			inchikey = record.getDrugIdentity().getInChiKey();

		ps.setString(18, inchikey);

		//	SMILES
		String smiles = "";
		if(record.getDrugIdentity().getSmiles() != null)
			smiles = record.getDrugIdentity().getSmiles();

		ps.setString(19, smiles);

		ps.executeUpdate();
		ps.close();

		//	Insert synonyms
		dataQuery = "INSERT INTO DRUGBANK_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		//	Primary name
		ps.setString(2, record.getName());
		ps.setString(3, "PRI");
		ps.executeUpdate();

		//	Systematic name
		String sysname = record.getDrugIdentity().getSysName();
		if(sysname != null) {
			ps.setString(2, sysname);
			ps.setString(3, "SYS");
			ps.executeUpdate();
		}
		//	Actual synonyms
		for(String synonym : record.getSynonyms()) {
			ps.setString(2, synonym);
			ps.setString(3, "SYN");
			ps.executeUpdate();
		}
		ps.close();

		//	Insert cross-reference data
		dataQuery = "INSERT INTO DRUGBANK_CROSSREF (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, record.getPrimaryId());

		for (Entry<CompoundDatabaseEnum, String> entry : record.getDrugIdentity().getDbIdMap().entrySet()) {

			ps.setString(2, entry.getKey().name());
			ps.setString(3, entry.getValue());
			ps.executeUpdate();
		}
		ps.close();
	}
}


























