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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;

/**
 * @author Sasha
 *
 */
public class MsImportUtils {

	@SuppressWarnings("unchecked")
	public static TandemMassSpectrum parseAgilentMsMsExportFile(File xmInput) throws Exception {

		TandemMassSpectrum msms = null;
		Document msmsDocument = XmlUtils.readXmlFile(xmInput);
		XPathExpression expr = null;
		List<Element>spectraNodes;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		expr = xpath.compile("//Report/SpecRawData");
		spectraNodes = (List<Element>) expr.evaluate(msmsDocument, XPathConstants.NODESET);

		Pattern fragVoltagePattern = Pattern.compile("Frag=(\\d+\\.\\d+)V");
		Pattern cidPattern = Pattern.compile("CID@(\\d+\\.\\d+)");
		Pattern parentIonPattern = Pattern.compile("\\((\\d+\\.\\d+)\\[z=\\d\\].+\\)");
		Matcher regexMatcher;
		double fragVoltage = 0.0d;
		double cid = 0.0d;
		double parentMass = 0.0d;

		for (int i = 0; i < spectraNodes.size(); i++) {

			//	Find first MSMS spectrum
			Element cpdElement = spectraNodes.get(i);		
			String title = cpdElement.getChild("Title").getText();
			
			//	String title = cpdElement.getElementsByTagName("Title").item(0).getFirstChild().getNodeValue();
			
			if(title.contains("Product Ion")) {

				Polarity polarity = null;
				if(title.startsWith("+"))
					polarity = Polarity.Positive;

				if(title.startsWith("-"))
					polarity = Polarity.Negative;

				if(polarity == null)
					throw new Exception("MSMS polarity not defined");

				regexMatcher = fragVoltagePattern.matcher(title);
				if (regexMatcher.find())
					fragVoltage = Double.parseDouble(regexMatcher.group(1));

				regexMatcher = cidPattern.matcher(title);
				if (regexMatcher.find())
					cid = Double.parseDouble(regexMatcher.group(1));

				regexMatcher = parentIonPattern.matcher(title);
				if (regexMatcher.find())
					parentMass = Double.parseDouble(regexMatcher.group(1));

				MsPoint parent  = new MsPoint(parentMass, 1000.0d);
				msms = new TandemMassSpectrum(2, parent, polarity);
				msms.setFragmenterVoltage(fragVoltage);
				msms.setCidLevel(cid);

				// Parse spectrum
				ArrayList<MsPoint>points = new ArrayList<MsPoint>();
				List<Element> peaks = cpdElement.getChildren("d");  //.getElementsByTagName("d");

				for (int j = 0; j < peaks.size(); j++) {

					Element peakElement = (Element) peaks.get(j);
					double mz = peakElement.getAttribute("x").getDoubleValue(); //	Double.parseDouble();
					double intensity = peakElement.getAttribute("y").getDoubleValue(); //	Double.parseDouble(peakElement.getAttribute("y"));
					points.add(new MsPoint(mz, intensity));
				}
				msms.setSpectrum(points);
			}
		}
		return msms;
	}

	public static TandemMassSpectrum parseMSPspectrum(
			File mspInput, LibraryMsFeature activeFeature) throws Exception {

		List<List<String>> mspChunks = parseMspInputFile(mspInput);

		if(mspChunks.isEmpty())
			return null;

		return parseMspDataSource(mspChunks.get(0), activeFeature);
	}

	/**
	 * Parse single MSP record converted to list of strings per line
	 * Try to determine parent ion from the MSP fields or from parent library feature
	 *
	 * @param sourceText - MSP record converted to list of strings per line
	 * @param activeFeature - parent library feature
	 * @return
	 * @throws Exception
	 */
	public static TandemMassSpectrum parseMspDataSource(List<String> sourceText, LibraryMsFeature activeFeature)
			throws Exception {

		TandemMassSpectrum msmsSet = null;
		int spectrumStart = 0;
		int pnum = 0;
		double precursorMz = 0.0;
		Matcher regexMatcher;
		List<ObjectAnnotation> annotations = new ArrayList<ObjectAnnotation>();
		Pattern searchPattern;

//		TODO handle through separate simple oblect for text annotation
		/*
		// Add name to annotations
		Pattern searchPattern = Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":?\\s+(.+)");
		regexMatcher = searchPattern.matcher(sourceText.get(0).trim());
		if (regexMatcher.find())
			annotations.add(new ObjectAnnotation(regexMatcher.group(1)));

		// Add comments to annotations
		searchPattern = Pattern
				.compile("(?i)^(" + MSPField.COMMENT.getName() + "|" + MSPField.COMMENT.getName() + "s):?\\s+(.+)");
		for (int i = 0; i < sourceText.size(); i++) {

			String line = sourceText.get(i).trim();

			regexMatcher = searchPattern.matcher(sourceText.get(i).trim());
			if (regexMatcher.find())
				annotations.add(new ObjectAnnotation(regexMatcher.group(2)));
		}
		*/
					
		// Find polarity
		searchPattern = Pattern.compile("(?i)^" + MSPField.ION_MODE.getName() + ":?\\s+([PN])");
		Polarity polarity = null;
		for (int i = 0; i < sourceText.size(); i++) {

			regexMatcher = searchPattern.matcher(sourceText.get(i).trim());
			if (regexMatcher.find()) {

				polarity = Polarity.getPolarityByCode(regexMatcher.group(1));
				break;
			}
		}
		if (polarity == null) {
			throw new Exception("MSMS polarity not defined!");
		}
		// Try to find precursor mass
		searchPattern = Pattern.compile("(?i)^" + MSPField.PRECURSORMZ.getName() + ":?\\s+(\\d+)");
		for (int i = 0; i < sourceText.size(); i++) {

			regexMatcher = searchPattern.matcher(sourceText.get(i).trim());
			if (regexMatcher.find()) {

				precursorMz = Double.parseDouble(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
		}

		if (precursorMz == 0.0d && activeFeature != null)
			precursorMz = activeFeature.getBasePeakMz();

		// Find number of peaks and spectrum data starting line
		searchPattern = Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+(\\d+)");
		for (int i = 0; i < sourceText.size(); i++) {

			regexMatcher = searchPattern.matcher(sourceText.get(i).trim());
			if (regexMatcher.find()) {

				pnum = Integer.parseInt(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
		}
		if (pnum > 0) {

			StringBuilder msmsStringBuilder = new StringBuilder();
			sourceText.stream().skip(spectrumStart).forEach(l -> msmsStringBuilder.append(" " + l.trim()));
			String[] msDataArray = msmsStringBuilder.toString().replaceAll("[\\,,;,:,\\(,\\),\\[,\\],\\{,\\}]", " ")
					.replaceAll("\\s+", " ").trim().split(" ", 0);

			if (msDataArray.length != pnum * 2) {
				throw new IllegalArgumentException("Bad peak data!");
			}
			Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
			for (int i = 0; i < msDataArray.length; i = i + 2) {
				dataPoints.add(new MsPoint(Double.parseDouble(msDataArray[i]), Double.parseDouble(msDataArray[i + 1])));
			}
			msmsSet = new TandemMassSpectrum(polarity);
			msmsSet.setSpectrum(dataPoints);
			//	Set name (stopgap code to carry over unique IDs when importing decoy)
			searchPattern = Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":?\\s+(.+)");
			regexMatcher = searchPattern.matcher(sourceText.get(0).trim());
			if (regexMatcher.find())
				msmsSet.setDescription(regexMatcher.group(1));

			// TODO Add identity tracing real source
			msmsSet.setSpectrumSource(SpectrumSource.LIBRARY);

			// Add annotations
			if(!annotations.isEmpty())
				msmsSet.getAnnotations().addAll(annotations);

			if (precursorMz > 0)
				msmsSet.setParent(new MsPoint(precursorMz, 1000.0d));
		} else {
			throw new IllegalArgumentException("No peak data!");
		}
		return msmsSet;
	}
	
	public static TandemMassSpectrum parseMspDataSource(List<String> sourceText, Polarity polarity) {

		TandemMassSpectrum msmsSet = null;
		int spectrumStart = 0;
		int pnum = 0;
		double precursorMz = 0.0;
		Matcher regexMatcher;
		Pattern searchPattern;

		// Try to find precursor mass
		searchPattern = Pattern.compile("(?i)^" + MSPField.PRECURSORMZ.getName() + ":?\\s+(\\d+\\.*\\d*)");
		for (int i = 0; i < sourceText.size(); i++) {

			regexMatcher = searchPattern.matcher(sourceText.get(i).trim());
			if (regexMatcher.find()) {

				precursorMz = Double.parseDouble(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
		}
		// Find number of peaks and spectrum data starting line
		searchPattern = Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+(\\d+)");
		for (int i = 0; i < sourceText.size(); i++) {

			regexMatcher = searchPattern.matcher(sourceText.get(i).trim());
			if (regexMatcher.find()) {

				pnum = Integer.parseInt(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
		}
		if (pnum > 0) {

			StringBuilder msmsStringBuilder = new StringBuilder();
			sourceText.stream().skip(spectrumStart).forEach(l -> msmsStringBuilder.append(" " + l.trim()));
			String[] msDataArray = msmsStringBuilder.toString().replaceAll("[\\,,;,:,\\(,\\),\\[,\\],\\{,\\}]", " ")
					.replaceAll("\\s+", " ").trim().split(" ", 0);

			if (msDataArray.length != pnum * 2) {
				System.out.println("Bad peak data!");
				System.out.print(StringUtils.join(sourceText, "\n") + "\n");
				return null;
			}
			Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
			for (int i = 0; i < msDataArray.length; i = i + 2) {
				dataPoints.add(new MsPoint(Double.parseDouble(msDataArray[i]), Double.parseDouble(msDataArray[i + 1])));
			}
			msmsSet = new TandemMassSpectrum(polarity);
			msmsSet.setSpectrum(dataPoints);
			//	Set name (stopgap code to carry over unique IDs when importing decoy)
			searchPattern = Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":?\\s+(.+)");
			regexMatcher = searchPattern.matcher(sourceText.get(0).trim());
			if (regexMatcher.find())
				msmsSet.setDescription(regexMatcher.group(1));

			msmsSet.setSpectrumSource(SpectrumSource.LIBRARY);
			if (precursorMz > 0) {
				
				double maxIntensity = msmsSet.getSpectrum().
						stream().mapToDouble(p -> p.getIntensity()).
						max().getAsDouble();
				msmsSet.setParent(new MsPoint(precursorMz, maxIntensity / 4.0d));
			}
//			else {
//				System.out.print(StringUtils.join(sourceText, "\n"));
//			}
		} else {
			return null;
		}
		return msmsSet;
	}

	public static List<List<String>> parseMspInputFile(File inputFile) {

		List<List<String>> mspChunks = new ArrayList<List<String>>();
		List<String> allLines = TextUtils.readTextFileToList(inputFile.getAbsolutePath());
		List<String> chunk = new ArrayList<String>();
		Pattern namePattern = Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":");
		Pattern pnumPattern = Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+\\d+");
		Matcher regexMatcher;

		for (String line : allLines) {

			regexMatcher = namePattern.matcher(line.trim());
			if (regexMatcher.find()) {

				if (!chunk.isEmpty()) {

					regexMatcher = pnumPattern.matcher(line.trim());
					Optional<String> numPeaks = chunk.stream()
							.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

					if (numPeaks.isPresent()) {

						List<String> newChunk = new ArrayList<String>();
						newChunk.addAll(chunk);
						mspChunks.add(newChunk);
					}
					chunk.clear();
				}
			}
			if(!line.trim().isEmpty())
				chunk.add(line.trim());
		}
		return mspChunks;
	}
}
























