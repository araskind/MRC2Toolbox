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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ImportMinimalMSOneFeaturesFromCefTask  extends AbstractTask {

	private Document dataDocument;
	private DataFile dataFile;
	private Collection<SimpleMsFeature> features;
	private Set<String> unmatchedAdducts;
	private Collection<MinimalMSOneFeature>minFeatures;

	public ImportMinimalMSOneFeaturesFromCefTask(DataFile dataFile) {

		this.dataFile = dataFile;
		total = 100;
		processed = 2;
		taskDescription = "Importing MS data from " + dataFile.getName();
		features = new HashSet<SimpleMsFeature>();
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public Task cloneTask() {
		return new ImportMinimalMSOneFeaturesFromCefTask(dataFile);
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		dataDocument = null;
		// Read CEF file
		String cefPath = dataFile.getFullPath();
		try {
			dataDocument = XmlUtils.readXmlFile(new File(cefPath));
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		//	Parse CEF data
		if(dataDocument != null) {
			try {
				parseCefData();
			}
			catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		convertSimpleMsFeatureToMinimal();
		setStatus(TaskStatus.FINISHED);
	}

	private void convertSimpleMsFeatureToMinimal() {
		
		minFeatures = new TreeSet<MinimalMSOneFeature>(); 
		for(SimpleMsFeature sf : features) {
			
			double rt = sf.getRetentionTime();
			for(Adduct aduct : sf.getObservedSpectrum().getAdducts()) {
				
				double mz = sf.getObservedSpectrum().getMsForAdduct(aduct)[0].getMz();
				minFeatures.add(new MinimalMSOneFeature(mz, rt));
			}
		}
	}

	private void parseCefData() throws Exception {

		taskDescription = "Parsing CEF data file " + dataFile.getName();
		features = new HashSet<SimpleMsFeature>();
		XPathExpression expr = null;
		NodeList targetNodes;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		expr = xpath.compile("//CEF/CompoundList/Compound");
		targetNodes = (NodeList) expr.evaluate(dataDocument, XPathConstants.NODESET);
		total = targetNodes.getLength();
		processed = 0;

		for (int i = 0; i < targetNodes.getLength(); i++) {

			Element cpdElement = (Element) targetNodes.item(i);
			Element locationElement = (Element) cpdElement.getElementsByTagName("Location").item(0);
			double rt = Double.parseDouble(locationElement.getAttribute("rt"));
			double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
			
			//	Polarity 
			Element msDetailsElement = (Element) cpdElement.getElementsByTagName("MSDetails").item(0);
			String sign = msDetailsElement.getAttribute("p");
			Polarity polarity = Polarity.Positive;
			if(sign.equals("-"))
				polarity = Polarity.Negative;

			// Parse spectrum
			MassSpectrum spectrum = new MassSpectrum();
			NodeList peaks = cpdElement.getElementsByTagName("p");

			for (int j = 0; j < peaks.getLength(); j++) {

				Element peakElement = (Element) peaks.item(j);
				String adduct = peakElement.getAttribute("s");
				double mz = Double.parseDouble(peakElement.getAttribute("x"));
				double intensity = Double.parseDouble(peakElement.getAttribute("y"));
				int charge = Integer.parseInt(peakElement.getAttribute("z"));

				spectrum.addDataPoint(mz, intensity, adduct, charge);
			}
			unmatchedAdducts.addAll(spectrum.finalizeCefImportSpectrum());

			// Parse identifications
			String targetId = getTargetId(cpdElement);
			SimpleMsFeature msf = new SimpleMsFeature(targetId, spectrum, rt, null);
			msf.setPolarity(polarity);

			if(!locationElement.getAttribute("a").isEmpty())
				msf.setArea(Double.parseDouble(locationElement.getAttribute("a")));

			if(!locationElement.getAttribute("y").isEmpty())
				msf.setHeight(Double.parseDouble(locationElement.getAttribute("y")));

			features.add(msf);
			processed++;
		}
	}

	private String getTargetId(Element cpdElement) {

		NodeList dbReference = cpdElement.getElementsByTagName("Accession");
		if (dbReference.getLength() > 0) {

			for (int j = 0; j < dbReference.getLength(); j++) {

				Element idElement = (Element) dbReference.item(j);
				String database = idElement.getAttribute("db");
				String accession = idElement.getAttribute("id");

				if (!database.isEmpty() && !accession.isEmpty()) {

					if (accession.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
							|| accession.startsWith(DataPrefix.MS_FEATURE.getName()))
						return accession;
				}
			}
		}
		return null;
	}

	public DataFile getInputCefFile() {
		return dataFile;
	}

	public Collection<SimpleMsFeature> getFeatures() {
		return features;
	}

	/**
	 * @return the unmatchedAdducts
	 */
	public Set<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

	public Collection<MinimalMSOneFeature> getMinFeatures() {
		return minFeatures;
	}
}
