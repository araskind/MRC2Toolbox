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

package edu.umich.med.mrc2.datoolbox.database.load.chebi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class ChebiParser {

	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);

	public static Map<String, String>getCompoundDataMap(){

		Map<String, String>compoundDataMap = new TreeMap<String, String>();
		for(ChebiFields f : ChebiFields.values())
			compoundDataMap.put(f.toString(), "");

		return compoundDataMap;
	}

	public static Map<String, String>getCompoundNamingMap(){

		Map<String, String>compoundDataMap = new TreeMap<String, String>();
		for(ChebiNameFields f : ChebiNameFields.values())
			compoundDataMap.put(f.toString(), "");

		return compoundDataMap;
	}

	public static Map<String, String>getCrossRefMap(){

		Map<String, String>crossRefMap = new TreeMap<String, String>();
		for(ChebiCrossrefFields f : ChebiCrossrefFields.values())
			crossRefMap.put(f.getName(), "");

		return crossRefMap;
	}

	public static Map<String, String>getCitationMap(){

		Map<String, String>citationMap = new TreeMap<String, String>();
		for(ChebiCitationFields f : ChebiCitationFields.values())
			citationMap.put(f.getName(), "");

		return citationMap;
	}

	public static Map<String, CompoundDatabaseEnum>getSourceDbMap(){

		Map<String, CompoundDatabaseEnum>crossRefMap = new TreeMap<String, CompoundDatabaseEnum>();

		for(ChebiCrossrefFields f : ChebiCrossrefFields.values())
			crossRefMap.put(f.getName(), f.getDatabase());

		for(ChebiCitationFields f : ChebiCitationFields.values())
			crossRefMap.put(f.getName(), f.getDatabase());

		return crossRefMap;
	}

	public static void insertChebiRecord(IAtomContainer molecule, Connection conn) throws SQLException {

		Map<String, String> chebiDataMap = ChebiParser.getCompoundDataMap();
		Map<String, String> namingMap = ChebiParser.getCompoundNamingMap();
		Map<String, String> chebiCrossrefMap = ChebiParser.getCrossRefMap();
		Map<String, String> chebiCitationMap = ChebiParser.getCitationMap();
		Map<String, CompoundDatabaseEnum> sourceDbMap = ChebiParser.getSourceDbMap();

		PreparedStatement ps = null;

		molecule.getProperties().forEach((k,v)->{

			if(chebiDataMap.containsKey(k.toString()))
				chebiDataMap.put(k.toString(), v.toString());

			if(namingMap.containsKey(k.toString()))
				namingMap.put(k.toString(), v.toString());

			if(chebiCrossrefMap.containsKey(k.toString()))
				chebiCrossrefMap.put(k.toString(), v.toString());

			if(chebiCitationMap.containsKey(k.toString()))
				chebiCitationMap.put(k.toString(), v.toString());
		});
		String id = chebiDataMap.get(ChebiFields.CHEBI_ID.toString()).replace("CHEBI:", "");

		//	Check if ID already in
		if(idInDatabase(id, conn))
			return;

		String commonName = chebiDataMap.get(ChebiFields.CHEBI_NAME.toString());

		IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molecule);
		double exactMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
		String mfString = MolecularFormulaManipulator.getString(mf);

		String smiles = chebiDataMap.get(ChebiFields.SMILES.toString());
		if(smiles.isEmpty()) {
			try {
				smiles = smilesGenerator.create(molecule);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//	Insert primary data
		String dataQuery =
			"INSERT INTO CHEBI_COMPOUND_DATA " +
			"(ACCESSION, NAME, MOL_FORMULA, EXACT_MASS, INCHI, INCHI_KEY, SMILES, CHARGE, STAR, DEFINITION) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);
		ps.setString(2, commonName);
		ps.setString(3, mfString);
		ps.setDouble(4, exactMass);
		ps.setString(5, chebiDataMap.get(ChebiFields.INCHI.toString()));
		ps.setString(6, chebiDataMap.get(ChebiFields.INCHIKEY.toString()));
		ps.setString(7, smiles);
		ps.setString(8, chebiDataMap.get(ChebiFields.CHARGE.toString()));
		ps.setString(9, chebiDataMap.get(ChebiFields.STAR.toString()));
		ps.setString(10, chebiDataMap.get(ChebiFields.DEFINITION.toString()));
		ps.executeUpdate();
		ps.close();

		//	Insert names
		dataQuery = "INSERT INTO CHEBI_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);

		//	Primary name
		ps.setString(2, commonName);
		ps.setString(3, "PRI");
		ps.executeUpdate();

		//	IUPAC
		String iupac = namingMap.get(ChebiNameFields.IUPAC_NAMES.toString());
		if(!iupac.isEmpty()) {

			String[] iupacArray = iupac.split("\\r?\\n");
			for(String iup : iupacArray) {

				ps.setString(2, iup);
				ps.setString(3, "IUP");
				ps.executeUpdate();
			}
		}
		//	Synonyms
		String synon = namingMap.get(ChebiNameFields.SYNONYMS.toString());
		if(!synon.isEmpty()) {

			String[] synonArray = synon.split("\\r?\\n");
			for(String syn : synonArray) {

				ps.setString(2, syn);
				ps.setString(3, "SYN");
				ps.executeUpdate();
			}
		}
		//	INN
		String synoninn = namingMap.get(ChebiNameFields.INN.toString());
		if(!synoninn.isEmpty()) {

			String[] synoninnArray = synoninn.split("\\r?\\n");
			for(String syn : synoninnArray) {

				ps.setString(2, syn);
				ps.setString(3, "SYN");
				ps.executeUpdate();
			}
		}
		//	BRAND names
		String brandn = namingMap.get(ChebiNameFields.BRAND_NAMES.toString());
		if(!brandn.isEmpty()) {

			String[] brandnArray = brandn.split("\\r?\\n");
			for(String brn : brandnArray) {

				ps.setString(2, brn);
				ps.setString(3, "BRN");
				ps.executeUpdate();
			}
		}
		ps.close();

		//	Insert database references
		dataQuery = "INSERT INTO CHEBI_CROSSREF (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);
		for (Map.Entry<String, String> entry : chebiCrossrefMap.entrySet()) {

			if(!entry.getValue().isEmpty()) {

				String[] refs = entry.getValue().split("\\r?\\n");

				//	Treat PubChem to separate SIDs and CIDs
				if(entry.getKey().equals(ChebiCrossrefFields.PUBCHEM.getName())) {

					for(String ref : refs) {

						if(ref.startsWith("CID")) {

							ps.setString(2, ChebiCrossrefFields.PUBCHEM.getDatabase().name());
							ps.setString(3, ref.replace("CID: ", ""));
						}
						if(ref.startsWith("SID")) {

							ps.setString(2, ChebiCrossrefFields.PUBCHEM_SID.getDatabase().name());
							ps.setString(3, ref.replace("SID: ", ""));
						}
						ps.executeUpdate();
					}
				}
				//	Secondary CEHBI
				else if(entry.getKey().equals(ChebiCrossrefFields.CHEBI_SECONDARY.getName())) {

					for(String ref : refs) {
						ps.setString(2, ChebiCrossrefFields.CHEBI_SECONDARY.getDatabase().name());
						ps.setString(3, ref.replace("CHEBI: ", ""));
						ps.executeUpdate();
					}
				}
				//	RHEA
				else if(entry.getKey().equals(ChebiCrossrefFields.RHEA.getName())) {

					for(String ref : refs) {
						ps.setString(2, ChebiCrossrefFields.RHEA.getDatabase().name());
						ps.setString(3, ref.replace("RHEA: ", ""));
						ps.executeUpdate();
					}
				}
				else {
					for(String ref : refs) {

						ps.setString(2, sourceDbMap.get(entry.getKey()).name());
						ps.setString(3, ref);
						ps.executeUpdate();
					}
				}
			}
		}
		ps.close();

		//Insert citationReferences
		dataQuery = "INSERT INTO CHEBI_CITATIONS (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);
		for (Map.Entry<String, String> entry : chebiCitationMap.entrySet()) {

			if(!entry.getValue().isEmpty()) {

				String[] refs = entry.getValue().split("\\r?\\n");
				for(String ref : refs) {

					ps.setString(2, sourceDbMap.get(entry.getKey()).name());
					ps.setString(3, ref);
					ps.executeUpdate();
				}
			}
		}
		ps.close();
	}

	private static boolean idInDatabase(String id, Connection conn) {

		try {
			PreparedStatement ps = conn.prepareStatement("SELECT NAME FROM CHEBI_COMPOUND_DATA WHERE ACCESSION = '" + id + "'");
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				ps.close();
				return true;
			}
			ps.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
