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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class CefLibraryImportTask extends CEFProcessingTask {

	private DataAnalysisProject currentProject;
	private DataPipeline dataPipeline;
	private String plusRtMask;
	private Matcher regexMatcher;
	private Pattern plusRtPattern;
	private ArrayList<MsFeature> unassigned;

	private HashSet<String>knownTargetIds;
	private HashSet<String>cpdDatabases;
	private Map<String, HashMap<String, String>> targetIdMap;
	private CompoundLibrary newLibrary;
	private String libraryId;
	private Map<String, MsFeatureIdentity> identityMap;
	private boolean matchToFeatures;
	private boolean generateLibraryFeatures;

	//	Debug only
	private Set<String>featureNames;

	public CefLibraryImportTask(
			DataPipeline dataPipeline,
			File inputFile,
			boolean matchToFeatures,
			boolean generateLibraryFeatures) {

		inputCefFile = inputFile;
		this.dataPipeline = dataPipeline;
		currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		taskDescription = "Importing library from  " + inputFile.getName();
		total = 100;
		processed = 0;
		libraryId = "";
		this.matchToFeatures = matchToFeatures;
		this.generateLibraryFeatures = generateLibraryFeatures;

		plusRtMask = "(\\d+.\\d+)@(\\d+.\\d+) [\\+\\-] (\\d+.\\d+)\\s*:*\\d*";
		plusRtPattern = Pattern.compile(plusRtMask);

		unmatchedAdducts = new TreeSet<String>();
		unassigned = new ArrayList<MsFeature>();
		identityMap =  new TreeMap<String, MsFeatureIdentity>();
		unassigned = new ArrayList<MsFeature>();
	}

	@Override
	public void run() {
		
		if(inputCefFile == null || !inputCefFile.exists()) {
			errorMessage = "Library file not found";
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.PROCESSING);

		if(currentProject.getCompoundLibraryForDataPipeline(dataPipeline) != null)
			clearCurrentLibrary();
		
		newLibrary = 
				new CompoundLibrary(FilenameUtils.getBaseName(inputCefFile.getPath()));
		newLibrary.setDataPipeline(dataPipeline);
		
		try {
			parseInputCefFile(inputCefFile);
		} catch (Exception e1) {
			errorMessage = "Failed to parse library file";
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		inputFeatureList.stream().
			forEach(f -> newLibrary.addFeature(new LibraryMsFeature(f)));
		
		copyLibraryFileToProject();
		
		if(matchToFeatures) {

			matchToFeatures();
			collectUnassignedFeatures();
		}
		currentProject.setCompoundLibraryForDataPipeline(dataPipeline, newLibrary);
		setStatus(TaskStatus.FINISHED);

//		try {
//
//
//			parseLibraryFile();
//			addIdentifications();
//			copyLibraryFileToProject();
//
//			if(matchToFeatures) {
//
//				matchToFeatures();
//				collectUnassignedFeatures();
//			}
//			currentProject.setCompoundLibraryForDataPipeline(dataPipeline, newLibrary);
//			setStatus(TaskStatus.FINISHED);
//		}
//		catch (Exception e) {
//
//			e.printStackTrace();
//			setStatus(TaskStatus.ERROR);
//		}
			
		
		setStatus(TaskStatus.FINISHED);
	}

	private void clearCurrentLibrary() {

		taskDescription = "Clearing old library data...";
		total = 100;
		processed = 10;

		for (MsFeature cf : currentProject.getMsFeaturesForDataPipeline(dataPipeline)) {

			cf.setSpectrum(null);
			cf.setNeutralMass(0.0d);
			cf.clearIdentification();
			processed++;
		}
	}

//	private void parseLibraryFile() throws Exception {
//
//		taskDescription = "Reading library file...";
//		featureNames = new TreeSet<String>();
//
//		knownTargetIds = new HashSet<String>();
//		cpdDatabases = new HashSet<String>();
//		targetIdMap = new HashMap<String, HashMap<String, String>>();
//
//		newLibrary = new CompoundLibrary(FilenameUtils.getBaseName(inputCefFile.getPath()));
//		newLibrary.setDataPipeline(dataPipeline);
//		Document cefLibraryDocument = null;
//		XPathExpression expr = null;
//		NodeList targetNodes;
//
//		XPathFactory factory = XPathFactory.newInstance();
//		XPath xpath = factory.newXPath();
//
//		cefLibraryDocument = XmlUtils.readXmlFile(inputLibraryFile);
//		libraryId = cefLibraryDocument.getDocumentElement().getAttribute("library_id");
//
//		if (cefLibraryDocument != null) {
//
//			expr = xpath.compile("//CEF/CompoundList/Compound");
//			targetNodes = (NodeList) expr.evaluate(cefLibraryDocument, XPathConstants.NODESET);
//			total = targetNodes.getLength();
//			processed = 0;
//
//			for (int i = 0; i < targetNodes.getLength(); i++) {
//
//				Element cpdElement = (Element) targetNodes.item(i);
//				Element locationElement = (Element) cpdElement.getElementsByTagName("Location").item(0);
//				Element msDetailsElement = (Element) cpdElement.getElementsByTagName("MSDetails").item(0);
//				double rt = Double.parseDouble(locationElement.getAttribute("rt"));
//				double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
//
//				// Parse spectrum
//				MassSpectrum spectrum = new MassSpectrum();
//				NodeList peaks = cpdElement.getElementsByTagName("p");
//				for (int j = 0; j < peaks.getLength(); j++) {
//
//					Element peakElement = (Element) peaks.item(j);
//					String adduct = peakElement.getAttribute("s");
//					double mz = Double.parseDouble(peakElement.getAttribute("x"));
//					double intensity = Double.parseDouble(peakElement.getAttribute("y"));
//					int charge = Integer.parseInt(peakElement.getAttribute("z"));
//
//					spectrum.addDataPoint(mz, intensity, adduct, charge);
//				}
//				unmatchedAdducts.addAll(spectrum.finalizeCefImportSpectrum());
//				try {
//					MsUtils.calculateMcMillanMassDefectForSpectrum(spectrum);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				//	Default unknown name
//				String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
//						MRC2ToolBoxConfiguration.defaultMzFormat.format(spectrum.getMonoisotopicMz()) + "_" + 
//						MRC2ToolBoxConfiguration.defaultRtFormat.format(rt);
//
//				if(cpdElement.getElementsByTagName("Molecule").item(0) != null) {
//
//					Element moleculeElement = (Element) cpdElement.getElementsByTagName("Molecule").item(0);
//					String molname = moleculeElement.getAttribute("name");
//
//					// Work-around for old data
//					if (molname.isEmpty())
//						molname = moleculeElement.getAttribute("formula");
//					
//					if(!molname.isEmpty() && !molname.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName()))
//						name = molname;
//				}
//				LibraryMsFeature lf = null;
//				if(generateLibraryFeatures)
//					lf = new LibraryMsFeature(name, spectrum, rt);
//				else {
//					lf = new LibraryMsFeature(name, rt);
//					lf.setSpectrum(spectrum);
//				}
//				lf.setNeutralMass(neutralMass);
//				Polarity pol = Polarity.Positive;
//				if(msDetailsElement.getAttribute("p").equals("-"))
//					pol = Polarity.Negative;
//
//				lf.setPolarity(pol);
//
//				// Parse identifications
//				NodeList dbReference = cpdElement.getElementsByTagName("Accession");
//
//				if (dbReference.getLength() > 0) {
//
//					HashMap<String, String> idMap = new HashMap<String, String>();
//
//					for (int j = 0; j < dbReference.getLength(); j++) {
//
//						Element idElement = (Element) dbReference.item(j);
//						String database = idElement.getAttribute("db");
//						cpdDatabases.add(database);
//						String accession = idElement.getAttribute("id");
//
//						if(!database.isEmpty() && !accession.isEmpty()){
//
//							if(accession.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
//									|| accession.startsWith(DataPrefix.MS_FEATURE.getName())){
//
//								lf.setId(accession);
//								if(!name.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName()))
//									knownTargetIds.add(accession);
//							}
//							else
//								idMap.put(database, accession);
//						}
//					}
//					targetIdMap.put(lf.getId(), idMap);
//				}
//				newLibrary.addFeature(lf);
//				processed++;
//			}
//		}
//	}

//	private void addIdentifications() {
//
//		taskDescription = "Adding identifications...";
//		total = 100;
//		processed = 20;
//
//		// Get Cpd databases
//		Map<String, CompoundDatabaseEnum>dbMap = cpdDatabases.stream().
//				map(dbid -> LibraryUtils.parseCompoundDatabaseName(dbid)).
//				collect(Collectors.toMap(CompoundDatabaseEnum::getName, Function.identity(),
//						(oldValue, newValue) -> oldValue));
//
//		//	Get known identifications
//		identityMap = null;
//		try {
//			identityMap = RemoteMsLibraryUtils.getCompoundIdentitiesByTargetIds(knownTargetIds, libraryId);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(identityMap != null) {
//
//			total = newLibrary.getFeatures().size();
//			processed = 0;
//			MsFeatureIdentity idf = null;
//
//			//	Assign identities
//			for(LibraryMsFeature feature : newLibrary.getFeatures()) {
//
//				idf = identityMap.get(feature.getId());
//
//				if(idf != null) {
//
//					idf.setIdentityName(feature.getName());
//					idf.getMsRtLibraryMatch().setExpectedRetention(feature.getRetentionTime());
//					feature.setPrimaryIdentity(idf);
//				}
//				if(targetIdMap.get(feature.getId()) != null) {
//
//					for (Entry<String, String> entry : targetIdMap.get(feature.getId()).entrySet()) {
//
//						if(dbMap.get(entry.getKey()) != null) {
//
//							CompoundIdentity cid =
//									new CompoundIdentity(dbMap.get(entry.getKey()), entry.getValue());
//							MsFeatureIdentity msId = new MsFeatureIdentity(cid,
//									CompoundIdentificationConfidence.ACCURATE_MASS_RT);
//
//							MsRtLibraryMatch libMatch = new MsRtLibraryMatch(feature.getId());
//							libMatch.setExpectedRetention(feature.getRetentionTime());
//							libMatch.setScore(100.0d);
//							msId.setMsRtLibraryMatch(libMatch);
//							msId.setIdentityName(feature.getName());
//							feature.addIdentity(msId);
//						}
//					}
//				}
//				processed++;
//			}
//		}
//	}

	private void matchToFeatures() {

		taskDescription = "Matching library entries to features ...";
		total = currentProject.getMsFeaturesForDataPipeline(dataPipeline).size();
		processed = 0;

		for (MsFeature cf : currentProject.getMsFeaturesForDataPipeline(dataPipeline)) {

			for(MsFeature lf : newLibrary.getFeatures()) {

				if (cf.getName().equals(lf.getName())) {

					cf.setSpectrum(lf.getSpectrum());
					cf.getSpectrum().finalizeCefImportSpectrum();
					cf.setNeutralMass(lf.getNeutralMass());

					for (MsFeatureIdentity cid : lf.getIdentifications())
						cf.addIdentity(cid);

					cf.setPrimaryIdentity(lf.getPrimaryIdentity());

					//	Set retention from library if not present
					if(cf.getRetentionTime() == 0.0d)
						cf.setRetentionTime(lf.getRetentionTime());
				}
			}
			processed++;
		}
	}

	//	TODO create separate folder for libraries in the project and copy library there
	private void copyLibraryFileToProject() {

		if (inputCefFile.exists()) {

			File libFile = new File(currentProject.getProjectDirectory() + 
					File.separator + inputCefFile.getName());

			if (!libFile.exists()) {

				try {
					FileUtils.copyFileToDirectory(
							inputCefFile, currentProject.getProjectDirectory());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void collectUnassignedFeatures() {

		taskDescription = "Checking for unassigned features ...";
		total = currentProject.getMsFeaturesForDataPipeline(dataPipeline).size();
		processed = 0;
		for (MsFeature cf : currentProject.getMsFeaturesForDataPipeline(dataPipeline)) {

			if (cf.getSpectrum() == null)
				unassigned.add(cf);

			processed++;
		}
	}

	@Override
	public Task cloneTask() {

		return new CefLibraryImportTask(
				dataPipeline, inputCefFile, matchToFeatures, generateLibraryFeatures);
	}

	public Collection<MsFeature> getUnassignedFeatures() {
		return unassigned;
	}

	public CompoundLibrary getParsedLibrary() {
		return newLibrary;
	}
}
