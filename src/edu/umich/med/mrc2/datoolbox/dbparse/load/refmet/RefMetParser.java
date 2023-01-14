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

package edu.umich.med.mrc2.datoolbox.dbparse.load.refmet;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class RefMetParser {

	public static Collection<Map<RefMetFields,String>>parseRefMetDataFile(File inputFile){

		Collection<Map<RefMetFields,String>>refMetDataCollection = new ArrayList<Map<RefMetFields,String>>();
		String[][] refmetData = null;
		try {
			refmetData = DelimitedTextParser.parseTextFileWithEncoding(inputFile, '\t');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (refmetData != null) {

			//	Parse header
			String[]header = refmetData[0];
			Map<Integer,RefMetFields>headerMap = new TreeMap<Integer,RefMetFields>();
			for(int i=0; i<header.length; i++) {

				for(RefMetFields f : RefMetFields.values()) {
					if(f.getName().equals(header[i]))
						headerMap.put(i, f);
				}
			}
			for(int i=1; i<refmetData.length; i++) {

				Map<RefMetFields,String>record = new TreeMap<RefMetFields,String>();

				for (Entry<Integer, RefMetFields> entry : headerMap.entrySet())
					record.put(entry.getValue(), refmetData[i][entry.getKey()]);

				refMetDataCollection.add(record);
			}
		}
		return refMetDataCollection;
	}

	//	TODO update connection manager if needed to re-parse the REFMET data
	public static void uploadRecordsToDatabase(Collection<Map<RefMetFields,String>>refMetDataCollection) throws Exception {

		Connection conn = CompoundDbConnectionManager.getConnection();

		String dataQuery =
				"INSERT INTO REFMET_COMPOUND_DATA " +
				"(MRC2_REFMET_ID, NAME, PUBCHEM_CID, EXACTMASS, FORMULA, "
				+ "INCHI_KEY, SUPER_CLASS, MAIN_CLASS, SUB_CLASS, MW_REGNO) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		int recordCount = 1;
		for(Map<RefMetFields, String> record : refMetDataCollection) {

			String mrc2id = DataPrefix.REFMET + StringUtils.leftPad(Integer.toString(recordCount), 7, '0');
			ps.setString(1, mrc2id);
			ps.setString(2, record.get(RefMetFields.NAME));
			ps.setString(3, record.get(RefMetFields.PUBCHEM_CID));
			ps.setString(4, record.get(RefMetFields.EXACT_MASS));
			ps.setString(5, record.get(RefMetFields.FORMULA));
			ps.setString(6, record.get(RefMetFields.INCHI_KEY));
			ps.setString(7, record.get(RefMetFields.SUPER_CLASS));
			ps.setString(8, record.get(RefMetFields.MAIN_CLASS));
			ps.setString(9, record.get(RefMetFields.SUB_CLASS));
			ps.setString(10, record.get(RefMetFields.MW_REGNO));

			ps.executeUpdate();

			recordCount++;
		}
		ps.close();
		CompoundDbConnectionManager.releaseConnection(conn);
	}
}

















