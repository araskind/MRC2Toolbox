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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.old;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.text.StringEscapeUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundPropertyType;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankCompoundProperties;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankDescriptiveFields;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankRecord;

public class DrugBankParser {

	private static final NumberFormat mzFormat = new DecimalFormat("###.####");

	public static DrugBankRecord parseRecord(Element drugElement) throws XPathExpressionException {

		DrugBankRecord record = new DrugBankRecord(null);
		record.setPrimaryId(getDrugBankId(drugElement));

		//	Name
		String name = drugElement.getChildText("name"); //.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		record.setName(name);
		record.getCompoundIdentity().setCommonName(name);

		//	UNII
//		Node uniiNode = drugElement.getElementsByTagName("unii").item(0).getFirstChild();
		String unii = drugElement.getChildText("unii");
		if(unii != null)
			record.getCompoundIdentity().addDbId(CompoundDatabaseEnum.UNII, unii);

		//	CAS ID
		//	Node casNode = drugElement.getElementsByTagName("cas-number").item(0).getFirstChild();
		String cas = drugElement.getChildText("cas-number");
		if(cas != null)
			record.getCompoundIdentity().addDbId(CompoundDatabaseEnum.CAS, cas);

		for(DrugBankDescriptiveFields field : DrugBankDescriptiveFields.values()) {

			String fieldText = drugElement.getChildText(field.getName()); //	.getElementsByTagName(field.getName()).item(0).getFirstChild();
			if(fieldText != null)
				record.setDescriptiveField(field, StringEscapeUtils.unescapeHtml4(fieldText));
			else
				record.setDescriptiveField(field, "");
		}
		//	Properties	
		parseProperties(drugElement, record);

		//	External IDs
		List<Element>externIdNodes = drugElement.getChildren("external-identifier");
		parseExternalIds(externIdNodes, record);

		//	Categories
		List<Element>categoriesNodes = drugElement.getChildren("category");
		parseCategories(categoriesNodes, record);

		//	Synonyms
		Element synonymsNode = drugElement.getChild("synonyms");
		List<Element>synonymNodes = null;
		if(synonymsNode != null) {
			
			synonymNodes = synonymsNode.getChildren("synonym");	
			for (int i = 0; i < synonymNodes.size(); i++)
					record.addSynonym(synonymNodes.get(i).getText());			
		}
		return record;
	}

	private static void parseExternalIds(List<Element>externIdNodes, DrugBankRecord record) {

		for (int i = 0; i < externIdNodes.size(); i++) {

			//	TODO
//			String eresource = (externIdNodes.get(i)).getElementsByTagName("resource").item(0).getFirstChild().getNodeValue();
//			String identifier = (externIdNodes.item(i)).getElementsByTagName("identifier").item(0).getFirstChild().getNodeValue();
//			DrugbankCrossrefFields cf = DrugbankCrossrefFields.getByName(eresource);
//			if(cf != null)
//				record.getCompoundIdentity().addDbId(cf.getDatabase(), identifier);
		}
	}

	private static void parseCategories(List<Element>categoriesNodes, DrugBankRecord record) {

		//	TODO
//		for (int i = 0; i < categoriesNodes.getLength(); i++) {
//
//			if (categoriesNodes.item(i).getParentNode().getNodeName().equals("categories")) {
//
//				String ccategory = ((Element)categoriesNodes.item(i)).getElementsByTagName("category").item(0).getFirstChild().getNodeValue();
//
//				String meshid = "";
//				Node mesh = ((Element)categoriesNodes.item(i)).getElementsByTagName("mesh-id").item(0).getFirstChild();
//
//				if(mesh != null)
//					meshid = mesh.getNodeValue();
//
//				DrugCategory dc = new DrugCategory(ccategory, meshid);
//				record.addCategory(dc);
//			}
//		}
	}

	private static void parseProperties(Element drugElement, DrugBankRecord record) {
		
		Element expPropNode = drugElement.getChild("experimental-properties");	
		if(expPropNode != null) {
			
			List<Element>expPropList = expPropNode.getChildren("property");
			for(Element pe : expPropList) {
				
				CompoundProperty expProp = new CompoundProperty(
						pe.getChildText("kind"), 
						pe.getChildText("value"), 
						pe.getChildText("source"), 
						CompoundPropertyType.EXPERIMENTAL);
				record.addProperty(expProp);
			}
		}
		Element calcPropNode = drugElement.getChild("calculated-properties");	
		if(calcPropNode != null) {
			
			List<Element>calcPropList = calcPropNode.getChildren("property");			
			for(Element pc : calcPropList) {
				
				CompoundProperty expProp = new CompoundProperty(
						pc.getChildText("kind"), 
						pc.getChildText("value"), 
						pc.getChildText("source"),
						CompoundPropertyType.PREDICTED);
				record.addProperty(expProp);
			}
		}
		for(CompoundProperty property : record.getCompoundProperties()){

			String propertyName = property.getPropertyName();
			String propertyValue = property.getPropertyValue();

			if(propertyName.equals(DrugBankCompoundProperties.MOLECULAR_FORMULA.getName()))
				record.getCompoundIdentity().setFormula(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.MONOISOTOPIC_WEIGHT.getName()))
				record.getCompoundIdentity().setExactMass(Double.valueOf(propertyValue));

			if(propertyName.equals(DrugBankCompoundProperties.SMILES.getName()))
				record.getCompoundIdentity().setSmiles(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.INCHI.getName()))
				record.getCompoundIdentity().setInChi(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.INCHIKEY.getName()))
				record.getCompoundIdentity().setInChiKey(propertyValue);

			if(propertyName.equals(DrugBankCompoundProperties.IUPAC_NAME.getName()))
				record.getCompoundIdentity().setSysName(propertyValue);
		}
	}

	private static String getDrugBankId(Element drugElement) {
		
		List<Element> idElements = drugElement.getChildren("drugbank-id");
		for(Element ide : idElements) {
			
			if(ide.getAttribute("primary") != null)
				return ide.getText();
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
		if(record.getCompoundIdentity().getFormula() != null)
			formula = record.getCompoundIdentity().getFormula();

		ps.setString(15, formula);

		//	Mass
		String mass = "";
		double em = record.getCompoundIdentity().getExactMass();
		if(em > 0.0d)
			mass = mzFormat.format(em);

		ps.setString(16, mass);

		//	InChi
		String inchi = "";
		if(record.getCompoundIdentity().getInChi() != null)
			inchi = record.getCompoundIdentity().getInChi();

		ps.setString(17, inchi);

		//	InChi key
		String inchikey = "";
		if(record.getCompoundIdentity().getInChiKey() != null)
			inchikey = record.getCompoundIdentity().getInChiKey();

		ps.setString(18, inchikey);

		//	SMILES
		String smiles = "";
		if(record.getCompoundIdentity().getSmiles() != null)
			smiles = record.getCompoundIdentity().getSmiles();

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
		String sysname = record.getCompoundIdentity().getSysName();
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

		for (Entry<CompoundDatabaseEnum, String> entry : record.getCompoundIdentity().getDbIdMap().entrySet()) {

			ps.setString(2, entry.getKey().name());
			ps.setString(3, entry.getValue());
			ps.executeUpdate();
		}
		ps.close();
	}
}


























