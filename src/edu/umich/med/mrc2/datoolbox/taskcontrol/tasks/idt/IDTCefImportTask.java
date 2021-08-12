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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.LibMatchedSimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentDatabaseFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTMsDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class IDTCefImportTask extends AbstractTask {

	protected DataFile dataFile;
	protected DataPipeline dataPipeline;
	protected DataExtractionMethod dataExtractionMethod;
	protected Document dataDocument;
	protected File inputCefFile;
	protected HashSet<LibMatchedSimpleMsFeature>features;
	protected Collection<String> importLog;
	protected Connection conn;

	public IDTCefImportTask(
			DataFile dataFile,
			DataPipeline dataPipeline) {

		super();
		this.dataFile = dataFile;
		this.dataPipeline = dataPipeline;
		this.dataExtractionMethod = dataPipeline.getDataExtractionMethod();
		inputCefFile = new File(dataFile.getFullPath());
		features = new HashSet<LibMatchedSimpleMsFeature>();
		importLog = new ArrayList<String>();
	}
	

	public IDTCefImportTask(
			DataFile dataFile,
			DataExtractionMethod dataExtractionMethod) {

		super();
		this.dataFile = dataFile;
		this.dataPipeline = null;
		this.dataExtractionMethod = dataExtractionMethod;
		inputCefFile = new File(dataFile.getFullPath());
		features = new HashSet<LibMatchedSimpleMsFeature>();
		importLog = new ArrayList<String>();
	}

	@Override
	public void run() {

		taskDescription = "Importing data from " + dataFile.getName();
		setStatus(TaskStatus.PROCESSING);
		try {
			dataDocument = null;
			// Read CEF file
			try {
				dataDocument = XmlUtils.readXmlFile(inputCefFile);
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
				try {
					mapCompoundIdsToDatabase();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					uploadParsedData();
				}
				catch (Exception e) {
					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		} catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void mapCompoundIdsToDatabase() throws Exception {
		
		conn = ConnectionManager.getConnection();
		taskDescription = "Mapping compound IDs to database for " + dataFile.getName();
		total = features.size();
		processed = 0;
		for(LibMatchedSimpleMsFeature feature  : features) {
			
//			CompoundIdentity mappedId = null;
//			try {
//				
//				//	TODO move to a separate cycle?
//				mappedId = CompoundDatabaseUtils.mapLibraryCompoundIdentity(cid, conn);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(mappedId == null) {
//				String error = cid.getName();
//				if(cid.getPrimaryDatabase() != null) {
//					error += "\t" + cid.getPrimaryDatabase().getName() + "\t" +
//						cid.getPrimaryDatabaseId() + "\t" + cid.getFormula();
//				}
//				importLog.add(error);
//				cid.setPrimaryDatabase(null);
//				return null;
//			}
//			else {
//				return mappedId;
//			}
			
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	protected void uploadParsedData() throws Exception {

		conn = ConnectionManager.getConnection();
		taskDescription = "Uploading data data from " + dataFile.getName() + " to database";
		total = features.size();
		processed = 0;
		String dataAnalysisId = IDTUtils.addNewDataAnalysis(
				dataExtractionMethod, dataFile.getInjectionId(), conn);
		for(LibMatchedSimpleMsFeature feature  : features) {
			IDTMsDataUtils.uploadExperimentalMsMsFeature(feature, dataAnalysisId, conn);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	protected void parseCefData() throws Exception {
	
		conn = ConnectionManager.getConnection();
		taskDescription = "Prsing CEF data file...";
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
			MassSpectrum spectrum =
					parseSpectraNodes(cpdElement.getElementsByTagName("Spectrum"));

			String targetId = getTargetId(cpdElement);
			String mass = null;
			LibMatchedSimpleMsFeature msf = 
					new LibMatchedSimpleMsFeature(targetId, spectrum, rt, dataPipeline);
			if(!locationElement.getAttribute("m").isEmpty()) {
				double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
				mass = MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass);
				msf.setNeutralMass(neutralMass);
			}
			else {
				mass = MRC2ToolBoxConfiguration.getMzFormat().format(spectrum.getMonoisotopicMz());
			}
			Element msDetailsElement = (Element) cpdElement.getElementsByTagName("MSDetails").item(0);
			String sign = msDetailsElement.getAttribute("p");
			Polarity polarity = Polarity.Positive;
			if(sign.equals("-"))
				polarity = Polarity.Negative;

			msf.setPolarity(polarity);
			
			// Parse identifications
			TandemMassSpectrum instrumentMsms = spectrum.getTandemSpectra().stream().
					filter(s -> (s.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL))).
					findFirst().orElse(null);

			TandemMassSpectrum dbMatch = spectrum.getTandemSpectra().stream().
					filter(s -> (s.getSpectrumSource().equals(SpectrumSource.LIBRARY)
							|| s.getSpectrumSource().equals(SpectrumSource.DATABASE))).
					findFirst().orElse(null);

			if(instrumentMsms != null) {
				mass = MRC2ToolBoxConfiguration.getMzFormat().format(instrumentMsms.getParent().getMz());
				if(dbMatch != null) {
					dbMatch.setCidLevel(instrumentMsms.getCidLevel());
					dbMatch.setPolarity(instrumentMsms.getPolarity());
				}
			}
			//	Get name
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					mass + "_" + locationElement.getAttribute("rt");
			msf.setName(name);
			CompoundIdentity id =  parseIdentity(cpdElement);
			if(id != null) {
				MsFeatureIdentity msid =
						new MsFeatureIdentity(id, CompoundIdentificationConfidence.ACCURATE_MASS);

				if(dbMatch != null) {
					msid.setConfidenceLevel(CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);

					double score = getLibraryMatchScore(cpdElement);
					String mrc2msmsId = getMrc2msmsIdForFeature(dbMatch, id);
					if(mrc2msmsId != null) {
						ReferenceMsMsLibraryMatch rmsMatch = new ReferenceMsMsLibraryMatch(mrc2msmsId, score);
						msid.setReferenceMsMsLibraryMatch(rmsMatch);
					}
					else {
						String importError =
								"Could not match library spectrum for " +
								name + " in " + inputCefFile.getName() + "\n";
						importLog.add(importError);
					}
//					if(metlinId != null && instrumentMsms.getCidLevel() > 0) {
//
//						mrc2msmsId = getMrc2MSMSIdForMetlinId(metlinId, instrumentMsms.getCidLevel(), idtConn);
//						ReferenceMsMsLibraryMatch rmsMatch = new ReferenceMsMsLibraryMatch(mrc2msmsId, score);
//						msid.setReferenceMsMsLibraryMatch(rmsMatch);
//					}
				}
				msf.setIdentity(msid);
			}
			else {
				id = new CompoundIdentity(name, null, spectrum.getMonoisotopicMz());
				MsFeatureIdentity msid = new MsFeatureIdentity(id, CompoundIdentificationConfidence.ACCURATE_MASS);
				if(instrumentMsms != null)
					msid.setConfidenceLevel(CompoundIdentificationConfidence.UNKNOWN_MSMS_RT);

				msf.setIdentity(msid);
			}
			//	Add extra data for feature
			if(!locationElement.getAttribute("a").isEmpty())
				msf.setArea(Double.parseDouble(locationElement.getAttribute("a")));

			if(!locationElement.getAttribute("y").isEmpty())
				msf.setHeight(Double.parseDouble(locationElement.getAttribute("y")));

			features.add(msf);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private String getMrc2msmsIdForFeature(TandemMassSpectrum msms, CompoundIdentity cid) throws Exception {

		//	TODO for now this is limited to METLIN only, make sure METLIN ID is present in the original form
		String mrcMsMsId = null;
		if(cid.getDbId(CompoundDatabaseEnum.METLIN) == null)
			return null;
		
		String metlinId = cid.getDbId(CompoundDatabaseEnum.METLIN).replace("METLIN:", "");
		String spectrumHash = MsUtils.calculateSpectrumHash(msms.getSpectrum());
		String idQuery =
				"SELECT C.MRC2_LIB_ID, SPECTRUM_HASH " +
				"FROM REF_MSMS_LIBRARY_COMPONENT C " +
				"WHERE C.ORIGINAL_LIBRARY_ID = ? " +
				"AND C.LIBRARY_NAME = 'METLIN' " +
				"AND C.COLLISION_ENERGY = ? " +
				"AND C.POLARITY = ?";
		PreparedStatement idPs = conn.prepareStatement(idQuery);
		idPs.setString(1, metlinId);
		idPs.setDouble(2, msms.getCidLevel());
		idPs.setString(3, msms.getPolarity().getCode());
		ResultSet rs = idPs.executeQuery();
		while(rs.next()) {
			if(spectrumHash.equals(rs.getString("SPECTRUM_HASH")))
				mrcMsMsId = rs.getString("MRC2_LIB_ID");
			else {
				//	TODO printout why there is a mismatch
				System.out.println("Couldn't find MRC2 MSMS library match for METLIN ID " + metlinId + "; wrong HASH " + spectrumHash);
			}
		}
		rs.close();
		idPs.close();
		return mrcMsMsId;
	}

	protected MassSpectrum parseSpectraNodes(NodeList spectra) {

		MassSpectrum spectrum = new MassSpectrum();
		TandemMassSpectrum observedMsms = null;
		TandemMassSpectrum libMsms = null;

		for (int i = 0; i < spectra.getLength(); i++) {

			Element spectrumElement = (Element) spectra.item(i);
			String ta = spectrumElement.getAttribute("type");

			//	Add MS1
			if(ta.equals(AgilentCefFields.MS1_SPECTRUM.getName())
					|| spectrumElement.getAttribute("type").equals(AgilentCefFields.FBF_SPECTRUM.getName())) {

				Map<Adduct,Collection<MsPoint>>adductMap = parseMsOneSpectrumElement(spectrumElement);
				adductMap.entrySet().stream().forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));
				String detectionAlgorithm =  spectrumElement.getAttribute("cpdAlgo");
				if(!detectionAlgorithm.isEmpty())
					spectrum.setDetectionAlgorithm(detectionAlgorithm);
			}
			//	Add observed MSMS
			if(ta.equals(AgilentCefFields.MS2_SPECTRUM.getName())) {
				observedMsms = parseMsTwoSpectrumElement(spectrumElement);
				observedMsms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);
				spectrum.addTandemMs(observedMsms);
			}
		}
		//	Add library MS2 spectrum
		if (observedMsms != null) {

			for (int i = 0; i < spectra.getLength(); i++) {

				Element spectrumElement = (Element) spectra.item(i);
				String ta = spectrumElement.getAttribute("type");
				if (ta.equals(AgilentCefFields.LIBRARY_MS2_SPECTRUM.getName())) {

					libMsms = parseLibMsTwoSpectrumElement(spectrumElement, observedMsms.getPolarity());
					if (libMsms != null) {
						libMsms.setSpectrumSource(SpectrumSource.LIBRARY);
						spectrum.addTandemMs(libMsms);
					}
					break;
				}
			}
		}
		return spectrum;
	}

	protected TandemMassSpectrum parseLibMsTwoSpectrumElement(Element spectrumElement, Polarity polarity) {

		NodeList precursors = spectrumElement.getElementsByTagName("mz");
		double mz = Double.parseDouble(precursors.item(0).getFirstChild().getNodeValue());
		TandemMassSpectrum msms = new TandemMassSpectrum(2, new MsPoint(mz, 999.0d), polarity);
		msms.setSpectrumSource(SpectrumSource.LIBRARY);
		NodeList msmsPeaks = spectrumElement.getElementsByTagName("p");
		Collection<MsPoint>msmsPoints = new ArrayList<MsPoint>();
		for (int j = 0; j < msmsPeaks.getLength(); j++) {

			Element peakElement = (Element) msmsPeaks.item(j);
			msmsPoints.add(new MsPoint(
					Double.parseDouble(peakElement.getAttribute("x")),
					Double.parseDouble(peakElement.getAttribute("y"))));
		}
		msms.setSpectrum(msmsPoints);
		return msms;
	}

	protected Map<Adduct,Collection<MsPoint>>parseMsOneSpectrumElement(Element spectrumElement){

		Element msDetails = (Element) spectrumElement.getElementsByTagName("MSDetails").item(0);
		String sign = msDetails.getAttribute("p");
		Polarity pol = null;
		if(sign.equals("+"))
			pol = Polarity.Positive;

		if(sign.equals("-"))
			pol = Polarity.Negative;

		Map<Adduct,Collection<MsPoint>>cmMap = new TreeMap<Adduct,Collection<MsPoint>>();
		NodeList msPeaks = spectrumElement.getElementsByTagName("p");

		//	Check if no adducts are specified
		if(((Element) msPeaks.item(0)).getAttribute("s").isEmpty()) {
			HashSet<MsPoint> points = new HashSet<MsPoint>();

			for (int j = 0; j < msPeaks.getLength(); j++) {

				Element peakElement = (Element) msPeaks.item(j);
				points.add(new MsPoint(
						Double.parseDouble(peakElement.getAttribute("x")),
						Double.parseDouble(peakElement.getAttribute("y"))));
			}
			cmMap.put(AdductManager.getDefaultAdductForPolarity(pol), points);
			return cmMap;
		}
		Map<String,Collection<MsPoint>>adductMap = new TreeMap<String,Collection<MsPoint>>();
		for (int j = 0; j < msPeaks.getLength(); j++) {

			Element peakElement = (Element) msPeaks.item(j);
			String adduct = peakElement.getAttribute("s").replaceAll("\\+[0-9]+$", "");
			if(!adductMap.containsKey(adduct))
				adductMap.put(adduct, new HashSet<MsPoint>());

			adductMap.get(adduct).add(new MsPoint(
					Double.parseDouble(peakElement.getAttribute("x")),
					Double.parseDouble(peakElement.getAttribute("y"))));
		}
		for (Entry<String, Collection<MsPoint>> entry : adductMap.entrySet()) {

			Adduct adduct = AdductManager.getAdductByCefNotation(entry.getKey());
			if(adduct != null)
				cmMap.put(adduct, entry.getValue());
		}
		return cmMap;
	}

	protected TandemMassSpectrum parseMsTwoSpectrumElement(Element spectrumElement){

		NodeList precursors = spectrumElement.getElementsByTagName("mz");
		double mz = Double.parseDouble(precursors.item(0).getFirstChild().getNodeValue());
		Element msDetails = (Element) spectrumElement.getElementsByTagName("MSDetails").item(0);
		String sign = msDetails.getAttribute("p");
		Polarity pol = null;
		if(sign.equals("+"))
			pol = Polarity.Positive;

		if(sign.equals("-"))
			pol = Polarity.Negative;

		TandemMassSpectrum msms = new TandemMassSpectrum(2, new MsPoint(mz, 999.0d), pol);
		String detectionAlgorithm =  spectrumElement.getAttribute("cpdAlgo");
		if(!detectionAlgorithm.isEmpty())
			msms.setDetectionAlgorithm(detectionAlgorithm);

		String ionisationType =  msDetails.getAttribute("is");
		if(!ionisationType.isEmpty())
			msms.setIonisationType(ionisationType);
		String collisionEnergy = msDetails.getAttribute("ce").replaceAll("V", "");
		if(!collisionEnergy.isEmpty()) {
			double ce = Double.parseDouble(collisionEnergy);
			msms.setCidLevel(ce);
		}
		String fragVoltage = msDetails.getAttribute("fv").replaceAll("V", "");
		if(!fragVoltage.isEmpty()) {
			double fv = Double.parseDouble(fragVoltage);
			msms.setFragmenterVoltage(fv);
		}
		NodeList msmsPeaks = spectrumElement.getElementsByTagName("p");
		Collection<MsPoint>msmsPoints = new ArrayList<MsPoint>();
		for (int j = 0; j < msmsPeaks.getLength(); j++) {

			Element peakElement = (Element) msmsPeaks.item(j);
			msmsPoints.add(new MsPoint(
					Double.parseDouble(peakElement.getAttribute("x")),
					Double.parseDouble(peakElement.getAttribute("y"))));
		}
		msms.setSpectrum(msmsPoints);
		return msms;
	}

	protected String getTargetId(Element cpdElement) {

		NodeList dbReference = cpdElement.getElementsByTagName("Accession");
		if (dbReference.getLength() > 0) {

			for (int j = 0; j < dbReference.getLength(); j++) {

				Element idElement = (Element) dbReference.item(j);
				String database = idElement.getAttribute("db");
				String accession = idElement.getAttribute("id");

				if (!database.isEmpty() && !accession.isEmpty()) {

					if (accession.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName()))
						return accession;
				}
			}
		}
		return null;
	}

	protected double getLibraryMatchScore(Element libraryFeatureElement) {

		if(libraryFeatureElement.getElementsByTagName("Molecule").getLength() == 0)
			return 0.0d;

		Element molecule = (Element) libraryFeatureElement.getElementsByTagName("Molecule").item(0);
		NodeList scoreList = molecule.getElementsByTagName("Match");
		if (scoreList.getLength() > 0) {

			for (int i = 0; i < scoreList.getLength(); i++) {
				Element scoreElement = (Element) scoreList.item(i);
				if(scoreElement.getAttribute("algo").equals("lib"))
					return Double.parseDouble(scoreElement.getAttribute("score"));
			}
		}
		return 0.0d;
	}

	protected CompoundIdentity parseIdentity(Element libraryFeatureElement) {

		if(libraryFeatureElement.getElementsByTagName("Molecule").getLength() == 0)
			return null;

		Element molecule = (Element) libraryFeatureElement.getElementsByTagName("Molecule").item(0);
		String name = molecule.getAttribute("name").trim();
		if(name.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {

			CompoundIdentity unkId = new CompoundIdentity();
			unkId.setCommonName(name);
			return unkId;
		}
		String molecularFormula = molecule.getAttribute("formula").replaceAll("\\s+", "");
		if(name.isEmpty())	// MFG identities
			return new CompoundIdentity(molecularFormula, molecularFormula);

		CompoundIdentity cid = new CompoundIdentity(name, molecularFormula);

		//	Add identity
		NodeList iDlist = libraryFeatureElement.getElementsByTagName("Accession");
		if (iDlist.getLength() > 0) {

			for (int j = 0; j < iDlist.getLength(); j++) {

				Element idElement = (Element) iDlist.item(j);
				String database = idElement.getAttribute("db").trim();
				String accession = idElement.getAttribute("id").trim();
				if (!database.isEmpty() && !accession.isEmpty()) {

					CompoundDatabaseEnum db = AgilentDatabaseFields.getDatabaseByName(database);
					if(db == null)
						db = CompoundDatabaseEnum.getCompoundDatabaseByName(database);

					if(db != null) {

						if(db.equals(CompoundDatabaseEnum.METLIN))
							accession = "METLIN:" + accession;

						if(db.equals(CompoundDatabaseEnum.HMDB))
							accession = accession.replace("HMDB", "HMDB00");

						cid.addDbId(db, accession);
					}
				}
			}
		}
		return cid;
	}

	@Override
	public Task cloneTask() {
		
		if(dataPipeline == null)
			return new IDTCefImportTask(dataFile, dataExtractionMethod);
		else
			return new IDTCefImportTask(dataFile, dataPipeline);
	}

	/**
	 * @return the inputCefFile
	 */
	public File getInputCefFile() {
		return inputCefFile;
	}

	/**
	 * @return the importLog
	 */
	public Collection<String> getImportLog() {
		return importLog;
	}



}














