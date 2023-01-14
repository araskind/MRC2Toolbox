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

package edu.umich.med.mrc2.datoolbox.dbparse.load.isdb;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MGFFields;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class ISDBMGFParser {

	private static final Pattern msmsPattern = Pattern.compile("^([0-9\\.]+)\\s?,?\\s?([0-9,\\.]+)\\s?(.+)?");

	public static List<TandemMassSpectrum> pareseInputFile(File inputFile) {

		List<TandemMassSpectrum> featureList = new ArrayList<TandemMassSpectrum>();
		List<String> allLines = TextUtils.readTextFileToList(inputFile.getAbsolutePath());
		List<String> chunk = new ArrayList<String>();
		for (String line : allLines) {

			if(line.startsWith(MGFFields.BEGIN_BLOCK.getName()))
				chunk.clear();
			else if(line.startsWith(MGFFields.END_IONS.getName()))
				featureList.add(parseMgfChunk(chunk));
			else
				chunk.add(line);
		}
		return featureList;
	}

	public static TandemMassSpectrum parseMgfChunk(List<String>mgfChunk) {

		Matcher regexMatcher;
		String[] dataBlock = mgfChunk.toArray(new String[mgfChunk.size()]);
		int msStart = -1;
		String pmaId = null;
		MsPoint parent = null;
		Polarity polarity = null;
		for(int i=0; i<dataBlock.length; i++) {

			if(dataBlock[i].startsWith(MGFFields.SCANS.getName())) {
				msStart = i+1;
				break;
			}
			if(dataBlock[i].startsWith(MGFFields.FILENAME.getName()))
				pmaId = dataBlock[i].replace(MGFFields.FILENAME.getName() + "=", "");

			if(dataBlock[i].startsWith(MGFFields.PEPMASS.getName())) {
				String pmz = dataBlock[i].replace(MGFFields.PEPMASS.getName() + "=", "");
				parent = new MsPoint(Double.parseDouble(pmz), 999.0, "M+H");
			}
			if(dataBlock[i].startsWith(MGFFields.IONMODE.getName())) {

				String ionMode = dataBlock[i].replace(MGFFields.IONMODE.getName() + "=", "").trim().toUpperCase();
				if(ionMode.startsWith("P"))
					polarity = Polarity.Positive;

				if(ionMode.startsWith("N"))
					polarity = Polarity.Negative;
			}
		}
		TandemMassSpectrum msms = new TandemMassSpectrum(polarity);
		msms.setParent(parent);
		if(pmaId != null)
			msms.setId(pmaId);

		Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
		for(int i=msStart; i<dataBlock.length; i++) {

			regexMatcher = msmsPattern.matcher(dataBlock[i]);
			if(regexMatcher.find()) {
				MsPoint dp = new MsPoint(Double.parseDouble(regexMatcher.group(1)), Double.parseDouble(regexMatcher.group(2)));
				dataPoints.add(dp);
			}
		}
		msms.setSpectrum(dataPoints);
		return msms;
	}

	public static void insertSpectrumRecords(Collection<TandemMassSpectrum> records, Connection conn) throws SQLException {

		String dataQuery =
			"INSERT INTO ISDB_PEAK " +
			"(PMA_ID, MZ, INTENSITY, ADDUCT, IS_PARENT) " +
			"VALUES(?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);

		for(TandemMassSpectrum record : records) {

			ps.setString(1, record.getId());

			//	Parent ion
			MsPoint parent = record.getParent();
			ps.setDouble(2, parent.getMz());
			ps.setDouble(3, parent.getIntensity());
			ps.setString(4, parent.getAdductType());
			ps.setString(5, "Y");
			ps.addBatch();

			//	Spectrum
			for(MsPoint p : record.getSpectrum()) {

				ps.setDouble(2, p.getMz());
				ps.setDouble(3, p.getIntensity());
				ps.setString(4, p.getAdductType());
				ps.setString(5, "N");
				ps.addBatch();
			}
			ps.executeBatch();
		}
		ps.close();
	}
}




























