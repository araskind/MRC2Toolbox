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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class RunContainer2 {

	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	private static final InChITautomerGenerator tautgen = new InChITautomerGenerator(InChITautomerGenerator.KETO_ENOL);
	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static Aromaticity aromaticity;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			calculateDistinctIsotopicPatterns();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void calculateDistinctIsotopicPatterns() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String>formulas = new ArrayList<String>();
		String query = "SELECT MOL_FORMULA, FSIZE FROM FORMULA_ISOTOPIC_PATTERNS ORDER BY 2";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			formulas.add(rs.getString(1));
		}
		rs.close();
		
		query = "UPDATE FORMULA_ISOTOPIC_PATTERNS "
				+ "SET MONOISOTOPE = ?, ISOTOPE_2 = ?,  ISOTOPE_3 = ?, "
				+ "INTENSITY_2 = ?,  INTENSITY_3 = ? "
				+ "WHERE MOL_FORMULA = ?";			
		
		ps = conn.prepareStatement(query);
		for(String formulaString : formulas) {
			
			IMolecularFormula queryFormula = null;
			try {
				queryFormula = 
						MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
			} catch (Exception e) {
				//	e.printStackTrace();
				System.out.println("Invalid formula: " + formulaString);
			}
			if (queryFormula != null) {

				queryFormula.setCharge(0);
				Collection<MsPoint> msPoints = null;
				try {
					msPoints = MsUtils.calculateIsotopeDistribution(queryFormula, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Can't generate isotopes for: " + formulaString);
				}
				if(msPoints != null) {
					
					MsPoint[] msa = msPoints.toArray(new MsPoint[msPoints.size()]);
					
					ps.setDouble(1, msa[0].getMz());
					
					if(msa.length> 1) {
						ps.setDouble(2, msa[1].getMz());
						ps.setDouble(4, msa[1].getIntensity());
					}
					else {
						ps.setNull(2, java.sql.Types.NULL);
						ps.setNull(4, java.sql.Types.NULL);
					}
									
					if(msa.length> 2) {
						ps.setDouble(3, msa[2].getMz());
						ps.setDouble(5, msa[2].getIntensity());
					}
					else {
						ps.setNull(3, java.sql.Types.NULL);
						ps.setNull(5, java.sql.Types.NULL);
					}				
					ps.setString(6,  formulaString);
					
					ps.executeUpdate();
				}
			}
		}
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
}
