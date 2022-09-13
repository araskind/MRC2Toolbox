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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.LibraryUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public abstract class CEFProcessingTask extends AbstractTask {
	
	protected File inputCefFile;
	protected File outputCefFile;
	protected Collection<MsFeature> inputFeatureList;
	protected TreeSet<String> allAdducts;
	protected TreeSet<String> unmatchedAdducts;
	protected List<String> errorLog;

	public CEFProcessingTask() {
		super();
		allAdducts = new TreeSet<String>();
		unmatchedAdducts = new TreeSet<String>();
		errorLog = new ArrayList<String>();
	}

	protected void parseInputCefFile(File fileToParse) throws Exception {
		
		inputCefFile = fileToParse;
		if(inputCefFile == null || !inputCefFile.exists()) {
			errorMessage = "Input file not found";
			setStatus(TaskStatus.ERROR);
			return;
		}
		taskDescription = 
				"Reading features from " + inputCefFile.getName();
				
		Document cefDocument = XmlUtils.readXmlFile(inputCefFile);
		if(cefDocument == null) {
			errorMessage = "Failed to parse input file";
			setStatus(TaskStatus.ERROR);
			return;
		}
		inputFeatureList = new ArrayList<MsFeature>();
		List<Element>featureNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");
		
		total = featureNodes.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();

		for (Element cpdElement : featureNodes) {

			MsFeature feature = parseCefCompoundElement(cpdElement, conn);
			if(feature != null)
				inputFeatureList.add(feature);
			
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	protected MsFeature parseCefCompoundElement(
			Element cpdElement, Connection conn) throws Exception {
		
		Element location = cpdElement.getChild("Location");
		double rt = location.getAttribute("rt").getDoubleValue();
		double neutralMass = location.getAttribute("m").getDoubleValue();
				
		String name = "";
		Element resultsElement = cpdElement.getChild("Results");
		Element moleculeElement = null;
		if(resultsElement != null) {
			
			moleculeElement = resultsElement.getChild("Molecule");						
			if(moleculeElement != null){
				
				name = moleculeElement.getAttributeValue("name");

				// Work-around for old data
				if (name.isEmpty())
					name = moleculeElement.getAttributeValue("formula");
			}
			else{
				name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);
			}
		}
		else{
			name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
				MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
				MRC2ToolBoxConfiguration.getRtFormat().format(rt);
		}
		MsFeature feature = new MsFeature(name, rt);
		feature.setNeutralMass(neutralMass);
		if(location.getAttribute("a") != null)
			feature.setArea(location.getAttribute("a").getDoubleValue());

		if(location.getAttribute("y") != null)
			feature.setHeight(location.getAttribute("y").getDoubleValue());
		
		parseSpectra(cpdElement, feature);

		// Parse identifications
		Collection<MsFeatureIdentity>identifications = 
				parseIdentifications(feature, cpdElement, conn);
		if(!identifications.isEmpty()) {
			identifications.stream().forEach(id -> feature.addIdentity(id));
			
			for(MsFeatureIdentity id : feature.getIdentifications()) {
				
				if(id.getCompoundIdentity() == null || id.getCompoundIdentity().getPrimaryDatabaseId() == null)
					continue;
					
				String dbId = id.getCompoundIdentity().getPrimaryDatabaseId();
				if(!dbId.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())
						&& !dbId.startsWith(DataPrefix.MS_FEATURE.getName()))
					feature.setPrimaryIdentity(id);
			}		
		}
		return feature;
	}
	
	protected void parseSpectra(Element cpdElement,  MsFeature feature) throws DataConversionException {
		
		List<Element> spectrumElements = cpdElement.getChildren("Spectrum");	
		MassSpectrum spectrum = new MassSpectrum();
		TandemMassSpectrum observedMsms = null;

		for(Element spectrumElement : spectrumElements) {
			
			String spectrumType = spectrumElement.getAttributeValue("type");
			
			if(spectrumType.equals(AgilentCefFields.MS1_SPECTRUM.getName())
					|| spectrumType.equals(AgilentCefFields.FBF_SPECTRUM.getName())
					|| spectrumType.equals(AgilentCefFields.MFE_SPECTRUM.getName())) {
				
				String sign = spectrumElement.getChild("MSDetails").getAttributeValue("p");
				Polarity pol = null;
				if(sign.equals("+"))
					pol = Polarity.Positive;

				if(sign.equals("-"))
					pol = Polarity.Negative;
				
				feature.setPolarity(pol);
				
				Map<Adduct,Collection<MsPoint>>adductMap = 
						parseMsOneSpectrumElement(spectrumElement);
				adductMap.entrySet().stream().
					forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));
				
				String detectionAlgorithm = spectrumElement.getAttributeValue("cpdAlgo");
				if(detectionAlgorithm != null && !detectionAlgorithm.isEmpty())
					spectrum.setDetectionAlgorithm(detectionAlgorithm);
				
				//	spectrum.finalizeCefImportSpectrum();
			}
			if(spectrumType.equals(AgilentCefFields.MS2_SPECTRUM.getName())) {
				
				observedMsms = parseMsTwoSpectrumElement(spectrumElement);
				observedMsms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);
				spectrum.addTandemMs(observedMsms);
			}
		}
		feature.setSpectrum(spectrum);
	}
	
	protected TandemMassSpectrum parseLibMsTwoSpectrumElement(
			Element spectrumElement, Polarity polarity) throws DataConversionException {

		List<Element> precursorList = 
				spectrumElement.getChild("MzOfInterest").getChildren("mz");
		double mz = 0.0d;
		if(precursorList != null && !precursorList.isEmpty())
			mz = Double.parseDouble(precursorList.get(0).getText());
		
		TandemMassSpectrum msms = new TandemMassSpectrum(2, new MsPoint(mz, 333.0d), polarity);
		List<Element> peaks = spectrumElement.getChild("MSPeaks").getChildren("p");
		Collection<MsPoint>msmsPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		for(Element peak : peaks) {
			msmsPoints.add(new MsPoint(
					peak.getAttribute("x").getDoubleValue(),
					peak.getAttribute("y").getDoubleValue()));
		}
		msms.setSpectrum(msmsPoints);
		return msms;
	}
	
	protected TandemMassSpectrum parseMsTwoSpectrumElement(
			Element spectrumElement) throws DataConversionException{
		
		List<Element> precursorList = 
				spectrumElement.getChild("MzOfInterest").getChildren("mz");
		double mz = 0.0d;
		if(precursorList != null && !precursorList.isEmpty())
			mz = Double.parseDouble(precursorList.get(0).getText());
		
		Element msDetailsElement = spectrumElement.getChild("MSDetails");
		String sign = msDetailsElement.getAttributeValue("p");
		Polarity pol = null;
		if(sign.equals("+"))
			pol = Polarity.Positive;

		if(sign.equals("-"))
			pol = Polarity.Negative;

		TandemMassSpectrum msms = new TandemMassSpectrum(2, new MsPoint(mz, 333.0d), pol);
		String detectionAlgorithm =  spectrumElement.getAttributeValue("cpdAlgo");
		if(detectionAlgorithm != null && !detectionAlgorithm.isEmpty())
			msms.setDetectionAlgorithm(detectionAlgorithm);

		String ionisationType =  msDetailsElement.getAttributeValue("is");
		if(msDetailsElement != null && !ionisationType.isEmpty())
			msms.setIonisationType(ionisationType);
		
		String collisionEnergy = msDetailsElement.getAttributeValue("ce");
		if(collisionEnergy != null && !collisionEnergy.isEmpty()) {
			double ce = Double.parseDouble(collisionEnergy.replaceAll("V", ""));
			msms.setCidLevel(ce);
		}
		String fragVoltage = msDetailsElement.getAttributeValue("fv");
		if(fragVoltage != null && !fragVoltage.isEmpty()) {
			double fv = Double.parseDouble(fragVoltage.replaceAll("V", ""));
			msms.setFragmenterVoltage(fv);
		}
		List<Element> peaks = spectrumElement.getChild("MSPeaks").getChildren("p");
		Collection<MsPoint>msmsPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		for(Element peak : peaks) {
			msmsPoints.add(new MsPoint(
					peak.getAttribute("x").getDoubleValue(),
					peak.getAttribute("y").getDoubleValue()));
		}
		msms.setSpectrum(msmsPoints);
		return msms;
	}
	
	protected Map<Adduct,Collection<MsPoint>>parseMsOneSpectrumElement(Element spectrumElement) 
				throws DataConversionException{

		String sign = spectrumElement.getChild("MSDetails").getAttributeValue("p");
		Polarity pol = null;
		if(sign.equals("+"))
			pol = Polarity.Positive;

		if(sign.equals("-"))
			pol = Polarity.Negative;

		Map<Adduct,Collection<MsPoint>>cmMap = 
				new TreeMap<Adduct,Collection<MsPoint>>();
		List<Element> peaks = spectrumElement.getChild("MSPeaks").getChildren("p");

		//	Check if no adducts are specified
		if(peaks.get(0).getAttribute("s") == null 
				|| peaks.get(0).getAttributeValue("s").isEmpty()) {
			
			Set<MsPoint> points = new TreeSet<MsPoint>(MsUtils.mzSorter);
			for(Element peak : peaks) {
				points.add(new MsPoint(
						peak.getAttribute("x").getDoubleValue(),
						peak.getAttribute("y").getDoubleValue()));
			}
			cmMap.put(AdductManager.getDefaultAdductForPolarity(pol), points);
			return cmMap;
		}
		Map<String,Collection<MsPoint>>adductMap = 
				new TreeMap<String,Collection<MsPoint>>();
		for(Element peak : peaks) {
						
			String adduct = peak.getAttributeValue("s").replaceAll("\\+[0-9]+$", "");
			if(!adductMap.containsKey(adduct))
				adductMap.put(adduct, new TreeSet<MsPoint>(MsUtils.mzSorter));

			adductMap.get(adduct).add(new MsPoint(
					peak.getAttribute("x").getDoubleValue(),
					peak.getAttribute("y").getDoubleValue()));
		}
		for (Entry<String, Collection<MsPoint>> entry : adductMap.entrySet()) {

			Adduct adduct = AdductManager.getAdductByCefNotation(entry.getKey());
			if(adduct != null)
				cmMap.put(adduct, entry.getValue());
			else
				unmatchedAdducts.add(entry.getKey());
		}
		return cmMap;
	}
	
	protected Collection<MsFeatureIdentity>parseIdentifications(
			MsFeature feature, Element compoundElement, Connection conn){
		
		Collection<MsFeatureIdentity>identifications = 
				new ArrayList<MsFeatureIdentity>();
		if(compoundElement.getChild("Results") == null) {
			CompoundIdentity unkId = new CompoundIdentity();
			unkId.setCommonName(feature.getName());
			MsFeatureIdentity msid = 
					new MsFeatureIdentity(unkId, CompoundIdentificationConfidence.UNKNOWN_ACCURATE_MASS_RT);
			msid.setIdSource(CompoundIdSource.UNKNOWN);
			identifications.add(msid);
			return identifications;
		}		
		for(Element molElement : compoundElement.getChild("Results").getChildren("Molecule")) {
			
			MsFeatureIdentity msid = null;
			String name = molElement.getAttributeValue("name");
			String molecularFormula = molElement.getAttributeValue("formula");
			if(name == null || name.isEmpty())	{
				
				CompoundIdentity mfId = new CompoundIdentity(molecularFormula, molecularFormula);
				if(mfId.getExactMass() == 0)
					mfId.setExactMass(feature.getNeutralMass());
					
				msid = new MsFeatureIdentity(mfId, CompoundIdentificationConfidence.ACCURATE_MASS);
				msid.setIdSource(CompoundIdSource.FORMULA_GENERATOR);
			}	
			else {
				CompoundIdentity cid = new CompoundIdentity(name, molecularFormula);
				if(cid.getExactMass() == 0)
					cid.setExactMass(feature.getNeutralMass());
				
				for(Element dbElement : molElement.getChild("Database").getChildren("Accession")) {
					
					String databaseName = dbElement.getAttributeValue("db");
					String databaseId = dbElement.getAttributeValue("id");
					
					if(databaseName != null && !databaseName.isEmpty() 
							&& databaseId != null && !databaseId.isEmpty()) {		
							
//						if(databaseName.equals(AgilentDatabaseFields.CAS_ID.getName())
//								&& databaseId.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())){
//								feature.setId(databaseId);
//						}
						CompoundDatabaseEnum database = 
								LibraryUtils.parseCompoundDatabaseName(databaseName);
						if(database == null)
							continue;
						
						if(database.equals(CompoundDatabaseEnum.METLIN))
							databaseId = "METLIN:" + databaseId;

						//	TODO may change if Agilent upgrades PCDL to use new HMDB ID format
						if(database.equals(CompoundDatabaseEnum.HMDB))
							databaseId = databaseId.replace("HMDB", "HMDB00");

						cid.addDbId(database, databaseId);
					}
					msid = new MsFeatureIdentity(cid, CompoundIdentificationConfidence.ACCURATE_MASS);
					msid.setIdSource(CompoundIdSource.DATABASE);
				}
				if (!cid.getDbIdMap().isEmpty()) {

					for (String accession : cid.getDbIdMap().values()) {

						CompoundIdentity newId = null;
						try {
							newId = CompoundDatabaseUtils.getCompoundById(accession, conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (newId != null) {
							msid.setCompoundIdentity(newId);
							break;
						}
					}
				}
			}
			Element spectrumElement = molElement.getChild("Spectrum");
			if(spectrumElement != null && spectrumElement.getAttributeValue("type").equals(
					AgilentCefFields.LIBRARY_MS2_SPECTRUM.getName())) {

				TandemMassSpectrum libMsms = null;
				try {
					libMsms = parseLibMsTwoSpectrumElement(spectrumElement, feature.getPolarity());
				} catch (DataConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (libMsms != null) {
					libMsms.setSpectrumSource(SpectrumSource.LIBRARY);
					feature.getSpectrum().addTandemMs(libMsms);
					msid.setIdSource(CompoundIdSource.LIBRARY_MS2);
					msid.setConfidenceLevel(CompoundIdentificationConfidence.ACCURATE_MASS_MSMS);				
					if(molElement.getChild("MatchScores") != null) {
						
						for(Element scoreElement : molElement.getChild("MatchScores").getChildren("Match")) {
							if(scoreElement.getAttributeValue("algo").equals("lib")) {
								
								double score = 0.0d;
								try {
									score = scoreElement.getAttribute("score").getDoubleValue();
								} catch (DataConversionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								msid.setScoreCarryOver(score);
							}
						}
					}
				}			
			}
			if(msid != null)
				identifications.add(msid);
		}
		// Add ID by name if not specified in the library
		if(!feature.getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())
				&& feature.getName().indexOf('@') == -1
				&& feature.getPrimaryIdentity() == null) {

			CompoundIdentity pcId = null;
			try {
				pcId = CompoundDatabaseUtils.getCompoundIdentityByName(feature.getName(), conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( pcId != null){

				MsFeatureIdentity msId2 = 
						new MsFeatureIdentity(pcId, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
				if (feature.getPrimaryIdentity() == null)
					feature.setPrimaryIdentity(msId2);
				else{
					for(Entry<CompoundDatabaseEnum, String> entry : pcId.getDbIdMap().entrySet()){

						if(feature.getPrimaryIdentity().getCompoundIdentity().getDbId(entry.getKey()) == null)
							feature.getPrimaryIdentity().getCompoundIdentity().addDbId(entry.getKey(), entry.getValue());
					}
				}
				identifications.add(msId2);
			}
		}
		return identifications;
	}

	public Collection<MsFeature> getInputFeatureList() {
		return inputFeatureList;
	}

	public Set<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}
}
