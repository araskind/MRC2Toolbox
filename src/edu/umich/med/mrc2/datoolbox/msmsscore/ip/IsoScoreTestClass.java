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

package edu.umich.med.mrc2.datoolbox.msmsscore.ip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class IsoScoreTestClass {

	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		
		try {
			recordExpectedRelIntensities();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordExpectedRelIntensities() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT MOL_FORMULA, EXACT_MASS, ISOTOPE_2, ISOTOPE_3, "
				+ "ISOTOPE_4 FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS "
				+ "SET ISOTOPE_2_CALC = ?, ISOTOPE_3_CALC = ?, ISOTOPE_4_CALC = ? "
				+ "WHERE MOL_FORMULA = ?";
		PreparedStatement updps = conn.prepareStatement(updQuery);
		
		IsotopePatternScorer ips = new IsotopePatternScorer();
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			double mass = rs.getDouble("EXACT_MASS");
			double expectedIsoTwoRi = 0.0d;
			double expectedIsoThreeRi = 0.0d;
			double expectedIsoFourRi = 0.0d;
			
			if(rs.getDouble("ISOTOPE_2") > 0)				
				expectedIsoTwoRi = ips.getMedianExpectedRelativeIntensityForIsotope(mass, 2);
			
			if(rs.getDouble("ISOTOPE_3") > 0)				
				expectedIsoThreeRi = ips.getMedianExpectedRelativeIntensityForIsotope(mass, 3);
			
			if(rs.getDouble("ISOTOPE_4") > 0)				
				expectedIsoFourRi = ips.getMedianExpectedRelativeIntensityForIsotope(mass, 4);

			updps.setDouble(1, expectedIsoTwoRi);
			updps.setDouble(2, expectedIsoThreeRi);
			updps.setDouble(3, expectedIsoFourRi);
			updps.setString(4, rs.getString("MOL_FORMULA"));
			updps.executeUpdate();
		}
		rs.close();
		ps.close();
		updps.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void testScorer() {
		
		double[]testMasses = new double[] {
				189.061220352,
				172.100048384,
				188.094963004,
				204.089877624,
				236.061948624,
				268.034019624,
				220.084792244,
				252.056863244,
				236.079706864,
				252.074621484,
				268.069536104,
				220.067034004,
				252.039105004,
				204.072119384,
				216.101111004,
				232.096025624,
				264.068096624,
				280.063011244,
		};		
		IsotopePatternScorer ips = new IsotopePatternScorer();
		for(double testMass : testMasses) {
			
			double expectedRi = ips.getMedianExpectedRelativeIntensityForIsotope(testMass, 4);
			System.out.println(Double.toString(testMass) + "\t" + Double.toString(expectedRi));
		}
		System.out.println("***");
	}

	
	
}
