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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class NISTPeptideMSPParser {
	
	/**
	 * Parse single NIST MSP record converted to list of strings per line
	 * Try to determine parent ion from the MSP fields or from parent library feature
	 *
	 * @param sourceText - MSP record converted to list of strings per line
	 * @return
	 */
	public static NISTPeptideTandemMassSpectrum parseNistMspDataSource(List<String>sourceText){


		int spectrumStart = -1;
		int pnum = 0;
		double precursorMz = -1.0;
		Matcher regexMatcher = null;
		
		NISTPeptideMSPField[] majorFields = 
				new NISTPeptideMSPField[] {
						NISTPeptideMSPField.NAME,
						NISTPeptideMSPField.MW, 
						NISTPeptideMSPField.COMMENTS,
						NISTPeptideMSPField.NUM_PEAKS};
		Map<NISTPeptideMSPField,Pattern>patternMap = new TreeMap<NISTPeptideMSPField,Pattern>();
		for(NISTPeptideMSPField field : majorFields)
			patternMap.put(field, Pattern.compile("(?i)^" + field.getName() + ":?\\s+(.+)"));
		
		List<NISTPeptideMSPField>majorFieldsList = Arrays.asList(majorFields);
		Map<NISTPeptideMSPField,Pattern>commentsPatternMap = new TreeMap<NISTPeptideMSPField,Pattern>();
		for(NISTPeptideMSPField field : NISTPeptideMSPField.values()) {
			
			if(!majorFieldsList.contains(field)) {
				
				if(field.equals(NISTPeptideMSPField.Protein) 
						|| field.equals(NISTPeptideMSPField.Organism)
						|| field.equals(NISTPeptideMSPField.Sample)
						|| field.equals(NISTPeptideMSPField.Filter)) {
					commentsPatternMap.put(field, Pattern.compile(field.getName() + "=\"(.*?)\" "));	
				}
				else {
					commentsPatternMap.put(field, Pattern.compile(field.getName() + "=(.*?) "));	
				}
			}
		}
		String[] record = sourceText.toArray(new String[sourceText.size()]);
		spectrumStart = -1;
		NISTPeptideTandemMassSpectrum peptideRecord = 
				new NISTPeptideTandemMassSpectrum(Polarity.Positive);

		//	Add all non-ms data
		for(int i=0; i<record.length; i++) {

			regexMatcher = patternMap.get(NISTPeptideMSPField.NUM_PEAKS).matcher(record[i]);
			if (regexMatcher.find()) {

				pnum = Integer.parseInt(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
			else {
				for (Entry<NISTPeptideMSPField, Pattern> entry : patternMap.entrySet()) {

					regexMatcher = entry.getValue().matcher(record[i]);
					if(regexMatcher.find()) {

						if(entry.getKey().equals(NISTPeptideMSPField.NAME)) {
							String name = regexMatcher.group(1);
							peptideRecord.addProperty(NISTPeptideMSPField.NAME, name);
							peptideRecord.setPeptideSequence(name.replaceAll("/.+", ""));
						}
						if(entry.getKey().equals(NISTPeptideMSPField.COMMENTS)) {
							peptideRecord.setComments(regexMatcher.group(1));
							parseComments(peptideRecord, commentsPatternMap);
						}
					}
				}
			}
		}
		if (pnum > 0) {

			Pattern msmsPattern = Pattern.compile("^([0-9\\.]+)\\s?,?\\s?([0-9,\\.]+)\\s?(.+)?");
			Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
			for(int i=spectrumStart; i<record.length; i++) {

				regexMatcher = msmsPattern.matcher(record[i]);
				if(regexMatcher.find()) {

					MsPoint dp = new MsPoint(
							Double.parseDouble(regexMatcher.group(1)), 
							Double.parseDouble(regexMatcher.group(2)));

					if(regexMatcher.groupCount() == 3 && regexMatcher.group(3) != null)
						dp.setAdductType(regexMatcher.group(3).replaceAll("^\"|\"$", ""));
					

					dataPoints.add(dp);
				}
			}
			peptideRecord.setSpectrum(dataPoints);
			String parrent = peptideRecord.getProperties().get(NISTPeptideMSPField.Parent);
			if(parrent != null) {
				
				double parentMz = Double.parseDouble(parrent);
				double parentIntensity = 
						dataPoints.stream().mapToDouble(p -> p.getIntensity()).
						max().getAsDouble() / 100.0d;
				peptideRecord.addPrecursor(new MsPoint(parentMz, parentIntensity));
			}
		}
		else {
			throw new IllegalArgumentException("No peak data!");
		}
		String charge = peptideRecord.getProperties().get(NISTPeptideMSPField.Charge);
		if(charge != null) {
			peptideRecord.setCharge(Integer.parseInt(charge));
			if(peptideRecord.getCharge() < 0)
				peptideRecord.setPolarity(Polarity.Negative);
		}		
		return peptideRecord;
	}

	private static void parseComments(
			NISTPeptideTandemMassSpectrum peptideRecord, 
			Map<NISTPeptideMSPField, Pattern> commentsPatternMap) {

		String comments = peptideRecord.getComments();
		if(comments == null || comments.isEmpty())
			return;
		
		comments+= " ";
		for(Entry<NISTPeptideMSPField, Pattern>entry : commentsPatternMap.entrySet()) {
			
			Matcher regexMatcher = 
					commentsPatternMap.get(entry.getKey()).matcher(comments);
			if (regexMatcher.find()) {
				
				String value = regexMatcher.group().
						replace(entry.getKey().getName() + "=", "").replaceAll("\"", "").trim();
				peptideRecord.addProperty(entry.getKey(), value);
			}
		}		
	}
}






































