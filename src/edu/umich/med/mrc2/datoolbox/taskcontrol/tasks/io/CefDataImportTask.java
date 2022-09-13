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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.ResultsFile;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class CefDataImportTask extends CEFProcessingTask {
	
	private DataFile dataFile;
	private ResultsFile resultsFile;
	private DataPipeline dataPipeline;
	private HashSet<SimpleMsFeature>features;

	private int fileIndex;
	private Matrix featureMatrix;
	private Matrix dataMatrix;
	private Map<String, Integer> featureCoordinateMap;
	private Map<String, List<Double>> retentionMap;
	private Map<String, List<Double>> mzMap;
	
	public CefDataImportTask(
			DataFile dataFile,
			ResultsFile resultsFile,
			int fileIndex,
			Matrix featureMatrix,
			Matrix dataMatrix,
			Map<String, Integer> featureCoordinateMap,
			Map<String, List<Double>> retentionMap,
			Map<String, List<Double>> mzMap) {

		this.dataFile = dataFile;
		this.resultsFile = resultsFile;
		this.fileIndex = fileIndex;
		this.featureMatrix = featureMatrix;
		this.dataMatrix = dataMatrix;
		this.featureCoordinateMap = featureCoordinateMap;
		this.retentionMap = retentionMap;
		this.mzMap = mzMap;

		total = 100;
		processed = 2;
		taskDescription = "Importing MS data from " + dataFile.getName();
		features = new HashSet<SimpleMsFeature>();
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public Task cloneTask() {
		return new CefDataImportTask(
				 dataFile,
				 resultsFile,
				 fileIndex,
				 featureMatrix,
				 dataMatrix,
				 featureCoordinateMap,
				 retentionMap,
				 mzMap);
	}

	@Override
	public void run() {
		
		if(featureCoordinateMap == null) {
			errorMessage = "Fature coordinates map missing";
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.PROCESSING);
		// Read CEF file
		String cefPath = null;
		if(resultsFile != null && resultsFile.getFullPath() != null)
			cefPath = resultsFile.getFullPath();	
		else if(dataPipeline != null && dataPipeline.getDataExtractionMethod() != null) 
			cefPath = dataFile.getResultForDataExtractionMethod(
					dataPipeline.getDataExtractionMethod()).getFullPath();
		else {
			System.err.println("Path to CEF file not specified");
			setStatus(TaskStatus.ERROR);
			return;
		}
		inputCefFile = new File(cefPath);		
		try {
			parseInputCefFile(inputCefFile);
		} catch (Exception e) {
			errorMessage = "Failed to parse " + inputCefFile.getName();
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		recordDataIntoFeatureMatrix();
//		try {
//			dataDocument = XmlUtils.readXmlFile(new File(cefPath));
//		} catch (Exception e) {
//			e.printStackTrace();
//			setStatus(TaskStatus.ERROR);
//		}
//		//	Parse CEF data
//		if(dataDocument != null) {
//			try {
//				parseCefData();
//				setStatus(TaskStatus.FINISHED);
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//				setStatus(TaskStatus.ERROR);
//			}
//		}
		setStatus(TaskStatus.FINISHED);
	}

	private void recordDataIntoFeatureMatrix() {
		// TODO Auto-generated method stub
		taskDescription = "Parsing CEF data file...";
		features = new HashSet<SimpleMsFeature>();
		total = inputFeatureList.size();
		processed = 0;
		
		long[] coordinates = new long[2];
		coordinates[0] = fileIndex;
		
		for(MsFeature feature : inputFeatureList) {

			SimpleMsFeature msf = new SimpleMsFeature(feature, dataPipeline);
				if(featureCoordinateMap.get(msf.getLibraryTargetId()) != null) {

					retentionMap.get(msf.getLibraryTargetId()).add(msf.getRetentionTime());
					mzMap.get(msf.getLibraryTargetId()).add(msf.getObservedSpectrum().getMonoisotopicMz());
					coordinates[1] = featureCoordinateMap.get(msf.getLibraryTargetId());
					featureMatrix.setAsObject(msf, coordinates);
					dataMatrix.setAsDouble(msf.getArea(), coordinates);
				}
				else {
					//	TODO handle library mismatches
					System.out.println(msf.getName() + "(" + msf.getLibraryTargetId() + ") not in the library.");
				}
			
			features.add(msf);
			processed++;
		}
	}

//	private void parseCefData() throws Exception {
//
//		taskDescription = "Parsing CEF data file...";
//		features = new HashSet<SimpleMsFeature>();
//		XPathExpression expr = null;
//		NodeList targetNodes;
//		XPathFactory factory = XPathFactory.newInstance();
//		XPath xpath = factory.newXPath();
//
//		expr = xpath.compile("//CEF/CompoundList/Compound");
//		targetNodes = (NodeList) expr.evaluate(dataDocument, XPathConstants.NODESET);
//		total = targetNodes.getLength();
//		processed = 0;
//		long[] coordinates = new long[2];
//		coordinates[0] = fileIndex;
//
//		for (int i = 0; i < targetNodes.getLength(); i++) {
//
//			Element cpdElement = (Element) targetNodes.item(i);
//			Element locationElement = (Element) cpdElement.getElementsByTagName("Location").item(0);
//			double rt = Double.parseDouble(locationElement.getAttribute("rt"));
//			double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
//			
//			//	Polarity 
//			Element msDetailsElement = (Element) cpdElement.getElementsByTagName("MSDetails").item(0);
//			String sign = msDetailsElement.getAttribute("p");
//			Polarity polarity = Polarity.Positive;
//			if(sign.equals("-"))
//				polarity = Polarity.Negative;
//
//			// Parse spectrum
//			MassSpectrum spectrum = new MassSpectrum();
//			NodeList peaks = cpdElement.getElementsByTagName("p");
//
//			for (int j = 0; j < peaks.getLength(); j++) {
//
//				Element peakElement = (Element) peaks.item(j);
//				String adduct = peakElement.getAttribute("s");
//				double mz = Double.parseDouble(peakElement.getAttribute("x"));
//				double intensity = Double.parseDouble(peakElement.getAttribute("y"));
//				int charge = Integer.parseInt(peakElement.getAttribute("z"));
//
//				spectrum.addDataPoint(mz, intensity, adduct, charge);
//			}
//			unmatchedAdducts.addAll(spectrum.finalizeCefImportSpectrum());
//
//			// Parse identifications
//			String targetId = getTargetId(cpdElement);
//			SimpleMsFeature msf = new SimpleMsFeature(targetId, spectrum, rt, dataPipeline);
//			msf.setPolarity(polarity);
//
//			// TODO	This is not required when matching to library, 
////			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + 
////					locationElement.getAttribute("m") + "_" + 
////					locationElement.getAttribute("rt");
////			if(cpdElement.getElementsByTagName("Molecule").item(0) != null) {
////
////				Element moleculeElement = (Element) cpdElement.getElementsByTagName("Molecule").item(0);
////				name = moleculeElement.getAttribute("name");
////
////				// Work-around for old data
////				if (name.isEmpty())
////					name = moleculeElement.getAttribute("formula");
////			}
////			msf.setName(name);
////
////			//	Add extra data for feature
////			msf.setNeutralMass(neutralMass);
//
//			if(!locationElement.getAttribute("a").isEmpty())
//				msf.setArea(Double.parseDouble(locationElement.getAttribute("a")));
//
//			if(!locationElement.getAttribute("y").isEmpty())
//				msf.setHeight(Double.parseDouble(locationElement.getAttribute("y")));
//
//			//	Insert feature in arrays
//			if(featureCoordinateMap != null) {
//				
//				if(featureCoordinateMap.get(msf.getLibraryTargetId()) != null) {
//
//					retentionMap.get(msf.getLibraryTargetId()).add(msf.getRetentionTime());
//					mzMap.get(msf.getLibraryTargetId()).add(msf.getObservedSpectrum().getMonoisotopicMz());
//					coordinates[1] = featureCoordinateMap.get(msf.getLibraryTargetId());
//					featureMatrix.setAsObject(msf, coordinates);
//					dataMatrix.setAsDouble(msf.getArea(), coordinates);
//				}
//				else {
//					//	TODO handle library mismatches
//					System.out.println(msf.getName() + "(" + msf.getLibraryTargetId() + ") not in the library.");
//				}
//			}
//			features.add(msf);
//			processed++;
//		}
//	}

//	private String getTargetId(Element cpdElement) {
//
//		NodeList dbReference = cpdElement.getElementsByTagName("Accession");
//		if (dbReference.getLength() > 0) {
//
//			for (int j = 0; j < dbReference.getLength(); j++) {
//
//				Element idElement = (Element) dbReference.item(j);
//				String database = idElement.getAttribute("db");
//				String accession = idElement.getAttribute("id");
//
//				if (!database.isEmpty() && !accession.isEmpty()) {
//
//					if (accession.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
//							|| accession.startsWith(DataPrefix.MS_FEATURE.getName()))
//						return accession;
//				}
//			}
//		}
//		return null;
//	}

	public DataFile getInputCefFile() {
		return dataFile;
	}

	public HashSet<SimpleMsFeature> getFeatures() {
		return features;
	}

	/**
	 * @return the unmatchedAdducts
	 */
	public TreeSet<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

}
