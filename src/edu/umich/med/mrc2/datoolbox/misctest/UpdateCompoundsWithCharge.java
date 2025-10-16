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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class UpdateCompoundsWithCharge {

	/**
	 *
	 */
	private static final long serialVersionUID = -7690376860260257788L;
	private static SmilesParser smipar;

	public static void main(String[] args) {

		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			updateCompoundsWithCharge();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateCompoundsWithCharge() throws Exception{

		smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
		Connection conn = CompoundDbConnectionManager.getConnection();
		String query = "SELECT ACCESSION, SMILES FROM COMPOUND_DATA";
		PreparedStatement ps = conn.prepareStatement(query);
		String updQuery = "UPDATE COMPOUND_DATA SET CHARGE = ? WHERE ACCESSION = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);
		ResultSet rs = ps.executeQuery();
		IAtomContainer mol = null;
		IMolecularFormula molFormula = null;
		String accession = null;
		while(rs.next()) {

			accession = rs.getString("ACCESSION");
			try {
				mol = smipar.parseSmiles(rs.getString("SMILES"));
				if(mol != null) {
					molFormula = MolecularFormulaManipulator.getMolecularFormula(mol);
//					System.out.println("ACCESSION: "  + accession +
//						" - Charge: " + Integer.toString(molFormula.getCharge()));
					updps.setInt(1, molFormula.getCharge());
					updps.setString(2, accession);
					updps.executeUpdate();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		rs.close();
		updps.close();
		ps.close();
		CompoundDbConnectionManager.releaseConnection(conn);
	}

	public UpdateCompoundsWithCharge() {
		super();
	}
}
