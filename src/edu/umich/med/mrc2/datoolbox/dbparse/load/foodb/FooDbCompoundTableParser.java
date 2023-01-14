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

package edu.umich.med.mrc2.datoolbox.dbparse.load.foodb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class FooDbCompoundTableParser {

	//	TODO update connection manager if needed to re-parse the FOODB data
	public static void parseAndUploadFooDbCompounsFile(File inputFile) throws Exception{

		String[][] compoundData = null;
		try {
			compoundData = DelimitedTextParser.parseTextFileWithEncoding(inputFile, ',');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (compoundData != null) {

			Connection conn = CompoundDbConnectionManager.getConnection();
			String dataQuery =
					"UPDATE FOODB_COMPOUNDS SET DESCRIPTION = ? WHERE ID = ?";

			PreparedStatement ps = conn.prepareStatement(dataQuery);
			for(int i=1; i<compoundData.length; i++) {

				//	String[] cd = compoundData[i];
				ps.setString(1, compoundData[i][6]);
				ps.setInt(2, Integer.parseInt(compoundData[i][0]));
				ps.executeUpdate();
			}
			MessageDialog.showInfoMsg("COMPLETE!");
			ps.close();
			CompoundDbConnectionManager.releaseConnection(conn);
		}
	}
}
