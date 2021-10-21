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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MGFFields;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class MGFUtils {

	private static final Pattern msmsPattern = Pattern.compile("^([0-9\\.]+)\\s?,?\\s?([0-9,\\.]+)\\s?(.+)?");
	private static Matcher regexMatcher;

	public static Collection<String[]> getMGFTextBlocksFromFile(File inputFile) {

		Collection<String[]> featureList = new ArrayList<String[]>();
		List<String> allLines = TextUtils.readTextFileToList(inputFile.getAbsolutePath());
		List<String> chunk = new ArrayList<String>();
		for (String line : allLines) {

			if(line.startsWith(MGFFields.BEGIN_BLOCK.getName()))
				chunk.clear();
			else if(line.startsWith(MGFFields.END_IONS.getName()))
				featureList.add(chunk.toArray(new String[chunk.size()]));
			else
				chunk.add(line);
		}
		return featureList;
	}
	
	public static TandemMassSpectrum parseXYMetaMGFBlock(String[]mgfBlock, Polarity polarity) {

		String msId = "";
		MsPoint parentIon = null;
		Collection<MsPoint> spectrum = 
				new TreeSet<MsPoint>(new MsDataPointComparator(SortProperty.MZ));
		int spectrumStart = 0;
		for (int i = 0; i < mgfBlock.length; i++) {

			if (mgfBlock[i].trim().startsWith(MGFFields.TITLE.getName())) 
				msId = mgfBlock[i].trim().replace(MGFFields.TITLE.getName() + "=", "").replace("_REV", "");
			
			if (Character.isDigit(mgfBlock[i].charAt(0))) {
				spectrumStart = i;
				break;
			}
		}
		for(int i=spectrumStart; i<mgfBlock.length; i++) {

			regexMatcher = msmsPattern.matcher(mgfBlock[i]);
			if(regexMatcher.find()) {

				MsPoint dp = new MsPoint(
						Double.parseDouble(regexMatcher.group(1)), 
						Double.parseDouble(regexMatcher.group(2)) * 999.0d);
				spectrum.add(dp);
				
				if(i == spectrumStart)
					parentIon = dp;
			}
		}		
		TandemMassSpectrum msms =  new TandemMassSpectrum(
				2,
				parentIon,
				spectrum,
				polarity);
		msms.setId(msId);
		return msms;
	}
}
