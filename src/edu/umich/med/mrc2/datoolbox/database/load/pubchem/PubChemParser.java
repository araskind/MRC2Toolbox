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

package edu.umich.med.mrc2.datoolbox.database.load.pubchem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;

public class PubChemParser {

	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);

	public static Map<String, String>getCompoundDataMap(){

		Map<String, String>compoundDataMap = new TreeMap<String, String>();
		for(PubChemFields f : PubChemFields.values())
			compoundDataMap.put(f.toString(), "");

		return compoundDataMap;
	}

	public static Map<String, String>getCompoundNamingMap(){

		Map<String, String>compoundDataMap = new TreeMap<String, String>();
		for(PubChemNameFields f : PubChemNameFields.values())
			compoundDataMap.put(f.toString(), "");

		return compoundDataMap;
	}

	public static CompoundIdentity insertPubchemRecord(
			IAtomContainer molecule, 
			String[] synonyms, 
			PubChemCompoundDescriptionBundle descBundle, 
			Connection conn) throws Exception {

		Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
		Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
		molecule.getProperties().forEach((k,v)->{

			if(pubchemDataMap.containsKey(k.toString()))
				pubchemDataMap.put(k.toString(), v.toString());

			if(namingMap.containsKey(k.toString()))
				namingMap.put(k.toString(), v.toString());
		});
		String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
		String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
		
		String commonName = descBundle.getTitle();
		String iupacName = null;
		Pattern casPattern = Pattern.compile("^\\d+-\\d+-\\d+$");
		Pattern zincPattern = Pattern.compile("^ZINC\\d+$");
		Pattern scPattern = Pattern.compile("^SCHEMBL\\d+$");
		Pattern cidPattern = Pattern.compile("^CID \\d+$");
		Matcher casMatcher = casPattern.matcher(commonName);
		Matcher zincMatcher = zincPattern.matcher(commonName);
		Matcher scMatcher = scPattern.matcher(commonName);
		Matcher cidMatcher = cidPattern.matcher(commonName);

		if(casMatcher.find() || zincMatcher.find() 
				|| scMatcher.find() || cidMatcher.find() 
				|| commonName.equals(inchiKey) || commonName.equals(id)) {

			for(PubChemNameFields nf : PubChemNameFields.values()) {

				if(!namingMap.get(nf.toString()).isEmpty()) {
					iupacName = namingMap.get(nf.toString());
					commonName = iupacName;
					break;
				}
			}
		}
		IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molecule);
		
		//	CDK 2.3 and above
		//	double exactMass = MolecularFormulaManipulator.getMass(mf, MolecularFormulaManipulator.MonoIsotopic);
		double exactMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);		
		String mfString = MolecularFormulaManipulator.getString(mf);
		String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
		if(smiles.isEmpty())
			pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

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
			"INSERT INTO COMPOUND_DATA " +
			"(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, "
			+ "SMILES, INCHI, INCHI_KEY, INCHI_KEY_CONNECT) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		ps.setString(1, id);
		ps.setString(2, CompoundDatabaseEnum.PUBCHEM.name());
		ps.setString(3, commonName);
		ps.setString(4, mfString);
		ps.setDouble(5, exactMass);
		ps.setString(6, smiles);
		ps.setString(7, pubchemDataMap.get(PubChemFields.INCHI.toString()));
		ps.setString(8, inchiKey);
		ps.setString(9, inchiKey.substring(0, 14));
		ps.executeUpdate();
		ps.close();

		//	Insert names
		dataQuery = "INSERT INTO COMPOUND_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);

		//	Primary name
		ps.setString(2, commonName);
		ps.setString(3, "PRI");
		ps.addBatch();
		for(int i=1; i<synonyms.length; i++) {

			ps.setString(2, synonyms[i]);
			ps.setString(3, "SYN");
			ps.addBatch();
		}
		if(iupacName != null && !iupacName.isEmpty()) {
			
			ps.setString(2, iupacName);
			ps.setString(3, "IUP");
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		
		//	TODO - insert descriptions if available
		if(descBundle != null && !descBundle.getDescriptions().isEmpty()) {
			
		}
		return CompoundDatabaseUtils.getCompoundById(id, conn);
	}

	public static String idInDatabase(String id, Connection conn) {

		try {
			PreparedStatement ps =
					conn.prepareStatement("SELECT PRIMARY_NAME FROM COMPOUND_DATA WHERE ACCESSION = ?");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				String name = rs.getString("PRIMARY_NAME");
				ps.close();
				return name;
			}
			ps.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
