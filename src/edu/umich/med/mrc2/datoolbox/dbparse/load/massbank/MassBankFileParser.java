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

package edu.umich.med.mrc2.datoolbox.dbparse.load.massbank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class MassBankFileParser {

	public static List<List<String>> parseInputMassBankFile(File inputFile) {

		List<List<String>> dataChunks = new ArrayList<List<String>>();
		List<String> allLines = TextUtils.readTextFileToList(inputFile.getAbsolutePath());
		List<String> chunk = new ArrayList<String>();
		Pattern namePattern = Pattern.compile("(?i)^" + MassBankDataField.ACCESSION.getName() + ":");
		Pattern pnumPattern = Pattern.compile("(?i)^" + MassBankDataField.PK_NUM_PEAK.getName().replace("$", "\\$") + ":?\\s+\\d+");
		Matcher regexMatcher;
		for (String line : allLines) {

			regexMatcher = namePattern.matcher(line.trim());
			if (regexMatcher.find()) {

				if (!chunk.isEmpty()) {

					Optional<String> numPeaks = chunk.stream()
							.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

					if (numPeaks.isPresent()) {

						List<String> newChunk = new ArrayList<String>();
						newChunk.addAll(chunk);
						dataChunks.add(newChunk);
					}
					chunk.clear();
				}
			}
			if(!line.trim().isEmpty() && !line.trim().equals("//"))
				chunk.add(line.trim());
		}
		//	process last chunk
		if (!chunk.isEmpty()) {

			Optional<String> numPeaks = chunk.stream()
					.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

			if (numPeaks.isPresent()) {

				List<String> newChunk = new ArrayList<String>();
				newChunk.addAll(chunk);
				dataChunks.add(newChunk);
			}
			chunk.clear();
		}
		return dataChunks;
	}
	
	public static List<List<String>> parseLargeInputMassBankFile(File inputFile) {
		
		List<List<String>> dataChunks = new ArrayList<List<String>>();
		List<String> chunk = new ArrayList<String>();
		Pattern namePattern = Pattern.compile("(?i)^" + MassBankDataField.ACCESSION.getName() + ":");
		Pattern pnumPattern = Pattern.compile("(?i)^" + MassBankDataField.PK_NUM_PEAK.getName().replace("$", "\\$") + ":?\\s+\\d+");
		Matcher regexMatcher;
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(inputFile, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
		    while (it.hasNext()) {
		        String line = it.nextLine();
				regexMatcher = namePattern.matcher(line.trim());
				if (regexMatcher.find()) {

					if (!chunk.isEmpty()) {

						Optional<String> numPeaks = chunk.stream()
								.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

						if (numPeaks.isPresent()) {

							List<String> newChunk = new ArrayList<String>();
							newChunk.addAll(chunk);
							dataChunks.add(newChunk);
						}
						chunk.clear();
					}
				}
				if(!line.trim().isEmpty() && !line.trim().equals("//"))
					chunk.add(line.trim());
		    }
			//	process last chunk
			if (!chunk.isEmpty()) {

				Optional<String> numPeaks = chunk.stream()
						.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

				if (numPeaks.isPresent()) {

					List<String> newChunk = new ArrayList<String>();
					newChunk.addAll(chunk);
					dataChunks.add(newChunk);
				}
				chunk.clear();
			}
		} finally {
		   try {
			it.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return dataChunks;
	}

	
	/**
	 * Parse single MassBank record converted to list of strings per line
	 * Try to determine parent ion from the data fields or from parent library feature
	 *
	 * @param sourceText - MSP record converted to list of strings per line
	 * @return
	 */
	public static MassBankTandemMassSpectrum parseMassBankDataSource(List<String>sourceText){

		int spectrumStart = -1;
		int pnum = 0;
		double exactMass = 0.0d;
		double precursorMz = -1.0;
		String precursorString = "";
		Matcher regexMatcher = null;
		Map<MassBankDataField,Pattern>patternMap = new TreeMap<MassBankDataField,Pattern>();
		for(MassBankDataField field : MassBankDataField.values())
			patternMap.put(field, Pattern.compile("(?i)^" + 
					field.getName().replace("$", "\\$") + ":?\\s+(.+)"));

		String[] record = sourceText.toArray(new String[sourceText.size()]);
		spectrumStart = -1;

		//	Find polarity
		Pattern polarityPattern = Pattern.compile(
				"(?i)^" + MassBankDataField.AC_MASS_SPECTROMETRY_ION_MODE.getName().replace("$", "\\$") + 
				":?\\s+(POSITIVE|NEGATIVE)");
		Polarity polarity = null;
		for(int i=0; i<record.length; i++) {
			regexMatcher = polarityPattern.matcher(record[i]);
			if (regexMatcher.find()) {
				
				if(regexMatcher.group(1).equals("POSITIVE"))
					polarity = Polarity.Positive;
				else
					polarity = Polarity.Negative;

				break;
			}
		}
		MassBankTandemMassSpectrum msmsSet = new MassBankTandemMassSpectrum(polarity);
		Pattern peakBlockPattern = patternMap.get(MassBankDataField.PK_PEAK);
		//	Add all non-ms data
		for(int i=0; i<record.length; i++) {
			
			if(peakBlockPattern.matcher(record[i]).find()) {
				spectrumStart = i+1;
				break;			
			}		
			for (Entry<MassBankDataField, Pattern> entry : patternMap.entrySet()) {
				
				//	TODO put all that in properties for now, deal with it later on real MassBank records					
				regexMatcher = entry.getValue().matcher(record[i]);
				if(regexMatcher.find()) {
									
					if(entry.getKey().equals(MassBankDataField.PK_NUM_PEAK))
						pnum = Integer.parseInt(regexMatcher.group(1));

//						if(entry.getKey().equals(NISTmspField.DB_NUM)) {
//							msmsSet.setDbnum(Integer.parseInt(regexMatcher.group(1)));
//						}
//						else if(entry.getKey().equals(NISTmspField.EXACT_MASS)) {
//							msmsSet.setExactMass(Double.parseDouble(regexMatcher.group(1)));
//						}
//						else if(entry.getKey().equals(NISTmspField.NIST_NUMBER)) {
//							msmsSet.setNistNum(Integer.parseInt(regexMatcher.group(1)));
//						}
//						//	When CAS is on one line with NIST #
//						else if(entry.getKey().equals(NISTmspField.CAS)) {
//
//							casNistMatcher = casNistPattern.matcher(record[i]);
//							if(casNistMatcher.find()) {
//								msmsSet.addProperty(NISTmspField.CAS, casNistMatcher.group(1));
//								msmsSet.setNistNum(Integer.parseInt(casNistMatcher.group(2)));
//							}
//							else {
//								msmsSet.addProperty(NISTmspField.CAS, regexMatcher.group(1));
//							}
//						}
//						else if(entry.getKey().equals(NISTmspField.SYNONYM)){
//							msmsSet.addSynonym(regexMatcher.group(1));
//						}
//						else if(entry.getKey().equals(NISTmspField.COMMENTS)) {						
//							msmsSet.getNotes().add(regexMatcher.group(1));
//						}
//						else if(entry.getKey().equals(NISTmspField.NOTES)) {
//							
//							String[] notes = regexMatcher.group(1).split(";");
//							for(String note : notes)
//								msmsSet.getNotes().add(note.trim());
//						}
//						else if(entry.getKey().equals(NISTmspField.PRECURSORMZ)) {
//							precursorString = regexMatcher.group(1);
//						}
//						else if(entry.getKey().equals(NISTmspField.PEPTIDE_MODS)) {
//							msmsSet.setPeptideModifications(regexMatcher.group(1));
//						}
//						else if(entry.getKey().equals(NISTmspField.PEPTIDE_SEQUENCE)) {
//							msmsSet.setPeptideSequence(regexMatcher.group(1));
//						}
//						else {
				else if(entry.getKey().equals(MassBankDataField.CH_EXACT_MASS)) {
					exactMass = Double.parseDouble(regexMatcher.group(1));
					msmsSet.setExactMass(exactMass);
				}
				else if(entry.getKey().equals(MassBankDataField.ACCESSION)){
					msmsSet.setId(regexMatcher.group(1));
				}
				else
					msmsSet.addProperty(entry.getKey(), regexMatcher.group(1));
//						}
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
					dataPoints.add(dp);
				}
			}
			MsPoint[] dataPointsNorm = MsUtils.normalizeAndSortMsPattern(dataPoints);
			msmsSet.setSpectrum(Arrays.asList(dataPointsNorm));
			//	TODO this is a stop-gap to parse old Passatutto output where precursor M/Z is written out as integer
			//	and only M+H/M-H are used as adducts. If spectrum contains precursor within 20 ppm, the data point from spectrum will be used
			if (exactMass > 0) {
				
				Adduct adduct = AdductManager.getDefaultAdductForPolarity(polarity);
				precursorMz = MsUtils.getAdductMz(exactMass, adduct);
				Range precursorMzRange = MsUtils.createPpmMassRange(precursorMz, 20);				
				MsPoint precursor = dataPoints.stream().
						filter(p -> precursorMzRange.contains(p.getMz())).
						findFirst().orElse(null);
				if(precursor == null) 
					precursor = new MsPoint(precursorMz, 999.0d);

				msmsSet.setParent(precursor);
			}
		}
		else {
			throw new IllegalArgumentException("No peak data!");
		}
		return msmsSet;
	}
}
